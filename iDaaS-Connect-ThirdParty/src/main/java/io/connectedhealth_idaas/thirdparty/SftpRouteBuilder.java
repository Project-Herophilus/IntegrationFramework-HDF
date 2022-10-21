/*
 * Copyright 2019 Project-Herophilus
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.connectedhealth_idaas.thirdparty;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SftpRouteBuilder extends RouteBuilder {

  public static final String HL7_ROUTE_ID = "hl7-third-party";
  public static final String DQAAS_ROUTE_ID = "dqass-third-party";

  @Override
  public void configure() throws Exception {

    onException(Exception.class)
    .log(LoggingLevel.ERROR,"${exception}")
    .to("micrometer:counter:sftp_exception_handled");


    from("sftp:{{sftp.host}}:{{sftp.port}}/{{sftp.hl7.dir}}?username={{sftp.username}}&password={{sftp.password}}&move={{sftp.dir.processed}}&moveFailed={{sftp.dir.error}}&include=^.*\\.(dat|hl7)$")
    .routeId(HL7_ROUTE_ID)
    .to("log:"+ HL7_ROUTE_ID + "?showAll=true")
    .to("kafka:SftpFiles?brokers={{idaas.kafka.brokers}}")
    .log("${exchangeId} fully processed")
    .to("micrometer:counter:num_processed_files");

    from("sftp:{{sftp.host}}:{{sftp.port}}/{{sftp.dqaas.dir}}?username={{sftp.username}}&password={{sftp.password}}&move={{sftp.dir.processed}}&moveFailed={{sftp.dir.error}}")
    .routeId(DQAAS_ROUTE_ID)
    .to("log:"+ DQAAS_ROUTE_ID + "?showAll=true")
    .to("sftp:{{sftp.host}}:{{sftp.port}}/{{sftp.ct.dir}}?username={{sftp.username}}&password={{sftp.password}}")
    .log("${exchangeId} fully processed")
    .to("micrometer:counter:num_processed_files");

  }
}
