///////////////////////////////////////////////////////////////////////////////
//
//JTOpen (IBM Toolbox for Java - OSS version)                                 
//
//Filename: AS400JDBCArray.java
//
//The source code contained herein is licensed under the IBM Public License   
//Version 1.0, which has been approved by the Open Source Initiative.         
//Copyright (C) 2009-2009 International Business Machines Corporation and     
//others. All rights reserved.                                                
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;


// @array new class
/** AS400JDBCArray is an implementation of java.sql.Array and contains an array of JDBC data.  
 *  It provides mapping in the Java programming language for the SQL type <code>ARRAY</code>.
 *  Currently AS400JDBCArray is only supported by using stored procedure input/output parameters.
 */

public class AS400JDBCArray implements Array
{

   
    private Object[] data_; //!!!Note this array may hold generic data such as Integers, or may hold special "output" 
                              //data in form of SQLData objects.  This is needed for managing efficient ArrayResultSet 
                              //data conversion.  But we don't want a user generated AS400JDBCArray (via Connection create method)
                              //to contain SQLData[] since at that point in time, we don't really know the prepare/describe data
                              //attributes.  And then we would have to convert data twice, once in AS400JDBCArray and once in SQLArray.
          
    private String typeName_;  //typename is supplied by user at creation or from hostserver prepare/describe
    //protected int type_;
    private int vrm_;
    private AS400JDBCConnection con_;
   
    // This is just a reference to the SQLData type that this array contains.  It is not a reflection from a prepare/describe, but is
    //just used for any potential temporary conversion needed before final conversion before setting in actual PreparedStatement going to hostserver (ie Array.getResultSet() -> ars.getString(1))
    private SQLData contentTemplate_;   
    
    
    //if data_[0] contains an SQLData, then contentTemplate_ will point to it!
    private boolean isSQLData_ = false; //true if data[0]_ == SQLData 
    private boolean isNull_ = false;

    private AS400JDBCArray(){} //restrict
    

    /**
    Constructs an AS400JDBCArray object.
    @param  typeName    The typeName.
    @param  data        The data.
    @param  vrm         The version.
    @param  con         Connection.
    **/
    AS400JDBCArray(String typeName, Object[] data, int vrm, AS400JDBCConnection con) throws SQLException
    {
        /*
         * typeName is used to create a dummy SQLData object used for potential data conversion when data[] is not of type SQLData[].
         * Pass in a connection if the Object[] data is not SQLData.  This will use connection's default conversion data.
         * Throws exception if typeName is not valid per SQLDataFactory.
         */
        
        typeName_ = typeName;
        vrm_ = vrm;        
       
        //here, we allow null array, or array[0] or array[x] of null elements
        //we will not interpret array[0] as a null array.(at least for now)
        //if null, then create a 0 length array and set flag.  This avoids messy code.
        if(data == null)
        {
            data_ = new Object[0];
            isNull_ = true;
        }
        else
            data_ = data; //Note that data_ is array of Objects.  When array is output then data_ will actually be an array of SQLData[] (needed for conversion)
         
        //check if array of SQLData 
        //since values can be null, don't check if data_[0] is instance of SQLData
        if(data_ instanceof SQLData[]) 
            isSQLData_ = true;
        else
        {
            //in this case, we will create our own temporary SQLData object used for potential conversion in AS400JDBCArrayResultSet
            isSQLData_ = false; 
        }
        
        con_ = con; //@arrayrs
       
        if(data_.length > 0 && isSQLData_ && data_[0] != null) //@nullelem
            contentTemplate_ = (SQLData)data_[0];
        else
            contentTemplate_ = SQLDataFactory.newData(typeName, 1, 1, 1, 37, null, vrm_, (con == null ? null: con.getProperties())); //@array
        //allow max for local conversion only since it is not associated with a column on hostserver yet
    }

    

    /**
     This method will free the internal memory that this object holds.
     **/
    synchronized public void free () throws SQLException
    { 
    	//na  no locators can be in arrays.
    }


