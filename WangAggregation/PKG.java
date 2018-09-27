import java.io.IOException;
import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import messages.PublicInfo;
import messages.RegBack;
import messages.RegMessage;

public class PKG {

	private Pairing pairing;
	private Element g;
	private Element gt;
	private BigInteger order;
	private BigInteger dx;
	private Element rx;
	private Element w;

	public PKG() {
		super();
		intializeEllipticCruve();
	}

	// initialization of the Elliptic Curve
	private void intializeEllipticCruve() {
		pairing = PairingFactory.getPairing("a256.properties");
		g = pairing.getG1().newRandomElement().getImmutable();
		this.dx = Utils.randomBig(pairing.getG1().getOrder());

		this.gt = pairing.getGT().newRandomElement().getImmutable();
		this.rx = this.g.duplicate().mul(this.dx);
		this.order = pairing.getG1().getOrder();
	}

	public RegBack getRegMessage(RegMessage reg) throws IOException {
		String temStr = Utils.sha256(Long.toString(reg.getId()));
		Element rid = Utils.hash2Element(temStr, pairing);
		Element did = rid.pow(this.dx);
//		System.out.println("ididdidi " + reg.getId());
//		System.out.println("rid kgc kgc " + rid);
//		System.out.println("did kgc kgc " + did);
		return new RegBack(did);

	}

	public PublicInfo publishPublicInfo(long id) throws IOException {
		String temStr = Utils.sha256(Long.toString(id));
		Element rid = Utils.hash2Element(temStr, pairing);
		this.w = pairing.pairing(rid, rx);
		//public PublicInfo(Pairing pairing, Element g, Element gt, Element rx, Element ww)
		PublicInfo pi = new PublicInfo(pairing, g, gt,rx, w);
		return pi;
	}

}
