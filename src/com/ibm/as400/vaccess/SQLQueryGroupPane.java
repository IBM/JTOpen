///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryGroupPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;


/**
The SQLQueryGroupPane class represents a panel which allows a
user to dynamically build the GROUP BY portion of an SQL query.
This panel is used for a page of the SQLQueryBuilderPane notebook.
**/
class SQLQueryGroupPane
extends SQLQueryFieldsPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// The variables which have private commented out had to made
// package scope since currently Internet Explorer does not
// allow inner class to access private variables in their
// containing class.

// GUI components
private SQLQueryClause clause_;

// Indicates if the selected items has changed.
/*private*/ boolean selectChanged_ = false;
// Listen to changes in the selected items.
private SelectListener_ selectListener_ = null;


/**
Constructs a SQLQueryGroupPane object.
<i>init</i> must be called to build the GUI contents.

@param parent The parent of this panel.
**/
public SQLQueryGroupPane (SQLQueryBuilderPane parent)
{
    super(parent);
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
    super.init();
}


/**
Adds the field to the clause.

@param index Index of the row in the table that was clicked upon.
**/
protected void rowPicked(int index)
{
    clause_.appendTextWithComma(fieldName(index));
}



/**
Builds the panel GUI components and sets up connections
between the components by using listeners.
**/
protected void setupPane()
{
    super.setupPane();

    clause_ = new SQLQueryClause(5);

    Box overallBox = Box.createVerticalBox();
    overallBox.add(fields_);
    overallBox.add(new LabelledComponent("DBQUERY_LABEL_CLAUSE_GROUP", new ScrollingTextPane(clause_)));
    setLayout(new BorderLayout());
    add("Center", overallBox);

}


/**
Update the fieldModel if needed.
**/
public void update()
{
    // Force reload of field table if selected items have changed.
    if (selectChanged_)
    {
        fieldListener_.propertyChange(new PropertyChangeEvent(this, "fields", null, null));
    }

    super.update();
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
