echo "Usage: ./demo_menu.sh <lb address>"
echo ""

address=$1

PS3='Please enter your choice: '
options=("Increase load" "Exit")

# Interactive menu
select opt in "${options[@]}"
do
    case $opt in
        "Increase load")
            xterm -xrm 'XTerm.vt100.allowTitleOps: false' -T "Load" -e "./demo_requests.sh $address" &
            ;;
        "Exit")
            echo "Goodbye."
            break
            ;;
    esac
done
