/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.load;

import aero.sita.messaging.mercury.utilities.testharness.domain.load.GeneratedMessage;
import aero.sita.messaging.mercury.utilities.testharness.service.load.GeneratedMessageStorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/load/messages")
public class GeneratedMessagesController {
  private final GeneratedMessageStorageService generatedMessageStorageService;

  @GetMapping("/{id}")
  public GeneratedMessage getGeneratedMessage(@PathVariable String id) {
    return generatedMessageStorageService.get(id);
  }

  @GetMapping("/profile/{id}")
  public List<GeneratedMessage> getGeneratedMessagesForLoadProfile(@PathVariable Long id) {
    return generatedMessageStorageService.getByLoadProfileId(id);
  }

  @DeleteMapping("/{id}")
  public void deleteGeneratedMessage(@PathVariable String id) {
    generatedMessageStorageService.delete(id);
  }

  @DeleteMapping("/profile/{id}")
  public void deleteGeneratedMessagesForLoadProfile(@PathVariable Long id) {
    generatedMessageStorageService.deleteGeneratedMessagesForLoadProfile(id);
  }

}
