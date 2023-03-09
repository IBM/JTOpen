///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryBuilderPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;



/**
The SQLQueryBuilderPane class represents a panel which allows a
user to dynamically build a SQL query using graphical interfaces.
The corresponding SQL is generated.

<p>Data for the GUI is retrieved from the system
when <i>load()</i> is called.  If <i>load()</i> is not called,
the tables in the GUI will be empty.

<p>If table names are provided, then the user is presented choices
based on the fields within those tables.  If <i>userSelectTables</i> is
true, the user may modify the tables on which the query is built.
<i>tableSchemas</i> and <i>userSelectTableSchemas</i> are used
to determine the list of tables which the user can choose from, however
the user is not prevented from using tables not in these schemas.

<p>It is up to the user to register a JDBC driver when using this class.
For example, the following code registers the IBM Toolbox for Java
JDBC driver.
<pre>
   DriverManager.registerDriver (new com.ibm.as400.access.AS400JDBCDriver ());
</pre>

<p>Most errors are reported by firing ErrorEvents, rather
than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>SQLQueryBuilderPane objects generate the following events:
<ul>
  <li>ErrorEvent
  <li>PropertyChangeEvent
</ul>

<pre>
// Register JDBC driver.
DriverManager.registerDriver (new com.ibm.as400.access.AS400JDBCDriver ());

 // Set up table for result set contents.
final SQLConnection connection = new SQLConnection("jdbc:as400://MySystem");
final SQLQueryBuilderPane pane = new SQLQueryBuilderPane(connection);

 // Set up window to hold table
JFrame frame = new JFrame ("My Window");
WindowListener l = new WindowAdapter()
{
     // Close the pane when window is closed.
    public void windowClosing(WindowEvent e)
    {
        pane.close();
        connection.close();
    }
};
frame.addWindowListener(l);

// Set up the error dialog adapter.
pane.addErrorListener (new ErrorDialogAdapter (frame));

// Add the component and get data from system.
frame.getContentPane().add(pane);
pane.load();

 // Display the window
frame.setVisible(true)
</pre>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class SQLQueryBuilderPane
extends JComponent
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// The variables and methods which have private commented out
// had to be made package scope since currently Internet Explorer
// does not allow inner class to access private items in their
// containing class.


// Current property values.
private SQLConnection sqlconn_ = null;
private String[] tables_;
private String[] schemas_ = new String[0];
transient private Connection connection_ = null;

// GUI control property values
private boolean tablesUserDefined_ = true;
private boolean schemasUserDefined_ = true;

// GUI controls
transient /*private*/ JTabbedPane notebook_; // The notebook control
transient private SQLQueryTablePane   tablePane_;   // Panes for each notebook pane
transient private SQLQuerySelectPane  selectPane_;   
transient private SQLQueryJoinPane    joinPane_;     
transient private SQLQueryWherePane   wherePane_;   
transient private SQLQueryGroupPane groupPane_;      
transient private SQLQueryHavingPane  havingPane_;   
transient private SQLQueryOrderPane orderPane_;      
transient private SQLQuerySummaryPane summaryPane_; 

// indices of panels in notebook
static final int TABLE_ = 0;
static final int SELECT_ = 1;
static final int JOIN_ = 2;
static final int WHERE_ = 3;
static final int GROUP_ = 4;
static final int HAVING_ = 5;
static final int ORDER_ = 6;
static final int SUMMARY_ = 7;
transient /*private*/ int currentPane_ = TABLE_;

// Whether the notebook page panel has been initialized.
transient private boolean panelInit[] = new boolean[8];

// Data model which represents the fields of the tables at
// the current connection.
transient SQLMetaDataTableModel fields_ = null;
// We have a separate listener list for panels to listen
// to changed to fields_.
transient private PropertyChangeSupport fieldListeners_
    = new PropertyChangeSupport (this);

// Flag used to determine load() function.
private boolean internalLoad_ = false;
// True if the user has done load().
transient private boolean loadDone_ = false;

// Event support.
transient private PropertyChangeSupport changeListeners_
    = new PropertyChangeSupport (this);
transient private VetoableChangeSupport vetoListeners_
    = new VetoableChangeSupport (this);
transient private ErrorEventSupport panelErrors_
    = new ErrorEventSupport(this);

// Adapter for listening for working events and enabling working cursor.
transient /*private*/ WorkingCursorAdapter worker_
    = new WorkingCursorAdapter(this);
