/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Data;

// TODO: rename this; it's not actually an IBM specific request.

@Data
@Builder(toBuilder = true)
public class SendMessageIbmMqRequest {

  @NotEmpty
  @NotNull
  private List<@Valid DestinationDetails> destinationsDetailsList;

  private Long loadProfileId;

  private String injectionId;

  private boolean preLoad;

  private int delayBetweenMessagesInMilliseconds;
}
