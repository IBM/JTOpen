///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PermissionMainPanel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Insets;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JSeparator;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.ChangeListener;
import com.ibm.as400.access.Permission;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.UserPermission;

/**
 * The PermissionMainPanel class provides three different panels according to 
 * different library structures of the object.
 */
class PermissionMainPanel 
      extends JPanel 
      implements ActionListener,
                 KeyListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     * The following five private variables respectively representing
     *   five JTextField.
     **/
    private JTextField  objectField;
    private JTextField  typeField;
    private JTextField  ownerField;
    private JTextField  groupField;
    private JTextField  listField;

    /**
     * Private variable representing a JTextfield in the dialog of Add a User.
     **/
    private JTextField  userNameField;

    /**
     * The following five variables respectively representing five buttons in the main wondow.
     **/
    private JButton    addButton;
    private JButton    removeButton;
    private JButton    okButton;
    private JButton    cancelButton;
    private JButton    applyButton;

    /**
     * Private variable representing the object of Permission.
     **/
    private Permission permission_;

    /**
     * Private variable representing JFrame.
     **/
    private JFrame frame_;

    /**
     * Private variable representing JTable.
     **/
    private JTable table_ = null;

    /**
     * Private variable representing the object of JDialog.
     **/
    private JDialog removeDialog_;

    /**
     * Private variable representing the object of JDialog.
    **/
    private JDialog addDialog_;

    /**
     * The following three variables respectively representing
     * three TableModels of the JTable.
    **/
    private PermissionTableModelDLO tableModelDLO_;
    private PermissionTableModelQSYS tableModelQSYS_;
    private PermissionTableModelRoot tableModelRoot_;

    /**
     *  Private variable indicating that the value of listField is changed.
     **/
    private boolean listChanged = false;

    /**
     *  The following private variables representing the labels for displaying.
     **/
    private static String addLabel_ = ResourceLoader.getText("DLG_ADD");
    private static String cancelLabel_ = ResourceLoader.getText("DLG_CANCEL");
    private static String removeLabel_ = ResourceLoader.getText("DLG_REMOVE");
    private static String okLabel_ = ResourceLoader.getText("DLG_OK");
    private static String yesLabel_ = ResourceLoader.getText("DLG_YES");
    private static String noLabel_ = ResourceLoader.getText("DLG_NO");
    private static String applyLabel_ = ResourceLoader.getText("DLG_APPLY");
    private static String objectName_ = ResourceLoader.getText("OBJECT_NAME");
    private static String objectType_ = ResourceLoader.getText("OBJECT_TYPE");
    private static String objectOwner_ = ResourceLoader.getText("OBJECT_OWNER");
    private static String primaryGroup_ = ResourceLoader.getText("OBJECT_GROUP");
    private static String authorizationList_ = ResourceLoader.getText("OBJECT_AUTHORIZATION_LIST");
    private static String objectPermission_ =  ResourceLoader.getText("OBJECT_PERMISSION");
    private static String addDialogTitle_ =  ResourceLoader.getText("OBJECT_ADD_DIALOG_TITLE");
    private static String removeDialogTitle_ =  ResourceLoader.getText("OBJECT_REMOVE_DIALOG_TITLE");
    private static String addDialogMessage_ = ResourceLoader.getText("OBJECT_ADD_MESSAGE");
    private static String removeDialogMessage_ = ResourceLoader.getText("OBJECT_REMOVE_MESSAGE");
    private static String commitDialogMessage_ = ResourceLoader.getText("OBJECT_COMMIT_DIALOG_MESSAGE");
    private static String commitDialogTitle_ = ResourceLoader.getText("OBJECT_COMMIT_DIALOG_TITLE");
    private static String typeNoDefined_ = ResourceLoader.getText("OBJECT_TYPE_NO_DEFINED");
    private static String permissionDialogMessage_ = ResourceLoader.getText("OBJECT_PERMISSION_DIALOG_MESSAGE");
    private static String permissionDialogTitle_ = ResourceLoader.getText("OBJECT_PERMISSION_DIALOG_TITLE");

    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);

    /**
     *  Constructs a PermissionMainPanel object.
     *  @param permission  The Permission object.
     **/
    public PermissionMainPanel(Permission permission)
    {
        permission_ = permission;
    }

    /**
     * Performs actions according to the appropriate event.
     * @param event  The ActionEvent.
     **/
    public void actionPerformed(ActionEvent event)
    {
        workingEventSupport_.fireStartWorking();

        // Add button was pressed
        if(event.getSource().equals(addButton))
        {
             String inputValue = JOptionPane.showInputDialog(new JFrame(),
                      addDialogMessage_,
                      addDialogTitle_,
                      JOptionPane.PLAIN_MESSAGE);
             if(inputValue !=null)
             {
                 int type =permission_.getType();
                 switch(type)
                 {
                    case permission_.TYPE_DLO :
                          tableModelDLO_.addRow(inputValue );
                          break;
                    case permission_.TYPE_QSYS :
                          tableModelQSYS_.addRow(inputValue );
                          break;
                    case permission_.TYPE_ROOT :
                          tableModelRoot_.addRow(inputValue );
                          break;
                 }
             }
        }

        // Remove button was pressed
        if(event.getSource().equals(removeButton))
        {
          int selectedRow = table_.getSelectedRow();
          if(selectedRow >= 0)
          {
            removeButton.setEnabled(false);
            applyButton.setEnabled(true);
            int type = permission_.getType();
            switch(type)
            {
              case permission_.TYPE_DLO :
                tableModelDLO_.removeRow(table_.getSelectedRow());
                break;
              case permission_.TYPE_QSYS :
                tableModelQSYS_.removeRow(table_.getSelectedRow());
                break;
              case permission_.TYPE_ROOT :
                tableModelRoot_.removeRow(table_.getSelectedRow());
                break;
            }
            table_.clearSelection();   
          }
        }

        // OK button was pressed
        // Commits if Apply is enabled,and closes the main window.
        // If an error occurs, we don't close the main window.
        if(event.getSource().equals(okButton))
        {
            if(applyButton.isEnabled())
            {
                try
                {
                    if(listChanged)
                    {                    
                        permission_.setAuthorizationList(listField.getText());
                        listChanged = false;
                    }
                    permission_.commit();
                    frame_.dispose();
                }
                catch (Exception err)
                {
                    Trace.log(Trace.ERROR,"commit : "+err);
                    errorEventSupport_.fireError (err);
                }
            }
            else
            {
              frame_.dispose();
            }
        }

        // Cancel button was pressed
        // Closes the main window.
        if(event.getSource().equals(cancelButton))
        {
            frame_.dispose();
        }

        // Apply button was pressed
        // Commits and closes the main window.
        if(event.getSource().equals(applyButton))
        {
            try
            {
                 if(listChanged)
                 {
                    permission_.setAuthorizationList(listField.getText());
                    listChanged = false;
                 }
                 permission_.commit();
                 applyButton.setEnabled(false);
            } catch (Exception err)
            {
                Trace.log(Trace.ERROR,"commit : "+err);
                errorEventSupport_.fireError (err);
            }
        }

        workingEventSupport_.fireStopWorking();
    }

    /**
     * Adds a listener to be notified when an error occurs. 
     * 
     * @param listener The listener.
    **/
    public void addErrorListener(ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener);
    }

    /**
     * Adds a listener to be notified when a VObject is changed, created, or deleted. 
     *
     * @param listener The listener.
    **/
    public void addVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener(listener);
    }

    /**
     * Adds a listener to be notified when work starts and stops on potentially long-running operations
     *
     * @param listener The listener.
    **/
    public void addWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener(listener);
    }

    /**
     *  Creates the main panel.
     **/
    void createPermissionMainPanel()
    {
            setLayout(new BorderLayout(5,8));
            JPanel main = this;
            JPanel part1=new JPanel(new BorderLayout(0,5));

            //starts one of part1
            JPanel box1=new JPanel(new GridLayout(2,1));
            JLabel objectLabel =new JLabel(objectName_,JLabel.LEFT);
       
            objectField=new JTextField(permission_.getObjectPath());
 
            objectField.setEditable(false);
            box1.add(objectLabel);
            box1.add(objectField);

            part1.add(box1,BorderLayout.NORTH);
 
            //starts two of part1
            JPanel box2    = new JPanel(new GridLayout(1,4,8,0));
            JPanel column1 = new JPanel(new GridLayout(2,1));
            JPanel column2 = new JPanel(new GridLayout(2,1));
            JPanel column3 = new JPanel(new GridLayout(2,1));
            JPanel column4 = new JPanel(new GridLayout(2,1));

            JLabel typeLabel = new JLabel(objectType_);

            int type = permission_.getType();
            typeField = new JTextField(typeNoDefined_);
            switch (type)
            {
                case Permission.TYPE_DLO :
                    typeField = new JTextField("DLO");
                    break;
                case Permission.TYPE_QSYS :
                    typeField = new JTextField("QSYS");
                    break;
                case Permission.TYPE_ROOT :
                    typeField = new JTextField("ROOT");
                    break;
            }
            typeField.setEditable(false);
            column1.add(typeLabel);
            column1.add(typeField);
            box2.add(column1);

            JLabel ownerLabel = new JLabel(objectOwner_);
            ownerField = new JTextField(permission_.getOwner());
            ownerField.setEditable(false);
            column2.add(ownerLabel);
            column2.add(ownerField);
            box2.add(column2);

            JLabel groupLabel = new JLabel(primaryGroup_);
            groupField = new JTextField(permission_.getPrimaryGroup());
            groupField.setEditable(false);
            column3.add(groupLabel);
            column3.add(groupField);
            box2.add(column3);

            JLabel listLabel = new JLabel(authorizationList_);
            listField = new JTextField(permission_.getAuthorizationList());
            listField.addKeyListener(this);
            column4.add(listLabel);
            column4.add(listField);
            box2.add(column4);

            part1.add(box2,BorderLayout.CENTER);

            JPanel box3 = new JPanel(new GridLayout(4,1));
            box3.add(new JLabel());
            box3.add(new JLabel());
            box3.add(new JSeparator());
            box3.add(new JLabel());

            part1.add(box3,BorderLayout.SOUTH);

            // Part3
            JPanel part3 = new JPanel(new BorderLayout(0,10));

            // Creates two buttons - OK and Cancel.
            JPanel line1 = new JPanel(new GridLayout(1,6,5,0));
            line1.add(new JLabel());
            line1.add(new JLabel());
            line1.add(new JLabel());
            line1.add(new JLabel());
            addButton    = new JButton(addLabel_);
            removeButton = new JButton(removeLabel_);
            addButton.addActionListener(this);
            removeButton.addActionListener(this);
            removeButton.setEnabled(false);
            line1.add(addButton);
            line1.add(removeButton);
            part3.add(line1,BorderLayout.NORTH);
            part3.add(new JSeparator(),BorderLayout.CENTER);

            JPanel line2 = new JPanel(new GridLayout(1,6,5,0));
            line2.add(new JLabel());
            line2.add(new JLabel());
            line2.add(new JLabel());

            okButton     = new JButton(okLabel_);
            cancelButton = new JButton(cancelLabel_);
            applyButton  = new JButton(applyLabel_);
            okButton.addActionListener(this);
            cancelButton.addActionListener(this);
            applyButton.addActionListener(this);
            applyButton.setEnabled(false);

            line2.add(okButton);
            line2.add(cancelButton);
            line2.add(applyButton);
            part3.add(line2,BorderLayout.SOUTH);
            main.add(part1,BorderLayout.NORTH);
            main.add(part3,BorderLayout.SOUTH);           
            main.add(getTablePane(),BorderLayout.CENTER);

            frame_ = new JFrame(ResourceLoader.substitute(objectPermission_, permission_.getName())); // @C1C
            frame_.getContentPane().add(main);
            frame_.setSize(650,450);
            frame_.setVisible(true);
    }

   /**
    * Invoked when an error has occurred.
    *
    **/
    void fireError(Exception except)
    {
      errorEventSupport_.fireError(new ErrorEvent(this, except));
    }

    /**
     * Returns The Apply button.
     * @return The Apply button.
     **/
    public JButton getApplyButton()
    {
        return applyButton;
    }

    /**
     * Returns the text of the field authorization list.
     * @return the text of the field authorization list.
    **/
    public String getAutList()
    {
        return listField.getText().trim();
    }

    /**
     * Copyright.
     **/
     private static String getCopyright()
     {
         return Copyright_v.copyright;
     }
     
    /**
     *  Return Insets object that defines the interval with the border.
     *  @return   Insets object that defines the interval with the border.
     **/
    public Insets getInsets()
    {
        return new Insets(5,8,5,8);
    }
    
    /**
     * Returns the authorization list field.
     * @return The authorization list field.
     **/
    public JTextField getListField()
    {
        return listField;
    }

    /**
     * Returns the Remove button.
     * @return The Remove button.
     **/
    public JButton getRemoveButton()
    {
        return removeButton;
    }

    /**
     *  Return the table pane.
     *  @return The table component.
     **/
    private JComponent getTablePane()
    {
        int type = permission_.getType();        
        switch(type)
        {
            case permission_.TYPE_DLO:
                  tableModelDLO_ = new PermissionTableModelDLO(this,permission_);
                  table_= new JTable(tableModelDLO_);
                  tableModelDLO_.setTableInstance(table_);
                  table_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                  break;
            case permission_.TYPE_QSYS:
                  tableModelQSYS_ = new PermissionTableModelQSYS(this,permission_);
                  table_= new JTable(tableModelQSYS_);
                  tableModelQSYS_.setTableInstance(table_);
                  table_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                  break;
            case permission_.TYPE_ROOT:
                  tableModelRoot_ = new PermissionTableModelRoot(this,permission_);
                  table_= new JTable(tableModelRoot_);
                  tableModelRoot_.setTableInstance(table_);
                  table_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                  break;
        }

        JScrollPane jsp = null;
        if (table_ != null)
        {
            table_.setAutoCreateColumnsFromModel(false);
            table_.setDefaultRenderer(table_.getColumnClass(0),
               (TableCellRenderer)new VObjectCellRenderer());
            table_.setCellSelectionEnabled(true);
            table_.selectAll();
            table_.setSelectionMode(0);
            table_.setSelectionBackground(Color.gray);
            table_.setShowGrid(false);
            table_.clearSelection();
            // Set the width of the name column to be wide enough to
            // display any username (max of 10 characters)
            // The +16 is for the icon.
            // The +2 is for the space between characters.
            // The 'W' is used since it is a very wide character.
            if (table_.getFont() != null)
            {
              int fontSize = table_.getFontMetrics(table_.getFont()).charWidth('W');
              table_.getColumnModel().getColumn(0).setWidth(10*(fontSize+2)+16);
            }
            jsp = new JScrollPane(table_);
        }
        return jsp;
    }

    /** The key is pressed.
     * @param keyEvent  The key event.
     **/
    public void keyPressed(KeyEvent keyEvent){}

    /** The key is released.
     * @param keyEvent  The key event.
     **/
    public void keyReleased(KeyEvent keyEvent)
    {
        switch (keyEvent.getKeyCode())
        {
            case KeyEvent.VK_TAB : 
            case KeyEvent.VK_ENTER :
            case KeyEvent.VK_ALT : 
            case KeyEvent.VK_SHIFT :
            case KeyEvent.VK_CONTROL : 
            case KeyEvent.VK_ESCAPE :
            case KeyEvent.VK_PAGE_DOWN :
            case KeyEvent.VK_PAGE_UP : 
            case KeyEvent.VK_UP     : 
            case KeyEvent.VK_DOWN : 
            case KeyEvent.VK_RIGHT :
            case KeyEvent.VK_LEFT :
            case KeyEvent.VK_HOME : 
            case KeyEvent.VK_END  : 
            case KeyEvent.VK_INSERT : 
            case KeyEvent.VK_CAPS_LOCK :
            case KeyEvent.VK_NUM_LOCK :
            case KeyEvent.VK_PRINTSCREEN :
            case KeyEvent.VK_SCROLL_LOCK :
            case KeyEvent.VK_PAUSE :
            case KeyEvent.VK_F1 : 
            case KeyEvent.VK_F2 : 
            case KeyEvent.VK_F3 : 
            case KeyEvent.VK_F4 : 
            case KeyEvent.VK_F5 : 
            case KeyEvent.VK_F6 : 
            case KeyEvent.VK_F7 : 
            case KeyEvent.VK_F8 : 
            case KeyEvent.VK_F9 : 
            case KeyEvent.VK_F10 : 
            case KeyEvent.VK_F11 : 
            case KeyEvent.VK_F12 :
                break;
            default :
                if (!listChanged)
                {
                  listChanged = true;
                  applyButton.setEnabled(true);
                }
                if (listField.getText().trim().toUpperCase().equals("*NONE"))
                {
                  // Can't call setValueAt directly because we don't know
                  // the row for *PUBLIC, but resetPublicAuthorizationList() does.
//                  permission_.getUserPermission("*PUBLIC").setFromAuthorizationList(false);
                  int type = permission_.getType();
                  switch(type)
                  {
                    case permission_.TYPE_DLO :
                          tableModelDLO_.resetPublicAuthorizationList();
                          break;
                    case permission_.TYPE_QSYS :
                          tableModelQSYS_.resetPublicAuthorizationList();
                          break;
                    case permission_.TYPE_ROOT :
                          tableModelRoot_.resetPublicAuthorizationList();
                          break;
                  }
                }
        }
    }

    /**
     * The key is typed.
     * @param keyEvent  The key event.
     **/
    public void keyTyped(KeyEvent keyEvent){}

    /**
     * Removes an error listener. 
     *
     * @param listener The listener.
    **/
    public void removeErrorListener(ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener(listener);
    }

    /**
     * Removes a VObjectListener
     *
     * @param listener The listener.
    **/
    public void removeVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener(listener);
    }

    /**
     * Removes a working listener. 
     *
     * @param listener The listener.
    **/
    public void removeWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener(listener);
    }

}

