/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.servlets;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.IServicesProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to serve ocelot-services.js
 * ocelot-services is enerated by annotation process
 * @author hhfrancois
 */
@WebServlet(name = "ServicesServlet", urlPatterns = {"/"+Constants.OCELOT_SERVICES_JS})
public class ServicesJSServlet extends HttpServlet {
	
	
	@Inject
	@Any
	Instance<IServicesProvider> servicesProviders;

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/javascript;charset=UTF-8");
		try (OutputStream out = response.getOutputStream()) {
			createLicenceComment(out);
			for (IServicesProvider servicesProvider : servicesProviders) {
				System.out.println("FIND JS PROVIDER : "+servicesProvider.getClass().getName());
				servicesProvider.streamJavascriptServices(out);
			}
		}
	}

	/* This Source Code Form is subject to the terms of the Mozilla Public
	 * License, v. 2.0. If a copy of the MPL was not distributed with this
	 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
	 */
	/**
	 * Rajoute la licence MPL 2.0
	 *
	 * @param out
	 */
	protected void createLicenceComment(OutputStream out) {
		try {
			out.write("/* This Source Code Form is subject to the terms of the Mozilla Public\n".getBytes());
			out.write(" * License, v. 2.0. If a copy of the MPL was not distributed with this\n".getBytes());
			out.write(" * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n".getBytes());
			out.write(" * Classes generated by Ocelot Framework.\n".getBytes());
			out.write(" */\n".getBytes());
		} catch (IOException ioe) {
		}
	}
	
	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
	
}
