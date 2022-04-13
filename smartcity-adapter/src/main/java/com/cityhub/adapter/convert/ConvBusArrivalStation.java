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

public class ConvBusArrivalStation extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {

    StringBuffer sendJson = new StringBuffer();

    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");

      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);
        JSONObject jTemplate = templateItem;
        JsonUtil jsonEx = new JsonUtil(jTemplate);

        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));

        if (ju.has("response.msgBody.busArrivalList") == true) {
          JSONArray loop = new JSONArray();
          JSONArray arrList = ju.getArray("response.msgBody.busArrivalList");

          for (Object obj : arrList) {
            JSONObject item = (JSONObject) obj;
            JSONObject ob = new JSONObject();
            ob.put("stationId", JsonUtil.nvl(item.getString("stationId") , DataType.STRING));
            ob.put("staOrder", JsonUtil.nvl(item.getString("staOrder") , DataType.STRING));
            ob.put("flag", JsonUtil.nvl(item.getString("flag") , DataType.STRING));
            ob.put("routeId", JsonUtil.nvl(item.getString("routeId") , DataType.STRING));

            ob.put("predictTime1", JsonUtil.nvl(item.getString("predictTime1") , DataType.STRING));
            ob.put("predictTime2", JsonUtil.nvl(item.getString("predictTime2") , DataType.STRING));
            ob.put("plateNo1", JsonUtil.nvl(item.getString("plateNo1") , DataType.STRING));
            ob.put("plateNo2", JsonUtil.nvl(item.getString("plateNo2") , DataType.STRING));
            ob.put("locationNo1", JsonUtil.nvl(ob.getString("locationNo1") , DataType.STRING));
            ob.put("locationNo2", JsonUtil.nvl(item.getString("locationNo2") , DataType.STRING));
            ob.put("lowPlate1", JsonUtil.nvl(item.getString("lowPlate1") , DataType.STRING));
            ob.put("lowPlate2", JsonUtil.nvl(item.getString("lowPlate2") , DataType.STRING));
            ob.put("remainSeatCnt1", JsonUtil.nvl(item.getString("remainSeatCnt1") , DataType.STRING));
            ob.put("remainSeatCnt2", JsonUtil.nvl(item.getString("remainSeatCnt2") , DataType.STRING));

            loop.put(ob);
          }
          jsonEx.put("BusArrivalList", loop);
        }
        sendJson.append(jTemplate.toString() + ",");
      }

    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage(), e);
    }

    return sendJson.toString();
  }

} // end of class
