///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryWherePane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;
                          
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Types;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


/**
The SQLQueryWherePane class represents a panel which allows a
user to dynamically build the SELECT portion of an SQL query.
This panel is used for a page of the SQLQueryBuilderPane notebook.
**/
class SQLQueryWherePane
extends SQLQueryFieldsPane
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
/*private*/ DoubleClickList otherList_;
/*private*/ SQLQueryClause clause_;

// Constants for SQL syntax.  Because these strings represent
// SQL syntax, they are not translated.
// Constants for functions.
private static final String FCN_CAST_ = "CAST";
private static final String FCN_CHAR_ = "CHAR";
private static final String FCN_CURRENT_ = "CURRENT";
private static final String FCN_DATE_ = "DATE";
private static final String FCN_DAY_ = "DAY";
private static final String FCN_HOUR_ = "HOUR";
private static final String FCN_LENGTH_ = "LENGTH";
private static final String FCN_MINUTE_ = "MINUTE";
private static final String FCN_MONTH_ = "MONTH";
private static final String FCN_SECOND_ = "SECOND";
private static final String FCN_SUBSTR_ = "SUBSTRING";
private static final String FCN_TIME_ = "TIME";
private static final String FCN_TIMESTAMP_ = "TIMESTAMP";
private static final String FCN_UPPER_ = "UPPER";
private static final String FCN_YEAR_ = "YEAR";
// Constants for tests.
private static final String TEST_BETWEEN_ = "BETWEEN";
private static final String TEST_IN_ = "IN";
private static final String TEST_NOT_NULL_ = "IS NOT NULL";
private static final String TEST_NULL_ = "IS NULL";
private static final String TEST_LIKE_ = "LIKE";

// Is NOT in affect for this expression.
private boolean notInEffect_ = false;

// Private variables used by internal methods.  These are
// instance variables because they are used in several places.
/*private*/ JDialog dialog;
/*private*/ boolean pane1Active;
/*private*/ JComboBox list1, list2, list3;
/*private*/ JTextField textField1, textField2;

private static final String [] notChoices = {"NOT"};
private static final String [] functionChoices = {FCN_CAST_, FCN_CHAR_, FCN_CURRENT_, FCN_DATE_,
      FCN_DAY_, FCN_HOUR_, FCN_LENGTH_, FCN_MINUTE_, FCN_MONTH_, FCN_SECOND_,
      FCN_SUBSTR_, FCN_TIME_, FCN_TIMESTAMP_, FCN_UPPER_, FCN_YEAR_};
private static final String [] testChoices = {"=", "<>", "<", ">", "<=", ">=",
        TEST_BETWEEN_, TEST_IN_, TEST_NOT_NULL_, TEST_NULL_, TEST_LIKE_};
private static final String [] otherChoices = {"AND", "OR"};

/**
Constructs a SQLQueryWherePane object.
Note <i>init</i> must be called to build the GUI contents.

@param parent The parent of this panel.
**/
public SQLQueryWherePane (SQLQueryBuilderPane parent)
{
    super(parent);
}


