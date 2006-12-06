///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDDatabaseMetaDataProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;



class JDDatabaseMetaDataProxy
extends AbstractProxyImpl
implements java.sql.DatabaseMetaData
{
  private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";


  // Private data.
 
  private JDConnectionProxy       jdConnection_;
         // The object that caused this object to be created.


  /**
   Constructs a JDDatabaseMetaDataProxy object.

   @param connectionProxy The Connection object that caused this object to be created.
   **/
  public JDDatabaseMetaDataProxy (JDConnectionProxy jdConnection)
  {
    jdConnection_ = jdConnection;
  }


    public boolean allProceduresAreCallable ()
    throws SQLException
    {
      return callMethodRtnBool ("allProceduresAreCallable");
    }

    public boolean allTablesAreSelectable ()
    throws SQLException
    {
      return callMethodRtnBool ("allTablesAreSelectable");
    }

    private boolean callMethodRtnBool (String methodName)
    throws SQLException
    {
      try {
        return connection_.callMethodReturnsBoolean (pxId_, methodName);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }

  // Call a method, and return a boolean.
    private boolean callMethodRtnBool (String methodName, int arg)
    throws SQLException
    {
      try {
        return connection_.callMethod (pxId_, methodName,
                                     new Class[] { Integer.TYPE },
                                     new Object[] { new Integer (arg) })
                                 .getReturnValueBoolean ();
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }

    private boolean callMethodRtnBool (String methodName,
                                       Class[] argClasses,
                                       Object[] argValues)
      throws SQLException
    {
      try {
        return connection_.callMethod (pxId_, methodName,
                                            argClasses, argValues)
                               .getReturnValueBoolean ();
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }

  // Call a method, and return an int.
    private int callMethodRtnInt (String methodName)
    throws SQLException
    {
      try {
        return connection_.callMethodReturnsInt (pxId_, methodName);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }

  // Call a method, and return a ResultSet.
    private JDResultSetProxy callMethodRtnRSet (String methodName)
    throws SQLException
    {
      try {
        JDResultSetProxy newResultSet = new JDResultSetProxy (jdConnection_);
        return (JDResultSetProxy) connection_.callFactoryMethod (
                                         pxId_, methodName, newResultSet);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }

    private JDResultSetProxy callMethodRtnRSet (String methodName,
                                         Class[] argClasses,
                                         Object[] argValues)
      throws SQLException
    {
      try {
        JDResultSetProxy newResultSet = new JDResultSetProxy (jdConnection_);
        return (JDResultSetProxy) connection_.callFactoryMethod (
                                            pxId_, methodName,
                                            argClasses, argValues,
                                            newResultSet);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }

  // Call a method, and return a String.
    private String callMethodRtnStr (String methodName)
    throws SQLException
    {
      try {
        return (String) connection_.callMethodReturnsObject (pxId_, methodName);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }

    //@PDA jdbc40
    private Object callMethodRtnObj (String methodName)
      throws SQLException
    {
      try {
        return connection_.callMethodReturnsObject (pxId_, methodName);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }
    
    //@PDA jdbc40
    private Object callMethodRtnObj(String methodName, Class[] argClasses, Object[] argValues) throws SQLException
    {
        try
        {
            return connection_.callMethod(pxId_, methodName, argClasses, argValues).getReturnValue();
        } catch (InvocationTargetException e)
        {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    public boolean dataDefinitionCausesTransactionCommit ()
    throws SQLException
    {
      return callMethodRtnBool ("dataDefinitionCausesTransactionCommit");
    }


    public boolean dataDefinitionIgnoredInTransactions ()
    throws SQLException
    {
      return callMethodRtnBool ("dataDefinitionIgnoredInTransactions");
    }



// JDBC 2.0
    public boolean deletesAreDetected (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("deletesAreDetected", resultSetType);
    }


    public boolean doesMaxRowSizeIncludeBlobs ()
    throws SQLException
    {
      return callMethodRtnBool ("doesMaxRowSizeIncludeBlobs");
    }


// JDBC 3.0
    public ResultSet getAttributes (String catalog,
                                    String schemaPattern,
                                    String typeNamePattern,
                                    String attributeNamePattern)
    throws SQLException
    {
        return callMethodRtnRSet ("getAttributes",
                                  new Class[] { String.class, String.class,
                                      String.class,
                                      String.class},
                                  new Object[] { catalog, schemaPattern, typeNamePattern,
                                      attributeNamePattern});
    }


    public ResultSet getBestRowIdentifier (String catalog,
                                           String schema,
                                           String table,
                                           int scope,
                                           boolean nullable)
    throws SQLException
    {
      return callMethodRtnRSet ("getBestRowIdentifier",
                                new Class[] { String.class, String.class,
                                              String.class,
                                              Integer.TYPE, Boolean.TYPE },
                                new Object[] { catalog, schema, table,
                                               new Integer (scope),
                                               new Boolean (nullable) });
    }


    public ResultSet getCatalogs ()
    throws SQLException
    {
      return callMethodRtnRSet ("getCatalogs");
    }

    public String getCatalogSeparator ()
    throws SQLException
    {
      return callMethodRtnStr ("getCatalogSeparator");
    }


    public String getCatalogTerm ()
    throws SQLException
    {
      return callMethodRtnStr ("getCatalogTerm");
    }

    public ResultSet getColumnPrivileges (String catalog,
                                          String schema,
                                          String table,
                                          String columnPattern)
    throws SQLException
    {
      return callMethodRtnRSet ("getColumnPrivileges",
                                     new Class[] { String.class, String.class,
                                                   String.class, String.class },
                                     new Object[] { catalog, schema,
                                                    table, columnPattern });
    }

    public ResultSet getColumns (String catalog,
                                 String schemaPattern,
                                 String tablePattern,
                                 String columnPattern)
    throws SQLException
    {
      return callMethodRtnRSet ("getColumns",
                                new Class[] { String.class, String.class,
                                              String.class, String.class },
                                new Object[] { catalog, schemaPattern,
                                               tablePattern, columnPattern });
    }


// JDBC 2.0
    public java.sql.Connection getConnection ()
    throws SQLException
    {
        return jdConnection_;
    }


    public ResultSet getCrossReference (String primaryCatalog,
                                        String primarySchema,
                                        String primaryTable,
                                        String foreignCatalog,
                                        String foreignSchema,
                                        String foreignTable)
    throws SQLException
    {
      return callMethodRtnRSet ("getCrossReference",
                                new Class[] { String.class, String.class,
                                              String.class, String.class,
                                              String.class, String.class },
                                new Object[] { primaryCatalog, primarySchema,
                                               primaryTable, foreignCatalog,
                                               foreignSchema, foreignTable });
    }


// JDBC 3.0
    public int getDatabaseMajorVersion ()
    throws SQLException
    {
        return callMethodRtnInt ("getDatabaseMajorVersion");
    }


// JDBC 3.0
    public int getDatabaseMinorVersion ()
    throws SQLException
    {
        return callMethodRtnInt ("getDatabaseMinorVersion");
    }


    public String getDatabaseProductName ()
    throws SQLException
    {
      return callMethodRtnStr ("getDatabaseProductName");
    }


    public String getDatabaseProductVersion ()
    throws SQLException
    {
      return callMethodRtnStr ("getDatabaseProductVersion");
    }


    public int getDefaultTransactionIsolation ()
    throws SQLException
    {
      return callMethodRtnInt ("getDefaultTransactionIsolation");
    }


    public int getDriverMajorVersion ()
    {
      try {
        return callMethodRtnInt ("getDriverMajorVersion");
      }
      catch (SQLException e)
      {
        throw new InternalErrorException (e.getMessage (), InternalErrorException.UNEXPECTED_EXCEPTION);
      }
    }


    public int getDriverMinorVersion ()
    {
      try {
        return callMethodRtnInt ("getDriverMinorVersion");
      }
      catch (SQLException e)
      {
        throw new InternalErrorException (e.getMessage (), InternalErrorException.UNEXPECTED_EXCEPTION);
      }
    }


    public String getDriverName ()
    throws SQLException
    {
      return callMethodRtnStr ("getDriverName");
    }


    public String getDriverVersion ()
    throws SQLException
    {
      return callMethodRtnStr ("getDriverVersion");
    }


    public ResultSet getExportedKeys (String catalog,
                                      String schema,
                                      String table)
    throws SQLException
    {
      return callMethodRtnRSet ("getExportedKeys",
                                new Class[] { String.class, String.class,
                                              String.class },
                                new Object[] { catalog, schema, table });
    }


    public String getExtraNameCharacters ()
    throws SQLException
    {
      return callMethodRtnStr ("getExtraNameCharacters");
    }


    public String getIdentifierQuoteString ()
    throws SQLException
    {
      return callMethodRtnStr ("getIdentifierQuoteString");
    }


    public ResultSet getImportedKeys (String catalog,
                                      String schema,
                                      String table)
    throws SQLException
    {
      return callMethodRtnRSet ("getImportedKeys",
                                new Class[] { String.class, String.class,
                                              String.class },
                                new Object[] { catalog, schema, table });
    }


    public ResultSet getIndexInfo (String catalog,
                                   String schema,
                                   String table,
                                   boolean unique,
                                   boolean approximate)
    throws SQLException
    {
      return callMethodRtnRSet ("getIndexInfo",
                                new Class[] { String.class, String.class,
                                              String.class, Boolean.TYPE,
                                              Boolean.TYPE },
                                new Object[] { catalog, schema,
                                               table, new Boolean (unique),
                                               new Boolean (approximate) });
    }


// JDBC 3.0
    public int getJDBCMajorVersion ()
    throws SQLException
    {
        return callMethodRtnInt ("getJDBCMajorVersion");
    }


// JDBC 3.0
    public int getJDBCMinorVersion ()
    throws SQLException
    {
        return callMethodRtnInt ("getJDBCMinorVersion");
    }


    public int getMaxBinaryLiteralLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxBinaryLiteralLength");
    }


    public int getMaxCatalogNameLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxCatalogNameLength");
    }


    public int getMaxCharLiteralLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxCharLiteralLength");
    }


    public int getMaxColumnNameLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxColumnNameLength");
    }


    public int getMaxColumnsInGroupBy ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxColumnsInGroupBy");
    }


    public int getMaxColumnsInIndex ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxColumnsInIndex");
    }


    public int getMaxColumnsInOrderBy ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxColumnsInOrderBy");
    }


    public int getMaxColumnsInSelect ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxColumnsInSelect");
    }


    public int getMaxColumnsInTable ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxColumnsInTable");
    }


    public int getMaxConnections ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxConnections");
    }


    public int getMaxCursorNameLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxCursorNameLength");
    }


    public int getMaxIndexLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxIndexLength");
    }


    public int getMaxProcedureNameLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxProcedureNameLength");
    }


    public int getMaxRowSize ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxRowSize");
    }


    public int getMaxSchemaNameLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxSchemaNameLength");
    }


    public int getMaxStatementLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxStatementLength");
    }


    public int getMaxStatements ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxStatements");
    }


    public int getMaxTableNameLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxTableNameLength");
    }


    public int getMaxTablesInSelect ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxTablesInSelect");
    }


    public int getMaxUserNameLength ()
    throws SQLException
    {
      return callMethodRtnInt ("getMaxUserNameLength");
    }


    public String getNumericFunctions ()
    throws SQLException
    {
      return callMethodRtnStr ("getNumericFunctions");
    }


    public ResultSet getPrimaryKeys (String catalog,
                                     String schema,
                                     String table)
    throws SQLException
    {
      return callMethodRtnRSet ("getPrimaryKeys",
                                new Class[] { String.class, String.class,
                                              String.class },
                                new Object[] { catalog, schema, table });
    } 


    public ResultSet getProcedureColumns (String catalog,
                                          String schemaPattern,
                                          String procedurePattern,
                                          String columnPattern)
    throws SQLException
    {
      return callMethodRtnRSet ("getProcedureColumns",
                           new Class[] { String.class, String.class,
                                         String.class, String.class },
                           new Object[] { catalog, schemaPattern,
                                          procedurePattern, columnPattern });
    }


    public ResultSet getProcedures (String catalog,
                                    String schemaPattern,
                                    String procedurePattern)
    throws SQLException
    {
      return callMethodRtnRSet ("getProcedures",
                                new Class[] { String.class, String.class,
                                              String.class },
                                new Object[] { catalog, schemaPattern,
                                               procedurePattern });
    }


    public String getProcedureTerm ()
    throws SQLException
    {
      return callMethodRtnStr ("getProcedureTerm");
    }


