///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDSQLTokenizer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.*;

/**
<p>This class allows SQL statements to be tokenized without having
to worry about delimiters appearing in comments.  The tokenizer will
check to make sure the delimiter is not inside a comment before using
it separate tokens.  This tokenizer behaves much like StringTokenizer.
**/

class JDSQLTokenizer extends Object implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";
    
    protected char[] buffer;
    protected char[] delimiters;

    private int currentTokenPtr;
    private int currentTokenLen;

    private int commentDepth = 0;
    private boolean lineComment = false;
    private boolean blockComment = false;
    private boolean singleQuote = false;
    private boolean doubleQuote = false;
    
    /**
    Constructs a JDSQLTokenizer with the default delimiter
    String of " \t\n\r\f".

    @param  statement               SQL statement to tokenize.
    **/
    public JDSQLTokenizer(String statement) {
        this(statement, " \t\n\r\f");
    }

    /**
    Constructs a JDSQLTokenizer with the specified delimiter
    String.

    @param  statement               SQL statement to tokenize.
    @param  delimiters              Set of delimiters to use.
    **/
    public JDSQLTokenizer(String statement, String delimiters) {
        buffer = statement.toCharArray();
        this.delimiters = delimiters.toCharArray();

        currentTokenPtr = 0;
        currentTokenLen = 0;
    }

    /**
    Returns the next token in the SQL string, ignoring delimiters with
    SQL style comments, including nested block comments. The delimiters
    are ALWAYS returned as tokens.

    @return                         The next token in the SQL String.
    **/
    public String nextToken() {
        // return null if we have read all the tokens
        if (!hasMoreTokens())
            throw new NoSuchElementException();

        // find the next delimiter and return everything up to it
        // unless the delimiter IS the next character
        currentTokenPtr = currentTokenPtr + currentTokenLen;
        if (isDelimiter(currentTokenPtr))
            currentTokenLen = 1;
        else {
            int ndelim = findNextDelimiter(currentTokenPtr);
            currentTokenLen = (ndelim == -1 ? buffer.length : ndelim)  - currentTokenPtr;
        }

        return new String(buffer, currentTokenPtr, currentTokenLen);
    }

    /**
    Returns true if there are more tokens in the SQL String.

    @return                         true if there are more tokens.
    **/
    public boolean hasMoreTokens() {
        if (currentTokenPtr + currentTokenLen >= buffer.length)
            return false;
        else
            return true;
    }

    /**
    Returns true if there are more tokens in the SQL String.

    @return                         true if there are more tokens.
    **/
    public boolean hasMoreElements() {
        return hasMoreTokens();
    }
           
    /**
    Returns the next token in the SQL string, ignoring delimiters with
    SQL style comments, including nested block comments. The delimiters
    are ALWAYS returned as tokens. This method returns the token as an
    Object rather than a String.

    @return                         The next token in the SQL String.
    **/
    public Object nextElement()  {
        return nextToken();
    }
          
    /**
    Checks a specified position in the buffer to see if it is one of the delimiter characters.

    @param  position                The index in the buffer to test.
    @return                         true if the character at position is a delimiter.
    **/
    private boolean isDelimiter(int position) {
        for (int i=0; i<delimiters.length; ++i) {
            if (delimiters[i] == buffer[position])
                return true;
        }
        return false;
    }

    /* finds the next delimiter starting at position */
    /**
    Finds the position in the buffer of the next delimiter.  This method first
    checks that the delimiter is not inside a quoted literal or a SQL style
    comment before returning its position as a valid delimiter.

    @param  position                Index to start searching the buffer.
    @return                         Index of the next valid delimiter.
    **/
    private int findNextDelimiter(int position) {
        int pos = position;
        while (pos < buffer.length) {

            // if we enter one of the if/else ifs below we are entering or leaving a comment or quoted literal
            // each block increments the pos pointer and continues to the top of the loop in order to avoid
            // possibility that one of the delimeters is at the position of pos and should not be returned
            if (pos+1 < buffer.length && !singleQuote && !doubleQuote && !lineComment && buffer[pos] == '/' && buffer[pos+1] == '*') {
                // entering one level of block comment
                blockComment = true;
                ++commentDepth;
                ++pos;
                continue;
            } else if (pos+1 < buffer.length && blockComment && buffer[pos] == '*' && buffer[pos+1] == '/') {
                // leaving one level of block comment
                --commentDepth;
                if (commentDepth == 0) {
                    blockComment = false;
                }
                ++pos;
                continue;
            } else if (pos+1 < buffer.length && !singleQuote && !doubleQuote && !blockComment && !lineComment && buffer[pos] == '-' && buffer[pos+1] == '-') {
                // entering single line comment
                lineComment = true;
                ++pos;
                continue;
            } else if (!singleQuote && !doubleQuote && lineComment && buffer[pos] == '\n') {
                // leaving single line comment
                lineComment = false;
                ++pos;
                continue;
            } else if (!singleQuote && !doubleQuote && !blockComment && !lineComment && buffer[pos] == '\'') {
                // entering single quote
                singleQuote = true;
                ++pos;
                continue;
            } else if (singleQuote && !blockComment && !lineComment && buffer[pos] == '\'') {
                // leaving single quote
                singleQuote = false;
                ++pos;
                continue;
            } else if (!singleQuote && !doubleQuote && !blockComment && !lineComment && buffer[pos] == '"') {
                // entering double quote
                doubleQuote = true;
                ++pos;
                continue;
            } else if (doubleQuote && !blockComment && !lineComment && buffer[pos] == '"') {
                // leaving double quote
                doubleQuote = false;
                ++pos;
                continue;
            }

            if (!singleQuote && !doubleQuote && !blockComment && !lineComment && isDelimiter(pos)) {
                return pos;
            }

            ++pos;
        }
        return -1;
    }

}
