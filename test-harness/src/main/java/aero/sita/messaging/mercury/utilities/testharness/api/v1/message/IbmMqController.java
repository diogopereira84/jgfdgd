/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.message;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.request.ReceiveMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.GenerateLoadIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestWithMessageDto;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.IbmMqService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ibm")
public class IbmMqController {

  private final IbmMqService service;

  @PostMapping("/send")
  void sendOneMessageIbmMq(@RequestBody @Valid SendMessageIbmMqRequestWithMessageDto request) {
    SendMessageIbmMqRequestDto serviceRequest = SendMessageIbmMqRequestDto.builder()
        .destinationsDetailsList(request.getDestinationsDetailsList())
        .build();

    service.sendMessages(serviceRequest, List.of(request.getMessage()));
  }

  @PostMapping("/send/multiple-messages")
  void sendMultipleMessagesIbmMq(@RequestParam SendMessageIbmMqRequestDto request,
                                 @RequestParam List<String> messages) {
    service.sendMessages(request, messages);
  }

  @PostMapping("/receive")
  ResponseEntity<List<DestinationAndMessagesResponseDto>> receiveMessageIbmMq(
      @RequestBody @Valid ReceiveMessageIbmMqRequestDto request) {
    List<DestinationAndMessagesResponseDto> destinationAndMessagesResponseDtos = service.receiveMessages(request);
    boolean hasSomeException = destinationAndMessagesResponseDtos.stream()
        .anyMatch(this::exceptionsIsNotEmpty);

    if (hasSomeException) {
      return ResponseEntity.accepted().body(destinationAndMessagesResponseDtos);
    } else {
      return ResponseEntity.ok().body(destinationAndMessagesResponseDtos);
    }
  }

  @PostMapping("/load/generate")
  ResponseEntity<Long> generateLoad(@Valid @RequestBody GenerateLoadIbmMqRequestDto request) {
    Long id = service.generateLoad(request);
    return ResponseEntity.ok(id);
  }

  @PostMapping("/load/inject/{generatedLoadId}")
  @Deprecated() // Use MessagesController.inject() instead...
  void injectLoad(@PathVariable Long generatedLoadId,
                  @RequestBody SendMessageIbmMqRequestDto request) {
    service.injectLoad(generatedLoadId, request);
  }

  private boolean exceptionsIsNotEmpty(DestinationAndMessagesResponseDto destinationAndMessagesResponseDto) {
    return destinationAndMessagesResponseDto.getErrors() != null
        && !destinationAndMessagesResponseDto.getErrors().isEmpty();
  }
}
