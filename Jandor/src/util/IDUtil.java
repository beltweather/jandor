package util;

import java.util.Random;
import java.util.UUID;

public class IDUtil {
	
	public static final String PREFIX_USER = "User";
	public static final String PREFIX_DECK = "Deck";
	public static final String PREFIX_DRAFT = "Draft";
	public static final String PREFIX_BOOSTER = "Booster";
	public static final String PREFIX_CARDLAYER = "CardLayer";
	public static final String PREFIX_INVITE = "Invite";
	public static final String PREFIX_INVITE_RESPONSE = "ResponseInvite";
	
	public static String toInviteResponsePrefix(String guid) {
		return PREFIX_INVITE_RESPONSE + ":" + guid + ":";
	}
	
	public static String getUniquePrefix(String prefix) {
		return prefix + ":" + IDUtil.generateGUID() + ":";
	}
	
	public static String extractPrefixGUID(String prefix) {
		return prefix.split(":")[1];
	}
	
	private static final Random random = new Random(System.currentTimeMillis());
	private static final int MIN = 10000;
	private static final int MAX = 99999;
	public static final int NONE = -1;
	
	private IDUtil() {}
	
	public static int newId() {
		int id = random.nextInt((MAX - MIN) + 1) + MIN;
		while(!isUnique(id)) {
			id = random.nextInt((MAX - MIN) + 1) + MIN;
		}
		return id;
	}
	
	public static boolean isUnique(int id) {
		return true;
	}
	
	public static String generateGUID() {
		return generateGUID(null);
	}
	
	public static String generateGUID(String prefix) {
		return (prefix == null ? "" : prefix + "-") + UUID.randomUUID().toString();
	}
	
}
