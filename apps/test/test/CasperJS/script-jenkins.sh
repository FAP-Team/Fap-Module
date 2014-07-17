#!/bin/bash

echo $FAPSDK
cd $FAPSDK/apps/test
/opt/play-1.2.4/play test &
sleep 30
export FAPPID=`ps -u jenkins -f | awk '$9=="-javaagent:/opt/play-1.2.4/framework/play-1.2.4.jar" {print $2}'`
echo El PID del proceso es $PID
casperjs test `pwd`/test/CasperJS/*.test.js --xunit="$WORKSPACE/log-casperjs.xml" --no-colors
kill $FAPPID
echo Acabando la ejecución... el PID era $FAPPID