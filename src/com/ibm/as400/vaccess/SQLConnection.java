///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Enumeration;


/**
SQLConnection objects represent a SQL Connection.  A SQLConnection
object encapsulates the properties needed to make a
JDBC Connection into a Java Bean.  A connection can only be made
once.  Once a connection is closed, it cannot be opened again.

<p>The actual connection is made when <i>getConnection()</i> is
called.

<p>Users should call <i>close()</i> when the connection is no
longer needed.

<p>Properties cannot be changed once a connection is made.

<p>Note that the password is not serialized if it is provided
by the password parameter on the constructor or <i>setPassword()</i>,
or if it is provided using the 'password' property in the
constructor 'properties' parameter or using <i>setProperties()</i>.
If the password is supplied via the URL or other property, it
will be serialized.

<p>SQLConnection objects generate the following events:
<ul>
  <li>PropertyChangeEvent
  <li>WorkingEvent
</ul>
**/
public class SQLConnection
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// Properties.
transient private String password_ = null;
transient private String uid_ = null;     // altered after connection
private String uid2_ = null;              // original
private Properties properties_ = null;             // excluding password
transient private Properties properties2_ = null;  // including password
transient private String url_ = null;     // altered after connection
private String url2_ = null;              // original

// SQL connection
transient private Connection connection_ = null;

// Event support.
transient private PropertyChangeSupport changeListeners_
    = new PropertyChangeSupport (this);
transient private VetoableChangeSupport vetoListeners_
    = new VetoableChangeSupport (this);
transient private WorkingEventSupport workingListeners_
    = new WorkingEventSupport (this);


/**
Creates a SQLConnection object.
**/
public SQLConnection()
{
}



/**
Creates a SQLConnection object.

@param URL The URL used to connect to the database.
**/
public SQLConnection(String URL)
{
    if (URL == null)
        throw new NullPointerException("URL");
    url2_ = URL;
    url_ = url2_;
}



/**
Creates a SQLConnection object.

@param URL The URL used to connect to the database.
@param userName The user name used to connect to the database.
**/
public SQLConnection(String URL,
                     String userName)
{
    if (URL == null)
        throw new NullPointerException("URL");
    url2_ = URL;
    url_ = url2_;
    if (userName == null)
        throw new NullPointerException("userName");
    uid2_ = userName;
    uid_ = uid2_;
}



/**
Creates a SQLConnection object.

@param URL The URL used to connect to the database.
@param userName The user name used to connect to the database.
@param password The password used to connect to the database.
**/
public SQLConnection(String URL,
                     String userName,
                     String password)
{
    if (URL == null)
        throw new NullPointerException("URL");
    url2_ = URL;
    url_ = url2_;
    if (userName == null)
        throw new NullPointerException("userName");
    uid2_ = userName;
    uid_ = uid2_;
    if (password == null)
        throw new NullPointerException("password");
    password_ = password;
}



/**
Creates a SQLConnection object.

@param URL The URL used to connect to the database.
@param properties The properties used to connect to the database.
**/
public SQLConnection(String URL,
                     Properties properties)
{
    if (URL == null)
        throw new NullPointerException("URL");
    url2_ = URL;
    url_ = url2_;
    if (properties == null)
        throw new NullPointerException("properties");
    properties2_ = properties;
    properties_ = new Properties();
    for (Enumeration e = properties2_.propertyNames(); e.hasMoreElements();)
    {
        String prop = (String)e.nextElement();
        properties_.put(prop, properties2_.getProperty(prop));
    }
    properties_.remove("password");
}



/**
Adds a listener to be notified when the value of any bound
property is changed.
The listener's <i>propertyChange()</i> method will be called.

@param  listener  The listener.
**/
public void addPropertyChangeListener (PropertyChangeListener listener)
{
    changeListeners_.addPropertyChangeListener (listener);
}



/**
Adds a listener to be notified when the value of any constrained
property is changed.
The listener's <i>vetoableChange()</i> method will be called.

@param  listener  The listener.
**/
public void addVetoableChangeListener (VetoableChangeListener listener)
{
    vetoListeners_.addVetoableChangeListener (listener);
}


/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
public void addWorkingListener (WorkingListener listener)
{
    workingListeners_.addWorkingListener (listener);
}


/**
Closes the SQL connection, if it is open.
Once a connection is closed, it cannot be opened again.
@exception SQLException If there is an error closing the connection.
**/
public void close()
throws SQLException
{
    if (connection_ != null)
    {
        connection_.close();
    }
}


/**
Returns a JDBC connection.  If a successful connection has not yet
been made, a new connection will be made.  Subsequent
invocations will reuse the connection.  It is up to the
user to close the connection, or allow garbage collection
to do so.
@return The connection.
@exception SQLException If there is an error connecting to
the database.
**/
public Connection getConnection()
throws SQLException
{
    if (connection_ == null)
    {
        if (url2_ == null)
            throw new IllegalStateException("URL");

        workingListeners_.fireStartWorking ();

        // Get connection.
        try
        {
            if (uid2_ != null)
            {
                Trace.log(Trace.INFORMATION, "Getting SQL Connection with uid, pwd.");
                connection_ = DriverManager.getConnection(url2_, uid2_, password_);
            }
            else if (properties2_ != null)
            {
                Trace.log(Trace.INFORMATION, "Getting SQL Connection with properties.");
                connection_ = DriverManager.getConnection(url2_, properties2_);
            }
            else
            {
                Trace.log(Trace.INFORMATION, "Getting SQL Connection with url only.");
                connection_ = DriverManager.getConnection(url2_);
            }
            Trace.log(Trace.INFORMATION, "Got SQL Connection.");
        }
        catch (SQLException e)
        {
            workingListeners_.fireStopWorking ();
            Trace.log(Trace.ERROR, "Error getting SQL Connection: " + e.getMessage());
            throw e;
        }

        // Get userid and URL.
        // Erase values in case of errors.
        uid_ = null;
        url_ = null;
        try
        {
            DatabaseMetaData meta = connection_.getMetaData();
            uid_ = meta.getUserName();
            url_ = meta.getURL();
        }
        catch (SQLException e)
        {
            // log error, but continue
            Trace.log(Trace.WARNING, "Error getting SQL meta data: " + e.getMessage());
        }

        workingListeners_.fireStopWorking ();
    }

    return connection_;
}


