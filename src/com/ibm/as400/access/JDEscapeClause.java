///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JDEscapeClause.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.StringTokenizer;



/**
<p>This class represents an escape clause in a SQL statement.
It is used to translate SQL statements with JDBC escape
syntax to DB2 for OS/400 format.
**/
class JDEscapeClause
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // String constants.  These will hopefully help performance
    // slightly - assuming a similar optimization does not
    // already take place.
    private static final String     CALL_           = "CALL";
    private static final String     CALL1_          = "?=";
    private static final String     CALL2_          = "?=CALL";
    private static final String     CALL3_          = "?";
    private static final String     DATE_           = "D";
    private static final String     ESCAPE_         = "ESCAPE";
    private static final String     FN_             = "FN";
    private static final String     OJ_             = "OJ";
    private static final String     TIME_           = "T";
    private static final String     TIMESTAMP_      = "TS";



    // Lists of supported scalar functions.
	private static String		numericFunctions_;
	private static String		stringFunctions_;
	private static String		systemFunctions_;
	private static String		timeDateFunctions_;



    // Scalar function parsing table.
	private static Hashtable	scalarFunctionTable_;



/**
Static initializer.  Initialize the scalar function table
and the supported function lists.
**/
	static {
        // Initialize the hashtable with a capacity that is
        // a prime number not near a power of 2.  It is around
        // 9 times the number of entries, so collisions should
        // be kept to a miniumum.  The load factor is .5 so the
        // table will never rehash.
        //
        scalarFunctionTable_ = new Hashtable (307, 0.5f);

		// Numeric functions.
		//
		// Not supported:
		//    atan2, ceiling, degrees, floor, power, radians, rand, round,
		//    sign, truncate.
		//
		StringBuffer numericFunctions = new StringBuffer ();
		initializeScalarFunction ("abs",        "ABSVAL(%1)",	  numericFunctions);
		initializeScalarFunction ("acos",       "ACOS(%1)",		  numericFunctions);
		initializeScalarFunction ("asin",       "ASIN(%1)",		  numericFunctions);
		initializeScalarFunction ("atan",       "ATAN(%1)",		  numericFunctions);
		initializeScalarFunction ("cos",        "COS(%1)",		  numericFunctions);
		initializeScalarFunction ("cot",        "COT(%1)",		  numericFunctions);
		initializeScalarFunction ("exp",        "EXP(%1)",		  numericFunctions);
		initializeScalarFunction ("log",        "LN(%1)",		  numericFunctions);
		initializeScalarFunction ("log10",      "LOG(%1)",		  numericFunctions);
		initializeScalarFunction ("mod",        "MOD(%1, %2)",	  numericFunctions);
		initializeScalarFunction ("pi",         "3%d1415926E00",  numericFunctions);
		initializeScalarFunction ("sin",        "SIN(%1)",		  numericFunctions);
		initializeScalarFunction ("sqrt",       "SQRT(%1)",		  numericFunctions);
		initializeScalarFunction ("tan",        "TAN(%1)",		  numericFunctions);
		numericFunctions_ = numericFunctions.toString();

		// String functions.
		//
		// Not supported:
		//    ascii, char, difference, lcase, locate, repeat, replace, soundex,
		//    space.
		//
		StringBuffer stringFunctions = new StringBuffer ();
		initializeScalarFunction ("concat",     "(%1 || %2)",           stringFunctions);
		initializeScalarFunction ("insert",     "SUBSTR(%1, 1, %2 - 1) || %4 || SUBSTR(%1, %2 + %3)",
								                                        stringFunctions);
		initializeScalarFunction ("left",       "SUBSTR(%1, 1, %2)",    stringFunctions);
		initializeScalarFunction ("length",     "LENGTH(STRIP(%1,T,' '))",
								                                        stringFunctions);
		initializeScalarFunction ("ltrim",      "STRIP(%1,L,' ')",      stringFunctions);
		initializeScalarFunction ("right",      "SUBSTR(%1, LENGTH(%1) - %2 + 1)",
                                       								    stringFunctions);
		initializeScalarFunction ("rtrim",      "STRIP(%1,T,' ')",      stringFunctions);
		initializeScalarFunction ("substring",  "SUBSTR(%1, %2, %3)",   stringFunctions);
		initializeScalarFunction ("ucase",      "TRANSLATE(%1)",        stringFunctions);
		stringFunctions_ = stringFunctions.toString();

		// System functions.
		//
		// Not supported:
		//    (None).
		//
		StringBuffer systemFunctions = new StringBuffer ();
		initializeScalarFunction ("database",   "CURRENT SERVER",   systemFunctions);
		initializeScalarFunction ("ifnull",     "VALUE(%1, %2)",    systemFunctions);
		initializeScalarFunction ("user",       "USER",             systemFunctions);
		systemFunctions_ = systemFunctions.toString();

		// Time and date functions.
		//
		// Not supported:
		//    dayname, dayofweek, dayofyear, monthname, quarter,
		//    timestampadd, timestampdiff, week.
		//
		StringBuffer timeDateFunctions = new StringBuffer ();

		initializeScalarFunction ("curdate",    "CURRENT DATE",     timeDateFunctions);
		initializeScalarFunction ("curtime",    "CURRENT TIME",     timeDateFunctions);
		initializeScalarFunction ("dayofmonth", "DAY(%1)",          timeDateFunctions);
		initializeScalarFunction ("hour",       "HOUR(%1)",         timeDateFunctions);
		initializeScalarFunction ("minute",     "MINUTE(%1)",       timeDateFunctions);
		initializeScalarFunction ("month",      "MONTH(%1)",        timeDateFunctions);
		initializeScalarFunction ("now",        "CURRENT TIMESTAMP",timeDateFunctions);
		initializeScalarFunction ("second",     "SECOND(%1)",       timeDateFunctions);
		initializeScalarFunction ("year",       "YEAR(%1)",         timeDateFunctions);
		timeDateFunctions_ = timeDateFunctions.toString();
	}



