///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxEventSupport.java
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
import java.util.Enumeration;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Vector;



/**
The PxEventSupport class maintains the listener lists
for all proxy objects associated with a single
ProxyClientConnection.
**/
//
// Implementation note:
//
// * This is implemented using a Hashtable where the
//   keys are proxy ids (as Longs) and the elements
//   are Vectors.  The Vectors contain the list of listeners
//   associated with the proxy id.
//
class PxEventSupport
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private Hashtable   idToListeners_  = new Hashtable();



/**
Adds a listener.
                                                       
@return             true if this is the first listener of its type
                    to be added to the proxy object, false otherwise.
**/
    public boolean addListener (long proxyId, EventListener listener)
    {
        boolean first = true;
        Object key = toKey (proxyId);
        
        // If the table already contains a list for this proxy object...
        if (idToListeners_.containsKey (key)) {
            Vector listeners = (Vector) idToListeners_.get (key);

            // Look through the list to see if this is the first of
            // its kind.
            Class listenerClass = listener.getClass ();
            Enumeration enum = listeners.elements ();
            while (enum.hasMoreElements () && first) {
                if (enum.nextElement ().getClass ().equals (listenerClass))
                    first = false;
            }

            // Add the listener to the list.
            listeners.addElement (listener);
        }

        // Otherwise, create a new list for this proxy object.
        else {
            Vector listeners = new Vector ();
            listeners.addElement (listener);
            idToListeners_.put (key, listeners);
        }

        return first;
    }
    


    public void fireEvent (long proxyId, 
                           String listenerInterfaceName,
                           String listenerMethodName, 
                           EventObject eventObject)
        throws InvocationTargetException
    {
        Object key = toKey (proxyId);
        if (idToListeners_.containsKey (key)) {

            // Enumerate the list of listeners.
            Vector listeners = (Vector) idToListeners_.get (key);
            Enumeration enum = listeners.elements ();
            while (enum.hasMoreElements ()) {
                Object listener = enum.nextElement ();                

                // The list may contain several types of listeners.  If this
                // is not the right kind, then an exception will be thrown.
                // We can ignore this exception, and assume the event does 
                // not need to be fired.
                try {
                    // We need to get a reference to the listener interface class object.
                    // If we use the listener's class object, it may not work due to access
                    // restrictions.
                    Class interfaze = Class.forName(listenerInterfaceName);
                    Method listenerMethod = interfaze.getMethod (listenerMethodName, new Class[] { eventObject.getClass () });
                    listenerMethod.invoke (listener, new Object[] { eventObject });
                }
                catch (ClassNotFoundException e) {
                    if (Trace.isTraceErrorOn ())
                        Trace.log (Trace.ERROR, "ClassNotFoundException while firing event", e);
                    throw new ExtendedIllegalStateException (ExtendedIllegalStateException.PROXY_SERVER_EVENT_NOT_FIRED);
                }
                catch (NoSuchMethodException e) {
                    if (Trace.isTraceErrorOn ())
                        Trace.log (Trace.ERROR, "NoSuchMethodException while firing event", e);
                    throw new ExtendedIllegalStateException (ExtendedIllegalStateException.PROXY_SERVER_EVENT_NOT_FIRED);
                }
                catch (IllegalAccessException e) {
                    if (Trace.isTraceErrorOn ())
                        Trace.log (Trace.ERROR, "IllegalAccessException while firing event", e);
                    throw new ExtendedIllegalStateException (ExtendedIllegalStateException.PROXY_SERVER_EVENT_NOT_FIRED);
                }
            }
        }
    }



    public void removeAll(long proxyId)
    {        
        Object key = toKey (proxyId);
        
        if (idToListeners_.containsKey (key)) {
            Vector listeners = (Vector) idToListeners_.get (key);
            listeners.removeAllElements();
            idToListeners_.remove(key);
        }
    }



/**
Removes a listener.

@param proxyId      The proxy id.
@param listener     The listener.
@return             true if this is the last listener of its type
                    to be removed from the proxy object, false otherwise.
**/
    public boolean removeListener (long proxyId, EventListener listener)
    {
        boolean last = true;
        Object key = toKey (proxyId);
        
        // If the table contains a list for this proxy object...
        if (idToListeners_.containsKey (key)) {
            Vector listeners = (Vector) idToListeners_.get (key);

            // Remove the listener from the list.
            listeners.removeElement (listener);

            // Look through the list to see if this was the last of
            // its kind.
            Class listenerClass = listener.getClass ();
            Enumeration enum = listeners.elements ();
            while (enum.hasMoreElements () && last) {
                if (enum.nextElement ().getClass ().equals (listenerClass))
                    last = false;
            }

        }

        // Otherwise, essentially do nothing.
        else {
            last = false;
        }        

        return last;
    }
    


/**
Returns the key associated with the proxy id.
This is for use in the internal hashtable.

@param proxyId  The proxy id.
@return         The key.
**/
    private static Object toKey (long proxyId)
    {
        return new Long (proxyId);
    }




}
