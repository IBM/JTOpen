///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDEscapeClause.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;



/**
<p>This class represents an escape clause in a SQL statement.
It is used to translate SQL statements with JDBC escape
syntax to DB2 for IBM i format.
**/
class JDEscapeClause
{
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

    // Scalar function parsing table.
    private static JDScalarTable    scalarFunctionTable_;

    /**
    Static initializer.  Initialize the scalar function table
    and the supported function lists.
    **/
    // @C1M Changed the static initializer to use the new JDScalarTable and provide earliest supported versions
    static {
        // create a new table
        scalarFunctionTable_ = new JDScalarTable();

        // Numeric functions.
        //
        // Supported by system:
        // V4R5:
        //    abs,acos,asin,atan,atan2,ceiling,cos,cot,degrees,exp,floor,log10,mod,power,round,sin,sign,sqrt,tan,truncate
        // >=V5R1:
        //    abs,acos,asin,atan,atan2,ceiling,cos,cot,degrees,exp,floor,log10,mod,power,radians,rand,round,sin,sign,sqrt,tan,truncate
        //
        // Supported by mapping:
        //    log,pi
        //

        scalarFunctionTable_.put("pi", "3%d1415926E00", JDUtilities.vrm510);
        scalarFunctionTable_.put("log", "LN(%1)", JDScalarTable.NOT_SUPPORTED);

        // String functions.
        //
        // Supported by system:
        // V4R5:
        //    concat,left,locate,ltrim,rtrim,substring,ucase
        // V5R1:
        //    concat,difference,left,locate,ltrim,rtrim,soundex,space,substring,ucase
        // >=V5R2:
        //    concat,difference,insert,lcase,left,locate,ltrim,repeat,replace,right,rtrim,soundex,space,substring,ucase
        // 
        // Supported by mapping:
        // <=V5R2:
        //    insert,length,right
        // V5R3:
        //    length
        //
        // Not supported:
        //    ascii,char
        //
        scalarFunctionTable_.put("insert", "SUBSTR(%1, 1, %2 - 1) || %4 || SUBSTR(%1, %2 + %3)", JDUtilities.vrm530);
        scalarFunctionTable_.put("right", "SUBSTR(%1, LENGTH(%1) - %2 + 1)", JDUtilities.vrm530);
        scalarFunctionTable_.put("length", "LENGTH(STRIP(%1,T))", JDScalarTable.NOT_SUPPORTED);

        // System functions.
        //
        // Supported by system:
        //    ifnull,user
        //
        // Supported by mapping:
        //    database            
        //
        scalarFunctionTable_.put("database", "CURRENT SERVER", JDUtilities.vrm530);
        // we map the below function because if it is simply passed through it will end up USER()
        // instead of USER
        scalarFunctionTable_.put("user", "USER", JDScalarTable.NOT_SUPPORTED);

        // Time and date functions.
        //
        // Supported by system:
        //    curdate,curtime,dayname,dayofmonth,dayofweek,dayofyear,hour,minute,month,monthname,now,quarter,second,timestampdiff,week,year
        //
        // Not supported:
        //    timestampadd
        //
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
                         String decimalSeparator,
                         int vrm) // @C1M accept vrm
        throws SQLException
    {
        // Tokenize the string and pass it to the other
        // parse method (which may end up being called
        // recursively.
        // @C4D StringTokenizer tokenizer = new StringTokenizer (escapeSyntax,
        // @C4D     "{}'\"", true);
        JDSQLTokenizer tokenizer = new JDSQLTokenizer(escapeSyntax, "{}'\""); // @C4A
        return parse (tokenizer, decimalSeparator, true, vrm);   // @C1M pass vrm
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
    // @C4D private static String parse (StringTokenizer tokenizer,
    private static String parse (JDSQLTokenizer tokenizer,
                          String decimalSeparator,
                          boolean flag, int vrm) // @C1M accept vrm
        throws SQLException
    {
        // Initialize.
        StringBuffer buffer = new StringBuffer ();
        boolean quotes = false;
        char quoteType = ' ';

        // Iterate through the tokens...
        while (tokenizer.hasMoreTokens ()) {
            String token = tokenizer.nextToken();
            // If the token is a left brace (and we are not in
            // quotes), then recursively parse the escape clause.
            if (token.equals ("{")) {
                if (quotes)
                    buffer.append (token);
                else
                    buffer.append (parse (tokenizer, decimalSeparator, false, vrm)); // @C1M pass vrm
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
                    return convert (buffer.toString (), decimalSeparator, vrm);   // @C1M pass vrm
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
                buffer.append(token);
            }

            // Anything else, just add it to the buffer.
            else
                buffer.append(token);
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
                                   String decimalSeparator, int vrm) // @C1M accept vrm
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
			keyword = trimmed;  // @C3M
			value = "";
		}
		else {
			keyword = trimmed.substring (0, i);  // @C3M
			value = trimmed.substring (i+1);
		}

		// Handle stored procedures.
		if ((keyword.equalsIgnoreCase (CALL_))             // @C3M
		    || (keyword.equalsIgnoreCase (CALL1_))         // @C3M
			|| (keyword.equalsIgnoreCase (CALL2_))     // @C3M
			|| (keyword.equalsIgnoreCase (CALL3_))) {  // @C3M
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
            buffer.append (tokenizer.nextToken().replace(':', '.'));
            /* @B1D - This will never happen, since we already counted tokens.
            while (tokenizer.hasMoreTokens ()) {
                buffer.append (' ');
                buffer.append (tokenizer.nextToken());
            } */
        }

		// Handle scalar functions.
		else if (keyword.equalsIgnoreCase (FN_))
		    buffer.append (convertScalarFunctionCall (value, decimalSeparator, vrm)); // @C1M pass vrm

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
    // @C1M changed parsing to support nested scalars and changed function mapping
    private static String convertScalarFunctionCall (String functionCall, String decimalSeparator, int vrm)
        throws SQLException {
        // Parse the function call into its pieces.
        int i = functionCall.indexOf('(');
        // find the last index of the closing parenthesis to make        // @C0A
        // sure we get the whole function call including nested calls    // @C0A
        int j = functionCall.lastIndexOf(')');                           // @C0M

        String functionName = null;
        String argumentString = null;

        // get the function name and arg string
        //if ((i == -1) && (j == -1)) {                                // @C2D
        //    functionName = functionCall.trim().toLowerCase();        // @C2D
        //    argumentString = "";                                     // @C2D
        //} else if ((i < j) && (i != -1) && (j != -1)) {              // @C2D
        if ((i < j) && (i != -1) && (j != -1)) {                       // @C2A
            functionName = functionCall.substring (0, i).trim().toLowerCase();
            argumentString = functionCall.substring (i+1, j).trim();
        } else
            JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);

        // get the mapped function or just pass it through
        if (!scalarFunctionTable_.contains(functionName, vrm)){                                                // @C1A
            // we dont map this function so just pass it through                                               // @C1A
            return functionCall;                                                                               // @C1A
        } else {                                                                                               // @C1A
            // Check for text after the right parenthesis.                                                     // @C1A
            if (j != -1 && j+1 < functionCall.length() && functionCall.substring(j+1).trim().length() > 0)     // @C1A
                JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);                                          // @C1A
                                                                                                               // @C1A
            // Parse the argument string into arguments.                                                       // @C1A
            Vector arguments = new Vector();                                                                   // @C1A
            if (argumentString.length() > 0) {                                                                 // @C1A
                StringTokenizer atok = new StringTokenizer(argumentString, "(),", true);                       // @C1A
                StringBuffer tokbuf = new StringBuffer();                                                      // @C1A
                int nestlevel = 0;                                                                             // @C1A
                while (atok.hasMoreTokens()) {                                                                 // @C1A
                    String token = atok.nextToken();                                                           // @C1A
                    // check if the argument has parenthesis, meaning it could be a nested fcn                 // @C1A
                    if (token.equals("(")) {                                                                   // @C1A
                        ++nestlevel;                                                                           // @C1A
                        tokbuf.append("(");                                                                    // @C1A
                    } else if (token.equals(")")) {                                                            // @C1A
                        // find a closing paren and decrement the nest count                                   // @C1A
                        --nestlevel;                                                                           // @C1A
                        tokbuf.append(")");                                                                    // @C1A
                        // if we have no more tokens after this one we add the argument to the list            // @C1A
                        if (!atok.hasMoreTokens()) {                                                           // @C1A
                            arguments.add(tokbuf.toString());                                                  // @C1A
                        }                                                                                      // @C1A
                    } else if (token.equals(",")) {                                                            // @C1A
                        // find a comma                                                                        // @C1A
                        if (nestlevel == 0) {                                                                  // @C1A
                            // if the nest count is zero we add the argument to the list because it is actually a delimeter
                            arguments.add(tokbuf.toString());                                                   // @C1A
                            tokbuf = new StringBuffer();                                                        // @C1A
                        } else {                                                                                // @C1A
                            // if the nest count is not zero the comma is not a delimeter so just append to the buffer
                            tokbuf.append(",");                                                                 // @C1A
                        }                                                                                       // @C1A
                    } else {                                                                                    // @C1A
                        // token is a special case so just add it to the buffer                                 // @C1A
                        tokbuf.append(token);                                                                   // @C1A
                        // there are no more tokens left so add the argument to the list                        // @C1A
                        if (!atok.hasMoreTokens()) {                                                            // @C1A
                            arguments.add(tokbuf.toString());                                                   // @C1A
                        }                                                                                       // @C1A
                    }                                                                                           // @C1A
                }                                                                                               // @C1A
            }                                                                                                   // @C1A
                                                                                                                // @C1A
            // Get the native SQL from the scalar function table.                                               // @C1A
            StringBuffer buffer = new StringBuffer ();
            String nativeSQL = scalarFunctionTable_.get(functionName, vrm).toString();                          // @C1M

            // Handle the substitution variables.
            int marker = 0;
            int nextPercent = 0;
            int highestArgumentNumber = 0;
            while (true) {

                // Find the next % and substitution code (the digit
                // after the %.
                nextPercent = nativeSQL.indexOf ('%', marker);
                if ((nextPercent == -1) || (nextPercent == nativeSQL.length() - 1)) {
                    buffer.append (nativeSQL.substring (marker));
                    break;
                }
                buffer.append (nativeSQL.substring (marker, nextPercent));
                char substitutionCode = nativeSQL.charAt (nextPercent + 1);

                // If an invalid substitution code, then it is a
                // syntax error.  Otherwise, do the substitution.
                if (Character.isDigit (substitutionCode)) {
                    int argumentNumber = Character.digit (substitutionCode, 10);
                    if (argumentNumber > arguments.size())
                        JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);
                    if (argumentNumber > highestArgumentNumber)
                        highestArgumentNumber = argumentNumber;
                    buffer.append (arguments.elementAt(argumentNumber-1));
                }
                else if (substitutionCode == 'd') {
                    buffer.append (decimalSeparator);
                }

                // Increment the marker past the substitution code.
                marker = nextPercent + 2;
            }

