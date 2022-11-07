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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWeatherMeasurement_Vilage_PublicDataPortal extends AbstractConvert {

  @Override
  public String doit()  {
    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {

      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      for (int i = 0; i < svcList.length(); i++) {

        JSONObject iSvc = svcList.getJSONObject(i); // Column별로 분리함
        id = iSvc.optString("gs1Code");

        Calendar cal = Calendar.getInstance();
        cal.setTime(DateUtil.getTimestamp());
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        DateFormat df2 = new SimpleDateFormat("HHmm");
        String yyyyMMddHH = df.format(cal.getTime().getTime());
        String HHmm = df2.format(cal.getTime().getTime());
        JsonUtil ju;

        Object[][] base_time = { { 0, "2300" }, { 211, "0200" }, { 511, "0500" }, { 811, "0800" }, { 1111, "1100" }, { 1411, "1400" }, { 1711, "1700" }, { 2011, "2000" }, { 2311, "2300" } };

        if (Integer.parseInt(HHmm) < 210) {
          cal.add(Calendar.DATE, -1);
          yyyyMMddHH = df.format(cal.getTime().getTime());
          ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc, iSvc.getString("url_addr") + "&base_date=" + yyyyMMddHH + "&base_time=" + ExponentialStage(Integer.parseInt(HHmm), base_time)));
        } else {
          ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc, iSvc.getString("url_addr") + "&base_date=" + yyyyMMddHH + "&base_time=" + ExponentialStage(Integer.parseInt(HHmm), base_time)));
        }
        if (ju.has("response.body.items.item")) {
          JSONArray arrList = ju.getArray("response.body.items.item");
          Map<String, Object> wMap = new LinkedHashMap<>(); // 분리한 데이터를 넣어줄 map을 만듬
          Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
          });

          JSONObject Tempitem = new JSONObject();
          if (arrList.length() > 0) {
            for (Object obj : arrList) {
              JSONObject item = (JSONObject) obj;
              if (item != null) {
                toLogger(SocketCode.SOCKET_CONNECT_TRY, id);
                // 해발 고도(m)
                TempSwich(Tempitem, item, "altitude", "altitude", "Double");
                // 풍향(deg)
                TempSwich(Tempitem, item, "VEC", "windDirection", "Double");
                // 풍속(m/s)
                TempSwich(Tempitem, item, "WSD", "windSpeed", "Double");
                // 기온(℃)
                TempSwich(Tempitem, item, "T1H", "temperature", "Double");
                // 습도(%)
                TempSwich(Tempitem, item, "REH", "humidity", "Int");
                // 현지기압(hPa)
                TempSwich(Tempitem, item, "atmosphericPressure", "atmosphericPressure", "Double");
                // 해면기압(hPa)
                TempSwich(Tempitem, item, "seaLevelPressure", "seaLevelPressure", "Double");

                Object[][] rainfall = { { 0, "없음" }, { 1, "비" }, { 2, "비/눈" }, { 3, "눈" }, { 4, "빗방울" }, { 5, "빗방울/눈날림" }, { 6, "눈날림" } };
                // 강수감지
                if ("PTY".equals(item.getString("category"))) {
                  int value = item.getInt("fcstValue");
                  Tempitem.put("rainfall", ExponentialStage(value, rainfall));
                  Tempitem.put("rainfall" + "Time", item.optString("fcstDate") + item.optString("fcstTime"));
                }
                // 시간누적강우량(mm), 강수량
                if ("PCP".equals(item.getString("category"))) {
                  if ("1mm 미만".equals(item.optString("fcstValue"))) {
                    Tempitem.put("hourlyRainfall", 1.0d);
                  } else if ("30~50mm".equals(item.optString("fcstValue"))) {
                    Tempitem.put("hourlyRainfall", 30.0d);
                  } else if ("50mm 이상".equals(item.optString("fcstValue"))) {
                    Tempitem.put("hourlyRainfall", 50.0d);
                  } else {
                    Tempitem.put("hourlyRainfall", item.optDouble("fcstValue", 0.0d));
                  }
                  Tempitem.put("hourlyRainfall" + "Time", item.optString("fcstDate") + item.optString("fcstTime"));
                }
                // 일누적강우량(mm)
                TempSwich(Tempitem, item, "dailyRainfall", "dailyRainfall", "Double");
                // 수증기압(hPa)
                TempSwich(Tempitem, item, "vaporPressure", "vaporPressure", "Double");
                // 이슬점온도(℃)
                TempSwich(Tempitem, item, "dewPoint", "dewPoint", "Double");
                // 일조 (시간)
                TempSwich(Tempitem, item, "sunshine", "sunshine", "Double");
                // 일사 (복사량)
                TempSwich(Tempitem, item, "insolation", "insolation", "Double");
                // 적설(cm)
                if ("SNO".equals(item.getString("category"))) {
                  if ("1 cm 미만".equals(item.optString("fcstValue"))) {
                    Tempitem.put("snowfall", 1.0d);
                  } else if ("5 cm 이상".equals(item.optString("fcstValue"))) {
                    Tempitem.put("snowfall", 5.0d);
                  } else {
                    Tempitem.put("snowfall", item.optDouble("fcstValue", 0.0d));
                  }
                  Tempitem.put("snowfall" + "Time", item.optString("fcstDate") + item.optString("fcstTime"));
                }
                // 3시간신적설(cm)
                TempSwich(Tempitem, item, "S03", "snowfallHour3", "Double");
                // 시정 (10m)
                TempSwich(Tempitem, item, "visibility", "visibility", "Double");
                toLogger(SocketCode.SOCKET_CONNECT, id);
              } else {
                toLogger(SocketCode.SOCKET_CONNECT_FAIL, id);
                continue;
              }
            } // end for

            toLogger(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());

            Find_wMap2(tMap, Tempitem, "altitude", "Double");
            Find_wMap2(tMap, Tempitem, "windDirection", "Double");
            Find_wMap2(tMap, Tempitem, "windSpeed", "Double");
            Find_wMap2(tMap, Tempitem, "temperature", "Double");
            Find_wMap2(tMap, Tempitem, "humidity", "Double");
            Find_wMap2(tMap, Tempitem, "atmosphericPressure", "Double");
            Find_wMap2(tMap, Tempitem, "seaLevelPressure", "Double");
            Find_wMap2(tMap, Tempitem, "rainfall", "String");
            Find_wMap2(tMap, Tempitem, "hourlyRainfall", "Double");
            Find_wMap2(tMap, Tempitem, "dailyRainfall", "Double");
            Find_wMap2(tMap, Tempitem, "vaporPressure", "Double");
            Find_wMap2(tMap, Tempitem, "dewPoint", "Double");
            Find_wMap2(tMap, Tempitem, "sunshine", "Double");
            Find_wMap2(tMap, Tempitem, "insolation", "Double");
            Find_wMap2(tMap, Tempitem, "snowfall", "Double");
            Find_wMap2(tMap, Tempitem, "snowfallHour3", "Double");
            Find_wMap2(tMap, Tempitem, "visibility", "Double");

            Find_wMap(tMap, "deviceType").put("value", "Static");

            wMap = (Map) tMap.get("globalLocationNumber");
            wMap.put("value", id);

            wMap = (Map) tMap.get("dataProvider");
            wMap.put("value", iSvc.optString("dataProvider", "https://www.weather.go.kr"));

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
          }
        }
      } // end for
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

  void TempSwich(JSONObject Tempitem, JSONObject item, String categoryName, String wMapName, String TypeName) {

    if (categoryName.equals(item.getString("category"))) {
      if (TypeName.equals("Double"))
        Tempitem.put(wMapName, item.optDouble("fcstValue"));
      else if (TypeName.equals("Int"))
        Tempitem.put(wMapName, item.optInt("fcstValue"));
      else if (TypeName.equals("String"))
        Tempitem.put(wMapName, item.optString("fcstValue"));
      Tempitem.put(wMapName + "Time", item.optString("fcstDate") + item.optString("fcstTime"));
    }

  }

  void Find_wMap2(Map<String, Object> tMap, JSONObject Tempitem, String Name, String TypeName) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    if (Tempitem.has(Name)) {
      ValueMap = (Map) tMap.get(Name);

      if (TypeName.equals("Double"))
        ValueMap.put("value", Tempitem.optDouble(Name));
      else if (TypeName.equals("Int"))
        ValueMap.put("value", Tempitem.optInt(Name));
      else if (TypeName.equals("String"))
        ValueMap.put("value", Tempitem.optString(Name));

      Calendar cal = Calendar.getInstance();
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
      Date date = null;
      try {
        date = df.parse(Tempitem.optString(Name + "Time"));
      } catch (ParseException e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
      cal.setTime(date);
      DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");
      ValueMap.put("observedAt", df2.format(cal.getTime()));
    } else
      tMap.remove(Name);
  }

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    ValueMap.put("observedAt", DateUtil.getTime());
    return ValueMap;
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
} // end of class
