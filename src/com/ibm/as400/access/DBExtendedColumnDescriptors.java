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
    //     4     reserved           6  bytes
    //
    //    10     extended column descriptor (repeated for each column)
    //           0   fixed data                              4  bytes
    //               1 byte  Updateable
    //               1 byte  Searchable
    //               2 bytes 
    //                 1 bit Identity column?
    //                 1 bit Column is part of an index?
    //                 1 bit Column is a lone unique index?
    //                 1 bit Column is part of a unique index attribute?
    //           4   offset from beginning to variable data  4  bytes
    //           8   length of variable data                 4  bytes
    //           12  reserved (alignment)                    4  bytes
    //
    // (#col*16) 0   length of the column descriptor         4  bytes
    // +10       4   code point of column descriptor         2  bytes
    //           6   CCSID (for label only)                  2  bytes
    //           6   value of column descriptor              n bytes
    //
    //           Note: these offsets are in terms of the start
    //                 of the LL for this parameter (i.e.
    //                 actually count from (offset_ - 6).
    //
    //---------------------------------------------------------



    public int getAttributeBitmap (int columnIndex)
    {
        return BinaryConverter.byteArrayToShort (data_, offset_ + getExtendedColumnDescriptorOffset (columnIndex)); //@A1C
    }



    //@A1D public int getCCSID ()
    //@A1D {
    //@A1D     return BinaryConverter.byteArrayToShort (data_, offset_ + 4);
    //@A1D }


    public DBColumnDescriptorsDataFormat getColumnDescriptors (int columnIndex)
    throws SQLException
    {    
        // if variable column info length is 0, then no variable length column information
        // was returned
        int variableColumnInfoLength = getVariableColumnInfoLength(columnIndex);
        if (variableColumnInfoLength > 0) {
            int offsetToDescriptor = getVariableColumnInfoOffset (columnIndex);
            DBColumnDescriptorsDataFormat columnDescriptorsDataFormat = new DBColumnDescriptorsDataFormat();  
            columnDescriptorsDataFormat.overlay (data_, ((offset_ - 6) + offsetToDescriptor), variableColumnInfoLength);
            return columnDescriptorsDataFormat;
        }
        else
            return null;
    }



    private int getExtendedColumnDescriptorOffset (int columnIndex)
    {
        return(((columnIndex-1) * 16) + 10); //@A1C
    }


    private int getVariableColumnInfoLength (int columnIndex)
    {
        return BinaryConverter.byteArrayToInt (data_, (offset_ + getExtendedColumnDescriptorOffset (columnIndex) + 8));
    }


    private int getVariableColumnInfoOffset (int columnIndex)
    {
        return BinaryConverter.byteArrayToInt (data_, (offset_ + getExtendedColumnDescriptorOffset (columnIndex) + 4));
    }


    int getNumberOfColumns ()
    {
        return BinaryConverter.byteArrayToInt (data_, offset_);
    }


    byte getSearchable (int columnIndex)
    {
        return data_[offset_ + getExtendedColumnDescriptorOffset (columnIndex) + 1];
    }


    byte getUpdateable (int columnIndex)
    {
        return data_[offset_ + getExtendedColumnDescriptorOffset (columnIndex)];
    }


}



