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
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkLocationRawSource extends AbstractPollSource {

  private String modelId;
  public String EXCEL_PATH;
  public String requestDtPath;
  private WatchKey watchKey;
  private StatusProducer producer = null;
  private String zone = "";
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
    zone = getInit().getString("ZONE");
    adapterType = context.getString("type", "");
  }

  @Override
  public void exit() {
    try {
      producer.close();
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
            if (fi.contains("xls") || fi.contains("xlsx") || fi.contains("csv")) {
              ParsingThread pt = new ParsingThread(EXCEL_PATH + modelId , paths.getFileName().toString());
              pt.modelTemplate = modelTemplate;
              pt.DATA_PROCESS_STATUS_UPDATED = DATA_PROCESS_STATUS_UPDATED;
              pt.modelId = modelId;
              pt.initJson = initJson;
              pt.zone = zone;

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
    public String modelId;
    public String zone;
    public JSONObject initJson;
    private Cor19FileInfoVO fvo;

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
        String T = modelTemplate;

        List<RequestDtVerify> reqDtList = new ArrayList<>();
        boolean isExistFile =  parsingReqDtVerify(fvo, reqDtList);
        RequestDtVerify rVo = null;

        List<ExcelVO> excelTemplate = new LinkedList<>();
        excelTemplate.add(new ExcelVO("퍼슨아이디", "ID", ColumnRequired.NOT_NULL));
        excelTemplate.add(new ExcelVO("시작시간", "ST", ColumnRequired.NOT_NULL));
        excelTemplate.add(new ExcelVO("종료시간", "ET", ColumnRequired.NULL));
        excelTemplate.add(new ExcelVO("위도", "y", ColumnRequired.NOT_NULL, ColumnType.DOUBLE ));
        excelTemplate.add(new ExcelVO("경도", "x", ColumnRequired.NOT_NULL, ColumnType.DOUBLE ));

        String oldModelId = "";
        String currModelIdGroupId = DateUtil.getTime("yyyyMMddHHmmssSSS");
        List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();

        List<Map<String, Object>> errorList = ExcelUtil.parseFile(rowList, excelTemplate,  1, fvo.getFilePath(), fvo.getFileName());
        log.info("Raw Data Size : {}, {}, {}",fvo.getRequestId(),  rowList.size(), fvo.getFileName());
        rowList = rowList.stream().distinct().collect(Collectors.toList());
        log.info("Remove Duplicate Data Size : {}, {}, {}",fvo.getRequestId(), rowList.size(), fvo.getFileName());

        List<VerifyStatusVO> dbInsertList = new ArrayList<>();

        // 통합 통신 일 때
        for (int k = 0; k < rowList.size(); k++) {
          Map<String, Object> m = rowList.get(k);
          JSONObject jTemplate = new JSONObject(T);
          JsonUtil jsonEx = new JsonUtil(jTemplate);

          String personId = StrUtil.trim(m.get("ID").toString());
          String currModelId = modelType + ":" + personId;
          String uniqueId = currModelId + "_" + currModelIdGroupId + "_" + UUID.randomUUID().toString().replaceAll("-","");

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

          String st = StrUtil.trim(m.get("ST").toString());
          LocalDateTime _sdate = null;
          try {
            _sdate = DateUtil.filteredDate(st);
          } catch (Exception e) {
            log.error("Date Parsing Error: {}, {}, {}", fvo.getRequestId(), personId, st);
            continue;
          }
          long millis = DateUtil.getEpochMillis(_sdate);
          String sdt = DateUtil.convertOffsetDateTime(_sdate);

          String edt = "";
          String et = StrUtil.trim(m.get("ET").toString());
          if (!"".equals(et)) {
            LocalDateTime _edate = null;
            try {
              _edate = DateUtil.filteredDate(et);
              edt = DateUtil.convertOffsetDateTime(_edate);
            } catch (Exception e) {
              edt = sdt;
            }
          } else {
            edt = sdt;
          }

          jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://data-hub.kr/cor19.jsonld"));
          jsonEx.put("id", currModelId + "-" + String.valueOf(millis));
          jsonEx.put("location.value.coordinates", pos);
          jsonEx.put("beginTime.value", sdt);
          jsonEx.put("endTime.value", edt);
          jsonEx.put("personId.value", personId);

          if (!oldModelId.equals(currModelId)) {
            rVo = getRequestDtVerify(reqDtList, personId);
            if (isExistFile == true && rVo != null && rVo.isWithinRange(_sdate) == false) {
              log.error("Date Range Error: {}, {}, {}", fvo.getRequestId(), personId, sdt);
              continue;
            }
            oldModelId = currModelId;
            sendDataProcessStatusUpdated(DATA_PROCESS_STATUS_UPDATED, "STORING_DATA_IN_PROGRESS", modelType, currModelId, fvo.getRequestId(), personId, sdt);
          }
          if (isExistFile == true && rVo != null && rVo.isWithinRange(_sdate) == false) {
            log.error("Date Range Error: {}, {}, {}", fvo.getRequestId(), personId, sdt);
            continue;
          }

          VerifyStatusVO vs = new VerifyStatusVO(currModelId + "_" + currModelIdGroupId,modelType, uniqueId, fvo.getRequestId(), personId, sdt, jTemplate.toString() );
          if ((double)m.get("x") != 0) {
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

        long parseEt = System.currentTimeMillis();
        log.info("Parsing end : {}, {}, {}ms", fvo.getRequestId(),  dbInsertList.size(), parseEt - parseSt);

      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    } // end parsingExcel

  } // end Thread




} //end class

