package net.qing.sms.simulator.sgip;

public class SGIPSendReport extends SGIPMoMsg implements Runnable{
	private byte[] msgId;
	private String srcId;
	private String destId;
	private String serviceId;
	private int state;

	public SGIPSendReport(byte[] msgId, String srcId, String destId,
			String serviceId, int state) {
		this.msgId = msgId;
		this.srcId = srcId;
		this.destId = destId;
		this.serviceId = serviceId;
		this.state = state;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		SGIPReport report = new SGIPReport(SGIPHeader.createSeq());
		report.setState(state);
		report.setUserNumber(destId);
		report.setReportType(0);
		report.setErrorCode(0);
		smgClient.sendMsg(report);
	}
}
