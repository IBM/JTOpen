///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VQRYMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.*;

/**
Locale-specific objects for the IBM Toolbox for Java.
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VQRYMRI extends ListResourceBundle
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

   public Object[][] getContents()
   {
       return resources;
   }

   private final static Object[][] resources= {
           // #TRANNOTE #####################################################
           // #TRANNOTE Text for SQL Query GUI.
           // #TRANNOTE #####################################################
           // #TRANNOTE Before you add a new resource, please make
           // #TRANNOTE sure you are not duplicating another.  The
           // #TRANNOTE goal is to keep the amount of translatable
           // #TRANNOTE text down.
           // #TRANNOTE
           // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI
           // #TRANNOTE    is { "KEY", "value" },
           // #TRANNOTE
           // #TRANNOTE The key must be left alone so translate only the value.
           // #TRANNOTE
           // #TRANNOTE


           // #TRANNOTE #####################################################
           // #TRANNOTE Button labels
           // #TRANNOTE #####################################################

           // #TRANNOTE Label for adding an item to a list.
      { "DBQUERY_BUTTON_ADD", "Add" },

           // #TRANNOTE Button label which exits the dialog without accepting the choices.
      { "DBQUERY_BUTTON_CANCEL", "Cancel" },

           // #TRANNOTE Label for button to change the schemas for which tables are shown
      { "DBQUERY_BUTTON_CHANGE_SCHEMAS", "Set schemas" },

           // #TRANNOTE Label for button to display a list of tables
      { "DBQUERY_BUTTON_DISPLAY_TABLES", "Show table list" },

           // #TRANNOTE Button label which accepts the current choice and exits the dialog.
      { "DBQUERY_BUTTON_OK", "OK" },

           // #TRANNOTE Button label which indicates an SQL inner join.
      { "DBQUERY_BUTTON_INNER_JOIN", "inner join" },

           // #TRANNOTE Button label which indicates an SQL outer join.
      { "DBQUERY_BUTTON_OUTER_JOIN", "outer join" },

           // #TRANNOTE Label for removing an item from a list.
      { "DBQUERY_BUTTON_REMOVE", "Remove" },

           // #TRANNOTE Label for button to specify a date and time field.
      { "DBQUERY_BUTTON_TIMESTAMP_2_FIELDS", "Date and time field" },

           // #TRANNOTE Label for button to specify a timestamp field.
      { "DBQUERY_BUTTON_TIMESTAMP_1_FIELDS", "Timestamp field" },



           // #TRANNOTE #####################################################
           // #TRANNOTE Choices (items in list boxes)
           // #TRANNOTE #####################################################

           // #TRANNOTE List box choice which means the user can enter a constant expression.
           // #TRANNOTE This should be the same as DBQUERY_TITLE_CONSTANT except for
           // #TRANNOTE the case of the first letter.
      { "DBQUERY_CHOICE_CONSTANT", "constant" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Column headings
           // #TRANNOTE #####################################################

           // #TRANNOTE Column heading for the name of the database field.
      { "DBQUERY_COLUMN_NAME", "Name" },

           // #TRANNOTE Column heading for the type of the database field.
      { "DBQUERY_COLUMN_TYPE", "Type" },

           // #TRANNOTE Column heading for the length of the database field.
      { "DBQUERY_COLUMN_LENGTH", "Length" },

           // #TRANNOTE Column heading for the number of decimal places of the database field.
      { "DBQUERY_COLUMN_DECIMALS", "Decimals" },

           // #TRANNOTE Column heading for whether the database field can have null values.
      { "DBQUERY_COLUMN_NULL", "Null capable" },

           // #TRANNOTE Column heading for the description of the database field.
      { "DBQUERY_COLUMN_DESCRIPTION", "Description" },

           // #TRANNOTE Column heading for items in the SELECT clause.  Only "items"
           // #TRANNOTE should be translated, "SELECT" shold not be translated.
      { "DBQUERY_COLUMN_SELECT", "SELECT items" },

           // #TRANNOTE Column heading for a database schema.
      { "DBQUERY_COLUMN_TABLE_SCHEMA", "Schema" },

           // #TRANNOTE Column heading for a database table table name.
      { "DBQUERY_COLUMN_TABLE_NAME", "Table" },

           // #TRANNOTE Column heading for a database table type.
      { "DBQUERY_COLUMN_TABLE_TYPE", "Type" },

           // #TRANNOTE Column heading for a database table description.
      { "DBQUERY_COLUMN_TABLE_TEXT", "Description" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Labels
           // #TRANNOTE #####################################################

           // #TRANNOTE Label for the database catalog.  The colon is a separator between
           // #TRANNOTE the label and the value.
      { "DBQUERY_LABEL_CATALOG", "Catalog:" },

           // #TRANNOTE Label for text field which contains the WHERE portion of an
           // #TRANNOTE SQL query.
      { "DBQUERY_LABEL_CLAUSE_WHERE", "Where clause" },

           // #TRANNOTE Label for text field which contains the SELECT portion of an
           // #TRANNOTE SQL query.
      { "DBQUERY_LABEL_CLAUSE_SELECT", "Select clause" },

           // #TRANNOTE Label for text field which contains the ORDER BY portion of an
           // #TRANNOTE SQL query.
      { "DBQUERY_LABEL_CLAUSE_ORDER", "Order By clause" },

           // #TRANNOTE Label for text field which contains the JOIN portion of an
           // #TRANNOTE SQL query.
      { "DBQUERY_LABEL_CLAUSE_JOIN", "Join By clause" },

           // #TRANNOTE Label for text field which contains the HAVING portion of an
           // #TRANNOTE SQL query.
      { "DBQUERY_LABEL_CLAUSE_HAVING", "Having clause" },

           // #TRANNOTE Label for text field which contains the GROUP BY portion of an
           // #TRANNOTE SQL query.
      { "DBQUERY_LABEL_CLAUSE_GROUP", "Group By clause" },

           // #TRANNOTE Label for list box that contains SQL functions.
      { "DBQUERY_LABEL_FUNCTIONS", "Functions" },

           // #TRANNOTE Label for a group of buttons where the user chooses the type
           // #TRANNOTE of SQL join.
      { "DBQUERY_LABEL_JOIN_TYPE", "Type of join" },

           // #TRANNOTE Label for list box that contains the "NOT" function.
      { "DBQUERY_LABEL_NOT", "Not" },

           // #TRANNOTE Label for list box that contains miscellaneous SQL components.
      { "DBQUERY_LABEL_OTHER", "Other" },

           // #TRANNOTE Label for text field which contains an SQL statement.
      { "DBQUERY_LABEL_SQL", "SQL statement" },

           // #TRANNOTE Label for Summary tab. @A1A
      { "DBQUERY_LABEL_SUMMARY", "Summary" },

           // #TRANNOTE Label for text field that contains the tables for the query.
      { "DBQUERY_LABEL_TABLES", "Tables" },

           // #TRANNOTE Label for list box that contains test conditions (such as equals,
           // #TRANNOTE less than, contains).
      { "DBQUERY_LABEL_TEST", "Test" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Error messages
           // #TRANNOTE #####################################################

           // #TRANNOTE Message used when no fields are available for a chosen function.
           // #TRANNOTE A function name will be appended to this message, so it should be
           // #TRANNOTE open-ended.  For example, the full message text will be
           // #TRANNOTE "No fields suitable for function MAX()"
      { "DBQUERY_MESSAGE_NO_FIELDS", "No fields suitable for function" },

           // #TRANNOTE Message used when a invalid value was entered when a
           // #TRANNOTE positve integer was required.
           // #TRANNOTE A function name will be prepended to this message, so it should be
           // #TRANNOTE open at the start.  For example, the full message text will be
           // #TRANNOTE "FROM value must be a positive integer."
      { "DBQUERY_MESSAGE_INVALID_INT_VALUE", "value must be a positive integer." },

           // #TRANNOTE Message used when a invalid value was entered when a
           // #TRANNOTE integer greater than 0 was required.
           // #TRANNOTE A function name will be prepended to this message, so it should be
           // #TRANNOTE open at the start.  For example, the full message text will be
           // #TRANNOTE "FOR value must be an integer greater than 0."
      { "DBQUERY_MESSAGE_INVALID_INT_VALUE2", "value must be an integer greater than 0." },

           // #TRANNOTE Message used when a invalid value was entered when a
           // #TRANNOTE integer greater than 0 was required.
      { "DBQUERY_MESSAGE_INVALID_INT_VALUE3", "Value must be an integer greater than 0." },

           // #TRANNOTE Message used when the user tries to complete a function dialog
           // #TRANNOTE without specifying a required value.  A field name will
           // #TRANNOTE be appended to this message, so it should be open-ended.
           // #TRANNOTE For example, the full message text will be
           // #TRANNOTE "A value must be specified for FROM"
      { "DBQUERY_MESSAGE_VALUE_MISSING", "A value must be specified for" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Prompts
           // #TRANNOTE #####################################################

           // #TRANNOTE Message used when the user is prompted for which field a
           // #TRANNOTE SQL function should be applied to.  A function name will
           // #TRANNOTE be appended to this message, so it should be open-ended.
           // #TRANNOTE For example, the full message text will be
           // #TRANNOTE "Field for function MAX()"
      { "DBQUERY_TEXT_CHOOSE", "Field for function" },

           // #TRANNOTE Message used when the user is prompted for values for an
           // #TRANNOTE SQL function.  A function name will
           // #TRANNOTE be appended to this message, so it should be open-ended.
           // #TRANNOTE For example, the full message text will be
           // #TRANNOTE "Values for function MAX()"
      { "DBQUERY_TEXT_CHOOSE2", "Values for function" },

           // #TRANNOTE Message used when the user is prompted for values for an
           // #TRANNOTE test (comparison).  A test name will
           // #TRANNOTE be appended to this message, so it should be open-ended.
           // #TRANNOTE For example, the full message text will be
           // #TRANNOTE "Values for test <"
      { "DBQUERY_TEXT_CHOOSE3", "Values for test" },

           // #TRANNOTE Message for when the user is prompted for a value to use in a
           // #TRANNOTE comparison (ie equal, less than) operation.
      { "DBQUERY_TEXT_COMPARE", "Enter or select a value for comparison." },

           // #TRANNOTE Message used to prompt the user to type an expression.
      { "DBQUERY_TEXT_CONSTANT", "Enter a constant expression" },

           // #TRANNOTE Message used to prompt the user to type a length.
           // #TRANNOTE If they don't type anything, a default length will be used.
      { "DBQUERY_TEXT_LENGTH", "Enter a length, or nothing to use the default length." },

           // #TRANNOTE Message used to prompt the user to enter the number of decimal postions.
           // #TRANNOTE If they don't type anything, a default will be used.
      { "DBQUERY_TEXT_LENGTH_DECIMAL", "Enter the number of decimal positions, or nothing to use the default." },

           // #TRANNOTE Message used to prompt the user to type a length.
           // #TRANNOTE A value is required, there is no default.
      { "DBQUERY_TEXT_LENGTH_REQ", "Enter a length (required)." },

           // #TRANNOTE Message used to prompt the user to type a length.
           // #TRANNOTE The length is the total length, including decimal positions.
           // #TRANNOTE If they don't type anything, a default length will be used.
      { "DBQUERY_TEXT_LENGTH_TOTAL", "Enter the total length, or nothing to use the default." },

           // #TRANNOTE Prompt telling the user to select the libraries for
           // #TRANNOTE which tables will be shown.
      { "DBQUERY_TEXT_SCHEMAS", "Select the schemas for which tables will be shown." },

           // #TRANNOTE Prompt which tells the user special character values that can be used.
           // #TRANNOTE Do not translate the characters in single quotes.
      { "DBQUERY_TEXT_SCHEMAS2", "Wildcard characters '%'(percent) and '_'(underscore) are allowed." },

           // #TRANNOTE Message used to prompt the user to type a constant value.
           // #TRANNOTE A test name will
           // #TRANNOTE be appended to this message, so it should be open-ended.
           // #TRANNOTE For example, the full message text will be
           // #TRANNOTE "Enter a constant for test <"
      { "DBQUERY_TEXT_TEST_CONSTANT", "Enter a constant for test" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Title bar text
           // #TRANNOTE #####################################################

           // #TRANNOTE Dialog box title which means the user will be chosing a value
           // #TRANNOTE for a comparison (ie equal, less than).
      { "DBQUERY_TITLE_COMPARE", "Comparison" },

           // #TRANNOTE Dialog box title which means the user can enter a constant expression.
           // #TRANNOTE This should be the same as DBQUERY_CHOICE_CONSTANT except for
           // #TRANNOTE the case of the first letter.
      { "DBQUERY_TITLE_CONSTANT", "Constant" },

           // #TRANNOTE Word or phrase indicating an error.  Will be used along with
           // #TRANNOTE a function name to build a title bar, for example "MAX() error".
      { "DBQUERY_TITLE_ERROR", "error" },

           // #TRANNOTE Text for title bar for dialog which prompts user for a length.
      { "DBQUERY_TITLE_LENGTH", "Length" },

           // #TRANNOTE Dialog box title for choosing a set of schemas
      { "DBQUERY_TITLE_SCHEMAS", "Schemas" },

           // #TRANNOTE Text for title bar for SQL query application.
      { "DBQUERY_TITLE", "SQL Query" }
   };



}

