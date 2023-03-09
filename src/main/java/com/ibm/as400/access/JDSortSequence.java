///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDSortSequence.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.StringTokenizer;



/**
<p>A National Language Support sort sequence.
**/
class JDSortSequence
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private static final int TYPE_HEX_                 = 0;
  private static final int TYPE_LANGUAGE_ID_SHARED_  = 1;
  private static final int TYPE_LANGUAGE_ID_UNIQUE_  = 2;
  private static final int TYPE_USER_SPECIFIED_      = 3;

  private String languageId_;
  private String tableFile_;
  private String tableLibrary_;
  private int type_;


  /**
  Constructor.
  @param  sort        The caller specified sort value.
  @param  language    The caller specified sort language.
  @param  table       The caller specified sort table.
  @param  weight      The caller specified sort weight.
  **/
  JDSortSequence(String sort, String language, String table, String weight)
  {
    // Initialize.
    languageId_     = "   ";
    tableFile_      = "";
    tableLibrary_   = "";
    type_           = TYPE_HEX_;

    // Handle sort == "language".
    if (sort.equalsIgnoreCase(JDProperties.SORT_LANGUAGE1))
    {
      if (weight.equalsIgnoreCase(JDProperties.SORT_WEIGHT_UNIQUE))
        type_ = TYPE_LANGUAGE_ID_UNIQUE_;
      else
        type_ = TYPE_LANGUAGE_ID_SHARED_;

      if (language.length() >= 3)
        languageId_ = language.substring(0, 3).toUpperCase();
      else
        languageId_ = language.toUpperCase();
    }

    // Handle sort == "table".
    else if (sort.equalsIgnoreCase(JDProperties.SORT_TABLE1))
    {
      type_ = TYPE_USER_SPECIFIED_;

      StringTokenizer tokenizer = new StringTokenizer(table, "./");
      if (tokenizer.countTokens() == 2)
      {
        tableLibrary_ = tokenizer.nextToken().toUpperCase(); //@A1C
        tableFile_    = tokenizer.nextToken().toUpperCase(); //@A1C
      }
      else
      {
        tableFile_ = table.toUpperCase(); //@A1C
      }
    }
  }


  /**
  Return the language id.
  @return     The language id.
  **/
  String getLanguageId()
  {
    return languageId_;
  }



  /**
  Return the table file.
  @return     The table file.
  **/
  String getTableFile()
  {
    return tableFile_;
  }



  /**
  Return the table library.
  @return     The table library.
  **/
  String getTableLibrary()
  {
    return tableLibrary_;
  }



  /**
  Return the type of sort sequence.
  @return     The type.
  **/
  int getType()
  {
    return type_;
  }
}




