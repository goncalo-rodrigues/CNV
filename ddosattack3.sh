while true 
do
sleep 0.5
curl 127.0.0.1:8000/r.html?f=test01.txt\&sc=400\&sr=400\&wc=300\&wr=300\&coff=0\&roff=0 &
done

