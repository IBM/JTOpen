///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSysvalArrayDialog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SystemValue;

import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JList;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JTabbedPane;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JRadioButton;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.Frame;
import java.awt.Component;

import java.util.Vector;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import com.ibm.as400.access.AS400Exception;


    /**
     * The VsysvalArrayDialog class defines a dialog 
     * for the user to modify system values of 
     * array type.
    **/
    class VSysvalArrayDialog 
                 extends JDialog 
                 implements ActionListener,
                            ListSelectionListener,
                            KeyListener
    {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
        // Private data.
        private VSystemValue systemValue_;
  
        private JButton addButton_;
        private JButton removeButton_;
        private JButton changeButton_;
        private JButton okButton_;
        private JButton cancelButton_;
        private JButton applyButton_;

        private JList list_;
        private JTextField textField_;
        private Vector vector_;
        private Frame parent_;

        private boolean changed_;

        // MRI
        final private static String okButtonText_;
        final private static String cancelButtonText_;
        final private static String applyButtonText_;
        final private static String modifyDialogTitle_;
        final private static String modifyActionTab_;
        final private static String valueName_;
        final private static String valueDescription_;
        final private static String valueInformation_;
        final private static String addButtonText_;
        final private static String removeButtonText_;
        final private static String changeButtonText_;

        static
        {
		
			okButtonText_ = (String)ResourceLoader.getText("DLG_OK");
			cancelButtonText_ = (String)ResourceLoader.getText("DLG_CANCEL");
			applyButtonText_ = (String)ResourceLoader.getText("DLG_APPLY");
                        modifyDialogTitle_ = (String)ResourceLoader.getText("DLG_MODIFY_0"); // @C1C
			modifyActionTab_ = (String)ResourceLoader.getText("ACTION_MODIFY");
			valueName_ = (String)ResourceLoader.getText("COLUMN_NAME");
			valueDescription_ = (String)ResourceLoader.getText("COLUMN_DESCRIPTION");
			valueInformation_ = (String)ResourceLoader.getText("COLUMN_VALUE");
			addButtonText_ = (String)ResourceLoader.getText("DLG_ADD");
			removeButtonText_ = (String)ResourceLoader.getText("DLG_REMOVE");
			changeButtonText_ = (String)ResourceLoader.getText("DLG_CHANGE");
		}
        

        /**
         * Constructs a VSysvalArrayDialog object. it creates a dialog to modify 
         * the array value.
         * @param frame	The parent Frame object.
         * @param sysval The SystemValue object.
         *
        **/
        public VSysvalArrayDialog(Frame frame, VSystemValue sysval)
        {
            super(frame, ResourceLoader.substitute(modifyDialogTitle_, sysval.getName()),true); // @C1C
            parent_ = frame;
    
            systemValue_ = sysval;
            vector_ = new Vector();
            Object[] obj = null;
            obj = (Object[])systemValue_.getValue();

            for (int i=0;i<obj.length;i++)
            {
                vector_.addElement((String)obj[i]);
            }

            changed_ = false;

            createPane();
            pack();
        }

        /**
         * Processes the action event.
         * @param event The action event.
         *
        **/
        public void actionPerformed(ActionEvent event)
        {
            Object source = event.getSource();
            String text;
            int index;

            // Add
            if(source.equals(addButton_))
            {
                text = textField_.getText().toUpperCase();
                if (vector_.indexOf(text)==-1 && text.trim().length() > 0)
                {
                    vector_.addElement(text);
                    list_.setListData(vector_);
                    changed_ = true;
                    list_.setSelectedValue(text,true);
                    changeButton_.setEnabled(false);
                    applyButton_.setEnabled(true);
                }
            } 
            // Remove
            else if(source.equals(removeButton_))
            {
                index = list_.getSelectedIndex();
                if (index!=-1)
                {
                    vector_.removeElementAt(index);
                    list_.setListData(vector_);
                    changed_ = true;
                    applyButton_.setEnabled(true);
                    int size = vector_.size();
                    if (index<size)
                        list_.setSelectedIndex(index);
                    else if (size>0)
                        list_.setSelectedIndex(size-1);
                    if (list_.getSelectedIndex()==-1)
                    {
                        removeButton_.setEnabled(false);
                        changeButton_.setEnabled(false);
                    }
                }
            } 
            // Change
            else if(source.equals(changeButton_))
            {
                index = list_.getSelectedIndex();
                text = textField_.getText();
                if (index!=-1)
                {
                    if (vector_.indexOf(text)==-1)
                    {
                        vector_.setElementAt(text,index);
                        list_.setListData(vector_); // this is to refresh the list
                        changed_ = true;
                        applyButton_.setEnabled(true);
                        changeButton_.setEnabled(false);
                    }
                }
            } 
            // OK
            else if(source.equals(okButton_))
            {
              if (changed_)
              {
                int count = vector_.size();
                String[] value= new String[count];
                for (int i=0;i<count;i++)
                {
                    value[i]= (String)vector_.elementAt(i);
                }
                systemValue_.setValue(value);
                changed_ = false;
              }
              dispose();
            } 
            // Cancel
            else if(source.equals(cancelButton_))
            {
                dispose();
            } 
            // Apply
            else if(source.equals(applyButton_))
            {
                int count = vector_.size();
                String[] value= new String[count];
                for (int i=0;i<count;i++)
                {
                    value[i]= (String)vector_.elementAt(i);
                    applyButton_.setEnabled(false);
                }
                systemValue_.setValue(value);
                changed_ = false;
            }
        }

        /**
         * Adds notification.
         *
        **/
//        public void addNotify()
//        {
//            super.addNotify();
//            setSize(getInsets().left + getInsets().right + 320,
//                getInsets().top + getInsets().bottom + 300);
//        }

        /**
         * Creates the pane.
        **/
        private void createPane()
        {
            // This is for the OK, Cancel, and Apply buttons
            JPanel buttonPanel = getButtonPanel();

            // This is for the rest
            JPanel panel = getModifyTab();

            Container container = getContentPane();
            container.setLayout(new BorderLayout());

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab(modifyActionTab_, panel);
//            container.add("West", new JPanel());
//            container.add("East", new JPanel());
            container.add("South", buttonPanel);        
            container.add("Center", tabbedPane);  
            setLocation(parent_.getLocation().x+parent_.getSize().width/2,
                    parent_.getLocation().y+parent_.getSize().height/2);
            list_.setSelectedIndex(0);
            textField_.setText((String)list_.getSelectedValue());
        }

        /**
         * Returns the button panel.
         * @return The button panel.
         *
        **/
        private JPanel getButtonPanel()
        {
            JPanel pane=new JPanel();
            okButton_=new JButton(okButtonText_);
            cancelButton_=new JButton(cancelButtonText_);
            applyButton_=new JButton(applyButtonText_);
            applyButton_.setEnabled(false);
            pane.add(okButton_);
            pane.add(cancelButton_);
            pane.add(applyButton_);

            okButton_.addActionListener(this);
            applyButton_.addActionListener(this);
            cancelButton_.addActionListener(this);
    
            return pane;
        }

        /**
     	 * Returns the copyright.
        **/
    	private static String getCopyright()
	    {
    	    return Copyright_v.copyright;
	    }

        /**
         * Returns the insets.
         * @return The insets.
         *
        **/
//        public Insets getInsets()
//        {
//            Insets insets = super.getInsets();
//            return new Insets(insets.top+10,insets.left,
//                          insets.bottom+20,insets.right);
//        }

        /**
         * Returns the tabbed panel component.
         *
        **/
        private JPanel getModifyTab()
        {
            JPanel panel = new JPanel();
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints gbc = null;
            panel.setLayout(((LayoutManager)layout));
            panel.setBorder(((Border)new EmptyBorder(10, 10, 10, 10)));

            // "Name:"
            gbc = new GridBagConstraints();
            gbc.ipady = 3;
            VUtilities.constrain(new JLabel(valueName_+": "),
                                 panel, layout, gbc,
                                 0, 1, 0,  0, 1, 0,
                                 GridBagConstraints.NONE,
                                 GridBagConstraints.NORTHWEST);
            // Name of sysval
            gbc = new GridBagConstraints();
            gbc.ipady = 3;
            gbc.ipadx = 3;
            VUtilities.constrain(new JLabel(systemValue_.getName()),
                                 panel, layout, gbc,
                                 1, 1, 0,  0, 1, 0,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST);
            // "Description:"
            gbc = new GridBagConstraints();
            gbc.ipady = 3;
            VUtilities.constrain(new JLabel(valueDescription_+": "),
                                 panel, layout, gbc,
                                 0, 1, 0,  1, 1, 0,
                                 GridBagConstraints.NONE,
                                 GridBagConstraints.NORTHWEST);
            // Description of sysval
            gbc = new GridBagConstraints();
            gbc.ipady = 3;
            gbc.ipadx = 3;
            VUtilities.constrain(new JLabel(systemValue_.getDescription()),
                                 panel, layout, gbc,
                                 1, 1, 0,  1, 1, 0,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST);

            // Text input field
            textField_ = new JTextField();
            textField_.addKeyListener(this);
            gbc = new GridBagConstraints();
            gbc.ipady = 3;
            VUtilities.constrain(textField_, panel, layout, gbc,
                                 0, 2, 1,  2, 1, 0,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST);


            // Scroll pane with list of values
            list_ = new JList(vector_);
            list_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only select one item at a time
            list_.addListSelectionListener(this);
            JScrollPane scrollPane = new JScrollPane(list_);
            gbc = new GridBagConstraints();
            gbc.ipady = 3;
            VUtilities.constrain(scrollPane, panel, layout, gbc,
                                 0, 2, 1,  3, 4, 2,
                                 GridBagConstraints.BOTH,
                                 GridBagConstraints.CENTER);

            // Add button
            addButton_ = new JButton(addButtonText_);
            addButton_.addActionListener(this);
            addButton_.setEnabled(false); // Default is disabled
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(5,10,5,10); // So the buttons aren't so close together
            VUtilities.constrain(addButton_, panel, layout, gbc,
                                 2, 1, 0,  3, 1, 0,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.CENTER);
        
            // Remove button
            removeButton_ = new JButton(removeButtonText_);
            removeButton_.addActionListener(this);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(5,10,5,10); // So the buttons aren't so close together
            VUtilities.constrain(removeButton_, panel, layout, gbc,
                                 2, 1, 0,  4, 1, 0,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.CENTER);
        
            // Change button
            changeButton_ = new JButton(changeButtonText_);
            changeButton_.addActionListener(this);
            changeButton_.setEnabled(false);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(5,10,5,10); // So the buttons aren't so close together
            VUtilities.constrain(changeButton_, panel, layout, gbc,
                                 2, 1, 0,  5, 1, 0,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.CENTER);

            return panel;
        }

        /**
         * The key is pressed.
         *
        **/
        public void keyPressed(KeyEvent event)
        {
        }

        /**
         * The key is released.
         * @param event  The key event.
         *
        **/
        public void keyReleased(KeyEvent event)
        {
            if(event.getSource().equals(textField_))
            {
                switch (event.getKeyCode())
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
                        String text = textField_.getText().toUpperCase();
                        if (vector_.indexOf(text) != -1) // It's in the list already
                        {
                          addButton_.setEnabled(false);
                          changeButton_.setEnabled(false);
                        }
                        else  // It's not in the list yet
                        {
                          addButton_.setEnabled(true);
                          // Can't change an entry that isn't selected
                          if (list_.getSelectedIndex() != -1)
                            changeButton_.setEnabled(true);
                          else
                            changeButton_.setEnabled(false);
                        }
                }   
            }
        }

        /**
         * The key is typed.
         * @param event  The key event.
         *
        **/
        public void keyTyped(KeyEvent event)
        {
        }

        /**
         * The value is changed.
         * @param event  The list selection event.
         *
        **/

        public void valueChanged(ListSelectionEvent event)
        {
            if (event.getSource().equals(list_)&&
                event.getValueIsAdjusting()==false)
            {
                int index = list_.getSelectedIndex();
                if(index != -1)
                {
                    removeButton_.setEnabled(true);
                    textField_.setText((String)list_.getSelectedValue());
                    changeButton_.setEnabled(false);
                    addButton_.setEnabled(false);
                } else 
                {
                    removeButton_.setEnabled(false);
                    changeButton_.setEnabled(false);
                }
            }
        }
    }

