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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvParkingOneM2M extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);

    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.ACCEPT, "application/json");
    headers.put("X-M2M-Origin", "SW001");
    headers.put("X-M2M-RI", "cityhub");

    try {

      String u = ConfItem.getString("metaInfo");
      HttpResponse discovery = OkUrlUtil.get(u + "?fu=1&ty=3", headers);
      if (discovery.getStatusCode() == 200) {
        JSONObject dis = new JSONObject(discovery.getPayload());
        for (Object obj : dis.getJSONArray("m2m:uril")) {
          String sp = (String) obj;
          String[] args = sp.split("/", -1);
          if (args.length == 3) {
            String url = u + "/" + args[2] + "/meta/la";
            HttpResponse info = OkUrlUtil.get(url, headers);
            if (info.getStatusCode() == 200) {
              JsonUtil ju = new JsonUtil(info.getPayload());
              if (ju.has("m2m:cin.con")) {
                JSONObject jObj = ju.getObject("m2m:cin.con");
                ConfItem.put(args[2], jObj);
              }
            }
          }
        }
      }

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  @Override
  public String doit(byte[] message) throws CoreException {

    StringBuffer sendJson = new StringBuffer();
    try {
      String msg = new String(message);

      if (JsonUtil.has(msg, "pc.m2m:sgn.nev.rep.m2m:cin.con") == true) {
        String sur = JsonUtil.get(msg, "pc.m2m:sgn.sur");
        String contents = JsonUtil.get(msg, "pc.m2m:sgn.nev.rep.m2m:cin.con");

        JSONObject jo = null;
        String[] Park = sur.split("/", -1);
        if (Park.length == 4) {
          // 주차장
          JsonUtil parkInfo = new JsonUtil(ConfItem.getJSONObject(Park[2]));
          jo = templateItem.getJSONObject("OffStreetParking");

          id = "urn:datahub:" + jo.getString("type") + ":" + Park[2];
          log(SocketCode.DATA_RECEIVE, id, parkInfo.toString().getBytes());

          JsonUtil.put(jo, "@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://cityhub.kr/ngsi-ld/parking.jsonld"));
          JsonUtil.put(jo, "id", "urn:datahub:" + jo.getString("type") + ":" + Park[2]);
          JsonUtil.put(jo, "location.value.coordinates", parkInfo.getArray("location.coordinates"));
          JsonUtil.put(jo, "address.value", JsonUtil.nvl(parkInfo.getObject("address")));

          JsonUtil.put(jo, "locationTag.value", JsonUtil.nvl(parkInfo.get("locationTag")));
          JsonUtil.put(jo, "category.value", parkInfo.getArray("category"));
          JsonUtil.put(jo, "paymentAccepted.value", parkInfo.getArray("paymentAccepted"));
          JsonUtil.put(jo, "priceRate.value", JsonUtil.nvl(parkInfo.get("priceRate"), DataType.STRING));
          JsonUtil.put(jo, "priceCurrency.value", parkInfo.getArray("priceCurrency"));
          JsonUtil.put(jo, "image.value", parkInfo.get("image"));
          JsonUtil.put(jo, "totalSpotNumber.value", JsonUtil.nvl(parkInfo.get("totalSpotNumber"), DataType.INTEGER));
          JsonUtil.put(jo, "maximumAllowedHeight.value", JsonUtil.nvl(parkInfo.get("maximumAllowedHeight"), DataType.FLOAT));
          JsonUtil.put(jo, "openingHours.value", parkInfo.getArray("openingHours"));

          JsonUtil.put(jo, "contactPoint.value", JsonUtil.nvl(parkInfo.getObject("contactPoint")));
          JsonUtil.put(jo, "status.value", parkInfo.getArray("status"));

          JsonUtil.put(jo, "name.value", parkInfo.get("name"));
          JsonUtil.put(jo, "availableSpotNumber.value", JsonUtil.nvl(contents, DataType.INTEGER));
          JsonUtil.put(jo, "availableSpotNumber.observedAt", DateUtil.getTime());

          JsonUtil.put(jo, "refParkingSpots.value", parkInfo.getArray("refParkingSpots"));

          List<String> rmKeys = new ArrayList<>();
          rmKeys.add("congestionIndexPrediction");
          JsonUtil.removeNullItem(jo, "contactPoint.value", rmKeys);

        } else {
          // 주차면
          if (!"meta".equals(Park[3]) && !"keepalive".equals(Park[3])) {
            JsonUtil parkInfo = new JsonUtil(ConfItem.getJSONObject(Park[2]));
            jo = templateItem.getJSONObject("ParkingSpot");
            id = "urn:datahub:" + jo.getString("type") + ":" + Park[3];
            log(SocketCode.DATA_RECEIVE, id, parkInfo.toString().getBytes());

            JsonUtil.put(jo, "@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://cityhub.kr/ngsi-ld/parking.jsonld"));
            JsonUtil.put(jo, "id", "urn:datahub:" + jo.getString("type") + ":" + Park[3]);

            JsonUtil.put(jo, "location.value.coordinates", parkInfo.getArray("location.coordinates"));
            JsonUtil.put(jo, "address.value", JsonUtil.nvl(parkInfo.getObject("address")));

            JsonUtil.put(jo, "length.value", 5.1);
            JsonUtil.put(jo, "width.value", 2.5);
            JsonUtil.put(jo, "category.value", new JSONArray().put("forDisabled"));
            JsonUtil.put(jo, "refParkingLot.value", "urn:datahub:OffStreetParking:" + Park[2]);

            JsonUtil.put(jo, "name.value", JsonUtil.nvl(Park[3]));
            JsonUtil.put(jo, "status.value", JsonUtil.nvl(contents));
            JsonUtil.put(jo, "status.observedAt", DateUtil.getTime());
          }
        }
        log(SocketCode.DATA_CONVERT_SUCCESS, id, jo.toString().getBytes());
        sendJson.append(jo.toString() + ",");
      }

    } catch (CoreException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage(), e);
    }
    return sendJson.toString();
  }

} // end of class
