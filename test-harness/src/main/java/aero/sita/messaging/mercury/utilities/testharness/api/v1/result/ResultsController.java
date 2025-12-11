/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.result;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.ClearResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.LatencyRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.ResultResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.TimingsResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.AggregationResult;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.LatencyRequest;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.MessageAggregateService;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ResultsService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/results")
@RequiredArgsConstructor
@RestController
@Slf4j
@Validated
public class ResultsController {

  private final ResultsService resultsService;
  private final MessageAggregateService messageAggregateService;

  @GetMapping("/{id}")
  public ResponseEntity<ResultResponseDto> getResult(
      @PathVariable @NotNull String id) {

    Result result = resultsService.getResult(id);
    ResultResponseDto dto = ResultsMapper.INSTANCE.toDto(result);

    return ResponseEntity.ok(dto);
  }

  @GetMapping
  public ResponseEntity<List<ResultResponseDto>> getResultsByLoadProfileId(
      @NotNull @RequestParam("loadProfileId") String loadProfileId) {

    List<Result> results = resultsService.getResultsByLoadProfileId(Long.valueOf(loadProfileId));
    List<ResultResponseDto> dtos = results.stream()
        .map(ResultsMapper.INSTANCE::toDto)
        .toList();

    return ResponseEntity.ok(dtos);
  }

  @PostMapping("/clear")
  public ResponseEntity<ClearResponseDto> clearMessages() {
    int clearedMessageCount = resultsService.clearMessages();
    ClearResponseDto response = ClearResponseDto.builder()
        .numberOfMessagesCleared(clearedMessageCount)
        .build();
    return ResponseEntity.ok(response);
  }

  @PostMapping("/latency")
  public ResponseEntity<ResultResponseDto> getLatencyForInjection(
      @RequestBody LatencyRequestDto dto) {

    LatencyRequest request = ResultsMapper.INSTANCE.toDomainObject(dto);
    Result result = resultsService.getLatency(request);
    ResultResponseDto responseDto = ResultsMapper.INSTANCE.toDto(result);
    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/timings")
  public ResponseEntity<TimingsResponseDto> getTimings(
      @NotNull @RequestParam("injectionId") String injectionId) {
    AggregationResult timings = messageAggregateService.getTimings(injectionId);
    TimingsResponseDto response = TimingsMapper.toDto(timings);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/timings")
  public ResponseEntity<Void> clearTimings() {
    messageAggregateService.clear();
    return ResponseEntity.ok().build();
  }
}