/**
Enables the appropriate controls after a function is chosen.
**/
void functionComplete()
{
    // Make appropriate controls available.
    fields_.setEnabled(false);
    notList_.setEnabled(false);
    functionList_.setEnabled(false);
    testList_.setEnabled(true);
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
    // Variables used when putting up an additional dialog.
    String[] choices;    // choices for the user
    Object choice;       // what the user selected

    // -------------------------
    //   FCN_CURRENT_
    // -------------------------
    // Current requires a second value for date, time or timestamp.
    if (item.equals(FCN_CURRENT_))
    {
        choices = new String[]{"DATE", "TIME", "TIMESTAMP"};
        choice = JOptionPane.showInputDialog(this, // parent
            item, // message
            item, // title
            JOptionPane.QUESTION_MESSAGE,   // message type
            null, // icon
            choices,  // choices
            choices[0]);  // initial choice
        if (choice == null)  // null means they cancelled
            return;
        String text = "(CURRENT " + choice ;
        clause_.appendText(text);
        functionComplete();
    }

    // -------------------------
    //   FCN_CAST_
    // -------------------------
    // On AS400, cast is only valid at V4R2 or later.
    // Cast requires a field name and an SQL type.
    else if (item.equals(FCN_CAST_))
    {
        choices = getFieldNames();
        if (choices.length ==0)
        {
            // put up error message and return
            noFields(item);
            return;
        }
        list1 = new JComboBox();
        for (int i=0; i< choices.length; ++i)
            list1.addItem(choices[i]);
        JLabel asLabel = new JLabel("AS");
        list2 = new JComboBox();
        list2.setEditable(true); // allow users to change type - ie CHAR(10)
        list2.addItem("CHARACTER()");
        list2.addItem("DATE");
        list2.addItem("DECIMAL(,)");
        list2.addItem("DOUBLE");
        list2.addItem("FLOAT()");
        list2.addItem("GRAPHIC()");
        list2.addItem("INTEGER");
        list2.addItem("NUMERIC(,)");
        list2.addItem("REAL");
        list2.addItem("SMALLINT");
        list2.addItem("TIME");
        list2.addItem("TIMESTAMP");
        list2.addItem("VARCHAR()");
        list2.addItem("VARGRAPHIC()");
        JPanel choicePanel = new JPanel();
        choicePanel.add(list1);
        choicePanel.add(asLabel);
        choicePanel.add(list2);

        // Create buttons
        JButton okButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_OK"));
        final String fitem = item;
        okButton.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent ev)
                {
                    String choice1 = (String)list1.getSelectedItem();
                    String choice2 = (String)list2.getSelectedItem();
                    if (choice2 == null || choice2.equals(""))
                    {
                        // put up error message and return (leave dialog up)
                        JOptionPane.showMessageDialog(parent_, // parent
                            ResourceLoader.getQueryText("DBQUERY_MESSAGE_VALUE_MISSING") + " AS", // message
                            fitem + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                            JOptionPane.ERROR_MESSAGE ); // message type
                        return;
                    }
                    if (choice2.endsWith("()"))
                    {
                        // Create dialog for getting one length
                        String type = choice2.substring(0,choice2.length()-2);
                        boolean required = type.startsWith("VAR");
                        String prompt2;
                        if (required)
                            prompt2 = ResourceLoader.getQueryText("DBQUERY_TEXT_LENGTH_REQ");
                        else
                            prompt2 = ResourceLoader.getQueryText("DBQUERY_TEXT_LENGTH");
                        boolean error = true;
                        while (error)
                        {
                            String result = JOptionPane.showInputDialog(parent_, // parent
                                prompt2, // message
                                type + " " + ResourceLoader.getQueryText("DBQUERY_TITLE_LENGTH"), // title
                                JOptionPane.QUESTION_MESSAGE ); // message type
                            if (result.equals(""))
                            {
                                if (required)
                                {
                                    // put up error message
                                    JOptionPane.showMessageDialog(parent_, // parent
                                        ResourceLoader.getQueryText("DBQUERY_MESSAGE_INVALID_INT_VALUE3"), // message
                                        fitem + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                                        JOptionPane.ERROR_MESSAGE ); // message type
                                }
                                else
                                {
                                    choice2 = type;  // use default length, remove parens
                                    error = false;
                                }
                            }
                            else
                            {
                                // verify input is a number
                                try
                                {
                                    int i = Integer.parseInt(result);
                                    if (i > 0) error = false;
                                }
                                catch(NumberFormatException e) {}
                                if (error)
                                {
                                    // put up error message
                                    JOptionPane.showMessageDialog(parent_, // parent
                                        ResourceLoader.getQueryText("DBQUERY_MESSAGE_INVALID_INT_VALUE3"), // message
                                        fitem + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                                        JOptionPane.ERROR_MESSAGE ); // message type
                                }
                                else
                                    choice2 = type + "(" + result + ")"; // add length
                            }
                        }
                    }
                    else if (choice2.endsWith("(,)"))
                    {
                        // Create dialog for getting the total length (precision).
                        String type = choice2.substring(0,choice2.length()-3);
                        boolean error = true;
                        while (error)
                        {
                            String result = JOptionPane.showInputDialog(parent_, // parent
                                ResourceLoader.getQueryText("DBQUERY_TEXT_LENGTH_TOTAL"), // message
                                type + " " + ResourceLoader.getQueryText("DBQUERY_TITLE_LENGTH"), // title
                                JOptionPane.QUESTION_MESSAGE ); // message type
                            if (result.equals(""))
                            {
                                choice2 = type;  // use default length, remove parens
                                error = false;
                            }
                            else
                            {
                                // verify input is a number
                                try
                                {
                                    int i = Integer.parseInt(result);
                                    if (i >= 0) error = false;
                                }
                                catch(NumberFormatException e) {}
                                if (error)
                                {
                                    // put up error message
                                    JOptionPane.showMessageDialog(parent_, // parent
                                        ResourceLoader.getQueryText("DBQUERY_MESSAGE_INVALID_INT_VALUE3"), // message
                                        fitem + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                                        JOptionPane.ERROR_MESSAGE ); // message type
                                }
                                else
                                {
                                    choice2 = type + "(" + result + ")"; // add length

                                    // Put up second dialog asking for scale (decimal positions)
                                    error = true;
                                    while (error)
                                    {
                                        String result2 = JOptionPane.showInputDialog(parent_, // parent
                                            ResourceLoader.getQueryText("DBQUERY_TEXT_LENGTH_DECIMAL"), // message
                                            type + " " + ResourceLoader.getQueryText("DBQUERY_TITLE_LENGTH"), // title
                                            JOptionPane.QUESTION_MESSAGE ); // message type
                                        if (result2 == null)
                                        {
                                            choice2 = type + "(" + result + ")"; // add one length
                                            error = false;
                                        }                                          
                                        else if (result2.equals(""))
                                        {
                                            choice2 = type + "(" + result + ")"; // add one length
                                            error = false;
                                        }
                                        else
                                        {
                                            // verify input is a number
                                            try
                                            {
                                                int i = Integer.parseInt(result2);
                                                if (i >= 0) error = false;
                                            }
                                            catch(NumberFormatException e) {}
                                            if (error)
                                            {
                                                // put up error message
                                                JOptionPane.showMessageDialog(parent_, // parent
                                                    ResourceLoader.getQueryText("DBQUERY_MESSAGE_INVALID_INT_VALUE3"), // message
                                                    fitem + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                                                    JOptionPane.ERROR_MESSAGE ); // message type
                                            }
                                            else
                                                choice2 = type + "(" + result + "," + result2 + ")"; // add two lengths
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String text;
                    /*if (choice2.indexOf("GRAPHIC") == 0 ||
                        choice2.indexOf("VARGRAPHIC") == 0)
                        // cast to graphic requires CCSID 13488
                        text = "(" + fitem + "(" + choice1 + " AS " + choice2 + " CCSID 13488)";
                    else*/
                        text = "(" + fitem + "(" + choice1 + " AS " + choice2 + ")";
                    clause_.appendText(text);
                    dialog.dispose();
                    functionComplete();
                }
            }  // end of ActionListenerAdapter
        );
        JButton cancelButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_CANCEL"));
        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ev)
                {
                    dialog.dispose();
                }
            }
        );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog = new JDialog(VUtilities.getFrame(this), item, true);
        dialog.getContentPane().setLayout(new BorderLayout());

        dialog.getContentPane().add("Center",new LabelledComponent(ResourceLoader.getQueryText("DBQUERY_TEXT_CHOOSE2") + " " + item + "()", choicePanel, false));
        dialog.getContentPane().add("South", buttonPanel);
        dialog.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent ev)
                {
                    dialog.dispose();
                }
            }
        );
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // -------------------------
    //   FCN_CHAR_
    // -------------------------
    // Char requires a date, time or timestamp field, and then an
    // optional SQL datetime format.
    else if (item.equals(FCN_CHAR_))
    {
        choices = getDateTimeFieldNames();
        if (choices.length ==0)
        {
            // put up error message and return
            noFields(item);
            return;
        }
        list1 = new JComboBox();
        for (int i=0; i< choices.length; ++i)
            list1.addItem(choices[i]);
        list2 = new JComboBox();
        list2.addItem(" ");  // Blank for no choice
        list2.addItem("ISO");
        list2.addItem("USA");
        list2.addItem("EUR");
        list2.addItem("JIS");

        // Create buttons
        JButton okButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_OK"));
        final String fitem = item;
        okButton.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent ev)
                {
                    String choice1 = (String)list1.getSelectedItem();
                    String choice2 = (String)list2.getSelectedItem();
                    String text;
                    if (choice2.equals(" "))
                        text = "(" + fitem + "(" + choice1 + ")";
                    else
                        text = "(" + fitem + "(" + choice1 + ", " + choice2 +  ")";
                    clause_.appendText(text);
                    dialog.dispose();
                    functionComplete();
                }
                private String getCopyright()
                { return Copyright_v.copyright;}
            }  // end of ActionListenerAdapter
        );
        JButton cancelButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_CANCEL"));
        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ev)
                {
                    dialog.dispose();
                }
            }
        );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog = new JDialog(VUtilities.getFrame(this), item, true);
        dialog.getContentPane().setLayout(new BorderLayout());
        JPanel listPanel = new JPanel();
        listPanel.add(list1);
        listPanel.add(list2);
        dialog.getContentPane().add("Center", new LabelledComponent(ResourceLoader.getQueryText("DBQUERY_TEXT_CHOOSE2") + " " + item + "()", listPanel, false));
        dialog.getContentPane().add("South", buttonPanel);
        dialog.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent ev)
                {dialog.dispose();}
            }
        );
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // -------------------------
    //   FCN_SUBSTR_
    // -------------------------
    // Substring requires a character field name, start index, and stop index.
    else if (item.equals(FCN_SUBSTR_))
    {
        // Substring requires a character field name and then two numbers
        // for the start and end.  We use the FROM/FOR syntax, so it
        // looks like this:  "SUBSTRING(<field> FROM 2 FOR 6)
        // The FOR part is optional.
        choices = getCharacterFieldNames();
        if (choices.length ==0)
        {
            // put up error message and return
            noFields(item);
            return;
        }
        list1 = new JComboBox();
        for (int i=0; i< choices.length; ++i)
            list1.addItem(choices[i]);
        final String fieldFROM = "FROM";
        final String fieldFOR = "FOR";
        JLabel fromLabel = new JLabel(fieldFROM);
        JLabel forLabel = new JLabel(fieldFOR);
        textField1 = new JTextField("1", 3);
        textField2 = new JTextField(3);
        JPanel choicePanel = new JPanel();
        choicePanel.add(list1);
        choicePanel.add(fromLabel);
        choicePanel.add(textField1);
        choicePanel.add(forLabel);
        choicePanel.add(textField2);

        // Create buttons
        JButton okButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_OK"));
        final String fitem = item;
        okButton.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent ev)
                {
                    String choice1 = (String)list1.getSelectedItem();
                    String choice2 = textField1.getText().trim();
                    String choice3 = textField2.getText().trim();
                    String text;
                    if (choice2.equals(""))
                    {
                        // put up error message and return (leave dialog up)
                        JOptionPane.showMessageDialog(parent_, // parent
                            ResourceLoader.getQueryText("DBQUERY_MESSAGE_VALUE_MISSING") + " " + fieldFROM, // message
                            fitem + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                            JOptionPane.ERROR_MESSAGE ); // message type
                        return;
                    }
                    else
                    {
                        // verify input is a number
                        boolean error = false;
                        try
                        {
                            int i = Integer.parseInt(choice2);
                            if (i<1) error = true;
                        }
                        catch(NumberFormatException e) {error = true;}
                        if (error)
                        {
                            // put up error message and return (leave dialog up)
                            JOptionPane.showMessageDialog(parent_, // parent
                                fieldFROM + " " + ResourceLoader.getQueryText("DBQUERY_MESSAGE_INVALID_INT_VALUE2"), // message
                                fitem + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                                JOptionPane.ERROR_MESSAGE ); // message type
                            return;
                        }
                    }
                    if (choice3.equals(""))
                        text = fitem + "(" + choice1 + " FROM " + choice2 + ")";
                    else
                    {
                        // verify input is a number
                        boolean error = false;
                        try
                        {
                            int i = Integer.parseInt(choice3);
                            if (i<0) error = true;
                        }
                        catch(NumberFormatException e) {error = true;}
                        if (error)
                        {
                            // put up error message and return (leave dialog up)
                            JOptionPane.showMessageDialog(parent_, // parent
                                fieldFOR + " " + ResourceLoader.getQueryText("DBQUERY_MESSAGE_INVALID_INT_VALUE"), // message
                                fitem + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                                JOptionPane.ERROR_MESSAGE ); // message type
                            return;
                        }
                        text = fitem + "(" + choice1 + " FROM " + choice2 + " FOR " + choice3 + ")";
                    }
                    clause_.appendText("(" + text);
                    dialog.dispose();
                    functionComplete();
                }
            }  // end of ActionListenerAdapter
        );
        JButton cancelButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_CANCEL"));
        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ev)
                {
                    dialog.dispose();
                }
            }
        );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog = new JDialog(VUtilities.getFrame(this), item, true);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add("Center",new LabelledComponent(ResourceLoader.getQueryText("DBQUERY_TEXT_CHOOSE2") + " " + item + "()",
                                                                   choicePanel, false));
        dialog.getContentPane().add("South", buttonPanel);
        dialog.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent ev)
                {dialog.dispose();}
            }
        );
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // -------------------------
    //   FCN_TIMESTAMP_
    // -------------------------
    // Timestamp requires either a timestamp field or a date field and
    // a time field.
    else if (item.equals(FCN_TIMESTAMP_))
    {
        // Make two panels, one for choosing a 1 date and 1 time field,
        // the other for choosing a timestamp field.  Have radio
        // buttons to switch between the panes.  Disable the pane and
        // button if the appropriate fields do not exist.

        // Get fields for each list.
        String[] dateFields = getFieldNamesOfType(Types.DATE);
        String[] timeFields = getFieldNamesOfType(Types.TIME);
        String[] timestampFields = getFieldNamesOfType(Types.TIMESTAMP);
        // Verify there are fields appropriate for this function
        boolean pane1valid = !(dateFields.length == 0 || timeFields.length == 0);
        boolean pane2valid = !(timestampFields.length == 0);
        if (!pane1valid && !pane2valid)
        {
            // put up error message and return
            noFields(item);
            return;
        }

        dialog = new JDialog(VUtilities.getFrame(this), item, true);
        dialog.getContentPane().setLayout(new BorderLayout());
        JPanel choicePane = new JPanel(new BorderLayout());
        pane1Active = pane1valid;  // switch for which pane active;
        JRadioButton pane1Button=null, pane2Button;
        // Make first panel for date and time fields
        if (pane1valid)
        {
            JPanel pane1 = new JPanel(new BorderLayout());
            pane1.setBorder(new CompoundBorder(
                new EmptyBorder(10,10,10,10),
                new CompoundBorder(LineBorder.createBlackLineBorder(),
                    new EmptyBorder(10,10,10,10))));
            if (pane2valid)  // need buttons only if both panes valid
            {
                pane1Button = new JRadioButton(
                    ResourceLoader.getQueryText("DBQUERY_BUTTON_TIMESTAMP_2_FIELDS"), true);
                pane1Button.setBorder(new EmptyBorder(0,10,10,10));
                pane1Button.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent ev)
                        {
                            list1.setEnabled(true);
                            list2.setEnabled(true);
                            list3.setEnabled(false);
                            pane1Active = true;
                        }
                    }
                );
                pane1.add("North", pane1Button);
            }
            list1 = new JComboBox();
            for (int i=0; i<dateFields.length; ++i)
                list1.addItem(dateFields[i]);
            list2 = new JComboBox();
            for (int i=0; i<timeFields.length; ++i)
                list2.addItem(timeFields[i]);

            Box listpane = Box.createHorizontalBox();
            listpane.add(list1);
            listpane.add(list2);
            pane1.add("Center", listpane);

            choicePane.add("West",pane1);
        }
        // Make second panel for timestamp fields
        if (pane2valid)
        {
            JPanel pane2 = new JPanel(new BorderLayout());
            pane2.setBorder(new CompoundBorder(
                new EmptyBorder(10,10,10,10),
                new CompoundBorder(LineBorder.createBlackLineBorder(),
                    new EmptyBorder(10,10,10,10))));
            if (pane1valid)  // need buttons only if both panes valid
            {
                pane2Button = new JRadioButton(
                    ResourceLoader.getQueryText("DBQUERY_BUTTON_TIMESTAMP_1_FIELDS"), false);
                pane2Button.setBorder(new EmptyBorder(0,10,10,10));
                ButtonGroup group = new ButtonGroup();
                group.add(pane1Button);
                group.add(pane2Button);
                pane2Button.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent ev)
                        {
                            list1.setEnabled(false);
                            list2.setEnabled(false);
                            list3.setEnabled(true);
                            pane1Active = false;
                        }
                    }
                );
                pane2.add("North", pane2Button);
            }
            list3 = new JComboBox();
            for (int i=0; i<timestampFields.length; ++i)
                list3.addItem(timestampFields[i]);
            pane2.add("Center", list3);
            choicePane.add("East",pane2);
        }

        // diable list in pane 2 if have 2 panes
        if (pane1valid && pane2valid)
            list3.setEnabled(false);

        // Create buttons
        JButton okButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_OK"));
        final String fitem = item;
        okButton.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent ev)
                {
                    String text;
                    if (pane1Active)
                    {
                        String choice1 = (String)list1.getSelectedItem();
                        String choice2 = (String)list2.getSelectedItem();
                        text = "(" + fitem + "(" + choice1 + ", " + choice2 + ")";
                    }
                    else // pane2 active
                    {
                        String choice1 = (String)list3.getSelectedItem();
                        text = "(" + fitem + "(" + choice1 + ")";
                    }
                    clause_.appendText(text);
                    dialog.dispose();
                    functionComplete();
                }
                private String getCopyright()
                { return Copyright_v.copyright;}
            }  // end of ActionListenerAdapter
        );
        JButton cancelButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_CANCEL"));
        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ev)
                {
                    dialog.dispose();
                }
            }
        );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.getContentPane().add("South", buttonPanel);
        dialog.getContentPane().add("Center", choicePane);
        dialog.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent ev)
                {dialog.dispose();}
            }
        );
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

    }

    // -------------------------
    //   remaining functions
    // -------------------------
    // Remaining functions all require a field name.
    // Which fields are valid depends on the function.
    else
    {
        // Show dialog to have user choose field.
        if (item.equals(FCN_DATE_) || item.equals(FCN_DAY_) ||
            item.equals(FCN_MONTH_) || item.equals(FCN_YEAR_))
        {
            // only date and timestamp fields are valid
            choices = getDateFieldNames();
        }
        else if (item.equals(FCN_MINUTE_) || item.equals(FCN_SECOND_) ||
                 item.equals(FCN_HOUR_) || item.equals(FCN_TIME_))
        {
            // all time and timestamp fields are valid
            choices = getTimeFieldNames();
        }
        else if (item.equals(FCN_UPPER_))
        {
            // all character fields are valid
            choices = getCharacterFieldNames();
        }
        else  // must be FCN_LENGTH
            // all fields are valid
            choices = getFieldNames();
        if (choices.length ==0)
        {
            // put up error message and return
            noFields(item);
            return;
        }
        choice = JOptionPane.showInputDialog(this, // parent
            ResourceLoader.getQueryText("DBQUERY_TEXT_CHOOSE") + " " + item + "()", // message
            item, // title
            JOptionPane.QUESTION_MESSAGE,   // message type
            null, // icon
            choices,  // choices
            choices[0]);  // initial choice
        if (choice == null)  // null means they cancelled
            return;
        clause_.appendText("(" + item + "(" + choice + ")");
        functionComplete();
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
Puts up an error message which tells the user there are no fields
that are suitable for the chosen function.
@param function Name of the function attempting to be used.
**/
private void noFields(String function)
{
    JOptionPane.showMessageDialog(this, // parent
        ResourceLoader.getQueryText("DBQUERY_MESSAGE_NO_FIELDS") + " " + function + "()", // message
        function + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
        JOptionPane.ERROR_MESSAGE ); // message type
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
    fields_.setEnabled(true);
    notList_.setEnabled(true);
    functionList_.setEnabled(true);
    otherList_.setEnabled(false);
    notInEffect_ = false;
}


/**
Adds the field to the clause.

@param index Index of the row in the table that was clicked upon.
**/
protected void rowPicked(int index)
{
    // Add the field to the clause with a leading left paren
    clause_.appendText("(" + fieldName(index));

    // Make appropriate controls available.
    fields_.setEnabled(false);
    notList_.setEnabled(false);
    functionList_.setEnabled(false);
    testList_.setEnabled(true);
}


/**
Builds the panel GUI components and sets up connections
between the components by using listeners.
**/
protected void setupPane()
{
    super.setupPane();

    // List box for not.
    notList_ = new DoubleClickList(notChoices);
    notList_.setVisibleRowCount(1); //@B0A
    notList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                notPicked((String)event.getItem());
            }
        });

    // Functions list.
    functionList_ = new DoubleClickList(functionChoices);
    functionList_.setVisibleRowCount(5); //@B0A - have more than 5 elements, but do this for consistency with other panes.
    functionList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                functionPicked((String)event.getItem());
            }
        });

    // Test list.
    testList_ = new DoubleClickList(testChoices);
    testList_.setEnabled(false);
    testList_.setVisibleRowCount(5); //@B0A - have more than 5 elements, but do this for consistency with other panes.
    testList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                testPicked((String)event.getItem());
            }
        });

    // Other list.
    otherList_ = new DoubleClickList(otherChoices);
    otherList_.setEnabled(false);
    otherList_.setVisibleRowCount(2); //@B0A
    otherList_.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                otherPicked((String)event.getItem());
            }
        });

    // Layout middle row.
    Box middleRow = Box.createHorizontalBox();
    middleRow.add(new LabelledComponent("DBQUERY_LABEL_NOT", notList_));
    middleRow.add(new LabelledComponent("DBQUERY_LABEL_FUNCTIONS", functionList_));
    middleRow.add(new LabelledComponent("DBQUERY_LABEL_TEST", testList_));
    middleRow.add(new LabelledComponent("DBQUERY_LABEL_OTHER", otherList_));

    // Clause.
    clause_ = new SQLQueryClause(5);

    // Layout overall.
    Box overallBox = Box.createVerticalBox();
    overallBox.add(fields_);
    overallBox.add(middleRow);
    overallBox.add(new LabelledComponent("DBQUERY_LABEL_CLAUSE_WHERE", new ScrollingTextPane(clause_)));
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
    String text = null;

    // -------------------------
    //   TEST_BETWEEN_
    // -------------------------
    // BETWEEN requires 2 inputs, either field names or a
    // constant.
    if (item.equals(TEST_BETWEEN_))
    {
        String[] choices = getFieldNames();
        list1 = new JComboBox();
        list2 = new JComboBox();
        list1.addItem(""); // Add empty items so user may type in constant.
        list2.addItem("");
        for (int i=0; i< choices.length; ++i)
        {
            list1.addItem(choices[i]);
            list2.addItem(choices[i]);
        }
        list1.setEditable(true);
        list2.setEditable(true);
        JLabel andLabel = new JLabel("AND");
        andLabel.setBorder(new EmptyBorder(10,10,10,10));
        JPanel choicePane = new JPanel();
        choicePane.add(list1);
        choicePane.add(andLabel);
        choicePane.add(list2);

        // Create buttons
        JButton okButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_OK"));
        final String fitem = item;
        okButton.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent ev)
                {
                    String choice1 = ((String)list1.getSelectedItem());
                    String choice2 = ((String)list2.getSelectedItem());
                    if (choice1 != null) choice1 = choice1.trim();
                    if (choice2 != null) choice2 = choice2.trim();
                    if (choice1 == null || choice2 == null ||
                        choice1.equals("") || choice2.equals(""))
                    {
                        // put up error message and return (leave dialog up)
                        JOptionPane.showMessageDialog(parent_, // parent
                            ResourceLoader.getQueryText("DBQUERY_MESSAGE_VALUE_MISSING") + " " + fitem, // message
                            fitem + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                            JOptionPane.ERROR_MESSAGE ); // message type
                        return;
                    }
                    // Add to clause
                    if (notInEffect_)
                        clause_.appendText(fitem + " " + choice1 + " AND " + choice2 + "))");
                    else
                        clause_.appendText(fitem + " " + choice1 + " AND " + choice2 + ")");
                    // Make appropriate controls available.
                    otherList_.setEnabled(true);
                    testList_.setEnabled(false);
                    // End dialog
                    dialog.dispose();
                }
                private String getCopyright()
                { return Copyright_v.copyright;}
            }  // end of ActionListenerAdapter
        );
        JButton cancelButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_CANCEL"));
        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ev)
                {
                    dialog.dispose();
                }
            }
        );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog = new JDialog(VUtilities.getFrame(this), item, true);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add("Center",new LabelledComponent(ResourceLoader.getQueryText("DBQUERY_TEXT_CHOOSE3") + " " + item, choicePane, false));
        dialog.getContentPane().add("South", buttonPanel);
        dialog.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent ev)
                {dialog.dispose();}
            }
        );
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // @A1 - Set default focus in JPanel-JComboBox-JTextField
        dialog.addFocusListener(new FocusAdapter()
        {
           public void focusGained(FocusEvent event)
           {
              JComponent comp, subcomp;
              for (int x=0; x<dialog.getContentPane().getComponentCount(); x++) {
                 comp = (JComponent) dialog.getContentPane().getComponent(x);
                 if (comp instanceof JPanel) {                                      // choicePane
                    for (int y=0; y<comp.getComponentCount(); y++)
                    {
                       subcomp = (JComponent) comp.getComponent(y);
                       if (subcomp instanceof JComboBox)                            // list1
                       {
                          for (int z=0; z<subcomp.getComponentCount(); z++)
                          {
                             if (subcomp.getComponent(z) instanceof JTextField)
                             {
                                subcomp.getComponent(z).requestFocus();
                                return;
                             }
                          }
                          return;
                       }
                    }
                    return;
                 }
              }
           }
        } );

        dialog.setVisible(true);
    }

    // -------------------------
    //   TEST_IN_  TEST_LIKE_
    // -------------------------
    // IN and LIKE require a single constant input.
    else if (item.equals(TEST_IN_) || item.equals(TEST_LIKE_))
    {
        boolean error = true;
        String result = "";
        while (error)
        {
            result = JOptionPane.showInputDialog(this, // parent
                ResourceLoader.getQueryText("DBQUERY_TEXT_TEST_CONSTANT") + " " + item, // message
                ResourceLoader.getQueryText("DBQUERY_TITLE_CONSTANT"), // title
                JOptionPane.QUESTION_MESSAGE);   // message type
            if (result == null) // null means they cancelled
                return;
            if (result.equals(""))
            {
                // put up error message
                JOptionPane.showMessageDialog(parent_, // parent
                    ResourceLoader.getQueryText("DBQUERY_MESSAGE_VALUE_MISSING") + " " + item, // message
                    item + "() " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                    JOptionPane.ERROR_MESSAGE ); // message type
            }
            else
                error = false;
        }
        if (item.equals(TEST_IN_))
            text = item + " (" + result + ")";
        else // TEST_LIKE_
            text = item + " '" + result + "'";
    }

    // -------------------------
    //   TEST_NOT_NULL_  TEST_NULL_
    // -------------------------
    // IS (NOT) NULL needs no further input.
    else if (item.equals(TEST_NOT_NULL_) || item.equals(TEST_NULL_))
    {
        text = item;
    }

    // -------------------------
    //   comparison operators
    // -------------------------
    // Remaining functions (comparison operators) require
    // one input which can be a field name or a constant expression.
    else
    {
        String[] choices = getFieldNames();
        list1 = new JComboBox();
        list1.addItem("");  // blank line for constant
        for (int i=0; i< choices.length; ++i)
            list1.addItem(choices[i]);
        list1.setEditable(true);

        // Create buttons
        JButton okButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_OK"));
        final String fitem = item;
        okButton.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent ev)
                {
                    String choice1 = ((String)list1.getSelectedItem());
                    if (choice1 != null) choice1 = choice1.trim();
                    if (choice1 == null || choice1.equals(""))
                    {
                        // put up error message and return (leave dialog up)
                        JOptionPane.showMessageDialog(parent_, // parent
                            ResourceLoader.getQueryText("DBQUERY_MESSAGE_VALUE_MISSING") + " " + fitem, // message
                            fitem + " " + ResourceLoader.getQueryText("DBQUERY_TITLE_ERROR"), // title
                            JOptionPane.ERROR_MESSAGE ); // message type
                        return;
                    }
                    // Add to clause
                    if (notInEffect_)
                        clause_.appendText(fitem + " " + choice1 + "))");
                    else
                        clause_.appendText(fitem + " " + choice1 + ")");
                    // Make appropriate controls available.
                    otherList_.setEnabled(true);
                    testList_.setEnabled(false);
                    // End dialog
                    dialog.dispose();
                }
                private String getCopyright()
                { return Copyright_v.copyright;}
            }  // end of ActionListenerAdapter
        );
        JButton cancelButton = new JButton(ResourceLoader.getQueryText("DBQUERY_BUTTON_CANCEL"));
        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ev)
                {
                    dialog.dispose();
                }
            }
        );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog = new JDialog(VUtilities.getFrame(this), item, true);
        dialog.getContentPane().setLayout(new BorderLayout());

        dialog.getContentPane().add("Center",new LabelledComponent(ResourceLoader.getQueryText("DBQUERY_TEXT_CHOOSE3") + " " + item, list1, false));
        dialog.getContentPane().add("South", buttonPanel);
        dialog.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent ev)
                {dialog.dispose();}
            }
        );
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // @A1 - Set default focus in JComboBox-JTextField
        dialog.addFocusListener(new FocusAdapter()
        {
           public void focusGained(FocusEvent event)
           {
              JComponent comp;
              for (int i=0; i<dialog.getContentPane().getComponentCount(); i++) {
                 comp = (JComponent) dialog.getContentPane().getComponent(i);
                 if (comp instanceof JComboBox) {                                   // list1
                    for (int x=0; x<comp.getComponentCount(); x++)
                    {
                       if (comp.getComponent(x) instanceof JTextField)
                       {
                          comp.getComponent(x).requestFocus();
                          return;
                       }
                    }
                    return;
                 }
              }
           }
        } );

        dialog.setVisible(true);
    }

    if (text != null)  // user completed test
    {
        // Add text to clause with trailing right paren(s).
        if (notInEffect_)
            clause_.appendText(text + "))");
        else
            clause_.appendText(text + ")");

        // Make appropriate controls available.
        otherList_.setEnabled(true);
        testList_.setEnabled(false);
    }
}



}
