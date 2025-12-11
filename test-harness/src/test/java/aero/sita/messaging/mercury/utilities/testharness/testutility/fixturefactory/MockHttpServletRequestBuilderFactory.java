/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class MockHttpServletRequestBuilderFactory {

  private MockHttpServletRequestBuilderFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static MockHttpServletRequestBuilder mockHttpServletRequestBuilder(HttpMethod httpMethod, String uri, String request) {
    return request(httpMethod, URI.create(uri))
        .contentType(APPLICATION_JSON)
        .content(request);
  }

  public static MockHttpServletRequestBuilder mockHttpServletRequestBuilder(HttpMethod httpMethod, String uri, List<MockMultipartFile> mockMultipartFiles,
                                                                            Map<String, String> params) {
    MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(httpMethod, URI.create(uri));
    mockMultipartFiles.forEach(mockMultipartHttpServletRequestBuilder::file);
    params.forEach(mockMultipartHttpServletRequestBuilder::param);
    return mockMultipartHttpServletRequestBuilder;
  }

  public static MockHttpServletRequestBuilder mockHttpServletRequestBuilder(HttpMethod httpMethod, String uri, Map<String, String> params) {
    MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(httpMethod, URI.create(uri));
    params.forEach(mockMultipartHttpServletRequestBuilder::param);
    return mockMultipartHttpServletRequestBuilder;
  }

}
