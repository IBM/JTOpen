///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryTablePane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;


/**
The SQLQueryTablePane class represents a panel which allows a
user to select the tables used to build an SQL query.
This panel is used for a page of the SQLQueryBuilderPane notebook.
**/
class SQLQueryTablePane
extends JComponent
implements Serializable //@B0A - for consistency
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// In AS400 language, library = schema, system = catalog.

// This class is not meant to be serialized, it should be transient.
// This class has items marked transient even though it is not
// serializable because otherwise errors were received when
// serializing objects that contained this class (even though they
// were transient instances.  readObject() was added to be safe.

// The variables and methods which have private commented out
// had to be made package scope since currently Internet Explorer
// does not allow inner class to access private items in their
// containing class.

// GUI components
private JButton tableButton_ = null;
private JLabel catalog_ = null;
private transient JTable tablesTable_; //@B0C - made transient
private boolean tablesShown_ = false;
private JScrollPane tablesPane_;
/*private*/ SQLQueryClause clause_;
/*private*/ JDialog dialog_;

// Data for GUI
private String catalogName_ = "";
private DefaultTableModel tablesTableModel_;

// List of tables the user has chosen.
private String[] tables_;
/*private*/ String[] schemas_;
/*private*/ boolean userSelectTableSchemas_;
private DefaultListModel schemaListModel_;
transient private Connection connection_;

// Tracks if the user changed the clause.
/*private*/ boolean changes_ = false;
transient private DocumentListener_ docListener_;

// Event support.
transient private ErrorEventSupport errors_ = new ErrorEventSupport(this);
transient private WorkingCursorAdapter worker_ = null;         // @A2
transient private WorkingEvent workEvent_ = null;              // 

/*private*/ SQLQueryBuilderPane parent_;

private boolean enabled_ = true;  // Whether this pane is enabled
private boolean buttonEnabled_;   // Whether the button is enabled

//@B0A: Has this table been init()-ed yet? (Used upon de-serialization)
private boolean inited_ = false; //@B0A


/**
Constructs a SQLQueryTablePane object.

@param parent The parent of this panel.
@param connection The SQL Connection.
@param tables The tables to initialize the clause with.
@param schemas The schemas to display the tables for.
@param  userSelectTableSchemas  true if the user is allowed
                  to change the schemas
                  whose tables are shown; false otherwise.
**/
public SQLQueryTablePane (SQLQueryBuilderPane parent,
                          Connection connection,
                          String[] tables,
                          String[] schemas,
                          boolean userSelectTableSchemas)
{
    super();
    parent_ = parent;
    connection_ = connection;
    tables_ = tables;
    schemas_ = schemas;
    userSelectTableSchemas_ = userSelectTableSchemas;
    tablesTableModel_ = new DefaultTableModel(){
        public boolean isCellEditable(int row, int column) {return false;}
        };
    addFocusListener(new SerializationListener(this)); //@B0A - for safe serialization
}


/**
Adds a listener to be notified when an error occurs.
The listener's errorOccurred method will be called.

@param  listener  The listener.
**/
public void addErrorListener (ErrorListener listener)
{
    errors_.addErrorListener(listener);
}


/**
Parse clause to determine the tables selected.
**/
public void complete()
{
    if (changes_)
    {
        // Parse the text area for the table names.
        StringTokenizer clause = new StringTokenizer(clause_.getText(), ",");
        Vector tables = new Vector();
        while (clause.hasMoreTokens())
        {
            tables.addElement(clause.nextToken().trim());
        }
        String[] newTables = new String[tables.size()];
        tables.copyInto(newTables);

        // Determine if the tables have changed.
        boolean different = false;
        if (tables_.length == newTables.length)
        {
            for (int i=0; i < tables_.length; ++i)
            {
                // note even order changes are considered changes
                if (!tables_[i].equals(newTables[i]))
                {
                    different = true;
                    break;
                }
            }
        }
        else
            different = true;
        if (different)
            tables_ = newTables;
        // Listen for more changes to the tables.
        changes_ = false;
    }
}


/**
Set the enabled state of the button.
**/
private void enableButton(boolean enable)
{
    buttonEnabled_ = enable;
    if (enabled_)
        tableButton_.setEnabled(enable);
}



/**
Fills the clause with the tables.
**/
private void fillClause()
{
    // Add tables to the edit area.
    if (tables_.length > 0 && clause_ != null)
    {
        StringBuffer temp = new StringBuffer(tables_[0]);
        for (int i = 1; i<tables_.length; ++i)
        {
            temp.append(", ");
            temp.append(tables_[i]);
        }
        clause_.setText(temp.toString());
    }
    else
        clause_.setText("");
}


