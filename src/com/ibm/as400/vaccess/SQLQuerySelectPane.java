///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQuerySelectPane.java
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
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
The SQLQuerySelectPane class represents a panel which allows a
user to dynamically build the SELECT portion of an SQL query.
This panel is used for a page of the SQLQueryBuilderPane notebook.
**/
class SQLQuerySelectPane
extends SQLQueryFieldsPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// This class is not meant to be serialized, it should be transient.
// This class has items marked transient even though it is not
// serializable because otherwise errors were received when
// serializing objects that contained this class (even though they
// were transient instances.

// The variables and methods which have private commented out
// had to be made package scope since currently Internet Explorer
// does not allow inner class to access private items in their
// containing class.

// GUI components
/*private*/ DoubleClickList functionList_;
/*private*/ SQLQueryClause clause_;

// Values for list boxes.  Note these are not translated.
private static final String FCN_COUNT_ = "COUNT";
private static final String FCN_SUM_ = "SUM";
private static final String FCN_AVG_ = "AVG";
private static final String FCN_MIN_ = "MIN";
private static final String FCN_MAX_ = "MAX";

// List of fields in the select clause.
/*private*/ String[] selectFields_ = new String[0];
// List of items in the select clause.
/*private*/ String[] selectItems_ = new String[0];

// Tracks if the user changed the clause.
/*private*/ boolean changes_ = false;

    
static final String [] functionChoices = {FCN_AVG_, FCN_COUNT_, FCN_MIN_, FCN_MAX_, FCN_SUM_};


// Tracks if this panel has been initialized (setupPane() called).
private boolean init_ = false;

transient private DocumentListener_ docListener_;

// We have a separate listener list for panels to listen
// to changes to the selected items.
/*private*/ PropertyChangeSupport selectListeners_
    = new PropertyChangeSupport(this);


/**
Constructs a SQLQuerySelectPane object.
<i>init</i> must be called to build the GUI contents.

@param parent The parent of this panel.
**/
public SQLQuerySelectPane (SQLQueryBuilderPane parent)
{
    super(parent);
    // Add listener to changes to table fields.
    parent_.addFieldListener(new FieldListener2_());
}



/**
Adds a listener to be notified when the selected items have changed.
The listener's propertyChange method will be called.

@param  listener  The listener.
**/
public void addSelectListener (PropertyChangeListener listener)
{
    selectListeners_.addPropertyChangeListener (listener);
}


/**
Adds the string to the clause text area.
If the clause is an asterisk, it is removed, otherwise the
text is appended to the clause after a comma separator.

@param text The string to add to the clause.
**/
private void addToClause(String text)
{
    if (clause_.getText().equals("*"))
        clause_.setText(text);
    else
        clause_.appendTextWithComma(text);
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
    if (item.equals(FCN_COUNT_))
    {
        // add to clause
        addToClause("COUNT(*)");
    }
    else
    // All other functions need a field name.
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
        if (choice != null)  // null means they cancelled
        {
            addToClause(item + "(" + choice + ")");
        }
    }
}



/**
Returns the sql clause for this panel.
@return The sql clause for this panel.
**/
public String getClause()
{
    if (clause_ == null)
        return "*";
    return clause_.getText();
}



/**
Parse clause to determine the fields selected.
**/
public void complete()
{
    if (changes_)
    {
        if (clause_.getText().equals("*"))
        {
            selectFields_ = getFieldNames();
            selectItems_ = selectFields_;
            selectListeners_.firePropertyChange(null, null, null);
        }
        else
        {
            // Parse the text area for the field names.
            StringTokenizer clause = new StringTokenizer(clause_.getText(), ",");
            Vector fields = new Vector();
            Vector items = new Vector();
            String token;
            while (clause.hasMoreTokens())
            {
                token = clause.nextToken().trim();
                // Add all to items.
                items.addElement(token);
                // If this is not a function, add to field names
                if (!token.startsWith(FCN_COUNT_ + "(") &&
                    !token.startsWith(FCN_SUM_ + "(") &&
                    !token.startsWith(FCN_AVG_ + "(") &&
                    !token.startsWith(FCN_MIN_ + "(") &&
                    !token.startsWith(FCN_MAX_ + "(") )
                        fields.addElement(token);
            }
            String[] newFields = new String[fields.size()];
            fields.copyInto(newFields);
            String[] newItems = new String[items.size()];
            items.copyInto(newItems);

            // Determine if the items have changed.
            boolean different = false;
            if (selectItems_.length == newItems.length)
            {
                for (int i=0; i < selectItems_.length; ++i)
                {
                    // note even order changes are considered changes
                    if (!selectItems_[i].equals(newItems[i]))
                    {
                        different = true;
                        break;
                    }
                }
            }
            else
                different = true;
            if (different)
            {
                selectItems_ = newItems;
                selectFields_ = newFields;
                selectListeners_.firePropertyChange(null, null, null);
            }
        }

        // Listen for more changes to the tables.
        changes_ = false;
    }
}


