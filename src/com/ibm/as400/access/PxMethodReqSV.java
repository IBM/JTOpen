///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxMethodReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



/**
The PxMethodReqSV class represents the
server view of a method request.
**/
class PxMethodReqSV
extends PxReqSV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private PxTable        pxTable_;
    //@B2D private PSConnection   connection_;



    public PxMethodReqSV(PxTable pxTable) //@B2D, PSConnection connection)
    { 
        super (ProxyConstants.DS_METHOD_REQ);
        pxTable_    = pxTable;
        //@B2Dconnection_ = connection;

        String x = Copyright.copyright;
    }



    static Object invoke(Object object, 
                         String methodName, 
                         Class[] argumentClasses,
                         Object[] arguments)
        throws ClassNotFoundException, 
               IllegalAccessException,
               InvocationTargetException, 
               NoSuchMethodException
    {      
        // Resolve the Method object.  First, try Class.getMethod() which 
        // only looks for public methods.  If that does not work, try 
        // Class.getDeclaredMethod(), which only looks for declared 
        // methods (not inherited methods).  Do this up the superclass tree.
        Method method = null;
        Class clazz = object.getClass();
        NoSuchMethodException e = null;
        while ((clazz != null) && (method == null)) {
            try {
                method = clazz.getMethod(methodName, argumentClasses);
            }
            catch (NoSuchMethodException e1) {
                try {
                    method = clazz.getDeclaredMethod(methodName, argumentClasses);
                }
                catch (NoSuchMethodException e2) {
                    e = e2;
                    clazz = clazz.getSuperclass();
                }
            }
        }
        if (method == null)
            throw e;

        // Call the method.
        return method.invoke (object, arguments);
    }


    public PxRepSV process()
    {      
        try {        
            // Get the information from the datastream parameters.
            int p = -1;
            Object proxy = ((PxPxObjectParm) getParm (++p)).getObjectValue ();            
            String methodName = ((PxStringParm) getParm (++p)).getStringValue ();
            boolean factory = ((PxBooleanParm) getParm (++p)).getBooleanValue ();

            int argumentCount = ((PxIntParm) getParm (++p)).getIntValue ();
            Class[] argumentClasses = new Class[argumentCount];
            for (int i = 0; i < argumentCount; ++i)
                argumentClasses[i] = ((PxClassParm) getParm (++p)).getClassValue ();
            Object[] arguments = new Object[argumentCount];
            for (int i = 0; i < argumentCount; ++i)
                arguments[i] = ((PxParm) getParm (++p)).getObjectValue (); 
            boolean[] returnArguments = new boolean[argumentCount];
            for (int i = 0; i < argumentCount; ++i)
                returnArguments[i] = ((PxBooleanParm)getParm(++p)).getBooleanValue();

            // There is a chance that the proxy object is null here.  It is rare,
            // but it can happen if the client called a method then finalized 
            // immediately following, and the proxy server gets a method call AFTER
            // the finalize so the object is gone by that point.
            Object returnValue = null;
            if (proxy != null) {

                // Invoke the method and return the value.  If the method
                // creates a proxy object, we need to add it to the proxy
                // table.
                returnValue = invoke (proxy, methodName, argumentClasses, arguments);
                if ((factory) && (returnValue != null))
                    pxTable_.add (returnValue);
            }

            return new PxReturnRepSV (pxTable_, returnValue, arguments, returnArguments);
        }
        catch (InvocationTargetException e) {
            return new PxExceptionRepSV (e.getTargetException ());
        }
        catch (Exception e) {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e.toString (), e);

            // If a method can not be called, there is a good chance that a mod x+1                                @A1A
            // client is connecting to a mod x proxy server.  This is bad, but we                                  @A1A
            // don't want to kill the whole proxy server as a result.  Therefore,                                  @A1A
            // we will return the exception to the client.                                                         @A1A            
            return new PxExceptionRepSV(new ProxyException(ProxyException.VERSION_MISMATCH));    // @A1C @B1C
        }
    }




}
