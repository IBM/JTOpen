///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PermissionTableModelRoot.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.Permission;
import com.ibm.as400.access.RootPermission;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.UserPermission;

/**
 *  The PermissionTableModelRoot class provides the TableModel for a specified JTable,
 *  and provides methods to add a user to the table or remove a user from the table.
 **/

class PermissionTableModelRoot extends DefaultTableModel
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     *  Private variable representing the object of Permission.
     **/
    private Permission permission_;

    /**
     *  Private variable representing the vector that stores userPermission object
     *  of the specified Permisssions.
     **/
    private Vector userPermissions_;

    /**
     *  Private variable representing the object of JTable.
     **/
    private JTable table_;

    /**
     *  Private variable representing the object of MainPanel.
     **/
    private PermissionMainPanel mainPanel_;

    /**
     *  Private variable representing the number of selected column in the table.
     **/
    private int selectedColumn_ = 1;

    /**
     *  The following three private variables representing the value of the authorities.
    **/
    private boolean read = false;
    private boolean write = false;
    private boolean execute = false;

    /**
     *  The String Array storing the column name of the table.
     */
    private String[] columnNames_ ;
    
    /**
     * The following two private variables representing message and title in the exception dialog.
     **/
    private static String addUserExceptionDialogMessage_;
    private static String addUserExceptionDialogTitle_;
    
    /**
     *  Constructs a PermissionTableModelRoot object.
     *  @param mainPanel     The object of the PermissionMainPanel object.
     *  @param permission    The object of the Permission object.
     */
    public PermissionTableModelRoot(PermissionMainPanel mainPanel,Permission permission)
    {
        initializeTable();
        mainPanel_ = mainPanel;
        permission_ = permission;
        Enumeration userPermissions = permission_.getUserPermissions();
        userPermissions_ = new Vector();
        while(userPermissions.hasMoreElements())
        {
            userPermissions_.addElement(userPermissions.nextElement());
        }
    }

    /**
     * Add a new user permission in the specified permission.
     * @param  name  The user name.
     */
    public void addRow(String name)
    {
         try
         {
             permission_.addAuthorizedUser(name);
             mainPanel_.getApplyButton().setEnabled(true);
             table_.clearSelection();
    
             // Gets the new data after adding the specified user.
             Enumeration userPermissions = permission_.getUserPermissions();
             userPermissions_ = new Vector();
             while(userPermissions.hasMoreElements())
             {
                userPermissions_.addElement(userPermissions.nextElement());
             }
             int updatedRow = userPermissions_.size()-1;
             fireTableRowsInserted(updatedRow, updatedRow);
         }
         catch(ExtendedIllegalArgumentException e)
         {
            Trace.log(Trace.ERROR,"Error in addRow : "+e);
            mainPanel_.fireError(e);
         }
    }

    /**
     * Returns the lowest common denominator class in the column.
     * @param  column  The index of column.
     * @return  The common ancestor class of the object values in the model.
     */
    public Class getColumnClass(int column)
    {
        return getValueAt(0,column).getClass();
    }

    /**
     * Returns the number of columns in the table.
     * @return  The number of columns in the table.
     */
    public int getColumnCount()
    {
        return columnNames_.length;
    }

    /**
     * Returns the name of the column at column. This is used to initialize the table's
     * column header name.
     * @return  The row number of the table.
     */
    public String getColumnName(int column)
    {
        return  columnNames_[column];
    }

    /**
     * Returns the number of records in the table.
     * @return  The number of records in the table.
     */
    public int getRowCount()
    {
        if (userPermissions_==null)
            return 0;
        return userPermissions_.size();
    }

    /**
     *  Returns an attribute value for the cell at column and row.
     *  @param  row  The index of row.
     *  @param  column  The index of column.
     *  @return The object that is at row, column.
     */
    public Object getValueAt(int row, int column)
    {
        if (row >= userPermissions_.size())
            return null;
        if(selectedColumn_ >0) 
        {
            table_.clearSelection();
        } else
        {
            if(table_.getSelectedRow()>=0
               && table_.getSelectedRowCount() > 0) //@B0A
                mainPanel_.getRemoveButton().setEnabled(true);
        }
        RootPermission user=null;

        user = (RootPermission)userPermissions_.elementAt(row);

        String authorityValue = user.getDataAuthority().toUpperCase();
        Object value = null;
        switch(column)
        {
            case 0 :
                   value = new PermissionNameCellObject(user.getUserID(),user.getGroupIndicator());
                   break;
            case 1 :
                   value = new Boolean(authorityValue.indexOf("R") > -1);
                   break;
            case 2 :
                   value = new Boolean(authorityValue.indexOf("W") > -1);
                   break;
            case 3 :
                   value = new Boolean((authorityValue.indexOf("X") > -1) &&
                                       (authorityValue.indexOf("E") == -1));
                   break;
            case 4 :
                   value = new Boolean(user.isManagement());
                   break;
            case 5 :
                   value = new Boolean(user.isExistence());
                   break;
            case 6 :
                   value = new Boolean(user.isAlter());
                   break;
            case 7 :
                   value = new Boolean(user.isReference());
                   break;
            case 8 :
                   value = new Boolean(user.isFromAuthorizationList());
                   break;
        }
        return value;
    }

    /**
     * Defines the column name.
     **/
    private void initializeTable()
    {
        
        columnNames_ = new String[9];

        columnNames_[0] = ResourceLoader.getText("OBJECT_USER_NAME");
        columnNames_[1] = ResourceLoader.getText("OBJECT_AUTHORITY_READ");
        columnNames_[2] = ResourceLoader.getText("OBJECT_AUTHORITY_WRITE");
        columnNames_[3] = ResourceLoader.getText("OBJECT_AUTHORITY_EXECUTE");
        columnNames_[4] = ResourceLoader.getText("OBJECT_AUTHORITY_MANAGEMENT");
        columnNames_[5] = ResourceLoader.getText("OBJECT_AUTHORITY_EXISTENCE");
        columnNames_[6] = ResourceLoader.getText("OBJECT_AUTHORITY_ALTER");
        columnNames_[7] = ResourceLoader.getText("OBJECT_AUTHORITY_REFERENCE");
        columnNames_[8] = ResourceLoader.getText("OBJECT_FROM_AUTHORIZATION_LIST");
        addUserExceptionDialogMessage_ = ResourceLoader.getText("OBJECT_ADD_USER_EXCEPTION_DIALOG_MESSAGE");
        addUserExceptionDialogTitle_   = ResourceLoader.getText("OBJECT_ADD_USER_EXCEPTION_DIALOG_TITLE");
    }

    /**
     * Returns true if the cell at row and column is editable. Otherwise, <i>setValueAt()</i> on the cell will not
     * change the value of that cell.
     * @param  row  The index of row.
     * @param  column  The index of column.
     * @return True if the cell is editable.
     */
    public boolean isCellEditable(int row,int column)
    {
        selectedColumn_ = column;
        switch (column)
        {
            case 0 : 
                return false;
            case 8 :
                UserPermission user = (UserPermission)userPermissions_.elementAt(row);
                if (mainPanel_.getAutList().toUpperCase().equals("*NONE") || 
                    !user.getUserID().toUpperCase().equals("*PUBLIC"))
                {
                    return false;
                }

            default :
                return true;
        }
    }

    /**
     * Removes the specified record from the table.
     * @param  row  The index of row.
     */
    public void removeRow(int row)
    {
        table_.clearSelection();   
        RootPermission user = (RootPermission)userPermissions_.elementAt(row);
        permission_.removeAuthorizedUser(user.getUserID());
        Enumeration userPermissions = permission_.getUserPermissions();
        userPermissions_ = new Vector();
        while(userPermissions.hasMoreElements())
        {
            userPermissions_.addElement(userPermissions.nextElement());
        }
        fireTableRowsDeleted(row, row);
    }

    /**
    Used so the main panel can set the authorization list checkbox on
    *PUBLIC to false.
    **/
    void resetPublicAuthorizationList()
    {
        UserPermission up = permission_.getUserPermission("*PUBLIC");
        int row = userPermissions_.indexOf(up);
        if (row >= 0)
        {
          setValueAt(new Boolean(false), row, 8);
        }
    }

    /**
     *  Sets the JTable object.
     *  @param table   The JTable object.
     **/
    public void setTableInstance(JTable table)
    {
        table_ = table;
    }

    /**
     *  Sets an attribute value for the record in the cell at column and row.
     *  @param  aValue    The new value.
     *  @param  row       The index of row.
     *  @param  column    The index of column.
     */
    public void setValueAt(Object aValue,int row,int column)
    {
        mainPanel_.getRemoveButton().setEnabled(false);

        boolean value;
        RootPermission user = (RootPermission)userPermissions_.elementAt(row);
        String authorityValue = user.getDataAuthority().trim();
        if (authorityValue.equals("*EXCLUDE")||
            authorityValue.equals("*NONE"))
        {
            read = false;
            write = false;
            execute = false;
        } else
        {
           if(authorityValue.indexOf("R")>-1)
               read = true;
           else
               read = false;
           if(authorityValue.indexOf("W")>-1)
               write = true;
           else
               write = false;
           if(authorityValue.indexOf("X")>-1)
               execute = true;
           else
               execute = false;
        }

        String string="*";
        switch(column)
        {
            case 1 :
                   read = ((Boolean)aValue).booleanValue();
                   if (read)
                       string += "R";
                   if (write)
                       string += "W";
                   if (execute)
                       string += "X";
                   if (string.equals("*"))
                       string += "NONE";
                   user.setDataAuthority(string);
                   break;
            case 2 :
                   write = ((Boolean)aValue).booleanValue();
                   if (read)
                       string += "R";
                   if (write)
                       string += "W";
                   if (execute)
                       string += "X";
                   if (string.equals("*"))
                       string += "NONE";
                   user.setDataAuthority(string);
                   break;
            case 3 :
                   execute = ((Boolean)aValue).booleanValue();
                   if (read)
                       string += "R";
                   if (write)
                       string += "W";
                   if (execute)
                       string += "X";
                   if (string.equals("*"))
                       string += "NONE";
                   user.setDataAuthority(string);
                   break;
            case 4 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setManagement(value);
                   break;
            case 5 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setExistence(value);
                   break;
            case 6 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setAlter(value);
                   break;
            case 7 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setReference(value);
                   break;
            case 8 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setFromAuthorizationList(value);
                   break;
        }
        // We don't call fireTableCellUpdated because more than one
        // cell could have changed due to one of the above sets().
        fireTableRowsUpdated(row, row);

        mainPanel_.getApplyButton().setEnabled(true);
     }
}

