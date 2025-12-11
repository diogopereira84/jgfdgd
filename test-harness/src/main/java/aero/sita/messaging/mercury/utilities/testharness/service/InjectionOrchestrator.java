/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service;

import aero.sita.messaging.mercury.libraries.sharedmodels.commands.StartAllConnectionsCommand;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileFoundException;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.OperationsClient;
import aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto.StopConnectionsRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.service.command.CommandPublisher;
import aero.sita.messaging.mercury.utilities.testharness.service.load.LoadProfileStorageService;
import aero.sita.messaging.mercury.utilities.testharness.service.message.MessagesService;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ResultsService;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InjectionOrchestrator {

  private final Map<String, SendMessageIbmMqRequest> inProgressInjectionRequests = new ConcurrentHashMap<>();

  private final LoadProfileStorageService loadProfileStorageService;
  private final MessagesService messagesService;
  private final ResultsService resultsService;

  private final CommandPublisher commandPublisher;
  private final OperationsClient operationsClient;

  public void inject(SendMessageIbmMqRequest request) {

    log.debug("Injecting messages {}", request.toString());

    // TODO: have a think about this, do the two paths here feel wrong?
    if (request.isPreLoad()) {
      injectStressLoad(request);
    } else {
      String injectionId = UUID.randomUUID().toString();
      request.setInjectionId(injectionId);
      messagesService.inject(request);
    }
  }

  private void injectStressLoad(SendMessageIbmMqRequest request) {
    log.debug("Injecting stress load {}", request.toString());

    // Record injection details
    recordInjection(request);

    // Record pre-result details
    recordPreResult(request);

    // Stop all connections
    stop();

    // The next steps don't happen until after the connections are stopped
  }

  private void recordInjection(SendMessageIbmMqRequest request) {
    String injectionId = UUID.randomUUID().toString();
    request.setInjectionId(injectionId);
    inProgressInjectionRequests.put(injectionId, request);

    log.debug("Recorded injection details for injection id: {}", injectionId);
  }

  private void recordPreResult(SendMessageIbmMqRequest request) {
    Result result = Result.builder().build();

    Long loadProfileId = request.getLoadProfileId();

    if (loadProfileId != null) {
      try {
        LoadProfile loadProfile = loadProfileStorageService.getById(loadProfileId);
        int totalCount = loadProfile.getTotalCount();
        result.setExpectedMessageCount(totalCount);
      } catch (NoLoadProfileFoundException e) {
        log.warn("No load profile found for load profile id: {}", loadProfileId);
      }

      result.setLoadProfileId(loadProfileId);
    } else {
      log.warn("No load profile id provided for request: {}", request);
    }

    result.setInjectionId(request.getInjectionId());

    resultsService.saveResult(result);

    log.debug("Recorded pre-result details : {}", result);
  }

  private void stop() {
    StopConnectionsRequestDto requestDto = StopConnectionsRequestDto.builder().build();
    operationsClient.stopConnections(requestDto);

    log.debug("Stopped all connections");
  }

  public void continueAfterStop() {
    log.debug("Attempting to continue after stopping; injecting messages...");

    Set<Map.Entry<String, SendMessageIbmMqRequest>> entries = inProgressInjectionRequests.entrySet();

    if (entries.isEmpty()) {
      log.warn("No in progress injection requests found");
      return;
    }

    for (Map.Entry<String, SendMessageIbmMqRequest> entry : entries) {
      messagesService.inject(entry.getValue());
    }

    // Start all connections
    start();
  }

  private void start() {
    log.debug("Starting all connections");
    StartAllConnectionsCommand command = new StartAllConnectionsCommand();
    commandPublisher.publish(command);
  }

  public void continueAfterStart() {
    log.debug("Attempting to continue after starting; recording results...");
    Instant startTime = Instant.now();

    for (Map.Entry<String, SendMessageIbmMqRequest> entry : inProgressInjectionRequests.entrySet()) {
      String injectionId = entry.getKey();
      Result result = resultsService.getResultsByInjectionId(injectionId);
      result.setConnectionsStartedTimestamp(startTime);
      resultsService.saveResult(result);
    }

    inProgressInjectionRequests.clear();
  }
}
