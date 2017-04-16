#!/bin/bash

init_dir=$(pwd)

wget http://grupos.tecnico.ulisboa.pt/~meic-cnv.daemon/labs/labs-bit/BIT.zip
unzip BIT.zip
rm BIT.zip

cd $init_dir
