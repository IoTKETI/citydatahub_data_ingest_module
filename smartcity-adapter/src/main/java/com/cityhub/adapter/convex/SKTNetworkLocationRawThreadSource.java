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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import com.cityhub.dto.ExcelVO;
import com.cityhub.dto.ExcelVO.ColumnRequired;
import com.cityhub.dto.ExcelVO.ColumnType;
import com.cityhub.dto.VerifyStatusVO;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.ExcelUtil;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SKTNetworkLocationRawThreadSource extends AbstractPollSource {

  private String modelId;
  public String RESPONSE_PATH;
  public String REQUEST_PATH;
  private WatchKey watchKey;
  private StatusProducer producer = null;
  private String zone = "";
  private String MOBILE_INTERWORKING_RESPONSE = "";
  private String DATA_PROCESS_STATUS_UPDATED = "";
  private String vendorType = "";
  private String modelTemplate = "";
  private JSONObject initJson;
  private String adapterType = "";

  @Override
  public void setup(Context context) {
    modelId = context.getString("MODEL_ID", "");
    RESPONSE_PATH = context.getString("RESPONSE_PATH", "");
    REQUEST_PATH = context.getString("REQUEST_PATH", "");
    RESPONSE_PATH = RESPONSE_PATH.lastIndexOf("/") !=  (RESPONSE_PATH.length() - 1) ? RESPONSE_PATH+= "/" : RESPONSE_PATH;
    REQUEST_PATH = REQUEST_PATH.lastIndexOf("/") !=  (REQUEST_PATH.length() - 1) ? REQUEST_PATH+= "/" : REQUEST_PATH;

    MOBILE_INTERWORKING_RESPONSE = context.getString("MOBILE_INTERWORKING", "MOBILE_INTERWORKING_RESPONSE");
    DATA_PROCESS_STATUS_UPDATED = context.getString("PROCESS_STATUS", "DATA_PROCESS_STATUS_UPDATED");
    vendorType = context.getString("VENDOR_TYPE", "COD_NET_SKT");
    zone = context.getString("ZONE", "");
    initJson = new JSONObject(getInitFile(context.getString("INIT_FILE", "")));
    modelTemplate = getInitFile(context.getString("MODEL_TEMPLATE", ""));
    adapterType = context.getString("type", "");
  }

  @Override
  public void execFirst() {
    try {
      producer  = new StatusProducer(initJson.toString(), true);
      log.info("RESPONSE_PATH:{}",RESPONSE_PATH);
      WatchService watchService = FileSystems.getDefault().newWatchService();
      Path path = Paths.get(RESPONSE_PATH );
      path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE );
      Thread thread = new Thread(()-> {
        while (true) {
          try {
            watchKey = watchService.take();
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
            if (fi.contains("txt")) {
              String tn = paths.getFileName().toString();
              ParsingThread pt = new ParsingThread(tn);
              pt.modelTemplate = modelTemplate;
              pt.responsePath = RESPONSE_PATH;
              pt.requestPath = REQUEST_PATH;
              pt.MOBILE_INTERWORKING_RESPONSE = MOBILE_INTERWORKING_RESPONSE;
              pt.DATA_PROCESS_STATUS_UPDATED = DATA_PROCESS_STATUS_UPDATED;
              pt.vendorType = vendorType;
              pt.modelId = modelId;
              pt.initJson = initJson;
              pt.zone = zone;

              pt.sourceName = this.getName();
              pt.adapterType = adapterType;
              pt.producer = producer;
              pt.channelProcessor = getChannelProcessor();
              new Thread(pt, tn.substring(0, tn.lastIndexOf("."))).start();
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
  @Override
  public void exit() {
    try {
      producer.close();
    } catch (Exception e) {
      extracted(e);
    }
  }
  @Override
  public void processing() {

  }

  class ParsingThread extends CommonThread implements Runnable {
    private String filename;
    public String responsePath;
    public String requestPath;
    public String zone = "";
    public String MOBILE_INTERWORKING_RESPONSE = "";
    public String DATA_PROCESS_STATUS_UPDATED = "";
    public String vendorType = "";
    public String modelTemplate;
    public String modelId;
    public JSONObject initJson;

    ParsingThread( String filename) {
      this.filename = filename;
    }

    @Override
    public void run() {
      parsingResponseText( filename);
    }

    public void parsingResponseText(String filename) {
      try {
        SktFileInfoVO fvo = null;
        Map<String,String> resMap = readResFile(responsePath , filename);
        Map<String,String> reqMap = readReqFile(requestPath  , filename.replaceAll("_LOC_RES" ,"_LOC_REQ"));
        if ("41".equals(resMap.get("resCode"))) {
          // 정상
          if (!"".equals(resMap.get("resFileName"))) {
            fvo = new SktFileInfoVO(responsePath  , resMap.get("resFileName"));
            File f = new File(fvo.getFilePath() + fvo.getFileName());
            int waitCount = 0;
            while(!f.exists()) {
              Thread.sleep(10000);
              waitCount++;
              if (waitCount == 360) break; // 10second refresh 60minute exit
            }
            if (f.exists()) {
              parsingExcel(fvo, resMap, reqMap );
            }
          } else {
            log.info("resFileName is Empty: {}, {}", reqMap.get("reqId"), reqMap.get("caseId") );
          }

        } else {
          if ("42".equals(resMap.get("resCode")) ) {
            if ("".equals(resMap.get("resReason")) && "".equals(resMap.get("resMemo")) ) {
              Interworking interworking = setInterworking(reqMap.get("reqId"), reqMap.get("caseId"), vendorType, reqMap.get("from"), reqMap.get("to"), 0, ResponseType.SUBSCRIPTION_NOT_EXISTS.getTitle());
              sendInterworking(MOBILE_INTERWORKING_RESPONSE, ResponseType.SUBSCRIPTION_NOT_EXISTS , interworking);
            } else if (!"".equals(resMap.get("resReason")) && "".equals(resMap.get("resMemo")) ) {
              Interworking interworking = setInterworking(reqMap.get("reqId"), reqMap.get("caseId"), vendorType, reqMap.get("from"), reqMap.get("to"), 0, ResponseType.SUBSCRIPTION_NOT_EXISTS.getTitle());
              sendInterworking(MOBILE_INTERWORKING_RESPONSE, ResponseType.SUBSCRIPTION_NOT_EXISTS, interworking);
            } else if ("".equals(resMap.get("resReason")) && !"".equals(resMap.get("resMemo")) ) {
              Interworking interworking = setInterworking(reqMap.get("reqId"), reqMap.get("caseId"), vendorType, reqMap.get("from"), reqMap.get("to"), 0, ResponseType.SUBSCRIPTION_NOT_MATCHED.getTitle());
              sendInterworking(MOBILE_INTERWORKING_RESPONSE, ResponseType.SUBSCRIPTION_NOT_MATCHED, interworking);
            } else if (!"".equals(resMap.get("resReason")) && !"".equals(resMap.get("resMemo")) ) {
              Interworking interworking = setInterworking(reqMap.get("reqId"), reqMap.get("caseId"), vendorType, reqMap.get("from"), reqMap.get("to"), 0, ResponseType.SUBSCRIPTION_NOT_MATCHED.getTitle());
              sendInterworking(MOBILE_INTERWORKING_RESPONSE, ResponseType.SUBSCRIPTION_NOT_MATCHED, interworking);
            }
          }
        }
        FileNio2Copy(resMap, fvo);
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    }

    public void FileNio2Copy(Map<String,String> resMap, SktFileInfoVO fvo)  {
      try {
        String day =  DateUtil.getTime("yyyyMM") + "/";
        String backupDir = initJson.getString("backupDir");
        backupDir = backupDir.lastIndexOf("/") !=  (backupDir.length() - 1) ? backupDir+= "/" : backupDir;
        File dir = new File (backupDir + day);
        if (!dir.exists()) {
          dir.mkdirs();
        }
        Thread.sleep(100);

        Path textSrcPath = new File(responsePath + resMap.get("resTextFileName")).toPath();
        Path textDestPath = new File(backupDir  + day + resMap.get("resTextFileName")).toPath();
        Files.move(textSrcPath, textDestPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("MOVE-TEXT:{}->{}",resMap.get("resTextFileName"),backupDir  + day + resMap.get("resTextFileName"));

        if (fvo != null ) {
          File f = new File(fvo.getFilePath() + fvo.getFileName());
          if (f.exists()) {
            Path xlsxSrcPath = new File(fvo.getFilePath() + fvo.getFileName()).toPath();
            Path xlsxDestPath = new File(backupDir  + day + fvo.getFileName()).toPath();

            Files.move(xlsxSrcPath, xlsxDestPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("MOVE-XLSX:{}->{}",fvo.getFileName(),backupDir  + day + fvo.getFileName());
          }
        }
      } catch (IOException e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    }
    public void parsingExcel(SktFileInfoVO fvo, Map<String,String> resMap, Map<String,String> reqMap) {
      try {
        long parseSt = System.currentTimeMillis();
        log.info("Parsing start : {}, {}, {}", reqMap.get("reqId"), reqMap.get("caseId"), parseSt);

        String modelType = "urn:datahub:" + modelId;

        List<ExcelVO> excelTemplate = new LinkedList<>();
        excelTemplate.add(new ExcelVO("퍼슨아이디", "ID", ColumnRequired.NOT_NULL));
        excelTemplate.add(new ExcelVO("시작시간", "ST", ColumnRequired.NOT_NULL));
        excelTemplate.add(new ExcelVO("종료시간", "ET", ColumnRequired.NULL));
        excelTemplate.add(new ExcelVO("위도", "y", ColumnRequired.NOT_NULL, ColumnType.DOUBLE ));
        excelTemplate.add(new ExcelVO("경도", "x", ColumnRequired.NOT_NULL, ColumnType.DOUBLE ));

        List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> errorList = ExcelUtil.parseFile(rowList, excelTemplate,  1, fvo.getFilePath(), fvo.getFileName());

        log.info("Raw Data Size : {}, {}, {}, {}",reqMap.get("reqId"), reqMap.get("caseId"), rowList.size(), fvo.getFileName());
        rowList = rowList.stream().distinct().collect(Collectors.toList());
        log.info("Remove Duplicate Data Size : {}, {}, {}, {}",reqMap.get("reqId"), reqMap.get("caseId"), rowList.size(), fvo.getFileName());

        List<VerifyStatusVO> dbInsertList = new ArrayList<>();

        String oldModelId = "";
        String currModelIdGroupId = DateUtil.getTime("yyyyMMddHHmmssSSS");
        String T = modelTemplate;
        // 통합 통신 일 때
        for (int k = 0; k < rowList.size(); k++) {
          Map<String, Object> m = rowList.get(k);
          JSONObject jTemplate = new JSONObject(T);
          JsonUtil jsonEx = new JsonUtil(jTemplate);

          JSONArray pos = new JSONArray();
          if ("KOREA".equals(zone)) {
            String x = StrUtil.trim(m.get("x"));
            String y = StrUtil.trim(m.get("y"));
            if (x.indexOf(".") == 2 && y.indexOf(".") == 3) {
              pos.put(JsonUtil.nvl(y, DataType.DOUBLE));
              pos.put(JsonUtil.nvl(x, DataType.DOUBLE));
            } else {
              pos.put(JsonUtil.nvl(x, DataType.DOUBLE));
              pos.put(JsonUtil.nvl(y, DataType.DOUBLE));
            }
          } else {
            pos.put(JsonUtil.nvl(m.get("x"), DataType.DOUBLE));
            pos.put(JsonUtil.nvl(m.get("y"), DataType.DOUBLE));
          }

          String st = StrUtil.nvl(m.get("ST").toString());
          LocalDateTime _sdate = null;
          try {
            _sdate = DateUtil.filteredDate(st);
          } catch (Exception e) {
            log.error("Date Parsing Error: {}, {}, {}", reqMap.get("reqId"), reqMap.get("caseId"), st);
            continue;
          }
          long sDateEpochMillis = DateUtil.getEpochMillis(_sdate);
          String sdt = DateUtil.convertOffsetDateTime(_sdate);

          String edt = DateUtil.getTime2Replace(StrUtil.nvl(m.get("ET").toString()), sdt);

          String personId = StrUtil.trim(m.get("ID").toString());
          String currModelId = modelType + ":" + personId;
          String uniqueId = currModelId + "_" + currModelIdGroupId + "_" + UUID.randomUUID().toString().replaceAll("-","");

          jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://data-hub.kr/cor19.jsonld"));
          jsonEx.put("id", currModelId + "-" + String.valueOf(sDateEpochMillis));
          jsonEx.put("location.value.coordinates", pos);
          jsonEx.put("beginTime.value", sdt);
          jsonEx.put("endTime.value", edt);
          jsonEx.put("personId.value", personId);

          if (!oldModelId.equals(currModelId)) {
            oldModelId = currModelId;
            sendDataProcessStatusUpdated(DATA_PROCESS_STATUS_UPDATED, "STORING_DATA_IN_PROGRESS", modelType, currModelId, reqMap.get("reqId"), reqMap.get("caseId"), sdt);
          }
          VerifyStatusVO vs = new VerifyStatusVO(currModelId + "_" + currModelIdGroupId ,modelType, uniqueId, reqMap.get("reqId"), personId, sdt, jTemplate.toString() );
          if (m.get("x") != null && !"".equals(m.get("x").toString()) && !"0".equals(m.get("x").toString()) && !"0.0".equals(m.get("x").toString()) ) {
            dbInsertList.add(vs);
          }

        } //  END FOR

        Thread.sleep(10);
        BatchInVerifyDatabase batchInVerifyDb = new BatchInVerifyDatabase(initJson.toString());
        batchInVerifyDb.insertListInVerifyDb(dbInsertList);
        batchInVerifyDb.close();
        Thread.sleep(10);
        sendListKafka(initJson, dbInsertList);
        Thread.sleep(10);

        Interworking interworking = setInterworking(reqMap.get("reqId"), reqMap.get("caseId"), vendorType, reqMap.get("from"), reqMap.get("to"), dbInsertList.size(), "");
        sendInterworking(MOBILE_INTERWORKING_RESPONSE, ResponseType.OK, interworking);

        long parseEt = System.currentTimeMillis();
        log.info("Parsing end : {}, {}, {}, {}ms", reqMap.get("reqId"), reqMap.get("caseId"), dbInsertList.size(), parseEt - parseSt);

      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }


    }
  } // end class



} // end of class



