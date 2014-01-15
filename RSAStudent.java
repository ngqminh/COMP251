import HW4.*;

//DO NOT IMPORT BigInteger (or any other implementation of BigInteger) from the standard java library or some other place. 
//ONLY use BigInt as provided in the HW4 package.

public class RSAStudent implements RSAInterface {

	/**
	 * Main function: please modify for your own testing purposes. We will use a
	 * different one for grading Feel free to play with larger prime numbers!!!
	 */
	public static void main(String[] args) {
		RSAStudent rsa = new RSAStudent();
		BigInt p = new BigInt("5333");
		BigInt q = new BigInt("6101");
		Pair<PublicKey, PrivateKey> keys;

		keys = rsa.generateKey(p, q);

		// IF YOU CAN'T GET generateKey TO WORK, USE THE FOLLOWING AS YOUR KEYS
		// keys = new Pair<PublicKey, PrivateKey>
		// ( new PublicKey(new BigInt("32536633"), new BigInt("3")),
		// new PrivateKey(new BigInt("21683467")));
		// ...TO HERE

		BigInt[] E = rsa.encryptASCII("smile because it happened!", keys.fst);
		System.out.println("Don\'t cry because it\'s over..."
				+ rsa.decryptASCII(E, keys.fst, keys.snd));
		System.out.println();

		E = rsa.encryptASCII("depending how far beyond zebra you go.", keys.fst);
		System.out.println("There\'s no limit to how much you'll know..."
				+ rsa.decryptASCII(E, keys.fst, rsa.hackKey(keys.fst)));
	}

	/**
	 * Will encrypt the ASCII code of each character in a given String
	 * 
	 * @param pMessage
	 *            : the Message to encrypt
	 * @param pPublic
	 *            : the public key to use for encryption
	 * @return : element at position i represents the encryption of character at
	 *         position i in the input string
	 */
	public BigInt[] encryptASCII(String pMessage, PublicKey pPublic) {
		BigInt[] toRet = new BigInt[pMessage.length()];
		for (int i = 0; i < toRet.length; i++) {
			toRet[i] = encrypt(new BigInt((int) pMessage.charAt(i)), pPublic);
		}
		return toRet;
	}

	/**
	 * Will decrypt a list of integers to ASCII code of each character, and then
	 * convert it to a String
	 * 
	 * @param pEncryptedMessage
	 *            : the list of encrypted ASCII codes
	 * @param pPublic
	 *            : the public key that was used to encrypt the message
	 * @param pPrivate
	 *            : the private key used by the receiver for decryption
	 * @return the String encrypted in the input list
	 */
	public String decryptASCII(BigInt[] pEncryptedMessage, PublicKey pPublic,
			PrivateKey pPrivate) {
		String s = "";
		for (int i = 0; i < pEncryptedMessage.length; i++) {
			BigInt val = decrypt(pEncryptedMessage[i], pPublic, pPrivate);
			s += val.toAscii();
		}
		return s;
	}

	public BigInt encrypt(BigInt pMessage, PublicKey pPublic) {
		BigInt encrypted = fastModularExpo(pMessage, pPublic.exponent,
				pPublic.modulus);
		return encrypted;
	}

	public BigInt decrypt(BigInt pEncryptedMessage, PublicKey pPublic,
			PrivateKey pPrivate) {
		BigInt message = fastModularExpo(pEncryptedMessage, pPrivate.key,
				pPublic.modulus);
		return message;
	}

	public PrivateKey hackKey(PublicKey pPublic) {
		BigInt one = new BigInt(1);
		BigInt guessD = new BigInt(0);
		BigInt guessP = new BigInt(2);
		BigInt guessQ;
		boolean hacked = false;
		while (!hacked) {
			guessQ = pPublic.modulus.div(guessP);
			BigInt guessPhiN = (guessP.subtract(one)).multiply(guessQ
					.subtract(one));
			// we keep going until we find p and q primes, and Phi(n) relatively
			// prime to e
			// the last condition is to ensure no errors occur because of integer division
			while (!isPrime(guessP) || !isPrime(guessQ) || !guessPhiN.gcd(pPublic.exponent).equals(one) || !guessP.multiply(guessQ).equals(pPublic.modulus)) {
				guessP.increment();
				if (isPrime(guessP)) {
					guessQ = pPublic.modulus.div(guessP);
					if (guessP.multiply(guessQ).equals(pPublic.modulus)) {
						if (isPrime(guessQ)) {
							guessPhiN = (guessP.subtract(one)).multiply(guessQ
									.subtract(one));
						}

					}

				}
			}
			guessD = pPublic.exponent.modInverse(guessPhiN);
			if ((pPublic.exponent.multiply(guessD)).mod(guessPhiN).equals(one)) {
				hacked = true;
			}
		}
		PrivateKey pvtGuess = new PrivateKey(guessD);
		return pvtGuess;
	}

	public Pair<PublicKey, PrivateKey> generateKey(BigInt pPrimeNo1,
			BigInt pPrimeNo2) {
		BigInt one = new BigInt(1);

		// n = p * q, this is the modulus
		BigInt n = pPrimeNo1.multiply(pPrimeNo2);

		// for the exponent e, we find a small odd integer that is relatively
		// prime to phi(n)
		// phi(n) = (p - 1)(q - 1)
		BigInt phiN = (pPrimeNo1.subtract(one)).multiply(pPrimeNo2
				.subtract(one));
		// can't start at 1 because 1 is relatively prime to every number
		int i = 3;
		boolean found = false;
		while (!found) {
			BigInt I = new BigInt(i);
			if (phiN.gcd(I).equals(one)) {
				found = true;
			} else {
				i += 2;
			}
		}
		BigInt e = new BigInt(i);

		// d = multiplicative inverse of e, mod phi(n)
		BigInt d = e.modInverse(phiN);
		PublicKey pub = new PublicKey(n, e);
		PrivateKey pvt = new PrivateKey(d);
		Pair<PublicKey, PrivateKey> keys = new Pair<PublicKey, PrivateKey>(pub,
				pvt);
		return keys;
	}

	public BigInt fastModularExpo(BigInt pBase, BigInt pExponent,
			BigInt pModulus) {
		BigInt zero = new BigInt(0);
		BigInt one = new BigInt(1);
		BigInt two = new BigInt(2);
		BigInt n = new BigInt(1);
		while (!pExponent.equals(zero)) {
			// if exp mod 2 = 1, then exponent is odd
			if (pExponent.gcd(two).equals(one)) {
				// n = n * base (mod m)
				n = n.multiply(pBase);
				n = n.mod(pModulus);
				// power = power - 1
				pExponent = pExponent.subtract(one);
			} else {
				// base = base^2 (mod m)
				pBase = pBase.multiply(pBase);
				pBase = pBase.mod(pModulus);
				// power = power / 2
				pExponent = pExponent.half();
			}

		}
		return n;
	}

	public boolean isPrime(BigInt p) {
		BigInt one = new BigInt(1);
		BigInt zero = new BigInt(0);
		if (p.equals(zero) || p.equals(one)) {
			return false;
		}
		for (int i = 0; i < 5; i++) {
			// if at least one of x^(p-1) != 1 mod p then it is not a prime
			BigInt rand = BigInt.generateRandom(p);
			if (!(fastModularExpo(rand, p.subtract(one), p).equals(one))) {
				return false;
			}
		}
		return true;
	}



}

