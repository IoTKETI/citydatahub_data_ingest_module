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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractPollSource;
import com.cityhub.dto.Cor19FileInfoVO;
import com.cityhub.dto.ExcelVO;
import com.cityhub.dto.VerifyStatusVO;
import com.cityhub.dto.ExcelVO.ColumnRequired;
import com.cityhub.dto.ExcelVO.ColumnType;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.ExcelUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreCardUsageSource extends AbstractPollSource {

  private String modelId;
  public String EXCEL_PATH;
  public String requestDtPath;
  private WatchKey watchKey;
  private StatusProducer producer = null;
  private String modelTemplate = "";
  private JSONObject initJson;
  private String DATA_PROCESS_STATUS_UPDATED = "";
  private String adapterType = "";
  @Override
  public void setup(Context context) {
    modelId = context.getString("MODEL_ID", "");
    EXCEL_PATH = context.getString("EXCEL_PATH", "");
    requestDtPath = context.getString("requestDtPath", "");
    initJson = new JSONObject(getInitFile(context.getString("INIT_FILE", "")));
    modelTemplate = getInitFile(context.getString("MODEL_TEMPLATE", ""));
    DATA_PROCESS_STATUS_UPDATED = context.getString("PROCESS_STATUS", "DATA_PROCESS_STATUS_UPDATED");
    adapterType = context.getString("type", "");
  }

  @Override
  public void exit() {
    try {
      producer.close();
      log.info("exit:::");
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }
  @Override
  public void processing() {
  }

  @Override
  public void execFirst() {
    producer  = new StatusProducer(initJson.toString(), true);

    try {
      WatchService watchService = FileSystems.getDefault().newWatchService();
      Path path = Paths.get(EXCEL_PATH + modelId);
      path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE );
      Thread thread = new Thread(()-> {
        while (true) {
          try {
            watchKey = watchService.take();//이벤트가 오길 대기(Blocking)
          } catch (InterruptedException e) {
            log.error("Exception : "+ExceptionUtils.getStackTrace(e));
          }
          List<WatchEvent<?>> events = watchKey.pollEvents();
          for (WatchEvent<?> event : events) {
            Kind<?> kind = event.kind();
            Path paths = (Path) event.context();
            try {
              Thread.sleep(300);
            } catch (InterruptedException e) {
              log.error("Exception : "+ExceptionUtils.getStackTrace(e));
            }
            String fi = paths.getFileName().toString().toLowerCase();
            if (fi.contains("xls") || fi.contains("xlsx") || fi.contains("csv")) {
              ParsingThread pt = new ParsingThread(EXCEL_PATH + modelId , paths.getFileName().toString());
              pt.modelTemplate = modelTemplate;
              pt.DATA_PROCESS_STATUS_UPDATED = DATA_PROCESS_STATUS_UPDATED;
              pt.modelId = modelId;
              pt.initJson = initJson;
              pt.requestDtPath = requestDtPath;
              pt.producer = producer;
              pt.sourceName = this.getName();
              pt.adapterType = adapterType;
              pt.channelProcessor = getChannelProcessor();
              new Thread(pt, paths.getFileName().toString()).start();
            }

          }
          if (!watchKey.reset()) {
            try {
              watchService.close();
            } catch (IOException e) {
              log.error("Exception : "+ExceptionUtils.getStackTrace(e));
            }
          }
        }
      });
      thread.start();

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }

  class ParsingThread extends CommonThread implements Runnable {

    public String DATA_PROCESS_STATUS_UPDATED = "";
    public String modelTemplate;
    public JSONObject initJson;
    private Cor19FileInfoVO fvo;
    public String modelId;

    ParsingThread(String path,  String filename) {
      fvo = new Cor19FileInfoVO(path, filename);
    }

    @Override
    public void run() {
      parsingExcel(fvo);
    }

    public void parsingExcel(Cor19FileInfoVO fvo) {
      try {
        long parseSt = System.currentTimeMillis();
        log.info("Parsing start : {}, {}", fvo.getRequestId(),  parseSt);

        String modelType = "urn:datahub:" + modelId;

        List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();

        List<ExcelVO> excelTemplate = new LinkedList<>();
        excelTemplate.add(new ExcelVO("정보제공요청ID", "REQUEST_ID", ColumnRequired.NOT_NULL));
        excelTemplate.add(new ExcelVO("사업장ID", "STORE_ID", ColumnRequired.NOT_NULL));
        excelTemplate.add(new ExcelVO("사업장명", "STORE_NAME", ColumnRequired.NULL));
        excelTemplate.add(new ExcelVO("추가정보", "STORE_BIGO", ColumnRequired.NULL));
        excelTemplate.add(new ExcelVO("카드사코드", "CARD_COMPANY_CODE", ColumnRequired.NOT_NULL, ColumnType.INTEGER));
        excelTemplate.add(new ExcelVO("카드사용일시", "TRANSACTION_TIME", ColumnRequired.NOT_NULL));
        excelTemplate.add(new ExcelVO("카드번호", "CARD_NUMBER", ColumnRequired.NOT_NULL));
        excelTemplate.add(new ExcelVO("거래금액", "CHARGE", ColumnRequired.NULL ));
        excelTemplate.add(new ExcelVO("카드사용자", "CARD_USER_NAME", ColumnRequired.NULL ));
        excelTemplate.add(new ExcelVO("주민번호", "CARD_USER_RESIDENT_REGISTRATION_NUMBER", ColumnRequired.NULL));
        excelTemplate.add(new ExcelVO("핸드폰번호", "CARD_USER_PHONE_NUMBER", ColumnRequired.NULL));
        excelTemplate.add(new ExcelVO("주소", "CARD_USER_ADDRESS", ColumnRequired.NULL));

        List<RequestDtVerify> reqDtList = new ArrayList<>();
        boolean isExistFile =  parsingReqDtVerify(fvo, reqDtList);
        RequestDtVerify rVo = null;

        List<Map<String, Object>> errorList = ExcelUtil.parseFile(rowList, excelTemplate,  1, fvo.getFilePath(), fvo.getFileName());
        log.info("Raw Data Size : {}, {}, {}",fvo.getRequestId(),  rowList.size(), fvo.getFileName());
        rowList = rowList.stream().distinct().collect(Collectors.toList());
        log.info("Remove Duplicate Data Size : {}, {}, {}",fvo.getRequestId(),  rowList.size(), fvo.getFileName());

        String currModelIdGroupId = DateUtil.getTime("yyyyMMddHHmmssSSS");
        String oldModelId = "";
        String T = modelTemplate;
        List<VerifyStatusVO> dbInsertList = new ArrayList<>();

        String storeId = "";
        for (int k = 0; k < rowList.size(); k++) {
          Map<String, Object> row = rowList.get(k);
          List<String> rmKeys = new ArrayList<String>();
          JSONObject jTemplate = new JSONObject(T);
          JsonUtil jsonEx = new JsonUtil(jTemplate);

          storeId = StrUtil.trim(row.get("STORE_ID").toString());
          String cardNumber = row.get("CARD_NUMBER").toString().replaceAll("[^0-9]", "");
          int cardCompanyCode = (int)row.get("CARD_COMPANY_CODE");
          String currModelId = modelType + ":" + StrUtil.trim(row.get("REQUEST_ID").toString()) + "-" + storeId + "-" + cardCompanyCode;
          String uniqueId = currModelId + "_" + currModelIdGroupId + "_" + UUID.randomUUID().toString().replaceAll("-","");

          String st = row.get("TRANSACTION_TIME").toString();
          LocalDateTime _date = null;
          try {
            _date =  com.cityhub.utils.DateUtil.filteredDate(st);
          } catch (Exception e) {
            log.error("Date Parsing Error: {}, {}, {}, {}", fvo.getRequestId(), storeId, cardNumber, st);
            continue;
          }
          long millis = com.cityhub.utils.DateUtil.getEpochMillis(_date);
          String sdt = com.cityhub.utils.DateUtil.convertOffsetDateTime(_date);

          jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://data-hub.kr/cor19.jsonld"));
          jsonEx.put("id", currModelId +  "-" + String.valueOf(millis));
          jsonEx.put("cardNumber.value", cardNumber);
          jsonEx.put("transactionTime.value", sdt);
          jsonEx.put("storeId.value", storeId);
          jsonEx.put("cardCompanyCode.value", cardCompanyCode);

          if ("".equals(StrUtil.trim(row.get("STORE_NAME").toString()))) {
            rmKeys.add("storeName");
          } else {
            jsonEx.put("storeName.value", StrUtil.trim(row.get("STORE_NAME").toString()));
          }
          if ("".equals(StrUtil.trim(row.get("CARD_USER_NAME").toString()))) {
            rmKeys.add("cardUserName");
          } else {
            jsonEx.put("cardUserName.value", StrUtil.trim(row.get("CARD_USER_NAME").toString()));
          }
          if ("".equals(StrUtil.trim(row.get("CARD_USER_PHONE_NUMBER").toString()))) {
            rmKeys.add("cardUserPhoneNumber");
          } else {
            jsonEx.put("cardUserPhoneNumber.value", StrUtil.trim(row.get("CARD_USER_PHONE_NUMBER").toString()));
          }
          if ("".equals(StrUtil.trim(row.get("CARD_USER_ADDRESS").toString()))) {
            rmKeys.add("cardUserAddress");
          } else {
            jsonEx.put("cardUserAddress.value", StrUtil.trim(row.get("CARD_USER_ADDRESS").toString()));
          }
          if ("".equals(StrUtil.trim(row.get("CARD_USER_RESIDENT_REGISTRATION_NUMBER").toString()))) {
            rmKeys.add("cardUserResidentRegistrationNumber");
          } else {
            jsonEx.put("cardUserResidentRegistrationNumber.value", StrUtil.trim(row.get("CARD_USER_RESIDENT_REGISTRATION_NUMBER").toString()));
          }
          String charge = StrUtil.trim(row.get("CHARGE").toString()).replaceAll("[^0-9.]", "");
          if ("".equals(charge)) {
            rmKeys.add("charge");
          } else {
            jsonEx.put("charge.value", JsonUtil.nvl(charge, DataType.DOUBLE));
          }
          rmKeys.add("storeAddress");
          rmKeys.add("storePhoneNumber");
          rmKeys.add("storeRegistrationNumber");
          JsonUtil.removes(jTemplate, rmKeys);

          if (!oldModelId.equals(currModelId)) {
            rVo = getRequestDtVerify(reqDtList, storeId);
            if (isExistFile == true && rVo != null && rVo.isWithinRange(st) == false) {
              log.error("Date Range Error: {}, {}, {}, {}", fvo.getRequestId(), storeId,cardNumber, sdt);
              continue;
            }
            oldModelId = currModelId;
            sendDataProcessStatusUpdated(DATA_PROCESS_STATUS_UPDATED, "STORING_DATA_IN_PROGRESS", modelType, currModelId, fvo.getRequestId(), storeId, sdt);
          }
          if (isExistFile == true && rVo != null && rVo.isWithinRange(st) == false) {
            log.error("Date Range Error: {}, {}, {}, {}", fvo.getRequestId(), storeId, cardNumber, sdt);
            continue;
          }

          VerifyStatusVO vs = new VerifyStatusVO(currModelId + "_" + currModelIdGroupId, modelType , uniqueId, fvo.getRequestId(), storeId, sdt,jTemplate.toString() );
          dbInsertList.add(vs);
        } // end for

        Thread.sleep(10);
        BatchInVerifyDatabase batchInVerifyDb = new BatchInVerifyDatabase(initJson.toString());
        batchInVerifyDb.insertListInVerifyDb(dbInsertList);
        batchInVerifyDb.close();
        Thread.sleep(10);
        sendListKafka(initJson, dbInsertList);
        Thread.sleep(10);


        long parseEt = System.currentTimeMillis();
        log.info("Parsing end : {}, {}, {}, {}ms", fvo.getRequestId(), storeId,  dbInsertList.size(), parseEt - parseSt);
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    } // end parsingExcel

    public JSONArray geocoding(String vendor, String addr, String phone) {
      JSONObject jrRoad = null;
      log.info("value :{}, {}, {}",vendor, addr, phone);
      if (!"".equals(addr)) {
        String addrurl = "https://dapi.kakao.com/v2/local/search/address.json?query=" + addr;
        HttpResponse resp = OkUrlUtil.get(addrurl, "Authorization", "KakaoAK " + initJson.getString("addrApiKey"));
        JSONObject jAddr = new JSONObject(resp.getPayload());

        if (jAddr.has("documents")  ) {
          if ( !jAddr.getJSONArray("documents").isEmpty() ) {
            JSONObject isRoad = (JSONObject)jAddr.getJSONArray("documents").get(0);
            if (isRoad.get("address") instanceof JSONObject ) {
              jrRoad = isRoad.getJSONObject("address");
            } else {
              if (isRoad.has("road_address")  ) {
                jrRoad = isRoad.getJSONObject("road_address");
              }
            }
          }
        }
      }
      if (!"".equals(phone) && jrRoad == null) {
        phone = phone.replaceAll("[^0-9]", "");

        String phoneurl = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + phone;
        HttpResponse resp = OkUrlUtil.get(phoneurl, "Authorization", "KakaoAK " + initJson.getString("addrApiKey"));
        JSONObject jAddr = new JSONObject(resp.getPayload());
        if (jAddr.has("documents")  ) {
          if ( !jAddr.getJSONArray("documents").isEmpty() ) {
            jrRoad = (JSONObject)jAddr.getJSONArray("documents").get(0);
          }
        }
      }
      JSONArray pos = new JSONArray();
      if (jrRoad != null) {
        pos.put(JsonUtil.nvl(jrRoad.getString("x"), DataType.DOUBLE));
        pos.put(JsonUtil.nvl(jrRoad.getString("y"), DataType.DOUBLE));
      }
      return pos;
    } // end position

  } // end Thread



} // end of class









