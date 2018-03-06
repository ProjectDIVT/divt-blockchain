package util;

import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;


public class CryptoUtil {

	public static String hash(String data) {

		MessageDigest md;
		byte[] hash = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			hash = md.digest(data.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return DatatypeConverter.printHexBinary(hash);
	}
}
