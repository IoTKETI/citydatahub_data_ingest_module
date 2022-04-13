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

package com.cityhub.dto;

import java.util.Map;
import com.cityhub.utils.ObjUtil;
import lombok.Data;

@SuppressWarnings("unchecked")
@Data
public class ResultObject<T> {

  private int responseCode;
  private String responseText;
  private Map<String,T> responseJson;
  private String responseDescription;

  public ResultObject() {

  }

  public ResultObject(int responseCode, String responseDescription) {
    this.responseCode = responseCode;
    this.responseDescription = responseDescription;
  }

  public Map<String,T> getResponseJson() {
    return responseJson;
  }

  public void setResponseJson(Object responseJson) {
    this.responseJson = ObjUtil.objToMap(responseJson);
  }




} // end class