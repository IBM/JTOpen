///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import com.ibm.as400.resource.Presentation;
import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.ResourceList;
import com.ibm.as400.resource.ResourceMetaData;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;



/**
The ResourceListPropertiesPane class represents a dialog for
updating the properties of a resource list.
**/
class ResourceListPropertiesPane
extends JDialog
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String DLG_APPLY_              = ResourceLoader.getText("DLG_APPLY");
    private static final String DLG_CANCEL_             = ResourceLoader.getText("DLG_CANCEL");
    private static final String DLG_OK_                 = ResourceLoader.getText("DLG_OK");
    private static final String DLG_PROPERTIES_TITLE_   = ResourceLoader.getText("DLG_PROPERTIES_TITLE");
    private static final String RESOURCE_SORT_TAB_      = ResourceLoader.getText("RESOURCE_SORT_TAB");



    // Private data.
    private JButton             applyButton_;
    private JButton             cancelButton_;
    private JButton             okButton_;

    private ResourceListPropertiesTabbedPane    tabbedPane_;

    private ErrorEventSupport   errorEventSupport_;



/**
Constructs a ResourceListPropertiesPane object.

@param resourceList         The resource list.
@param errorEventSupport    The error event support.
**/
    public ResourceListPropertiesPane(ResourceList resourceList, ErrorEventSupport errorEventSupport)
    {
        super();

        errorEventSupport_      = errorEventSupport;

        // Set up the buttons.
        okButton_ = new JButton(DLG_OK_);
        cancelButton_ = new JButton(DLG_CANCEL_);
        applyButton_ = new JButton(DLG_APPLY_);

        applyButton_.setEnabled(false);

        ActionListener actionListener = new ActionListener_();
        okButton_.addActionListener(actionListener);
        cancelButton_.addActionListener(actionListener);
        applyButton_.addActionListener(actionListener);

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton_);
        buttons.add(cancelButton_);
        buttons.add(applyButton_);

        // Set up the tabbed pane.
        tabbedPane_ = new ResourceListPropertiesTabbedPane(resourceList);
        tabbedPane_.addErrorListener(errorEventSupport_);
        tabbedPane_.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    applyButton_.setEnabled(true);
                }
            });
        
        // Arrange everything on this dialog.
        setTitle(ResourceLoader.substitute(DLG_PROPERTIES_TITLE_, resourceList.getPresentation().getName()));
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add("Center", tabbedPane_);
        getContentPane().add("South", buttons);
        pack();
    }



/**
Applies any changes made by the user.
**/
    public void applyChanges()
    {
        tabbedPane_.applyChanges();
        applyButton_.setEnabled(false);
    }



/**
The ActionListener_ class processes clicks of the OK, Cancel, and
Apply buttons.
**/
    private class ActionListener_ implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            Object source = event.getSource();
        
            if (source == okButton_) {
                applyChanges();
                dispose();
            }
        
            else if (source == cancelButton_)
                dispose();
        
            else if (source == applyButton_) {
                applyChanges();
            }
        }
    }



}

            
