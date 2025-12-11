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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoGeneratedMessagesFoundException;
import aero.sita.messaging.mercury.utilities.testharness.api.error.exception.queue.NoLoadProfileException;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.GenerateLoadIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.message.send.request.SendMessageIbmMqRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.GeneratedMessage;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.LoadProfile;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.MessageSize;
import aero.sita.messaging.mercury.utilities.testharness.domain.load.Profile;
import aero.sita.messaging.mercury.utilities.testharness.service.load.GeneratedMessageStorageService;
import aero.sita.messaging.mercury.utilities.testharness.service.load.LoadProfileStorageService;
import aero.sita.messaging.mercury.utilities.testharness.service.load.MessageGenerationService;
import aero.sita.messaging.mercury.utilities.testharness.service.message.JmsTemplateFactory;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.ConnectionManager;
import aero.sita.messaging.mercury.utilities.testharness.service.message.ibmmq.IbmMqService;
import aero.sita.messaging.mercury.utilities.testharness.service.performance.MessageTimingsService;
import aero.sita.messaging.mercury.utilities.testharness.service.result.ReceivedMessagesService;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.GeneratedMessageFixtureFactory;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.LoadProfileFixtureFactory;
import aero.sita.messaging.mercury.utilities.testharness.testutility.fixturefactory.SendMessageIbmMqRequestFixtureFactory;
import jakarta.jms.ConnectionFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

@ExtendWith(MockitoExtension.class)
public class IbmMqServiceImplLoadGenerationAndInjectionTest {

  private Validator validator;

  @InjectMocks
  private IbmMqServiceImpl service;

  @Mock
  private GeneratedMessageStorageService generatedMessageStorageService;

  @Mock
  private LoadProfileStorageService loadProfileStorageService;

  @Mock
  private MessageGenerationService messageGenerationService;

  @Mock
  private JmsTemplate jmsTemplate;

  @Mock
  private ConnectionFactory connectionFactory;

  @Mock
  private ConnectionManager connectionManager;

  @Mock
  private JmsTemplateFactory jmsTemplateFactory;

  @BeforeEach
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    jmsTemplate.setConnectionFactory(connectionFactory);

    MessageSender messageSender = new MessageSender(mock(MessageTimingsService.class));

