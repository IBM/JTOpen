///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemValueList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.SystemValueList;
import com.ibm.as400.access.SystemValue;

import javax.swing.Icon;

import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeListener;

import java.beans.PropertyVetoException;


import javax.swing.table.TableColumnModel;
import java.io.ObjectInputStream;
import javax.swing.tree.TreeNode;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.util.Enumeration;


/**
 * The VSystemValueList class defines the representation of a 
 * system value list in an AS/400 for use in various models 
 * and panes in this package.
 * You must explicitly call load() to load the information from
 * the AS/400.
 * 
 * <p>Most errors are reported as ErrorEvents rather than
 * throwing exceptions.  Users should listen for ErrorEvents
 * in order to diagnose and recover from error conditions.
 * 
 * <p>VSystemValueList objects generate the following events:
 * <ul>
 *     <li>ErrorEvent
 *     <li>PropertyChangeEvent
 *     <li>VObjectEvent
 *     <li>WorkingEvent
 * </ul>
**/
public class VSystemValueList implements VNode, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    // Properties.
    private SystemValueList systemValueList_;
    private VNode parent_;    

    // Private data.
    private transient VNode[] children_; // These are VSystemValueGroups

    // Event support.
    private transient ErrorEventSupport errorEventSupport_;
    private transient PropertyChangeSupport propertyChangeSupport_;
    private transient VetoableChangeSupport vetoableChangeSupport_;
    private transient VObjectEventSupport objectEventSupport_;
    private transient WorkingEventSupport workingEventSupport_;

    // Static data.
    private static String description_;
    private static Icon icon16_;
    private static Icon icon32_;
    private static TableColumnModel detailsColumnModel_;
    private static String nameColumnHeader_;
    private static String descriptionColumnHeader_;

    // MRI
    final private static String groupName_;
    final private static String groupDescription_;

    final private static int groupCount_ = SystemValueList.getGroupCount();
        
    static
    {
        description_ = (String)ResourceLoader.getText("SYSTEM_VALUE_LIST_DESCRIPTION");
        groupName_ = (String)ResourceLoader.getText("COLUMN_GROUP");
        groupDescription_= (String)ResourceLoader.getText("COLUMN_DESCRIPTION");
        icon16_= ResourceLoader.getIcon("VSystemValueList16.gif", description_); 
        icon32_= ResourceLoader.getIcon("VSystemValueList32.gif", description_); 

        nameColumnHeader_ = groupName_;
        descriptionColumnHeader_ = groupDescription_;
        
        detailsColumnModel_= new DefaultTableColumnModel(); 

        int i= 0; 

        // These are the columns to display for the children.
        // The children are VSystemValueGroups.

        // Name Column
        VTableColumn vTable1= new VTableColumn(i++, NAME_PROPERTY); 
        vTable1.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
        vTable1.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
        vTable1.setHeaderValue(nameColumnHeader_); 
        vTable1.setPreferredCharWidth(10); 
        detailsColumnModel_.addColumn(((TableColumn)vTable1));     

        // Description Column
        VTableColumn vTable2= new VTableColumn(i++, DESCRIPTION_PROPERTY); 
        vTable2.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
        vTable2.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
        vTable2.setHeaderValue(descriptionColumnHeader_); 
        vTable2.setPreferredCharWidth(40); 
        detailsColumnModel_.addColumn(((TableColumn)vTable2)); 
    }
    
    /**
     * Constructs a VSystemValueList object.
    **/
     public VSystemValueList()
     {
        systemValueList_ = new SystemValueList(); 
        initializeTransient();             
     }

    /**
     * Constructs a VSystemValueList object.
     * @param system  The AS/400 system.
    **/    
    public VSystemValueList(AS400 system)
    {
      systemValueList_ = new SystemValueList(system); 
      initializeTransient();             
    }

    /**
     * Constructs a VSystemValueList object.
     *  @param parentNode    The parent node.
     *  @param system         The AS/400 system.
    **/
    public VSystemValueList(VNode parentNode, AS400 system)
    {
      if (parentNode == null)
        throw new NullPointerException("parent");

      systemValueList_ = new SystemValueList(system);
      parent_ = parentNode;
      initializeTransient();
    }
    
    /**
     * Adds a listener to be notified when an error occurs.
     * @param listener      The listener.
    **/    
    public void addErrorListener(ErrorListener listener)
    {
      errorEventSupport_.addErrorListener(listener);
    }

    /**
    * Adds a listener to be notified when the value of any bound
    * property changes.
    * @param listener      The listener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
      propertyChangeSupport_.addPropertyChangeListener(listener);
    }

    /**
     * Adds a listener to be notified when the value of any constrained
     * property changes.
     * @param listener      The listener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
      vetoableChangeSupport_.addVetoableChangeListener(listener);
    }

    /**
     * Adds a listener to be notified when a VObject is changed,
     * created, or deleted.
     * @param listener      The listener.
    **/
    public void addVObjectListener(VObjectListener listener)
    {
      objectEventSupport_.addVObjectListener(listener);
    }

    /**
     * Adds a listener to be notified when work starts and stops
     * on potentially long-running operations.
     * @param listener      The listener.
    **/
    public void addWorkingListener(WorkingListener listener)
    {
      workingEventSupport_.addWorkingListener(listener);
    }

    /**
     * Returns the children of the node.
     * 
     * @return         The children.
    **/
    public Enumeration children()
    {
        return (Enumeration)(new VEnumeration(this)); 
    }    

    /**
     * Returns the list of actions that can be performed.
     *      
     * @return Always null.  There are no actions.
     * 
    **/
    public VAction[] getActions()
    {
        return null; 
    }
    
    /**
     * Indiciates if the node allows children.
     * 
     * @return  Always true.
    **/    
    public boolean getAllowsChildren()
    {
        return true; 
    }


    /**
     * Returns the child node at the specified index.
     * 
     * @param  index   The index.
     * @return The child node at the specified index.
    **/    
    public TreeNode getChildAt(int index)
    {
        if (index < 0 || index >= children_.length)
        {
            return null; 
        }
        return (TreeNode)children_[index]; 
    }
        
    /**
     * Returns the number of the children.
     * 
     * @return  The number of the children.
    **/    
    public int getChildCount()
    {
        return children_.length; 
    }


    /**
     * Returns the default action.
     * 
     * @return Always null.  There is no default action.
    **/   
    public VAction getDefaultAction()
    {
        return null; 
    }
    
    /**
      * Returns the child for the details at the specified index.
      *    
      * @param  index   The index.
      * @return         The child, or null if the index is not
      *                  valid.
    **/
    public VObject getDetailsChildAt(int index)
    {
        if (index < 0 || index >= children_.length)
        {
          return null; 
        }
        return children_[index]; 
    }
    
    /**
     * Returns the number of children for the details.
     * 
     * @return  The number of children for the details.
    **/
    public int getDetailsChildCount()
    {
        return children_.length; 
    }
    
    /**
     *  Returns the table column model to be used in the details
     *  when representing the children.  This column model
     *  describes the details values for the children.
     * 
     *  @return The details column model.
    **/
    public TableColumnModel getDetailsColumnModel()
    {
        return detailsColumnModel_; 
    }
        
    /**
     *  Returns the index of the specified child for the details.
     * 
     *  @param  detailsChild   The details child.
     *  @return                The index, or -1 if the child is not found
     *                          in the details.
    **/
    public int getDetailsIndex(VObject detailsChild)
    {
        for (int i=0; i < groupCount_; i++)
        {
            if (children_[i] == detailsChild)
            {
                return i; 
            }
        }
        return -1;
    }
        
        
    /**
     * Returns the icon.
     * 
     * @param  size    The icon size, either 16 or 32.  If any other
     *                 value is given, then returns a default.
     * @param  open    This parameter has no effect.
     * @return         The icon.
    **/
    public Icon getIcon(int size, boolean open)
    {
        if (size != 32)
        {
          return icon16_; 
        }
        return icon32_; 
    }
    
    /**
     * Returns the index of the specified child.
     * 
     * @param  child   The child.
     * @return         The index of the specified child.
    **/    
    public int getIndex(TreeNode child)
    {
        for (int index=0; index < groupCount_; index++)
        {
            if (children_[index] == child)
            {
                return index; 
            }
        }
        return -1;
    }

	                                        
    /**
     * Returns the parent node.
     * 
     * @return The parent node, or null if there is no parent.
    **/
    public TreeNode getParent()
    {
        return parent_; 
    }

    
    /**
     * Returns the properties pane.
     * 
     * @return The properties pane.
    **/    
    public VPropertiesPane getPropertiesPane()    
    {
        return null;
    }
    
    /**
     * Returns a property value.
     * 
     * @param      propertyIdentifier  The property identifier.  
     *             The choices are
     *                  <ul>
     *                     <li>NAME_PROPERTY
     *                     <li>DESCRIPTION_PROPERTY
     *                  </ul>
     * @return     The property value, or null if the
     *             property identifier is not recognized.
    **/    
    public Object getPropertyValue(Object propertyIdentifier)
    {    
      if (propertyIdentifier == NAME_PROPERTY)
      {
        return this;
      }
      if (propertyIdentifier == DESCRIPTION_PROPERTY)
      {
        return description_;
      }
      return null;
    }
    
    /**
     * Returns the AS/400 system in which the system 
     * values resides.
     * 
     * @return The AS/400 system in which the system
     * values resides.
     * 
     * @see com.ibm.as400.access.SystemValueList
    **/
    public AS400 getSystem()
    {
        return systemValueList_.getSystem();
    }

    /**
     * Returns the system value list.
     * @return The SystemValueList object.
     **/
     public SystemValueList getSystemValueList()
     {
        return systemValueList_;
     }
     
    /**
     * Returns the text representation of the system value list.
     * 
     * @return The text representation of the system value list.
    **/    
    public String getText()
    {    
        return description_; 
    }
    
    /**
     * Initializes the transient data.
    **/    
    private void initializeTransient()
    {
        errorEventSupport_= new ErrorEventSupport(this); 
        objectEventSupport_= new VObjectEventSupport(this); 
        propertyChangeSupport_= new PropertyChangeSupport(this); 
        vetoableChangeSupport_= new VetoableChangeSupport(this); 
        workingEventSupport_= new WorkingEventSupport(this); 
        
        children_ = new VSystemValueGroup[groupCount_];
        for (int i=0; i<groupCount_; ++i)
        {
          children_[i] = new VSystemValueGroup(this, i);
          children_[i].addErrorListener(errorEventSupport_); 
          children_[i].addVObjectListener(objectEventSupport_); 
          children_[i].addWorkingListener(workingEventSupport_);
        }
    }
    
    /**
     * Indicates if the node is a leaf.
     * 
     * @return true if this object is a leaf; false otherwise.
    **/    
    public boolean isLeaf()
    {
        return false; 
    }
    
    /**
     * Indicates if the details children are sortable.
     * 
     * @return Always false.
    **/
    public boolean isSortable()
    {
        return false;
    }
    
    /**
     * Loads information about the object from the AS/400.
    **/    
    public void load()
    {
      workingEventSupport_.fireStartWorking();
      try
      {
        // We make a connection here so that a signon dialog does
        // not appear when we are loading a VSystemValueGroup. If
        // that should happen, it will hang.
        systemValueList_.getSystem().connectService(AS400.COMMAND);
      }
      catch(Exception e)
      {
        errorEventSupport_.fireError(e);
      }
      workingEventSupport_.fireStopWorking();
    }

    /**
     * Restores the state of the object from an input stream.
     * This is used when deserializing an object.
     * 
     * @param in   The input stream.
    **/
    private void readObject(ObjectInputStream in) 
            throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject(); 
        initializeTransient(); 
    }
    
    /**
     * Removes an error listener.
     * @param  listener    The listener.
    **/
    public void removeErrorListener(ErrorListener listener)
    {
      if (listener == null)
        throw new NullPointerException("listener");

      errorEventSupport_.removeErrorListener(listener);
    }
    
    /**
     * Removes a property change listener.
     * @param  listener  The listener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
      if (listener == null)
        throw new NullPointerException("listener");

        propertyChangeSupport_.removePropertyChangeListener(listener); 
    }
    
    /**
     * Removes a vetoable change listener.
     * @param  listener  The listener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener )
    {
      if (listener == null)
        throw new NullPointerException("listener");

        vetoableChangeSupport_.removeVetoableChangeListener(listener); 
    }

    /**
     * Removes a VObjectListener.
     * @param  listener    The listener.
    **/
    public void removeVObjectListener(VObjectListener listener)
    {
      if (listener == null)
        throw new NullPointerException("listener");

        objectEventSupport_.removeVObjectListener(listener); 
    }

    /**
     * Removes a working listener.
     * @param  listener    The listener.
    **/
    public void removeWorkingListener(WorkingListener listener)
    {
      if (listener == null)
        throw new NullPointerException("listener");

        workingEventSupport_.removeWorkingListener(listener); 
    }

    /**
     * Sets the AS400 system from which the system values will be retrieved.
     *
     * @param   system The AS/400 system object.
     * @exception PropertyVetoException If the change is vetoed.
     * @see     #getSystem
    **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (system == null)
            throw new NullPointerException ("system");

        AS400 oldValue = systemValueList_.getSystem();
        AS400 newValue = system;
        vetoableChangeSupport_.fireVetoableChange ("system", oldValue, newValue);

        if (oldValue != newValue)
            systemValueList_.setSystem(newValue);

        propertyChangeSupport_.firePropertyChange ("system", oldValue, newValue);
    }
    	

    /**
     * Sorts the children for the details. Since sorting is not supported,
       this method does nothing.
     * 
     * @param  propertyIdentifiers The property identifiers.
     * @param  orders              The sorting orders for each property
     *                             identifier. true for ascending order;
     *                             false for descending order.
    **/
    public void sortDetailsChildren(Object[] propertyIdentifiers, boolean[] orders)
    {
      // No sorting here!
    }

    /**
     * Returns the string representation of the system 
     * value list.
     * 
     * @return The string representation of the system
     *          value list.
    **/
    public String toString()
    {
      return description_;
    }
}

