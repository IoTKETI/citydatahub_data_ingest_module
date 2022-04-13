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

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class AES256Util {
  private String iv;
  private Key keySpec;

  /**
   * @param key
   * @throws UnsupportedEncodingException
   */
  final static String key = "infectioncityhubkeypass";

  /**
   * @throws UnsupportedEncodingException
   */
  public AES256Util() throws UnsupportedEncodingException {
    this.iv = key.substring(0, 16);
    byte[] keyBytes = new byte[16];
    byte[] b = key.getBytes("UTF-8");
    int len = b.length;
    if (len > keyBytes.length) {
      len = keyBytes.length;
    }
    System.arraycopy(b, 0, keyBytes, 0, len);
    SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

    this.keySpec = keySpec;
  }

  /**
   * AES/CBC/PKCS5Padding 알고리즘 암호화
   * @param str
   * @return
   * @throws NoSuchAlgorithmException
   * @throws GeneralSecurityException
   * @throws UnsupportedEncodingException
   */
  public String encrypt(String str) throws NoSuchAlgorithmException,
      GeneralSecurityException, UnsupportedEncodingException {
    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
    byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
    String enStr = new String(Base64.encodeBase64(encrypted));
    return enStr;
  }

  /**
   * AES/CBC/PKCS5Padding 알고리즘  복호화
   * @param str
   * @return
   * @throws NoSuchAlgorithmException
   * @throws GeneralSecurityException
   * @throws UnsupportedEncodingException
   */
  public String decrypt(String str) throws NoSuchAlgorithmException,
      GeneralSecurityException, UnsupportedEncodingException {
    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
    byte[] byteStr = Base64.decodeBase64(str.getBytes());
    return new String(c.doFinal(byteStr), "UTF-8");
  }
}