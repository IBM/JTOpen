///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryJoinPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;



/**
The SQLQueryJoinPane class represents a panel which allows a
user to dynamically build the JOIN portion of an SQL query.
This panel is used for a page of the SQLQueryBuilderPane notebook.
**/
class SQLQueryJoinPane
extends SQLQueryFieldsPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// The variables and methods which have private commented out
// had to be made package scope since currently Internet Explorer
// does not allow inner class to access private items in their
// containing class.

/**
Constant indicating an inner join was chosen.
**/
public static final int INNER_JOIN = 1;
/**
Constant indicating an outer (left) join was chosen.
**/
public static final int OUTER_JOIN = 2;

// GUI components
private ButtonGroup joinButtonGroup_;
private JPanel joinButtonPanel_;
private DefaultListModel tableListModel_;
/*private*/ DoubleClickList tableList_;
/*private*/ DoubleClickList testList_;
/*private*/ DoubleClickList otherList_;
/*private*/ JRadioButton innerJoinButton_;
private JRadioButton outerJoinButton_;
private SQLQueryClause clause_;

// Keeps track of where we are in the clause.
private boolean secondField_ = false;

// Vector of the text for each join clause.
private Vector clauses_ = new Vector();
// The clause we are currently working on.
/*private*/ int current_ = 0;
// The clause we are currently working on if outer join.
// Used to save state if user switches between inner and outer.
/*private*/ int currentTable_ = 0;
// Keeps track of where we are in building the clause.
// This tells us which controls to enable/disable.
// 1=choose field #1, 2=choose test, 3=choose field #2
// 4=choose other
private Vector locationInClause_ = new Vector();

static final String [] testChoices = {"=", "<>", "<", ">", "<=", ">="};
static final    String [] otherChoices = {"AND"};

/**
Constructs a SQLQueryJoinPane object.
<i>init</i> must be called to build the GUI contents.

@param parent The parent of this panel.
**/
public SQLQueryJoinPane (SQLQueryBuilderPane parent)
{
    super(parent);
}



/**
Fills table list model with values, ensures the table vectors have
enough elements.
**/
private void buildTableList()
{
    String[] tables = parent_.getTables();
    int numTables = tables.length - 1;
    tableListModel_.removeAllElements();
    // First line we do the first plus the second table
    if (numTables > 0)
    {
        tableListModel_.addElement(tables[0] + " + " + tables[1]);
    }
    // Remaining lines we just do the scond table
    for (int i = 1; i < numTables; ++i)
    {
        tableListModel_.addElement("+ " + tables[i+1]);
    }
    // make sure the vectors contain enough elements
    while (clauses_.size() < numTables)
    {
        clauses_.addElement(new String());
        locationInClause_.addElement(new Integer(1));
    }
}


/**
Saves the last clause.
**/
public void complete()
{
    // clauses_ may be 0 if there are < 2 tables (pane disabled)
    if (clauses_.size() > 0)
        clauses_.setElementAt(clause_.getText(), current_);
}


/**
Gets the sql clause fo this panel.
@param The index of the join clause to return.
**/
public String getClause(int index)
{
    if (clause_ == null || clauses_.size() == 0)
        return null;
    return (String)(clauses_.elementAt(index));
}



/**
Returns the type of join that should be performed.
@return The type of join that should be performed.
**/
public int getJoinType()
{
    if (joinButtonGroup_ == null)
        return INNER_JOIN;
    ButtonModel selection = joinButtonGroup_.getSelection();
    if (selection == innerJoinButton_.getModel())
        return INNER_JOIN;
    return OUTER_JOIN;
}


/**
Called when an item in the other list is double-clicked on.
The request is processed, and the completed item is added
to the clause.

@param item The item that was chosen.
**/
/*private*/ void otherPicked(String item)
{
    // Currently only one item in other list - AND.
    // add to clause
    clause_.appendText(item);
    // disable other list, enable fields
    otherList_.setEnabled(false);
    fields_.setEnabled(true);
    locationInClause_.setElementAt(new Integer(1), current_);
}


/**
Adds the field to the clause, enables/disables pane controls.

@param index Index of the row in the table that was clicked upon.
**/
protected void rowPicked(int index)
{
    if (((Integer)locationInClause_.elementAt(current_)).intValue() == 1)  // first field
    {
        // Add field to clause
        clause_.appendText("(" + fieldName(index));

        // Enable the appropriate list box.
        testList_.setEnabled(true);
        locationInClause_.setElementAt(new Integer(2), current_);
    }
    else  // second field
    {
        // Add field to clause
        clause_.appendText(fieldName(index) + ")");

        // Enable the appropriate list box.
        otherList_.setEnabled(true);
        locationInClause_.setElementAt(new Integer(4), current_);
    }

    // Disable the fields table
    fields_.setEnabled(false);
}



