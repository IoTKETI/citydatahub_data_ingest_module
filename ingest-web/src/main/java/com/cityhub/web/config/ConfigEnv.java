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

  @Value("${ingest.interfaceApiUrl}")
  private String interfaceApiUrl;

  @Value("${ingest.interfaceApiUrlUseYn}")
  private String interfaceApiUrlUseYn;

  @Value("${ingest.dataModelApiUrl}")
  private String dataModelApiUrl;

  private String logUrl = "/exec/logger";

  private String agentUrl= "/exec/agent";

  private String compileUrl = "/exec/compile";

  private String configUrl = "/exec/config";


  public String getDaemonSrv() {
    return daemonSrv;
  }

  public void setDaemonSrv(String daemonSrv) {
    this.daemonSrv = daemonSrv;
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

  public String getDataModelApiUrl() {
    return dataModelApiUrl;
  }

  public void setDataModelApiUrl(String dataModelApiUrl) {
    this.dataModelApiUrl = dataModelApiUrl;
  }

  public String getLogUrl() {
    return daemonSrv + logUrl;
  }
  public String getAgentUrl() {
    return daemonSrv + agentUrl;
  }
  public String getCompileUrl() {
    return daemonSrv + compileUrl;
  }
  public String getConfigUrl() {
    return daemonSrv + configUrl;
  }




}
