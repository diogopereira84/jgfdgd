/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.event;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.event.request.SendEventRequest;
import aero.sita.messaging.mercury.utilities.testharness.service.event.EventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class EventController {

  private final EventPublisher<SendEventRequest> eventPublisher;

  @PostMapping("/send")
  void sendEvent(@RequestBody @Valid SendEventRequest request) {
    eventPublisher.sendEvent(request);
  }

}
