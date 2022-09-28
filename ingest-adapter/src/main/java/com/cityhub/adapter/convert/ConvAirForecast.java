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

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.GradeType;
import com.cityhub.utils.JsonUtil;

public class ConvAirForecast extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {
    StringBuffer sendJson = new StringBuffer();

    try {
      JSONObject jTemplate = new JSONObject(templateItem.getJSONObject(ConfItem.getString("model_id")).toString());
      JSONArray serviceList = ConfItem.getJSONArray("serviceList");
      for (int k = 0; k < serviceList.length(); k++) {
        JSONObject iSvc = serviceList.getJSONObject(k);
        id = iSvc.getString("gs1Code");

        String dateCreated = DateUtil.getTime();
        String[] types = { "pm10", "pm25", "o3" };
        JSONArray aForecast = new JSONArray();
        JSONArray urlAddr = iSvc.getJSONArray("url_addr");
        String objLength = "";
        for (int j = 0; j < 2; j++) {
          JSONObject forecast = new JSONObject();
          for (int i = 0; i < urlAddr.length(); i++) {
            iSvc.put("url_addr", urlAddr.getString(i));
            JSONObject obj = (JSONObject) CommonUtil.getData(iSvc);
            objLength += obj.toString();
            if (!obj.has("list")) {
              throw new CoreException(ErrorCode.NORMAL_ERROR);
            }

            JSONArray objarr = obj.getJSONArray("list");
            if (objarr.length() > 0) {
              JSONObject item = (JSONObject) objarr.get(0);
              int cnt = 0;
              for (String type : types) {
                if (type.equals(item.getString("informCode").toLowerCase())) {
                  cnt++;
                  String[] strs = item.getString("informGrade").replace(" ", "").split(",", -1);
                  forecast.put(type + "Category", JsonUtil.nvl(getGrade(strs, iSvc.getString("zoneName"))));
                }
              } // for (String type : types)
              if (cnt == 0) {
                throw new Exception(SocketCode.DATA_CONVERT_FAIL.getCode());
              }
            } else {
              log(SocketCode.DATA_CONVERT_FAIL, id, "파싱하기 위한 필수항목이 존재하지 않습니다");
            } // if (objarr.length() > 0)
          } // for (int i = 0; i < urlAddr.length(); j++)
          String add = DateUtil.addDate(dateCreated, ChronoUnit.HOURS, j + 1, "yyyyMMddHH");
          forecast.put("predictedAt", DateUtil.getISOTime(add));
          aForecast.put(forecast);
        } // for (int j = 0; j < 2; j++)

        log(SocketCode.DATA_RECEIVE, id, objLength.getBytes());

        JsonUtil jsonEx = new JsonUtil(jTemplate);
        jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://cityhub.kr/ngsi-ld/airquality.jsonld"));
        jsonEx.put("id", id);
        jsonEx.put("location.value.coordinates", iSvc.getJSONArray("location"));
        jsonEx.put("indexRef.value", "https://www.airkorea.or.kr/web/khaiInfo");
        jsonEx.put("address.value.addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")));
        jsonEx.put("address.value.addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")));
        jsonEx.put("address.value.addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")));
        jsonEx.put("address.value.addressTown", JsonUtil.nvl(iSvc.getString("addressTown")));
        jsonEx.put("address.value.streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")));

        jsonEx.put("airQualityIndexPrediction.value", aForecast);
        jsonEx.put("airQualityIndexPrediction.observedAt", dateCreated);

        List<String> rmKeys = new ArrayList<>();
        rmKeys.add("airQualityPrediction");
        rmKeys.add("name");
        JsonUtil.removeNullItem(jTemplate, "airQualityIndexPrediction.value", rmKeys);

        log(SocketCode.DATA_CONVERT_SUCCESS, id, jTemplate.toString().getBytes());
        sendJson.append(jTemplate.toString() + ",");

      } // for (int k = 0 ; k < serviceList.length(); k++)

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
