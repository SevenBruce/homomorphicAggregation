import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import messages.ECC_Parameters;
import messages.RegBack;
import messages.RegMessage;
import messages.RepMessage;

public class Agg {

	private ArrayList<RepMessage> alRep = new ArrayList<RepMessage>();

	private long id;
	private Pairing pairing;
	private Element generator;
	private Element did;
	private Element rid;
	private long serverId;

	private Element rx;
	private BigInteger order;
	private BigInteger sumCi;
	private BigInteger[] groupSumCi;
	
	private BigInteger nsquare;

	public Agg(ECC_Parameters ps) throws IOException {
		super();
		this.id = Utils.randomlong();
		this.pairing = ps.getPairing();
		this.rid = Utils.hash2Element(this.id, pairing);

		this.generator = ps.getGenerator();
		this.rx = ps.getRx();
		this.order = pairing.getG1().getOrder();
		this.sumCi = BigInteger.ONE;
	}

	public RegMessage genRegMesssage() {
		Long t = System.currentTimeMillis();
		String hash = Utils.sha256(Long.toString(this.id) + Long.toString(t));
		RegMessage reg = new RegMessage(this.id, t, hash);
		return reg;
	}

	public void getRegBack(RegBack back) {
		if (null == back) {
			System.out.println("Reg failed");
			return;
		}
		String hash = Utils.sha256(Long.toString(this.id) + back.getDid().toString());
		if (hash.equals(back.getHash())) {
			this.did = back.getDid();
		}
	}

	public RepMessage getRepMessage(RepMessage rep) throws IOException {
		alRep.add(rep);
		
		if (alRep.size() < PublicParams.METERS_NUM)
			return null;

		if (checkingIncomeMessage() == false) {
			System.out.println("check failed at the agg side");
			return null;
		}
		return generateRep();
	}

	public void setServerId(long id) {
		this.serverId = id;
	}
	
	public void setNsquare(BigInteger nsquare) {
		this.nsquare = nsquare;
	}

	public RepMessage[] getRepMessageForANOVA(RepMessage rep) throws IOException {
		alRep.add(rep);
		
		if (alRep.size() < PublicParams.ANOVA_GROUP_SIZE) {
			return null;
		}
		if (checkingIncomeMessageForANOVA() == false) {
			return null;
		}
		
		sortDataIntoGroupsANOVA();
		return generateRepForANOVA();
	}

	public void clearReportMessage() throws IOException {
		alRep.clear();
	}

	// private boolean signCheckAndSortDataIntoGroupsANOVA() throws IOException
	// {
	// Iterator<RepMessage> itRep = alRep.iterator();
	// groupSumCi = new BigInteger[PublicParams.ANOVA_GROUP_NUMBER];
	// for (int i = 0; i < groupSumCi.length; i++) {
	// groupSumCi[i] = BigInteger.ONE;
	// }
	// int groupCount[] = new int[PublicParams.ANOVA_GROUP_NUMBER];
	// int cout = 0;
	// int groupSize = alRep.size() / PublicParams.ANOVA_GROUP_NUMBER;
	//
	// Element sumV = pairing.getG1().newOneElement();
	// Element sumUp = pairing.getG1().newOneElement();
	//
	// while (itRep.hasNext()) {
	//
	// RepMessage rep = itRep.next();
	// sumV = sumV.duplicate().mul(rep.getV());
	// BigInteger temH1 = Utils.hash2Big(rep.getCi().toString() +
	// rep.getU().toString()).mod(this.order);
	// Element rid =
	// pairing.getG1().newElementFromBytes(Base64.decode(Utils.sha256(Long.toString(rep.getId()))));
	// Element up = rep.getU().duplicate().add(rid.mul(temH1));
	// sumUp = sumUp.duplicate().mul(up);
	//
	// cout = selectGroupNumberForReportingData(groupCount, groupSize);
	// // cout = selectGroupNumberForReportingDataTest(groupCount,
	// // groupSize);
	// groupSumCi[cout] = groupSumCi[cout].multiply(rep.getCi());
	// groupCount[cout]++;
	// }
	//
	// Element left = pairing.pairing(this.generator, sumV);
	// Element right = pairing.pairing(this.rx, sumUp);
	// if (left.equals(right))
	// return true;
	// return false;
	// }

