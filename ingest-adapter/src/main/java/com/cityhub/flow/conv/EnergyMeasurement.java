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

public class EnergyMeasurement extends AbstractConvertSink<RequestMessageVO, JSONObject> {

  @Override
  public Map<String, Object> doit() throws CoreException {

    try {
      JsonUtil jsonEx = new JsonUtil(requestMessageVO.getContent());

      JsonUtil.objectToParam(param, (JSONObject)jsonEx.getObj("energyMeasurement.value") );

      param.put("measurementType", jsonEx.getObj("measurementType"));
      param.put("workplaceName", jsonEx.getObj("workplaceName"));
      param.put("measureDay", jsonEx.getObj("measureDay"));
      param.put("measureTime", jsonEx.getObj("measureTime"));
      param.put("sequence", jsonEx.getObj("sequence"));
      param.put("measure1Hour", jsonEx.getObj("measure1Hour"));
      param.put("measurePeak", jsonEx.getObj("measurePeak"));
      param.put("measure1To15", jsonEx.getObj("measure1To15"));
      param.put("measure16To30", jsonEx.getObj("measure16To30"));
      param.put("measure31To45", jsonEx.getObj("measure31To45"));
      param.put("measure46To0", jsonEx.getObj("measure46To0"));



    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + requestMessageVO.getId(), e);
    }

    return param;
  }

} // end of class
