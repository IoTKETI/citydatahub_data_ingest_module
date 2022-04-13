AHOME=/root/flume
javac -classpath ${AHOME}/plugins.d/agent/libext/*:${AHOME}/lib/*:${AHOME}/plugins.d/agent/lib/* -d ${AHOME}/plugins.d/agent/lib/ ${AHOME}/plugins.d/agent/lib/$1