/**
Returns the copyright.
**/
private static String getCopyright()
{
    return Copyright_v.copyright;
}


/**
Returns the schemas for which the tables will be listed for inclusion
in the query.
@return The schema names.
**/
public String[] getSchemas ()
{
    return schemas_;
}



/**
Returns the tables selected for the query.

@return The tables selected for the query.
**/
public String[] getTables()
{
    return tables_;
}



/**
Build the panel GUI.
**/
public void init()
{
    setupPane();
    inited_ = true; //@B0A - so we know what state we are in if deserialized
}


/**
Gets data from the system for the pane.
**/
public void load()
{
    if (tablesShown_)
    {
        // Change button to function as way to show table list.
        tableButton_.setText(ResourceLoader.getQueryText("DBQUERY_BUTTON_DISPLAY_TABLES"));
        // Remove any old tables.
        for (int i = tablesTableModel_.getRowCount(); i>0;)
        {
            tablesTableModel_.removeRow(--i);
        }
        tablesShown_ = false;
    }

    // Get catalog name.
    if (connection_ != null)  // if null, leave blank
    {
        enableButton(true);
        try
        {
            catalog_.setText(connection_.getCatalog());
        }
        catch(SQLException e)
        {
            errors_.fireError(e);
        }
    }
    else
        enableButton(false);
}


/**
Get the list of tables.
**/
void loadTables()
{
    worker_.startWorking(workEvent_);   // @A2

    if (!tablesShown_)
    {
        // Change button to function as way to change schemas.
        tableButton_.setText(ResourceLoader.getQueryText("DBQUERY_BUTTON_CHANGE_SCHEMAS"));
        if (userSelectTableSchemas_)
            enableButton(true);
        else
            enableButton(false);
        tablesShown_ = true;
    }

    // Remove any old tables.
    for (int i = tablesTableModel_.getRowCount(); i>0;)
    {
        tablesTableModel_.removeRow(--i);
    }

    // Add tables to table.
    String schema;
    ResultSet results = null;
    try
    {
        DatabaseMetaData meta =  connection_.getMetaData();
        results = meta.getTableTypes();
        Vector types1 = new Vector();
        while (results.next())
        {
            types1.addElement(results.getString(1));
        }
        String[] types = new String[types1.size()];
        types1.copyInto(types);
        for (int i=0; i<schemas_.length; ++i)
        {
            schema = schemas_[i];
            results = meta.getTables(null, schema, "%", types);
            String[] row = new String[4];
            while (results.next())
            {
                // Do not add catalog.
                row[0]=results.getString(2).trim(); // schema
                row[1]=results.getString(3).trim(); // table
                row[2]=results.getString(4);        // type
                row[3]=results.getString(5).trim(); // description
                tablesTableModel_.addRow(row);
            }
        }
    }
    catch (SQLException e)
    {
        errors_.fireError(e);
    }
    finally
    {
        worker_.stopWorking(workEvent_);  // @A2

        if (results != null)
        {
            try
            {
                results.close();
            }
            catch(SQLException e)
            {
                errors_.fireError(e);
            }
        }
    }
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
    // Initialize the transient fields.
    errors_ = new ErrorEventSupport (this);
    connection_ = null;
    addFocusListener(new SerializationListener(this)); //@B0A - for safe serialization next time
    if (inited_) init(); //@B0A - preserve state of object upon deserialization
}


/**
Removes a listener from being notified when an error occurs.

@param  listener  The listener.
**/
public void removeErrorListener (ErrorListener listener)
{
    errors_.removeErrorListener(listener);
}


/**
Adds the table to the clause.

@param index Index of the row in the table that was clicked upon.
**/
protected void rowPicked(int index)
{
    String text = tablesTable_.getValueAt(index, 0) + "." +
        tablesTable_.getValueAt(index, 1);
    clause_.appendTextWithComma(text);
}


/**
Sets the system this pane contains data for.
The new value will not be in effect until a load() is done.

@param tables The system this pane will contain data for.
**/
public void setConnection (Connection connection)
{
    connection_ = connection;
}


/**
Enables or disables this pane
@param enabled true if the pane should be enabled, false otherwise.
**/
public void setEnabled(boolean enabled)
{
    enabled_ = enabled;
    clause_.setEditable(enabled);
    tablesTable_.setEnabled(enabled);
    if (buttonEnabled_)
        tableButton_.setEnabled(enabled);
}


