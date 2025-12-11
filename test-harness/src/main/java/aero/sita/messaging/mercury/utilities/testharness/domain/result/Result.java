/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.result;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "results")
@Builder
public class Result {
  @Id
  private String id;

  @NotNull
  private Long loadProfileId;

  private String injectionId;

  private Instant connectionsStartedTimestamp;
  private Instant connectionsStartedAckTimestamp;

  private long elapsedTimeInMilliseconds;

  private int expectedMessageCount;
  private int actualMessageCount;

  @CreatedDate
  private Instant createdTimestamp;

  @LastModifiedDate
  private Instant lastUpdatedTimestamp;
}
