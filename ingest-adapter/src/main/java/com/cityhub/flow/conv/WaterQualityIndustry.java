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

public class WaterQualityIndustry extends AbstractConvertSink<RequestMessageVO, JSONObject> {

  @Override
  public Map<String, Object> doit() throws CoreException {

    try {
      JsonUtil jsonEx = new JsonUtil(requestMessageVO.getContent());

      JsonUtil.objectToParam(param, (JSONObject)jsonEx.getObj("waterQualityIndustry.value") );

      param.put("interaval", jsonEx.getObj("interaval"));
      param.put("temperatureOfSource", jsonEx.getObj("temperatureOfSource"));
      param.put("temperatureOfPrecipitation", jsonEx.getObj("temperatureOfPrecipitation"));
      param.put("hydrogenIndexOfSource", jsonEx.getObj("hydrogenIndexOfSource"));
      param.put("hydrogenIndexOfPrecipitation", jsonEx.getObj("hydrogenIndexOfPrecipitation"));
      param.put("turbidityOfSource", jsonEx.getObj("turbidityOfSource"));
      param.put("turbidityOfPrecipitation", jsonEx.getObj("turbidityOfPrecipitation"));
      param.put("conductivityOfSource", jsonEx.getObj("conductivityOfSource"));
      param.put("conductivityOfPrecipitation", jsonEx.getObj("conductivityOfPrecipitation"));
      param.put("alkaliOfSource", jsonEx.getObj("alkaliOfSource"));
      param.put("alkaliOfPrecipitation", jsonEx.getObj("alkaliOfPrecipitation"));
      param.put("CODOfSource", jsonEx.getObj("CODOfSource"));
      param.put("consumptionKMnO4OfPrecipitation", jsonEx.getObj("consumptionKMnO4OfPrecipitation"));
      param.put("TDSOfSource", jsonEx.getObj("TDSOfSource"));
      param.put("TDSOfPrecipitation", jsonEx.getObj("TDSOfPrecipitation"));
      param.put("hardnessOfSource", jsonEx.getObj("hardnessOfSource"));
      param.put("hardnessOfPrecipitation", jsonEx.getObj("hardnessOfPrecipitation"));
      param.put("suspendedSolid", jsonEx.getObj("suspendedSolid"));
      param.put("dissolvedOxygen", jsonEx.getObj("dissolvedOxygen"));
      param.put("totalPhosphorus", jsonEx.getObj("totalPhosphorus"));
      param.put("totalNitrogen", jsonEx.getObj("totalNitrogen"));
      param.put("chlorophyll", jsonEx.getObj("chlorophyll"));
      param.put("totalColiforms", jsonEx.getObj("totalColiforms"));



    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + requestMessageVO.getId(), e);
    }

    return param;
  }

} // end of class
