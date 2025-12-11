/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ServerConfiguration {
  private String id;
  private String hostname;
  private Integer port;
  private String queueManager;
  private String channel;
  private String user;
  private String password;
  @Builder.Default
  private Map<String, ConnectionConfiguration> connections = new HashMap<>();
}
