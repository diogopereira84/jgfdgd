/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.load;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LoadProfileTest {

  public static Stream<Arguments> loadProfileProvider() {
    Stream<Arguments> args = Stream.of(
        Arguments.of(0, List.of()),
        Arguments.of(1, List.of(1)),
        Arguments.of(2, List.of(1, 1)),
        Arguments.of(3, List.of(1, 2)),
        Arguments.of(108, List.of(4, 8, 15, 16, 23, 42))
    );
    return args;
  }

  @DisplayName("""
      When the profiles list is null,
      Then the total count should return zero.
      """)
  @Test
  void shouldReturnZeroWhenProfilesIsNull() {
    LoadProfile loadProfile = LoadProfile.builder().profiles(null).build();
    assertEquals(0, loadProfile.getTotalCount());
  }

  @DisplayName("""
      When the profiles list is empty,
      Then the total count should return zero.
      """)
  @Test
  void shouldReturnZeroWhenProfilesIsEmpty() {
    LoadProfile loadProfile = LoadProfile.builder().profiles(List.of()).build();
    assertEquals(0, loadProfile.getTotalCount());
  }

  @DisplayName("""
      Given a list of profiles with counts,
      When the total count is calculated,
      Then it should return the sum of all counts in the profiles.
      """)
  @ParameterizedTest
  @MethodSource("loadProfileProvider")
  void shouldReturnCorrectCount(int expectedCount, List<Integer> counts) {

    List<Profile> profileList = new ArrayList<>();
    for (Integer count : counts) {
      profileList.add(Profile.builder()
          .count(count)
          .build());
    }

    LoadProfile loadProfile = LoadProfile.builder()
        .profiles(profileList)
        .build();
    assertEquals(expectedCount, loadProfile.getTotalCount());

  }
}