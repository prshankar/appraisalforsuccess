package com.cfs.util;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.RandomStringUtils;

public class Util {

	public static String encodeString(String base) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String generatePassayPassword() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
		String randomPassword = RandomStringUtils.random( 8, characters );
		return randomPassword;
	}

	public static String getDate(String oldFormat, String newFormat, String oldDate) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(oldFormat);
			java.util.Date date = sdf.parse(oldDate);
			sdf.applyPattern(newFormat);
			return sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getYear(String oldDate) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy");
			java.util.Date date = df.parse(oldDate);
			return df.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}
}