            // Check that the number of arguments is what we expected.
            if (highestArgumentNumber != arguments.size())
                JDError.throwSQLException (JDError.EXC_SYNTAX_ERROR);

            return buffer.toString();
        }
    }



/**
Get a list of supported math functions.

@param  vrm                 The version of the host OS.
@return                     A list of function names, separated by commas.
**/
    static String getNumericFunctions(int vrm) {
        // @C1A the below if/else block was added to report the correct functions through DatabaseMetaData
        if (vrm < JDUtilities.vrm510) {
            // we are running to a V4R5 or older host
            return "abs,acos,asin,atan,atan2,ceiling,cos,cot,degrees,exp,floor,log,log10,mod,pi,power,round,sin,sign,sqrt,tan,truncate";
        } else {
            // we are running to a V5R1 or newer host 
            return "abs,acos,asin,atan,atan2,ceiling,cos,cot,degrees,exp,floor,log,log10,mod,pi,power,radians,rand,round,sin,sign,sqrt,tan,truncate";
        }
    }


/**
Get a list of supported string functions.

@param  vrm                 The version of the host OS.
@return                     A list of function names, separated by commas.
**/
    static String getStringFunctions(int vrm) {
        // @C1A the below if/else block was added to report the correct functions through DatabaseMetaData
        if (vrm < JDUtilities.vrm510) {
            // we are running to a V4R5M0 or older host
            return "concat,insert,left,length,locate,ltrim,right,rtrim,substring,ucase";
        } else if (vrm < JDUtilities.vrm520) {
            // we are running to a V5R1 host
            return "concat,difference,insert,left,length,locate,ltrim,right,rtrim,soundex,space,substring,ucase";
        } else if (vrm < JDUtilities.vrm530) {
            // we are running to a V5R2 host
            return "char,concat,difference,insert,lcase,left,length,locate,ltrim,right,rtrim,soundex,space,substring,ucase";
        } else {
            // we are running to a V5R3 or newer host
            return "char,concat,difference,insert,lcase,left,length,locate,ltrim,repeat,replace,right,rtrim,soundex,space,substring,ucase";
        }
    }


