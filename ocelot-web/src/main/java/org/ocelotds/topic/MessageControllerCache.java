/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.topic;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;
import org.ocelotds.security.JsTopicMessageController;

/**
 * Memory Cache for MessageController
 * @author hhfrancois
 */
@Singleton
public class MessageControllerCache {
	private static final Annotation ANY_AT = new AnnotationLiteral<Any>() {
	};

	final Map<String, Class<? extends JsTopicMessageController>> messageControllers = new HashMap<>();;
	
	/**
	 * Save messageController Factory for topic
	 * @param topic
	 * @param cls 
	 */
	public void saveToCache(String topic, Class<? extends JsTopicMessageController> cls) {
		if(null != topic && null != cls) {
			messageControllers.put(topic, cls);
		}
	}

	/**
	 * Get CDI instance messageController from cache
	 * @param topic
	 * @return 
	 */
	public JsTopicMessageController loadFromCache(String topic) {
		if(null == topic) {
			return null;
		}
		if(messageControllers.containsKey(topic)) {
			Instance<? extends JsTopicMessageController> instances = getInstances(messageControllers.get(topic));
			if(!instances.isUnsatisfied()) {
				return instances.get();
			}
		}
		return null;
	}
	
	Instance<? extends JsTopicMessageController> getInstances(Class<? extends JsTopicMessageController> cls) {
		return CDI.current().select(cls, ANY_AT);
	}
}
