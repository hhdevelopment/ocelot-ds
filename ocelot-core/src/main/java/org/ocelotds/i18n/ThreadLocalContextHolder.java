/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.i18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author hhfrancois
 */
public class ThreadLocalContextHolder {

	private static final ThreadLocal<Map<String, Object>> THREAD_WITH_CONTEXT = new ThreadLocal<>();

	ThreadLocalContextHolder() {
	}

	public static void put(String key, Object payload) {
		if (Objects.isNull(THREAD_WITH_CONTEXT.get())) {
			THREAD_WITH_CONTEXT.set(new HashMap<String, Object>());
		}
		THREAD_WITH_CONTEXT.get().put(key, payload);
	}

	public static Object get(String key) {
		return THREAD_WITH_CONTEXT.get().get(key);
	}

	public static void cleanupThread() {
		THREAD_WITH_CONTEXT.remove();
	}

}
