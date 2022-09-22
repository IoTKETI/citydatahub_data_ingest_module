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

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.event.EventBuilder;
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
import com.cityhub.dto.LogVO;
import com.cityhub.environment.Constants;
import com.cityhub.environment.DefaultConstants;
import com.cityhub.environment.ReflectExecuter;
import com.cityhub.environment.ReflectExecuterManager;
import com.cityhub.model.DataModelEx;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.LogWriterToDb;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttSource extends AbstractBaseSource implements EventDrivenSource, MqttCallbackExtended {

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
  private ReflectExecuter reflectExecuter = null;

  private String datasetId;
  private String schemaSrv;
  private String[] ArrDatasetId = null;
  private ObjectMapper objectMapper;

  @Override
  public void configure(Context context) {
    super.configure(context);
    urlAddr = context.getString(DefaultConstants.URL_ADDR, "");
    topic = context.getString("TOPIC", "");
    metaInfo = context.getString("META_INFO", "");
    reqTopic = context.getString("REQ_PREFIX", "") + topic ;
    respTopic = context.getString("RESP_PREFIX", "") + topic;
    setInvokeClass(context.getString(DefaultConstants.INVOKE_CLASS, ""));
    modelId = context.getString("MODEL_ID", "");
    ArrModel = StrUtil.strToArray(modelId, ",");

    adapterType = context.getString("type", "");
    schemaSrv = context.getString("DATAMODEL_API_URL", "");
    datasetId = context.getString("DATASET_ID", "");
    ArrDatasetId = StrUtil.strToArray(datasetId, ",");

    String confFile = context.getString("CONF_FILE", "");
    if (!"".equals(confFile)) {
      ConfItem = new JsonUtil().getFileJsonObject(confFile);
    } else {
      ConfItem = new JSONObject();
    }
    if (getInit().has("daemonSrverLogApi") ) {
      ConfItem.put("daemonSrverLogApi", getInit().getString("daemonSrverLogApi"));
    } else {
      ConfItem.put("daemonSrverLogApi", "http://localhost:8888/logToDbApi");
    }
    ConfItem.put("topic", topic);
    ConfItem.put("req_topic", reqTopic + "/#");
    ConfItem.put("resp_topic", respTopic + "/json");
    ConfItem.put("schemaSrv", schemaSrv);
    ConfItem.put("modelId", modelId);
    ConfItem.put("model_id", modelId);
    ConfItem.put("metaInfo", metaInfo);
    ConfItem.put("sourceName", this.getName());
    ConfItem.put("adapterType", adapterType);
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
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE ));


  }

  @Override
  public void start() {
    try {
      String clientid = DefaultConstants.VENDOR + "-" + topic + "-" + MqttClient.generateClientId();

      log.debug("source: {} , UrlAddr: {} , clientid: {}" , this.getName() , urlAddr , clientid);
      mqttClient = new MqttClient(urlAddr, clientid, dataStore);
      mqttClient.setCallback(this);

      IMqttToken iMqttToken = mqttClient.connectWithResult(mqttOptions);
      iMqttToken.waitForCompletion();
      log.info("connected: {}", iMqttToken.isComplete());

    } catch (MqttException e) {
      log.error("Error connecting to the MQTT broker.", e);
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
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
        HttpResponse resp = OkUrlUtil.get(schemaSrv+"?id=" + model, "Accept", "application/json");
        log.info("schema info: {},{},{}",model, resp.getStatusCode(), schemaSrv+"?id=" + model);
        if (resp.getStatusCode() == 200) {
          DataModelEx dm = new DataModelEx(resp.getPayload());
          if (dm.hasModelId(model)) {
            templateItem.put(model, dm.createModel(model));
            log.info("schema server: {},{}",model, templateItem);
          } else {
            templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
          }
        } else {
          templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
        }
      }
    } else {
      log.error("`{}`{}`{}`{}`{}`{}", this.getName(), modelId , getStr(SocketCode.DATA_NOT_EXIST_MODEL), "", 0, adapterType);
    }

    if (log.isDebugEnabled()) {
      //log.debug("ConfItem:{} -- {}", topic, ConfItem);
      log.debug("templateItem:{} -- {}", topic, templateItem);
    }
    try {
      reflectExecuter = ReflectExecuterManager.getInstance(getInvokeClass() ,  ConfItem, templateItem);
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }


  @Override
  public void messageArrived(String topic, MqttMessage mqttMessage) {
    long eventCounter = counterGroup.get("events.success");
    counterGroup.addAndGet("events.success", eventCounter + 1);

    log.debug("Source Topic: {}\tQoS: {}\tMessage: {}", topic, mqttMessage.getQos(), new String(mqttMessage.getPayload()));

    try {
      if (reflectExecuter == null) {
        reflectExecuter = ReflectExecuterManager.getInstance(getInvokeClass() ,  ConfItem, templateItem);
      }
      if (mqttMessage.getPayload() != null && reflectExecuter != null) {
        JsonUtil je = new JsonUtil(new String(mqttMessage.getPayload()));
        if (!"".equals(je.get("pc")) ) {
          callback(mqttMessage.getPayload());

          String sb = reflectExecuter.doit(mqttMessage.getPayload());

          if (sb != null  && !"".equals(sb) ) {
            List<Map<String,Object>> entities = objectMapper.readValue(sb, new TypeReference<List<Map<String,Object>>>(){});
            for (Map<String,Object> itm : entities) {
              int length = objectMapper.writeValueAsString(itm).getBytes().length;
              log.info("`{}`{}`{}`{}`{}`{}",this.getName() ,itm.get("type"), getStr(SocketCode.DATA_SAVE_REQ) , itm.get("id"), length , adapterType);
              StringBuilder l =  new StringBuilder();
              l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
              l.append("`").append(ConfItem.getString("sourceName"));
              l.append("`").append(modelId);
              l.append("`").append(SocketCode.DATA_SAVE_REQ.getCode() + ";" + SocketCode.DATA_SAVE_REQ.getMessage());
              l.append("`").append(itm.get("id")+"");
              l.append("`").append(length);
              l.append("`").append(adapterType);
              l.append("`").append(ConfItem.getString("invokeClass"));
              LogVO logVo = new LogVO();
              logVo.setSourceName(ConfItem.getString("sourceName"));
              logVo.setPayload(l.toString());
              logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
              logVo.setType(modelId);
              logVo.setStep(SocketCode.DATA_SAVE_REQ.getCode());
              logVo.setDesc(SocketCode.DATA_SAVE_REQ.getMessage());
              logVo.setId(itm.get("id")+"");
              logVo.setLength(String.valueOf(length));
              logVo.setAdapterType(ConfItem.getString("invokeClass"));
              LogWriterToDb.logToDaemonApi(ConfItem,logVo);
              for (int i = 0; i < ArrModel.length; i++) {
                if (ArrModel[i].equals(itm.get("type") + "")) {
                  sendEventEx(itm, ArrDatasetId[i]);
                }
              }
              Thread.sleep(10);
            }

          }
        }

      }
    } catch (NullPointerException npe) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(npe));
      log.error("NullPointerException", npe);
    } catch (ChannelException ex) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(ex));
      log.error("Error writting to channel, event dropped", ex);
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }


  public void sendEventEx(Map<String,Object> entity, String datasetId) {
    try {
      List<Map<String,Object>> entities = new LinkedList<>();
      entities.add(entity);
      Map<String,Object> body = new LinkedHashMap<>();
      body.put("entities", entities);
      body.put("datasetId", datasetId);
      Event event = EventBuilder.withBody(objectMapper.writeValueAsString(body).getBytes(Charset.forName("UTF-8")));
      getChannelProcessor().processEvent(event);
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }


  public String getStr(SocketCode sc) {
    return sc.getCode() + ";" + sc.getMessage();
  }
  public String getStr(SocketCode sc,String msg) {
    return sc.getCode() + ";" + sc.getMessage() + "-" + msg;
  }

  public void callback(byte[] msg) {
    try {
      JsonUtil je = new JsonUtil(new String(msg));
      if( je.has("pc.m2m:sgn") == true) {
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
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }




  @Override
  public void stop() {
    if (mqttClient != null) {
      try {
        mqttClient.disconnect();
        mqttClient.close();
      } catch (MqttException e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    }
    super.stop();
  }

  @Override
  public void connectComplete(boolean reconnect, String serverURI) {
    try {
      int Qos = 0;
      log.info("Subscribing to topic: {}, QoS: {}", reqTopic+ "/#", Qos);
      mqttClient.subscribe(reqTopic+ "/#", Qos);
    } catch (MqttException e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
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
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }

  public String getInvokeClass() {
    return invokeClass;
  }

  public void setInvokeClass(String invokeClass) {
    this.invokeClass = invokeClass;
  }


} // end class
