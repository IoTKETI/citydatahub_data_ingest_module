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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.source.PollableSourceConstants;

import com.cityhub.environment.DefaultConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class LAbstractPollSource extends LAbstractBaseSource implements PollableSource {

  private Integer connTerm; // second

  @Override
  public void configure(Context context) {
    super.configure(context);
    connTerm = context.getInteger(DefaultConstants.CONN_TERM, 600);
    setup(context);
  }

  @Override
  public void start() {
    super.start();
    execFirst();
  }

  @Override
  public void stop() {
    exit();
    super.stop();
  }

  @Override
  public Status process() throws EventDeliveryException {
    Status status = Status.READY;
    try {
      long eventCounter = counterGroup.get("events.success");
      counterGroup.addAndGet("events.success", eventCounter);
      processing();
      Thread.sleep(connTerm * 1000); // second * 1000
      status = Status.READY;
    } catch (Exception e) {
      counterGroup.incrementAndGet("events.failed");
      status = Status.BACKOFF;
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return status;
  }

  public abstract void setup(Context context);

  public abstract void execFirst();

  public abstract void processing();

  public void exit() {
  }

  @Override
  public long getBackOffSleepIncrement() {
    return PollableSourceConstants.DEFAULT_BACKOFF_SLEEP_INCREMENT;
  }

  @Override
  public long getMaxBackOffSleepInterval() {
    return PollableSourceConstants.DEFAULT_MAX_BACKOFF_SLEEP;
  }

}