/**
Returns the names of the fields in the select clause.

@return The field names.
**/
public String[] getSelectedFields()
{
    init();
    update();
    return selectFields_;
}


/**
Returns the names of the items (fields and functions) in the select clause.

@return The field names.
**/
public String[] getSelectedItems()
{
    init();
    update();
    return selectItems_;
}


/**
Only init if not already done, initialize fields selected
to all.
**/
public void init()
{
    if (!init_)
    {
        super.init();  // calls setupPane
        selectFields_ = getFieldNames();
        selectItems_ = selectFields_;
    }
}


/**
Adds the field to the clause.

@param index Index of the row in the table that was clicked upon.
**/
protected void rowPicked(int index)
{
    addToClause(fieldName(index));
}



/**
Builds the panel GUI components and sets up connections
between the components by using listeners.
**/
protected void setupPane()
{
    super.setupPane();
    init_ = true;

    // Functions list.
    functionList_ = new DoubleClickList(functionChoices);
    functionList_.setVisibleRowCount(5); //@B0A - have more than 5 elements, but do this for consistency with other panes.
    functionList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                functionPicked((String)event.getItem());
            }
        });
    Box functionsBox = Box.createHorizontalBox();
    functionsBox.add(new LabelledComponent("DBQUERY_LABEL_FUNCTIONS", functionList_));
    functionsBox.add(Box.createHorizontalGlue());

    // Text area
    clause_ = new SQLQueryClause(5);
    clause_.setText("*");
    docListener_ = new DocumentListener_();
    clause_.getDocument().addDocumentListener(docListener_);

    // Layout overall.
    Box overallBox = Box.createVerticalBox();
    overallBox.add(fields_);
    overallBox.add(functionsBox);
    overallBox.add(new LabelledComponent("DBQUERY_LABEL_CLAUSE_SELECT", new ScrollingTextPane(clause_)));
    setLayout(new BorderLayout());
    add("Center", overallBox);
}


/**
Update the document listener if needed.
**/
public void update()
{
    if (fieldsChanged_)
        changes_ = true;
    super.update();
    complete();
    docListener_.orderEnabled_ = selectItems_.length==0?false:true;
}



/**
Class for listening to document events.
**/
private class DocumentListener_
implements DocumentListener
{
    boolean orderEnabled_ = true;
    
    public void insertUpdate(DocumentEvent e)
    {
        changes_ = true;
        // If there is a chance we might need to enable the
        // order pane, update now.
        if (!orderEnabled_ &&
            (selectItems_.length == 0 ||
             clause_.getText().equals("*")))
        {
            complete();
            if (selectItems_.length != 0)
            {
                parent_.notebook_.setEnabledAt(parent_.ORDER_, true);
                orderEnabled_ = true;
            }
        }
    }
    public void changedUpdate(DocumentEvent e)
    {
    }
    public void removeUpdate(DocumentEvent e)
    {
        changes_ = true;
        // If we might need to disable the order pane, update now.
        if (orderEnabled_ && clause_.getText().equals(""))
        {
            complete();
            if (selectItems_.length == 0)
            {
                parent_.notebook_.setEnabledAt(parent_.ORDER_, false);
                orderEnabled_ = false;
            }
        }
    }
}


/**
Class to listen for property changes on the fields contained in the
tables associated with the query.
This class needs to listen to changes on the fields if the clause
is "*" in order to notify the selectListeners.
**/
private class FieldListener2_
implements PropertyChangeListener
{
    public void propertyChange(PropertyChangeEvent event)
    {
        if (clause_ == null || clause_.getText().equals("*"))
        {
            selectFields_ = getFieldNames();
            selectItems_ = selectFields_;
            selectListeners_.firePropertyChange(null, null, null);
        }
    }
}


}
