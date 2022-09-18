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

public class WaterQualityTap extends AbstractConvertSink<RequestMessageVO, JSONObject> {

  @Override
  public Map<String, Object> doit() throws CoreException {

    try {
      JsonUtil jsonEx = new JsonUtil(requestMessageVO.getContent());

      JsonUtil.objectToParam(param, (JSONObject)jsonEx.getObj("waterQualityTap.value") );

      param.put("interaval", jsonEx.getObj("interaval"));
      param.put("taste", jsonEx.getObj("taste"));
      param.put("odor", jsonEx.getObj("odor"));
      param.put("chromaticity", jsonEx.getObj("chromaticity"));
      param.put("hydrogenIndex", jsonEx.getObj("hydrogenIndex"));
      param.put("turbidity", jsonEx.getObj("turbidity"));
      param.put("residualChlorine", jsonEx.getObj("residualChlorine"));
      param.put("bacteria", jsonEx.getObj("bacteria"));
      param.put("totalColiforms", jsonEx.getObj("totalColiforms"));
      param.put("coliforms", jsonEx.getObj("coliforms"));
      param.put("ammoniumNitrogen", jsonEx.getObj("ammoniumNitrogen"));
      param.put("nitrateNitrogen", jsonEx.getObj("nitrateNitrogen"));
      param.put("totalSolids", jsonEx.getObj("totalSolids"));
      param.put("lead", jsonEx.getObj("lead"));
      param.put("fluoride", jsonEx.getObj("fluoride"));
      param.put("arsenic", jsonEx.getObj("arsenic"));
      param.put("selenium", jsonEx.getObj("selenium"));
      param.put("mercury", jsonEx.getObj("mercury"));
      param.put("cyanide", jsonEx.getObj("cyanide"));
      param.put("chrome", jsonEx.getObj("chrome"));
      param.put("cadmium", jsonEx.getObj("cadmium"));
      param.put("boron", jsonEx.getObj("boron"));
      param.put("phenol", jsonEx.getObj("phenol"));
      param.put("diazinon", jsonEx.getObj("diazinon"));
      param.put("parathion", jsonEx.getObj("parathion"));
      param.put("fenitrothion", jsonEx.getObj("fenitrothion"));
      param.put("carbaryl", jsonEx.getObj("carbaryl"));
      param.put("trichloroethane", jsonEx.getObj("trichloroethane"));
      param.put("tetrachloroethylene", jsonEx.getObj("tetrachloroethylene"));
      param.put("trichloroethylene", jsonEx.getObj("trichloroethylene"));
      param.put("dichloromethane", jsonEx.getObj("dichloromethane"));
      param.put("benzene", jsonEx.getObj("benzene"));
      param.put("toluene", jsonEx.getObj("toluene"));
      param.put("ethylBenzene", jsonEx.getObj("ethylBenzene"));
      param.put("xylene", jsonEx.getObj("xylene"));
      param.put("dichloroethylene", jsonEx.getObj("dichloroethylene"));
      param.put("carbonTetrachloride", jsonEx.getObj("carbonTetrachloride"));
      param.put("dibromoChloropropane", jsonEx.getObj("dibromoChloropropane"));
      param.put("dioxan", jsonEx.getObj("dioxan"));
      param.put("trihalomethane", jsonEx.getObj("trihalomethane"));
      param.put("chloroform", jsonEx.getObj("chloroform"));
      param.put("bromodichloromethane", jsonEx.getObj("bromodichloromethane"));
      param.put("dibromochloromethane", jsonEx.getObj("dibromochloromethane"));
      param.put("chloralHydrate", jsonEx.getObj("chloralHydrate"));
      param.put("dibromoacetonitrile", jsonEx.getObj("dibromoacetonitrile"));
      param.put("trichloroacetonitrile", jsonEx.getObj("trichloroacetonitrile"));
      param.put("dichloroacetonitrile", jsonEx.getObj("dichloroacetonitrile"));
      param.put("haloAceticAcid", jsonEx.getObj("haloAceticAcid"));
      param.put("formaldehyde", jsonEx.getObj("formaldehyde"));
      param.put("hardness", jsonEx.getObj("hardness"));
      param.put("consumptionKMnO4", jsonEx.getObj("consumptionKMnO4"));
      param.put("cooper", jsonEx.getObj("cooper"));
      param.put("detergentABS", jsonEx.getObj("detergentABS"));
      param.put("zinc", jsonEx.getObj("zinc"));
      param.put("chloride", jsonEx.getObj("chloride"));
      param.put("iron", jsonEx.getObj("iron"));
      param.put("manganese", jsonEx.getObj("manganese"));
      param.put("sulfate", jsonEx.getObj("sulfate"));
      param.put("aluminium", jsonEx.getObj("aluminium"));




    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + requestMessageVO.getId(), e);
    }

    return param;
  }

} // end of class
