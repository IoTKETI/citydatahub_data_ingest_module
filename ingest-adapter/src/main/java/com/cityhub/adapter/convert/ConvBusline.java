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

public class ConvBusline extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {
    StringBuffer sendJson = new StringBuffer();
    try {
      JSONArray serviceList = ConfItem.getJSONArray("serviceList");

      for (int i = 0; i < serviceList.length(); i++) {
        JSONObject iService = serviceList.getJSONObject(i);
        JSONObject jsonData = (JSONObject) CommonUtil.getData(iService);

        if (JsonUtil.has(jsonData.toString(), "response.body.items.item") == true) {
          JSONArray jarr = JsonUtil.getArray(jsonData, "response.body.items.item");

          for (Object jitem : jarr) {
            JSONObject item = (JSONObject) jitem;

            JSONObject jTemplate = templateItem;
            JsonUtil jsonEx = new JsonUtil(jTemplate);

            jsonEx.put("id", ConfItem.getString("gs1Code") + "." + item.get("lineid"));
            jsonEx.put("location.value.coordinates", new JSONArray().put(item.get("gpsX")).put(item.get("gpsY")));
            jsonEx.put("localID.value", JsonUtil.nvl(item.get("lineNo")));
            jsonEx.put("shortID.value", JsonUtil.nvl(item.get("lineid")));
            jsonEx.put("name.value", JsonUtil.nvl(item.get("lineNo")));
            jsonEx.put("busLineType.value", JsonUtil.nvl(item.get("bustype")));

            sendJson.append(jTemplate.toString() + ",");
          }
        }
      }

    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage(), e);
    }
    return sendJson.toString();
  }

} // end of class
