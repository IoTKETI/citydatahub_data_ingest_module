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


import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvUCityPlatformEvent_rev extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) { //Source.java에서 설정한 내용들 (.conf & .template)
    log.info("-----------------------------------init()-----------------------------------");
    super.setup(ConfItem, templateItem);
    log.info("TEMPPLATEITEM: {}",templateItem);
  }

  @Override
  public String doit() throws CoreException{
    log.info("-----------------------------------doit()-----------------------------------");
    StringBuffer strBuff = new StringBuffer();

    FileInputStream fis = null;
    HSSFWorkbook workbook = null;

    try {
      fis = new FileInputStream(ConfItem.getString("EXCEL"));
      workbook = new HSSFWorkbook(fis);
      HSSFSheet curSheet;
      HSSFRow curRow;

      JsonUtil jsonEx = new JsonUtil(templateItem);
      log.info("EXCEL_PARSING START: "+System.currentTimeMillis());
      List<Map<String,String>> listOfExcel = new ArrayList<>();
      for(int sheetIdx=0; sheetIdx<workbook.getNumberOfSheets(); sheetIdx++) {
        curSheet = workbook.getSheetAt(sheetIdx); //첫 번째 시트안의 모든 데이터
        for(int rowIdx=1; rowIdx<curSheet.getPhysicalNumberOfRows(); rowIdx++) {
          curRow = curSheet.getRow(rowIdx);
          Map<String,String> row = new HashMap<>();
          for(int cellIdx=0; cellIdx<curRow.getPhysicalNumberOfCells(); cellIdx++) {

            String type = curSheet.getRow(rowIdx).getCell(1).toString();
            String grade = curSheet.getRow(rowIdx).getCell(2).toString();
            String status = curSheet.getRow(rowIdx).getCell(3).toString();
            String streetAddress = curSheet.getRow(rowIdx).getCell(4).toString();
            String content = curSheet.getRow(rowIdx).getCell(5).toString();
            String generatedAt = curSheet.getRow(rowIdx).getCell(6).toString();
            String finishedAt = curSheet.getRow(rowIdx).getCell(7).toString();
            String pointX = curSheet.getRow(rowIdx).getCell(8).toString();
            String pointY = curSheet.getRow(rowIdx).getCell(9).toString();
            row.put("type", type);
            row.put("grade", grade);
            row.put("status", status);
            row.put("streetAddress", streetAddress);
            row.put("content", content);
            row.put("generatedAt", generatedAt);
            row.put("finishedAt", finishedAt);
            row.put("pointX", pointX);
            row.put("pointY", pointY);
          }
          listOfExcel.add(row);
        } // end for rowIdx
      } // end for sheet
      log.info("EXCEL_PARSING END: "+System.currentTimeMillis());

      log.info("Model transfer start: "+System.currentTimeMillis());
      for(Map<String,String> row : listOfExcel) {
        jsonEx.put("address.value.addressCountry", "KR");
        jsonEx.put("address.value.addressRegion", "경기도");
        jsonEx.put("address.value.addressLocality", "시흥시");
        jsonEx.put("address.value.streetAddress", row.get("streetAddress"));
        jsonEx.remove("address.value.addressTown");

        jsonEx.put("location.value.coordinates", new JSONArray().put(Float.parseFloat(row.get("pointX"))).put(Float.parseFloat(row.get("pointY"))));
        jsonEx.put("content.value", JsonUtil.nvl(row.get("content"), DataType.STRING));

        jsonEx.put("generatedAt.value", DateUtil.getISOTime(row.get("generatedAt")));
        jsonEx.put("generatedAt.type", "Property");

        jsonEx.put("finishedAt.value", DateUtil.getISOTime(row.get("finishedAt")));
        jsonEx.put("finishedAt.type", "Property");

        jsonEx.put("eventType.value", JsonUtil.nvl(row.get("type") , DataType.STRING));
        jsonEx.put("id", "urn:datahub:UCityPlatformEvent:4010000."+row.get("type"));

        insertValue(row.get("type"), row.get("grade"), row.get("status"), jsonEx);
        jsonEx.toString();
        strBuff.append(jsonEx+",");
      }
      log.info("Model transfer end: "+System.currentTimeMillis());

    }catch(CoreException e) {
      log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
    }catch(Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
    }

    return strBuff.toString();
  }