/**
Sets the database files the query is being built with.
@param tables The database files the query is being built with.
**/
public void setTables (String[] tables)
{
    tables_ = tables;
    fillClause();
}


/**
Sets the schemas for which the tables will be listed for inclusion
in the query.  This will not affect the list of tables currently
being shown, it will only affect the schema dialog the next time
it is shown.
@param schemas The schema names.
**/
public void setSchemas (String[] schemas)
{
    schemas_ = schemas;
    if (dialog_ != null)
    {
        schemaListModel_.removeAllElements();
        for (int i=0; i < schemas_.length; ++i)
        {
            schemaListModel_.addElement(schemas_[i]);
        }
    }
}



/**
Sets whether the user will be able to select and change the schemas
from which tables are shown.

@param       flag  true if the user is allowed to change the schemas
                  whose tables are shown; false otherwise.
**/
public void setUserSelectTableSchemas (boolean flag)
{
    userSelectTableSchemas_ = flag;
    if (tablesShown_ && tableButton_ != null)
    {
        if (userSelectTableSchemas_)
            enableButton(true);
        else
            enableButton(false);
    }
}


/**
Builds the panel GUI components and sets up connections
between the components by using listeners.
**/
private void setupPane()
{
    // Add catalog name to screen.
    JLabel catalogLabel = new JLabel(ResourceLoader.getQueryText("DBQUERY_LABEL_CATALOG"));
    catalog_ = new JLabel(catalogName_);
    Box catalogBox = Box.createHorizontalBox();
    catalogBox.add(Box.createHorizontalStrut(5));
    catalogBox.add(catalogLabel);
    catalogBox.add(Box.createHorizontalStrut(5));
    catalogBox.add(catalog_);
    catalogBox.add(Box.createHorizontalGlue());

    // Button for displaying the tables, and then changing the schemas
    // listed.
    tableButton_ = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_DISPLAY_TABLES"));
    if (connection_ != null)
        enableButton(true);
    else
        enableButton(false);
    tableButton_.addActionListener(new ButtonListener_());
    Box buttonBox = Box.createHorizontalBox();
    buttonBox.add(Box.createHorizontalStrut(5));
    buttonBox.add(tableButton_);
    buttonBox.add(Box.createHorizontalGlue());

    // Adapter for listening for working events and enabling working cursor.
    worker_ = new WorkingCursorAdapter(this);     // @A2
    workEvent_ = new WorkingEvent(this);          // @A2

    // Multi-column listbox for tables.
    // Create table of tables.
    // Build columns for table
    String header = ResourceLoader.getQueryText("DBQUERY_COLUMN_TABLE_SCHEMA");
    tablesTableModel_.addColumn(header);
    header = ResourceLoader.getQueryText("DBQUERY_COLUMN_TABLE_NAME");
    tablesTableModel_.addColumn(header);
    header = ResourceLoader.getQueryText("DBQUERY_COLUMN_TABLE_TYPE");
    tablesTableModel_.addColumn(header);
    header = ResourceLoader.getQueryText("DBQUERY_COLUMN_TABLE_TEXT");
    tablesTableModel_.addColumn(header);
    tablesTable_ = new JTable(tablesTableModel_);
    tablesTable_.setName("tableTableTable");
    tablesTable_.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN); //@B0C
    tablesTable_.setShowGrid(false);
    tablesTable_.getColumnModel().getColumn(0).setPreferredWidth(100); //@B0C
    tablesTable_.getColumnModel().getColumn(1).setPreferredWidth(100); //@B0C
    tablesTable_.getColumnModel().getColumn(2).setPreferredWidth(70);  //@B0C
    tablesTable_.getColumnModel().getColumn(3).setPreferredWidth(180); //@B0C
    tablesPane_ = new JScrollPane (tablesTable_); // @A1C
    tablesPane_.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), tablesPane_.getBorder()));
    // Listen for double clicks on table.
    final JTable table2 = tablesTable_;  // need final for anonymous class
    table2.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked (MouseEvent event)
            {
                if (event.getClickCount () > 1) // if double click
                {
                    // Get the object that was double clicked, if any.
                    int tablerow = table2.rowAtPoint(event.getPoint());
                    if (tablerow != -1)  // -1 means no object under mouse
                    {
                        rowPicked(tablerow);
                    }
                }
            }
        }
    );

    // Edit area for having clause.
    clause_ = new SQLQueryClause(5);

    fillClause();

    // Listen for any changes to the tables.
    boolean otherPanesEnabled = tables_.length==0?false:true;
    boolean enableOrder = parent_.getSelectedItems().length==0?false:true;
    docListener_ = new DocumentListener_(otherPanesEnabled, enableOrder);
    clause_.getDocument().addDocumentListener(docListener_);

    // Overall layout.
    Box overallBox = Box.createVerticalBox();
    overallBox.add(Box.createVerticalStrut(10));
    overallBox.add(catalogBox);
    overallBox.add(Box.createVerticalStrut(10));
    overallBox.add(buttonBox);
    overallBox.add(Box.createVerticalStrut(10));
    overallBox.add(tablesPane_);
    overallBox.add(Box.createVerticalStrut(10));
    overallBox.add(new LabelledComponent("DBQUERY_LABEL_TABLES", new ScrollingTextPane(clause_)));
    setLayout(new BorderLayout());
    add("Center", overallBox);
}


