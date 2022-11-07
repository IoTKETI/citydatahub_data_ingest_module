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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.json.JSONObject;

import com.cityhub.exception.CoreException;
import com.cityhub.core.AbstractConvert;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvParkingOneM2M extends AbstractConvert {

  @Override
  public void setup() {
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
                log.info("meta Info has: {},{},{}",url,ju.has("m2m:cin.con"), ju.getObject("m2m:cin.con"));
                ConfItem.put(args[2], jObj);
              }
            }
          }
        }
      }
      log.info("parkinfo:{}", ConfItem.toString());
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }


  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public String doit(byte[] message) {
    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    String modelType = "";
    try {
      String msg = new String(message);

      if (JsonUtil.has(msg, "pc.m2m:sgn.nev.rep.m2m:cin.con") == true) {
        String sur = JsonUtil.get(msg, "pc.m2m:sgn.sur");
        String contents = JsonUtil.get(msg, "pc.m2m:sgn.nev.rep.m2m:cin.con");
        String[] Park = sur.split("/", -1);
        if (Park.length == 4) {
          JsonUtil parkInfo = null;
          try {
            parkInfo = new JsonUtil(ConfItem.getJSONObject(Park[2]));
          } catch (JSONException e) {
            log.info("OffStreeting:{}", Park[2]);
            parkInfo = new JsonUtil(ConfItem.getJSONObject("yt_lot_1"));
          }

          Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject("OffStreetParking").toString(), new TypeReference<Map<String, Object>>() {
          });
          modelType = tMap.get("type").toString();
          id = "urn:datahub:" + tMap.get("type") + ":" + Park[2];
          toLogger(SocketCode.DATA_RECEIVE, id, parkInfo.toString().getBytes());

          Map<String, Object> address = new LinkedHashMap<>();
          address.put("type", "Property");
          address.put("observedAt", DateUtil.getTime());
          address.put("value", JsonUtil.nvl(parkInfo.getObject("address").toMap()));
          tMap.put("address", address);

          Map<String, Object> locMap = (Map) tMap.get("location");
          Map<String, Object> locValueMap = (Map) locMap.get("value");
          locValueMap.put("coordinates", parkInfo.getArray("location.coordinates").toList());

          tMap.put("id", "urn:datahub:" + tMap.get("type") + ":" + Park[2]);

          ((Map) tMap.get("locationTag")).put("value", JsonUtil.nvl(parkInfo.get("locationTag")));
          ((Map) tMap.get("category")).put("value", parkInfo.getArray("category").toList());
          ((Map) tMap.get("paymentAccepted")).put("value", parkInfo.getArray("paymentAccepted").toList());
          ((Map) tMap.get("priceRate")).put("value", JsonUtil.nvl(parkInfo.get("priceRate"), DataType.STRING));
          ((Map) tMap.get("priceCurrency")).put("value", parkInfo.getArray("priceCurrency").toList());
          ((Map) tMap.get("image")).put("value", parkInfo.get("image"));
          ((Map) tMap.get("totalSpotNumber")).put("value", JsonUtil.nvl(parkInfo.get("totalSpotNumber"), DataType.INTEGER));
          ((Map) tMap.get("maximumAllowedHeight")).put("value", JsonUtil.nvl(parkInfo.get("maximumAllowedHeight"), DataType.FLOAT));
          ((Map) tMap.get("openingHours")).put("value", parkInfo.getArray("openingHours").toList());
          ((Map) tMap.get("contactPoint")).put("value", JsonUtil.nvl(parkInfo.getObject("contactPoint").toMap()));
          ((Map) tMap.get("status")).put("value", parkInfo.getArray("status").toList());
          ((Map) tMap.get("name")).put("value", parkInfo.get("name"));

          if (ConfItem.has(Park[2])) {
            ((Map) tMap.get("refParkingSpots")).put("value", parkInfo.getArray("refParkingSpots").toList());
          }

          ((Map) tMap.get("availableSpotNumber")).put("value", JsonUtil.nvl(contents, DataType.INTEGER));
          ((Map) tMap.get("availableSpotNumber")).put("observedAt", DateUtil.getTime());

          tMap.remove("inAccident");
          tMap.remove("category");
          tMap.remove("congestionIndexPrediction");
          tMap.remove("predictions");

          rtnList.add(tMap);
          String str = objectMapper.writeValueAsString(tMap);
          toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
        } else {
          if (!"meta".equals(Park[3]) && !"keepalive".equals(Park[3])) {
            JsonUtil parkInfo = null;
            try {
              parkInfo = new JsonUtil(ConfItem.getJSONObject(Park[2]));
            } catch (JSONException e) {
              log.info("OffStreeting:{}", Park[2]);
              parkInfo = new JsonUtil(ConfItem.getJSONObject("yt_lot_1"));
            }

            Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject("ParkingSpot").toString(), new TypeReference<Map<String, Object>>() {
            });
            id = "urn:datahub:" + tMap.get("type") + ":" + Park[3];
            modelType = tMap.get("type").toString();
            toLogger(SocketCode.DATA_RECEIVE, id, parkInfo.toString().getBytes());

            Map<String, Object> address = new LinkedHashMap<>();
            address.put("type", "Property");
            address.put("value", JsonUtil.nvl(parkInfo.getObject("address").toMap()));
            tMap.put("address", address);

            Map<String, Object> locMap = (Map) tMap.get("location");
            Map<String, Object> locValueMap = (Map) locMap.get("value");
            locValueMap.put("coordinates", parkInfo.getArray("location.coordinates").toList());

            tMap.put("id", "urn:datahub:" + tMap.get("type") + ":" + Park[3]);

            ((Map) tMap.get("length")).put("value", 5.1);
            ((Map) tMap.get("width")).put("value", 2.5);
            List category = new LinkedList();
            category.add("forDisabled");
            ((Map) tMap.get("category")).put("value", category);
            ((Map) tMap.get("refParkingLot")).put("value", "urn:datahub:OffStreetParking:" + Park[2]);
            ((Map) tMap.get("name")).put("value", JsonUtil.nvl(Park[3]));

            ((Map) tMap.get("status")).put("value", JsonUtil.nvl(contents));
            ((Map) tMap.get("status")).put("observedAt", DateUtil.getTime());

            tMap.remove("refParkingLot");

            rtnList.add(tMap);
            String str = objectMapper.writeValueAsString(tMap);
            toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
          } // if (!"meta".equals(Park[3]) && !"keepalive".equals(Park[3]) )

        } // if (Park.length == 4)

        rtnStr = objectMapper.writeValueAsString(rtnList);
      } // if ( JsonUtil.has(msg, "pc.m2m:sgn.nev.rep.m2m:cin.con") == true)

    } catch (CoreException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      if ("!C0099".equals(e.getErrorCode())) {
        toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return rtnStr;
  }

} // end of class
