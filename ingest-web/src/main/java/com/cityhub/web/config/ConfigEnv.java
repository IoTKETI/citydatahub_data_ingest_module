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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigEnv {

  @Value("${ingest.daemonSrv}")
  private String daemonSrv;

  @Value("${ingest.logUrl}")
  private String logUrl;

  @Value("${ingest.agentUrl}")
  private String agentUrl;

  @Value("${ingest.compileUrl}")
  private String compileUrl;

  @Value("${ingest.dataModelApiUrl}")
  private String dataModelApiUrl;

  @Value("${ingest.configUrl}")
  private String configUrl;

  @Value("${ingest.interfaceApiUrl}")
  private String interfaceApiUrl;


  @Value("${ingest.interfaceApiUrlUseYn}")
  private String interfaceApiUrlUseYn;


  public String getLogUrl() {
    return logUrl;
  }

  public String getAgentUrl() {
    return agentUrl;
  }


  public String getCompileUrl() {
    return compileUrl;
  }


  public String getDataModelApiUrl() {
    return dataModelApiUrl;
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

  public void setDataModelApiUrl(String dataModelApiUrl) {
    this.dataModelApiUrl = dataModelApiUrl;
  }

  public void setConfigUrl(String configUrl) {
    this.configUrl = configUrl;
  }


  public String getInterfaceApiUrl() {
    return interfaceApiUrl;
  }

  public void setInterfaceApiUrl(String interfaceApiUrl) {
    this.interfaceApiUrl = interfaceApiUrl;
  }

  public String getInterfaceApiUrlUseYn() {
    return interfaceApiUrlUseYn;
  }

  public void setInterfaceApiUrlUseYn(String interfaceApiUrlUseYn) {
    this.interfaceApiUrlUseYn = interfaceApiUrlUseYn;
  }


}
