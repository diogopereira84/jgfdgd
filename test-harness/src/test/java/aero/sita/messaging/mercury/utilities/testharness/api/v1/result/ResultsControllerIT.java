/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.result;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.NoResultFoundException;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.LatencyRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.ResultResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.TimingsResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.AggregationResult;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.MessageAggregateService;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ResultsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ResultsController.class)
public class ResultsControllerIT {

  private static final String RESULTS = "/api/v1/results";
  public static final String RESULTS_CLEAR = "/api/v1/results/clear";
  public static final String RESULTS_LATENCY = "/api/v1/results/latency";
  public static final String RESULTS_TIMINGS = "/api/v1/results/timings";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ResultsService resultsService;

  @MockitoBean
  private MessageAggregateService messageAggregateService;

  @DisplayName("""
      Given the results service is configured to return a mocked domain result,
      And the result has an ID of 123,
      And the result has a load profile ID of 2403,
      And the result has an elapsed time of 600 milliseconds,
      When a GET request is made to /api/v1/results/123,
      Then the response should be a 200 OK status,
      And the response body should be correctly mapped to a v1 ResultResponseDto,
      And the ResultResponseDto should contain the ID 123,
      And the ResultResponseDto should contain the load profile ID 2403,
      And the ResultResponseDto should contain the elapsed time of 600 milliseconds.
      """)
  @Test
  void shouldRetrieveCorrectlyMappedResultDto() throws Exception {

    // Given
    Result result = Result.builder()
        .loadProfileId(2403L)
        .id("123")
        .elapsedTimeInMilliseconds(600)
        .build();

    result.setId("123");
    result.setLoadProfileId(2403L);
    result.setElapsedTimeInMilliseconds(600);

    when(resultsService.getResult("123"))
        .thenReturn(result);

    // When
    // Then
    mockMvc.perform(get(RESULTS + "/123"))
        .andExpect(status().isOk())
        .andExpect(response -> {
          ResultResponseDto responseResult = new ObjectMapper().readValue(
              response.getResponse().getContentAsString(), ResultResponseDto.class);
          assertEquals("123", responseResult.getId());
          assertEquals(2403L, responseResult.getLoadProfileId());
          assertEquals(600, responseResult.getElapsedTimeInMilliseconds());
        });
  }

  @DisplayName("""
      Given the results service is configured to throw a NoResultFoundException for a non-existent result ID,
      When a GET request is made to /api/v1/results/999,
      Then the response should be a 404 Not Found status.
      """)
  @Test
  void shouldReturnNotFoundForNonExistentResult() throws Exception {

    // Given
    when(resultsService.getResult("999"))
        .thenThrow(new NoResultFoundException("999"));

    // When
    // Then
    mockMvc.perform(get(RESULTS + "/999"))
        .andExpect(status().isNotFound());
  }

  @DisplayName("""
      Given the results service is mocked to clear 50 messages,
      When a POST request is made to /api/v1/results/clear,
      Then the response should be a 200 OK status,
      And the response body should contain a numberOfMessagesCleared field with a value of 50.
      """)
  @Test
  void shouldClearMessages() throws Exception {
    // Given
    int clearedMessageCount = 50;
    when(resultsService.clearMessages()).thenReturn(clearedMessageCount);

    // When
    // Then
    mockMvc.perform(post(RESULTS_CLEAR))
        .andExpect(status().isOk())
        .andExpect(response -> {
          String responseBody = response.getResponse().getContentAsString();
          assertEquals("{\"numberOfMessagesCleared\":50}", responseBody);
        });
  }

