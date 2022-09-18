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
import com.cityhub.utils.DataType;
import com.cityhub.utils.JsonUtil;

public class ConvAirAverage extends AbstractConvert  {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {
    StringBuffer sendJson = new StringBuffer();
    try {
      JSONArray serviceList = ConfItem.getJSONArray("serviceList");

      for (int i = 0 ; i < serviceList.length(); i++) {
        JSONObject iService = serviceList.getJSONObject(i);
        JSONObject jsonData = (JSONObject) CommonUtil.getData(iService );

        JSONObject jTemplate = templateItem;
        JsonUtil jsonEx = new JsonUtil(jTemplate);
        JSONArray jaSido = jsonData.getJSONArray("list");
        JSONObject jCity = null;
        for (Object jitem : jaSido) {
          JSONObject item = (JSONObject)jitem;
          if (ConfItem.getString("cityName").equals(item.getString("cityName"))) {
            jCity = new JSONObject(item.toString());
            break;
          }
        }

        jsonEx.put("id", ConfItem.getString("gs1Code"));

        jsonEx.put("so2.value", JsonUtil.nvl(jCity.get("so2Value"), DataType.FLOAT));
        jsonEx.put("co.value", JsonUtil.nvl(jCity.get("coValue"), DataType.FLOAT));
        jsonEx.put("o3.value", JsonUtil.nvl(jCity.get("o3Value"), DataType.FLOAT));
        jsonEx.put("no2.value", JsonUtil.nvl(jCity.get("no2Value"), DataType.FLOAT));
        jsonEx.put("pm10.value", JsonUtil.nvl(jCity.get("pm10Value"), DataType.FLOAT));
        jsonEx.put("pm25.value", JsonUtil.nvl(jCity.get("pm25Value"), DataType.FLOAT));

        sendJson.append(jTemplate.toString() + ",");
      }

    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage(), e);
    }

    return sendJson.toString();
  }



} // end of class
