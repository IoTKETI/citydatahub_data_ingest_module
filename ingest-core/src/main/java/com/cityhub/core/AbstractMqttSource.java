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

import java.util.Arrays;

import org.apache.commons.lang3.exception.ExceptionUtils;
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

import com.cityhub.environment.DefaultConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMqttSource extends AbstractBaseSource implements EventDrivenSource, MqttCallbackExtended {

  private MqttClient mqttClient;
  private MqttConnectOptions mqttOptions;
  private String urlAddr;
  private String topic;
  protected String reqTopic;
  protected String respTopic;
  private String invokeClass;

  @Override
  public void configure(Context context) {
    super.configure(context);

    setUrlAddr(context.getString(DefaultConstants.URL_ADDR, ""));
    topic = context.getString(DefaultConstants.TOPIC, "");
    reqTopic = "/oneM2M/req/+/" + topic + "/#";
    respTopic = "/oneM2M/resp/" + context.getString("MQTT_PREFIX", "") + "/" + topic + "/json";
    invokeClass = context.getString(DefaultConstants.INVOKE_CLASS, "");

    setup(context);
  }

  @Override
  public void start() {
    try {
      connect();

      execFirst();

    } catch (Exception e) {
      log.error("Error connecting to the MQTT broker.", e);
    }
    super.start();
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
    exit();
    super.stop();
  }

  public void connect() {

    try {
      mqttOptions = new MqttConnectOptions();
      mqttOptions.setCleanSession(true);
      mqttOptions.setKeepAliveInterval(30);
      // mqttOptions.setAutomaticReconnect(true);

      String tmpDir = System.getProperty("java.io.tmpdir");
      MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
      mqttClient = new MqttClient(getUrlAddr(), DefaultConstants.VENDOR + "-" + MqttClient.generateClientId(), dataStore);
      mqttClient.setCallback(this);

      IMqttToken iMqttToken = mqttClient.connectWithResult(mqttOptions);
      iMqttToken.waitForCompletion();
      log.info("connected: {}", iMqttToken.isComplete());

    } catch (MqttException e) {
      log.error("Error connecting to the MQTT broker.", e);
    } catch (Exception e) {
      log.error("Please define a hostname to be used as clientId.", e);
    }
  }

  @Override
  public void connectComplete(boolean reconnect, String serverURI) {
    try {
      int Qos = 0;
      log.info("Subscribing to topic: {}, QoS: {}", topic, Qos);
      mqttClient.subscribe(reqTopic, Qos);
    } catch (MqttException e) {
      log.error("Error connecting to the MQTT broker.", e);
    }

  }

  @Override
  public void connectionLost(Throwable throwable) {

    try {
      log.info("Connection to {} lost! : {}", getUrlAddr(), throwable.getMessage());

      if (!mqttClient.isConnected()) {
        IMqttToken iMqttToken = mqttClient.connectWithResult(mqttOptions);
        iMqttToken.waitForCompletion();
        log.info("connected: {}", iMqttToken.isComplete());
      }
    } catch (Exception e) {
      log.error("Reconnection failed.", e);
      connectionLost(e);
    }
  }

  @Override
  public void messageArrived(String topic, MqttMessage mqttMessage) {
    long eventCounter = counterGroup.get("events.success");
    counterGroup.addAndGet("events.success", eventCounter + 1);
    processing(topic, mqttMessage);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    try {
      log.debug("Topic - {} : Delivery complete.", Arrays.toString(token.getTopics()));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  public void publish(byte[] resp) {
    try {
      mqttClient.publish(getTopic(), new MqttMessage(resp));
      log.debug("publish: {} \t {}", respTopic, new String(resp));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  public abstract void setup(Context context);

  public abstract void execFirst();

  public abstract void processing(String s, MqttMessage mqttMessage);

  public void exit() {
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getInvokeClass() {
    return invokeClass;
  }

  public void setInvokeClass(String invokeClass) {
    this.invokeClass = invokeClass;
  }

  public String getUrlAddr() {
    return urlAddr;
  }

  public void setUrlAddr(String urlAddr) {
    this.urlAddr = urlAddr;
  }

}
