/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileException;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.MessageSize;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.Profile;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.LoadProfileFixtureFactory;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LoadGeneratorHelperTest {

  private static final String CRLF = "\r\n";
  private static final String END_OF_TEXT = "\u0003";

  @Test
  @DisplayName("""
      Given that a load profile is null
      When the service generateLoad() API is called with this request
      Then the service should throw a NoLoadProfileException
      """)
  void shouldThrowNoLoadProfileExceptionWhenLoadProfileIsNull() {
    LoadProfile loadProfile = null;
    assertThrows(NoLoadProfileException.class, () -> LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile));
  }

  @Test
  @DisplayName("""
      Given that a load profile is provided but is empty
      When the service generateLoad() API is called with this request
      Then the service should throw a NoLoadProfileException
      """)
  void shouldThrowNoLoadProfileExceptionWhenLoadProfileIsEmpty() {
    LoadProfile loadProfile = new LoadProfile();
    loadProfile.setProfiles(new ArrayList<>());
    assertThrows(NoLoadProfileException.class, () -> LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile));
  }

  @Test
  @DisplayName("""
      Given that a load profile is provided but has a null Priority list
      When the generateTypeBMessageFromLoadProfile() method is called with this request
      Then the default of QN should be set as the priority
      """)
  void shouldDefaultPriorityWhenLoadProfileHasANullPriority() {
    List<String> nalList = new ArrayList<>(1);
    nalList.add("ABCDEFG");
    Profile profile = createTestProfile(nalList, null, null);

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile))
        .build();

    List<String> generatedMessages = LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile);

    assertNotNull(generatedMessages);
    assertTrue(generatedMessages.getFirst().contains("QN"));
  }

  @Test
  @DisplayName("""
      Given that a load profile is provided but has an empty Priority list
      When the generateTypeBMessageFromLoadProfile() method is called with this request
      Then the default of QN should be set as the priority
      """)
  void shouldDefaultPriorityWhenLoadProfileHasAnEmptyPriorityList() {
    List<String> nalList = new ArrayList<>(1);
    nalList.add("ABCDEFG");
    List<String> priorityList = new ArrayList<>(1);
    Profile profile = createTestProfile(nalList, priorityList, null);

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();
    List<String> generatedMessages = LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile);
    assertNotNull(generatedMessages);
    assertTrue(generatedMessages.getFirst().contains("QN"));
  }

  @Test
  @DisplayName("""
      Given that a load profile is provided but the Size list is empty
      When the generateTypeBMessageFromLoadProfile() method is called with this request
      Then the default message size of SMALL should be set
      """)
  void shouldDefaultSizeToSmallWhenLoadProfileHasANullSizeList() {
    List<String> nalList = new ArrayList<>(1);
    nalList.add("ABCDEFG");
    List<String> priorityList = new ArrayList<>(1);
    priorityList.add("QK");
    Profile profile = createTestProfile(nalList, priorityList, null);

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();

    List<String> generatedMessages = LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile);
    assertNotNull(generatedMessages);
    assertTrue(generatedMessages.getFirst().contains("QK"));
    assertTrue(generatedMessages.getFirst().contains("SMALL"));
  }

  @Test
  @DisplayName("""
      Given that a load profile is provided but the Size list is empty
      When the generateTypeBMessageFromLoadProfile() method is called with this request
      Then the default message size of SMALL should be set
      """)
  void shouldDefaultSizeToSmallWhenLoadProfileHasAEmptySizeList() {
    List<String> nalList = new ArrayList<>(1);
    nalList.add("ABCDEFG");
    List<String> priorityList = new ArrayList<>(1);
    priorityList.add("QK");

    Profile profile = createTestProfile(nalList, priorityList, new ArrayList<>());

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();

    List<String> generatedMessages = LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile);
    assertNotNull(generatedMessages);
    assertTrue(generatedMessages.getFirst().contains("QK"));
    assertTrue(generatedMessages.getFirst().contains("SMALL"));
  }

  @Test
  @DisplayName("""
      Given that a load profile with 2 valid entries is provided
      And the first profile has a MEDIUM message size
      And the second profile has a Large message size
      When the generateTypeBMessageFromLoadProfile() method is called with this request
      Then the two messages should be returned with MEDIUM and LARGE message sizes respectively
      """)
  void shouldGenerateTwoMessagesWhenTwoLoadProfilesProvided() {
    List<String> nalList = new ArrayList<>(1);
    nalList.add("ABCDEFG");

    List<String> priorityList = new ArrayList<>(1);
    priorityList.add("QK");
    Profile profile1 = createTestProfile(nalList, priorityList, new ArrayList<>(List.of(MessageSize.MEDIUM)));

    // Change some properties for the second profile
    priorityList.add("QD");
    Profile profile2 = createTestProfile(nalList, priorityList, new ArrayList<>(List.of(MessageSize.LARGE)));

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile1, profile2)).build();

    List<String> generatedMessages = LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile);
    assertNotNull(generatedMessages);
    assertEquals(2, generatedMessages.size());
    assertTrue(generatedMessages.getFirst().contains("QK"));
    assertTrue(generatedMessages.getFirst().contains("MEDIUM"));
    assertTrue(generatedMessages.getLast().contains("QK"));
    assertTrue(generatedMessages.getLast().contains("LARGE"));
  }


  @Test
  @DisplayName("""
      Given we have a profile that will generate 5 messages,
      When we call the method to generate the messages,
      Then each message should be appended with the equivalent index.
      """)
  void shouldAddCounterToGeneratedMessages() {
    // Given
    List<String> nalList = new ArrayList<>(1);
    nalList.add("ABCDEFG");

    List<String> priorityList = new ArrayList<>(1);
    priorityList.add("QK");
    Profile profile = createTestProfile(nalList, priorityList, new ArrayList<>(List.of(MessageSize.MEDIUM)));
    profile.setCount(5);

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile))
        .build();

    // When
    List<String> generatedMessages = LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile);

    // Then
    assertNotNull(generatedMessages);
    assertEquals(5, generatedMessages.size());

    for (int i = 0; i < 5; i++) {
      String message = generatedMessages.get(i);
      assertTrue(message.contains("___" + (i + 1) + "___"));
      assertTrue(message.endsWith(CRLF + END_OF_TEXT));
    }
  }

  @Test
  @DisplayName("""
      Given that a load profile with a small message size is provided
      When the generateTypeBMessageFromLoadProfile() method is called with this request
      Then the message should contain the correct small message payload
      """)
  void shouldGenerateCorrectSmallMessages() {
    // Given
    List<String> nalList = new ArrayList<>(1);
    nalList.add("ABCDEFG");

    List<String> priorityList = new ArrayList<>(1);
    priorityList.add("QK");
    Profile profile = createTestProfile(nalList, priorityList, new ArrayList<>(List.of(MessageSize.SMALL)));
    profile.setCount(1);

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile))
        .build();

    // When
    List<String> generatedMessages = LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile);

    // Then
    assertNotNull(generatedMessages);
    assertEquals(1, generatedMessages.size());
    String first = generatedMessages.getFirst();
    assertNotNull(first);
    assertTrue(first.contains("\r\n\u0002SMALL_MESSAGE_PAYLOAD\r\n\r\n___1___\r\n\u0003"));
  }

  @Test
  @DisplayName("""
      Given that a load profile with an extra large message size is provided
      When the generateTypeBMessageFromLoadProfile() method is called with this request
      Then the message should contain the correct extra large message payload with expected character count
      """)
  void shouldGenerateCorrectExtraLargeMessages() {
    // Given
    List<String> nalList = new ArrayList<>(1);
    nalList.add("ABCDEFG");

    List<String> priorityList = new ArrayList<>(1);
    priorityList.add("QK");
    Profile profile = createTestProfile(nalList, priorityList, new ArrayList<>(List.of(MessageSize.EXTRA_LARGE)));
    profile.setCount(1);

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile))
        .build();

    // When
    List<String> generatedMessages = LoadGeneratorHelper.generateTypeBMessageFromLoadProfile(loadProfile);

    // Then
    assertNotNull(generatedMessages);
    assertEquals(1, generatedMessages.size());
    String first = generatedMessages.getFirst();
    assertNotNull(first);
    assertTrue(first.contains("\r\n\u0002EXTRA_LARGE_MESSAGE_PAYLOAD_"));
    assertEquals(63975, first.chars().filter(ch -> ch == 'X').count());
    assertTrue(first.contains("___1___\r\n\u0003"));
  }

  private Profile createTestProfile(List<String> nalList, List<String> priorityList, List<MessageSize> messageSize) {
    Profile.ProfileBuilder profileBuilder = LoadProfileFixtureFactory.defaultProfileBuilder();

    return profileBuilder.nal(nalList).priority(priorityList).size(messageSize).build();
  }
}