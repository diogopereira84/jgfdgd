/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event;

import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event.IncomingEventHeaderDtoFixtureFactory.defaultIncomingEventHeaderDto;
import static aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event.MessagePayloadDtoFixtureFactory.defaultMessagePayloadDto;

import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventHeaderDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.MessagePayloadDto;
import java.util.Map;

public class IncomingEventDtoFixtureFactory {

  private IncomingEventDtoFixtureFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static IncomingEventDto defaultIncomingEventDto() {
    IncomingEventDto event = new IncomingEventDto();
    IncomingEventHeaderDto header = defaultIncomingEventHeaderDto();
    MessagePayloadDto payload = defaultMessagePayloadDto();
    event.setHeader(header);
    event.setPayload(payload);
    event.setMetadata(Map.of("priority", "1"));
    return event;
  }
}
