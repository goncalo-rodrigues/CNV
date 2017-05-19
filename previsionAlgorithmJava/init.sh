#!/bin/bash

CLPTH="/home/ec2-user/aws-java-sdk-1.11.132/lib/aws-java-sdk-1.11.132.jar:\
  /home/ec2-user/aws-java-sdk-1.11.132/lib/aws-java-sdk-1.11.132-sources.jar:\
  aws-java-sdk-1.11.132-javadoc.jar:/home/ec2-user/aws-java-sdk-1.11.132/third-party/lib/*:."

cd src
rm -rf *.class
javac -cp "$CLPTH" ist/cnv/*.java ist/cnv/loadBalancer/*.java \
  ist/cnv/scaler/*.java ist/cnv/worker/*.java

echo
echo "Starting LoadBalancer..."
java_bin=$(which java)
log=../server.log
rm $log
$java_bin -cp "$CLPTH" -XX:-UseSplitVerifier ist.cnv.loadBalancer.LoadBalancer > >(tee -a $log) 2> >(tee -a $log >&2)
cd ..
