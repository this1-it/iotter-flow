package it.thisone.iotter.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.UUID;

//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;
import org.apache.commons.codec.binary.Base64;

import com.google.common.io.BaseEncoding;
public class EncryptUtils {
	public static final String DEFAULT_ENCODING = "UTF-8";
//	private static BASE64Encoder enc = new BASE64Encoder();
//	private static BASE64Decoder dec = new BASE64Decoder();
	private static final String KEY = "d.3.d.4.l.0";

	public static String encode(String text) {
		return base64encode(xorMessage(text, KEY));
	}

	public static String decode(String text) {
		return xorMessage(base64decode(text), KEY);
	}

	public static String base64encode(String text) {
//		try {
			byte[] encodedBytes = Base64.encodeBase64(text.getBytes());
			return new String(encodedBytes);
//			String rez = enc.encode(text.getBytes(DEFAULT_ENCODING));
//			return rez;
//		} catch (UnsupportedEncodingException e) {
//			return null;
//		}
	} // base64encode

	public static String base64decode(String text) {

		byte[] decodedBytes = Base64.decodeBase64(text.getBytes());
		return new String(decodedBytes);
		
//		try {
//			return new String(dec.decodeBuffer(text), DEFAULT_ENCODING);
//		} catch (IOException e) {
//			return null;
//		}

	}// base64decode


	/*
	 * 	Feature #141 API KEY Creation
	 */
	public static String createWriteApiKey(String serial) {
		int i = 0, number = 0;
		int len = serial.length();
		byte[] key = new byte[len];
		while (i < len) {
			number += (serial.charAt(i++) - '0');
		}
		int remainder = number % 4;
		for (int j = 0; j < key.length; j++) {
			int letter = (serial.charAt(j) - '0') + remainder + 'A';
			if (letter > 'Z') {
				letter = 'Z';
			}
			key[j] = (byte) letter;
		}
		return new String(key);
	}
	
	
	public static void main(String[] args) {
		//testXOR();
		System.out.println( urlDecode(urlEncode("Marconi Energie")) );
	}

	public static void testXOR() {
		String txt = "some text to be encrypted";
		String key = "key phrase used for XOR-ing";
		System.out.println(txt + " XOR-ed to: " + (txt = xorMessage(txt, key)));
		String encoded = base64encode(txt);
		System.out.println(" is encoded to: " + encoded + " and that is decoding to: " + (txt = base64decode(encoded)));
		System.out.print("XOR-ing back to original: " + xorMessage(txt, key));
	}
	/**
	 * The encryption mechanism is VERY DANGEROUS if used more than once. that is
	 * the reason why it is called One-time pad. The secret key can be easily
	 * recovered by an attacker using 2 encrypted messages. xor 2 encrypted messages
	 * and you get the key.
	 * 
	 * 
	 */
	public static String xorMessage(String message, String key) {
		try {
			if (message == null || key == null)
				return null;
			char[] keys = key.toCharArray();
			char[] mesg = message.toCharArray();
			int ml = mesg.length;
			int kl = keys.length;
			char[] newmsg = new char[ml];
			for (int i = 0; i < ml; i++) {
				newmsg[i] = (char) (mesg[i] ^ keys[i % kl]);
			}// for i
			mesg = null;
			keys = null;
			return new String(newmsg);
		} catch (Exception e) {
			return null;
		}
	}// xorMessage

	public static String getUniqueId() {
		return "-"+UUID.randomUUID().toString();
	}
	
	public static String digest(String key) {
		try {
			byte[] message = key.getBytes(DEFAULT_ENCODING);
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(message);
			byte[] digest = md.digest(message);
			return BaseEncoding.base64().encode(digest);
		} catch (Exception e) {
		}
		return key;
	}
	
	public static String urlDecode(String input) {
		try {
			input = URLDecoder.decode(input, "UTF-8");
		} catch (UnsupportedEncodingException ignored) {
		    // Can be safely ignored because UTF-8 is always supported
		}
		return input;
	}	
	
	public static String urlEncode(String input) {
		try {
			input = URLEncoder.encode(input, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException ignored) {
		    // Can be safely ignored because UTF-8 is always supported
		}
		return input;
	}
	
}// class