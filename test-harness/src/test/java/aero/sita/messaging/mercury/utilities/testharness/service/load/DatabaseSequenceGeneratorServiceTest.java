/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.load;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import aero.sita.messaging.mercury.utilities.testharness.domain.load.DatabaseSequence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

public class DatabaseSequenceGeneratorServiceTest {
  @Test
  @DisplayName("""
      Given the DatabaseSequenceGeneratorService is available
      When the service is called
      Then the MongoTemplate findAndModifyMethod should be called
      """)
  public void verifyFindAndModifyIsCalledWhenDatabaseSequenceGeneratorServiceIsCalled() {
    MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    DatabaseSequenceGeneratorService service = new DatabaseSequenceGeneratorService(mongoTemplate);
    service.generateSequence("test");
    verify(mongoTemplate, times(1)).findAndModify(any(), any(), any(), eq(DatabaseSequence.class));
  }

}
