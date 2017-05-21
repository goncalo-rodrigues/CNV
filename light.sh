while true
do
(
start="$(date +%s)"
curl "http://127.0.0.1:8000/r.html?f=test01.txt&sc=400&sr=300&wc=400&wr=300&coff=0&roff=0"  &>/dev/null
echo $start,`(date +%s)` >> results.txt
 )&
sleep 1
done
