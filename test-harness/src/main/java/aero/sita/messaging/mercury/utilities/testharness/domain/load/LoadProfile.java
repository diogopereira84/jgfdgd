/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.load;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "load-profiles")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class LoadProfile {
  @Transient
  public static final String SEQUENCE_NAME = "load_profile_sequence";

  @Id
  private Long id;
  private String name;
  private String description;
  @NotEmpty
  @Valid
  private List<Profile> profiles;

  @CreatedDate
  private Instant createdTimestamp;
  @LastModifiedDate
  private Instant lastUpdatedTimestamp;
  @Version
  private Long revision;
  @Builder.Default
  private Map<String, String> metadata = new HashMap<>();

  @JsonIgnore
  public int getTotalCount() {
    if (profiles == null || profiles.isEmpty()) {
      return 0;
    }

    return profiles.stream()
        .mapToInt(Profile::getCount)
        .sum();
  }
}