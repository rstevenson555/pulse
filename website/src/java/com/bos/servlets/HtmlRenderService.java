/* $Id$
 *
 * $Author$
 * $Date$
 * $Revision$
 *
 * This class is responsible for generating XML Documents via an http request
 *
 * Copyright (c) 2003 Boise Office Solutions Inc.
 * 800 West Bryn Mawr, Itasca, IL 60148, U. S. A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Boise Office Solutions, Inc. ("Confidential Information").
 */

package com.bos.servlets;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;


public class HtmlRenderService extends HttpServlet {

    protected static String _version = "1.0";
    private static final Logger logger = (Logger)Logger.getLogger(HtmlRenderService.class.getName());



    /**
     * doPost method required from the HttpServlet class.
     * @param request        the HttpServletRequest associated with XML Request
     * @param response       the HttpSerlvetResponse associated with XML Request
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        //redirect call to doGet().  This needs to be here to handle POST for form actions
        //(i.e. updateItemQuantities.web).
        doGet(request, response);
    }

    /**
     * doGet method required from the HttpServlet class. This method will accept the baseXMLService request and send back the
     * appropriate XML or HTML data. The following steps will be performed as part of the invokation of this method:
     * 1) get the model and stylesheet parameters from the httpRequest
     * 2) construct the ModelXML object via dynamic class loading 
     * 3) initialize the ModelXML object
     * 4) invoke the processHttpResponse method
     * @param request        the HttpServletRequest associated with XML Request
     * @param response       the HttpSerlvetResponse associated with XML Request
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {


		OutputStream outStream = response.getOutputStream();
		response.setContentType("text/html");
		String myData = (String) request.getAttribute("HtmlPage");
		outStream.write(myData.getBytes());
        outStream.flush();
    }

    /** init method required from the HttpServlet class. */
    public void init() throws ServletException {
        super.init();
    }



}

