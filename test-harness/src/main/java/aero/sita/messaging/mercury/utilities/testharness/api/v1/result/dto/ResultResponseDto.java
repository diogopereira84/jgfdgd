/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto;

import lombok.Data;

@Data
public class ResultResponseDto {
  private String id;
  private Long loadProfileId;
  private long elapsedTimeInMilliseconds;
  private int actualMessageCount;
}