// JDBC 3.0
    public int getResultSetHoldability ()
    throws SQLException
    {
        return callMethodRtnInt ("getResultSetHoldability");
    }


    public ResultSet getSchemas ()
    throws SQLException
    {
      return callMethodRtnRSet ("getSchemas");
    }


    public String getSchemaTerm ()
    throws SQLException
    {
      return callMethodRtnStr ("getSchemaTerm");
    }


    public String getSearchStringEscape ()
    throws SQLException
    {
      return callMethodRtnStr ("getSearchStringEscape");
    }


    public String getSQLKeywords ()
    throws SQLException
    {
      return callMethodRtnStr ("getSQLKeywords");
    }


// JDBC 3.0
    public int getSQLStateType ()
    throws SQLException
    {
        return callMethodRtnInt ("getSQLStateType");
    }


    public String getStringFunctions ()
    throws SQLException
    {
      return callMethodRtnStr ("getStringFunctions");
    }


// JDBC 3.0
    public ResultSet getSuperTables (String catalog,
                                     String schemaPattern,
                                     String tableNamePattern)
    throws SQLException
    {
        return callMethodRtnRSet ("getSuperTables",
                                  new Class[] { String.class, String.class,
                                      String.class},
                                  new Object[] { catalog, schemaPattern,
                                      tableNamePattern});
    }