/**
Builds the dialog in which the user selects the schemas for which
tables are displayed.
**/
/*private*/ void setupSchemaDialog()
{
    JLabel prompt = new JLabel(ResourceLoader.getQueryText("DBQUERY_TEXT_SCHEMAS"));
    JLabel prompt2 = new JLabel(ResourceLoader.getQueryText("DBQUERY_TEXT_SCHEMAS2"));

    prompt.setBorder(new EmptyBorder(10,10,5,10));
    prompt2.setBorder(new EmptyBorder(5,10,10,10));
    JPanel promptPanel = new JPanel(new BorderLayout());
    promptPanel.add("North",prompt);
    promptPanel.add("South",prompt2);
    promptPanel.getAccessibleContext().setAccessibleName("PROMPT");	// @A3

    final JTextField entry = new JTextField(12);
    schemaListModel_ = new DefaultListModel();
    for (int i=0; i < schemas_.length; ++i)
    {
        schemaListModel_.addElement(schemas_[i]);
    }
    final JList schemas = new JList(schemaListModel_);
    JScrollPane listScroll = new JScrollPane(schemas);
    schemas.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    final DefaultListModel model = schemaListModel_;

    JButton add = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_ADD"));
    add.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent ev)
            {
                String s = entry.getText();
                if (!s.equals(""))
                {
                    model.addElement(s);
                    entry.setText("");
                }
            }
        }  // end of ActionListenerAdapter
    );

    JButton remove = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_REMOVE"));
    remove.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent ev)
            {
                int[] selected = schemas.getSelectedIndices();
                for (int i = selected.length; i>0;)
                {
                    model.removeElementAt(selected[--i]);
                }
            }
        }  // end of ActionListenerAdapter
    );

    JButton okButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_OK"));
    okButton.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent ev)
            {
                Object[] items = model.toArray();
                String[] libs = new String[items.length];
                for (int i = 0; i < items.length; ++i)
                {
                    libs[i] = (String)items[i];
                }
                schemas_ = libs;
                loadTables();
                // End dialog
                dialog_.dispose();
            }
        }  // end of ActionListenerAdapter
    );

    JButton cancelButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_CANCEL"));
    cancelButton.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent ev)
            {
                // Set back the list for next time.
                model.removeAllElements();
                for (int i=0; i < schemas_.length; ++i)
                {
                    model.addElement(schemas_[i]);
                }
                dialog_.dispose();
            }
        }
    );

    GridBagLayout layout = new GridBagLayout();
    JPanel body = new JPanel(layout);
    body.getAccessibleContext().setAccessibleName("BODY");  		// @A3
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(5,5,5,5);
    VUtilities.constrain (entry, body, layout, constraints,
            0, 1, 100,
            0, 1, 0,
            GridBagConstraints.BOTH, GridBagConstraints.CENTER);
    VUtilities.constrain (add, body, layout, constraints,
            1, 1, 0,
            0, 1, 0,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
    VUtilities.constrain (listScroll, body, layout, constraints,
            0, 1, 100,
            1, 1, 100,
            GridBagConstraints.BOTH, GridBagConstraints.CENTER);
    VUtilities.constrain (remove, body, layout, constraints,
            1, 1, 0,
            1, 1, 100,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    buttonPanel.getAccessibleContext().setAccessibleName("BUTTON");    // @A3

    dialog_ = new JDialog(VUtilities.getFrame(this), // parent
                          ResourceLoader.getQueryText("DBQUERY_TITLE_SCHEMAS"), // title
                          true); // modal
    dialog_.getContentPane().setLayout(new BorderLayout());
    dialog_.getContentPane().add("North", promptPanel);
    dialog_.getContentPane().add("Center", body);
    dialog_.getContentPane().add("South", buttonPanel);
    dialog_.addWindowListener(
        new WindowAdapter() {
            public void windowClosing(WindowEvent ev)
            {dialog_.dispose();}
        }
    );
    dialog_.pack();
    dialog_.setLocationRelativeTo(this);

    // @A3
    dialog_.addFocusListener(new FocusAdapter()
    {
       public void focusGained(FocusEvent e)
       {
          // Set default focus in TextField of main body panel.   
          for (int index=0; index< dialog_.getContentPane().getComponentCount(); index++) {
             JComponent inputPanel = (JComponent) dialog_.getContentPane().getComponent(index);
             if (inputPanel.getAccessibleContext().getAccessibleName().equals("BODY")) {
                // get TextField component and request focus.
                for (int i=0; i<inputPanel.getComponentCount(); i++) {                                                            
                   if (inputPanel.getComponent(i) instanceof JTextField) 
                   {
                      inputPanel.getComponent(i).requestFocus();
                      return;
                   }
                }
                return;
             }
          }
       }
    }  );

}


/**
Update the document listener if needed.
**/
void update()
{
    docListener_.enableOrder_ =
        parent_.getSelectedItems().length==0?false:true;
    if (docListener_.enableOrder_)
        parent_.notebook_.setEnabledAt(parent_.ORDER_, true);
}


/**
Class for listening to document events.
**/
private class DocumentListener_
implements DocumentListener
{
    boolean enableOrder_;         // whether to enable order pane
    boolean enabled_;             // if other tabs are enabled
    public DocumentListener_(boolean enabled, boolean enableOrder)
    {
        enabled_ = enabled;
        enableOrder_ = enableOrder;
    }
    public void insertUpdate(DocumentEvent e)
    {
        changes_ = true;
        String text = clause_.getText();
        if (!enabled_ && !text.equals(""))
        {
            enabled_ = true;
            parent_.notebook_.setEnabledAt(parent_.SELECT_, true);
            parent_.notebook_.setEnabledAt(parent_.WHERE_, true);
            parent_.notebook_.setEnabledAt(parent_.GROUP_, true);
            parent_.notebook_.setEnabledAt(parent_.HAVING_, true);
            parent_.notebook_.setEnabledAt(parent_.JOIN_, true);
            if (enableOrder_)
                parent_.notebook_.setEnabledAt(parent_.ORDER_, true);
        }

        int comma = text.indexOf(",");
        if (comma == -1 || comma == text.length()-1)
            parent_.notebook_.setEnabledAt(parent_.JOIN_, false);
        else
            parent_.notebook_.setEnabledAt(parent_.JOIN_, true);
    }
    public void changedUpdate(DocumentEvent e)
    {
    }
    public void removeUpdate(DocumentEvent e)
    {
        changes_ = true;
        String text = clause_.getText();
        if (text.equals(""))
        {
            enabled_ = false;
            parent_.notebook_.setEnabledAt(parent_.SELECT_, false);
            parent_.notebook_.setEnabledAt(parent_.WHERE_, false);
            parent_.notebook_.setEnabledAt(parent_.GROUP_, false);
            parent_.notebook_.setEnabledAt(parent_.HAVING_, false);
            parent_.notebook_.setEnabledAt(parent_.JOIN_, false);
            parent_.notebook_.setEnabledAt(parent_.ORDER_, false);
        }
        else
        {
            int comma = text.indexOf(",");
            if (comma == -1 || comma == text.length()-1)
                parent_.notebook_.setEnabledAt(parent_.JOIN_, false);
        }
    }
}

/**
Class for listening to document events.
**/
private class ButtonListener_
implements ActionListener
{
    public void actionPerformed(ActionEvent ev)
    {
        // Display dialog allowing user to change schemas.
        if (userSelectTableSchemas_)
        {
            // Build dialog if not already done.
            if (dialog_ == null)
            {
                setupSchemaDialog();
            }
            // Show dialog.
            dialog_.setVisible(true);
        }
        else
        {
            // If get here, the tables weren't previously shown,
            // and the user cannot change the schemas.
            // Update panel with list of tables.
            loadTables();
        }
    }
}


}
