///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDLibraryList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import java.util.StringTokenizer;



/**
<p>This class represents an AS/400 library list for database access.
**/
class JDLibraryList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




	private static final String		LIBL_	    = "*LIBL";

    private char[]                  indicators_;
    private String                  defaultSchema_;
	private String[]				list_;



/**
Constructor.

@param  list                The list of libraries.
@param  defaultSchema       The default schema.
@param  naming              The naming convention. 
**/
	JDLibraryList (String list, String defaultSchema, String naming) // @C1C
	{
	    // Initialize.
	    defaultSchema_ = null;    
	    if (defaultSchema != null)
	        if (defaultSchema.length() > 0)
        	    defaultSchema_ = defaultSchema.toUpperCase ();
	    list_ = null;
	    int liblPosition = -1;

        // If a list is specified, then construct the
        // internal list array.
	    if (list.length() != 0) {

	        // Determine if the *LIBL token is included.
	        boolean includesLibl = (list.toUpperCase().indexOf (LIBL_) != -1);

	        // Parse the list into tokens.
    		StringTokenizer tokenizer = new StringTokenizer (list, " ,:;");
    		String token;
    		int count = tokenizer.countTokens ();
    		if (includesLibl)
    		    --count;
    		indicators_ = new char[count];
    		list_ = new String[count];
    		int i = 0;
    		while (tokenizer.hasMoreTokens ()) {
	    		token = tokenizer.nextToken().toUpperCase();

	    		// Mark the position of *LIBL.  Only mark
	    		// the first occurence.  The server will
	    		// return an error later if it occurs
	    		// more than once.
		    	if (token.equalsIgnoreCase (LIBL_)) {
			    	if (liblPosition == -1)
				    	liblPosition = i;
				}
				else {
				    if (includesLibl)
				        indicators_[i] = (liblPosition == -1) ? 'F' : 'L';
				    else
    		            indicators_[i] = (i == 0) ? 'C' : 'L';
				    list_[i] = token;
				    ++i;
				}
    		}

            // If no default schema was specified, then
            // derive it from the list.
            if ((defaultSchema_ == null) && (naming.equals (JDProperties.NAMING_SQL))) { // @C1C
                if (liblPosition != 0)
                    defaultSchema_ = list_[0];
                else if (list_.length > 1)
                    defaultSchema_ = list_[1];
            }

            // Reverse the order of the 'F' libraries, so that
            // they get added to the library in the right order.
            if (liblPosition > 1) {
                int halfPoint = ((int) (liblPosition / 2)) - 1;
                String temp;
                for (int j = 0; j <= halfPoint ; ++j) {
                    int j2 = liblPosition - j - 1;
                    temp = list_[j];
                    list_[j] = list_[j2];
                    list_[j2] = temp;
                }

            }
    	}

        // Otherwise, no list was specified.
    	else {
   	        list_ = new String[0];
    	}
	}



/**
Add the libraries in this list to the server job's library
list for the specified connection.  If the caller specified
*LIBL in the list, then add libraries listed before
*LIBL to the front of the server job's library list.
Likewise, add any libraries listed after *LIBL to the
back of the server job's library list.  If *LIBL is not
specified in the list, then all libraries are replaced
in the server job's library list.

@param      connection      Connection to the server.
@param      id              The id.

@exception  SQLException    If an error occurs.
**/
    void addOnServer (AS400JDBCConnection connection, int id)
		throws SQLException
	{
	    if (list_.length > 0) {

            try {
	    		DBNativeDatabaseRequestDS request = new
	    		    DBNativeDatabaseRequestDS (
		    	    DBNativeDatabaseRequestDS.FUNCTIONID_ADD_LIBRARY_LIST,
			        id, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA, 0);

    			request.setListOfLibraries (indicators_, list_, connection.getConverter ());

    			DBReplyRequestedDS reply = connection.sendAndReceive (request);

    			int errorClass = reply.getErrorClass();
	    		int returnCode = reply.getReturnCode();

    			if (errorClass != 0)
	    			JDError.throwSQLException (connection, id, errorClass, returnCode);
     		}
 	    	catch (DBDataStreamException e) {
 		    	JDError.throwSQLException (JDError.EXC_INTERNAL, e);
     		}
     	}
	}



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



/**
Get the default schema.

@return     The default schema, or null if none specified.
**/
    String getDefaultSchema ()
    {
        return defaultSchema_;
    }


}