/**
Parses an escape clause, and substitute it with native SQL.
This will recursively parse all nested escape clauses.

@param  escapeSyntax            SQL escape syntax.
@param  decimalSeparator        The decimal separator.
@return                         The parsed string.

@exception  SQLException        If there is a syntax error or
                                a reference to an unsupported
                                scalar function.
**/
    static String parse (String escapeSyntax,
                         String decimalSeparator)
        throws SQLException
    {
        // Tokenize the string and pass it to the other
        // parse method (which may end up being called
        // recursively.
        StringTokenizer tokenizer = new StringTokenizer (escapeSyntax,
            "{}'\"", true);
        return parse (tokenizer, decimalSeparator, true);
    }



/**
Parses an escape clause, and substitute it with native SQL.
This will recursively parse all nested escape clauses.

<p>When the flag is true, the tokenized string is considered
to be the entire SQL statement and will end at the end of
the string.

<p>When the flag is false, the tokenized string is considered
to be just a single escape clause, starting 1 token after
its left brace.  It will end at the matching right brace.

@param  tokenizer               The tokenized string.
@param  decimalSeparator        The decimal separator.
@param  flag                    The flag.

@exception  SQLException        If there is a syntax error or
                                a reference to an unsupported
                                scalar function.
**/
    private static String parse (StringTokenizer tokenizer,
                          String decimalSeparator,
                          boolean flag)
        throws SQLException
    {
        // Initialize.
        StringBuffer buffer = new StringBuffer ();
        boolean quotes = false;
        char quoteType = ' ';

        // Iterate through the tokens...
        while (tokenizer.hasMoreTokens ()) {
            String token = tokenizer.nextToken ();

            // If the token is a left brace (and we are not in
            // quotes), then recursively parse the escape clause.
            if (token.equals ("{")) {
                if (quotes)
                    buffer.append (token);
                else
                    buffer.append (parse (tokenizer, decimalSeparator, false));
            }

            // If the token is a right brace (and we are not in
            // quotes), then this is the end of a clause.
            //
            // If we are parsing the whole string, then this does
            // not have a matching left brace.
            //
            else if (token.equals ("}")) {
                if (quotes)
                    buffer.append (token);
                else if (flag)
                    JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);
                else
                    return convert (buffer.toString (), decimalSeparator);
            }

            // If the token is a quote, then toggle the quote
            // information.
            else if ((token.equals ("'")) || (token.equals ("\""))) {
                if (quotes) {
                    if (quoteType == token.charAt (0))
                        quotes = false;
                }
                else {
                    quotes = true;
                    quoteType = token.charAt (0);
                }
                buffer.append (token);
            }

            // Anything else, just add it to the buffer.
            else
                buffer.append (token);
        }

        // If we have gotten this far and we are just parsing
        // a clause, then there is no closing right brace.
        if (! flag)
            JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);

        return buffer.toString ();
    }


