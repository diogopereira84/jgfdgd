package aero.sita.messaging.mercury.utilities.testharness.api.v1.result;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.DeliveredMessagesDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.LatencyRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.MessageDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.ReceivedMessageDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.result.dto.ResultResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.DeliveredMessages;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.LatencyRequest;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Message;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.ReceivedMessage;
import aero.sita.messaging.mercury.utilities.testharness.domain.result.Result;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-11T08:20:51-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class ResultsMapperImpl implements ResultsMapper {

    @Override
    public ResultResponseDto toDto(Result result) {
        if ( result == null ) {
            return null;
        }

        ResultResponseDto resultResponseDto = new ResultResponseDto();

        resultResponseDto.setId( result.getId() );
        resultResponseDto.setLoadProfileId( result.getLoadProfileId() );
        resultResponseDto.setElapsedTimeInMilliseconds( result.getElapsedTimeInMilliseconds() );
        resultResponseDto.setActualMessageCount( result.getActualMessageCount() );

        return resultResponseDto;
    }

    @Override
    public DeliveredMessagesDto toDto(DeliveredMessages deliveredMessages) {
        if ( deliveredMessages == null ) {
            return null;
        }

        DeliveredMessagesDto.DeliveredMessagesDtoBuilder deliveredMessagesDto = DeliveredMessagesDto.builder();

        deliveredMessagesDto.messages( toDto( deliveredMessages.getMessages() ) );
        deliveredMessagesDto.totalMessagesDelivered( deliveredMessages.getTotalMessagesDelivered() );

        return deliveredMessagesDto.build();
    }

    @Override
    public MessageDto toDto(Message message) {
        if ( message == null ) {
            return null;
        }

        MessageDto.MessageDtoBuilder messageDto = MessageDto.builder();

        messageDto.body( message.getBody() );
        messageDto.jmsTimestamp( message.getJmsTimestamp() );

        return messageDto.build();
    }

    @Override
    public Map<String, List<MessageDto>> toDto(Map<String, List<Message>> value) {
        if ( value == null ) {
            return null;
        }

        Map<String, List<MessageDto>> map = LinkedHashMap.newLinkedHashMap( value.size() );

        for ( java.util.Map.Entry<String, List<Message>> entry : value.entrySet() ) {
            String key = entry.getKey();
            List<MessageDto> value1 = toDto( entry.getValue() );
            map.put( key, value1 );
        }

        return map;
    }

    @Override
    public List<MessageDto> toDto(List<Message> value) {
        if ( value == null ) {
            return null;
        }

        List<MessageDto> list = new ArrayList<MessageDto>( value.size() );
        for ( Message message : value ) {
            list.add( toDto( message ) );
        }

        return list;
    }

    @Override
    public ReceivedMessageDto toDto(ReceivedMessage receivedMessage) {
        if ( receivedMessage == null ) {
            return null;
        }

        ReceivedMessageDto receivedMessageDto = new ReceivedMessageDto();

        receivedMessageDto.setId( receivedMessage.getId() );
        receivedMessageDto.setHandOffTimestamp( receivedMessage.getHandOffTimestamp() );
        receivedMessageDto.setProtocol( receivedMessage.getProtocol() );
        receivedMessageDto.setBody( receivedMessage.getBody() );
        receivedMessageDto.setConnectionName( receivedMessage.getConnectionName() );
        receivedMessageDto.setQueueName( receivedMessage.getQueueName() );
        receivedMessageDto.setInjectionId( receivedMessage.getInjectionId() );

        return receivedMessageDto;
    }

    @Override
    public LatencyRequest toDomainObject(LatencyRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        LatencyRequest.LatencyRequestBuilder latencyRequest = LatencyRequest.builder();

        latencyRequest.injectionId( dto.getInjectionId() );
        latencyRequest.atMostInSeconds( dto.getAtMostInSeconds() );
        latencyRequest.pollDelayInSeconds( dto.getPollDelayInSeconds() );
        latencyRequest.pollIntervalInSeconds( dto.getPollIntervalInSeconds() );

        return latencyRequest.build();
    }
}
