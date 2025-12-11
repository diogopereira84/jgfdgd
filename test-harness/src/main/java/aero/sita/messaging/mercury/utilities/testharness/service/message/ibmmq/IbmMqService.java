/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.request.ReceiveMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.receive.response.DestinationAndMessagesResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.GenerateLoadIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import java.util.List;

public interface IbmMqService {

  void sendMessages(SendMessageIbmMqRequestDto request, List<String> messages);

  void sendMessages(SendMessageIbmMqRequest request, List<String> messages);

  List<DestinationAndMessagesResponseDto> receiveMessages(ReceiveMessageIbmMqRequestDto request);

  Long generateLoad(GenerateLoadIbmMqRequestDto request);

  @Deprecated
  void injectLoad(Long id, SendMessageIbmMqRequestDto request);

  void injectLoad(Long id, SendMessageIbmMqRequest request);
}
