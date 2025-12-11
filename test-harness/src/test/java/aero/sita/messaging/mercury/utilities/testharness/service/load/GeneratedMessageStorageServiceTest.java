/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.load;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoGeneratedMessagesFoundException;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.GeneratedMessage;
import aero.sita.messaging.mercury.utilities.testharness.persistence.load.GeneratedMessageRepository;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.GeneratedMessageFixtureFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GeneratedMessageStorageServiceTest {
  @Mock
  GeneratedMessageRepository generatedMessageRepository;

  @InjectMocks
  private GeneratedMessageStorageService generatedMessageStorageService;

  @Test
  @DisplayName("""
      Given that a valid GeneratedMessage is stored
       When the GeneratedMessage id is provided in the service getById() call
       Then the valid GeneratedMessage is returned
      """)
  void shouldReturnGeneratedMessageWhenExists() {
    GeneratedMessage generatedMessage = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder().build();

    when(generatedMessageRepository.findById(generatedMessage.getId())).thenReturn(Optional.of(generatedMessage));

    GeneratedMessage result = generatedMessageStorageService.get(generatedMessage.getId());

    assertEquals(result.getMessage(), generatedMessage.getMessage());
    assertEquals(result.getLoadProfileId(), generatedMessage.getLoadProfileId());
  }

  @Test
  @DisplayName("""
      Given a GeneratedMessage id
       When the GeneratedMessage id is not found
       Then a NoGeneratedMessagesFoundException is thrown
      """)
  void shouldThrowNoGeneratedMessagesFoundExceptionWhenIdDoesNotExist() {
    final String generatedMessageId = "685fff61c4a13926b5c261e0";

    when(generatedMessageRepository.findById(generatedMessageId)).thenReturn(Optional.empty());

    assertThrows(NoGeneratedMessagesFoundException.class, () -> generatedMessageStorageService.get(generatedMessageId));
  }

  @Test
  @DisplayName("""
      Given that a valid GeneratedMessage is stored
       When the LoadProfile id associated with the GeneratedMessage is provided in the fi
       Then the valid GeneratedMessage is returned
      """)
  void shouldReturnGeneratedMessageWhenFindByLoadProfileIdIsCalled() {
    GeneratedMessage generatedMessage = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder().build();

    when(generatedMessageRepository.findGeneratedMessagesByLoadProfileId(generatedMessage.getLoadProfileId()))
        .thenReturn(List.of(generatedMessage));
    List<GeneratedMessage> result = generatedMessageStorageService.getByLoadProfileId(generatedMessage.getLoadProfileId());

    assertEquals(1, result.size());
    assertEquals(result.getLast().getMessage(), generatedMessage.getMessage());
    assertEquals(result.getLast().getLoadProfileId(), generatedMessage.getLoadProfileId());
  }

  @Test
  @DisplayName("""
      Given a GeneratedMessage LoadProfile id
       When the GeneratedMessage LoadProfile id is not found
       Then a NoGeneratedMessagesFoundException is thrown
      """)
  void shouldThrowNoGeneratedMessagesFoundExceptionWhenLoadProfileIdDoesNotExist() {
    final Long loadProfileId = 123L;

    when(generatedMessageRepository.findGeneratedMessagesByLoadProfileId(loadProfileId)).thenReturn(List.of());

    assertThrows(NoGeneratedMessagesFoundException.class,
        () -> generatedMessageStorageService.getByLoadProfileId(loadProfileId));
  }

  @Test
  @DisplayName("""
      Given a valid GeneratedMessage is stored
       When delete is called with the valid GeneratedMessage id
       Then the valid GeneratedMessage is deleted
      """)
  void shouldDeleteGeneratedMessageWhenItExists() {
    GeneratedMessage generatedMessage = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder().build();

    when(generatedMessageRepository.findById(generatedMessage.getId())).thenReturn(Optional.of(generatedMessage));

    generatedMessageStorageService.delete(generatedMessage.getId());

    verify(generatedMessageRepository).deleteById(generatedMessage.getId());
  }

  @Test
  @DisplayName("""
      Given a GeneratedMessage id that does not exist
       When delete() is called with the GeneratedMessage id
       Then a NoGeneratedMessageFoundException is thrown
      """)
  void shouldThrowExceptionWhenDeletingWhenIdIsNotFound() {
    final String invalidId = "abcdef123456";
    when(generatedMessageRepository.findById(invalidId)).thenReturn(Optional.empty());
    assertThrows(NoGeneratedMessagesFoundException.class, () -> generatedMessageStorageService.delete(invalidId));
  }

  @Test
  @DisplayName("""
      Given a valid GeneratedMessage is stored
       When delete is called with the valid LoadProfile id
       Then the GeneratedMessages associated with that LoadProfile are deleted
      """)
  void shouldDeleteGeneratedMessageByLoadProfileIdWhenItExists() {
    // Generate 2 messages with the same load profile id
    GeneratedMessage generatedMessage1 = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder().build();
    GeneratedMessage generatedMessage2 = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder().build();

    when(generatedMessageRepository.findGeneratedMessagesByLoadProfileId(generatedMessage1.getLoadProfileId()))
        .thenReturn(List.of(generatedMessage1, generatedMessage2));

    generatedMessageStorageService.deleteGeneratedMessagesForLoadProfile(generatedMessage1.getLoadProfileId());

    verify(generatedMessageRepository).deleteGeneratedMessagesByLoadProfileId(generatedMessage1.getLoadProfileId());
  }

  @Test
  @DisplayName("""
      Given a LoadProfile id that does not exist
       When deleteGeneratedMessagesByLoadProfileId() is called with the invalid LoadProfile id
       Then a NoGeneratedMessageFoundException is thrown
      """)
  void shouldThrowExceptionWhenDeletingWhenLoadProfileIsNotFound() {
    final Long loadProfileId = 123L;
    when(generatedMessageRepository.findGeneratedMessagesByLoadProfileId(loadProfileId)).thenReturn(List.of());
    assertThrows(NoGeneratedMessagesFoundException.class,
        () -> generatedMessageStorageService.deleteGeneratedMessagesForLoadProfile(loadProfileId));
  }

  @Test
  @DisplayName("""
      Given a valid GeneratedMessage
       When an attempt is made to save it
       Then the an id should be generated
       And the collection should be saved""")
  public void testSaveGeneratedMessage() {
    final String generatedMessageId = "685fff61c4a13926b5c261e0";
    GeneratedMessage generatedMessage = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder().build();
    GeneratedMessage savedGeneratedMessage = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder()
        .id(generatedMessageId)
        .build();
    when(generatedMessageRepository.save(generatedMessage)).thenReturn(savedGeneratedMessage);

    GeneratedMessage result = generatedMessageStorageService.save(generatedMessage);

    assertNotNull(result);
    assertEquals(generatedMessageId, result.getId());
    assertEquals("Test Message", result.getMessage());

    verify(generatedMessageRepository, times(1)).save(generatedMessage);
  }

}