///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLStatementDocument.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.ActionCompletedEvent;
import com.ibm.as400.access.ActionCompletedListener;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;



/**
The SQLStatementDocument class represents SQL statement that is
issued when <i>execute()</i> is called.

<p>It is up to the user to register a JDBC driver when using this class.
For example, the following code registers the IBM Toolbox for Java
JDBC driver.
<pre>
   DriverManager.registerDriver (new com.ibm.as400.access.AS400JDBCDriver ());
</pre>

<p>Most errors are reported by firing ErrorEvents, rather
than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>SQLStatementDocument objects generate the following events:
<ul>
  <li>ActionCompletedEvent
  <li>ErrorEvent
  <li>PropertyChangeEvent
  <li>DocumentEvent
  <li>WorkingEvent
</ul>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
// Because the information from SQLStatement Documents may be the result
// of SQL that is no longer equal to the text of the document,
// getSQLStatement is provided.
// Note that the JDBC resources are not explicitly closed,
// the user or garbage collection is relied upon to close them.
public class SQLStatementDocument
extends PlainDocument
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



// Event support.
transient private PropertyChangeSupport changeListeners_
    = new PropertyChangeSupport (this);
transient private VetoableChangeSupport vetoListeners_
    = new VetoableChangeSupport (this);
transient private ActionCompletedEventSupport actionListeners_
    = new ActionCompletedEventSupport (this);
transient private ErrorEventSupport errorListeners_
    = new ErrorEventSupport (this);
transient private WorkingEventSupport workingListeners_
    = new WorkingEventSupport (this);

// properties
private SQLConnection connection_ = null;
transient private SQLWarning warnings_ = null;

transient private Statement statement_ = null;
transient private String sql_ = "";  // Last SQL statement submitted.


/**
Constructs a SQLStatementDocument object.
**/
public SQLStatementDocument ()
{
    super();
}



/**
Constructs a SQLStatementDocument object.

@param   connection      The SQL connection.
@param   text    The text for the document
**/
public SQLStatementDocument (SQLConnection connection,
                             String text)
{
    super();
    if (connection == null)
        throw new NullPointerException("connection");
    if (text == null)
        throw new NullPointerException("text");
    try
    {
        insertString(0, text, null);
    }
    catch(BadLocationException e) {}  // will never happen
    connection_ = connection;
}



/**
Adds a listener to be notified when a SQL statement is executed.
The listener's <i>actionCompleted()</i> method will be called.

@param  listener  The listener.
**/
public void addActionCompletedListener (ActionCompletedListener listener)
{
    actionListeners_.addActionCompletedListener(listener);
}



