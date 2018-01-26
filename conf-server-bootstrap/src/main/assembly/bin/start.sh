#!/bin/sh

BASEDIR=`dirname $0`/..
BASEDIR=`(cd "$BASEDIR"; pwd)`
echo current path:$BASEDIR

BASEBIN_DIR=$BASEDIR"/bin"
cd $BASEBIN_DIR

PIDPATH="$BASEBIN_DIR"

SPRING_PROFILE="prod"

GC_DATE=`date +%Y-%m-%d-%H-%M`

LOG_PATH="/home/admin/logsconf-server/"
JVM_FILE="-XX:+UseCondCardMark -XX:+UseConcMarkSweepGC -XX:+HeapDumpOnOutOfMemoryError "
JVM_FILE="$JVM_FILE -XX:CMSWaitDuration=250"
JVM_FILE="$JVM_FILE -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:${LOG_PATH}/gc-${GC_DATE}.log"
JVM_FILE="$JVM_FILE -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M"
JVM_FILE="$JVM_FILE -XX:HeapDumpPath=${LOG_PATH}/ -XX:ErrorFile=${LOG_PATH}/java_error-${GC_DATE}.log"

if [ "$#" -le 0 ]; then
    echo "---------------------------"
    echo " please specify environment"
    echo " prod is production env"
    echo " test is test env"
    echo " e.g"
    echo " ./start.sh prod"
    echo "---------------------------"
    exit 1
fi

if [ "$1" != "" ]; then
    SPRING_PROFILE="$1"
fi

if [ "$2" != "" ]; then
    PIDPATH="$2"
fi


# ------ check if server is already running
PIDFILE=$PIDPATH"/startup.pid"
if [ -f $PIDFILE ]; then
    if kill -0 `cat $PIDFILE` > /dev/null 2>&1; then
        echo server already running as process `cat $PIDFILE`.
        exit 0
    fi
fi

#JAVACMD="/export/servers/jdk1.8.0_60/bin/java"
# ------ set JAVACMD
# If a specific java binary isn't specified search for the standard 'java' binary
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java`
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

# ------ set jvm memory
if [ -z "$OPTS_MEMORY" ] ; then
    OPTS_MEMORY="`sed -n '1p' jvm.properties`"
fi
if [ "`sed -n '2p' jvm.properties`" != "" ] ; then
    JAVACMD="`sed -n '2p' jvm.properties`"
fi

nohup "$JAVACMD" \
  $OPTS_MEMORY $JVM_FILE \
  -Dbasedir="$BASEDIR" \
  -Dfile.encoding="UTF-8" \
  -Dspring.profiles.active="${SPRING_PROFILE}" \
  -jar ../lib/conf-server-bootstrap-1.0.1-SNAPSHOT.jar \
  > /dev/null &


# ------ wirte pid to file
if [ $? -eq 0 ]
then
    if /bin/echo -n $! > "$PIDFILE"
    then
        sleep 1
        echo STARTED SUCCESS
    else
        echo FAILED TO WRITE PID
        exit 1
    fi
#    tail -100f $LOGFILE
else
    echo SERVER DID NOT START
    exit 1
fi
