///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterFileImplProxy.java
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

/**
 * The PrinterFileImplProxy class implements proxy versions of
 * the public methods defined in the PrinterFileImpl class.
 * Unless commented otherwise, the implementations of the methods below
 * are merely proxy calls to the corresponding method in the remote
 * implementation class (PrinterFileImplRemote).
 **/

class PrinterFileImplProxy extends PrintObjectImplProxy
implements PrinterFileImpl, ProxyImpl
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    PrinterFileImplProxy()
    {
        super("PrinterFile");
    }


    public void setAttributes(PrintParameterList attributes)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException
    {
        try {
            connection_.callMethod(pxId_, "setAttributes",
                                   new Class[] { PrintParameterList.class },
                                   new Object[] { attributes });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

}

