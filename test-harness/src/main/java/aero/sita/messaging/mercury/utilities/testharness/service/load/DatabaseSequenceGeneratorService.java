/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.load;

import aero.sita.messaging.mercury.utilities.testharness.domain.load.DatabaseSequence;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSequenceGeneratorService {

  private final MongoTemplate mongoTemplate;

  DatabaseSequenceGeneratorService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public long generateSequence(String seqName) {
    DatabaseSequence counter = mongoTemplate.findAndModify(
        Query.query(Criteria.where("_id").is(seqName)),
        new Update().inc("seq", 1),
        FindAndModifyOptions.options().returnNew(true).upsert(true),
        DatabaseSequence.class
    );
    return counter != null ? counter.getSeq() : 1;
  }
}
