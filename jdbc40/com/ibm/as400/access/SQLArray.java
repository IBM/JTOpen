///////////////////////////////////////////////////////////////////////////////

//JTOpen (IBM Toolbox for Java - OSS version)                                 

//Filename: SQLArray.java

//The source code contained herein is licensed under the IBM Public License   
//Version 1.0, which has been approved by the Open Source Initiative.         
//Copyright (C) 2009-2009 International Business Machines Corporation and     
//others. All rights reserved.                                                

///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
/* ifdef JDBC40 */
import java.sql.NClob;
import java.sql.RowId;
/* endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 */
import java.sql.SQLXML;
/* endif */ 
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

//@array new class
class SQLArray implements SQLData
{
	static final String copyright = "Copyright (C) 2009-2010 International Business Machines Corporation and others.";

    private SQLData[] values_; // Since lobs do conversion during execute (not
                                // setLob()), let SQLData objects manage
                                // conversion and truncation etc for each
                                // element
    private int arrayCount_ = 0; 
    private SQLData contentTemplate_; // This is just a reference to the SQLData type that this array contains which is cloned at set() time
                                      //It will reflect the prepare/describe metadata both for DB input and output
    private int elemDataTypeLen_ = 0; //needed for to/fromRawBytes
    private int vrm_;
    
    SQLArray()
    {
        // restrict type-less array construction
    }

    /**
     * 
     * @param elemDataTypeLen The length of the datatype that are in the array
     * @param contentTemplate A dummy SQLDataX object used for cloning
     * @param vrm Version
     */
    public SQLArray(int elemDataTypeLen, SQLData contentTemplate, int vrm)
    {

        values_ = null; // length not known at prepare/describe time.
        contentTemplate_ = contentTemplate; // contains type, length, etc
        vrm_ = vrm;
        elemDataTypeLen_ = elemDataTypeLen;
    }

    public Object clone()
    {

        //Just clone with attrs with no data 
        SQLArray cpy = new SQLArray(elemDataTypeLen_, contentTemplate_, vrm_);
    
        return cpy;
    }

    /* populate values_[] with data from host. */
    public void convertFromRawBytes(byte[] rawBytes, int offset,
            ConvTable converter) throws SQLException
    {
        values_ = new SQLData[arrayCount_];
        for (int x = 0; x < arrayCount_; x++)
        {
            values_[x] = (SQLData)contentTemplate_.clone();  //create empty SQLX objects
            //No need to deal with locators in arrays here.  They are not supported in locators due to QQ constraint   
            try{  //@dec
                values_[x].convertFromRawBytes(rawBytes, offset, converter);
            }catch(NumberFormatException e)   //@dec
            { //@dec
                //ignore since null array elements will have invalid decimal/numeric value bits
            } //@dec
            offset += elemDataTypeLen_; //values_[x].getActualSize();
        }
    }

    /* copy data from values_[] to stream for sending to host. */
    public void convertToRawBytes(byte[] rawBytes, int offset,
            ConvTable ccsidConverter) throws SQLException
    {
        for (int x = 0; x < arrayCount_; x++)
        {
            if(values_ != null && values_[x] != null)     //@array null element
                values_[x].convertToRawBytes(rawBytes, offset, ccsidConverter);
            else
            {
                //for arrays, we set the element null indicator here since we don't really have the offset before now
                contentTemplate_.convertToRawBytes(rawBytes, offset, ccsidConverter); 
            }
            offset += elemDataTypeLen_; //values_[x].getActualSize(); 
        }
    }

    public void set(Object object, Calendar calendar, int scale)
            throws SQLException
    {
        //note that we could be getting a user-defined Array as input
        Object[] data = (Object[]) ((Array) object).getArray(); // These elements must be wrapped in a type wrapper before calling (ie Integer, not int)
        if(data == null)
            data = new Object[0]; //if null array just make 0 length array
        
        arrayCount_ = data.length;
        values_ = new SQLData[arrayCount_];
        
        boolean isSQLData = false;
        //since values can be null, don't check if data_[0] is instance of SQLData
        if((arrayCount_ > 0) && (data[0]  != null) && (data instanceof SQLData[]))
            isSQLData = true; //data was output from previous and is still in SQLData object
              
        for (int x = 0; x < arrayCount_; x++)
        {
            //prepare/describe is the type that we ARE.
            //create sqlX objects and let them do conversion from input type to actual type
            values_[x] = (SQLData)contentTemplate_.clone();  //create empty SQLX objects
            //@array no locators supported in qq of arrays...none to set here
           
            Object inObj;
            if(isSQLData)
                inObj = ((SQLData)data[x]).getObject(); 
            else 
                inObj = data[x];//can be null array element
            
            if(inObj == null)
                setElementNull(x);
            else
                values_[x].set(inObj, calendar, scale); //let the SQLX objects do the conversions
        }
    }

