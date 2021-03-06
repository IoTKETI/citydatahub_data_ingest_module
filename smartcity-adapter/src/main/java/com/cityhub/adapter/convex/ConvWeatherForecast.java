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

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.WeatherType;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWeatherForecast extends AbstractConvert {
  private ObjectMapper objectMapper;
  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE ));
  }

  @Override
  public String doit() throws CoreException {
    List<Map<String,Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {
      JSONArray serviceList = ConfItem.getJSONArray("serviceList");

      for (int i = 0; i < serviceList.length(); i++) {
        JSONObject iSvc = serviceList.getJSONObject(i);
        id = iSvc.getString("gs1Code");
        JSONObject jsonData = (JSONObject) CommonUtil.getData(iSvc);
        Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});

        JsonUtil ju = new JsonUtil(jsonData);
        log.info("ju : {}", ju);
        if (!ju.has("response.body.items.item") == true) {
          log(SocketCode.DATA_RECEIVE, id);
          log(SocketCode.DATA_CONVERT_FAIL,  id ,"???????????? ?????? ??????????????? ???????????? ????????????");
          throw new CoreException(ErrorCode.NORMAL_ERROR);
        } else {
          log(SocketCode.DATA_RECEIVE, id, jsonData.toString().getBytes());
          JSONArray arrList = ju.getArray("response.body.items.item");

          if (arrList.length() > 0) {
            List<String> tmpFcstTimeArray = new LinkedList<>();
            for (Object jitem : arrList) {
              JSONObject item = (JSONObject) jitem;
              String fcstDateTime = item.get("fcstDate") + "" + item.get("fcstTime");
              tmpFcstTimeArray.add(fcstDateTime);
            }
            List<String> rmList = new LinkedList<>(new HashSet<>(tmpFcstTimeArray));
            rmList.sort( (a, b) -> a.compareTo( b ) );

            Map<String, Object> tmpMap = getBaseForeMap(rmList);

            for (Object jitem : arrList) {
              JSONObject item = (JSONObject) jitem;
              String fcstDateTime = item.get("fcstDate") + "" + item.get("fcstTime");

              Map<String, Object> cTimeMap = (Map<String, Object>) tmpMap.get(fcstDateTime);

              if ("PTY".equals(item.getString("category"))) {
                cTimeMap.put("rainType", WeatherType.findBy(Integer.parseInt(item.get("fcstValue")+"")).getEngNm() );
                if(WeatherType.findBy(Integer.parseInt(item.get("fcstValue")+"")).getEngNm().length()==0) {
                  cTimeMap.put("rainType", WeatherType.findBy(0).getEngNm() );
                }
              }
              if ("T1H".equals(item.getString("category"))) {
                cTimeMap.put("temperature", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT) );
              }
              if ("RN1".equals(item.getString("category"))) {
                cTimeMap.put("rainfall", JsonUtil.nvl(item.get("fcstValue"), DataType.INTEGER) );
              }
              if ("WSD".equals(item.getString("category"))) {

                cTimeMap.put("windSpeed", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT) );
              }
              if ("REH".equals(item.getString("category"))) {
                cTimeMap.put("humidity", JsonUtil.nvl(item.get("fcstValue"), DataType.INTEGER) );
              }
              if ("TMN".equals(item.getString("category"))) {
                cTimeMap.put("lowestTemperature", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT) );
              }
              if ("TMX".equals(item.getString("category"))) {
                cTimeMap.put("highestTemperature", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT) );
              }
              if ("T3H".equals(item.getString("category"))) {
                cTimeMap.put("feelsLikeTemperature", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT) );
              }
              cTimeMap.put("predictedAt", DateUtil.getISOTime(fcstDateTime));
              tmpMap.put(fcstDateTime, cTimeMap);
            }
            // ???????????? ????????? ????????? ???????????? ??????
            // ???????????? ??? ?????? JSONArray??? ?????? ??????
            List<Map<String,Object>> forecastValueArray = new LinkedList<>();
            tmpMap.forEach((k, v) -> forecastValueArray.add((Map) v));
            // ???????????? ??? ?????? JSONArray??? ?????? ??????

            Map<String,Object> addrValue = (Map)((Map)tMap.get("address")).get("value");
            addrValue.put("addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")) );
            addrValue.put("addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")) );
            addrValue.put("addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")) );
            addrValue.put("addressTown", JsonUtil.nvl(iSvc.getString("addressTown")) );
            addrValue.put("streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")) );

            Map<String,Object> locMap = (Map)tMap.get("location");
            Map<String,Object> locValueMap  = (Map)locMap.get("value");
            locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

            tMap.put("id", iSvc.getString("gs1Code"));

            Map<String,Object> weather = new LinkedHashMap<>();
            weather.put("type","Property");
            weather.put("observedAt",DateUtil.getTime());
            weather.put("value",forecastValueArray);
            tMap.put("weatherPrediction", weather);

            tMap.remove("name");

            rtnList.add(tMap);
            String str = objectMapper.writeValueAsString(tMap);
            log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());

          } else {
            log(SocketCode.DATA_CONVERT_FAIL, id);
          } // if (arrList.length() > 0)

        } // if (!ju.has("response.body.items.item") == true)
      } // for (int i = 0; i < serviceList.length(); i++)

      rtnStr = objectMapper.writeValueAsString(rtnList);
      log.info("rtnStr:{}",rtnStr);
    } catch (CoreException e) {
      e.printStackTrace();
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL,   id, e.getMessage());
      }
    } catch (Exception e) {
      e.printStackTrace();
      log(SocketCode.DATA_CONVERT_FAIL,   id, e.getMessage());
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage(), e);
    }

    return rtnStr;
  }


  public Map<String, Object> getBaseForeMap(List<String>  fcstTimeList) {

    // ??????????????? ???????????? ????????? ????????? ??????
    Map<String, Object> tmpMap = new LinkedHashMap<>();
    for (String item : fcstTimeList) {
      Map<String, Object> foreMap = new LinkedHashMap<>();
      tmpMap.put(item, foreMap);
    }
    // ??????????????? ???????????? ????????? ????????? ??????

    return tmpMap;
  }


} // end of class
