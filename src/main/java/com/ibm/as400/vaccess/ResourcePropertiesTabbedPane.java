///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourcePropertiesTabbedPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import com.ibm.as400.resource.ChangeableResource;
import com.ibm.as400.resource.Presentation;
import com.ibm.as400.resource.Resource;
import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.ResourceMetaData;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;


class ResourcePropertiesTabbedPane
extends JComponent
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private Hashtable           changes_            = new Hashtable();
    private ResourceMetaData[]  attributeMetaData_;
    private Presentation        presentation_;
    private ResourceProperties  properties_;
    private Resource            resource_;
    
    private ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);
    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);



    public ResourcePropertiesTabbedPane(Resource resource, 
                                        ResourceProperties properties)
    {
        super();

        resource_               = resource;
        properties_             = (properties != null) ? properties : new ResourceProperties();
        attributeMetaData_      = resource.getAttributeMetaData();
        presentation_           = resource.getPresentation();

        // Set up the tabbed pane.
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Set up the tabs.
        int tabCount = properties_.getTabCount();
        for(int i = 0; i < tabCount; ++i)
            tabbedPane.addTab(properties_.getLabel(i), createTabComponent(i));        

        // Arrange everything on this component.
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

    
/**
Applies any changes made by the user.
**/
    public void applyChanges()
    {
        synchronized(changes_) {        

            try {
                // Make the changes.
                if (resource_ instanceof ChangeableResource) {
                    Enumeration attributeIDs = changes_.keys();
                    while(attributeIDs.hasMoreElements()) {
                        Object attributeID = attributeIDs.nextElement();
                        Object value = changes_.get(attributeID);
                        if ((resource_.getAttributeMetaData(attributeID).getType() == Integer.class)    // @A1A
                            && (value  instanceof String))                                              // @A1A
                            value = new Integer((String)value);                                         // @A1A
                        ((ChangeableResource)resource_).setAttributeValue(attributeID, value);
                    }
                    ((ChangeableResource)resource_).commitAttributeChanges();
                }
            }
            catch(Exception e) {                                                // @A1C
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Error applying changes", e);
                errorEventSupport_.fireError(e);
            }

            // Clear out the list of changes.
            changes_.clear();
        }
    }



/**
Creates a component for a single attribute.

@param amd      The attribute meta data.
@return         The component.

@exception ResourceException If an error occurs.
**/
    private Component createAttributeComponent(ResourceMetaData attributeMetaData)
    throws ResourceException
    {
        // Get the initial value.
        Object attributeID = attributeMetaData.getID();
        Object initialValue = resource_.getAttributeValue(attributeID);

        // Determine if the component should be editable.
        boolean editable = properties_.isEditable() && !attributeMetaData.isReadOnly();

        // Create the component.  Decide the type of component based
        // on the type of value, etc.
        Component component = null;        
        Class type = attributeMetaData.getType();
        Object[] possibleValues = attributeMetaData.getPossibleValues();

        // If the type is Boolean, use a JCheckBox.
        if (type == Boolean.class) {
            JCheckBox checkBox = new JCheckBox("", ((Boolean)initialValue).booleanValue());
            checkBox.setEnabled(editable); 
            checkBox.addActionListener(new ChangeListener_(attributeID));
            component = checkBox;
        }

        // If the type is an array, use a list.
        else if (type.isArray()) {
            Object[] initialValues = (Object[])initialValue;
            JList list = new JList(initialValues);
            list.setEnabled(false); // For now!
            list.setVisibleRowCount(Math.min(5, initialValues.length));
            component = list;
        }

        // If there are any possible values AND the value is
        // editable, use a JComboBox.  If its not editable,
        // use a JLabel.
        else if (possibleValues != null) {
            if (possibleValues.length > 0) {
                if (editable) {
                    JComboBox comboBox = new JComboBox();
                    for(int i = 0; i < possibleValues.length; ++i)
                        comboBox.addItem(possibleValues[i]);
                    comboBox.setRenderer(new JComboBoxRenderer(attributeMetaData));
                    comboBox.setEditor(new JComboBoxEditor(attributeMetaData));         // @A1A
                    comboBox.setEditable(!attributeMetaData.isValueLimited());
                    comboBox.setSelectedItem(initialValue);                             // @A1C
                    ChangeListener_ changeListener =  new ChangeListener_(attributeID);
                    comboBox.addItemListener(changeListener);
                    comboBox.getEditor().getEditorComponent().addKeyListener(changeListener);
                    component = comboBox;
                }
                else
                    component = new JLabel(attributeMetaData.getPossibleValuePresentation(initialValue).getFullName());
            }            
        }

        // Anything else, use a JTextField (editable), JTextArea (non-editable, multiline),
        // or JLabel (non-editable, single line).
        if (component == null) {
            if (editable) {                
                JTextField textField = new JTextField(initialValue.toString());
                int initialValueLength = initialValue.toString().length();
                textField.setColumns(Math.max(10, Math.min(30, initialValueLength)));
                textField.addKeyListener(new ChangeListener_(attributeID));
                component = textField;
            }
            else {
                String text = initialValue.toString();
                if (text.length() > 30) {
                    component = new JTextArea(VUtilities.formatHelp2(text, 40));
                    ((JTextArea)component).setEditable(false);
                    component.setBackground(null); // Set to gray.
                }
                else
                    component = new JLabel(initialValue.toString());
            }
        }

        return component;
    }
                                                                            


/**
Creates a Box for presenting a list of attributes.

@param attributeIDs         The attribute IDs.
@param firstIndex           The index (within attributeIDs) to start with.
@param numberOfAttributes   The number of attributes to include.
**/
    private Component createAttributeBox(Object[] attributeIDs, int firstIndex, int numberOfAttributes)
    {
        // Initialize the layouts.
        JPanel attributePanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        attributePanel.setLayout(layout);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;                          // @A1C
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);

        // Add the components.
        int row = 0;
        for(int i = firstIndex; i < numberOfAttributes; ++i) {
                
            // Find the meta data for the attribute ID.
            ResourceMetaData attributeMetaData = resource_.getAttributeMetaData(attributeIDs[i]);

            // If the meta data was found, then do the right thing.
            if (attributeMetaData != null) {
                Presentation presentation2 = attributeMetaData.getPresentation();
                String componentLabel = null;
                if (presentation2 != null)
                    componentLabel = presentation2.getFullName();
                else
                    componentLabel = attributeMetaData.getID().toString();

                // Make the left component.
                Component leftComponent = new JLabel(componentLabel + ':');
                attributePanel.add(leftComponent);
                constraints.gridx = 0;
                constraints.gridy = row;
                layout.setConstraints(leftComponent, constraints);

                // Make the right component.
                try {
                    Component rightComponent = createAttributeComponent(attributeMetaData);
                    attributePanel.add(rightComponent);
                    constraints.gridx = 1;
                    constraints.gridy = row;
                    layout.setConstraints(rightComponent, constraints);
                }
                catch(ResourceException e) {
                    if (Trace.isTraceOn())
                        Trace.log(Trace.ERROR, "Error creating attribute component for:" + attributeIDs[i], e);
                    errorEventSupport_.fireError(e);
                }

            }

            // If it was not found, then add a placeholder.
            else {
                Component leftComponent = new JLabel(attributeIDs[i].toString() + ':');
                attributePanel.add(leftComponent);
                constraints.gridx = 0;
                constraints.gridy = row;
                layout.setConstraints(leftComponent, constraints);
            }

            ++row;
        }
        
        // Align so that extra space appears at the bottom.
        JPanel spacer = new JPanel();
        attributePanel.add(spacer);
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weighty = 1;
        layout.setConstraints(spacer, constraints);

        Box attributeBox = new Box(BoxLayout.Y_AXIS);
        attributeBox.add(attributePanel);
        attributeBox.add(Box.createVerticalGlue());
        return attributeBox;
    }



