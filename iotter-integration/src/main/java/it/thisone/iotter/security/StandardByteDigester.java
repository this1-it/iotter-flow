package it.thisone.iotter.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

/**
 * <p>
 * Standard implementation of the {@link ByteDigester} interface. This class
 * lets the user specify the algorithm (and provider) to be used for creating
 * digests, the size of the salt to be applied, the number of times the hash
 * function will be applied (iterations) and the salt generator to be used.
 * </p>
 * <p>
 * This class is <i>thread-safe</i>.
 * </p>
 * <p>
 * <br/>
 * <b><u>Configuration</u></b>
 * </p>
 * <p>
 * The algorithm, provider, salt size iterations and salt generator can take
 * values in any of these ways:
 * <ul>
 * <li>Using its default values.</li>
 * <li>Setting a <tt>{@link org.jasypt.digest.config.DigesterConfig}</tt> object
 * which provides new configuration values.</li>
 * <li>Calling the corresponding <tt>setAlgorithm(...)</tt>,
 * <tt>setProvider(...)</tt>, <tt>setProviderName(...)</tt>,
 * <tt>setSaltSizeBytes(...)</tt>, <tt>setIterations(...)</tt> or
 * <tt>setSaltGenerator(...)</tt> methods.</li>
 * </ul>
 * And the actual values to be used for initialization will be established by
 * applying the following priorities:
 * <ol>
 * <li>First, the default values are considered.</li>
 * <li>Then, if a <tt>{@link org.jasypt.digest.config.DigesterConfig}</tt>
 * object has been set with <tt>setConfig</tt>, the non-null values returned by
 * its <tt>getX</tt> methods override the default values.</li>
 * <li>Finally, if the corresponding <tt>setX</tt> method has been called on the
 * digester itself for any of the configuration parameters, the values set by
 * these calls override all of the above.</li>
 * </ol>
 * </p>
 * 
 * <p>
 * <br/>
 * <b><u>Initialization</u></b>
 * </p>
 * <p>
 * Before it is ready to create digests, an object of this class has to be
 * <i>initialized</i>. Initialization happens:
 * <ul>
 * <li>When <tt>initialize</tt> is called.</li>
 * <li>When <tt>digest</tt> or <tt>matches</tt> are called for the first time,
 * if <tt>initialize</tt> has not been called before.</li>
 * </ul>
 * Once a digester has been initialized, trying to change its configuration
 * (algorithm, provider, salt size, iterations or salt generator) will result in
 * an <tt>AlreadyInitializedException</tt> being thrown.
 * </p>
 * 
 * <p>
 * <br/>
 * <b><u>Usage</u></b>
 * </p>
 * <p>
 * A digester may be used in two different ways:
 * <ul>
 * <li>For <i>creating digests</i>, by calling the <tt>digest</tt> method.</li>
 * <li>For <i>matching digests</i>, this is, checking whether a digest
 * corresponds adequately to a digest (as in password checking) or not, by
 * calling the <tt>matches</tt> method.</li>
 * </ul>
 * The steps taken for creating digests are:
 * <ol>
 * <li>A salt of the specified size is generated (see {@link SaltGenerator}). If
 * salt size is zero, no salt will be used.</li>
 * <li>The salt bytes are added to the message.</li>
 * <li>The hash function is applied to the salt and message altogether, and then
 * to the results of the function itself, as many times as specified
 * (iterations).</li>
 * <li>If specified by the salt generator (see
 * {@link org.jasypt.salt.SaltGenerator#includePlainSaltInEncryptionResults()}),
 * the <i>undigested</i> salt and the final result of the hash function are
 * concatenated and returned as a result.</li>
 * </ol>
 * Put schematically in bytes:
 * <ul>
 * <li>
 * DIGEST =
 * <tt>|<b>S</b>|..(ssb)..|<b>S</b>|<b>X</b>|<b>X</b>|<b>X</b>|...|<b>X</b>|</tt>
 * <ul>
 * <li><tt><b>S</b></tt>: salt bytes (plain, not digested). <i>(OPTIONAL)</i>.</li>
 * <li><tt>ssb</tt>: salt size in bytes.</li>
 * <li><tt><b>X</b></tt>: bytes resulting from hashing (see below).</li>
 * </ul>
 * </li>
 * <li>
 * <tt>|<b>X</b>|<b>X</b>|<b>X</b>|...|<b>X</b>|</tt> =
 * <tt><i>H</i>(<i>H</i>(<i>H</i>(..(it)..<i>H</i>(<b>Z</b>|<b>Z</b>|<b>Z</b>|...|<b>Z</b>|))))</tt>
 * <ul>
 * <li><tt><i>H</i></tt>: Hash function (algorithm).</li>
 * <li><tt>it</tt>: Number of iterations.</li>
 * <li><tt><b>Z</b></tt>: Input for hashing (see below).</li>
 * </ul>
 * </li>
 * <li>
 * <tt>|<b>Z</b>|<b>Z</b>|<b>Z</b>|...|<b>Z</b>|</tt> =
 * <tt>|<b>S</b>|..(ssb)..|<b>S</b>|<b>M</b>|<b>M</b>|<b>M</b>...|<b>M</b>|</tt>
 * <ul>
 * <li><tt><b>S</b></tt>: salt bytes (plain, not digested).</li>
 * <li><tt>ssb</tt>: salt size in bytes.</li>
 * <li><tt><b>M</b></tt>: message bytes.</li>
 * </ul>
 * </li>
 * </ul>
 * <b>If a random salt generator is used, two digests created for the same
 * message will always be different (except in the case of random salt
 * coincidence).</b> Because of this, in this case the result of the
 * <tt>digest</tt> method will contain both the <i>undigested</i> salt and the
 * digest of the (salt + message), so that another digest operation can be
 * performed with the same salt on a different message to check if both messages
 * match (all of which will be managed automatically by the <tt>matches</tt>
 * method).
 * </p>
 * <p>
 * To learn more about the mechanisms involved in digest creation, read <a
 * href="http://www.rsasecurity.com/rsalabs/node.asp?id=2127"
 * target="_blank">PKCS &#035;5: Password-Based Cryptography Standard</a>.
 * </p>
 * 
 * @since 1.0
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 */
public final class StandardByteDigester {

