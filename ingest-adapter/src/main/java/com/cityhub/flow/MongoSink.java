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
package com.cityhub.flow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cityhub.core.AbstractBaseSink;
import com.google.common.base.Preconditions;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoSink extends AbstractBaseSink {
  private static final Logger logger = LoggerFactory.getLogger(MongoSink.class);

  private MongoClient client;
  private MongoCollection<Document> collection;
  private List<ServerAddress> seeds;
  private MongoCredential credential;

  private String database;
  private MongoDatabase db;
  private String dbcollection;
  private boolean ssltype;

  @Override
  public void setup(Context context) {
    String username = context.getString("username");
    String password = context.getString("password");
    String url = context.getString("url");
    database = context.getString("database");
    seeds = getSeeds(context.getString("url"));
    dbcollection = context.getString("dbcollection");
    ssltype = Boolean.parseBoolean(context.getString("ssltype"));



    Preconditions.checkState(username != null, "No username specified");
    Preconditions.checkState(password != null, "No password specified");
    Preconditions.checkState(url != null, "No url specified");
    Preconditions.checkState(database != null, "No database specified");

    credential = MongoCredential.createCredential(username, database, password.toCharArray());

  }

  @Override
  public void execFirst() {
    MongoClientOptions options = MongoClientOptions.builder().sslEnabled(ssltype).build();
    client = new MongoClient(seeds, credential, options);
    db = client.getDatabase(database);

  }

  @Override
  public void exit() {
    if (client != null) {
      client.close();
    }
  }

  @Override
  public void processing(Event event) {

    byte[] bodyBytes = new byte[event.getBody().length - 5];
    System.arraycopy(event.getBody(), 5, bodyBytes, 0, event.getBody().length - 5);
    String body = new String(bodyBytes);


//    String body = new String(event.getBody(), StandardCharsets.UTF_8).substring(5); //20201229 1차변형

//    String body = new String(event.getBody(), StandardCharsets.UTF_8); //20201228 원형

    List<Document> jsonList = new ArrayList<>();  //insertMany

    if (body.startsWith("[")) {
      JSONArray jarr = new JSONArray(body);
      for (Object obj : jarr) {
        JSONObject json = (JSONObject)obj;
//        collection = db.getCollection(json.getString("header"));
        collection = db.getCollection(dbcollection);
//        collection.insertOne(Document.parse(json.toString()));

        Document jsnObject = Document.parse(json.getJSONObject("content").toString()); //insertMany
        jsonList.add(jsnObject);  //insertMany

      }
      collection.insertMany(jsonList);  //insertMany

    } else if (body.startsWith("{")) {
      JSONObject json = new JSONObject(body);
//      collection = db.getCollection(json.getString("header"));
      collection = db.getCollection(dbcollection);
      collection.insertOne(Document.parse(json.getJSONObject("content").toString()));  //기존insertOne

//      Document jsnObject = Document.parse(json.toString()); //insertMany
//      jsonList.add(jsnObject);  //insertMany
//      collection.insertMany(jsonList);  //insertMany
    } else {
      logger.info("Logger: " + body);
    }

  }


  private List<ServerAddress> getSeeds(String seedsString) {
    List<ServerAddress> seeds = new LinkedList<>();
    logger.info("seedsString: " + seedsString);
    String[] seedStrings = StringUtils.deleteWhitespace(seedsString).split(",");
    for (String seed : seedStrings) {
      String[] hostAndPort = seed.split(":");
      String host = hostAndPort[0];
      int port;
      if (hostAndPort.length == 2) {
        port = Integer.parseInt(hostAndPort[1]);
      } else {
        port = 27017;
      }
      seeds.add(new ServerAddress(host, port));
    }

    return seeds;
  }


}
