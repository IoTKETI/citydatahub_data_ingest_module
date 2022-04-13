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
import org.springframework.http.HttpStatus;

public enum ResponseCode {

  OK("2000", "OK", "2000", HttpStatus.OK),
  CREATED("2001", "Created", "2001", HttpStatus.CREATED),
  DELETED("2002", "OK", "2002", HttpStatus.OK),
  CHANGE("2004", "OK", "2004", HttpStatus.NO_CONTENT),
  BAD_REQUEST("4000", "Bad Request", "4000", HttpStatus.BAD_REQUEST),
  INVALID_REQUEST("4000", "Invalid request", "4000", HttpStatus.BAD_REQUEST),
  BAD_REQUEST_DATA("4100", "Bad request data", "4100", HttpStatus.BAD_REQUEST),

  UNAUTHORIZED("4001", "Unauthorized", "4001",HttpStatus.UNAUTHORIZED),
  NOT_FOUND("4004", "Not Found", "4004", HttpStatus.NOT_FOUND),
  METHOD_NOT_ALLOWED("4005", "Method Not Allowed", "4005", HttpStatus.METHOD_NOT_ALLOWED),
  NOT_ACCEPTABLE("4006", "Not Acceptable", "4006",HttpStatus.NOT_ACCEPTABLE),
  UNSUPPORTED_MEDIA_TYPE("4015", "Unsupported Media Type", "4015", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

  MANDATORY_PARAMETER_MISSING("4101", "Mandatory Parameter Missing", "4101", HttpStatus.BAD_REQUEST),
  INVAILD_PARAMETER_TYPE("4102", "Invaild Parameter Type", "4102", HttpStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR("5000", "Internal Server Error", "5000", HttpStatus.INTERNAL_SERVER_ERROR),

  CONFLICT("4009", "Already Exists", "4009", HttpStatus.CONFLICT),
  UNPROCESSABLE_ENTITY("4220", "Unprocessable Entity", "4220", HttpStatus.UNPROCESSABLE_ENTITY),
  LENGTH_REQUIRED("4011", "request entity too large", "4011", HttpStatus.LENGTH_REQUIRED),
  ;

  private final String detailResponseCode;
  private final String detailDescription;
  private final String detailType;
  private final HttpStatus httpStatusCode;

  private ResponseCode(String detailResponseCode, String detailDescription, String detailType, HttpStatus httpStatusCode) {
    this.detailResponseCode = detailResponseCode;
    this.detailDescription = detailDescription;
    this.detailType = detailType;
    this.httpStatusCode = httpStatusCode;
  }

  private static final Map<String, ResponseCode> valueMap = new HashMap<>(ResponseCode.values().length);

  static {
    for (ResponseCode it : values()) {
      valueMap.put(it.getDetailCode(), it);
    }
  }

  public String getDetailCode() {
    return this.detailResponseCode;
  }

  public String getReasonPhrase() {
    return detailDescription;
  }

  public String getDetailType() {
    return detailType;
  }

  public HttpStatus getHttpStatusCode() {
    return httpStatusCode;
  }

  public static ResponseCode fromDetailResponseCode(String detailResponseCode) {
    return valueMap.get(detailResponseCode);
  }
}
