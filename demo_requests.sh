echo "Usage: ./demo_requests.sh <lb address>"
echo ""

address=$1

low="http://$address:8000/r.html?f=test04.txt&sc=400&sr=300&wc=400&wr=300&coff=0&roff=0"
medium="http://$address:8000/r.html?f=test04.txt&sc=8000&sr=6000&wc=1600&wr=1200&coff=0&roff=640"
requests=($low $medium)

while true; do
  rnd=$(echo $RANDOM)
  let i=$rnd%2
  curl ${requests[$i]} &
  sleep 3
done
