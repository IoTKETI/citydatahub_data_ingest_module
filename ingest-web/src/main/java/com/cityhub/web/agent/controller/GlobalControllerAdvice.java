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
package com.cityhub.web.agent.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.cityhub.environment.ResponseCode;
import com.cityhub.exception.BadRequestException;
import com.cityhub.exception.ErrorPayload;
import com.cityhub.exception.InternalServerErrorException;
import com.cityhub.exception.LengthRequiredException;
import com.cityhub.utils.DataCoreCode;
import com.fasterxml.jackson.core.JsonProcessingException;

@ControllerAdvice // (basePackages = {"kr.re.keti.sc.datacore.controller", "error"}) 404 에러 처리를 위해
                  // 주석처리
public class GlobalControllerAdvice {

  private final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

  private String debugMessageKey = "BBBBBB";

  private int causeMessageLevel = 3;

  /**
   * URL not found 에러 공통 처리 (404)
   *
   * @param request
   * @param e
   * @return
   */

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorPayload> notFoundErrorException(HttpServletRequest request, NoHandlerFoundException e) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ErrorPayload errorPayload = new ErrorPayload(ResponseCode.NOT_FOUND.getDetailType(), ResponseCode.NOT_FOUND.getReasonPhrase(), e.getMessage());

    if (request.getHeader(debugMessageKey) != null) {
      errorPayload.setDebugMessage(makeDebugMessage(e));
    }

