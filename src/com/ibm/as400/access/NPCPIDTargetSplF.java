///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPCPIDTargetSplF.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * NPCPIDTargetSplF is used to contain a target spooled file ID code point.
 * This code point is just a spooled file ID with a different codepoint ID.
 * It is used on a send splf action.
 **/

class NPCPIDTargetSplF extends NPCPIDSplF implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   /**
    * copy constructor
    **/
    NPCPIDTargetSplF(NPCPIDTargetSplF cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    **/
    NPCPIDTargetSplF()
    {
       super();
       setID(NPCodePoint.TARGET_SPOOLED_FILE_ID);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    **/
    NPCPIDTargetSplF( byte[] data )
    {
       super(data);
       setID(NPCodePoint.TARGET_SPOOLED_FILE_ID);
    }

  /**
    * constructor that takes the ID as seperate items
    **/
    NPCPIDTargetSplF(String splFileName,
                     int    splFileNumber,
                     String jobName,
                     String jobUser,
                     String jobNumber)
    {
       super(splFileName, splFileNumber,
             jobName, jobUser, jobNumber);
       setID(NPCodePoint.TARGET_SPOOLED_FILE_ID);
    }

    protected Object clone()
    {
       NPCPIDTargetSplF cp = new NPCPIDTargetSplF(this);
       return cp;
    }

    // add copyright
    static private String getCopyright()
    {
        return Copyright.copyright;
    }

} // NPCPIDTargetSplF

