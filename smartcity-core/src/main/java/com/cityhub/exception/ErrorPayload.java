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
package com.cityhub.exception;

import com.cityhub.dto.CommonEntityVO;

public class ErrorPayload extends CommonEntityVO {

  private String type;
  private String title;
  private String detail;
  private String debugMessage;

  public ErrorPayload(String type, String title, String detail) {
    this.type = type;
    this.title = title;
    this.detail = detail;
  }

  public ErrorPayload(String type, String title, String detail, String debugMessage) {
    this.type = type;
    this.title = title;
    this.detail = detail;
    this.debugMessage = debugMessage;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public String getDebugMessage() {
    return debugMessage;
  }

  public void setDebugMessage(String debugMessage) {
    this.debugMessage = debugMessage;
  }
}
