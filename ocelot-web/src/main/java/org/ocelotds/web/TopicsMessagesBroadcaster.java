/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ocelotds.web;

import org.ocelotds.core.SessionManager;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;
import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton that send push messages
 *
 * @author hhfrancois
 */
public class TopicsMessagesBroadcaster {

	private final static Logger logger = LoggerFactory.getLogger(TopicsMessagesBroadcaster.class);

	@Inject
	private SessionManager sessionManager;

	/**
	 * Send message to topic
	 *
	 * @param msg
	 */
	public void sendMessageToTopic(@Observes @MessageEvent MessageToClient msg) {
		msg.setType(MessageType.MESSAGE);
		logger.debug("Sending message to topic {}...", msg);
		Collection<Session> sessions = sessionManager.getSessionsForTopic(msg.getId());
		if (!sessions.isEmpty()) {
			Collection<Session> closed = new ArrayList<>();
			for (Session session : sessions) {
				if (session.isOpen()) {
					session.getAsyncRemote().sendObject(msg);
				} else {
					closed.add(session);
				}
			}
			if(logger.isDebugEnabled()){
				logger.debug("Send message to '{}' topic {} client(s) : {}", new Object[]{msg.getId(), sessions.size()-closed.size(), msg});
			}
			if(!closed.isEmpty()) {
				logger.debug("Session closed to remove '{}'", closed.size());
				sessionManager.removeSessionsToTopic(sessions);
			}
		} else {
			logger.debug("No client for topic '{}'", msg.getId());
		}
	}
}
