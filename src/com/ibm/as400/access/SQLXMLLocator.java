///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLXMLLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2009-2009 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
//@xml3 new class
//In JDBC 3.0, this is just a DUMMY STUB class, so we do not have to duplicate SQLDataFactory.java in JDBC 4.0
import java.util.Calendar;

public class SQLXMLLocator  implements SQLLocator {

	 SQLXMLLocator(AS400JDBCConnection connection,
             int id,
             int maxLength, 
             SQLConversionSettings settings,
             ConvTable converter,
             int columnIndex,
             int xmlType) 
     {
	    //dummy
     }
	 
	 public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}
	 public void convertFromRawBytes(byte[] rawBytes, int offset,
			ConvTable converter) throws SQLException {
		// TODO Auto-generated method stub
		
	}
	 public void convertToRawBytes(byte[] rawBytes, int offset,
			ConvTable ccsidConverter) throws SQLException {
		// TODO Auto-generated method stub
		
	}
	 public int getActualSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	 public Array getArray() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	 public InputStream getAsciiStream() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	 public BigDecimal getBigDecimal(int scale) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	} 
	 public InputStream getBinaryStream() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public Blob getBlob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean getBoolean() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
	public byte getByte() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	public byte[] getBytes() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public Reader getCharacterStream() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public Clob getClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public String getCreateParameters() {
		// TODO Auto-generated method stub
		return null;
	}
	public Date getDate(Calendar calendar) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public int getDisplaySize() {
		// TODO Auto-generated method stub
		return 0;
	}
	public double getDouble() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	public float getFloat() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getHandle() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getInt() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	public String getJavaClassName() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getLiteralPrefix() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getLiteralSuffix() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}
	public long getLong() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getMaximumPrecision() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getMaximumScale() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getMinimumScale() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getNativeType() {
		// TODO Auto-generated method stub
		return 0;
	}
	public Object getObject() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public int getPrecision() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getRadix() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getScale() {
		// TODO Auto-generated method stub
		return 0;
	}
	public short getShort() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getSQLType() {
		// TODO Auto-generated method stub
		return 0;
	}
	public String getString() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public Time getTime(Calendar calendar) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public Timestamp getTimestamp(Calendar calendar) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public int getTruncated() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	public String getTypeName() {
		// TODO Auto-generated method stub
		return null;
	}
	public InputStream getUnicodeStream() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean isSigned() {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean isText() {
		// TODO Auto-generated method stub
		return false;
	}
	public void set(Object object, Calendar calendar, int scale)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}
	public void setHandle(int handle) {
		// TODO Auto-generated method stub
		
	}
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
}