    service = new IbmMqServiceImpl(
        mock(ReceivedMessagesService.class),
        jmsTemplateFactory,
        connectionManager,
        loadProfileStorageService,
        generatedMessageStorageService,
        messageGenerationService,
        messageSender);
  }

  @Test
  @DisplayName("""
      Given that a load profile is not provided
      When the service generateLoad() method is called with this request
      Then the service should throw a NoLoadProfileException
      """)
  void shouldThrowNoLoadProfileExceptionWhenLoadProfileIsNotProvided() {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();
    Set<ConstraintViolation<GenerateLoadIbmMqRequestDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty()); // Expect validation errors
    assertThrows(NoLoadProfileException.class, () -> service.generateLoad(dto));
  }

  @Test
  @DisplayName("""
      Given that a load profile is provided but is empty
      When the service generateLoad() method is called with this request
      Then the service should throw a NoLoadProfileException
      """)
  void shouldThrowNoLoadProfileExceptionWhenLoadProfileIsProvidedButEmpty() {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();
    LoadProfile loadProfile = new LoadProfile();
    dto.setLoadProfile(loadProfile);
    Set<ConstraintViolation<GenerateLoadIbmMqRequestDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty()); // Expect validation errors
    assertThrows(NoLoadProfileException.class, () -> service.generateLoad(dto));
  }

  @Test
  @DisplayName("""
      Given that a load profile is provided without a NAL
      When the service generateLoad() method is called with this request
      Then the service should return a validation error
      And indicate that the NAL field is required
      """)
  void shouldThrowValidationErrorWhenNALFieldIsMissingFromLoadProfile() {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();

    Profile profile = LoadProfileFixtureFactory.defaultProfileBuilder()
        .nal(List.of())
        .priority(List.of())
        .size(List.of(MessageSize.SMALL))
        .build();
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();

    dto.setLoadProfile(loadProfile);

    Set<ConstraintViolation<GenerateLoadIbmMqRequestDto>> violations = validator.validate(dto);

    assertEquals(1, violations.size());
    ConstraintViolation<GenerateLoadIbmMqRequestDto> theViolation = violations.iterator().next();
    assertEquals("NAL is required", theViolation.getMessage());
  }

  @Test
  @DisplayName("""
      Given that a load profile is provided without an Origin
      When the service generateLoad() method is called with this request
      Then the service should return a validation error
      And indicate that the Origin field has not been provided
      """)
  void shouldReturnValidationErrorWhenOriginFieldIsMissingFromLoadProfile() {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();

    Profile profile = LoadProfileFixtureFactory.defaultProfileBuilder()
        .nal(List.of("HDQRIUX"))
        .priority(List.of("QN"))
        .origin(null).build();
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();

    dto.setLoadProfile(loadProfile);

    Set<ConstraintViolation<GenerateLoadIbmMqRequestDto>> violations = validator.validate(dto);

    assertEquals(1, violations.size());
    ConstraintViolation<GenerateLoadIbmMqRequestDto> theViolation = violations.iterator().next();
    assertEquals("Origin is required", theViolation.getMessage());
  }

  @Test
  @DisplayName("""
      Given that a load profile is provided without a NAL and without an Origin
      When the service generateLoad() method is called with this request
      Then the service should return a validation error
      And indicate that the Origin and NAL fields have not been provided
      """)
  void shouldReturnTwoValidationErrorsWhenOriginAndNALFieldsAreMissingFromLoadProfile() {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();

    Profile profile = LoadProfileFixtureFactory.defaultProfileBuilder()
        .nal(List.of())
        .priority(List.of("QD"))
        .origin(new ArrayList<>()).build();
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile)).build();

    dto.setLoadProfile(loadProfile);
    Set<ConstraintViolation<GenerateLoadIbmMqRequestDto>> violations = validator.validate(dto);

    assertEquals(2, violations.size());

    Set<String> expectedErrors = Set.of("NAL is required", "Origin is required");

    Set<String> actualMessages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    assertTrue(actualMessages.containsAll(expectedErrors), "Some expected errors are missing");
  }

  @Test
  @DisplayName("""
      Given that 2 load profiles are provided
      And the first load profile is missing the origin field
      And the second load profile is missing the NAL field
      When the service generateLoad() method is called with this request
      Then the service should return a validation error
      And indicate that the origin field has not been provided for the first load profile
      And that the NAL field has not been provided for the second load profile
      """)
  void shouldReturnValidationErrorWhenTwoLoadProfilesHaveMissingMandatoryFields() {
    GenerateLoadIbmMqRequestDto dto = new GenerateLoadIbmMqRequestDto();

    Profile profile1 = LoadProfileFixtureFactory.defaultProfileBuilder()
        .nal(List.of("ABCDEF", "BCDEFG"))
        .priority(List.of("QD", "QN")) // No priority
        .origin(null)
        .size(List.of(MessageSize.MEDIUM)).build();

    // Add second profile with different origin
    Profile profile2 = LoadProfileFixtureFactory.defaultProfileBuilder()
        .nal(null)
        .origin(List.of("ATLXTXS"))
        .size(List.of(MessageSize.LARGE)).build();

    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder()
        .profiles(List.of(profile1, profile2)).build();

    dto.setLoadProfile(loadProfile);

    Set<ConstraintViolation<GenerateLoadIbmMqRequestDto>> violations = validator.validate(dto);
    assertEquals(2, violations.size());
    Set<String> expectedErrors = Set.of("NAL is required", "Origin is required");

    Set<String> actualMessages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    assertTrue(actualMessages.containsAll(expectedErrors), "Some expected errors are missing");
  }

  @Test
  @DisplayName("""
      Given that no load has been generated
      When the service injectLoad() method is called with an id
      Then the service should throw a NoMessagesToInjectException
      """)
  void shouldThrowNoMessagesToInjectExceptionWhenNoLoadHasBeenGenerated() {
    SendMessageIbmMqRequestDto request = new SendMessageIbmMqRequestDto();
    assertThrows(NoGeneratedMessagesFoundException.class, () -> service.injectLoad(123L, request));
  }

  @Test
  void shouldGenerateLoadWhenValidRequestProvided() {
    Long loadProfileId = 1234L;
    LoadProfile loadProfile = LoadProfileFixtureFactory.defaultLoadProfileBuilder().id(loadProfileId).build();

    GeneratedMessage generatedMessage = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder()
        .loadProfileId(loadProfileId)
        .build();
    // Set up mock responses
    doReturn(loadProfile).when(loadProfileStorageService).save(loadProfile);
    doReturn(List.of(generatedMessage.getMessage()))
        .when(messageGenerationService).generateTypeBMessagesFromLoadProfile(loadProfile);
    doReturn(generatedMessage).when(generatedMessageStorageService).save(generatedMessage);

    GenerateLoadIbmMqRequestDto request = new GenerateLoadIbmMqRequestDto();
    request.setLoadProfile(loadProfile);

    service.generateLoad(request);

    verify(messageGenerationService, times(1)).generateTypeBMessagesFromLoadProfile(any());
    verify(generatedMessageStorageService, times(1)).save(any());
  }

  @Test
  @DisplayName("""
      Given that we have a load of GeneratedMessages to inject
       When we inject the messages
       Then the GenerateMessageStorageService should be called to retrieve the load
       And the IbmMqService sendMessages() method should be called to send the messages
      """)
  void shouldInjectLoadWhenValidRequestIsProvided() {
    SendMessageIbmMqRequestDto request = SendMessageIbmMqRequestFixtureFactory.defaultSendMessageIbmMqRequest();
    GeneratedMessage generatedMessage = GeneratedMessageFixtureFactory.defaultGeneratedMessageBuilder()
        .id("asdfghjkl12345").build();

    when(generatedMessageStorageService.getByLoadProfileId(123L)).thenReturn(List.of(generatedMessage));

    // Added to verify that the sendMessages() method was called
    IbmMqService spyService = spy(service);

    // For this test, Do not attempt to send the messages being injected
    doNothing().when(spyService).sendMessages(any(SendMessageIbmMqRequest.class), anyList());

    // Inject the same load 3 times
    spyService.injectLoad(123L, request);
    spyService.injectLoad(123L, request);
    spyService.injectLoad(123L, request);

    // Verify generated messages were retrieved using service
    verify(generatedMessageStorageService, times(3)).getByLoadProfileId(123L);
    // Verify sendMessages() was called
    verify(spyService, times(3)).sendMessages(any(SendMessageIbmMqRequest.class), anyList());
  }
}