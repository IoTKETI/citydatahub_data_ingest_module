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
import java.util.List;

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

public class ConvWeatherObserved extends AbstractConvert {

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
        id = iSvc.getString("gs1Code");
        JSONObject jTemplate = templateItem.getJSONObject(ConfItem.getString("model_id"));
        JsonUtil jsonEx = new JsonUtil(jTemplate);

        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
        if (!ju.has("response.body.items.item") ) {
          throw new CoreException(ErrorCode.NORMAL_ERROR);
        } else {
          log(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());
          JSONArray arrList = ju.getArray("response.body.items.item");

          if (arrList.length() > 0) {
            for (Object obj : arrList) {
              JSONObject item = (JSONObject) obj;

              if ("PTY".equals(item.getString("category"))) {
                jsonEx.put("weatherObservation.value.rainType", WeatherType.findBy(item.getInt("obsrValue")).getEngNm());
              }

              if ("T1H".equals(item.getString("category"))) {
                jsonEx.put("weatherObservation.value.temperature", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
              }

              if ("RN1".equals(item.getString("category"))) {
                jsonEx.put("weatherObservation.value.rainfall", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
                jsonEx.put("weatherObservation.value.hourlyRainfall", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
              }
              if ("WSD".equals(item.getString("category"))) {
                jsonEx.put("weatherObservation.value.windSpeed", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
              }
              if ("REH".equals(item.getString("category"))) {
                jsonEx.put("weatherObservation.value.humidity", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
              }
              if ("S06".equals(item.getString("category"))) {
                jsonEx.put("weatherObservation.value.snowfall", JsonUtil.nvl(item.get("obsrValue") , DataType.FLOAT));
              }
            }
            jsonEx.put("weatherObservation.observedAt", DateUtil.getTime());

            jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://cityhub.kr/ngsi-ld/weather.jsonld"));
            jsonEx.put("id", iSvc.getString("gs1Code"));
            jsonEx.put("location.value.coordinates", iSvc.getJSONArray("location"));
            jsonEx.put("location.observedAt", DateUtil.getTime());
            jsonEx.put("address.value.addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")) );
            jsonEx.put("address.value.addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")) );
            jsonEx.put("address.value.addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")) );
            jsonEx.put("address.value.addressTown", JsonUtil.nvl(iSvc.getString("addressTown")) );
            jsonEx.put("address.value.streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")) );

            List<String> rmKeys = new ArrayList<String>();
            rmKeys.add("name");
            JsonUtil.removeNullItem(jTemplate,"weatherObservation.value", rmKeys );

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
      log(SocketCode.DATA_CONVERT_FAIL,  id, e.getMessage() );
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage() + "`" + id  , e);
    }

    return sendJson.toString();
  }


} // end of class
