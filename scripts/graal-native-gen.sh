#!/usr/bin/env bash 

java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ -jar  target/sarifconvert-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s IND -o /tmp/
 java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ -jar  target/sarifconvert-0.0.1-SNAPSHOT-jar-with-dependencies.jar -V
 java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ -jar  target/sarifconvert-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h
 java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ -jar  target/sarifconvert-0.0.1-SNAPSHOT-jar-with-dependencies.jar
