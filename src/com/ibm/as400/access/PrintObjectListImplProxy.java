///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectListImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.io.IOException;

/**
 * The PrintObjectListImplProxy class implements proxy versions of
 * the public methods defined in the PrintObjectListImpl class.
 * Unless commented otherwise, the implementations of the methods below
 * are merely proxy calls to the corresponding method in the remote
 * implementation class (PrintObjectListImplRemote).
 **/

abstract class PrintObjectListImplProxy
extends AbstractProxyImpl
implements PrintObjectListImpl, ProxyImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   

    PrintObjectListImplProxy(String className)
    {
        super(className);
    }

    
    
    public void addPrintObjectListListener( PrintObjectListListener listener )
    {
        connection_.addListener(pxId_, listener, "PrintObjectList");  
    }



    public void close() 
    {
        try {
            connection_.callMethod(pxId_, "close");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }

     
        
    /* @A5D
    public PrintObject getObject(int index)
    {
        try {
            return (PrintObject) connection_.callMethod(pxId_, "getObject",
                                 new Class[]{ Integer.TYPE },
                                 new Object[] { new Integer(index) }).getReturnValue();  
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }
    */

    public void setCache(boolean b)
    {
      try
      {
        connection_.callMethod(pxId_, "setCache", new Class[] { Boolean.TYPE }, new Object[] { new Boolean(b) });
      }
      catch(InvocationTargetException e)
      {
         throw ProxyClientConnection.rethrow(e);
      }
    }

    // @A5A
    public NPCPID getNPCPID(int index)
    {
        try {
            return (NPCPID) connection_.callMethod(pxId_, "getNPCPID",
                                 new Class[]{ Integer.TYPE },
                                 new Object[] { new Integer(index) }).getReturnValue();  
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }


    // @A5A
    public NPCPAttribute getNPCPAttribute(int index)
    {
        try {
            return (NPCPAttribute) connection_.callMethod(pxId_, "getNPCPAttribute",
                                 new Class[]{ Integer.TYPE },
                                 new Object[] { new Integer(index) }).getReturnValue();  
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }


    /* @A5D
    public Vector getObjects()
    {
        try {
            return (Vector) connection_.callMethod(pxId_, "getObjects").getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }
    */
    
    
    
    public boolean isCompleted()
      throws  AS400Exception,
              AS400SecurityException,
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              RequestNotSupportedException
    { 
        try {
            return (boolean) connection_.callMethod(pxId_, "isCompleted").getReturnValueBoolean();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public void openAsynchronously()
    {
        try {  
            connection_.callMethod(pxId_, "openAsynchronously");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }
      

 
    public void openSynchronously()
        throws  AS400Exception,
             AS400SecurityException,
             ConnectionDroppedException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             RequestNotSupportedException
             
    {
        try {
            connection_.callMethod(pxId_, "openSynchronously");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }
     
             
   
    public void removePrintObjectListListener( PrintObjectListListener listener )
    {
        connection_.removeListener(pxId_, listener, "PrintObjectList");
    }


             
    public void resetAttributesToRetrieve()
    {
        try {
            connection_.callMethod(pxId_, "resetAttributesToRetrieve");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }
    
   
   
    public void resetFilter()
    {
        try {
            connection_.callMethod(pxId_, "resetFilter");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }
   
 
 
    public void setAttributesToRetrieve(int[] attributes)
    {
        try {
            connection_.callMethod(pxId_, "setAttributesToRetrieve",
                                              new Class[] { int[].class },
                                              new Object[] { attributes });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }
   
      
      
    public void setIDCodePointFilter(NPCPID cpID)
    {
        try {
            connection_.callMethod(pxId_, "setIDCodePointFilter",
                                              new Class[] { NPCPID.class },
                                              new Object[] { cpID });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }
      
      
           
    public void setFilter(String filterType, String filter)
    {
        try {
            connection_.callMethod(pxId_, "setFilter",
                                   new Class[] { String.class, String.class },
                                   new Object[] { filterType, filter });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    } 
      
      
      
    public void setPrintObjectListAttrs(NPCPAttributeIDList attrsToRetrieve,
                                        NPCPID idFilter,
                                        NPCPSelection selection,
                                        int typeOfObject)
    {
        try {
            connection_.callMethod(pxId_, "setPrintObjectListAttrs",
                                              new Class[] { NPCPAttributeIDList.class,
                                                            NPCPID.class, NPCPSelection.class,
                                                            Integer.TYPE},
                                              new Object[] { attrsToRetrieve, idFilter, selection, new Integer(typeOfObject) });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }
    
    
    
    public void setSystem(AS400Impl system)  // @A1C
    {
        try {
            connection_.callMethod(pxId_, "setSystem",
                                   new Class[] { AS400Impl.class },  // @A1C
                                   new Object[] { system });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }
    
    
    
    public int size()
    {
        try {
            return connection_.callMethod(pxId_, "size").getReturnValueInt();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    
    
    public void waitForItem(int itemNumber)
      throws  AS400Exception,
              AS400SecurityException,
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "waitForItem",
                                              new Class[] { Integer.TYPE },
                                              new Object[] { new Integer(itemNumber) });
        }
        catch (InvocationTargetException e) {   
            throw ProxyClientConnection.rethrow6a(e);
        }
    }  


   
    public void waitForListToComplete()
      throws  AS400Exception,
              AS400SecurityException,
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              RequestNotSupportedException
               {
        try {
            connection_.callMethod(pxId_, "waitForListToComplete");
        }
        catch (InvocationTargetException e) { 
            throw ProxyClientConnection.rethrow6a(e);
        }
    }  
}
