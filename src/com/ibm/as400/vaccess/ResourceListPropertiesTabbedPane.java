///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListPropertiesTabbedPane.java
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;



class ResourceListPropertiesTabbedPane
extends JComponent
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String DLG_ADD_                = ResourceLoader.getText("DLG_ADD");
    private static final String DLG_REMOVE_             = ResourceLoader.getText("DLG_REMOVE");

    private static final String RESOURCE_ALL_SORTS_     = ResourceLoader.getText("RESOURCE_ALL_SORTS");
    private static final String RESOURCE_CURRENT_SORTS_ = ResourceLoader.getText("RESOURCE_CURRENT_SORTS");
    private static final String RESOURCE_SELECTION_TAB_ = ResourceLoader.getText("RESOURCE_SELECTION_TAB");
    private static final String RESOURCE_SORT_TAB_      = ResourceLoader.getText("RESOURCE_SORT_TAB");



    // Private data.
    private Hashtable           changes_            = new Hashtable();
    private DefaultListModel    currentSortsModel_;
    private Presentation        presentation_;
    private ResourceList        resourceList_;

    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);



    public ResourceListPropertiesTabbedPane(ResourceList resourceList)
    {
        super();

        resourceList_           = resourceList;
        presentation_           = resourceList.getPresentation();

        // Set up the tabbed pane.
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Set up the tabs.
        tabbedPane.addTab(RESOURCE_SELECTION_TAB_, createSelectionComponent());
        if (resourceList.getSortMetaData().length > 0)
            tabbedPane.addTab(RESOURCE_SORT_TAB_, createSortComponent());

        // Arrange everything on this dialog.
        setLayout(new BorderLayout());
        add("Center", tabbedPane);
    }




    public void addChangeListener (ChangeListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      changeEventSupport_.addChangeListener (listener);
    }

    

    public void addErrorListener (ErrorListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      errorEventSupport_.addErrorListener (listener);
    }



    public void applyChanges()                                  // @A1A
    {                                                           // @A1A
        applyChanges(true);                                     // @A1A
    }                                                           // @A1A

    

    public void applyChanges(boolean refreshContents)           // @A1C
    {
        synchronized(changes_) {     

            try {
                // Make the selection changes.
                Enumeration selectionIDs = changes_.keys();
                while(selectionIDs.hasMoreElements()) {
                    Object selectionID = selectionIDs.nextElement();
                    Object value = changes_.get(selectionID);
                    resourceList_.setSelectionValue(selectionID, value);
                }
    
                // Clear out the list of changes.
                changes_.clear();
    
                // Set the sort value.
                if (currentSortsModel_ != null)
                    resourceList_.setSortValue(currentSortsModel_.toArray());

                // Load the resource list.
                if (refreshContents)                            // @A1A
                    resourceList_.refreshContents();
            }
            catch(Exception e) {
                if(Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Error applying changes", e);
                errorEventSupport_.fireError(e);
            }
        }
    }



/**
Creates the component for a selection.

@param rmd      The selection meta data for the selection.
@return         The component.

@throws ResourceException   If an error occurs.
**/
    private Component createSelectionComponent(ResourceMetaData rmd)
    throws ResourceException
    {
        // Get the initial value.
        Object selectionID = rmd.getID();
        Object initialValue = resourceList_.getSelectionValue(selectionID);

        // Determine if the component should be editable.
        boolean editable = !rmd.isReadOnly();

        // Create the component.  Decide the type of component based
        // on the type of value, etc.
        Component component = null;        
        Class type = rmd.getType();
        Object[] possibleValues = rmd.getPossibleValues();

        // If the type is Boolean, use a JCheckBox.
        if (type == Boolean.class) {
            JCheckBox checkBox = new JCheckBox("", ((Boolean)initialValue).booleanValue());
            checkBox.setEnabled(editable); 
            checkBox.addActionListener(new ChangeListener_(selectionID));
            component = checkBox;
        }

        // If there are any possible values AND the value is
        // editable, use a JComboBox.  If its not editable,
        // use a JLabel.
        else if (possibleValues != null) {
            if (possibleValues.length > 0) {
                if (editable) {
                    JComboBox comboBox = new JComboBox();
                    if (initialValue.getClass().isArray())
                        initialValue = ((Object[])initialValue)[0];
                    boolean initialValueFound = false;
                    for(int i = 0; i < possibleValues.length; ++i) {
                        comboBox.addItem(possibleValues[i]);
                        if (possibleValues[i] == initialValue)
                            initialValueFound = true;
                    }
                    if (!initialValueFound)
                        comboBox.addItem(initialValue);
                    comboBox.setRenderer(new JComboBoxRenderer(rmd));
                    comboBox.setEditor(new JComboBoxEditor(rmd));
                    comboBox.setEditable(!rmd.isValueLimited());
                    ChangeListener_ changeListener =  new ChangeListener_(selectionID, comboBox.getEditor());
                    comboBox.addItemListener(changeListener);
                    comboBox.getEditor().getEditorComponent().addKeyListener(changeListener);
                    comboBox.setSelectedItem(initialValue);
                    component = comboBox;
                }
                else
                    component = new JLabel(rmd.getPossibleValuePresentation(initialValue).getFullName());
            }            
        }

        // Anything else, use a JTextField.
        if (component == null) {
            if (editable) {
                JTextField textField = new JTextField(initialValue.toString());
                textField.addKeyListener(new ChangeListener_(selectionID));
                component = textField;
            }
            else 
                component = new JLabel(initialValue.toString());
        }

        return component;
    }
    


/**
Creates the selection box.  This is the box that contains the main
selection GUI.

@return The selection box.
**/
    private Component createSelectionBox()
    {
        // Initialize the left and right box.
        Box leftBox = new Box(BoxLayout.Y_AXIS);
        Box rightBox = new Box(BoxLayout.Y_AXIS);
        Box selectionBox = new Box(BoxLayout.X_AXIS);
        selectionBox.add(leftBox);
        selectionBox.add(Box.createRigidArea(new Dimension(10, 0)));
        selectionBox.add(rightBox);
        selectionBox.add(Box.createHorizontalGlue());

        // Add the components.
        ResourceMetaData[] rmd = resourceList_.getSelectionMetaData();
        for(int i = 0; i < rmd.length; ++i) {
                
            Presentation presentation2 = rmd[i].getPresentation();
            String componentLabel = null;
            if (presentation2 != null)
                componentLabel = presentation2.getFullName();
            else
                componentLabel = rmd[i].getID().toString();

            // Make the left component.
            Component leftComponent = new JLabel(componentLabel + ':');
            leftBox.add(leftComponent);
            int leftComponentSize = leftComponent.getPreferredSize().height;

            // Make the right component.
            int rightComponentSize = 0;
            try {
                Component rightComponent = createSelectionComponent(rmd[i]);
                rightBox.add(rightComponent);
                rightComponentSize = rightComponent.getPreferredSize().height;
            }
            catch(ResourceException e) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Error creating property component for:" + rmd[i].getID(), e);
                errorEventSupport_.fireError(e);
            }

            // Add space to make the components line up.
            int diff = leftComponentSize - rightComponentSize;
            if (diff > 0)
                rightBox.add(Box.createRigidArea(new Dimension(0, diff)));
            else if (diff < 0)
                leftBox.add(Box.createRigidArea(new Dimension(0, -diff)));

            // Add space between components.
            leftBox.add(Box.createRigidArea(new Dimension(0, 3)));
            rightBox.add(Box.createRigidArea(new Dimension(0, 3)));
        }

        // Make sure any excessive space is at the bottom.
        leftBox.add(Box.createVerticalGlue());
        rightBox.add(Box.createVerticalGlue());
        
        return selectionBox;
    }



