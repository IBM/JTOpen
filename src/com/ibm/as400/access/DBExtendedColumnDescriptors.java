///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBExtendedColumnDescriptors.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;



/**
   Provides access to the package info portion of the reply data stream.
**/
class DBExtendedColumnDescriptors {
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    private byte[]      data_;
    private int         offset_;                                          


    /**
       Constructs a new DBExtendedColumnDescriptors object.  It initializes
       the variables using the data provided.
    **/
    public DBExtendedColumnDescriptors (byte[] data,
                                        int offset)                                
    {
        data_ = data;
        offset_ = offset;                                              
    }


    //---------------------------------------------------------
    // The following structure is used to determine the amount
    // needed to be added to the offset to get the appropriate
    // data.
    //
    //  offset+  field              length
    //
    //     0     # of columns       4  bytes
    //     4     reserved           8  bytes
    //
    //    12     extended column descriptor (repeated for each column)
    //           0   fixed data                              4  bytes
    //               1 byte  Updateable
    //               1 byte  Searchable
    //               2 bytes Attribute Indicator
    //           4   variable offest                         4  bytes
    //           8   variable length                         2  bytes
    //           10  reserved (alignment)                    6  bytes
    //
    //           14  length of the column descriptor         4  bytes
    //           18  code point of column descriptor         2  bytes
    //           20  CCSID (for label only)                  2  bytes
    //           22  value of column descriptor              n bytes
    //
    //           Note: these offsets are in terms of the start
    //                 of the LL for this parameter (i.e.
    //                 actually count from (offset_ - 6).
    //
    //---------------------------------------------------------



    public int getAttributeBitmap (int columnIndex)
    {
        return BinaryConverter.byteArrayToShort (data_, offset_ + getExtendedColumnDescriptorOffset (columnIndex) + 2);
    }



    public int getCCSID ()
    {
        return BinaryConverter.byteArrayToShort (data_, offset_ + 4);
    }


    public DBColumnDescriptorsDataFormat getColumnDescriptors (int columnIndex)
    throws SQLException
    {    
        // if variable column info length is 0, then no variable length column information
        // was returned
        int variableColumnInfoLength = getVariableColumnInfoLength(columnIndex);
        if (variableColumnInfoLength > 0) {
            int offsetToDescriptor = offset_ + getExtendedColumnDescriptorOffset (columnIndex);
            DBColumnDescriptorsDataFormat columnDescriptorsDataFormat = new DBColumnDescriptorsDataFormat();  
            columnDescriptorsDataFormat.overlay (data_, offsetToDescriptor, variableColumnInfoLength);
            return columnDescriptorsDataFormat;
        }
        else
            return null;
    }



    private int getExtendedColumnDescriptorOffset (int columnIndex)
    {
        return 12 + 24 * (columnIndex);
    }


    private int getVariableColumnInfoLength (int columnIndex)
    {
        return BinaryConverter.byteArrayToInt (data_, offset_ + getExtendedColumnDescriptorOffset (columnIndex) + 16);
    }



    int getNumberOfColumns ()
    {
        return BinaryConverter.byteArrayToInt (data_, offset_);
    }



    int getSearchable (int columnIndex)
    {
        return data_[offset_ + getExtendedColumnDescriptorOffset (columnIndex) + 1];
    }



    int getUpdateable (int columnIndex)
    {
        return data_[offset_ + getExtendedColumnDescriptorOffset (columnIndex)];
    }


}

