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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.exception.CoreException;
import com.cityhub.source.core.AbstractConvert;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvCrowdSourcingReport_SiheungLivingLab extends AbstractConvert {


  @Override
  public String doit() {
    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {

      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      int IdNumber = 0;
      for (int i = 0; i < svcList.length(); i++) {

        JSONObject iSvc = svcList.getJSONObject(i); // Column별로 분리함
        id = iSvc.getString("gs1Code"); // 분리한 데이터의 gs1Code 값을 is에 넣어줌
        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
        JSONArray arrList = ju.getArray("data.findAllPosts");

        if (arrList.length() > 0) {
          for (Object obj : arrList) {
            JSONObject item = (JSONObject) obj;
            if (item != null) {
              Map<String, Object> wMap = new LinkedHashMap<>(); // 분리한 데이터를 넣어줄 map을 만듬
              Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
              });

              Double latitude = item.optDouble("latitude", 0.0d);
              Double longitude = item.optDouble("longitude", 0.0d);
              String sourcingDescription = item.optString("sourcing_description", "");
              String postDate = item.optString("post_date", DateUtil.getTime());
              String update_date = item.optString("update_date", DateUtil.getTime());
              String upload_date = item.optString("upload_date", DateUtil.getTime());

              update_date = DateUtil.getISOTime(update_date.substring(0, 14));
              upload_date = DateUtil.getISOTime(upload_date);

              wMap.put("postId", item.optString("post_id", ""));
              wMap.put("sourcingTitle", item.optString("sourcing_title", ""));
              wMap.put("sourcingDescription", sourcingDescription);
              wMap.put("latitude", latitude);
              wMap.put("longitude", longitude);
              wMap.put("fileData", item.optString("file_data", ""));
              wMap.put("dislikeCount", item.optInt("dislikeCount", 0));
              wMap.put("obstacle", item.optString("obstacle", ""));
              log.info("postDate : " + postDate);
              log.info("update_date : " + update_date);
              log.info("upload_date : " + upload_date);

              wMap.put("postDate", postDate);
              wMap.put("updateDate", update_date);
              wMap.put("uploadDate", upload_date);

              Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
              addrValue.put("addressCountry", iSvc.optString("addressCountry", ""));
              addrValue.put("addressRegion", iSvc.optString("addressRegion", ""));
              addrValue.put("addressLocality", iSvc.optString("addressLocality", ""));
              addrValue.put("addressTown", sourcingDescription);
              addrValue.put("streetAddress", iSvc.optString("streetAddress", ""));

              Map<String, Object> locMap = (Map) tMap.get("location");
              locMap.put("observedAt", DateUtil.getTime());
              Map<String, Object> locValueMap = (Map) locMap.get("value");

              ArrayList<Double> location = new ArrayList<>();
              location.add(longitude);
              location.add(latitude);

              locValueMap.put("coordinates", location);

              tMap.put("id", iSvc.optString("gs1Code") + "_" + IdNumber++);
              Map<String, Object> EntityAttribute = new LinkedHashMap<>();
              EntityAttribute.put("type", "Property");
              EntityAttribute.put("observedAt", DateUtil.getTime());
              EntityAttribute.put("value", wMap);
              tMap.put("crowdSourcingReportContent", EntityAttribute);

              log.info("tMap : {}", tMap);
              rtnList.add(tMap);
              String str = objectMapper.writeValueAsString(tMap);
              toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
            } else {
              toLogger(SocketCode.DATA_CONVERT_FAIL, id);
            } // end if (arrList.length() > 0)
          }
        }
      } // end for
      rtnStr = objectMapper.writeValueAsString(rtnList);
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

} // end of class