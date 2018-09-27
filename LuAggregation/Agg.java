import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import messages.EccParameters;
import messages.RegBack;
import messages.RegMessage;
import messages.RepMessage;

public class Agg {

	private ArrayList<Long> alMeterIdentities = new ArrayList<Long>();
	private ArrayList<Element> alMeterPublicKeys = new ArrayList<Element>();
	private ArrayList<RepMessage> alRep = new ArrayList<RepMessage>();
	// private int repSize = PublicParams.METERS_NUM;
	private long id;
	private Pairing pairing;
	private Element generator;
	private BigInteger di;
	private Element ri;
	private Element rx;
	private BigInteger order;
	private BigInteger sumCi;
	private BigInteger[] groupSumCi;
	
	private BigInteger nsquare;

	public Agg(EccParameters pubInfo) {
		super();
		this.id = Utils.randomlong();
		this.id = Utils.randomlong();

		this.pairing = pubInfo.getPairing();
		this.order = pubInfo.getPairing().getG1().getOrder();
		this.generator = pubInfo.getGenerator();

		this.di = Utils.randomBig(order);
		this.ri = this.generator.duplicate().mul(di);

		this.rx = pubInfo.getRx();
		this.order = pubInfo.getPairing().getG1().getOrder();
	}

	public RegMessage genRegMesssage() {
		RegMessage reg = new RegMessage(this.id, this.ri);
		return reg;
	}

	public void getRegMessage(RegMessage reg) {
		alMeterIdentities.add(reg.getId());
		alMeterPublicKeys.add(reg.getPublicKey());
	}

	public void clear() {
		alMeterIdentities.clear();
		alMeterPublicKeys.clear();
		alRep.clear();
	}
	
	public void clearReportMessages() {
		alRep.clear();
	}

	public RepMessage getRepMessage(RepMessage rep) throws IOException {
		alRep.add(rep);
		
		if (alRep.size() < PublicParams.METERS_NUM)
			return null;
		
		if (signCheck() == true) {
			// System.out.println("checked at the agg side");
			return generateRep();
		} else {
			System.out.println("check failed at the agg side");
			return null;
		}

	}

	public void setNsquare(BigInteger nsquare) {
		this.nsquare = nsquare;
	}
	
	private boolean signCheck() throws IOException {
		Iterator<RepMessage> itRep = alRep.iterator();

		sumCi = BigInteger.ONE;
		Element sumSigma = pairing.getG1().newOneElement();
		Element right = pairing.getGT().newOneElement();
		Element temRight = pairing.getGT().newOneElement();
		RepMessage rep;
		
		while (itRep.hasNext()) {
			rep = itRep.next();

			temRight = pairing.pairing(getPublicKeyById(rep.getId()), getHash2Element(rep));
			right = right.mul(temRight);

			sumSigma = sumSigma.mul(rep.getSigma());
			sumCi = (sumCi.multiply(rep.getCi())).mod(this.nsquare);
		}
		Element left = pairing.pairing(this.generator, sumSigma);
		if (left.equals(right))
			return true;
		return false;
	}

	private Element getPublicKeyById(long id) {
		int index = alMeterIdentities.indexOf(id);
		return alMeterPublicKeys.get(index);
	}

	private Element getHash2Element(RepMessage rep) throws IOException {
		String temStr = rep.getCi().toString() + rep.getId() + rep.getId() + rep.getTi();
		temStr = Utils.sha256(temStr);
		Element temHash = Utils.hash2Element(temStr, pairing);
		return temHash;
	}

	private RepMessage generateRep() throws IOException {
		RepMessage rep = generateSingleRep(sumCi);
		return rep;
	}

	private RepMessage generateSingleRep(BigInteger ci) throws IOException {
		long ti = System.currentTimeMillis();

		String temStr = ci.toString() + this.id + this.id + ti;
		temStr = Utils.sha256(temStr);
//		Element sigma = this.pairing.getG1().newElementFromBytes(Base64.decode(temStr));
		Element sigma =Utils.hash2Element(temStr, pairing);
		sigma = sigma.duplicate().mul(this.di);

		return new RepMessage(this.id, this.id, ci, sigma, ti);
	}

}
