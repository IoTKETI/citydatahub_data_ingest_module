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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.WeatherType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWeatherForecast extends AbstractConvert {

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
        JSONObject iSvc = serviceList.getJSONObject(i);
        id = iSvc.getString("gs1Code");
        JSONObject jsonData = (JSONObject) CommonUtil.getData(iSvc);

        JSONObject jTemplate = templateItem.getJSONObject(ConfItem.getString("model_id"));
        JsonUtil jsonEx = new JsonUtil(jTemplate);
//        log.info("jsonData:{}",jsonData.toString());
        JsonUtil ju = new JsonUtil(jsonData);
        if (!ju.has("response.body.items.item") == true) {
          log(SocketCode.DATA_RECEIVE, id);
          log(SocketCode.DATA_CONVERT_FAIL, id, "파싱하기 위한 필수항목이 존재하지 않습니다");
          throw new CoreException(ErrorCode.NORMAL_ERROR);
        } else {
          log(SocketCode.DATA_RECEIVE, id, jsonData.toString().getBytes());
          JSONArray arrList = ju.getArray("response.body.items.item");

          if (arrList.length() > 0) {
            // 중복제거 시작
            ArrayList<String> tmpFcstTimeArray = new ArrayList<>();
            for (Object jitem : arrList) {
              JSONObject item = (JSONObject) jitem;
              String fcstDateTime = item.get("fcstDate") + "" + item.get("fcstTime");
              tmpFcstTimeArray.add(fcstDateTime);
            }
            ArrayList<String> rmList = new ArrayList<>(new HashSet<>(tmpFcstTimeArray));
            // 중복제거 종료

            // 중복제거 후 리스트를 가지고 기본형 시작
            Map<String, Object> tmpMap = getBaseForeMap(rmList);
            // 중복제거 후 리스트를 가지고 기본형 종료

            // 기본형을 가지고 데이터 업데이트 시작
            for (Object jitem : arrList) {
              JSONObject item = (JSONObject) jitem;
              String fcstDateTime = item.get("fcstDate") + "" + item.get("fcstTime");

              Map<String, Object> cTimeMap = (Map<String, Object>) tmpMap.get(fcstDateTime);

              if ("PTY".equals(item.getString("category"))) {
//              cTimeMap.put("rainType", WeatherType.findBy(item.getInt("fcstValue")).getEngNm() );기존
//              cTimeMap.put("rainType", WeatherType.findBy(Integer.parseInt(item.get("fcstValue")+"")).getEngNm() );
//              cTimeMap.put("rainType", WeatherType.findBy(item.getString("fcstValue")).getEngNm() );
                cTimeMap.put("rainType", WeatherType.findBy(Integer.parseInt(item.get("fcstValue") + "")).getEngNm());
                if (WeatherType.findBy(Integer.parseInt(item.get("fcstValue") + "")).getEngNm().length() == 0) {
                  cTimeMap.put("rainType", WeatherType.findBy(0).getEngNm());
                }
              }
              if ("T1H".equals(item.getString("category"))) {
                cTimeMap.put("temperature", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT));
              }
              if ("RN1".equals(item.getString("category"))) {
                cTimeMap.put("rainfall", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT));
              }
              if ("WSD".equals(item.getString("category"))) {
                cTimeMap.put("windSpeed", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT));
              }
              if ("REH".equals(item.getString("category"))) {
                cTimeMap.put("humidity", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT));
              }
              if ("TMN".equals(item.getString("category"))) {
                cTimeMap.put("lowestTemperature", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT));
              }
              if ("TMX".equals(item.getString("category"))) {
                cTimeMap.put("highestTemperature", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT));
              }
              if ("T3H".equals(item.getString("category"))) {
                cTimeMap.put("feelsLikeTemperature", JsonUtil.nvl(item.get("fcstValue"), DataType.FLOAT));
              }

              cTimeMap.put("predictedAt", DateUtil.getISOTime(fcstDateTime));
              tmpMap.put(fcstDateTime, cTimeMap);
            }
            // 기본형을 가지고 데이터 업데이트 종료

            // 업데이트 된 맵을 JSONArray에 담기 시작
            JSONArray forecastValueArray = new JSONArray();
            tmpMap.forEach((k, v) -> forecastValueArray.put((Map) v));
            // 업데이트 된 맵을 JSONArray에 담기 종료

            jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://cityhub.kr/ngsi-ld/weather.jsonld"));
            jsonEx.put("id", iSvc.get("gs1Code") + "");
            jsonEx.put("location.value.coordinates", iSvc.getJSONArray("location"));
            jsonEx.put("address.value.addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")));
            jsonEx.put("address.value.addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")));
            jsonEx.put("address.value.addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")));
            jsonEx.put("address.value.addressTown", JsonUtil.nvl(iSvc.getString("addressTown")));
            jsonEx.put("address.value.streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")));

            jsonEx.put("weatherPrediction.value", forecastValueArray);
            jsonEx.put("weatherPrediction.observedAt", DateUtil.getTime());

            List<String> rmKeys = new ArrayList<>();
            rmKeys.add("name");
            JsonUtil.removeNullItem(jTemplate, "weatherPrediction.value", rmKeys);

            // sendJson.put(new JSONObject(jTemplate.toString()));
            log(SocketCode.DATA_CONVERT_SUCCESS, id, jTemplate.toString().getBytes());
            sendJson.append(jTemplate.toString() + ",");
          } else {
            log(SocketCode.DATA_CONVERT_FAIL, id);
          }
        }
      }

    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage(), e);
    }

    return sendJson.toString();
  }

  public Map<String, Object> getBaseForeMap(List<String> fcstTimeList) {

    // 중복제거한 리스트를 가지고 기본형 시작
    Map<String, Object> tmpMap = new HashMap<>();
    for (String item : fcstTimeList) {
      Map<String, Object> foreMap = new HashMap<>();
      foreMap.put("rainType", JSONObject.NULL);
      foreMap.put("temperature", JSONObject.NULL);
      foreMap.put("rainfall", JSONObject.NULL);
      foreMap.put("windSpeed", JSONObject.NULL);
      foreMap.put("humidity", JSONObject.NULL);
      foreMap.put("lowestTemperature", JSONObject.NULL);
      foreMap.put("highestTemperature", JSONObject.NULL);
      foreMap.put("feelsLikeTemperature", JSONObject.NULL);
      foreMap.put("predictedAt", "-");
      tmpMap.put(item, foreMap);
    }
    // 중복제거한 리스트를 가지고 기본형 종료

    return tmpMap;
  }

} // end of class
