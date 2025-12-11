/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.load;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "generated-messages")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class GeneratedMessage {
  @Id
  private String id;
  String message;
  @NotNull
  Long loadProfileId;
  @CreatedDate
  private Instant createdTimestamp;
  @LastModifiedDate
  private Instant lastUpdatedTimestamp;
  @Version
  private Long revision;
}