// JDBC 3.0
    public ResultSet getSuperTypes (String catalog,
                                    String schemaPattern,
                                    String typeNamePattern)
    throws SQLException
    {
        return callMethodRtnRSet ("getSuperTypes",
                                  new Class[] { String.class, String.class,
                                      String.class},
                                  new Object[] { catalog, schemaPattern,
                                      typeNamePattern});
    }



    public String getSystemFunctions ()
    throws SQLException
    {
      return callMethodRtnStr ("getSystemFunctions");
    }


    public ResultSet getTablePrivileges (String catalog,
                                         String schemaPattern,
                                         String tablePattern)
    throws SQLException
    {
      return callMethodRtnRSet ("getTablePrivileges",
                                new Class[] { String.class, String.class,
                                              String.class },
                                new Object[] { catalog, schemaPattern,
                                               tablePattern });
    }


    public ResultSet getTables (String catalog,
                                String schemaPattern,
                                String tablePattern,
                                String tableTypes[])
    throws SQLException
    {
      return callMethodRtnRSet ("getTables",
                                new Class[] { String.class, String.class,
                                              String.class, String[].class },
                                new Object[] { catalog, schemaPattern,
                                               tablePattern, tableTypes });
    }


    public ResultSet getTableTypes ()
    throws SQLException
    {
      return callMethodRtnRSet ("getTableTypes");
    }


    public String getTimeDateFunctions ()
    throws SQLException
    {
      return callMethodRtnStr ("getTimeDateFunctions");
    }


    public ResultSet getTypeInfo ()
    throws SQLException
    {
      return callMethodRtnRSet ("getTypeInfo");
    }


