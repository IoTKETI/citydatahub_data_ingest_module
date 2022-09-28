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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class DataCoreCode {

  public static enum ErrorCode {
    UNKNOWN_ERROR("C001"), NOT_EXIST_ENTITY("C002"), SQL_ERROR("C003"), MEMORY_QUEUE_INPUT_ERROR("C004"), REQUEST_MESSAGE_PARSING_ERROR("C005"), RESPONSE_MESSAGE_PARSING_ERROR("C006"),
    INVALID_ENTITY_TYPE("C007"), INVALID_ACCEPT_TYPE("C008"), INVALID_PARAMETER("C009"), INVALID_AUTHORIZATION("C0010"), LENGTH_REQUIRED("C0011"), ALREADY_EXISTS("C0012"), NOT_EXIST_ID("C013"),
    NORMAL_ERROR("C0099");

    private String code;

    private ErrorCode(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    public static ErrorCode parseCode(String code) {
      for (ErrorCode entityType : values()) {
        if (entityType.getCode().equals(code)) {
          return entityType;
        }
      }
      return null;
    }
  }

  public static enum EventType {
    PARTIALLY_UPDATED, FULLY_UPDATED, CREATED, DELETED;
  }

  public static enum Operation {
    CREATE("CREATE"), PARTIAL_UPDATE("PARTIAL_UPDATE"), FULL_UPDATE("FULL_UPDATE"), PARTIAL_UPSERT("PARTIAL_UPSERT"), FULL_UPSERT("FULL_UPSERT"), DELETE("DELETE"), RETRIEVE("RETRIEVE");

    private String code;

    private Operation(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    public static Operation parseType(String code) {
      for (Operation operation : values()) {
        if (operation.getCode().equals(code)) {
          return operation;
        }
      }
      return null;
    }
  }

  public static enum RetrieveOptions {
    KEY_VALUES("keyValues"), KEY_VALUES_HISTORY("keyValuesHistory"), NORMALIZED_HISTORY("normalizedHistory"), TEMPORAL_VALUES("temporalValues");

    private String code;

    private RetrieveOptions(String code) {
      this.code = code;
    }

    @JsonCreator
    public String getCode() {
      return code;
    }

    @JsonValue
    public static RetrieveOptions parseType(String code) {
      for (RetrieveOptions operation : values()) {
        if (operation.getCode().equals(code)) {
          return operation;
        }
      }
      return null;
    }
  }

  public static enum GeometryType {
    POINT("Point"), POLYGON("Polygon"), NEAR_REL("near"), WITHIN_REL("within"), CONTAINS_REL("contains"), INTERSECTS_REL("intersects"), EQUALS_REL("equals"), DISJOINT_REL("disjoint"),
    OVERLAPS_REL("overlaps"), MIN_DISTANCE("maxDistance"), MAX_DISTANCE("maxDistance");

    private String code;

    private GeometryType(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    public static GeometryType parseType(String code) {
      for (GeometryType geometryType : values()) {
        if (geometryType.getCode().equals(code)) {
          return geometryType;
        }
      }
      return null;
    }
  }

  public static enum QueryOperator {

    SINGLE_EQUAL("=", "%x3D"), EQUAL("==", "%x3D%x3D"), UNEQUAL("!=", "%x21%x3D"), GREATER(">", "%x3E"), GREATEREQ(">=", "%x3E%x3D"), LESS("<", "%x3C"), LESSEQ("<=", "%x3C%x3D"),
    DOTS("..", "%x2E%x2E"), COMMA(",", "%x2C");

    private String sign;
    private String unicode;

    private QueryOperator(String sign, String unicode) {
      this.sign = sign;
      this.unicode = unicode;
    }

    public String getSign() {
      return sign;
    }

    public String getUnicode() {
      return unicode;
    }
  }

  public static enum TemporalOperator {

    BEFORE_REL("before"), AFTER_REL("after"), BETWEEN_REL("between"), TIME("time"), END_TIME("endtime"), TIME_PROPERTY("timeproperty");

    private String code;

    private TemporalOperator(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  public static enum UseYn {

    YES("Y"), NO("N");

    private String code;

    private UseYn(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  public static enum ValueType {
    PROPERTY("Property");

    private String code;

    private ValueType(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  public static enum GeoValueType {
    PROPERTY("GeoProperty");

    private String code;

    private GeoValueType(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  public static enum RequestParameterName {
    REQUEST_ID("requestId"), E2E_REQUEST_ID("e2eRequestId"), OPERATION("operation"), TO("to"), CONTENT_TYPE("contentType"), QUERY("query"), CONTENT("content"), OWNER("owner");

    private String name;

    private RequestParameterName(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static enum RepresentationType {

    FULL("FULL"), SIMPLIFIED("SIMPLIFIED");

    private String code;

    private RepresentationType(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    public static RepresentationType parseType(String code) {
      for (RepresentationType representationType : values()) {
        if (representationType.getCode().equals(code)) {
          return representationType;
        }
      }
      return null;
    }
  }

  public static enum ResultCode {
    SAVE_SUCCESS("1001", "저장에 성공하였습니다."), SAVE_FAILED("9001", "저장에 실패하였습니다."), DEL_SUCCESS("1002", "삭제에 성공하였습니다."), DEL_FAILED("9002", "삭제에 실패하였습니다."), RESET_SUCCESS("1003", "초기화에 성공하였습니다."),
    RESET_FAILED("9003", "초기화에 실패하였습니다.");

    private String code;
    private String message;

    private ResultCode(String code, String message) {
      this.code = code;
      this.message = message;
    }

    public String getCode() {
      return code;
    }

    public void setMessage(String msg) {
      this.message = msg;
    }

    public String getMessage() {
      return message;
    }

    public static ResultCode parseCode(String str) {
      String code = str.split(";")[0];
      String msg = str.split(";")[1];
      for (ResultCode entityType : values()) {
        if (entityType.getCode().equals(code)) {
          entityType.setMessage(msg);
          return entityType;
        }
      }
      return null;
    }
  }

  public static enum ResponseCode {
    OK("2000", "OK"), CREATED("2001", "CREATED"), DELETED("2002", "DELETED"), CHANGED("2004", "CHENAGED"), UNAUTHENTICATED("4001", "UNAUTHENTICATED"), NOT_FOUND("4004", "NOT FOUND"),
    METHOD_NOT_ALLOWED("4005", "METHOD NOT ALLOWED"), NOT_ACCEPTABLE("4006", "NOT ACCEPTABLE"), UNSUPPORTED_MEDIA_TYPE("4015", "UNSUPPORTED MEDIA TYPE"), ALREADY_EXISTS("4100", "ALREADY EXISTS"),
    MANDATORY_PARAMETER_MISSING("4101", "MANDATORY PARAMETER MISSING"), INVALID_PARAMETER_TYPE("4102", "INVALID PARAMETER TYPE"), EXPIRED_TOKEN("4103", "EXPIRED TOKEN"),
    INVALID_TOKEN("4104", "INVALID TOKEN"), UNAUTHORIZED("4105", "UNAUTHORIZED"), UNSUPPORTED_TOKEN_TYPE("4106", "UNSUPPORTED TOKEN TYPE"), INTERNAL_SERVER_ERROR("5000", "INTERNAL SERVER ERROR"),

    INITIAL_CONDITION_SETUP("7001", "INITIAL CONDITION SETUP"), TEST_START("7002", "TEST START"),

    REQ_AGENT_SEL01("1001", "TestAgent01 에이전트 조회 요청"), REQ_AGENT_INS01("1002", "TestAgent01 신규 에이전트 생성 요청"), REQ_AGENT_REINS("1003", "TestAgent01 동일 에이전트 생성 요청"),
    REQ_AGENT_MOD01("1004", "TestAgent01 에이전트 수정 요청"), REQ_AGENT_DEL01("1005", "TestAgent01 에이전트 삭제 요청"), REQ_AGENT_SEL02("1006", "TestAgent02 에이전트 조회 요청"),
    REQ_AGENT_INS02("1007", "TestAgent02 신규 에이전트 생성 요청"), REQ_AGENT_MOD02("1008", "TestAgent02 에이전트 수정 요청"), REQ_AGENT_DEL02("1009", "TestAgent02 에이전트 삭제 요청"),
    REQ_AGENT_ALLSEL("1010", "모든 에이전트 조회 요청"),

    REQ_ADAPTOR_SEL01("1011", "TestAdaptor01 어뎁터 조회 요청"), REQ_ADAPTOR_INS01("1012", "TestAdaptor01 신규 어뎁터 생성 요청"), REQ_ADAPTOR_REINS("1013", "TestAdaptor01 동일 어뎁터 생성 요청"),
    REQ_ADAPTOR_MOD01("1014", "TestAdaptor01 어뎁터 수정 요청"), REQ_ADAPTOR_DEL01("1015", "TestAdaptor01 어뎁터 삭제 요청"), REQ_ADAPTOR_SEL02("1016", "TestAdaptor02 어뎁터 조회 요청"),
    REQ_ADAPTOR_INS02("1017", "TestAdaptor02 신규 어뎁터 생성 요청"), REQ_ADAPTOR_MOD02("1018", "TestAdaptor02 어뎁터 수정 요청"), REQ_ADAPTOR_DEL02("1019", "TestAdaptor02 어뎁터 삭제 요청"),
    REQ_ADAPTOR_ALLSEL("1020", "모든 어뎁터 조회 요청"),

    REQ_ADAPTORTYPE_SEL01("1021", "AdaType01 어뎁터유형 조회 요청"), REQ_ADAPTORTYPE_INS01("1022", "AdaType01 신규 어뎁터유형 생성 요청"), REQ_ADAPTORTYPE_REINS("1023", "AdaType01 동일 어뎁터유형 생성 요청"),
    REQ_ADAPTORTYPE_MOD01("1024", "AdaType01 어뎁터유형 수정 요청"), REQ_ADAPTORTYPE_DEL01("1025", "AdaType01 어뎁터유형 삭제 요청"), REQ_ADAPTORTYPE_SEL02("1026", "AdaType02 어뎁터유형 조회 요청"),
    REQ_ADAPTORTYPE_INS02("1027", "AdaType02 신규 어뎁터유형 생성 요청"), REQ_ADAPTORTYPE_MOD02("1028", "AdaType02 어뎁터유형 수정 요청"), REQ_ADAPTORTYPE_DEL02("1029", "AdaType02 어뎁터유형 삭제 요청"),
    REQ_ADAPTORTYPE_ALLSEL("1030", "모든 어뎁터유형 조회 요청"),

    REQ_OBMODEL_SEL01("1031", "TestObModel01 원천데이터모델 조회 요청"), REQ_OBMODEL_INS01("1032", "TestObModel01 신규 원천데이터모델 생성 요청"), REQ_OBMODEL_REINS("1033", "TestObModel01 동일 원천데이터모델 생성 요청"),
    REQ_OBMODEL_MOD01("1034", "TestObModel01 원천데이터모델 수정 요청"), REQ_OBMODEL_DEL01("1035", "TestObModel01 원천데이터모델 삭제 요청"), REQ_OBMODEL_SEL02("1036", "TestObModel02 원천데이터모델 조회 요청"),
    REQ_OBMODEL_INS02("1037", "TestObModel02 신규 원천데이터모델 생성 요청"), REQ_OBMODEL_MOD02("1038", "TestObModel02 원천데이터모델 수정 요청"), REQ_OBMODEL_DEL02("1039", "TestObModel02 원천데이터모델 삭제 요청"),
    REQ_OBMODEL_ALLSEL("1040", "모든 원천데이터모델 조회 요청");

    private String code;
    private String message;

    private ResponseCode(String code, String message) {
      this.code = code;
      this.message = message;
    }

    public String getCode() {
      return code;
    }

    public void setMessage(String msg) {
      this.message = msg;
    }

    public String getMessage() {
      return message;
    }

    public static ResponseCode parseCode(String str) {
      String code = str.split(";")[0];
      String msg = str.split(";")[1];
      for (ResponseCode entityType : values()) {
        if (entityType.getCode().equals(code)) {
          entityType.setMessage(msg);
          return entityType;
        }
      }
      return null;
    }
  }

  public static enum SocketCode {
    DATA_CREATE_REQ_RECEIVED("1000", "연계 요청 준비 중 입니다."), DATA_REQ("1001", "데이터 요청"), DATA_CREATE("1002", "데이터 생성"), SOCKET_CONNECT_TRY("1003", "연결 시도"), SOCKET_CONNECT("2000", "연결 성공"),
    DATA_RECEIVE("2001", "데이터를 받았습니다."), DATA_CONVERT_REQ("2002", "데이터 변환 요청 중입니다."), DATA_CONVERT_SUCCESS("2003", "데이터를 변환을 성공했습니다."), DATA_SAVE_REQ("2004", "데이터 적재를 요청합니다."),
    DATA_SAVE("2005", "데이터를 적재했습니다."), DATA_CONVERT_FAIL("9001", "데이터를 변환을 실패했습니다."), DATA_NOT_EXIST_MODEL("9002", "데이터 모델이 없습니다."), SOCKET_CONNECT_FAIL("9003", "소켓 연결에 실패하였습니다."),
    NORMAL_ERROR("9800", "일반에러"), SYSTEM_ERROR("9900", "시스템에러"), SOCKET_END("10000", "종료"), DATA_CREATE_REQ_END("10001", "연계 요청을 중단합니다.");

    private String code;
    private String message;

    private SocketCode(String code, String message) {
      this.code = code;
      this.message = message;
    }

    public String getCode() {
      return code;
    }

    public void setMessage(String msg) {
      this.message = msg;
    }

    public String getMessage() {
      return message;
    }

    public static SocketCode parseCode(String str) {
      String code = str.split(";")[0];
      String msg = str.split(";")[1];
      for (SocketCode entityType : values()) {
        if (entityType.getCode().equals(code)) {
          entityType.setMessage(msg);
          return entityType;
        }
      }
      return null;
    }

  }

  public static enum LinkCode {
    VERIFIER_TO_IUT("1000", "VERIFIER -----> IUT"), IUT_TO_VERIFIER("2000", "VERIFIER <----- IUT");

    private String code;
    private String message;

    private LinkCode(String code, String message) {
      this.code = code;
      this.message = message;
    }

    public String getCode() {
      return code;
    }

    public void setMessage(String msg) {
      this.message = msg;
    }

    public String getMessage() {
      return message;
    }

    public static LinkCode parseCode(String str) {
      String code = str.split(";")[0];
      String msg = str.split(";")[1];
      for (LinkCode entityType : values()) {
        if (entityType.getCode().equals(code)) {
          entityType.setMessage(msg);
          return entityType;
        }
      }
      return null;
    }
  }
}
