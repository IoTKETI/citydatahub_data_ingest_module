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
package com.cityhub.utils;

public enum WeatherType {
  clear("맑음", 0,  "clear"), rain("비", 1,  "rainy"), rainsnow("진눈깨비", 2,  "rainsnow"), snow("눈", 3,  "snowy"), rainshower("소나기", 4,  "shower"), Raindrop("빗방울", 5,  "Raindrop"),
  Rainsnowfly("빗방울눈날림", 6,  "Rainsnowfly"),Snowfly("눈날림", 7,  "Snowfly"),
  cleary("맑음", 11,  "clear"), cloudy("구름", 13,  "cloudy"), foggy("안개", 14,  "foggy"), rainy("비", 14,  "rainy"), snowy("눈", 15,  "snowy") , windy("바람", 16,  "windy");

  private int code;
  private String hanNm;
  private String engNm;

  WeatherType(String hanNm, int code, String engNm) {
    this.code = code;
    this.hanNm = hanNm;
    this.engNm = engNm;
  }

  public int getCode() {
    return code;
  }

  public String getHanNm() {
    return hanNm;
  }

  public String getEngNm() {
    return engNm;
  }

  public static WeatherType findBy(int code) {
    for (WeatherType wt : WeatherType.values()) {
      if (wt.code == code) {
        return wt;
      }
    }
    return null;
  }
  public static WeatherType findBy(String hanNm) {
    for (WeatherType wt : WeatherType.values()) {
      if (wt.hanNm.equals(hanNm)) {
        return wt;
      }
    }
    return null;
  }

}
