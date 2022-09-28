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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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

public class ConvWaterQualityTap extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {
    StringBuffer sendJson = new StringBuffer();

    JSONObject ItemAdapter = new JsonUtil().getFileJsonObject("openapi/WaterQualityTapItemAdapter.conf");

    try {
      JSONArray svcList = new JSONArray(ConfItem.getJSONArray("serviceList").toString());
      String model = ConfItem.getString("model_id");

      for (int i = 0; i < svcList.length(); i++) {

        JSONObject iSvc = (JSONObject) svcList.get(i);
        String interaval = iSvc.get("interaval").toString();

        JSONObject jTemplate = new JSONObject(templateItem.getJSONObject(model).toString()); // jTemplate까지는 "type" : "WaterQualityTap" 이 존재
        Thread.sleep(300);

        if (!"daily".equals(interaval)) {

          String[] stdtArr = iSvc.getString("stdt").split(",");
          String[] eddtArr = iSvc.getString("eddt").split(",");
          String stdt = "";
          String eddt = "";

          if (stdtArr.length > 1) {
            stdt = DateUtil.addDate(DateUtil.getChronoUnit(stdtArr[1]), Integer.parseInt(stdtArr[2]), stdtArr[0]);
          } else if (stdtArr.length == 1) {
            stdt = new SimpleDateFormat("yyyy-MM").format(new Date());
          }

          if (eddtArr.length > 1) {
            eddt = DateUtil.addDate(DateUtil.getChronoUnit(eddtArr[1]), Integer.parseInt(eddtArr[2]), eddtArr[0]);
          } else if (eddtArr.length == 1) {
            eddt = new SimpleDateFormat("yyyy-MM").format(new Date());
          }

          iSvc.put("stdt", stdt);
          iSvc.put("eddt", eddt);
        }

        JSONObject itemAdapter = (JSONObject) ItemAdapter.get(model);
        String data = insertData(jTemplate, iSvc, itemAdapter);

        log(SocketCode.DATA_CONVERT_SUCCESS, id);

        sendJson.append(data);

      } // for i end

    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
    }

    return sendJson.toString();
  } // end of doit

  private String insertData(JSONObject jTemplate, JSONObject iSvc, JSONObject itemAdapter) throws Exception {

    StringBuffer _strResult = new StringBuffer();
    String svcInteraval = iSvc.getString("interaval");

    JSONObject jsonData = (JSONObject) CommonUtil.getData(iSvc);
    JsonUtil _jUtil = new JsonUtil(jsonData);

    if (!_jUtil.has("response.body.items.item")) {
      throw new CoreException(ErrorCode.NORMAL_ERROR);
    } else {

      String itemObj = _jUtil.get("response.body.items.item");
      log(SocketCode.DATA_RECEIVE, id, _jUtil.toString().getBytes());

      JSONObject rTemplate = setTemplate(svcInteraval, itemAdapter);
      List<String> keyList = new ArrayList<>();
      Iterator<String> keySet = rTemplate.keys();

      while (keySet.hasNext()) {
        keyList.add(keySet.next().toString());
      }

      if (itemObj.startsWith("[")) {

        JSONArray arrItem = new JSONArray(itemObj);

        for (int i = 0; i < arrItem.length(); i++) {
          setData(jTemplate, rTemplate, (JSONObject) arrItem.get(i), iSvc, keyList);
          _strResult.append(jTemplate.toString() + ", ");
        }
      } else if (itemObj.startsWith("{")) {
        setData(jTemplate, rTemplate, new JSONObject(itemObj), iSvc, keyList);
        _strResult.append(jTemplate.toString() + ", ");
      }
    }

    return _strResult.toString();
  } // end of insertData

  private JSONObject setTemplate(String svcInteraval, JSONObject itemInfo) {

    String strTemplate = "";

    if ("daily".equals(svcInteraval)) {
      strTemplate = itemInfo.get("daily").toString();
    } else if ("weekly".equals(svcInteraval)) {
      strTemplate = itemInfo.get("weekly").toString();
    } else if ("monthly".equals(svcInteraval)) {
      strTemplate = itemInfo.get("monthly").toString();
    }

    return new JSONObject(strTemplate);
  } // end of setTemplate

  private void setData(JSONObject jTemplate, JSONObject rTemplate, JSONObject itemObj, JSONObject iSvc, List<String> keyList) {

    JsonUtil _jUtil = new JsonUtil(jTemplate);

    for (String key : keyList) { // 여기서 실질적으로 데이터를 템플릿에 삽입
      if ("불검출".equals(itemObj.get(key))) {
        itemObj.put(key, 0);
      }

      String data = itemObj.get(key).toString();

      try {
        Float floatData = Float.parseFloat(data);
        _jUtil.put(rTemplate.get(key) + ".value", JsonUtil.nvl(floatData, DataType.FLOAT));
      } catch (Exception e) {
        _jUtil.put(rTemplate.get(key) + ".value", JsonUtil.nvl(data, DataType.STRING));
      }

    }
    String createdAt = itemObj.get("mesurede").toString();

    if ("monthly".equals(iSvc.get("interaval"))) {
      createdAt += "0112";
    } else {
      createdAt += "12";
    }

    createdAt = DateUtil.getISOTime(createdAt);

//		jTemplate.put("location", iSvc.get("location"));
    _jUtil.put("location.value.coordinates", iSvc.get("location"));
    _jUtil.put("address.value.addressCountry", iSvc.get("addressCountry"));
    _jUtil.put("address.value.addressRegion", iSvc.get("addressRegion"));
    _jUtil.put("address.value.addressLocality", iSvc.get("addressLocality"));
    _jUtil.put("address.value.addressTown", iSvc.get("addressTown"));
    _jUtil.put("address.value.streetAddress", iSvc.get("streetAddress"));
    _jUtil.put("interaval.value", iSvc.get("interaval"));

    jTemplate.put("createdAt", createdAt);
    jTemplate.put("id", iSvc.get("gs1Code"));

    removeNull(jTemplate);
  } // end of setData

  private void removeNull(JSONObject jTemplate) {

    Iterator<String> keys = jTemplate.keys();
    List<String> keyList = new ArrayList<>();
    JsonUtil _jUtil = new JsonUtil(jTemplate);

    while (keys.hasNext()) {
      keyList.add(keys.next().toString());
    }

    for (String key : keyList) {
      if (_jUtil.get(key).startsWith("{") && _jUtil.has(key + ".value") && "".equals(_jUtil.get(key + ".value"))) {
        _jUtil.remove(key);
      }
    }
  } // end of removeNull
}
// end of class
