/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class TimingsResponseDto {
  // Message Acceptance
  private TimingsDto messageAcceptanceTimings = new TimingsDto();
  private List<PercentileDto> messageAcceptanceLatencyPercentiles = List.of();

  // End to End Latency
  private TimingsDto endToEndTimings;
  private List<PercentileDto> endToEndLatencyPercentiles = List.of();

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TimingsDto {
    private Double averageLatency = 0.0;
    private long minimumLatency = 0;
    private long maximumLatency = 0;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PercentileDto {
    private Double percentile = 0.0;
    private Double latency = 0.0;
  }
}
