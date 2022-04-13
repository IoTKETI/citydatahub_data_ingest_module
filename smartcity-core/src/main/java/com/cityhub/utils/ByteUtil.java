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

import java.nio.ByteBuffer;

public class ByteUtil {

  /**
   * 바이트 to Int
   * @param value
   * @return
   */
  public static int bytesToint(byte[] value) {
    return ByteBuffer.wrap(value).getInt();
  }

  /**
   * int to 바이트
   * @param value
   * @return
   */
  public static byte[] intTobytes(int value) {
    return ByteBuffer.allocate(4).putInt(value).array();
  }
}