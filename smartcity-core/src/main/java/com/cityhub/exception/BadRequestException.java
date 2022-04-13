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

import com.cityhub.utils.DataCoreCode.ErrorCode;

public class BadRequestException extends BaseException {

  private static final long serialVersionUID = 325647261102179280L;

  public BadRequestException(ErrorCode errorCode) {
    super(errorCode);
  }

  public BadRequestException(ErrorCode errorCode, String msg) {
    super(errorCode, msg);
    this.errorCode = errorCode;
  }

  public BadRequestException(ErrorCode errorCode, Throwable throwable) {
    super(errorCode, throwable);
    this.errorCode = errorCode;
  }

  public BadRequestException(ErrorCode errorCode, String msg, Throwable throwable) {
    super(errorCode, msg, throwable);
    this.errorCode = errorCode;
  }
}
