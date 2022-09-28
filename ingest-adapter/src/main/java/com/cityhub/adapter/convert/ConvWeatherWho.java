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
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;

public class ConvWeatherWho extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {
    StringBuffer sendJson = new StringBuffer();
    try {
      JSONObject jTemplate = templateItem;
      JsonUtil jsonEx = new JsonUtil(jTemplate);

      JSONArray serviceList = ConfItem.getJSONArray("serviceList");

      for (int i = 0; i < serviceList.length(); i++) {
        JSONObject iService = serviceList.getJSONObject(i);

        JsonUtil jh = new JsonUtil((JSONObject) CommonUtil.getData(iService));
        JSONObject jHeader = jh.getObject("Response.header");

        if ("Y".equals(jHeader.getString("successYN"))) {
          JSONObject jModel = jh.getObject("Response.body.indexModel");

          String dataObserved = DateUtil.getISOTime(jModel.getString("date"));
          // jsonEx.put("dataObserved.value", dataObserved);

          if (dataObserved.contains("T06:00:00")) { // 6시 발표
            jsonEx.put(iService.get("key") + ".value", JsonUtil.nvl(jModel.getString("today")));
          } else { // 18시 발표
            jsonEx.put(iService.get("key") + ".value", JsonUtil.nvl(jModel.getString("tomorrow")));
          }

        } else {
          jsonEx.put(iService.getString("key") + ".value", JsonUtil.nvl("-"));
        }

      }

      jsonEx.put("id", ConfItem.getString("gs1Code"));

      sendJson.append(jTemplate.toString() + ",");

    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage(), e);
    }

    return sendJson.toString();
  }

} // end of class
