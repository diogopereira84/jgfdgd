/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.extension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class WiremockExtension implements BeforeAllCallback {
  public static final String STOP_CONNECTIONS_PATH = "/orchestrator/api/v1/operations/connections/stop";
  public static final String START_CONNECTIONS_PATH = "/orchestrator/api/v1/operations/connections/start";
  public static final String ENDPOINTS_STATUS_PATH = "/orchestrator/api/v1/operations/endpoints/status";
  public static final String CONNECTIONS_STATUS_PATH = "/orchestrator/api/v1/operations/connections/status";
  public static WireMockServer wireMockServer;

  @Override
  public void beforeAll(ExtensionContext context) {

    if (wireMockServer != null) {
      return;
    }

    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();
    configureFor("localhost", wireMockServer.port());
    System.setProperty("orchestrator.client.base-url", "http://localhost:" + wireMockServer.port() + "/orchestrator");

    tearDown();
    setupStopEndpoint();
    setupStartEndpoint();
    setupEndpointsStatusEndpoint();
    setupConnectionsStatusEndpoint();
  }

  private void setupStopEndpoint() {
    stubFor(post(urlEqualTo(STOP_CONNECTIONS_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")));
  }


  private void setupStartEndpoint() {
    stubFor(post(urlEqualTo(START_CONNECTIONS_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")));
  }

  private void setupEndpointsStatusEndpoint() {
    stubFor(get(urlEqualTo(ENDPOINTS_STATUS_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{}")));
  }

  private void setupConnectionsStatusEndpoint() {
    stubFor(get(urlEqualTo(CONNECTIONS_STATUS_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{}")));
  }

  private void tearDown() {
    WiremockExtension.wireMockServer.resetAll();
  }

}
