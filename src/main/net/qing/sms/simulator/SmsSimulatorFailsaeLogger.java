package net.qing.sms.simulator;

import eet.evar.tool.logger.support.FailsafeLogger;

public class SmsSimulatorFailsaeLogger extends FailsafeLogger {
	protected String appendContextMessage(String msg) {
		return "[SmsSimulator] " + msg;// + ", version: " + Version.getVersion();
	}
}
