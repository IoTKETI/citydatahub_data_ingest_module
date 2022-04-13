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

public class SktFileInfoVO {
  private String modelId;
  private String requestId;
  private String personId;
  private String networkType = "";
  private String uploadTime;
  private String fileName;
  private String fileSeq;
  private String filePath;

  public SktFileInfoVO() {

  }

  public SktFileInfoVO(String filePath, String fileName) {
    this.filePath = filePath.lastIndexOf("/") !=  (filePath.length() - 1) ? filePath+= "/" : filePath;
    this.fileName = fileName;
    this.parseFileName(fileName);
  }

  public void parseFileName(String fileName) {
    this.fileName = fileName;
    String[] fArr = fileName.split("_", -1);
    this.requestId = fArr[2].substring(0,13);
    this.personId = fArr[2].substring(13,26);
  }


  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getPersonId() {
    return personId;
  }

  public void setPersonId(String personId) {
    this.personId = personId;
  }

  public String getNetworkType() {
    return networkType;
  }

  public void setNetworkType(String networkType) {
    this.networkType = networkType;
  }

  public String getUploadTime() {
    return uploadTime;
  }

  public void setUploadTime(String uploadTime) {
    this.uploadTime = uploadTime;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileSeq() {
    return fileSeq;
  }

  public void setFileSeq(String fileSeq) {
    this.fileSeq = fileSeq;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }




} // end class