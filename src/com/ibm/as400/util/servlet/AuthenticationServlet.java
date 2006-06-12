///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AuthenticationServlet.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import java.util.Hashtable;
import java.util.Properties;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import sun.misc.BASE64Decoder;

import javax.servlet.*;
import javax.servlet.http.*;

import com.ibm.as400.access.Log;                       //$A2C
import com.ibm.as400.access.Trace;

/**
  * AuthenticationServlet is an HttpServlet implementation that performs basic authentication for servlets.
  * Subclasses should override the validateAuthority() method to perform the authentication.  The bypassValidation()
  * method can be overridden to authenticate only certain requests and the postValidation() method can be overridden
  * for additional processing of the request after authenticating.
  **/
public class AuthenticationServlet extends HttpServlet
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
    static final long serialVersionUID = 3761917964251765027L;

    /**
      * The realm used for this servlet instance.
      **/
    private String realm_ = "localhost";

    /**
       * The actual name the user will see.  This may be the same as realm in some cases.  A realm of
       * "localhost" may be meaningless to the user, while a realmDisplayName of "mysystem" may be more
       * meaningful.
       **/
    private String realmDisplayName_;   

    /**
       * Hashtable used to keep session data.
       **/
    private Hashtable sessionTable_ = new Hashtable();

    /**
       * The log to use for logging traces and errors.    //$A2C
       **/
    private Log log_;                                    //$A2C

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader_s loader_;       //$A1A




    /**
     *  Constructs a default AuthenticationServlet object.
     **/
    public AuthenticationServlet()
    {
        super();
    }


    /**
     *  Constructs an AuthenticationServlet object with the specified <i>user</i>, <i>password</i>, and <i>realm</i>.
     *
     *  @param user The user ID to use.
     *  @param password The password for this user ID.
     *  @param realm The realm, which refers to the system name.
     **/
    public AuthenticationServlet(String user, String password, String realm)
    {
        super();

        setUser(user);
        setPassword(password);
        setRealm(realm);
    }



    /**
     * Method to check to see if authentication should be performed.  The default implementation returns false.
     * Subclasses that wish to implement authentication based on the URL can override this method, interrogate
     * the request object and determine if authentication should be performed.
     *
     * @param req  The HttpServletRequest object for this request.
     * @return true if authentication should not be performed.
     */
    public boolean bypassAuthentication(HttpServletRequest req)
    {
        return false;
    }


    /**
      * Get the log object used for tracing and error logging.
      *
      * @return     The Log object to use for this servlet.
      */
    public Log getLog()
    {
        return log_;
    }


    /**
     * Retrieve the user that was used for the authentication.
     *
     * @return     The authenticated user ID.
     */
    public String getUser()
    {
        Thread currentThread = Thread.currentThread();
        String threadId = currentThread.getName();
        Properties p = (Properties)sessionTable_.get(threadId);
        return p.getProperty("uid");
    } 


    /**
     * Retrieve the realm that was used for the authentication.  For the server, the realm is the
     * i5/OS system name.
     *
     * @return     The realm.
     */
    public String getRealm()
    {
        return realm_;
    }


    /**
      * Retrieve the Hashtable used to keep session data
     *
      * @return     The session data.
      */
    Hashtable getSessionData()
    {
        return sessionTable_;
    }


    /**
     * Servlet initialization.  The realm is initialized at this point to localhost.  It can be overridden
     * by the setRealm() method.
     *
     * @param   config  The servlet configuration.  
     * @exception   ServletException A ServletException is thrown if a problem with the servlet occurs. 
     */
    public void init(ServletConfig config)
    throws ServletException
    {
        log_ = new ServletEventLog(config);
        super.init(config);

        setRealm("localhost");               // @A7C
    }


    /**
     * Log a message to the event log.
     *
     * @param   msg  The message to log.
     */
    public void log(String msg)
    {
        if (log_ != null)
            log_.log(msg);
    }


    /**
     * Log an exception and message to the event log.
     *
     * @param   e  The exception to log.
     * @param   msg  The message to log.
     */
    public void log(Exception e, String msg)
    {
        if (log_ != null)
            log_.log(msg, e);
    }





    /**
      * Send the authentication response to the client.
      *
      * @param   resp  The HttpServletResponse object to use.
      * @param   realmDisplayName  The realm name to be displayed by the client.
      * @exception   IOException  An IOException is thrown if a communications error occurs.
      */
    private void sendAuthenticationResponse(HttpServletResponse resp, String realmDisplayName)
    throws IOException
    {
        log(loader_.substitute(loader_.getText("PROP_DESC_CHALLENGE"), new String[] {realmDisplayName_}));     //$A1C
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setHeader("Www-authenticate", "Basic realm=\"" + realmDisplayName_ + "\"");
        resp.setContentType("text/html");       
    }


    /**
      * Set the log object used for tracing and error logging.
      *
      * @param log The Log.
      */
    public void setLog(Log log)                          //$A2C
    {
        if (log == null)
            throw new NullPointerException("log");

        log_ = log;                                       //$A2C
    }


    /**
      * Set the password.  This method can be used to set the password to a default password after bypassing
      * authentication.
      *
      * @param   password  The password to use.
      */
    public void setPassword(String password)
    {
        if (password == null)
            throw new NullPointerException("password");

        Thread currentThread = Thread.currentThread();
        String threadId = currentThread.getName();
        Properties p = (Properties)sessionTable_.get(threadId);

        if (p == null)
            p = new Properties();

        p.put("pw", password);              
    }


    /**
      * Override the default service() method for HttpServlet.  Subclasses should not override this method unless
      * necessary.  If a subclass overrides this method, it should call super.service() or authentication would
      * not occur for the servlet.
      *
      * @param   req  The HTTP servlet request.
      * @param   resp The HTTP servlet response. 
      * @exception   ServletException A ServletException is thrown if a problem with the servlet occurs. 
      * @exception   IOException An IOException is thrown if a communications error occurs. 
      **/
    public void service(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        log(loader_.substitute(loader_.getText("PROP_DESC_SERVICE"), new String[] {req.getRemoteHost(), req.getRemoteAddr()})); //$A1C

        // check to see if we should authenticate.  Call bypassAuthentication() method, which can be overridden
        // by subclasses to see if authentication should be performed. Default implementation is to authenticate
        // all requests. Subclasses can override the method and interrogate the request object to determine
        // if authentication is needed.
        if (!bypassAuthentication(req))
        {
            // see if the header contained realm user ID and password
            String uidpw = req.getHeader("Authorization");

            if (uidpw != null)
            {
                int index = uidpw.indexOf(" ");
                uidpw = uidpw.substring(index+1);
                // now decode from base64
                BASE64Decoder decoder = new BASE64Decoder();
                byte[] buffer = decoder.decodeBuffer(uidpw);
                String s = new String(buffer);
                index = s.indexOf(":");
                String uid, pw;                                                                          // $A8C

                // If a character causes the ':' and password which follows to be omitted from the       // $A8A
                // authoriztion header, then we need to ensure the password is set to an empty           // $A8A
                // string, which will allow the validateAuthority method to gracefully handle            // $A8A
                // an invalid uid/pwd string instead of generating exceptions.                           // $A8A
                if (index == -1)                                                                         // $A8A
                {
                    uid = s;                                                                              // $A8C
                    pw  = "";                                                                             // $A8C
                    Trace.log(Trace.INFORMATION, "Missing ':' (colon) in authorization header.");         // $A8A
                }                                                                                        // $A8A
                else                                                                                     // $A8A
                {
                    uid = s.substring(0, index);
                    pw = s.substring(index+1);
                }

                try
                {
                    log(loader_.substitute(loader_.getText("PROP_DESC_AUTHENTICATING"), new String[] {realm_, uid}));       //$A1C

                    // $A5A
                    // The return from validateAuthority will tell if the caller wants to continue validation.  
                    // When the caller overrides validateAuthority() they can then return false, which will
                    // allow them to display there own HTML error message/page in the browser instead of
                    // getting the default browser message (Error: 503... etc) when an exception occurs.
                    if (!validateAuthority(realm_, uid, pw))             //$A5C
                        return;                                           //$A5A

                    Thread currentThread = Thread.currentThread();
                    String threadId = currentThread.getName();              

                    Properties p = new Properties();
                    p.put("realm", realm_);
                    p.put("uid", uid);
                    p.put("pwd", pw);


                    sessionTable_.put(threadId, p);             
                    log(   loader_.substitute(   loader_.getText(   "PROP_DESC_AUTHENTICATED"),    new    String[   ]    {realm_,    uid}));            //$A1C

                    //$A5A
                    // The return from postValidation will tell if the caller wants to continue validation.  
                    // When the caller overrides postValidation() they can then return false, which will
                    // allow them to display there own HTML error message/page in the browser instead of
                    // getting the default browser message (Error: 503... etc) when an exception occurs.
                    if (!postValidation(req, resp))                      //$A5C
                        return;                                           //$A5A

                    super.service(req, resp);

                    sessionTable_.remove(threadId);             
                }
                catch (SecurityException se)
                {
                    log(loader_.substitute(loader_.getText("PROP_DESC_AUTHENTICATEFAILED"), new String[] {uid, se.getMessage()}));  //$A1C  $A6C

                    if (Trace.isTraceOn())              //$A6A
                        Trace.log(Trace.ERROR, se);      //$A6A

                    sendAuthenticationResponse(resp, realmDisplayName_);
                }
                catch (Exception e)
                {
                    log(loader_.substitute(loader_.getText("PROP_DESC_REQFAILED"), new String[] {uid, e.getMessage()}));    //$A1C  $A6C

                    if (Trace.isTraceOn())              //$A6A
                        Trace.log(Trace.ERROR, e);       //$A6A

                    if (e instanceof IOException)                        //$A4A
                        throw (IOException)e;                             //$A4A
                    else if (e instanceof ServletException)              //$A4A
                        throw (ServletException)e;                        //$A4A
                    else if (e instanceof RuntimeException)              // @A4C
                        throw (RuntimeException)e;                        // @A4C
                    else                                                 //$A4C
                        throw new ServletException(e.getMessage());       // @A4C

                }

                log(loader_.substitute(loader_.getText("PROP_DESC_REQCOMPLETED"), new String[] {req.getRemoteHost(), req.getRemoteAddr()})); //$A1C
                return;
            }

            // respond with realm challenge
            sendAuthenticationResponse(resp, realmDisplayName_);
        }
        else
        {
            super.service(req, resp);
        }
    }


    /**
      * Set the realm that will be used for the authentication.  For the server, the realm is the
      * i5/OS system name.
      *
      * @param realm The realm, which refers to the system name.
      **/
    public void setRealm(String realm)
    {
        if (realm == null)
            throw new NullPointerException("realm");


        if (realm.equalsIgnoreCase("localhost"))
        {
            try
            {
                InetAddress local = InetAddress.getLocalHost();
                realmDisplayName_ = local.getHostName();
                realm_ = realmDisplayName_;                                // @A7A
            }
            catch (UnknownHostException e)
            {
                log(loader_.getText("PROP_DESC_REALMFAILED"));             //$A1C  $A6C

                if (Trace.isTraceOn())            //$A6A
                    Trace.log(Trace.ERROR, e);     //$A6A

                realm_ = realm;                                            // @A7A
                realmDisplayName_ = realm;
            }
        }
        else
        {
            realm_ = realm;                                               // @A7A
            realmDisplayName_ = realm;
        }
    }


    /**
      * Set the user ID.  This method can be used to set the user ID to a default user after bypassing
      * authenticaiton.
      *
      * @param user The user ID to use.
      **/
    public void setUser(String user)
    {
        if (user == null)
            throw new NullPointerException("user");

        Thread currentThread = Thread.currentThread();
        String threadId = currentThread.getName();
        Properties p = (Properties)sessionTable_.get(threadId);

        if (p == null)
            p = new Properties();

        p.put("uid", user);     
    }


    /**
      * Method used to validate.  The default implementation does nothing.  Subclasses should override this method
      * and implement appropriate validation scheme.
      *
      * @param   realm  The realm to validate against.
      * @param   uid  The user ID to use for validation.
      * @param   pw  The password to use for validation.
     *
     * @return     true if the servlet should continue authenticating; false otherwise.  The default is true;
     *
      * @exception   SecurityException  This exception should be thrown if validation fails.
      * @exception   IOException  This exception should be thrown if a communication error occurs during validation.
      **/
    public boolean validateAuthority(String realm, String uid, String pw)                           //$A5C
    throws SecurityException, IOException
    {
        return true;
    }


    /**
      * Method called after validation has occured.  The default implementation does nothing.  
     * Subclasses should override this method to continue processing the request.
      *
     * @param   req  The HTTP servlet request.
      * @param   resp The HTTP servlet response. 
     *
     * @return     true if the servlet should continue authenticating; false otherwise.  The default is true;
     *
      * @exception   ServletException A ServletException is thrown if a problem with the servlet occurs. 
      * @exception   IOException An IOException is thrown if a communications error occurs. 
      **/
    public boolean postValidation(HttpServletRequest req, HttpServletResponse resp)           //$A3 //$A5C
    throws ServletException, IOException
    {
        return true;
    }  
}
