///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDServerRow.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;



/**
The JDServerRow class implements a row of data that is loaded
directly from a datastream to or from the server.
**/
class JDServerRow
implements JDRow
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private AS400JDBCConnection     connection_;
    private int[]                   ccsids_;
    private int[]                   dataLength_;
    private int[]                   dataOffset_;
    private String[]                fieldNames_;
    private int[]                   lobLocatorHandles_;       // @C2A
    private int[]                   precisions_;
    private byte[]                  rawBytes_;
    private int                     rowDataOffset_;
    private int                     rowIndex_;
    private int[]                   scales_;
    private DBData                  serverData_;
    private DBDataFormat            serverFormat_;
    private SQLData[]               sqlData_;
    private int[]                   sqlTypes_;
    private boolean[]               translated_;



    /**
    Constructs a JDServerRow object.  Use this constructor
    when the format information has already been retrieved
    from the server.
    
    @param      connection      The connection to the server.
    @param      id              The id.
    @param      serverFormat    The server format information.
    @param      settings        The conversion settings.
    
    @exception  SQLException    If an error occurs.
    **/
    JDServerRow (AS400JDBCConnection connection,
                 int id,
                 DBDataFormat serverFormat,
                 SQLConversionSettings settings)
    throws SQLException
    {
        initialize (connection, id, serverFormat, settings);
    }



    /**
    Constructs a JDServerRow object.  Use this constructor when format
    information has not yet been retrieved from the server.
    
    @param  connection      The connection to the server.
    @param  id              The id.
    @param  settings        The conversion settings.
    
    @exception  SQLException    If an error occurs.
    **/
    /* @C1D
    JDServerRow (AS400JDBCConnection connection,
                 int id,
                 SQLConversionSettings settings)
        throws SQLException
    {
        DBDataFormat serverFormat = null;
  
        DBSQLRequestDS request = new DBSQLRequestDS (
            DBSQLRequestDS.FUNCTIONID_DESCRIBE,
      id, DBSQLRequestDS.ORS_BITMAP_RETURN_DATA
      + DBSQLRequestDS.ORS_BITMAP_DATA_FORMAT, 0);
  
      DBReplyRequestedDS reply = connection.sendAndReceive (request, id);
  
      int errorClass = reply.getErrorClass();
      int returnCode = reply.getReturnCode();
  
      if (errorClass != 0)
        JDError.throwSQLException (connection, id, errorClass, returnCode);
  
        serverFormat = reply.getDataFormat ();
  
        initialize (connection, id, serverFormat, settings);
    }
    */



    // @D1A
    /**
    Returns the raw bytes.
    
    @param      index   The field index (1-based).
    @return             A copy of the bytes.
    **/
    byte[] getRawBytes(int index)
    {
        int index0 = index - 1;
        byte[] copy = new byte[dataLength_[index0]];
        System.arraycopy(rawBytes_, rowDataOffset_ + dataOffset_[index0], copy, 0, dataLength_[index0]);
        return copy;
    }



    /**
    Initializes the state of the object.
    
    @param      connection          The connection to the server.
    @param      id                  The id.
    @param      serverFormat        The server format information.
    @param      settings            The conversion settings.
    
    @exception  SQLException        If an error occurs.
    **/
    private void initialize (AS400JDBCConnection connection,
                             int id,
                             DBDataFormat serverFormat,
                             SQLConversionSettings settings)
    throws SQLException
    {
        // Initialization.
        connection_         = connection;
        rawBytes_           = null;
        rowDataOffset_      = -1;
        rowIndex_           = -1;
        serverData_         = null;
        serverFormat_       = serverFormat;

        try
        {
            int count;
            if (serverFormat_ == null)
                count = 0;
            else
                count = serverFormat_.getNumberOfFields ();

            ccsids_     = new int[count];
            dataLength_ = new int[count];
            dataOffset_ = new int[count];
            fieldNames_ = new String[count];
            lobLocatorHandles_= new int[count];       // @C2A
            precisions_ = new int[count];
            scales_     = new int[count];
            sqlData_    = new SQLData[count];
            sqlTypes_   = new int[count];
            translated_ = new boolean[count];

            // Compute the offsets, lengths, and SQL data for
            // each field.
            if (count > 0)
            {
                int offset = 0;
                boolean translateBinary = connection.getProperties().getBoolean (JDProperties.TRANSLATE_BINARY);
                for (int i = 0; i < count; ++i)
                {
                    ccsids_[i] = serverFormat_.getFieldCCSID (i);
                    dataOffset_[i] = offset;
                    dataLength_[i] = serverFormat_.getFieldLength (i);
                    lobLocatorHandles_[i] = serverFormat_.getFieldLOBLocator (i);       // @C2C                    
                    offset += dataLength_[i];
                    scales_[i] = serverFormat_.getFieldScale (i);
                    precisions_[i] = serverFormat_.getFieldPrecision (i);
                    sqlTypes_[i] = serverFormat_.getFieldSQLType (i);
                    int maxLobSize = serverFormat_.getFieldLOBMaxSize (i);              // @C2C

                    sqlData_[i] = SQLDataFactory.newData (connection, id,
                                                          sqlTypes_[i] & 0xFFFE, dataLength_[i], precisions_[i], 
                                                          scales_[i], ccsids_[i], translateBinary, settings,
                                                          maxLobSize, (i+1));     //@F1C                                               // @C2C
                    // @E2D // SQLDataFactory never returns null.
                    // @E2D if (sqlData_[i] == null)
                    // @E2D    JDError.throwSQLException (JDError.EXC_INTERNAL);
                }
            }
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }
    }



    /**
    Sets the server data.  Use this when new data has been retrieved
    from the server.
    
    @param  serverData      The server data.
    
    @exception  SQLException        If an error occurs.
    **/
    void setServerData (DBData serverData)
    throws SQLException
    {
        serverData_ = serverData;

        try
        {
            rawBytes_   = serverData_.getRawBytes ();
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }
    }



    /**
    Sets the row index within the server data.
    
    @param  rowIndex        The row index (0-based).
    
    @exception  SQLException        If an error occurs.
    **/
    void setRowIndex (int rowIndex)
    throws SQLException
    {
        rowIndex_ = rowIndex;

        try
        {
            if (serverData_ != null)
                rowDataOffset_ = serverData_.getRowDataOffset (rowIndex_);
            else
                rowDataOffset_ = -1;
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        // Reset so that data gets retranslated.
        for (int i = 0; i < translated_.length; ++i)
            translated_[i] = false;
    }



    //-------------------------------------------------------------//
    //                                                             //
    // INTERFACE IMPLEMENTATIONS                                   //
    //                                                             //
    //-------------------------------------------------------------//



    public int findField (String name)
    throws SQLException
    {       
        if (name.indexOf("\"") >= 0)                              //@D6a
            name = name.replace('"', ' ').trim();                  //@D6a
        else                                                      //@D6a
            name = name.toUpperCase();                             //@D6a

        for (int i = 1; i <= sqlData_.length; ++i)
            if (name.equals (getFieldName (i)))                    //@D6c (used to be equalsIgnoreCase)
                return i;

        JDError.throwSQLException (JDError.EXC_COLUMN_NOT_FOUND);
        return -1;
    }



    public int getFieldCount ()
    {
        return sqlData_.length;
    }



    public int getFieldLOBLocatorHandle (int index)         // @C2A
    throws SQLException                                 // @C2A
    {                                                       // @C2A
        return lobLocatorHandles_[index-1];                 // @C2A
    }                                                       // @C2A



    public String getFieldName (int index)
    throws SQLException
    {
        try
        {
            // We need to trim() the field name before
            // returning it, since in some cases (e.g.
            // stored procedure written in RPG) the
            // field names have spaces at end of the name.
            //
            // Cache the field names so we only translate them once.
            //
            int index0 = index-1;
            if (fieldNames_[index0] == null)
                fieldNames_[index0] = serverFormat_.getFieldName (index0,
                                                                  connection_.getConverter (serverFormat_.getFieldNameCCSID (index0))).trim();
            return fieldNames_[index0];
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return null;
        }
    }


    /* @C1D
    public int getFieldPrecision (int index)
        throws SQLException
    {
        return precisions_[index-1];
    }
  
  
  
    public int getFieldScale (int index)
        throws SQLException
    {
        return scales_[index-1];
    }
    */



    public SQLData getSQLData (int index)
    throws SQLException
    {
        try
        {
            int index0 = index - 1;

            // Translate the first time only, and only when there
            // is a current row.
            // (There is a chance that there are not when
            // this gets called, specifically in the case
            // where result set meta data methods get called
            // before fetching data.)
            if ((rowIndex_ >= 0) && (translated_[index0] == false))
            {

                // @E1D // The CCSID returned in the data format is not
                // @E1D // necessarily correct (says the database server
                // @E1D // documentation), so we should always use the server
                // @E1D // job CCSID or its graphic equivalent.
                // @E1D ConverterImplRemote ccsidConverter = null;
                // @E1D if (sqlData_[index0].isText ()) {
                // @E1D     if (sqlData_[index0].isGraphic ()) {
                // @E1D         // @A0A
                // @E1D         // Code added here to check for the 13488 Unicode ccsid.
                // @E1D         // If there's one, set 'ccsidConverter' to null so that
                // @E1D         // hand conversion is done in SQLChar.set().
                // @E1D         if (ccsids_[index0] == 13488)                          // @A0A
                // @E1D             ccsidConverter = null;                             // @A0A
                // @E1D         else                                                   // @A0A
                // @E1D             ccsidConverter = connection_.getGraphicConverter ();
                // @E1D     }
                // @E1D     else
                // @E1D         ccsidConverter = connection_.getConverter ();
                // @E1D }

                // Use the CCSID returned in the data format.                                       // @E1A
                ConvTable ccsidConverter = connection_.getConverter(ccsids_[index0]);     // @E1A @P0C

                // If there are bytes, then do a translation.
                if (rawBytes_ != null)
                {
                    sqlData_[index0].convertFromRawBytes (rawBytes_,
                                                          rowDataOffset_ + dataOffset_[index0],
                                                          ccsidConverter);
                    translated_[index0] = true;
                }

            }

            return sqlData_[index0];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return null;
        }
    }



    public SQLData getSQLType (int index)
    throws SQLException
    {
        return sqlData_[index - 1];
    }



    /**
    Is the field value SQL NULL?
    
    @param      index   The field index (1-based).
    @return             true or false
    
    @exception  SQLException    If an error occurs.
    **/
    public boolean isNull (int index)
    throws SQLException
    {
        try
        {
            if (serverData_ != null)
                return(serverData_.getIndicator (rowIndex_, index - 1) != 0);
            else
                return false;
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return false;
        }
    }



    /**
    Can the field contain a SQL NULL value?
    
    @param  index   The field index (1-based).
    @return         true if nullable.
    
    @exception  SQLException    If an error occurs.
    **/
    public int isNullable (int index)
    throws SQLException
    {
        //@F2 Add try/catch block to this method.
        try
        {                                                                         //@F2A
            return(((sqlTypes_[index-1] & 0x0001) != 0) 
                   ? ResultSetMetaData.columnNullable
                   : ResultSetMetaData.columnNoNulls);
        }
        catch (ArrayIndexOutOfBoundsException e)                                  //@F2A
        {                                                                         //@F2A
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);  //@F2A
            return 0;                                                             //@F2A
        }                                                                         //@F2A
    }



    /**
    Return the CCSID for a field.
    
    @param      index   The field index (1-based).
    @return             The CCSID.
    
    @exception  SQLException    If an error occurs.
    **/
    public int getCCSID (int index)
    throws SQLException
    {
        return ccsids_[index-1];
    }



    /**
    Return the length of a field's data within server
    data.
    
    @param      index   The field index (1-based).
    @return             The data length.
    
    @exception  SQLException    If an error occurs.
    **/
    public int getLength (int index)
    throws SQLException
    {
        return dataLength_[index-1];
    }




    /**
    Return the length of the row's data.
    
    @return             The row length.
    
    @exception  SQLException    If an error occurs.
    **/
    int getRowLength ()
    throws SQLException
    {
        try
        {
            return serverFormat_.getRecordSize();
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return -1;
        }
    }



    /**
    Is the field an input value?
    
    @param  index   The field index (1-based).
    @return         true or false
    
    @exception  SQLException    If an error occurs.
    **/
    boolean isInput (int index)
    throws SQLException
    {
        int parameterType = -1;

        try
        {
            parameterType = serverFormat_.getFieldParameterType (index - 1);
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
        }

        return(((byte) parameterType == (byte) 0xF0)
               || ((byte) parameterType == (byte) 0xF2));
    }



    /**
    Is the field an output value?
    
    @param  index   The field index (1-based).
    @return         true or false
    
    @exception  SQLException    If an error occurs.
    **/
    boolean isOutput (int index)
    throws SQLException
    {
        int parameterType = -1;

        try
        {
            parameterType = serverFormat_.getFieldParameterType (index - 1);
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
        }

        return(((byte) parameterType == (byte) 0xF1)
               || ((byte) parameterType == (byte) 0xF2));
    }



    /* @C1D
    // @A1
    // Added setLength().
    public void setLength(int index, int length)
    {
        dataLength_[index-1] = length;
        if (index < sqlData_.length) {
            for (int i=index; i<sqlData_.length; i++) {
                dataOffset_[i] = dataOffset_[i-1] + dataLength_[i-1];
            }
        }
    }
    */


}
