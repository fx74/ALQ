#!/bin/bash

DISTR="1 2 3 4 5 6 7 8"

for i in ${DISTR}
do

START=100
END=900
INCR=100
LIMIT=10000000
qsize=$START

while [ "$qsize" -le "$LIMIT" ]
do

java -cp .:../lib/commons-math3.jar it.unical.dimes.elq.test.Test ${i} ${qsize} > ${i}_${qsize}.csv

if [ "$qsize" -lt "$END" ]
then
  qsize=$(($qsize+$INCR))
else
  START=$((START*10))
  END=$((END*10))
  INCR=$((INCR*10))
  qsize=$START
fi

done

done