    return new ResponseEntity<>(errorPayload, headers, ResponseCode.NOT_FOUND.getHttpStatusCode());
  }

  /**
   * 개발자 정의 에러 공통 처리
   *
   * @param request
   * @param e
   * @return
   */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorPayload> badRequestException(HttpServletRequest request, BadRequestException e) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String errorCode = e.getErrorCode();

    if (errorCode.equals(DataCoreCode.ErrorCode.ALREADY_EXISTS.getCode())) {

      // CREATE 시, 중복 id가 있을 경우, 에러에 대한 공통 처리 (409)
      ErrorPayload errorPayload = new ErrorPayload(ResponseCode.CONFLICT.getDetailType(), ResponseCode.CONFLICT.getReasonPhrase(), e.getMessage());

      if (request.getHeader(debugMessageKey) != null) {
        errorPayload.setDebugMessage(makeDebugMessage(e));
      }

      return new ResponseEntity<>(errorPayload, headers, ResponseCode.CONFLICT.getHttpStatusCode());

    } else {

      // 잘 못된 요청에 대한 에러 공통 처리 (400)
      ErrorPayload errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), e.getMessage());

      if (request.getHeader(debugMessageKey) != null) {
        errorPayload.setDebugMessage(makeDebugMessage(e));

      }

      return new ResponseEntity<>(errorPayload, headers, ResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());

    }

  }

  /**
   * 잘 못된 HTTP Method 요청에 대한 에러 공통 처리 (405)
   *
   * @param request
   * @param e
   * @return
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorPayload> httpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ErrorPayload errorPayload = new ErrorPayload(ResponseCode.METHOD_NOT_ALLOWED.getDetailType(), ResponseCode.METHOD_NOT_ALLOWED.getReasonPhrase(), e.getMessage());
    if (request.getHeader(debugMessageKey) != null) {
      errorPayload.setDebugMessage(makeDebugMessage(e));

    }
    return new ResponseEntity<>(errorPayload, headers, ResponseCode.METHOD_NOT_ALLOWED.getHttpStatusCode());
  }

  /**
   * Unsupported Media Type 요청에 대한 공통 에러 처리 (415)
   *
   * @param request
   * @param e
   * @return
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorPayload> unsupportedMediaTypeStatusException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ErrorPayload errorPayload = new ErrorPayload(ResponseCode.UNSUPPORTED_MEDIA_TYPE.getDetailType(), ResponseCode.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), e.getMessage());
    if (request.getHeader(debugMessageKey) != null) {
      errorPayload.setDebugMessage(makeDebugMessage(e));

    }
    return new ResponseEntity<>(errorPayload, headers, ResponseCode.UNSUPPORTED_MEDIA_TYPE.getHttpStatusCode());
  }
  //

  /**
   * POST 요청 시, Content-Length header가 없을 경우 예외 처리
   *
   * @param request
   * @param e
   * @return
   */
  @ExceptionHandler(LengthRequiredException.class)
  public ResponseEntity<ErrorPayload> lengthRequiredException(HttpServletRequest request, LengthRequiredException e) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ErrorPayload errorPayload = new ErrorPayload(ResponseCode.LENGTH_REQUIRED.getDetailType(), ResponseCode.LENGTH_REQUIRED.getReasonPhrase(), e.getMessage());
    if (request.getHeader(debugMessageKey) != null) {
      errorPayload.setDebugMessage(makeDebugMessage(e));

    }
    return new ResponseEntity<>(errorPayload, headers, ResponseCode.LENGTH_REQUIRED.getHttpStatusCode());
  }

  /**
   * POST body가 오류가 있을 경우, 공통 에러 처리 (400)
   *
   * @param request
   * @param e
   * @return
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorPayload> httpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ErrorPayload errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), "No HttpInputMessage available");
    if (request.getHeader(debugMessageKey) != null) {
      errorPayload.setDebugMessage(makeDebugMessage(e));

    }
    return new ResponseEntity<>(errorPayload, headers, ResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());
  }

  /**
   * 서버 내부 오류가 있을 경우, 공통 에러 처리 (500)
   *
   * @param request
   * @param e
   * @return
   */
  @ExceptionHandler(InternalServerErrorException.class)
  public ResponseEntity<ErrorPayload> internalServerErrorException(HttpServletRequest request, InternalServerErrorException e) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ErrorPayload errorPayload = new ErrorPayload(ResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), ResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Internal Server Error");
    if (request.getHeader(debugMessageKey) != null) {
      errorPayload.setDebugMessage(makeDebugMessage(e));

    }
    return new ResponseEntity<>(errorPayload, headers, ResponseCode.INTERNAL_SERVER_ERROR.getHttpStatusCode());
  }

  /**
   * DB 커넥션 에러 시, 공통 처리 (500)
   *
   * @param request
   * @param e
   * @return
   */
  @ExceptionHandler(java.sql.SQLException.class)
  public ResponseEntity<ErrorPayload> sqlException(HttpServletRequest request, java.sql.SQLException e) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    logger.error("Exception : " + ExceptionUtils.getStackTrace(e));

    ErrorPayload errorPayload = new ErrorPayload(ResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), ResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), "DB connection error");
    if (request.getHeader(debugMessageKey) != null) {
      errorPayload.setDebugMessage(makeDebugMessage(e));

    }
    return new ResponseEntity<>(errorPayload, headers, ResponseCode.INTERNAL_SERVER_ERROR.getHttpStatusCode());
  }

  /**
   * JSON 파싱 시, 에러가 날 경우 공통 처리 (400)
   *
   * @param request
   * @param e
   * @return
   */
  @ExceptionHandler(JsonProcessingException.class)
  public ResponseEntity<ErrorPayload> jsonParseException(HttpServletRequest request, JsonProcessingException e) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ErrorPayload errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), "json parsing error");
    if (request.getHeader(debugMessageKey) != null) {
      errorPayload.setDebugMessage(makeDebugMessage(e));

    }
    return new ResponseEntity<>(errorPayload, headers, ResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());
  }

  /**
   * 상세 error message(debugMessage)를 포함하는 ErrorPayload 생성
   *
   * @param e
   * @return
   */
  private String makeDebugMessage(Exception e) {

    StringBuilder errorMsg = new StringBuilder();

    List<Throwable> throwableList = ExceptionUtils.getThrowableList(e);

    // 발생한 throwable 레벨보다 application cause 레벨이 클 경우
    // 발생한 cause 레벨의 길이만큼만 조회함
    int throwableLevel;
    if (causeMessageLevel > throwableList.size()) {
      throwableLevel = throwableList.size();

    } else {
      throwableLevel = causeMessageLevel;
    }

    for (int i = 0; i < throwableLevel; i++) {
      errorMsg.append(throwableList.get(i).getMessage());
    }
    return errorMsg.toString();
  }

}
