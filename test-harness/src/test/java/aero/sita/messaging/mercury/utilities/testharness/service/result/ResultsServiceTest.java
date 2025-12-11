/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.result;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.NoResultFoundException;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.LatencyRequest;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import aero.sita.messaging.mercury.utilities.testharness.persistence.result.ResultsRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResultsServiceTest {

  @Mock
  private ReceivedMessagesService receivedMessagesService;

  @Mock
  private ResultsRepository resultsRepository;

  @Captor
  private ArgumentCaptor<Result> resultCaptor;

  @InjectMocks
  private ResultsService resultsService;

  @DisplayName("""
      Given a valid request,
      And the result exists in the database,
      And initially there are 0 messages delivered,
      And subsequently there is 1 message delivered,
      When we call the getLatency method,
      Then no exception is thrown,
      And we should return a valid result,
      And it contains the correct data based on the persisted information,
      And we persist the data in the database.
      """)
  @Test
  void shouldGetLatency() {
    // Given
    String injectionId = "injection-123";
    List<ReceivedMessage> noMessages = List.of();
    List<ReceivedMessage> singleMessage = List.of(new ReceivedMessage());

    LatencyRequest request = LatencyRequest.builder()
        .pollDelayInSeconds(0)
        .atMostInSeconds(2)
        .pollIntervalInSeconds(1)
        .injectionId(injectionId)
        .build();

    when(resultsRepository.findByInjectionId(request.getInjectionId()))
        .thenReturn(List.of(Result.builder()
            .id("203")
            .expectedMessageCount(1)
            .connectionsStartedTimestamp(Instant.parse("2022-01-01T00:00:00Z"))
            .injectionId(injectionId)
            .build()));

    when(receivedMessagesService.filter(Optional.of(injectionId)))
        .thenReturn(noMessages, singleMessage);

    when(receivedMessagesService.getTimestampOfLastMessage("injection-123"))
        .thenReturn(Optional.of(Instant.parse("2022-01-01T00:00:30Z")));

    // When
    Result result = assertDoesNotThrow(() -> resultsService.getLatency(request));

    // Then
    assertNotNull(result);
    assertEquals("203", result.getId());
    assertEquals("injection-123", result.getInjectionId());
    assertEquals(30000, result.getElapsedTimeInMilliseconds());
    assertEquals(1, result.getExpectedMessageCount());

    verify(resultsRepository).save(resultCaptor.capture());
    Result value = resultCaptor.getValue();
    assertNotNull(value);
    assertEquals("203", value.getId());
    assertEquals("injection-123", value.getInjectionId());
    assertEquals(30000, value.getElapsedTimeInMilliseconds());
    assertEquals(1, value.getExpectedMessageCount());
    assertEquals(Instant.parse("2022-01-01T00:00:00Z"), value.getConnectionsStartedTimestamp());
  }

  @DisplayName("""
      Given a valid request,
      And the result exists in the database,
      And the expected message count is zero
      When we call the getLatency method,
      Then no exception is thrown,
      And we should return a valid result,
      And it contains the correct data based on the persisted information,
      And the expected message count is zero.
      """)
  @Test
  void shouldGetLatencyWhenExpectedMessageCountIsZero() {
    // Given
    LatencyRequest request = LatencyRequest.builder()
        .injectionId("injection-123")
        .build();

    when(resultsRepository.findByInjectionId(request.getInjectionId()))
        .thenReturn(List.of(Result.builder()
            .id("203")
            .expectedMessageCount(0)
            .injectionId("injection-123")
            .build()));

    // When/Then
    Result response = assertDoesNotThrow(() -> resultsService.getLatency(request));

    assertNotNull(response);
    assertEquals("203", response.getId());
    assertEquals("injection-123", response.getInjectionId());
    assertEquals(0, response.getElapsedTimeInMilliseconds());
    assertEquals(0, response.getExpectedMessageCount());
  }


  @DisplayName("""
      Given a valid request,
      And the result does not exist in the database,
      When we call the getLatency method,
      Then we should throw a NoResultFoundException.
      """)
  @Test
  void shouldThrowNoResultsFoundExceptionWhenNoResultsFound() {
    // Given
    LatencyRequest request = LatencyRequest.builder()
        .injectionId("injection-123")
        .build();

    // When/Then
    assertThrows(NoResultFoundException.class,
        () -> resultsService.getLatency(request));
  }

  @DisplayName("""
      Given a request with a null injection id,
      When we call the getLatency method,
      Then we should throw an IllegalArgumentException.
      """)
  @Test
  void shouldThrowIllegalArgumentExceptionWhenInjectionIdIsNullWhenCallingGetLatency() {
    // Given
    LatencyRequest request = LatencyRequest.builder()
        .injectionId(null)
        .build();

    // When/Then
    assertThrows(IllegalArgumentException.class,
        () -> resultsService.getLatency(request));
  }

  @DisplayName("""
      Given a result exists in the database,
      And the result has an ID of 203,
      And the result has a load profile ID of 1404,
      And the result has an elapsed time of 600 milliseconds,
      When retrieving the result by ID 203
      Then the retrieved result should have an ID of 203,
      And the retrieved result should have a load profile ID of 1404,
      And the retrieved result should have an elapsed time of 600 milliseconds.
      """)
  @Test
  void shouldRetrieveResultWhenItExistsInDatabase() {
    // Given
    when(resultsRepository.findById("203"))
        .thenReturn(Optional.ofNullable(Result.builder()
            .id("203")
            .loadProfileId(1404L)
            .elapsedTimeInMilliseconds(600)
            .build()));

    // When
    Result retrievedResult = resultsService.getResult("203");

    // Then
    assertNotNull(retrievedResult);
    assertEquals("203", retrievedResult.getId());
    assertEquals(1404L, retrievedResult.getLoadProfileId());
    assertEquals(600, retrievedResult.getElapsedTimeInMilliseconds());
  }

  @DisplayName("""
      When retrieving a result that does not exist in the database,
      Then a NoResultFoundException should be thrown
      And the message should be "No result found for id = 203."
      """)
  @Test
  void shouldThrowExceptionWhenResultDoesNotExistInDatabase() {
    // When
    NoResultFoundException noResultFoundException = assertThrows(NoResultFoundException.class,
        () -> resultsService.getResult("203"));

    // Then
    assertEquals("No result found for id = 203", noResultFoundException.getMessage());
  }

  @DisplayName("""
      When we call the resultsService.getResult method with a null ID,
      Then a NoResultFoundException should be thrown
      """)
  @Test
  void shouldHandleNullIdWhenCallingGetResult() {
    // Given
    // When
    NoResultFoundException noResultFoundException = assertThrows(NoResultFoundException.class,
        () -> resultsService.getResult(null));

    // Then
    assertEquals("No result found for id = null", noResultFoundException.getMessage());
  }

  @DisplayName("""
      When we call the resultsService.saveResult method with a null Result,
      Then an IllegalArgumentException should be thrown
      """)
  @Test
  void shouldHandleNullIdWhenCallingSaveResult() {
    // When
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> resultsService.saveResult(null));

    // Then
    assertEquals("Result cannot be null", exception.getMessage());
  }

  @DisplayName("""
      Given a valid result object,
      And the result has an ID of 204,
      And the result has a load profile ID of 1405,
      And the result has an elapsed time of 700 milliseconds,
      When saving the result,
      Then the saved result should not be null,
      And the saved result should have an ID of 204,
      And the saved result should have a load profile ID of 1405,
      And the saved result should have an elapsed time of 700 milliseconds,
      And the resultsRepository.save method should be called with the result object.
      """)
  @Test
  void shouldSuccessfullySaveValidResult() {
    // Given
    Result result = Result.builder()
        .id("204")
        .loadProfileId(1405L)
        .elapsedTimeInMilliseconds(700)
        .build();

    when(resultsRepository.save(result))
        .thenReturn(result);

    // When
    Result response = resultsService.saveResult(result);

    // Then
    assertNotNull(response);
    assertEquals("204", response.getId());
    assertEquals(1405L, response.getLoadProfileId());
    assertEquals(700, response.getElapsedTimeInMilliseconds());
    verify(resultsRepository).save(result);
  }

  @DisplayName("""
      When we call the resultsService.getResultsByLoadProfileId method with a null load profile ID,
      Then it should return an empty list of results.
      """)
  @Test
  void shouldReturnEmptyListWhenLoadProfileIdIsNull() {
    // When
    List<Result> results = resultsService.getResultsByLoadProfileId(null);

    // Then
    assertNotNull(results);
    assertEquals(0, results.size());
  }

  @DisplayName("""
      When we call the resultsService.getResultsByLoadProfileId method with a load profile ID that does not exist in the repository,
      Then it should return an empty list of results.
      """)
  @Test
  void shouldReturnEmptyListWhenLoadProfileIdIsNotInRepository() {
    // When
    List<Result> results = resultsService.getResultsByLoadProfileId(10L);

    // Then
    assertNotNull(results);
    assertEquals(0, results.size());
  }

  @DisplayName("""
      Given a valid Result object,
      And the Result has a load profile ID of 1406,
      And the Result has an ID of 205,
      And the Result has an elapsed time of 800 milliseconds,
      And the repository is mocked to return a list containing that Result object,
      When we call the resultsService.getResultsByLoadProfileId method with that load profile ID,
      Then it should return a list containing that Result object,
      And the Result object in the list should have the same ID, load profile ID, and elapsed time as the original Result object.
      """)
  @Test
  void shouldReturnMatchWhenLoadProfileIdExists() {
    // Given
    Result result = Result.builder()
        .id("205")
        .loadProfileId(1406L)
        .elapsedTimeInMilliseconds(800)
        .build();

    when(resultsRepository.findByLoadProfileId(1406L))
        .thenReturn(List.of(result));

    // When
    List<Result> results = resultsService.getResultsByLoadProfileId(1406L);

    // Then
    assertNotNull(results);
    assertEquals(1, results.size());
    assertEquals("205", results.getFirst().getId());
    verify(resultsRepository).findByLoadProfileId(1406L);
  }

  @DisplayName("""
      When we call the resultsService.getResultsByInjectionId method with a null injection ID,
      Then it should throw an IllegalArgumentException.
      """)
  @Test
  void shouldThrowIllegalArgumentExceptionWhenInjectionIdIsNull() {
    assertThrows(IllegalArgumentException.class, () -> resultsService.getResultsByInjectionId(null));
  }

  @DisplayName("""
      Given a valid Result object,
      And the Result has an injection ID of "injection-123",
      And the Result has an ID of "206",
      And the repository is mocked to return a list containing that Result object,
      When we call the resultsService.getResultsByInjectionId method with that injection ID,
      Then it should return that Result object,
      And the Result object should have the same ID and injection ID as the original Result object.
      """)
  @Test
  void shouldReturnMatchWhenInjectionIdExists() {
    // Given
    Result result = Result.builder()
        .id("206")
        .injectionId("injection-123")
        .build();

    when(resultsRepository.findByInjectionId("injection-123"))
        .thenReturn(List.of(result));

    Result serviceResponse = resultsService.getResultsByInjectionId("injection-123");

    assertNotNull(serviceResponse);
    assertEquals("206", serviceResponse.getId());
    assertEquals("injection-123", serviceResponse.getInjectionId());
    verify(resultsRepository).findByInjectionId("injection-123");
  }

  @DisplayName("""
      When we call the resultsService.getResultsByInjectionId method with a null injection ID,
      Then it should throw an IllegalArgumentException with the message "Injection ID cannot be null or blank".
      """)
  @Test
  void shouldReturnIllegalArgumentExceptionWhenInjectionIdIsNullWhenCallGetResultsByInjectionId() {
    // When
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> resultsService.getResultsByInjectionId(null));

    // Then
    assertEquals("Injection ID cannot be null or blank", exception.getMessage());
  }

  @DisplayName("""
      When we call the resultsService.getResultsByInjectionId method with a blank injection ID,
      Then it should throw an IllegalArgumentException with the message "Injection ID cannot be null or blank".
      """)
  @Test
  void shouldReturnIllegalArgumentExceptionWhenInjectionIdIsBlankWhenCallGetResultsByInjectionId() {
    // When
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> resultsService.getResultsByInjectionId(""));

    // Then
    assertEquals("Injection ID cannot be null or blank", exception.getMessage());
  }

  @DisplayName("""
      Given the repository returns an empty list for a specific injection ID,
      When we call the resultsService.getResultsByInjectionId method with that injection ID,
      Then it should throw a NoResultFoundException with the message "No results found for injection ID: injection-123".
      """)
  @Test
  void shouldReturnNoResultFoundExceptionWhenInjectionIdDoesNotExist() {
    // Given
    when(resultsRepository.findByInjectionId("injection-123"))
        .thenReturn(List.of());

    // When
    NoResultFoundException noResultFoundException = assertThrows(NoResultFoundException.class,
        () -> resultsService.getResultsByInjectionId("injection-123"));

    // Then
    assertEquals("No results found for injection ID: injection-123", noResultFoundException.getMessage());
  }

  @DisplayName("""
        Given the repository returns multiple results for a specific injection ID,
        When we call the resultsService.getResultsByInjectionId method with that injection ID,
        Then it should throw an IllegalStateException with the message "More than one result found for injection ID: injection-123".
      """)
  @Test
  void shouldReturnIllegalStateExceptionWhenMultipleResultsFoundForInjectionId() {
    // Given
    Result result1 = Result.builder().id("207").injectionId("injection-123").build();
    Result result2 = Result.builder().id("208").injectionId("injection-123").build();
    when(resultsRepository.findByInjectionId("injection-123"))
        .thenReturn(List.of(result1, result2));

    // When
    IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
        () -> resultsService.getResultsByInjectionId("injection-123"));

    // Then
    assertEquals("More than one result found for injection ID: injection-123", illegalStateException.getMessage());
  }
}