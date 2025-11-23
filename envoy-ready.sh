x=1
while [ $x -le 10 ]
do
  response=$(curl -s -m 5 --write-out '%{http_code}' --silent --output /dev/null -k -X POST http://localhost:15000)
  if [ "$response" == "200" ];
  then
    echo "Envoy Ready continuing..."
    break;
  else
    echo "Envoy not yet ready... response code $response"
    x=$(( $x + 1 ))
    sleep 1
  fi
done

java  -XX:MaxRAMPercentage=75 -XX:+UseG1GC -jar $1

