/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.impl;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileException;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.MessageSize;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.Profile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class LoadGeneratorHelper {

  private static final String CRLF = "\r\n";
  private static final String SPACE = " ";
  private static final String DOT = ".";
  private static final String START_OF_HEADING = "\u0001";
  private static final String START_OF_TEXT = "\u0002";
  private static final String END_OF_TEXT = "\u0003";
  private static final String DEFAULT_PRIORITY = "QN";

  public static final String SMALL_MESSAGE = "SMALL_MESSAGE_PAYLOAD";
  public static final String MEDIUM_MESSAGE = String.format("%-500s", "MEDIUM_MESSAGE_PAYLOAD_").replace(' ', 'M');
  public static final String LARGE_MESSAGE = String.format("%-1000s", "LARGE_MESSAGE_PAYLOAD_").replace(' ', 'L');
  public static final String EXTRA_LARGE_MESSAGE = String.format("%-64000s", "EXTRA_LARGE_MESSAGE_PAYLOAD_").replace(' ', 'X');

  public static List<String> generateTypeBMessageFromLoadProfile(LoadProfile loadProfile) {
    if (loadProfile == null || loadProfile.getProfiles().isEmpty()) {
      throw new NoLoadProfileException();
    }

    int totalMessages = loadProfile.getProfiles().stream()
        .mapToInt(Profile::getCount)
        .sum();
    List<String> messages = new ArrayList<>(totalMessages);

    for (Profile profile : loadProfile.getProfiles()) {
      List<String> nal = profile.getNal();
      List<String> priority = profile.getPriority();
      List<MessageSize> size = profile.getSize();
      List<String> origin = profile.getOrigin();
      int count = profile.getCount();
      messages.addAll(generateMessages(nal, priority, size, origin, count));
    }

    return messages;
  }

  private static List<String> generateMessages(List<String> nal, List<String> priority, List<MessageSize> size, List<String> origin, int count) {
    List<String> messageCombinations = new ArrayList<>();

    priority = setDefaultPriorityIfNotProvided(priority);
    size = setDefaultSizeIfNotProvided(size);

    //Get all combinations of messages based on parameters
    for (String o : origin) {
      for (String n : nal) {
        for (String p : priority) {
          for (MessageSize s : size) {
            messageCombinations.add(populateTypeBMessage(n, p, s, o));
          }
        }
      }
    }

    // Repeat the combinations in round-robin order until the count value of messages is reached
    List<String> typeBMessages = new ArrayList<>(count);
    int noOfCombinations = messageCombinations.size();
    for (int i = 0; i < count; i++) {
      typeBMessages.add(messageCombinations.get(i % noOfCombinations).replace("___", "___" + (i + 1) + "___"));
    }

    return typeBMessages;
  }

  private static List<String> setDefaultPriorityIfNotProvided(List<String> priority) {
    if (priority == null) {
      priority = new ArrayList<>(1);
      priority.add(DEFAULT_PRIORITY);
    } else if (priority.isEmpty()) {
      priority.add(DEFAULT_PRIORITY);
    }
    return priority;
  }

  private static List<MessageSize> setDefaultSizeIfNotProvided(List<MessageSize> size) {
    if (size == null) {
      size = new ArrayList<>(1);
      size.add(MessageSize.SMALL);
    } else if (size.isEmpty()) {
      size.add(MessageSize.SMALL);
    }
    return size;
  }

  private static String populateTypeBMessage(String nal, String priority, MessageSize size, String origin) {
    StringBuilder message = new StringBuilder(CRLF);

    message.append(START_OF_HEADING);
    message.append(priority);
    message.append(SPACE);
    message.append(nal);
    message.append(CRLF);

    message.append(DOT);
    message.append(origin);
    message.append(SPACE);
    LocalDateTime now = LocalDateTime.now();
    String messageIndicator = String.valueOf(now.getDayOfMonth()) + now.getHour() + now.getMinute();
    message.append(messageIndicator);
    message.append(CRLF);

    message.append(START_OF_TEXT);
    message.append(getMessagePayload(size));
    message.append(CRLF);
    message.append(CRLF);
    message.append("___");
    message.append(CRLF);
    message.append(END_OF_TEXT);

    return message.toString();
  }

  private static String getMessagePayload(MessageSize size) {
    return switch (size) {
      case SMALL -> SMALL_MESSAGE;
      case MEDIUM -> MEDIUM_MESSAGE;
      case LARGE -> LARGE_MESSAGE;
      case EXTRA_LARGE -> EXTRA_LARGE_MESSAGE;
    };
  }
}