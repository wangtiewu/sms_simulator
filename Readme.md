# sms_simulator

sms_simulator is a short message service's simulator,this time it supports ChinaMobile CMPP and ChinaUnicom SGIP ISMG function.SP can use this simulator for test.

## ChinaMobile CMPP/CMPP3 Features

* Supports CMPP's connect,submit,delivery,active_test,terminate instrucs.
* Supports IP limits,Connections limits,Rate-limits advinced functions.

## ChinaUnicom SGIP Featrues

* Supports SGIP's bind,submit,report,delivery,unbind instrucs.
* Supports IP limits,Connections limits,Rate-limits advinced functions.

## Configure

* See bin/sms.properties 

## Run

* On Linux, run sms_simulator.sh start
* On Windows, run sms_simulator.bat

## Test

* delivery message test
	#telnet host 9999
	srcNumber destNumber content
	
	//host is simulator's server ip

