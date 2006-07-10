///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDSQLTokenizer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.*;

/**
 * This class allows SQL statements to be tokenized without having
 * to worry about delimiters appearing in comments.  The tokenizer will
 * check to make sure the delimiter is not inside a comment before using
 * it to separate tokens.  This tokenizer behaves much like StringTokenizer.
**/
class JDSQLTokenizer implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  /**
  Constant containing the default delimiters for SQLTokenizers.
  **/
  public static final String DEFAULT_DELIMITERS = " \t\n\r\f";

  // Performance improvement when using default delimiters.
  private static final char[] INTERNAL_DELIMITERS = DEFAULT_DELIMITERS.toCharArray();

  /**
  Constant indicating the token is a delimiter.
  **/
  private static final int TOKEN_TYPE_DELIMITER = 1;

  /**
  Constant indicating the token is a comment.
  **/
  private static final int TOKEN_TYPE_COMMENT = 2;

  /**
  Constant indicating the token is a literal.
  **/
  private static final int TOKEN_TYPE_LITERAL = 4;

  /**
  Constant indicating the token is part of the SQL statement.
  **/
  private static final int TOKEN_TYPE_SQL = 8;

  private JDSQLToken[] tokens_;
  private int numberOfParameters_;

  private int currentTokenIndex_;

  /**
  Constructs a JDSQLTokenizer with the default delimiter
  String of " \t\n\r\f".

  @param  statement               SQL statement to tokenize.
  **/
  public JDSQLTokenizer(String statement)
  {
    this(statement, DEFAULT_DELIMITERS, true, true);
  }

  /**
  Constructs a JDSQLTokenizer with the specified delimiter
  String.

  @param  statement               SQL statement to tokenize.
  @param  delimiters              Set of delimiters to use.
  **/
  public JDSQLTokenizer(String statement, String delimiters)
  {
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
  public JDSQLTokenizer(String statement, String delimiters, boolean returnDelimiters, boolean returnComments)
  {
    final char[] delims = (delimiters == DEFAULT_DELIMITERS ? INTERNAL_DELIMITERS : delimiters.toCharArray());
    tokens_ = scanForTokens(statement, delims, returnComments, returnDelimiters);
    numberOfParameters_ = 0;
    for (int i=0; i<tokens_.length; ++i)
    {
      numberOfParameters_ += tokens_[i].parms_;
    }
    currentTokenIndex_ = 0;
  }

  /**
  Returns the number of tokens in the SQL string.
  
  @return Number of tokens in the SQL string.
  **/
  public int countTokens()
  {
    return tokens_.length;
  }

  /**
  Returns the number of parameter markers found in the SQL tokens of this SQL string.
  
  @return The number of parameter markers found.
  **/
  public int getNumberOfParameters()
  {
    return numberOfParameters_;
  }

  /**
  Returns true if there are more tokens in the SQL String.
  
  @return true if there are more tokens to be returned.
  **/
  public boolean hasMoreElements()
  {
    return hasMoreTokens();
  }

  /**
  Returns true if there are more tokens in the SQL String.

  @return true if there are more tokens.
  **/
  public boolean hasMoreTokens()
  {
    return (currentTokenIndex_ < tokens_.length);
  }

  /**
  Checks a specified position in the buffer to see if it is one of the delimiter characters.

  @param  position                The index in the buffer to test.
  @return                         true if the character at position is a delimiter.
  **/
  private static final boolean isDelimiter(final char c, final char[] delimiters)
  {
    for (int i=0; i<delimiters.length; ++i)
    {
      if (delimiters[i] == c) return true;
    }
    return false;
  }

  /**
  Returns the next token in the SQL string, ignoring delimiters within
  SQL style comments, including nested block comments.  The delimiters
  and comments may be returned as tokens.
  
  @return The next token in the SQL String.
  **/
  public Object nextElement()
  {
    return nextToken();
  }

  /**
  Returns the next token in the SQL string, ignoring delimiters within
  SQL style comments, including nested block comments. The delimiters
  and comments may be returned as tokens.

  @return The next token in the SQL String.
  **/
  public String nextToken()
  {
    if (!hasMoreTokens()) throw new NoSuchElementException();

    return tokens_[currentTokenIndex_++].getToken();
  }

  /**
  Returns the next token in the SQL string without actually moving the
  tokenizer ahead as nextToken() does.
  
  @return The next token in the SQL String.
  **/
  public String peekToken()
  {
    if (!hasMoreTokens()) throw new NoSuchElementException();

    return tokens_[currentTokenIndex_].getToken();
  }

  /**
  Scans the SQL statement for delimiters and stores each tokens info in an SQLToken object.
  
  @return                         Array of SQLToken objects containing token information.
  **/
  private static final JDSQLToken[] scanForTokens(final String sql, final char[] delimiters, final boolean returnComments, final boolean returnDelimiters)
  {
    final JDSQLTokenList tokens = new JDSQLTokenList();
    final char[] buffer = sql.toCharArray();
    int offset = 0;
    final int bufferLen = buffer.length;
    while (offset < bufferLen)
    {
      // if we enter one of the if/else ifs below we are entering or leaving a comment or quoted literal
      // each block increments the pos pointer and continues to the top of the loop in order to avoid
      // possibility that one of the delimeters is at the position of pos and should not be returned
      final int p = offset+1;
      JDSQLToken token = null;
      if (p < bufferLen && buffer[offset] == '/' && buffer[p] == '*')
      {
        // Scan for end of comment block. We check for nested comment blocks, because this is
        // what the system expects.
        int start = offset;
        offset = p+1;
        int commentDepth = 1;
        while (offset < bufferLen && commentDepth > 0)
        {
          final int p2 = offset+1;
          if (p2 < bufferLen)
          {
            if (buffer[offset] == '/' && buffer[p2] == '*')
            {
              ++commentDepth;
              ++offset;
            }
            else if (buffer[offset] == '*' && buffer[p2] == '/')
            {
              --commentDepth;
              ++offset;
            }
          }
          ++offset;
        }
        if (returnComments) token = new JDSQLToken(buffer, start, offset-start, TOKEN_TYPE_COMMENT);
      }
      else if (p < bufferLen && buffer[offset] == '-' && buffer[p] == '-')
      {
        // Scan for newline that ends the single-line comment.
        int start = offset;
        offset = p+1;
        while (offset < bufferLen && buffer[offset++] != '\n');
        if (returnComments) token = new JDSQLToken(buffer, start, offset-start, TOKEN_TYPE_COMMENT);
      }
      else if (buffer[offset] == '\'')
      {
        // entering single quote
        int start = offset;
        ++offset;
        while (offset < bufferLen && buffer[offset++] != '\'');
        token = new JDSQLToken(buffer, start, offset-start, TOKEN_TYPE_LITERAL);
      }
      else if (buffer[offset] == '"')
      {
        // entering double quote
        int start = offset;
        ++offset;
        while (offset < bufferLen && buffer[offset++] != '\"');
        token = new JDSQLToken(buffer, start, offset-start, TOKEN_TYPE_LITERAL);
      }
      else if (buffer[offset] == '(')
      { //@PDA
        // Need to check for case like "insert into x (select ...)" where there is no space after (
        // Since no select token was found, the "extended dynamic" package was not being generated. 
        // So force a token if '(' is matched.  Should be ok in all cases that are not delimited since 
        // they can have a space after '(' anyway.
        int start = offset;
        ++offset;
        token = new JDSQLToken(buffer, start, offset-start, TOKEN_TYPE_SQL);
      }
      else if (isDelimiter(buffer[offset], delimiters))
      {
        // character at pos is a delimiter
        if (returnDelimiters) token = new JDSQLToken(buffer, offset, 1, TOKEN_TYPE_DELIMITER);
        ++offset;
      }
      else
      {
        // character is not any of the above
        // scan up to the next delimiter
        int numberOfParms = 0;
        final int start = offset;
        while (offset < bufferLen &&
               !isDelimiter(buffer[offset], delimiters) &&
               buffer[offset] != '\'' &&
               buffer[offset] != '\"')
        {
          final int p2 = offset+1;
          if (p2 < bufferLen &&
              ((buffer[offset] == '/' && buffer[p2] == '*') ||
               (buffer[offset] == '-' && buffer[p2] == '-')))
          {
            break;
          }
          else if (buffer[offset] == '?')
          {
            ++numberOfParms;
          }
          ++offset;
        }
        token = new JDSQLToken(buffer, start, offset-start, TOKEN_TYPE_SQL, numberOfParms);
      }

      if (token != null) tokens.addToken(token);
    }
    final JDSQLToken[] jdTokens = new JDSQLToken[tokens.count_];
    System.arraycopy(tokens.tokens_, 0, jdTokens, 0, tokens.count_);
    return jdTokens;
  }

  /**
  Returns a list of the tokens after scanning the String, with no separators.
  
  @return The String representation of this tokenizer.
  **/
  public String toString()
  {
    StringBuffer contents = new StringBuffer();
    for (int i=0; i<tokens_.length; ++i)
    {
      contents.append(tokens_[i].getToken());
    }
    return contents.toString();
  }
}

