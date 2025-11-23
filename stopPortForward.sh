#!/usr/bin/env bash


SEARCH="kubectl.port";

ps -e | grep $SEARCH

#read -p "Are you sure you want to continue to kill the above processes? <y/N> " prompt
#if [[ $prompt == "y" || $prompt == "Y" || $prompt == "yes" || $prompt == "Yes" ]]
#then
#  echo ""
#else
#  exit 0
#fi

echo "Killing port forwards $SEARCH"

for ENTRY in `ps -ef | grep $SEARCH | awk '{ print $2 }'`;
do `kill -9 $ENTRY`;
done


for ENTRY in `ipcs | grep $USER | awk '{if ( $4 == 644) print $2}'`;
do `ipcrm -m $ENTRY`;
done