// JDBC 2.0
    public ResultSet getUDTs (String catalog,
                              String schemaPattern,
                              String typeNamePattern,
                              int[] types)
    throws SQLException
    {
      return callMethodRtnRSet ("getUDTs",
                                new Class[] { String.class, String.class,
                                              String.class, int[].class },
                                new Object[] { catalog, schemaPattern,
                                               typeNamePattern, types });
    }


    public String getURL ()
    throws SQLException
    {
      return callMethodRtnStr ("getURL");
    }


    public String getUserName ()
    throws SQLException
    {
      return callMethodRtnStr ("getUserName");
    }


    public ResultSet getVersionColumns (String catalog,
                                        String schema,
                                        String table)
    throws SQLException
    {
      return callMethodRtnRSet ("getVersionColumns",
                                new Class[] { String.class, String.class,
                                              String.class },
                                new Object[] { catalog, schema, table });
    }


// JDBC 2.0
    public boolean insertsAreDetected (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("insertsAreDetected", resultSetType);
    }


    public boolean isCatalogAtStart ()
    throws SQLException
    {
      return callMethodRtnBool ("isCatalogAtStart");
    }


    private boolean isCatalogValid (String catalog)
    throws SQLException
    {
      return callMethodRtnBool ("isCatalogValid",
                               new Class[] { String.class },
                               new Object[] { catalog });
    }


    public boolean isReadOnly ()
    throws SQLException
    {
      return callMethodRtnBool ("isReadOnly");
    }


