/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.performance;

import static aero.sita.messaging.mercury.libraries.sharedmodels.common.EventHeaders.TEST_HARNESS_MESSAGE_ID;

import aero.sita.messaging.mercury.libraries.common.messaging.Message;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventHeaderDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.MessagePayloadDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.StatusDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimings;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
@Slf4j
public class IncomingEventService {

  private final MessageTimingsService messageTimingsService;

  @Retryable(
      maxAttemptsExpression = "10",
      backoff = @Backoff(delayExpression = "1000")
  )
  public void captureMessageTimings(@Valid Message<IncomingEventDto> message) {
    if (!shouldHandle(message)) {
      log.debug("Message does not meet the required criteria, not tracking times: {}", message);
      return;
    }

    IncomingEventHeaderDto header = message.getPayload().getHeader();
    StatusDto status = header.getStatus();

    switch (status) {
      case RECEIVED -> handleReceivedStatus(message);
      case PARSED -> log.info("Parse Messages will be handled in a later phase...");
      default -> log.info("Unsupported status: {}", status);
    }
  }

  private boolean shouldHandle(Message<IncomingEventDto> message) {
    if (message == null) {
      log.warn("Message is null");
      return false;
    }

    IncomingEventDto event = message.getPayload();
    if (event == null) {
      log.warn("Payload is null for message: {}", message);
      return false;
    }

    // We should not do anything if the required metadata is not available.
    Map<String, String> metadata = event.getMetadata();
    if (metadata == null || metadata.isEmpty()) {
      log.debug("Metadata is null for message: {}", message);
      return false;
    }

    String testHarnessMessageId = metadata.get(TEST_HARNESS_MESSAGE_ID);
    if (!StringUtils.hasText(testHarnessMessageId)) {
      log.debug("Unable to extract testHarnessMessageId from message : {}", message);
      return false;
    }

    return true;
  }

  private void handleReceivedStatus(Message<IncomingEventDto> message) {
    log.info("Saving message timings for RECEIVED message: {}", message);

    // Retrieve by testHarnessMessage ID...
    IncomingEventDto event = message.getPayload();
    MessagePayloadDto payload = event.getPayload();

    if (payload == null) {
      log.warn("Payload is null for message, unable to calculate timings: {}", message);
      return;
    }

    String rawData = payload.getRawData();
    if (!StringUtils.hasText(rawData)) {
      log.warn("Raw data is null for message, unable to calculate timings: {}", message);
      return;
    }

    Map<String, String> metadata = event.getMetadata();
    String testHarnessMessageId = metadata.get(TEST_HARNESS_MESSAGE_ID);

    Optional<MessageTimings> messageTimingsOptional = messageTimingsService
        .findByTestHarnessMessageId(testHarnessMessageId);
    if (messageTimingsOptional.isEmpty()) {
      log.warn("No message timings found for testHarnessMessageId: {}", testHarnessMessageId);
    }

    IncomingEventHeaderDto header = event.getHeader();
    UUID correlationId = header.getCorrelationId();

    MessageTimings messageTimings = messageTimingsOptional.get();
    messageTimings.setCorrelationId(correlationId.toString());
    messageTimings.setEndpointToNormalizerPublishTimestamp(message.getProducedTimestamp());
    messageTimingsService.save(messageTimings);
  }

  @Recover
  private void recoverFromMessageTimingsNotFound(
      MessageTimingsNotFoundException exception,
      Message<IncomingEventDto> message) {
    log.debug("Unable to find message timings after retries for message: {}. Error: {}",
        message, exception.getMessage());
  }

  public static class MessageTimingsNotFoundException extends RuntimeException {

    public MessageTimingsNotFoundException(String message) {
      super(message);
    }
  }

}