transient WorkingEvent workEvent_ = new WorkingEvent(this);


/**
Constructs a SQLQueryBuilderPane object.
**/
public SQLQueryBuilderPane ()
{
    super();
    tables_ = new String[0];
    setupPane();
    addFocusListener(new SerializationListener(this)); //@B0A - for safe serialization
}



/**
Constructs a SQLQueryBuilderPane object.

@param   connection      The SQL connection.
**/
public SQLQueryBuilderPane (SQLConnection connection)
{
    super();
    sqlconn_ = connection;
    tables_ = new String[0];
    setupPane();
    addFocusListener(new SerializationListener(this)); //@B0A - for safe serialization
}



/**
Constructs a SQLQueryBuilderPane object.

@param   connection      The SQL connection.
@param   tables          The names of the tables to be used for the query.
                         Tables should be in the form of <em>schema.table</em>.
**/
public SQLQueryBuilderPane (SQLConnection connection,
                             String[] tables)
{
    super();
    if (tables == null)
        throw new NullPointerException("tables");
    sqlconn_ = connection;
    tables_ = tables;
    setupPane();
    addFocusListener(new SerializationListener(this)); //@B0A - for safe serialization
}


/**
Adds a listener to be notified when an error occurs.
The listener's <i>errorOccurred()</i> method will be called.

@param  listener  The listener.
**/
public void addErrorListener (ErrorListener listener)
{
    panelErrors_.addErrorListener(listener);
}



/**
Adds a listener to be notified when fields_ is changed.
The listener's <i>propertyChange()</i> method will be called.

@param  listener  The listener.
**/
void addFieldListener (PropertyChangeListener listener)
{
    fieldListeners_.addPropertyChangeListener (listener);
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
    super.addPropertyChangeListener(listener);
}



/**
Adds a listener to be notified when the selected items have changed.
The listener's <i>propertyChange()</i> method will be called.

@param  listener  The listener.
**/
void addSelectListener (PropertyChangeListener listener)
{
    selectPane_.addSelectListener (listener);
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
    super.addVetoableChangeListener(listener);
}


/**
Saves any changes made to the current page.
**/
synchronized /*private*/ void completePage()
{
    Trace.log(Trace.DIAGNOSTIC, "Completing page " +  currentPane_);

    // Finalize the old page.
    switch(currentPane_)
    {
        case TABLE_:
            tablePane_.complete();
            if (tablePane_.getSchemas() != schemas_)
            {
                try {setTableSchemas(tablePane_.getSchemas());}
                catch(PropertyVetoException ev)
                {
                    // property change rejected, set tables back
                    tablePane_.setSchemas(schemas_);
                }
            }
            if (tablePane_.getTables() != tables_)
            {
                try {setTables(tablePane_.getTables());}
                catch(PropertyVetoException ev)
                {
                    // property change rejected, set tables back
                    tablePane_.setTables(tables_);
                }
                internalLoad_ = true;
                load();
                internalLoad_ = false;
                if (tables_.length == 0)
                {  // only summary panel is valid, disable others
                    notebook_.setEnabledAt(SELECT_,false);
                    notebook_.setEnabledAt(JOIN_,false);
                    notebook_.setEnabledAt(WHERE_,false);
                    notebook_.setEnabledAt(GROUP_,false);
                    notebook_.setEnabledAt(HAVING_,false);
                    notebook_.setEnabledAt(ORDER_,false);
                    break;
                }
                else
                {  // enable panels
                    notebook_.setEnabledAt(SELECT_,true);
                    notebook_.setEnabledAt(WHERE_,true);
                    notebook_.setEnabledAt(GROUP_,true);
                    notebook_.setEnabledAt(HAVING_,true);
                    if (tables_.length == 1)
                    {
                        notebook_.setEnabledAt(JOIN_,false);
                    }
                    else
                    {
                        notebook_.setEnabledAt(JOIN_,true);
                    }
                    if (selectPane_.getSelectedItems().length > 0)
                        notebook_.setEnabledAt(ORDER_,true);
                    else
                        notebook_.setEnabledAt(ORDER_,false);
                    break;
                }
            }
            else
            {
                break;
            }
        case SELECT_:
            selectPane_.complete();
            // enable or disable order by and group by panels
            if (selectPane_.getSelectedItems().length > 0)
            {
                notebook_.setEnabledAt(ORDER_,true);
            }
            else
            {
                notebook_.setEnabledAt(ORDER_,false);
            }
            break;
        case JOIN_:
            joinPane_.complete();
            break;
        case WHERE_:
        case GROUP_:
        case HAVING_:
        case ORDER_:
        case SUMMARY_:
            break;
        default:
            Trace.log(Trace.ERROR, "Unknown page " +  currentPane_);
    }
}



