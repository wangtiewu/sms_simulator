echo off
if '%1=='## goto ENVSET
rem 设置工作目录
set WORK_DIR=.
cd %WORK_DIR%
rem 设置lib库目录
SET EXTEN_LIB_HOME=%WORK_DIR%/lib
rem 设置执行类库目录
set CLASS_DIR=%WORK_DIR%/bin
rem 设置path路径分隔符
set PATH_SPLIT=;
rem 设置java路径
set JAVA=java
rem 设置classpath
set CLASSPATH=%CLASSPATH%%PATH_SPLIT%%CLASS_DIR%
for %%i in (%EXTEN_LIB_HOME%/*.jar) do (
	CALL %0 ## %EXTEN_LIB_HOME%/%%i
)
rem set CLASSPATH=%CLASSPATH%%PATH_SPLIT%sms_simulator.jar
echo %CLASSPATH%
rem 设置pid文件
set PIDFILE=%WORK_DIR%/logs/sms_simulator.pid
rem 设置执行类
set BEAN=net.qing.sms.simulator.SmsSimulatorStartupX
rem JVM_OPTION="-server -Xms4G -Xmx4G -XX:NewSize=3584m -XX:PermSize=64m -XX:SurvivorRatio=1 -XX:+UseParallelGC -verbose:gc -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy"

%JAVA% %JVM_OPTION% -cp %CLASSPATH% %BEAN%

:ENVSET
set CLASSPATH=%CLASSPATH%;%2
goto END 

:END

