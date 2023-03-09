///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLStatementButton.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.ActionCompletedListener;
import com.ibm.as400.access.ActionCompletedEvent;
import com.ibm.as400.access.Trace;
import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;



/**
The SQLStatementButton class represents a button control that issues a
SQL statement when pressed.

<p>It is up to the user to register a JDBC driver when using this class.
For example, the following code registers the IBM Toolbox for Java
JDBC driver.
<pre>
   DriverManager.registerDriver (new com.ibm.as400.access.AS400JDBCDriver ());
</pre>

<p>Most errors are reported by firing ErrorEvents, rather
than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>SQLStatementButton objects generate the following events:
<ul>
  <li>ActionCompletedEvent
  <li>ErrorEvent
  <li>PropertyChangeEvent
  <li>WorkingEvent
</ul>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
// Note that the JDBC resources are not explicitly closed,
// the user or garbage collection is relied upon to close them.
public class SQLStatementButton
extends JButton
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// The variables which have private commented out had to made
// package scope since currently Internet Explorer does not
// allow inner class to access private variables in their
// containing class.

// Event support.
transient /*private*/ ActionCompletedEventSupport actionListeners_
    = new ActionCompletedEventSupport (this);
transient /*private*/ ErrorEventSupport errorListeners_
    = new ErrorEventSupport (this);
transient /*private*/ WorkingEventSupport workingListeners_
    = new WorkingEventSupport (this);

// Adapter for listening for working events and enabling working cursor.
transient /*private*/ WorkingCursorAdapter worker_
    = new WorkingCursorAdapter(this);

// properties
/*private*/ SQLConnection connection_ = null;
/*private*/ String sql_ = null;
transient /*private*/ SQLWarning warnings_ = null;

// SQL statement
transient /*private*/ Statement statement_ = null;


/**
Constructs a SQLStatementButton object.
**/
public SQLStatementButton ()
{
    super();
    addActionListener(new ButtonListener_());
}



/**
Constructs a SQLStatementButton object.

@param   icon           The icon to be placed on the button.
**/
public SQLStatementButton (Icon icon)
{
    super(icon);
    addActionListener(new ButtonListener_());
}



/**
Constructs a SQLStatementButton object.

@param   text            The text to be placed on the button.
**/
public SQLStatementButton (String text)
{
    super(text);
    addActionListener(new ButtonListener_());
}



/**
Constructs a SQLStatementButton object.

@param   text            The text to be placed on the button.
@param   icon            The icon to be placed on the button.
**/
public SQLStatementButton (String text,
                           Icon icon)
{
    super(text, icon);
    addActionListener(new ButtonListener_());
}



/**
Constructs a SQLStatementButton object.

@param   text            The text to be placed on the button.
@param   icon            The icon to be placed on the button.
@param   connection      The SQL connection.
@param   SQLStatement    The SQL statement.
**/
public SQLStatementButton (String text,
                           Icon icon,
                           SQLConnection connection,
                           String SQLStatement)
{
    super(text, icon);
    if (connection == null)
        throw new NullPointerException("connection");
    connection_ = connection;
    if (SQLStatement == null)
        throw new NullPointerException("SQLStatement");
    sql_ = SQLStatement;
    addActionListener(new ButtonListener_());
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
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
public void addWorkingListener (WorkingListener listener)
{
    workingListeners_.addWorkingListener (listener);
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
Returns the SQL statement that will be run when this button is pressed.

@return The SQL statement that will be run when this button is pressed.
**/
public String getSQLStatement ()
{
    if (sql_ == null)
        return "";
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
Each time the button is pressed (the SQL statement is run), the
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
    actionListeners_ = new ActionCompletedEventSupport(this);
    errorListeners_ = new ErrorEventSupport(this);
    workingListeners_ = new WorkingEventSupport(this);
    worker_ = new WorkingCursorAdapter(this);
    warnings_ = null;
    statement_ = null;
    // Restore listener
    addActionListener(new ButtonListener_());
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
    fireVetoableChange("connection", connection_, connection);

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
    firePropertyChange("connection", old, connection_);
}



/**
Sets the SQL statement to run.  This property
is bound and constrained.

@param       SQLStatement            The SQL statement.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setSQLStatement (String SQLStatement)
    throws PropertyVetoException
{
    if (SQLStatement == null)
        throw new NullPointerException("SQLStatement");

    String old = getSQLStatement();

    // Fire a vetoable change event.
    fireVetoableChange("SQLStatement", old, SQLStatement);

    // Make property change.
    sql_ = SQLStatement;

    // Fire the property change event.
    firePropertyChange("SQLStatement", old, sql_);
}



/**
Class for listening to action events.  This is used to run the
SQL statement when the button is pressed.
**/
private class ButtonListener_
implements ActionListener
{

public void actionPerformed(ActionEvent ev)
{
    // Ensure required properties have been set.
    if (connection_ == null)
    {
        Exception e = new IllegalStateException("connection");
        errorListeners_.fireError(e);
        return;
    }
    if (sql_ == null)
    {
        Exception e = new IllegalStateException("SQLStatement");
        errorListeners_.fireError(e);
        return;
    }

    Trace.log(Trace.INFORMATION, "Running button, sql is:  " + sql_);

    workingListeners_.fireStartWorking ();
    // Change cursor to working.
    worker_.startWorking(new WorkingEvent(this));

    // Make sure we have a valid statement to use.
    if (statement_ == null)
    {
        Connection conn;
        try
        {
            conn = connection_.getConnection();
            statement_ = conn.createStatement();
        }
        catch(SQLException e)
        {
            // Cannot continue, so send event and return.
            errorListeners_.fireError(e);
            worker_.stopWorking(new WorkingEvent(this));
            workingListeners_.fireStopWorking ();
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
        statement_.execute(sql_);
    }
    catch(SQLException e)
    {
        // Throw error event.
        errorListeners_.fireError(e);
    }

    // Send completion event.
    actionListeners_.fireActionCompleted();

    // Set cursor back.
    worker_.stopWorking(new WorkingEvent(this));

    workingListeners_.fireStopWorking ();
}

}  // end of class ButtonListener_


}
