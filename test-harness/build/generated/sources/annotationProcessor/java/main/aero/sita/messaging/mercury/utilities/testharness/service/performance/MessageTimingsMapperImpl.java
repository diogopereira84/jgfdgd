package aero.sita.messaging.mercury.utilities.testharness.service.performance;

import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimings;
import aero.sita.messaging.mercury.utilities.testharness.domain.performance.MessageTimingsDocument;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-11T08:20:52-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class MessageTimingsMapperImpl implements MessageTimingsMapper {

    @Override
    public MessageTimings toDomainObject(MessageTimingsDocument messageTimingsDocument) {
        if ( messageTimingsDocument == null ) {
            return null;
        }

        MessageTimings.MessageTimingsBuilder messageTimings = MessageTimings.builder();

        messageTimings.correlationId( messageTimingsDocument.getCorrelationId() );
        messageTimings.injectionId( messageTimingsDocument.getInjectionId() );
        messageTimings.testHarnessMessageId( messageTimingsDocument.getTestHarnessMessageId() );
        messageTimings.customerToEndpointPublishTimestamp( messageTimingsDocument.getCustomerToEndpointPublishTimestamp() );
        messageTimings.endpointToNormalizerPublishTimestamp( messageTimingsDocument.getEndpointToNormalizerPublishTimestamp() );
        messageTimings.endpointToCustomerConsumeTimestamp( messageTimingsDocument.getEndpointToCustomerConsumeTimestamp() );
        if ( messageTimingsDocument.getTimeToEndpointInboundPublish() != null ) {
            messageTimings.timeToEndpointInboundPublish( messageTimingsDocument.getTimeToEndpointInboundPublish() );
        }
        if ( messageTimingsDocument.getMessageAcceptanceLatency() != null ) {
            messageTimings.messageAcceptanceLatency( messageTimingsDocument.getMessageAcceptanceLatency() );
        }
        if ( messageTimingsDocument.getExternalEndToEndLatency() != null ) {
            messageTimings.externalEndToEndLatency( messageTimingsDocument.getExternalEndToEndLatency() );
        }

        return messageTimings.build();
    }

    @Override
    public MessageTimingsDocument toDocumentObject(MessageTimings messageTimings) {
        if ( messageTimings == null ) {
            return null;
        }

        MessageTimingsDocument.MessageTimingsDocumentBuilder messageTimingsDocument = MessageTimingsDocument.builder();

        messageTimingsDocument.testHarnessMessageId( messageTimings.getTestHarnessMessageId() );
        messageTimingsDocument.correlationId( messageTimings.getCorrelationId() );
        messageTimingsDocument.injectionId( messageTimings.getInjectionId() );
        messageTimingsDocument.customerToEndpointPublishTimestamp( messageTimings.getCustomerToEndpointPublishTimestamp() );
        messageTimingsDocument.endpointToNormalizerPublishTimestamp( messageTimings.getEndpointToNormalizerPublishTimestamp() );
        messageTimingsDocument.endpointToCustomerConsumeTimestamp( messageTimings.getEndpointToCustomerConsumeTimestamp() );
        messageTimingsDocument.timeToEndpointInboundPublish( messageTimings.getTimeToEndpointInboundPublish() );
        messageTimingsDocument.messageAcceptanceLatency( messageTimings.getMessageAcceptanceLatency() );
        messageTimingsDocument.externalEndToEndLatency( messageTimings.getExternalEndToEndLatency() );

        return messageTimingsDocument.build();
    }
}
