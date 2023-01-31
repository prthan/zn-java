#!/bin/bash

mvn clean install -f ./dbio-core/pom.xml
mvn clean install -f ./dbio-rs/pom.xml
mvn clean package -f ./dbio-web/pom.xml
