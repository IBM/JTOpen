///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemValueGroup.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;


import com.ibm.as400.access.SystemValue;
import com.ibm.as400.access.SystemValueList;
import com.ibm.as400.access.AS400;

import java.io.ObjectInputStream;
import javax.swing.tree.TreeNode;
import javax.swing.Icon;
import java.util.Enumeration;
import java.util.Vector;


import javax.swing.Icon;

import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeListener;

import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The VSystemValueGroup class defines the representation of a 
 * system value group on an AS/400 for use in various models 
 * and panes in this package.
 * You must explicitly call load() to load the information from
 * the AS/400.
 *
 * <p>Most errors are reported as ErrorEvents rather than
 * throwing exceptions.  Users should listen for ErrorEvents
 * in order to diagnose and recover from error conditions.
 * 
 * <p>VSystemValueGroup objects generate the following events:
 * <ul>
 *     <li>ErrorEvent
 *     <li>PropertyChangeEvent
 *     <li>VObjectEvent
 *     <li>WorkingEvent
 * </ul>
**/

class VSystemValueGroup implements VNode
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private VSystemValueList systemValueList_;
    private int group_;
    private String groupName_;
    private String groupDescription_;

    private VNode parent_ = null;
    private boolean loaded_ = false;

    private Vector systemValues_;

    private VObject[] detailsChildren_;    
    private VPropertiesPane propertiesPane_;

   
    // Event support.
    private ErrorEventSupport errorEventSupport_;
    private VObjectEventSupport objectEventSupport_;
    private WorkingEventSupport workingEventSupport_;
    // This one is for refreshing all other date and time values
    // if one of them should change.
    private VObjectListener_ dateTimeListener_;

    // Static data.
    static public final String VALUE_PROPERTY = "Value";

    private static TableColumnModel detailsColumnModel_;    

    // MRI
    private static String nameColumnHeader_;
    private static String descriptionColumnHeader_;
    private static String valueColumnHeader_;
    private static String description_;
    private static Icon icon16_;
    private static Icon icon32_;

    /**
     * Static initializer
     **/
    static
    {
        nameColumnHeader_ = (String)ResourceLoader.getText("COLUMN_NAME");
        valueColumnHeader_ = (String)ResourceLoader.getText("COLUMN_VALUE");
        descriptionColumnHeader_ = (String)ResourceLoader.getText("COLUMN_DESCRIPTION");
        description_ = (String)ResourceLoader.getText("COLUMN_GROUP");

        icon16_= ResourceLoader.getIcon("VSystemValueGroup16.gif", description_); 
        icon32_= ResourceLoader.getIcon("VSystemValueGroup32.gif", description_); 

        detailsColumnModel_= new DefaultTableColumnModel(); 
        int i = 0; 

        // These are the columns to display for the children.
        // The children are VSystemValues.

        // Name column.
        VTableColumn vTable1= new VTableColumn(i++, NAME_PROPERTY); 
        vTable1.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
        vTable1.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
        vTable1.setHeaderValue(nameColumnHeader_); 
        vTable1.setPreferredCharWidth(10); 
        detailsColumnModel_.addColumn(((TableColumn)vTable1));     

        // Value column.
        VTableColumn vTable2= new VTableColumn(i++, VALUE_PROPERTY); 
        vTable2.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
        vTable2.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
        vTable2.setHeaderValue(valueColumnHeader_); 
        vTable2.setPreferredCharWidth(15); 
        detailsColumnModel_.addColumn(((TableColumn)vTable2));         

        // Description column
        VTableColumn vTable3= new VTableColumn(i++, DESCRIPTION_PROPERTY); 
        vTable3.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
        vTable3.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
        vTable3.setHeaderValue(descriptionColumnHeader_); 
        vTable3.setPreferredCharWidth(40); 
        detailsColumnModel_.addColumn(((TableColumn)vTable3));                 
    }


    /**
     * Constructs a VSystemValueGroup object.
     * @param systemValueList  The SystemValueList object.
     * @param group            The group type. 
     **/    
    public VSystemValueGroup(VSystemValueList systemValueList, int group)
    {    
        systemValueList_ = systemValueList;
        group_ = group;
        groupName_ = SystemValueList.getGroupName(group_);
        groupDescription_ = SystemValueList.getGroupDescription(group_);
        parent_ = systemValueList;

        errorEventSupport_= new ErrorEventSupport(this);
        objectEventSupport_= new VObjectEventSupport(this); 
        workingEventSupport_= new WorkingEventSupport(this); 
        dateTimeListener_ = new VObjectListener_();

        propertiesPane_= new VSystemValueGroupPropertiesPane(this);
        propertiesPane_.addErrorListener(errorEventSupport_); 
        propertiesPane_.addVObjectListener(objectEventSupport_); 
        propertiesPane_.addWorkingListener(workingEventSupport_); 
        propertiesPane_.addVObjectListener(dateTimeListener_);

        systemValues_ = new Vector();
    
        detailsChildren_= new VSystemValue[0];

        loaded_ = false;
    }

    /**
     * Adds the specified error listener
     * to receive error event from this
     * component.
     * @param listener      The error listener.
    **/    
    public void addErrorListener(ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener); 
    }
    
    /**
     * Adds the specified VObject listener
     * to receive VObject event from this
     * component.
     * @param listener      The VObject listener.
   **/
    public void addVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener(listener); 
    }

    /**
     * Adds the specified working listener
     * to receive working event from this
     * component.
     * @param listener      The working listener.
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
        return (Enumeration)(new VEnumeration((VNode)this));
    }    

    /**
     * Returns the list of actions that can be performed.
     *      
     * @return Always null. There are no actions.
     * 
     **/
    public VAction[] getActions()
    {
        return null; 
    }
    
    
    /**
     * Indicates if the node allows children.
     * 
     * @return  Always false.
     **/    
    public boolean getAllowsChildren()
    {
        return false;
    }

    /**
     * Returns the child node at the specified index.
     * 
     * @param  index   The index.
     * @return         Always null.
     **/    
    public TreeNode getChildAt(int index)
    {        
        return null;
    }
    
    /**
     * Returns the number of children.
     * 
     * @return  The number of the children.
     **/    
    public int getChildCount()
    {
        return 0;
    }

    /**
     * Returns the description.
     * @return The description.
    **/
    public String getDescription()
    {
      return groupDescription_;
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
        if (index < 0 || index >= detailsChildren_.length)
          return null;
        loadChildren();
        return detailsChildren_[index];
    }

    /**
     * Returns the number of children for the details.
     * 
     *  @return  The number of children for the details.
     **/
    public int getDetailsChildCount()
    {
        loadChildren();
        return detailsChildren_.length;
    }
    
    /**
     *  Returns the table column model to use in the details
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
    public int getDetailsIndex(VObject vObject)
    {
        loadChildren();
        for (int i=0; i<detailsChildren_.length; i++)
        {
          if (detailsChildren_[i] == vObject)
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
     *                 value is given, then return a default.
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
     * @param  treeNode   The child.
     * @return         The index of the specified child.
     **/    
    public int getIndex(TreeNode treeNode)
    {
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
        return propertiesPane_;    
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
    public Object getPropertyValue(Object object)
    {    
      if (object == NAME_PROPERTY)
      {
        return groupName_;
      }
      if (object == DESCRIPTION_PROPERTY)
      {
        return groupDescription_;
      }
      return null;
    }

    /**
     * Returns the text. This is the system value group name
     * 
     * @return The text which is the system value group name.
     **/    
    public String getText()
    {
        return groupName_;
    }
    
    /**
     * Indicates if the node is a leaf.
     * 
     * @return  Always true.
     **/    
    public boolean isLeaf()
    {
        return true;
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
        
        for (int i=0; i<detailsChildren_.length; i++)
        {
          detailsChildren_[i].removeErrorListener(errorEventSupport_); 
          detailsChildren_[i].removeVObjectListener(objectEventSupport_); 
          detailsChildren_[i].removeWorkingListener(workingEventSupport_);
          detailsChildren_[i].removeVObjectListener(dateTimeListener_);
        }
        try
        {
          systemValues_ = systemValueList_.getSystemValueList().getGroup(group_);
          detailsChildren_ = new VSystemValue[systemValues_.size()];
        }
        catch(Exception e)
        {
          detailsChildren_= new VSystemValue[0]; 
          errorEventSupport_.fireError(e);
        }

        for (int i = 0; i < detailsChildren_.length; i++)
        {                    
          detailsChildren_[i] = new VSystemValue((SystemValue)systemValues_.elementAt(i));
          detailsChildren_[i].addErrorListener(errorEventSupport_); 
          detailsChildren_[i].addVObjectListener(objectEventSupport_); 
          detailsChildren_[i].addWorkingListener(workingEventSupport_);
          detailsChildren_[i].addVObjectListener(dateTimeListener_);
          detailsChildren_[i].load();
        }
        workingEventSupport_.fireStopWorking();
        loaded_ = true;
    }
    
    /**
    Loads the children if we haven't loaded them already.
    **/
    private void loadChildren()
    {
      if (!loaded_)
        load();
    }
        
    /**
     * Removes an error listener.
     * 
     * @param  listener    The listener.
    **/
    public void removeErrorListener(ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener(listener); 
    }

    /**
     * Removes a VObjectListener.
     * 
     * @param  listener    The listener.
    **/
    public void removeVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener(listener); 
    }

    /**
     * Removes a working listener.
     * 
     * @param  listener    The listener.
    **/
    public void removeWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener(listener); 
    }

    /**
     * Sorts the children for the details. Since sorting is not supported,
       this method does nothing.
     * 
     * @param  propertyIdentifiers The property identifiers.
     * @param  orders              The sorting orders for each property
     *                             identifier. True for ascending order;
     *                             false for descending order.
     **/
    public void sortDetailsChildren(Object[] object, boolean[] b)
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
        return groupName_;
    }

    /**
     * Listens for events and adjusts the children accordingly.
     * Used for refreshing system values in GROUP_DATTIM.
    **/
    private class VObjectListener_ implements VObjectListener
    {

      public void objectChanged(VObjectEvent event)
      {
        VObject object = event.getObject();
        if (object instanceof VSystemValue)
        {
          if (group_ == SystemValueList.GROUP_DATTIM)
          {
//@A2D            load();
            //@A2A:
            // Can't just do a load() on the group because
            // then our listeners will go away, then if a 2nd change
            // is made to a system value, it wouldn't get updated
            // in the pane.
            // What we do is to tell each of the system values in the
            // date/time group to refresh themselves. This has the effect
            // of firing an objectChanged event, so we have to remove
            // ourselves from listening to that event or we'll get stuck
            // in an infinite loop.
            for (int i = 0; i < detailsChildren_.length; i++)
            {                    
              // We don't need to fire an objectChanged event for the
              // object that got changed, because it is the one that fired
              // the objectChanged event to get us here in the first place.
              if (detailsChildren_[i] != object)
              {
                detailsChildren_[i].removeVObjectListener(this);
                ((VSystemValue)detailsChildren_[i]).objectEventSupport_.fireObjectChanged(detailsChildren_[i]);
                detailsChildren_[i].addVObjectListener(this);
              }
            }
          }
        }
      }

      public void objectCreated(VObjectEvent event)
      {
      }

      public void objectDeleted(VObjectEvent event)
      {
      }
    }
}