/**
Creates a component for a specific tab.

@param tab  The tab.
@return     The component.
**/
    private Component createTabComponent(int tab)
    {
        // Initialize the overall box.  We need to stick it in
        // a JPanel just so we can add a border.
        Box overallBox = new Box(BoxLayout.Y_AXIS);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add("Center", overallBox);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add the default components if this is the first tab.
        if (tab == 0) {

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
        }        

        // Add any other components.
        Object[] attributeIDs = properties_.getProperties(tab);
        overallBox.add(createAttributeBox(attributeIDs, 0, attributeIDs.length));
        
        // Make excess space appear at the bottom.
        overallBox.add(Box.createVerticalGlue());

        return panel;
    }



/**
The ChangeListener class listens for changes to any of the editable components
in the properties pane.  It enables the Apply button and records any changes.
**/
    private class ChangeListener_ extends KeyAdapter implements ActionListener, ItemListener
    {
        private Object attributeID_;

        public ChangeListener_(Object attributeID)
        {
            attributeID_ = attributeID;
        }

        public void actionPerformed(ActionEvent event)
        {
            registerChange(new Boolean(((JCheckBox)event.getSource()).isSelected()));
        }

        public void itemStateChanged(ItemEvent event)
        {
            registerChange(event.getItem());
        }

        public void keyReleased(KeyEvent event)                                             // @A1C
        {
            registerChange(((JTextField)event.getSource()).getText());
        }

        private void registerChange(Object value)
        {
            synchronized(changes_) {
                changes_.put(attributeID_, value);
            }
            changeEventSupport_.fireStateChanged();
        }
    }



