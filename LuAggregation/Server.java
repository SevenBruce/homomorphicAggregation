import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;

import messages.EccParameters;
import messages.GeneratorInfo;
import messages.RegBack;
import messages.RegMessage;
import messages.RepMessage;

public class Server {
	private ArrayList<Long> alMeterIdentities = new ArrayList<Long>();
	private ArrayList<Element> alMeterPublicKeys = new ArrayList<Element>();

	/**
	 * p and q are two large primes. lambda = lcm(p-1, q-1) =
	 * (p-1)*(q-1)/gcd(p-1, q-1).
	 */
	private BigInteger p, q, lambda;
	/**
	 * n = p*q, where p and q are two large primes.
	 */
	public BigInteger n;
	/**
	 * nsquare = n*n
	 */
	public BigInteger nsquare;
	/**
	 * a random integer in Z*_{n^2} where gcd (L(g^lambda mod n^2), n) = 1.
	 */
	private BigInteger g;
	/**
	 * number of bits of modulus
	 */
	private int bitLength;

	private Pairing pairing;
	private Element generator;
	private BigInteger order;
	private BigInteger s;
	private Element rx;
	BigInteger[] gg;
	BigInteger[] a;

	public Server() {
		// multiple data report without square
		super();
		intializeEllipticCruve();
		KeyGeneration(1024, 64);
//		initializeVectorA();
//		initializeGeneratorArray();
	}

