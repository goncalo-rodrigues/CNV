for i in {1..10}
do
curl "http://127.0.0.1:8000/r.html?f=test04.txt&sc=20000&sr=15000&wc=3250&wr=2500&coff=5000&roff=6500" &
sleep 1
done
