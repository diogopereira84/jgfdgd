@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  test-harness startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and TEST_HARNESS_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\test-harness-plain.jar;%APP_HOME%\lib\dispatcher-starter.jar;%APP_HOME%\lib\common.jar;%APP_HOME%\lib\shared-models.jar;%APP_HOME%\lib\mq-jms-spring-boot-starter-3.4.3.jar;%APP_HOME%\lib\spring-boot-starter-data-mongodb-3.4.4.jar;%APP_HOME%\lib\spring-boot-starter-validation-3.4.9.jar;%APP_HOME%\lib\spring-boot-starter-web-3.4.9.jar;%APP_HOME%\lib\spring-boot-starter-json-3.4.9.jar;%APP_HOME%\lib\spring-cloud-starter-openfeign-4.3.0.jar;%APP_HOME%\lib\spring-cloud-starter-4.3.0.jar;%APP_HOME%\lib\spring-cloud-azure-starter-servicebus-5.22.0.jar;%APP_HOME%\lib\spring-cloud-azure-starter-5.22.0.jar;%APP_HOME%\lib\spring-boot-starter-3.5.0.jar;%APP_HOME%\lib\logback-ecs-encoder-1.7.0.jar;%APP_HOME%\lib\awaitility-4.2.0.jar;%APP_HOME%\lib\mapstruct-1.6.3.jar;%APP_HOME%\lib\springdoc-openapi-starter-webmvc-ui-2.8.6.jar;%APP_HOME%\lib\spring-boot-starter-log4j2-3.4.9.jar;%APP_HOME%\lib\spring-kafka-3.3.9.jar;%APP_HOME%\lib\spring-cloud-openfeign-core-4.3.0.jar;%APP_HOME%\lib\springdoc-openapi-starter-webmvc-api-2.8.6.jar;%APP_HOME%\lib\springdoc-openapi-starter-common-2.8.6.jar;%APP_HOME%\lib\spring-cloud-azure-autoconfigure-5.22.0.jar;%APP_HOME%\lib\spring-boot-autoconfigure-3.5.0.jar;%APP_HOME%\lib\spring-boot-3.5.0.jar;%APP_HOME%\lib\spring-boot-starter-tomcat-3.4.9.jar;%APP_HOME%\lib\jakarta.annotation-api-2.1.1.jar;%APP_HOME%\lib\spring-jms-6.2.3.jar;%APP_HOME%\lib\spring-data-mongodb-4.4.4.jar;%APP_HOME%\lib\spring-webmvc-6.2.10.jar;%APP_HOME%\lib\feign-form-spring-13.6.jar;%APP_HOME%\lib\spring-web-6.2.10.jar;%APP_HOME%\lib\spring-cloud-azure-service-5.22.0.jar;%APP_HOME%\lib\spring-cloud-azure-core-5.22.0.jar;%APP_HOME%\lib\spring-context-6.2.10.jar;%APP_HOME%\lib\spring-messaging-6.2.10.jar;%APP_HOME%\lib\spring-tx-6.2.10.jar;%APP_HOME%\lib\spring-data-commons-3.4.4.jar;%APP_HOME%\lib\spring-aop-6.2.10.jar;%APP_HOME%\lib\spring-beans-6.2.10.jar;%APP_HOME%\lib\spring-expression-6.2.10.jar;%APP_HOME%\lib\spring-core-6.2.10.jar;%APP_HOME%\lib\swagger-core-jakarta-2.2.29.jar;%APP_HOME%\lib\jackson-datatype-jdk8-2.18.4.jar;%APP_HOME%\lib\azure-messaging-servicebus-7.17.10.jar;%APP_HOME%\lib\azure-core-amqp-2.9.16.jar;%APP_HOME%\lib\azure-identity-1.15.4.jar;%APP_HOME%\lib\azure-core-http-netty-1.15.11.jar;%APP_HOME%\lib\azure-core-management-1.17.0.jar;%APP_HOME%\lib\azure-core-1.55.3.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.18.4.jar;%APP_HOME%\lib\jackson-module-parameter-names-2.18.4.jar;%APP_HOME%\lib\msal4j-persistence-extension-1.3.0.jar;%APP_HOME%\lib\msal4j-1.19.1.jar;%APP_HOME%\lib\jackson-databind-2.18.4.jar;%APP_HOME%\lib\swagger-models-jakarta-2.2.29.jar;%APP_HOME%\lib\jackson-annotations-2.18.4.jar;%APP_HOME%\lib\jackson-core-2.18.4.jar;%APP_HOME%\lib\jackson-dataformat-yaml-2.18.4.jar;%APP_HOME%\lib\snakeyaml-2.4.jar;%APP_HOME%\lib\spring-aspects-6.2.6.jar;%APP_HOME%\lib\micrometer-registry-otlp-1.15.0.jar;%APP_HOME%\lib\ecs-logging-core-1.7.0.jar;%APP_HOME%\lib\com.ibm.mq.jakarta.client-9.4.2.0.jar;%APP_HOME%\lib\pooled-jms-3.1.7.jar;%APP_HOME%\lib\hamcrest-2.1.jar;%APP_HOME%\lib\swagger-ui-5.20.1.jar;%APP_HOME%\lib\webjars-locator-lite-1.0.1.jar;%APP_HOME%\lib\mongodb-driver-sync-5.2.1.jar;%APP_HOME%\lib\tomcat-embed-el-10.1.44.jar;%APP_HOME%\lib\hibernate-validator-8.0.3.Final.jar;%APP_HOME%\lib\spring-cloud-commons-4.3.0.jar;%APP_HOME%\lib\feign-slf4j-13.6.jar;%APP_HOME%\lib\feign-form-13.6.jar;%APP_HOME%\lib\feign-core-13.6.jar;%APP_HOME%\lib\spring-retry-2.0.12.jar;%APP_HOME%\lib\kafka-clients-3.8.1.jar;%APP_HOME%\lib\micrometer-core-1.15.0.jar;%APP_HOME%\lib\micrometer-observation-1.15.0.jar;%APP_HOME%\lib\spring-jcl-6.2.10.jar;%APP_HOME%\lib\aspectjweaver-1.9.22.1.jar;%APP_HOME%\lib\opentelemetry-proto-1.5.0-alpha.jar;%APP_HOME%\lib\bcpkix-jdk18on-1.80.jar;%APP_HOME%\lib\bcutil-jdk18on-1.80.jar;%APP_HOME%\lib\bcprov-jdk18on-1.80.jar;%APP_HOME%\lib\jakarta.jms-api-3.1.0.jar;%APP_HOME%\lib\json-20250107.jar;%APP_HOME%\lib\qpid-proton-j-extensions-1.2.5.jar;%APP_HOME%\lib\log4j-slf4j2-impl-2.24.3.jar;%APP_HOME%\lib\slf4j-api-2.0.17.jar;%APP_HOME%\lib\commons-pool2-2.12.0.jar;%APP_HOME%\lib\jspecify-1.0.0.jar;%APP_HOME%\lib\mongodb-driver-core-5.2.1.jar;%APP_HOME%\lib\bson-record-codec-5.2.1.jar;%APP_HOME%\lib\bson-5.2.1.jar;%APP_HOME%\lib\jakarta.validation-api-3.0.2.jar;%APP_HOME%\lib\jboss-logging-3.4.3.Final.jar;%APP_HOME%\lib\classmate-1.5.1.jar;%APP_HOME%\lib\tomcat-embed-websocket-10.1.44.jar;%APP_HOME%\lib\tomcat-embed-core-10.1.44.jar;%APP_HOME%\lib\spring-cloud-context-4.3.0.jar;%APP_HOME%\lib\spring-security-crypto-6.5.0.jar;%APP_HOME%\lib\zstd-jni-1.5.6-4.jar;%APP_HOME%\lib\lz4-java-1.8.0.jar;%APP_HOME%\lib\snappy-java-1.1.10.5.jar;%APP_HOME%\lib\micrometer-commons-1.15.0.jar;%APP_HOME%\lib\HdrHistogram-2.2.2.jar;%APP_HOME%\lib\LatencyUtils-2.0.3.jar;%APP_HOME%\lib\protobuf-java-4.28.3.jar;%APP_HOME%\lib\azure-xml-1.2.0.jar;%APP_HOME%\lib\commons-text-1.13.0.jar;%APP_HOME%\lib\commons-fileupload-1.5.jar;%APP_HOME%\lib\azure-json-1.5.0.jar;%APP_HOME%\lib\reactor-netty-http-1.0.48.jar;%APP_HOME%\lib\reactor-netty-core-1.0.48.jar;%APP_HOME%\lib\reactor-core-3.4.41.jar;%APP_HOME%\lib\proton-j-0.34.1.jar;%APP_HOME%\lib\netty-handler-proxy-4.1.118.Final.jar;%APP_HOME%\lib\netty-codec-http2-4.1.118.Final.jar;%APP_HOME%\lib\netty-codec-http-4.1.118.Final.jar;%APP_HOME%\lib\netty-resolver-dns-native-macos-4.1.112.Final-osx-x86_64.jar;%APP_HOME%\lib\netty-resolver-dns-classes-macos-4.1.112.Final.jar;%APP_HOME%\lib\netty-resolver-dns-4.1.112.Final.jar;%APP_HOME%\lib\netty-handler-4.1.118.Final.jar;%APP_HOME%\lib\netty-codec-socks-4.1.118.Final.jar;%APP_HOME%\lib\netty-codec-dns-4.1.112.Final.jar;%APP_HOME%\lib\netty-codec-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-native-epoll-4.1.118.Final-linux-x86_64.jar;%APP_HOME%\lib\netty-transport-native-kqueue-4.1.118.Final-osx-x86_64.jar;%APP_HOME%\lib\netty-transport-classes-epoll-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-classes-kqueue-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-4.1.118.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.118.Final.jar;%APP_HOME%\lib\netty-tcnative-boringssl-static-2.0.70.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.118.Final.jar;%APP_HOME%\lib\netty-common-4.1.118.Final.jar;%APP_HOME%\lib\commons-lang3-3.17.0.jar;%APP_HOME%\lib\swagger-annotations-jakarta-2.2.29.jar;%APP_HOME%\lib\jakarta.xml.bind-api-3.0.1.jar;%APP_HOME%\lib\reactive-streams-1.0.4.jar;%APP_HOME%\lib\netty-tcnative-classes-2.0.70.Final.jar;%APP_HOME%\lib\jakarta.activation-2.0.1.jar;%APP_HOME%\lib\jna-platform-5.13.0.jar;%APP_HOME%\lib\oauth2-oidc-sdk-11.23.jar;%APP_HOME%\lib\json-smart-2.5.2.jar;%APP_HOME%\lib\jna-5.13.0.jar;%APP_HOME%\lib\jcip-annotations-1.0-1.jar;%APP_HOME%\lib\content-type-2.3.jar;%APP_HOME%\lib\lang-tag-1.7.jar;%APP_HOME%\lib\nimbus-jose-jwt-10.0.1.jar;%APP_HOME%\lib\accessors-smart-2.5.2.jar;%APP_HOME%\lib\asm-9.7.1.jar;%APP_HOME%\lib\log4j-core-2.24.3.jar;%APP_HOME%\lib\log4j-jul-2.24.3.jar;%APP_HOME%\lib\log4j-api-2.24.3.jar


@rem Execute test-harness
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %TEST_HARNESS_OPTS%  -classpath "%CLASSPATH%"  %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable TEST_HARNESS_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%TEST_HARNESS_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
