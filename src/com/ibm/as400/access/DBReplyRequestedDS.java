///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBReplyRequestedDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
   Creates requested reply data streams.
**/
class DBReplyRequestedDS
extends DBBaseReplyDS
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

	public DBReplyRequestedDS ()
	{
		super();
	}


       // Returns the copyright.
       private static String getCopyright()
       {
        return Copyright.copyright;
       }



	final public int hashCode ()
	{
		return 0x2800;
	}


	/**
	   Returns a DBReplyRequestedDS
	**/
	// overrides method from datastream
	final public Object getNewDataStream ()
	{
		return new DBReplyRequestedDS ();
	}

 }  // End of DBReplyRequestedDS class