// JDBC 3.0
    public boolean locatorsUpdateCopy ()
    throws SQLException
    {
        return callMethodRtnBool ("locatorsUpdateCopy");
    } 


    public boolean nullPlusNonNullIsNull ()
    throws SQLException
    {
      return callMethodRtnBool ("nullPlusNonNullIsNull");
    }


    public boolean nullsAreSortedAtEnd ()
    throws SQLException
    {
      return callMethodRtnBool ("nullsAreSortedAtEnd");
    }


    public boolean nullsAreSortedAtStart ()
    throws SQLException
    {
      return callMethodRtnBool ("nullsAreSortedAtStart");
    }


    public boolean nullsAreSortedHigh ()
    throws SQLException
    {
      return callMethodRtnBool ("nullsAreSortedHigh");
    }


    public boolean nullsAreSortedLow ()
    throws SQLException
    {
      return callMethodRtnBool ("nullsAreSortedLow");
    }


// JDBC 2.0
    public boolean othersDeletesAreVisible (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("othersDeletesAreVisible", resultSetType);
    }


// JDBC 2.0
    public boolean othersInsertsAreVisible (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("othersInsertsAreVisible", resultSetType);
    }


// JDBC 2.0
    public boolean othersUpdatesAreVisible (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("othersUpdatesAreVisible", resultSetType);
    }


// JDBC 2.0
    public boolean ownDeletesAreVisible (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("ownDeletesAreVisible", resultSetType);
    }


// JDBC 2.0
    public boolean ownInsertsAreVisible (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("ownInsertsAreVisible", resultSetType);
    }


// JDBC 2.0
    public boolean ownUpdatesAreVisible (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("ownUpdatesAreVisible", resultSetType);
    }


    public boolean storesLowerCaseIdentifiers ()
    throws SQLException
    {
      return callMethodRtnBool ("storesLowerCaseIdentifiers");
    }


    public boolean storesLowerCaseQuotedIdentifiers ()
    throws SQLException
    {
      return callMethodRtnBool ("storesLowerCaseQuotedIdentifiers");
    }


    public boolean storesMixedCaseIdentifiers ()
    throws SQLException
    {
      return callMethodRtnBool ("storesMixedCaseIdentifiers");
    }


    public boolean storesMixedCaseQuotedIdentifiers ()
    throws SQLException
    {
      return callMethodRtnBool ("storesMixedCaseQuotedIdentifiers");
    }


    public boolean storesUpperCaseIdentifiers ()
    throws SQLException
    {
      return callMethodRtnBool ("storesUpperCaseIdentifiers");
    }


    public boolean storesUpperCaseQuotedIdentifiers ()
    throws SQLException
    {
      return callMethodRtnBool ("storesUpperCaseQuotedIdentifiers");
    }


    public boolean supportsAlterTableWithAddColumn ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsAlterTableWithAddColumn");
    }


    public boolean supportsAlterTableWithDropColumn ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsAlterTableWithDropColumn");
    }


    public boolean supportsANSI92EntryLevelSQL ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsANSI92EntryLevelSQL");
    }


    public boolean supportsANSI92FullSQL ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsANSI92FullSQL");
    }


    public boolean supportsANSI92IntermediateSQL ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsANSI92IntermediateSQL");
    }


// JDBC 2.0
    public boolean supportsBatchUpdates ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsBatchUpdates");
    }


    public boolean supportsCatalogsInDataManipulation ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsCatalogsInDataManipulation");
    }


    public boolean supportsCatalogsInIndexDefinitions ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsCatalogsInIndexDefinitions");
    }


    public boolean supportsCatalogsInPrivilegeDefinitions ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsCatalogsInPrivilegeDefinitions");
    }


    public boolean supportsCatalogsInProcedureCalls ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsCatalogsInProcedureCalls");
    }


    public boolean supportsCatalogsInTableDefinitions ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsCatalogsInTableDefinitions");
    }


    public boolean supportsColumnAliasing ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsColumnAliasing");
    }


    public boolean supportsConvert ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsConvert");
    }


    public boolean supportsConvert (int fromType, int toType)
    throws SQLException
    {
      return callMethodRtnBool ("supportsConvert",
                               new Class[] { Integer.TYPE, Integer.TYPE },
                               new Object[] { new Integer (fromType),
                                              new Integer (toType) });
    }


    public boolean supportsCoreSQLGrammar ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsCoreSQLGrammar");
    }


    public boolean supportsCorrelatedSubqueries ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsCorrelatedSubqueries");
    }


    public boolean supportsDataDefinitionAndDataManipulationTransactions ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsDataDefinitionAndDataManipulationTransactions");
    }


    public boolean supportsDataManipulationTransactionsOnly ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsDataManipulationTransactionsOnly");
    }


    public boolean supportsDifferentTableCorrelationNames ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsDifferentTableCorrelationNames");
    }


    public boolean supportsExpressionsInOrderBy ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsExpressionsInOrderBy");
    }


    public boolean supportsExtendedSQLGrammar ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsExtendedSQLGrammar");
    }


    public boolean supportsFullOuterJoins ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsFullOuterJoins");
    }


