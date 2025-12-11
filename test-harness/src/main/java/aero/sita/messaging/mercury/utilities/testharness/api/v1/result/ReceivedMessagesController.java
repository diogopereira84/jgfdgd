/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.result;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.ReceivedMessageDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ReceivedMessagesService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/received")
@RequiredArgsConstructor
@RestController
@Slf4j
@Validated
public class ReceivedMessagesController {

  private final ReceivedMessagesService service;

  @GetMapping()
  public ResponseEntity<List<ReceivedMessageDto>> getReceivedMessages(
      @RequestParam Optional<String> injectionId) {
    List<ReceivedMessageDto> response = new ArrayList<>();

    List<ReceivedMessage> all = service.filter(injectionId);

    for (ReceivedMessage receivedMessage : all) {
      response.add(ResultsMapper.INSTANCE.toDto(receivedMessage));
    }

    return ResponseEntity.ok(response);
  }
}
