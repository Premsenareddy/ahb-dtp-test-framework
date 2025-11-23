#!/bin/bash

export RED='\033[0;31m'
export GREEN='\033[1;32m'
export NC='\033[0m'
export totalPassed=0;
export totalFailed=0;
export testLogs=()

function heading() {
  echo "/////////////////////////////////////////////"
  echo "$1"
  echo "/////////////////////////////////////////////"
  echo
}

function test-task(){
  echo $1
}

function pass-fail-total(){
  printf "Total Passed: ${totalPassed} / Total Failed: ${totalFailed}"
}

function test-logs(){
  testLogs+=$1
}

function test-summary(){
  for i in "${testLogs[@]}"
  do
    printf "$i"
  done

  if [ ${#testLogs[@]} -eq 0 ]; then
    echo "All Tests passed successfully"
  else
    echo
    echo
    exit 1
  fi
}

function set-namespace(){
  namespace=$1
  echo "Setting Namespace = ${namespace}"
  echo
}

function set-user-group(){
  userGroup=$1
  echo "Setting User Group = ${userGroup}"
  echo
}

export -f heading
export -f test-task
export -f pass-fail-total
export -f test-logs
export -f test-summary
export -f set-namespace
export -f set-user-group
