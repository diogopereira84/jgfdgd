/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.result;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.TimingsResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.AggregationResult;
import java.util.ArrayList;
import java.util.List;

public class TimingsMapper {

  private TimingsMapper() {
  }

  public static TimingsResponseDto toDto(AggregationResult timings) {
    TimingsResponseDto.TimingsDto messageAcceptanceTimingsDto = new TimingsResponseDto.TimingsDto();
    messageAcceptanceTimingsDto.setAverageLatency(timings.averageMessageAcceptanceLatency());
    messageAcceptanceTimingsDto.setMaximumLatency(timings.maximumMessageAcceptanceLatency());
    messageAcceptanceTimingsDto.setMinimumLatency(timings.minimumMessageAcceptanceLatency());

    TimingsResponseDto.TimingsDto endToEndTimingsDto = new TimingsResponseDto.TimingsDto();
    endToEndTimingsDto.setAverageLatency(timings.averageEndToEndLatency());
    endToEndTimingsDto.setMaximumLatency(timings.maximumEndToEndLatency());
    endToEndTimingsDto.setMinimumLatency(timings.minimumEndToEndLatency());

    TimingsResponseDto responseDto = new TimingsResponseDto();
    responseDto.setMessageAcceptanceTimings(messageAcceptanceTimingsDto);
    responseDto.setMessageAcceptanceLatencyPercentiles(TimingsMapper.toPercentileDtoList(timings.messageAcceptanceLatencyPercentiles()));
    responseDto.setEndToEndTimings(endToEndTimingsDto);
    responseDto.setEndToEndLatencyPercentiles(TimingsMapper.toPercentileDtoList(timings.externalEndToEndLatencyPercentiles()));

    return responseDto;
  }

  public static List<TimingsResponseDto.PercentileDto> toPercentileDtoList(List<Double> percentiles) {

    if (percentiles == null || percentiles.isEmpty()) {
      return new ArrayList<>();
    }

    return List.of(
        new TimingsResponseDto.PercentileDto(0.1, percentiles.getFirst()),
        new TimingsResponseDto.PercentileDto(0.25, percentiles.get(1)),
        new TimingsResponseDto.PercentileDto(0.5, percentiles.get(2)),
        new TimingsResponseDto.PercentileDto(0.75, percentiles.get(3)),
        new TimingsResponseDto.PercentileDto(0.9, percentiles.get(4)),
        new TimingsResponseDto.PercentileDto(0.95, percentiles.get(5)),
        new TimingsResponseDto.PercentileDto(0.99, percentiles.get(6))
    );
  }
}
