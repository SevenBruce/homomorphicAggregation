package messages;

import it.unisa.dia.gas.jpbc.Element;

public class RepMessage {
	private long id;
	private Element c1i;
	private Element c2i;
	private Element vi;
	private long ti;
	public RepMessage(long id, Element c1i, Element c2i, Element vi, long ti) {
		super();
		this.id = id;
		this.c1i = c1i;
		this.c2i = c2i;
		this.vi = vi;
		this.ti = ti;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Element getC1i() {
		return c1i;
	}
	public void setC1i(Element c1i) {
		this.c1i = c1i;
	}
	public Element getC2i() {
		return c2i;
	}
	public void setC2i(Element c2i) {
		this.c2i = c2i;
	}
	public Element getVi() {
		return vi;
	}
	public void setVi(Element vi) {
		this.vi = vi;
	}
	public long getTi() {
		return ti;
	}
	public void setTi(long ti) {
		this.ti = ti;
	}
	

}
