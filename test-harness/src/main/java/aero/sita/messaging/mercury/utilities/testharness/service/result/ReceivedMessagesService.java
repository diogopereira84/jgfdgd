/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.result;

import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimings;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.persistence.result.ReceivedMessageRepository;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.MessageTimingsService;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ReceivedMessagesService {

  private final ReceivedMessageRepository repository;
  private final MessageTimingsService messageTimingsService;

  public ReceivedMessage save(ReceivedMessage message) {
    log.debug("Saving received message: {}", message);

    Optional<MessageTimings> byInjectionId = messageTimingsService.findByTestHarnessMessageId(message.getMessageId());

    if (byInjectionId.isPresent()) {
      MessageTimings timings = byInjectionId.get();
      timings.setEndpointToCustomerConsumeTimestamp(message.getHandOffTimestamp());
      messageTimingsService.save(timings);
    }

    return repository.save(message);
  }

  public void deleteAll() {
    log.debug("Deleting all received messages");
    repository.deleteAll();
    log.debug("All received messages deleted");
  }

  public List<ReceivedMessage> filter(Optional<String> injectionId) {
    return repository.findByInjectionId(injectionId.orElse(null));
  }

  public List<ReceivedMessage> filter(Optional<String> connectionName, Optional<String> queueName) {
    return repository.findByConnectionNameAndQueueName(
        connectionName.orElse(null),
        queueName.orElse(null));
  }

  public Optional<Instant> getTimestampOfLastMessage(String injectionId) {
    List<ReceivedMessage> result = repository.findByInjectionId(injectionId);
    if (result == null || result.isEmpty()) {
      return Optional.empty();
    }

    return result.stream()
        .map(ReceivedMessage::getHandOffTimestamp)
        .filter(Objects::nonNull)
        .max(Comparator.naturalOrder());
  }
}
