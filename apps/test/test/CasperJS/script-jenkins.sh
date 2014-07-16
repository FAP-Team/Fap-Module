#!/bin/bash

echo $FAPSDK
cd $FAPSDK/apps/test
/opt/play-1.2.4/play test &
PID=$!
echo El PID del proceso es $PID
sleep 30
casperjs test `pwd`/test/CasperJS/*.test.js --xunit="$WORKSPACE/log-casperjs.xml" --no-colors
/opt/play-1.2.4/play stop
kill $(($PID+2))