import java.io.IOException;
import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.Point;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import messages.PublicInfo;
import messages.RegBack;
import messages.RegMessage;
import messages.RepAgg;
import messages.RepMessage;

public class Server {
	private Pairing pairing;
	private Element g;
	private Element gt;
	private Element rid;
	private BigInteger order;
	private Element rx;
	private long id;
	private Element did;

	public Server() throws IOException {
		// multiple data report with square
		super();
		this.id = Utils.randomlong();
	}

	public void getPublicInfo(PublicInfo pubInfo) throws IOException {
		this.pairing = pubInfo.getPairing();
		String temStr = Utils.sha256(Long.toString(this.id));
		this.rid = Utils.hash2Element(temStr, pairing);
		
		this.order = pubInfo.getPairing().getG1().getOrder();
		this.g = pubInfo.getG();
		this.gt = pubInfo.getGt();
		this.rx = pubInfo.getRx();
	}

	public long getIdentity() {
		return this.id;
	}

	public RegMessage genRegMesssage() {
		RegMessage reg = new RegMessage(this.id);
		return reg;
	}

	public void getRegBack(RegBack back) {
		if (null == back) {
			System.out.println("Reg failed");
		}
		this.did = back.getDid();
	}

	public void getRepMessage(RepAgg rep) throws IOException {
		if (null == rep) {
			return;
		}
		if (signCheck(rep) == false)
			System.out.println("agg check failed");

		Element temDivide = pairing.pairing(this.did, rep.getC1());
		Element m = rep.getC2().div(temDivide);
		int r = longKangaroo(gt, m);
//		System.out.println("reporting data is " + r);
	}

	private boolean signCheck(RepAgg rep) throws IOException {
		Element left = pairing.pairing(this.g, rep.getV());
		Element right2 = pairing.pairing(rep.getU(), hash2ElementG2(rep.getC2(), rep.getTi()));
		Element right1 = pairing.pairing(this.rx, getPublicKeyById(rep.getId()));

		Element right = right2.duplicate().mul(right1);

		if (left.equals(right)){
			return true;
		}
		
		System.out.println("why not true here?");
		return false;
	}

	private Element getPublicKeyById(long id) throws IOException {
		String temStr = Utils.sha256(Long.toString(id));
		return Utils.hash2Element(temStr, pairing);
	}

	private Element hash2ElementG2(Element c2i, long ti) throws IOException {
		String temStr = Utils.sha256(c2i.toString() + ti);
		return Utils.hash2Element(temStr, pairing);
	}

	private static BigInteger LIMIT = BigInteger.valueOf(PublicParams.REPORT_UPBOUND_LIMIT);
	private static BigInteger LEAPS = BigInteger.valueOf(346);
	private static BigInteger LEAPS_DIVIDE = BigInteger.valueOf(4);

	// kangaroo in the group G1
	public static int longKangaroo(Element generator, Element num) {
		/*
		 * Pollard's lambda algorithm for finding discrete logs * which are
		 * known to be less than a certain limit LIMIT
		 */
		int kanResult = -1;
		Element trap, x;
		Element Num = num;
		Element[] table = new Element[32];
		int i, j, m;
		BigInteger dm, dn, s;
		BigInteger[] distance = new BigInteger[32];

		for (s = BigInteger.ONE, m = 1;; m++) {
			distance[m - 1] = s;
			s = s.add(s);
			BigInteger aLEAP = LEAPS.divide(LEAPS_DIVIDE);
			if ((s.add(s)).divide(BigInteger.valueOf(m)).compareTo(aLEAP) > 0)
				break;
		}

		for (i = 0; i < m; i++) {
			/* create table */
			table[i] = generator.pow(distance[i]);
			// System.out.println("trap failed... : " + table[i]);
		}

		x = generator.pow(LIMIT);
		// System.out.println("setting trap..." + m);
		for (dn = BigInteger.ZERO, j = 0; j < LEAPS.intValue(); j++) {
			/* set traps beyond LIMIT using tame kangaroo */
			try {
				i = ((((Point) x).getX().toBigInteger().intValue())) % m; /* random function */
			} catch (Exception e) {
				i = 0;
			}
			x = x.mul(table[i]);
			dn = dn.add(distance[i]);
		}

		trap = x;
		// Random randomWild = new Random(System.currentTimeMillis());
		for (dm = BigInteger.ZERO;;) {
			try {
				i = ((((Point) x).getX().toBigInteger().intValue())) % m; /* random function */
			} catch (Exception e) {
				i = 0;
			}
			Num = Num.mul(table[i]);
			dm = dm.add(distance[i]);

			if (Num.equals(trap))
				break;
			if (dm.compareTo(LIMIT.add(dn)) > 0)
				break;
		}
		if (dm.compareTo(LIMIT.add(dn)) > 0) { /* trap stepped over */
			// System.out.println("trap failed... : " + dm);
			return kanResult;
		}

		kanResult = (LIMIT.add(dn).subtract(dm)).intValue();
		return kanResult;
	}

	public static BigInteger sqrt(BigInteger x) {
		BigInteger div = BigInteger.ZERO.setBit(x.bitLength() / 2);
		BigInteger div2 = div;
		// Loop until we hit the same value twice in a row, or wind
		// up alternating.
		for (;;) {
			BigInteger y = div.add(x.divide(div)).shiftRight(1);
			if (y.equals(div) || y.equals(div2))
				return y;
			div2 = div;
			div = y;
		}
	}

}
