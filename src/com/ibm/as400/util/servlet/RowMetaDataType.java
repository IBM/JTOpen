///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: RowMetaDataType.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

/**
*  The RowMetaDataType class defines constants to identify metadata data types.
**/
public class RowMetaDataType
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   /**
   *  java.lang.Byte array data type.
   **/
   public final static int BYTE_ARRAY_DATA_TYPE = 1;
   /**
   *  java.math.BigDecimal data type.
   **/
   public final static int BIG_DECIMAL_DATA_TYPE = 2;
   /**
   *  java.lang.Double data type.
   **/
   public final static int DOUBLE_DATA_TYPE = 3;
   /**
   *  java.lang.Float data type.
   **/
   public final static int FLOAT_DATA_TYPE = 4;
   /**
   *  java.lang.Integer data type.
   **/
   public final static int INTEGER_DATA_TYPE = 5;
   /**
   *  java.lang.Long data type.
   **/
   public final static int LONG_DATA_TYPE = 6;
   /**
   *  java.lang.Short data type.
   **/
   public final static int SHORT_DATA_TYPE = 7;
   /**
   *  java.lang.String data type.
   **/
   public final static int STRING_DATA_TYPE = 8;

   // The data type.
   private int type_;
   
   /**
   *  Returns the data type name.
   *  @param type The data type.
   *  @return The name.
   **/
   public static String getDataTypeName(int type)
   {
      switch(type)
      {
	  case BYTE_ARRAY_DATA_TYPE: return "java.lang.Byte[]";
	  case BIG_DECIMAL_DATA_TYPE: return "java.math.BigDecimal";
	  case DOUBLE_DATA_TYPE: return "java.lang.Double";
	  case FLOAT_DATA_TYPE: return "java.lang.Float";
	  case INTEGER_DATA_TYPE: return "java.lang.Integer";
	  case LONG_DATA_TYPE: return "java.lang.Long";
	  case SHORT_DATA_TYPE: return "java.lang.Short";
	  case STRING_DATA_TYPE: return "java.lang.String";
	  default: return "java.lang.Byte[]";
      }
   }

   /**
   *  Indicates if the data type is valid.
   *  @param type The data type.
   *  @return true if the data type is valid; false otherwise.
   **/
   public static boolean isDataTypeValid(int type)
   {
      if (type >= BYTE_ARRAY_DATA_TYPE && type <= STRING_DATA_TYPE)
         return true;
      else
         return false;
   }

   /**
   *  Indicates if the data type is numeric data.
   *  @param type The data type.
   *  @return true if numeric data; false otherwise.
   **/
   static boolean isNumericData(int type)		// @A1 - buhr
   {
      switch(type)
      {
	  case RowMetaDataType.BIG_DECIMAL_DATA_TYPE: return true;
	  case RowMetaDataType.DOUBLE_DATA_TYPE: return true;
	  case RowMetaDataType.FLOAT_DATA_TYPE: return true;
	  case RowMetaDataType.INTEGER_DATA_TYPE: return true;
	  case RowMetaDataType.LONG_DATA_TYPE: return true;
	  case RowMetaDataType.SHORT_DATA_TYPE: return true;
	  default: return false;
      }
   }

   /**
   *  Indicates if the data type is text data.
   *  @param type The data type.
   *  @return true if text data; false otherwise.
   **/
   static boolean isTextData(int type)
   {

      switch(type)
      {
      case RowMetaDataType.STRING_DATA_TYPE:
         return true;
      default:
         return false;
      }
   }
}
