/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.performance;

import aero.sita.messaging.mercury.utilities.testharness.domain.performance.AggregationResult;
import aero.sita.messaging.mercury.utilities.testharness.persistence.performance.MessageAggregationRepository;
import aero.sita.messaging.mercury.utilities.testharness.persistence.performance.MessageTimingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class MessageAggregateService {

  private final MessageAggregationRepository messageAggregationRepository;
  private final MessageTimingsRepository messageTimingsRepository;

  public AggregationResult getTimings(String injectionId) {
    return messageAggregationRepository.getMessageAcceptanceAggregation(injectionId);
  }

  public void clear() {
    messageTimingsRepository.deleteAll();
  }
}
