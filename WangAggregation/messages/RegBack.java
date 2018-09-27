package messages;

import it.unisa.dia.gas.jpbc.Element;

public class RegBack {
	
	private Element did;
	public RegBack(Element did) {
		super();
		this.did = did;
	}
	public Element getDid() {
		return did;
	}
	public void setDid(Element did) {
		this.did = did;
	}
}
