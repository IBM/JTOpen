///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryOrderPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.swing.Box;
import javax.swing.JComponent;


/**
The SQLQueryOrderPane class represents a panel which allows a
user to dynamically build the ORDER BY portion of an SQL query.
This panel is used for a page of the SQLQueryBuilderPane notebook.
**/
class SQLQueryOrderPane
extends JComponent
implements Serializable //@B0A - added it for consistency
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// The variables and methods which have private commented out
// had to be made package scope since currently Internet Explorer
// does not allow inner class to access private items in their
// containing class.

// GUI components
private SQLQueryBuilderPane parent_;
private transient DoubleClickList selectItems_; //@B0C - made transient
/*private*/ DoubleClickList otherList_;
private SQLQueryClause clause_;

// Indicates if the selected items has changed.
/*private*/ boolean selectChanged_ = false;
// The fields that are selected
private String[] selectFields_;
// Listen to changes in the selected items.
protected SelectListener_ selectListener_ = null;

//@B0A: Has this table been init()-ed yet? (Used upon de-serialization)
private boolean inited_ = false; //@B0A

private static final String [] otherChoices = {"ASC", "DESC"};

/**
Constructs a SQLQueryOrderPane object.
<i>init</i> must be called to build the GUI contents.

@param parent The parent of this panel.
**/
public SQLQueryOrderPane (SQLQueryBuilderPane parent)
{
    super();
    parent_ = parent;
    initializeTransient(); //@B0A
}



/**
Returns the sql clause for this panel.
@return The sql clause for this panel.
**/
public String getClause()
{
    if (clause_ == null)
        return null;
    return clause_.getText();
}



/**
Build the panel GUI.
**/
public void init()
{
    // Add listener to changes to selected items.
    selectListener_ = new SelectListener_();
    parent_.addSelectListener(selectListener_);
    // Set up GUI.
    setupPane();
    inited_ = true; //@B0A - so we know what state we are in if deserialized
}


//@B0A
/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
      addFocusListener(new SerializationListener(this)); // for safe serialization next time
      if (inited_) init(); // preserve state of object upon deserialization
    }


/**
Called when an item in the other list is double-clicked on.
The request is processed, and the completed item is added
to the clause.

@param item The item that was chosen.
**/
/*private*/ void otherPicked(String item)
{
    // Add to clause
    clause_.appendText(item);
    otherList_.setEnabled(false);
}


//@B0A
/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();        
        initializeTransient();
    }


/**
Adds the field to the clause.

@param index Index of the row in the table that was clicked upon.
**/
protected void rowPicked()
{
    String item = (String)((Object[])selectItems_.getSelectedObjects())[0];
    clause_.appendTextWithComma(item);
    otherList_.setEnabled(true);
}


/**
Returns whether the item is a field name.
@return true if the item is a field name, false otherwise.
**/
private boolean isField(String item)
{
    for (int i=0; i < selectFields_.length; ++i)
    {
        if (item.equals(selectFields_[i]))
            return true;
    }
    return false;
}


/**
Builds the panel GUI components and sets up connections
between the components by using listeners.
**/
protected void setupPane()
{
    // Select items.
    selectFields_ = parent_.getSelectedFields();
    selectItems_ = new DoubleClickList(parent_.getSelectedItems());
    selectItems_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                rowPicked();
            }
        });
    
    // Other list.
    otherList_ = new DoubleClickList(otherChoices);
    otherList_.setVisibleRowCount(2);
    otherList_.setEnabled(false);
    otherList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                otherPicked((String)event.getItem());
            }
        });

    // Arrange top row.
    Box topRow = Box.createHorizontalBox();
    topRow.add(new LabelledComponent("DBQUERY_COLUMN_SELECT", selectItems_));
    topRow.add(new LabelledComponent("DBQUERY_LABEL_OTHER", otherList_));
    
    // Query clause.
    clause_ = new SQLQueryClause(5);

    // Overall layout.
    Box overallBox = Box.createVerticalBox();
    overallBox.add(topRow);
    overallBox.add(new LabelledComponent("DBQUERY_LABEL_CLAUSE_ORDER", new ScrollingTextPane(clause_)));
    setLayout(new BorderLayout());
    add("Center", overallBox);
}



/**
Update the selectItems_ if needed.
**/
public void update()
{
    if (selectChanged_)
    {
        selectItems_.setListData(parent_.getSelectedItems());

        // update the selected fields
        selectFields_ = parent_.getSelectedFields();

        selectChanged_ = false;
    }
}



/**
Class to listen for property changes on the selected items.
**/
private class SelectListener_
implements PropertyChangeListener
{
    public void propertyChange(PropertyChangeEvent event)
    {
        selectChanged_ = true;
    }
}

}
