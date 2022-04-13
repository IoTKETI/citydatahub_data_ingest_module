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
import com.cityhub.utils.RoadType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvRoadLinkTrafficInformation extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {
    StringBuffer sendJson = new StringBuffer();
    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      JSONObject ori = templateItem.getJSONObject(ConfItem.getString("model_id"));
      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);
        id = iSvc.getString("gs1Code");

        JSONObject jTemplate = new JSONObject(ori.toString());
        JsonUtil jsonEx = new JsonUtil(jTemplate);

        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));

        log.info("ju:{}",ju.toString());

        if (!ju.has("ServiceResult.msgBody.itemList") ) {
          throw new CoreException(ErrorCode.NORMAL_ERROR);
        } else {
          log(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());

          log.info("ServiceResult:{}",ju.get("ServiceResult"));
          log.info("getObj:{}",ju.getObj("ServiceResult.msgBody.itemList"));

          JSONObject itemList =  (JSONObject) ju.getObj("ServiceResult.msgBody.itemList");


//          log.info("*******************");
//          log.info("*******************");




          if (itemList.length() > 0) {


          jsonEx.put("routeId.value", JsonUtil.nvl(itemList.get("routeId") , DataType.STRING));
          jsonEx.put("routeName.value", JsonUtil.nvl(itemList.get("routeNm") , DataType.STRING));
          jsonEx.put("speed.value", JsonUtil.nvl(itemList.get("spd") , DataType.FLOAT));
          jsonEx.put("trafficVolume.value", JsonUtil.nvl(itemList.get("vol") , DataType.FLOAT));
          jsonEx.put("travelTime.value", JsonUtil.nvl(itemList.get("trvlTime") , DataType.FLOAT));
          jsonEx.put("linkDelayTime.value", JsonUtil.nvl(itemList.get("linkDelayTime") , DataType.FLOAT));
//          jsonEx.put("congestedGrade.value", JsonUtil.nvl(itemList.get("congGrade") , DataType.STRING));
          jsonEx.put("congestedGrade.value", RoadType.findBy(itemList.getInt("congGrade")).getEngNm());

//            jsonEx.put("interaval", JsonUtil.nvl(itemList.getString("") , DataType.STRING));
//            jsonEx.put("taste", JsonUtil.nvl(itemList.getString("item1") , DataType.STRING));
//            jsonEx.put("odor", JsonUtil.nvl(itemList.getString("item2") , DataType.STRING));
//            jsonEx.put("chromaticity", JsonUtil.nvl(itemList.get("item3") , DataType.FLOAT));
//            jsonEx.put("hydrogenIndex", JsonUtil.nvl(itemList.get("item4") , DataType.FLOAT));
//            jsonEx.put("turbidity", JsonUtil.nvl(itemList.get("item5") , DataType.FLOAT));
//            jsonEx.put("residualChlorine", JsonUtil.nvl(itemList.get("item6") , DataType.FLOAT));



            jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://cityhub.kr/ngsi-ld/trafficInformation.jsonld"));
            jsonEx.put("id", iSvc.getString("gs1Code"));
            jsonEx.put("location.value.coordinates", iSvc.getJSONArray("location"));
            jsonEx.put("location.observedAt", DateUtil.getTime());
            jsonEx.put("address.value.addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")) );
            jsonEx.put("address.value.addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")) );
            jsonEx.put("address.value.addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")) );
            jsonEx.put("address.value.addressTown", JsonUtil.nvl(iSvc.getString("addressTown")) );
            jsonEx.put("address.value.streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")) );

            List<String> rmKeys = new ArrayList<>();

            String[] searchKey = {"routeId", "routeName", "speed", "trafficVolume", "travelTime", "linkDelayTime", "congestedGrade", "address.value"};
            JsonUtil.removeNullItem(jTemplate, searchKey , new ArrayList<String>()  );
//            JsonUtil.removeNullItem(jTemplate,"RoadLinkTrafficInformation.value", rmKeys );
            log(SocketCode.DATA_CONVERT_SUCCESS, id,jTemplate.toString().getBytes());
            sendJson.append(jTemplate.toString() + ",");
            log.info("jTemplate:{}",jTemplate.toString());

          } else {
            log(SocketCode.DATA_CONVERT_FAIL, "" , id);
          }


        }
      }



    } catch (CoreException e) {
      e.printStackTrace();
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, id,  e.getMessage());
      }
    } catch (Exception e) {
      e.printStackTrace();
      log(SocketCode.DATA_CONVERT_FAIL,  id,  e.getMessage());
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage() + "`" + id  , e);
    }

    return sendJson.toString();
  }


} // end of class
