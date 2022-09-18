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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.FlumeException;
import org.apache.flume.Source;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.Configurables;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.environment.DefaultConstants;
import com.cityhub.utils.JsonUtil;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NewTotalSource extends AbstractSource implements Configurable, EventDrivenSource {

  private String hostName;
  private int port;
  private int maxLineLength;
  private boolean ackEveryEvent;
  private String sourceEncoding;

  private CounterGroup counterGroup;
  private ServerSocketChannel serverSocket;
  private AtomicBoolean acceptThreadShouldStop;
  private Thread acceptThread;
  private ExecutorService handlerService;

  public NewTotalSource() {
    super();

    port = 0;
    counterGroup = new CounterGroup();
    acceptThreadShouldStop = new AtomicBoolean(false);
  }

  @Override
  public void configure(Context context) {
    String hostKey = DefaultConstants.CONFIG_HOSTNAME;
    String portKey = DefaultConstants.CONFIG_PORT;
    String ackEventKey = DefaultConstants.CONFIG_ACKEVENT;

    Configurables.ensureRequiredNonNull(context, hostKey, portKey);

    hostName = context.getString(hostKey);
    port = context.getInteger(portKey);
    ackEveryEvent = context.getBoolean(ackEventKey, true);
    maxLineLength = context.getInteger(DefaultConstants.CONFIG_MAX_LINE_LENGTH, DefaultConstants.DEFAULT_MAX_LINE_LENGTH);
    sourceEncoding = context.getString(DefaultConstants.CONFIG_SOURCE_ENCODING, DefaultConstants.DEFAULT_ENCODING);
  }

  @Override
  public void start() {

    log.info("Source starting");

    counterGroup.incrementAndGet("open.attempts");

    try {
      SocketAddress bindPoint = new InetSocketAddress(hostName, port);

      serverSocket = ServerSocketChannel.open();
      serverSocket.socket().setReuseAddress(true);
      serverSocket.socket().bind(bindPoint);

      log.info("Created serverSocket:{}", serverSocket);
    } catch (IOException e) {
      counterGroup.incrementAndGet("open.errors");
      log.error("Unable to bind to socket. Exception follows.", e);
      stop();
      throw new FlumeException(e);
    }

    handlerService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("netcat-handler-%d").build());

    AcceptHandler acceptRunnable = new AcceptHandler(maxLineLength);
    acceptThreadShouldStop.set(false);
    acceptRunnable.counterGroup = counterGroup;
    acceptRunnable.handlerService = handlerService;
    acceptRunnable.shouldStop = acceptThreadShouldStop;
    acceptRunnable.ackEveryEvent = ackEveryEvent;
    acceptRunnable.source = this;
    acceptRunnable.serverSocket = serverSocket;
    acceptRunnable.sourceEncoding = sourceEncoding;

    acceptThread = new Thread(acceptRunnable);

    acceptThread.start();

    log.debug("Source started");
    super.start();
  }

  @Override
  public void stop() {
    log.info("Source stopping");

    acceptThreadShouldStop.set(true);

    if (acceptThread != null) {
      log.debug("Stopping accept handler thread");

      while (acceptThread.isAlive()) {
        try {
          log.debug("Waiting for accept handler to finish");
          acceptThread.interrupt();
          acceptThread.join(500);
        } catch (InterruptedException e) {
          log.debug("Interrupted while waiting for accept handler to finish");
          Thread.currentThread().interrupt();
        }
      }

      log.debug("Stopped accept handler thread");
    }

    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        log.error("Unable to close socket. Exception follows.", e);
        return;
      }
    }

    if (handlerService != null) {
      handlerService.shutdown();

      log.debug("Waiting for handler service to stop");

      // wait 500ms for threads to stop
      try {
        handlerService.awaitTermination(500, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        log.debug("Interrupted while waiting for netcat handler service to stop");
        Thread.currentThread().interrupt();
      }

      if (!handlerService.isShutdown()) {
        handlerService.shutdownNow();
      }

      log.debug("Handler service stopped");
    }

    log.debug("Source stopped. Event metrics:{}", counterGroup);
    super.stop();
  }

  private static class AcceptHandler implements Runnable {

    private ServerSocketChannel serverSocket;
    private CounterGroup counterGroup;
    private ExecutorService handlerService;
    private EventDrivenSource source;
    private AtomicBoolean shouldStop;
    private boolean ackEveryEvent;
    private String sourceEncoding;

    private final int maxLineLength;

    public AcceptHandler(int maxLineLength) {
      this.maxLineLength = maxLineLength;
    }

    @Override
    public void run() {
      log.debug("Starting accept handler");

      while (!shouldStop.get()) {
        try {
          SocketChannel socketChannel = serverSocket.accept();

          NewTotalSocketHandler request = new NewTotalSocketHandler(maxLineLength);

          request.socketChannel = socketChannel;
          request.counterGroup = counterGroup;
          request.source = source;
          request.ackEveryEvent = ackEveryEvent;
          request.sourceEncoding = sourceEncoding;

          handlerService.submit(request);

          counterGroup.incrementAndGet("accept.succeeded");
        } catch (ClosedByInterruptException e) {
          // Parent is canceling us.
        } catch (IOException e) {
          log.error("Unable to accept connection. Exception follows.", e);
          counterGroup.incrementAndGet("accept.failed");
        }
      }

      log.debug("Accept handler exiting");
    }
  }

  private static class NewTotalSocketHandler implements Runnable {

    private Source source;
    private CounterGroup counterGroup;
    private SocketChannel socketChannel;
    private boolean ackEveryEvent;
    private String sourceEncoding;

    private final int maxLineLength;

    public NewTotalSocketHandler(int maxLineLength) {
      this.maxLineLength = maxLineLength;
    }

    @Override
    public void run() {
      log.debug("Starting connection handler");
      Event event = null;

      try {
        Reader reader = Channels.newReader(socketChannel, sourceEncoding);
        Writer writer = Channels.newWriter(socketChannel, sourceEncoding);
        CharBuffer buffer = CharBuffer.allocate(maxLineLength);
        buffer.flip(); // flip() so fill() sees buffer as initially empty

        while (true) {
          // this method blocks until new data is available in the socket
          int charsRead = fill(buffer, reader);
          log.debug("Chars read = {}", charsRead);

          // attempt to process all the events in the buffer
          int eventsProcessed = processEvents(buffer, writer);
          log.debug("Events processed = {}", eventsProcessed);

          if (charsRead == -1) {
            // if we received EOF before last event processing attempt, then we
            // have done everything we can
            break;
          } else if (charsRead == 0 && eventsProcessed == 0) {
            if (buffer.remaining() == buffer.capacity()) {
              // If we get here it means:
              // 1. Last time we called fill(), no new chars were buffered
              // 2. After that, we failed to process any events => no newlines
              // 3. The unread data in the buffer == the size of the buffer
              // Therefore, we are stuck because the client sent a line longer
              // than the size of the buffer. Response: Drop the connection.
              log.warn("Client sent event exceeding the maximum length");
              counterGroup.incrementAndGet("events.failed");
              writer.write("FAILED: Event exceeds the maximum length (" + buffer.capacity() + " chars, including newline)\n");
              writer.flush();
              break;
            }
          }
        }

        socketChannel.close();

        counterGroup.incrementAndGet("sessions.completed");
      } catch (IOException e) {
        counterGroup.incrementAndGet("sessions.broken");
        try {
          socketChannel.close();
        } catch (IOException ex) {
          log.error("Unable to close socket channel. Exception follows.", ex);
        }
      }

      log.debug("Connection handler exiting");
    }

    public static final String HEADER_BODY_HEX = (char) 0xD0 + "";
    public static final String FIELD_HEX = (char) 0xD1 + "";
    public static final String ARRAY_HEX = (char) 0xD2 + "";
    public static final String ARRAY_FIELD_HEX = (char) 0xD3 + "";
    public static final String BODY_START_HEX = (char) 0xDA + "";
    public static final String BODY_END_HEX = (char) 0xDB + "";
    public static final String END_HEX = (char) 0xDF + "";

    /**
     * <p>
     * Consume some number of events from the buffer into the system.
     * </p>
     *
     * Invariants (pre- and post-conditions): <br/>
     * buffer should have position @ beginning of unprocessed data. <br/>
     * buffer should have limit @ end of unprocessed data. <br/>
     *
     * @param buffer The buffer containing data to process
     * @param writer The channel back to the client
     * @return number of events successfully processed
     * @throws IOException
     */
    private int processEvents(CharBuffer buffer, Writer writer) throws IOException {

      int numProcessed = 0;

      boolean foundNewLine = true;
      while (foundNewLine) {
        foundNewLine = false;

        int limit = buffer.limit();
        for (int pos = buffer.position(); pos < limit; pos++) {
          if (buffer.get(pos) == '\n') {

            // parse event body bytes out of CharBuffer
            buffer.limit(pos); // temporary limit
            ByteBuffer bytes = Charsets.UTF_8.encode(buffer);
            buffer.limit(limit); // restore limit

            // build event object
            /***
            byte[] body = new byte[bytes.remaining()];
            bytes.get(body);
            Event event = EventBuilder.withBody(body);
            ***/
            byte[] body = new byte[bytes.remaining()];
            bytes.get(body);
            String telegram = new String(body);

            String template = new JsonUtil().getFile("openapi/CityNewTotal.template");
            JSONObject jTemplate = new JSONObject(template);
            JsonUtil jsonEx = new JsonUtil(jTemplate);

            String[] array = telegram.split(HEADER_BODY_HEX);

            String tHeader = array[0];
            String[] arHeader = tHeader.split(FIELD_HEX);
            String[] keyHeader = {"event", "headCnt", "bodyCnt", "telegramOrder", "linkType", "sendCode",
                                  "receiveCode", "encryptYN", "sendType", "rrKey", "reqDate", "dataLen"};
            for (int i = 0; i < arHeader.length; i++) {

              if ( keyHeader[i].equals("telegramOrder")
                      || keyHeader[i].equals("receiveCode")
                      || keyHeader[i].equals("encryptYN")
                      || keyHeader[i].equals("sendType") ) {
                String[] arStr = arHeader[i].split(ARRAY_HEX);
                JSONArray aArray = new JSONArray();
                for (String element : arStr) {
                  aArray.put(element);
                }
                jsonEx.put(keyHeader[i] + ".value", aArray);
              } else {
                jsonEx.put(keyHeader[i] + ".value", arHeader[i]);
              }
            }

            String tBody = array[1];
            tBody = tBody.replaceAll(BODY_START_HEX, "");
            tBody = tBody.replaceAll(BODY_END_HEX, "");
            tBody = tBody.replaceAll(END_HEX, "");
            tBody = tBody.replaceAll("\r", "");
            String[] arBody = tBody.split(FIELD_HEX);
            String[] keyBody = {"eventId", "eventName", "eventGrade", "eventNum", "progress",
                                "eventPoint", "eventPointName", "eventContent", "eventDate", "progressContent",
                                "progressUser", "progressDate", "endDate", "eventItemCnt", "eventItemName",
                                "eventDtlCode", "sendHis"};
            for (int i = 0; i < arBody.length; i++) {

              if ( keyBody[i].equals("eventPoint")
                      || keyBody[i].equals("progressContent")
                      || keyBody[i].equals("progressUser")
                      || keyBody[i].equals("eventItemName")
                      || keyBody[i].equals("sendHis") ) {
                String[] arStr = arBody[i].split(ARRAY_HEX);
                JSONArray aArray = new JSONArray();
                for (String element : arStr) {
                  aArray.put(element);
                }
                jsonEx.put(keyBody[i] + ".value", aArray);
              } else {
                jsonEx.put(keyBody[i] + ".value", arBody[i]);
              }
            }

            JSONObject jobj = new JSONObject();
            jobj.put(jTemplate.getString("type"), jTemplate);
            Event event = EventBuilder.withBody(jobj.toString().getBytes());

            // process event
            ChannelException ex = null;
            try {
              source.getChannelProcessor().processEvent(event);
            } catch (ChannelException chEx) {
              ex = chEx;
            }

            if (ex == null) {
              counterGroup.incrementAndGet("events.processed");
              numProcessed++;
              if (true == ackEveryEvent) {
                writer.write("0;\n");
              }
            } else {
              counterGroup.incrementAndGet("events.failed");
              log.warn("Error processing event. Exception follows.", ex);
              writer.write("1" + ex.getMessage() + ";\n");
            }
            writer.flush();

            // advance position after data is consumed
            buffer.position(pos + 1); // skip newline
            foundNewLine = true;

            break;
          }
        }

      }

      return numProcessed;
    }

    /**
     * <p>
     * Refill the buffer read from the socket.
     * </p>
     *
     * Preconditions: <br/>
     * buffer should have position @ beginning of unprocessed data. <br/>
     * buffer should have limit @ end of unprocessed data. <br/>
     *
     * Postconditions: <br/>
     * buffer should have position @ beginning of buffer (pos=0). <br/>
     * buffer should have limit @ end of unprocessed data. <br/>
     *
     * Note: this method blocks on new data arriving.
     *
     * @param buffer The buffer to fill
     * @param reader The Reader to read the data from
     * @return number of characters read
     * @throws IOException
     */
    private int fill(CharBuffer buffer, Reader reader) throws IOException {

      // move existing data to the front of the buffer
      buffer.compact();

      // pull in as much data as we can from the socket
      int charsRead = reader.read(buffer);
      counterGroup.addAndGet("characters.received", Long.valueOf(charsRead));

      // flip so the data can be consumed
      buffer.flip();

      return charsRead;
    }

  }
}
