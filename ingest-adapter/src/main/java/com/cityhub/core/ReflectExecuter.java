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

package com.cityhub.core;

import org.apache.flume.channel.ChannelProcessor;
import org.json.JSONObject;

public interface ReflectExecuter {

  public void init(ChannelProcessor channelProcessor, JSONObject ConfItem, JSONObject templateItem);

  public String doit(byte[] t2) ;

  public String doit() ;

  public void setup();

} // end of class
