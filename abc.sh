wget http://groups.ist.utl.pt/meic-cnv/project/raytracer-master.tgz
tar xvzf raytracer-master.tgz
cd raytracer-master
for i in "*.txt"; do sed -i s/\\./,/g $i; done
make
cd ..
jar cvf raytracer.jar -C raytracer-master/src .
javac -cp raytracer.jar WebServer.java
