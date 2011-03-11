///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProxyClientConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Locale;
import java.util.Vector;

// The ProxyClientConnection class represents the connection to a proxy server.  This acts as the interface between proxy implementation classes in each Toolbox component and the proxy datastream classes.
//
// Implementation notes:
//
// 1.  We used to keep a PxTable on the client side to keep track of the objects we created.  The problem is that the table keeps a reference to every object, which prevents them from being garbage collected.  This is certainly a memory leak on the client, but since the objects never get finalized, their
//
class ProxyClientConnection extends PxClientConnectionAdapter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private static final String locale_ = Locale.getDefault().toString();
    private static final Class[] noArgumentClasses_ = new Class[0];
    private static final Object[] noArguments_ = new Object[0];

    private int connectAttempts_ = 0;
    private PxEventSupport eventSupport_;
    private Vector pxList_ = new Vector();
    private SecondaryFinalizerThread_ secondaryFinalizerThread_;

    // Constructs a ProxyClientConnection object.
    // @param  proxyServer  The proxy server.
    // @param  secure  Options for a SSL connection, null indicates non-SSL connection.
    public ProxyClientConnection(String proxyServer, SSLOptions secure)
    {
        super(proxyServer, secure);
        connect();
    }

    // Adds a listener.
    // @param  proxyId  The proxy id.
    // @param  listener  The listener.
    // @param  eventName  The event name.
    public void addListener(long proxyId, EventListener listener, String eventName)
    {
        // If this is the first listener of its kind, then add a listener down on the proxy server, so that events get reported back.
        if (eventSupport_.addListener(proxyId, listener))
        {
            PxListenerReqCV request = new PxListenerReqCV(proxyId, ProxyConstants.LISTENER_OPERATION_ADD, eventName);
            send(request);
        }
    }

    // Calls a constructor on the proxy server.  The constructor creates an object on the proxy server.
    // @param  className  The class name.
    // @return  The proxy id.
    // @exception  InvocationTargetException  If the constructor throws an exception.
    public long callConstructor(String className) throws InvocationTargetException
    {
        return callConstructor(className, true);
    }

    // Calls a constructor on the proxy server.  The constructor creates an object on the proxy server.
    // @param  className  The class name.
    // @param  flag  true to tack on ImplRemote to the class name, false to use the class name, as-is.
    // @return  The proxy id.
    // @exception  InvocationTargetException  If the constructor throws an exception.
    public long callConstructor(String className, boolean flag) throws InvocationTargetException
    {
        PxConstructorReqCV request = new PxConstructorReqCV(className, flag);
        long pxId = ((ProxyReturnValue)sendAndReceive(request)).getReturnValuePxId();
        pxList_.addElement(new Long(pxId));
        return pxId;
    }

    // Calls a factory method on the proxy server.
    // @param  proxyId  The proxy id.
    // @param  methodName  The method name.
    // @param  proxyImpl  The proxy object representing the created object.
    // @return  The proxy object representing the created object, or null if no object was returned.
    // @exception  InvocationTargetException  If the method throws an exception.
    public ProxyFactoryImpl callFactoryMethod(long proxyId, String methodName, ProxyFactoryImpl proxyImpl) throws InvocationTargetException
    {
        return callFactoryMethod(proxyId, methodName, noArgumentClasses_, noArguments_, proxyImpl);
    }

    // Calls a factory method on the proxy server.
    // @param  proxyId  The proxy id.
    // @param  methodName  The method name.
    // @param  argumentClasses  The argument classes.
    // @param  arguments  The arguments.
    // @param  proxyImpl  The proxy object representing the created object.
    // @return  The proxy object representing the created object, or null if no object was returned.
    // @exception  InvocationTargetException  If the method throws an exception.
    public ProxyFactoryImpl callFactoryMethod(long proxyId, String methodName, Class[] argumentClasses, Object[] arguments, ProxyFactoryImpl proxyImpl) throws InvocationTargetException
    {
        PxMethodReqCV request = new PxMethodReqCV(proxyId, methodName, argumentClasses, arguments, null, false, true);
        long proxyId2 = ((ProxyReturnValue)sendAndReceive(request)).getReturnValuePxId();
        if (proxyId2 >= 0)
        {
            proxyImpl.initialize(proxyId2, this);
            pxList_.addElement(new Long(proxyId2));
            return proxyImpl;
        }
        return null;
    }

    // Calls a finalize method on the proxy server.  The object will be garbage collected on the proxy server.
    // @param  proxyId  The proxy id.
    // @exception  InvocationTargetException  If the finalize method throws an exception.
    public void callFinalize(long proxyId) throws InvocationTargetException
    {
        // This may have already been called (by this object's finalize() or the ProxyImpl's finalize).  Account for that fact, so that it only runs once.
        Long pxId2 = new Long(proxyId);
        if (pxList_.contains(pxId2))
        {
            eventSupport_.removeAll(proxyId);
            PxFinalizeReqCV request = new PxFinalizeReqCV(proxyId);
            secondaryFinalizerThread_.addRequest(request);
            pxList_.removeElement(pxId2);
        }
    }

    // Calls a method on the proxy server.
    // @param  proxyId  The proxy id.
    // @param  methodName  The method name.
    // @return  The return value.
    // @exception  InvocationTargetException  If the method throws an exception.
    public ProxyReturnValue callMethod(long proxyId, String methodName) throws InvocationTargetException
    {
        return callMethod(proxyId, methodName, noArgumentClasses_, noArguments_, false);
    }

    // Calls a method on the proxy server.
    // @param  proxyId  The proxy id.
    // @param  methodName  The method name.
    // @param  argumentClasses  The argument classes.
    // @param  arguments  The arguments.
    // @return  The return value.
    // @exception  InvocationTargetException  If the method throws an exception.
    public ProxyReturnValue callMethod(long proxyId, String methodName, Class[] argumentClasses, Object[] arguments) throws InvocationTargetException
    {
        return callMethod(proxyId, methodName, argumentClasses, arguments, false);
    }

    // Calls a method on the proxy server.
    // @param  proxyId  The proxy id.
    // @param  methodName  The method name.
    // @param  argumentClasses  The argument classes.
    // @param  arguments  The arguments.
    // @param  asynchronous  true if asynchronous, false otherwise.
    // @return  The return value.
    // @exception  InvocationTargetException  If the method throws an exception.
    public ProxyReturnValue callMethod(long proxyId, String methodName, Class[] argumentClasses, Object[] arguments, boolean asynchronous) throws InvocationTargetException
    {
        PxMethodReqCV request = new PxMethodReqCV(proxyId, methodName, argumentClasses, arguments, null, asynchronous, false);
        return (ProxyReturnValue)sendAndReceive(request);
    }

    // Calls a method on the proxy server.
    // @param  proxyId  The proxy id.
    // @param  methodName  The method name.
    // @param  argumentClasses  The argument classes.
    // @param  arguments  The arguments.
    // @param  returnArguments  Whether return arguments are needed, or null if none are needed.
    // @param  asynchronous  true if asynchronous, false otherwise.
    // @return  The return value.
    // @exception  InvocationTargetException  If the method throws an exception.
    public ProxyReturnValue callMethod(long proxyId, String methodName, Class[] argumentClasses, Object[] arguments, boolean[] returnArguments, boolean asynchronous) throws InvocationTargetException
    {
        PxMethodReqCV request = new PxMethodReqCV(proxyId, methodName, argumentClasses, arguments, returnArguments, asynchronous, false);
        return (ProxyReturnValue)sendAndReceive(request);
    }

    // Calls a method on the proxy server.
    // @param  proxyId  The proxy id.
    // @param  methodName  The method name.
    // @return  The return value as a boolean.
    // @exception  InvocationTargetException  If the method throws an exception.
    public boolean callMethodReturnsBoolean(long proxyId, String methodName) throws InvocationTargetException
    {
        return callMethod(proxyId, methodName, noArgumentClasses_, noArguments_, false).getReturnValueBoolean();
    }

    // Calls a method on the proxy server.
    // @param  proxyId  The proxy id.
    // @param  methodName  The method name.
    // @return  The return value as an int.
    // @exception  InvocationTargetException  If the method throws an exception.
    public int callMethodReturnsInt(long proxyId, String methodName) throws InvocationTargetException
    {
        return callMethod(proxyId, methodName, noArgumentClasses_, noArguments_, false).getReturnValueInt();
    }

    // Calls a method on the proxy server.
    // @param  proxyId  The proxy id.
    // @param  methodName  The method name.
    // @return  The return value as an Object.
    // @exception  InvocationTargetException  If the method throws an exception.
    public Object callMethodReturnsObject(long proxyId, String methodName) throws InvocationTargetException
    {
        return callMethod(proxyId, methodName, noArgumentClasses_, noArguments_, false).getReturnValue();
    }

    // Closes the connection to the proxy server.
    public void close()
    {
        super.close();
        secondaryFinalizerThread_.stopSafely();
    }

    // Initiates the connection to the proxy server.
    public void connect()
    {
        PxConnectReqCV request;
        if (tunnel_)                                                                                                                    // @D1a
           request = new PxConnectReqCV(ProxyConstants.CURRENT_MOD, connectAttempts_++, locale_, ProxyConstants.DS_CONNECT_TUNNEL_REQ); // @D1c
        else                                                                                                                            // @D1a
           request = new PxConnectReqCV(ProxyConstants.CURRENT_MOD, connectAttempts_++, locale_, ProxyConstants.DS_CONNECT_REQ);        // @D1c

        try
        {
            sendAndReceive(request);
        }
        catch (InvocationTargetException e)
        {
            rethrow(e);
        }
    }


    protected void finalize() throws Throwable
    {
        // We should tell all of our proxy objects to finalize themselves BEFORE the socket closes.
        Long[] pxList = new Long[pxList_.size()];
        pxList_.copyInto(pxList);
        for (int i = 0; i < pxList.length; ++i)
        {
            long pxId = pxList[i].longValue();
            callFinalize(pxId);
        }

        // Call the superclass's finalize(), which will close the socket, etc.
        super.finalize();
    }

    public void open(String proxyServer)
    {
        super.open(proxyServer);

        // We need to reinitialize the factory everytime the connection is opened.
        PxDSFactory factory = getFactory();
        factory.register(new PxByteParm());
        factory.register(new PxShortParm());
        factory.register(new PxIntParm());
        factory.register(new PxLongParm());
        factory.register(new PxFloatParm());
        factory.register(new PxDoubleParm());
        factory.register(new PxBooleanParm());
        factory.register(new PxCharParm());
        factory.register(new PxStringParm());
        factory.register(new PxPxObjectParm());
        factory.register(new PxSerializedObjectParm());
        factory.register(new PxToolboxObjectParm());
        factory.register(new PxNullParm());
        factory.register(new PxClassParm());

        factory.register(new PxAcceptRepCV());
        factory.register(new PxRejectRepCV(this));
        factory.register(new PxReturnRepCV());
        factory.register(new PxExceptionRepCV());
        factory.register(new PxEventRepCV(eventSupport_ = new PxEventSupport()));

        // Start the secondary finalizer thread.
        secondaryFinalizerThread_ = new SecondaryFinalizerThread_();
        secondaryFinalizerThread_.start();
    }

    // Removes a listener.
    // @param  proxyId  The proxy id.
    // @param  listener  The listener.
    // @param  eventName  The event name.
    public void removeListener(long proxyId, EventListener listener, String eventName)
    {
        // If this is the last listener of its kind, then remove the listener down on the proxy server, so that events no longer get reported back.
        if (eventSupport_.removeListener(proxyId, listener))
        {
            PxListenerReqCV request = new PxListenerReqCV(proxyId, ProxyConstants.LISTENER_OPERATION_REMOVE, eventName);
            send(request);
            eventSupport_.removeAll(proxyId);
        }
    }

    // Rethrows exceptions returned as InvocationTargetExceptions.  This provides some common exception handling.
    // @param  e  The InvocationTargetException.
    // @return  An InternalErrorException if the exception is not known.
    // @throws  The exception.
    public static InternalErrorException rethrow(InvocationTargetException e)
    {
        Throwable e2 = e.getTargetException();
        if (e2 instanceof RuntimeException)
        {
            throw (RuntimeException)e2;
        }
        if (e2 instanceof Error)
        {
            throw (Error)e2;
        }
        return new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
    }

    // Rethrows exceptions returned as InvocationTargetExceptions.  This provides some common exception handling.
    // @param  e  The InvocationTargetException.
    // @return  An InternalErrorException if the exception is not known.
    // @throws  The exception.
    //
    // Implementation note:
    //
    // * Throws IOException.
    //
    public static InternalErrorException rethrow1(InvocationTargetException e) throws IOException
    {
        Throwable e2 = e.getTargetException();
        if (e2 instanceof IOException)
        {
            throw (IOException)e2;
        }
        return rethrow(e);
    }

    // Rethrows exceptions returned as InvocationTargetExceptions.  This provides some common exception handling.
    // @param  e  The InvocationTargetException.
    // @return  An InternalErrorException if the exception is not known.
    // @throws  The exception.
    //
    // Implementation note:
    //
    // * Throws [rethrow] + AS400SecurityException.
    //
    public static InternalErrorException rethrow2(InvocationTargetException e) throws AS400SecurityException, IOException
    {
        Throwable e2 = e.getTargetException();
        if (e2 instanceof AS400SecurityException)
        {
            throw (AS400SecurityException)e2;
        }
        return rethrow1(e);
    }

    // Rethrows exceptions returned as InvocationTargetExceptions.  This provides some common exception handling.
    // @param  e  The InvocationTargetException.
    // @return  An InternalErrorException if the exception is not known.
    // @throws  The exception.
    //
    // Implementation note:
    //
    // * Throws [rethrow2] + InterruptedException.
    //
    public static InternalErrorException rethrow3(InvocationTargetException e) throws AS400SecurityException, InterruptedException, IOException
    {
        Throwable e2 = e.getTargetException();
        if (e2 instanceof InterruptedException)
        {
            throw (InterruptedException)e2;
        }
        return rethrow2(e);
    }

    // Rethrows exceptions returned as InvocationTargetExceptions.  This provides some common exception handling.
    // @param  e  The InvocationTargetException.
    // @return  An InternalErrorException if the exception is not known.
    // @throws  The exception.
    //
    // Implementation note:
    //
    // * Throws [rethrow3] + ErrorCompletingRequestException.
    //
    public static InternalErrorException rethrow4(InvocationTargetException e) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        Throwable e2 = e.getTargetException();
        if (e2 instanceof ErrorCompletingRequestException)
        {
            throw (ErrorCompletingRequestException)e2;
        }
        return rethrow3 (e);
    }

    // Rethrows exceptions returned as InvocationTargetExceptions.  This provides some common exception handling.
    // @param  e  The InvocationTargetException.
    // @return  An InternalErrorException if the exception is not known.
    // @throws  The exception.
    //
    // Implementation note:
    //
    // * Throws [rethrow3] + AS400Exception.
    //
    public static InternalErrorException rethrow4a(InvocationTargetException e) throws AS400Exception, AS400SecurityException, InterruptedException, IOException
    {
        Throwable e2 = e.getTargetException();
        if (e2 instanceof AS400Exception)
        {
            throw (AS400Exception)e2;
        }
        return rethrow3(e);
    }

    // Rethrows exceptions returned as InvocationTargetExceptions.  This provides some common exception handling.
    // @param  e  The InvocationTargetException.
    // @return  An InternalErrorException if the exception is not known.
    // @throws  The exception.
    //
    // Implementation note:
    //
    // * Throws [rethrow4] + ObjectDoesNotExistException.
    //
    public static InternalErrorException rethrow5(InvocationTargetException e) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        Throwable e2 = e.getTargetException();
        if (e2 instanceof ObjectDoesNotExistException)
        {
            throw (ObjectDoesNotExistException)e2;
        }
        return rethrow4(e);
    }

    // Rethrows exceptions returned as InvocationTargetExceptions.  This provides some common exception handling.
    // @param  e  The InvocationTargetException.
    // @return  An InternalErrorException if the exception is not known.
    // @throws  The exception.
    //
    // Implementation note:
    //
    // * Throws [rethrow5] + ObjectAlreadyExistsException.
    //
    public static InternalErrorException rethrow6(InvocationTargetException e) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ObjectAlreadyExistsException
    {
        Throwable e2 = e.getTargetException();
        if (e2 instanceof ObjectAlreadyExistsException)
        {
            throw (ObjectAlreadyExistsException)e2;
        }
        return rethrow5(e);
    }

    // Rethrows exceptions returned as InvocationTargetExceptions.  This provides some common exception handling.
    // @param  e  The InvocationTargetException.
    // @return  An InternalErrorException if the exception is not known.
    // @throws  The exception.
    //
    // Implementation note:
    //
    // * Throws [rethrow5] + RequestNotSupportedException.
    //
    public static InternalErrorException rethrow6a(InvocationTargetException e) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, RequestNotSupportedException
    {
        Throwable e2 = e.getTargetException();
        if (e2 instanceof RequestNotSupportedException)
        {
            throw (RequestNotSupportedException)e2;
        }
        return rethrow4(e);
    }

    private class SecondaryFinalizerThread_ extends StoppableThread
    {
        private Vector requests_ = new Vector();

        public SecondaryFinalizerThread_()
        {
            super("Proxy client secondary finalizer thread-" + newId());

            // Mark this as a daemon thread so that its running does not prevent the JVM from going away.
            setDaemon(true);
        }

        public void addRequest(PxReqCV request)
        {
            synchronized(requests_)
            {
                requests_.addElement(request);
                requests_.notify();
            }
        }

        public void run()
        {
            while (canContinue())
            {
                synchronized(requests_)
                {
                    try
                    {
                        requests_.wait();
                    }
                    catch(InterruptedException ignore)
                    {
                        // Ignore.
                    }

                    Enumeration list = requests_.elements();
                    while(list.hasMoreElements())
                    {
                        PxReqCV request = (PxReqCV)list.nextElement();
                        send(request);
                    }
                    requests_.removeAllElements();
                }
            }
        }
    }
}
