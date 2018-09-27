import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import messages.PublicInfo;
import messages.RegBack;
import messages.RegMessage;
import messages.RepAgg;
import messages.RepMessage;

public class Agg {

	private ArrayList<RepMessage> alRep = new ArrayList<RepMessage>();
	private long id;
	private Pairing pairing;
	private Element g;
	private Element gt;
	private Element did;
	private Element rid;
	private Element rx;

	private Element c1;
	private Element c2;

	private BigInteger order;

	public Agg(PublicInfo pubInfo) throws IOException {
		super();
		this.id = Utils.randomlong();
		String temStr = Utils.sha256(Long.toString(this.id));
		this.pairing = pubInfo.getPairing();
		this.rid = Utils.hash2Element(temStr, pairing);
		
		this.g = pubInfo.getG();
		this.gt = pubInfo.getGt();
		this.rx = pubInfo.getRx();
		this.order = pubInfo.getPairing().getG1().getOrder();
	}

	public RegMessage genRegMesssage() {
		RegMessage reg = new RegMessage(this.id);
		return reg;
	}

	public void getRegBack(RegBack back) {
		if (null == back) {
			System.out.println("Reg failed");
			return;
		}
		this.did = back.getDid();
	}

	public RepAgg getRepMessage(RepMessage rep) throws IOException {
		alRep.add(rep);
		if (alRep.size() < PublicParams.METERS_NUM)
			return null;

		if (signCheck() == true) {
//			 System.out.println("checked at the agg side");
			return generateRep();
		} else {
			System.out.println("check failed at the agg side, really,,sdfsfsdfsd");
			return null;
		}

	}

	public void clearReportMessage() throws IOException {
		alRep.clear();
	}
	
	private boolean signCheck() throws IOException {
		Iterator<RepMessage> itRep = alRep.iterator();

		Element sumV = pairing.getG1().newOneElement();
		Element sumHid = pairing.getG1().newOneElement();
		
		Element right2 = pairing.getGT().newOneElement();
		Element temRight2 = pairing.getGT().newOneElement();

		c1 = pairing.getG1().newOneElement();
		c2 = pairing.getGT().newOneElement();
		
		
		while (itRep.hasNext()) {
			RepMessage rep = itRep.next();
			
			BigInteger pi = Utils.randomLengthBig(PublicParams.RANDOM_BIT_LENGTH);

			sumV = sumV.duplicate().mul(rep.getVi().pow(pi));
			sumHid = sumHid.duplicate().mul(getPublicKeyById(rep.getId()).pow(pi));
			temRight2 = pairing.pairing(rep.getC1i(), hash2ElementG2(rep.getC2i(), rep.getTi()).pow(pi));
			right2 = right2.duplicate().mul(temRight2);

			c1 = c1.duplicate().mul(rep.getC1i());
			c2 = c2.duplicate().mul(rep.getC2i());
		}
		
		Element left = pairing.pairing(this.g, sumV);
		Element right1 = pairing.pairing(this.rx, sumHid);
		Element right = right1.duplicate().mul(right2);
//		System.out.println("rx agg agg " + this.rx);
//		System.out.println("sumHid agg agg " + sumHid);
//		System.out.println(left);
//		System.out.println(right1);
//		System.out.println(right2);
		
		if (left.equals(right)){
			return true;
		}
		return false;
	}

//	private RepAgg generateRepAgg() throws IOException {
//		Iterator<RepMessage> itRep = alRep.iterator();
//
//		while (itRep.hasNext()) {
//			RepMessage rep = itRep.next();
//
//			c1 = c1.duplicate().mul(rep.getC1i());
//			c2 = c2.duplicate().mul(rep.getC2i());
//		}
//		return  generateRep(c1, c2) ;
//	}

	private Element getPublicKeyById(long id) throws IOException {
		String temStr = Utils.sha256(Long.toString(id));
		return Utils.hash2Element(temStr, pairing);
	}

	private Element hash2ElementG2(Element c2i, long ti) throws IOException {
		String temStr = Utils.sha256(c2i.toString() + ti);
		return Utils.hash2Element(temStr, pairing);
	}
	
	private RepAgg generateRep() throws IOException {

		long ti = System.currentTimeMillis();
		BigInteger rc = Utils.randomBig(this.order);
		Element u = this.g.pow(rc);
		Element v = this.did.duplicate().mul(hash2ElementG2(c2, ti).duplicate().pow(rc));

		// RepAgg(long id, Element c1, Element c2, Element u, Element v, long  ti)
		return new RepAgg(this.id, c1, c2, u, v, ti);
	}


}
