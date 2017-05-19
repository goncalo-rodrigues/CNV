address=$1

for fn in "test01.txt" "test02.txt" "test03.txt" "test04.txt" "test05.txt"
do 
	for i in 0 100 200 300
	do 
		for j in 0 100 200 300
		do
		curl http://$address:8000/r.html?f=$fn\&sc=400\&sr=400\&wc=100\&wr=100\&coff=$i\&roff=$j & 
		sleep 0.1
		#sleep 0.05
		done
	done
done
