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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

// @array new class
/**
 * AS400JDBCArray is an implementation of java.sql.Array and contains an array
 * of JDBC data. It provides mapping in the Java programming language for the
 * SQL type <code>ARRAY</code>. Currently AS400JDBCArray is only supported by
 * using stored procedure input/output parameters.
 */

public class AS400JDBCArray implements Array {

  private Object[] data_; // !!!Note this array may hold generic data such as
                          // Integers,
  // or may hold special "output" data in form of SQLData
  // objects. This is needed for managing efficient
  // ArrayResultSet data conversion. But we don't want a user
  // generated AS400JDBCArray (via Connection create method)
  // to contain SQLData[] since at that point in time, we
  // don't really know the prepare/describe data attributes.
  // And then we would have to convert data twice, once in
  // AS400JDBCArray and once in SQLArray.

  private String typeName_; // typename is supplied by user at creation or from
                            // hostserver prepare/describe
  int typeCode_; // type from from java.sql.Types
  // protected int type_;
  private int vrm_;
  private AS400JDBCConnection con_;

  // This is just a reference to the SQLData type that this array contains. It
  // is not
  // a reflection from a prepare/describe, but is
  // just used for any potential temporary conversion needed before final
  // conversion
  // before setting in actual PreparedStatement going to hostserver
  // (ie Array.getResultSet() -> ars.getString(1))
  private SQLData contentTemplate_;

  // if data_[0] contains an SQLData, then contentTemplate_ will point to it!
  private boolean isSQLData_ = false; // true if data[0]_ == SQLData

  AS400JDBCArray() {
  } // restrict

  /**
   * Constructs an AS400JDBCArray object.
   * 
   * @param typeName
   *          The typeName.
   * @param data
   *          The data.
   * @param vrm
   *          The version.
   * @param con
   *          Connection.
   **/
  AS400JDBCArray(String typeName, Object[] data, int vrm,
      AS400JDBCConnection con) throws SQLException {
    /*
     * typeName is used to create a dummy SQLData object used for potential data
     * conversion when data[] is not of type SQLData[]. Pass in a connection if
     * the Object[] data is not SQLData. This will use connection's default
     * conversion data. Throws exception if typeName is not valid per
     * SQLDataFactory.
     */
    typeCode_ = JDUtilities.getTypeCode(typeName);
    typeName_ = JDUtilities.getTypeName(typeCode_);
    vrm_ = vrm;

    // check if array of SQLData
    // since values can be null, don't check if data_[0] is instance of SQLData
    if (data instanceof SQLData[]) {
      isSQLData_ = true;
      data_ = data;
    } else {
      // in this case, we will create our own temporary SQLData object used for
      // potential conversion in AS400JDBCArrayResultSet
      isSQLData_ = false;
      // Set the data using our own routines to get the type correct in the
      // object. This will set data_
      setArray(data);
    }

    con_ = con; // @arrayrs

    if (data_.length > 0 && isSQLData_ && data_[0] != null) // @nullelem
      contentTemplate_ = (SQLData) data_[0];
    else
      contentTemplate_ = SQLDataFactory.newData(typeName, 1, 1, 1, 37, null,
          vrm_, (con == null ? null : con.getProperties())); // @array
    // allow max for local conversion only since it is not associated with a
    // column on hostserver yet
  }

  /**
   * This method will free the internal memory that this object holds.
   **/
  synchronized public void free() throws SQLException {
    // na no locators can be in arrays.
  }

  /**
   * Retrieves the SQL type name of the elements in the array designated by this
   * <code>Array</code> object. If the elements are a built-in type, it returns
   * the database-specific type name of the elements. If the elements are a
   * user-defined type (UDT), this method returns the fully-qualified SQL type
   * name.
   * 
   * @return database-specific name for a built-in base type; or the
   *         fully-qualified SQL type name for a base type that is a UDT
   * @exception SQLException
   *              if an error occurs while attempting to access the type name
   * 
   */
  public String getBaseTypeName() throws SQLException {
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
   *              if an error occurs while attempting to access the base type
   * 
   */
  public int getBaseType() throws SQLException {

    return contentTemplate_.getType();

  }

  /**
   * Retrieves the contents of the SQL <code>ARRAY</code> value designated by
   * this <code>Array</code> object in the form of an array in the Java
   * programming language. This version of the method <code>getArray</code> uses
   * the type map associated with the connection for customizations of the type
   * mappings.
   * 
   * @return an array in the Java programming language that contains the ordered
   *         elements of the SQL <code>ARRAY</code> value designated by this
   *         <code>Array</code> object
   * @exception SQLException
   *              if an error occurs while attempting to access the array
   * 
   */
  synchronized public Object getArray() throws SQLException {
    // data could be sqlData if output from query or input Ojbects such as
    // Integer or Clob

    return getArrayX(1, data_.length);
  }

  /**
   * Retrieves a slice of the SQL <code>ARRAY</code> value designated by this
   * <code>Array</code> object, beginning with the specified <code>index</code>
   * and containing up to <code>count</code> successive elements of the SQL
   * array. This method uses the type map associated with the connection for
   * customizations of the type mappings.
   * 
   * @param index
   *          the array index of the first element to retrieve; the first
   *          element is at index 1
   * @param count
   *          the number of successive SQL array elements to retrieve
   * @return an array containing up to <code>count</code> consecutive elements
   *         of the SQL array, beginning with element <code>index</code>
   * @exception SQLException
   *              if an error occurs while attempting to access the array
   */
  synchronized public Object getArray(long index, int count)
      throws SQLException {

    if ((index <= 0) || (count < 0) || (index > data_.length)
        || ((index + count) > (data_.length + 1)))
      JDError.throwSQLException(JDError.EXC_BUFFER_LENGTH_INVALID);

    return getArrayX(index, count);
  }

  /**
   * Retrieves Array ResultSet that contains the elements of the SQL
   * <code>ARRAY</code> value designated by this <code>Array</code> object. The
   * result set contains one row for each array element, with two columns in
   * each row. The second column stores the element value; the first column
   * stores the index into the array for that element (with the first array
   * element being at index 1). The rows are in ascending order corresponding to
   * the order of the indices.
   * 
   * @return an Array ResultSet object containing one row for each of the
   *         elements in the array designated by this <code>Array</code> object,
   *         with the rows in ascending order based on the indices.
   * @exception SQLException
   *              if an error occurs while attempting to access the array
   */
  synchronized public ResultSet getResultSet() throws SQLException {

    AS400JDBCArrayResultSet rs = new AS400JDBCArrayResultSet(data_,
        contentTemplate_, isSQLData_, getBaseType(), vrm_, con_);
    if (JDTrace.isTraceOn())
      JDTrace.logInformation(this, "getResultSet");
    return rs;
  }

  /**
   * Retrieves a result set holding the elements of the subarray that starts at
   * index <code>index</code> and contains up to <code>count</code> successive
   * elements. This method uses the connection's type map to map the elements of
   * the array if the map contains an entry for the base type. Otherwise, the
   * standard mapping is used.
   * <P>
   * The result set has one row for each element of the SQL array designated by
   * this object, with the first row containing the element at index
   * <code>index</code>. The result set has up to <code>count</code> rows in
   * ascending order based on the indices. Each row has two columns: The second
   * column stores the element value; the first column stores the index into the
   * array for that element.
   * 
   * @param index
   *          the array index of the first element to retrieve; the first
   *          element is at index 1
   * @param count
   *          the number of successive SQL array elements to retrieve
   * @return a <code>ResultSet</code> object containing up to <code>count</code>
   *         consecutive elements of the SQL array designated by this
   *         <code>Array</code> object, starting at index <code>index</code>.
   * @exception SQLException
   *              if an error occurs while attempting to access the array
   * 
   */
  synchronized public ResultSet getResultSet(long index, int count)
      throws SQLException {

    int intIndex = (int) index - 1; // make 0 based
    Object[] retArry = new Object[count]; // !!!!here also

    if (data_.length > 0) {

      for (int x = 0; x < count; x++) {
        retArry[x] = data_[x + intIndex]; // just return values that were set by
                                          // user with no conversion
      }
    } else {
      retArry = data_; // length 0 or null value
    }

    AS400JDBCArrayResultSet rs = new AS400JDBCArrayResultSet(retArry,
        contentTemplate_, isSQLData_, getBaseType(), vrm_, con_);
    if (JDTrace.isTraceOn())
      JDTrace.logInformation(this, "getResultSet");
    return rs;
  }

  /**
   * Retrieves a result set that contains the elements of the SQL
   * <code>ARRAY</code> value designated by this <code>Array</code> object. This
   * method uses the specified <code>map</code> for type map customizations
   * unless the base type of the array does not match a user-defined type in
   * <code>map</code>, in which case it uses the standard mapping. This version
   * of the method <code>getResultSet</code> uses either the given type map or
   * the standard mapping; it never uses the type map associated with the
   * connection.
   * <p>
   * The result set contains one row for each array element, with two columns in
   * each row. The second column stores the element value; the first column
   * stores the index into the array for that element (with the first array
   * element being at index 1). The rows are in ascending order corresponding to
   * the order of the indices.
   * 
   * @param map
   *          contains the mapping of SQL user-defined types to classes in the
   *          Java programming language
   * @return a <code>ResultSet</code> object containing one row for each of the
   *         elements in the array designated by this <code>Array</code> object,
   *         with the rows in ascending order based on the indices.
   * @exception SQLException
   *              if an error occurs while attempting to access the array
   * 
   */
  synchronized public Object getArray(java.util.Map map) throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    return null;
  }

