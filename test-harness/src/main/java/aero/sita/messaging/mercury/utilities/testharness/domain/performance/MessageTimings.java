/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.performance;


import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Builder
public class MessageTimings {

  @Setter
  private String correlationId;

  @Setter
  private String injectionId;

  @Setter
  private String testHarnessMessageId;

  // Raw timestamps, taken from events that are read from topics.
  private Instant customerToEndpointPublishTimestamp;
  private Instant endpointToNormalizerPublishTimestamp;
  private Instant endpointToCustomerConsumeTimestamp;

  // Calculated durations
  private long timeToEndpointInboundPublish;

  private long messageAcceptanceLatency;
  private long externalEndToEndLatency;

  // --- Setters with recalculation hooks ---

  public void setCustomerToEndpointPublishTimestamp(Instant value) {
    this.customerToEndpointPublishTimestamp = value;
    recalculateTimeToEndpointInboundPublish();
    recalculateExternalEndToEndLatency();
  }

  public void setEndpointToNormalizerPublishTimestamp(Instant value) {
    this.endpointToNormalizerPublishTimestamp = value;
    recalculateTimeToEndpointInboundPublish();
  }

  public void setEndpointToCustomerConsumeTimestamp(Instant value) {
    this.endpointToCustomerConsumeTimestamp = value;
    recalculateExternalEndToEndLatency();
  }

  // --- Recalculation methods ---

  private void recalculateTimeToEndpointInboundPublish() {
    timeToEndpointInboundPublish = calculateDuration(
        customerToEndpointPublishTimestamp,
        endpointToNormalizerPublishTimestamp);

    // MessageAcceptanceLatency is really an alias for this same value, so just set it accordingly.
    // Maybe we don't really need two member variables for this?
    messageAcceptanceLatency = timeToEndpointInboundPublish;
  }

  private void recalculateExternalEndToEndLatency() {
    externalEndToEndLatency = calculateDuration(customerToEndpointPublishTimestamp, endpointToCustomerConsumeTimestamp);
  }

  /**
   * Calculates the duration in milliseconds between two timestamps.
   *
   * @param start the start timestamp
   * @param end   the end timestamp
   * @return the duration in milliseconds, or -1 if either timestamp is null
   */
  private long calculateDuration(Instant start, Instant end) {
    if (start != null && end != null) {
      return java.time.Duration.between(start, end).toMillis();
    }
    return -1;
  }
}