    /**
     * Retrieves the SQL type name of the elements in the array designated by
     * this <code>Array</code> object. If the elements are a built-in type, it
     * returns the database-specific type name of the elements. If the elements
     * are a user-defined type (UDT), this method returns the fully-qualified
     * SQL type name.
     * 
     * @return database-specific name for a
     *         built-in base type; or the fully-qualified SQL type name for a
     *         base type that is a UDT
     * @exception SQLException
     *                if an error occurs while attempting to access the type
     *                name
     * 
     */
    public String getBaseTypeName() throws SQLException
    {
        return typeName_;
    }

    /**
     * Retrieves the JDBC type of the elements in the array designated by this
     * <code>Array</code> object.
     * 
     * @return a constant from the class {@link java.sql.Types} that is the type
     *         code for the elements in the array designated by this
     *         <code>Array</code> object
     * @exception SQLException
     *                if an error occurs while attempting to access the base
     *                type
     * 
     */
    public int getBaseType() throws SQLException
    {
      
        return contentTemplate_.getType();

    }

    /**
     * Retrieves the contents of the SQL <code>ARRAY</code> value designated
     * by this <code>Array</code> object in the form of an array in the Java
     * programming language. This version of the method <code>getArray</code>
     * uses the type map associated with the connection for customizations of
     * the type mappings.
     * 
     * @return an array in the Java programming language that contains the
     *         ordered elements of the SQL <code>ARRAY</code> value designated
     *         by this <code>Array</code> object
     * @exception SQLException
     *                if an error occurs while attempting to access the array
     * 
     */
    synchronized public Object getArray() throws SQLException
    { 
        //data could be sqlData if output from query or input Ojbects such as Integer or Clob

        return getArrayX(1, data_.length);
    }

    /**
     * Retrieves a slice of the SQL <code>ARRAY</code> value designated by
     * this <code>Array</code> object, beginning with the specified
     * <code>index</code> and containing up to <code>count</code> successive
     * elements of the SQL array. This method uses the type map associated with
     * the connection for customizations of the type mappings.
     * 
     * @param index
     *            the array index of the first element to retrieve; the first
     *            element is at index 1
     * @param count
     *            the number of successive SQL array elements to retrieve
     * @return an array containing up to <code>count</code> consecutive
     *         elements of the SQL array, beginning with element
     *         <code>index</code>
     * @exception SQLException
     *                if an error occurs while attempting to access the array
     */
    synchronized public Object getArray(long index, int count)
            throws SQLException
    {
       
        if ((index <= 0) || (count < 0) || (index > data_.length)
                || ((index + count) > (data_.length + 1)))
            JDError.throwSQLException(JDError.EXC_BUFFER_LENGTH_INVALID);

        return getArrayX(index, count);
    }

    /**
     * Retrieves  Array ResultSet that contains the elements of the SQL
     * <code>ARRAY</code> value designated by this <code>Array</code>
     * object. 
     * The result set contains one row for each array element, with two columns
     * in each row. The second column stores the element value; the first column
     * stores the index into the array for that element (with the first array
     * element being at index 1). The rows are in ascending order corresponding
     * to the order of the indices.
     * 
     * @return an Array ResultSet object containing one row for each of the
     *         elements in the array designated by this <code>Array</code>
     *         object, with the rows in ascending order based on the indices.
     * @exception SQLException
     *                if an error occurs while attempting to access the array
     */
    synchronized public ResultSet getResultSet()
            throws SQLException
    {
        

        AS400JDBCArrayResultSet rs = new AS400JDBCArrayResultSet(data_, contentTemplate_, isSQLData_, getBaseType(), vrm_, con_);
        if (JDTrace.isTraceOn())
            JDTrace.logInformation(this, "getResultSet");
        return rs;
    }

