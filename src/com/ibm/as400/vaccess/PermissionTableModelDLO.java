///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PermissionTableModelDLO.java
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.DLOPermission;
import com.ibm.as400.access.Permission;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.UserPermission;

/**
 *  The PermissionTableModelDLO class provides the TableModel for a specified JTable,
 *  and provides methods to add a user to the table or remove a user from the table.
 **/
class PermissionTableModelDLO extends DefaultTableModel
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     *  Private variable representing the object of Permission.
     **/
    private Permission permission_;

    /**
     *  Private variable representing the vector that stores userPermission objects
     *     of the specified Permission.
     **/
    private Vector userPermissions_;

    /**
     *  Private variable representing the object of JTable .
     **/
    private JTable table_;

    /**
     *  Private variable representing the object of PermissionMainPanel object .
     **/
    private PermissionMainPanel mainPanel_;

    /**
     *  Private variable representing the number of selected column in the table.
     **/
    private int selectedColumn_ =1;

    /**
     *  The String Array storing the column name of the table.
     */
    private static String[] columnNames_;

    /**
     * The following two private variables representing message and title in the exception dialog.
     **/
    private static String addUserExceptionDialogMessage_;
    private static String addUserExceptionDialogTitle_;

    /**
     *  Constructs a PermissionsTableTableModelDLO object.
     *  @param mainPanel     The PermissionMainPanel object.
     *  @param permission    The Permission object.
     */
    public PermissionTableModelDLO(PermissionMainPanel mainPanel,Permission permission)
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
     * Adds a new user permission in the specified permission.
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
     * Returns the name of the column at column.
     * @return  The column number of the table.
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
     *  @param  row     The index of row.
     *  @param  column  The index of column.
     *  @return         The object that is at row, column.
     */
    public Object getValueAt(int row, int column)
    {
        if (row >= userPermissions_.size())
            return null;

        if (selectedColumn_ >0)
            table_.clearSelection();
        else if (table_.getSelectedRow()>=0
               && table_.getSelectedRowCount() > 0) //@B0A        
            mainPanel_.getRemoveButton().setEnabled(true);

        DLOPermission user=null;

        user = (DLOPermission)userPermissions_.elementAt(row);

        String authorityValue = user.getDataAuthority().toUpperCase();
        Object value = null;
        switch(column)
        {
            case 0 :
                   value = new PermissionNameCellObject(user.getUserID(),user.getGroupIndicator());
                   break;
            case 1 :
                   value = new Boolean(authorityValue.equals("*USE"));
                   break;
            case 2 :
                   value = new Boolean(authorityValue.equals("*CHANGE"));
                   break;
            case 3 :
                   value = new Boolean(authorityValue.equals("*ALL"));
                   break;
            case 4 :
                   value = new Boolean(authorityValue.equals("*EXCLUDE"));
                   break;
            case 5 :
                   value = new Boolean(user.isFromAuthorizationList());
                   break;
            case 6:                                                        //@A2A
                   value = new Boolean(authorityValue.equals("USER_DEF")); //@A2A
                   break;                                                  //@A2A
        }
        return value;
    }

    /**
     * Defines the column name.
     **/
    private void initializeTable()
    {
        
        columnNames_ = new String[7]; //@A2C
                
        columnNames_[0] = ResourceLoader.getText("OBJECT_USER_NAME");
        columnNames_[1] = ResourceLoader.getText("OBJECT_AUTHORITY_USE");
        columnNames_[2] = ResourceLoader.getText("OBJECT_AUTHORITY_CHANGE");
        columnNames_[3] = ResourceLoader.getText("OBJECT_AUTHORITY_ALL");
        columnNames_[4] = ResourceLoader.getText("OBJECT_AUTHORITY_EXCLUDE");
        columnNames_[5] = ResourceLoader.getText("OBJECT_FROM_AUTHORIZATION_LIST");
        columnNames_[6] = ResourceLoader.getText("OBJECT_AUTHORITY_USER_DEF"); //@A2A
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
            case 5 :
                UserPermission user = (UserPermission)userPermissions_.elementAt(row);
                if (mainPanel_.getAutList().toUpperCase().equals("*NONE") || 
                    !user.getUserID().toUpperCase().equals("*PUBLIC"))
                {
                    return false;
                }
                return true; //@A2A
            case 6 :          //@A2A
                return false; //@A2A
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
        DLOPermission user = (DLOPermission)userPermissions_.elementAt(row);
        permission_.removeUserPermission(user);
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
          setValueAt(new Boolean(false), row, 5);
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

        DLOPermission user = (DLOPermission)userPermissions_.elementAt(row);

        boolean value;
        switch(column)
        {
            case 1 :
                   value = ((Boolean)aValue).booleanValue();
                   if (value == true)
                        user.setDataAuthority("*Use");
                   break;
            case 2 :
                   value = ((Boolean)aValue).booleanValue();
                   if (value == true)
                        user.setDataAuthority("*Change");
                   break;
            case 3 :
                   value = ((Boolean)aValue).booleanValue();
                   if (value == true)
                        user.setDataAuthority("*All");
                   break;
            case 4 :
                   value = ((Boolean)aValue).booleanValue();
                   if (value == true)
                       user.setDataAuthority("*Exclude");
                   break;
            case 5 :
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
