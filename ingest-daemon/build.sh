#! /bin/sh

mvn dockerfile:build -DU_ID=$(id -u) -DG_ID=$(id -g) -DUSER=${USER}