/**
Returns the SQL connection with which to access data for the GUI.

@return The SQL connection.
**/
public SQLConnection getConnection ()
{
    return sqlconn_;
}




/**
Returns the SQL query that corresponds to the user's selections.
An empty String is returned if no tables have been selected.

@return The SQL query that corresponds to the user's selections.
**/
synchronized public String getQuery ()
{
    // save info from the current page
    completePage();

    // make sure there are tables
    if (tables_.length == 0)
        return "";

    worker_.startWorking(workEvent_);

    StringBuffer result;
    String clause;
    // Get clauses from all panes that have been inited.

    // Select clause
    result = new StringBuffer("SELECT " + selectPane_.getClause());

    // join clause
    boolean whereProcessed = false;
    boolean fromAdded = false;
    // Only process if there are at least 2 tables
    if (tables_.length > 1)
    {
        if (joinPane_.getJoinType() == SQLQueryJoinPane.INNER_JOIN)
        {
            clause = joinPane_.getClause(0);
            if (clause != null && !clause.equals(""))
            {
                // Add FROM tables
                result.append(" FROM ");
                for (int i = 0; i < tables_.length; ++i)
                {
                    if (i > 0)
                        result.append(", ");
                    result.append(tables_[i]);
                    result.append(" AS ");
                    result.append(tables_[i].substring(tables_[i].lastIndexOf(".")+1));
                }
                fromAdded = true;
                // Add join condition in the WHERE clause
                result.append(" WHERE ");
                result.append(clause);
                // Add WHERE clause
                String where = wherePane_.getClause();
                if (where != null && !where.equals(""))
                {
                    result.append(" AND ");
                    result.append(where);
                }
                whereProcessed = true;
            }
        }
        else  // left outer join
        {
            // Add tables and join conditions interspersed.
            // If the join clause is empty, we do inner join,
            // if not empty, we do a left outer join.
            result.append(" FROM ");
            result.append(tables_[0]);
            result.append(" AS ");
            result.append(tables_[0].substring(tables_[0].lastIndexOf(".")+1));
            for (int i = 0; i < tables_.length-1; )
            {
                // clause should never be null since outer join is
                // not the default join type
                clause = joinPane_.getClause(i).trim();
                if (clause.equals(""))  // empty join clause, do inner
                {
                    result.append(", ");
                    result.append(tables_[++i]);
                    result.append(" AS ");
                    result.append(tables_[i].substring(tables_[i].lastIndexOf(".")+1));
                }
                else // not empty, do left outer
                {
                    result.append(" LEFT JOIN ");
                    result.append(tables_[++i]);
                    result.append(" AS ");
                    result.append(tables_[i].substring(tables_[i].lastIndexOf(".")+1));
                    result.append(" ON ");
                    result.append(clause);
                }
            }
            fromAdded = true;
        }
    }
    if (!fromAdded)
    {
        // Add FROM tables
        result.append(" FROM ");
        for (int i = 0; i < tables_.length; ++i)
        {
            if (i > 0)
                result.append(", ");
            result.append(tables_[i]);
            result.append(" AS ");
            result.append(tables_[i].substring(tables_[i].lastIndexOf(".")+1));
        }
    }

    // where clause
    if (!whereProcessed)  // not handled by join processing
    {
        clause = wherePane_.getClause();
        if (clause != null && !clause.equals(""))
        {
            result.append(" WHERE ");
            result.append(clause);
        }
    }

    // group by clause
    clause = groupPane_.getClause();
    if (clause != null && !clause.equals(""))
    {
        result.append(" GROUP BY ");
        result.append(clause);
    }

    //  having clause
    clause = havingPane_.getClause();
    if (clause != null && !clause.equals(""))
    {
        result.append(" HAVING ");
        result.append(clause);
    }

    //  order by clause
    clause = orderPane_.getClause();
    if (clause != null && !clause.equals(""))
    {
        result.append(" ORDER BY ");
        result.append(clause);
    }

    worker_.stopWorking(workEvent_);

    return result.toString();
}



