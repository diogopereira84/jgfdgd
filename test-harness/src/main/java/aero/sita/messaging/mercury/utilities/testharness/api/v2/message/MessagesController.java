/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v2.message;

import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.SendMessagesRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.SendMessagesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.utilities.testharness.service.InjectionOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v2/messages")
@RequiredArgsConstructor
@RestController
public class MessagesController {

  private final InjectionOrchestrator injectionOrchestrator;

  @PostMapping("/inject")
  public ResponseEntity<SendMessagesResponseDto> inject(
      @RequestBody @Valid SendMessagesRequestDto dto) {
    SendMessageIbmMqRequest domainObject = MessagesMapper.INSTANCE.toDomainObject(dto);
    injectionOrchestrator.inject(domainObject);
    SendMessagesResponseDto response = SendMessagesResponseDto.builder()
        .injectionId(domainObject.getInjectionId())
        .build();
    return ResponseEntity.ok(response);
  }
}