/**
Get a list of supported system functions.

@param  vrm                 The version of the host OS.
@return                     A list of function names, separated by commas.
**/
    static String getSystemFunctions(int vrm) {
        return "database,ifnull,user"; // @C1A added to report the correct functions through DatabaseMetaData
    }


/**
Get a list of supported time and date functions.

@param  vrm                 The version of the host OS.
@return                     A list of function names, separated by commas.
**/
    static String getTimeDateFunctions(int vrm) {
        // @C1A the below if/else block was added to report the correct functions through DatabaseMetaData
        if (vrm < JDUtilities.vrm510) {
            // we are running to a V4R5 or older host
            return "curdate,curtime,dayofmonth,dayofweek,dayofyear,hour,minute,month,now,quarter,second,week,year";
        } else if (vrm < JDUtilities.vrm530) {
            // we are running to a V5R1 or V5R2 host
            return "curdate,curtime,dayofmonth,dayofweek,dayofyear,hour,minute,month,now,quarter,second,timestampdiff,week,year";
        } else {
            // we are running to a V5R3 or newer host
            return "curdate,curtime,dayname,dayofmonth,dayofweek,dayofyear,hour,minute,month,monthname,now,quarter,second,timestampdiff,week,year";
        }
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
// @C1D removed this method because it is no longer needed
//	private static void initializeScalarFunction (String functionName, String nativeSql, int vrmSupported, StringBuffer functionList)
//	{
//		// Add to scalar function table.
//		scalarFunctionTable_.put (functionName, nativeSql, vrmSupported);
//
//		// Add to the function list.
//		if (functionList.length() > 0)
//			functionList.append (',');
//		functionList.append (functionName);
//	}




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


    /**
    <p>This class is a hashtable-like container for mapping JDBC to
    native scalar functions.
    **/
    // @C1A Added this inner class to store mappings
    private static final class JDScalarTable {
    
        public static final int NOT_SUPPORTED = 0;
        private static final int HASH = 10;
    
        private String[][] keys = new String[HASH][];
        private String[][] data = new String[HASH][];
        private int[][] vrms = new int[HASH][];
    
        /**
        Returns true if the function corresponding to key should
        be mapped for this VRM of the host.
        
        @param key The function key
        @param vrm The VRM of the host
        @return true if we should get the mapped function from the table
        **/
        final boolean contains(String key, int vrm) {
            if (key == null) throw new NullPointerException("key");
            int hash = (key.hashCode()<0?key.hashCode()*-1:key.hashCode()) % HASH;
            String[] keyChain = keys[hash];
            int[] vrmChain = vrms[hash];
            if (keyChain == null) return false;
            if (vrmChain == null) return false;
            for (int i=0; i<keyChain.length; ++i) {
                // checks the keys that satisfy the hash function for the current vrm and if one exists, returns true
                if (keyChain[i] != null && keyChain[i].equals(key) && (vrmChain[i] > vrm || vrmChain[i] == NOT_SUPPORTED)) {
                    return true;
                }
            }
            return false;
        }
    
        /**
        Returns the function corresponding to the key and VRM.
        
        @param key The function key
        @param vrm The VRM of the host
        @return null if the key/vrm combination is not mapped
        **/
        final String get(String key, int vrm) {
            if (key == null) throw new NullPointerException("key");
            int hash = (key.hashCode()<0?key.hashCode()*-1:key.hashCode()) % HASH;
            synchronized(keys) {
                String[] keyChain = keys[hash];
                int[] vrmChain = vrms[hash];
                if (keyChain == null) return null;
                if (vrmChain == null) return null;
                for (int i=0; i<keyChain.length; ++i) {
                    // searches the keys that satisfy the hash function and returns a match if one exists
                    if (keyChain[i] != null && keyChain[i].equals(key) && (vrmChain[i] > vrm || vrmChain[i] == NOT_SUPPORTED)) {
                        return data[hash][i];
                    }
                }
            }
            return null;
        }
    
        /**
        Sets the JDBC to native function mapping.  The VRM indicates the earliest
        version of host to support the function.
        
        @param key The function key
        @param value The map for the key
        @param vrm The VRM of the host
        **/
        final void put(String key, String value, int vrm) {
            if (key == null) throw new NullPointerException("key");
            if (vrm < 0) throw new IllegalArgumentException("vrm");
            int hash = (key.hashCode()<0?key.hashCode()*-1:key.hashCode()) % HASH;
            synchronized(keys)
            {
                String[] valueChain = data[hash];
                String[] keyChain = keys[hash];
                int[] vrmChain = vrms[hash];
                if (keyChain == null) {  // there are currently no keys in this chain of the hashtable so create a new chain
                    keyChain = new String[] { key };  // create a new key chain
                    valueChain = new String[] { value };  // create a new value chain
                    vrmChain = new int[] { vrm };  // create a new vrm chain
                    keys[hash] = keyChain;         // set the key, value, and vrm
                    data[hash] = valueChain;
                    vrms[hash] = vrmChain;
                    return;
                } else {  // keys exist in this chain of the hashtable so add this one to the chain
                    int len = keyChain.length;
                    for (int i=0; i<len; ++i) {
                        // this key already exists in the chain so set its new value
                        if (keyChain[i] != null && keyChain[i].equals(key)) {
                            valueChain[i] = value;
                            vrmChain[i] = vrm;
                            return;
                        }
                        // a chain already exists for this hash but its value is null
                        if (keyChain[i] == null) {
                            keyChain[i] = key;
                            valueChain[i] = value;
                            vrmChain[i] = vrm;
                            return;
                        }
                    }
                    // if we have to, make the table bigger and copy the values over
                    String[] newKeyChain = new String[len*2];
                    System.arraycopy(keyChain, 0, newKeyChain, 0, len);
                    String[] newValueChain = new String[len*2];
                    System.arraycopy(valueChain, 0, newValueChain, 0, len);
                    int[] newVRMChain = new int[len*2];
                    System.arraycopy(vrmChain, 0, newVRMChain, 0, len);
                    newKeyChain[len] = key;
                    newValueChain[len] = value;
                    newVRMChain[len] = vrm;
                    // then make the table use the new bigger chains
                    keys[hash] = newKeyChain;
                    data[hash] = newValueChain;
                    vrms[hash] = newVRMChain;
                }
            }
        }
    
    }
}
