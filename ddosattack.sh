while true 
do
curl 127.0.0.1:8000/r.html?f=test01.txt\&sc=400\&sr=400\&wc=400\&wr=400\&coff=0\&roff=0 & 
sleep 0.2
curl 127.0.0.1:8000/r.html?f=test01.txt\&sc=200\&sr=200\&wc=200\&wr=200\&coff=0\&roff=0 &
sleep 2 
done

