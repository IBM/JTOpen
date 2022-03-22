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
import java.util.Calendar;
import java.util.Hashtable;




/**
The JDServerRow class implements a row of data that is loaded
directly from a datastream to or from the system.
**/
class JDServerRow
implements JDRow
{
    static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private int[] parameterTypes_;


    // Private data.
    private AS400JDBCConnection     connection_;
    private int[]                   ccsids_;
    // Represents the total length of the data.  For arrays
    // this is arrayLen * arrayDataLen.
    private int[]                   dataLength_;
    private int[]                   arrayDataLength_;
    private int[]                   dataOffset_;
    private String[]                fieldNames_;
    private int[]                   lobLocatorHandles_;    // @C2A
    private int[]                   precisions_;
    private byte[]                  rawBytes_;
    private int                     rowDataOffset_;
    private int                     rowIndex_;
    private int[]                   scales_;
    private DBData                  serverData_;
    private DBDataFormat            serverFormat_;
    private SQLData[]               sqlData_;
    private int[]                   sqlTypes_;
    private String[]                sqlTypeNames_;   /*@L1A*/
    private boolean[]               translated_;
    private boolean                 wasCompressed = false;   // set to true if variable length field compression is used
    private Hashtable               insensitiveColumnNames_; // @PDA maps strings to column indexes
    boolean                         containsLob_;     //@re-prep
    boolean                         containsArray_;     //@array

    /**
    Constructs a JDServerRow object.  Use this constructor
    when the format information has already been retrieved
    from the system.

    @param      connection      The connection to the system.
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
    information has not yet been retrieved from the system.

    @param  connection      The connection to the system.
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

    @param      connection          The connection to the system.
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
        int dateFormat = -1;	// @550A
        int timeFormat = -1;	// @550A

        try
        {
            int count;
            if(serverFormat_ == null)
                count = 0;
            else
                count = serverFormat_.getNumberOfFields ();

            ccsids_     = new int[count];
            dataLength_ = new int[count];
            arrayDataLength_ = new int[count];
            dataOffset_ = new int[count];
            fieldNames_ = new String[count];
            lobLocatorHandles_= new int[count];    // @C2A
            precisions_ = new int[count];
            scales_     = new int[count];
            sqlData_    = new SQLData[count];
            sqlTypes_   = new int[count];
            sqlTypeNames_ = new String[count];  /*@L1A*/
            translated_ = new boolean[count];
            insensitiveColumnNames_ = null;  //@PDA
            containsLob_ = false;   //@re-prep
            containsArray_ = false; //@array

            // Compute the offsets, lengths, and SQL data for
            // each field.
            if(count > 0)
            {
                int offset = 0;
                boolean translateBinary = connection.getProperties().getBoolean (JDProperties.TRANSLATE_BINARY);
                if(connection_.getVRM() >= JDUtilities.vrm610 && serverFormat_.getCSRSData())	// @550A retrieve date/time formats if the data is from a stored procedure result set
                {
                	dateFormat = serverFormat_.getDateFormat();
                	timeFormat = serverFormat_.getTimeFormat();
                }
                for(int i = 0; i < count; ++i)
                {
                    ccsids_[i] = serverFormat_.getFieldCCSID (i);
                    dataOffset_[i] = offset;
                    dataLength_[i] = serverFormat_.getFieldLength (i);
                    //@array (if array type) here we do not know the array length, just the element length, but that is okay since the 
                    // elem length is fed into the sqlDataTemplate in SQLArray.  
                    // for reply result, setRowIndex() will later re-populate the dataLength_ and dataOffset_ arrays anyways.)
                    // @G2C
                    lobLocatorHandles_[i] = serverFormat_.getFieldLOBLocator (i);    // @C2C
                    offset += dataLength_[i];
                    scales_[i] = serverFormat_.getFieldScale (i);
                    precisions_[i] = serverFormat_.getFieldPrecision (i);
                    sqlTypes_[i] = serverFormat_.getFieldSQLType (i);
                    int compositeContentType = -1;
                    if( serverFormat_.getArrayType (i) == 1)          //@array
                    {
                        compositeContentType = sqlTypes_[i] & 0xFFFE; //@array
                        sqlTypes_[i] =  SQLData.NATIVE_ARRAY;   //@array not a hostserver number, since we only get a 1 bit array flag for the type
                        arrayDataLength_[i] = serverFormat_.getArrayFieldLength(i);  /*@G2A*/ 
                        
                    } else {
                      arrayDataLength_[i] = 0; /*@G2A*/ 
                    }
                    //@array comment: we are assuming here that all of the metadata above (except sqlType) is for the array content type

                    //@re-prep check if lob or locator type here
                    //hostserver cannot know beforehand if type will be a lob or a locator.  This is on a per-connection basis.
                    int fieldType = sqlTypes_[i] & 0xFFFE;    //@re-prep
                    if(fieldType ==  404 || fieldType ==  960 || fieldType ==  408 || fieldType == 964 || fieldType == 412 || fieldType == 968)  //@re-prep
                        containsLob_ = true;                  //@re-prep
                    else if(fieldType == SQLData.NATIVE_ARRAY)          //@array
                        containsArray_ = true;           //@array

                    int maxLobSize = serverFormat_.getFieldLOBMaxSize (i);    // @C2C
                    int xmlCharType = serverFormat_.getXMLCharType(i); //@xml3 sb=0 or db=1
                    if (fieldType == SQLData.NATIVE_ARRAY) {  /*@G2A*/ 
                      sqlData_[i] = SQLDataFactory.newData (connection, id,
                          fieldType, arrayDataLength_[i], precisions_[i],
                          scales_[i], ccsids_[i], translateBinary, settings,
                          maxLobSize, (i+1), dateFormat, timeFormat, compositeContentType, xmlCharType);    //@F1C // @C2C @550C @array //@xml3

                    } else {
                    sqlData_[i] = SQLDataFactory.newData (connection, id,
                                                          fieldType, dataLength_[i], precisions_[i],
                                                          scales_[i], ccsids_[i], translateBinary, settings,
                                                          maxLobSize, (i+1), dateFormat, timeFormat, compositeContentType, xmlCharType);    //@F1C // @C2C @550C @array //@xml3
                    }
                    // @E2D // SQLDataFactory never returns null.
                    // @E2D if (sqlData_[i] == null)
                    // @E2D    JDError.throwSQLException (JDError.EXC_INTERNAL);
                }
            }
        }
        catch(DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }
    }

    // If varying length field compression was used, and it was not used on a subsequent request, we need to set the
    // data offsets and data lengths based on the server format
    void setOriginalData() throws SQLException{
        try{
            int count = 0;
            if(serverFormat_ != null)
                count = serverFormat_.getNumberOfFields ();

            if(count>0){
                int offset = 0;
                for(int i = 0; i < count; ++i)
                {
                    dataOffset_[i] = offset;
                    dataLength_[i] = serverFormat_.getFieldLength (i);
                    arrayDataLength_[i] = serverFormat_.getArrayFieldLength(i); /*@G2A*/ 
                    offset += dataLength_[i];
                }
            }
        }
        catch(DBDataStreamException e){
            JDError.throwSQLException(JDError.EXC_INTERNAL, e);
        }

    }



    /**
    Sets the server data.  Use this when new data has been retrieved
    from the system.

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
            //@array all parsed variable array data from host is inside of DBVariableData (serverData_)
        }
        catch(DBDataStreamException e)
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
            if(serverData_ != null)
            {
                rowDataOffset_ = serverData_.getRowDataOffset (rowIndex_);
                //@array calculate data offsets for arrays (result data from host)
                if(this.containsArray_ && rowDataOffset_ != -1)                     //@array array data not VLC but variable in length
                {
                    //Here if reply is VariableData needed for Arrays.              //@array
                    //@array set input array lengths of data
                    int offset = 0;                                                 //@array
                    int numOfFields = serverFormat_.getNumberOfFields();            //@array
                    int[] dataLengths = ((DBVariableData)serverData_).getTotalDataLengthsFromHost(); //@array
                    int[] arrayDataLengths = ((DBVariableData)serverData_).getArrayDataLengthsFromHost(); /*@G2A*/ 
                    int outCount = 0;  //@arrayout
                    for(int j=0; j<numOfFields; j++)                                 //@array
                    {                                                               //@array
                        if(isOutput(j+1))                                                 //@arrayout
                        {                                                               //@array
                            // String typeName = sqlData_[j].getTypeName();                //@array
                            int length = 0;                                             //@array
                            dataOffset_[j] = offset;                                    //@array
                            length = dataLengths[outCount];                                    //@arrayout

                            offset += length;                                           //@array
                            dataLength_[j] = length;                                    //@array //set full array length here if array
                            // If the arrayDataLengths[outCount] is zero, then don't override the 
                            // previous value.  Null arrays will return 0 as the arrayDataLength
                            // @G7A
                            if (arrayDataLengths[outCount] > 0) {
                               arrayDataLength_[j] = arrayDataLengths[outCount]; /*@G2A*/
                            }
                            outCount++;                                                 //@arrayout
                        }                                                               //@array
                    }
                }                                                                   //@array
        else if (serverData_.isVariableFieldsCompressed()
            && rowDataOffset_ != -1) // @K54
        { // @K54
          wasCompressed = true;
          int offset = 0; // @K54
          int numOfFields = serverFormat_.getNumberOfFields(); // @K54
          for (int j = 0; j < numOfFields; j++) // @K54
          {
            // Use SQL type to eliminate the string comparisons in the old code @P7A
            int sqlType = sqlData_[j].getSQLType();
            int length = 0; // @K54
            dataOffset_[j] = offset; // @K54
            switch (sqlType) {
              case SQLData.VARCHAR:
              case SQLData.VARCHAR_FOR_BIT_DATA:
              case SQLData.LONG_VARCHAR:
              case SQLData.LONG_VARCHAR_FOR_BIT_DATA:
              case SQLData.VARBINARY:
              case SQLData.DATALINK: {
                length = BinaryConverter.byteArrayToUnsignedShort(rawBytes_,
                  rowDataOffset_ + offset); // @K54 //get actual length of data
                length += 2; // Add two bytes for length portion on datastream
                           // //@K54
                break;
              }
              case SQLData.VARGRAPHIC:
              case SQLData.LONG_VARGRAPHIC:
              case SQLData.LONG_NVARCHAR:
              case SQLData.NVARCHAR: {
                length = (2 * BinaryConverter.byteArrayToUnsignedShort(rawBytes_, rowDataOffset_ + offset));    //@K54 //get actual length of data
                length += 2;        //Add two bytes for length portion on datastream                                        //@K54
                
              
               break; 
              }
              default: 
                length = serverFormat_.getFieldLength (j);             //@K54 //get fixed size of data
            } /* switch */                 
/* ----------------- Old code             
                        String typeName = sqlData_[j].getTypeName();                //@K54
                        int length = 0;                                             //@K54
                        dataOffset_[j] = offset;                                    //@K54
                        //if it is a variable-length field, get actual size of data  //@K54
                        if(typeName.equals("VARCHAR") ||                            //@K54
                           typeName.equals("VARCHAR () FOR BIT DATA") ||             //@K54
                           typeName.equals("LONG VARCHAR") ||                       //@K54
                           typeName.equals("LONG VARCHAR () FOR BIT DATA") ||          //@P3C@K54
                           typeName.equals("VARBINARY") ||                          //@K54
                           typeName.equals("DATALINK"))                             //@K54
                        {                                                           //@K54
                            length = BinaryConverter.byteArrayToUnsignedShort(rawBytes_, rowDataOffset_ + offset);    //@K54 //get actual length of data
                            length += 2;        //Add two bytes for length portion on datastream                                        //@K54
                        }                                                           //@K54
                        else if(typeName.equals("VARGRAPHIC") ||                         //@K54  graphics are two-byte characters
                                typeName.equals("LONG VARGRAPHIC") ||                          //@K54
                                typeName.equals("NVARCHAR"))                        //@PD61
                        {                                                           //@K54
                            length = (2 * BinaryConverter.byteArrayToUnsignedShort(rawBytes_, rowDataOffset_ + offset));    //@K54 //get actual length of data
                            length += 2;        //Add two bytes for length portion on datastream                                        //@K54
                        }
                        else
                            length = serverFormat_.getFieldLength (j);             //@K54 //get fixed size of data

------------------------ end old Code */ 


                        offset += length;                                           //@K54
                        dataLength_[j] = length;                                    //@K54
                        arrayDataLength_[j] = serverFormat_.getArrayFieldLength(j); /*@G2A*/ 
                    }                                                               //@K54
                }                                                                   //@K54
                else if(wasCompressed){     // If varying length field compression was used on one request, and not a subsequent fetch, we need to reset the data lengths and offsets based on the server format
                    wasCompressed = false;
                    setOriginalData();
                }
            }
            else
                rowDataOffset_ = -1;
        }
        catch(DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        // Reset so that data gets retranslated.
        for(int i = 0; i < translated_.length; ++i)
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
        if(name.startsWith("\"") && name.endsWith("\""))    //@D6a @DELIMc
        {
            name = JDUtilities.stripOuterDoubleQuotes(name);  //@DELIMa
            for(int i=1; i<=sqlData_.length; ++i)
                if(name.equals(getFieldName(i)))    //@D6c (used to be equalsIgnoreCase)
                    return i;
        }
        else
        {
            //@PDA  use hashtable to reduce number of toUpper calls
            //X.equalsIgnoreCase(Y) converts both X and Y to uppercase.
            if(insensitiveColumnNames_ == null)
            {
                // Create a new hash table to hold all the column name/number mappings.
                insensitiveColumnNames_ = new Hashtable(sqlData_.length);

                // cache all the column names and numbers.
                for (int i = sqlData_.length; i >= 1; i--)//@pdc 776N6J (int i = 1; i <= sqlData_.length; i++)
                {
                    String cName = getFieldName(i);

                    // Never uppercase the name from the database. If the name is
                    // supposed to be uppercase, it will already be. If it isn't, it will be
                    // lowercase and its double quotes will be missing.
                    insensitiveColumnNames_.put(cName, new Integer(i));
                }
            }

            // Look up the mapping in our cache. First look up using the user's casing
            Integer x = (Integer) insensitiveColumnNames_.get(name);
            if (x != null)
                return (x.intValue());
            else
            {
                String upperCaseName = name.toUpperCase();
                x = (Integer) insensitiveColumnNames_.get(upperCaseName);

                if (x != null)
                {
                    // Add the user's casing
                    insensitiveColumnNames_.put(name, x);
                    return (x.intValue());
                }
            }
        }

        if (JDTrace.isTraceOn()) {
            JDTrace.logInformation (this, "Did not find column " + name);
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i <= sqlData_.length; i++)
            {
               sb.append("["+i+"]="+getFieldName(i)+" ");
            }
            JDTrace.logInformation (this, "Columns are " + sb.toString());
        }

        JDError.throwSQLException (JDError.EXC_COLUMN_NOT_FOUND);
        return -1;
    }



    public int getFieldCount ()
    {
        return sqlData_.length;
    }



    public int getFieldLOBLocatorHandle (int index)    // @C2A
    throws SQLException    // @C2A
    {    // @C2A
        return lobLocatorHandles_[index-1];    // @C2A
    }    // @C2A



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
            if(fieldNames_[index0] == null) {
                String thisFieldName = serverFormat_.getFieldName (index0,
                    connection_.getConverter (serverFormat_.getFieldNameCCSID (index0)));
              
                // The native JDBC driver behavior is to only trim spaces from  
                // the end of the string and to preserve leading whitespace. 
                // Use the same behavior for the toolbox driver.  @O7A
                if (thisFieldName.length() > 0) {
                  thisFieldName = JDUtilities.trimTrailingSpace(thisFieldName); 
                } 
                
                fieldNames_[index0] = thisFieldName;
            }

            //Bidi-HCG - add converion from serverFormat_.getFieldNameCCSID (index0) to "bidi string type" here
            //Bidi-HCG start
            boolean reorder = connection_.getProperties().getBoolean(JDProperties.BIDI_IMPLICIT_REORDERING);
            if(reorder){
            	String value_ = fieldNames_[index0];
            	value_ = AS400BidiTransform.convertDataFromHostCCSID(value_, connection_, serverFormat_.getFieldNameCCSID (index0));
            	return value_;
            }
            //Bidi-HCG end
            return fieldNames_[index0];
        }
        catch(DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return null;
        }
    }


    /*@L1A*/    
    public String getSQLTypeName (int index)
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
            if(sqlTypeNames_[index0] == null) {
                int ccsid = serverFormat_.getUDTNameCCSID (index0);
                if (ccsid > -1 )  { 
                  if (ccsid == 0) {
                    // There is a bug in the host server where the CCSID was not returned
                    // To keep working, just use CCSID 37
                    ccsid = 37; 
                  }
                sqlTypeNames_[index0] = 
                    serverFormat_.getUDTName (index0,
                                              connection_.getConverter (ccsid)).trim();
                

                }
                if (sqlTypeNames_[index0] == null) {
                  sqlTypeNames_[index0] = sqlData_[index0].getTypeName(); 
                }
            }
            if (sqlTypeNames_[index0] != null) { 
            boolean reorder = connection_.getProperties().getBoolean(JDProperties.BIDI_IMPLICIT_REORDERING);
            if(reorder){
              String value_ = sqlTypeNames_[index0];
              value_ = AS400BidiTransform.convertDataFromHostCCSID(value_, connection_, serverFormat_.getFieldNameCCSID (index0));
              return value_;
            }
            }
            //Bidi-HCG end
            return sqlTypeNames_[index0];
        }
        catch(DBDataStreamException e)
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
            if((rowIndex_ >= 0) && (translated_[index0] == false))
            {

                // @E1D // The CCSID returned in the data format is not
                // @E1D // necessarily correct (says the database host server
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
                ConvTable ccsidConverter = connection_.getConverter(ccsids_[index0]);    // @E1A @P0C

                // If there are bytes, then do a translation.
                if(rawBytes_ != null)
                {
                    //set array length so convertFromRawBytes knows how many to convert
                    if(sqlData_[index0].getType() == java.sql.Types.ARRAY)                                          //@array
                    {                                                                                               //@array
                        int outputIndex0 = getVariableOutputIndex(index0);                           //@arrayout (arrays in DBVariableData only contain output parms here, so need to skip any input parms in other JDServer arrays)
                        int arrayCount = ((DBVariableData)serverData_).getIndicatorCountsFromHost()[outputIndex0] ; //@arrayout
                        ((SQLArray)sqlData_[index0]).setArrayCount( arrayCount );        //@array indicatorCountsFromHost will be array count if array type
                        //SQLArray.convertFromRawBytes will create array elements and iterate calling convertFromRayBytes and it knows array length from above call
                        sqlData_[index0].convertFromRawBytes (rawBytes_,
                                                           rowDataOffset_ + dataOffset_[index0],
                                                           ccsidConverter);                                       //@array
                        //We need to set null values into the array elements here since there is not JDBC wasNull method like there is for ResultSet.
                        //For an array elements, a null value is just a null array element.
                        for(int i = 0; i < arrayCount; i++)                                                       //@array
                        {                                                                                         //@array
                            if((serverData_ != null) && (serverData_.getIndicator(rowIndex_, outputIndex0, i) == -1))   //@array //@arrayout
                                ((SQLArray)sqlData_[index0]).setElementNull(i);                                        //@array //@nullelem
                        }                                                                                         //@array
                    }                                                                                             //@array
                    else
                    {
                      try { 
                        sqlData_[index0].convertFromRawBytes (rawBytes_,
                                                          rowDataOffset_ + dataOffset_[index0],
                                                          ccsidConverter);
                      } catch (NumberFormatException nfe) { 
                        if (JDTrace.isTraceOn()) {
                          JDTrace.logInformation (this, "Caught number format exception rowDataOffset_="+rowDataOffset_+" dataOffset_["+index0+"]="+dataOffset_[index0]);
                          for (int  i = 0; i < dataOffset_.length; i++) { 
                            JDTrace.logInformation (this, "....dataOffset_["+i+"]="+dataOffset_[i]);
                          }
                          JDTrace.logInformation (this, "....rowIndex_="+rowIndex_);
                          serverData_.logInformation("..."); 
                        }
                        throw nfe; 
                      }
                    }
                    translated_[index0] = true;
                }

            }

            return sqlData_[index0];
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return null;
        }
    }

    //@arrayout
    /**
    Calculates the index by skipping any non output parms (inout parms are output)

    @param  index   The field index (0-based).
    @return         new index into an output-only parm list (-1 if non are output parms)

    @exception  SQLException    If an error occurs.
    **/
    int getVariableOutputIndex(int index) throws SQLException
    {
        int newIndex = 0;
        for(int x = 0; x <= index; x++) //@index
        {
            if(isOutput(x+1)) //isOutput is 1 based //@index
                newIndex++;
        }
        return newIndex-1;
    }

    public SQLData getSQLType (int index)
    throws SQLException
    {
        return sqlData_[index - 1];
    }

    /**
    Is there a data mapping error for the field?

    @param      index   The field index (1-based).
    @return             true or false

    @exception  SQLException    If an error occurs.
    **/
    public boolean isDataMappingError(int index)
    throws SQLException
    {
        try
        {
            int outputIndex = index;                                  //@arrayout
            if(serverData_ instanceof DBVariableData)                 //@arrayout
                outputIndex = getVariableOutputIndex(index-1) + 1;    //@arrayout (arrays in DBVariableData only contain output parms here, so need to skip any input parms in other JDServer arrays)

            if((serverData_ != null) && (serverData_.getIndicator(rowIndex_, outputIndex - 1) == -2))  //@arrayout
                return true;
            else
                return false;
        }
        catch(DBDataStreamException e)
        {
            JDError.throwSQLException(JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return false;
        }
    }

    /**
    Is the field value SQL NULL?

    @param      index   The field index (1-based).
    @return             true or false

    @exception  SQLException    If an error occurs.
    **/
    public boolean isNull(int index)
    throws SQLException
    {
        try
        {
            int outputIndex = index;                                  //@arrayout
            if(serverData_ instanceof DBVariableData)                 //@arrayout
                outputIndex = getVariableOutputIndex(index-1) + 1;    //@arrayout (arrays in DBVariableData only contain output parms here, so need to skip any input parms in other JDServer arrays)

            if((serverData_ != null) && (serverData_.getIndicator(rowIndex_, outputIndex - 1) == -1)) //@arrayout
                return true;
            else
                return false;
        }
        catch(DBDataStreamException e)
        {
            JDError.throwSQLException(JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
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
        {    //@F2A
            return(((sqlTypes_[index-1] & 0x0001) != 0)
                   ? ResultSetMetaData.columnNullable
                   : ResultSetMetaData.columnNoNulls);
        }
        catch(ArrayIndexOutOfBoundsException e)    //@F2A
        {
            //@F2A
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);    //@F2A
            return 0;    //@F2A
        }    //@F2A
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
    data.  For an array, this is arrayCount * arrayDataLen.

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
     * Return the length of element in an array
     * @param index index of array element to examine
     * @return length of the array data element
     */
     /*@G2A*/ 
    
    public int getArrayDataLength(int index ) {
      return arrayDataLength_[index-1];
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
        catch(DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return -1;
        }
    }


    //@CRS - performance
    private synchronized void initParmTypes()
    {
      int count = getFieldCount();
      parameterTypes_ = new int[count];
      for (int i=0; i<count; ++i) parameterTypes_[i] = -1;
    }

    /**
    Is the field an input value?

    @param  index   The field index (1-based).
    @return         true or false

    @exception  SQLException    If an error occurs.
    **/
    boolean isInput(int index) throws SQLException
    {
      if (parameterTypes_ == null)
      {
        initParmTypes();
      }

      int i = index-1;
      if (parameterTypes_[i] == -1)
      {
        try
        {
          parameterTypes_[i] = serverFormat_.getFieldParameterType(i) & 0x00FF;
        }
        catch(DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
        }
      }

      return parameterTypes_[i] == 0xF0 || parameterTypes_[i] == 0xF2;
    }



    /**
    Is the field an output value?

    @param  index   The field index (1-based).
    @return         true or false

    @exception  SQLException    If an error occurs.
    **/
    boolean isOutput(int index) throws SQLException
    {
      if (parameterTypes_ == null)
      {
        initParmTypes();
      }

      int i = index-1;
      if (parameterTypes_[i] == -1)
      {
        try
        {
          parameterTypes_[i] = serverFormat_.getFieldParameterType(i) & 0x00FF;
        }
        catch(DBDataStreamException e)
        {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
        }
      }

      if(parameterTypes_[i] == 0x000000F1 || parameterTypes_[i] == 0x000000F2) //@index
          return true;
      else
          return false;
    }


   /** Update the settings used by this object
   * @param settings settings to use for this object
    * 
    */
   public void updateSettings(SQLConversionSettings settings) {
     if (sqlData_ != null) { 
     for (int i = 0; i < sqlData_.length; i++) {
     sqlData_[i].updateSettings(settings); 
     }
     }
   }


  /* Method to restore saved parameters */ 
  
  public void restoreSavedParameters(JDServerRow parameterRow_) {
    SQLData[] oldSqlData = parameterRow_.sqlData_; 
    for (int i = 0; i < sqlData_.length; i++) {
      if (i < oldSqlData.length) { 
        try { 
           SQLData oldOne = oldSqlData[i];
           Calendar calendar = AS400Calendar.getGregorianInstance();
           int scale = oldOne.getScale(); 
           Object object; 
           if (oldOne instanceof  SQLLocator) {
             // Must set handle before setting data 
             ((SQLLocator) sqlData_[i]).setHandle(getFieldLOBLocatorHandle(i+1)); 
             object = oldOne.getSavedValue(); 
             sqlData_[i].set(object, calendar, scale);
           }else { 
             object = oldOne.getSavedValue();
             if (object != null) { 
               sqlData_[i].set(object, calendar, scale);
             } else {
               if (JDTrace.isTraceOn()) {
                 JDTrace.logInformation(this, "restoredSavedParameters did not find saved parameter for i="+i);
               }
             }
           }
        
        } catch (SQLException e) { 
          if (JDTrace.isTraceOn()) {
            JDTrace.logException(this, "Restoring parameters", e); 
          }
        }
      }
    }
    
    
    
  }



  public void saveValues() throws SQLException {
    for (int i = 0; i < sqlData_.length; i++) { 
       sqlData_[i].saveValue(); 
    }
  }
     

}
