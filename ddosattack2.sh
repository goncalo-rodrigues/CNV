while true 
do
sleep 0.5
curl 127.0.0.1:8000/r.html?f=test01.txt\&sc=200\&sr=200\&wc=200\&wr=200\&coff=0\&roff=0 &
done

