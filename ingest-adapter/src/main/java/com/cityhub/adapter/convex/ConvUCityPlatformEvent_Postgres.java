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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.cityhub.exception.CoreException;
import com.cityhub.source.core.AbstractNormalSource;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvUCityPlatformEvent_Postgres extends AbstractNormalSource {

  @Override
  public String doit(BasicDataSource datasource)  {
    List<Map<String, Object>> modelList = new LinkedList<>();


    String sql = ConfItem.getString("query");
    try (PreparedStatement pstmt = datasource.getConnection().prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        ){
      int gs1number = 0;
      String id = ConfItem.getString("id_prefix");
      while (rs.next()) {
        toLogger(SocketCode.DATA_RECEIVE, id, id.getBytes());
        JsonUtil jsonUtil = new JsonUtil(templateItem.getJSONObject(ConfItem.getString("modelId")).toString() );

        Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> wMap;
        ArrayList<Double> coordinates = new ArrayList<>();

        Object[][] EVT_ID = { { "112UC001", "112긴급영상" }, { "112UC120", "112긴급출동" }, { "119UC001", "119긴급출동" }, { "APLEN110", "기상특보" }, { "BISTR110", "버스정보" }, { "BNRCP140", "강도발생" },
            { "DUCDP110", "재난정보긴급지원" }, { "EBSCP110", "비상벨" }, { "FCLFT101", "시설물" }, { "FMSFT110", "차량번호인식" }, { "LPRUM120", "차량번호인식" }, { "MTSUC210", "범죄" }, { "MTSUC220", "방재" },
            { "MTSUC230", "안전" }, { "MTSUC240", "CCTV" }, { "MTSUC250", "일반시설물" }, { "POCCP103", "안심귀가" }, { "SEBSF110", "비상벨" }, { "STLSF110", "안심택시" }, { "TUATR100", "교통돌발" },
            { "WESEN110", "기상특보" }, { "WPSSF110", "버스정보" }, { "BISTR110", "사회적약자" } };
        Object[][] EVT_GRAD_CD = { { "10", "긴급" }, { "20", "일반" } };
        Object[][] EVT_PRGRS_CD = { { "10", "발생" }, { "40", "정보변경" }, { "91", "종료" }, { "92", "자동종료" } };

        coordinates.add(Double.parseDouble(rs.getString("POINT_X")));
        coordinates.add(Double.parseDouble(rs.getString("POINT_Y")));
        String eventName = ExponentialStage(rs.getString("EVT_ID"), EVT_ID);
        String grade = ExponentialStage(rs.getString("EVT_GRAD_CD"), EVT_GRAD_CD);
        String status = ExponentialStage(rs.getString("EVT_PRGRS_CD"), EVT_PRGRS_CD);

        Find_wMap(tMap, "eventType").put("value", rs.getString("EVT_ID"));
        Find_wMap(tMap, "eventName").put("value", eventName);
        Find_wMap(tMap, "grade").put("value", grade);
        Find_wMap(tMap, "status").put("value", status);
        Find_wMap(tMap, "content").put("value", rs.getString("EVT_DTL"));
        Find_wMap(tMap, "generatedAt").put("value", DateUtil.getISOTime(rs.getString("EVT_OCR_YMD_HMS")));
        Find_wMap(tMap, "finishedAt").put("value", DateUtil.getISOTime(rs.getString("EVT_END_YMD_HMS")));

        id = ConfItem.getString("id_prefix") + "_" + (gs1number++) + "_" + rs.getString("EVT_ID");

        wMap = (Map) tMap.get("dataProvider");
        wMap.put("value", ConfItem.getString("dataProvider"));

        wMap = (Map) tMap.get("globalLocationNumber");
        wMap.put("value", id);

        Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
        addrValue.put("addressCountry", ConfItem.getString("addressCountry"));
        addrValue.put("addressRegion", ConfItem.getString("addressRegion"));
        addrValue.put("addressLocality", ConfItem.getString("addressLocality"));
        addrValue.put("addressTown", "");
        addrValue.put("streetAddress", rs.getString("EVT_PLACE"));

        Map<String, Object> locMap = (Map) tMap.get("location");
        locMap.put("observedAt", DateUtil.getTime());
        Map<String, Object> locValueMap = (Map) locMap.get("value");
        locValueMap.put("coordinates", coordinates);

        tMap.put("id", id);

        modelList.add(jsonUtil.toMap());

        count++;


        String str = objectMapper.writeValueAsString(jsonUtil.toMap());
        toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());

        if (count == bufferCount) {
          sendEvent(modelList, ConfItem.getString("datasetId"));
          toLogger(SocketCode.DATA_SAVE_REQ, id, objectMapper.writeValueAsBytes(modelList));
          count = 0;
          modelList = new LinkedList<>();
        }
      }

      if (modelList.size() < bufferCount) {
        sendEvent(modelList, ConfItem.getString("datasetId"));
        toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, objectMapper.writeValueAsBytes(modelList));
      }


    } catch (SQLException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        toLogger(SocketCode.DATA_CONVERT_FAIL, ConfItem.getString("id_prefix"), "".getBytes());
      }
    } catch (Exception e) {
      toLogger(SocketCode.DATA_CONVERT_FAIL, ConfItem.getString("id_prefix"), "".getBytes());
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + ConfItem.getString("id_prefix"), e);
    }

    return "Success";
  } // end of doit

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    ValueMap.put("observedAt", DateUtil.getTime());
    return ValueMap;
  }

  String ExponentialStage(Integer Exponential, Object[][] arrList) {
    Integer Min = 0;
    String resultName = "";
    for (Integer i = 0; i < arrList.length; i++) {
      Integer _arrayNumber = (Integer) arrList[i][0];
      String _arrayName = (String) arrList[i][1];
      if ((Exponential >= _arrayNumber) && (_arrayNumber >= Min)) {
        Min = _arrayNumber;
        resultName = _arrayName;
      }
    }
    return resultName;
  }

  String ExponentialStage(String Exponential, Object[][] arrList) {
    String resultName = "";
    for (Integer i = 0; i < arrList.length; i++) {
      String _arrayString = (String) arrList[i][0];
      String _arrayName = (String) arrList[i][1];
      if (Exponential.equals(_arrayString)) {
        return _arrayName;
      }
    }
    return resultName;
  }

} // end of class
