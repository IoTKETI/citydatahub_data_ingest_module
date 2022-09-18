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

public class ConvWaterDailyIndustry extends AbstractConvert {

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

        JSONObject jTemplate = templateItem;
        JsonUtil jsonEx = new JsonUtil(jTemplate);

        JsonUtil ju = new JsonUtil(jsonData);

        if (ju.has("response.body.items.item") == true) {
          JSONArray arrList = ju.getArray("response.body.items.item");
          if (arrList.length() > 0) {
            JSONObject jItem = arrList.getJSONObject(arrList.length() - 1);

            jsonEx.put("id", ConfItem.getString("gs1Code"));

            jsonEx.put("tempSource.value", JsonUtil.nvl(jItem.get("item1"), DataType.FLOAT));
            jsonEx.put("tempPreci.value", JsonUtil.nvl(jItem.get("item2"), DataType.FLOAT));
            jsonEx.put("pHSource.value", JsonUtil.nvl(jItem.get("item3"), DataType.FLOAT));
            jsonEx.put("pHPreci.value", JsonUtil.nvl(jItem.get("item4"), DataType.FLOAT));
            jsonEx.put("turbSource.value", JsonUtil.nvl(jItem.get("item5"), DataType.FLOAT));
            jsonEx.put("turbPreci.value", JsonUtil.nvl(jItem.get("item6"), DataType.FLOAT));
            jsonEx.put("conductSource.value", JsonUtil.nvl(jItem.get("item7"), DataType.FLOAT));
            jsonEx.put("conductPreci.value", JsonUtil.nvl(jItem.get("item8"), DataType.FLOAT));
            jsonEx.put("alkaliSource.value", JsonUtil.nvl(jItem.get("item9"), DataType.FLOAT));
            jsonEx.put("alkaliPreci.value", JsonUtil.nvl(jItem.get("item10"), DataType.FLOAT));

            sendJson.append(jTemplate.toString() + ",");

          }
        }
      }

    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage(), e);
    }

    return sendJson.toString();
  }


} // end of class
