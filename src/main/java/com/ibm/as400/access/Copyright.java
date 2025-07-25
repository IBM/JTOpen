///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  Copyright.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 The Copyright interface is used to hold the copyright string and the version information for the IBM Toolbox for Java.
 **/

//
//Toolbox driver change log.  Flags are used in ascending order.
//
//Flag  YYYYMMDD  Reason
//----- --------  ---------------
//@A1   20100406  Correct Syntax Error on Insert
//@A2   20100407  Update/Delete Blocking fix
//@A3   20100407  Allow DMD.getFunctions call on V5R4
//@A4   20100415  Fix update counts for batched update
//@A5   20100430  Correct JDUtilities.streamToBytes
//@A6   20100503  maximum blocked input rows property / Free storage used by blocked updates.
//@A7   20100506  buffer synchronization / remove Class.forName() contention / gc tracing
//@A8   20100507  minimize buffer usage for blocked insert
//@A9   20100720  Fix AS400JDBCResultSetMetaData.isAutoIncrement() when extended metadata is off
//@AA   20100805  Fix AS400JDBCStatement.execute to return syntax error from database engine.
//@AB   20101108  Make sure locators are scoped to cursor when isolation level = *NONE
//
//@B1   20101203  Call Trace.logLoadPath when loading class, so trace indicated where driver was loaded from.
//              Also trace JVM information.
//@B2   20101209  Fix Statement.setQueryTimeout
//@B3   20101209  Delay reading of input stream until execute
//@B4   20101215  Use NEW TABLE instead of FINAL table for autogenerated keys.
//@B5   20110105  Fix leaking DBStorage objects.
//@B6   20110114  Fix SQLResultSetTablePane
//@C1   20110119  Message File enhancement from David Gibbs
//@C2   20110121  CommandHelpRetriever fixes for empty help text.
//@C3   20110122  Make sure returnToPool is associated with nulling object. (Not Marked)
//@C4   20110217  Stored procedure scrollable cursor fixes.
//@C5   20110221  RLA Bidi Conversion
//@C6   20110224  Change JVM16 synchronization to not be default behavior
//@C7   20110303  Identify Generic Objects (replace with  subclasses)
//@C8   20110322  PCML Date/Time/Timestamp fixes
//@C9   20110405  Deadlock in DBStoragePool
//@CA   20110418  More timezone fixes.
//@D1   20110513  FileAttributes.getAttributes fails when returned data is larger than 2048 bytes.
//@D2   20010531  User library list updates
//@D3   20110601  Profile token or identity token is not valid when getting pooled connection while token is automatically refreshed.
//@D4   20110614 JDBC: query timeout mechanism property
//@D5   20110704  Handle ClassCastException in NPConversation.makeRequest()
//@D6   20110714  JDBC:  Correct Connection.isValid()
//@D7   20110714  JDBC:  Initial JDBC 4.1 changes
//@D8   20110715  Fix reading of com.ibm.as400.access.noDBReplyPooling and com.ibm.as400.access.DBDSPool.monitor properties
//@D9   20110802  JDBC: Add fetch warning to result set object.
//@DA   20110810  JDBC: Fix bug where clearParameters() causes executeBatch() failure.
//@Bidi-HCG3 20110812  Updated BIDI support
//@D10  20110815  Program Call: query timeout mechanism property
//@E1   20110902  ProfileTokenValue:  Fix bug when profileTokenCredential not used
//@E2   20110926  JDBC: Additional JDBC 4.1 changes
//@E3   20110926  JDBC: AS/IS JDBC client program
//@E4   20110929  JDBC: Always use GregorianCalendar to interpret database dates.
//@E5   20110929  JDBC: Fix updated row count for auto generated keys
//@F1   20111122  JDBC: Miscellaneous conversion fixes
//@F2   20111220  Ignore all exceptions when loading Buddhist calendar
//@F3   20111220  JDBC: Report fetch errors correctly
//@F4   20120118  PCML parser performance tuning
//@F5   20120208  JDBC: Honor decimal separator for PreparedStatement.setString()
//@F6   20120210  JDBC: describe option property
//@F7   20120210  JDBC: Ignore exceptions during race condition
//@F8   20120210  Improve CADSPool performance
//@F9   20120213  JDBC:  Decimal data errors property
//@FA   20120228  JDBC:  Fix java.util.NoSuchElementException: Vector Enumeration thrown by rollback
//@FB   20120410  Print: synchronized send and receive request in NPConversation.makeRequest()
//@FC   20120524  JDBC:  Allow using a java.net.URL for setObject and Datalink
//@G1   20120605  JDBC:  Fix array input parameters on reused CallableStatement
//@G2   20120606  JDBC:  Array parameter fixes
//@G3   20120611  JDBC:  Handle java.version of "0"
//@G4   20120730  JDBC:  Correct timestamp conversion.
//@G5   20120820  JDBC:  Fix named parameters.
//@G6   20120820  JDBC:  Fix DatabaseMetadata getCatalogTerm() and supportsExpressionsInOrderBy()
//@G7   20120823  JDBC:  Fix array input parameter when reusing callable statement
//@H1   20120830  JDBC:  Allow blocking when using asensitive cursors
//@H2   20121002  JDBC:  Report Truncation for mixed/open CCSIDs
//@H3   20121101  JDBC:  Improve timestamp support
//@H4   20121117  JDBC:  Reduce number of SQLConversionSettingsObjects
//@H5   20121119  JDBC:  Fix truncation for SQLNumeric and SQLDecimal objects
//@H6   20121206  Add QPWDEXPWRN support
//@H7   20130102  JDBC:  For CCSID 1208, do not report truncation if extra characters are spaces
//@I1   20130225  Refactor code block of SSL socket provider (shift between JSSE and SSL )
//@I2   20130318  JDBC:  Fix timestamp to String formatting
//@I3   20130404  Add Serializable interface to CancelLock
//@J1   20130702  JDBC:  Support IPV6 addresses in JDBC URL.
//@J2   20130812  JDBC:  Support timestamp as time parameter
//@J3   20130822  Support for up to 255 parameters on a remote program call request
//@J4   20130822  Support additional message data returned on remote command and remote program call replies
//@J5   20131001  JDBC:  Fix UTF-8 clobs
//@K1   20131110  JDBC:  Fix concurrent access resolution property
//@K2   20131114  JDBC:  Fix named parameters for CALL with return parameter
//@K4   20131212  JDBC:  Fix for JVM crash when -Xshareclasses is used with jt400Native.jar
//@K3   20140113  JDBC:  Variable Field compression 
//@K5   20140120  JDBC:  Fix DatabaseMetaData calls on READONLY connection
//@K6   20140127  JDBC:  Fix DatabaseMetaData.getSQLKeywords
//@K7   20140221  JDBC:  JDBC 4.2 Support
//@K8   20140225  Command Call:  Correct library name in returned message.
//@K9   20140303  Conversion:  Fix corruption of 16684 table by loading of CCSIDs 5026,5035,930, and 939. 
//@KA   20140303  Command Call:  Additional message information
//@KD   20140307  Conversion:  Add surrogate support and update 16684 table
//@KE   20140307  JDBC:  timestamp format property
//      20140408  JDBC: Set Minor Version for JDBC 4.2
//@L1   20140408  JDBC: Fix UDT Name in ResultSetMetaData
//@L2   20140408  JDBC: Correct DatabaseMetaData.getXXXFunctions
//@L3   20140408  JDBC: Variable Field Compression fixes
//@L4   20140423  PCML: Use non-validating parser if validating parser not available.
//@L5   20140516  Program Call: Compress output parameters for performance.
//@L6   20140523  Joblist: update attribute type.
//@L7   20140527  PCML: Fix xpcml parsing issue with struct_i tag.
//@L8   20140527  Program Call: Identify object in trace
//@L9   20140603  JDBC: Improve variable field compression performance
//@L10   20140624  Program Call: bi-direction RLE compression
//@L11   20140624  Cmd/pgm call: Improve performance about getjobinfo
//@L12   20140626  PTFGroup: get additional attributes
//@L13   20140701  Message: make DateFormate thread safe
//@L14   20140701  HTMLForm: make hidden field in top
//@L15   20140707  JDBC:  Correct error message when value overflow when setting BIGINT
//@L16   20140710  JDBC:  Correct default connection properties in AS400JDBCPooledConnection
//@L17   20140718  PCML: Update max string length to support 16MB
//@M1    20140821  PCML: Escape special characters when generateXPCML
//@M2    20140821  Program Call: Support program on IASP
//@M3    20140925  JDBC: Support *ALLUSR schema name on DatabaseMetaData.getTables
//@M4    20140926  Cause chain support
//@M5    20141008  JDBC:  Add getPositionOfSyntaxError to syntax exceptions
//@M6    20141010  JDBC:  Allow use of sort sequence table in IASP
//@M7    20141024  Misc:  Improve performance of RLE decompression
//@M8    20141111  DDM:  Fix DRDA correlation id issue
//@N1    20141204  JDBC:  Fix default setting of schema in AS400JDBCPooledConnect for system naming
//@N2    20141212  JDBC:  Fix !THREAD command of jdbc client to inherit environment
//@N3    20150113  JDBC:  Fix CHAR FOR BIT DATA parameters in input variable field compression
//@N4    20150119  Convert: update 16684, 300, 4396 conversion table  
//@N5    20150211  DHCP connect to port with non-localhost  
//@N6    20150213  Program Call: Support max 255 parameters in program call
//@N7    20150217  JDBC:   Delay errors from combined open/fetch
//@N8    20150324  JDBC:   Fix LONG CHAR FOR BIT DATA parameter in input variable field compression
//@O1    20150423  JPing ddm-ssl with correct port
//@O2    20150505  Command: fix up the offset for getting command processing library and program
//@O3    20150505  Fix Object Description size for OBJD0400 format
//@O4    20150505  JDBC:  Fix AS400DataSource and secure=true   
//@O5    20150610  Fix objectList name, library and type to not case sensitive   
//@O6    20150629  PCML: Fix performance issue about Class.forName in PcmlDataValues    
//@O7    20150629  JDBC: Fix trimming of leading spaces of column names
//@O8    20150630  JDBC: Fix errors from QSQFETCH from stored procedures not reported
//@O9    20150803  JDBC: Report truncation for InputStream parameters
//@P1    20150827  User: add STRAUTCOL parameters for v7r3
//@P2    20150828  Program Call: set iasp to pick up libs from current user profile and only call setasp when job asp is different
//@P3    20151005  JDBC: Fix ResultSetMetaData.getType() to match DatabaseMetaData.getColumns
//@P4    20151012  SSL set jvm ciphers list for iNav
//@P5    20151016  AS400 adds more interface to set asp group
//@P6    20151110  JDBC: Fix blank column labels.  Return labels from stored procedure calls. 
//@P7    20151110  JDBC: Performance improvement -- remove string comparisons
//@Q1    20151207  JDBC: Honor "ignore warnings" connection property for more scenarios
//@Q2    20151208  JDBC: Reduce exceptions generated by Decimal arrays
//@Q3    20160121  JDBC: Add getDB2ParameterName to CallabledStatement and ParameterMetaData
//@Q4    20160128  JDBC: Correct NullPointerException from JDError when multiple threads
//@Q5    20160222  DDS: Correct field type of Date, Time and Timestamp //remove this temply
//@Q6    20160222  Fix various javadoc typos
//@Q7    20160222  JDBC Client:  Support unicode escape '\u0000' in SQL statements
//@Q8    20160224  JDBC:  Fix BIDI column labels
//@Q9    20160224  JDBC Client:  Fix column label display
//@Q10   20160225  Make AS400.getServerName public
//@R1    20160408  Conversion: Added CCSIDs 1047,1166,5233 and Table Generator
//@R2    20160411  Beans:  Fix IFSFileBeanInfo (Due to JDK 1.7 change)
//@R3    20160419  JDBC:  "numeric range error" property
//@R4    20160422  JDBC:  Add missing get/set methods to DataSource and DataSourceBeanInfo
//@R5    20160428  IFSFileEnumeration fix issue when pattern without wildcard
//@R6    20160429  SystemStatus supports on status statistics reset
//@R7    20160516  JDBC:  "character truncation" property
//@R8    20160516  JDBC:  Fix setting timestamp from String
//@R9    20160518  JDBC:  Optimize sending of timestamp to server
//@RA    20160519  JDBC:  Fix batch insert of timestamp
//@RB    20160520  DDM: long record number support in KeyedFile
//@RC    20160523  Support V7R3 for up to 255 parameters on a remote program call request 
//@RD    20160607  IFSFile get ASP 
//@RE    20160607  JDBC:  Fix processing of NCHAR/NVARCHAR types
//@RF    20160628  JDBC:  Fix padding of batched GRAPHIC CCSID 835 parameters
//@S1    20160811  JDBC:  Set warning message to blank if sqlcode is 0
//@S2    20160822  JDBC:  Always return truncation warnings SQLSTATE 01004
//@S3    20160828  JDBC:  Correct ResultSetMetaData for NCHAR and NVARCHAR types
//@S4    20160828  SystemPool: Support pool size in long type
//@S5    20160927  JDBC:  Fix getting input stream from XML
//@S6    20161104  JDBC:  Allow setSchema(*LIBL) for system naming
//@S7    20161114  JDBC:  Allow AS440JDBCRowSet to use AS400JDBCManagedDataSource
//@S8    20161115  SystemPool:  Fix SystemPoolBeanInfo
//@S9    20161118  MessageQueue: Fix memory leak issue
//@SA    20161122  IFSFile get ASP and get file system type
//@SB    20161209  JDBC:  Add details to descriptor index invalid exception
//@SC    20170104  IFSFile: Fix getCCSID and getOwnerName
//@T1    20170309  JDBC:  Fix BLOB IO parameters
//@T2    20170321  IFSFile:  Create user handle only supports password authentication
//@T3    20170331  Misc:   Prepare for Java 9 by removing obsolete classes
//@U1    20170405  AS400 changePassword prepend 'Q' for numeric password
//@U2    20170405  Remove UserQueue class which is not finished.
//@U3    20170524  JDBC: Fix maxrows result set setting.  
//@U4    20170630  DDM:  Support ENCUSRPWD server setting
//@V1    20170908  JDBC:  portNumber property
//@V2    20170911  Misc:  Testability improvements 
//@V3    20170914  Correct ObjectList javadoc
//@V4    20171011  Free user handle
//@V5    20171011  Conversion:  Fix various ccsid conversions to match host conversions
//@V6    20171030  JDBC:  Fix detection of UTF-8 parameter truncation
//@V7    20171109  JDBC:  Handle java.version for Java 9
//@V8    20171115  JDBC:  Provide methods to get CCSIDs for Columns and Parameters
//@V9    20171212  Conversion:  Fix ConvTable4396
//@VA    20180103  Update setIASPGroup javadoc
//@VB    20180109  JTOpen 9.5
//@W1    20180309  JDBC:  Add parameter number to DATA_TYPE_MISMATCH exceptions
//@W2    20180228  JDBC:  enableClientAffinitiesList property
//@W3    20180320  Conversion:  Return substitution character if mixed ccsid buffer ends with half a character
//@W4    20180406  JDBC:  Fix SET CONNECTION with prepared Statements 
//@W5    20180515  JDBC:  maxRetriesForClientReroute and retryIntervalForClientReroute properties
//@W6    20180605  JDBC:  enableSeamlessFailover property
//@W7    20180808  JDBC:  add jtopeninfo() JDBC escaped function
//@W8    20180823  JDBC:  Added com.ibm.as400.util.UpdateACSJar utility
//@W9    20180830  To Uppercase user id for Turkish specific characters 
//@WA    20180906  JDBC:  Fix common table expressions and updatable result sets. 
//@WB    20180906  jdbcClient:  Handle common table expressions as query
//@X0    20180915  JTOpen 9.6
//@X1    20180927  JDBC:  request alternate server from host
//@X2    20180927  Translation: correct fault tolerant conversion
//@X3    20181011  JDBC:  alternate server fixes 
//@X4    20181024  JDBC:  For character truncation=none, do not insert invalid mixed CCSID strings
//@X5    20181024  JDBC:  Fix stored procedure call with null and array parameters
//@X6    20181108  JDBC:  Fix looping when mixed character truncation occurs during batch insert
//@X7    20181112  Fix incorrect time zone returned because a space at end of string.
//@X8    20181114  Support ASP API 
//@X9    20181116  Translation: Truncation fixes, CCSID 930 fixes, and CCSID 1175 support
//@Y0    20181119  JTOPen 9.7
//@Y1    20181206  Translation:  fixes for CCSID 918, 1097, 1371
//@Y2    20190102  JDBC:  affinity failback interval
//@Y3    20190123  Support for Japanese new era
//@Y4    20190128  Check the connection before close the list
//@Y5    20190129  Fix same ptfs are returned for different PTF groups
//@Y6    20190129  Support to delivery timeout from ProgramCallDocument to ProgramCall
//@Y7    20190130  Check *PUBLIC when check user profile object authority
//@Y8    20190214  Fix AS400Timestamp exception when timestamp length < 26
//@Z0    20190301  JTOpen 9.8
//@Z1    20190328  JDBC:  Support accepting errors from commit
//@Z2    20190419  JDBC:  Add thread safety for alternate server connections
//@Z3    20190508  Listener fixes
//@Z4    20190508  JDBC:  Connect using port to system with password level 2
//@Z4    20190709  build:  Javadoc get version from Copyright.java
//@Z5    20190718  JDBC:  Allow blocking of locators
//@Z6    20190828  validateSignon timeout
//@Z7    20190828  Fix stripping leading spaces from message text
//@Z8    20190828  Show Technology refresh PTF for PTF
//@AA1   20190902  JTOpen 10.1
//@AA2   20191106  JDBC:  Fix Connection.abort
//@AA3   20191111  PortMapper:  Use socket properties for port mapper connection
//@AA4   20191127  Fix setting *NONE for *PUBLIC when checking user profile object authority
//@AA5   20191127  Support to retrieve job queue
//@AA6   20191127  Add exist() function for JobDescription
//@AA7   20191127  Support to retrieve routing data entry from subsystem
//@AA8   20191210  Conversion: Refresh CCSID 13488 table
//@AA9   20191211  Fix Potential data integrity when setCCSID for file
//@AAA   20191211  JDBC:  Additional tracing for AS400JDBCConnectionRedirect
//@AAB   20191211  JDBC:  Fix lost parameters for PreparedStatement after Connection redirect 
//@AB1   20200107  JDBC:  Close JDBC connection when exit program prevents access. 
//@AB2   20200116  JDBC:  Do not truncate SIGNAL MESSAGE_TEXT
//@AB3   20200217  JDBC:  If possible, use job CCSID for default library attribute
//@AB4   20200219  ProgramCall timeout for SecureAS400
//@AB5   20200224  Not remove authorized user *PUBLIC
//@AC1   20200310  JDBC:  Correct affinityFailbackInterval property
//@AC2   20200316  JDBC:  setNetworkTimeout not supported when "thread used=true"
//@AC3   20200429  JDBC:  Change JDConnectionPoolManager to use minPoolSize when reuseConnection is set
//@AC4   20200603  SignonConverter throw AS400SecurityException.
//@AC5   20201019  Findbugs fixes.
//@AC6   20201023  Conversion: CCSID 1379 support
//@AC7   20201027  IFS Performance improvement
//@AC8   20201027  Varchar converter
//@AC9   20201029  Conversion: Various CCSID fixes
//@ACA   20201030  Support Create UserHandle2
//@AD1   20201215  JDBC:  Miscellaneous fixes
//@AD2   20201217  Conversion:  CCSID 1388 updates
//@AD3   20210112  JDBC Datasource:  Do not validate setClientRerouteAlternatePortNumber and setClientRerouteAlternateServerName 
//@AD4   20210120  JDBC Datasource:  Correct EnableClientAccessList
//@AD5   20210128  JDBC Proxy:  Fix problem with using proxy and JDBC
//@AD6   20210204  JDBC:  avoid signon server if portNumber != 0
//@AD7   20210212  Conversion:  CCSID 5473 and 13676 support.
//@AD8   20210215  Conversion:  CCSID 1379 Updates
//@AD9   20210216  JDBC:  Handle multiple warnings
//@AE1   20210302  PTF Requisite
//@AE2   20210310  Service program support 248 parameters
//@AE3   20210506  NetServer API updates for AUTL and SMB
//@AE4   20210615  Trace: Remove public read access from trace file
//@AE5   20210824  Fix commandcall connection keeps active after Job end().
//@AE6   20210824  Support to get password level.
//@AE7   20210825  Support to IFS File open node request and response.
//@AE8   20210825  Fix path name lower case issue for IFSFile functions.
//@AF1   20210929  JDBC: Report warnings from prepare
//@AF2   20210923  Support password level 4
//@AF3   20211020  Fix Base64 issue for Java11
//@AF4   20211102  Preserve original exception
//@AF5   20211102  JDBC: Add connection property:  tcp no delay 
//@AF6   20211115  Generate profile token for Password level 4
//@AF7   20220308  JDBC:  Support LocalTime,LocalDate,LocalDateTime
//@AF8   20220309  JDBC:  Handle SQL7061 reason code 80 as blocked mirror system
//@AF9   20220322  Fix MemberDescription convert issue.
//@AG1   20220322  Support varchar when convert pcml to xpcml
//@AG2   20220406  Add constructor AS400JDBCConnectionPoolDataSource(AS400)
//@AH1   20220625  JDBC: Update trace to include system name with RDB name
//@AH2   20220628  JDBC: Close socket on error 
//@AH3   20220823  Update URL in java doc
//@AH4   20220823  Set SECMEC value for password level 4 for DDM SECCHK Request
//@AH5   20220823  Fix swap user failure when user is disabled or expired.
//@AH6   20220929  Deprecate interfaces where password passed as a String
//@AH7   20221010  JDBC:  Permit cancel when using seamless failover
//@AH8   20221017  Set socket timeout for FTP
//@AH9   20221017  Add boolean attribute in PCML (pcml.dtd)
//@AI1   20221017  AS400FTP send \r\n when send user and pwd
//@AI2   20221017  Support VARCHAR in pcml.
//@AI3   20221020  Fix parse issue when non-ASCII characters in PCML
//@AI4   20221027  Fix DDM connect issue for Lowest encryption algorithm is AES
//@AI5   20221213  AS400Text support char array
//@AI6   20230103  ProgramCallDocument setCharArrayValue
//@AI7   20230111  JDBC:Internal password changes
//@AI8   20230208  Fix generate profile token issue
//@AI9   20230216  Clear char array and byte array
//@AJ1   20230331  Fetch info from new BuildInfo (autogenerated) class
// NOTE:  When adding a line above, adjust the "String version" with the flag value.
//--------------------------------------------------------------------

public interface Copyright
{
    /** @deprecated  This field is reserved for use within the Toolbox product. **/
    public static String copyright = "Copyright (C) 1997-"+BuildInfo.getTimestampYear()+" International Business Machines Corporation and others.";
    // Name of current release to be picked up by Java doc build. 
    public static String JTOpenName = "JTOpen "+BuildInfo.getVersion();
    //                                                                                                 
    public static String version   = "Open Source Software, "+JTOpenName+" codebase 5770-SS1 V7R6M0.00 built="+BuildInfo.getTimestampString();  


    // Constants for reference by AS400JDBCDriver.
    static final int    MAJOR_VERSION = 14; // ex: "14" indicates V7R6 
    static final int    MINOR_VERSION = 2; // ex: "1" indicates PTF #1 (1 is first PTF in a release)
                                           //Note: JTOpen 21.0.4 is syncing with ptf 14.1
    static final String DRIVER_LEVEL  = "07060002"; //(ex: 07060001 -> V7R6M0 PTF#1) (needed for hidden clientInfo) (each # is 2 digits in length)

}
