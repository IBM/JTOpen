///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JDSQLStatement.java
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
<p>This class represents a parsed SQL statement.
**/
//
// Implementation note:
//
// Originally, the statement was parsed as information
// was needed.  For example, it did not parse to see
// if "FOR UPDATE" appeared until isForUpdate() was
// called.  This strategy caused a lot of extraneous
// parsing, and most of the information is needed for
// most statements, so now all parsing is done at object
// construction time.
//
class JDSQLStatement
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Native statement types.
    //
    static final int    TYPE_UNDETERMINED  = 0;
    static final int    TYPE_OTHER         = 1;
    static final int    TYPE_SELECT        = 2;
    static final int    TYPE_CALL          = 3;
    static final int    TYPE_COMMIT        = 4;
    static final int    TYPE_ROLLBACK      = 5;
    static final int    TYPE_CONNECT       = 6;
    static final int    TYPE_BLOCK_INSERT  = 7;



    // String constants.  These will hopefully help performance
    // slightly - assuming a similar optimization does not
    // already take place.
    private static final String     AS_             = "AS";
    private static final String     CALL_           = "CALL";
    private static final String     CALL1_          = "?=";
    private static final String     CALL2_          = "?=CALL";
    private static final String     COMMA_          = ",";
    private static final String     CONNECT_        = "CONNECT";
    private static final String     CROSS_          = "CROSS";
    private static final String     CURRENT_        = "CURRENT";
    private static final String     DECLARE_        = "DECLARE";
    private static final String     DELETE_         = "DELETE";
    private static final String     DISCONNECT_     = "DISCONNECT";
    private static final String     EXCEPTION_      = "EXCEPTION";
    private static final String     FETCH_          = "FETCH";
    private static final String     FOR_            = "FOR";
    private static final String     FROM_           = "FROM";
    private static final String     INNER_          = "INNER";
    private static final String     INSERT_         = "INSERT";
    private static final String     JOIN_           = "JOIN";
    private static final String     LEFT_           = "LEFT";
    private static final String     LPAREN_         = "(";
    private static final String     OF_             = "OF";
    private static final String     ONLY_           = "ONLY";
    private static final String     READ_           = "READ";
    private static final String     RELEASE_        = "RELEASE";
    private static final String     ROWS_           = "ROWS";
    private static final String     SELECT_         = "SELECT";
    private static final String     SET_            = "SET";
    private static final String     UPDATE_         = "UPDATE";
    private static final String     VALUES_         = "VALUES";
    private static final String     WITH_           = "WITH";   // @B3A



    private String          correlationName_            = null;
    private boolean         isCall_                     = false;
    private boolean         isDeclare_                  = false;
    private boolean         isCurrentOf_                = false;
    private boolean         isDRDAConnect_              = false;    // @B1A
    private boolean         isDRDADisconnect_           = false;    // @B1A
    private boolean         isForFetchOrReadOnly_       = false;
    private boolean         isForUpdate_                = false;
    private boolean         isImmediatelyExecutable_    = false;
    private boolean         isInsert_                   = false;
    private boolean         isSelect_                   = false;
    private boolean         isSubSelect_                = false;
    private boolean         isPackaged_                 = false;
    private boolean         isUpdateOrDelete_           = false;
	private int			    nativeType_                 = TYPE_OTHER;
    private int             numberOfParameters_;
    private String          selectTable_                = null;
    private StringTokenizer tokenizer_                  = null;
    private String          value_;



/**
Constructs a JDSQLStatement object.  Use this constructor
if you know you do not need to do any conversion to native
SQL.

@param  sql                 A SQL statement.

@exception  SQLException        If there is a syntax error or
                                a reference to an unsupported
                                scalar function.
**/
    JDSQLStatement (String sql)
		throws SQLException
    {
        this (sql, "", false, JDProperties.PACKAGE_CRITERIA_DEFAULT);             // @A1C
    }



