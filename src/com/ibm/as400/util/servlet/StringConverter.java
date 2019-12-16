///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: StringConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.access.Copyright;
import java.beans.PropertyVetoException;
import java.util.Vector;

/**
*  The StringConverter class represents a row data string converter to
*  convert the data from RowData objects to formatted String arrays.
**/
abstract public class StringConverter
{
   /**
   *  Converts the row data specified by <i>rowdata</i>.
   *  @param rowdata The row data.
   *  @return A String array representation of the row data.
   *  @exception PropertyVetoException If a change is vetoed on the conversion source object.
   *  @exception RowDataException If a row data error occurs.
   **/
   public String[] convert(RowData rowdata) throws PropertyVetoException, RowDataException
   {
      if (rowdata == null)
         throw new NullPointerException("rowdata");
      return doConvert(rowdata, rowdata.getMetaData());
   }

   /**
   *  Converts the row data specified by <i>rowdata</i> and described by <i>metadata</i>.
   *  
   *  @param rowdata The row data.
   *  @param metadata The meta data.
   *  @return An array of Strings.
   *  @exception PropertyVetoException If a change is vetoed on the conversion source object.
   *  @exception RowDataException If a row data error occurs.
   **/
   abstract String[] doConvert(RowData rowdata, RowMetaData metadata) throws PropertyVetoException, RowDataException;

}
