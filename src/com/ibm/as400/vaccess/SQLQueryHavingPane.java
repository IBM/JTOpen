///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryHavingPane.java
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
import javax.swing.Box;
import javax.swing.JOptionPane;



/**
The SQLQueryHavingPane class represents a panel which allows a
user to dynamically build the HAVING portion of an SQL query.
This panel is used for a page of the SQLQueryBuilderPane notebook.
**/
class SQLQueryHavingPane
extends SQLQueryFieldsPane
// Even though this panel does not have a field table on it, it
// extend SQLQueryFields panel because it needs to be aware of
// the fields to do its processing of functions.
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// The variables and methods which have private commented out
// had to be made package scope since currently Internet Explorer
// does not allow inner class to access private items in their
// containing class.

// GUI components
/*private*/ DoubleClickList notList_;
/*private*/ DoubleClickList functionList_;
/*private*/ DoubleClickList testList_;
/*private*/ DoubleClickList functionList2_;
/*private*/ DoubleClickList otherList_;
private SQLQueryClause clause_;

// Values for function list boxes.  Note these are not translated.
private static final String FCN_COUNT_ = "COUNT";
private static final String FCN_SUM_ = "SUM";
private static final String FCN_AVG_ = "AVG";
private static final String FCN_MIN_ = "MIN";
private static final String FCN_MAX_ = "MAX";

// Keeps track of last choice
private int lastChoice_;
// Is NOT in affect for this expression.
private boolean notInEffect_ = false;
// Statics for each list box user could have chosen from.
private static final int NOT_       = 1;
private static final int FUNCTION_  = 2;
private static final int TEST_      = 3;
private static final int FUNCTION2_ = 4;
private static final int OTHER_     = 5;

private static final String [] notChoices = {"NOT"};
private static final String [] functionChoices = {FCN_AVG_, FCN_COUNT_, FCN_MIN_, FCN_MAX_, FCN_SUM_};
private static final String [] testChoices = {"=", "<>", "<", ">", "<=", ">="};
private static final String [] functionChoices2 = {
        "<" + ResourceLoader.getQueryText("DBQUERY_CHOICE_CONSTANT") + ">",
        FCN_AVG_, FCN_COUNT_, FCN_MIN_, FCN_MAX_, FCN_SUM_};
private static final String [] otherChoices = {"AND", "OR"};


/**
Constructs a SQLQueryHavingPane object.
**/
public SQLQueryHavingPane (SQLQueryBuilderPane parent)
{
    super(parent);
}


/**
Called when an item in the function list is double-clicked on.
The request is processed, requesting additional information
from the user if needed, and the completed item is added
to the clause.

@param item The item that was chosen.
**/
/*private*/ void functionPicked(String item)
{
    // Determine what to add to clause.
    String result;
    if (item.equals(FCN_COUNT_))
    {
        // no extra info needed
        result = "COUNT(*)";
    }
    else if (item.equals(FCN_SUM_) || item.equals(FCN_MIN_) ||
             item.equals(FCN_MAX_) || item.equals(FCN_AVG_))
    {
        // Show dialog to have user choose field.
        String[] choices;
        if (item.equals(FCN_SUM_) || item.equals(FCN_AVG_))
        {
            // only numeric fields are valid
            choices = getNumericFieldNames();
        }
        else
        {
            // all fields are valid
            choices = getFieldNames();
        }
        if (choices.length ==0)
        {
            // put up error message and return
            JOptionPane.showMessageDialog(this, // parent
                ResourceLoader.getQueryText("DBQUERY_MESSAGE_NO_FIELDS") + " " + item + "()", // message
                item + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                JOptionPane.ERROR_MESSAGE ); // message type
            return;
        }
        Object choice = JOptionPane.showInputDialog(this, // parent
            ResourceLoader.getQueryText("DBQUERY_TEXT_CHOOSE") + " " + item + "()", // message
            item, // title
            JOptionPane.QUESTION_MESSAGE,   // message type
            null, // icon
            choices,  // choices
            choices[0]);  // initial choice
        if (choice == null)  // null means they cancelled
            return;
        result = item + "(" + choice + ")";
    }
    else // must be constant expression
    {
        result = JOptionPane.showInputDialog(this, // parent
            ResourceLoader.getQueryText("DBQUERY_TEXT_CONSTANT"), // message
            ResourceLoader.getQueryText("DBQUERY_TITLE_CONSTANT"), // title
            JOptionPane.QUESTION_MESSAGE);   // message type
        if (result == null)  // means they cancelled
            return;
    }

    // Add parenthesis and spaces as needed
    if (lastChoice_ == TEST_)  // choice is from second function list
    {
        if (notInEffect_)
            result = " " + result + "))";
        else
            result = " " + result + ")";
    }
    else  // choice is from first function list
    {
        result = "(" + result; 
    }

    // Add text to clause
    clause_.appendText(result);

    // Make appropriate controls available.
    if (lastChoice_ == TEST_)
    {
        otherList_.setEnabled(true);
        functionList2_.setEnabled(false);
        lastChoice_ = FUNCTION2_; // choice is from second function list
    }
    else
    {
        testList_.setEnabled(true);
        notList_.setEnabled(false);
        functionList_.setEnabled(false);
        lastChoice_ = FUNCTION_;   // choice is from first function list
    }

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
    setupPane();
}


