#!/bin/bash

ls_res=$(ls)
if [[ ${ls_res} != *"raytracer-master"* ]]; then
  wget http://groups.ist.utl.pt/meic-cnv/project/raytracer-master.tgz
  tar xvzf raytracer-master.tgz
  cd raytracer-master
  for i in "*.txt"; do sed -i s/\\./,/g $i; done
  make
  cd ..
  jar cvf raytracer.jar -C raytracer-master/src .
fi

javac -cp raytracer.jar *.java
echo
echo "Starting WebServer..."
java_bin=$(which java)
sudo $java_bin WebServer
