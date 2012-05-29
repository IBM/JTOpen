///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCColumnMetaData.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import java.sql.SQLException;
import java.sql.Types;

import com.ibm.jtopenlite.database.DB2Type;

/*
 * Utility class to get metadata information from columns and parameters
 */
public class JDBCColumnMetaData {

  public static int getPrecision(Column column) {
    /* The JDBC notion of precision and scale do not match what is returned in the 0x3812 -- Super extended data format. */
    int precision = 0;
    switch(0xFFFE & column.getType()) {
      case DB2Type.CHAR:
      case DB2Type.VARCHAR:
        case DB2Type.LONGVARCHAR:
        case DB2Type.DATALINK:
        case DB2Type.BINARY:
        case DB2Type.VARBINARY:
        case DB2Type.LONGVARGRAPHIC:
        case DB2Type.VARGRAPHIC:
        case DB2Type.GRAPHIC:
        precision = column.getScale();
        break;
        case DB2Type.FLOATINGPOINT:
        	if (column.getLength() == 4) {
        		return 24;
        	} else {
        		return 53;
        	}
        case DB2Type.CLOB:
        case DB2Type.BLOB:
        precision = column.getLength() - 4;
        break;
        case DB2Type.DBCLOB:
          precision = (column.getLength() - 4) / 2;
        break;
        case DB2Type.INTEGER:         precision = 10;      break;
        case DB2Type.SMALLINT:        precision = 5;       break;
        case DB2Type.DATE:          precision = 10;    break;
        case DB2Type.TIME:            precision = 8;       break;
        case DB2Type.TIMESTAMP:       precision = 26;      break;

        case DB2Type.BLOB_LOCATOR:
        case DB2Type.CLOB_LOCATOR:
        case DB2Type.DBCLOB_LOCATOR:    precision = column.getLobMaxSize(); break;
        case DB2Type.XML_LOCATOR:
        case DB2Type.XML:   precision = 2147483647; break;
        default:
          precision = column.getPrecision();
    }
    return precision;

  }

  public static int getScale(Column column) throws SQLException {
    switch(column.getSQLType()) {
    case Types.CHAR:
    case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.DATE:
      case Types.TIME:
      case Types.DATALINK:
      case Types.BINARY:
      case Types.VARBINARY:
      case DB2Type.LONGVARGRAPHIC:
      case DB2Type.VARGRAPHIC:
      case DB2Type.GRAPHIC:
      return 0;
      case Types.TIMESTAMP:
        return 6;
    }
  return column.getScale();

  }

}
