///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PermissionTableModelQSYS.java
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
import com.ibm.as400.access.Permission;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.QSYSPermission;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.UserPermission;

/**
 *  The PermissionTableModelQSYS provides the TableModel for a specified JTable, and provides methods
 *  to add a user to the table or remove a user from the table.
 **/
class PermissionTableModelQSYS extends DefaultTableModel
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
    private Vector  userPermissions_;

    /**
     *  Private variable representing object is an authorization list.
     **/
    private boolean  isAuthorizationList_;

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
    private int selectedColumn_ =1;

    /**
     *  The String Array storing the column name of the table.
     */
    private String[] columnNames_;

    /**
     * The following two private variables representing message and title in the exception dialog.
     **/
    private static String addUserExceptionDialogMessage_;
    private static String addUserExceptionDialogTitle_;

    /**
     *  Constructs a PermissionTableModelQSYS object.
     *  @param mainPanel      The PermissionMainPanel object.
     *  @param permission    The Permission object .
     */
    public PermissionTableModelQSYS(PermissionMainPanel mainPanel,Permission permission)
    {
        QSYSObjectPathName objectPathName = new QSYSObjectPathName(permission.getObjectPath());

        if (objectPathName.getObjectType().toUpperCase().equals("AUTL"))
            isAuthorizationList_ = true;
        else
            isAuthorizationList_ = false;

        mainPanel_ = mainPanel;
        permission_ = permission;
        initializeTable();
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
     *  @param  row  The index of row.
     *  @param  column  The index of column.
     *  @return  The object that is at row, column.
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

        QSYSPermission user = (QSYSPermission)userPermissions_.elementAt(row);
        String authorityValue = user.getObjectAuthority().toUpperCase();
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
                   if (isAuthorizationList_)
                     value = new Boolean(user.isAuthorizationListManagement());
                   else
                     value = new Boolean(user.isFromAuthorizationList());
                   break;
            case 6 :
                   value = new Boolean(user.isOperational());
                   break;
            case 7 :
                   value = new Boolean(user.isManagement());
                   break;
            case 8 :
                   value = new Boolean(user.isExistence());
                   break;
            case 9 :
                   value = new Boolean(user.isAlter());
                   break;
            case 10 :
                   value = new Boolean(user.isReference());
                   break;
            case 11 :
                   value = new Boolean(user.isRead());
                   break;
            case 12 :
                   value = new Boolean(user.isAdd());
                   break;
            case 13 :
                   value = new Boolean(user.isUpdate());
                   break;
            case 14 :
                   value = new Boolean(user.isDelete());
                   break;
            case 15 :
                   value = new Boolean(user.isExecute());
                   break;
        }
        return value;
    }

    /**
     * Defines the column name.
     **/
    private void initializeTable()
    {
        columnNames_ = new String[16];
        columnNames_[0] = ResourceLoader.getText("OBJECT_USER_NAME");
        columnNames_[1] = ResourceLoader.getText("OBJECT_AUTHORITY_USE");
        columnNames_[2] = ResourceLoader.getText("OBJECT_AUTHORITY_CHANGE");
        columnNames_[3] = ResourceLoader.getText("OBJECT_AUTHORITY_ALL");
        columnNames_[4] = ResourceLoader.getText("OBJECT_AUTHORITY_EXCLUDE");

        if (isAuthorizationList_)
        {
            mainPanel_.getListField().setEnabled(false);
            columnNames_[5] = ResourceLoader.getText("OBJECT_LIST_MANAGEMENT"); //@A3C
        }
        else
        {
            columnNames_[5] = ResourceLoader.getText("OBJECT_FROM_AUTHORIZATION_LIST");
        }
        columnNames_[6] = ResourceLoader.getText("OBJECT_AUTHORITY_OPERATION");
        columnNames_[7] = ResourceLoader.getText("OBJECT_AUTHORITY_MANAGEMENT");
        columnNames_[8] = ResourceLoader.getText("OBJECT_AUTHORITY_EXISTENCE");
        columnNames_[9] = ResourceLoader.getText("OBJECT_AUTHORITY_ALTER");
        columnNames_[10] = ResourceLoader.getText("OBJECT_AUTHORITY_REFERENCE");
        columnNames_[11] = ResourceLoader.getText("OBJECT_AUTHORITY_READ");
        columnNames_[12] = ResourceLoader.getText("OBJECT_AUTHORITY_ADD");
        columnNames_[13] = ResourceLoader.getText("OBJECT_AUTHORITY_UPDATE");
        columnNames_[14] = ResourceLoader.getText("OBJECT_AUTHORITY_DELETE");
        columnNames_[15] = ResourceLoader.getText("OBJECT_AUTHORITY_EXECUTE");
        addUserExceptionDialogMessage_ = ResourceLoader.getText("OBJECT_ADD_USER_EXCEPTION_DIALOG_MESSAGE");
        addUserExceptionDialogTitle_   = ResourceLoader.getText("OBJECT_ADD_USER_EXCEPTION_DIALOG_TITLE");
    }

    /**
     * Returns true if the cell at row and column is editable. Otherwise, <i>setValueAt()</i> 
     * on the cell will not change the value of that cell.
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
                if (isAuthorizationList_) //@A2C
                {
                  if (user.getUserID().toUpperCase().equals("*PUBLIC")) //@A2C
                    return false;
                }
                else
                {
                    if (mainPanel_.getAutList().toUpperCase().equals("*NONE") ||
                        !user.getUserID().toUpperCase().equals("*PUBLIC"))
                    {
                        return false;
                    }
                }
        }
        return true;
    }

    /**
     * Removes the specified record from the table.
     * @param  row  The index of row.
     */
    public void removeRow(int row)
    {
         table_.clearSelection();   
         QSYSPermission user = (QSYSPermission)userPermissions_.elementAt(row);
         permission_.removeAuthorizedUser(user.getUserID());

         // Gets the new data after removing the specified user.
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

        QSYSPermission user = (QSYSPermission)userPermissions_.elementAt(row);

        boolean value;

        switch(column)
        {
            case 1 :
                   value = ((Boolean)aValue).booleanValue();
                   if (value)
                       user.setObjectAuthority("*Use");
                   break;
            case 2 :
                   value = ((Boolean)aValue).booleanValue();
                   if (value)
                       user.setObjectAuthority("*Change");
                   break;
            case 3 :
                   value = ((Boolean)aValue).booleanValue();
                   if (value)
                       user.setObjectAuthority("*All");
                   break;
            case 4 :
                   value = ((Boolean)aValue).booleanValue();
                   if (value)
                   {
                       user.setObjectAuthority("*Exclude");
                       user.setAuthorizationListManagement(false); //@A2A
                   }
                   break;
            case 5 :
                   value = ((Boolean)aValue).booleanValue();
                   if (isAuthorizationList_)
                       user.setAuthorizationListManagement(value);
                   else
                       user.setFromAuthorizationList(value);
                   break;
            case 6 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setOperational(value);
                   break;
            case 7 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setManagement(value);
                   break;
            case 8 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setExistence(value);
                   break;
            case 9 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setAlter(value);
                   break;
            case 10 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setReference(value);
                   break;
            case 11 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setRead(value);
                   break;
            case 12 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setAdd(value);
                   break;
            case 13 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setUpdate(value);
                   break;
            case 14 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setDelete(value);
                   break;
            case 15 :
                   value = ((Boolean)aValue).booleanValue();
                   user.setExecute(value);
                   break;
        }
        // We don't call fireTableCellUpdated because more than one
        // cell could have changed due to one of the above sets().
        fireTableRowsUpdated(row, row);

        mainPanel_.getApplyButton().setEnabled(true);
    }
}
