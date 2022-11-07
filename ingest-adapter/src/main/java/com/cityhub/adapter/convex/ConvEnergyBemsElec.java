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

import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.exception.CoreException;
import com.cityhub.core.AbstractConvert;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class ConvEnergyBemsElec extends AbstractConvert {

  @Override
  public String doit() {
//		StringBuffer sendJson = new StringBuffer();
    List<Map<String, Object>> rtnList = new LinkedList<>(); // buffer대신 List로 데이터 받을예정
    String rtnStr = ""; // list로 받은것 string으로 변환해서 적재할거임

    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      log.info("svcList:{}", svcList.toString());

      String model = ConfItem.getString("modelId");
      log.info("model : " + model);

      for (int i = 0; i < svcList.length(); i++) {

        Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> wMap = new LinkedHashMap<>();

        JSONObject iSvc = (JSONObject) svcList.get(i);
        iSvc.put("measureTime", LocalTime.now().getHour() + "");

        JSONObject jsonData = getData(iSvc);
//				log.info("jsonData:{}",jsonData.toString());
        JsonUtil ju = new JsonUtil(jsonData);

        JSONArray itemsArray = null;

        if (ju.has("header") && "SUCCESS".equals(ju.get("header.result")) && ju.has("body.items") && ju.getArray("body.items").length() > 0) {
          itemsArray = ju.getArray("body.items");
        } else {
          itemsArray = new JSONArray();
        }

        for (Object tempItemObj : itemsArray) {
          JsonUtil templateJsonUtil = new JsonUtil(templateItem.getJSONObject(model).toString());
          JSONObject jobj = (JSONObject) tempItemObj;
          inputData(jobj, templateJsonUtil, iSvc);
//					sendJson.append(templateJsonUtil.toString() + ", ");
        }

      } // for i end

    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        toLogger(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
      }
    } catch (Exception e) {
      toLogger(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
    }
//		return sendJson.toString();
    return "";
  } // end of doit

  private void inputData(JSONObject jobj, JsonUtil templateJsonUtil, JSONObject iSvc) {

    templateJsonUtil.put("measurePeak.value", JsonUtil.nvl(jobj.get("peakAmt"), DataType.FLOAT));
    templateJsonUtil.put("measure1To15.value", JsonUtil.nvl(jobj.get("ctagVal00"), DataType.FLOAT));
    templateJsonUtil.put("measure16To30.value", JsonUtil.nvl(jobj.get("ctagVal15"), DataType.FLOAT));
    templateJsonUtil.put("measure31To45.value", JsonUtil.nvl(jobj.get("ctagVal30"), DataType.FLOAT));
    templateJsonUtil.put("measure46To0.value", JsonUtil.nvl(jobj.get("ctagVal45"), DataType.FLOAT));
    templateJsonUtil.put("measure1Hour.value", JsonUtil.nvl(jobj.get("ctagVal1hour"), DataType.FLOAT));
    templateJsonUtil.put("measureTime.value", JsonUtil.nvl(jobj.get("measureTime"), DataType.STRING));
    templateJsonUtil.put("sequence.value", JsonUtil.nvl(jobj.get("ctagCode"), DataType.STRING));
    templateJsonUtil.put("measureDay.value", JsonUtil.nvl(jobj.get("measureDay"), DataType.STRING));

    templateJsonUtil.put("address.value.addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry"), DataType.STRING));
    templateJsonUtil.put("address.value.addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion"), DataType.STRING));
    templateJsonUtil.put("address.value.addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality"), DataType.STRING));
    templateJsonUtil.put("address.value.addressTown", JsonUtil.nvl(iSvc.getString("addressTown"), DataType.STRING));
    templateJsonUtil.put("address.value.streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress"), DataType.STRING));

    templateJsonUtil.put("location.value.coordinates", iSvc.get("location"));
    templateJsonUtil.put("workplaceName.value", JsonUtil.nvl(iSvc.getString("workplaceName"), DataType.STRING));
    templateJsonUtil.put("measurementType.value", JsonUtil.nvl(iSvc.getString("measurementType"), DataType.STRING));
    templateJsonUtil.put("id", JsonUtil.nvl(iSvc.getString("gs1Code"), DataType.STRING) + "." + JsonUtil.nvl(jobj.get("ctagCode"), DataType.STRING));


  }

  private JSONObject getData(JSONObject svc) throws Exception {
    Object obj = null;
    String url_addr = urlAssemble(svc);
    Headers headerbuild = buildHeader(svc);
    Request request = new Request.Builder().url(url_addr).get().headers(headerbuild).build();

    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).writeTimeout(3 * 10, TimeUnit.SECONDS).readTimeout(3 * 10, TimeUnit.SECONDS).build();

    Response response = client.newCall(request).execute();
    String responseBody = response.body().string();
    return new JSONObject(responseBody);
  }

  private String urlAssemble(JSONObject svc) {
    String url = svc.getString("url_addr");
    String[] paramVariable = svc.getString("ParamVariable").split(",");
    String value = "";

    for (String it : paramVariable) {
      if (!"".equals(it) && svc.has(it)) {
        value = svc.getString(it) + "";
        if (value.indexOf(",") > -1) {
          String[] val = value.split(",");
          if (DateUtil.isPatternDate(val[0])) {
            url += "&" + it + "=" + DateUtil.addDate(DateUtil.getChronoUnit(val[1]), Integer.parseInt(val[2]), val[0]);
          }
        } else {
          if (DateUtil.isPatternDate(value)) {
            url += "&" + it + "=" + DateUtil.getDate(value);
          } else {
            url += "&" + it + "=" + value;
          }
        }
      } else {
        if (svc.has("key")) {
          url += "&" + it + "=" + svc.getString("key");
        }
      }
    }
    return url;
  }

  private Headers buildHeader(JSONObject svc) {
    Map h = new HashMap();
    JSONArray headers = svc.getJSONArray("headers");

    for (Object obj : headers) {
      JSONObject jobj = (JSONObject) obj;
      h.putAll(jobj.toMap());
    }

    return Headers.of(h);
  }
}
// end of class