/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.result;

import static org.awaitility.Awaitility.await;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.NoResultFoundException;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.LatencyRequest;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import aero.sita.messaging.mercury.utilities.testharness.persistence.result.ResultsRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultsService {

  private final ResultsRepository resultsRepository;
  private final ReceivedMessagesService receivedMessagesService;

  public Result getResult(String id) {
    return resultsRepository.findById(id)
        .orElseThrow(() -> new NoResultFoundException("No result found for id = " + id));
  }

  public List<Result> getResultsByLoadProfileId(Long loadProfileId) {
    return resultsRepository.findByLoadProfileId(loadProfileId);
  }

  public Result getResultsByInjectionId(String injectionId) {
    if (!StringUtils.hasText(injectionId)) {
      throw new IllegalArgumentException("Injection ID cannot be null or blank");
    }

    List<Result> byInjectionId = resultsRepository.findByInjectionId(injectionId);

    if (byInjectionId.isEmpty()) {
      throw new NoResultFoundException("No results found for injection ID: " + injectionId);
    }

    if (byInjectionId.size() > 1) {
      throw new IllegalStateException("More than one result found for injection ID: " + injectionId);
    }

    return byInjectionId.getFirst();
  }

  public Result saveResult(Result result) {
    if (result == null) {
      throw new IllegalArgumentException("Result cannot be null");
    }
    return resultsRepository.save(result);
  }

  public int clearMessages() {
    log.info("Clearing all messages");
    int size = receivedMessagesService.filter(Optional.empty()).size();
    receivedMessagesService.deleteAll();
    log.debug("All messages cleared");
    return size;
  }

  public Result getLatency(LatencyRequest request) {
    String injectionId = request.getInjectionId();
    Result result = getResultsByInjectionId(injectionId);

    int expectedMessageCount = result.getExpectedMessageCount();

    if (expectedMessageCount > 0) {
      await()
          .atMost(Duration.ofSeconds(request.getAtMostInSeconds()))
          .pollDelay(Duration.ofSeconds(request.getPollDelayInSeconds()))
          .pollInterval(Duration.ofSeconds(request.getPollIntervalInSeconds()))
          .until(() -> receivedMessagesService.filter(Optional.of(injectionId)).size() == expectedMessageCount);
    }
    result.setActualMessageCount(receivedMessagesService.filter(Optional.of(injectionId)).size());

    Optional<Instant> timestampOfLastMessage = receivedMessagesService.getTimestampOfLastMessage(injectionId);

    if (timestampOfLastMessage.isEmpty()) {
      log.warn("No messages found for injection id: {}", injectionId);
      resultsRepository.save(result);
      return result;
    }

    Instant deliveryTimeOfLastMessage = timestampOfLastMessage.get();
    Instant processingStartedTime = result.getConnectionsStartedTimestamp();
    long latency = deliveryTimeOfLastMessage.toEpochMilli() - processingStartedTime.toEpochMilli();

    log.info("Latency for injection {}: {} ms", injectionId, latency);

    result.setElapsedTimeInMilliseconds(latency);

    resultsRepository.save(result);

    return result;
  }
}
