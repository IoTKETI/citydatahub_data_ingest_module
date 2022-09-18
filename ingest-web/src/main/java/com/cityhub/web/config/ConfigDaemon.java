/**
 *
 * Copyright 2021 PINE C&I CO., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.cityhub.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="daemon")
public class ConfigDaemon {

//getter, setter
  private String daemonSrv;
  private String logUrl;
  private String agentUrl;
  private String compileUrl;
  private String schemaServer;
  private String configUrl;

  public String getLogUrl() {
    return logUrl;
  }

  public String getAgentUrl() {
    return agentUrl;
  }


  public String getCompileUrl() {
    return compileUrl;
  }


  public String getSchemaServer() {
    return schemaServer;
  }


  public String getConfigUrl() {
    return configUrl;
  }


  public String getDaemonSrv() {
    return daemonSrv;
  }

  public void setDaemonSrv(String daemonSrv) {
    this.daemonSrv = daemonSrv;
  }

  public void setLogUrl(String logUrl) {
    this.logUrl = logUrl;
  }

  public void setAgentUrl(String agentUrl) {
    this.agentUrl = agentUrl;
  }

  public void setCompileUrl(String compileUrl) {
    this.compileUrl = compileUrl;
  }

  public void setSchemaServer(String schemaServer) {
    this.schemaServer = schemaServer;
  }

  public void setConfigUrl(String configUrl) {
    this.configUrl = configUrl;
  }


}
