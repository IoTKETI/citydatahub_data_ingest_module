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

public class LifeWeatherIndex extends AbstractConvertSink<RequestMessageVO, JSONObject> {

  @Override
  public Map<String, Object> doit() throws CoreException {

    try {
      JsonUtil jsonEx = new JsonUtil(requestMessageVO.getContent());

      JsonUtil.objectToParam(param, (JSONObject)jsonEx.getObj("lifeWeatherIndex.value") );

      param.put("windChillIndex", jsonEx.getObj("windChillIndex"));
      param.put("discomportIndex", jsonEx.getObj("discomportIndex"));
      param.put("freezeIndex", jsonEx.getObj("freezeIndex"));
      param.put("ultraVioletIndex", jsonEx.getObj("ultraVioletIndex"));
      param.put("airDiffusionIndex", jsonEx.getObj("airDiffusionIndex"));
      param.put("heatFeelingIndex", jsonEx.getObj("heatFeelingIndex"));
      param.put("heatIndex", jsonEx.getObj("heatIndex"));
      param.put("sensibleTemperatureOldIndex", jsonEx.getObj("sensibleTemperatureOldIndex"));
      param.put("sensibleTemperatureChildIndex", jsonEx.getObj("sensibleTemperatureChildIndex"));
      param.put("sensibleTemperatureFarmIndex", jsonEx.getObj("sensibleTemperatureFarmIndex"));
      param.put("sensibleTemperatureVinylHouseIndex", jsonEx.getObj("sensibleTemperatureVinylHouseIndex"));
      param.put("sensibleTemperatureWeakResidenceIndex", jsonEx.getObj("sensibleTemperatureWeakResidenceIndex"));
      param.put("sensibleTemperatureRoadIndex", jsonEx.getObj("sensibleTemperatureRoadIndex"));
      param.put("sensibleTemperatureConstructionIndex", jsonEx.getObj("sensibleTemperatureConstructionIndex"));
      param.put("sensibleTemperatureShipyardIndex", jsonEx.getObj("sensibleTemperatureShipyardIndex"));



    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + requestMessageVO.getId(), e);
    }

    return param;
  }

} // end of class
