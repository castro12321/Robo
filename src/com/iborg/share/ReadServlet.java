/*
 *  Copyright (C) 2002 iBorg Corporation. All Rights Reserved.
 *  Copyright (C) 2002 Boris Galinsky. All Rights Reserved.
 *
 *  This file is part of the share system.
 *
 *  The share system is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation.
 *
 *  See terms of license at gnu.org.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

/*
 * ReadServlet.java
 *
 * Created on March 18, 2002, 6:49 PM
 */

package com.iborg.share;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version
 */

public class ReadServlet extends HttpServlet {
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
    }
    
    /** Destroys the servlet.
     */
    public void destroy() {
        
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        response.setContentType("application/binary");
        String socket = request.getQueryString();
        
        int index = socket.indexOf(':');
        if(index != -1) {
            if(socket.length() > index + 3) {
                socket = socket.substring(0, index + 3);
            }
        }
        
        byte [] data= com.iborg.net.CommunicationManager.read(socket);
        if(data != null) {
            ServletOutputStream servletOutputStream = response.getOutputStream();
            response.setContentLength(data.length);
            try {
                servletOutputStream.write(data);
            } catch (IOException ieo) {
                System.err.println("IOException socket closed");
            } finally {
                try {
                    servletOutputStream.flush();
                    servletOutputStream.close();
                } catch (Exception e) {
                }
            }
        }
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    
}