	/**
	 * Default digest algorithm will be MD5
	 */
	public static final String DEFAULT_ALGORITHM = "MD5";
	/**
	 * The minimum recommended size for salt is 8 bytes
	 */
	public static final int DEFAULT_SALT_SIZE_BYTES = 8;
	/**
	 * The minimum recommended iterations for hashing are 1000
	 */
	public static final int DEFAULT_ITERATIONS = 1000;

	// Algorithm to be used for hashing
	private String algorithm = DEFAULT_ALGORITHM;
	// Size of salt to be applied
	private int saltSizeBytes = DEFAULT_SALT_SIZE_BYTES;
	// Number of hash iterations to be applied
	private int iterations = DEFAULT_ITERATIONS;
	// SaltGenerator to be used. Initialization of a salt generator is costly,
	// and so default value will be applied only in initialize(), if it finally
	// becomes necessary.
	private RandomSaltGenerator saltGenerator = null;
	// Name of the java.security.Provider which will be asked for the selected
	// algorithm
	private String providerName = null;

	/*
	 * MessageDigest to be used.
	 * 
	 * IMPORTANT: MessageDigest is not a thread-safe class, and thus any use of
	 * this variable will have to be adequately synchronized.
	 */
	private MessageDigest md = null;

	/*
	 * Length of the result digest for the specified algorithm. This might be
	 * zero if this operation is not supported by the algorithm provider and the
	 * implementation is not cloneable.
	 */
	private int digestLengthBytes = 0;

	private boolean initialized = true;

	/**
	 * Creates a new instance of <tt>StandardByteDigester</tt>.
	 */
	public StandardByteDigester() {
		super();
	}

