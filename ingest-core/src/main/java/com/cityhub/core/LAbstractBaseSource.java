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

import java.util.UUID;

import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class LAbstractBaseSource extends AbstractSource implements Configurable {

  protected CounterGroup counterGroup;

  @Override
  public void configure(Context context) {
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


} // end of class
