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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.json.JSONObject;

import com.cityhub.dto.KafkaStatusVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatusProducer {
  private String topic = "DATA_PROCESS_STATUS_UPDATED";
  private KafkaProducer<String, byte[]> producer = null;
  private JSONObject init = null;

  public StatusProducer(String str, Boolean isAsync) {
    init = new JSONObject(str);
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, init.getString("kafkaServers"));
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "PINICNI_DATA_PROCESS_STATUS_UPDATED"); // 구별하고자 하는 식별자 기입
    props.put(ProducerConfig.ACKS_CONFIG, "0");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    if ("Y".equals(init.getString("kafkaSecYn"))) {
      String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
      String jaasCfg = String.format(jaasTemplate, init.getString("username"), init.getString("password")); // 사용자 계정정보
      props.put("security.protocol", "SASL_PLAINTEXT"); // 보안 설정
      props.put("sasl.mechanism", "PLAIN"); // 보안 설정
      props.put("sasl.jaas.config", jaasCfg); // 보안 설정
    }
    this.producer = new KafkaProducer<>(props);
    this.topic = init.getString("kafkaTopic");
  }

  public void flush() {
    this.producer.flush();
  }

  public void close() {
    this.producer.flush();
    this.producer.close();
  }

  public void send(byte[] messageBytes) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(messageBytes.length + 5);
    byte version = 0x10;// 4bit: Major version, 4bit: minor version
    Integer bodyLength = messageBytes.length;// length = 1234
    byteBuffer.put(version);
    byteBuffer.putInt(bodyLength.byteValue());
    byteBuffer.put(messageBytes);
    producer.send(new ProducerRecord<>(init.getString("kafkaTopic"), byteBuffer.array()));
    try {
      this.producer.flush();
      Thread.sleep(10);
    } catch (InterruptedException e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }

  public void send(String topic, byte[] messageBytes) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(messageBytes.length + 5);
    byte version = 0x10;// 4bit: Major version, 4bit: minor version
    Integer bodyLength = messageBytes.length;// length = 1234
    byteBuffer.put(version);
    byteBuffer.putInt(bodyLength.byteValue());
    byteBuffer.put(messageBytes);
    producer.send(new ProducerRecord<>(topic, byteBuffer.array()));
    try {
      this.producer.flush();
      Thread.sleep(10);
    } catch (InterruptedException e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }

  public byte[] statusJson(KafkaStatusVO vo) {
    return vo.toJson().getBytes(Charset.forName("UTF-8"));
  }

} // end of class
