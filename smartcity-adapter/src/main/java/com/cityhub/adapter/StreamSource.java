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
package com.cityhub.adapter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;

import com.cityhub.core.AbstractPollSource;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamSource extends AbstractPollSource {

  @Override
  public void setup(Context context) {
  }
  @Override
  public void execFirst() {
  }

  @Override
  public void processing() {

    try {
      String uid = UUID.randomUUID().toString().replaceAll("-","");
      IpCamDeviceRegistry.register("rasberryCam-" + uid, getUrlAddr(), IpCamMode.PUSH);
      IpCamDevice capture = IpCamDeviceRegistry.getIpCameras().get(0);
      while (!capture.isOnline()) ;
      capture.open();
      while (true) {
        BufferedImage image = capture.getImage();
        //byte[] imageInByte = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        //ImageIO.write(ImageIO.read(new ByteArrayInputStream(imageInByte)), "jpg", new File("/b/test3.jpg"));
        Event event = EventBuilder.withBody(imageInByte);
        getChannelProcessor().processEvent(event);
        Thread.sleep(15);
      }
    } catch (MalformedURLException e) {
      log.error("Invalid url error : {}" , e.getMessage());
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    } catch (InterruptedException e) {
      log.error("connected error : {}" , e.getMessage());
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

  }

} // end of class
