package com.cityhub.source.core;

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


import java.util.List;
import java.util.Map;

import org.apache.flume.channel.ChannelProcessor;
import org.json.JSONObject;


public interface ReflectOpenApi  {

  public void init(ChannelProcessor channelProcessor , JSONObject configEnv);

  public String doit() ;

  public void sendEvent(List<Map<String, Object>> bodyMap,String DATASET_ID) ;


} // end of class
