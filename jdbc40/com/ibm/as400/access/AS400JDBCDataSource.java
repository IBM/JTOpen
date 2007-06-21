///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCDataSource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;                          // @J3a
import javax.sql.DataSource;                      // JDBC2.0 std-ext
import javax.naming.NamingException;              // JNDI
import javax.naming.Reference;                    // JNDI
import javax.naming.Referenceable;                // JNDI
import javax.naming.StringRefAddr;                // JNDI

/**
*  The AS400JDBCDataSource class represents a factory for i5/OS database connections.
*
*  <P>The following is an example that creates an AS400JDBCDataSource object and creates a
*  connection to the database.
*
*  <pre><blockquote>
*  // Create a data source for making the connection.
*  AS400JDBCDataSource datasource = new AS400JDBCDataSource("myAS400");
*  datasource.setUser("myUser");
*  datasource.setPassword("MYPWD");

*  // Create a database connection to the system.
*  Connection connection = datasource.getConnection();
*  </blockquote></pre>
*
*  <P>The following example registers an AS400JDBCDataSource object with JNDI and then
*  uses the object returned from JNDI to obtain a database connection.
*  <pre><blockquote>
*  // Create a data source to the i5/OS database.
*  AS400JDBCDataSource dataSource = new AS400JDBCDataSource();
*  dataSource.setServerName("myAS400");
*
*  // Register the datasource with the Java Naming and Directory Interface (JNDI).
*  Hashtable env = new Hashtable();
*  env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
*  Context context = new InitialContext(env);
*  context.bind("jdbc/customer", dataSource);
*
*  // Return an AS400JDBCDataSource object from JNDI and get a connection.
*  AS400JDBCDataSource datasource = (AS400JDBCDataSource) context.lookup("jdbc/customer");
*  Connection connection = datasource.getConnection("myUser", "MYPWD");
*  </pre></blockquote>
**/
public class AS400JDBCDataSource extends ToolboxWrapper //@pdc jdbc40
implements DataSource, Referenceable, Serializable, Cloneable //@PDC 550
{
    static final long serialVersionUID = 4L;



    /**
    *  Implementation notes:
    *  The properties listed in com.ibm.as400.access.JDProperties should also be included here.
    **/

    // Constants
    private static final String DATABASE_NAME = "databaseName";
    private static final String DATASOURCE_NAME = "dataSourceName";
    private static final String DESCRIPTION = "description";
    private static final String SERVER_NAME = "serverName";
    private static final String USER = "userName";
    private static final String KEY_RING_NAME = "keyring";       // @F0A
    private static final String PASSWORD = "pw";                 // @F0A
    private static final String KEY_RING_PASSWORD = "keyringpw"; // @F0A
    private static final String SECURE = "secure";               // @F0A
    private static final String SAVE_PASSWORD = "savepw";        // @F0A
    private static final String PLAIN_TEXT_PASSWORD = "pwd";     //@K1A
    private static final String TRUE_ = "true";
    private static final String FALSE_ = "false";
    private static final String TOOLBOX_DRIVER = "jdbc:as400:";
    private static final int MAX_THRESHOLD = 16777216;                  // Maximum threshold (bytes). @A3C, @A4A
    private static final int MAX_SCALE = 63;                            // Maximum decimal scale

    // socket options to store away in JNDI
    private static final String SOCKET_KEEP_ALIVE = "soKeepAlive"; // @F1A
    private static final String SOCKET_RECEIVE_BUFFER_SIZE = "soReceiveBufferSize"; // @F1A
    private static final String SOCKET_SEND_BUFFER_SIZE = "soSendBufferSize"; // @F1A
    private static final String SOCKET_LINGER = "soLinger"; // @F1A
    private static final String SOCKET_TIMEOUT = "soTimeout"; // @F1A
    private static final String SOCKET_TCP_NO_DELAY = "soTCPNoDelay"; // @F1A

    // Data source properties.
    transient private AS400 as400_;                           // AS400 object used to store and encrypt the password.
    // @J2d private String databaseName_ = "";                // Database name. @A6C
    private String dataSourceName_ = "";                      // Data source name. @A6C
    private String description_ = "";                         // Data source description. @A6C
    private JDProperties properties_;                         // i5/OS connection properties.
    private SocketProperties sockProps_;                      // i5/OS socket properties @F1A
    transient private PrintWriter writer_;                    // The EventLog print writer.  @C7c
    transient private EventLog log_;       //@C7c

    private String serialServerName_;                         // system name used in serialization.
    private String serialUserName_;                           // User used in serialization.
    private String serialKeyRingName_;     //@B4A             // Key ring name used in serialization.
    transient PropertyChangeSupport changes_; //@B0C
    private boolean isSecure_ = false;  //@B4A

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;      //@A9A


    // In mod 5 support was added to optionally serialize the password with the
    // rest of the properties.  By default this is off.  setSavePasswordWhenSerialized(true)
    // must be called to save the password.  By calling this the application takes
    // responsibility for protecting the serialized bytes.  The password is not saved in the 
    // clear.  The password string is confused so that something more than just looking at the 
    // serialized bytes must be done to see the password.  
    private char[]  serialPWBytes_ = null;               //@J3a
    private char[]  serialKeyRingPWBytes_ = null;        //@J3a
    private boolean savePasswordWhenSerialized_ = false; //@J3a   by default, don't save password!!!!

    /**
     * The maximum storage space in megabytes, that can be used to execute a query.
    **/
    public static final int MAX_STORAGE_LIMIT = 2147352578;                    // Maximum query storage limit @550


    /**
      Start tracing the JDBC client.  This is the same as setting
      property "trace=true";  Note the constant is not public.
      It is defined only to be compatible with ODBC
      The numeric value of this constant is 1.
     **/
    static final int TRACE_CLIENT = 1;                // @j1a

    /**
      Start the database monitor on the JDBC server job.
      This constant is used when setting the level of tracing for the JDBC server job.
      The numeric value of this constant is 2.
     **/
    public static final int SERVER_TRACE_START_DATABASE_MONITOR = 2;           // @j1a

    /**
      Start debug on the JDBC server job.
      This constant is used when setting the level of tracing for the JDBC server job.
      The numeric value of this constant is 4.
     **/
    public static final int SERVER_TRACE_DEBUG_SERVER_JOB = 4;           // @j1a

    /**
      Save the joblog when the JDBC server job ends.
      This constant is used when setting the level of tracing for the JDBC server job.
      The numeric value of this constant is 8.
     **/
    public static final int SERVER_TRACE_SAVE_SERVER_JOBLOG = 8;           // @j1a

    /**
      Start job trace on the JDBC server job.
      This constant is used when setting the level of tracing for the JDBC server job.
      The numeric value of this constant is 16.
     **/
    public static final int SERVER_TRACE_TRACE_SERVER_JOB = 16;           // @j1a

    /**
      Save SQL information.
      This constant is used when setting the level of tracing for the JDBC server job.
      The numeric value of this constant is 32.
     **/
    public static final int SERVER_TRACE_SAVE_SQL_INFORMATION = 32;           // @j1a


    /**
    *  Constructs a default AS400JDBCDataSource object.
    **/
    public AS400JDBCDataSource()
    {
        initializeTransient();
        properties_ = new JDProperties(null, null);
        sockProps_ = new SocketProperties();
    }

    /**
    *  Constructs an AS400JDBCDataSource object to the specified <i>serverName</i>.
    *  @param serverName The name of the i5/OS system.
    **/
    public AS400JDBCDataSource(String serverName)
    {
        this();

        setServerName(serverName);
    }

    /**
    *  Constructs an AS400JDBCDataSource object with the specified signon information.
    *  @param serverName The name of the i5/OS system.
    *  @param user The user id.
    *  @param password The user password.
    **/
    public AS400JDBCDataSource(String serverName, String user, String password)
    {
        this();

        setServerName(serverName);
        setUser(user);
        setPassword(password);
    }

    //@K1A
    /**
    * Constructs an AS400JDBCDataSource object with the specified AS400 object
    * @param as400 The AS400 object
    **/
    public AS400JDBCDataSource(AS400 as400)
    {
        this();

        as400_ = as400;
        if( as400 instanceof SecureAS400 )
            setSecure(true);

    }

    //@B4A
    /**
    *  Constructs an AS400JDBCDataSource object with the specified signon information
    *  to use for SSL communications with the system.
    *  @param serverName The name of the i5/OS system.
    *  @param user The user id.
    *  @param password The user password.
       *  @param keyRingName The key ring class name to be used for SSL communications with the system.
       *  @param keyRingPassword The password for the key ring class to be used for SSL communications with the system.
    **/
    public AS400JDBCDataSource(String serverName, String user, String password,
                               String keyRingName, String keyRingPassword)
    {
        this();

        setSecure(true);  // @F0M

        try
        {
            as400_ = new SecureAS400(as400_);
            ((SecureAS400)as400_).setKeyRingName(keyRingName, keyRingPassword);
        }
        catch (PropertyVetoException pe)
        { /* will never happen */
        }
        serialKeyRingName_ = keyRingName;

        // @J3 There is no get/set keyring name / password methods so they really aren't bean
        // properties, but in v5r1 the keyring name is saved as if it is a property.  Since
        // the code saved the name we will also save the password. 
        serialKeyRingPWBytes_ = xpwConfuse(keyRingPassword);     //@J3a  // @F0M  (changed from keyRingName to keyRingPassword)

        setServerName(serverName);
        setUser(user);
        setPassword(password);
    }

    // @F0A - Added the following constructor to avoid creating some extra objects
    /**
    * Constructs an AS400JDBCDataSource object from the specified Reference object
    * @param reference to retrieve the DataSource properties from
    **/
    AS400JDBCDataSource(Reference reference) {
        /*
        *  Implementation note:  This method is called from AS400JDBCObjectFactory.getObjectInstance
        */

        // check to make sure our reference is not null
        if (reference == null)
            throw new NullPointerException("reference");

        // set up property change support
        changes_ = new PropertyChangeSupport(this);

        // set up the as400 object
        if (((String)reference.get(SECURE).getContent()).equalsIgnoreCase(TRUE_)) {
            isSecure_ = true;
            as400_ = new SecureAS400();

            // since the as400 object is secure, get the key ring info
            serialKeyRingName_ = (String)reference.get(KEY_RING_NAME).getContent();
            if (reference.get(KEY_RING_PASSWORD) != null)
                serialKeyRingPWBytes_ = ((String)reference.get(KEY_RING_PASSWORD).getContent()).toCharArray();
            else
                serialKeyRingPWBytes_ = null;

            try {
                if (serialKeyRingPWBytes_ != null && serialKeyRingPWBytes_.length > 0)
                    ((SecureAS400)as400_).setKeyRingName(serialKeyRingName_, xpwDeconfuse(serialKeyRingPWBytes_));
                else
                    ((SecureAS400)as400_).setKeyRingName(serialKeyRingName_);
            } catch (PropertyVetoException pve) { /* Will never happen */ }

        } else {
            isSecure_ = false;
            as400_ = new AS400();
        }

        // must initialize the JDProperties so the property change checks dont get a NullPointerException
        properties_ = new JDProperties(null, null);

        Properties properties = new Properties();
        sockProps_ = new SocketProperties();

        Enumeration list = reference.getAll();
        while (list.hasMoreElements())
        {
            StringRefAddr refAddr = (StringRefAddr)list.nextElement();
            String property = refAddr.getType();
            String value = (String)reference.get(property).getContent();

            // constant identifiers were used to store in JNDI
            // all of these were handled already so do not put them in the properties
            if (property.equals(DATABASE_NAME))                         
                setDatabaseName(value);
            else if (property.equals(DATASOURCE_NAME))
                setDataSourceName(value);
            else if (property.equals(DESCRIPTION))
                setDescription(value);
            else if (property.equals(SERVER_NAME))
                setServerName(value);
            else if (property.equals(USER))
                setUser(value);
            else if(property.equals(PLAIN_TEXT_PASSWORD)) {         //@K1A
                //set the password                                  //@K1A
                setPassword(value);                                 //@K1A
            }
            else if (property.equals(PASSWORD)) {
                if(reference.get(PLAIN_TEXT_PASSWORD) != null)      //@K1A
                {                                                   //@K1A
                    setPassword((String)reference.get(PLAIN_TEXT_PASSWORD).getContent());       //@K1A
                }                                                                               //@K1A
                else                                                                            //@K1A
                {                                                                               //@K1A
                    // get the password back from the serialized char[]
                    serialPWBytes_ = value.toCharArray();
                    // decode the password and set it on the as400
                    as400_.setPassword(xpwDeconfuse(serialPWBytes_));
                }                                                                               //@K1A
            }
            else if (property.equals(SAVE_PASSWORD)) {
                // set the savePasswordWhenSerialized_ flag
                savePasswordWhenSerialized_ = value.equals(TRUE_) ? true : false;
            } else if (property.equals(SECURE) || property.equals(KEY_RING_NAME) || property.equals(KEY_RING_PASSWORD)) {
                // do nothing for these keys, they have already been handled
            }
            else if (property.equals(SOCKET_KEEP_ALIVE)) {
                sockProps_.setKeepAlive((value.equals(TRUE_)? true : false));
            }
            else if (property.equals(SOCKET_RECEIVE_BUFFER_SIZE)) {
                sockProps_.setReceiveBufferSize(Integer.parseInt(value));
            }
            else if (property.equals(SOCKET_SEND_BUFFER_SIZE)) {
                sockProps_.setSendBufferSize(Integer.parseInt(value));
            }
            else if (property.equals(SOCKET_LINGER)) {
                sockProps_.setSoLinger(Integer.parseInt(value));
            }
            else if (property.equals(SOCKET_TIMEOUT)) {
                sockProps_.setSoTimeout(Integer.parseInt(value));
            }
            else if (property.equals(SOCKET_TCP_NO_DELAY)) {
                sockProps_.setTcpNoDelay((value.equals(TRUE_)? true : false));
            }
            else
            {
                properties.put(property, value);
            }
        }
        properties_ = new JDProperties(properties, null);

        // get the prompt property and set it back in the as400 object
        String prmpt = properties_.getString(JDProperties.PROMPT);
        if (prmpt != null && prmpt.equalsIgnoreCase(FALSE_))
            setPrompt(false);
        else if (prmpt != null && prmpt.equalsIgnoreCase(TRUE_))
            setPrompt(true);

    }

    /**
    *  Adds a PropertyChangeListener.  The specified PropertyChangeListener's
    *  <b>propertyChange</b> method is called each time the value of any bound
    *  property is changed.
    *  @see #removePropertyChangeListener
    *  @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        changes_.addPropertyChangeListener(listener);

        as400_.addPropertyChangeListener(listener);
    }

    //@PDA 550 - clone
    /**
     * Method to create a clone of AS400JDBCDataSource. This does a shallow
     * copy, with the exception of JDProperties, which also gets cloned.
     */
    public Object clone()
    {
        try
        {
            AS400JDBCDataSource clone = (AS400JDBCDataSource) super.clone();
            clone.properties_ = (JDProperties) this.properties_.clone();
            return clone;
        } catch (CloneNotSupportedException e)
        { // This should never happen.
            Trace.log(Trace.ERROR, e);
            throw new UnsupportedOperationException("clone()");
        }
    }
    
    /**
    *  Returns the level of database access for the connection.
    *  @return The access level.  Valid values include: "all" (all SQL statements allowed),
    *  "read call" (SELECT and CALL statements allowed), and "read only" (SELECT statements only).
    *  The default value is "all".
    **/
    public String getAccess()
    {
        return properties_.getString(JDProperties.ACCESS);
    }

    // @C9 new method
    /**
    *  Returns what behaviors of the Toolbox JDBC driver have been overridden.
    *  Multiple behaviors can be overridden in combination by adding 
    *  the constants and passing that sum on the setBehaviorOverride() method.  
    *  @return The behaviors that have been overridden. 
    *  <p>The return value is a combination of the following:
    *  <ul>
    *  <li>1 - Do not throw an exception if Statement.executeQuery() or
    *          PreparedStatement.executeQuery() do not return a result set.
    *          Instead, return null for the result set.
    *  </ul>
    *
    **/
    public int getBehaviorOverride()
    {
        return properties_.getInt(JDProperties.BEHAVIOR_OVERRIDE);
    }

    //@B2A
    /**
    *  Returns the output string type of bidi data, as defined by the CDRA
    *  (Character Data Representation Architecture). See <a href="BidiStringType.html">
    *  BidiStringType</a> for more information and valid values.  -1 will be returned
    *  if the value has not been set.
    **/
    public int getBidiStringType()                                                               //@B3C
    {
        String value = properties_.getString(JDProperties.BIDI_STRING_TYPE);     //@B3C
        try
        {                                                                                          //@B3A                                                                                            //@B3A
            return Integer.parseInt (value);                                              //@B3A
        }                                                                                            //@B3A
        catch (NumberFormatException nfe)  // if value is "", that is, not set        //@B3A
        {                                                                                            //@B3A
            return -1;                                                                              //@B3A
        }                                                                                            //@B3A
    }


    /**
    *  Returns the criteria for retrieving data from the system in
    *  blocks of records.  Specifying a non-zero value for this property
    *  will reduce the frequency of communication to the system, and
    *  therefore increase performance.
    *  @return The block criteria.
    *  <p>Valid values include:
    *  <ul>
    *    <li> 0 (no record blocking)
    *    <li> 1 (block if FOR FETCH ONLY is specified)
    *    <li> 2 (block if FOR UPDATE is specified) - The default value.
    *  </ul>
    **/
    public int getBlockCriteria()
    {
        return properties_.getInt(JDProperties.BLOCK_CRITERIA);
    }

    /**
    *  Returns the block size in kilobytes to retrieve from the system and
    *  cache on the client.  This property has no effect unless the block criteria
    *  property is non-zero.  Larger block sizes reduce the frequency of
    *  communication to the system, and therefore may increase performance.
    *  @return The block size in kilobytes.
    *  <p>Valid values include:
    *  <ul>
    *    <li> 0
    *    <li> 8
    *    <li> 16
    *    <li> 32   - The default value.
    *    <li> 64
    *    <li> 128
    *    <li> 256
    *    <li> 512
    *  </ul>
    **/
    public int getBlockSize()
    {
        return properties_.getInt(JDProperties.BLOCK_SIZE);
    }


    /**
    *  Returns the database connection.
    *  @return The connection.
    *  @exception SQLException If a database error occurs.
    **/
    public Connection getConnection() throws SQLException
    {    
        //if the object was created with a keyring, or if the user asks for the object
        //to be secure, clone a SecureAS400 object; otherwise, clone an AS400 object
        if (isSecure_ || isSecure())                     //@B4A  //@C2C
            return getConnection(new SecureAS400(as400_));   //@B4A
        else                               //@B4A
            return getConnection(new AS400(as400_));
    }


    // @J3 Nothing to change here.  The password is serialized only when passed on the c'tor 
    //     or via the settors.  That is, "bean properties" are affected only when using the 
    //     c'tor specifying system, uid, and pwd, or the settors are used.  The bean properties
    //     are not affected if this method is used, or if the default c'tor is used such
    //     that our sign-on dialog is used to get system, uid and pwd from the user.  
    /**
    *  Returns the database connection using the specified <i>user</i> and <i>password</i>.
    *  @param user The database user.
    *  @param password The database password.
    *  @return The connection
    *  @exception SQLException If a database error occurs.
    **/
    public Connection getConnection(String user, String password) throws SQLException
    {
        // Validate the parameters.
        if (user == null)
            throw new NullPointerException("user");
        if (password == null)
            throw new NullPointerException("password");

        AS400 as400Object;

        //if the object was created with a keyring, or if the user asks for the object
        //to be secure, clone a SecureAS400 object; otherwise, clone an AS400 object
        if (isSecure_ || isSecure())                                        //@C2A
        {                                                                   //@C2A
            as400Object = new SecureAS400(getServerName(), user, password); //@C2A
        }                                                                   //@C2A
        else
        {                                                                //@C2A                                                                   //@C2A     
            as400Object = new AS400(getServerName(), user, password);       //@C2A
        }                                                                   //@C2A

        try                                                                 //@PDA
        {                                                                   //@PDA
            if(!as400_.isThreadUsed())                                      //@PDA
                as400Object.setThreadUsed(false);  //true by default        //@PDA
        } catch (PropertyVetoException pve)                                 //@PDA
        { /*ignore*/                                                        //@PDA
        }                                                                   //@PDA
        
        //set gui available on the new object to false if user turned prompting off
        try
        {                                                                   //@C2A                                
            if (!isPrompt())                                                //@C2A
                as400Object.setGuiAvailable(false);                         //@C2A
        }                                                                   //@C2A
        catch (PropertyVetoException pve)                                   //@C2A
        { /*ignore*/                                                        //@C2A
        }                                                                   //@C2A
        return getConnection(as400Object);                                  //@C2A

        //@C2D return getConnection(new AS400(getServerName(), user, password));
    }


    /**
    *  Creates the database connection based on the signon and property information.
    *  @param as400 The AS400 object used to make the connection.
    *  @exception SQLException If a database error occurs.
    **/
    private Connection getConnection(AS400 as400) throws SQLException
    {
        // Set the socket properties, if there are any, on the AS400 object before making a connection.
        if(sockProps_ != null){
            as400.setSocketProperties(sockProps_);
        }else
        {
            if(JDTrace.isTraceOn())
                JDTrace.logInformation(this, "sockProps_:  null");
        }

        AS400JDBCConnection connection = null;

        connection = new AS400JDBCConnection();    

        connection.setSystem(as400);
        connection.setProperties(new JDDataSourceURL(TOOLBOX_DRIVER + "//" + as400.getSystemName()), properties_, as400); //@C1C

        log(loader_.getText("AS400_JDBC_DS_CONN_CREATED"));     //@A9C
        return connection;
    }

    //@C8A
    /**
    *  Returns the value of the cursor sensitivity property.  If the resultSetType is 
    *  ResultSet.TYPE_FORWARD_ONLY or ResultSet.TYPE_SCROLL_SENSITIVE, the value of this property
    *  will control what cursor sensitivity is requested from the database.  If the resultSetType
    *  is ResultSet.TYPE_SCROLL_INSENSITIVE, this property will be ignored.
    *  @return The cursor sensitivity.  
    *  <p>Valid values include:
    *  <ul>
    *    <li> "asensitive"
    *    <li> "insensitive"
    *    <li> "sensitive"
    *  </ul>
    *  The default is "asensitive".
    *
    *  This property is ignored when connecting to systems
    *  running OS/400 V5R1 and earlier.   
    **/
    public String getCursorSensitivity()
    {
        return properties_.getString(JDProperties.CURSOR_SENSITIVITY);      
    }


    /**
    *  Returns the database name property.  For more information see
    *  the documentation for the setDatabaseName() method in this class.
    *  @return The database name.
    **/
    public String getDatabaseName()
    {
        // @J2d return databaseName_;
        return properties_.getString(JDProperties.DATABASE_NAME);      // @J2a
    }

    /**
    *  Returns the data source name property.
    *  This property is used to name an underlying data source when connection pooling is used.
    *  @return The data source name.
    **/
    public String getDataSourceName()
    {
        return dataSourceName_;
    }

    /**
    *  Returns the i5/OS date format used in date literals within SQL statements.
    *  @return The date format.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "mdy"
    *    <li> "dmy"
    *    <li> "ymd"
    *    <li> "usa"
    *    <li> "iso"
    *    <li> "eur"
    *    <li> "jis"
    *    <li> "julian"
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default is based on the server job.
    **/
    public String getDateFormat()
    {
        return properties_.getString(JDProperties.DATE_FORMAT);
    }

    /**
    *  Returns the i5/OS date separator used in date literals within SQL statements.
    *  This property has no effect unless the "data format" property is set to:
    *  "julian", "mdy", "dmy", or "ymd".
    *  @return The date separator.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "/" (slash)
    *    <li> "-" (dash)
    *    <li> "." (period)
    *    <li> "," (comma)
    *    <li> " " (space)
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default value is based on the server job.
    **/
    public String getDateSeparator()
    {
        return properties_.getString(JDProperties.DATE_SEPARATOR);
    }

    //@DFA
    /**
    *  Returns the decfloat rounding mode.
    *  @return The decfloat rounding mode.
    *   <p>Valid values include:
    *   <ul>
    *   <li>"half even" - default
    *   <li>"half up" 
    *   <li>"down" 
    *   <li>"ceiling" 
    *   <li>"floor" 
    *   <li>"half down" 
    *   <li>"up" 
    *   </ul>
    **/
    public String getDecfloatRoundingMode()
    {
        return properties_.getString(JDProperties.DECFLOAT_ROUNDING_MODE);
    }
     
    /**
    *  Returns the i5/OS decimal separator used in numeric literals within SQL statements.
    *  @return The decimal separator.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "." (period)
    *    <li> "," (comma)
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default value is based on the server job.
    **/
    public String getDecimalSeparator()
    {
        return properties_.getString(JDProperties.DECIMAL_SEPARATOR);
    }

    /**
    *  Returns the description of the data source.
    *  @return The description.
    **/
    public String getDescription()
    {
        return description_;
    }

    // @A2A
    /**
    * Returns the JDBC driver implementation.
    * This property has no
    * effect if the "secondary URL" property is set.
    * This property cannot be set to "native" if the
    * environment is not an OS/400 or i5/OS Java Virtual
    * Machine.
    *  <p>Valid values include:
    *  <ul>
    *  <li>"toolbox" (use the IBM Toolbox for Java JDBC driver)
    *  <li>"native" (use the IBM Developer Kit for Java JDBC driver)
    *  </ul>
    *  The default value is "toolbox".
    **/
    public String getDriver()
    {
        return properties_.getString(JDProperties.DRIVER);
    }

    /**
    *  Returns the amount of detail for error messages originating from
    *  the i5/OS system.
    *  @return The error message level.
    *  Valid values include: "basic" and "full".  The default value is "basic".
    **/
    public String getErrors()
    {
        return properties_.getString(JDProperties.ERRORS);
    }

    /**
    *  Returns the i5/OS system libraries to add to the server job's library list.
    *  The libraries are delimited by commas or spaces, and
    *  "*LIBL" may be used as a place holder for the server job's
    *  current library list.  The library list is used for resolving
    *  unqualified stored procedure calls and finding schemas in
    *  DatabaseMetaData catalog methods.  If "*LIBL" is not specified,
    *  the specified libraries will replace the server job's current library list.
    *  @return The library list.
    **/
    public String getLibraries()
    {
        return properties_.getString(JDProperties.LIBRARIES);
    }

    /**
    *  Returns the maximum LOB (large object) size in bytes that
    *  can be retrieved as part of a result set.  LOBs that are larger
    *  than this threshold will be retrieved in pieces using extra
    *  communication to the system.  Larger LOB thresholds will reduce
    *  the frequency of communication to the system, but will download
    *  more LOB data, even if it is not used.  Smaller LOB thresholds may
    *  increase frequency of communication to the system, but will only
    *  download LOB data as it is needed.
    *  @return The lob threshold.  Valid range is 0-16777216.
    *  The default value is 32768.
    **/
    public int getLobThreshold()
    {
        return properties_.getInt(JDProperties.LOB_THRESHOLD);
    }

    /**
    *  Returns the timeout value in seconds.
    *  Note: This value is not used or supported.
    *  The timeout value is determined by the i5/OS system.
    *  @return the maximum time in seconds that this data source can wait while attempting to connect to a database. 
    **/
    public int getLoginTimeout()
    {
        return properties_.getInt(JDProperties.LOGIN_TIMEOUT);
    }

    /**
    *  Returns the log writer for this data source.
    *  @return The log writer for this data source.
    *  @exception SQLException If a database error occurs.
    **/
    public PrintWriter getLogWriter() throws SQLException
    {
        return writer_;
    }

    //@PDA
    /**                                                               
    *  Indicates how to retrieve DatabaseMetaData.
    *  If set to 0, database metadata will be retrieved through the ROI data flow.  
    *  If set to 1, database metadata will be retrieved by calling system stored procedures. 
    *  The methods that currently are available through stored procedures are:
    *  getColumnPrivileges
    *  @return the metadata setting.
    *  The default value is 1.
    **/
    public int getMetaDataSource()
    {
        return properties_.getInt(JDProperties.METADATA_SOURCE);
    }
    
    //@dup
    /**                                                               
     *  Indicates how to retrieve DatabaseMetaData.
     *  If set to 0, database metadata will be retrieved through the ROI data flow.  
     *  If set to 1, database metadata will be retrieved by calling system stored procedures. 
     *  The methods that currently are available through stored procedures are:
     *  getColumnPrivileges
     *  @return the metadata setting.
     *  The default value is 1.
     *  Note:  this method is the same as getMetaDataSource() so that it corresponds to the connection property name
     **/
    public int getMetaDatasource()
    {
        return getMetaDataSource();
    }
    
    /**
    *  Returns the naming convention used when referring to tables.
    *  @return The naming convention.  Valid values include: "sql" (e.g. schema.table)
    *  and "system" (e.g. schema/table).  The default value is "sql".
    **/
    public String getNaming()
    {
        return properties_.getString(JDProperties.NAMING);
    }

    /**
    *  Returns the base name of the SQL package.  Note that only the
    *  first seven characters are used to generate the name of the SQL package on the system.  
    *  This property has no effect unless
    *  the extended dynamic property is set to true.  In addition, this property
    *  must be set if the extended dynamic property is set to true.
    *  @return The base name of the SQL package.
    **/
    public String getPackage()
    {
        return properties_.getString(JDProperties.PACKAGE);
    }

    /**
    *  Returns the type of SQL statement to be stored in the SQL package.  This can
    *  be useful to improve the performance of complex join conditions.  This
    *  property has no effect unless the extended dynamic property is set to true.
    *  @return The type of SQL statement.
    *  Valid values include: "default" (only store SQL statements with parameter
    *  markers in the package) and "select" (store all SQL SELECT statements
    *  in the package).  The default value is "default".
    **/
    public String getPackageCriteria()
    {
        return properties_.getString(JDProperties.PACKAGE_CRITERIA);
    }

    /**
    *  Returns the action to take when SQL package errors occur.  When an SQL package
    *  error occurs, the driver will optionally throw an SQLException or post a
    *  warning to the Connection, based on the value of this property.  This property
    *  has no effect unless the extended dynamic property is set to true.
    *  @return The action to take when SQL errors occur.
    *  Valid values include: "exception", "warning", and "none".  The default value is "warning".
    **/
    public String getPackageError()
    {
        return properties_.getString(JDProperties.PACKAGE_ERROR);
    }
    /**
    *  Returns the library for the SQL package.  This property has no effect unless
    *  the extended dynamic property is set to true.
    *  @return The SQL package library.  The default library is "QGPL".
    **/
    public String getPackageLibrary()
    {
        return properties_.getString(JDProperties.PACKAGE_LIBRARY);
    }

    /**
    *  Returns the name of the proxy server.
    *  @return The proxy server.
    **/
    public String getProxyServer()
    {
        return properties_.getString(JDProperties.PROXY_SERVER);
    }

    /**
    *  Returns the Reference object for the data source object.
    *  This is used by JNDI when bound in a JNDI naming service.
    *  Contains the information necessary to reconstruct the data source
    *  object when it is later retrieved from JNDI via an object factory.
    *
    *  @return A Reference object of the data source object.
    *  @exception NamingException If a naming error occurs in resolving the object.
    **/
    public Reference getReference() throws NamingException
    {
        Reference ref = new Reference(this.getClass().getName(),
                                      "com.ibm.as400.access.AS400JDBCObjectFactory",
                                      null);

        // Add the JDBC properties.
        DriverPropertyInfo[] propertyList = properties_.getInfo();
        for (int i=0; i< propertyList.length; i++)
        {
            if (propertyList[i].value != null)
                ref.add(new StringRefAddr(propertyList[i].name, propertyList[i].value));
        }

        // Add the Socket options
        if (sockProps_.keepAliveSet_) ref.add(new StringRefAddr(SOCKET_KEEP_ALIVE, (sockProps_.keepAlive_ ? "true" : "false")));
        if (sockProps_.receiveBufferSizeSet_) ref.add(new StringRefAddr(SOCKET_RECEIVE_BUFFER_SIZE, Integer.toString(sockProps_.receiveBufferSize_)));
        if (sockProps_.sendBufferSizeSet_) ref.add(new StringRefAddr(SOCKET_SEND_BUFFER_SIZE, Integer.toString(sockProps_.sendBufferSize_)));
        if (sockProps_.soLingerSet_) ref.add(new StringRefAddr(SOCKET_LINGER, Integer.toString(sockProps_.soLinger_)));
        if (sockProps_.soTimeoutSet_) ref.add(new StringRefAddr(SOCKET_TIMEOUT, Integer.toString(sockProps_.soTimeout_)));
        if (sockProps_.tcpNoDelaySet_) ref.add(new StringRefAddr(SOCKET_TCP_NO_DELAY, (sockProps_.tcpNoDelay_ ? "true" : "false")));

        // Add the data source properties.  (unique constant identifiers for storing in JNDI).
        if (getDatabaseName() != null)
            ref.add(new StringRefAddr(DATABASE_NAME, getDatabaseName()));
        if (getDataSourceName() != null)
            ref.add(new StringRefAddr(DATASOURCE_NAME, getDataSourceName()));
        if (getDescription() != null)
            ref.add(new StringRefAddr(DESCRIPTION, getDescription()));
        ref.add(new StringRefAddr(SERVER_NAME, getServerName()));
        ref.add(new StringRefAddr(USER, getUser()));
        ref.add(new StringRefAddr(KEY_RING_NAME, serialKeyRingName_));                             // @F0A
        if (savePasswordWhenSerialized_) {                                                         // @F0A
            ref.add(new StringRefAddr(PASSWORD, new String(serialPWBytes_)));                      // @F0A
            if (serialKeyRingPWBytes_ != null)                                                     // @F0A
                ref.add(new StringRefAddr(KEY_RING_PASSWORD, new String(serialKeyRingPWBytes_)));  // @F0A
            else                                                                                   // @F0A
                ref.add(new StringRefAddr(KEY_RING_PASSWORD, null));                               // @F0A
        }                                                                                          // @F0A
        ref.add(new StringRefAddr(SECURE, (isSecure_ ? TRUE_ : FALSE_)));                          // @F0A
        ref.add(new StringRefAddr(SAVE_PASSWORD, (savePasswordWhenSerialized_ ? TRUE_ : FALSE_))); // @F0A

        return ref;
    }

    /**
    *  Returns the source of the text for REMARKS columns in ResultSets returned
    *  by DatabaseMetaData methods.
    *  @return The text source.
    *  Valid values include: "sql" (SQL object comment) and "system" (OS/400 or i5/OS object description).
    *  The default value is "system".
    **/
    public String getRemarks()
    {
        return properties_.getString(JDProperties.REMARKS);
    }

    /**
    *  Returns the secondary URL.
    *  @return The secondary URL.
    **/
    public String getSecondaryUrl()
    {
        return properties_.getString(JDProperties.SECONDARY_URL);
    }
    
    //@dup
    /**
     *  Returns the secondary URL.
     *  @return The secondary URL.
     *  Note:  this method is the same as setSecondaryUrl() so that it corresponds to the connection property name
     **/
    public String getSecondaryURL()
    {
        return getSecondaryUrl();
    }
     

    /**
    *  Returns the name of the i5/OS system.
    *  @return The system name.
    **/
    public String getServerName()
    {
        return as400_.getSystemName();
    }


    // @j1 new method
    /**
    *  Returns the level of tracing started on the JDBC server job.
    *  If tracing is enabled, tracing is started when
    *  the client connects to the system and ends when the connection
    *  is disconnected.  Tracing must be started before connecting to
    *  the system since the client enables system tracing only at connect time.
    *  Trace data is collected in spooled files on the system.  Multiple
    *  levels of tracing can be turned on in combination by adding
    *  the constants and passing that sum on the set method.  For example,
    *  <pre>
    *  dataSource.setServerTraceCategories(AS400JDBCDataSource.SERVER_TRACE_START_DATABASE_MONITOR + AS400JDBCDataSource.SERVER_TRACE_SAVE_SERVER_JOBLOG);
    *  </pre>
    *  @return The tracing level.
    *  <p>The value is a combination of the following:
    *  <ul>
    *  <li>SERVER_TRACE_START_DATABASE_MONITOR - Start the database monitor on the JDBC server job.
    *                               The numeric value of this constant is 2.
    *  <LI>SERVER_TRACE_DEBUG_SERVER_JOB - Start debug on the JDBC server job.
    *                         The numeric value of this constant is 4.
    *  <LI>SERVER_TRACE_SAVE_SERVER_JOBLOG - Save the joblog when the JDBC server job ends.
    *                           The numeric value of this constant is 8.
    *  <LI>SERVER_TRACE_TRACE_SERVER_JOB - Start job trace on the JDBC server job.
    *                         The numeric value of this constant is 16.
    *  <LI>SERVER_TRACE_SAVE_SQL_INFORMATION - Save SQL information.
    *                             The numeric value of this constant is 32.
    *  </ul>
    *
    *  <P>
    *  Tracing the JDBC server job will use significant amounts of system resources.
    *  Additional processor resource is used to collect the data, and additional
    *  storage is used to save the data.  Turn on tracing only to debug
    *  a problem as directed by IBM service.
    *
    **/
    public int getServerTraceCategories()
    {
        return properties_.getInt(JDProperties.TRACE_SERVER);
    }

    /**
    *  Returns how the system sorts records before sending them to the 
    *  client.
    *  @return The sort value.
    *  <p>Valid values include:
    *  <ul>
    *    <li>"hex" (base the sort on hexadecimal values)
    *    <li>"language" (base the sort on the language set in the sort language property)
    *    <li> "table" (base the sort on the sort sequence table set in the sort table property)
    *  </ul>
    *  The default value is "hex".
    **/
    public String getSort()
    {
        return properties_.getString(JDProperties.SORT);
    }

    /**
    *  Returns the three-character language id to use for selection of a sort sequence.
    *  @return The three-character language id.
    *  The default value is ENU.
    **/
    public String getSortLanguage()
    {
        return properties_.getString(JDProperties.SORT_LANGUAGE);
    }

    /**
    *  Returns the library and file name of a sort sequence table stored on the
    *  system.
    *  @return The qualified sort table name.
    **/
    public String getSortTable()
    {
        return properties_.getString(JDProperties.SORT_TABLE);
    }

    /**
    *  Returns how the system treats case while sorting records.
    *  @return The sort weight.
    *  Valid values include: "shared" (upper- and lower-case characters are sorted as the
    *  same character) and "unique" (upper- and lower-case characters are sorted as
    *  different characters).  The default value is "shared".
    **/
    public String getSortWeight()
    {
        return properties_.getString(JDProperties.SORT_WEIGHT);
    }

    /**
    *  Returns the time format used in time literals with SQL statements.
    *  @return The time format.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "hms"
    *    <li> "usa"
    *    <li> "iso"
    *    <li> "eur"
    *    <li> "jis"
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default value is based on the server job.
    **/
    public String getTimeFormat()
    {
        return properties_.getString(JDProperties.TIME_FORMAT);
    }

    /**
    *  Returns the time separator used in time literals within SQL 
    *  statements.
    *  @return The time separator.
    *  <p>Valid values include:
    *  <ul>
    *    <li> ":" (colon)
    *    <li> "." (period)
    *    <li> "," (comma)
    *    <li> " " (space)
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default value is based on the server job.
    **/
    public String getTimeSeparator()
    {
        return properties_.getString(JDProperties.TIME_SEPARATOR);
    }


    /**
    *  Returns the system's transaction isolation.
    *  @return The transaction isolation level.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "none"
    *    <li> "read uncommitted"  - The default value.
    *    <li> "read committed"
    *    <li> "repeatable read"
    *    <li> "serializable"
    *  </ul>
    **/
    public String getTransactionIsolation()
    {
        return properties_.getString(JDProperties.TRANSACTION_ISOLATION);
    }


    // @J3 No change needeadd code here.  UID already properly serialized
    /**
    *  Returns the database user property.
    *  @return The user.
    **/
    public String getUser()
    {
        return as400_.getUserId();
    }

    // @K3A
    /**
    *  Returns the QAQQINI library name.
    *  @return The QAQQINI library name.
    **/
    public String getQaqqiniLibrary()
    {
        return properties_.getString(JDProperties.QAQQINILIB);
    }
    
    
    //@dup
    /**
     *  Returns the QAQQINI library name.
     *  @return The QAQQINI library name.
     *  Note:  this method is the same as getQaqqiniLibrary() so that it corresponds to the connection property name
     **/
    public String getQaqqinilib()
    {
        return getQaqqiniLibrary();
    }
     

    //@540
    /**                                                               
    *  Returns the goal the i5/OS system should use with optimization of queries.  
    *  @return the goal the i5/OS system should use with optimization of queries.
    *  <p>Valid values include:
    *  <ul>
    *  <li>0 = Optimize query for first block of data (*ALLIO) when extended dynamic packages are used; Optimize query for entire result set (*FIRSTIO) when packages are not used</li>
    *  <li>1 = Optimize query for first block of data (*FIRSTIO)</li>
    *  <li>2 = Optimize query for entire result set (*ALLIO) </li>
    *  </ul>
    *  The default value is 0.
    **/
    public int getQueryOptimizeGoal()
    {
        return properties_.getInt(JDProperties.QUERY_OPTIMIZE_GOAL);
    }

    //@550
    /**
    * Returns the storage limit in megabytes, that should be used for statements executing a query in a connection.
    * Note, this setting is ignored when running to V5R4 i5/OS or earlier
    * <p> Valid values are -1 to MAX_STORAGE_LIMIT megabytes.  
    * The default value is -1 meaning there is no limit.
    **/
    public int getQueryStorageLimit()
    {
        return properties_.getInt(JDProperties.QUERY_STORAGE_LIMIT);
    }

    //@540
    /**                                                               
    *  Indicates whether lock sharing is allowed for loosely coupled transaction branches.
    *  @return the lock sharing setting.
    *  <p>Valid values include:
    *  <ul>
    *  <li>0 = Locks cannot be shared</li>
    *  <li>1 = Locks can be shared</li>
    *  </ul>
    *  The default value is 0.
    **/
    public int getXALooselyCoupledSupport()
    {
        return properties_.getInt(JDProperties.XA_LOOSELY_COUPLED_SUPPORT);
    }

    /**
    *  Initializes the transient data for object de-serialization.
    **/
    private void initializeTransient()
    {
        changes_ = new PropertyChangeSupport(this);

        if (isSecure_)            //@B4A  
            as400_ = new SecureAS400();         //@B4A
        else                     //@B4A
            as400_ = new AS400();

        // Reinitialize the serverName, user, password, keyRingName, etc.
        if (serialServerName_ != null)
            setServerName(serialServerName_);

        if (serialUserName_ != null)
        {                                                               // @J3a
            setUser(serialUserName_);

            if ((serialPWBytes_ != null) &&                             // @J3a
                (serialPWBytes_.length > 0))                            // @J3a
            {                                                           // @J3a
                as400_.setPassword(xpwDeconfuse(serialPWBytes_));        // @J3a
            }                                                           // @J3a
        }

        try
        {
            if (serialKeyRingName_ != null && isSecure_)                  //@B4A
            {                                                             //@J3a
                if ((serialKeyRingPWBytes_ != null) &&                    //@J3a      
                    (serialKeyRingPWBytes_.length > 0))                   //@J3a      
                {                                                         //@J3a
                    String keyRingPassword = xpwDeconfuse(serialKeyRingPWBytes_);  // @J3a
                    ((SecureAS400)as400_).setKeyRingName(serialKeyRingName_, keyRingPassword); //@J3A
                }                                                            //@J3a
                else
                {                                                         //@J3a                                                            //@J3a
                    ((SecureAS400)as400_).setKeyRingName(serialKeyRingName_); //@B4A
                }                                                            //@J3a
            }                                                                //@J3a
        }
        catch (PropertyVetoException pve)
        { /* Will never happen */
        }

        // @J4 Make sure the prompt flag is correctly de-serialized.  The problem was
        //     the flag would get serialized with the rest of the properties 
        //     (in the properties_ object), but the flag would never be applied
        //     to the AS400 object when de-serialzed.  De-serialization puts the
        //     flag back in properties_ but that does no good unless the value
        //     is passed on to the AS400 object.  That is what the new code does. 
        //     There is no affect on normal "new" objects since at the time this 
        //     method is called properties_ is null.
        try
        {                                                           //@J4A                                                             //@J4A
            if (properties_ != null)                                   //@J4A
                if (!isPrompt())                                        //@J4A
                    as400_.setGuiAvailable(false);                       //@J4A
        }                                                             //@J4A
        catch (PropertyVetoException pve)                             //@J4A
        { /* Will never happen */                                     //@J4A
        }                                                             //@J4A

    }

    //@KBA
    /**
    *  Indicates whether true auto commit support is used.
    *  @return true if true auto commit support is used; false otherwise.
    *  The default value is false.
    **/
    public boolean isTrueAutoCommit()
    {
        return properties_.getBoolean(JDProperties.AUTO_COMMIT);
    }
    

    //@dup
    /**
     *  Indicates whether true auto commit support is used.
     *  @return true if true auto commit support is used; false otherwise.
     *  The default value is false.
     *  Note:  this method is the same as isTrueAutoCommit() so that it corresponds to the connection property name
     **/
    public boolean isTrueAutocommit()
    {
        return isTrueAutoCommit();
    }

    //@K54
    /**
    *  Indicates whether variable-length fields are compressed.
    *  @return true if variable-length fields are compressed; false otherwise.
    *  The default value is true.
    **/
    public boolean isVariableFieldCompression()
    {
        return properties_.getBoolean(JDProperties.VARIABLE_FIELD_COMPRESSION);
    }

    //@CE1
    /**
     *  Returns whether commit throws SQLException when autocommit is enabled.
     *  @return Autocommit Exception.
     *  The default value is false.
     **/
     public boolean isAutocommitException()
     {
         return properties_.getBoolean(JDProperties.AUTOCOMMIT_EXCEPTION);
     }
     
    //@K24
    /**
    *  Indicates whether bidi implicit reordering is used.
    *  @return true if bidi implicit reordering is used; false otherwise.
    *  The default value is true.
    **/
    public boolean isBidiImplicitReordering()
    {
        return properties_.getBoolean(JDProperties.BIDI_IMPLICIT_REORDERING);
    }

    //@K24
    /**
    *  Indicates whether bidi numeric ordering round trip is used.
    *  @return true if bidi numeric ordering round trip is used; false otherwise.
    *  The default value is false.
    **/
    public boolean isBidiNumericOrdering()
    {
        return properties_.getBoolean(JDProperties.BIDI_NUMERIC_ORDERING);
    }

    /**
    *  Indicates whether a big decimal value is returned.
    *  @return true if a big decimal is returned; false otherwise.
    *  The default value is true.
    **/
    public boolean isBigDecimal()
    {
        return properties_.getBoolean(JDProperties.BIG_DECIMAL);
    }

    /**
    *  Indicates whether the cursor is held.
    *  @return true if the cursor is held; false otherwise.
    *  The default value is true.
    **/
    public boolean isCursorHold()
    {
        return properties_.getBoolean(JDProperties.CURSOR_HOLD);
    }

    /**
    *  Indicates whether data compression is used.
    *  @return true if data compression is used; false otherwise.
    *  The default value is true.
    **/
    public boolean isDataCompression()
    {
        return properties_.getBoolean(JDProperties.DATA_COMPRESSION);
    }

    /**
    *  Indicates whether data truncation is used.
    *  @return true if data truncation is used; false otherwise.
    *  The default value is true.
    **/
    public boolean isDataTruncation()
    {
        return properties_.getBoolean(JDProperties.DATA_TRUNCATION);
    }

    /**
    *  Indicates whether extended dynamic support is used.  Extended dynamic
    *  support provides a mechanism for caching dynamic SQL statements on
    *  the system.  The first time a particular SQL statement is prepared, it is
    *  stored in an SQL package on the system.  
    *  If the package does not exist, it will be automatically created.
    *  On subsequent prepares of the
    *  same SQL statement, the system can skip a significant part of the
    *  processing by using information stored in the SQL package.
    *  @return true if extended dynamic support is used; false otherwise.
    *  The default value is not to use extended dynamic support.
    **/
    public boolean isExtendedDynamic()
    {
        return properties_.getBoolean(JDProperties.EXTENDED_DYNAMIC);
    }


    // @C3A
    /**
    *  Indicates whether the driver should request extended metadata from the
    *  i5/OS system.  If this property is set to true, the accuracy of the information 
    *  that is returned from ResultSetMetaData methods getColumnLabel(int),
    *  isReadOnly(int), isSearchable(int), and isWriteable(int) will be increased.
    *  In addition, the ResultSetMetaData method getSchemaName(int) will be supported with this 
    *  property set to true.  However, performance will be slower with this 
    *  property on.  Leave this property set to its default (false) unless you
    *  need more specific information from those methods.
    *
    *  For example, without this property turned on, isSearchable(int) will 
    *  always return true even though the correct answer may be false because 
    *  the driver does not have enough information from the system to make a judgment.  Setting 
    *  this property to true forces the driver to get the correct data from the i5/OS system.
    *
    *  @return true if extended metadata will be requested; false otherwise.
    *  The default value is false.
    **/

    public boolean isExtendedMetaData()
    {
        return properties_.getBoolean(JDProperties.EXTENDED_METADATA);
    }

    
    //@dup
    /**
     *  Indicates whether the driver should request extended metadata from the
     *  i5/OS system.  If this property is set to true, the accuracy of the information 
     *  that is returned from ResultSetMetaData methods getColumnLabel(int),
     *  isReadOnly(int), isSearchable(int), and isWriteable(int) will be increased.
     *  In addition, the ResultSetMetaData method getSchemaName(int) will be supported with this 
     *  property set to true.  However, performance will be slower with this 
     *  property on.  Leave this property set to its default (false) unless you
     *  need more specific information from those methods.
     *
     *  For example, without this property turned on, isSearchable(int) will 
     *  always return true even though the correct answer may be false because 
     *  the driver does not have enough information from the system to make a judgment.  Setting 
     *  this property to true forces the driver to get the correct data from the i5/OS system.
     *
     *  @return true if extended metadata will be requested; false otherwise.
     *  The default value is false.
     *  Note:  this method is the same as isExtendedMetaData() so that it corresponds to the connection property name
     **/

    public boolean isExtendedMetadata()
    {
        return isExtendedMetaData();
    }


    // @W1a
    /**
    *  Indicates whether the i5/OS system fully opens a file when performing a query.
    *  By default the system optimizes opens so they perform better.  In
    *  certain cases an optimized open will fail.  In some
    *  cases a query will fail when a database performance monitor
    *  is turned on even though the same query works with the monitor
    *  turned off.  In this case set the full open property to true.
    *  This disables optimization on the system.
    *  @return true if files are fully opened; false otherwise.
    *  The default value is false.
    **/
    public boolean isFullOpen()
    {
        return properties_.getBoolean(JDProperties.FULL_OPEN);
    }

    // @A1A
    /**
    *  Indicates whether to delay closing cursors until subsequent requests.
    *  @return true to delay closing cursors until subsequent requests; false otherwise.
    *  The default value is false.
    **/
    public boolean isLazyClose()
    {
        return properties_.getBoolean(JDProperties.LAZY_CLOSE);
    }

    //@KBL
    /**
    *  Indicates whether input locators are of type hold.
    *  @return true if input locators are of type hold; false otherwise.
    *  The default value is true.
    **/
    public boolean isHoldInputLocators()
    {
        return properties_.getBoolean(JDProperties.HOLD_LOCATORS);
    }

    /**
    *  Indicates whether to add newly prepared statements to the   
    *  SQL package specified on the "package" property.  This property
    *  has no effect unless the extended dynamic property is set to true;
    *  @return true If newly prepared statements should be added to the SQL package specified 
    *  on the "package" property; false otherwise.
    *  The default value is true.
    **/
    public boolean isPackageAdd()
    {
        return properties_.getBoolean(JDProperties.PACKAGE_ADD);
    }

    /**
    *  Indicates whether a subset of the SQL package information is cached in client memory.  
    *  Caching SQL packages locally
    *  reduces the amount of communication to the i5/OS system for prepares and describes.  This
    *  property has no effect unless the extended dynamic property is set to true.
    *  @return true if caching is used; false otherwise.
    *  The defalut value is false.
    **/
    public boolean isPackageCache()
    {
        return properties_.getBoolean(JDProperties.PACKAGE_CACHE);
    }

    //@C6D Deprecated method.
    /**
    *  Indicates whether SQL packages are cleared when they become full.  This method
    *  has been deprecated.  Package clearing and the decision for the 
    *  threshold where package clearing is needed is now handled
    *  automatically by the database.  
    *  @return Always false.  This method is deprecated.
    *  @deprecated
    **/
    public boolean isPackageClear()
    {
        //@C6D return properties_.getBoolean(JDProperties.PACKAGE_CLEAR);
        return false;  //@C6A
    }

    /**
    *  Indicates whether data is prefetched upon executing a SELECT statement.
    *  This will increase performance when accessing the initial rows in the result set.
    *  @return If prefetch is used; false otherwise.
    *  The default value is prefetch data.
    **/
    public boolean isPrefetch()
    {
        return properties_.getBoolean(JDProperties.PREFETCH);
    }

    /**
    *  Indicates whether the user is prompted if a user name or password is
    *  needed to connect to the i5/OS system.  If a connection can not be made
    *  without prompting the user, and this property is set to false, then an
    *  attempt to connect will fail throwing an exception.
    *  @return true if the user is prompted for signon information; false otherwise.
    *  The default value is false.
    **/
    public boolean isPrompt()
    {
        return properties_.getBoolean(JDProperties.PROMPT);
    }

    //@K94
    /**
    *  Indicates whether the cursor is held after a rollback.
    *  @return true if the cursor is held; false otherwise.
    *  The default value is false.
    **/
    public boolean isRollbackCursorHold()
    {
        return properties_.getBoolean(JDProperties.ROLLBACK_CURSOR_HOLD);
    }

    //@KBL
    /**
    *  Indicates whether statements remain open until a transaction boundary when autocommit is off and they
    *  are associated with Lob locators.
    *  @return true if statements are only closed at transaction boundaries; false otherwise.
    *  The default value is false.
    **/
    public boolean isHoldStatements()
    {
        return properties_.getBoolean(JDProperties.HOLD_STATEMENTS);
    }

    // @J3 new method
    /**
    *  Indicates whether the password is saved locally with the rest of
    *  the properties when this data source object is serialized.
    *  <P>
    *  If the password is saved, it is up to the application to protect
    *  the serialized form of the object because it contains all necessary
    *  information to connect to the i5/OS system.  The default is false.  It
    *  is a security risk to save the password with the rest of the
    *  properties so by default the password is not saved.  If the programmer
    *  chooses to accept this risk, call setSavePasswordWhenSerialized(true)
    *  to force the Toolbox to save the password with the other properties
    *  when the data source object is serialized.   
    *  @return true if the password is saved with the rest of the properties when the
    *          data source object is serialized; false otherwise.
    *  The default value is false.
    **/
    public boolean isSavePasswordWhenSerialized()
    {
        return savePasswordWhenSerialized_;
    }





    /**
    *  Indicates whether a Secure Socket Layer (SSL) connection is used to communicate
    *  with the i5/OS system.  SSL connections are only available when connecting to systems
    *  at V4R4 or later.
    *  @return true if Secure Socket Layer connection is used; false otherwise.
    *  The default value is false.
    **/
    public boolean isSecure()
    {
        return properties_.getBoolean(JDProperties.SECURE);
    }


    /**
    *  Indicates whether a thread is used.
    *  @return true if a thread is used; false otherwise.
    *  The default value is true.
    **/
    public boolean isThreadUsed()
    {
        return properties_.getBoolean(JDProperties.THREAD_USED);
    }

    /**
    *  Indicates whether trace messages should be logged.
    *  @return true if trace message are logged; false otherwise.
    *  The default value is false.
    **/
    public boolean isTrace()
    {
        return properties_.getBoolean(JDProperties.TRACE);
    }

    /**
    *  Indicates whether binary data is translated.  If this property is set
    *  to true, then BINARY and VARBINARY fields are treated as CHAR and
    *  VARCHAR fields.
    *  @return true if binary data is translated; false otherwise.
    *  The default value is false.
    **/
    public boolean isTranslateBinary()
    {
        return properties_.getBoolean(JDProperties.TRANSLATE_BINARY);
    }

    //@PDA
    /**
    *  Indicates how Boolean objects are interpreted when setting the value 
    *  for a character field/parameter using the PreparedStatement.setObject(), 
    *  CallableStatement.setObject() or ResultSet.updateObject() methods.  Setting the 
    *  property to "true", would store the Boolean object in the character field as either 
    *  "true" or "false".  Setting the property to "false", would store the Boolean object 
    *  in the character field as either "1" or "0".
    *  @return true if boolean data is translated; false otherwise.
    *  The default value is true.
    **/
    public boolean isTranslateBoolean()
    {
        return properties_.getBoolean(JDProperties.TRANSLATE_BOOLEAN);
    }
     
    /**
    *  Logs a message to the event log.
    *  @param message The message to log.
    **/
    void log(String message)
    {
        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, message);

        if (log_ != null)
            log_.log(message);
    }

    /**
    *  Logs an exception and message to the event log.
    *  @param property The property to log.
    *  @param value The property value to log.
    **/
    private void logProperty(String property, String value)
    {
        if (Trace.isTraceOn())
            JDTrace.logProperty (this, property, value);

        //@A8D if (log_ != null)
        //@A8D log_.log(property + ": " + value);
    }

    /**
    *  Deserializes and initializes transient data.
    *  @exception ClassNotFoundException If the class cannot be found.
    *  @exception IOException If an I/O exception occurs.
    **/
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        in.defaultReadObject();
        initializeTransient();
    }

    /**
    *  Removes the PropertyChangeListener.
    *  If the PropertyChangeListener is not in the list, nothing is done.
    *  @param listener The PropertyChangeListener.
    *  @see #addPropertyChangeListener
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        changes_.removePropertyChangeListener(listener);

        as400_.removePropertyChangeListener(listener);                 //@K1C  changed to removePropertyChangeListener instead of addPropertyChangeListener
    }

    /**
    *  Sets the level of database access for the connection.
    *  @param access The access level.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "all" (all SQL statements allowed)
    *    <li> "read call" (SELECT and CALL statements allowed)
    *    <li> "read only" (SELECT statements only)
    *  </ul>
    *  The default value is "all".
    **/
    public void setAccess(String access)
    {
        String property = "access";

        if (access == null)
            throw new NullPointerException(property);
        validateProperty(property, access, JDProperties.ACCESS);

        String old = getAccess();
        properties_.setString(JDProperties.ACCESS, access);

        changes_.firePropertyChange(property, old, access);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + access);  //@A8C
    }

    //@CE1
    /**
     *  Sets whether commit throws SQLException when autocommit is enabled.
     *  @param value
     *  The default value is false.
     **/
     public void setAutocommitException(boolean value)
     {
         String property = "autocommitException";
         Boolean oldValue = new Boolean(isAutocommitException());
         Boolean newValue = new Boolean(value);

         if (value)
             properties_.setString(JDProperties.AUTOCOMMIT_EXCEPTION, TRUE_);
         else
             properties_.setString(JDProperties.AUTOCOMMIT_EXCEPTION, FALSE_);

         changes_.firePropertyChange(property, oldValue, newValue);

         if (JDTrace.isTraceOn()) 
             JDTrace.logInformation (this, property + ": " + value);   
     }
     
    //@KBA
    /**
    *  Sets whether true auto commit support is used.
    *  @param value true if true auto commit support should be used; false otherwise.
    *  The default value is false.
    **/
    public void setTrueAutoCommit(boolean value)
    {
        String property = "trueAutoCommit";
        Boolean oldValue = new Boolean(isTrueAutoCommit());
        Boolean newValue = new Boolean(value);

        if (value)
            properties_.setString(JDProperties.AUTO_COMMIT, TRUE_);
        else
            properties_.setString(JDProperties.AUTO_COMMIT, FALSE_);

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + value);      
    }


    //@dup
    /**
     *  Sets whether true auto commit support is used.
     *  @param value true if true auto commit support should be used; false otherwise.
     *  The default value is false.
     *  Note:  this method is the same as setTrueAutoCommit() so that it corresponds to the connection property nameproperty name
     **/
    public void setTrueAutocommit(boolean value)
    {
        setTrueAutoCommit(value); 
    }


    // @C9 new method
    /**                                                               
    *  Sets the Toolbox JDBC Driver behaviors to override.  Multiple
    *  behaviors can be changed in combination by adding
    *  the constants and passing that sum on the this method. 
    *  @param behaviors The driver behaviors to override.
    *  <p>Valid values include:
    *  <ul>
    *  <li>1 - Do not throw an exception if Statement.executeQuery() or
    *          PreparedStatement.executeQuery() do not return a result set.
    *          Instead, return null for the result set.
    *  </ul>
    *
    *  Carefully consider the result of overriding the default behavior of the
    *  driver.  For example, setting the value of this property to 1 means
    *  the driver will no longer throw an exception even though the JDBC 3.0
    *  specification states throwing an exception is the correct behavior.  
    *  Be sure your application correctly handles the altered behavior.  
    *
    **/
    public void setBehaviorOverride(int behaviors)
    {
        String property = "behaviorOverride";

        Integer oldValue = new Integer(getBehaviorOverride());
        Integer newValue = new Integer(behaviors);

        properties_.setString(JDProperties.BEHAVIOR_OVERRIDE, newValue.toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + behaviors);
    }




    //@B2A
    /**
     *  Sets the output string type of bidi data, as defined by the CDRA (Character Data
     *  Representation Architecture). See <a href="BidiStringType.html">
     *  BidiStringType</a> for more information and valid values.
     **/
    public void setBidiStringType(int bidiStringType)                          //@B3C
    {
        String property = "bidiStringType";                                             //@B3C

        //@B3D if (bidiStringType == null)
        //@B3D    throw new NullPointerException(property);
        Integer oldBidiStringType = new Integer(getBidiStringType());         //@B3A
        Integer newBidiStringType = new Integer(bidiStringType);              //@B3A

        validateProperty(property, newBidiStringType.toString(), JDProperties.BIDI_STRING_TYPE); //@B3C

        properties_.setString(JDProperties.BIDI_STRING_TYPE, newBidiStringType.toString());   //@B3C

        changes_.firePropertyChange(property, oldBidiStringType, newBidiStringType);  //@B3C

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, property + ": " + bidiStringType);
    }

    //@K24
    /**
    *  Sets whether bidi implicit reordering is used.
    *  @param value true if implicit reordering should be used; false otherwise.
    *  The default value is true.
    **/
    public void setBidiImplicitReordering(boolean value)
    {
        String property = "bidiImplicitReordering";
        Boolean oldValue = new Boolean(isBidiImplicitReordering());
        Boolean newValue = new Boolean(value);

        if (value)
            properties_.setString(JDProperties.BIDI_IMPLICIT_REORDERING, TRUE_);
        else
            properties_.setString(JDProperties.BIDI_IMPLICIT_REORDERING, FALSE_);

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + value);      
    }

    //@K24
    /**
    *  Sets whether bidi numeric ordering round trip is used.
    *  @param value true if numeric ordering round trip should be used; false otherwise.
    *  The default value is false.
    **/
    public void setBidiNumericOrdering(boolean value)
    {
        String property = "bidiNumericOrdering";
        Boolean oldValue = new Boolean(isBidiNumericOrdering());
        Boolean newValue = new Boolean(value);

        if (value)
            properties_.setString(JDProperties.BIDI_NUMERIC_ORDERING, TRUE_);
        else
            properties_.setString(JDProperties.BIDI_NUMERIC_ORDERING, FALSE_);

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + value);      
    }

    /**
    *  Sets whether a big decimal value is returned.
    *  @param value true if a big decimal is returned; false otherwise.
    *  The default value is true.
    **/
    public void setBigDecimal(boolean value)
    {
        String property = "bigDecimal";
        Boolean oldValue = new Boolean(isBigDecimal());
        Boolean newValue = new Boolean(value);

        if (value)
            properties_.setString(JDProperties.BIG_DECIMAL, TRUE_);
        else
            properties_.setString(JDProperties.BIG_DECIMAL, FALSE_);

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + value);      //@A8C
    }

    /**
    *  Sets the criteria for retrieving data from the i5/OS system in
    *  blocks of records.  Specifying a non-zero value for this property
    *  will reduce the frequency of communication to the system, and
    *  therefore increase performance.
    *  @param blockCriteria The block criteria.
    *  <p>Valid values include:
    *  <ul>
    *    <li> 0 (no record blocking)
    *    <li> 1 (block if FOR FETCH ONLY is specified)
    *    <li> 2 (block if FOR UPDATE is specified) - The default value.
    *  </ul>
    **/
    public void setBlockCriteria(int blockCriteria)
    {
        String property = "blockCriteria";
        Integer oldCriteria = new Integer(getBlockCriteria());
        Integer newCriteria = new Integer(blockCriteria);

        validateProperty(property, newCriteria.toString(), JDProperties.BLOCK_CRITERIA);

        properties_.setString(JDProperties.BLOCK_CRITERIA, newCriteria.toString());
        changes_.firePropertyChange(property, oldCriteria, newCriteria);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + blockCriteria);   //@A8C
    }

    /**
    *  Sets the block size in kilobytes to retrieve from the i5/OS system and
    *  cache on the client.  This property has no effect unless the block criteria
    *  property is non-zero.  Larger block sizes reduce the frequency of
    *  communication to the system, and therefore may increase performance.
    *  @param blockSize The block size in kilobytes.
    *  <p>Valid values include:
    *  <ul>
    *    <li> 0
    *    <li> 8
    *    <li> 16
    *    <li> 32  - The default value.
    *    <li> 64
    *    <li> 128
    *    <li> 256
    *    <li> 512
    *  </ul>
    **/
    public void setBlockSize(int blockSize)
    {
        String property = "blockSize";

        Integer oldBlockSize = new Integer(getBlockSize());
        Integer newBlockSize = new Integer(blockSize);

        validateProperty(property, newBlockSize.toString(), JDProperties.BLOCK_SIZE);

        properties_.setString(JDProperties.BLOCK_SIZE, new Integer(blockSize).toString());
        changes_.firePropertyChange(property, oldBlockSize, newBlockSize);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + blockSize);  //@A8C
    }


    //@C8A
    /**
    *  Sets the cursor sensitivity to be requested from the database.  If the resultSetType is 
    *  ResultSet.TYPE_FORWARD_ONLY or ResultSet.TYPE_SCROLL_SENSITIVE, the value of this property
    *  will control what cursor sensitivity is requested from the database.  If the resultSetType
    *  is ResultSet.TYPE_SCROLL_INSENSITIVE, this property will be ignored.
    *
    *  <p>Valid values include:
    *  <ul>
    *    <li> "asensitive"
    *    <li> "insensitive"
    *    <li> "sensitive"
    *  </ul>
    *  The default is "asensitive".
    *
    *  This property is ignored when connecting to systems
    *  running OS/400 V5R1 and earlier. 
    **/
    public void setCursorSensitivity(String cursorSensitivity)
    {
        String property = "cursorSensitivity";

        String oldCursorSensitivity = getCursorSensitivity();
        String newCursorSensitivity = cursorSensitivity;

        validateProperty(property, newCursorSensitivity, JDProperties.CURSOR_SENSITIVITY);

        properties_.setString(JDProperties.CURSOR_SENSITIVITY, cursorSensitivity);
        changes_.firePropertyChange(property, oldCursorSensitivity, newCursorSensitivity);

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, property + ": " + cursorSensitivity);
    }


    /**
    *  Sets whether the cursor is held.
    *  @param cursorHold true if the cursor is held; false otherwise.  The default value is true.
    **/
    public void setCursorHold(boolean cursorHold)
    {
        String property = "cursorHold";
        Boolean oldHold = new Boolean(isCursorHold());
        Boolean newHold = new Boolean(cursorHold);

        if (cursorHold)
            properties_.setString(JDProperties.CURSOR_HOLD, TRUE_);
        else
            properties_.setString(JDProperties.CURSOR_HOLD, FALSE_);

        changes_.firePropertyChange(property, oldHold, newHold);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + cursorHold);  //@A8C
    }

    /**
    *  Sets the database name.  
    *  This property is ignored when connecting to systems
    *  running OS/400 V5R1 and earlier.  
    *  If a database name is specified it must exist in the relational 
    *  database directory on the system.  Use CL command WRKRDBDIRE 
    *  to view the directory.
    *  The following criteria are used to determine
    *  which database is accessed:  
    *  <OL>
    *  <LI>If a database name is specified, that database is used.  Attempts
    *      to connect will fail if the database does not exist.
    *  <LI>If special value *SYSBAS is specified, the system default database is used.
    *  <LI>If a database name is not specified, the database specified
    *      in the job description for the user profile is used.
    *  <LI>If a database name is not specified and a database is not specified
    *      in the job description for the user profile, the system default
    *      database is used.   
    *  </OL>
    *  @param databaseName The database name or *SYSBAS.
    **/
    public void setDatabaseName(String databaseName)
    {
        String property = DATABASE_NAME;

        if (databaseName == null)
            throw new NullPointerException(property);

        String old = getDatabaseName();

        // @J2d databaseName_ = databaseName;
        // @J2d changes_.firePropertyChange(property, old, databaseName);
        // @J2d logProperty("database", databaseName_);

        properties_.setString(JDProperties.DATABASE_NAME, databaseName);      // @J2a
        changes_.firePropertyChange(property, old, databaseName);             // @J2a
                                                                              // @J2a
        if (JDTrace.isTraceOn())                                              // @J2a
            JDTrace.logInformation (this, property + ": " + databaseName);   // @J2a 
    }

    /**
    *  Sets whether to use data compression.  The default value is true.
    *  @param compression true if data compression is used; false otherwise.
    **/
    public void setDataCompression(boolean compression)
    {
        Boolean oldCompression = new Boolean(isDataCompression());
        Boolean newCompression = new Boolean(compression);

        if (compression)
            properties_.setString(JDProperties.DATA_COMPRESSION, TRUE_);
        else
            properties_.setString(JDProperties.DATA_COMPRESSION, FALSE_);

        changes_.firePropertyChange("dataCompression", oldCompression, newCompression);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "dataCompression: " + compression);  //@A8C
    }

    /**
    *  Sets the data source name.
    *  This property can be used for connection pooling implementations.
    *  @param dataSourceName The data source name.
    **/
    public void setDataSourceName(String dataSourceName)
    {
        String property = DATASOURCE_NAME;

        if (dataSourceName == null)
            throw new NullPointerException(property);

        String old = getDataSourceName();

        dataSourceName_ = dataSourceName;

        changes_.firePropertyChange(property, old, dataSourceName);

        logProperty("dataSource", dataSourceName_);
    }

    /**
    *  Sets whether to use data truncation.  The default value is true.
    *  @param truncation true if data truncation is used; false otherwise.
    **/
    public void setDataTruncation(boolean truncation)
    {
        Boolean oldTruncation = new Boolean(isDataTruncation());
        Boolean newTruncation = new Boolean(truncation);

        if (truncation)
            properties_.setString(JDProperties.DATA_TRUNCATION, TRUE_);
        else
            properties_.setString(JDProperties.DATA_TRUNCATION, FALSE_);

        changes_.firePropertyChange("dataTruncation", oldTruncation, newTruncation);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "dataTruncation: " + truncation);   //@A8C
    }

    /**
    *  Sets the date format used in date literals within SQL statements.
    *  @param dateFormat The date format.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "mdy"
    *    <li> "dmy"
    *    <li> "ymd"
    *    <li> "usa"
    *    <li> "iso"
    *    <li> "eur"
    *    <li> "jis"
    *    <li> "julian"
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default is based on the server job.
    **/
    public void setDateFormat(String dateFormat)
    {
        String property = "dateFormat";

        if (dateFormat == null)
            throw new NullPointerException(property);
        validateProperty(property, dateFormat, JDProperties.DATE_FORMAT);

        String old = getDateFormat();

        properties_.setString(JDProperties.DATE_FORMAT, dateFormat);

        changes_.firePropertyChange(property, old, dateFormat);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + dateFormat);  //@A8C
    }

    /**
    *  Sets the date separator used in date literals within SQL statements.
    *  This property has no effect unless the "data format" property is set to:
    *  "julian", "mdy", "dmy", or "ymd".
    *  @param dateSeparator The date separator.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "/" (slash)
    *    <li> "-" (dash)
    *    <li> "." (period)
    *    <li> "," (comma)
    *    <li> " " (space)
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default value is based on the server job.
    **/
    public void setDateSeparator(String dateSeparator)
    {
        String property = "dateSeparator";
        if (dateSeparator == null)
            throw new NullPointerException(property);
        validateProperty(property, dateSeparator, JDProperties.DATE_SEPARATOR);

        String old = getDateSeparator();

        properties_.setString(JDProperties.DATE_SEPARATOR, dateSeparator);

        changes_.firePropertyChange(property, old, dateSeparator);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + dateSeparator);   //@A8C
    }

    //@DFA
    /**
    *  Sets the decfloat rounding mode.
    *  @param decfloatRoundingMode The decfloat rounding mode.
    *   <p>Valid values include:
    *   <ul>
    *   <li>"half even" - default
    *   <li>"half up" 
    *   <li>"down" 
    *   <li>"ceiling" 
    *   <li>"floor" 
    *   <li>"half down" 
    *   <li>"up" 
    *   </ul>
    **/
    public void setDecfloatRoundingMode(String decfloatRoundingMode)
    {
        String property = "decfloatRoundingMode";
        if (decfloatRoundingMode == null)
            throw new NullPointerException(property);
        validateProperty(property, decfloatRoundingMode, JDProperties.DECFLOAT_ROUNDING_MODE);

        String old = getDecfloatRoundingMode();

        properties_.setString(JDProperties.DECFLOAT_ROUNDING_MODE, decfloatRoundingMode);

        changes_.firePropertyChange(property, old, decfloatRoundingMode);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + decfloatRoundingMode);
    }
     
    /**
    *  Sets the decimal separator used in numeric literals within SQL 
    *  statements.
    *  @param decimalSeparator The decimal separator.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "." (period)
    *    <li> "," (comma)
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default value is based on the server job.
    **/
    public void setDecimalSeparator(String decimalSeparator)
    {
        String property = "decimalSeparator";
        if (decimalSeparator == null)
            throw new NullPointerException(property);
        validateProperty(property, decimalSeparator, JDProperties.DECIMAL_SEPARATOR);

        String old = getDecimalSeparator();

        properties_.setString(JDProperties.DECIMAL_SEPARATOR, decimalSeparator);

        changes_.firePropertyChange(property, old, decimalSeparator);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + decimalSeparator);    //@A8C
    }

    /**
    *  Sets the data source description.
    *  @param description The description.
    **/
    public void setDescription(String description)
    {
        String property = DESCRIPTION;
        if (description == null)
            throw new NullPointerException(property);

        String old = getDescription();

        description_ = description;

        changes_.firePropertyChange(property, old, description);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + description);  //@A8C
    }

    /**
    *  Sets how the i5/OS system sorts records before sending them to the client.
    *  @param sort The sort value.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "hex" (base the sort on hexadecimal values)
    *    <li> "language" (base the sort on the language set in the sort language property)
    *    <li> "table" (base the sort on the sort sequence table set in the sort table property).
    *  </ul>
    *  The default value is "hex".
    **/
    public void setSort(String sort)
    {
        String property = "sort";
        if (sort == null)
            throw new NullPointerException(property);

        //@JOB fix to allow "sort=job" but use default value
        if(sort.equals("job"))                 //@JOB
        {                                      //@JOB
            if (JDTrace.isTraceOn())           //@JOB
                JDTrace.logInformation (this, property + ": " + getSort() + " (warning: " + getSort() + " will be used since sort=job is not valid)");  //@JOB 
            return; //return and allow default setting to be used                                                  //@JOB
        }                                     //@JOB

        
        validateProperty(property, sort, JDProperties.SORT);
        String old = getSort();

        properties_.setString(JDProperties.SORT, sort);

        changes_.firePropertyChange(property, old, sort);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + sort); //@A8C
    }

    /**
    *  Sets the amount of detail to be returned in the message for errors
    *  occurring on the i5/OS system.
    *  @param errors The error message level.
    *  Valid values include: "basic" and "full".  The default value is "basic".
    **/
    public void setErrors(String errors)
    {
        String property = "errors";
        if (errors == null)
            throw new NullPointerException(property);
        validateProperty(property, errors, JDProperties.ERRORS);

        String old = getErrors();
        properties_.setString(JDProperties.ERRORS, errors);

        changes_.firePropertyChange(property, old, errors);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + errors);  //@A8C
    }

    /**
    *  Sets whether to use extended dynamic support.  Extended dynamic
    *  support provides a mechanism for caching dynamic SQL statements on
    *  the i5/OS system.  The first time a particular SQL statement is prepared, it is
    *  stored in an SQL package on the system.  
    *  If the package does not exist, it will be automatically created.
    *  On subsequent prepares of the
    *  same SQL statement, the system can skip a significant part of the
    *  processing by using information stored in the SQL package.  If this
    *  is set to "true", then a package name must be set using the "package"
    *  property.
    *  @param extendedDynamic If extended dynamic support is used; false otherwise.
    *  The default value is not to use extended dynamic support.
    **/
    public void setExtendedDynamic(boolean extendedDynamic)
    {
        Boolean oldValue = new Boolean(isExtendedDynamic());
        Boolean newValue = new Boolean(extendedDynamic);

        if (extendedDynamic)
            properties_.setString(JDProperties.EXTENDED_DYNAMIC, TRUE_);
        else
            properties_.setString(JDProperties.EXTENDED_DYNAMIC, FALSE_);

        changes_.firePropertyChange("extendedDynamic", oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "extendedDynamic: " + extendedDynamic);  //@A8C
    }

    // @C3A
    /**
    *  Sets whether the driver should request extended metadata from the
    *  i5/OS system.  This property is ignored when connecting to systems
    *  running OS/400 V5R1 and earlier. 
    *  If this property is set to true and connecting to a system running
    *  OS/400 V5R2 or i5/OS, the accuracy of the information 
    *  that is returned from ResultSetMetaData methods getColumnLabel(int),
    *  isReadOnly(int), isSearchable(int), and isWriteable(int) will be increased.
    *  In addition, the ResultSetMetaData method getSchemaName(int) will be supported with this 
    *  property set to true.  However, performance will be slower with this 
    *  property on.  Leave this property set to its default (false) unless you
    *  need more specific information from those methods.
    *
    *  For example, without this property turned on, isSearchable(int) will 
    *  always return true even though the correct answer may be false because 
    *  the driver does not have enough information from the system to make a judgment.  Setting 
    *  this property to true forces the driver to get the correct data from the system.
    *
    *  @param extendedMetaData True to request extended metadata from the system, false otherwise.
    *  The default value is false.
    **/
    public void setExtendedMetaData(boolean extendedMetaData)
    {
        Boolean oldValue = new Boolean(isExtendedMetaData());
        Boolean newValue = new Boolean(extendedMetaData);

        if (extendedMetaData)
            properties_.setString(JDProperties.EXTENDED_METADATA, TRUE_);
        else
            properties_.setString(JDProperties.EXTENDED_METADATA, FALSE_);

        changes_.firePropertyChange("extendedMetaData", oldValue, newValue);

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "extendedMetaData: " + extendedMetaData);
    }
    

    //@dup
    /**
     *  Sets whether the driver should request extended metadata from the
     *  i5/OS system.  This property is ignored when connecting to systems
     *  running OS/400 V5R1 and earlier. 
     *  If this property is set to true and connecting to a system running
     *  OS/400 V5R2 or i5/OS, the accuracy of the information 
     *  that is returned from ResultSetMetaData methods getColumnLabel(int),
     *  isReadOnly(int), isSearchable(int), and isWriteable(int) will be increased.
     *  In addition, the ResultSetMetaData method getSchemaName(int) will be supported with this 
     *  property set to true.  However, performance will be slower with this 
     *  property on.  Leave this property set to its default (false) unless you
     *  need more specific information from those methods.
     *
     *  For example, without this property turned on, isSearchable(int) will 
     *  always return true even though the correct answer may be false because 
     *  the driver does not have enough information from the system to make a judgment.  Setting 
     *  this property to true forces the driver to get the correct data from the system.
     *
     *  @param extendedMetaData True to request extended metadata from the system, false otherwise.
     *  The default value is false.
     *  Note:  this method is the same as setExtendedMetaData() so that it corresponds to the connection property name
     **/
    public void setExtendedMetadata(boolean extendedMetaData)
    {
        setExtendedMetaData(extendedMetaData);
    }



    // @W1a new method
    /**
    *  Sets whether to fully open a file when performing a query.
    *  By default the i5/OS system optimizes opens so they perform better.
    *  In most cases optimization functions correctly and improves
    *  performance.  Running a query repeatedly
    *  when a database performance monitor is turned on may fail
    *  because of the optimization, however.
    *  Leave this property set to its default (false) until
    *  you experience errors running queries with monitors
    *  turned on.  At that time set the property to true which
    *  will disable the optimization.
    *  @param fullOpen True to fully open a file (turn off optimizations), false
    *          to allow optimizations.  The default value is false.
    **/
    public void setFullOpen(boolean fullOpen)
    {
        Boolean oldValue = new Boolean(isFullOpen());
        Boolean newValue = new Boolean(fullOpen);

        if (fullOpen)
            properties_.setString(JDProperties.FULL_OPEN, TRUE_);
        else
            properties_.setString(JDProperties.FULL_OPEN, FALSE_);

        changes_.firePropertyChange("fullOpen", oldValue, newValue);

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "fullOpen: " + fullOpen);
    }

    //@KBL
    /**
    *  Sets whether input locators are allocated as hold locators.
    *  @param value true if locators should be allocated as hold locators; false otherwise.
    *  The default value is true.
    **/
    public void setHoldInputLocators(boolean value)
    {
        String property = "holdInputLocators";
        Boolean oldValue = new Boolean(isHoldInputLocators());
        Boolean newValue = new Boolean(value);

        if (value)
            properties_.setString(JDProperties.HOLD_LOCATORS, TRUE_);
        else
            properties_.setString(JDProperties.HOLD_LOCATORS, FALSE_);

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + value);      
    }   
       
    //@KBL
    /**
    *  Sets whether statements should remain open until a transaction boundary when autocommit is off
    *  and they are associated with Lob locators.
    *  @param value true if statements should remain open; false otherwise.
    *  The default value is false.
    **/
    public void setHoldStatements(boolean value)
    {
        String property = "holdStatements";
        Boolean oldValue = new Boolean(isHoldStatements());
        Boolean newValue = new Boolean(value);

        if (value)
            properties_.setString(JDProperties.HOLD_STATEMENTS, TRUE_);
        else
            properties_.setString(JDProperties.HOLD_STATEMENTS, FALSE_);

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + value);      
    }

    // @A1A
    /**
    *  Sets whether to delay closing cursors until subsequent requests.
    *  @param lazyClose true to delay closing cursors until subsequent requests; false otherwise.
               The default value is false.
    **/
    public void setLazyClose(boolean lazyClose)
    {
        Boolean oldValue = new Boolean(isLazyClose());
        Boolean newValue = new Boolean(lazyClose);

        if (lazyClose)
            properties_.setString(JDProperties.LAZY_CLOSE, TRUE_);
        else
            properties_.setString(JDProperties.LAZY_CLOSE, FALSE_);

        changes_.firePropertyChange("lazyClose", oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "lazyClose: " + lazyClose);  //@A8C
    }

    /**
    *  Sets the libraries to add to the server job's library list.
    *  The libraries are delimited by commas or spaces, and
    *  "*LIBL" may be used as a place holder for the server job's
    *  current library list.  The library list is used for resolving
    *  unqualified stored procedure calls and finding schemas in
    *  DatabaseMetaData catalog methods.  If "*LIBL" is not specified,
    *  the specified libraries will replace the server job's
    *  current library list.
    *  @param libraries The library list.
    **/
    public void setLibraries(String libraries)
    {
        String property = "libraries";
        if (libraries == null)
            throw new NullPointerException("libraries");

        String old = getLibraries();
        properties_.setString(JDProperties.LIBRARIES, libraries);

        changes_.firePropertyChange(property, old, libraries);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + libraries);  //@A8C
    }

    /**
    *  Sets the maximum LOB (large object) size in bytes that
    *  can be retrieved as part of a result set.  LOBs that are larger
    *  than this threshold will be retrieved in pieces using extra
    *  communication to the i5/OS system.  Larger LOB thresholds will reduce
    *  the frequency of communication to the system, but will download
    *  more LOB data, even if it is not used.  Smaller LOB thresholds may
    *  increase frequency of communication to the system, but will only
    *  download LOB data as it is needed.
    *
    *  @param threshold The lob threshold.  Valid range is 0-16777216.
    *  The default value is 32768.
    **/
    public void setLobThreshold(int threshold)
    {
        String property = "threshold";
        if (threshold < 0 || threshold > MAX_THRESHOLD)
            throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        Integer oldValue = new Integer(getLobThreshold());
        Integer newValue = new Integer(threshold);

        properties_.setString(JDProperties.LOB_THRESHOLD, new Integer(threshold).toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + threshold);  //@A8C
    }

    /**
    *  Sets the maximum time in seconds that this data source can wait while attempting to connect to a database.
    *  A value of zero specifies that the timeout is the system default if one exists; otherwise it specifies that
    *  there is no timeout. The default value is initially zero.
    *  @param timeout The login timeout in seconds.
    **/
    public void setLoginTimeout(int timeout) throws SQLException
    {
        //This sets the socket timeout
        setSoTimeout(timeout * 1000);                                                   //@K5A  setSoTimeout takes milliseconds as a parameter
        String property = "loginTimeout";                                               //@K5A

        Integer oldValue = new Integer(getLoginTimeout());                              //@K5A
        Integer newValue = new Integer(timeout);                                        //@K5A

        properties_.setString(JDProperties.LOGIN_TIMEOUT, newValue.toString());         //@K5A

        changes_.firePropertyChange(property, oldValue, newValue);                      //@K5A

        if (JDTrace.isTraceOn())                                                        //@K5A
            JDTrace.logInformation (this, property + ": " + timeout);                   //@K5A

        //@K5D JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    }

    /**
    *  Sets the log writer for this data source.
    *  @param writer The log writer; to disable, set to null.
    *  @exception SQLException If a database error occurs.
    **/
    public void setLogWriter(PrintWriter writer) throws SQLException
    {
        String property = "writer";

        //@C4D if (writer == null)
        //@C4D    throw new NullPointerException(property);

        PrintWriter old = getLogWriter();
        writer_ = writer;
        changes_.firePropertyChange(property, old, writer);

        if (writer == null)         //@C4A
        {                           //@C4A
            log_ = null;            //@C4A
            return;                 //@C4A
        }                           //@C4A

        log_ = new EventLog(writer);
    }

    //@PDA
    /**                                                               
    *  Sets how to retrieve DatabaseMetaData.
    *  If set to 0, database metadata will be retrieved through the ROI data flow.  
    *  If set to 1, database metadata will be retrieved by calling system stored procedures. 
    *  The methods that currently are available through stored procedures are:
    *  getColumnPrivileges
    *  @param mds The setting for metadata source
    *  The default value is 1.
    **/
    public void setMetaDataSource(int mds)
    {
        String property = "metaDataSource";

        Integer oldValue = new Integer(getMetaDataSource());
        Integer newValue = new Integer(mds);

        properties_.setString(JDProperties.METADATA_SOURCE, newValue.toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + mds);
    }
    
    //@dup
    /**                                                               
     *  Sets how to retrieve DatabaseMetaData.
     *  If set to 0, database metadata will be retrieved through the ROI data flow.  
     *  If set to 1, database metadata will be retrieved by calling system stored procedures. 
     *  The methods that currently are available through stored procedures are:
     *  getColumnPrivileges
     *  @param mds The setting for metadata source
     *  The default value is 1.
     *  Note:  this method is the same as setMetadataSource() so that it corresponds to the connection property name
     **/
    public void setMetadataSource(int mds)
    {
        setMetaDataSource(mds);
    }
    
    /**
    *  Sets the naming convention used when referring to tables.
    *  @param naming The naming convention.  Valid values include: "sql" (e.g. schema.table)
    *  and "system" (e.g. schema/table).  The default value is "sql".
    **/
    public void setNaming(String naming)
    {
        String property = "naming";
        if (naming == null)
            throw new NullPointerException("naming");
        validateProperty(property, naming, JDProperties.NAMING);

        String old = getNaming();
        properties_.setString(JDProperties.NAMING, naming);

        changes_.firePropertyChange(property, old, naming);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + naming);  //@A8C
    }

    /**
    *  Sets the base name of the SQL package.  Note that only the
    *  first seven characters are used to generate the name of the SQL package on the i5/OS system.  
    *  This property has no effect unless
    *  the extended dynamic property is set to true.  In addition, this property
    *  must be set if the extended dynamic property is set to true.
    *  @param packageName The base name of the SQL package.
    **/
    public void setPackage(String packageName)
    {
        String property = "packageName";
        if (packageName == null)
            throw new NullPointerException(property);

        String old = getPackage();
        properties_.setString(JDProperties.PACKAGE, packageName);

        changes_.firePropertyChange(property, old, packageName);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + packageName);  //@A8C
    }

    /**
    *  Sets whether to add newly prepared statements to the SQL package 
    *  specified on the "package" property.  This property
    *  has no effect unless the extended dynamic property is set to true.
    *  @param add If newly prepared statements should be added to the SQL package specified on 
    *  the "package" property; false otherwise.
    *  The default value is true.
    **/
    public void setPackageAdd(boolean add)
    {
        Boolean oldValue = new Boolean(isPackageAdd());
        Boolean newValue = new Boolean(add);

        if (add)
            properties_.setString(JDProperties.PACKAGE_ADD, TRUE_);
        else
            properties_.setString(JDProperties.PACKAGE_ADD, FALSE_);

        changes_.firePropertyChange("packageAdd", oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "packageAdd: " + add);  //@A8C
    }

    /**
    *  Sets whether to cache a subset of the SQL package information in client memory.  
    *  Caching SQL packages locally
    *  reduces the amount of communication to the i5/OS system for prepares and describes.  This
    *  property has no effect unless the extended dynamic property is set to true.
    *  @param cache If caching is used; false otherwise.  The default value is false.
    **/
    public void setPackageCache(boolean cache)
    {
        Boolean oldValue = new Boolean(isPackageCache());
        Boolean newValue = new Boolean(cache);

        if (cache)
            properties_.setString(JDProperties.PACKAGE_CACHE, TRUE_);
        else
            properties_.setString(JDProperties.PACKAGE_CACHE, FALSE_);

        changes_.firePropertyChange("packageCache", oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "packageCache: " + cache);  //@A8C
    }


    //@C6C Changed javadoc since package clearing is now done automatically
    //@C6C by the database.
    /**
    *  Sets whether to clear SQL packages when they become full.  This method
    *  has been deprecated.  Package clearing and the decision for the 
    *  threshold where package clearing is needed is now handled
    *  automatically by the database.  
    *  @param clear If the SQL package are cleared when full; false otherwise.
    *  @deprecated
    **/
    public void setPackageClear(boolean clear)
    {
        //@C6D Package clearing and the decision for the 
        //@C6D threshold where package clearing is needed is now handled
        //@C6D automatically by the database.

        //@C6D Boolean oldValue = new Boolean(isPackageClear());
        //@C6D Boolean newValue = new Boolean(clear);

        //@C6D String value = null;
        //@C6D if (clear)
        //@C6D     properties_.setString(JDProperties.PACKAGE_CLEAR, TRUE_);
        //@C6D else
        //@C6D     properties_.setString(JDProperties.PACKAGE_CLEAR, FALSE_);

        //@C6D changes_.firePropertyChange("packageClear", oldValue, newValue);

        //@C6D if (JDTrace.isTraceOn()) //@A8C
        //@C6D     JDTrace.logInformation (this, "packageClear: " + clear);  //@A8C
    }


    /**
    *  Sets the type of SQL statement to be stored in the SQL package.  This can
    *  be useful to improve the performance of complex join conditions.  This
    *  property has no effect unless the extended dynamic property is set to true.
    *  @param packageCriteria The type of SQL statement.
    *  Valid values include: "default" (only store SQL statements with parameter
    *  markers in the package), and "select" (store all SQL SELECT statements
    *  in the package).  The default value is "default".
    **/
    public void setPackageCriteria(String packageCriteria)
    {
        String property = "packageCriteria";

        if (packageCriteria == null)
            throw new NullPointerException(property);
        validateProperty(property, packageCriteria, JDProperties.PACKAGE_CRITERIA);

        String old = getPackageCriteria();
        properties_.setString(JDProperties.PACKAGE_CRITERIA, packageCriteria);

        changes_.firePropertyChange(property, old, packageCriteria);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + packageCriteria);  //@A8C
    }

    /**
    *  Sets the action to take when SQL package errors occur.  When an SQL package
    *  error occurs, the driver will optionally throw an SQLException or post a
    *  warning to the Connection, based on the value of this property.  This property
    *  has no effect unless the extended dynamic property is set to true.
    *  @param packageError The action when SQL errors occur.
    *  Valid values include: "exception", "warning", and "none".  The default value is "warning".
    **/
    public void setPackageError(String packageError)
    {
        String property = "packageError";
        if (packageError == null)
            throw new NullPointerException(property);
        validateProperty(property, packageError, JDProperties.PACKAGE_ERROR);

        String old = getPackageError();
        properties_.setString(JDProperties.PACKAGE_ERROR, packageError);

        changes_.firePropertyChange(property, old, packageError);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + packageError);   //@A8C
    }
    /**
    *  Sets the library for the SQL package.  This property has no effect unless
    *  the extended dynamic property is set to true.
    *  @param packageLibrary The SQL package library.  The default library is "QGPL".
    **/
    public void setPackageLibrary(String packageLibrary)
    {
        String property = "packageLibrary";
        if (packageLibrary == null)
            throw new NullPointerException(property);

        String old = getPackageLibrary();
        properties_.setString(JDProperties.PACKAGE_LIBRARY, packageLibrary);

        changes_.firePropertyChange(property, old, packageLibrary);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + packageLibrary);  //@A8C
    }

    /**
    *  Sets the database password.
    *  @param password The password.
    **/
    public void setPassword(String password)
    {
        as400_.setPassword(password);
        serialPWBytes_ = xpwConfuse(password);                  //@J3a
        log(loader_.getText("AS400_JDBC_DS_PASSWORD_SET"));     //@A9C
    }

    /**
    *  Sets whether to prefetch data upon executing a SELECT statement.
    *  This will increase performance when accessing the initial rows in the result set.
    *  @param prefetch If prefetch is used; false otherwise.
    *  The default value is to prefectch data.
    **/
    public void setPrefetch(boolean prefetch)
    {
        Boolean oldValue = new Boolean(isPrefetch());
        Boolean newValue = new Boolean(prefetch);

        if (prefetch)
            properties_.setString(JDProperties.PREFETCH, TRUE_);
        else
            properties_.setString(JDProperties.PREFETCH, FALSE_);

        changes_.firePropertyChange("prefetch", oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "prefetch: " + prefetch);      //@A8C
    }

    /**
    *  Sets whether the user should be prompted if a user name or password is
    *  needed to connect to the i5/OS system.  If a connection can not be made
    *  without prompting the user, and this property is set to false, then an
    *  attempt to connect will fail.
    *  @param prompt true if the user is prompted for signon information; false otherwise.
    *  The default value is false.
    **/
    public void setPrompt(boolean prompt)
    {
        Boolean oldValue = new Boolean(isPrompt());
        Boolean newValue = new Boolean(prompt);

        if (prompt)
            properties_.setString(JDProperties.PROMPT, TRUE_);
        else
            properties_.setString(JDProperties.PROMPT, FALSE_);

        try
        {                                     //@C2A  
            as400_.setGuiAvailable(prompt);   //@C2A
        }                                     //@C2A
        catch (PropertyVetoException vp)      //@C2A
        { /* ignore */                        //@C2A
        }                                     //@C2A

        changes_.firePropertyChange("prompt", oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "prompt: " + prompt);     //@A8C
    }

    // @F0D - Removed unused method
    ///**
    //*  Sets the JDBC properties.
    //*  @param Properties The JDBC properties list.
    //**/
    //void setProperties(Reference reference)
    //{
    //    /*
    //    *  Implementation note:  This method is called from AS400JDBCObjectFactory.getObjectInstance
    //    */
    //    if (reference == null)
    //        throw new NullPointerException("reference");
    // 
    //    Properties properties = new Properties();
    //
    //    Enumeration list = reference.getAll();
    //    while (list.hasMoreElements())
    //    {
    //        StringRefAddr refAddr = (StringRefAddr)list.nextElement();
    //        String property = refAddr.getType();
    //        String value = (String)reference.get(property).getContent();
    //
    //        if (property.equals(DATABASE_NAME))                         // constant identifiers were used to store in JNDI.
    //            setDatabaseName(value);
    //        else if (property.equals(DATASOURCE_NAME))
    //            setDataSourceName(value);
    //        else if (property.equals(DESCRIPTION))
    //            setDescription(value);
    //        else if (property.equals(SERVER_NAME))
    //            setServerName(value);
    //        else if (property.equals(USER))
    //            setUser(value);
    //        else if (property.equals(PASSWORD)) {
    //            // get the password back from the serialized char[]
    //            serialPWBytes_ = value.toCharArray();
    //            // decode the password and set it on the as400
    //            as400_.setPassword(xpwDeconfuse(serialPWBytes_));
    //        }
    //        else if (property.equals(KEY_RING_NAME)) {
    //            // set the key ring name
    //            serialKeyRingName_ = value;
    //        }
    //        else if (property.equals(KEY_RING_PASSWORD)) {
    //            // get the key ring password back from the serialized char[]
    //            if (value != null)
    //                serialKeyRingPWBytes_ = value.toCharArray();
    //        }
    //        else if (property.equals(SECURE)) {
    //            // set the isSecure_ flag
    //            isSecure_ = value.equals(TRUE_) ? true : false;
    //        }
    //        else if (property.equals(SAVE_PASSWORD)) {
    //            // set the savePasswordWhenSerialized_ flag
    //            savePasswordWhenSerialized_ = value.equals(TRUE_) ? true : false;
    //        }
    //        else
    //        {
    //            properties.put(property, value);
    //        }
    //    }
    //    properties_ = new JDProperties(properties, null);
    //
    //    // get the prompt property and set it back in the as400 object
    //    String prmpt = properties_.getString(JDProperties.PROMPT);
    //    if (prmpt != null && prmpt.equalsIgnoreCase(FALSE_))
    //        setPrompt(false);
    //    else if (prmpt != null && prmpt.equalsIgnoreCase(TRUE_))
    //        setPrompt(true);
    //
    //    // if the system is secure create a SecureAS400 object
    //    if (isSecure_) {
    //        try
    //        {
    //            as400_ = new SecureAS400(as400_);
    //            ((SecureAS400)as400_).setKeyRingName(serialKeyRingName_, xpwDeconfuse(serialKeyRingPWBytes_));
    //        }
    //        catch (PropertyVetoException pe)
    //        { /* will never happen */
    //        }
    //    }
    //}
    
    //@PDA
    /**
     * Sets the properties based on ";" delimited string of properties, in same
     * fashion as URL properties specified with
     * DriverManager.getConnection(urlProperties). This method simply parses
     * property string and then calls setPropertes(Properties). This method is
     * intended as an enhancement so that the user does not have to write new
     * code to call the setters for new/deleted properties.
     * 
     * @param propertiesString list of ";" delimited properties
     */
    public void setProperties(String propertiesString)
    {
        //use existing JDDatasourceURL to parse properties string like Connection does
        //but first have to add dummy protocol so we can re-use parsing code
        propertiesString = "jdbc:as400://dummyhost;" + propertiesString;
        JDDataSourceURL dsURL = new JDDataSourceURL(propertiesString);
        //returns only properties specified in propertyString.. (none of
        // JDProperties defaults)
        Properties properties = dsURL.getProperties();
        setProperties(properties);
    }

    //@PDA
    /**
     * Sets the properties for this datasource. This method is intended as an
     * enhancement so that the user does not have to write new code to call the
     * setters for new/deleted properties.
     * 
     * @param newProperties object containing updated property values
     */
    public void setProperties(Properties newProperties)
    {
        //1. turn on/off tracing per new props
        //2. set needed AS400JDBCDataSource instance variables
        //3. set socket props
        //4. propagate newProperties to existing properties_ object

        // Check first thing to see if the trace property is
        // turned on. This way we can trace everything, including
        // the important stuff like loading the properties.

        // If trace property was set to true, turn on tracing. If trace property
        // was set to false,
        // turn off tracing. If trace property was not set, do not change.
        if (JDProperties.isTraceSet(newProperties, null) == JDProperties.TRACE_SET_ON)
        {
            if (!JDTrace.isTraceOn())
                JDTrace.setTraceOn(true);
        } else if (JDProperties.isTraceSet(newProperties, null) == JDProperties.TRACE_SET_OFF)
        {
            if (JDTrace.isTraceOn())
                JDTrace.setTraceOn(false);
        }

        // If toolbox trace is set to datastream. Turn on datastream tracing.
        if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_DATASTREAM)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceDatastreamOn(true);
        }
        // If toolbox trace is set to diagnostic. Turn on diagnostic tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_DIAGNOSTIC)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceDiagnosticOn(true);
        }
        // If toolbox trace is set to error. Turn on error tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_ERROR)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceErrorOn(true);
        }
        // If toolbox trace is set to information. Turn on information tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_INFORMATION)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceInformationOn(true);
        }
        // If toolbox trace is set to warning. Turn on warning tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_WARNING)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceWarningOn(true);
        }
        // If toolbox trace is set to conversion. Turn on conversion tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_CONVERSION)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceConversionOn(true);
        }
        // If toolbox trace is set to proxy. Turn on proxy tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_PROXY)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceProxyOn(true);
        }
        // If toolbox trace is set to pcml. Turn on pcml tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_PCML)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTracePCMLOn(true);
        }
        // If toolbox trace is set to jdbc. Turn on jdbc tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_JDBC)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceJDBCOn(true);
        }
        // If toolbox trace is set to all. Turn on tracing for all categories.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_ALL)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceAllOn(true);
        }
        // If toolbox trace is set to thread. Turn on thread tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_THREAD)
        {
            if (!Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            Trace.setTraceThreadOn(true);
        }
        // If toolbox trace is set to none. Turn off tracing.
        else if (JDProperties.isToolboxTraceSet(newProperties, null) == JDProperties.TRACE_TOOLBOX_NONE)
        {
            if (Trace.isTraceOn())
            {
                Trace.setTraceOn(false);
            }
        }

        //next we need to set instance vars (via setX() methods)
        //or setup socket properties or set in properties_
        //Note: this is similar to AS400JDBCDataSource(Reference reference)logic

        Enumeration e = newProperties.keys();
        while (e.hasMoreElements())
        {
            String propertyName = (String) e.nextElement();
            String propertyValue = (String) newProperties.getProperty(propertyName);

            int propIndex = JDProperties.getPropertyIndex(propertyName);

            //some of the setter methods also set the properties_ below
            if (propIndex == JDProperties.DATABASE_NAME)
                setDatabaseName(propertyValue);
            else if (propIndex == JDProperties.USER)
                setUser(propertyValue);
            else if (propIndex == JDProperties.PASSWORD)
                setPassword(properties_.getString(JDProperties.PASSWORD));
            else if (propIndex == JDProperties.SECURE)
                setSecure(propertyValue.equals(TRUE_) ? true : false);
            else if (propIndex == JDProperties.KEEP_ALIVE)
                setKeepAlive(propertyValue.equals(TRUE_) ? true : false);
            else if (propIndex == JDProperties.RECEIVE_BUFFER_SIZE)
                setReceiveBufferSize(Integer.parseInt(propertyValue));
            else if (propIndex == JDProperties.SEND_BUFFER_SIZE)
                setSendBufferSize(Integer.parseInt(propertyValue));
            else if (propIndex == JDProperties.PROMPT)
                setPrompt(propertyValue.equals(TRUE_) ? true : false);
            else if (propIndex == JDProperties.KEY_RING_NAME){
                //at this time, decided to not allow this due to security and fact that there is no setKeyRingName() method
                if (JDTrace.isTraceOn())
                    JDTrace.logInformation(this, "Property: " + propertyName + " can only be changed in AS400JDBCDataSource constructor");  
            } else if (propIndex == JDProperties.KEY_RING_PASSWORD){
                //at this time, decided to not allow this due to security and fact that there is no setKeyRingPassword() method
                if (JDTrace.isTraceOn())
                    JDTrace.logInformation(this, "Property: " + propertyName + " can only be changed in AS400JDBCDataSource constructor");  
            } else if (propIndex != -1)
            {
                properties_.setString(propIndex, propertyValue);
            }
        } 

    }
    
    /**
    *  Sets the name of the proxy server.
    *  @param proxyServer The proxy server.
    **/
    public void setProxyServer(String proxyServer)
    {
        String property = "proxyServer";
        if (proxyServer == null)
            throw new NullPointerException(property);

        String old = getProxyServer();
        properties_.setString(JDProperties.PROXY_SERVER, proxyServer);

        changes_.firePropertyChange(property, old, proxyServer);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + proxyServer);  //@A8C
    }

    /**
    *  Sets the source of the text for REMARKS columns in ResultSets returned
    *  by DatabaseMetaData methods.
    *  @param remarks The text source.
    *  Valid values include: "sql" (SQL object comment) and "system" (OS/400 or i5/OS object description).
    *  The default value is "system".
    **/
    public void setRemarks(String remarks)
    {
        String property = "remarks";
        if (remarks == null)
            throw new NullPointerException(remarks);
        validateProperty(property, remarks, JDProperties.REMARKS);

        String old = getRemarks();
        properties_.setString(JDProperties.REMARKS, remarks);

        changes_.firePropertyChange(property, old, remarks);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + remarks);  //@A8C
    }

    //@K94
    /**
    *  Sets whether the cursor is held after a rollback.
    *  @param cursorHold true if the cursor is held; false otherwise.  The default value is false.
    **/
    public void setRollbackCursorHold(boolean cursorHold)
    {
        String property = "rollbackCursorHold";
        Boolean oldHold = new Boolean(isRollbackCursorHold());
        Boolean newHold = new Boolean(cursorHold);

        if (cursorHold)
            properties_.setString(JDProperties.ROLLBACK_CURSOR_HOLD, TRUE_);
        else
            properties_.setString(JDProperties.ROLLBACK_CURSOR_HOLD, FALSE_);

        changes_.firePropertyChange(property, oldHold, newHold);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + cursorHold);  
    }

    /**
    *  Sets the secondary URL to be used for a connection on the middle-tier's
    *  DriverManager in a multiple tier environment, if it is different than
    *  already specified.  This property allows you to use this driver to connect
    *  to databases other than DB2 for i5/OS. Use a backslash as an escape character
    *  before backslashes and semicolons in the URL.
    *  @param url The secondary URL.
    **/
    public void setSecondaryUrl(String url)
    {
        if (url == null)
            throw new NullPointerException("url");

        String old = getSecondaryUrl();
        properties_.setString(JDProperties.SECONDARY_URL, url);

        changes_.firePropertyChange("secondaryUrl", old, url);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "secondaryUrl: " + url); //@A8C
    }

    
    //@dup
    /**
     *  Sets the secondary URL to be used for a connection on the middle-tier's
     *  DriverManager in a multiple tier environment, if it is different than
     *  already specified.  This property allows you to use this driver to connect
     *  to databases other than DB2 for i5/OS. Use a backslash as an escape character
     *  before backslashes and semicolons in the URL.
     *  @param url The secondary URL.
     *  Note:  this method is the same as setSecondaryUrl() so that it corresponds to the connection property name
     **/
    public void setSecondaryURL(String url)
    {
        setSecondaryUrl(url);
    }
    
    
    /**
    *  Sets whether a Secure Socket Layer (SSL) connection is used to communicate
    *  with the i5/OS system.  SSL connections are only available when connecting to systems
    *  at V4R4 or later.
    *  @param secure true if Secure Socket Layer connection is used; false otherwise.
    *  The default value is false.
    **/
    public void setSecure(boolean secure)
    {
        Boolean oldValue = new Boolean(isSecure());
        Boolean newValue = new Boolean(secure);

        //Do not allow user to change to not secure if they constructed the data source with 
        //a keyring.
        if (!secure && isSecure_)                //@C2A
        {                                        //@C2A
            throw new ExtendedIllegalStateException("secure", 
                                                    ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);  //@C2A
        }                                        //@C2A

        // keep away the secure flag  // @F0A
        isSecure_ = secure;           // @F0A

        if (secure)
            properties_.setString(JDProperties.SECURE, TRUE_);
        else
            properties_.setString(JDProperties.SECURE, FALSE_);

        changes_.firePropertyChange("secure", oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "secure: " + secure);     //@A8C
    }

    /**
    *  Sets the i5/OS system name.
    *  @param serverName The system name.
    **/
    public void setServerName(String serverName)
    {
        String property = SERVER_NAME;
        if (serverName == null)
            throw new NullPointerException(property);

        String old = getServerName();
        
        // keep away the name to serialize    // @F0A
        serialServerName_ = serverName;       // @F0A

        try
        {
            as400_.setSystemName(serverName);
        }
        catch (PropertyVetoException pv)
        { /* ignore */
        }

        changes_.firePropertyChange(property, old, serverName);

        logProperty ("server name", as400_.getSystemName());
    }


    // @j1 new method
    /**
    *  Enables tracing of the JDBC server job.
    *  If tracing is enabled, tracing is started when
    *  the client connects to the i5/OS system, and ends when the connection
    *  is disconnected.  Tracing must be started before connecting to
    *  the system since the client enables tracing only at connect time.
    *
    *  <P>
    *  Trace data is collected in spooled files on the system.  Multiple
    *  levels of tracing can be turned on in combination by adding
    *  the constants and passing that sum on the set method.  For example,
    *  <pre>
    *  dataSource.setServerTraceCategories(AS400JDBCDataSource.SERVER_TRACE_START_DATABASE_MONITOR + AS400JDBCDataSource.SERVER_TRACE_SAVE_SERVER_JOBLOG);
    *  </pre>
    *  @param traceCategories level of tracing to start.
    *  <p>Valid values include:
    *  <ul>
    *  <li>SERVER_TRACE_START_DATABASE_MONITOR - Start the database monitor on the JDBC server job.
    *                               The numeric value of this constant is 2.
    *  <LI>SERVER_TRACE_DEBUG_SERVER_JOB - Start debug on the JDBC server job.
    *                         The numeric value of this constant is 4.
    *  <LI>SERVER_TRACE_SAVE_SERVER_JOBLOG - Save the joblog when the JDBC server job ends.
    *                           The numeric value of this constant is 8.
    *  <LI>SERVER_TRACE_TRACE_SERVER_JOB - Start job trace on the JDBC server job.
    *                         The numeric value of this constant is 16.
    *  <LI>SERVER_TRACE_SAVE_SQL_INFORMATION - Save SQL information.
    *                             The numeric value of this constant is 32.
    *  </ul>
    *  <P>
    *  Tracing the JDBC server job will use significant amounts of system resources.
    *  Additional processor resource is used to collect the data, and additional
    *  storage is used to save the data.  Turn on tracing only to debug
    *  a problem as directed by IBM service.
    *
    *
    **/
    public void setServerTraceCategories(int traceCategories)
    {
        String property = "serverTrace";

        Integer oldValue = new Integer(getServerTraceCategories());
        Integer newValue = new Integer(traceCategories);

        properties_.setString(JDProperties.TRACE_SERVER, newValue.toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + traceCategories);
    }




    // @A2A
    /**
    * Sets the JDBC driver implementation.
    * This property has no
    * effect if the "secondary URL" property is set.
    * This property cannot be set to "native" if the
    * environment is not an OS/400 or i5/OS Java Virtual
    * Machine.
    * param driver The driver value.
    *  <p>Valid values include:
    *  <ul>
    *  <li>"toolbox" (use the IBM Toolbox for Java JDBC driver)
    *  <li>"native" (use the IBM Developer Kit for Java JDBC driver)
    *  </ul>
    *  The default value is "toolbox".
    **/
    public void setDriver(String driver)
    {
        String property = "driver";
        if (driver == null)
            throw new NullPointerException(property);

        validateProperty(property, driver, JDProperties.DRIVER);
        String old = getDriver();

        properties_.setString(JDProperties.DRIVER, driver);

        changes_.firePropertyChange(property, old, driver);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + driver);  //@A8C
    }

    // @J3 new method
    /**
    *  Sets whether to save the password locally with the rest of the properties when 
    *  this data source object is serialized.
    *  <P>  
    *  If the password is saved, it is up to the application to protect
    *  the serialized form of the object because it contains all necessary
    *  information to connect to the i5/OS system.  The default is false.  It
    *  is a security risk to save the password with the rest of the
    *  properties so by default the password is not saved.  If the application
    *  programmer chooses to accept this risk, set this property to true
    *  to force the Toolbox to save the password with the other properties
    *  when the data source object is serialized.  
    *
    *  @param savePassword true if the password is saved; false otherwise.
    *  The default value is false
    **/
    public void setSavePasswordWhenSerialized(boolean savePassword)
    {                                             
        String property = "savePasswordWhenSerialized";            //@C5A

        boolean oldValue = isSavePasswordWhenSerialized();         //@C5A
        boolean newValue = savePassword;                           //@C5A

        savePasswordWhenSerialized_ = savePassword;                        

        changes_.firePropertyChange(property, oldValue, newValue); //@C5A

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, "save password: " + savePassword);
    }


    /**
    *  Sets the three-character language id to use for selection of a sort sequence.
    *  This property has no effect unless the sort property is set to "language".
    *  @param language The three-character language id.
    *  The default value is ENU.
    **/
    public void setSortLanguage(String language)
    {
        if (language == null)
            throw new NullPointerException("language");

        String old = getSortLanguage();
        properties_.setString(JDProperties.SORT_LANGUAGE, language);

        changes_.firePropertyChange("sortLanguage", old, language);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "sortLanguage: " + language);  //@A8C
    }

    /**
    *  Sets the library and file name of a sort sequence table stored on the
    *  i5/OS system.
    *  This property has no effect unless the sort property is set to "table".
    *  The default is an empty String ("").
    *  @param table The qualified sort table name.
    **/
    public void setSortTable(String table)
    {
        if (table == null)
            throw new NullPointerException("table");

        String old = getSortTable();
        properties_.setString(JDProperties.SORT_TABLE, table);

        changes_.firePropertyChange("sortTable", old, table);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "sortTable: " + table);  //@A8C
    }

    /**
    *  Sets how the i5/OS system treats case while sorting records.  This property 
    *  has no effect unless the sort property is set to "language".
    *  @param sortWeight The sort weight.
    *  Valid values include: "shared" (upper- and lower-case characters are sorted as the
    *  same character) and "unique" (upper- and lower-case characters are sorted as
    *  different characters).  The default value is "shared".
    **/
    public void setSortWeight(String sortWeight)
    {
        String property = "sortWeight";
        if (sortWeight == null)
            throw new NullPointerException(property);

        validateProperty(property, sortWeight, JDProperties.SORT_WEIGHT);

        String old = getSortWeight();
        properties_.setString(JDProperties.SORT_WEIGHT, sortWeight);

        changes_.firePropertyChange(property, old, sortWeight);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + sortWeight);  //@A8C
    }

    /**
    *  Sets whether a thread is used.
    *  @param threadUsed true if a thread is used; false otherwise.
    *  The default value is true.
    **/
    public void setThreadUsed(boolean threadUsed)
    {
        Boolean oldValue = new Boolean(isThreadUsed());
        Boolean newValue = new Boolean(threadUsed);

        if (threadUsed)
            properties_.setString(JDProperties.THREAD_USED, TRUE_);
        else
            properties_.setString(JDProperties.THREAD_USED, FALSE_);

        try
        {                                     
            as400_.setThreadUsed(threadUsed);                       
        }                                                            
        catch (PropertyVetoException pve)                            
        { /* Will never happen */                                    
        }
        
        changes_.firePropertyChange("threadUsed", oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "threadUsed: " + threadUsed);  //@A8C
    }

    /**
    *  Sets the time format used in time literals with SQL statements.
    *  @param timeFormat The time format.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "hms"
    *    <li> "usa"
    *    <li> "iso"
    *    <li> "eur"
    *    <li> "jis"
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default value is based on the server job.
    **/
    public void setTimeFormat(String timeFormat)
    {
        String property = "timeFormat";
        if (timeFormat == null)
            throw new NullPointerException(property);
        validateProperty(property, timeFormat, JDProperties.TIME_FORMAT);

        String old = getTimeFormat();
        properties_.setString(JDProperties.TIME_FORMAT, timeFormat);

        changes_.firePropertyChange(property, old, timeFormat);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + timeFormat);  //@A8C
    }

    /**
    *  Sets the time separator used in time literals within SQL statements.
    *  This property has no effect unless the time format property is set to "hms".
    *  @param timeSeparator The time separator.
    *  <p>Valid values include:
    *  <ul>
    *    <li> ":" (colon)
    *    <li> "." (period)
    *    <li> "," (comma)
    *    <li> " " (space)
    *    <li> ""  (server job value) - default.
    *  </ul>
    *  The default value is based on the server job.
    **/
    public void setTimeSeparator(String timeSeparator)
    {
        String property = "timeSeparator";
        if (timeSeparator == null)
            throw new NullPointerException(property);
        validateProperty(property, timeSeparator, JDProperties.TIME_SEPARATOR);

        String old = getTimeSeparator();
        properties_.setString(JDProperties.TIME_SEPARATOR, timeSeparator);

        changes_.firePropertyChange(property, old, timeSeparator);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + timeSeparator);   //@A8C
    }

    /**
    *  Sets whether trace messages should be logged.  Trace messages are
    *  useful for debugging programs that call JDBC.  However, there is a
    *  performance penalty associated with logging trace messages, so this
    *  property should only be set to true for debugging.  Trace messages
    *  are logged to System.out.
    *  @param trace true if trace message are logged; false otherwise.
    *  The default value is false.
    **/
    public void setTrace(boolean trace)
    {
        Boolean oldValue = new Boolean(isTrace());
        Boolean newValue = new Boolean(trace);

        if (trace)
            properties_.setString(JDProperties.TRACE, TRUE_);
        else
            properties_.setString(JDProperties.TRACE, FALSE_);

        changes_.firePropertyChange("trace", oldValue, newValue);

        if (trace)
        {
            if (!JDTrace.isTraceOn ())
                JDTrace.setTraceOn (true);
        }
        else
            JDTrace.setTraceOn (false);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, "trace: " + trace);  //@A8C
    }


    /**
    *  Sets the i5/OS system's transaction isolation.
    *  @param transactionIsolation The transaction isolation level.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "none"
    *    <li> "read uncommitted"  - The default value.
    *    <li> "read committed"
    *    <li> "repeatable read"
    *    <li> "serializable"
    *  </ul>
    **/
    public void setTransactionIsolation(String transactionIsolation)
    {
        String property = "transactionIsolation";

        if (transactionIsolation == null)
            throw new NullPointerException(property);
        validateProperty(property, transactionIsolation, JDProperties.TRANSACTION_ISOLATION);

        String old = getTransactionIsolation();

        properties_.setString(JDProperties.TRANSACTION_ISOLATION, transactionIsolation);

        changes_.firePropertyChange(property, old, transactionIsolation);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + transactionIsolation);     //@A8C
    }

    /**
    *  Sets whether binary data is translated.  If this property is set
    *  to true, then BINARY and VARBINARY fields are treated as CHAR and
    *  VARCHAR fields.
    *  @param translate true if binary data is translated; false otherwise.
    *  The default value is false.
    **/
    public void setTranslateBinary(boolean translate)
    {
        String property = "translateBinary";

        Boolean oldValue = new Boolean(isTranslateBinary());
        Boolean newValue = new Boolean(translate);

        if (translate)
            properties_.setString(JDProperties.TRANSLATE_BINARY, TRUE_);
        else
            properties_.setString(JDProperties.TRANSLATE_BINARY, FALSE_);

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) //@A8C
            JDTrace.logInformation (this, property + ": " + translate);  //@A8C
    }
    
    //@PDA
    /**
    *  Sets how Boolean objects are interpreted when setting the value 
    *  for a character field/parameter using the PreparedStatement.setObject(), 
    *  CallableStatement.setObject() or ResultSet.updateObject() methods.  Setting the 
    *  property to "true", would store the Boolean object in the character field as either 
    *  "true" or "false".  Setting the property to "false", would store the Boolean object 
    *  in the character field as either "1" or "0".
    *  @param translate true if boolean data is translated; false otherwise.
    *  The default value is true.
    **/
    public void setTranslateBoolean(boolean translate)
    {
        String property = "translateBoolean";

        Boolean oldValue = new Boolean(isTranslateBoolean());
        Boolean newValue = new Boolean(translate);

        if (translate)
            properties_.setString(JDProperties.TRANSLATE_BOOLEAN, TRUE_);
        else
            properties_.setString(JDProperties.TRANSLATE_BOOLEAN, FALSE_);

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + translate);
    }
    

    /**
    *  Sets the database user.
    *  @param user The user.
    **/
    public void setUser(String user)
    {
        String property = "user";

        String old = getUser();

        // save away the user to serialize    // @F0A
        serialUserName_ = user;               // @F0A

        try
        {
            as400_.setUserId(user);
        }
        catch (PropertyVetoException vp)
        { /* ignore */
        }

        changes_.firePropertyChange(property, old, user);

        logProperty ("user", as400_.getUserId());
    }

    //@K54
    /**
    *  Specifies whether variable-length fields should be compressed. 
    *  @param compress true if variable-length fields should be compressed; false otherwise.
    *  The default value is true.
    **/
    public void setVariableFieldCompression(boolean compress)
    {
        String property = "variableFieldCompression";

        Boolean oldValue = new Boolean(isVariableFieldCompression());
        Boolean newValue = new Boolean(compress);

        if (compress)
            properties_.setString(JDProperties.VARIABLE_FIELD_COMPRESSION, TRUE_);
        else
            properties_.setString(JDProperties.VARIABLE_FIELD_COMPRESSION, FALSE_);

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + compress);  
    }

    // @F1A Added the below methods to set socket options
    /**
    * Gets the socket keepalive option.
    * @return The value of the socket keepalive option.
    **/
    public boolean getKeepAlive()
    {
        return sockProps_.isKeepAlive();
    }

    /**
    * Gets the socket receive buffer size option.  NOTE: This does not get
    * the actual receive buffer size, only the option which is used as a hint
    * by the underlying socket code.
    * @return The value of the socket receive buffer size option.
    **/
    public int getReceiveBufferSize()
    {
        return sockProps_.getReceiveBufferSize();
    }

    /**
    * Gets the socket send buffer size option.  NOTE: This does not get
    * the actual send buffer size, only the option which is used as a hint
    * by the underlying socket code.
    * @return The value of the socket send buffer size option.
    **/
    public int getSendBufferSize()
    {
        return sockProps_.getSendBufferSize();
    }

    /**
    * Gets the socket linger option in seconds.
    * @return The value of the socket linger option.
    **/
    public int getSoLinger()
    {
        return sockProps_.getSoLinger();
    }

    /**
    * Gets the socket timeout option in milliseconds.
    * @return The value of the socket timeout option.
    **/
    public int getSoTimeout()
    {
        return sockProps_.getSoTimeout();
    }

    /**
    * Gets the socket TCP no delay option.
    * @return The value of the socket TCP no delay option.
    **/
    public boolean getTcpNoDelay()
    {
        return sockProps_.isTcpNoDelay();
    }

    /**
    * This property allows the turning on of socket keep alive.
    * @param keepAlive The keepalive option value.
    **/
    public void setKeepAlive(boolean keepAlive)
    {
        sockProps_.setKeepAlive(keepAlive);
    }

    /**
    * This property sets the receive buffer size socket option to the
    * specified value. The receive buffer size option is used as a hint
    * for the size to set the underlying network I/O buffers. Increasing
    * the receive buffer size can increase the performance of network
    * I/O for high-volume connection, while decreasing it can help reduce
    * the backlog of incoming data.  This value must be greater than 0.
    * @param size The socket receive buffer size option value.
    **/
    public void setReceiveBufferSize(int size)
    {
        sockProps_.setReceiveBufferSize(size);
    }

    /**
    * This property sets the send buffer size socket option to the
    * specified value. The send buffer size option is used by the
    * platform's networking code as a hint for the size to set the
    * underlying network I/O buffers.  This value must be greater
    * than 0.
    * @param size The socket send buffer size option value.
    **/
    public void setSendBufferSize(int size)
    {
        sockProps_.setSendBufferSize(size);
    }

    /**
    * This property allows the turning on of socket linger with the
    * specified linger time in seconds.  The maxium value for this
    * property is platform specific.
    * @param seconds The socket linger option value.
    **/
    public void setSoLinger(int seconds)
    {
        sockProps_.setSoLinger(seconds);
    }

    /**
    * This property enables/disables socket timeout with the
    * specified value in milliseconds.  A timeout value must be
    * greater than zero, a value of zero for this property indicates
    * infinite timeout.
    * @param milliseconds The socket timeout option value.
    **/
    public void setSoTimeout(int milliseconds)
    {
        sockProps_.setSoTimeout(milliseconds);
    }

    /**
    * This property allows the turning on of the TCP no delay socket option.
    * @param noDelay The socket TCP no delay option value.
    **/
    public void setTcpNoDelay(boolean noDelay)
    {
        sockProps_.setTcpNoDelay(noDelay);
    }
    // @F1A End of new socket option methods

    // @M0A - added support for sending statements in UTF-16 and storing them in a UTF-16 package
    /**
    * Gets the package CCSID property, which indicates the
    * CCSID in which statements are sent to the i5/OS system and
    * also the CCSID of the package they are stored in.
    * Valid values:  1200 (UCS-2) and 13488 (UTF-16).  
    * Default value: 13488
    * @return The value of the package CCSID property.
    **/
    public int getPackageCCSID()
    {
        return properties_.getInt(JDProperties.PACKAGE_CCSID);
    }
    
    //@dup
    /**
     * Gets the package CCSID property, which indicates the
     * CCSID in which statements are sent to the i5/OS system and
     * also the CCSID of the package they are stored in.
     * Valid values:  1200 (UCS-2) and 13488 (UTF-16).  
     * Default value: 13488
     * @return The value of the package CCSID property.
     * Note:  this method is the same as getPackageCCSID() so that it corresponds to the connection property name
     **/
    public int getPackageCcsid()
    {
        return getPackageCCSID();
    }

    // @M0A
    /**
    * Sets the package CCSID property, which indicates the
    * CCSID in which statements are sent to the i5/OS system and
    * also the CCSID of the package they are stored in.
    * Valid values:  1200 (UCS-2) and 13488 (UTF-16).  
    * Default value: 13488
    * @param ccsid The package CCSID.
    **/
    public void setPackageCCSID(int ccsid)
    {
        String property = "packageCCSID";

        Integer oldPackageCCSID = new Integer(getPackageCCSID());
        Integer newPackageCCSID = new Integer(ccsid);

        validateProperty(property, newPackageCCSID.toString(), JDProperties.PACKAGE_CCSID);

        properties_.setString(JDProperties.PACKAGE_CCSID, newPackageCCSID.toString());

        changes_.firePropertyChange(property, oldPackageCCSID, newPackageCCSID);

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, property + ": " + ccsid);
    }

    //@dup
    /**
     * Sets the package CCSID property, which indicates the
     * CCSID in which statements are sent to the i5/OS system and
     * also the CCSID of the package they are stored in.
     * Valid values:  1200 (UCS-2) and 13488 (UTF-16).  
     * Default value: 13488
     * @param ccsid The package CCSID.
     * Note:  this method is the same as setPackageCCSID() so that it corresponds to the connection property name
     **/
    public void setPackageCcsid(int ccsid)
    {
        setPackageCCSID(ccsid);
    }
    
    // @M0A - added support for 63 digit decimal precision
    /**
    * Gets the minimum divide scale property.  This property ensures the scale
    * of the result of decimal division is never less than its specified value.
    * Valid values: 0-9.  0 is default.
    * @return The minimum divide scale.
    **/
    public int getMinimumDivideScale()
    {
        return properties_.getInt(JDProperties.MINIMUM_DIVIDE_SCALE);
    }

    // @M0A
    /**
    * Gets the maximum precision property. This property indicates the 
    * maximum decimal precision the i5/OS system should use.
    * Valid values: 31 or 63.  31 is default.
    * @return The maximum precision.
    **/
    public int getMaximumPrecision()
    {
        return properties_.getInt(JDProperties.MAXIMUM_PRECISION);
    }

    // @M0A
    /**
    * Gets the maximum scale property.  This property indicates the
    * maximum decimal scale the i5/OS system should use.
    * Valid values: 0-63.  31 is default.
    * @return The maximum scale.
    **/
    public int getMaximumScale()
    {
        return properties_.getInt(JDProperties.MAXIMUM_SCALE);
    }

    // @M0A
    /**
    * Sets the minimum divide scale property.  This property ensures the scale
    * of the result of decimal division is never less than its specified value.
    * Valid values: 0-9.  0 is default.
    * @param scale The minimum divide scale.
    **/
    public void setMinimumDivideScale(int scale)
    {
        String property = "minimumDivideScale";

        Integer oldValue = new Integer(getMinimumDivideScale());
        Integer newValue = new Integer(scale);

        validateProperty(property, newValue.toString(), JDProperties.MINIMUM_DIVIDE_SCALE);

        properties_.setString(JDProperties.MINIMUM_DIVIDE_SCALE, newValue.toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, property + ": " + scale);
    }

    // @M0A
    /**
    * Sets the maximum precision property. This property indicates the 
    * maximum decimal precision the i5/OS system should use.
    * Valid values: 31 or 63.  31 is default.
    * @param precision The maximum precision.
    **/
    public void setMaximumPrecision(int precision)
    {
        String property = "maximumPrecision";

        Integer oldValue = new Integer(getMaximumPrecision());
        Integer newValue = new Integer(precision);

        validateProperty(property, newValue.toString(), JDProperties.MAXIMUM_PRECISION);

        properties_.setString(JDProperties.MAXIMUM_PRECISION, newValue.toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, property + ": " + precision);
    }

    // @M0A
    /**
    * Sets the maximum scale property.  This property indicates the
    * maximum decimal scale the i5/OS system should use.
    * Valid values: 0-63.  31 is default.
    * @param scale The maximum scale.
    **/
    public void setMaximumScale(int scale)
    {
        String property = "maximumScale";

        Integer oldValue = new Integer(getMaximumScale());
        Integer newValue = new Integer(scale);

        // validate the new value
        validateProperty(property, newValue.toString(), JDProperties.MAXIMUM_SCALE);
        
        properties_.setString(JDProperties.MAXIMUM_SCALE, newValue.toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, property + ": " + scale);
    }

    // @M0A - added support for hex constant parser option
    /**
    * Gets the translate hex property, which indicates how
    * the parser will treat hexadecimal literals.
    * @return The value of the translate hex property.
    * <p>Valid values include:
    * <ul>
    *   <li>"character" (Interpret hexadecimal constants as character data)
    *   <li>"binary" (Interpret hexadecimal constants as binary data)
    * </ul>
    * The default value is "character".
    **/
    public String getTranslateHex()
    {
        return properties_.getString(JDProperties.TRANSLATE_HEX);
    }

    // @M0A
    /**
    * Sets the translate hex property, which indicates how
    * the parser will treat hexadecimal literals.
    * @param parseOption The hex constant parser option.
    * <p>Valid values include:
    * <ul>
    *   <li>"character" (Interpret hexadecimal constants as character data)
    *   <li>"binary" (Interpret hexadecimal constants as binary data)
    * </ul>
    * The default value is "character".
    **/
    public void setTranslateHex(String parseOption)
    {
        String property = "translateHex";

        String oldOption = getTranslateHex();
        String newOption = parseOption;

        validateProperty(property, newOption, JDProperties.TRANSLATE_HEX);

        properties_.setString(JDProperties.TRANSLATE_HEX, newOption);

        changes_.firePropertyChange(property, oldOption, newOption);

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, property + ": " + parseOption);
    }

    //@K3A
    /**
    *  Sets the QAQQINI library name.  
    *  @param libraryName The QAQQINI library name.
    **/
    public void setQaqqiniLibrary(String libraryName)
    {
        String property = "qaqqiniLibrary";
        if (libraryName == null)
            throw new NullPointerException(property);

        String old = getQaqqiniLibrary();
        properties_.setString(JDProperties.QAQQINILIB, libraryName);

        changes_.firePropertyChange(property, old, libraryName);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + libraryName);  
    }
    
    //@dup
    /**
     *  Sets the QAQQINI library name.  
     *  @param libraryName The QAQQINI library name.
     *  Note:  this method is the same as setQaqqiniLibrary() so that it corresponds to the connection property name
     **/
    public void setQaqqinilib(String libraryName)
    {
        setQaqqiniLibrary(libraryName);
    }
    

    /**                                                               
    *  Sets the goal the i5/OS system should use with optimization of queries.  
    *  This setting corresponds with the system's QAQQINI option called OPTIMIZATION_GOAL.  
    *  Note, this setting is ignored when running to V5R3 i5/OS or earlier  
    *  @param goal - the optimization goal 
    *  <p>Valid values include:
    *  <ul>
    *  <li>0 = Optimize query for first block of data (*ALLIO) when extended dynamic packages are used; Optimize query for entire result set (*FIRSTIO) when packages are not used</li>
    *  <li>1 = Optimize query for first block of data (*FIRSTIO)</li>
    *  <li>2 = Optimize query for entire result set (*ALLIO) </li>
    *  </ul>
    *  The default value is 0.
    **/
    public void setQueryOptimizeGoal(int goal)
    {
        String property = "queryOptimizeGoal";

        Integer oldValue = new Integer(getQueryOptimizeGoal());
        Integer newValue = new Integer(goal);

        properties_.setString(JDProperties.QUERY_OPTIMIZE_GOAL, newValue.toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + goal);
    }

    //@550
    /**
    * Sets the storage limit in megabytes, that should be used for statements executing a query in a connection.
    * Note, this setting is ignored when running to V5R4 i5/OS or earlier
    * @param limit the storage limit (in megabytes)
    * <p> Valid values are -1 to MAX_STORAGE_LIMIT megabytes.  
    * The default value is -1 meaning there is no limit.
    **/
    public void setQueryStorageLimit(int limit)
    {
        String property = "queryStorageLimit";

        if (limit < -1 || limit > MAX_STORAGE_LIMIT)
            throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        Integer oldValue = new Integer(getQueryStorageLimit());
        Integer newValue = new Integer(limit);

        properties_.setString(JDProperties.QUERY_STORAGE_LIMIT, newValue.toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if(JDTrace.isTraceOn())
            JDTrace.logInformation (this, property + ": " + limit);
    }

    //@540
    /**                                                               
    *  Sets whether lock sharing is allowed for loosely coupled transaction branches.
    *  Note, this setting is ignored when running to V5R3 i5/OS or earlier.  
    *  @param lcs - the "loosely coupled support" setting 
    *  <p>Valid values include:
    *  <ul>
    *  <li>0 = Locks cannot be shared</li>
    *  <li>1 = Locks can be shared</li>
    *  </ul>
    *  The default value is 0.
    **/
    public void setXALooselyCoupledSupport(int lcs)
    {
        String property = "xaLooselyCoupledSupport";

        Integer oldValue = new Integer(getXALooselyCoupledSupport());
        Integer newValue = new Integer(lcs);

        properties_.setString(JDProperties.XA_LOOSELY_COUPLED_SUPPORT, newValue.toString());

        changes_.firePropertyChange(property, oldValue, newValue);

        if (JDTrace.isTraceOn()) 
            JDTrace.logInformation (this, property + ": " + lcs);
    }

    //K2A
    /**
    *  Returns the toolbox trace category.
    *  @return The toolbox trace category.
    *  <p>Valid values include:
    *  <ul>
    *    <li> "none" - The default value.
    *    <li> "datastream"
    *    <li> "diagnostic"
    *    <li> "error"
    *    <li> "information"
    *    <li> "warning"
    *    <li> "conversion"
    *    <li> "proxy"
    *    <li> "pcml"
    *    <li> "jdbc"
    *    <li> "all"
    *    <li> "thread"
    *  </ul>
    **/
    public String getToolboxTraceCategory()
    {
        return properties_.getString(JDProperties.TRACE_TOOLBOX);
    }
    

    //@dup
    /**
     *  Returns the toolbox trace category.
     *  @return The toolbox trace category.
     *  <p>Valid values include:
     *  <ul>
     *    <li> "none" - The default value.
     *    <li> "datastream"
     *    <li> "diagnostic"
     *    <li> "error"
     *    <li> "information"
     *    <li> "warning"
     *    <li> "conversion"
     *    <li> "proxy"
     *    <li> "pcml"
     *    <li> "jdbc"
     *    <li> "all"
     *    <li> "thread"
     *  </ul>
     *  Note:  this method is the same as getToolboxTraceCategory() so that it corresponds to the connection property name
     **/
    public String getToolboxTrace()
    {
        return getToolboxTraceCategory();
    }


    // @K2A
    /**
    * Sets the toolbox trace category, which indicates 
    * what trace points and diagnostic messages should be logged.
    * @param traceCategory The category option.
    * <p>Valid values include:
    * <ul>
    *    <li> "none" 
    *    <li> "datastream"
    *    <li> "diagnostic"
    *    <li> "error"
    *    <li> "information"
    *    <li> "warning"
    *    <li> "conversion"
    *    <li> "proxy"
    *    <li> "pcml"
    *    <li> "jdbc"
    *    <li> "all"
    *    <li> "thread"    
    * </ul>
    * The default value is "none".
    **/
    public void setToolboxTraceCategory(String traceCategory)
    {
        String property = "toolboxTrace";

        String oldOption = getToolboxTraceCategory();
        String newOption = traceCategory;

        validateProperty(property, newOption, JDProperties.TRACE_TOOLBOX);

        properties_.setString(JDProperties.TRACE_TOOLBOX, newOption);

        changes_.firePropertyChange(property, oldOption, newOption);

        if(!traceCategory.equals("") && !traceCategory.equals("none"))
        {
            if (! Trace.isTraceOn())
            {
                Trace.setTraceOn(true);
            }
            if(traceCategory.equals("datastream"))
                Trace.setTraceDatastreamOn(true);
            else if(traceCategory.equals("diagnostic"))
                Trace.setTraceDiagnosticOn(true);
            else if(traceCategory.equals("error"))
                Trace.setTraceErrorOn(true);
            else if(traceCategory.equals("information"))
                Trace.setTraceInformationOn(true);
            else if(traceCategory.equals("warning"))
                Trace.setTraceWarningOn(true);
            else if(traceCategory.equals("conversion"))
                Trace.setTraceConversionOn(true);
            else if(traceCategory.equals("proxy"))
                Trace.setTraceProxyOn(true);
            else if(traceCategory.equals("pcml"))
                Trace.setTracePCMLOn(true);
            else if(traceCategory.equals("jdbc"))
                Trace.setTraceJDBCOn(true);
            else if(traceCategory.equals("all"))
                Trace.setTraceAllOn(true);
            else if(traceCategory.equals("thread"))
                Trace.setTraceThreadOn(true);
        }

        if (JDTrace.isTraceOn())
            JDTrace.logInformation (this, property + ": " + traceCategory);
    }
    
    //@dup
    /**
     * Sets the toolbox trace category, which indicates 
     * what trace points and diagnostic messages should be logged.
     * @param traceCategory The category option.
     * <p>Valid values include:
     * <ul>
     *    <li> "none" 
     *    <li> "datastream"
     *    <li> "diagnostic"
     *    <li> "error"
     *    <li> "information"
     *    <li> "warning"
     *    <li> "conversion"
     *    <li> "proxy"
     *    <li> "pcml"
     *    <li> "jdbc"
     *    <li> "all"
     *    <li> "thread"    
     * </ul>
     * The default value is "none".
     * Note:  this method is the same as setToolboxTraceCategory() so that it corresponds to the connection property name
     **/
    public void setToolboxTrace(String traceCategory)
    {
        setToolboxTraceCategory(traceCategory);
    }

    /**
    *  Validates the property value.
    *  @param property The property name.
    *  @param value The property value.
    *  @param index The property index.
    **/
    private void validateProperty(String property, String value, int index)
    {
        if (value.length() != 0)
        {                                                      // @A7A
            DriverPropertyInfo[] info = properties_.getInfo();
            String[] choices = info[index].choices;

            boolean notValid = true;
            int current = 0;
            while (notValid && current < choices.length)
            {
                if (value.equalsIgnoreCase(choices[current]))
                    notValid = false;
                else
                    current++;
            }
            if (notValid)
                throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }                                                                                    // @A7A
    }

    /**
    *  Serializes the i5/OS system and user information.
    *  @param out The output stream.
    *  @exception IOException If a file I/O error occurs.
    **/
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        // @F0D String server = getServerName();
        // @F0D if (!server.equals(""))
        // @F0D     serialServerName_ = server;

        // @F0D String user = getUser();
        // @F0D if (!user.equals(""))
        // @F0D     serialUserName_ = user;

        if (!savePasswordWhenSerialized_)                        //@J3a
        {                                                        //@J3a
            serialPWBytes_ = null;                                //@J3a
            serialKeyRingPWBytes_ = null;                         //@J3a
        }                                                        //@J3a

        // Serialize the object.
        out.defaultWriteObject();
    }

    /**
    *  Returns the string representation of the object.
    *  @return The string representation.
    **/
    public String toString()
    {
        /*
        * Implementation note: Used only for tracing information.
        */
        String name = getDataSourceName();
        if (name == null)
            name = "";
        return name;
    }

    // @J3 new method.
    // Twiddle password bytes.
    private static char[] xpwConfuse(String info)
    {
        Random rng = new Random();
        byte[] adderBytes = new byte[18];
        rng.nextBytes(adderBytes);
        char[] adder = BinaryConverter.byteArrayToCharArray(adderBytes);

        byte[] maskBytes = new byte[14];
        rng.nextBytes(maskBytes);
        char[] mask = BinaryConverter.byteArrayToCharArray(maskBytes);

        char[] infoBytes = xencode(adder, mask, info.toCharArray());
        char[] returnBytes = new char[info.length() + 16];
        System.arraycopy(adder, 0, returnBytes, 0, 9);
        System.arraycopy(mask, 0, returnBytes, 9, 7);
        System.arraycopy(infoBytes, 0, returnBytes, 16, info.length());

        return returnBytes;
    }

    // @J3 new method.
    // Get clear password bytes back.
    private static String xpwDeconfuse(char[] info)
    {
        char[] adder = new char[9];
        System.arraycopy(info, 0, adder, 0, 9);
        char[] mask = new char[7];
        System.arraycopy(info, 9, mask, 0, 7);
        char[] infoBytes = new char[info.length - 16];
        System.arraycopy(info, 16, infoBytes, 0, info.length - 16);

        return new String(xdecode(adder, mask, infoBytes));
    }

    // @J3 new method    
    // Scramble some bytes.
    private static char[] xencode(char[] adder, char[] mask, char[] bytes)
    {
        if (bytes == null) return null;
        int length = bytes.length;
        char[] buf = new char[length];
        for (int i = 0; i < length; ++i)
        {
            buf[i] = (char)(bytes[i] + adder[i % 9]);
        }
        for (int i = 0; i < length; ++i)
        {
            buf[i] = (char)(buf[i] ^ mask[i % 7]);
        }
        return buf;
    }


    // @J3 new method.       
    private static char[] xdecode(char[] adder, char[] mask, char[] bytes)
    {
        int length = bytes.length;
        char[] buf = new char[length];
        for (int i = 0; i < length; ++i)
        {
            buf[i] = (char)(mask[i % 7] ^ bytes[i]);
        }
        for (int i = 0; i < length; ++i)
        {
            buf[i] = (char)(buf[i] - adder[i % 9]);
        }
        return buf;
    }
    
    
    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] {  "com.ibm.as400.access.AS400JDBCDataSource", "javax.sql.DataSource" };
    } 


}
