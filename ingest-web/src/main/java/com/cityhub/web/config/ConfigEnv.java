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


  @Value("${cityhub.auth.yn:N}")
  private String authYn;

  @Value("${cityhub.auth.clientId}")
  private String clientId;

  @Value("${cityhub.auth.clientSecret}")
  private String clientSecret;

  @Value("${cityhub.auth.authorizationUri}")
  private String authorizationUri;

  @Value("${cityhub.auth.tokenUri}")
  private String tokenUri;

  @Value("${cityhub.auth.publicKeyUri}")
  private String publicKeyUri;

  @Value("${cityhub.auth.logoutUri}")
  private String logoutUri;

  @Value("${cityhub.auth.redirectUri}")
  private String redirectUri;

  @Value("${cityhub.auth.userInfoUri}")
  private String userInfoUri;




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

  public void setLogUrl(String logUrl) {
    this.logUrl = logUrl;
  }

  public void setAgentUrl(String agentUrl) {
    this.agentUrl = agentUrl;
  }

  public void setCompileUrl(String compileUrl) {
    this.compileUrl = compileUrl;
  }

  public void setConfigUrl(String configUrl) {
    this.configUrl = configUrl;
  }


  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }


  public String getPublicKeyUri() {
    return publicKeyUri;
  }

  public void setPublicKeyUri(String publicKeyUri) {
    this.publicKeyUri = publicKeyUri;
  }

  public String getLogoutUri() {
    return logoutUri;
  }

  public void setLogoutUri(String logoutUri) {
    this.logoutUri = logoutUri;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getUserInfoUri() {
    return userInfoUri;
  }

  public void setUserInfoUri(String userInfoUri) {
    this.userInfoUri = userInfoUri;
  }

  public String getAuthYn() {
    return authYn;
  }

  public void setAuthYn(String authYn) {
    this.authYn = authYn;
  }


  public String getTokenUri() {
    return tokenUri;
  }

  public void setTokenUri(String tokenUri) {
    this.tokenUri = tokenUri;
  }

  public String getAuthorizationUri() {
    return authorizationUri;
  }

  public void setAuthorizationUri(String authorizationUri) {
    this.authorizationUri = authorizationUri;
  }



}