/**
Creates the selection component.  This is the component
which makes up the selection tab.

@return The selection component.
**/
    private Component createSelectionComponent()
    {
        // Initialize the overall box.  We need to stick it in
        // a JPanel just so we can add a border.
        Box overallBox = new Box(BoxLayout.Y_AXIS);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add("Center", overallBox);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create the label in a box so it won't always be centered.
        JLabel label = new JLabel(presentation_.getFullName(), 
                               new ImageIcon((Image)presentation_.getValue(Presentation.ICON_COLOR_32x32)), 
                                        SwingConstants.LEFT);
        Box labelBox = new Box(BoxLayout.X_AXIS);
        labelBox.add(label);
        labelBox.add(Box.createHorizontalGlue());
        overallBox.add(labelBox);

        // Add a separator and some space.
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(separator.getMaximumSize().width, separator.getPreferredSize().height));
        overallBox.add(Box.createRigidArea(new Dimension(0, 6)));
        overallBox.add(separator);
        overallBox.add(Box.createRigidArea(new Dimension(0, 6)));

        // Add any other components.
        overallBox.add(createSelectionBox());
        
        // Make excess space appear at the bottom.
        overallBox.add(Box.createVerticalGlue());

        return panel;
    }



/**
Creates the sort component.  This is the component
which makes up the sort tab.

@return The sort component.
**/
    private Component createSortComponent()
    {
        // Initialize the overall box.  We need to stick it in
        // a JPanel just so we can add a border.
        Box overallBox = new Box(BoxLayout.X_AXIS);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add("Center", overallBox);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create the All Sorts panel.
        JPanel allSortsPanel = new JPanel();
        allSortsPanel.setLayout(new BorderLayout());
        allSortsPanel.add("North", new JLabel(RESOURCE_ALL_SORTS_, SwingConstants.LEFT));

        DefaultListModel allSortsModel = new DefaultListModel();
        ResourceMetaData[] smd = resourceList_.getSortMetaData();
        for (int i = 0; i < smd.length; ++i)
            allSortsModel.addElement(smd[i].getID());
        JList allSortsList = new JList(allSortsModel);
        allSortsList.setBorder(new EtchedBorder());
        allSortsList.setAlignmentX(Component.LEFT_ALIGNMENT);
        ListCellRenderer_ listCellRenderer = new ListCellRenderer_();
        allSortsList.setCellRenderer(listCellRenderer);
        allSortsPanel.add("Center", allSortsList);
        overallBox.add(allSortsPanel);
        overallBox.add(Box.createRigidArea(new Dimension(6, 0)));

        // Create the button box.
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        buttonBox.add(Box.createVerticalGlue());
        JButton addButton = new JButton(DLG_ADD_);
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setEnabled(false);
        buttonBox.add(addButton);
        buttonBox.add(Box.createRigidArea(new Dimension(0, 6)));

        JButton removeButton = new JButton(DLG_REMOVE_); 
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.setEnabled(false);
        buttonBox.add(removeButton);
        buttonBox.add(Box.createVerticalGlue());
        overallBox.add(buttonBox);
        overallBox.add(Box.createRigidArea(new Dimension(6, 0)));

        // Create the Current Sorts panel.
        JPanel currentSortsPanel = new JPanel();
        currentSortsPanel.setLayout(new BorderLayout());
        currentSortsPanel.add("North", new JLabel(RESOURCE_CURRENT_SORTS_, SwingConstants.LEFT));

        currentSortsModel_ = new DefaultListModel();
        JList currentSortsList = new JList(currentSortsModel_);
        currentSortsList.setBorder(new EtchedBorder());
        currentSortsList.setAlignmentX(Component.LEFT_ALIGNMENT);
        currentSortsList.setCellRenderer(listCellRenderer);
        currentSortsPanel.add("Center", currentSortsList);
        overallBox.add(currentSortsPanel);

        // Sync the buttons with the JLists.
        allSortsList.addListSelectionListener(new ListSelectionListener_(addButton));
        currentSortsList.addListSelectionListener(new ListSelectionListener_(removeButton));
        addButton.addActionListener(new SortMover_(allSortsList, allSortsModel, currentSortsModel_));
        removeButton.addActionListener(new SortMover_(currentSortsList, currentSortsModel_, allSortsModel));

        // When the buttons are pressed, enable apply.                                 @A1A
        ActionListener applyEnabler = new ActionListener() {                        // @A1A
                public void actionPerformed(ActionEvent event) {                    // @A1A
                    changeEventSupport_.fireStateChanged();                         // @A1A
                }};                                                                 // @A1A
        addButton.addActionListener(applyEnabler);                                  // @A1A
        removeButton.addActionListener(applyEnabler);                               // @A1A

        return panel;
    }



    private class ChangeListener_ extends KeyAdapter implements ActionListener, ItemListener
    {
        private Object selectionID_;
        private ComboBoxEditor editor_;

        public ChangeListener_(Object selectionID)
        {
            selectionID_ = selectionID;
        }

        public ChangeListener_(Object selectionID, ComboBoxEditor editor)
        {
            selectionID_ = selectionID;
            editor_ = editor;
        }

        public void actionPerformed(ActionEvent event)
        {
            registerChange(new Boolean(((JCheckBox)event.getSource()).isSelected()));
        }

        public void itemStateChanged(ItemEvent event)
        {
            registerChange(event.getItem());
        }

        public void keyTyped(KeyEvent event)
        {
            registerChange(editor_.getItem());
        }

        private void registerChange(Object value)
        {
            synchronized(changes_) {
                changes_.put(selectionID_, value);
            }
            changeEventSupport_.fireStateChanged();
        }
    }




