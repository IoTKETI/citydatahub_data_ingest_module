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
package com.cityhub.adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.EventDrivenSource;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONObject;

import com.cityhub.core.AbstractBaseSource;
import com.cityhub.core.ReflectNormalSystem;
import com.cityhub.core.ReflectNormalSystemManager;
import com.cityhub.environment.Constants;
import com.cityhub.environment.DefaultConstants;
import com.cityhub.model.DataModelEx;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OneM2M extends AbstractBaseSource implements EventDrivenSource, MqttCallbackExtended {

  private MqttClient mqttClient;
  private MqttConnectOptions mqttOptions;
  private MqttDefaultFilePersistence dataStore;
  private String urlAddr;
  private String topic;
  private String metaInfo;
  protected String reqTopic;
  protected String respTopic;
  private String invokeClass;
  private String modelId;

  private String[] ArrModel = null;

  private String adapterType;

  private JSONObject ConfItem;
  private JSONObject templateItem;

  private String datasetId;
  private String DATAMODEL_API_URL;
  private String[] ArrDatasetId = null;
  private ObjectMapper objectMapper;

  @Override
  public void configure(Context context) {
    super.configure(context);

    String confFile = context.getString("CONF_FILE", "");
    if (!"".equals(confFile)) {
      ConfItem = new JsonUtil().getFileJsonObject(confFile);
    } else {
      ConfItem = new JSONObject();
    }
    String DAEMON_SERVER_LOGAPI = context.getString("DAEMON_SERVER_LOGAPI", "http://localhost:8888/logToDbApi");
    ConfItem.put("daemonServerLogApi", DAEMON_SERVER_LOGAPI);

    urlAddr = context.getString(DefaultConstants.URL_ADDR, "");
    topic = context.getString("TOPIC", "");
    metaInfo = context.getString("META_INFO", "");
    reqTopic = context.getString("REQ_PREFIX", "") + topic;
    respTopic = context.getString("RESP_PREFIX", "") + topic;
    invokeClass = context.getString(DefaultConstants.INVOKE_CLASS, "");
    modelId = context.getString("MODEL_ID", "");
    ArrModel = StrUtil.strToArray(modelId, ",");

    adapterType = context.getString("type", "");
    DATAMODEL_API_URL = context.getString("DATAMODEL_API_URL", "");
    datasetId = context.getString("DATASET_ID", "");
    ArrDatasetId = StrUtil.strToArray(datasetId, ",");

    ConfItem.put("topic", topic);
    ConfItem.put("req_topic", reqTopic + "/#");
    ConfItem.put("resp_topic", respTopic + "/json");
    ConfItem.put("DATAMODEL_API_URL", DATAMODEL_API_URL);
    ConfItem.put("modelId", modelId);
    ConfItem.put("metaInfo", metaInfo);
    ConfItem.put("sourceName", this.getName());
    ConfItem.put("adapterType", adapterType);
    ConfItem.put("invokeClass", invokeClass );
    ConfItem.put("datasetId", datasetId);

    mqttOptions = new MqttConnectOptions();
    mqttOptions.setCleanSession(true);
    mqttOptions.setKeepAliveInterval(30);
    mqttOptions.setAutomaticReconnect(true);
    mqttOptions.setMaxReconnectDelay(5000);

    String tmpDir = System.getProperty("java.io.tmpdir");
    dataStore = new MqttDefaultFilePersistence(tmpDir);

    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));

  }

  @Override
  public void start() {
    try {
      String clientid = DefaultConstants.VENDOR + "-" + topic + "-" + MqttClient.generateClientId();

      log.debug("source: {} , UrlAddr: {} , clientid: {}", this.getName(), urlAddr, clientid);
      mqttClient = new MqttClient(urlAddr, clientid, dataStore);
      mqttClient.setCallback(this);

      IMqttToken iMqttToken = mqttClient.connectWithResult(mqttOptions);
      iMqttToken.waitForCompletion();
      log.info("connected: {}", iMqttToken.isComplete());

    } catch (MqttException e) {
      log.error("Error connecting to the MQTT broker.", e);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      log.error("Please define a hostname to be used as clientId.", e);
    }
    execFirst();
    super.start();
  }

  @SuppressWarnings("rawtypes")
  public void execFirst() {
    templateItem = new JSONObject();
    if (ArrModel != null) {
      for (String model : ArrModel) {
        HttpResponse resp = OkUrlUtil.get(DATAMODEL_API_URL + "?id=" + model, "Accept", "application/json");
        log.info("DATAMODEL_API_URL info: {},{},{}", model, resp.getStatusCode(), DATAMODEL_API_URL + "?id=" + model);
        if (resp.getStatusCode() == 200) {
          DataModelEx dm = new DataModelEx(resp.getPayload());
          if (dm.hasModelId(model)) {
            templateItem.put(model, dm.createModel(model));
            log.info("MODEL INFO: {},{}", model, templateItem);
          } else {
            log.info("HAS NOT MODEL : {}", model);
            if (exists(model)) {
              templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
            } else {
              log.info("NOT FOUND TEPLATE FILE: {},{}", model);
            }
          }
        } else {
          if (exists(model)) {
            templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
          } else {
            log.info("NOT FOUND TEPLATE FILE: {},{}", model);
          }
        }
      }
    } else {
      log.error("`{}`{}`{}`{}`{}`{}`{}", this.getName(), modelId, SocketCode.DATA_NOT_EXIST_MODEL.toMessage(), "", 0, adapterType,ConfItem.getString("invokeClass"));
    }

    ConfItem.put("MODEL_TEMPLATE",templateItem);

  }
  private boolean exists(String model) {
    String templatePath = new CommonUtil().getJarPath();
    File file = new File(templatePath + "openapi/" + model + ".template");
    return file.exists();
  }
  private boolean hasModel() {
    boolean hasModel = false;
    for (String model : ArrModel) {
      JSONObject templateItem = ConfItem.getJSONObject("MODEL_TEMPLATE");
      if (templateItem.has(model)) {
        hasModel = true;
      }
    }
    return hasModel;
  }
  @Override
  public void messageArrived(String topic, MqttMessage mqttMessage) {
    long eventCounter = counterGroup.get("events.success");
    counterGroup.addAndGet("events.success", eventCounter + 1);

    log.debug("Source Topic: {}\tQoS: {}\tMessage: {}", topic, mqttMessage.getQos(), new String(mqttMessage.getPayload()));

    try {
      if (hasModel() ) {
        ReflectNormalSystem reflectNormalSystem = ReflectNormalSystemManager.getInstance(invokeClass);
        reflectNormalSystem.init(getChannelProcessor(), ConfItem);

        if (mqttMessage.getPayload() != null && reflectNormalSystem != null) {
          JsonUtil je = new JsonUtil(new String(mqttMessage.getPayload()));
          if (!"".equals(je.get("pc"))) {
            String sb = reflectNormalSystem.doit(mqttMessage.getPayload());
          }
          callback(mqttMessage.getPayload());
        }
      } else {
        if (mqttMessage.getPayload()  != null ) {
          callback(mqttMessage.getPayload());
        }
      }

    } catch (NullPointerException npe) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(npe));
      log.error("NullPointerException", npe);
    } catch (ChannelException ex) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(ex));
      log.error("Error writting to channel, event dropped", ex);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }


  public void callback(byte[] msg) {
    try {
      JsonUtil je = new JsonUtil(new String(msg));
      if (je.has("pc.m2m:sgn") == true) {
        JSONObject sub = je.getObject("pc.m2m:sgn");

        boolean vrq = sub.has("vrq") == false ? false : sub.getBoolean("vrq");
        boolean sud = sub.has("sud") == false ? false : sub.getBoolean("sud");
        if (vrq == true || sud == true) {
          JSONObject resp = new JsonUtil().getFileJsonObject("openapi/CallbackOneM2M.template");
          resp.put("fr", topic);
          resp.put("rqi", je.get("rqi"));
          mqttClient.publish(reqTopic + "/json", new MqttMessage(resp.toString().getBytes()));
          log.debug("publish: {} \t {}", reqTopic, resp.toString());
        }
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  @Override
  public void stop() {
    if (mqttClient != null) {
      try {
        mqttClient.disconnect();
        mqttClient.close();
      } catch (MqttException e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }
    super.stop();
  }

  @Override
  public void connectComplete(boolean reconnect, String serverURI) {
    try {
      int Qos = 0;
      log.info("Subscribing to topic: {}, QoS: {}", reqTopic + "/#", Qos);
      mqttClient.subscribe(reqTopic + "/#", Qos);
    } catch (MqttException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      log.error("Error connecting to the MQTT broker.", e);
    }
  }

  @Override
  public void connectionLost(Throwable throwable) {
    try {
      log.info("Connection to {} lost! : {}", urlAddr, ExceptionUtils.getStackTrace(throwable));
      if (!mqttClient.isConnected()) {
        IMqttToken iMqttToken = mqttClient.connectWithResult(mqttOptions);
        iMqttToken.waitForCompletion();
        log.info("connected: {}", iMqttToken.isComplete());
      }
    } catch (Exception e) {
      log.error("Reconnection failed.", ExceptionUtils.getStackTrace(e));
    }
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    try {
      log.debug("Topic - {} : Delivery complete.", Arrays.toString(token.getTopics()));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }


} // end class
