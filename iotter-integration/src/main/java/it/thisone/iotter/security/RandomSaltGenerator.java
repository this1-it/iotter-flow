package it.thisone.iotter.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomSaltGenerator {
    
    /**
     * The default algorithm to be used for secure random number 
     * generation: set to SHA1PRNG.
     */
    public static final String DEFAULT_SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
    
    private final SecureRandom random;
    
    
    /**
     * Creates a new instance of <tt>RandomSaltGenerator</tt> using the 
     * default secure random number generation algorithm.
     */
    public RandomSaltGenerator() {
        this(DEFAULT_SECURE_RANDOM_ALGORITHM);
    }
    
    
    /**
     * Creates a new instance of <tt>RandomSaltGenerator</tt> specifying a 
     * secure random number generation algorithm.
     * 
     * @since 1.5
     * 
     */
    public RandomSaltGenerator(final String secureRandomAlgorithm) {
        super();
        try {
            this.random = SecureRandom.getInstance(secureRandomAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionInitializationException(e);
        }
    }
    

    /**
     * Generate a random salt of the specified length in bytes.
     * 
     * @param lengthBytes length in bytes.
     * @return the generated salt. 
     */
    public byte[] generateSalt(final int lengthBytes) {
        final byte[] salt = new byte[lengthBytes];
        synchronized (this.random) {
            this.random.nextBytes(salt);
        }
        return salt;
    }



    
}