	/**
	 * Sets up the public key and private key.
	 * 
	 * @param bitLengthVal
	 *            number of bits of modulus.
	 * @param certainty
	 *            The probability that the new BigInteger represents a prime
	 *            number will exceed (1 - 2^(-certainty)). The execution time of
	 *            this constructor is proportional to the value of this
	 *            parameter.
	 */
	public void KeyGeneration(int bitLengthVal, int certainty) {
		bitLength = bitLengthVal;
		/*
		 * Constructs two randomly generated positive BigIntegers that are
		 * probably prime, with the specified bitLength and certainty.
		 */
		p = new BigInteger(bitLength / 2, certainty, new Random());
		q = new BigInteger(bitLength / 2, certainty, new Random());

		n = p.multiply(q);
		nsquare = n.multiply(n);

		g = new BigInteger("2");
		lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
				.divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
		/* check whether g is good. */
		if (g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).gcd(n).intValue() != 1) {
			System.out.println("g is not good. Choose g again.");
			System.exit(1);
		}
	}

	/**
	 * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function
	 * automatically generates random input r (to help with encryption).
	 * 
	 * @param m
	 *            plaintext as a BigInteger
	 * @return ciphertext as a BigInteger
	 */
	public BigInteger Encryption(BigInteger m) {
		BigInteger r = new BigInteger(bitLength, new Random());
		return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);
	}

	/**
	 * Decrypts ciphertext c. plaintext m = L(c^lambda mod n^2) * u mod n, where
	 * u = (L(g^lambda mod n^2))^(-1) mod n.
	 * 
	 * @param c
	 *            ciphertext as a BigInteger
	 * @return plaintext as a BigInteger
	 */
	public GeneratorInfo initializeGenerators() {
		initializeVectorA();
		initializeGeneratorArray();
		GeneratorInfo gen = new GeneratorInfo(this.n , this.nsquare, this.gg, this.bitLength);
		return gen;
	}

	
	/**
	 * Decrypts ciphertext c. plaintext m = L(c^lambda mod n^2) * u mod n, where
	 * u = (L(g^lambda mod n^2))^(-1) mod n.
	 * 
	 * @param c
	 *            ciphertext as a BigInteger
	 * @return plaintext as a BigInteger
	 */
	public BigInteger Decryption(BigInteger c) {
		BigInteger u = g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).modInverse(n);
		return c.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);

	}

	private void printDifferentData(BigInteger m) {
		BigInteger[] out = new BigInteger[gg.length];
		BigInteger[] xl = new BigInteger[gg.length];
		xl[gg.length - 1] = m;

		for (int j = gg.length - 1; j > 0; j--) {
			xl[j - 1] = xl[j].mod(a[j]);
			out[j] = (xl[j].subtract(xl[j - 1])).divide(a[j]);
		}
		out[0] = xl[0];
//		printRestult(out);
	}

	private void printRestult(BigInteger[] out) {
		for (int j = 0; j < out.length; j++) {
			System.out.println(" out[ " + j + " ] : " + out[j]);
		}
	}

	private void initializeVectorA() {
		a = new BigInteger[PublicParams.NUMBER_OF_REPORTING_DATA_TYPE];
		for (int i = 0; i < a.length; i++) {
			a[i] = BigInteger.ZERO;
		}
		a[0] = BigInteger.ONE;

		if (PublicParams.NUMBER_OF_REPORTING_DATA_TYPE < 2)
			return;

		a[1] = Utils.randomBig(bitLength / 2);
		if (PublicParams.NUMBER_OF_REPORTING_DATA_TYPE < 3)
			return;

		// the first half of the array using the SINGLE_METER_REPROTING_RANGE
		for (int i = 2; i < PublicParams.NUMBER_OF_REPORTING_DATA_TYPE; i++) {
			a[i] = aSum().add(BigInteger.ONE);
		}
	}

	private void initializeGeneratorArray() {
		gg = new BigInteger[a.length];
		for (int i = 0; i < a.length; i++) {
			gg[i] = g.modPow(a[i], nsquare);
		}
	}

	private BigInteger aSum() {
		BigInteger sum = BigInteger.ZERO;
		for (BigInteger x : a) {
			sum = sum.add(x.multiply(BigInteger.valueOf(PublicParams.METERS_NUM))
					.multiply(BigInteger.valueOf(PublicParams.SINGLE_METER_REPROTING_RANGE)));
		}
		return sum;
	}

	private boolean isSumOK() {
		BigInteger sum = BigInteger.ZERO;
		for (BigInteger x : a) {
			sum = sum.add(x.multiply(BigInteger.valueOf(PublicParams.METERS_NUM))
					.multiply(BigInteger.valueOf(PublicParams.SINGLE_METER_REPROTING_RANGE)));
		}
		if (sum.compareTo(n) < 0) {
			return true;
		}
		return false;
	}

	// initialization of the Elliptic Curve
	private void intializeEllipticCruve() {
		pairing = PairingFactory.getPairing("a256.properties");
		generator = pairing.getG1().newRandomElement().getImmutable();
		this.s = Utils.randomBig(pairing.getG1().getOrder());
		this.rx = this.generator.duplicate().mul(this.s);
		this.order = pairing.getG1().getOrder();
	}

	public EccParameters publishEccParameters() {
		EccParameters pi = new EccParameters(this.pairing, this.generator, this.rx);
		return pi;
	}

	public void getRegMessage(RegMessage reg) {
		alMeterIdentities.add(reg.getId());
		alMeterPublicKeys.add(reg.getPublicKey());
	}

	public void getRepMessage(RepMessage rep) throws IOException {
		if (null == rep) {
			return;
		}
		
		if (signCheck(rep) == true) {
			// System.out.println("message checked at Server side. ");
			BigInteger res = Decryption(rep.getCi());
//			System.out.println("reproting data is : " + res);
			printDifferentData(res);
		} else {
			System.out.println("agg check failed");
		}
	}

	private boolean signCheck(RepMessage rep) throws IOException {
		Element right = pairing.getGT().newOneElement();
		
		right = pairing.pairing(getPublicKeyById(rep.getId()), getHash2Element(rep));
		Element left = pairing.pairing(this.generator, rep.getSigma());
		
		if (left.equals(right))
			return true;
		return false;
	}

	private Element getPublicKeyById(long id) {
		int index = alMeterIdentities.indexOf(id);
		return alMeterPublicKeys.get(index);
	}

	private Element getHash2Element(RepMessage rep) throws IOException {
		String temStr = rep.getCi().toString() + rep.getId() + rep.getId() + rep.getTi();
		temStr = Utils.sha256(temStr);
//		Element temHash = this.pairing.getG1().newElementFromBytes(Base64.decode(temStr));
		Element temHash = Utils.hash2Element(temStr, pairing);
		return temHash;
	}

}
