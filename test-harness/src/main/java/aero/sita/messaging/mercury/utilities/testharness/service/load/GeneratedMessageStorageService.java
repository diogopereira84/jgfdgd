/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.load;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoGeneratedMessagesFoundException;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.GeneratedMessage;
import aero.sita.messaging.mercury.utilities.testharness.persistence.load.GeneratedMessageRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeneratedMessageStorageService {

  private final GeneratedMessageRepository generatedMessageRepository;

  GeneratedMessageStorageService(GeneratedMessageRepository generatedMessageRepository) {
    this.generatedMessageRepository = generatedMessageRepository;
  }

  public GeneratedMessage save(GeneratedMessage generatedMessage) {
    return generatedMessageRepository.save(generatedMessage);
  }

  public GeneratedMessage get(String generatedMessageId) {
    return generatedMessageRepository.findById(generatedMessageId).orElseThrow(
        () -> new NoGeneratedMessagesFoundException(generatedMessageId));
  }

  public List<GeneratedMessage> getByLoadProfileId(Long loadProfileId) {
    List<GeneratedMessage> results = generatedMessageRepository.findGeneratedMessagesByLoadProfileId(loadProfileId);
    if (results.isEmpty()) {
      throw new NoGeneratedMessagesFoundException(loadProfileId);
    }
    return results;
  }

  public void delete(String generatedMessageId) {
    get(generatedMessageId);
    generatedMessageRepository.deleteById(generatedMessageId);
  }

  public void deleteGeneratedMessagesForLoadProfile(Long loadProfileId) {
    getByLoadProfileId(loadProfileId);
    generatedMessageRepository.deleteGeneratedMessagesByLoadProfileId(loadProfileId);
  }
}