    /**
     * Retrieves a result set holding the elements of the subarray that starts
     * at index <code>index</code> and contains up to <code>count</code>
     * successive elements. This method uses the connection's type map to map
     * the elements of the array if the map contains an entry for the base type.
     * Otherwise, the standard mapping is used.
     * <P>
     * The result set has one row for each element of the SQL array designated
     * by this object, with the first row containing the element at index
     * <code>index</code>. The result set has up to <code>count</code> rows
     * in ascending order based on the indices. Each row has two columns: The
     * second column stores the element value; the first column stores the index
     * into the array for that element.
     * 
     * @param index
     *            the array index of the first element to retrieve; the first
     *            element is at index 1
     * @param count
     *            the number of successive SQL array elements to retrieve
     * @return a <code>ResultSet</code> object containing up to
     *         <code>count</code> consecutive elements of the SQL array
     *         designated by this <code>Array</code> object, starting at index
     *         <code>index</code>.
     * @exception SQLException
     *                if an error occurs while attempting to access the array
     * 
     */
    synchronized public ResultSet getResultSet(long index, int count)
            throws SQLException
    {
        
        
        int intIndex = (int)index - 1;  //make 0 based
        Object[] retArry = new Object[count]; //!!!!here also
        
        if( data_.length > 0 )
        {
          
            for(int x = 0 ; x < count; x++)
            {
                retArry[x] = data_[x + intIndex];  //just return values that were set by user with no conversion
            }
        }
        else
        {
            retArry = data_;  //length 0 or null value
        }     


        AS400JDBCArrayResultSet rs = new AS400JDBCArrayResultSet(retArry, contentTemplate_, isSQLData_, getBaseType(), vrm_, con_);
        if (JDTrace.isTraceOn())
            JDTrace.logInformation(this, "getResultSet");
        return rs;
    }

    /**
     * Retrieves a result set that contains the elements of the SQL
     * <code>ARRAY</code> value designated by this <code>Array</code>
     * object. This method uses the specified <code>map</code> for type map
     * customizations unless the base type of the array does not match a
     * user-defined type in <code>map</code>, in which case it uses the
     * standard mapping. This version of the method <code>getResultSet</code>
     * uses either the given type map or the standard mapping; it never uses the
     * type map associated with the connection.
     * <p>
     * The result set contains one row for each array element, with two columns
     * in each row. The second column stores the element value; the first column
     * stores the index into the array for that element (with the first array
     * element being at index 1). The rows are in ascending order corresponding
     * to the order of the indices.
     * 
     * @param map
     *            contains the mapping of SQL user-defined types to classes in
     *            the Java programming language
     * @return a <code>ResultSet</code> object containing one row for each of
     *         the elements in the array designated by this <code>Array</code>
     *         object, with the rows in ascending order based on the indices.
     * @exception SQLException
     *                if an error occurs while attempting to access the array
     * 
     */
    synchronized public Object getArray(java.util.Map map)
            throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    /**
     * Retreives a slice of the SQL <code>ARRAY</code> value designated by
     * this <code>Array</code> object, beginning with the specified
     * <code>index</code> and containing up to <code>count</code> successive
     * elements of the SQL array.
     * <P>
     * This method uses the specified <code>map</code> for type map
     * customizations unless the base type of the array does not match a
     * user-defined type in <code>map</code>, in which case it uses the
     * standard mapping. This version of the method <code>getArray</code> uses
     * either the given type map or the standard mapping; it never uses the type
     * map associated with the connection.
     * 
     * @param index
     *            the array index of the first element to retrieve; the first
     *            element is at index 1
     * @param count
     *            the number of successive SQL array elements to retrieve
     * @param map
     *            a <code>java.util.Map</code> object that contains SQL type
     *            names and the classes in the Java programming language to
     *            which they are mapped
     * @return an array containing up to <code>count</code> consecutive
     *         elements of the SQL <code>ARRAY</code> value designated by this
     *         <code>Array</code> object, beginning with element
     *         <code>index</code>
     * @exception SQLException
     *                if an error occurs while attempting to access the array
     * 
     */
    synchronized public Object getArray(long index, int count, java.util.Map map)
            throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    /**
     * Retrieves a result set that contains the elements of the SQL
     * <code>ARRAY</code> value designated by this <code>Array</code>
     * object. This method uses the specified <code>map</code> for type map
     * customizations unless the base type of the array does not match a
     * user-defined type in <code>map</code>, in which case it uses the
     * standard mapping. This version of the method <code>getResultSet</code>
     * uses either the given type map or the standard mapping; it never uses the
     * type map associated with the connection.
     * <p>
     * The result set contains one row for each array element, with two columns
     * in each row. The second column stores the element value; the first column
     * stores the index into the array for that element (with the first array
     * element being at index 1). The rows are in ascending order corresponding
     * to the order of the indices.
     * 
     * @param map
     *            contains the mapping of SQL user-defined types to classes in
     *            the Java programming language
     * @return a <code>ResultSet</code> object containing one row for each of
     *         the elements in the array designated by this <code>Array</code>
     *         object, with the rows in ascending order based on the indices.
     * @exception SQLException
     *                if an error occurs while attempting to access the array
     * 
     */
    synchronized public ResultSet getResultSet(java.util.Map map)
            throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    /**
     * Retrieves a result set holding the elements of the subarray that starts
     * at index <code>index</code> and contains up to <code>count</code>
     * successive elements. This method uses the specified <code>map</code>
     * for type map customizations unless the base type of the array does not
     * match a user-defined type in <code>map</code>, in which case it uses
     * the standard mapping. This version of the method
     * <code>getResultSet</code> uses either the given type map or the
     * standard mapping; it never uses the type map associated with the
     * connection.
     * <P>
     * The result set has one row for each element of the SQL array designated
     * by this object, with the first row containing the element at index
     * <code>index</code>. The result set has up to <code>count</code> rows
     * in ascending order based on the indices. Each row has two columns: The
     * second column stores the element value; the first column stroes the index
     * into the array for that element.
     * 
     * @param index
     *            the array index of the first element to retrieve; the first
     *            element is at index 1
     * @param count
     *            the number of successive SQL array elements to retrieve
     * @param map
     *            the <code>Map</code> object that contains the mapping of SQL
     *            type names to classes in the Java(tm) programming language
     * @return a <code>ResultSet</code> object containing up to
     *         <code>count</code> consecutive elements of the SQL array
     *         designated by this <code>Array</code> object, starting at index
     *         <code>index</code>.
     * @exception SQLException
     *                if an error occurs while attempting to access the array
     * 
     */
    synchronized public ResultSet getResultSet(long index, int count,
            java.util.Map map) throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    // --------------------jdbc internal methods-----------------

     

