package messages;

import java.math.BigInteger;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class EccParameters2 {
	
	private Pairing pairing;
	private Element generator;
	private Element rx;

	
	private BigInteger n;
	private BigInteger nsquare;
	private BigInteger[] gg;
	private int bitLength;
	
	public EccParameters2(Pairing pairing, Element generator, Element rx, BigInteger n, BigInteger nsquare,BigInteger[] gg,
			int bitLength) {
		super();
		this.pairing = pairing;
		this.generator = generator;
		this.rx = rx;
		this.n = n;
		this.nsquare = nsquare;
		this.gg = new BigInteger[gg.length];
		System.arraycopy(gg, 0, this.gg, 0, gg.length);
		
		this.bitLength = bitLength;
	}
	
	public BigInteger[] getGg() {
		return gg;
	}
	public void setGg(BigInteger[] gg) {
		System.arraycopy(gg, 0, this.gg, 0, gg.length);
	}
	public Pairing getPairing() {
		return pairing;
	}
	public void setPairing(Pairing pairing) {
		this.pairing = pairing;
	}
	public Element getGenerator() {
		return generator;
	}
	public void setGenerator(Element generator) {
		this.generator = generator;
	}
	
	public Element getRx() {
		return rx;
	}

	public void setRx(Element rx) {
		this.rx = rx;
	}

	public BigInteger getN() {
		return n;
	}
	public void setN(BigInteger n) {
		this.n = n;
	}
	public BigInteger getNsquare() {
		return nsquare;
	}
	public void setNsquare(BigInteger nsquare) {
		this.nsquare = nsquare;
	}
	public int getBitLength() {
		return bitLength;
	}
	public void setBitLength(int bitLength) {
		this.bitLength = bitLength;
	}
	
	

	
	
}
