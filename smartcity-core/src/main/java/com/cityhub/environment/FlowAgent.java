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

public interface FlowAgent {

  public String getValidation(String AdapterId);
  public String importAgent(String AdapterName);
  public String exportAgent(String AdapterName);
  public String stopAgent(String AdapterName);
  public String startAgent(String AdapterName);
  public String createAgent(String AdapterName, AdapterConfig properties);
  public String restartAgent(String AdapterName);
  public String uploadAgent(String AdapterName);
  public String convertDataModel(String AdapterName);

}
