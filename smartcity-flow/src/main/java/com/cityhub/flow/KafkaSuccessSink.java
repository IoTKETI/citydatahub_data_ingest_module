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

import static com.cityhub.environment.KafkaSinkConstants.BATCH_SIZE;
import static com.cityhub.environment.KafkaSinkConstants.BOOTSTRAP_SERVERS_CONFIG;
import static com.cityhub.environment.KafkaSinkConstants.BROKER_LIST_FLUME_KEY;
import static com.cityhub.environment.KafkaSinkConstants.DEFAULT_ACKS;
import static com.cityhub.environment.KafkaSinkConstants.DEFAULT_BATCH_SIZE;
import static com.cityhub.environment.KafkaSinkConstants.DEFAULT_KEY_SERIALIZER;
import static com.cityhub.environment.KafkaSinkConstants.DEFAULT_TOPIC;
import static com.cityhub.environment.KafkaSinkConstants.DEFAULT_VALUE_SERIAIZER;
import static com.cityhub.environment.KafkaSinkConstants.KAFKA_PRODUCER_PREFIX;
import static com.cityhub.environment.KafkaSinkConstants.KEY_HEADER;
import static com.cityhub.environment.KafkaSinkConstants.KEY_SERIALIZER_KEY;
import static com.cityhub.environment.KafkaSinkConstants.MESSAGE_SERIALIZER_KEY;
import static com.cityhub.environment.KafkaSinkConstants.OLD_BATCH_SIZE;
import static com.cityhub.environment.KafkaSinkConstants.REQUIRED_ACKS_FLUME_KEY;
import static com.cityhub.environment.KafkaSinkConstants.TOPIC_CONFIG;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Future;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.BatchSizeSupported;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.ConfigurationException;
import org.apache.flume.conf.LogPrivacyUtil;
import org.apache.flume.formatter.output.BucketPath;
import org.apache.flume.instrumentation.kafka.KafkaSinkCounter;
import org.apache.flume.shared.kafka.KafkaSSLUtil;
import org.apache.flume.sink.AbstractSink;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cityhub.dto.IngestSuccessEventVO;
import com.cityhub.dto.RequestMessageVO;
import com.cityhub.environment.Constants;
import com.cityhub.environment.KafkaSinkConstants;
import com.cityhub.model.DataModel;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.EventType;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public class KafkaSuccessSink extends AbstractSink implements Configurable, BatchSizeSupported {

  private static final Logger logger = LoggerFactory.getLogger(KafkaSuccessSink.class);

  private final Properties kafkaProps = new Properties();
  private KafkaProducer<String, byte[]> producer;

  private String topic;
  private int batchSize;
  private List<Future<RecordMetadata>> kafkaFutures;
  private KafkaSinkCounter counter;
  private boolean useAvroEventFormat;
  private String partitionHeader = null;
  private Integer staticPartitionId = null;
  private boolean allowTopicOverride;
  private String topicHeader = null;

  private Optional<SpecificDatumWriter<AvroFlumeEvent>> writer = Optional.absent();
  private Optional<SpecificDatumReader<AvroFlumeEvent>> reader = Optional.absent();
  private Optional<ByteArrayOutputStream> tempOutStream = Optional.absent();

  // Fine to use null for initial value, Avro will create new ones if this
  // is null
  private BinaryEncoder encoder = null;

  /** 카프카메시지 버전 파라미터 길이 */
  private int MESSAGE_VERSION_LENGTH = 1;
  /** 카프카메시지 길이 파라미터 길이 */
  private int MESSAGE_LENGTH_LENGTH = 4;
  /** 카프카메시지 바디 시작위치 */
  private int MESSAGE_BODY_START_INDEX = MESSAGE_VERSION_LENGTH + MESSAGE_LENGTH_LENGTH;

  // For testing
  public String getTopic() {
    return topic;
  }

  @Override
  public long getBatchSize() {
    return batchSize;
  }

  @Override
  public Status process() throws EventDeliveryException {
    Status result = Status.READY;
    Channel channel = getChannel();
    Transaction transaction = null;
    Event event = null;
    String eventTopic = null;
    String eventKey = null;

    try {
      long processedEvents = 0;

      transaction = channel.getTransaction();
      transaction.begin();

      kafkaFutures.clear();
      long batchStartTime = System.nanoTime();
      for (; processedEvents < batchSize; processedEvents += 1) {
        event = channel.take();

        if (event == null) {
          // no events available in channel
          if (processedEvents == 0) {
            result = Status.BACKOFF;
            counter.incrementBatchEmptyCount();
          } else {
            counter.incrementBatchUnderflowCount();
          }
          break;
        }
        counter.incrementEventDrainAttemptCount();

        byte[] eventBody = event.getBody();
        Map<String, String> headers = event.getHeaders();

        if (allowTopicOverride) {
          eventTopic = headers.get(topicHeader);
          if (eventTopic == null) {
            eventTopic = BucketPath.escapeString(topic, event.getHeaders());
            logger.debug("{} was set to true but header {} was null. Producing to {}" + " topic instead.", new Object[] { KafkaSinkConstants.ALLOW_TOPIC_OVERRIDE_HEADER, topicHeader, eventTopic });
          }
        } else {
          eventTopic = topic;
        }

        eventKey = headers.get(KEY_HEADER);
        if (logger.isTraceEnabled()) {
          if (LogPrivacyUtil.allowLogRawData()) {
            logger.trace("{Event} " + eventTopic + " : " + eventKey + " : " + new String(eventBody, "UTF-8"));
          } else {
            logger.trace("{Event} " + eventTopic + " : " + eventKey);
          }
        }
        logger.debug("event #{}", processedEvents);

        // TODO : 필수값 체크 후 성공시에 데이터 카프카 저장
        byte[] body = event.getBody();
        byte[] bodyBytes = new byte[body.length - MESSAGE_BODY_START_INDEX];
        System.arraycopy(body, MESSAGE_BODY_START_INDEX, bodyBytes, 0, body.length - MESSAGE_BODY_START_INDEX);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
        objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));

        RequestMessageVO requestMessageVO = objectMapper.readValue(bodyBytes, RequestMessageVO.class);
        String id = DataModel.extractId(requestMessageVO.getTo());
        String entityType = DataModel.extractEntityType(requestMessageVO);
        requestMessageVO.setId(id);
        requestMessageVO.setEntityType(entityType);

        // 필수값 체크 부분
        JSONObject vjson = new JSONObject(requestMessageVO.getContent());
        JsonUtil vJu = new JsonUtil(vjson);
        if (!vJu.has("id")) {
          logger.error("{},{}", ErrorCode.INVALID_PARAMETER, "'id' is null or empty");
        }
        if (!vJu.has("type")) {
          logger.error("{},{}", ErrorCode.INVALID_PARAMETER, "'type' is null or empty");
        }
        if (!vJu.has("location") || !vJu.has("location.value") || !vJu.has("location.value.oordinates") || (vJu.getArray("location.value.oordinates")).length() != 2) {
          logger.error("{},{}", ErrorCode.INVALID_PARAMETER, "'location' is null or empty");
        }

        EventType eventType = DataModel.operationToEventType(requestMessageVO.getOperation());
        IngestSuccessEventVO ingestSuccessEventVO = new IngestSuccessEventVO();
        ingestSuccessEventVO.setEventTime(requestMessageVO.getEventTime());
        ingestSuccessEventVO.setEventReference(id);
        ingestSuccessEventVO.setEventType(eventType);
        ingestSuccessEventVO.setRepresentation(requestMessageVO.getContent());

        String ingestSuccessEventMessage = null;
        try {
          ingestSuccessEventMessage = objectMapper.writeValueAsString(ingestSuccessEventVO);
        } catch (JsonProcessingException e) {
          String errorMessage = "IngestSuccessEvent(Full Topic) Json Parsing ERROR. ingestSuccessEventVO=" + ingestSuccessEventVO;
          logger.warn(errorMessage, e);
        }
        byte[] sendMessageBytes = DataModel.createSendMessage(ingestSuccessEventMessage.getBytes());
        // TODO : END

        // create a message and add to buffer
        long startTime = System.currentTimeMillis();

        Integer partitionId = null;
        try {
          ProducerRecord<String, byte[]> record;
          if (staticPartitionId != null) {
            partitionId = staticPartitionId;
          }
          // Allow a specified header to override a static ID
          if (partitionHeader != null) {
            String headerVal = event.getHeaders().get(partitionHeader);
            if (headerVal != null) {
              partitionId = Integer.parseInt(headerVal);
            }
          }
          if (partitionId != null) {
            record = new ProducerRecord<String, byte[]>(eventTopic, partitionId, eventKey, sendMessageBytes);
          } else {
            record = new ProducerRecord<String, byte[]>(eventTopic, eventKey, sendMessageBytes);
          }
          kafkaFutures.add(producer.send(record, new SinkSuccessCallback(startTime)));
        } catch (NumberFormatException ex) {
          throw new EventDeliveryException("Non integer partition id specified", ex);
        } catch (Exception ex) {
          // N.B. The producer.send() method throws all sorts of RuntimeExceptions
          // Catching Exception here to wrap them neatly in an EventDeliveryException
          // which is what our consumers will expect
          throw new EventDeliveryException("Could not send event", ex);
        }
      }

      // Prevent linger.ms from holding the batch
      producer.flush();

      // publish batch and commit.
      if (processedEvents > 0) {
        for (Future<RecordMetadata> future : kafkaFutures) {
          future.get();
        }
        long endTime = System.nanoTime();
        counter.addToKafkaEventSendTimer((endTime - batchStartTime) / (1000 * 1000));
        counter.addToEventDrainSuccessCount(Long.valueOf(kafkaFutures.size()));
      }

      transaction.commit();

    } catch (Exception ex) {
      String errorMsg = "Failed to publish events";
      logger.error("Failed to publish events", ex);
      counter.incrementEventWriteOrChannelFail(ex);
      result = Status.BACKOFF;
      if (transaction != null) {
        try {
          kafkaFutures.clear();
          transaction.rollback();
          counter.incrementRollbackCount();
        } catch (Exception e) {
          logger.error("Transaction rollback failed", e);
          throw Throwables.propagate(e);
        }
      }
      throw new EventDeliveryException(errorMsg, ex);
    } finally {
      if (transaction != null) {
        transaction.close();
      }
    }

    return result;
  }

  @Override
  public synchronized void start() {
    // instantiate the producer
    producer = new KafkaProducer<String, byte[]>(kafkaProps);
    counter.start();
    super.start();
  }

  @Override
  public synchronized void stop() {
    producer.close();
    counter.stop();
    logger.info("Kafka Sink {} stopped. Metrics: {}", getName(), counter);
    super.stop();
  }

  /**
   * We configure the sink and generate properties for the Kafka Producer
   *
   * Kafka producer properties is generated as follows: 1. We generate a
   * properties object with some static defaults that can be overridden by Sink
   * configuration 2. We add the configuration users added for Kafka (parameters
   * starting with .kafka. and must be valid Kafka Producer properties 3. We add
   * the sink's documented parameters which can override other properties
   *
   * @param context
   */
  @Override
  public void configure(Context context) {

    translateOldProps(context);

    String topicStr = context.getString(TOPIC_CONFIG);
    if (topicStr == null || topicStr.isEmpty()) {
      topicStr = DEFAULT_TOPIC;
      logger.warn("Topic was not specified. Using {} as the topic.", topicStr);
    } else {
      logger.info("Using the static topic {}. This may be overridden by event headers", topicStr);
    }

    topic = topicStr;

    batchSize = context.getInteger(BATCH_SIZE, DEFAULT_BATCH_SIZE);

    if (logger.isDebugEnabled()) {
      logger.debug("Using batch size: {}", batchSize);
    }

    useAvroEventFormat = context.getBoolean(KafkaSinkConstants.AVRO_EVENT, KafkaSinkConstants.DEFAULT_AVRO_EVENT);

    partitionHeader = context.getString(KafkaSinkConstants.PARTITION_HEADER_NAME);
    staticPartitionId = context.getInteger(KafkaSinkConstants.STATIC_PARTITION_CONF);

    allowTopicOverride = context.getBoolean(KafkaSinkConstants.ALLOW_TOPIC_OVERRIDE_HEADER, KafkaSinkConstants.DEFAULT_ALLOW_TOPIC_OVERRIDE_HEADER);

    topicHeader = context.getString(KafkaSinkConstants.TOPIC_OVERRIDE_HEADER, KafkaSinkConstants.DEFAULT_TOPIC_OVERRIDE_HEADER);

    if (logger.isDebugEnabled()) {
      logger.debug(KafkaSinkConstants.AVRO_EVENT + " set to: {}", useAvroEventFormat);
    }

    kafkaFutures = new LinkedList<Future<RecordMetadata>>();

    String bootStrapServers = context.getString(BOOTSTRAP_SERVERS_CONFIG);
    if (bootStrapServers == null || bootStrapServers.isEmpty()) {
      throw new ConfigurationException("Bootstrap Servers must be specified");
    }

    setProducerProps(context, bootStrapServers);

    if (logger.isDebugEnabled() && LogPrivacyUtil.allowLogPrintConfig()) {
      logger.debug("Kafka producer properties: {}", kafkaProps);
    }

    if (counter == null) {
      counter = new KafkaSinkCounter(getName());
    }
  }

  private void translateOldProps(Context ctx) {

    if (!(ctx.containsKey(TOPIC_CONFIG))) {
      ctx.put(TOPIC_CONFIG, ctx.getString("topic"));
      logger.warn("{} is deprecated. Please use the parameter {}", "topic", TOPIC_CONFIG);
    }

    // Broker List
    // If there is no value we need to check and set the old param and log a warning
    // message
    if (!(ctx.containsKey(BOOTSTRAP_SERVERS_CONFIG))) {
      String brokerList = ctx.getString(BROKER_LIST_FLUME_KEY);
      if (brokerList == null || brokerList.isEmpty()) {
        throw new ConfigurationException("Bootstrap Servers must be specified");
      } else {
        ctx.put(BOOTSTRAP_SERVERS_CONFIG, brokerList);
        logger.warn("{} is deprecated. Please use the parameter {}", BROKER_LIST_FLUME_KEY, BOOTSTRAP_SERVERS_CONFIG);
      }
    }

    // batch Size
    if (!(ctx.containsKey(BATCH_SIZE))) {
      String oldBatchSize = ctx.getString(OLD_BATCH_SIZE);
      if (oldBatchSize != null && !oldBatchSize.isEmpty()) {
        ctx.put(BATCH_SIZE, oldBatchSize);
        logger.warn("{} is deprecated. Please use the parameter {}", OLD_BATCH_SIZE, BATCH_SIZE);
      }
    }

    // Acks
    if (!(ctx.containsKey(KAFKA_PRODUCER_PREFIX + ProducerConfig.ACKS_CONFIG))) {
      String requiredKey = ctx.getString(KafkaSinkConstants.REQUIRED_ACKS_FLUME_KEY);
      if (!(requiredKey == null) && !(requiredKey.isEmpty())) {
        ctx.put(KAFKA_PRODUCER_PREFIX + ProducerConfig.ACKS_CONFIG, requiredKey);
        logger.warn("{} is deprecated. Please use the parameter {}", REQUIRED_ACKS_FLUME_KEY, KAFKA_PRODUCER_PREFIX + ProducerConfig.ACKS_CONFIG);
      }
    }

    if (ctx.containsKey(KEY_SERIALIZER_KEY)) {
      logger.warn("{} is deprecated. Flume now uses the latest Kafka producer which implements " + "a different interface for serializers. Please use the parameter {}", KEY_SERIALIZER_KEY,
          KAFKA_PRODUCER_PREFIX + ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
    }

    if (ctx.containsKey(MESSAGE_SERIALIZER_KEY)) {
      logger.warn("{} is deprecated. Flume now uses the latest Kafka producer which implements " + "a different interface for serializers. Please use the parameter {}", MESSAGE_SERIALIZER_KEY,
          KAFKA_PRODUCER_PREFIX + ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
    }
  }

  private void setProducerProps(Context context, String bootStrapServers) {
    kafkaProps.clear();
    kafkaProps.put(ProducerConfig.ACKS_CONFIG, DEFAULT_ACKS);
    // Defaults overridden based on config
    kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, DEFAULT_KEY_SERIALIZER);
    kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DEFAULT_VALUE_SERIAIZER);
    kafkaProps.putAll(context.getSubProperties(KAFKA_PRODUCER_PREFIX));
    kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);

    KafkaSSLUtil.addGlobalSSLParameters(kafkaProps);
  }

  protected Properties getKafkaProps() {
    return kafkaProps;
  }

  private byte[] serializeEvent(Event event, boolean useAvroEventFormat) throws IOException {
    byte[] bytes;
    if (useAvroEventFormat) {
      if (!tempOutStream.isPresent()) {
        tempOutStream = Optional.of(new ByteArrayOutputStream());
      }
      if (!writer.isPresent()) {
        writer = Optional.of(new SpecificDatumWriter<AvroFlumeEvent>(AvroFlumeEvent.class));
      }
      tempOutStream.get().reset();
      AvroFlumeEvent e = new AvroFlumeEvent(toCharSeqMap(event.getHeaders()), ByteBuffer.wrap(event.getBody()));
      encoder = EncoderFactory.get().directBinaryEncoder(tempOutStream.get(), encoder);
      writer.get().write(e, encoder);
      encoder.flush();
      bytes = tempOutStream.get().toByteArray();
    } else {
      bytes = event.getBody();
    }
    return bytes;
  }

  private static Map<CharSequence, CharSequence> toCharSeqMap(Map<String, String> stringMap) {
    Map<CharSequence, CharSequence> charSeqMap = new HashMap<CharSequence, CharSequence>();
    for (Map.Entry<String, String> entry : stringMap.entrySet()) {
      charSeqMap.put(entry.getKey(), entry.getValue());
    }
    return charSeqMap;
  }

  class SinkSuccessCallback implements Callback {
    // private static final Logger logger =
    // LoggerFactory.getLogger(SinkSuccessCallback.class);
    private long startTime;

    public SinkSuccessCallback(long startTime) {
      this.startTime = startTime;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
      if (exception != null) {
        logger.debug("Error sending message to Kafka {} ", exception.getMessage());
      }

      if (logger.isDebugEnabled()) {
        long eventElapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null) {
          logger.debug("Acked message partition:{} ofset:{}", metadata.partition(), metadata.offset());
        }
        logger.debug("Elapsed time for send: {}", eventElapsedTime);
      }
    }
  }

}
