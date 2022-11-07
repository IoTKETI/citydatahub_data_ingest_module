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
package com.cityhub.adapter.convex;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.exception.CoreException;
import com.cityhub.core.AbstractConvert;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvBusArrivalInformation extends AbstractConvert {

  @Override
  public String doit() {

    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";

    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);

        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));

        if (!ju.has("response.msgBody.busArrivalList")) {
          // throw new CoreException(ErrorCode.NORMAL_ERROR);
          continue;
        } else {

          JSONArray arr = new JSONArray();
          Integer arrlength;
          boolean JsonArrayAs = ju.getObj("response.msgBody.busArrivalList") instanceof JSONArray;

          if (JsonArrayAs) {
            arr = (JSONArray) ju.getObj("response.msgBody.busArrivalList");
            arrlength = arr.length();
          } else if (ju.getObj("response.msgBody.busArrivalList") instanceof JSONObject) {
            arrlength = 1;
          } else {
            arrlength = 0;
          }

          for (int j = 0; j < arrlength; j++) {

            JSONObject item;
            if (JsonArrayAs) {
              item = arr.getJSONObject(j);
            } else {
              item = (JSONObject) ju.getObj("response.msgBody.busArrivalList");
            }

            Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> wMap = new LinkedHashMap<>();

            if (item.has("predictTime1"))
              Find_wMap(tMap, "predictTime1").put("value", item.optInt("predictTime1"));
            else
              Delete_wMap(tMap, "predictTime1");

            if (item.has("predictTime2"))
              Find_wMap(tMap, "predictTime2").put("value", item.optInt("predictTime2"));
            else
              Delete_wMap(tMap, "predictTime2");

            if (item.has("flag"))
              Find_wMap(tMap, "flag").put("value", item.optInt("flag"));
            else
              Delete_wMap(tMap, "flag");

            if (item.has("lowPlate2"))
              Find_wMap(tMap, "lowPlate2").put("value", item.optInt("lowPlate2"));
            else
              Delete_wMap(tMap, "lowPlate2");

            if (item.has("plateNo1"))
              Find_wMap(tMap, "plateNo1").put("value", item.optInt("plateNo1"));
            else
              Delete_wMap(tMap, "plateNo1");

            if (item.has("plateNo2"))
              Find_wMap(tMap, "plateNo2").put("value", item.optInt("plateNo2"));
            else
              Delete_wMap(tMap, "plateNo2");

            id = iSvc.getString("gs1Code");

            wMap = (Map) tMap.get("globalLocationNumber");
            wMap.put("value", id);

            wMap = (Map) tMap.get("dataProvider");
            wMap.put("value", iSvc.optString("dataProvider", "https://www.gbis.go.kr"));

            tMap.put("id", id);

            Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
            addrValue.put("addressCountry", iSvc.optString("addressCountry"));
            addrValue.put("addressRegion", iSvc.optString("addressRegion"));
            addrValue.put("addressLocality", iSvc.optString("addressLocality"));
            addrValue.put("addressTown", iSvc.optString("addressTown"));
            addrValue.put("streetAddress", iSvc.optString("streetAddress"));

            Map<String, Object> locMap = (Map) tMap.get("location");
            locMap.put("observedAt", DateUtil.getTime());
            Map<String, Object> locValueMap = (Map) locMap.get("value");
            locValueMap.put("coordinates", iSvc.getJSONArray("location"));

            rtnList.add(tMap);
            String str = objectMapper.writeValueAsString(tMap);
            toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
          }
        }
      }
      rtnStr = objectMapper.writeValueAsString(rtnList);

    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return rtnStr;
  }// end of doit()

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    ValueMap.put("observedAt", DateUtil.getTime());
    return ValueMap;
  }

  void Delete_wMap(Map<String, Object> tMap, String Name) {
    tMap.remove(Name);
  }

}// end of class
