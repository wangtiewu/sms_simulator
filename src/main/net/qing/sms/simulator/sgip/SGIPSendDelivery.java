package net.qing.sms.simulator.sgip;

public class SGIPSendDelivery extends SGIPMoMsg implements Runnable{
	private String content;
	private String destNumber;
	private String srcNumber;

	public SGIPSendDelivery(String srcNumber, String destNumber, String content) {
		this.srcNumber = srcNumber;
		this.destNumber = destNumber;
		this.content = content;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		SGIPDelivery delivery = new SGIPDelivery(SGIPHeader.createSeq());
		delivery.setUserNumber(srcNumber);
		delivery.setSpNumber(destNumber);
		delivery.setContent(content);
		smgClient.sendMsg(delivery);
	}
}
