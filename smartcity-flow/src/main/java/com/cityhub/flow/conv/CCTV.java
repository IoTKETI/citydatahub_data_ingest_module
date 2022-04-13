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

public class CCTV extends AbstractConvertSink<RequestMessageVO, JSONObject> {

  @Override
  public Map<String, Object> doit() throws CoreException {

    try {
      JsonUtil jsonEx = new JsonUtil(requestMessageVO.getContent());

      JsonUtil.objectToParam(param, (JSONObject)jsonEx.getObj("cctv.value") );

      param.put("name", jsonEx.getObj("name"));
      param.put("is_rotatable", jsonEx.getObj("is_rotatable"));
      param.put("height", jsonEx.getObj("height"));
      param.put("status", jsonEx.getObj("status"));
      param.put("distance", jsonEx.getObj("distance"));
      param.put("direction", jsonEx.getObj("direction"));
      param.put("field_of_view", jsonEx.getObj("field_of_view"));
      param.put("has_emergency_bell", jsonEx.getObj("has_emergency_bell"));
      param.put("installed_at", jsonEx.getObj("installed_at"));
      param.put("typeOfCCTV", jsonEx.getObj("typeOfCCTV"));
      param.put("numberOfCCTV", jsonEx.getObj("numberOfCCTV"));
      param.put("pixel", jsonEx.getObj("pixel"));

    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + requestMessageVO.getId(), e);
    }

    return param;
  }

} // end of class
