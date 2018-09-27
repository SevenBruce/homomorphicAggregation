package messages;
import it.unisa.dia.gas.jpbc.Element;

public class RegMessage {
	
	private long id;
	private Element publicKey;
	public RegMessage(long id, Element publicKey) {
		super();
		this.id = id;
		this.publicKey = publicKey;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Element getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(Element publicKey) {
		this.publicKey = publicKey;
	}
	
}
