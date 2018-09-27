package messages;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class PublicInfo {
	
	private Pairing pairing;
	private Element g;
	private Element gt;
	private Element rx;
	private Element ww;
	
	public PublicInfo(Pairing pairing, Element g, Element gt, Element rx, Element ww) {
		super();
		this.pairing = pairing;
		this.g = g;
		this.gt = gt;
		this.rx = rx;
		this.ww = ww;
	}
	public Pairing getPairing() {
		return pairing;
	}
	public void setPairing(Pairing pairing) {
		this.pairing = pairing;
	}
	public Element getG() {
		return g;
	}
	public void setG(Element g) {
		this.g = g;
	}
	public Element getRx() {
		return rx;
	}
	public void setRx(Element rx) {
		this.rx = rx;
	}
	public Element getWw() {
		return ww;
	}
	public void setWw(Element ww) {
		this.ww = ww;
	}
	public Element getGt() {
		return gt;
	}
	public void setGt(Element gt) {
		this.gt = gt;
	}
	
}
