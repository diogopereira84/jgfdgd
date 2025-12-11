/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.event;

import static aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventFormatDto.TYPE_B;
import static aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventProtocolDto.IBM_MQ;
import static aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.StatusDto.RECEIVED;

import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventHeaderDto;
import java.time.Instant;
import java.util.UUID;

public class IncomingEventHeaderDtoFixtureFactory {

  private IncomingEventHeaderDtoFixtureFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static IncomingEventHeaderDto defaultIncomingEventHeaderDto() {
    IncomingEventHeaderDto header = new IncomingEventHeaderDto();
    header.setMessageId("1");
    header.setConnectionId("1");
    header.setIncomingServiceAddress("AAABBCC");
    header.setCorrelationId(UUID.randomUUID());
    header.setReceivedDateTime(Instant.now());
    header.setReceivedProtocol(IBM_MQ);
    header.setIncomingFormat(TYPE_B);
    header.setStatus(RECEIVED);
    header.setVersion("v1.0");
    return header;
  }
}