/**
Adds a listener to be notified when an error occurs.
The listener's <i>errorOccurred()</i> method will be called.

@param  listener  The listener.
**/
public void addErrorListener (ErrorListener listener)
{
    errorListeners_.addErrorListener (listener);
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
Runs the contents of this document as a SQL statement.
**/
public void execute()
{
    // Ensure required properties have been set.
    if (connection_ == null)
    {
        Exception e = new IllegalStateException("connection");
        errorListeners_.fireError(e);
        return;
    }

    workingListeners_.fireStartWorking ();

    // Make sure we have a valid statement to use.
    if (statement_ == null)
    {
        Connection conn;
        try
        {
            conn = connection_.getConnection();
            statement_ = conn.createStatement();
        }
        catch (SQLException e)
        {
            // Cannot continue, so send event and return.
            errorListeners_.fireError(e);
            return;
        }
        // Store warnings to chain if warnings are requested.
        try
        {
            warnings_ = conn.getWarnings();
        }
        catch (SQLException e)
        {
            // Fire error event, but continue.
            errorListeners_.fireError(e);
        }
    }
    else
    {
        // clear all warnings
        warnings_ = null;
        try
        {
            statement_.clearWarnings();
        }
        catch(SQLException e)
        {
            // Fire error event, but continue.
            errorListeners_.fireError(e);
        }
    }

    // try to execute the statement
    try
    {
        sql_ = getText(0, getLength());
        statement_.execute(sql_);
    }
    catch (BadLocationException e)// {} // should not occur
    {
        errorListeners_.fireError(e);
    }
    catch (SQLException e)
    {
        errorListeners_.fireError(e);
    }

    // Send events.
    actionListeners_.fireActionCompleted();
    workingListeners_.fireStopWorking ();
}



/**
Returns the SQL connection used to execute statements.

@return The SQL connection.
**/
public SQLConnection getConnection ()
{
    return connection_;
}



/**
Makes the next result of the last statement run the current result.
Nothing is done if no statement has been executed.
<p>Refer to JDBC documentation for more information.
**/
public void getMoreResults()
{
    if (statement_ != null)
    {
        workingListeners_.fireStartWorking ();
        try
        {
            statement_.getMoreResults();
        }
        catch (SQLException e)
        {
            // Throw error event.
            errorListeners_.fireError(e);
        }
        workingListeners_.fireStopWorking ();
    }
}



/**
Returns the current results of the last SQL statement run.
Multiple results can be obtained by
calling <i>getUpdateCount()</i> and/or <i>getResultSet()</i> multiple
times, with intervening <i>getMoreResults()</i> calls.
<p>Refer to JDBC documentation for more information.

@return The result set or null if the result was not a
result set, or if there are no results.
**/
public ResultSet getResultSet()
{
    // If we haven't run anything, return null.
    if (statement_ == null)
    {
        return null;
    }
    // Get the result set from the statement.
    workingListeners_.fireStartWorking ();
    ResultSet result = null;
    try
    {
        result = statement_.getResultSet();
    }
    catch(SQLException e)
    {
        // Throw error event.
        errorListeners_.fireError(e);
    }
    workingListeners_.fireStopWorking ();
    return result;
}



/**
Returns the SQL statement that was last executed
which the results in this object represent.

@return The SQL statement that the results in this object represent.
**/
public String getSQLStatement ()
{
    return sql_;
}



/**
Returns the number of rows affected by the last SQL statement run.
Multiple results can be obtained by
calling <i>getUpdateCount()</i> and/or <i>getResultSet()</i> multiple
times, with intervening <i>getMoreResults()</i> calls.
If an error occurs, 0 is returned.
<p>Refer to JDBC documentation for more information.

@return The number of rows that were
affected.  If no rows were affected or the SQL statement was
a DDL command, 0 is returned.  If there are no results or the results
are a result set, -1 is returned.
**/
public int getUpdateCount()
{
    // If we haven't run anything, return -1.
    if (statement_ == null)
    {
        return -1;
    }
    // Get the update count from the statement.
    try
    {
        return statement_.getUpdateCount();
    }
    catch(SQLException e)
    {
        // Throw error event.
        errorListeners_.fireError(e);
        return 0;
    }
}



/**
Returns the warnings generated by the JDBC connection and statement.
Each time the <i>execute()</i> is called (the SQL statement is run), the
warnings are cleared.  Connection warnings are only available after the
first time a statement is executed.  The warnings from the statement will
be linked to the end of any connection warnings.

@return The warnings generated by the connection and statement,
or null if none.
**/
public SQLWarning getWarnings ()
{
    // If no statement, we are not in a state to have warnings.
    if (statement_ == null)
        return null;

    // Get statement warnings.
    SQLWarning s_warnings = null;
    try
    {
        s_warnings = statement_.getWarnings();
    }
    catch(SQLException e)
    {
        // Throw error event, then continue.
        errorListeners_.fireError(e);
    }

    // If connection warnings...
    if (warnings_ != null)
    {
        // If no statement warnings, just return connection warnings.
        if (s_warnings == null)
        {
            return warnings_;
        }

        // We have both connection and statement warnings.
        // Find the last warning in the connection chain.
        SQLWarning last = warnings_;
        SQLWarning next;
        while ((next = last.getNextWarning()) != null)
        {
            last = next;
        }
        // Add statement warnings to the end of connection chain.
        last.setNextWarning(s_warnings);
        return warnings_;
    }
    else
    {
        // otherwise, just return statement warnings
        return s_warnings;
    }
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
    actionListeners_ = new ActionCompletedEventSupport(this);
    errorListeners_ = new ErrorEventSupport(this);
    workingListeners_ = new WorkingEventSupport(this);
    warnings_ = null;
    statement_ = null;
    sql_ = "";
}



/**
Removes a listener from being notified when a SQL statement is issued.

@param  listener  The listener.
**/
public void removeActionCompletedListener(ActionCompletedListener listener)
{
    actionListeners_.removeActionCompletedListener(listener);
}


/**
Removes a listener from being notified when an error occurs.

@param  listener  The listener.
**/
public void removeErrorListener (ErrorListener listener)
{
    errorListeners_.removeErrorListener (listener);
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
Sets the SQL connection used to execute statements.
This property is bound and constrained.

@param       connection          The SQL connection.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setConnection (SQLConnection connection)
    throws PropertyVetoException
{
    if (connection == null)
        throw new NullPointerException("connection");

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("connection", connection_, connection);

    // Make property change.
    SQLConnection old = connection_;
    connection_ = connection;

    // Statement is no longer valid, must be reconstructed.
    if (old != connection)
    {
        if (statement_ != null)
        {
            try
            {
                statement_.close();
            }
            catch (SQLException e)
            {
                errorListeners_.fireError(e);
            }
        }
        statement_ = null;
        warnings_ = null;
    }

    // Fire the property change event.
    changeListeners_.firePropertyChange("connection", old, connection_);
}

}
