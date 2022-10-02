#!/usr/bin/env bash 

# This a script for generating additional graalvm configuration and find paths not found during initial analyze.
# Every input option that is added to the program should be added here.

if [[ -z "$1" ]]; then
    echo "Run the script with version: graal-native-gen.sh CURRENT_VERSION_OF_JAR"; 
    echo "Example: scripts/graal-native-gen.sh '0.0.1-SNAPSHOT'"; 
    exit 1;
fi

echo "Input version was $1"
JAR="target/sariftool-$1-jar-with-dependencies.jar"
echo "Version given was $1, so will look for $JAR"


generate_native_image_conf() {
    java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ -jar "${JAR}" convert -s IND -o target
    java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ -jar "${JAR}" convert -V
    java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ -jar "${JAR}" convert -h
    java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ -jar "${JAR}" convert 
    java -agentlib:native-image-agent=config-merge-dir=./src/main/resources/META-INF/native-image/ -jar "${JAR}" -h
}

generate_native_image_conf