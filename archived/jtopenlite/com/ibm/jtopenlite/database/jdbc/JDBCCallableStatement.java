///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCCallableStatement.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;


public class JDBCCallableStatement extends JDBCPreparedStatement implements
		CallableStatement {

    public JDBCCallableStatement(JDBCConnection conn, String sql, Calendar calendar, String statementName, String cursorName, int rpbID) throws SQLException {
    	super(conn, sql, calendar, statementName, cursorName, rpbID);
    }

	public Array getArray(int i) throws SQLException {
	    throw new NotImplementedException();
	}

	public Array getArray(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public BigDecimal getBigDecimal(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
    throw new NotImplementedException();
	}

	public Blob getBlob(int i) throws SQLException {
	    throw new NotImplementedException();
	}

	public Blob getBlob(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public boolean getBoolean(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public boolean getBoolean(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public byte getByte(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public byte getByte(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public byte[] getBytes(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public byte[] getBytes(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public Clob getClob(int i) throws SQLException {
	    throw new NotImplementedException();
	}

	public Clob getClob(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public Date getDate(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public Date getDate(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
	    throw new NotImplementedException();
	}

	public Date getDate(String parameterName, Calendar cal) throws SQLException {
	    throw new NotImplementedException();
	}

	public double getDouble(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public double getDouble(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public float getFloat(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public float getFloat(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public int getInt(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public int getInt(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public long getLong(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public long getLong(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public Object getObject(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public Object getObject(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public Object getObject(int i, Map<String, Class<?>> map)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public Object getObject(String parameterName, Map<String, Class<?>> map)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public Ref getRef(int i) throws SQLException {
	    throw new NotImplementedException();
	}

	public Ref getRef(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public short getShort(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public short getShort(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public String getString(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public String getString(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public Time getTime(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public Time getTime(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
	    throw new NotImplementedException();
	}

	public Time getTime(String parameterName, Calendar cal) throws SQLException {
	    throw new NotImplementedException();
	}

	public Timestamp getTimestamp(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public Timestamp getTimestamp(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public Timestamp getTimestamp(int parameterIndex, Calendar cal)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public Timestamp getTimestamp(String parameterName, Calendar cal)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public URL getURL(int parameterIndex) throws SQLException {
	    throw new NotImplementedException();
	}

	public URL getURL(String parameterName) throws SQLException {
	    throw new NotImplementedException();
	}

	public void registerOutParameter(int parameterIndex, int sqlType)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public void registerOutParameter(String parameterName, int sqlType)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public void registerOutParameter(int parameterIndex, int sqlType, int scale)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public void registerOutParameter(int paramIndex, int sqlType,
			String typeName) throws SQLException {
	    throw new NotImplementedException();
	}

	public void registerOutParameter(String parameterName, int sqlType,
			int scale) throws SQLException {
	    throw new NotImplementedException();
	}

	public void registerOutParameter(String parameterName, int sqlType,
			String typeName) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setAsciiStream(String parameterName, InputStream x, int length)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public void setBigDecimal(String parameterName, BigDecimal x)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public void setBinaryStream(String parameterName, InputStream x, int length)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public void setBoolean(String parameterName, boolean x) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setByte(String parameterName, byte x) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setBytes(String parameterName, byte[] x) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setCharacterStream(String parameterName, Reader reader,
			int length) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setDate(String parameterName, Date x) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setDate(String parameterName, Date x, Calendar cal)
			throws SQLException {
	    throw new NotImplementedException();
	}

	public void setDouble(String parameterName, double x) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setFloat(String parameterName, float x) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setInt(String parameterName, int x) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setLong(String parameterName, long x) throws SQLException {
	    throw new NotImplementedException();
	}

	public void setNull(String parameterName, int sqlType) throws SQLException {
		throw new NotImplementedException();

	}

	public void setNull(String parameterName, int sqlType, String typeName)
			throws SQLException {
		throw new NotImplementedException();

	}

	public void setObject(String parameterName, Object x) throws SQLException {
		throw new NotImplementedException();

	}

	public void setObject(String parameterName, Object x, int targetSqlType)
			throws SQLException {
		throw new NotImplementedException();

	}

	public void setObject(String parameterName, Object x, int targetSqlType,
			int scale) throws SQLException {
		throw new NotImplementedException();

	}

	public void setShort(String parameterName, short x) throws SQLException {
		throw new NotImplementedException();

	}

	public void setString(String parameterName, String x) throws SQLException {
		throw new NotImplementedException();

	}

	public void setTime(String parameterName, Time x) throws SQLException {
		throw new NotImplementedException();

	}

	public void setTime(String parameterName, Time x, Calendar cal)
			throws SQLException {
		throw new NotImplementedException();

	}

	public void setTimestamp(String parameterName, Timestamp x)
			throws SQLException {
		throw new NotImplementedException();

	}

	public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
			throws SQLException {
		throw new NotImplementedException();

	}

	public void setURL(String parameterName, URL val) throws SQLException {
		throw new NotImplementedException();

	}

	public boolean wasNull() throws SQLException {
		throw new NotImplementedException();
	}


  /**
   * @param parm
   */
  public Reader getCharacterStream(int parm) throws SQLException {
    throw new NotImplementedException();
  }


  /**
   * @param parm
   */
  public Reader getCharacterStream(String parm) throws SQLException {
    throw new NotImplementedException();
  }


  /**
   * @param parm
   */
  public Reader getNCharacterStream(int parm) throws SQLException {
    return getCharacterStream(parm);
  }


  /**
   * @param parm
   */
  public Reader getNCharacterStream(String parm) throws SQLException {
    throw new NotImplementedException();
  }




  /**
   * @param parm
   */
  public String getNString(int parm) throws SQLException {
    return getString(parm);
  }


  /**
   * @param parm
   */
  public String getNString(String parm) throws SQLException {
    return getString(parm);
  }







  /**
   * @param parm
   * @param arg1
   */
  public void setAsciiStream(String parm, InputStream arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   * @param arg2
   */
  public void setAsciiStream(String parm, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   */
  public void setBinaryStream(String parm, InputStream arg1)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   * @param arg2
   */
  public void setBinaryStream(String parm, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   */
  public void setBlob(String parm, Blob arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   */
  public void setBlob(String parm, InputStream arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   * @param arg2
   */
  public void setBlob(String parm, InputStream arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   */
  public void setCharacterStream(String parm, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   * @param arg2
   */
  public void setCharacterStream(String parm, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   */
  public void setClob(String parm, Clob arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   */
  public void setClob(String parm, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   * @param arg2
   */
  public void setClob(String parm, Reader arg1, long arg2) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   */
  public void setNCharacterStream(String parm, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   * @param arg2
   */
  public void setNCharacterStream(String parm, Reader arg1, long arg2)
      throws SQLException {
    throw new NotImplementedException();

  }




  /**
   * @param parm
   * @param arg1
   */
  public void setNClob(String parm, Reader arg1) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   * @param arg2
   */
  public void setNClob(String parm, Reader arg1, long arg2) throws SQLException {
    throw new NotImplementedException();

  }


  /**
   * @param parm
   * @param arg1
   */
  public void setNString(String parm, String arg1) throws SQLException {
    throw new NotImplementedException();

  }



}
