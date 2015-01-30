#!/bin/bash

# Source function library.
. /etc/rc.d/init.d/functions

# Source networking configuration.
. /etc/sysconfig/network

# Check networking is up.
[ "$NETWORKING" = "no" ] && exit 0

RETVAL=0
PID=

#设置工作目录
WORK_DIR=.
#设置lib库目录
EXTEN_LIB_HOME=$WORK_DIR/lib
#设置执行类库目录
CLASS_DIR=$WORK_DIR/bin
#设置path路径分隔符
PATH_SPLIT=:
#设置java路径
JAVA=java
#设置classpath
CLASSPATH=$CLASSPATH$PATH_SPLIT$CLASS_DIR
for i in $EXTEN_LIB_HOME/*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done
CLASSPATH="$CLASSPATH":"sms_simulator.jar"
export CLASSPATH
#设置pid文件
PIDFILE=$WORK_DIR/logs/sms_simulator.pid
#设置执行类
BEAN=net.qing.sms.simulator.SmsSimulatorStartupX

#JVM_OPTION="-server -Xms4G -Xmx4G -XX:NewSize=3584m -XX:PermSize=64m -XX:SurvivorRatio=1 -XX:+UseParallelGC -verbose:gc -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy"

cd $WORK_DIR
case $1 in
    start)
       echo $$ > $PIDFILE;
       exec $JAVA $JVM_OPTION -cp ${CLASSPATH} $BEAN;;
     stop)
	if [ -f $PIDFILE ] ;then
		read PID <  "$PIDFILE" 
	else 
      		failure;echo;
      		echo -n $"$PIDFILE not found.";failure;echo
      		exit $RETVAL;
	fi
    	if checkpid $PID; then
		kill -TERM $PID >/dev/null 2>&1
    	 	RETVAL=$?
    	 	if [ $RETVAL -eq 0 ] ;then
                	success;echo 
                	echo -n "Waiting for sms_simulator to shutdown .."
        		while checkpid $PID;do
                		echo -n "."
                		sleep 1;
                	done
                	success;echo;rm -f $PIDFILE
          	else 
                	failure;echo
    	 	fi
	else
        	echo -n $"sms_simulator is dead and $PIDFILE exists.";failure;echo
        	RETVAL=7
	fi
	;;
     *)  
       echo "usage: $0 {start|stop}" ;;
esac
exit $RETVAL

