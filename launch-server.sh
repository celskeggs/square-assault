echo "Launching server..."
cd $(dirname $0)
exec java -jar ./Server.jar ./main.map