// JDBC 3.0
    public boolean supportsGetGeneratedKeys ()
    throws SQLException
    {
        return callMethodRtnBool ("supportsGetGeneratedKeys");
    }


    public boolean supportsGroupBy ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsGroupBy");
    }


    public boolean supportsGroupByBeyondSelect ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsGroupByBeyondSelect");
    }


    public boolean supportsGroupByUnrelated ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsGroupByUnrelated");
    }


    public boolean supportsIntegrityEnhancementFacility ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsIntegrityEnhancementFacility");
    }


    public boolean supportsLikeEscapeClause ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsLikeEscapeClause");
    }


    public boolean supportsLimitedOuterJoins ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsLimitedOuterJoins");
    }


    public boolean supportsMinimumSQLGrammar ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsMinimumSQLGrammar");
    }


    public boolean supportsMixedCaseIdentifiers ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsMixedCaseIdentifiers");
    }


    public boolean supportsMixedCaseQuotedIdentifiers ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsMixedCaseQuotedIdentifiers");
    }


// JDBC 3.0
    public boolean supportsMultipleOpenResults ()
    throws SQLException
    {
        return callMethodRtnBool ("supportsMultipleOpenResults");
    }


    public boolean supportsMultipleResultSets ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsMultipleResultSets");
    }


    public boolean supportsMultipleTransactions ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsMultipleTransactions");
    }


// JDBC 3.0
    public boolean supportsNamedParameters ()
    throws SQLException
    {
        return callMethodRtnBool ("supportsNamedParameters");
    }


    public boolean supportsNonNullableColumns ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsNonNullableColumns");
    }


    public boolean supportsOpenCursorsAcrossCommit ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsOpenCursorsAcrossCommit");
    }


    public boolean supportsOpenCursorsAcrossRollback ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsOpenCursorsAcrossRollback");
    }


    public boolean supportsOpenStatementsAcrossCommit ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsOpenStatementsAcrossCommit");
    }


    public boolean supportsOpenStatementsAcrossRollback ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsOpenStatementsAcrossRollback");
    }


    public boolean supportsOrderByUnrelated ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsOrderByUnrelated");
    }


    public boolean supportsOuterJoins ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsOuterJoins");
    }


    public boolean supportsPositionedDelete ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsPositionedDelete");
    }


    public boolean supportsPositionedUpdate ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsPositionedUpdate");
    }


// JDBC 2.0
    public boolean supportsResultSetConcurrency (int resultSetType, int resultSetConcurrency)
    throws SQLException
    {
      return callMethodRtnBool ("supportsResultSetConcurrency",
                          new Class[] { Integer.TYPE, Integer.TYPE },
                          new Object[] { new Integer (resultSetType),
                                         new Integer (resultSetConcurrency) });
    }


// JDBC 3.0
    public boolean supportsResultSetHoldability (int resultSetHoldability)
    throws SQLException
    {
        return callMethodRtnBool ("supportsResultSetHoldability",
                                  new Class[] { Integer.TYPE},
                                  new Object[] { new Integer (resultSetHoldability)});
    }


// JDBC 2.0
    public boolean supportsResultSetType (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("supportsResultSetType", resultSetType);
    }


