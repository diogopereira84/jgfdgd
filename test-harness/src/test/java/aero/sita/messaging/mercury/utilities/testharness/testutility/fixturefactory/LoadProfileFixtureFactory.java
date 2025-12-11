/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory;

import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.MessageSize;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.Profile;
import java.util.List;

public class LoadProfileFixtureFactory {

  public static LoadProfile.LoadProfileBuilder defaultLoadProfileBuilder() {
    Profile profile = defaultProfileBuilder().build();

    List<Profile> profiles = List.of(profile);

    return LoadProfile.builder()
        .name("Test Profile")
        .description("Test profile description")
        .profiles(profiles);
  }

  public static Profile.ProfileBuilder defaultProfileBuilder() {
    List<String> priority = List.of("QN");
    List<String> nal = List.of("BARXTXS");
    List<String> origin = List.of("ATLXTXS");

    return Profile.builder()
        .nal(nal)
        .origin(origin)
        .priority(priority)
        .count(1)
        .size(List.of(MessageSize.SMALL));
  }
}
