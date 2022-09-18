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
package com.cityhub;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import com.cityhub.daemon.code.Consts;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = Consts.BASE_PACKAGE, exclude = DataSourceAutoConfiguration.class)
@Slf4j
@Configuration
public class App {


  @Value("${server.timezone:Asia/Seoul}")
  private String timeZone;

  @PostConstruct
  public void initApplication() {
    try {
      TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    } catch(Exception e) {
      log.error("initApplication set timezone error.", e);
      TimeZone.setDefault(TimeZone.getTimeZone(Consts.CONTENT_DATE_TIMEZONE));
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }


}
