/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.persistence.performance;

import aero.sita.messaging.mercury.utilities.testharness.domain.performance.AggregationResult;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimingsDocument;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.PartialAggregationResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageAggregationRepositoryImpl implements MessageAggregationRepository {
  private final MongoTemplate mongoTemplate;

  private static final String AVG_MESSAGE_ACCEPTANCE_LATENCY = "averageMessageAcceptanceLatency";
  private static final String MAX_MESSAGE_ACCEPTANCE_LATENCY = "maximumMessageAcceptanceLatency";
  private static final String MIN_MESSAGE_ACCEPTANCE_LATENCY = "minimumMessageAcceptanceLatency";

  private static final String AVG_EXTERNAL_END_TO_END_LATENCY = "averageEndToEndLatency";
  private static final String MAX_EXTERNAL_END_TO_END_LATENCY = "maximumEndToEndLatency";
  private static final String MIN_EXTERNAL_END_TO_END_LATENCY = "minimumEndToEndLatency";

  private static final String MESSAGE_ACCEPTANCE_LATENCY = "$messageAcceptanceLatency";
  private static final String EXTERNAL_END_TO_END_LATENCY = "$externalEndToEndLatency";

  private static final double[] PERCENTILES = new double[] {0.1, 0.25, 0.5, 0.75, 0.9, 0.95, 0.99};

  @Override
  public AggregationResult getMessageAcceptanceAggregation(String injectionId) {
    // 1. Filter by injectionId
    AggregationOperation byInjectionId = Aggregation.match(
        Criteria.where("InjectionId").is(injectionId)
    );

    // 2. Build up an AggregationOperation to capture the average/max/min latencies
    AggregationOperation aggregateGroup = context ->
        new Document("$group",
            new Document("_id", null)
                .append(AVG_MESSAGE_ACCEPTANCE_LATENCY,
                    new Document("$avg", MESSAGE_ACCEPTANCE_LATENCY))
                .append(MAX_MESSAGE_ACCEPTANCE_LATENCY,
                    new Document("$max", MESSAGE_ACCEPTANCE_LATENCY))
                .append(MIN_MESSAGE_ACCEPTANCE_LATENCY,
                    new Document("$min", MESSAGE_ACCEPTANCE_LATENCY))
                .append(AVG_EXTERNAL_END_TO_END_LATENCY,
                    new Document("$avg", EXTERNAL_END_TO_END_LATENCY))
                .append(MAX_EXTERNAL_END_TO_END_LATENCY,
                    new Document("$max", EXTERNAL_END_TO_END_LATENCY))
                .append(MIN_EXTERNAL_END_TO_END_LATENCY,
                    new Document("$min", EXTERNAL_END_TO_END_LATENCY))
        );

    // 3. Build the mongo "Aggregation" for the filter and group
    Aggregation aggregation = Aggregation.newAggregation(
        byInjectionId,
        aggregateGroup);

    // 4. Execute the aggregation and return the raw results (not including the percentiles)
    AggregationResults<PartialAggregationResult> results = mongoTemplate.aggregate(
        aggregation,
        MessageTimingsDocument.class,
        PartialAggregationResult.class);

    PartialAggregationResult partialAggregationResult = results.getUniqueMappedResult();

    // 5. Fallback if no documents were found
    if (partialAggregationResult == null) {
      return new AggregationResult(0.0,
          0L,
          0L,
          List.of(),
          0.0,
          0L,
          0L,
          List.of());
    }

    // 6.  Fetch raw latency values for percentile computation
    Query valuesQuery = Query.query(Criteria.where("InjectionId").is(injectionId));
    valuesQuery.fields()
        .include("messageAcceptanceLatency")
        .include("externalEndToEndLatency");

    List<MessageTimingsDocument> docs = mongoTemplate.find(valuesQuery, MessageTimingsDocument.class);

    // 7. Create a holder list for the acceptance raw values
    List<Long> acceptanceValues = new ArrayList<>(docs.size());
    List<Long> e2eValues = new ArrayList<>(docs.size());

    for (MessageTimingsDocument d : docs) {
      Long acc = d.getMessageAcceptanceLatency();
      Long e2e = d.getExternalEndToEndLatency();
      acceptanceValues.add(acc);
      e2eValues.add(e2e);
    }

    // 8. Now that we have the raw values, compute the percentiles
    List<Double> acceptancePercentiles = computePercentiles(acceptanceValues);
    List<Double> e2ePercentiles = computePercentiles(e2eValues);

    return new AggregationResult(
        partialAggregationResult.averageMessageAcceptanceLatency(),
        partialAggregationResult.maximumMessageAcceptanceLatency(),
        partialAggregationResult.minimumMessageAcceptanceLatency(),
        acceptancePercentiles,
        partialAggregationResult.averageEndToEndLatency(),
        partialAggregationResult.maximumEndToEndLatency(),
        partialAggregationResult.minimumEndToEndLatency(),
        e2ePercentiles
    );
  }

  // Linear interpolation percentile calculation
  private static List<Double> computePercentiles(List<Long> values) {
    // 1. Defensive code
    if (values == null || values.isEmpty()) {
      return List.of();
    }

    // 2. Sort the values before doing any calculations
    List<Long> sorted = new ArrayList<>(values);
    Collections.sort(sorted);

    // 3. Create a holder for computed percentiles
    int sortedSize = sorted.size();
    List<Double> computedPercentiles = new ArrayList<>(MessageAggregationRepositoryImpl.PERCENTILES.length);

    // 4. Loop around all defined percentiles (0.1, 0.25, etc.)
    for (double percentile : MessageAggregationRepositoryImpl.PERCENTILES) {

      // 5. Adjust the percentile value to the range of 0.0 to 1.0. This is a purely defensive measure.
      double clampedPercentile = Math.max(0.0, Math.min(1.0, percentile));

      // 6. If we only have 1 single value, then add it and continue to the next defined percentile.
      if (sortedSize == 1) {
        computedPercentiles.add(sorted.getFirst().doubleValue());
        continue;
      }

      // 7. Compute fractional index; this is the precise position where the percentile should be located. Essentially maps
      // the percentile (0.0 to 1.0) to the arrays index position (0 to sortedSize -1)
      double fractionalIndex = clampedPercentile * (sortedSize - 1);

      // 8. Calculate the index positions before and after the fractional index.
      int indexBefore = (int) Math.floor(fractionalIndex);
      int indexAfter = (int) Math.ceil(fractionalIndex);

      if (indexBefore == indexAfter) {
        // 9. If they are the same, then we can just use the value at the index position.
        computedPercentiles.add(sorted.get(indexBefore).doubleValue());
      } else {
        // 10. Otherwise, linearly interpolates between the two values. Essentially this means; start at the lower value,
        // then take a proportional step toward the upper value based on how far between the indices you need to be.
        double indexPositionFraction = fractionalIndex - indexBefore;
        double calculatedPercentile = sorted.get(indexBefore) + indexPositionFraction * (sorted.get(indexAfter) - sorted.get(indexBefore));
        computedPercentiles.add(calculatedPercentile);
      }
    }
    return computedPercentiles;
  }
}