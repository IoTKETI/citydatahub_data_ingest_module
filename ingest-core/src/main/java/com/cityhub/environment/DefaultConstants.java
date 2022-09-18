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

package com.cityhub.environment;

public class DefaultConstants  {

  public static final String VENDOR = "PINECNI";
  public static final String URL_ADDR = "URL_ADDR";
  public static final String INVOKE_CLASS = "INVOKE_CLASS";
  public static final String CONN_TERM = "CONN_TERM";
  public static final String CONF_FILE = "CONF_FILE";
  public static final String HOST = "HOST";
  public static final String CSE_NAME = "CSE_NAME";
  public static final String AE_NAME = "AE_NAME";
  public static final String TEMPLATE_FILE = "TEMPLATE_FILE";
  public static final String TOPIC = "TOPIC";


  /**
   * Hostname to bind to.
   */
  public static final String CONFIG_HOSTNAME = "bind";

  /**
   * Port to bind to.
   */
  public static final String CONFIG_PORT = "port";


  /**
   * Ack every event received with an "OK" back to the sender
   */
  public static final String CONFIG_ACKEVENT = "ack-every-event";

  /**
   * Maximum line length per event.
   */
  public static final String CONFIG_MAX_LINE_LENGTH = "max-line-length";
  public static final int DEFAULT_MAX_LINE_LENGTH = 512;

  /**
   * Encoding for the netcat source
   */
  public static final String CONFIG_SOURCE_ENCODING = "encoding";
  public static final String DEFAULT_ENCODING = "utf-8";


} // end of class