  /**
   * Retreives a slice of the SQL <code>ARRAY</code> value designated by this
   * <code>Array</code> object, beginning with the specified <code>index</code>
   * and containing up to <code>count</code> successive elements of the SQL
   * array.
   * <P>
   * This method uses the specified <code>map</code> for type map customizations
   * unless the base type of the array does not match a user-defined type in
   * <code>map</code>, in which case it uses the standard mapping. This version
   * of the method <code>getArray</code> uses either the given type map or the
   * standard mapping; it never uses the type map associated with the
   * connection.
   * 
   * @param index
   *          the array index of the first element to retrieve; the first
   *          element is at index 1
   * @param count
   *          the number of successive SQL array elements to retrieve
   * @param map
   *          a <code>java.util.Map</code> object that contains SQL type names
   *          and the classes in the Java programming language to which they are
   *          mapped
   * @return an array containing up to <code>count</code> consecutive elements
   *         of the SQL <code>ARRAY</code> value designated by this
   *         <code>Array</code> object, beginning with element
   *         <code>index</code>
   * @exception SQLException
   *              if an error occurs while attempting to access the array
   * 
   */
  synchronized public Object getArray(long index, int count, java.util.Map map)
      throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    return null;
  }

  /**
   * Retrieves a result set that contains the elements of the SQL
   * <code>ARRAY</code> value designated by this <code>Array</code> object. This
   * method uses the specified <code>map</code> for type map customizations
   * unless the base type of the array does not match a user-defined type in
   * <code>map</code>, in which case it uses the standard mapping. This version
   * of the method <code>getResultSet</code> uses either the given type map or
   * the standard mapping; it never uses the type map associated with the
   * connection.
   * <p>
   * The result set contains one row for each array element, with two columns in
   * each row. The second column stores the element value; the first column
   * stores the index into the array for that element (with the first array
   * element being at index 1). The rows are in ascending order corresponding to
   * the order of the indices.
   * 
   * @param map
   *          contains the mapping of SQL user-defined types to classes in the
   *          Java programming language
   * @return a <code>ResultSet</code> object containing one row for each of the
   *         elements in the array designated by this <code>Array</code> object,
   *         with the rows in ascending order based on the indices.
   * @exception SQLException
   *              if an error occurs while attempting to access the array
   * 
   */
  synchronized public ResultSet getResultSet(java.util.Map map)
      throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    return null;
  }

  /**
   * Retrieves a result set holding the elements of the subarray that starts at
   * index <code>index</code> and contains up to <code>count</code> successive
   * elements. This method uses the specified <code>map</code> for type map
   * customizations unless the base type of the array does not match a
   * user-defined type in <code>map</code>, in which case it uses the standard
   * mapping. This version of the method <code>getResultSet</code> uses either
   * the given type map or the standard mapping; it never uses the type map
   * associated with the connection.
   * <P>
   * The result set has one row for each element of the SQL array designated by
   * this object, with the first row containing the element at index
   * <code>index</code>. The result set has up to <code>count</code> rows in
   * ascending order based on the indices. Each row has two columns: The second
   * column stores the element value; the first column stroes the index into the
   * array for that element.
   * 
   * @param index
   *          the array index of the first element to retrieve; the first
   *          element is at index 1
   * @param count
   *          the number of successive SQL array elements to retrieve
   * @param map
   *          the <code>Map</code> object that contains the mapping of SQL type
   *          names to classes in the Java(tm) programming language
   * @return a <code>ResultSet</code> object containing up to <code>count</code>
   *         consecutive elements of the SQL array designated by this
   *         <code>Array</code> object, starting at index <code>index</code>.
   * @exception SQLException
   *              if an error occurs while attempting to access the array
   * 
   */
  synchronized public ResultSet getResultSet(long index, int count,
      java.util.Map map) throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    return null;
  }

  // --------------------jdbc internal methods-----------------

  /*
   * Get part or all of array. index is 1-based. If array content is output from
   * database, then convert data via getObject().
   */
  private Object getArrayX(long index, int count) throws SQLException {

    int intIndex = (int) index - 1; // make 0 based
    if ((data_.length >= 0) && isSQLData_) // @nullelem
    {

      // create array of same type as data_
      Class dummySQLXType = null; // @nullelem
      try {
        dummySQLXType = Class.forName(contentTemplate_.getJavaClassName()); // data_[0]).getObject();
                                                                            // //returns
                                                                            // column's
                                                                            // rs.getX()
                                                                            // type
                                                                            // (ie
                                                                            // Integer,
                                                                            // String,
                                                                            // etc)
                                                                            // //@nullelem
      } catch (Exception e) {
        try {
          dummySQLXType = Class.forName("java.lang.Object");
        } catch (Exception ee) {
          dummySQLXType = null;
        }
      }
      Object retArry = java.lang.reflect.Array
          .newInstance(dummySQLXType, count);
      for (int x = 0; x < count; x++) {
        if (data_[x + intIndex] != null)
          ((Object[]) retArry)[x] = ((SQLData) data_[x + intIndex]).getObject(); // convert
                                                                                 // based
                                                                                 // on
                                                                                 // SQLData
        else
          ((Object[]) retArry)[x] = null; // @nullelem
      }
      return retArry; // returns array of types such as Ingeter[] etc
    } else if ((data_.length > 0) && !isSQLData_) {
      Object retArry = java.lang.reflect.Array.newInstance(data_.getClass()
          .getComponentType(), count); // array of same type as user passed in
      for (int x = 0; x < count; x++) {
        ((Object[]) retArry)[x] = data_[x + intIndex]; // just return values
                                                       // that were set by user
                                                       // with no conversion
      }
      return retArry;
    } else {
      return data_; // length 0 or null value
    }
  }

  //
  // Set the array elements from a user supplied array.
  // 
  /*
   * Make sure the constructed value is valid and make a copy of the array
   * converting as needed. If this isn't copied, there could be some weird
   * behavior if the set array is changed before it is send to the database.
   */

  private void setArray(Object inArray) throws SQLException {
    /* The array cannot be null */
    if (inArray == null) {
      if (JDTrace.isTraceOn()) {
        JDTrace.logInformation(this, "DB2Array.validate array is null 07006");
      }
      JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    }

    /* ifndef JDBC40 */
    // If not JDBC 40, switch the type code
    switch (typeCode_) {
    case JDTypes.NCHAR:
      typeCode_ = Types.CHAR;
      break;
    case JDTypes.NVARCHAR:
      typeCode_ = Types.VARCHAR;
      break;
    case JDTypes.SQLXML:
    case JDTypes.NCLOB:
      typeCode_ = Types.CLOB;
      break;
    }
    /* endif */

    /* Make sure the type is valid and matches the type of the array */
    /* TODO: What if object is not array type */
    Class arrayComponentClass = inArray.getClass().getComponentType();
    String arrayType = arrayComponentClass.getName();
    if (JDTrace.isTraceOn()) {
      JDTrace.logInformation(this, "setArray typeCode is " + typeCode_
          + " arrayType is " + arrayType);
    }
    switch (typeCode_) {
    case Types.CHAR:
    case Types.VARCHAR:
    case JDTypes.NCHAR:
    case JDTypes.NVARCHAR:

      if ("java.lang.String".equals(arrayType)) {
        String[] inStringArray = (String[]) inArray;
        String[] stringArray = new String[inStringArray.length];
        for (int i = 0; i < inStringArray.length; i++) {
          stringArray[i] = inStringArray[i];
        }
        data_ = stringArray;
      } else if ("java.math.BigDecimal".equals(arrayType)
          || "java.lang.Boolean".equals(arrayType)
          || "java.lang.Byte".equals(arrayType)
          || "java.lang.Short".equals(arrayType)
          || "java.lang.Integer".equals(arrayType)
          || "java.lang.Long".equals(arrayType)
          || "java.lang.Float".equals(arrayType)
          || "java.lang.Double".equals(arrayType)
          || "java.sql.Date".equals(arrayType)
          || "java.sql.Time".equals(arrayType)
          || "java.sql.Timestamp".equals(arrayType)
          || JDUtilities.classIsInstanceOf(arrayComponentClass,
              "java.math.BigDecimal")
          || JDUtilities
              .classIsInstanceOf(arrayComponentClass, "java.sql.Date")
          || JDUtilities
              .classIsInstanceOf(arrayComponentClass, "java.sql.Time")
          || JDUtilities.classIsInstanceOf(arrayComponentClass,
              "java.sql.Timestamp")) {
        Object[] inObjectArray = (Object[]) inArray;
        String[] stringArray = new String[inObjectArray.length];
        for (int i = 0; i < inObjectArray.length; i++) {
          if (inObjectArray[i] == null) {
            stringArray[i] = null;
          } else {
            stringArray[i] = inObjectArray[i].toString();
          }
        }
        data_ = stringArray;
      } else if (JDUtilities.classIsInstanceOf(arrayComponentClass,
          "java.sql.Clob")) {

        Clob[] inObjectArray = (Clob[]) inArray;
        String[] stringArray = new String[inObjectArray.length];
        for (int i = 0; i < inObjectArray.length; i++) {
          if (inObjectArray[i] == null) {
            stringArray[i] = null;
          } else {
            int length = (int) inObjectArray[i].length();
            stringArray[i] = inObjectArray[i].getSubString(1, length);
          }
        }
        data_ = stringArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is CHAR/VARCHAR but array type is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }

      break;

    case Types.SMALLINT:
      if ("java.lang.Short".equals(arrayType)) {
        /*
         * smallints are Integers from jdbc 4.0 spec: Note: The JDBC 1.0
         * specification defined the Java object mapping for the SMALLINT and
         * TINYINT JDBC types to be Integer. The Java language did not include
         * the Byte and Short data types when the JDBC 1.0 specification was
         * finalized. The mapping of SMALLINT and TINYINT to Integer is
         * maintained to preserve backwards compatibility.
         */

        Short[] inShortArray = (Short[]) inArray;
        Integer[] shortArray = new Integer[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          if (inShortArray[i] == null) {
            shortArray[i] = null;
          } else { /* not null */
            shortArray[i] = new Integer(inShortArray[i].shortValue());
          } /* not null */
        } /* for i */
        data_ = shortArray;
      } else if ("short".equals(arrayType)) {
        short[] inShortArray = (short[]) inArray;
        Integer[] shortArray = new Integer[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          shortArray[i] = new Integer(inShortArray[i]);
        } /* for i */
        data_ = shortArray;
      } else if ("java.lang.Integer".equals(arrayType)) {
        Integer[] inIntegerArray = (Integer[]) inArray;
        Integer[] shortArray = new Integer[inIntegerArray.length];
        for (int i = 0; i < inIntegerArray.length; i++) {
          shortArray[i] = inIntegerArray[i];
        } /* for i */
        data_ = shortArray;
      } else if ("int".equals(arrayType)) {
        int[] inIntArray = (int[]) inArray;
        Integer[] shortArray = new Integer[inIntArray.length];
        for (int i = 0; i < inIntArray.length; i++) {
          shortArray[i] = new Integer(inIntArray[i]);
        } /* for i */
        data_ = shortArray;
      } else if ("java.lang.Long".equals(arrayType)) {
        Long[] inLongArray = (Long[]) inArray;
        Integer[] shortArray = new Integer[inLongArray.length];
        for (int i = 0; i < inLongArray.length; i++) {
          if (inLongArray[i] == null) {
            shortArray[i] = null;
          } else { /* not null */
            long lv = inLongArray[i].longValue();
            if (lv >= Short.MIN_VALUE && lv <= Short.MAX_VALUE) {
              shortArray[i] = new Integer((int) lv);
            } else { /* not in range */
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is SMALLINT but value at index "
                        + i + " is " + lv);
              } /* debug */
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
            } /* not in range */
          } /* not null */
        } /* for i */
        data_ = shortArray;
      } else if ("java.lang.Float".equals(arrayType)) {
        Float[] inFloatArray = (Float[]) inArray;
        Integer[] shortArray = new Integer[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          if (inFloatArray[i] == null) {
            shortArray[i] = null;
          } else { /* not null */
            float lv = inFloatArray[i].floatValue();
            if (lv >= Short.MIN_VALUE && lv <= Short.MAX_VALUE) {
              shortArray[i] = new Integer((int) lv);
            } else { /* not in range */
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is SMALLINT but value at index "
                        + i + " is " + lv);
              } /* JDTrace.isTraceOn() */
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            } /* not in range */
          } /* not null */
        } /* for i */
        data_ = shortArray;
      } else if ("java.lang.Double".equals(arrayType)) {
        Double[] inDoubleArray = (Double[]) inArray;
        Integer[] shortArray = new Integer[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          if (inDoubleArray[i] == null) {
            shortArray[i] = null;
          } else { /* not null */
            double lv = inDoubleArray[i].doubleValue();
            if (lv >= Short.MIN_VALUE && lv <= Short.MAX_VALUE) {
              shortArray[i] = new Integer((int) lv);
            } else { /* not in range */
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is SMALLINT but value at index "
                        + i + " is " + lv);
              } /* JDTrace.isTraceOn() */
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            } /* not in range */
          } /* not null */
        } /* for i */
        data_ = shortArray;
      } else if ("java.math.BigDecimal".equals(arrayType)) {
        BigDecimal[] inBigDecimalArray = (BigDecimal[]) inArray;
        Integer[] shortArray = new Integer[inBigDecimalArray.length];
        for (int i = 0; i < inBigDecimalArray.length; i++) {
          if (inBigDecimalArray[i] == null) {
            shortArray[i] = null;
          } else { /* not null */
            BigInteger bi = inBigDecimalArray[i].toBigInteger();
            long lv = bi.longValue();
            if (lv >= Short.MIN_VALUE && lv <= Short.MAX_VALUE) {
              shortArray[i] = new Integer((int) lv);
            } else { /* not in range */
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is SMALLINT but value at index "
                        + i + " is " + lv);
              } /* debug */
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            } /* not in range */
          } /* not null */
        } /* for i */
        data_ = shortArray;
      } else if ("java.lang.Boolean".equals(arrayType)) {
        Boolean[] inBooleanArray = (Boolean[]) inArray;
        Integer[] shortArray = new Integer[inBooleanArray.length];
        for (int i = 0; i < inBooleanArray.length; i++) {
          if (inBooleanArray[i] == null) {
            shortArray[i] = null;
          } else { /* not null */
            boolean b = inBooleanArray[i].booleanValue();
            if (b) {
              shortArray[i] = new Integer(1);
            } else { /* false */
              shortArray[i] = new Integer(0);
            } /* false */
          } /* not null */
        } /* for i */
        data_ = shortArray;
      } else if ("java.lang.Byte".equals(arrayType)) {
        Byte[] inByteArray = (Byte[]) inArray;
        Integer[] shortArray = new Integer[inByteArray.length];
        for (int i = 0; i < inByteArray.length; i++) {
          if (inByteArray[i] == null) {
            shortArray[i] = null;
          } else { /* not null */
            byte b = inByteArray[i].byteValue();
            shortArray[i] = new Integer(b);
          } /* not null */
        } /* for i */
        data_ = shortArray;
      } else if ("java.lang.String".equals(arrayType)) {
        String[] inStringArray = (String[]) inArray;
        Integer[] shortArray = new Integer[inStringArray.length];
        for (int i = 0; i < inStringArray.length; i++) {
          if (inStringArray[i] == null) {
            shortArray[i] = null;
          } else { /* not null */
            try {
              shortArray[i] = new Integer(Short.parseShort(inStringArray[i]));
            } catch (NumberFormatException nfe) {
              if (JDTrace.isTraceOn()) {
                JDTrace
                    .logInformation(
                        this,
                        "DB2Array.validate 07006 type is SMALLINT but NumberFormatException thrown for string "
                            + inStringArray[i]);
              } /* debug */
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            } /* catch */
          } /* not null */
        } /* for i */
        data_ = shortArray;
      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is SMALLINT but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    case Types.INTEGER:
      if ("java.lang.Integer".equals(arrayType)) {
        Integer[] inIntegerArray = (Integer[]) inArray;
        Integer[] integerArray = new Integer[inIntegerArray.length];
        for (int i = 0; i < inIntegerArray.length; i++) {
          integerArray[i] = inIntegerArray[i];
        }
        data_ = integerArray;

      } else if ("int".equals(arrayType)) {
        int[] inIntegerArray = (int[]) inArray;
        Integer[] integerArray = new Integer[inIntegerArray.length];
        for (int i = 0; i < inIntegerArray.length; i++) {
          integerArray[i] = new Integer(inIntegerArray[i]);
        }
        data_ = integerArray;
      } else if ("java.math.BigDecimal".equals(arrayType)) {
        BigDecimal[] inBigDecimalArray = (BigDecimal[]) inArray;
        Integer[] integerArray = new Integer[inBigDecimalArray.length];
        for (int i = 0; i < inBigDecimalArray.length; i++) {
          if (inBigDecimalArray[i] == null) {
            integerArray[i] = null;
          } else {
            BigInteger bi = inBigDecimalArray[i].toBigInteger();
            long lv = bi.longValue();
            if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) {
              integerArray[i] = new Integer((int) lv);
            } else {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is INTEGER but value at index "
                        + i + " is " + lv);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            }
          }
        }
        data_ = integerArray;
      } else if ("java.lang.Long".equals(arrayType)) {
        Long[] inLongArray = (Long[]) inArray;
        Integer[] integerArray = new Integer[inLongArray.length];
        for (int i = 0; i < inLongArray.length; i++) {
          if (inLongArray[i] == null) {
            integerArray[i] = null;
          } else {
            long lv = inLongArray[i].longValue();
            if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) {
              integerArray[i] = new Integer((int) lv);
            } else {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is SMALLINT but value at index "
                        + i + " is " + lv);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            }
          }
        }
        data_ = integerArray;
      } else if ("java.lang.Float".equals(arrayType)) {
        Float[] inFloatArray = (Float[]) inArray;
        Integer[] integerArray = new Integer[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          if (inFloatArray[i] == null) {
            integerArray[i] = null;
          } else {
            float lv = inFloatArray[i].floatValue();
            if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) {
              integerArray[i] = new Integer((int) lv);
            } else {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is SMALLINT but value at index "
                        + i + " is " + lv);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            }
          }
        }
        data_ = integerArray;

      } else if ("java.lang.Double".equals(arrayType)) {
        Double[] inDoubleArray = (Double[]) inArray;
        Integer[] integerArray = new Integer[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          if (inDoubleArray[i] == null) {
            integerArray[i] = null;
          } else {
            double lv = inDoubleArray[i].doubleValue();
            if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) {
              integerArray[i] = new Integer((int) lv);
            } else {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is SMALLINT but value at index "
                        + i + " is " + lv);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            }
          }
        }
        data_ = integerArray;

      } else if ("java.lang.Boolean".equals(arrayType)) {
        Boolean[] inBooleanArray = (Boolean[]) inArray;
        Integer[] integerArray = new Integer[inBooleanArray.length];
        for (int i = 0; i < inBooleanArray.length; i++) {
          if (inBooleanArray[i] == null) {
            integerArray[i] = null;
          } else {
            boolean b = inBooleanArray[i].booleanValue();
            if (b) {
              integerArray[i] = new Integer(1);
            } else {
              integerArray[i] = new Integer(0);
            }
          }
        }
        data_ = integerArray;

      } else if ("java.lang.Byte".equals(arrayType)) {
        Byte[] inByteArray = (Byte[]) inArray;
        Integer[] integerArray = new Integer[inByteArray.length];
        for (int i = 0; i < inByteArray.length; i++) {
          if (inByteArray[i] == null) {
            integerArray[i] = null;
          } else {
            byte b = inByteArray[i].byteValue();
            integerArray[i] = new Integer(b);
          }
        }
        data_ = integerArray;

      } else if ("java.lang.Short".equals(arrayType)) {
        Short[] inShortArray = (Short[]) inArray;
        Integer[] integerArray = new Integer[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          if (inShortArray[i] == null) {
            integerArray[i] = null;
          } else {
            short b = inShortArray[i].shortValue();
            integerArray[i] = new Integer(b);
          }
        }
        data_ = integerArray;

      } else if ("java.lang.String".equals(arrayType)) {
        String[] inStringArray = (String[]) inArray;
        Integer[] intArray = new Integer[inStringArray.length];
        for (int i = 0; i < inStringArray.length; i++) {
          if (inStringArray[i] == null) {
            intArray[i] = null;
          } else {
            try {
              intArray[i] = new Integer(Integer.parseInt(inStringArray[i]));
            } catch (NumberFormatException nfe) {

              if (JDTrace.isTraceOn()) {
                JDTrace
                    .logInformation(
                        this,
                        "DB2Array.validate 07006 type is INTEGER but NumberFormatException thrown for string "
                            + inStringArray[i]);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;

            }
          }
        }
        data_ = intArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is INTEGER but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    case Types.BIGINT:
      if ("java.lang.Long".equals(arrayType)) {
        Long[] inLongArray = (Long[]) inArray;
        Long[] longArray = new Long[inLongArray.length];
        for (int i = 0; i < inLongArray.length; i++) {
          longArray[i] = inLongArray[i];
        }
        data_ = longArray;

      } else if ("long".equals(arrayType)) {
        long[] inLongArray = (long[]) inArray;
        Long[] longArray = new Long[inLongArray.length];
        for (int i = 0; i < inLongArray.length; i++) {
          longArray[i] = new Long(inLongArray[i]);
        }
        data_ = longArray;

      } else if ("java.math.BigDecimal".equals(arrayType)) {
        BigDecimal[] inBigDecimalArray = (BigDecimal[]) inArray;
        Long[] longArray = new Long[inBigDecimalArray.length];
        for (int i = 0; i < inBigDecimalArray.length; i++) {
          if (inBigDecimalArray[i] == null) {
            longArray[i] = null;
          } else {
            BigInteger bi = inBigDecimalArray[i].toBigInteger();
            long lv = bi.longValue();
            if (lv >= Long.MIN_VALUE && lv <= Long.MAX_VALUE) {
              longArray[i] = new Long((int) lv);
            } else {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is Long but value at index "
                        + i + " is " + lv);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            }
          }
        }
        data_ = longArray;

      } else if ("java.lang.Boolean".equals(arrayType)) {
        Boolean[] inBooleanArray = (Boolean[]) inArray;
        Long[] longArray = new Long[inBooleanArray.length];
        for (int i = 0; i < inBooleanArray.length; i++) {
          if (inBooleanArray[i] == null) {
            longArray[i] = null;
          } else {
            boolean b = inBooleanArray[i].booleanValue();
            if (b) {
              longArray[i] = new Long(1);
            } else {
              longArray[i] = new Long(0);
            }
          }
        }
        data_ = longArray;

      } else if ("java.lang.Float".equals(arrayType)) {
        Float[] inFloatArray = (Float[]) inArray;
        Long[] longArray = new Long[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          if (inFloatArray[i] == null) {
            longArray[i] = null;
          } else {
            float lv = inFloatArray[i].floatValue();
            if (lv >= Long.MIN_VALUE && lv <= Long.MAX_VALUE) {
              longArray[i] = new Long((int) lv);
            } else {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is SMALLINT but value at index "
                        + i + " is " + lv);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            }
          }
        }
        data_ = longArray;

      } else if ("java.lang.Double".equals(arrayType)) {
        Double[] inDoubleArray = (Double[]) inArray;
        Long[] longArray = new Long[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          if (inDoubleArray[i] == null) {
            longArray[i] = null;
          } else {
            double lv = inDoubleArray[i].doubleValue();
            if (lv >= Long.MIN_VALUE && lv <= Long.MAX_VALUE) {
              longArray[i] = new Long((int) lv);
            } else {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is SMALLINT but value at index "
                        + i + " is " + lv);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            }
          }
        }
        data_ = longArray;

      } else if ("java.lang.Byte".equals(arrayType)) {
        Byte[] inByteArray = (Byte[]) inArray;
        Long[] longArray = new Long[inByteArray.length];
        for (int i = 0; i < inByteArray.length; i++) {
          if (inByteArray[i] == null) {
            longArray[i] = null;
          } else {
            byte b = inByteArray[i].byteValue();
            longArray[i] = new Long(b);
          }
        }
        data_ = longArray;

      } else if ("java.lang.Short".equals(arrayType)) {
        Short[] inShortArray = (Short[]) inArray;
        Long[] longArray = new Long[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          if (inShortArray[i] == null) {
            longArray[i] = null;
          } else {
            short b = inShortArray[i].shortValue();
            longArray[i] = new Long(b);
          }
        }
        data_ = longArray;

      } else if ("java.lang.Integer".equals(arrayType)) {
        Integer[] inIntegerArray = (Integer[]) inArray;
        Long[] longArray = new Long[inIntegerArray.length];
        for (int i = 0; i < inIntegerArray.length; i++) {
          if (inIntegerArray[i] == null) {
            longArray[i] = null;
          } else {
            int b = inIntegerArray[i].intValue();
            longArray[i] = new Long(b);
          }
        }
        data_ = longArray;

      } else if ("java.lang.String".equals(arrayType)) {
        String[] inStringArray = (String[]) inArray;
        Long[] longArray = new Long[inStringArray.length];
        for (int i = 0; i < inStringArray.length; i++) {
          if (inStringArray[i] == null) {
            longArray[i] = null;
          } else {
            try {
              longArray[i] = new Long(Long.parseLong(inStringArray[i]));
            } catch (NumberFormatException nfe) {

              if (JDTrace.isTraceOn()) {
                JDTrace
                    .logInformation(
                        this,
                        "DB2Array.validate 07006 type is INTEGER but NumberFormatException thrown for string "
                            + inStringArray[i]);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;

            }
          }
        }
        data_ = longArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is BIGINT but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return;
      }
      break;

    case Types.REAL:
      if ("java.lang.Float".equals(arrayType)) {
        Float[] inFloatArray = (Float[]) inArray;
        Float[] floatArray = new Float[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          floatArray[i] = inFloatArray[i];
        }
        data_ = floatArray;

      } else if ("float".equals(arrayType)) {
        float[] inFloatArray = (float[]) inArray;
        Float[] floatArray = new Float[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          floatArray[i] = new Float(inFloatArray[i]);
        }
        data_ = floatArray;

      } else if ("java.lang.Short".equals(arrayType)) {

        Short[] inShortArray = (Short[]) inArray;
        Float[] floatArray = new Float[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          if (inShortArray[i] == null) {
            floatArray[i] = null;
          } else {
            floatArray[i] = new Float(inShortArray[i].shortValue());
          }
        }
        data_ = floatArray;

      } else if ("short".equals(arrayType)) {
        short[] inShortArray = (short[]) inArray;
        Float[] floatArray = new Float[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          floatArray[i] = new Float(inShortArray[i]);
        } /* for i */
        data_ = floatArray;

      } else if ("java.lang.Integer".equals(arrayType)) {
        Integer[] inIntegerArray = (Integer[]) inArray;
        Float[] floatArray = new Float[inIntegerArray.length];
        for (int i = 0; i < inIntegerArray.length; i++) {
          if (inIntegerArray[i] == null) {
            floatArray[i] = null;
          } else { /* not null */
            floatArray[i] = new Float(inIntegerArray[i].floatValue());
          } /* not null */
        } /* for i */
        data_ = floatArray;

      } else if ("int".equals(arrayType)) {
        int[] inIntArray = (int[]) inArray;
        Float[] floatArray = new Float[inIntArray.length];
        for (int i = 0; i < inIntArray.length; i++) {
          floatArray[i] = new Float(inIntArray[i]);
        } /* for */
        data_ = floatArray;

      } else if ("java.lang.Long".equals(arrayType)) {
        Long[] inLongArray = (Long[]) inArray;
        Float[] floatArray = new Float[inLongArray.length];
        for (int i = 0; i < inLongArray.length; i++) {
          if (inLongArray[i] == null) {
            floatArray[i] = null;
          } else { /* not null */
            long lv = inLongArray[i].longValue();
            if (lv >= Float.MIN_VALUE && lv <= Float.MAX_VALUE) {
              floatArray[i] = new Float(lv);
            } else { /* in range */
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is REAL but value at index "
                        + i + " is " + lv);
              } /* debug */
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            } /* in range */
          } /* not null */
        } /* for */
        data_ = floatArray;

      } else if ("java.lang.Float".equals(arrayType)) {
        Float[] inFloatArray = (Float[]) inArray;
        Float[] floatArray = new Float[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          if (inFloatArray[i] == null) {
            floatArray[i] = null;
          } else { /* not null */
            float lv = inFloatArray[i].floatValue();
            if (lv >= Float.MIN_VALUE && lv <= Float.MAX_VALUE) {
              floatArray[i] = new Float(lv);
            } else { /* in range */
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is REAL but value at index "
                        + i + " is " + lv);
              } /* debug */
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            } /* in range */
          } /* not null */
        } /* for */
        data_ = floatArray;

      } else if ("java.lang.Double".equals(arrayType)) {
        Double[] inDoubleArray = (Double[]) inArray;
        Float[] floatArray = new Float[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          if (inDoubleArray[i] == null) {
            floatArray[i] = null;
          } else {
            double lv = inDoubleArray[i].doubleValue();
            if (lv >= Float.MIN_VALUE && lv <= Float.MAX_VALUE) {
              floatArray[i] = new Float(lv);
            } else {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is REAL but value at index "
                        + i + " is " + lv);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;
            }
          }
        }
        data_ = floatArray;

      } else if ("java.math.BigDecimal".equals(arrayType)) {
        BigDecimal[] inBigDecimalArray = (BigDecimal[]) inArray;
        Float[] floatArray = new Float[inBigDecimalArray.length];
        for (int i = 0; i < inBigDecimalArray.length; i++) {
          if (inBigDecimalArray[i] == null) {
            floatArray[i] = null;
          } else {
            float f = inBigDecimalArray[i].floatValue();
            if (f == Float.NEGATIVE_INFINITY || f == Float.POSITIVE_INFINITY) {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is REAL but value at index "
                        + i + " is " + f);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;

            } else {
              floatArray[i] = new Float(f);

            }
          }
        }
        data_ = floatArray;

      } else if ("java.lang.Boolean".equals(arrayType)) {
        Boolean[] inBooleanArray = (Boolean[]) inArray;
        Float[] floatArray = new Float[inBooleanArray.length];
        for (int i = 0; i < inBooleanArray.length; i++) {
          if (inBooleanArray[i] == null) {
            floatArray[i] = null;
          } else {
            boolean b = inBooleanArray[i].booleanValue();
            if (b) {
              floatArray[i] = new Float(1);
            } else {
              floatArray[i] = new Float(0);
            }
          }
        }
        data_ = floatArray;

      } else if ("java.lang.Byte".equals(arrayType)) {
        Byte[] inByteArray = (Byte[]) inArray;
        Float[] floatArray = new Float[inByteArray.length];
        for (int i = 0; i < inByteArray.length; i++) {
          if (inByteArray[i] == null) {
            floatArray[i] = null;
          } else {
            byte b = inByteArray[i].byteValue();
            floatArray[i] = new Float(b);
          }
        }
        data_ = floatArray;

      } else if ("java.lang.String".equals(arrayType)) {
        String[] inStringArray = (String[]) inArray;
        Float[] floatArray = new Float[inStringArray.length];
        for (int i = 0; i < inStringArray.length; i++) {
          if (inStringArray[i] == null) {
            floatArray[i] = null;
          } else {
            try {
              floatArray[i] = new Float(Float.parseFloat(inStringArray[i]));
            } catch (NumberFormatException nfe) {

              if (JDTrace.isTraceOn()) {
                JDTrace
                    .logInformation(
                        this,
                        "DB2Array.validate 07006 type is REAL but NumberFormatException thrown for string "
                            + inStringArray[i]);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;

            }
          }
        }
        data_ = floatArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is REAL but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    case Types.FLOAT:

      if ("java.lang.Double".equals(arrayType)) {
        Double[] inDoubleArray = (Double[]) inArray;
        Double[] doubleArray = new Double[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          doubleArray[i] = inDoubleArray[i];
        }
        data_ = doubleArray;

      } else if ("double".equals(arrayType)) {
        double[] inDoubleArray = (double[]) inArray;
        Double[] doubleArray = new Double[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          doubleArray[i] = new Double(inDoubleArray[i]);
        }
        data_ = doubleArray;

      } else if ("java.lang.Float".equals(arrayType)) {
        /* Types.FLOAT should always map to Double */
        Float[] inFloatArray = (Float[]) inArray;
        Double[] floatArray = new Double[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          if (inFloatArray[i] == null) {
            floatArray[i] = null;
          } else {
            floatArray[i] = new Double(inFloatArray[i].doubleValue());
          }
        }
        data_ = floatArray;

      } else if ("float".equals(arrayType)) {
        float[] inFloatArray = (float[]) inArray;
        Double[] floatArray = new Double[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          floatArray[i] = new Double(inFloatArray[i]);
        }
        data_ = floatArray;

      } else if ("java.lang.Short".equals(arrayType)) {

        Short[] inShortArray = (Short[]) inArray;
        Double[] doubleArray = new Double[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          if (inShortArray[i] == null) {
            doubleArray[i] = null;
          } else {
            doubleArray[i] = new Double(inShortArray[i].shortValue());
          }
        }
        data_ = doubleArray;

      } else if ("short".equals(arrayType)) {
        short[] inShortArray = (short[]) inArray;
        Double[] doubleArray = new Double[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          doubleArray[i] = new Double(inShortArray[i]);
        }
        data_ = doubleArray;

      } else if ("java.lang.Integer".equals(arrayType)) {
        Integer[] inIntegerArray = (Integer[]) inArray;
        Double[] doubleArray = new Double[inIntegerArray.length];
        for (int i = 0; i < inIntegerArray.length; i++) {
          if (inIntegerArray[i] == null) {
            doubleArray[i] = null;
          } else {
            doubleArray[i] = new Double(inIntegerArray[i].doubleValue());
          }
        }
        data_ = doubleArray;

      } else if ("int".equals(arrayType)) {
        int[] inIntArray = (int[]) inArray;
        Double[] doubleArray = new Double[inIntArray.length];
        for (int i = 0; i < inIntArray.length; i++) {
          doubleArray[i] = new Double(inIntArray[i]);
        }
        data_ = doubleArray;

      } else if ("java.lang.Long".equals(arrayType)) {
        Long[] inLongArray = (Long[]) inArray;
        Double[] doubleArray = new Double[inLongArray.length];
        for (int i = 0; i < inLongArray.length; i++) {
          if (inLongArray[i] == null) {
            doubleArray[i] = null;
          } else {
            long lv = inLongArray[i].longValue();
            doubleArray[i] = new Double(lv);
          }
        }
        data_ = doubleArray;

      } else if ("java.math.BigDecimal".equals(arrayType)) {
        BigDecimal[] inBigDecimalArray = (BigDecimal[]) inArray;
        Double[] doubleArray = new Double[inBigDecimalArray.length];
        for (int i = 0; i < inBigDecimalArray.length; i++) {
          if (inBigDecimalArray[i] == null) {
            doubleArray[i] = null;
          } else {
            double f = inBigDecimalArray[i].doubleValue();
            if (f == Double.NEGATIVE_INFINITY || f == Double.POSITIVE_INFINITY) {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is Double but value at index "
                        + i + " is " + f);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;

            } else {
              doubleArray[i] = new Double(f);

            }
          }
        }
        data_ = doubleArray;

      } else if ("java.lang.Boolean".equals(arrayType)) {
        Boolean[] inBooleanArray = (Boolean[]) inArray;
        Double[] doubleArray = new Double[inBooleanArray.length];
        for (int i = 0; i < inBooleanArray.length; i++) {
          if (inBooleanArray[i] == null) {
            doubleArray[i] = null;
          } else {
            boolean b = inBooleanArray[i].booleanValue();
            if (b) {
              doubleArray[i] = new Double(1);
            } else {
              doubleArray[i] = new Double(0);
            }
          }
        }
        data_ = doubleArray;

      } else if ("java.lang.Byte".equals(arrayType)) {
        Byte[] inByteArray = (Byte[]) inArray;
        Double[] doubleArray = new Double[inByteArray.length];
        for (int i = 0; i < inByteArray.length; i++) {
          if (inByteArray[i] == null) {
            doubleArray[i] = null;
          } else {
            byte b = inByteArray[i].byteValue();
            doubleArray[i] = new Double(b);
          }
        }
        data_ = doubleArray;

      } else if ("java.lang.String".equals(arrayType)) {
        String[] inStringArray = (String[]) inArray;
        Double[] doubleArray = new Double[inStringArray.length];
        for (int i = 0; i < inStringArray.length; i++) {
          if (inStringArray[i] == null) {
            doubleArray[i] = null;
          } else {
            try {
              doubleArray[i] = new Double(Double.parseDouble(inStringArray[i]));
            } catch (NumberFormatException nfe) {

              if (JDTrace.isTraceOn()) {
                JDTrace
                    .logInformation(
                        this,
                        "DB2Array.validate 07006 type is Double but NumberFormatException thrown for string "
                            + inStringArray[i]);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;

            }
          }
        }
        data_ = doubleArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is FLOAT but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }

      break;

    case Types.DOUBLE:

      if ("java.lang.Double".equals(arrayType)) {
        Double[] inDoubleArray = (Double[]) inArray;
        Double[] doubleArray = new Double[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          doubleArray[i] = inDoubleArray[i];
        }
        data_ = doubleArray;

      } else if ("double".equals(arrayType)) {
        double[] inDoubleArray = (double[]) inArray;
        Double[] doubleArray = new Double[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          doubleArray[i] = new Double(inDoubleArray[i]);
        }
        data_ = doubleArray;

      } else if ("java.lang.Float".equals(arrayType)) {
        /* Types.FLOAT should always map to Double */
        Float[] inFloatArray = (Float[]) inArray;
        Double[] floatArray = new Double[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          if (inFloatArray[i] == null) {
            floatArray[i] = null;
          } else {
            floatArray[i] = new Double(inFloatArray[i].doubleValue());
          }
        }
        data_ = floatArray;

      } else if ("float".equals(arrayType)) {
        float[] inFloatArray = (float[]) inArray;
        Double[] floatArray = new Double[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          floatArray[i] = new Double(inFloatArray[i]);
        }
        data_ = floatArray;

      } else if ("java.lang.Short".equals(arrayType)) {

        Short[] inShortArray = (Short[]) inArray;
        Double[] doubleArray = new Double[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          if (inShortArray[i] == null) {
            doubleArray[i] = null;
          } else {
            doubleArray[i] = new Double(inShortArray[i].shortValue());
          }
        }
        data_ = doubleArray;

      } else if ("short".equals(arrayType)) {
        short[] inShortArray = (short[]) inArray;
        Double[] doubleArray = new Double[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          doubleArray[i] = new Double(inShortArray[i]);
        }
        data_ = doubleArray;

      } else if ("java.lang.Integer".equals(arrayType)) {
        Integer[] inIntegerArray = (Integer[]) inArray;
        Double[] doubleArray = new Double[inIntegerArray.length];
        for (int i = 0; i < inIntegerArray.length; i++) {
          if (inIntegerArray[i] == null) {
            doubleArray[i] = null;
          } else {
            doubleArray[i] = new Double(inIntegerArray[i].doubleValue());
          }
        }
        data_ = doubleArray;

      } else if ("int".equals(arrayType)) {
        int[] inIntArray = (int[]) inArray;
        Double[] doubleArray = new Double[inIntArray.length];
        for (int i = 0; i < inIntArray.length; i++) {
          doubleArray[i] = new Double(inIntArray[i]);
        }
        data_ = doubleArray;

      } else if ("java.lang.Long".equals(arrayType)) {
        Long[] inLongArray = (Long[]) inArray;
        Double[] doubleArray = new Double[inLongArray.length];
        for (int i = 0; i < inLongArray.length; i++) {
          if (inLongArray[i] == null) {
            doubleArray[i] = null;
          } else {
            long lv = inLongArray[i].longValue();
            doubleArray[i] = new Double(lv);
          }
        }
        data_ = doubleArray;

      } else if ("java.math.BigDecimal".equals(arrayType)) {
        BigDecimal[] inBigDecimalArray = (BigDecimal[]) inArray;
        Double[] doubleArray = new Double[inBigDecimalArray.length];
        for (int i = 0; i < inBigDecimalArray.length; i++) {
          if (inBigDecimalArray[i] == null) {
            doubleArray[i] = null;
          } else {
            double f = inBigDecimalArray[i].doubleValue();
            if (f == Double.NEGATIVE_INFINITY || f == Double.POSITIVE_INFINITY) {
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this,
                    "DB2Array.setArray 07006 type is Double but value at index "
                        + i + " is " + f);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;

            } else {
              doubleArray[i] = new Double(f);

            }
          }
        }
        data_ = doubleArray;

      } else if ("java.lang.Boolean".equals(arrayType)) {
        Boolean[] inBooleanArray = (Boolean[]) inArray;
        Double[] doubleArray = new Double[inBooleanArray.length];
        for (int i = 0; i < inBooleanArray.length; i++) {
          if (inBooleanArray[i] == null) {
            doubleArray[i] = null;
          } else {
            boolean b = inBooleanArray[i].booleanValue();
            if (b) {
              doubleArray[i] = new Double(1);
            } else {
              doubleArray[i] = new Double(0);
            }
          }
        }
        data_ = doubleArray;

      } else if ("java.lang.Byte".equals(arrayType)) {
        Byte[] inByteArray = (Byte[]) inArray;
        Double[] doubleArray = new Double[inByteArray.length];
        for (int i = 0; i < inByteArray.length; i++) {
          if (inByteArray[i] == null) {
            doubleArray[i] = null;
          } else {
            byte b = inByteArray[i].byteValue();
            doubleArray[i] = new Double(b);
          }
        }
        data_ = doubleArray;

      } else if ("java.lang.String".equals(arrayType)) {
        String[] inStringArray = (String[]) inArray;
        Double[] doubleArray = new Double[inStringArray.length];
        for (int i = 0; i < inStringArray.length; i++) {
          if (inStringArray[i] == null) {
            doubleArray[i] = null;
          } else {
            try {
              doubleArray[i] = new Double(Double.parseDouble(inStringArray[i]));
            } catch (NumberFormatException nfe) {

              if (JDTrace.isTraceOn()) {
                JDTrace
                    .logInformation(
                        this,
                        "DB2Array.validate 07006 type is Double but NumberFormatException thrown for string "
                            + inStringArray[i]);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;

            }
          }
        }
        data_ = doubleArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is DOUBLE/FLOAT but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    case Types.DECIMAL:
    case Types.NUMERIC:
    case JDTypes.SQL_DECFLOAT:
      if (JDUtilities.classIsInstanceOf(arrayComponentClass,
          "java.math.BigDecimal")) {
        BigDecimal[] inBdArray = (BigDecimal[]) inArray;
        BigDecimal[] bdArray = new BigDecimal[inBdArray.length];
        for (int i = 0; i < inBdArray.length; i++) {
          bdArray[i] = inBdArray[i];
        }
        data_ = bdArray;

      } else if ("java.lang.Double".equals(arrayType)) {
        Double[] inDoubleArray = (Double[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          if (inDoubleArray[i] == null) {
            bigDecimalArray[i] = null;
          } else {
            bigDecimalArray[i] = new BigDecimal(inDoubleArray[i].doubleValue());
          }
        }
        data_ = bigDecimalArray;

      } else if ("double".equals(arrayType)) {
        double[] inDoubleArray = (double[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inDoubleArray.length];
        for (int i = 0; i < inDoubleArray.length; i++) {
          bigDecimalArray[i] = new BigDecimal(inDoubleArray[i]);
        }
        data_ = bigDecimalArray;

      } else if ("java.lang.Float".equals(arrayType)) {
        /* Types.FLOAT should always map to Double */
        Float[] inFloatArray = (Float[]) inArray;
        BigDecimal[] floatArray = new BigDecimal[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          if (inFloatArray[i] == null) {
            floatArray[i] = null;
          } else {
            floatArray[i] = new BigDecimal(inFloatArray[i].doubleValue());
          }
        }
        data_ = floatArray;

      } else if ("float".equals(arrayType)) {
        float[] inFloatArray = (float[]) inArray;
        BigDecimal[] floatArray = new BigDecimal[inFloatArray.length];
        for (int i = 0; i < inFloatArray.length; i++) {
          floatArray[i] = new BigDecimal(inFloatArray[i]);
        }
        data_ = floatArray;

      } else if ("java.lang.Short".equals(arrayType)) {

        Short[] inShortArray = (Short[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          if (inShortArray[i] == null) {
            bigDecimalArray[i] = null;
          } else {
            bigDecimalArray[i] = new BigDecimal(inShortArray[i].doubleValue());
          }
        }
        data_ = bigDecimalArray;

      } else if ("short".equals(arrayType)) {
        short[] inShortArray = (short[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inShortArray.length];
        for (int i = 0; i < inShortArray.length; i++) {
          bigDecimalArray[i] = new BigDecimal((double) inShortArray[i]);
        }
        data_ = bigDecimalArray;

      } else if ("java.lang.Integer".equals(arrayType)) {
        Integer[] inIntegerArray = (Integer[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inIntegerArray.length];
        for (int i = 0; i < inIntegerArray.length; i++) {
          if (inIntegerArray[i] == null) {
            bigDecimalArray[i] = null;
          } else {
            bigDecimalArray[i] = new BigDecimal(inIntegerArray[i].doubleValue());
          }
        }
        data_ = bigDecimalArray;

      } else if ("int".equals(arrayType)) {
        int[] inIntArray = (int[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inIntArray.length];
        for (int i = 0; i < inIntArray.length; i++) {
          bigDecimalArray[i] = new BigDecimal((double) inIntArray[i]);
        }
        data_ = bigDecimalArray;

      } else if ("java.lang.Long".equals(arrayType)) {
        Long[] inLongArray = (Long[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inLongArray.length];
        for (int i = 0; i < inLongArray.length; i++) {
          if (inLongArray[i] == null) {
            bigDecimalArray[i] = null;
          } else {
            long lv = inLongArray[i].longValue();
            bigDecimalArray[i] = new BigDecimal((double) lv);
          }
        }
        data_ = bigDecimalArray;

      } else if ("java.lang.Boolean".equals(arrayType)) {
        Boolean[] inBooleanArray = (Boolean[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inBooleanArray.length];
        for (int i = 0; i < inBooleanArray.length; i++) {
          if (inBooleanArray[i] == null) {
            bigDecimalArray[i] = null;
          } else {
            boolean b = inBooleanArray[i].booleanValue();
            if (b) {
              bigDecimalArray[i] = new BigDecimal(1.0);
            } else {
              bigDecimalArray[i] = new BigDecimal(0.0);
            }
          }
        }
        data_ = bigDecimalArray;

      } else if ("java.lang.Byte".equals(arrayType)) {
        Byte[] inByteArray = (Byte[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inByteArray.length];
        for (int i = 0; i < inByteArray.length; i++) {
          if (inByteArray[i] == null) {
            bigDecimalArray[i] = null;
          } else {
            byte b = inByteArray[i].byteValue();
            bigDecimalArray[i] = new BigDecimal((double) b);
          }
        }
        data_ = bigDecimalArray;

      } else if ("java.lang.String".equals(arrayType)) {
        String[] inStringArray = (String[]) inArray;
        BigDecimal[] bigDecimalArray = new BigDecimal[inStringArray.length];
        for (int i = 0; i < inStringArray.length; i++) {
          if (inStringArray[i] == null) {
            bigDecimalArray[i] = null;
          } else {
            try {
              bigDecimalArray[i] = new BigDecimal(inStringArray[i]);
            } catch (NumberFormatException nfe) {

              if (JDTrace.isTraceOn()) {
                JDTrace
                    .logInformation(
                        this,
                        "DB2Array.validate 07006 type is Double but NumberFormatException thrown for string "
                            + inStringArray[i]);
              }
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
              return;

            }
          }
        }
        data_ = bigDecimalArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is DECIMAL/NUMERIC/DECFLOAT but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return;
      }
      break;

    case Types.BINARY:
    case Types.VARBINARY:
      if ("[B".equals(arrayType)) {
        byte[][] inByteArray = (byte[][]) inArray;
        byte[][] byteArray = new byte[inByteArray.length][];
        for (int i = 0; i < inByteArray.length; i++) {
          byteArray[i] = inByteArray[i];
        }
        data_ = byteArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is BINARY/VARBINARY but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    case Types.BLOB:
      if (JDUtilities.classIsInstanceOf(arrayComponentClass, "java.sql.Blob")) {

        Blob[] inBlobArray = (Blob[]) inArray;
        Blob[] blobArray = new Blob[inBlobArray.length];
        for (int i = 0; i < inBlobArray.length; i++) {
          blobArray[i] = inBlobArray[i];
        }
        data_ = blobArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is BLOB but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    case Types.CLOB:
    case 2011: /* Types.NCLOB */

      if (JDUtilities.classIsInstanceOf(arrayComponentClass, "java.sql.Clob")) {

        Clob[] inClobArray = (Clob[]) inArray;
        Clob[] clobArray = new Clob[inClobArray.length];
        for (int i = 0; i < inClobArray.length; i++) {
          clobArray[i] = inClobArray[i];
        }
        data_ = clobArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is CLOB but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    case Types.DATE:
      if (JDUtilities.classIsInstanceOf(arrayComponentClass, "java.sql.Date")) {
        Date[] inDateArray = (Date[]) inArray;
        Date[] dateArray = new Date[inDateArray.length];
        for (int i = 0; i < inDateArray.length; i++) {
          dateArray[i] = inDateArray[i];
        }
        data_ = dateArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is DATE but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    /*
     * ifdef JDBC40
     * 
     * case 2009: // Types.SQLXML { boolean allValid = true; String subArrayType
     * = "";
     * 
     * Object[] inSQLXMLArray = (Object[]) inArray; SQLXML[] SQLXMLArray = new
     * SQLXML[inSQLXMLArray.length]; for (int i = 0; allValid && i <
     * inSQLXMLArray.length; i++) { if ( inSQLXMLArray[i] == null) {
     * SQLXMLArray[i] = null; } else { if ( inSQLXMLArray[i] instanceof SQLXML )
     * { SQLXMLArray[i] = (SQLXML) inSQLXMLArray[i]; } else { subArrayType =
     * inSQLXMLArray[i].getClass().getName(); allValid = false; } } }
     * 
     * if (allValid) { data_ =SQLXMLArray;
     * 
     * } else { if (JDTrace.isTraceOn()) { JDTrace.logInformation(this,
     * "DB2Array.validate 07006 type is SQLXML but array types is "
     * +arrayType+" subArrayType = "+subArrayType); }
     * JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH); } } break;
     * 
     * endif
     */
    case Types.TIME:
      if (JDUtilities.classIsInstanceOf(arrayComponentClass, "java.sql.Time")) {
        Time[] inTimeArray = (Time[]) inArray;
        Time[] timeArray = new Time[inTimeArray.length];
        for (int i = 0; i < inTimeArray.length; i++) {
          timeArray[i] = inTimeArray[i];
        }
        data_ = timeArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is TIME but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    case Types.TIMESTAMP:
      if (JDUtilities.classIsInstanceOf(arrayComponentClass,
          "java.sql.Timestamp")) {
        Timestamp[] inTimestampArray = (Timestamp[]) inArray;
        Timestamp[] timestampArray = new Timestamp[inTimestampArray.length];
        for (int i = 0; i < inTimestampArray.length; i++) {
          timestampArray[i] = inTimestampArray[i];
        }
        data_ = timestampArray;

      } else {
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this,
              "DB2Array.validate 07006 type is TIMESTAMP but array types is "
                  + arrayType);
        }
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
      break;

    /* Array of array not supported */

    /* case Types.ROWID : */
    case Types.NULL:
    case Types.DISTINCT:
    case Types.STRUCT:
    case Types.JAVA_OBJECT:
      /* case Types.LONGNVARCHAR : */
    case Types.LONGVARBINARY:
    case Types.LONGVARCHAR:
    case Types.DATALINK:
    case Types.BOOLEAN:
    case Types.OTHER:
    case Types.REF:
    case Types.TINYINT:
    case Types.BIT:
    case Types.ARRAY:
    default:
      if (JDTrace.isTraceOn()) {
        JDTrace.logInformation(this, "DB2Array.validate 07006 invalid type "
            + typeCode_ + " array type is " + arrayType);
      }
      JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);

    }

  }

}