    /* Get part or all of array.  index is 1-based.  
     * If array content is output from database, then convert data via getObject().
     */
    private Object getArrayX(long index, int count) throws SQLException
    {
         
        
        int intIndex = (int)index - 1;  //make 0 based
        if((data_.length >= 0) && isSQLData_) //@nullelem
        {
        
            //create array of same type as data_
            Class dummySQLXType = null; //@nullelem
            try{
                dummySQLXType = Class.forName( contentTemplate_.getJavaClassName()); //data_[0]).getObject(); //returns column's rs.getX() type (ie Integer, String, etc) //@nullelem
            }catch( Exception e)
            {
                try{
                    dummySQLXType = Class.forName("java.lang.Object");
                }catch(Exception ee){
                    dummySQLXType = null;
                }
             }
            Object retArry = java.lang.reflect.Array.newInstance (dummySQLXType, count);
            for(int x = 0 ; x < count; x++)
            {
                if(data_[x + intIndex] != null)
                    ((Object[]) retArry)[x] = ((SQLData) data_[x + intIndex]).getObject();  //convert based on SQLData
                else
                    ((Object[]) retArry)[x] = null; //@nullelem
            }
            return retArry; //returns array of types such as Ingeter[] etc
        }
        else if( (data_.length > 0) && !isSQLData_ )
        {
            Object retArry = java.lang.reflect.Array.newInstance (data_.getClass().getComponentType(), count); //array of same type as user passed in
            for(int x = 0 ; x < count; x++)
            {
                ((Object[]) retArry)[x] = data_[x + intIndex];  //just return values that were set by user with no conversion
            }
            return retArry;
        }
        else
        {
            return data_;  //length 0 or null value
        }     
    }
    
}
