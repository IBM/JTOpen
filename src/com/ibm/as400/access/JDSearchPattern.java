///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDSearchPattern.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
   The JDSearchPattern class represents a search pattern based on a
   pattern string used as input to many functions.
**/
//
// Note that there is a difference between a null and empty pattern
// string.
//
final class JDSearchPattern
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    private static final String 	ESCAPE_CHAR_   = "\\"; // same as \

    private String patternString_;



/**
   Constructs a JDSearchPattern object.

    @param      patternString   The pattern string.
**/
    JDSearchPattern (String patternString)
    {
        patternString_ = (patternString != null) ? patternString.trim()
            : null;
    }



/**
   Indicates if the search pattern contains an escape character.

   @return   true if it contains an escape character; false otherwise.
**/
    final boolean containsEscape ()
    {
        return (patternString_ != null)
            ? (patternString_.indexOf (ESCAPE_CHAR_) != -1)
            : false;
    }



/**
    Indicates if the search pattern contains any wildcard
    characters.

    @return     true if it contains a wildcard; false otherwise.
**/
    final boolean containsSearchPattern ()
    {
        return  (patternString_ != null) ? ((patternString_.indexOf ('%') != -1)
                 || (patternString_.indexOf ('_') != -1)) : false;
    }



/**
   Returns the escape character.

   @return     The escape character.
**/
    static final String getEscape ()
    {
        return ESCAPE_CHAR_;
    }



/**
   Returns the appropriate search pattern indicator.  This
   is used in ROI server requests.

   @return     The search pattern indicator.
**/
    final int getIndicator ()
    {
        int indicator;
    	if (containsSearchPattern () || containsEscape ())
	{
            indicator = 0xF1;
	}
       	else
	{
            indicator = 0xF0;
        }
        return indicator;
    }




/**
   Returns the pattern string.

  @return     The pattern string.
**/
    final String getPatternString ()
    {
        return patternString_;
    }



/**
   Returns the appropriate SQL WHERE clause for inclusion in
   a SELECT statement in order to query for this search pattern.

   @param  columnName  The name of the column associated with
                       this search pattern.
   @return             The SQL WHERE clause.
**/
    final String getSQLWhereClause (String columnName)
    {
        StringBuffer clause = new StringBuffer ();
        if (isSpecified())
        {
            clause.append (columnName);  // ie SCHEMA_NAME
            clause.append (" ");
            if (containsSearchPattern ())
            {
                clause.append ("LIKE  '");
                clause.append (patternString_);
                clause.append ("' ");                
                if (containsEscape ()) 
	            {
                    clause.append ("ESCAPE '");
                    clause.append (ESCAPE_CHAR_);
                    clause.append ("' ");
	            }
            }
            else  // Does not contain a search pattern, don't use LIKE
            {
                clause.append ("=  '");
                clause.append (patternString_);
                clause.append ("' ");
            }

        } // if not specified, will return empty string

        return clause.toString();
    }



/**
   Indicates if the search pattern is specified.

   @return     true if specified; false otherwise.
**/
    final boolean isSpecified ()
    {
        return (patternString_ != null);
    }



}

