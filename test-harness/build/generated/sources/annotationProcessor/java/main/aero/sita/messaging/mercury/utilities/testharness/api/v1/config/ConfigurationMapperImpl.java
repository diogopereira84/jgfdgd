package aero.sita.messaging.mercury.utilities.testharness.api.v1.config;

import aero.sita.messaging.mercury.utilities.testharness.api.v1.config.dto.ConnectionConfigurationDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.config.dto.GetServersResponseDto;
import aero.sita.messaging.mercury.utilities.testharness.api.v1.config.dto.ServerConfigurationDto;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ConnectionConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServerConfiguration;
import aero.sita.messaging.mercury.utilities.testharness.domain.ibmmq.ServersConfiguration;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-11T08:20:51-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class ConfigurationMapperImpl implements ConfigurationMapper {

    @Override
    public GetServersResponseDto toDto(ServersConfiguration serversConfiguration) {
        if ( serversConfiguration == null ) {
            return null;
        }

        GetServersResponseDto.GetServersResponseDtoBuilder getServersResponseDto = GetServersResponseDto.builder();

        getServersResponseDto.serverConfigurations( stringServerConfigurationMapToStringServerConfigurationDtoHashMap( serversConfiguration.getServerConfigurations() ) );

        return getServersResponseDto.build();
    }

    @Override
    public ConnectionConfigurationDto toDto(ConnectionConfiguration connectionConfiguration) {
        if ( connectionConfiguration == null ) {
            return null;
        }

        ConnectionConfigurationDto.ConnectionConfigurationDtoBuilder connectionConfigurationDto = ConnectionConfigurationDto.builder();

        connectionConfigurationDto.enabled( map( connectionConfiguration.getState() ) );
        connectionConfigurationDto.id( connectionConfiguration.getId() );
        connectionConfigurationDto.inboundQueueName( connectionConfiguration.getInboundQueueName() );
        connectionConfigurationDto.outboundQueueName( connectionConfiguration.getOutboundQueueName() );
        connectionConfigurationDto.concurrencyMin( connectionConfiguration.getConcurrencyMin() );
        connectionConfigurationDto.concurrencyMax( connectionConfiguration.getConcurrencyMax() );

        return connectionConfigurationDto.build();
    }

    protected HashMap<String, ConnectionConfigurationDto> stringConnectionConfigurationMapToStringConnectionConfigurationDtoHashMap(Map<String, ConnectionConfiguration> map) {
        if ( map == null ) {
            return null;
        }

        HashMap<String, ConnectionConfigurationDto> hashMap = new HashMap<String, ConnectionConfigurationDto>();

        for ( java.util.Map.Entry<String, ConnectionConfiguration> entry : map.entrySet() ) {
            String key = entry.getKey();
            ConnectionConfigurationDto value = toDto( entry.getValue() );
            hashMap.put( key, value );
        }

        return hashMap;
    }

    protected ServerConfigurationDto serverConfigurationToServerConfigurationDto(ServerConfiguration serverConfiguration) {
        if ( serverConfiguration == null ) {
            return null;
        }

        ServerConfigurationDto.ServerConfigurationDtoBuilder serverConfigurationDto = ServerConfigurationDto.builder();

        serverConfigurationDto.id( serverConfiguration.getId() );
        serverConfigurationDto.hostname( serverConfiguration.getHostname() );
        serverConfigurationDto.port( serverConfiguration.getPort() );
        serverConfigurationDto.queueManager( serverConfiguration.getQueueManager() );
        serverConfigurationDto.channel( serverConfiguration.getChannel() );
        serverConfigurationDto.user( serverConfiguration.getUser() );
        serverConfigurationDto.password( serverConfiguration.getPassword() );
        serverConfigurationDto.connections( stringConnectionConfigurationMapToStringConnectionConfigurationDtoHashMap( serverConfiguration.getConnections() ) );

        return serverConfigurationDto.build();
    }

    protected HashMap<String, ServerConfigurationDto> stringServerConfigurationMapToStringServerConfigurationDtoHashMap(Map<String, ServerConfiguration> map) {
        if ( map == null ) {
            return null;
        }

        HashMap<String, ServerConfigurationDto> hashMap = new HashMap<String, ServerConfigurationDto>();

        for ( java.util.Map.Entry<String, ServerConfiguration> entry : map.entrySet() ) {
            String key = entry.getKey();
            ServerConfigurationDto value = serverConfigurationToServerConfigurationDto( entry.getValue() );
            hashMap.put( key, value );
        }

        return hashMap;
    }
}