/**
Returns the properties for this connection.
This method will return the properties as set by the
constructor or the last <i>setProperties()</i> call, minus
the 'password' property, if included.
@return The properties for this connection.
**/
public Properties getProperties()
{
    return properties_;
}


/**
Returns the URL for this connection.
If a connection has not yet been made, this method will
return the URL supplied on the constructor or the
last <i>setURL()</i> call.  If a connection has been
made, the URL will be the URL for the connection.
@return The URL for this connection.
**/
public String getURL()
{
    if (url_ == null)
        return "";
    return url_;
}


/**
Returns the user name for this connection.
If a connection has not yet been made, this method will
return the user name supplied on the constructor or the
last <i>setUserName()</i> call.  If a connection has been
made, the user name will be the user name being used for
the connection.
@return The user name for this connection.
**/
public String getUserName()
{
    if (uid_ == null)
        return "";
    return uid_;
}


/**
Restore the state of this object from an object input stream.
It is used when deserializing an object.
@param in The input stream of the object being deserialized.
@exception IOException
@exception ClassNotFoundException
**/
private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
{
    // Restore the non-static and non-transient fields.
    in.defaultReadObject();
    // Initialize the transient fields.
    changeListeners_ = new PropertyChangeSupport(this);
    vetoListeners_ = new VetoableChangeSupport(this);
    workingListeners_ = new WorkingEventSupport(this);
    password_ = null;
    connection_ = null;
    url_ = url2_;
    uid_ = uid2_;
    properties2_ = properties_;
}


/**
Removes a listener from being notified when the value of any bound
property is changed.

@param  listener  The listener.
**/
public void removePropertyChangeListener (PropertyChangeListener listener)
{
    changeListeners_.removePropertyChangeListener (listener);
}



/**
Removes a listener from being notified when the value of any constrained
property is changed.

@param  listener  The listener.
**/
public void removeVetoableChangeListener (VetoableChangeListener listener)
{
    vetoListeners_.removeVetoableChangeListener (listener);
}



/**
Removes a listener from being notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
public void removeWorkingListener (WorkingListener listener)
{
    workingListeners_.removeWorkingListener (listener);
}


/**
Sets the password used to connect to the database.
This property cannot be changed after a connection is made
by calling <i>getConnection()</i>.
This property is bound and constrained.  The events will
always have null and an empty string for the old and
new value, respectively.

@param password The password used to connect to the database.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setPassword(String password)
    throws PropertyVetoException
{
    if (connection_ != null)
        throw new IllegalStateException();
    if (password == null)
        throw new NullPointerException("password");

    // If values are the same, don't bother.
    if (password_ != null && password_.equals(password))
        return;

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("password", null, "");

    // Make property change.
    password_ = password;

    // Fire the property change event.
    changeListeners_.firePropertyChange("password", null, "");
}


/**
Sets the properties used to connect to the database.
This property cannot be changed after a connection is made
by calling <i>getConnection()</i>.
This property is bound and constrained.

@param properties The properties used to connect to the database.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setProperties(Properties properties)
    throws PropertyVetoException
{
    if (connection_ != null)
        throw new IllegalStateException();
    if (properties == null)
        throw new NullPointerException("properties");

    Properties temp = new Properties();
    for (Enumeration e = properties.propertyNames(); e.hasMoreElements();)
    {
        String prop = (String)e.nextElement();
        temp.put(prop, properties.getProperty(prop));
    }
    temp.remove("password");

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("properties", properties_, temp);

    // Make property change.
    Properties old = properties_;
    properties2_ = properties;
    properties_ = temp;

    // Fire the property change event.
    changeListeners_.firePropertyChange("properties", old, properties_);
}


/**
Sets the URL used to connect to the database.
This property cannot be changed after a connection is made
by calling <i>getConnection()</i>.
This property is bound and constrained.

@param URL The URL used to connect to the database.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setURL(String URL)
    throws PropertyVetoException
{
    if (connection_ != null)
        throw new IllegalStateException();
    if (URL == null)
        throw new NullPointerException("URL");

    String old = getURL();

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("URL", old, URL);

    // Make property change.
    url2_ = URL;
    url_ = url2_;

    // Fire the property change event.
    changeListeners_.firePropertyChange("URL", old, url_);
}


/**
Sets the user name used to connect to the database.
This property cannot be changed after a connection is made
by calling <i>getConnection()</i>.
This property is bound and constrained.

@param userName The user name used to connect to the database.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setUserName(String userName)
    throws PropertyVetoException
{
    if (connection_ != null)
        throw new IllegalStateException();
    if (userName == null)
        throw new NullPointerException("userName");

    String old = getUserName();

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("userName", old, userName);

    // Make property change.
    uid2_ = userName;
    uid_ = uid2_;

    // Fire the property change event.
    changeListeners_.firePropertyChange("userName", old, uid_);
}




} // end of class SQLConnection
