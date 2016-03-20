/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web;

import org.ocelotds.Constants;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet to serve ocelot.js
 *
 * @author hhfrancois
 */
@WebServlet(urlPatterns = {Constants.SLASH_OCELOT, Constants.SLASH_OCELOT_JS})
public class JSServlet extends AbstractFileServlet {

	private static final long serialVersionUID = 1973549844535787671L;

	@Override
	protected String getFilename(HttpServletRequest request) {
		String minify = request.getParameter(Constants.MINIFY_PARAMETER);
		final ServletContext servletContext = request.getServletContext();
		String filename = servletContext.getInitParameter(Constants.OCELOT_MIN);
		if (Constants.FALSE.equalsIgnoreCase(minify)) {
			filename = servletContext.getInitParameter(Constants.OCELOT);
		}
		return filename;
	}

	@Override
	protected String getMimetype(HttpServletRequest request) {
		return Constants.JSTYPE;
	}
}
