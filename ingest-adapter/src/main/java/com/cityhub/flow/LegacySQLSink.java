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
package com.cityhub.flow;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cityhub.core.AbstractBaseSink;
import com.cityhub.dto.RequestMessageVO;
import com.cityhub.environment.Constants;
import com.cityhub.environment.ReflectExecuterEx;
import com.cityhub.model.DataModel;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.SqlUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegacySQLSink extends AbstractBaseSink {

  private static final Logger logger = LoggerFactory.getLogger(LegacySQLSink.class);

  private SqlSession session;
  private Properties config;

  /** 카프카메시지 버전 파라미터 길이 */
  private int MESSAGE_VERSION_LENGTH = 1;
  /** 카프카메시지 길이 파라미터 길이 */
  private int MESSAGE_LENGTH_LENGTH = 4;
  /** 카프카메시지 바디 시작위치 */
  private int MESSAGE_BODY_START_INDEX = MESSAGE_VERSION_LENGTH + MESSAGE_LENGTH_LENGTH;

  @Override
  public void setup(Context context) {
    String url = context.getString("URL_ADDR");
    String driver = context.getString("DRIVER");
    String username = context.getString("USERNAME");
    String password = context.getString("PASSWORD");

    Preconditions.checkState(password != null, "No password specified");
    Preconditions.checkState(username != null, "No username specified");
    Preconditions.checkState(url != null, "No url specified");
    Preconditions.checkState(driver != null, "No driver specified");

    Properties config = new Properties();
    config.setProperty("url", url);
    config.setProperty("driver", driver);
    config.setProperty("username", username);
    config.setProperty("password", password);
    try {
      SqlUtil sql = new SqlUtil(config);
      session = sql.getSqlSession(true);
    } catch (Exception e) {
      session.close();
    }
  }

  @Override
  public void execFirst() {

  }

  @Override
  public void exit() {
    if (session != null) {
      session.close();
    }
  }

  @Override
  public void processing(Event event) {
    String entityType = "";
    try {
      byte[] body = event.getBody();

      byte[] bodyBytes = new byte[body.length - MESSAGE_BODY_START_INDEX];
      System.arraycopy(body, MESSAGE_BODY_START_INDEX, bodyBytes, 0, body.length - MESSAGE_BODY_START_INDEX);
      log.debug("receiveData:: {}", new String(bodyBytes));

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.setSerializationInclusion(Include.NON_NULL);
      objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
      objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));
      RequestMessageVO requestMessageVO = objectMapper.readValue(bodyBytes, RequestMessageVO.class);

      String id = DataModel.extractId(requestMessageVO.getTo());
      entityType = DataModel.extractEntityType(requestMessageVO);
      if (id == null) {
        log.error("{},{},{}", SocketCode.NORMAL_ERROR, "Invalid RequestMessage", requestMessageVO.getTo());
      }
      if (entityType == null) {
        log.error("{},{},{}", SocketCode.NORMAL_ERROR, "Invalid Entity Type", requestMessageVO.getTo());
      }
      String mapperPath = new CommonUtil().getJarPath() + "/mapper/";
      File f = new File(mapperPath + "Mapper-" + entityType + ".xml");

      if (f.exists()) {
        requestMessageVO.setId(id);
        requestMessageVO.setEntityType(entityType);

        Class<?> clz = Class.forName("com.cityhub.flow.conv." + entityType);
        ReflectExecuterEx<RequestMessageVO, JSONObject> reflectExecuter = (ReflectExecuterEx) clz.getDeclaredConstructor().newInstance();
        reflectExecuter.setInitial(requestMessageVO);
        Map<String, Object> param = reflectExecuter.doit();

        session.update(entityType + ".upsertFull", param);
        session.insert(entityType + ".insertHist", param);

      } else {
        log.error("Not Exist Mapper. {}, {}", entityType, ErrorCode.NOT_EXIST_ENTITY);
      }

    } catch (ClassNotFoundException cne) {
      session.rollback();
      log.error("Invalid Entity Type {}, {}", entityType, ErrorCode.INVALID_ENTITY_TYPE);
    } catch (Exception e) {
      session.rollback();
      logger.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
      session.commit();
    }
  }

}
