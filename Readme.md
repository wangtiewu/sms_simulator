# sms_simulator

sms_simulator is a short message service's simulator,this time it supports ChinaMobile CMPP and ChinaUnicom SGIP ISMG function.SP can use this simulator for test.

## ChinaMobile CMPP/CMPP3 Features

* Supports CMPP(CMPP3)'s connect,submit,delivery,active_test,terminate instructs.
* Supports IP limits,connections limits,rate-limits advinced instructs.

## ChinaUnicom SGIP Featrues

* Supports SGIP's bind,submit,report,delivery,unbind instructs.
* Supports IP limits,connections limits,rate-limits advinced instructs.

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

