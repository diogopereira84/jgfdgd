/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.performance;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "message-timings")
public class MessageTimingsDocument {

  @Id
  private String testHarnessMessageId;

  private String correlationId;

  private String injectionId;

  private Instant customerToEndpointPublishTimestamp;
  private Instant endpointToNormalizerPublishTimestamp;
  private Instant endpointToCustomerConsumeTimestamp;

  private Long timeToEndpointInboundPublish;

  private Long messageAcceptanceLatency;
  private Long externalEndToEndLatency;
}