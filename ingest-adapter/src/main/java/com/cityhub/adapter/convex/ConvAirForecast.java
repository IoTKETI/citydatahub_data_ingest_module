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

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.cityhub.utils.GradeType;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvAirForecast extends AbstractConvert {

  @Override
  public String doit() throws CoreException {
    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {
      Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
      });
      JSONArray serviceList = ConfItem.getJSONArray("serviceList");
      for (int k = 0; k < serviceList.length(); k++) {
        JSONObject iSvc = serviceList.getJSONObject(k);
        id = iSvc.getString("gs1Code");

        Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
        addrValue.put("addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")));
        addrValue.put("addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")));
        addrValue.put("addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")));
        addrValue.put("addressTown", JsonUtil.nvl(iSvc.getString("addressTown")));
        addrValue.put("streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")));

        Map<String, Object> locMap = (Map) tMap.get("location");
        locMap.put("observedAt", DateUtil.getTime());
        Map<String, Object> locValueMap = (Map) locMap.get("value");
        locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

        tMap.put("id", id);
        Map<String, Object> indexRefMap = new LinkedHashMap<>();
        indexRefMap.put("type", "Property");
        indexRefMap.put("value", "https://www.airkorea.or.kr/web/khaiInfo");
        tMap.put("indexRef", indexRefMap);

        String dateCreated = DateUtil.getTime();
        String[] types = { "pm10", "pm25", "o3" };

        List<Map<String, Object>> aForecast = new LinkedList<>();
        Map<String, Object> forecast = new LinkedHashMap<>();
        String objLength = "";
        try {
          JSONArray urlAddr = iSvc.getJSONArray("url_addr");
          JSONObject obj = (JSONObject) CommonUtil.getData(iSvc, urlAddr.get(0).toString());
          objLength += obj.toString();
          JsonUtil ju = new JsonUtil(obj);
          if (!ju.has("response.body.items")) {
            throw new CoreException(ErrorCode.NORMAL_ERROR);
          }
          JSONArray objarr = ju.getArray("response.body.items");
          log.info("++++response:{}", objarr.toString());
          if (objarr.length() > 0) {
            for (String type : types) {
              for (int j = 0; j < objarr.length(); j++) {
                JSONObject item = (JSONObject) objarr.get(j);
                if (type.equals(item.getString("informCode").toLowerCase())) {
                  String[] strs = item.getString("informGrade").replaceAll(" ", "").split(",", -1);
                  forecast.put(type + "Category", JsonUtil.nvl(getGrade(strs, iSvc.getString("zoneName"))));
                  forecast.put("predictedAt", DateUtil.getISOTime(item.getString("dataTime").replaceAll("[^0-9]", "").trim()));
                  break;
                }
              } // (int j = 0; j < objarr.length(); j++ )
            } // (String type : types)
          } else {
            log(SocketCode.DATA_CONVERT_FAIL, id, "파싱하기 위한 필수항목이 존재하지 않습니다");
          } // if (objarr.length() > 0)
        } catch (Exception e) {
          log.error("Exception : " + ExceptionUtils.getStackTrace(e));
        }
        log(SocketCode.DATA_RECEIVE, id, objLength.getBytes());
        /*
         * String add = DateUtil.addDate(dateCreated, ChronoUnit.HOURS, + 1,
         * "yyyyMMddHH"); forecast.put("predictedAt", DateUtil.getISOTime(add));
         */
        String aDate = forecast.get("predictedAt") + "";
        aForecast.add(new HashMap<>(forecast));

        forecast.put("predictedAt", DateUtil.getISOTime(DateUtil.addDate(aDate.substring(0, 23), ChronoUnit.HOURS, 1, "yyyy-MM-dd HH:mm:ss")));
        aForecast.add(new HashMap<>(forecast));

        forecast.put("predictedAt", DateUtil.getISOTime(DateUtil.addDate(aDate.substring(0, 23), ChronoUnit.HOURS, 2, "yyyy-MM-dd HH:mm:ss")));
        aForecast.add(new HashMap<>(forecast));
        forecast.put("predictedAt", DateUtil.getISOTime(DateUtil.addDate(aDate.substring(0, 23), ChronoUnit.HOURS, 3, "yyyy-MM-dd HH:mm:ss")));
        aForecast.add(new HashMap<>(forecast));
        forecast.put("predictedAt", DateUtil.getISOTime(DateUtil.addDate(aDate.substring(0, 23), ChronoUnit.HOURS, 4, "yyyy-MM-dd HH:mm:ss")));
        aForecast.add(new HashMap<>(forecast));
        forecast.put("predictedAt", DateUtil.getISOTime(DateUtil.addDate(aDate.substring(0, 23), ChronoUnit.HOURS, 5, "yyyy-MM-dd HH:mm:ss")));
        aForecast.add(new HashMap<>(forecast));

        Map<String, Object> airQualityIndexPrediction = new LinkedHashMap<>();
        airQualityIndexPrediction.put("type", "Property");
        airQualityIndexPrediction.put("observedAt", dateCreated);
        airQualityIndexPrediction.put("value", aForecast);
        tMap.put("airQualityIndexPrediction", airQualityIndexPrediction);

        tMap.remove("airQualityPrediction");
        tMap.remove("name");
        tMap.remove("indexRef");

        rtnList.add(tMap);
        String str = objectMapper.writeValueAsString(tMap);
        log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());

        rtnStr = objectMapper.writeValueAsString(rtnList);

      } // for (int k = 0 ; k < serviceList.length(); k++)

    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage(), e);
    }
    return rtnStr;
  }

  public Object getGrade(String[] strs, String zone) {
    Object grade = "";
    for (String str : strs) {
      if (str.contains(zone)) {
        int index = str.indexOf(":");
        String sub = str.substring(index + 1, str.length());
        if (!"".equals(sub)) {
          grade = GradeType.findBy(sub).getValue();
          break;
        }
      }
    }
    return grade;
  }

} // end of class
