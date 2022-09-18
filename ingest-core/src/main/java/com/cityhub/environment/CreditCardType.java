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

/**
 * HTTP Response 코드 클래스
 */
public enum CreditCardType {

  BC(361,  "BC카드"),
  KJ(364,  "광주카드"),
  SAMSUNG(365,  "삼성카드"),
  SHINHAN(366,  "신한카드"),
  HYUNDAI(367,  "현대카드"),
  LOTTE(368,  "롯데카드"),
  SUHYUP(369,  "수협카드"),
  CITY(370,  "씨티카드"),
  NH(371,  "NH카드"),
  JB(372,  "전북카드"),
  JEJU(373,  "제주카드"),
  HANA(374,  "하나SK카드"),
  KB(381,  "KB국민카드")
  ;

  private int code;
  private String desc;

  CreditCardType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }


  public int getCode() {
    return code;
  }


  public String getDesc() {
    return desc;
  }

  public static CreditCardType findBy(int code) {
    for (CreditCardType gt : CreditCardType.values()) {
      if (gt.code == code) {
        return gt;
      }
    }
    return null;
  }
  public static CreditCardType findBy(String desc) {
    for (CreditCardType gt : CreditCardType.values()) {
      if (gt.desc == desc) {
        return gt;
      }
    }
    return null;
  }

}