	private void sortDataIntoGroupsANOVA() {
		Iterator<RepMessage> itRep = alRep.iterator();
		
		groupSumCi = new BigInteger[PublicParams.ANOVA_GROUP_NUMBER];
		Arrays.fill(groupSumCi, BigInteger.ONE);
		
		int groupCount[] = new int[PublicParams.ANOVA_GROUP_NUMBER];
		int cout = 0;
		int groupSize = alRep.size() / PublicParams.ANOVA_GROUP_NUMBER;

		while (itRep.hasNext()) {
			RepMessage rep = itRep.next();
			// cout = selectGroupNumberForReportingData(groupCount, groupSize);
			cout = selectGroupNumberForReportingDataTest(groupCount, groupSize);
			groupSumCi[cout] = groupSumCi[cout].multiply(rep.getCi());
			groupCount[cout]++;
		}
	}

	private int selectGroupNumberForReportingData(int groupCount[], int groupSize) {
		int k = Utils.randomInt() % PublicParams.ANOVA_GROUP_NUMBER;
		while (groupCount[k] >= groupSize) {
			k = (k + 1) % PublicParams.ANOVA_GROUP_NUMBER;
		}
		return k;
	}

	private int selectGroupNumberForReportingDataTest(int groupCount[], int groupSize) {
		int k = 0;
		while (groupCount[k] >= groupSize) {
			k = (k + 1) % PublicParams.ANOVA_GROUP_NUMBER;
		}
		return k;
	}

	private boolean checkingIncomeMessage() throws IOException {
		Iterator<RepMessage> itRep = alRep.iterator();

		sumCi = BigInteger.ONE;
		while (itRep.hasNext()) {
			RepMessage rep = itRep.next();
			sumCi = (sumCi.multiply(rep.getCi())).mod(this.nsquare);
			
			Element ri = Utils.hash2Element(rep.getId(), pairing);
			Element kij = pairing.pairing(ri, this.did);

			String temStr = rep.getCi().toString() + rep.getId() + rep.getTi() + kij.toString();
			String v = Utils.sha256(temStr);

			if (!v.equals(rep.getV()))
				return false;
		}
		return true;
	}
	
	private boolean checkingIncomeMessageForANOVA() throws IOException {
		Iterator<RepMessage> itRep = alRep.iterator();

		while (itRep.hasNext()) {
			RepMessage rep = itRep.next();
			
			Element ri = Utils.hash2Element(rep.getId(), pairing);
			Element kij = pairing.pairing(ri, this.did);

			String temStr = rep.getCi().toString() + rep.getId() + rep.getTi() + kij.toString();
			String v = Utils.sha256(temStr);

			if (!v.equals(rep.getV()))
				return false;
		}
		return true;
	}

	private RepMessage generateRep() throws IOException {
		RepMessage rep = generateSingleRep(sumCi);
		return rep;
	}

	private RepMessage[] generateRepForANOVA() throws IOException {
		RepMessage[] rep = new RepMessage[PublicParams.ANOVA_GROUP_NUMBER];
		for (int i = 0; i < rep.length; i++) {
			rep[i] = generateSingleRep(groupSumCi[i]);
		}
		return rep;
	}

	private RepMessage generateSingleRep(BigInteger ci) throws IOException {
		long ti = System.currentTimeMillis();

		Element rj = Utils.hash2Element(this.serverId, pairing);
		Element kij = pairing.pairing(this.did, rj);

		String temStr = ci.toString() + Long.toString(this.id) + ti + kij.toString();
		String v = Utils.sha256(temStr);

		return new RepMessage(this.id, ci, v, ti);
	}

	public long getId() {
		return this.id;
	}

}