// @A1A
/**
The JComboBoxEditor class renders possible values by using their
presentation full names.
**/
    private class JComboBoxEditor implements ComboBoxEditor
    {
        private ResourceMetaData metaData_;
        private JTextField editorComponent_ = new JTextField();

        public JComboBoxEditor(ResourceMetaData metaData)
        {
            metaData_ = metaData;
        }

        public void addActionListener(ActionListener listener)
        {
            editorComponent_.addActionListener(listener);
        }

        public Component getEditorComponent()  
        {
            return editorComponent_;
        }

        public Object getItem()
        {
            Object item = editorComponent_.getText();
            Object[] possibleValues = metaData_.getPossibleValues();
            if (possibleValues != null) {
                for(int i = 0; i < possibleValues.length; ++i) {
                    if (metaData_.getPossibleValuePresentation(possibleValues[i]).getFullName().equals(item))
                        item = possibleValues[i];
                }
            }
            return item;
        }

        public void removeActionListener(ActionListener listener)
        {
            editorComponent_.removeActionListener(listener);
        }

        public void selectAll()
        {
            editorComponent_.selectAll();
        }

        public void setItem(Object item)
        {
            Object fullName = item;
            if (item != null) {
                Presentation presentation = metaData_.getPossibleValuePresentation(item);
                if (presentation != null)
                    fullName = presentation.getFullName();
            }
            editorComponent_.setText((fullName == null ? "" : fullName.toString()));
        }
    }
        


/**
The JComboBoxRenderer class renders possible values by using their
presentation full names.
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
            if (value != null)
                fullName = metaData_.getPossibleValuePresentation(value).getFullName();
            return super.getListCellRendererComponent(list, 
                                                      fullName, 
                                                      index, 
                                                      isSelected, 
                                                      cellHasFocus);
        }
    }
        


        /**
     * Removes a change listener.
     *
     * @param  listener    The listener.
    **/
    public void removeChangeListener (ChangeListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      changeEventSupport_.removeChangeListener (listener);
    }

    /**
     * Removes a listener to be notified when an error occurs.
     *
     * @param  listener    The listener.
    **/
    public void removeErrorListener (ErrorListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      errorEventSupport_.removeErrorListener (listener);
    }



}

            
