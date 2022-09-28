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
package com.cityhub.core;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.json.JSONObject;

import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractBaseSource extends AbstractSource implements Configurable {

  protected CounterGroup counterGroup;
  private JSONObject init = null;
  private String schemaSrv;

  @Override
  public void configure(Context context) {
    setInit(new JsonUtil().getFileJsonObject("init.conf"));
    try {
      if (getInit().has("SCHEMA_SERVER")) {
        if (!"".equals(getInit().getString("SCHEMA_SERVER"))) {
          setSchemaSrv(getInit().getString("SCHEMA_SERVER"));
        }
      }
    } catch (Exception e) {

    }

    if (counterGroup == null) {
      counterGroup = new CounterGroup();
    }
  }

  @Override
  public void start() {
    super.start();
    log.debug("source {} starting", this.getName());
    log.debug("source {} started. Metrics:{}", this.getName(), counterGroup);
  }

  @Override
  public void stop() {
    super.stop();
    log.debug("source {} stopping", this.getName());

    log.debug("source {} stopped. Metrics:{}", this.getName(), counterGroup);
  }

  public String getUuid() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }

  public void sendEvent(byte[] bodyBytes) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(bodyBytes.length + 5);
    byte version = 0x10;// 4bit: Major version, 4bit: minor version
    Integer bodyLength = bodyBytes.length;// length = 1234
    byteBuffer.put(version);
    byteBuffer.putInt(bodyLength.byteValue());
    byteBuffer.put(bodyBytes);
    Event event = EventBuilder.withBody(byteBuffer.array());
    getChannelProcessor().processEvent(event);
  }

  public String getSchemaSrv() {
    return schemaSrv;
  }

  public void setSchemaSrv(String schemaSrv) {
    this.schemaSrv = schemaSrv;
  }

  public JSONObject getInit() {
    return init;
  }

  public void setInit(JSONObject init) {
    this.init = init;
  }

} // end of class
