#!/bin/bash
javac -classpath .:/opt/flume/plugins.d/agent/libext/*:/opt/flume/lib/*:/opt/flume/plugins.d/agent/lib/* -d /opt/flume/plugins.d/agent/lib/ /opt/flume/plugins.d/agent/lib/$1