/**
Convert the escape syntax to native SQL.

@param  escapeSyntax            The escape syntax to convert.
                                It is assumed that all leading
                                and trailing blanks are
                                already trimmed.
@param  decimalSeparator        The decimal separator.
@return                         Native SQL.

@exception  SQLException        If there is a syntax error or
                                a reference to an unsupported
                                scalar function.
**/
    private static String convert (String escapeSyntax,
                                   String decimalSeparator)
        throws SQLException
    {
		StringBuffer buffer	= new StringBuffer ();

		// Parse out the keyword and the value.  The value
		// is just the rest of the escape syntax.
		String trimmed = escapeSyntax.trim ();
		int i = trimmed.indexOf (' ');
		String keyword = null;
		String value = null;
		if (i == -1) {
			keyword = trimmed.toUpperCase ();
			value = "";
		}
		else {
			keyword = trimmed.substring (0, i).toUpperCase ();
			value = trimmed.substring (i+1);
		}

		// Handle stored procedures.
		if ((keyword.equals (CALL_))
		    || (keyword.equals (CALL1_))
			|| (keyword.equals (CALL2_))
			|| (keyword.equals (CALL3_))) {
			buffer.append (keyword);
			buffer.append (' ');
			buffer.append (value);
		}

		// Handle date literal.
		//
		// This works because the JDBC escape syntax is the same
		// as ISO format, which works no matter what the server
		// job has for its date format and separator.
		//
		else if (keyword.equalsIgnoreCase (DATE_))
            buffer.append (value);

		// Handle time literal.
		//
		// This works because the JDBC escape syntax is the same
		// as ISO format, which works no matter what the server
		// job has for its date format and separator.
		//
		else if (keyword.equalsIgnoreCase (TIME_))
            buffer.append (value);

		// Handle timestamp literal.
		else if (keyword.equalsIgnoreCase (TIMESTAMP_)) {
		    StringTokenizer tokenizer = new StringTokenizer (value);
	    	if (tokenizer.countTokens() != 2)
	    	    JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);
            buffer.append (tokenizer.nextToken());
            buffer.append ('-');
            buffer.append (tokenizer.nextToken().replace (':', '.'));
            /* @B1D - This will never happen, since we already counted tokens.
            while (tokenizer.hasMoreTokens ()) {
                buffer.append (' ');
                buffer.append (tokenizer.nextToken());
            } */
        }

		// Handle scalar functions.
		else if (keyword.equalsIgnoreCase (FN_))
			buffer.append (convertScalarFunctionCall (value, decimalSeparator));

		// Handle LIKE escape characters.
		else if (keyword.equalsIgnoreCase (ESCAPE_)) {
		    if (value.trim ().length () == 0)
	    	    JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);
			buffer.append (keyword);
			buffer.append (' ');
			buffer.append (value);
		}

		// Handle outer joins.
		else if (keyword.equalsIgnoreCase (OJ_)) {
			buffer.append (value);
		}

		// If none of the keywords matched any that we recognize,
		// then call it a syntax error.
		if (buffer.length() == 0)
			JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);

		return buffer.toString();
    }



