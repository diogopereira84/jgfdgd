/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.load;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoGeneratedMessagesFoundException;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.load.GeneratedMessagesController;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.GeneratedMessage;
import aero.sita.messaging.mercury.utilities.testharness.service.load.GeneratedMessageStorageService;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.GeneratedMessageFixtureFactory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GeneratedMessagesController.class)
public class GeneratedMessagesControllerIT {

  public static final String LOAD_MESSAGES = "/api/v1/load/messages/";
  public static final String LOAD_MESSAGES_PROFILE = "/api/v1/load/messages/profile/";
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GeneratedMessageStorageService generatedMessageStorageService;

  @Test
  @DisplayName("""
      Given a valid GeneratedMessage is stored
      When the /load/messages GET endpoint is called with a valid id
      Then the expected GeneratedMessage is returned
      """)
  void shouldReturnGeneratedMessageById() throws Exception {
    final String generatedMessageId = "685fff61c4a13926b5c261e0";

    GeneratedMessage generatedMessage = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder()
        .id(generatedMessageId).build();
    when(generatedMessageStorageService.get(generatedMessageId)).thenReturn(generatedMessage);

    mockMvc.perform(get(LOAD_MESSAGES + generatedMessageId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Test Message"));
  }

  @Test
  @DisplayName("""
      Given a non existent GeneratedMessage id
      When the /load/messages GET endpoint is called with this id
      Then a 404 NOT FOUND error code is returned
      """)
  void shouldReturn404NotFoundIfGeneratedMessageIdDoesNotExist() throws Exception {
    String nonExistentId = "zyxwvut654321";
    when(generatedMessageStorageService.get(nonExistentId)).thenThrow(new NoGeneratedMessagesFoundException(nonExistentId));

    mockMvc.perform(get(LOAD_MESSAGES + nonExistentId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("""
      Given a valid GeneratedMessage is stored
       When the /load/messages/profile GET endpoint is called with a valid LoadProfile id
       Then the expected GeneratedMessage list is returned
      """)
  void shouldReturnGeneratedMessagesByLoadProfileId() throws Exception {
    final String generatedMessageId = "685fff61c4a13926b5c261e0";
    final Long loadProfileId = 123L;

    GeneratedMessage generatedMessage = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder()
        .id(generatedMessageId).build();
    when(generatedMessageStorageService.getByLoadProfileId(loadProfileId)).thenReturn(List.of(generatedMessage));

    mockMvc.perform(get(LOAD_MESSAGES_PROFILE + loadProfileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].message", is("Test Message")));
  }

  @Test
  @DisplayName("""
      Given a valid GeneratedMessage is stored
      When the /load/messages DELETE endpoint is called with a valid id
      Then the expected GeneratedMessage is deleted
      """)
  void shouldDeleteGeneratedMessageById() throws Exception {
    final String generatedMessageId = "abcdef123456";

    mockMvc.perform(delete(LOAD_MESSAGES + generatedMessageId))
        .andExpect(status().isOk());

    verify(generatedMessageStorageService).delete(generatedMessageId);
  }

  @Test
  @DisplayName("""
      Given a GeneratedMessage id that does not exist
      When the /load/messages DELETE endpoint is called with this id
      Then a 404 NOT FOUND error code is returned
      """)
  void shouldReturn404NotFoundIfWhenDeletingGeneratedMessageIdDoesNotExist() throws Exception {
    String nonExistentId = "nonExistentIdValue";

    doThrow(new NoGeneratedMessagesFoundException(nonExistentId))
        .when(generatedMessageStorageService)
        .delete(nonExistentId);

    mockMvc.perform(delete(LOAD_MESSAGES + nonExistentId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("""
      Given a valid GeneratedMessage is stored
       When the /load/messages/profile DELETE endpoint is called with a valid LoadProfile id
       Then the expected list of GeneratedMessage is deleted
      """)
  void shouldDeleteGeneratedMessagesByLoadProfileId() throws Exception {
    final Long loadProfileId = 123L;

    mockMvc.perform(delete(LOAD_MESSAGES_PROFILE + loadProfileId))
        .andExpect(status().isOk());

    verify(generatedMessageStorageService).deleteGeneratedMessagesForLoadProfile(loadProfileId);
  }

  @Test
  @DisplayName("""
      Given a GeneratedMessage with a LoadProfile id that does not exist
      When the /load/messages/profile DELETE endpoint is called with this id
      Then a 404 NOT FOUND error code is returned
      """)
  void shouldReturn404NotFoundIfWhenDeletingLoadProfileIdDoesNotExist() throws Exception {
    Long nonExistentId = 123L;

    doThrow(new NoGeneratedMessagesFoundException(nonExistentId))
        .when(generatedMessageStorageService)
        .deleteGeneratedMessagesForLoadProfile(nonExistentId);

    mockMvc.perform(delete(LOAD_MESSAGES_PROFILE + nonExistentId))
        .andExpect(status().isNotFound());
  }
}
