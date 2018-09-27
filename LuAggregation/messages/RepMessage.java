package messages;

import java.math.BigInteger;
import it.unisa.dia.gas.jpbc.Element;

public class RepMessage {
	private long id;
	private long ra;
	private BigInteger ci;
	private Element sigma;
	private long ti;
	
	public RepMessage(long id, long ra, BigInteger ci, Element sigma, long ti) {
		super();
		this.id = id;
		this.ra = ra;
		this.ci = ci;
		this.sigma = sigma;
		this.ti = ti;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getRa() {
		return ra;
	}
	public void setRa(long ra) {
		this.ra = ra;
	}
	public BigInteger getCi() {
		return ci;
	}
	public void setCi(BigInteger ci) {
		this.ci = ci;
	}
	public Element getSigma() {
		return sigma;
	}
	public void setSigma(Element sigma) {
		this.sigma = sigma;
	}
	public long getTi() {
		return ti;
	}
	public void setTi(long ti) {
		this.ti = ti;
	}
	
	
	

}
