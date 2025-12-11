/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.io.rest.v1.dto;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionsStatusResponseDto {
  private List<ConnectionStatusDto> connections;

  @Data
  @Builder
  public static class ConnectionStatusDto {
    private String connectionId;

    private String instanceId;

    private ConnectionStateDto status;

    private OffsetDateTime lastNotificationTimestamp;
  }

  public enum ConnectionStateDto {
    STARTED_PENDING,
    STARTED,
    STOPPED_PENDING,
    STOPPED,
    UNMANAGED
  }
}