/**
The JComboBoxRenderer class renders a possible value for display
in the JComboBox by using its presentation full name.
**/
    private class JComboBoxRenderer extends DefaultListCellRenderer
    {
        private ResourceMetaData metaData_;

        public JComboBoxRenderer(ResourceMetaData metaData)
        {
            metaData_ = metaData;
        }

        public Component getListCellRendererComponent(JList list, 
                                                      Object value, 
                                                      int index, 
                                                      boolean isSelected, 
                                                      boolean cellHasFocus)  
        {
            Object fullName = value;
            if (value != null) {
                Presentation presentation = metaData_.getPossibleValuePresentation(value);
                if (presentation != null)
                    fullName = presentation.getFullName();
            }
            return super.getListCellRendererComponent(list, 
                                                      fullName, 
                                                      index, 
                                                      isSelected, 
                                                      cellHasFocus);
        }
    }
        



/**
The JComboBoxEditor class allows the editing in the JComboBox 
but displays any possible values using its presentation full name.
**/
    private class JComboBoxEditor extends BasicComboBoxEditor
    {
        private ResourceMetaData metaData_;
        private Object actualValue_ = null;
        private String fullName_ = null;

        public JComboBoxEditor(ResourceMetaData metaData)
        {
            metaData_ = metaData;
        }
                               
        public Object getItem() {
            Object value = super.getItem();
            if (fullName_ != null)
                if (value.equals(fullName_))
                    return actualValue_;
            return value;
        }

        public void setItem(Object value)
        {   
            if (value != null) {
                try {
                    Presentation presentation = metaData_.getPossibleValuePresentation(value);
                    if (presentation != null)
                        fullName_ = presentation.getFullName();
                    else
                        fullName_ = value.toString();
                    actualValue_ = value;
                }
                catch(Exception e) {
                    fullName_ = null;
                    actualValue_ = null;
                }
            }
            super.setItem(fullName_);
        }

    }
        



