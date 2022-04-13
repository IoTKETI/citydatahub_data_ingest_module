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

public abstract class BaseException extends RuntimeException {

  private static final long serialVersionUID = 6697553987008675632L;

  ErrorCode errorCode = null;

  public BaseException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public BaseException(ErrorCode errorCode, String msg) {
    super(msg);
    this.errorCode = errorCode;
  }

  public BaseException(ErrorCode errorCode, Throwable throwable) {
    super(throwable);
    this.errorCode = errorCode;
  }

  public BaseException(ErrorCode errorCode, String msg, Throwable throwable) {
    super(msg, throwable);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode.getCode();
  }
}
