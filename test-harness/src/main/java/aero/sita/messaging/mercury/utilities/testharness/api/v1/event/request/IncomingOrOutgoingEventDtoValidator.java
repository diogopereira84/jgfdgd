/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.api.v1.event.request;

import static org.springframework.util.StringUtils.hasText;

import aero.sita.messaging.mercury.libraries.sharedmodels.incomingevent.v1.IncomingEventDto;
import aero.sita.messaging.mercury.libraries.sharedmodels.outgoingevent.v1.OutgoingEventDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IncomingOrOutgoingEventDtoValidator implements ConstraintValidator<IncomingOrOutgoingEventDtoConstraint, SendEventRequest> {

  private static final String NOT_EMPTY = "must not be empty";
  private static final String NOT_NULL = "must not be null";

  @Override
  public boolean isValid(SendEventRequest value, ConstraintValidatorContext context) {
    boolean valid = true;
    IncomingEventDto incomingEventDto = value.getIncomingEvent();
    OutgoingEventDto outgoingEventDto = value.getOutgoingEvent();
    context.disableDefaultConstraintViolation();

    if (incomingEventDto != null && outgoingEventDto != null) {
      context.buildConstraintViolationWithTemplate("incomingEvent and outgoingEvent cannot be both assigned, please use either-or")
          .addPropertyNode("incomingEvent|outgoingEvent")
          .addConstraintViolation();
      valid = false;
    }
    if (incomingEventDto == null && outgoingEventDto == null) {
      context.buildConstraintViolationWithTemplate(NOT_NULL)
          .addPropertyNode("incomingEvent|outgoingEvent")
          .addConstraintViolation();
      valid = false;
    } else if (incomingEventDto != null) {
      valid = validateIncoming(incomingEventDto, context, valid);
    } else if (outgoingEventDto != null) {
      valid = validateOutgoing(outgoingEventDto, context, valid);
    }

    return valid;
  }

  private boolean validateIncoming(IncomingEventDto incomingEventDto, ConstraintValidatorContext context, boolean isValid) {
    boolean valid = isValid;
    if (incomingEventDto.getHeader() == null) {
      context.buildConstraintViolationWithTemplate(NOT_NULL)
          .addPropertyNode("incomingEvent.header")
          .addConstraintViolation();
      valid = false;
    } else {
      if (!hasText(incomingEventDto.getHeader().getConnectionId())) {
        context.buildConstraintViolationWithTemplate(NOT_EMPTY)
            .addPropertyNode("incomingEvent.header.connectionId")
            .addConstraintViolation();
        valid = false;
      }
      if (!hasText(incomingEventDto.getHeader().getMessageId())) {
        context.buildConstraintViolationWithTemplate(NOT_EMPTY)
            .addPropertyNode("incomingEvent.header.messageId")
            .addConstraintViolation();
        valid = false;
      }
    }
    return valid;
  }

  private boolean validateOutgoing(OutgoingEventDto outgoingEventDto, ConstraintValidatorContext context, boolean isValid) {
    boolean valid = isValid;
    if (outgoingEventDto.getHeader() == null) {
      context.buildConstraintViolationWithTemplate(NOT_NULL)
          .addPropertyNode("outgoingEvent.header")
          .addConstraintViolation();
      valid = false;
    } else if (!hasText(outgoingEventDto.getHeader().getOutgoingMessageId())) {
      context.buildConstraintViolationWithTemplate(NOT_EMPTY)
          .addPropertyNode("outgoingEvent.header.outgoingMessageId")
          .addConstraintViolation();
      valid = false;
    }
    return valid;
  }
}