/**
Constructs a JDSQLStatement object.

@param  sql                 A SQL statement.
@param  decimalSeparator    The decimal separator.
@param  convert             Convert to native SQL?
@param  packageCriteria     The package criteria.

@exception  SQLException        If there is a syntax error or
                                a reference to an unsupported
                                scalar function.
**/
    JDSQLStatement (String sql,
                    String decimalSeparator,
                    boolean convert,
                    String packageCriteria)                                       // @A1C
		throws SQLException
    {
        if (sql == null)
            JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);

        // Ensure that the string always contains at least one
        // character, since some methods depend on that fact.
        if (sql.trim ().length() == 0)
            JDError.throwSQLException (JDError.EXC_SYNTAX_BLANK);

        // Count the number of parameters.  Do not count parameter
        // markers that appear within quotes or after a comment
        // delimiter (two dashes).
        numberOfParameters_ = 0;
        int commentIndex = -1;
        int length = sql.length ();
        for (int i = 0; i < length; ++i) {
            char ch = sql.charAt (i);
            if (ch == '\'') {
                while ((i < length - 1) && (sql.charAt (++i) != '\''))
                    ;
            }
            else if (ch == '\"' ) {
                while ((i < length - 1) && (sql.charAt (++i) != '\"'))
                    ;
            }
            else if (ch == '-') {
                if (i < length - 1)
                    if (sql.charAt (++i) == '-') {
                        commentIndex = i;
                        while (i < length - 1)
                            ++i;
                    }
            }
            else if (ch == '?') 
                ++numberOfParameters_;            
        }

        // If we want to process escape syntax, then treat the
        // whole string as a big escape clause for parsing.
		if (convert) {

            // Weed off the comment before parsing.  This causes
            // problems if we try to handle it inside the parsing
            // code, since the parsing is recursive, but "skip-to-
            // the-end-of-the-line" is hard to implement in
            // recursive decent parsing.
            if (commentIndex >= 0)
                value_ = JDEscapeClause.parse (sql.substring (0, commentIndex), decimalSeparator)
                    + sql.substring (commentIndex);
            else
		        value_ = JDEscapeClause.parse (sql, decimalSeparator);
        }
		else
		    value_ = sql;

        // Determine the first word.
        String firstWord;
        tokenizer_ = new StringTokenizer (value_);
        if (tokenizer_.countTokens() > 0)
            firstWord = tokenizer_.nextToken ().toUpperCase ();
        else
            firstWord = "";

        // If the statement is a SELECT...
        if ((firstWord.equals (SELECT_)) || (firstWord.equals (WITH_))) { // @B3C
            isSelect_ = true;
            nativeType_ = TYPE_SELECT;
        }

        // If the statement is a CALL...
        else if ((firstWord.equals (CALL_))
                 || (firstWord.equals (CALL1_))
                 || (firstWord.equals (CALL2_))) {
            isCall_ = true;
            nativeType_ = TYPE_CALL;
        }

        // If the statement is a CONNECT, etc...
        else if ((firstWord.equals (CONNECT_))
                    || (firstWord.equals (SET_))
                    || (firstWord.equals (DISCONNECT_))
                    || (firstWord.equals (RELEASE_))) {
            nativeType_ = TYPE_CONNECT;

            if (firstWord.equals (CONNECT_))            // @B1A
                isDRDAConnect_ = true;                  // @B1A
            else if (firstWord.equals (DISCONNECT_))    // @B1A
                isDRDADisconnect_ = true;               // @B1A
        }

        // If the statement is an INSERT...
        else if (firstWord.equals (INSERT_)) {
            isInsert_ = true;

            // Look for the string ROWS VALUES in the string.
            String upperCaseSql = value_.toUpperCase ();
            int k = upperCaseSql.indexOf (ROWS_);
            if (k != -1) {
                for (k += 4; (k < upperCaseSql.length()) && (Character.isWhitespace (upperCaseSql.charAt (k))); ++k);
                if (upperCaseSql.regionMatches (k, VALUES_, 0, 6))
                    nativeType_ = TYPE_BLOCK_INSERT;
            }

        }

        // If the statement is an UPDATE or DELETE...
        else if ((firstWord.equals (UPDATE_))
                || (firstWord.equals (DELETE_))) {

            isUpdateOrDelete_ = true;
        }

        // If the statement is a DECLARE...
        else if (firstWord.equals (DECLARE_))
            isDeclare_ = true;

        // Now we need to do some parsing based on the
        // rest of the words.  These are tests for the
        // following certain phrases:
        //
        //    CURRENT OF
        //    FOR FETCH ONLY
        //    FOR READ ONLY
        //    FOR UPDATE
        //    FROM (select from-clause)
        //
        int parsingState = 0;
        String token;
        while (tokenizer_.hasMoreTokens ()) {
            token = tokenizer_.nextToken().toUpperCase ();

            if (token.equals (CURRENT_))
                parseCurrent ();
            else if (token.equals (FOR_))
                parseFor ();
            else if ((isSelect_) && (token.equals (FROM_)))
                parseFrom ();

            // If this is an INSERT, then check for
            // a sub SELECT
            if (isInsert_) {
                if (token.equals (SELECT_))  
                    isSubSelect_ = true;
            }
        }

        // Based on all of the information that we
        // have gathered up to this point, determine
        // a few more tidbits.
        boolean intermediate = (numberOfParameters_ > 0)
            || (isInsert_ && isSubSelect_)
            || (isCurrentOf_ && isUpdateOrDelete_);

        isImmediatelyExecutable_ = ! (intermediate || isSelect_);

        // @A1C
        // Changed the logic to determine isPackaged_ from the
        // "package criteria" property.
        if (packageCriteria.equalsIgnoreCase(JDProperties.PACKAGE_CRITERIA_DEFAULT)) {  // @A1A
            isPackaged_ = intermediate
                || (isSelect_ && ! isForFetchOrReadOnly_)
                || (isDeclare_);
        }                                                                               // @A1A
        else {                                                                          // @A1A
            isPackaged_ = (isInsert_ && isSubSelect_)                                   // @A1A
                || (isCurrentOf_ && isUpdateOrDelete_)                                  // @A1A
                || (isSelect_ && ! isForFetchOrReadOnly_)                               // @A1A
                || (isDeclare_);                                                        // @A1A
        }                                                                               // @A1A
    }