	/**
	 * <p>
	 * Sets the algorithm to be used for digesting, like <tt>MD5</tt> or
	 * <tt>SHA-1</tt>.
	 * </p>
	 * <p>
	 * This algorithm has to be supported by your security infrastructure, and
	 * it should be allowed as an algorithm for creating
	 * java.security.MessageDigest instances.
	 * </p>
	 * <p>
	 * If you are specifying a security provider with
	 * {@link #setProvider(Provider)} or {@link #setProviderName(String)}, this
	 * algorithm should be supported by your specified provider.
	 * </p>
	 * <p>
	 * If you are not specifying a provider, you will be able to use those
	 * algorithms provided by the default security provider of your JVM vendor.
	 * For valid names in the Sun JVM, see <a target="_blank" href=
	 * "http://java.sun.com/j2se/1.5.0/docs/guide/security/CryptoSpec.html#AppA"
	 * >Java Cryptography Architecture API Specification & Reference</a>.
	 * </p>
	 * 
	 * @param algorithm
	 *            the name of the algorithm to be used.
	 */
	public synchronized void setAlgorithm(final String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * <p>
	 * Sets the size of the salt to be used to compute the digest. This
	 * mechanism is explained in <a
	 * href="http://www.rsasecurity.com/rsalabs/node.asp?id=2127"
	 * target="_blank">PKCS &#035;5: Password-Based Cryptography Standard</a>.
	 * </p>
	 * 
	 * <p>
	 * If salt size is set to zero, then no salt will be used.
	 * </p>
	 * 
	 * @param saltSizeBytes
	 *            the size of the salt to be used, in bytes.
	 */
	public synchronized void setSaltSizeBytes(final int saltSizeBytes) {
		this.saltSizeBytes = saltSizeBytes;
	}

	/**
	 * <p>
	 * Set the number of times the hash function will be applied recursively. <br/>
	 * The hash function will be applied to its own results as many times as
	 * specified: <i>h(h(...h(x)...))</i>
	 * </p>
	 * <p>
	 * This mechanism is explained in <a
	 * href="http://www.rsasecurity.com/rsalabs/node.asp?id=2127"
	 * target="_blank">PKCS &#035;5: Password-Based Cryptography Standard</a>.
	 * </p>
	 * 
	 * @param iterations
	 *            the number of iterations.
	 */
	public synchronized void setIterations(final int iterations) {
		this.iterations = iterations;
	}

	/**
	 * <p>
	 * Sets the salt generator to be used. If no salt generator is specified, an
	 * instance of
	 * {@link it.thisone.iotter.security.jasypt.salt.RandomSaltGenerator} will
	 * be used.
	 * </p>
	 * 
	 * @since 1.2
	 * 
	 * @param saltGenerator
	 *            the salt generator to be used.
	 */
	public synchronized void setSaltGenerator(final RandomSaltGenerator saltGenerator) {
		this.saltGenerator = saltGenerator;
	}

	/**
	 * <p>
	 * Sets the name of the security provider to be asked for the digest
	 * algorithm. This security provider has to be registered beforehand at the
	 * JVM security framework.
	 * </p>
	 * <p>
	 * The provider can also be set with the {@link #setProvider(Provider)}
	 * method, in which case it will not be necessary neither registering the
	 * provider beforehand, nor calling this {@link #setProviderName(String)}
	 * method to specify a provider name.
	 * </p>
	 * <p>
	 * Note that a call to {@link #setProvider(Provider)} overrides any value
	 * set by this method.
	 * </p>
	 * <p>
	 * If no provider name / provider is explicitly set, the default JVM
	 * provider will be used.
	 * </p>
	 * 
	 * @since 1.3
	 * 
	 * @param providerName
	 *            the name of the security provider to be asked for the digest
	 *            algorithm.
	 */
	public synchronized void setProviderName(final String providerName) {
		this.providerName = providerName;
	}



	/**
	 * <p>
	 * Initialize the digester.
	 * </p>
	 * <p>
	 * This operation will consist in determining the actual configuration
	 * values to be used, and then initializing the digester with them. <br/>
	 * These values are decided by applying the following priorities:
	 * </p>
	 * <ol>
	 * <li>First, the default values are considered.</li>
	 * <li>Then, if a <tt>{@link org.jasypt.digest.config.DigesterConfig}</tt>
	 * object has been set with <tt>setConfig</tt>, the non-null values returned
	 * by its <tt>getX</tt> methods override the default values.</li>
	 * <li>Finally, if the corresponding <tt>setX</tt> method has been called on
	 * the digester itself for any of the configuration parameters, the values
	 * set by these calls override all of the above.</li>
	 * </ol>
	 * <p>
	 * Once a digester has been initialized, trying to change its configuration
	 * will result in an <tt>AlreadyInitializedException</tt> being thrown.
	 * </p>
	 * 
	 * @throws EncryptionInitializationException
	 *             if initialization could not be correctly done (for example,
	 *             if the digest algorithm chosen cannot be used).
	 * 
	 */
	public synchronized void initialize() {

		this.saltGenerator = new RandomSaltGenerator();

		/*
		 * MessageDigest is initialized the usual way, and the digester is
		 * marked as "initialized" so that configuration cannot be changed in
		 * the future.
		 */
		try {
			this.md = MessageDigest.getInstance(this.algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new EncryptionInitializationException(e);
		} 
//		catch (NoSuchProviderException e) {
//			throw new EncryptionInitializationException(e);
//		}

		/*
		 * Store the digest length (algorithm-dependent) and check the operation
		 * is supported by the provider.
		 */
		this.digestLengthBytes = this.md.getDigestLength();
		if (this.digestLengthBytes <= 0) {
			throw new EncryptionInitializationException("The configured algorithm (" + this.algorithm + ") or its provider do  "
					+ "not allow knowing the digest length beforehand " + "(getDigestLength() operation), which is not compatible"
					+ "with setting the salt size checking behaviour to \"lenient\".");
		}

	}

	/**
	 * <p>
	 * Performs a digest operation on a byte array message.
	 * </p>
	 * <p>
	 * The steps taken for creating the digest are:
	 * <ol>
	 * <li>A salt of the specified size is generated (see {@link SaltGenerator}
	 * ).</li>
	 * <li>The salt bytes are added to the message.</li>
	 * <li>The hash function is applied to the salt and message altogether, and
	 * then to the results of the function itself, as many times as specified
	 * (iterations).</li>
	 * <li>If specified by the salt generator (see
	 * {@link org.jasypt.salt.SaltGenerator#includePlainSaltInEncryptionResults()}
	 * ), the <i>undigested</i> salt and the final result of the hash function
	 * are concatenated and returned as a result.</li>
	 * </ol>
	 * Put schematically in bytes:
	 * <ul>
	 * <li>
	 * DIGEST =
	 * <tt>|<b>S</b>|..(ssb)..|<b>S</b>|<b>X</b>|<b>X</b>|<b>X</b>|...|<b>X</b>|</tt>
	 * <ul>
	 * <li><tt><b>S</b></tt>: salt bytes (plain, not digested).
	 * <i>(OPTIONAL)</i>.</li>
	 * <li><tt>ssb</tt>: salt size in bytes.</li>
	 * <li><tt><b>X</b></tt>: bytes resulting from hashing (see below).</li>
	 * </ul>
	 * </li>
	 * <li>
	 * <tt>|<b>X</b>|<b>X</b>|<b>X</b>|...|<b>X</b>|</tt> =
	 * <tt><i>H</i>(<i>H</i>(<i>H</i>(..(it)..<i>H</i>(<b>Z</b>|<b>Z</b>|<b>Z</b>|...|<b>Z</b>|))))</tt>
	 * <ul>
	 * <li><tt><i>H</i></tt>: Hash function (algorithm).</li>
	 * <li><tt>it</tt>: Number of iterations.</li>
	 * <li><tt><b>Z</b></tt>: Input for hashing (see below).</li>
	 * </ul>
	 * </li>
	 * <li>
	 * <tt>|<b>Z</b>|<b>Z</b>|<b>Z</b>|...|<b>Z</b>|</tt> =
	 * <tt>|<b>S</b>|..(ssb)..|<b>S</b>|<b>M</b>|<b>M</b>|<b>M</b>...|<b>M</b>|</tt>
	 * <ul>
	 * <li><tt><b>S</b></tt>: salt bytes (plain, not digested).</li>
	 * <li><tt>ssb</tt>: salt size in bytes.</li>
	 * <li><tt><b>M</b></tt>: message bytes.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>If a random salt generator is used, two digests created for the same
	 * message will always be different (except in the case of random salt
	 * coincidence).</b> Because of this, in this case the result of the
	 * <tt>digest</tt> method will contain both the <i>undigested</i> salt and
	 * the digest of the (salt + message), so that another digest operation can
	 * be performed with the same salt on a different message to check if both
	 * messages match (all of which will be managed automatically by the
	 * <tt>matches</tt> method).
	 * </p>
	 * 
	 * @param message
	 *            the byte array to be digested
	 * @return the digest result
	 * @throws EncryptionOperationNotPossibleException
	 *             if the digest operation fails, ommitting any further
	 *             information about the cause for security reasons.
	 * @throws EncryptionInitializationException
	 *             if initialization could not be correctly done (for example,
	 *             if the digest algorithm chosen cannot be used).
	 * 
	 */
	public byte[] digest(byte[] message) {

		if (message == null) {
			return null;
		}

		// Create salt
		byte[] salt = null;
        if (this.saltSizeBytes > 0) {
    		salt = this.saltGenerator.generateSalt(this.saltSizeBytes);
        }

		// Create digest
		return digest(message, salt);

	}

	/*
	 * This method truly performs the digest operation, assuming that a salt has
	 * already been created (if needed) and the digester has already been
	 * initialized.
	 */
	private byte[] digest(final byte[] message, final byte[] salt) {

		try {

			byte[] digest = null;

			synchronized (this.md) {

				this.md.reset();

				if (salt != null) {
					// The salt bytes are added before the message to be
					// digested
					this.md.update(salt);
					this.md.update(message);

				} else {

					// No salt to be added
					this.md.update(message);

				}

				digest = this.md.digest();
				for (int i = 0; i < (this.iterations - 1); i++) {
					this.md.reset();
					digest = this.md.digest(digest);
				}

			}

			// Finally we build an array containing both the unhashed (plain)
			// salt
			// and the digest of the (salt + message). This is done only
			// if the salt generator we are using specifies to do so.
			if (salt != null) {

				// Insert unhashed salt before the hashing result (default
				// behaviour)
				return this.appendArrays(salt, digest);

			}

			return digest;

		} catch (Exception e) {
			// If digest fails, it is more secure not to return any information
			// about the cause in nested exceptions. Simply fail.
			throw new EncryptionOperationNotPossibleException();
		}

	}

	/**
	 * <p>
	 * Checks a message against a given digest.
	 * </p>
	 * <p>
	 * This method tells whether a message corresponds to a specific digest or
	 * not by getting the salt with which the digest was created and applying it
	 * to a digest operation performed on the message. If new and existing
	 * digest match, the message is said to match the digest.
	 * </p>
	 * <p>
	 * This method will be used, for instance, for password checking in
	 * authentication processes.
	 * </p>
	 * <p>
	 * A null message will only match a null digest.
	 * </p>
	 * 
	 * @param message
	 *            the message to be compared to the digest.
	 * @param digest
	 *            the digest.
	 * @return true if the specified message matches the digest, false if not.
	 * @throws EncryptionOperationNotPossibleException
	 *             if the digest matching operation fails, ommitting any further
	 *             information about the cause for security reasons.
	 * @throws EncryptionInitializationException
	 *             if initialization could not be correctly done (for example,
	 *             if the digest algorithm chosen cannot be used).
	 */
	public boolean matches(final byte[] message, final byte[] digest) {

		if (message == null) {
			return (digest == null);
		} else if (digest == null) {
			return false;
		}

		try {

			// If we are using a salt, extract it to use it.
			byte[] salt = null;

			// Compute size figures and perform length checks
			int digestSaltSize = this.saltSizeBytes;
			if (this.digestLengthBytes > 0) {

				if (digest.length != (this.digestLengthBytes + this.saltSizeBytes)) {
					throw new EncryptionOperationNotPossibleException();
				}

			} else {
				if (digest.length < this.saltSizeBytes) {
					throw new EncryptionOperationNotPossibleException();
				}
			}

			salt = new byte[digestSaltSize];
			System.arraycopy(digest, 0, salt, 0, digestSaltSize);

			// Digest the message with the extracted digest.
			final byte[] encryptedMessage = digest(message, salt);

			// If, using the same salt, digests match, then messages too.
			return (digestsAreEqual(encryptedMessage, digest));

		} catch (Exception e) {
			// If digest fails, it is more secure not to return any information
			// about the cause in nested exceptions. Simply fail.
			throw new EncryptionOperationNotPossibleException();
		}

	}

	// Time-constant comparison of byte arrays
	private static boolean digestsAreEqual(byte[] a, byte[] b) {
		if (a == null || b == null) {
			return false;
		}
		final int aLen = a.length;
		if (b.length != aLen) {
			return false;
		}
		int match = 0;
		for (int i = 0; i < aLen; i++) {
			match |= a[i] ^ b[i];
		}
		return (match == 0);

	}

	private byte[] appendArrays(final byte[] firstArray, final byte[] secondArray) {

		final byte[] result = new byte[firstArray.length + secondArray.length];

		System.arraycopy(firstArray, 0, result, 0, firstArray.length);
		System.arraycopy(secondArray, 0, result, firstArray.length, secondArray.length);

		return result;

	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

}