/**
Builds the panel GUI components and sets up connections
between the components by using listeners.
**/
protected void setupPane()
{
    super.setupPane();

    // Toggle for type of join.
    JoinListener_ l = new JoinListener_();
    outerJoinButton_ = new JRadioButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_OUTER_JOIN"), false);
    outerJoinButton_.setName("joinOuterButton");
    outerJoinButton_.addActionListener(l);
    innerJoinButton_ = new JRadioButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_INNER_JOIN"), true);
    innerJoinButton_.setName("joinInnerButton");
    innerJoinButton_.addActionListener(l);
    joinButtonGroup_ = new ButtonGroup();
    joinButtonGroup_.add(innerJoinButton_);
    joinButtonGroup_.add(outerJoinButton_);
    Box buttonBox = Box.createVerticalBox();
    buttonBox.add(innerJoinButton_);
    buttonBox.add(outerJoinButton_);
    joinButtonPanel_ = new JPanel();
    joinButtonPanel_.setLayout(new BorderLayout());
    joinButtonPanel_.setBorder(new TitledBorder(ResourceLoader.getQueryText("DBQUERY_LABEL_JOIN_TYPE")));
    joinButtonPanel_.add("Center", buttonBox);
    
    // List box for tables
    tableListModel_ = new DefaultListModel();
    buildTableList();
    tableList_ = new DoubleClickList(tableListModel_);
    tableList_.setVisibleRowCount(3);
    tableList_.setSelectedIndex(current_);
    tableList_.setEnabled(false);
    tableList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                tablePicked(tableList_.getSelectedIndex());
            }
        });

    // Top row.
    Box topRow = Box.createHorizontalBox();
    topRow.add(joinButtonPanel_);
    topRow.add(new LabelledComponent("DBQUERY_LABEL_TABLES", tableList_));

    // List box for test
    testList_ = new DoubleClickList(testChoices);
    testList_.setEnabled(false);
    testList_.setVisibleRowCount(6);
    testList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                testPicked((String)event.getItem());
            }
        });

    // List box for others
    otherList_ = new DoubleClickList(otherChoices);
    otherList_.setEnabled(false);
    otherList_.setVisibleRowCount(6);
    otherList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                otherPicked((String)event.getItem());
            }
        });

    // Middle row.
    Box middleRow = Box.createHorizontalBox();
    middleRow.add(new LabelledComponent("DBQUERY_LABEL_TEST", testList_));
    middleRow.add(new LabelledComponent("DBQUERY_LABEL_OTHER", otherList_));

    // Text area
    clause_ = new SQLQueryClause(5);

    // Layout overall.
    Box overallBox = Box.createVerticalBox();
    overallBox.add(topRow);
    overallBox.add(fields_);
    overallBox.add(middleRow);
    overallBox.add(new LabelledComponent("DBQUERY_LABEL_CLAUSE_JOIN", new ScrollingTextPane(clause_)));
    setLayout(new BorderLayout());
    add("Center", overallBox);
}


/**
Called when an item in the table list is double-clicked on.
The clause and enabled items are updated.

@param item The item that was chosen.
**/
/*private*/ void tablePicked(int item)
{
    // Save current clause
    complete();
    // Update with new table
    clause_.setText((String)clauses_.elementAt(item));
    current_ = item;
    // enable appropriate controls
    switch(((Integer)locationInClause_.elementAt(item)).intValue())
    {
        case 1:  // pick field 1
        case 3:  // pick field 2
            fields_.setEnabled(true);
            testList_.setEnabled(false);
            otherList_.setEnabled(false);
            break;
        case 2:  // pick test
            fields_.setEnabled(false);
            testList_.setEnabled(true);
            otherList_.setEnabled(false);
            break;
        case 4:  // pick other
            fields_.setEnabled(false);
            testList_.setEnabled(false);
            otherList_.setEnabled(true);
            break;
    }
}


/**
Called when an item in the test list is double-clicked on.
The request is processed, and the completed item is added
to the clause.

@param item The item that was chosen.
**/
/*private*/ void testPicked(String item)
{
    // add to clause
    clause_.appendText(item);
    // disable test list, enable fields
    testList_.setEnabled(false);
    fields_.setEnabled(true);
    locationInClause_.setElementAt(new Integer(3), current_);
}



/**
Update the field model and table list if needed.
**/
public void update()
{
    boolean update = fieldsChanged_;  // save state before calling super
    super.update();  // update field model
    if (update)  // update table list
    {
        // rebuild the table list
        buildTableList();
        // verify the current indices are in range
        if (currentTable_ > parent_.getTables().length - 1)
        {
            currentTable_ = 0;
            if (getJoinType() == OUTER_JOIN)
                current_ = currentTable_;
        }
        tableList_.setSelectedIndex(current_);
    }
}




/**
Class which listens for click on the join type buttons
**/
private class JoinListener_
implements ActionListener
{
    public void actionPerformed (ActionEvent event)
    {
        // check who sent
        if (event.getSource() == innerJoinButton_)
        {
            if (tableList_.isEnabled()) //@B0A
            { //@B0A
              // disable table list
              tableList_.setEnabled(false);
              // update the clause with the first join condition
              tablePicked(0);
            } //@B0A
        }
        else
        {
            // enable table list
            tableList_.setEnabled(true);
            // update the clause with the current join condition
            tablePicked(currentTable_);
        }
    }
}



}
