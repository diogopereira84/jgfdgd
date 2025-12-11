/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.load;

import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.service.load.LoadProfileStorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/load/profiles")
public class LoadProfileController {

  private final LoadProfileStorageService loadProfileStorageService;

  @GetMapping("/{id}")
  public LoadProfile getLoadProfile(@PathVariable long id) {
    return loadProfileStorageService.getById(id);
  }

  @DeleteMapping("/{id}")
  public void deleteLoadProfile(@PathVariable Long id) {
    loadProfileStorageService.delete(id);
  }

  @GetMapping
  private List<LoadProfile> getAllLoadProfiles() {
    return loadProfileStorageService.getAll();
  }
}