    public int getActualSize()
    {
        
        int totalSize = 0;
        for (int x = 0; x < arrayCount_; x++)
        {
            totalSize += values_[x].getActualSize();
        }
        return totalSize;
    }

    public InputStream getAsciiStream() throws SQLException
    {

        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public BigDecimal getBigDecimal(int scale) throws SQLException
    {

        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream getBinaryStream() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Blob getBlob() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public boolean getBoolean() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return false;
    }

    public byte getByte() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public byte[] getBytes() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Reader getCharacterStream() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Clob getClob() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Reader getNCharacterStream() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

/* ifdef JDBC40 */
    public NClob getNClob() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
/* endif */ 
    public String getNString() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
/* ifdef JDBC40 */

    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
/* endif */ 
    
/* ifdef JDBC40 */

    public SQLXML getSQLXML() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
/* endif */ 
    public Date getDate(Calendar calendar) throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public double getDouble() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public float getFloat() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public int getInt() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public long getLong() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public short getShort() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public String getString() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Time getTime(Calendar calendar) throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Timestamp getTimestamp(Calendar calendar) throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream getUnicodeStream() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    // @array
    public Array getArray() throws SQLException
    {
        /*Object[] content = new Object[values_.length];
        for(int x = 0; x < values_.length; x++)
        {
            content[x] = values_[x].getObject(); //get in default object type
        }*/
        
        //return new AS400JDBCArray with array of SQLData[] as elements
        return new AS400JDBCArray(contentTemplate_.getTypeName(), values_, vrm_, null);
    }

    public String getCreateParameters()
    {

        return null;
    }

    public int getDisplaySize()
    {

        return 0;
    }

    public String getJavaClassName()
    {
        return "java.sql.Array";
    }

    public String getLiteralPrefix()
    {
        return null;
    }

    public String getLiteralSuffix()
    {
        return null;
    }

    public String getLocalName()
    {
        return "ARRAY";
    }

    public int getMaximumPrecision()
    {
        return 0;
    }

    public int getMaximumScale()
    {
        return 0;
    }

    public int getMinimumScale()
    {

        return 0;
    }

    public int getNativeType()
    {
        return SQLData.NATIVE_ARRAY; 
        //For arrays, we don't have a native type number, just a bit flag in the stream header
    }
    
    //array only method
    public int getElementNativeType()
    {
        return contentTemplate_.getNativeType(); //works if elems are null
        //if(values_ != null && values_.length > 0)
        //    return values_[0].getNativeType();
        //else
         //   return getNativeType(); //should not ever return this with current design, but better than null pointers
       
    }
    
  /*  //array only method
    public int getElementSize()
    {
        return getActualSize();
       
    }
*/
    public Object getObject() throws SQLException
    {
        return getArray();
    }

    public int getPrecision()
    {

        return 0;
    }

    public int getRadix()
    {

        return 0;
    }

    public int getSQLType()
    {

        return SQLData.ARRAY;
    }

    public int getScale()
    {

        return 0;
    }

    public int getTruncated()
    {
        return 0;
    }
    public boolean getOutOfBounds() {
      return false; 
    }

    public int getType() {
         
        return java.sql.Types.ARRAY;
    }

    public String getTypeName()
    {

        return "ARRAY";
    }

    public boolean isSigned()
    {
        return values_[0].isSigned();
    }

    public boolean isText()
    {
        return values_[0].isText();

    }
    
    public void setArrayCount(int count)
    {
        arrayCount_ = count;
    }
    
    public int getArrayCount()
    {
        return arrayCount_;
    }

    public void setElementNull(int element)
    {
        values_[element] = null;
    }
    
    public boolean isElementNull(int element)
    {
        if( values_[element] == null)
            return true;
        else
            return false;
    }
}
