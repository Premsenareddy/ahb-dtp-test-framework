
export NS=$1
export TAG=$2

echo "NS $NS"
echo "TAG $TAG"

./gradlew clean build -x test 
./alpha-k8-cli.sh config-generate $TAG obp-$NS ahbaenssdbpnpdacrtmp.azurecr.io $NS