/**
The ListCellRenderer_ class renders a possible value for display
in the JList by using its presentation full name.
**/
    private class ListCellRenderer_ extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(JList list, 
                                                      Object value, 
                                                      int index, 
                                                      boolean isSelected, 
                                                      boolean cellHasFocus)  
        {
            Object fullName = value;
            ResourceMetaData rmd = resourceList_.getSortMetaData(value);
            if (rmd != null)
                fullName = rmd.getPresentation().getFullName();
            return super.getListCellRendererComponent(list, 
                                                      fullName, 
                                                      index, 
                                                      isSelected, 
                                                      cellHasFocus);
        }
    }
        



/**
The ListSelectionListener_ class enables a button when a list selection
has been made.  This is used in the sort GUI so that the appropriate
button is enabled/disabled depending on whether an item in the corresponding
JList has been selected.
**/
    private class ListSelectionListener_ implements ListSelectionListener
    {
        private JButton button_;

        public ListSelectionListener_(JButton button)
        {
            button_ = button;
        }

        public void valueChanged(ListSelectionEvent event) 
        {
            button_.setEnabled(((JList)event.getSource()).getMinSelectionIndex() >= 0);
        }
    }



/**
The SortMover_ class moves items from one JList to another when a 
button is clicked.
**/
    private class SortMover_ implements ActionListener
    {
        private JList               fromList_;
        private DefaultListModel    fromModel_;
        private DefaultListModel    toModel_;

        public SortMover_(JList fromList,
                          DefaultListModel fromModel, 
                          DefaultListModel toModel)
        {
            fromList_   = fromList;
            fromModel_  = fromModel;
            toModel_    = toModel;
        }

        public void actionPerformed(ActionEvent event)
        {
            Object[] selectedValues = fromList_.getSelectedValues();
            for(int i = 0; i < selectedValues.length; ++i) {
                fromModel_.removeElement(selectedValues[i]);
                toModel_.addElement(selectedValues[i]);
            }

            ((JButton)event.getSource()).setEnabled(false);
        }
    }



}

            
