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
package com.cityhub.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdapterConfig {

  private Map<String, String> prop = new HashMap<String, String>();

  public void put(String key, String value) {
    prop.put(key, value);
  }

  public Map<String, String> getProp() {
    return prop;
  }

  public static Properties getProp(String dbconn, String adapterId) {
    return new Properties();
  }



  public static String getRawDataModel(Properties prop) {
    return new String();
  }

  /**
   * 데이터모델 서식 가져오기
   */
  public static String getRawDataModel(String categoryId, String modelId) {
    return new String();
  }

  /**
   * 데이터모델 서식 가져오기
   */
  public static String getModel(DataModelType dmt, Properties prop) {
    return new String();
  }

  /**
   * 데이터모델 서식 가져오기
   */
  public static String getModel(DataModelType dmt, String categoryId, String modelId) {
    return new String();
  }

  /**
   * 원천데이터가 이미지이거나 영상일경우 메타데이터 가져오기
   */
  public static String getMetadata(DataModelType dmt, Properties prop, byte[] rawData) {
    return new String();
  }

  /**
   * 원천데이터에서 데이터모델로 변환
   */
  public static String getConvert(DataModelType convertDataModel, Properties prop, String rawData) {
    return new String();
  }

  /**
   * 원천데이터에서 데이터모델로 변환
   */
  public static String getConvert(DataModelType convertDataModel, Properties prop, byte[] rawData) {
    return new String();
  }

  /**
   * 유효성 체크 및 필수값 체크
   */
  private String validation(DataModelType convertDataModel, Properties prop, String rawData) {
    return new String();
  }

  /**
   * 표준데이터 모델로 변환
   */
  private String convertToStandard(Properties prop, String rawData) {
    return new String();
  }

  /**
   * epcis로 변환
   */
  private String convertToEpcis(Properties prop, String rawData) {
    return new String();
  }

  /**
   * 데이터모델 변환시에 스크립트처리일때 처리(함수처리)
   */
  private String parse(Properties prop) {
    return new String();
  }

  public static enum DataModelType {
    STANDARD, EPCIS, STREAM, RAWDATA
  }

}
