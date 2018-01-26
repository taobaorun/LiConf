#!/bin/sh
BASEDIR=`dirname $0`
BASEDIR=`(cd "$BASEDIR"; pwd)`
echo current path $BASEDIR

PIDPATH="$BASEDIR"

if [ "$1" != "" ]; then
    PIDPATH="$1"
fi

PIDFILE=$PIDPATH"/startup.pid"
echo $PIDFILE

if [ ! -f "$PIDFILE" ]
then
    echo "no worker to stop (could not find file $PIDFILE)"
else
    kill -9 $(cat "$PIDFILE")
    rm -f "$PIDFILE"
    echo STOPPED
fi
exit 0

echo stop finished.