/**
Called when an item in the not list is double-clicked on.
The request is processed, the completed item is added
to the clause.

@param item The item that was chosen.
**/
/*private*/ void notPicked(String item)
{
    notInEffect_ = true;

    // Add text to clause
    clause_.appendText("(" + item);

    // Make appropriate controls available.
    notList_.setEnabled(false);
    lastChoice_ = NOT_;

}


/**
Called when an item in the other list is double-clicked on.
The request is processed, the completed item is added
to the clause.

@param item The item that was chosen.
**/
/*private*/ void otherPicked(String item)
{
    // Add text to clause
    clause_.appendText(item);

    // Make appropriate controls available.
    notList_.setEnabled(true);
    functionList_.setEnabled(true);
    otherList_.setEnabled(false);
    lastChoice_ = OTHER_;
    notInEffect_ = false;
}


/**
Builds the panel GUI components and sets up connections
between the components by using listeners.
**/
protected void setupPane()
{
    super.setupPane();

    // List box for not
    notList_ = new DoubleClickList(notChoices);
    notList_.setVisibleRowCount(6);
    notList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                notPicked((String)event.getItem());
            }
        });

    // List box for functions
    functionList_ = new DoubleClickList(functionChoices);
    functionList_.setVisibleRowCount(6);
    functionList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                functionPicked((String)event.getItem());
            }
        });

    // List box for test choices.
    testList_ = new DoubleClickList(testChoices);
    testList_.setVisibleRowCount(6);
    testList_.setEnabled(false);
    testList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                testPicked((String)event.getItem());
            }
        });

    // List box for functions
    functionList2_ = new DoubleClickList(functionChoices2);
    functionList2_.setVisibleRowCount(6);
    functionList2_.setEnabled(false);
    functionList2_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                functionPicked((String)event.getItem());
            }
        });

    // List box for others
    otherList_ = new DoubleClickList(otherChoices);
    otherList_.setVisibleRowCount(6);
    otherList_.setEnabled(false);
    otherList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                otherPicked((String)event.getItem());
            }
        });

    // Arrange the top row.
    Box topRow = Box.createHorizontalBox();
    topRow.add(new LabelledComponent("DBQUERY_LABEL_NOT", notList_));
    topRow.add(new LabelledComponent("DBQUERY_LABEL_FUNCTIONS", functionList_));
    topRow.add(new LabelledComponent("DBQUERY_LABEL_TEST", testList_));
    topRow.add(new LabelledComponent("DBQUERY_LABEL_FUNCTIONS", functionList2_));
    topRow.add(new LabelledComponent("DBQUERY_LABEL_OTHER", otherList_));

    // Edit area for having clause.
    clause_ = new SQLQueryClause(5);

    // Overall layout.
    Box overallBox = Box.createVerticalBox();
    overallBox.add(topRow);
    overallBox.add(new LabelledComponent("DBQUERY_LABEL_CLAUSE_HAVING", new ScrollingTextPane(clause_)));
    setLayout(new BorderLayout());
    add("Center", overallBox);
}


/**
Called when an item in the test list is double-clicked on.
The request is processed, the completed item is added
to the clause.

@param item The item that was chosen.
**/
/*private*/ void testPicked(String item)
{
    // Add text to clause
    clause_.appendText(item);

    // Make appropriate controls available.
    functionList2_.setEnabled(true);
    testList_.setEnabled(false);
    lastChoice_ = TEST_;

}



}
