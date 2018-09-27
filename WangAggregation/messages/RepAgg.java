package messages;

import it.unisa.dia.gas.jpbc.Element;

public class RepAgg {
	private long id;
	private Element c1;
	private Element c2;
	private Element u;
	private Element v;
	private long ti;
	public RepAgg(long id, Element c1, Element c2, Element u, Element v, long ti) {
		super();
		this.id = id;
		this.c1 = c1;
		this.c2 = c2;
		this.u = u;
		this.v = v;
		this.ti = ti;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Element getC1() {
		return c1;
	}
	public void setC1(Element c1) {
		this.c1 = c1;
	}
	public Element getC2() {
		return c2;
	}
	public void setC2(Element c2) {
		this.c2 = c2;
	}
	public Element getU() {
		return u;
	}
	public void setU(Element u) {
		this.u = u;
	}
	public Element getV() {
		return v;
	}
	public void setV(Element v) {
		this.v = v;
	}
	public long getTi() {
		return ti;
	}
	public void setTi(long ti) {
		this.ti = ti;
	}
	

}
