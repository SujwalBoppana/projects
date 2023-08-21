package de.zeroco.core.validation;

import de.zeroco.core.util.ZcMap;
import de.zeroco.core.util.ZcUtil;

public class FeedValidation {

	public static boolean requiredFields(ZcMap reqData, String... keys) {
		if (ZcUtil.isBlank(reqData) || ZcUtil.isBlank(keys))
			return false;
		for (String key : keys) {
			Object value = reqData.get(key);
			if (ZcUtil.isBlank(value)) {
				return false;
			}
		}
		return true;
	}
}
