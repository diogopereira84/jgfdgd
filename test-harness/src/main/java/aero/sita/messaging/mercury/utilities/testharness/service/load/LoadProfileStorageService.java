/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.load;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileFoundException;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.persistence.load.LoadProfileRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoadProfileStorageService {
  private static final String DATABASE_SEQUENCE_NAME = "load_profile_sequence";
  private final LoadProfileRepository loadProfileRepository;
  private final DatabaseSequenceGeneratorService sequenceGeneratorService;

  LoadProfileStorageService(LoadProfileRepository loadProfileRepository, DatabaseSequenceGeneratorService sequenceGeneratorService) {
    this.loadProfileRepository = loadProfileRepository;
    this.sequenceGeneratorService = sequenceGeneratorService;
  }

  public LoadProfile save(LoadProfile loadProfile) {
    if (loadProfile == null
        || loadProfile.getProfiles() == null
        || loadProfile.getProfiles().isEmpty()) {
      throw new NoLoadProfileException();
    }
    loadProfile.setId(sequenceGeneratorService.generateSequence(DATABASE_SEQUENCE_NAME));
    return loadProfileRepository.save(loadProfile);
  }

  public LoadProfile getById(Long loadProfileId) {
    return loadProfileRepository.findById(loadProfileId)
        .orElseThrow(() -> new NoLoadProfileFoundException(loadProfileId));
  }

  public void delete(Long loadProfileId) {
    getById(loadProfileId);
    loadProfileRepository.deleteById(loadProfileId);
  }

  public List<LoadProfile> getAll() {
    List<LoadProfile> loadProfiles = loadProfileRepository.findAll();
    if (loadProfiles.isEmpty()) {
      throw new NoLoadProfileException();
    }
    return loadProfiles;
  }
}
