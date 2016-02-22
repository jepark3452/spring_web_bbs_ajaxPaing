package spring_web_bbs_study.common.util;

import java.util.UUID;

public class CommonUtils {
	
	public static String getRandomString() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
