import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import messages.PublicInfo;
import messages.RegBack;
import messages.RegMessage;
import messages.RepMessage;

public class SmartMeter {

	private long id;
	private Pairing pairing;
	private Element did;
	private Element rid;
	private BigInteger order;
	private Element g;
	private Element gt;
	private Element w;
	private Element rx;


	public SmartMeter(PublicInfo pubInfo) throws IOException {
		super();
		this.id = Utils.randomlong();
		this.pairing = pubInfo.getPairing();
		
		String temStr = Utils.sha256(Long.toString(this.id));
		this.rid = Utils.hash2Element(temStr, pairing);
		this.order = pairing.getG1().getOrder();
		this.g = pubInfo.getG();
		this.gt = pubInfo.getGt();
		this.w = pubInfo.getWw();
		rx = pubInfo.getRx();
	}

	public RegMessage genRegMesssage() {
		RegMessage reg = new RegMessage(this.id);
		return reg;
	}

	public void genRegBack(RegBack back) {
		if (null == back) {
			System.out.println("Reg failed at meter side");
			return;
		}
		this.did = back.getDid();
//		System.out.println("ididdidi " + this.id);
//		System.out.println("rid meter meter " + this.rid);
//		System.out.println("did meter meter " + this.did);
	}

	public RepMessage genSingleRepMessage() throws IOException {

		long ti = System.currentTimeMillis();
		BigInteger mi = BigInteger.valueOf(Utils.randomInt());

		BigInteger ri = Utils.randomBig(this.order);
//		BigInteger rsi = Utils.randomBig(this.order);
		
		Element c1i = this.g.duplicate().pow(ri);
		Element c2i = this.gt.duplicate().pow(mi).duplicate().mul(this.w.duplicate().pow(ri));

		
		String temStr = Utils.sha256(c2i.toString() + ti);
		Element temH2 = Utils.hash2Element(temStr, pairing);
//		System.out.println("temH2 " + temH2);
//		temH2 = temH2.duplicate().pow(rsi);
		temH2 = temH2.duplicate().pow(ri);
		Element vi = this.did.duplicate().mul(temH2);
		
//		Element left = pairing.pairing(this.g,vi);
//		Element right1 = pairing.pairing(this.rx,this.rid);
//		Element right12 = pairing.pairing(this.rx,Utils.hash2Element(Utils.sha256(Long.toString(this.id)), pairing));
//		Element right2 = pairing.pairing(c1i, Utils.hash2Element(temStr, pairing));
//		Element right22 = pairing.pairing(this.g,temH2);
//		
		
//		System.out.println("rxrxrxrx   " + rx);
//		System.out.println("ridridrid   " + rid);
//		System.out.println("right1 " + right1);;
//		System.out.println("right12" + right12);;
//		System.out.println("right2 " + right2);
//		System.out.println("right22" + right22);
//		System.out.println("right  " + right1.duplicate().mul(right2));
//		System.out.println("right  " + right2.duplicate().mul(right1));
//		
//		if( left.equals(right1.duplicate().mul(right2))){
//			System.out.println("left ");
//		}else{
//			System.out.println("asdffffffffff");
//		}
		
		//public RepMessage(long id, Element c1i, Element c2i, Element vi, long ti) {
		return new RepMessage(this.id, c1i, c2i, vi, ti);
	}

}
