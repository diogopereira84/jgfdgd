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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileFoundException;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.persistence.load.LoadProfileRepository;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.LoadProfileFixtureFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoadProfileStorageServiceTest {

  @Mock
  LoadProfileRepository loadProfileRepository;

  @Mock
  DatabaseSequenceGeneratorService databaseSequenceGeneratorService;

  @InjectMocks
  private LoadProfileStorageService loadProfileStorageService;

  @Test
  @DisplayName("""
      Given that a valid LoadProfile is stored
       When the valid LoadProfile id is provided in the service getById() call
       Then the valid LoadProfile is returned
      """)
  void shouldReturnLoadProfileWhenExists() {
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder().build();
    when(loadProfileRepository.findById(1L)).thenReturn(Optional.of(loadProfile));

    LoadProfile result = loadProfileStorageService.getById(1L);

    assertEquals(result.getName(), loadProfile.getName());
    assertEquals(result.getDescription(), loadProfile.getDescription());
    assertEquals(result.getProfiles().size(), loadProfile.getProfiles().size());
  }

  @Test
  @DisplayName("""
      Given a LoadProfile id that does not exist
       When the LoadProfile id is provided in the service getById() call
       Then a NoLoadProfileFoundException is thrown
      """)
  void shouldThrowExceptionWhenLoadProfileDoesNotExist() {
    when(loadProfileRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NoLoadProfileFoundException.class, () -> loadProfileStorageService.getById(1L));
  }

  @Test
  @DisplayName("""
      Given a valid LoadProfile is stored
       When delete is called with the valid LoadProfile id
       Then the valid LoadProfile is deleted
      """)
  void shouldDeleteLoadProfileWhenItExists() {
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder().build();
    when(loadProfileRepository.findById(1L)).thenReturn(Optional.of(loadProfile));

    loadProfileStorageService.delete(1L);

    verify(loadProfileRepository).deleteById(1L);
  }

  @Test
  @DisplayName("""
      Given a LoadProfile id that does not exist
       When delete() is called with the LoadProfile id
       Then a NoLoadProfileFoundException is thrown
      """)
  void shouldThrowExceptionWhenDeletingWhenLoadProfileIsNotFound() {
    when(loadProfileRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(NoLoadProfileFoundException.class, () -> loadProfileStorageService.delete(1L));
  }

  @Test
  @DisplayName("""
      Given a valid LoadProfile
       When an attempt is made to save it
       Then the an id should be generated
       And the collection should be saved""")
  public void testSaveLoadProfile() {
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder().build();
    LoadProfile savedLoadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .id(123L).build();
    when(loadProfileRepository.save(loadProfile)).thenReturn(savedLoadProfile);
    when(databaseSequenceGeneratorService.generateSequence("load_profile_sequence")).thenReturn(123L);

    LoadProfile result = loadProfileStorageService.save(loadProfile);

    assertNotNull(result);
    assertEquals(123L, result.getId());
    assertEquals("Test Profile", result.getName());

    verify(databaseSequenceGeneratorService, times(1)).generateSequence("load_profile_sequence");
    verify(loadProfileRepository, times(1)).save(loadProfile);
  }

  @Test
  @DisplayName("""
      Given a null LoadProfile
       When the LoadProfileService save() method is called
       Then an exception is thrown
      """)
  public void whenSavingNullLoadProfileEnsureExceptionIsThrown() {
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder().build();
    assertThrows(NoLoadProfileException.class, () -> loadProfileStorageService.save(null));

    verify(loadProfileRepository, never()).save(loadProfile);
  }

  @Test
  @DisplayName("""
      Given a LoadProfile with a null Profile list
       When the LoadProfileService create() is called
       Then an exception is thrown
      """)
  public void whenSaveIsCalledWithALoadProfileWithANullProfileListThenAnExceptionIsThrown() {
    LoadProfile loadProfile = new LoadProfile();
    assertThrows(NoLoadProfileException.class, () -> loadProfileStorageService.save(loadProfile));

    verify(loadProfileRepository, never()).save(loadProfile);
  }

  @Test
  @DisplayName("""
      Given a LoadProfile with an empty Profile list
       When the LoadProfileService save() method is called
       Then an exception is thrown
      """)
  public void whenSaveIsCalledWithALoadProfileWithAnEmptyProfileListThenAnExceptionIsThrown() {
    LoadProfile loadProfile = new LoadProfile();
    loadProfile.setProfiles(List.of());
    assertThrows(NoLoadProfileException.class, () -> loadProfileStorageService.save(loadProfile));

    verify(loadProfileRepository, never()).save(loadProfile);
  }
}
