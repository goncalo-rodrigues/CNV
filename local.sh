#!/bin/bash

init_dir=$(pwd)

ls_res=$(ls)
if [[ ${ls_res} != *"raytracer-master"* ]]; then

  # BIT
  wget http://grupos.tecnico.ulisboa.pt/~meic-cnv.daemon/labs/labs-bit/BIT.zip
  unzip BIT.zip
  rm BIT.zip

  # Raytracer - CNV
  wget http://groups.ist.utl.pt/meic-cnv/project/raytracer-master.tgz
  tar xvzf raytracer-master.tgz
  rm raytracer-master.tgz
  cd raytracer-master
  for i in "*.txt"; do sed -i s/\\./,/g $i; done
  for i in "*.txt"; do sed -i s/,bmp/\\.bmp/g $i; done
  cd ..

  # Raytracer - Missing Files
  wget https://github.com/idris/raytracer/archive/master.zip
  unzip master.zip -d miss
  cp miss/raytracer-master/hardwood.bmp raytracer-master
  cp miss/raytracer-master/spectrum.bmp raytracer-master
  rm master.zip
  rm -rf miss
fi

clpth="$init_dir/raytracer-master/src:$init_dir/BIT:$init_dir/BIT/samples:."

# Just BIT stuff
cp $init_dir/CustomBIT/*.java $init_dir/BIT/samples
cd "$init_dir/BIT/samples"
javac -cp $clpth *.java

cd "$init_dir/raytracer-master"
make clean
make

# Reinstruments the raytracer code
java -cp $clpth StatisticsToolToFile -dynamic src/raytracer/ src/raytracer/
java -cp $clpth StatisticsToolToFile -dynamic src/raytracer/pigments src/raytracer/pigments
java -cp $clpth StatisticsToolToFile -dynamic src/raytracer/shapes/ src/raytracer/shapes
#java -cp $clpth StatisticsFunctionCallsToFile -dynamic src/raytracer/ src/raytracer/
#java -cp $clpth StatisticsFunctionCallsToFile -dynamic src/raytracer/pigments src/raytracer/pigments
#java -cp $clpth StatisticsFunctionCallsToFile -dynamic src/raytracer/shapes/ src/raytracer/shapes

#java -cp $clpth StatisticsDotMethodTool src/raytracer/ src/raytracer/
#java -cp $clpth StatisticsDotMethodTool src/raytracer/pigments src/raytracer/pigments
#java -cp $clpth StatisticsDotMethodTool src/raytracer/shapes/ src/raytracer/shapes

cd $init_dir

# Raytracer stuff
cp WebServer/*.java raytracer-master
cd raytracer-master
javac -cp $clpth *.java
echo
echo "Starting WebServer..."
java_bin=$(which java)
log=../server.log
sudo rm $log
sudo $java_bin -cp $clpth -XX:-UseSplitVerifier WebServer > >(tee -a $log) 2> >(tee -a $log >&2)

cd $init_dir
