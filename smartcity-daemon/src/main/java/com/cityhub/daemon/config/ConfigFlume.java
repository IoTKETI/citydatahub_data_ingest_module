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
package com.cityhub.daemon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author AN
 *
 */
@Component
@ConfigurationProperties(prefix="flume")
public class ConfigFlume {

  private String home;
  private String conf;
  private String log;
  private String schemaServer;


  public String getHome() {
    return home;
  }
  public void setHome(String home) {
    this.home = home;
  }
  public String getLog() {
    return log;
  }

  public void setLog(String log) {
    this.log = log;
  }

  public String getSchemaServer() {
    return schemaServer;
  }

  public String getConf() {
    return conf;
  }
  public void setConf(String conf) {
    this.conf = conf;
  }
  public void setSchemaServer(String schemaServer) {
    this.schemaServer = schemaServer;
  }


}
