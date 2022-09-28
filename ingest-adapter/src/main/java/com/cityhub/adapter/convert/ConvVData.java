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
import com.cityhub.utils.GradeType;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvVData extends AbstractConvert {

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
        JSONObject jsonData = (JSONObject) CommonUtil.getData(iSvc);
        if (!jsonData.has("list")) {
          throw new CoreException(ErrorCode.NORMAL_ERROR);
        } else {
          log(SocketCode.DATA_RECEIVE, id, jsonData.toString().getBytes());
          JSONArray jarr = jsonData.getJSONArray("list");

          if (jarr.length() > 0) {
            for (Object jitem : jarr) {
              JSONObject item = (JSONObject) jitem;

              JSONObject jTemplate = templateItem.getJSONObject(ConfItem.getString("model_id"));
              JsonUtil jsonEx = new JsonUtil(jTemplate);

              jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://cityhub.kr/ngsi-ld/airquality.jsonld"));
              jsonEx.put("id", iSvc.get("gs1Code") + "");
              jsonEx.put("location.value.coordinates", iSvc.getJSONArray("location"));
              jsonEx.put("location.observedAt", DateUtil.getTime());
              jsonEx.put("indexRef.value", "https://www.airkorea.or.kr/web/khaiInfo");
              jsonEx.put("address.value.addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")));
              jsonEx.put("address.value.addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")));
              jsonEx.put("address.value.addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")));
              jsonEx.put("address.value.addressTown", JsonUtil.nvl(iSvc.getString("addressTown")));
              jsonEx.put("address.value.streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")));

              jsonEx.put("airQualityObservation.value.so2", JsonUtil.nvl(item.get("so2Value"), DataType.FLOAT));
              jsonEx.put("airQualityObservation.value.co", JsonUtil.nvl(item.get("coValue"), DataType.FLOAT));
              jsonEx.put("airQualityObservation.value.o3", JsonUtil.nvl(item.get("o3Value"), DataType.FLOAT));
              jsonEx.put("airQualityObservation.value.no2", JsonUtil.nvl(item.get("no2Value"), DataType.FLOAT));
              jsonEx.put("airQualityObservation.value.pm10", JsonUtil.nvl(item.get("pm10Value"), DataType.FLOAT));
              jsonEx.put("airQualityObservation.value.pm25", JsonUtil.nvl(item.get("pm25Value"), DataType.FLOAT));
              jsonEx.put("airQualityObservation.observedAt", DateUtil.getISOTime((String) JsonUtil.nvl(item.get("dataTime"), DataType.STRING)));

              jsonEx.put("airQualityIndexObservation.observedAt", DateUtil.getISOTime((String) JsonUtil.nvl(item.get("dataTime"), DataType.STRING)));
              if (JsonUtil.nvl(item.getString("so2Grade")) != JSONObject.NULL) {
                int o = (int) JsonUtil.nvl(item.getString("so2Grade"), DataType.INTEGER);
                jsonEx.put("airQualityIndexObservation.value.so2Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
              }
              if (JsonUtil.nvl(item.getString("coGrade")) != JSONObject.NULL) {
                int o = (int) JsonUtil.nvl(item.getString("coGrade"), DataType.INTEGER);
                jsonEx.put("airQualityIndexObservation.value.coCategory", JsonUtil.nvl(GradeType.findBy(o).getValue()));
              }
              if (JsonUtil.nvl(item.getString("o3Grade")) != JSONObject.NULL) {
                int o = (int) JsonUtil.nvl(item.getString("o3Grade"), DataType.INTEGER);
                jsonEx.put("airQualityIndexObservation.value.o3Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
              }
              if (JsonUtil.nvl(item.getString("no2Grade")) != JSONObject.NULL) {
                int o = (int) JsonUtil.nvl(item.getString("no2Grade"), DataType.INTEGER);
                jsonEx.put("airQualityIndexObservation.value.no2Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
              }
              if (JsonUtil.nvl(item.getString("pm10Grade")) != JSONObject.NULL) {
                int o = (int) JsonUtil.nvl(item.getString("pm10Grade"), DataType.INTEGER);
                jsonEx.put("airQualityIndexObservation.value.pm10Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
              }
              if (JsonUtil.nvl(item.getString("pm25Grade")) != JSONObject.NULL) {
                int o = (int) JsonUtil.nvl(item.getString("pm25Grade"), DataType.INTEGER);
                jsonEx.put("airQualityIndexObservation.value.pm25Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
              }
              if (JsonUtil.nvl(item.getString("khaiValue")) != JSONObject.NULL) {
                int o = (int) JsonUtil.nvl(item.getString("khaiValue"), DataType.INTEGER);
                jsonEx.put("airQualityIndexObservation.value.totalIndex", JsonUtil.nvl(o, DataType.INTEGER));
              }
              if (JsonUtil.nvl(item.getString("khaiGrade")) != JSONObject.NULL) {
                int o = (int) JsonUtil.nvl(item.getString("no2Grade"), DataType.INTEGER);
                jsonEx.put("airQualityIndexObservation.value.totalCategory", JsonUtil.nvl(GradeType.findBy(o).getValue()));
              }

              List<String> rmKeys = new ArrayList<>();
              rmKeys.add("name");
              JsonUtil.removeNullItem(jTemplate, new String[] { "airQualityIndexObservation.value", "airQualityObservation.value" }, rmKeys);

              log(SocketCode.DATA_CONVERT_SUCCESS, id, jTemplate.toString().getBytes());
              sendJson.append(jTemplate.toString() + ",");
            }
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
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
    }

    return sendJson.toString();
  }

} // end of class
