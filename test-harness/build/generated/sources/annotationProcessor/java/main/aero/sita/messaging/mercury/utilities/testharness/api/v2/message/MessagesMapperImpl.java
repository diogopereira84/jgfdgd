package aero.sita.messaging.mercury.utilities.testharness.api.v2.message;

import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.DestinationDetailsDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v2.message.dto.SendMessagesRequestDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.DestinationDetails;
import aero.sita.messaging.mercury.utilities.testharness.domain.SendMessageIbmMqRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-11T08:20:52-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class MessagesMapperImpl implements MessagesMapper {

    @Override
    public SendMessageIbmMqRequest toDomainObject(SendMessagesRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        SendMessageIbmMqRequest.SendMessageIbmMqRequestBuilder sendMessageIbmMqRequest = SendMessageIbmMqRequest.builder();

        sendMessageIbmMqRequest.destinationsDetailsList( destinationDetailsDtoListToDestinationDetailsList( dto.getDestinationsDetailsList() ) );
        sendMessageIbmMqRequest.loadProfileId( dto.getLoadProfileId() );
        sendMessageIbmMqRequest.preLoad( dto.isPreLoad() );
        sendMessageIbmMqRequest.delayBetweenMessagesInMilliseconds( dto.getDelayBetweenMessagesInMilliseconds() );

        return sendMessageIbmMqRequest.build();
    }

    protected DestinationDetails destinationDetailsDtoToDestinationDetails(DestinationDetailsDto destinationDetailsDto) {
        if ( destinationDetailsDto == null ) {
            return null;
        }

        DestinationDetails.DestinationDetailsBuilder destinationDetails = DestinationDetails.builder();

        destinationDetails.server( destinationDetailsDto.getServer() );
        destinationDetails.port( destinationDetailsDto.getPort() );
        List<String> list = destinationDetailsDto.getDestinationNames();
        if ( list != null ) {
            destinationDetails.destinationNames( new ArrayList<String>( list ) );
        }

        return destinationDetails.build();
    }

    protected List<DestinationDetails> destinationDetailsDtoListToDestinationDetailsList(List<DestinationDetailsDto> list) {
        if ( list == null ) {
            return null;
        }

        List<DestinationDetails> list1 = new ArrayList<DestinationDetails>( list.size() );
        for ( DestinationDetailsDto destinationDetailsDto : list ) {
            list1.add( destinationDetailsDtoToDestinationDetails( destinationDetailsDto ) );
        }

        return list1;
    }
}