  @DisplayName("""
      Given the results service is configured to return a Result for a specific injection ID,
      And the Result has an ID of 123,
      And the Result has a load profile ID of 2403,
      And the Result has an elapsed time of 600 milliseconds,
      When a GET request is made to /api/v1/results/latency?injectionId=latency-id1,
      Then the response should be a 200 OK status,
      And the response body should be correctly mapped to a ResultResponseDto,
      And the ResultResponseDto should contain the ID 123,
      And the ResultResponseDto should contain the load profile ID 2403,
      And the ResultResponseDto should contain the elapsed time of 600 milliseconds.
      """)
  @Test
  void shouldReturnLatencyForInjection() throws Exception {
    // Given
    Result result = Result.builder()
        .id("123")
        .loadProfileId(2403L)
        .elapsedTimeInMilliseconds(600)
        .build();

    when(resultsService.getLatency(any()))
        .thenReturn(result);

    // When
    // Then

    LatencyRequestDto dto = new LatencyRequestDto();
    dto.setInjectionId("latency-id1");
    ObjectMapper objectMapper = new ObjectMapper();
    String requestBody = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post(RESULTS_LATENCY)
            .contentType("application/json")
            .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(response -> {
          ResultResponseDto responseResult = new ObjectMapper().readValue(
              response.getResponse().getContentAsString(), ResultResponseDto.class);
          assertEquals("123", responseResult.getId());
          assertEquals(2403L, responseResult.getLoadProfileId());
          assertEquals(600, responseResult.getElapsedTimeInMilliseconds());
        });
  }

  @DisplayName("""
        Given the results service is setup to return a valid response,
        When a GET request is made to /api/v1/results?loadProfileId=2403,
        Then the response should be a 200 OK status,
        And the response body should be correctly mapped to a list of ResultResponseDtos.
      """)
  @Test
  void shouldGetResultsByLoadProfileId() throws Exception {
    // Given
    String loadProfileId = "135";
    when(resultsService.getResultsByLoadProfileId(Long.valueOf(loadProfileId)))
        .thenReturn(List.of(
            Result.builder()
                .actualMessageCount(10)
                .connectionsStartedTimestamp(Instant.parse("2021-01-01T00:00:00Z"))
                .expectedMessageCount(15)
                .loadProfileId(Long.valueOf(loadProfileId))
                .build()));

    // When / Then
    mockMvc.perform(get(RESULTS)
            .param("loadProfileId", loadProfileId))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(response -> {
          String json = response.getResponse().getContentAsString();
          ObjectMapper mapper = new ObjectMapper();
          List<ResultResponseDto> responseResult = mapper.readValue(
              json,
              new com.fasterxml.jackson.core.type.TypeReference<>() {
              }
          );
          assertNotNull(responseResult);
          assertEquals(1, responseResult.size());

          ResultResponseDto first = responseResult.getFirst();
          assertNotNull(first);
          assertEquals(10, first.getActualMessageCount());
          assertEquals(135L, first.getLoadProfileId());
        });

    verify(resultsService)
        .getResultsByLoadProfileId(Long.valueOf(loadProfileId));
  }


  @Test
  @DisplayName("""
      When we call the delete for timings,
      Then we invoke clear on the message aggregate service.
      """
  )
  void shouldClearTimings() throws Exception {
    // When
    // Then
    mockMvc.perform(delete(RESULTS_TIMINGS))
        .andExpect(status().isOk());

    verify(messageAggregateService).clear();
  }

  @DisplayName("""
      Given a valid injection ID,
      When we pass that into the timings endpoint,
      Then we should get back the correct results.
      """)
  @Test
  void shouldGetTimingsByInjectionId() throws Exception {
    // Given
    String injectionId = "ABC";
    when(messageAggregateService.getTimings(injectionId))
        .thenReturn(new AggregationResult(12.0, 20L, 8L, List.of(), 7.5, 31L, 22L, List.of()));

    // When
    // Then
    mockMvc.perform(get(RESULTS_TIMINGS)
            .param("injectionId", injectionId))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(response -> {
          String json = response.getResponse().getContentAsString();
          ObjectMapper mapper = new ObjectMapper();
          TimingsResponseDto responseDto = mapper.readValue(json, TimingsResponseDto.class);
          assertNotNull(responseDto);
          TimingsResponseDto.TimingsDto messageAcceptanceTimings = responseDto.getMessageAcceptanceTimings();
          assertNotNull(messageAcceptanceTimings);
          assertEquals(12.0, messageAcceptanceTimings.getAverageLatency());
          assertEquals(8, messageAcceptanceTimings.getMinimumLatency());
          assertEquals(20, messageAcceptanceTimings.getMaximumLatency());
        });
  }
}