///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPCPIDWriter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * NPCPIDWriter is used to contain a writer job ID code point.
 * This code point has 3 values in it:
 *     NP_ATTR_WTRJOBNAME  - writer job name
 *     NP_ATTR_WTRJOBNUM   - writer job number
 *     NP_ATTR_WTRJOBUSER  - writer job user
 **/

class NPCPIDWriter extends NPCPID implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   /**
    * copy constructor
    **/
    NPCPIDWriter(NPCPIDWriter cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    **/
    NPCPIDWriter()
    {
       super(NPCodePoint.WRITER_JOB_ID);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    **/
    NPCPIDWriter( byte[] data )
    {
       super(NPCodePoint.WRITER_JOB_ID, data);
    }

   /**
    * constructor that takes the ID as seperate items
    **/
    NPCPIDWriter(String writerJobName,
                 String writerJobNumber,
                 String writerJobUser)
    {
       super(NPCodePoint.WRITER_JOB_ID);
       setAttrValue(PrintObject.ATTR_WTRJOBNAME, writerJobName);
       setAttrValue(PrintObject.ATTR_WTRJOBNUM,  writerJobNumber);
       setAttrValue(PrintObject.ATTR_WTRJOBUSER, writerJobUser);
    }


    protected Object clone()
    {
	NPCPIDWriter cp = new NPCPIDWriter(this);
	return cp;
    }

    

    /**
     * get the writer's job number
     **/
    String jobNumber()
    {
	return getStringValue(PrintObject.ATTR_WTRJOBNUM);
    }

    /**
     * get the writer's job name
     **/
    String name()
    {
	return getStringValue(PrintObject.ATTR_WTRJOBNAME);
    }

    /**
     * get the writer's job user
     **/
    String user()
    {
	return getStringValue(PrintObject.ATTR_WTRJOBUSER);
    }

} // NPCPIDWriter

