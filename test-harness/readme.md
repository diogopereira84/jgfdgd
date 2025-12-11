# Test Harness

Welcome to the Test Harness for Project Mercury.

## Environment Variables

This project can send and receive messages from multiple IBM MQ servers, but all servers must be active at startup. Each
server can have multiple connections to queues available for API endpoints.

### Spring Profiles

- **Default Profile**:
    - Server points to `localhost:1414` with queue manager `QM1` and channel `APP.SVRCONN`.
    - Connections: `DEV.QUEUE.1`, `DEV.QUEUE.2`, `DEV.QUEUE.3`.
    - Should be used when developing/testing locally.

- **Dev Profile**:
    - Server points to `${IBM_MQ_HOSTNAME}:1414` with queue manager `QM1` and channel `APP.SVRCONN`.
    - Connections: `DEV.QUEUE.1`, `DEV.QUEUE.2`.
    - Should be used in azure dev environment.

### Setting Environment Variables

You can set an environment variable in PowerShell as follows:

```powershell
[System.Environment]::SetEnvironmentVariable("SERVERS_0_PASSWORD", "replace_with_the_password", "User")
```

To read the value of an environment variable:

```powershell
(Get-Item Env:SERVERS_0_PASSWORD).Value
```

**Note**: After setting an environment variable, you must run commands in a new PowerShell session for changes to take
effect.

### Overriding Spring Profile Servers

To override the default servers, set all required properties. Existing server configurations will be ignored. Required
properties include:

- `IBM_MQ_SERVERS_0_CONNECTIONS_0_CONCURRENCY_MAX`
- `IBM_MQ_SERVERS_0_CHANNEL`
- `IBM_MQ_SERVERS_0_CONNECTIONS_0_CONCURRENCY_MIN`
- `IBM_MQ_SERVERS_0_CONNECTIONS_0_ID`
- `IBM_MQ_SERVERS_0_CONNECTIONS_0_INBOUND_QUEUE_NAME`
- `IBM_MQ_SERVERS_0_CONNECTIONS_0_OUTBOUND_QUEUE_NAME`
- `IBM_MQ_SERVERS_0_CONNECTIONS_0_STATE`
- `IBM_MQ_SERVERS_0_HOSTNAME`
- `IBM_MQ_SERVERS_0_ID`
- `IBM_MQ_SERVERS_0_USER`
- `IBM_MQ_SERVERS_0_PASSWORD`
- `IBM_MQ_SERVERS_0_PORT`
- `IBM_MQ_SERVERS_0_QUEUE_MANAGER`

**Important**: The array index starts at 0. Use the inbound queue name for message discovery; the outbound queue name
can be ignored. Set concurrency min and max to 1, and state to ENABLED. Use `APP.SVRCONN` for the channel.

### Example: Setting a New Server

To set a new server in the local spring profile:

```powershell
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_ID", "server1", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_HOSTNAME", "localhost", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_PORT", "1414", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_QUEUE_MANAGER", "QM1", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CHANNEL", "APP.SVRCONN", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_PASSWORD", "replace_with_the_password", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_USER", "replace_with_the_user", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_ID", "connection1", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_INBOUND_QUEUE_NAME", "DEV.QUEUE.1", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_OUTBOUND_QUEUE_NAME", "", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_CONCURRENCY_MIN", "1", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_CONCURRENCY_MAX", "1", "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_STATE", "ENABLED", "User")
```

### Deleting Old Environment Variables

To remove old environment variables:

```powershell
[System.Environment]::SetEnvironmentVariable("SERVERS_0_PASSWORD", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("SERVERS_0_USER", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("SERVERS_0_USER", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_ID", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_HOSTNAME", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_PORT", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_QUEUE_MANAGER", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CHANNEL", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_PASSWORD", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_USER", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_ID", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_INBOUND_QUEUE_NAME", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_OUTBOUND_QUEUE_NAME", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_CONCURRENCY_MIN", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_CONCURRENCY_MAX", [NullString]::Value, "User")
[System.Environment]::SetEnvironmentVariable("IBM_MQ_SERVERS_0_CONNECTIONS_0_STATE", [NullString]::Value, "User")
```

## Swagger UI

You can access the Swagger UI at [Swagger UI](http://localhost:8082/test-harness/swagger-ui.html).

## Running the Application

To run the application from the root folder, use the following command:

```powershell
.\gradlew.bat :utilities:test-harness:bootRun --parallel
```
