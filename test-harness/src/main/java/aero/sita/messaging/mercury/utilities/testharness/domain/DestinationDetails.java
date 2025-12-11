/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DestinationDetails {

  @NotEmpty
  private String server;

  @NotNull
  private Integer port;

  @NotNull
  private String queueManager;

  @NotEmpty
  private List<@NotEmpty String> destinationNames;

  public String getConnName() {
    return server + "(" + port + ")";
  }
}
