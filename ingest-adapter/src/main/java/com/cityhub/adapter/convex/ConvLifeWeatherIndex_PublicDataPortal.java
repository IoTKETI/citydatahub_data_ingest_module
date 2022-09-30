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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.exception.CoreException;
import com.cityhub.source.core.AbstractConvert;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvLifeWeatherIndex_PublicDataPortal extends AbstractConvert {


  @Override
  public String doit() {

    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);
        id = iSvc.getString("gs1Code");
        Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> wMap;
        JSONArray urlC = new JSONArray();
        for (Integer j = 0; j < iSvc.getInt("url_addr_Number"); j++) {
          urlC.put(iSvc.getString("url_addr" + j.toString()));
        }
        JSONObject Tempitem = new JSONObject();

        for (int j = 0; j < urlC.length(); j++) {
          iSvc.put("url_addr", urlC.getString(j));
          JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
          Thread.sleep(500);
          if (ju.has("response.body.items.item")) {
            JSONArray arrList = ju.getArray("response.body.items.item");
            JSONObject item = arrList.getJSONObject(0);
            if (item != null) {
              toLogger(SocketCode.SOCKET_CONNECT_TRY, id, ju.toString().getBytes());

              Object[][] A08 = { { 0, "low" }, { 25, "normal" }, { 75, "high" }, { 100, "very high" } };
              Object[][] A07_1 = { { 0, "low" }, { 3, "normal" }, { 6, "high" }, { 8, "very high" }, { 11, "danger" } };
              Object[][] A41 = { { 0, "nomal" }, { 29, "attetion" }, { 31, "caution" }, { 34, "warning" }, { 37, "danger" } };
              Object[][] A44 = { { 0, "nomal" }, { 31, "attetion" }, { 33, "caution" }, { 35, "warning" }, { 38, "danger" } };

              if ("A08".equals(item.getString("code"))) {
                TempItemSet(Tempitem, item, A08, 8, "freeze");
              }

              if ("A07_1".equals(item.getString("code"))) {
                Integer _value = item.optInt("today");
                String Name = "ultraViolet";
                if (Tempitem.has(Name + "Value") && Tempitem.has(Name + "Index")) {
                  Tempitem.put(Name + "Value", _value);
                  Tempitem.put(Name + "Index", ExponentialStage(_value, A07_1));
                } else {
                  tMap.remove(Name + "Value");
                  tMap.remove(Name + "Index");
                }

              }

              if ("A09".equals(item.getString("code"))) {
                TempItemSet(Tempitem, item, A08, 8, "airDiffusion");
              }

              if ("A41".equals(item.getString("code"))) {
                TempItemSet(Tempitem, item, A41, 8, "sensibleTemperatureOld");
              }

              if ("A47".equals(item.getString("code"))) {
                TempItemSet(Tempitem, item, A44, 8, "sensibleTemperatureWork");
              }
              toLogger(SocketCode.SOCKET_CONNECT, id, Tempitem.toString());
            } else {
              toLogger(SocketCode.SOCKET_CONNECT_FAIL, id);
            }
          } else {
            toLogger(SocketCode.SOCKET_CONNECT_FAIL, id);
          }
        } // end j for
        toLogger(SocketCode.DATA_RECEIVE, id, Tempitem.toString());

        TempFind(tMap, Tempitem, "freeze");
        TempFind(tMap, Tempitem, "airDiffusion");
        TempFind(tMap, Tempitem, "sensibleTemperatureOld");
        TempFind(tMap, Tempitem, "sensibleTemperatureWork");

        wMap = (Map) tMap.get("dataProvider");
        wMap.put("value", iSvc.optString("dataProvider", "https://www.weather.go.kr"));

        wMap = (Map) tMap.get("globalLocationNumber");
        wMap.put("value", id);

        Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
        addrValue.put("addressCountry", iSvc.optString("addressCountry", ""));
        addrValue.put("addressRegion", iSvc.optString("addressRegion", ""));
        addrValue.put("addressLocality", iSvc.optString("addressLocality", ""));
        addrValue.put("addressTown", iSvc.optString("addressTown", ""));
        addrValue.put("streetAddress", iSvc.optString("streetAddress", ""));

        Map<String, Object> locMap = (Map) tMap.get("location");
        locMap.put("observedAt", DateUtil.getTime());
        Map<String, Object> locValueMap = (Map) locMap.get("value");
        locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

        tMap.put("id", id);

        rtnList.add(tMap);
        String str = objectMapper.writeValueAsString(tMap);
        toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
      } // end i for
      rtnStr = objectMapper.writeValueAsString(rtnList);
      if (rtnStr.length() < 10) {
        throw new CoreException(ErrorCode.NORMAL_ERROR);
      }
    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }

    } catch (Exception e) {
      toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return rtnStr;
  }

  void TempFind(Map<String, Object> tMap, JSONObject Tempitem, String Name) {
    if (Tempitem.has(Name + "Value") && Tempitem.has(Name + "Index")) {
      Map<String, Object> ValueMap = (Map) tMap.get(Name + "Value");
      Map<String, Object> IndexMap = (Map) tMap.get(Name + "Index");

      ValueMap.put("value", Tempitem.get(Name + "Value"));
      IndexMap.put("value", Tempitem.get(Name + "Index"));

      ValueMap.put("observedAt", DateUtil.getTime());
      IndexMap.put("observedAt", DateUtil.getTime());
    } else {
      tMap.remove(Name + "Value");
      tMap.remove(Name + "Index");
    }

  }

  void TempItemSet(JSONObject Tempitem, JSONObject item, Object[][] list, Integer size, String ItemName) {
    Integer[] Value = new Integer[size];
    String[] Index = new String[size];
    for (Integer i = 3; i <= (size * 3); i += 3) {
      Integer _value = item.optInt("h" + i.toString());
      Value[((i / 3) - 1)] = _value;
      Index[((i / 3) - 1)] = ExponentialStage(_value, list);
    }
    Tempitem.put(ItemName + "Value", Value);
    Tempitem.put(ItemName + "Index", Index);
  }

  String ExponentialStage(Integer Exponential, Object[][] arrList) {

    Integer Min = 0;

    String resultName = "";

    for (Integer i = 0; i < arrList.length; i++) {

      Integer _arrayNumber = (Integer) arrList[i][0];
      String _arrayName = (String) arrList[i][1];

      if ((Exponential >= _arrayNumber) && (_arrayNumber >= Min)) {
        Min = _arrayNumber;
        resultName = _arrayName;

      }
    }

    return resultName;
  }

}