/**
Returns the names of the fields in the select clause.

@return The names of the fields in the select clause.
**/
synchronized String[] getSelectedFields()
{
    worker_.startWorking(workEvent_);
    String[] result = selectPane_.getSelectedFields();
    worker_.stopWorking(workEvent_);
    return result;
}



/**
Returns the names of the items in the select clause.

@return The names of the items in the select clause.
**/
synchronized String[] getSelectedItems()
{
    worker_.startWorking(workEvent_);
    String[] result = selectPane_.getSelectedItems();
    worker_.stopWorking(workEvent_);
    return result;
}



/**
Returns the names of the tables used in the query.

@return The table names.
**/
public String[] getTables ()
{
    return tables_;
}



/**
Returns the schemas for which the tables will be listed for inclusion
in the query.

@return The schema names.
**/
public String[] getTableSchemas ()
{
    return schemas_;
}


/**
Returns true if the user is able to select and change the tables that
are included in the query.  This property controls when the <i>table</i>
page is enabled or disabled.
The default value is true.

@return true if the user is allowed to change the tables in the
                  query; false otherwise.
**/
public boolean getUserSelectTables ()
{
    return tablesUserDefined_;
}


/**
Returns true if the user is able to select and change the schemas
for which tables are shown.
This property controls whether the
list of tables on the <i>table</i> page is changeable.
The default value is true.

@return true if the user is allowed to change the schemas
                  whose tables are shown; false otherwise.
**/
public boolean getUserSelectTableSchemas ()
{
    return schemasUserDefined_;
}


/**
Gets data from the system.
If the <i>connection</i> is null, the system JDBC driver will
be used, and the user will be prompted for sign-on information.
**/
synchronized public void load()
{
    Trace.log(Trace.DIAGNOSTIC, "Doing query builder load");
    worker_.startWorking(workEvent_);

    // Save info from current page.
    // Don't save tables, since that will wipe out the new tables value
    // if a setTables() was just done.
    if (currentPane_ != TABLE_)
        completePage();

    // Only do these functions if the user called load()
    // (we haven't called load ourselves).
    if (!internalLoad_)
    {
        // If connection not set or completely specified,
        // prompt for system, uid, password here.
        if (sqlconn_ == null)
        {
            sqlconn_ = new SQLConnection("jdbc:as400");
        }
        connection_ = null;
        try
        {
            connection_ = sqlconn_.getConnection();
        }
        catch (SQLException e)
        {
            panelErrors_.fireError(e);
            return;
        }
    }

    // Update fields_, notify other panes that the fields have
    // changed.
    fields_.setConnection(connection_);
    fields_.setTables(tables_);
    fields_.load();
    fieldListeners_.firePropertyChange(null, null, null);

    // Only do these functions if the user called load()
    // (we haven't called load ourselves).
    if (!internalLoad_)
    {
        // Update table pane
        tablePane_.setConnection(connection_);
        tablePane_.setTables(tables_);
        tablePane_.load();
        if (tablesUserDefined_)
        {
            tablePane_.setEnabled(true);
        }

        // Update current page.
        updatePage();
    }

    worker_.stopWorking(workEvent_);
}


/**
Restores the state of this object from an object input stream.
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
    addFocusListener(new SerializationListener(this)); //@B0A - for safe serialization later on
    // Initialize the transient fields.
    panelInit = new boolean[8];
    fieldListeners_ = new PropertyChangeSupport (this);
    changeListeners_ = new PropertyChangeSupport (this);
    vetoListeners_ = new VetoableChangeSupport (this);
    panelErrors_ = new ErrorEventSupport(this);
    fields_ = null;
    worker_ = new WorkingCursorAdapter(this);
    workEvent_ = new WorkingEvent(this);
    currentPane_ = TABLE_;
    loadDone_ = false;
    setupPane();
}


/**
Removes a listener from being notified when an error occurs.

@param  listener  The listener.
**/
public void removeErrorListener (ErrorListener listener)
{
    panelErrors_.removeErrorListener(listener);
}



/**
Removes a listener from being notified when the value of any bound
property is changed.

@param  listener  The listener.
**/
public void removePropertyChangeListener (PropertyChangeListener listener)
{
    changeListeners_.removePropertyChangeListener (listener);
    super.removePropertyChangeListener(listener);
}



