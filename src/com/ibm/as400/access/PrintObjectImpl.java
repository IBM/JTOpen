///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PrintObjectImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 * The PrintObjectImpl interface defines a set of methods
 * needed for a full implementation of the PrintObject class.
 **/

interface PrintObjectImpl
{

    /**
     * The getAttrValue (package scope) method is introduced to allow the
     * propagation of any changes made to attrs (by updateAttrs) to the ImplRemote
     * object.
     **/
    public abstract NPCPAttribute getAttrValue();



    public abstract Integer getIntegerAttribute(int attributeID)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;



    public abstract Float getFloatAttribute(int attributeID)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;



    public abstract String getStringAttribute(int attributeID)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;



    /**
     * The setPrintObjectAttrs (package scope) method is introduced to allow
     * the propagation of PrintObject property changes to the ImplRemote object.
     **/
    public abstract void setPrintObjectAttrs(NPCPID idCodePoint,
                                             NPCPAttribute cpAttrs,
                                             int type);



    public abstract void setSystem(AS400Impl system);  // @A1C



    public abstract void update()
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;

}
