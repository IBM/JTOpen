///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBColumnDescriptorsDataFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;



/**
The DBColumnDescriptorsDataFormat describes the 
data in the variable length column descriptor.
**/
class DBColumnDescriptorsDataFormat
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    // Store these as byte arrays so we can 
    private byte[]                 baseColumnName_;
    private byte[]                 baseTableName_;
    private byte[]                 baseTableSchemaName_;

    // Column label is a string because we are given the CCSID in the field if we receive
    // a column label from the server.
    private String                 columnLabel_;  
    private int                     jobCCSID_;                  // Fix for JTOpen Bug 4034


    /**
    Constructs a DBColumnDescriptorsDataFormat object.  Use this when overlaying
    on a reply datastream.  The cached data will be set when overlay()
    is called.
    **/
    DBColumnDescriptorsDataFormat()                              
    {                                            
    }

    // Fix for JTOpen Bug 4034
    /**
    Constructs a DBColumnDescriptorsDataFormat object.  Use this when overlaying
    on a reply datastream.  The cached data will be set when overlay()
    is called.
    @param jobCCSID The ccsid of the server job.
    **/
    DBColumnDescriptorsDataFormat(int jobCCSID)                              
    {                                            
        jobCCSID_ = jobCCSID;
    }

    /**
    Positions the overlay structure.  This reads the cached data only
    when it was not previously set by the constructor.
    **/
    void overlay (byte[] rawBytes, int offset, int variableColumnInfoLength)
    throws SQLException
    {
        // Parse through how ever many of the 3900, 3901, 3902, and 3904 there are (can be 0 
        // to 4).

        // Make sure variableColumnInfoLength is greater than 0.  If it is 0, 
        // that means the query did not return us variable column information.
        while (variableColumnInfoLength > 0)
        {
            int length = BinaryConverter.byteArrayToInt (rawBytes, offset);
            short codePoint = BinaryConverter.byteArrayToShort (rawBytes, offset + 4);
            switch (codePoint)
            {
            case 0x3900:
                // base column name
                baseColumnName_ = new byte[length-6];
                System.arraycopy(rawBytes, offset + 6, baseColumnName_, 0, length-6);
                break;

            case 0x3901:
                // base table name
                baseTableName_ = new byte[length-6];
                System.arraycopy(rawBytes, offset + 6, baseTableName_, 0, length-6);
                break;

            case 0x3902:
                // column label (carries its own CCSID, so make it a String, not a byte array)
                int ccsid = BinaryConverter.byteArrayToShort (rawBytes, offset + 6); 
                if(ccsid == -1)             // Fix for JTOpen Bug 4034 - CCSID is 65535, use the server job's ccsid
                    ccsid = jobCCSID_;      // Fix for JTOpen Bug 4034
                try
                {
                    columnLabel_ = (ConvTable.getTable(ccsid, null)).byteArrayToString(rawBytes, 
                                                                                       offset + 8, 
                                                                                       length-8);
                }
                catch (UnsupportedEncodingException e)
                {
                    JDError.throwSQLException (JDError.EXC_INTERNAL, e);
                }
                break;

            case 0x3904:
                // schema name
                baseTableSchemaName_ = new byte[length-6];
                System.arraycopy(rawBytes, offset + 6, baseTableSchemaName_, 0, length-6);
                break;
            }
            //Subtract off the length what we took off the datastream.
            variableColumnInfoLength = variableColumnInfoLength - length;
            //Move the offset to the next code point.
            offset = offset + length;
        }
    }



    String getBaseColumnName(ConvTable convTable)
    {
        //We don't have to be returned a baseColumnName by the server, depending on the query
        if (baseColumnName_ != null)
        {
            return convTable.byteArrayToString (baseColumnName_, 0, baseColumnName_.length);
        }
        else
            return null;
    }



    String getBaseTableName(ConvTable convTable)
    {
        //We don't have to be returned a baseTableName by the server, depending on the query
        if (baseColumnName_ != null)
        {
            return convTable.byteArrayToString (baseTableName_, 0, baseTableName_.length);
        }
        else
            return null;
    }



    String getBaseTableSchemaName(ConvTable convTable)
    {
        //We don't have to be returned a baseTableSchemaName by the server, depending on the query
        if (baseTableSchemaName_ != null)
        {
            return convTable.byteArrayToString (baseTableSchemaName_, 0, 
                                                baseTableSchemaName_.length);
        }
        else
            return null;
    }



    String getColumnLabel(ConvTable convTable)
    {
        //We don't have to be returned a column label by the server, depending on the query.
        //In fact, if the column label is the same as the base column name, we will not be returned
        //it.  If we get a base column name and not a column label, we can assume that the column
        //label is the same as the base column name and return that.
        if (columnLabel_ != null)
        {
            // If we have a column label, we already converted it based on the CCSID provided 
            // by the database, so don't use the converter the user passed in.
            return columnLabel_;
        }
        // We weren't returned a column label, so try to return the base column name.
        //@D9 If a alias is being used, the base column name will be different from the alias name
        // We should be returning the alias name
        
        //@D9D else if (baseColumnName_ != null)
        //@D9D {
        //@D9D     return convTable.byteArrayToString (baseColumnName_, 0, baseColumnName_.length);
        //@D9D }
        // We weren't returned a column label or a base column name, so return null.
        else
            return null;
    }
}