/**
Removes a listener from being notified when the value of any constrained
property is changed.

@param  listener  The listener.
**/
public void removeVetoableChangeListener (VetoableChangeListener listener)
{
    vetoListeners_.removeVetoableChangeListener (listener);
    super.removeVetoableChangeListener(listener);
}



/**
Sets the SQL connection with which to access data for the GUI.
This property is bound and constrained.
Note that the data in the GUI will not change
until a <i>load()</i> is done.

@param       connection              The SQL connection.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setConnection (SQLConnection connection)
    throws PropertyVetoException
{
    SQLConnection old = sqlconn_;

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("connection", old, connection);

    // Make property change.
    sqlconn_ = connection;

    // Fire the property change event.
    changeListeners_.firePropertyChange("connection", old, connection);
}


/**
Sets the table names for the query.
This property is bound and constrained.
Note that the data in the GUI will not change
until a <i>load()</i> is done.

@param       tables                  The names of the tables used in the query.
                        Tables should be in the form of <em>schema.table</em>.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setTables (String[] tables)
    throws PropertyVetoException
{
    if (tables == null)
        throw new NullPointerException("tables");

    String[] old = tables_;

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("tables", old, tables);

    // Make property change.
    tables_ = tables;

    // Fire the property change event.
    changeListeners_.firePropertyChange("tables", old, tables);
}


/**
Sets the schemas for which the tables will be listed for inclusion
in the query.
The default is no schemas, so no tables will be listed.
This property is bound and constrained.

@param       tableSchemas   The schemas for which tables will be listed.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setTableSchemas (String[] tableSchemas)
    throws PropertyVetoException
{
    if (tableSchemas == null)
        throw new NullPointerException("tableSchemas");

    String[] old = schemas_;

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("tableSchemas", old, tableSchemas);

    // Make property change.
    synchronized (this)
    {
        schemas_ = tableSchemas;
        tablePane_.setSchemas(tableSchemas);
    }

    // Fire the property change event.
    changeListeners_.firePropertyChange("tableSchemas", old, tableSchemas);
}



/**
Sets whether the user will be able to select and change the tables that
are included in the query.  This property controls when the <i>table</i>
page is enabled or disabled.
The default value is true.
This property is bound and constrained.

@param       flag  true if the user is allowed to change the tables in the
                  query; false otherwise.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setUserSelectTables (boolean flag)
    throws PropertyVetoException
{
    Boolean old = new Boolean(tablesUserDefined_);

    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("userSelectTables", old, new Boolean(flag));

    // Make property change.
    tablesUserDefined_ = flag;

    // Update the GUI.
    tablePane_.setEnabled(flag);

    // Fire the property change event.
    changeListeners_.firePropertyChange("userSelectTables", old, new Boolean(flag));
}



/**
Sets whether the user will be able to select and change the schemas
for which tables are shown.
This property controls whether the
list of tables on the <i>table</i> page is changeable.
The default value is true.
This property is bound and constrained.

@param       flag  true if the user is allowed to change the schemas
                  whose tables are shown; false otherwise.
@exception  PropertyVetoException   If the change is vetoed.
**/
public void setUserSelectTableSchemas (boolean flag)
    throws PropertyVetoException
{
    // Fire a vetoable change event.
    vetoListeners_.fireVetoableChange("userSelectTableSchemas",
           new Boolean(schemasUserDefined_), new Boolean(flag));

    // Make property change.
    boolean old = schemasUserDefined_;
    schemasUserDefined_ = flag;

    // Update the GUI.
    synchronized (this)
    {
        tablePane_.setUserSelectTableSchemas(flag);
    }

    // Fire the property change event.
    changeListeners_.firePropertyChange("userSelectTableSchemas",
           new Boolean(old), new Boolean(flag));
}


