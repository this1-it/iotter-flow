package it.thisone.iotter.security;

import org.apache.commons.codec.binary.Base64;

/**
 * <p>
 * Standard implementation of the {@link StringDigester} interface. This class
 * lets the user specify the algorithm (and provider) to be used for creating
 * digests, the size of the salt to be applied, the number of times the hash
 * function will be applied (iterations) and the salt generator to be used.
 * </p>
 * <p>
 * This class avoids byte-conversion problems related to the fact of different
 * platforms having different default charsets, and returns digests in the form
 * of BASE64-encoded or HEXADECIMAL ASCII Strings.
 * </p>
 * <p>
 * This class is <i>thread-safe</i>.
 * </p>
 * <p>
 * <br/>
 * <b><u>Configuration</u></b>
 * </p>
 * <p>
 * The algorithm, provider, salt size, iterations and salt generator can take
 * values in any of these ways:
 * <ul>
 * <li>Using its default values.</li>
 * <li>Setting a <tt>{@link org.jasypt.digest.config.DigesterConfig}</tt> object
 * which provides new configuration values.</li>
 * <li>Calling the corresponding <tt>setX(...)</tt> methods.</li>
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
 * Once a digester has been initialized, trying to change its configuration will
 * result in an <tt>AlreadyInitializedException</tt> being thrown.
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
 * <li>The String message is converted to a byte array.</li>
 * <li>A salt of the specified size is generated (see
 * {@link org.jasypt.salt.SaltGenerator}).</li>
 * <li>The salt bytes are added to the message.</li>
 * <li>The hash function is applied to the salt and message altogether, and then
 * to the results of the function itself, as many times as specified
 * (iterations).</li>
 * <li>If specified by the salt generator (see
 * {@link org.jasypt.salt.SaltGenerator#includePlainSaltInEncryptionResults()}),
 * the <i>undigested</i> salt and the final result of the hash function are
 * concatenated and returned as a result.</li>
 * <li>The result of the concatenation is encoded in BASE64 or HEXADECIMAL and
 * returned as an ASCII String.</li>
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
public final class StandardStringDigester {

	/**
	 * <p>
	 * Charset to be used to obtain "digestable" byte arrays from input Strings.
	 * Set to <b>UTF-8</b>.
	 * </p>
	 * <p>
	 * This charset has to be fixed to some value so that we avoid problems with
	 * different platforms having different "default" charsets.
	 * </p>
	 * <p>
	 * It is set to <b>UTF-8</b> because it covers the whole spectrum of
	 * characters representable in Java (which internally uses UTF-16), and
	 * avoids the size penalty of UTF-16 (which will always use two bytes for
	 * representing each character, even if it is an ASCII one).
	 * </p>
	 * <p>
	 * Setting this value to UTF-8 does not mean that Strings that originally
	 * come for, for example, an ISO-8859-1 input, will not be correcly
	 * digested. It simply provides a way of "fixing" the way a String will be
	 * converted into bytes for digesting.
	 * </p>
	 */
	public static final String MESSAGE_CHARSET = "UTF-8";

	/**
	 * <p>
	 * Charset to be used for encoding the resulting digests. Set to
	 * <b>US-ASCII</b>.
	 * </p>
	 * <p>
	 * The result of digesting some bytes can be any other bytes, and so the
	 * result of digesting, for example, some LATIN-1 valid String bytes, can be
	 * bytes that may not conform a "valid" LATIN-1 String.
	 * </p>
	 * <p>
	 * Because of this, digests are always encoded in <i>BASE64</i> or
	 * HEXADECIMAL after being created, and this ensures that the digests will
	 * make perfectly representable, safe ASCII Strings. Because of this, the
	 * charset used to convert the digest bytes to the returned String is set to
	 * <b>US-ASCII</b>.
	 * </p>
	 */
	public static final String DIGEST_CHARSET = "US-ASCII";

	/**
	 * <p>
	 * Whether the Unicode normalization step should be ignored because of
	 * legacy-compatibility issues. Defaults to <b>FALSE</b> (the normalization
	 * step WILL be performed).
	 * </p>
	 */
	public static final boolean DEFAULT_UNICODE_NORMALIZATION_IGNORED = false;

	/**
	 * <p>
	 * Default type of String output. Set to <b>BASE64</b>.
	 * </p>
	 */
	public static final String DEFAULT_STRING_OUTPUT_TYPE = "base64";

	// The StandardByteDigester that will be internally used.
	private final StandardByteDigester byteDigester;

	// This variable holds whether the unicode normalization step should
	// be ignored or not (default = DO NOT ignore).
	private boolean unicodeNormalizationIgnored = DEFAULT_UNICODE_NORMALIZATION_IGNORED;

	// This variable holds the type of String output which will be done,
	// and also a boolean variable for faster comparison
	private String stringOutputType = DEFAULT_STRING_OUTPUT_TYPE;

	// Prefix and suffix to be added to encryption results (if any)
	private String prefix = null;
	private String suffix = null;

	/*
	 * Set of booleans which indicate whether the config or default values have
	 * to be overriden because of the setX methods having been called.
	 */
	private boolean unicodeNormalizationIgnoredSet = false;
	private boolean stringOutputTypeSet = false;
	private boolean prefixSet = false;
	private boolean suffixSet = false;

	// BASE64 encoder which will make sure the returned digests are
	// valid US-ASCII strings (if the user chooses BASE64 output).
	// The Bsae64 encoder is THREAD-SAFE
	private final org.apache.commons.codec.binary.Base64 base64;

	/**
	 * Creates a new instance of <tt>StandardStringDigester</tt>.
	 */
	public StandardStringDigester() {
		super();
		this.byteDigester = new StandardByteDigester();
		this.base64 = new Base64();
	}

	/*
	 * Creates a new instance of <tt>StandardStringDigester</tt> using the
	 * specified byte digester (constructor used for cloning)
	 */
	public StandardStringDigester(final StandardByteDigester standardByteDigester) {
		super();
		this.byteDigester = standardByteDigester;
		this.base64 = new Base64();
	}

	/**
	 * <p>
	 * Sets the prefix to be added at the beginning of encryption results, and
	 * also to be expected at the beginning of plain messages provided for
	 * matching operations (raising an
	 * {@link EncryptionOperationNotPossibleException} if not).
	 * </p>
	 * <p>
	 * By default, no prefix will be added to encryption results.
	 * </p>
	 * 
	 * @since 1.7
	 * 
	 * @param prefix
	 *            the prefix to be set
	 */
	public synchronized void setPrefix(final String prefix) {
		this.prefix = prefix;
		this.prefixSet = true;
	}

	/**
	 * <p>
	 * Sets the suffix to be added at the end of encryption results, and also to
	 * be expected at the end of plain messages provided for matching operations
	 * (raising an {@link EncryptionOperationNotPossibleException} if not).
	 * </p>
	 * <p>
	 * By default, no suffix will be added to encryption results.
	 * </p>
	 * 
	 * @since 1.7
	 * 
	 * @param suffix
	 *            the suffix to be set
	 */
	public synchronized void setSuffix(final String suffix) {

		this.suffix = suffix;
		this.suffixSet = true;
	}

	/**
	 * <p>
	 * Returns true if the digester has already been initialized, false if not.<br/>
	 * Initialization happens:
	 * </p>
	 * <ul>
	 * <li>When <tt>initialize</tt> is called.</li>
	 * <li>When <tt>digest</tt> or <tt>matches</tt> are called for the first
	 * time, if <tt>initialize</tt> has not been called before.</li>
	 * </ul>
	 * <p>
	 * Once a digester has been initialized, trying to change its configuration
	 * will result in an <tt>AlreadyInitializedException</tt> being thrown.
	 * </p>
	 * 
	 * @return true if the digester has already been initialized, false if not.
	 */
	public boolean isInitialized() {
		return this.byteDigester.isInitialized();
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
		this.byteDigester.initialize();
	}

	/**
	 * <p>
	 * Performs a digest operation on a String message.
	 * </p>
	 * <p>
	 * The steps taken for creating the digest are:
	 * <ol>
	 * <li>The String message is converted to a byte array.</li>
	 * <li>A salt of the specified size is generated (see
	 * {@link org.jasypt.salt.SaltGenerator}).</li>
	 * <li>The salt bytes are added to the message.</li>
	 * <li>The hash function is applied to the salt and message altogether, and
	 * then to the results of the function itself, as many times as specified
	 * (iterations).</li>
	 * <li>If specified by the salt generator (see
	 * {@link org.jasypt.salt.SaltGenerator#includePlainSaltInEncryptionResults()}
	 * ), the <i>undigested</i> salt and the final result of the hash function
	 * are concatenated and returned as a result.</li>
	 * <li>The result of the concatenation is encoded in BASE64 (default) or
	 * HEXADECIMAL and returned as an ASCII String.</li>
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
	 *            the String to be digested
	 * @return the digest result
	 * @throws EncryptionOperationNotPossibleException
	 *             if the digest operation fails, ommitting any further
	 *             information about the cause for security reasons.
	 * @throws EncryptionInitializationException
	 *             if initialization could not be correctly done (for example,
	 *             if the digest algorithm chosen cannot be used).
	 */
	public String digest(final String message) {

		if (message == null) {
			return null;
		}

		// Check initialization
		if (!isInitialized()) {
			initialize();
		}

		try {

			final byte[] messageBytes = message.getBytes(MESSAGE_CHARSET);

			// The StandardByteDigester does its job.
			byte[] digest = this.byteDigester.digest(messageBytes);

			// We build the result variable
			final StringBuffer result = new StringBuffer();

			if (this.prefix != null) {
				// Prefix is added
				result.append(this.prefix);
			}

			// We encode the result in BASE64
			// the safest result String possible.

			digest = this.base64.encode(digest);
			result.append(new String(digest, DIGEST_CHARSET));

			if (this.suffix != null) {
				// Suffix is added
				result.append(this.suffix);
			}

			return result.toString();

		} catch (EncryptionInitializationException e) {
			throw e;
		} catch (EncryptionOperationNotPossibleException e) {
			throw e;
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
	public boolean matches(final String message, final String digest) {

		String processedDigest = digest;

		if (processedDigest != null) {
			if (this.prefix != null) {
				if (!processedDigest.startsWith(this.prefix)) {
					throw new EncryptionOperationNotPossibleException("Digest does not start with required prefix \"" + this.prefix + "\"");
				}
				processedDigest = processedDigest.substring(this.prefix.length());
			}
			if (this.suffix != null) {
				if (!processedDigest.endsWith(this.suffix)) {
					throw new EncryptionOperationNotPossibleException("Digest does not end with required suffix \"" + this.suffix + "\"");
				}
				processedDigest = processedDigest.substring(0, processedDigest.length() - this.suffix.length());
			}
		}

		if (message == null) {
			return (processedDigest == null);
		} else if (processedDigest == null) {
			return false;
		}

		// Check initialization
		if (!isInitialized()) {
			initialize();
		}

		try {

			// We get a valid byte array from the message, in the
			// fixed MESSAGE_CHARSET that the digest operations use.
			final byte[] messageBytes = message.getBytes(MESSAGE_CHARSET);

			// The BASE64 or HEXADECIMAL encoding is reversed and the digest
			// is converted into a byte array.
			byte[] digestBytes = null;

			// The digest must be a US-ASCII String BASE64-encoded
			digestBytes = processedDigest.getBytes(DIGEST_CHARSET);
			digestBytes = this.base64.decode(digestBytes);

			// The StandardByteDigester is asked to match message to digest.
			return this.byteDigester.matches(messageBytes, digestBytes);

		} catch (EncryptionInitializationException e) {
			throw e;
		} catch (EncryptionOperationNotPossibleException e) {
			throw e;
		} catch (Exception e) {
			// If digest fails, it is more secure not to return any information
			// about the cause in nested exceptions. Simply fail.
			throw new EncryptionOperationNotPossibleException();
		}

	}

}