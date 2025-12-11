/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.performance;

import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimings;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimingsDocument;
import aero.sita.messaging.mercury.utilities.testharness.persistence.performance.MessageTimingsRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageTimingsService {

  private final MessageTimingsRepository repository;

  public void save(MessageTimings timings) {
    MessageTimingsDocument documentObject = MessageTimingsMapper.INSTANCE.toDocumentObject(timings);
    repository.save(documentObject);
  }

  public Optional<MessageTimings> findByTestHarnessMessageId(
      String messageId) {

    return repository
        .findByTestHarnessMessageId(messageId)
        .map(MessageTimingsMapper.INSTANCE::toDomainObject);
  }
}
