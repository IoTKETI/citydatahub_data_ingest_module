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

import org.apache.flume.Channel;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractBaseSink extends AbstractSink implements Configurable {

  protected CounterGroup counterGroup;

  @Override
  public void configure(Context context) {
    setup(context);
    if (counterGroup == null) {
      counterGroup = new CounterGroup();
    }
  }

  @Override
  public void start() {
    super.start();
    log.debug("sink {} starting", this.getName());
    counterGroup.setName(this.getName());
    execFirst();
    log.debug("sink {} started. Metrics:{}", this.getName(), counterGroup);
  }

  @Override
  public void stop() {
    super.stop();
    log.debug("sink {} stopping", this.getName());
    exit();
    log.debug("sink {} stopped. Metrics:{}", this.getName(), counterGroup);
  }

  @Override
  public Status process() throws EventDeliveryException {
    Status status = Status.READY;
    Channel channel = getChannel();
    Transaction transaction = channel.getTransaction();
    try {
      transaction.begin();
      Event event = channel.take();
      if (event == null) {
        counterGroup.incrementAndGet("events.empty");
        status = Status.BACKOFF;
      } else {
        counterGroup.incrementAndGet("events.success");
        processing(event);
      }
      transaction.commit();
      counterGroup.incrementAndGet("transaction.success");

    } catch (ChannelException e) {
      transaction.rollback();
      counterGroup.incrementAndGet("transaction.failed");
      log.error("Unable to get event from channel. Exception follows.", e);
      status = Status.BACKOFF;
    } catch (Exception e) {
      transaction.rollback();
      counterGroup.incrementAndGet("transaction.failed");
      log.error("Unable to communicate with server. Exception follows.", e);
      status = Status.BACKOFF;
    } finally {
      transaction.close();
    }
    return status;
  }

  public abstract void setup(Context context);
  public abstract void execFirst();
  public abstract void exit();
  public abstract void processing(Event event);



}
