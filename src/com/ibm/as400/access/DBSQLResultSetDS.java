///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBSQLResultSetDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   Create SQL Result Set data stream
**/
class DBSQLResultSetDS
extends DBBaseRequestDS

{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  public static final int	FUNCTIONID_DELETE_RESULTS_SET = 0x1F01;
  public static final int	FUNCTIONID_SEND_RESULTS_SET   = 0x1F00;

/**
   Constructs a datastream for the SQL Server Result Set functions.
   @param  requestId the 4 digit code that represents the function being called.
   @param  rpbId   the request parameter block id.
   @param  operationResultsBitmap the bitmap which describes how the results are to be returned.
   @param  parameterMarkerDescriptorHandle the Parameter marker descriptor handle identifier.
**/

  public DBSQLResultSetDS(int requestId,
			    int rpbId,
			    int operationResultsBitmap,
			    int parameterMarkerDescriptorHandle)

	{
      // Create the datastream header and template
      super(requestId, rpbId, operationResultsBitmap,
		     parameterMarkerDescriptorHandle);
	  setServerID(SERVER_SQL);
	}

// Returns the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }


	// There are no parameters for Result Set functions
        // and therefore no addParameters are required


}	// End of DBSQLResultSetDS




