///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPCPIDSplF.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * NPCPIDSplF is used to contain a spooled file ID code point.
 * This code point has 5 values in it:
 *     NP_ATTR_SPOOLFILE    - spooled file name
 *     NP_ATTR_SPLFNUM      - spooled file number
 *     NP_ATTR_JOBNAME      - job name
 *     NP_ATTR_JOBUSER      - job user
 *     NP_ATTR_JOBNUMBER    - job number
 **/

class NPCPIDSplF extends NPCPID implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   /**
    * copy constructor
    */
    NPCPIDSplF(NPCPIDSplF cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    */
    NPCPIDSplF()
    {
       super(NPCodePoint.SPOOLED_FILE_ID);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    */
    NPCPIDSplF( byte[] data )
    {
       super(NPCodePoint.SPOOLED_FILE_ID, data);
    }

  /**
    * constructor that takes the ID as seperate items
    */
    NPCPIDSplF(String splFileName,
               int    splFileNumber,
               String jobName,
               String jobUser,
               String jobNumber)
    {
       super(NPCodePoint.SPOOLED_FILE_ID);
       setAttrValue(PrintObject.ATTR_SPOOLFILE, splFileName);
       setAttrValue(PrintObject.ATTR_SPLFNUM, splFileNumber);
       setAttrValue(PrintObject.ATTR_JOBNAME, jobName);
       setAttrValue(PrintObject.ATTR_JOBUSER, jobUser);
       setAttrValue(PrintObject.ATTR_JOBNUMBER, jobNumber);
    }

    protected Object clone()
    {
	NPCPIDSplF cp = new NPCPIDSplF(this);
       return cp;
    }

    
  /**
   * get the job name
   */
   String jobName()
   {
      return getStringValue(PrintObject.ATTR_JOBNAME);
   }

  /**
   * get the job number
   */
   String jobNumber()
   {
      return getStringValue(PrintObject.ATTR_JOBNUMBER);
   }

  /**
   * get the spooled file name
   */
   String name()
   {
      return getStringValue(PrintObject.ATTR_SPOOLFILE);
   }

  /**
   * get the spooled file number
   */
   Integer number()
   {
      return getIntValue(PrintObject.ATTR_SPLFNUM);
   }

  /**
   * get the job user
   */
   String user()
   {
      return getStringValue(PrintObject.ATTR_JOBUSER);
   }

} // NPCPIDSplF

