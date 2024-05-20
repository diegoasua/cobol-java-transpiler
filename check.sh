#!/usr/bin/env bash
set -e

if [ $# -lt 2 ]; then
  echo "usage $0: <source-path> <targetpath>"
  exit 1
fi

ARGS=("$@")
SOURCEPATH=$1
TARGETPATH=$2

TARGET=$(basename $2 ".java")
echo "Comparing $SOURCEPATH with $TARGETPATH"

cobc -I "cpy" -x -o p $SOURCEPATH

if [[ "$(uname)" == "Darwin" ]]; then
  SED=gsed
else
  SED=sed
fi

javac $TARGETPATH
set +e

./p > cobol_output.txt

# reset data files
git checkout HEAD -- ./data
git clean -fd ./data

java -cp ./java $TARGET > java_output.txt

git checkout HEAD -- ./data
git clean -fd ./data

$SED -i -r 's/(\b|\+)(0+)?([0-9]+)(\.0+)?/\3/g' cobol_output.txt java_output.txt

EXIT_CODE=1

if diff cobol_output.txt java_output.txt; then
  echo "The outputs are the same."
else
  echo "The outputs are different."
fi

rm cobol_output.txt java_output.txt p
exit $EXIT_CODE