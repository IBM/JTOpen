///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SQLNativeType.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
<p>Class containing constants for the native SQL type values.
The number is included in the definition for ease of debugging 
the values when viewed in the data stream.  
**/
public class SQLNativeType {
   public final static int DATE_384 = 384; 
   public final static int TIME_388 = 388; 
   public final static int TIMESTAMP_392 = 392; 
   public final static int DATALINK_396=396;  
   public final static int BLOB_404 = 404;  
   public final static int CLOB_408 = 408;
   public final static int DBCLOB_412 = 412;
   public final static int VARCHAR_448 = 448;
   public final static int CHAR_452 = 452;  
   public final static int LONGVARCHAR_456=456;
   public final static int VARGRAPHIC_464 = 464;
   public final static int GRAPHIC_468=468; 
   public final static int LONGVARGRAPHIC_472 = 472; 
   public final static int FLOAT_480=480; 
   public final static int PACKED_DECIMAL_484=484;
   public final static int ZONED_DECIMAL_488=488;
   public final static int BIGINT_492=492;
   public final static int INTEGER_496=496;
   public final static int SMALLINT_500=500; 
   public final static int ROWID_904=904;
   public final static int VARBINARY_908=908;
   public final static int BINARY_912=912;
   public final static int BLOB_LOCATOR_960=960;
   public final static int CLOB_LOCATOR_964=964;
   public final static int DBCLOB_LOCATOR_968=968;
   public final static int DECFLOAT_996=996;
   public final static int XML_988=988; 
   public final static int NATIVE_ARRAY = SQLData.NATIVE_ARRAY;
   public final static int XML_LOCATOR_2452=2452;
   public final static int BOOLEAN_2436=2436; 
}
