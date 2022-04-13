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
package com.cityhub.flow.conv;

import java.util.Map;

import org.json.JSONObject;

import com.cityhub.core.AbstractConvertSink;
import com.cityhub.dto.RequestMessageVO;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.JsonUtil;

public class AirQualityMeasurement extends AbstractConvertSink<RequestMessageVO, JSONObject> {

  @Override
  public Map<String, Object> doit() throws CoreException {

    try {
      JsonUtil jsonEx = new JsonUtil(requestMessageVO.getContent());

      JsonUtil.objectToParam(param, (JSONObject)jsonEx.getObj("airQualityMeasurement.value") );

      param.put("deviceType", jsonEx.getObj("deviceType"));
      param.put("deviceId", jsonEx.getObj("deviceId"));
      param.put("observedAt", jsonEx.getObj("observedAt"));
      param.put("pm10", jsonEx.getObj("pm10"));
      param.put("pm25", jsonEx.getObj("pm25"));
      param.put("temper", jsonEx.getObj("temper"));
      param.put("humid", jsonEx.getObj("humid"));
      param.put("vocs", jsonEx.getObj("vocs"));
      param.put("tod", jsonEx.getObj("tod"));
      param.put("h2s", jsonEx.getObj("h2s"));
      param.put("nh3", jsonEx.getObj("nh3"));



    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + requestMessageVO.getId(), e);
    }

    return param;
  }

} // end of class
