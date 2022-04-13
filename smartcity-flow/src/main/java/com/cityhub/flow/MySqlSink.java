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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cityhub.core.AbstractBaseSink;
import com.cityhub.utils.SqlUtil;
import com.google.common.base.Preconditions;

public class MySqlSink extends AbstractBaseSink {

  private static final Logger logger = LoggerFactory.getLogger(MySqlSink.class);

  private String postfix = "mapper.mysql.";

  private String password;
  private String username;
  private String driver;
  private String url;
  private String mapper;
  private SqlSession session;
  private Properties config;

  @Override
  public void setup(Context context) {
    driver = context.getString("driver");
    url = context.getString("url");
    username = context.getString("username");
    password = context.getString("password");
    mapper = context.getString("resource", "mapper/mybatis-config.xml");
    Preconditions.checkState(password != null, "No password specified");
    Preconditions.checkState(username != null, "No username specified");
    Preconditions.checkState(driver != null, "No driver specified");
    Preconditions.checkState(url != null, "No url specified");
    Preconditions.checkState(mapper != null, "No mapper specified");

    config = new Properties();
    config.setProperty("driver", driver);
    config.setProperty("url", url);
    config.setProperty("username", username);
    config.setProperty("password", password);
    config.setProperty("mapper", mapper);
  }

  @Override
  public void execFirst() {
    try {
      SqlUtil sql = new SqlUtil(config);
      session = sql.getSqlSession(true);
    } catch (Exception e) {
      session.close();
    }
  }

  @Override
  public void exit() {
    if (session != null) {
      session.close();
    }
  }

  @Override
  public void processing(Event event) {
    try {
      String body = new String(event.getBody(), StandardCharsets.UTF_8);
      if (body.startsWith("[")) {
        JSONArray jarr = new JSONArray(body);
        for (Object obj : jarr) {
          JSONObject json = (JSONObject)obj;
          Map<String, Object> param = json.toMap();
          if (json.has("header")) {
            param.put("tableName", json.getString("header"));
          }
          session.insert(postfix + "insertQuery", param);
        }
      } else if (body.startsWith("{")) {
        JSONObject json = new JSONObject(body);
        Map<String, Object> param = json.toMap();
        if (json.has("header")) {
          param.put("tableName", json.getString("header"));
        }
        session.insert(postfix + "insertQuery", param);
      } else {
        logger.info("Logger: " + body);
      }

    } catch (Exception e) {
      session.rollback();
    } finally {
      session.commit();
    }

  }

}
