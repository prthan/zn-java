#!/bin/bash

mvn clean install -f ./zn-core/pom.xml
mvn clean install -f ./zn-logger/pom.xml
mvn clean install -f ./zn-rs/pom.xml
mvn clean install -f ./zn-msg-client/pom.xml