public static void insertValue(String type, String grade, String status, JsonUtil jsonEx) {

    switch(type) {
    case "112UC001":
      jsonEx.put("eventName.value", JsonUtil.nvl("112긴급영상", DataType.STRING));
      break;
    case "112UC120":
      jsonEx.put("eventName.value", JsonUtil.nvl("112긴급출동", DataType.STRING));
      break;
    case "119UC001":
      jsonEx.put("eventName.value", JsonUtil.nvl("119긴급출동", DataType.STRING));
      break;
    case "APLEN110":
      jsonEx.put("eventName.value", JsonUtil.nvl("기상특보", DataType.STRING));
      break;
    case "BISTR110":
      jsonEx.put("eventName.value", JsonUtil.nvl("버스정보", DataType.STRING));
      break;
    case "BNRCP140":
      jsonEx.put("eventName.value", JsonUtil.nvl("강도발생", DataType.STRING));
      break;
    case "DUCDP110":
      jsonEx.put("eventName.value", JsonUtil.nvl("재난정보긴급지원", DataType.STRING));
      break;
    case "EBSCP110":
      jsonEx.put("eventName.value", JsonUtil.nvl("비상벨", DataType.STRING));
      break;
    case "FCLFT101":
      jsonEx.put("eventName.value", JsonUtil.nvl("시설물", DataType.STRING));
      break;
    case "FMSFT110":
      jsonEx.put("eventName.value", JsonUtil.nvl("차량번호인식", DataType.STRING));
      break;
    case "LPRUM120":
      jsonEx.put("eventName.value", JsonUtil.nvl("차량번호인식", DataType.STRING));
      break;
    case "MTSUC210":
      jsonEx.put("eventName.value", JsonUtil.nvl("범죄", DataType.STRING));
      break;
    case "MTSUC220":
      jsonEx.put("eventName.value", JsonUtil.nvl("방재", DataType.STRING));
      break;
    case "MTSUC230":
      jsonEx.put("eventName.value", JsonUtil.nvl("안전", DataType.STRING));
      break;
    case "MTSUC240":
      jsonEx.put("eventName.value", JsonUtil.nvl("CCTV", DataType.STRING));
      break;
    case "MTSUC250":
      jsonEx.put("eventName.value", JsonUtil.nvl("일반시설물", DataType.STRING));
      break;
    case "POCCP103":
      jsonEx.put("eventName.value", JsonUtil.nvl("안심귀가", DataType.STRING));
      break;
    case "SEBSF110":
      jsonEx.put("eventName.value", JsonUtil.nvl("비상벨", DataType.STRING));
      break;
    case "STLSF110":
      jsonEx.put("eventName.value", JsonUtil.nvl("안심택시", DataType.STRING));
      break;
    case "TUATR100":
      jsonEx.put("eventName.value", JsonUtil.nvl("교통돌발", DataType.STRING));
      break;
    case "WESEN110":
      jsonEx.put("eventName.value", JsonUtil.nvl("기상특보", DataType.STRING));
      break;
    case "WPSSF110":
      jsonEx.put("eventName.value", JsonUtil.nvl("사회적약자", DataType.STRING));
      break;
    }

    switch(grade){ //이벤트 등급
    case "10":
      jsonEx.put("grade.value", JsonUtil.nvl("긴급", DataType.STRING));
      break;
    case "20":
      jsonEx.put("grade.value", JsonUtil.nvl("일반", DataType.STRING));
      break;
    }

    switch(status){ //이벤트 진행상태
    case "10":
      jsonEx.put("status.value", JsonUtil.nvl("발생", DataType.STRING));
      break;
    case "40":
      jsonEx.put("status.value", JsonUtil.nvl("정보변경", DataType.STRING));
      break;
    case "91":
      jsonEx.put("status.value", JsonUtil.nvl("종료", DataType.STRING));
      break;
    case "92":
      jsonEx.put("status.value", JsonUtil.nvl("자동종료", DataType.STRING));
      break;
    }

  }


} // end of class
