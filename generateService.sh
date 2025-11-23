#!/usr/bin/env bash
echo "WARNING:: By running this script alpha repos will be created"
echo -n "Are you sure? [y/N] "
read response
if [[ "$response" =~ ^([yY][eE][sS]|[yY])+$ ]]
then


TEMPLATE_REPLACEMENT=$1
if [ $2 ]; then
  SERVICE_REPLACEMNET=$2
else
  SERVICE_REPLACEMNET=XX
fi
if [ $3 ]; then
  PACKAGE_REPLACEMENT=$3
else
   PACKAGE_REPLACEMENT=YY
fi

for i in $(grep -rl TEMPLATE .* | grep -v generateService.sh); do
  sed -i '' "s/TEMPLATE/${TEMPLATE_REPLACEMENT}/g" ${i};
done;

for i in $(grep -rl tempService .* | grep -v generateService.sh); do
    sed -i '' "s/tempService/${SERVICE_REPLACEMNET}/g" ${i};
done;

for i in $(grep -rl tempPackage .* | grep -v generateService.sh); do
    sed -i '' "s/tempPackage/${PACKAGE_REPLACEMENT}/g" ${i};
done;

curr_dir=$(pwd)

#Setting up Main folder structure
cd src/main/java/uk/co/deloitte/banking/alpha

str="${PACKAGE_REPLACEMENT}"
IFS='.'
read -ra ADDR <<< "$str"
for i in "${ADDR[@]}"; do
    echo "$i"
    mkdir ${i}
    cd ${i}
    testDir=${testDir}${i}/
    newDir=$(pwd)
    echo "New Dir ="
    echo $newDir
    echo "Test Dir ="
    echo ${testDir}
done

for i in "${ADDR[@]}"; do
    cd ../
    echo "Current path is: "
    echo oldDir=$(pwd)
done

mv onboarding/* ${testDir}
rm -r onboarding

echo "Main Java Folder Structure Set Up"


#Setting up Test folder structure
cd $curr_dir
cd src/test/java/uk/co/deloitte/banking/alpha

str="${PACKAGE_REPLACEMENT}"
IFS='.' # space is set as delimiter
read -ra ADDR <<< "$str" # str is read into an array as tokens separated by IFS
for i in "${ADDR[@]}"; do # access each element of array
    echo "$i"
    mkdir ${i}
    cd ${i}
done

for i in "${ADDR[@]}"; do # access each element of array
    cd ../
done

mv onboarding/* ${testDir}
rm -r onboarding

echo "Test Java Folder Structure Set Up"


#Setting up Git link and removing template branches
git remote set-url origin git@bitbucket.org:alphaplatform/alpha-${TEMPLATE_REPLACEMENT}-${SERVICE_REPLACEMNET}.git

  echo "Make sure your initial commit is to the Master Branch, you are now pointing to repository:"
git remote -v

git branch | grep -v "master" | xargs git branch -D

  echo "Service Created!"

else
echo "Service Creation Canceled!"
fi
