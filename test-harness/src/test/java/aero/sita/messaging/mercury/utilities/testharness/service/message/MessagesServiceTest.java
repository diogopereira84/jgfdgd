/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.IbmMqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessagesServiceTest {

  @InjectMocks
  private MessagesService messagesService;

  @Mock
  private IbmMqService ibmMqService;

  @Captor
  private ArgumentCaptor<SendMessageIbmMqRequest> sendMessageIbmMqRequestArgumentCaptor;

  @Captor
  private ArgumentCaptor<Long> longArgumentCaptor;

  @DisplayName("""
      Given a valid request,
      When this is passed into the inject method,
      It should be delegated correctly to the ibmMqService.
      """)
  @Test
  void shouldPassValidRequestToIbmMqService() {
    // Given
    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .loadProfileId(12345L)
        .build();

    // When
    messagesService.inject(request);

    // Then
    verify(ibmMqService)
        .injectLoad(longArgumentCaptor.capture(), sendMessageIbmMqRequestArgumentCaptor.capture());

    long capturedLoadProfileId = longArgumentCaptor.getValue();
    assertEquals(12345L, capturedLoadProfileId);
    assertEquals(request, sendMessageIbmMqRequestArgumentCaptor.getValue());
  }

  @DisplayName("""
      Given the request is null,
      When inject is called,
      Then an IllegalArgumentException should be thrown.
      """)
  @Test
  void shouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
    assertThrows(IllegalArgumentException.class, () -> messagesService.inject(null));
  }
}
