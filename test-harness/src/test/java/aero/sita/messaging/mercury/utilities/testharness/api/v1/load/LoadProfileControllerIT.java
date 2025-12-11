/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.load;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileFoundException;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.load.LoadProfileController;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.service.load.LoadProfileStorageService;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.LoadProfileFixtureFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoadProfileController.class)
public class LoadProfileControllerIT {
  public static final String LOAD_PROFILES = "/api/v1/load/profiles/";
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LoadProfileStorageService loadProfileStorageService;

  @Test
  @DisplayName("""
      Given a valid LoadProfile is stored
      When the /load/profiles GET endpoint is called with a valid id
      Then the expected LoadProfile is returned
      """)
  void shouldReturnLoadProfileById() throws Exception {
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder().id(123L).build();
    when(loadProfileStorageService.getById(123L)).thenReturn(loadProfile);

    mockMvc.perform(get(LOAD_PROFILES + "123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Profile"));
  }

  @Test
  @DisplayName("""
      Given a LoadProfile id that is not stored
      When the /load/profiles GET endpoint is called with this id
      Then a 404 NOT FOUND error code is returned
      """)
  void shouldReturn404NotFoundIdLoadProfileIdDoesNotExist() throws Exception {
    Long nonExistentId = 123L;
    when(loadProfileStorageService.getById(nonExistentId)).thenThrow(new NoLoadProfileFoundException(nonExistentId));

    mockMvc.perform(get(LOAD_PROFILES + nonExistentId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("""
      Given a valid LoadProfile is stored
      When the /load/profiles DELETE endpoint is called with a valid id
      Then the expected LoadProfile is deleted
      """)
  void shouldDeleteLoadProfileById() throws Exception {
    mockMvc.perform(delete(LOAD_PROFILES + "123"))
        .andExpect(status().isOk());

    verify(loadProfileStorageService).delete(123L);
  }

  @Test
  @DisplayName("""
      Given a LoadProfile id that is not stored
      When the /load/profiles DELETE endpoint is called with this id
      Then a 404 NOT FOUND error code is returned
      """)
  void shouldReturn404NotFoundIfWhenDeletingLoadProfileIdDoesNotExist() throws Exception {
    Long nonExistentId = 123L;

    doThrow(new NoLoadProfileFoundException(nonExistentId))
        .when(loadProfileStorageService)
        .delete(nonExistentId);

    mockMvc.perform(delete(LOAD_PROFILES + nonExistentId))
        .andExpect(status().isNotFound());
  }
}
