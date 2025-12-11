/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.result;

import aero.sita.messaging.mercury.libraries.sharedmodels.common.Protocol;
import java.time.Instant;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "received-messages")
public class ReceivedMessage {
  private String id;

  private Instant handOffTimestamp;
  private Protocol protocol;
  private String body;

  private String connectionName;
  private String queueName;

  private String injectionId;
  private String messageId;
}
