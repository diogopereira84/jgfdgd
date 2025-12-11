/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.performance;

import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimings;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimingsDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MessageTimingsMapper {
  MessageTimingsMapper INSTANCE = Mappers.getMapper(MessageTimingsMapper.class);

  MessageTimings toDomainObject(MessageTimingsDocument messageTimingsDocument);

  MessageTimingsDocument toDocumentObject(MessageTimings messageTimings);
}
