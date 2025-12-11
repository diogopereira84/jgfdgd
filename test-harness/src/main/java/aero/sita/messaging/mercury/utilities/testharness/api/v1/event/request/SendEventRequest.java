/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.event.request;

import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.outgoingevent.v1.OutgoingEventDto;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@IncomingOrOutgoingEventDtoConstraint
public class SendEventRequest {

  IncomingEventDto incomingEvent;

  OutgoingEventDto outgoingEvent;

  @NotEmpty
  List<@NotEmpty String> topics;

}


