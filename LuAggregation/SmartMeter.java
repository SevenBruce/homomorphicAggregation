import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import messages.EccParameters;
import messages.GeneratorInfo;
import messages.RegBack;
import messages.RegMessage;
import messages.RepMessage;

public class SmartMeter {

	private long id;
	private BigInteger di;
	private Element ri;
	private BigInteger order;
	private Element generator; 
	private Pairing pairing;

	private BigInteger n;
	private BigInteger nsquare;
	private BigInteger[] gg;
	private int bitLength;

	public SmartMeter(EccParameters pubInfo) throws IOException {
		super();
		this.id = Utils.randomlong();
		this.order = pubInfo.getPairing().getG1().getOrder();
		this.generator = pubInfo.getGenerator();
		this.di = Utils.randomBig(order);
		this.ri = this.generator.duplicate().mul(di);
		this.pairing = pubInfo.getPairing();
	}
	
	public void setGenerators(GeneratorInfo gen){
		this.n = gen.getN();
		this.gg = new BigInteger[gen.getGg().length];
		System.arraycopy(gen.getGg(), 0, this.gg, 0, this.gg.length);
		this.nsquare = gen.getNsquare();
		this.bitLength = gen.getBitLength();
	}

	public RegMessage genRegMesssage() {
		RegMessage reg = new RegMessage(this.id, this.ri);
		return reg;
	}

	public RepMessage genSingleRepMessage(int count) throws IOException {

		BigInteger mi, tem, c = BigInteger.ONE;
		for (int i = 0; i < count; i++) {
			mi = BigInteger.valueOf(Utils.randomInt());
			tem = gg[i].modPow(mi, nsquare);
			c = (c.multiply(tem)).mod(nsquare);
		}

		BigInteger r = new BigInteger(bitLength, new Random());
		BigInteger ci = c.multiply(r.modPow(n, nsquare)).mod(nsquare);

		return generateReportingMessageBasedOnCi(ci);
	}

	private RepMessage generateReportingMessageBasedOnCi(BigInteger ci) throws IOException {
		long ti = System.currentTimeMillis();

		String temStr = ci.toString() + this.id + this.id + ti;
		temStr = Utils.sha256(temStr);
//		Element sigma = this.pairing.getG1().newElementFromBytes(Base64.decode(temStr));
		Element sigma = Utils.hash2Element(temStr, pairing);
		sigma = sigma.duplicate().mul(this.di);
		
		return new RepMessage(this.id, this.id, ci, sigma, ti);
	}

}
