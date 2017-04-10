#!/bin/bash

curr_dir=$(pwd)

cd "/home/ec2-user/BIT/samples"
javac *.java

cd "/home/ec2-user/raytracer-master"
make clean
make

# Reinstruments the raytracer code
java StatisticsToolToFile -dynamic src/raytracer/ src/raytracer/
java StatisticsToolToFile -dynamic src/raytracer/pigments src/raytracer/pigments
java StatisticsToolToFile -dynamic src/raytracer/shapes/ src/raytracer/pigments

cd $curr_dir