/**
Convert a scalar function call to native SQL.

@param  functionCall        Function call.
@param  decimalSeparator    The decimal separator.
@return                     Native SQL.

@exception  SQLException        If there is a syntax error or
                                a reference to an unsupported
                                scalar function.
**/
	private static String convertScalarFunctionCall (String functionCall,
	                                                 String decimalSeparator)
		throws SQLException
	{
		// Parse the function call into its pieces.
		int	i = functionCall.indexOf ('(');
		int	j = functionCall.indexOf (')');

        String functionName = null;
        String argumentString = null;

        // Handle the case where there are no arguments.
        if ((i == -1) && (j == -1)) {
            functionName = functionCall.trim().toLowerCase();
            argumentString = "";
        }

        // Handle the case where there are arguments.
        else if ((i < j) && (i != -1) && (j != -1)) {
		    functionName = functionCall.substring (0, i).trim().toLowerCase();
		    argumentString = functionCall.substring (i+1, j).trim();

		    // Check for text after the right parenthesis.
		    if (j+1 < functionCall.length ())
    		    if (functionCall.substring (j+1).trim().length() > 0)
    			    JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);
        }

        // Otherwise, there is a syntax error.
        else
			JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);

		// Parse the argument string into arguments.
		String[] arguments;
		if (argumentString.length() > 0) {
    		StringTokenizer argumentTokenizer = new StringTokenizer (argumentString, ",", false);
	    	arguments = new String[argumentTokenizer.countTokens()];
		    for (int t = 0; t < arguments.length; ++t)
			    arguments[t] = argumentTokenizer.nextToken();
		}
		else
		    arguments = new String[0];

		// Get the native SQL from the scalar function table.
		StringBuffer buffer	= new StringBuffer ();
		if (! scalarFunctionTable_.containsKey (functionName))
			JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);
		String nativeSQL = scalarFunctionTable_.get (functionName).toString();

		// Handle the substitution variables.
		int marker = 0;
		int nextPercent = 0;
		int highestArgumentNumber = 0;
		while (true) {

		    // Find the next % and substitution code (the digit
		    // after the %.
			nextPercent = nativeSQL.indexOf ('%', marker);
			if ((nextPercent == -1)
			    || (nextPercent == nativeSQL.length() - 1)) {
				buffer.append (nativeSQL.substring (marker));
				break;
			}
			buffer.append (nativeSQL.substring (marker, nextPercent));
			char substitutionCode = nativeSQL.charAt (nextPercent + 1);

			// If an invalid substitution code, then it is a
			// syntax error.  Otherwise, do the substitution.
			if (Character.isDigit (substitutionCode)) {
    			int argumentNumber = Character.digit (substitutionCode, 10);
    			if (argumentNumber > arguments.length)
	    			JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);
	            if (argumentNumber > highestArgumentNumber)
	                highestArgumentNumber = argumentNumber;
		    	buffer.append (arguments[argumentNumber-1]);
		    }
		    else if (substitutionCode == 'd') {
   		        buffer.append (decimalSeparator);
		    }

		    // Increment the marker past the substitution code.
			marker = nextPercent + 2;
		}

        // Check that the number of arguments is what we expected.
        if (highestArgumentNumber != arguments.length)
            JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);

		return buffer.toString();
	}



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



/**
Get a list of supported math functions.

@return     A list of function names, separated by commas.
**/
	static String getNumericFunctions ()
	{
		return numericFunctions_;
	}



/**
Get a list of supported string functions.

@return     A list of function names, separated by commas.
**/
	static String getStringFunctions ()
	{
		return stringFunctions_;
	}



/**
Get a list of supported system functions.

@return     A list of function names, separated by commas.
**/
	static String getSystemFunctions ()
	{
		return systemFunctions_;
	}



/**
Get a list of supported time and date functions.

@return     A list of function names, separated by commas.
**/
	static String getTimeDateFunctions ()
	{
		return timeDateFunctions_;
	}



/**
Add the specified scalar function and native SQL to
the scalar function table and the specified function
list.

@param  functionName    Name of scalar function
@param  nativeSQL       Native SQL to translate function call
                        into.  This string can contain
                        substitution variables, which are
                        % followed by a substitution code.
                        A numeric substitution code (e.g. %1,
                        %2, %3) marks where argument
                        will be copied to during translation.
                        %d marks where the decimal separator
                        should appear.
@param  functionList    Function list to which to append
                        this supported scalar function.
**/
	private static void initializeScalarFunction (String functionName,
								 				  String nativeSql,
												  StringBuffer functionList)
	{
		// Add to scalar function table.
		scalarFunctionTable_.put (functionName, nativeSql);

		// Add to the function list.
		if (functionList.length() > 0)
			functionList.append (',');
		functionList.append (functionName);
	}




/**
Is the CONVERT function between SQL types supported?

@return     false
**/
    static boolean supportsConvert ()
    {
        // We do not support this.
		return false;
    }



/**
Is CONVERT between the given SQL types supported?

@param      fromType        SQL type code defined in java.sql.Types.
@param      toType          SQL type code defined in java.sql.Types.
@return     false (We currently do not support CONVERT
            at all.
**/
    static boolean supportsConvert (int fromType, int toType)
    {
        // We do not support this.
		return false;
    }



}
