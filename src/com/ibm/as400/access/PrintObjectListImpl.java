///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PrintObjectListImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;
import java.io.IOException;
 

/**
  * The PrintObjectListImpl interface defines a set of methods
  * needed for a full implementation of the PrintObjectList class.
 **/

interface PrintObjectListImpl
{  
    
    public abstract void addPrintObjectListListener(PrintObjectListListener listener);
    
    
    
    public abstract void close();
    
    
        
    // @A5D public abstract PrintObject getObject(int index);
    public abstract NPCPID getNPCPID(int index);                        // @A5A
    public abstract NPCPAttribute getNPCPAttribute(int index);          // @A5A



    // @A5D public abstract Vector getObjects();
    
    
    
    public abstract boolean isCompleted()
        throws  AS400Exception,
                AS400SecurityException,
                ConnectionDroppedException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                RequestNotSupportedException;
 


    public abstract void openAsynchronously();

  
  
    public abstract void openSynchronously()
        throws  AS400Exception,
                AS400SecurityException,
                ConnectionDroppedException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                RequestNotSupportedException;
     
             

    public abstract void removePrintObjectListListener(PrintObjectListListener listener);
    
    
             
    public abstract void resetAttributesToRetrieve();
   
   
   
    public abstract void resetFilter();
  
 
 
    public abstract void setAttributesToRetrieve(int[] attributes);
    
    
    
    public abstract void setIDCodePointFilter(NPCPID cpID);
    
    
    
    public abstract void setFilter(String filterType, String filter);
    
    
    
    public abstract void setPrintObjectListAttrs(NPCPAttributeIDList attrsToRetrieve,
                                       NPCPID idFilter,
                                       NPCPSelection selection,
                                       int typeOfObject);

    
     
    public abstract void setSystem(AS400Impl system);
    
    
    
    public abstract int size();



    public abstract void waitForItem(int itemNumber)
        throws  AS400Exception,
                AS400SecurityException,
                ConnectionDroppedException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                RequestNotSupportedException;


   
    public abstract void waitForListToComplete()
        throws  AS400Exception,
                AS400SecurityException,
                ConnectionDroppedException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                RequestNotSupportedException;

}