/**
Returns the number of parameters in the SQL statement.

@return         Number of parameters.
**/
	int countParameters ()
	{
        return numberOfParameters_;
	}



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



/**
Returns the correlation name for a SELECT statement.

@return The correlation name, or null if no correlation name
        is specified, or this is not a SELECT statement.
**/
    String getCorrelationName ()
    {
        return correlationName_;
    }



/**
Returns the native statement type.

@return         Native type.
**/
	int getNativeType()
	{
		return nativeType_;
	}



/**
Returns the single table name for a SELECT statement.

@return The single table name, or null if multiple tables
        were specified, or this is not a SELECT statement.
**/
    String getSelectTable ()
    {
        return selectTable_;
    }



/**
Indicates if the statement contains a CURRENT
OF clause.

@return     true if the statement contains a
            CURRENT OF clause; false otherwise.
**/
    /* @B2D boolean isCurrentOf ()
    {
        return isCurrentOf_;
    } */



// @B1A
/**
Indicates if the statement initiates a
DRDA connection.

@return     true if the statement initiates a
            DRDA connection; false otherwise.
**/
    boolean isDRDAConnect ()
    {
        return isDRDAConnect_;
    }



// @B1A
/**
Indicates if the statement closes a
DRDA connection.

@return     true if the statement closes a
            DRDA connection; false otherwise.
**/
    boolean isDRDADisconnect ()
    {
        return isDRDADisconnect_;
    }



/**
Indicates if the statement contains a FOR FETCH
ONLY or FOR READ ONLY clause.

@return     true if the statement contains a
            FOR FETCH ONLY or FOR READ ONLY clause;
            false otherwise.
**/
    boolean isForFetchOnly ()
    {
        return isForFetchOrReadOnly_;
    }



/**
Indicates if the statement contains a FOR UPDATE
clause.

@return     true if the statement contains a
            FOR UPDATE clause; false otherwise.
**/
    boolean isForUpdate ()
    {
        return isForUpdate_;
    }



/**
Indicates if the statement can be executed immediately
without doing a separate prepare and execute.

@return     true if the statement can be executed
            immediately executable; false otherwise.
**/
    boolean isImmediatelyExecutable ()
    {
        return isImmediatelyExecutable_;
    }



/**
Indicates if this statement should be stored in
a package.  This decision is based on characteristics
that make statements good candidates for being
stored in packages (those that will likely benefit
overall performance by being stored in a package).
This helps to reduce clutter in packages.

@return     true if the statement should be stored
            in a package; false otherwise.
**/
    boolean isPackaged ()
    {
        return isPackaged_;
    }



/**
Indicates if the statement is a stored procedure call.

@return     true if the statement is a stored
            procedure call; false otherwise.
**/
	boolean isProcedureCall ()
	{
		return isCall_;
	}



/**
Indicates if the statement a SELECT.

@return     true if the statement is a SELECT;
            false otherwise.
**/
	boolean isSelect ()
	{
		return isSelect_;
	}



/**
Parses the token after CURRENT.
**/
    private void parseCurrent ()
    {
        if (tokenizer_.hasMoreTokens ()) {
            String token = tokenizer_.nextToken().toUpperCase ();

            if (token.equals (OF_))
                isCurrentOf_ = true;
        }
    }



/**
Parses the token after FOR.
**/
    private void parseFor ()
    {
        if (tokenizer_.hasMoreTokens ()) {
            String token = tokenizer_.nextToken().toUpperCase ();

            if ((token.equals (FETCH_))
                || (token.equals (READ_)))
                parseForFetchOrRead ();
            else if (token.equals (UPDATE_))
                isForUpdate_ = true;
        }
    }



/**
Parses the token after FOR FETCH or FOR READ.
**/
    private void parseForFetchOrRead ()
    {
        if (tokenizer_.hasMoreTokens ()) {
            String token = tokenizer_.nextToken().toUpperCase ();

            if (token.equals (ONLY_))
                isForFetchOrReadOnly_ = true;
        }
    }



/**
Parses the token after a FROM.
**/
    private void parseFrom ()
    {
        if (tokenizer_.hasMoreTokens ()) {
            String token = tokenizer_.nextToken();

            if (! token.startsWith (LPAREN_)) {
                selectTable_ = token;
                parseFrom2 ();
            }
        }
    }



/**
Parses the token after a FROM table.
**/
    private void parseFrom2 ()
    {
        if (tokenizer_.hasMoreTokens ()) {
            String token = tokenizer_.nextToken().toUpperCase ();

            if (token.equals (AS_))
                parseFromAs ();
            else if (token.equals (FOR_))
                parseFor ();
        }
    }



/**
Parses the token after a FROM table AS.  The next token is the
correlation name.
**/
    private void parseFromAs ()
    {
        if (tokenizer_.hasMoreTokens ()) 
            correlationName_ = tokenizer_.nextToken();        
    }



/**
Returns the SQL statement as a String.  This will be
native SQL if conversion was requested.

@return     The string, optionally native SQL.
**/
    public String toString ()
    {
        return value_.trim();
    }



}
