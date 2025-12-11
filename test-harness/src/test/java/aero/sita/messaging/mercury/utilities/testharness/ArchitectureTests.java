/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Disabled("Disabled due to how long this test runs")
public class ArchitectureTests {

  // Currently violating this architecture rule in multiple places.
  //  @Test
  //  void shouldNotReferenceControllerLayerFromServiceLayer() {
  //    JavaClasses classes = new ClassFileImporter().importPackages("aero.sita.messaging.mercury.utilities.testharness");
  //    Architectures.LayeredArchitecture layeredArchitecture = layeredArchitecture()
  //        .consideringOnlyDependenciesInLayers()
  //        .layer("Controller").definedBy("aero.sita.messaging.mercury.utilities.testharness.controller..")
  //        .layer("Service").definedBy("aero.sita.messaging.mercury.utilities.testharness.service..")
  //
  //        .whereLayer("Service").mayNotAccessAnyLayer()
  //        .whereLayer("Controller").mayOnlyAccessLayers("Service");
  //
  //    layeredArchitecture.check(classes);
  //  }


  // Plan: fix this architecture bit-by-bit; starting with v2 controllers.

  @DisplayName("""
      Any class in the service layer must not reference any class in the v2 controller layer.
      Any class in the v2 controller layer must only reference classes in the service layer.
      """)
  @Test
  void shouldNotReferenceV2ControllerFromServiceLayer() {
    JavaClasses classes = new ClassFileImporter()
        .importPackages(
            "aero.sita.messaging.mercury.utilities.testharness.controller",
            "aero.sita.messaging.mercury.utilities.testharness.service");

    Architectures.LayeredArchitecture layeredArchitecture = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("ControllersV2").definedBy("aero.sita.messaging.mercury.utilities.testharness.controller..v2..")
        .layer("Service").definedBy("aero.sita.messaging.mercury.utilities.testharness.service..")

        .whereLayer("Service").mayNotAccessAnyLayer()
        .whereLayer("ControllersV2").mayOnlyAccessLayers("Service");
    layeredArchitecture.check(classes);
  }

  @DisplayName("""
      Any class in the service layer must not reference any class in the protocol layer.
      Any class in the protocol layer may access the service layer to facilitate dependency injection.
      """)
  @Test
  void shouldNotReferenceProtocolLayerFromServiceLayer() {
    JavaClasses classes = new ClassFileImporter().importPackages(
        "aero.sita.messaging.mercury.utilities.testharness.protocol",
        "aero.sita.messaging.mercury.utilities.testharness.service");
    Architectures.LayeredArchitecture layeredArchitecture = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Protocol").definedBy("aero.sita.messaging.mercury.utilities.testharness.protocol..")
        .layer("Service").definedBy("aero.sita.messaging.mercury.utilities.testharness.service..")

        .whereLayer("Service").mayNotAccessAnyLayer()
        .whereLayer("Protocol").mayOnlyAccessLayers("Service");
    layeredArchitecture.check(classes);
  }

  // TODO: these tests feel very slow, maybe we should scope the imported packages?
}