// JDBC 3.0
    public boolean supportsSavepoints ()
    throws SQLException
    {
        return callMethodRtnBool ("supportsSavepoints");
    }


    public boolean supportsSchemasInDataManipulation ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSchemasInDataManipulation");
    }


    public boolean supportsSchemasInIndexDefinitions ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSchemasInIndexDefinitions");
    }


    public boolean supportsSchemasInPrivilegeDefinitions ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSchemasInPrivilegeDefinitions");
    }


    public boolean supportsSchemasInProcedureCalls ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSchemasInProcedureCalls");
    }


    public boolean supportsSchemasInTableDefinitions ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSchemasInTableDefinitions");
    }


    public boolean supportsSelectForUpdate ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSelectForUpdate");
    }


    public boolean supportsStatementPooling()
    throws SQLException
    {
        return callMethodRtnBool ("supportsStatementPooling");
    }


    public boolean supportsStoredProcedures ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsStoredProcedures");
    }


    public boolean supportsSubqueriesInComparisons ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSubqueriesInComparisons");
    }


    public boolean supportsSubqueriesInExists ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSubqueriesInExists");
    }


    public boolean supportsSubqueriesInIns ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSubqueriesInIns");
    }


    public boolean supportsSubqueriesInQuantifieds ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsSubqueriesInQuantifieds");
    }


    public boolean supportsTableCorrelationNames ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsTableCorrelationNames");
    }


    public boolean supportsTransactionIsolationLevel (int transactionIsolationLevel)
    throws SQLException
    {
      return callMethodRtnBool ("supportsTransactionIsolationLevel", transactionIsolationLevel);
    }


    public boolean supportsTransactions ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsTransactions");
    }


    public boolean supportsUnion ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsUnion");
    }


    public boolean supportsUnionAll ()
    throws SQLException
    {
      return callMethodRtnBool ("supportsUnionAll");
    }


    // This method is not required by java.sql.DatabaseMetaData,
    // but it is used by the JDBC testcases, and is implemented
    // in the public class.
    public String toString ()
    {
      try {
        return (String) connection_.callMethodReturnsObject (pxId_, "toString");
      }
      catch (InvocationTargetException e) {
        throw ProxyClientConnection.rethrow (e);
      }
    }


// JDBC 2.0
    public boolean updatesAreDetected (int resultSetType)
    throws SQLException
    {
      return callMethodRtnBool ("updatesAreDetected", resultSetType);
    }


    public boolean usesLocalFilePerTable ()
    throws SQLException
    {
      return callMethodRtnBool ("usesLocalFilePerTable");
    }


    public boolean usesLocalFiles ()
    throws SQLException
    {
      return callMethodRtnBool ("usesLocalFiles");
    }

    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] { "java.sql.DatabaseMetaData" };
    } 
  

    //@PDA jdbc40
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException
    {
        return callMethodRtnBool("autoCommitFailureClosesAllResultSets");
    }

    
    //@PDA jdbc40
    public ResultSet getClientInfoProperties() throws SQLException
    {
        return callMethodRtnRSet("getClientInfoProperties");
    }


    //@PDA jdbc40
    public RowIdLifetime getRowIdLifetime() throws SQLException
    {
        return (RowIdLifetime) callMethodRtnObj("getRowIdLifetime");
    }


    //@PDA jdbc40
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException
    {
        return callMethodRtnRSet("getSchemas", new Class[] {String.class, String.class}, new Object[] {catalog, schemaPattern});
    }

    // @PDA jdbc40
    public boolean providesQueryObjectGenerator() throws SQLException
    {
        return callMethodRtnBool("providesQueryObjectGenerator");
    }

    //@PDA jdbc40
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
    {
        return callMethodRtnBool("supportsStoredFunctionsUsingCallSyntax");
    }


    //@PDA jdbc40
    public ResultSet getFunctionParameters(String catalog, String schemaPattern, String functionNamePattern, String parameterNamePattern) throws SQLException
    {
        return callMethodRtnRSet ("getFunctionParameters",
                new Class[] { String.class, String.class, String.class, String.class },
                new Object[] { catalog, schemaPattern, functionNamePattern, parameterNamePattern });
    }

    //@PDA jdbc40
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException
    {
        return callMethodRtnRSet ("getFunctions",
                new Class[] { String.class, String.class, String.class },
                new Object[] { catalog, schemaPattern, functionNamePattern });
    }
    
    //@pda jdbc40
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException
    { 
        return callMethodRtnRSet ("getFunctionColumns",
                new Class[] { String.class, String.class, String.class, String.class },
                new Object[] { catalog, schemaPattern, functionNamePattern, columnNamePattern });
    }

}
