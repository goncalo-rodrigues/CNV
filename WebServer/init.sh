#!/bin/bash

ls_res=$(ls)
if [[ ${ls_res} != *"raytracer-master"* ]]; then
  # Raytracer - CNV
  wget http://groups.ist.utl.pt/meic-cnv/project/raytracer-master.tgz
  tar xvzf raytracer-master.tgz
  rm raytracer-master.tgz

  # Raytracer - Missing Files
  wget https://github.com/idris/raytracer/archive/master.zip
  unzip master.zip -d miss
  cp miss/raytracer-master/hardwood.bmp raytracer-master
  cp miss/raytracer-master/spectrum.bmp raytracer-master
  rm master.zip
  rm -rf miss

  cd raytracer-master
  make
  cd ..
fi

cp *.java raytracer-master
cd raytracer-master
javac -cp /home/ec2-user/raytracer-master/src *.java
echo
echo "Starting WebServer..."
java_bin=$(which java)
log=../server.log
sudo rm $log
sudo $java_bin -cp /home/ec2-user/raytracer-master/src:/home/ec2-user/BIT:/home/ec2-user/BIT/samples:. -XX:-UseSplitVerifier WebServer > >(tee -a $log) 2> >(tee -a $log >&2)
cd ..
