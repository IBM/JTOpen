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

    public static final String DEFAULT_DELIMITERS = " \t\n\r\f";

    private static final int TOKEN_TYPE_DELIMITER = 1;
    private static final int TOKEN_TYPE_COMMENT = 2;
    private static final int TOKEN_TYPE_LITERAL = 4;
    private static final int TOKEN_TYPE_SQL = 8;

    private char[] buffer;
    private char[] delimiters;

    private SQLToken[] tokens;
    private int currentTokenIndex;

    private int pos = 0;
    
    private boolean returnDelimiters = true;
    private boolean returnComments = true;

    private int numberOfParameters = 0;
    
    /**
    Constructs a JDSQLTokenizer with the default delimiter
    String of " \t\n\r\f".

    @param  statement               SQL statement to tokenize.
    **/
    public JDSQLTokenizer(String statement) {
        this(statement, DEFAULT_DELIMITERS, true, true);
    }

    /**
    Constructs a JDSQLTokenizer with the specified delimiter
    String.

    @param  statement               SQL statement to tokenize.
    @param  delimiters              Set of delimiters to use.
    **/
    public JDSQLTokenizer(String statement, String delimiters) {
        this(statement, delimiters, true, true);
    }

    /**
    Constructs a JDSQLTokenizer with the specified delimiter String
    and the specified delimiter/comment behavior.
    
    @param statement                SQL statement to tokenize.
    @param delimiters               Set of delimiters to use.
    @param returnDelimiters         true if we should return delimiters as tokens
    @param returnComments           true if we should return comments as tokens
    **/
    public JDSQLTokenizer(String statement, String delimiters, boolean returnDelimiters, boolean returnComments) {
        buffer = statement.toCharArray();
        this.delimiters = delimiters.toCharArray();
        this.returnDelimiters = returnDelimiters;
        this.returnComments = returnComments;

        tokens = scanForTokens();
        currentTokenIndex = -1;
    }

    /**
    Returns the next token in the SQL string, ignoring delimiters within
    SQL style comments, including nested block comments. The delimiters
    and comments may be returned as tokens.

    @return The next token in the SQL String.
    **/
    public String nextToken() {
        if (!hasMoreTokens())
            throw new NoSuchElementException();

        return tokens[++currentTokenIndex].getToken();
    }

    /**
    Returns the next token in the SQL string without actually moving the
    tokenizer ahead as nextToken() does.
    
    @return The next token in the SQL String.
    **/
    public String peekToken() {
        if (!hasMoreTokens())
            throw new NoSuchElementException();

        return tokens[currentTokenIndex+1].getToken();
    }

    /**
    Returns true if there are more tokens in the SQL String.

    @return true if there are more tokens.
    **/
    public boolean hasMoreTokens() {
        if (tokens.length > currentTokenIndex+1)
            return true;
        else
            return false;
    }

    /**
    Returns the number of tokens in the SQL string.
    
    @return Number of tokens in the SQL string.
    **/
    public int countTokens() {
        return tokens.length;
    }

    /**
    Returns true if there are more tokens in the SQL String.
    
    @return true if there are more tokens to be returned.
    **/
    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    /**
    Returns the next token in the SQL string, ignoring delimiters within
    SQL style comments, including nested block comments.  The delimiters
    and comments may be returned as tokens.
    
    @return The next token in the SQL String.
    **/
    public Object nextElement() {
        return nextToken();
    }

    /**
    Returns a list of the tokens after scanning the String, with no separators.
    
    @return The String representation of this tokenizer.
    **/
    public String toString() {
        StringBuffer contents = new StringBuffer();
        for (int i=0; i<tokens.length; ++i) {
            contents.append(tokens[i].getToken());
        }
        return contents.toString();
    }

    /**
    Returns the number of parameter markers found in the SQL tokens of this SQL string.
    
    @return The number of parameter markers found.
    **/
    public int getNumberOfParameters() {
        return numberOfParameters;
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

    /* scans the SQL statement for delimiters and stores each tokens info in an SQLToken object */
    private SQLToken[] scanForTokens() {
        Vector tokens = new Vector();
        
        pos = 0;
        while (pos < buffer.length) {
            while (pos < buffer.length) {
                // if we enter one of the if/else ifs below we are entering or leaving a comment or quoted literal
                // each block increments the pos pointer and continues to the top of the loop in order to avoid
                // possibility that one of the delimeters is at the position of pos and should not be returned
                if (pos+1 < buffer.length && buffer[pos] == '/' && buffer[pos+1] == '*') {
                    // entering one level of block comment
                    SQLToken tok = scanBlockComment();
                    if (returnComments)
                        tokens.add(tok);
                } else if (pos+1 < buffer.length && buffer[pos] == '-' && buffer[pos+1] == '-') {
                    // entering single line comment
                    SQLToken tok = scanSLComment();
                    if (returnComments)
                        tokens.add(tok);
                } else if (buffer[pos] == '\'') {
                    // entering single quote
                    tokens.add(scanSQLiteral());
                } else if (buffer[pos] == '"') {
                    // entering double quote
                    tokens.add(scanDQLiteral());
                } else if (isDelimiter(pos)) {
                    // character at pos is a delimiter
                    SQLToken tok = scanDelimiter();
                    if (returnDelimiters)
                        tokens.add(tok);
                } else {
                    // character at pos is not any of the above
                    tokens.add(scanSQL());
                }
            }
        }
        return (SQLToken[])tokens.toArray(new SQLToken[]{});
    }

    private SQLToken scanBlockComment() {
        int start = pos;
        int length = 0;
        int type = TOKEN_TYPE_COMMENT;

        int commentDepth = 0;

        // scan to the end of the block comment
        do {
            if (pos+1 < buffer.length && buffer[pos] == '/' && buffer[pos+1] == '*') {
                ++commentDepth;
            } else if (pos+1 < buffer.length && buffer[pos] == '*' && buffer[pos+1] == '/') {
                --commentDepth;
            }
            ++pos;
        } while (pos < buffer.length && commentDepth > 0);

        // increment past the trailing / of the block comment
        ++pos;
        length = pos - start;
        
        return new SQLToken(start, length, type);
    }

    private SQLToken scanSLComment() {
        int start = pos;
        int length = 0;
        int type = TOKEN_TYPE_COMMENT;

        // scan to the end of the line
        while (pos < buffer.length && buffer[pos] != '\n') {
            ++pos;
        }

        if (pos >= buffer.length) {
            length = pos - start;
        } else {
            ++pos;
            length = pos - start;            
        }

        return new SQLToken(start, length, type);
    }

    private SQLToken scanSQLiteral() {
        int start = pos;
        int length = 0;
        int type = TOKEN_TYPE_LITERAL;

        // scan to the end of the single quote literal
        do {
            ++pos;
        } while (pos < buffer.length && buffer[pos] != '\'' );

        if (pos >= buffer.length) {
            length = pos - start;
        } else {
            ++pos;
            length = pos - start;
        }

        return new SQLToken(start, length, type);
    }

    private SQLToken scanDQLiteral() {
        int start = pos;
        int length = 0;
        int type = TOKEN_TYPE_LITERAL;

        // scan to the end of the double quote literal
        do {
            ++pos;
        } while (pos < buffer.length && buffer[pos] != '"' );

        if (pos >= buffer.length) {
            length = pos - start;
        } else {
            ++pos;
            length = pos - start;
        }

        return new SQLToken(start, length, type);
    }

    private SQLToken scanDelimiter() {
        int start = pos;
        int length = 1;
        int type = TOKEN_TYPE_DELIMITER;
        
        // increment the pos past the delimiter
        ++pos;

        return new SQLToken(start, length, type);
    }

    private SQLToken scanSQL() {
        int start = pos;
        int length = 0;
        int type = TOKEN_TYPE_SQL;

        // scan up to the next delimiter
        while (pos < buffer.length && !isDelimiter(pos) && buffer[pos] != '\'' && buffer[pos] != '"') {
            if (pos+1 < buffer.length &&
                ((buffer[pos] == '/' && buffer[pos+1] == '*') ||
                (buffer[pos] == '-' && buffer[pos+1] == '-'))) {
                // we break out of the loop because this the start of a comment
                break;
            } else {
                if (buffer[pos] == '?')
                    ++numberOfParameters;
                ++pos;
            }            
        }
        length = pos - start;

        return new SQLToken(start, length, type);
    }

    /* SQLToken inner class stores information about
       the location of tokens inside of the SQL
       statement for this JDSQLTokenizer object */
    private class SQLToken {
        int start;
        int length;
        int type;

        public SQLToken(int start, int length, int type) {
            this.start = start;
            this.length = length;
            this.type = type;
        }

        public String getToken() {
            return new String(buffer, start, length);
        }

        public int getType() {
            return type;
        }
    }
}
