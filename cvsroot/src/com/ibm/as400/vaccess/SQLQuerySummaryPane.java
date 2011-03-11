///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQuerySummaryPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JComponent;
import java.awt.BorderLayout;



/**
The SQLQuerySummaryPane class represents a panel which displays
an SQL query in a text pane.  The query is not editable.
This panel is used for a page of the SQLQueryBuilderPane notebook.
**/
class SQLQuerySummaryPane
extends JComponent
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// GUI components
private SQLQueryClause sql_;
private SQLQueryBuilderPane parent_;


/**
Constructs a SQLQuerySummaryPane object.
**/
public SQLQuerySummaryPane (SQLQueryBuilderPane parent)
{
    super();
    parent_ = parent;
}



/**
Build the panel GUI.
**/
public void init()
{
    setupPane();
    update();
}


/**
Builds the panel GUI components and sets up connections
between the components by using listeners.
**/
private void setupPane()
{
    // Text Area
    sql_ = new SQLQueryClause(20);
    sql_.setName("summaryClause");
    sql_.setEditable(false);

    setLayout(new BorderLayout());
    add("Center", new LabelledComponent("DBQUERY_LABEL_SQL", new ScrollingTextPane(sql_)));
}



/**
Update the text with new data.
**/
public void update()
{
    String sql = parent_.getQuery();
    if (sql == null)
        sql_.setText("");
    else
        sql_.setText(sql);
}

}
