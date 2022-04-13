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
package com.cityhub.adapter.convex;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public enum ResponseType {

	OK("2000", "OK", "http://eiss.cdc.go.kr/success/OK", HttpStatus.OK),
	ACCEPTED("2002", "Accepted", "http://eiss.cdc.go.kr/success/Accepted", HttpStatus.ACCEPTED),
	NO_CONTENT("2004", "No Content", "http://eiss.cdc.go.kr/success/NoContent", HttpStatus.NO_CONTENT),
	NOT_ACCEPTABLE("4006", "Not Acceptable", "http://eiss.cdc.go.kr/errors/NotAcceptable", HttpStatus.NOT_ACCEPTABLE),
	RESOURCE_NOT_FOUND("4004", "Resource Not Found", "http://eiss.cdc.go.kr/errors/ResourceNotFound", HttpStatus.NOT_FOUND),
	CREATED("2001", "Created", "", HttpStatus.CREATED),
	DELETED("2002", "OK", "", HttpStatus.OK),

	CHANGE("2004", "OK", "", HttpStatus.NO_CONTENT),
	//    BAD_REQUEST("4000", "Bad Request", HttpStatus.BAD_REQUEST),
	INVALID_REQUEST("4000", "Invalid Request", "https://eiss.cdc.go.kr/errors/InvalidRequest", HttpStatus.BAD_REQUEST),
	BAD_REQUEST_DATA("4100", "Bad Request Data", "http://eiss.cdc.go.kr/errors/BadRequestData", HttpStatus.BAD_REQUEST),


	//    UNAUTHORIZED("4001", "Unauthorized", HttpStatus.UNAUTHORIZED),
	NOT_FOUND("4004", "Not Found", "http://eiss.cdc.go.kr/errors/ResourceNotFound", HttpStatus.NOT_FOUND),
	METHOD_NOT_ALLOWED("4005", "Method Not Allowed", " ", HttpStatus.METHOD_NOT_ALLOWED),
	//    NOT_ACCEPTABLE("4006", "Not Acceptable", HttpStatus.NOT_ACCEPTABLE),
	UNSUPPORTED_MEDIA_TYPE("4015", "Unsupported Media Type", "http://eiss.cdc.go.kr/errors/UnsupportedMediaType", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

	//    MANDATORY_PARAMETER_MISSING("4101", "Mandatory Parameter Missing", HttpStatus.BAD_REQUEST),
	//    INVAILD_PARAMETER_TYPE("4102", "Invaild Parameter Type", HttpStatus.BAD_REQUEST),
	INTERNAL_SERVER_ERROR("5000", "Internal Server Error", "http://eiss.cdc.go.kr/errors/InternalError", HttpStatus.INTERNAL_SERVER_ERROR),
	SERVICE_UNAVAILABLE("5001", "Service Unavailable", "http://eiss.cdc.go.kr/errors/ServiceUnavailable", HttpStatus.INTERNAL_SERVER_ERROR),

	CONFLICT("4009", "Already Exists", "http://eiss.cdc.go.kr/errors/AlreadyExists", HttpStatus.CONFLICT),
	UNPROCESSABLE_ENTITY("4220", "Unprocessable Entity", "http://eiss.cdc.go.kr/errors/OperationNotSupported", HttpStatus.UNPROCESSABLE_ENTITY),
	LENGTH_REQUIRED("4011", "request entity too large", " ", HttpStatus.LENGTH_REQUIRED),
	SUBSCRIPTION_NOT_EXISTS("4011", "Subscription Not Exists", "http://eiss.cdc.go.kr/errors/SubscriptionNotExists", HttpStatus.OK),
  SUBSCRIPTION_NOT_MATCHED("4011", "Subscription Not Matched", "http://eiss.cdc.go.kr/errors/SubscriptionNotMatched", HttpStatus.OK),
	;

	private final String type;
	private final String title;
	private String detail;
	private final HttpStatus httpStatusCode;

	private ResponseType(String type, String title, String detail, HttpStatus httpStatusCode) {
		this.type = type;
		this.title = title;
		this.detail = detail;
		this.httpStatusCode = httpStatusCode;
	}

	private static final Map<String, ResponseType> valueMap = new HashMap<>(ResponseType.values().length);

	static {
		for (ResponseType it : values()) {
			valueMap.put(it.getType(), it);
		}
	}

	public static ResponseType parseDetail(String detail) {
    for (ResponseType entityType : values()) {
      if (entityType.getTitle().equals(detail)) {
        return entityType;
      }
    }
    return null;
  }

	public String getType() {
		return this.type;
	}

	public String getTitle() {
		return title;
	}

	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
    this.detail = detail;
  }

	public HttpStatus getHttpStatusCode() {
		return httpStatusCode;
	}
	public static ResponseType parseTitle(String title) {
    for (ResponseType entityType : values()) {
      if (entityType.getTitle().equals(title)) {
        return entityType;
      }
    }
    return null;
  }

	public static ResponseType fromResponseCode(String type) {
		return valueMap.get(type);
	}
	public static String toJSON(ResponseType rt) {
	  JSONObject j = new JSONObject();
    j.put("type", rt.getType());
    j.put("title", rt.getDetail());
    j.put("detail", rt.getTitle());
    return j.toString();
  }

}