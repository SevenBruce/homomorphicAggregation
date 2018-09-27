import java.io.IOException;
import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import messages.ECC_Parameters;
import messages.RegBack;
import messages.RegMessage;

public class KGC {
	
	private Pairing pairing;
	private Element generator;
	private BigInteger dx;
	private Element rx;
	
	
	public KGC() {
		super();
		intializeEllipticCruve();
	}
	
	public ECC_Parameters publishECC_Parameters() {
		ECC_Parameters pi = new ECC_Parameters(this.pairing, this.generator, this.rx);
		return pi;
	}

	// initialization of the Elliptic Curve
	private void intializeEllipticCruve() {
		pairing = PairingFactory.getPairing("a256.properties");
		generator = pairing.getG1().newRandomElement().getImmutable();
		this.dx = Utils.randomBig(pairing.getG1().getOrder());
		this.rx = this.generator.duplicate().mul(this.dx);
	}
	
	public RegBack getRegMessage(RegMessage reg) throws IOException {
		String hash = Utils.sha256(Long.toString(reg.getId()) + Long.toString(reg.getT()));
		if (hash.equals(reg.getHash())) {
			
			byte[] temByte = Utils.sha2561(Long.toString(reg.getId()));
			Element rid = pairing.getG1().newElementFromHash(temByte,0,temByte.length);
			Element did = rid.duplicate().mul(this.dx);
			
			String temHash = Utils.sha256(Long.toString(reg.getId()) + did.toString());
			return new RegBack(did, temHash);
		} else {
			System.out.println("reg failed!");
		}
		return null;
	}

}
