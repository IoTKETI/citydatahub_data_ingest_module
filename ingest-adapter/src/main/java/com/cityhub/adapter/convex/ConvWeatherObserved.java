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
public class ConvWeatherObserved extends AbstractConvert {
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
      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);
        id = iSvc.getString("gs1Code");

        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
        log.info("jujuju: {}",ju);
        if (!ju.has("response.body.items.item") ) {
          throw new CoreException(ErrorCode.NORMAL_ERROR);
        } else {
          log(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());
          JSONArray arrList = ju.getArray("response.body.items.item");
          Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});

          Map<String,Object> wMap = new LinkedHashMap<>();
          if (arrList.length() > 0) {
            for (Object obj : arrList) {
              JSONObject item = (JSONObject) obj;
              if ("PTY".equals(item.getString("category"))) {
                wMap.put("rainType", WeatherType.findBy(item.getInt("obsrValue")).getEngNm());
              }
              if ("T1H".equals(item.getString("category"))) {
                wMap.put("temperature", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
              }
              if ("RN1".equals(item.getString("category"))) {
                wMap.put("rainfall", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
                wMap.put("hourlyRainfall", JsonUtil.nvl(item.get("obsrValue") , DataType.INTEGER));
              }
              if ("WSD".equals(item.getString("category"))) {
                wMap.put("windSpeed", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
              }
              if ("REH".equals(item.getString("category"))) {
                wMap.put("humidity", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
              }
              if ("S06".equals(item.getString("category"))) {
                wMap.put("snowfall", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
              }
            } // end for

            Map<String,Object> addrValue = (Map)((Map)tMap.get("address")).get("value");
            addrValue.put("addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")) );
            addrValue.put("addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")) );
            addrValue.put("addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")) );
            addrValue.put("addressTown", JsonUtil.nvl(iSvc.getString("addressTown")) );
            addrValue.put("streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")) );

            Map<String,Object> locMap = (Map)tMap.get("location");
            locMap.put("observedAt",DateUtil.getTime());
            Map<String,Object> locValueMap  = (Map)locMap.get("value");
            
            locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

            tMap.put("id", iSvc.getString("gs1Code"));
            Map<String,Object> weatherObservation = new LinkedHashMap<>();
            weatherObservation.put("type","Property");
            weatherObservation.put("observedAt",DateUtil.getTime());
            weatherObservation.put("value",wMap);
            tMap.put("weatherObservation", weatherObservation);

            tMap.remove("airQualityIndexObservation");
            log.info("tMap:{}", tMap);
            rtnList.add(tMap);
            String str = objectMapper.writeValueAsString(tMap);
            log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
          } else {
            log(SocketCode.DATA_CONVERT_FAIL, id);
          } // end if (arrList.length() > 0)
        } // if (!ju.has("response.body.items.item") )
      }  // for (int i = 0; i < svcList.length(); i++)
      rtnStr = objectMapper.writeValueAsString(rtnList);
    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL,  id, e.getMessage() );
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage() + "`" + id  , e);
    }

    return rtnStr;
  }


} // end of class