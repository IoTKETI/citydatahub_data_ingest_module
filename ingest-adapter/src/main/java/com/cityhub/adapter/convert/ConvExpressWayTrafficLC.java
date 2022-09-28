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
package com.cityhub.adapter.convert;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.JsonUtil;

public class ConvExpressWayTrafficLC extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {
    StringBuffer sendJson = new StringBuffer();
    try {
      JSONArray serviceList = ConfItem.getJSONArray("serviceList");

      for (int z = 0; z < serviceList.length(); z++) {
        JSONObject iSvc = serviceList.getJSONObject(z);

        JSONObject jsonData = (JSONObject) CommonUtil.getData(iSvc);

        JSONObject jTemplate = templateItem;
        JsonUtil jsonEx = new JsonUtil(jTemplate);

        String[] sInOut = { "in", "out" };
        String[] sTCS = { "TCS", "Hipass" };

        JSONArray jList = jsonData.getJSONArray("trafficIc");
        for (int i = 0; i < jList.length(); i++) {
          JSONObject item = (JSONObject) jList.get(i);
          int inout = Integer.parseInt(item.getString("inoutType"));
          int tcs = Integer.parseInt(item.getString("tcsType"));
          String key = sInOut[inout] + sTCS[tcs - 1] + item.getString("carType");
          jsonEx.put(key, JsonUtil.nvl(item.getString("trafficAmout")));

        }

        jsonEx.put("id", ConfItem.getString("gs1Code"));

        sendJson.append(jTemplate.toString() + ",");
      }

    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage(), e);
    }
    return sendJson.toString();
  }

} // end of class