/**
Builds the tabbed panels.
**/
private void setupPane()
{
    // Set up fields table
    fields_ = new SQLMetaDataTableModel(connection_, tables_);
    fields_.addErrorListener(panelErrors_);

    // Edge
	setLayout(new BorderLayout());

	// Build notebook control.
	// The panels are added in the same order as the
	// constants for the pane indices.
    // Add error listeners to all panels
	notebook_ = new JTabbedPane();
	tablePane_ = new SQLQueryTablePane(this, connection_, tables_,
	                        schemas_, schemasUserDefined_);
    tablePane_.addErrorListener(panelErrors_);
	notebook_.addTab(ResourceLoader.getQueryText ("DBQUERY_LABEL_TABLES"), null, tablePane_); // @A1C
	selectPane_ = new SQLQuerySelectPane(this);
	notebook_.addTab("Select", null, selectPane_);
	joinPane_ = new SQLQueryJoinPane(this);
	notebook_.addTab("Join By", null, joinPane_);
	wherePane_ = new SQLQueryWherePane(this);
	notebook_.addTab("Where", null, wherePane_);
	groupPane_ = new SQLQueryGroupPane(this);
	notebook_.addTab("Group By", null, groupPane_);
	havingPane_ = new SQLQueryHavingPane(this);
	notebook_.addTab("Having", null, havingPane_);
	orderPane_ = new SQLQueryOrderPane(this);
	notebook_.addTab("Order By", null, orderPane_);
	summaryPane_ = new SQLQuerySummaryPane(this);
	notebook_.addTab(ResourceLoader.getQueryText ("DBQUERY_LABEL_SUMMARY"), null, summaryPane_); // @A1C

//notebook_.setPreferredSize(new Dimension(600,500));
    notebook_.setSelectedIndex(0);
    tablePane_.init(); // Init initial pane.

    // Enable appropriate tabs.
    tablePane_.setEnabled(tablesUserDefined_);
    // If only one table, do not enable join.
    if (tables_.length == 1)
        notebook_.setEnabledAt(JOIN_,false);
    // If no tables, disable all but tables and summary pane.
    else if (tables_.length == 0)
    {
        notebook_.setEnabledAt(SELECT_,false);
        notebook_.setEnabledAt(JOIN_,false);
        notebook_.setEnabledAt(WHERE_,false);
        notebook_.setEnabledAt(GROUP_,false);
        notebook_.setEnabledAt(HAVING_,false);
        notebook_.setEnabledAt(ORDER_,false);
    }


    // Add listener for when pane changes.
    notebook_.addChangeListener(new Listener_());
    add("Center", notebook_);

}


/**
Updates the current page.
**/
synchronized /*private*/ void updatePage()
{
    Trace.log(Trace.DIAGNOSTIC, "Updating page " +  currentPane_);
    switch(currentPane_)
    {
        case TABLE_:
            // table panel is initialized at creation since it is
            // first panel displayed
            tablePane_.update();
            break;
        case SELECT_:
            if (!panelInit[SELECT_])
            {
                selectPane_.init();
                panelInit[SELECT_] = true;
                selectPane_.validate();
            }
            else
            {
                selectPane_.update();
            }
            break;
        case JOIN_:
            if (!panelInit[JOIN_])
            {
                joinPane_.init();
                panelInit[JOIN_] = true;
                joinPane_.validate();
            }
            else
            {
                joinPane_.update();
            }
            break;
        case WHERE_:
            if (!panelInit[WHERE_])
            {
                wherePane_.init();
                panelInit[WHERE_] = true;
                wherePane_.validate();
            }
            else
            {
                wherePane_.update();
            }
            break;
        case GROUP_:
            if (!panelInit[GROUP_])
            {
                groupPane_.init();
                panelInit[GROUP_] = true;
                groupPane_.validate();
            }
            else
            {
                groupPane_.update();
            }
            break;
        case HAVING_:
            if (!panelInit[HAVING_])
            {
                havingPane_.init();
                panelInit[HAVING_] = true;
                havingPane_.validate();
            }
            else
            {
                havingPane_.update();
            }
            break;
        case ORDER_:
            if (!panelInit[ORDER_])
            {
                orderPane_.init();
                panelInit[ORDER_] = true;
                orderPane_.validate();
            }
            else
            {
                orderPane_.update();
            }
            break;
        case SUMMARY_:
            if (!panelInit[SUMMARY_])
            {
                summaryPane_.init();
                panelInit[SUMMARY_] = true;
                summaryPane_.validate();
            }
            else
            {
                summaryPane_.update();
            }
            break;
        default:
            Trace.log(Trace.ERROR, "Unknown page " +  currentPane_);
    }  // end switch
}


/**
Class used to listen for events.
Change events are used to track when the page is turned.  The new
page will either be built or updated.
**/
private class Listener_
implements ChangeListener
{

  public void stateChanged(ChangeEvent e) 
  {
    worker_.startWorking(workEvent_);

    completePage();  // save current page
    currentPane_ = notebook_.getSelectedIndex();
    updatePage();    // update new page

    worker_.stopWorking(workEvent_);

  }

} // end of class Listener_

}
