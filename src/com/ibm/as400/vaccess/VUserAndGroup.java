///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VUserAndGroup.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.UserList;
import javax.swing.Icon;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
// @A3D import java.beans.PropertyChangeSupport;
// @A3D import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeListener;
import java.io.ObjectInputStream;
import java.util.Enumeration;

/**
 * The VUserAndGroup class represents the users and groups.
 *
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class  VUserAndGroup
    implements VNode, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    //MRI
    // @A1A 
    private static final String allUsersText_               =ResourceLoader.getText("USER_ALL_USERS").trim();
    private static final String usersNotInGroupsText_       =ResourceLoader.getText("USER_USERS_NOT_IN_GROUPS").trim();
    private static final String groupsText_                 =ResourceLoader.getText("USER_GROUPS").trim();
    // @A1A end
    //MRI end
    
    private static Icon             icon16_                     = ResourceLoader.getIcon ("VUserList16.gif", ResourceLoader.getText ("USER_LIST_DESCRIPTION"));
    private static Icon             icon32_                     = ResourceLoader.getIcon ("VUserList32.gif", ResourceLoader.getText ("USER_LIST_DESCRIPTION"));
    private AS400 as400_;
    private VNode parent_;

    private transient VNode[] children_; // These are VUserList objects

    private transient ErrorEventSupport errorEventSupport_;
    private transient PropertyChangeSupport propertyChangeSupport_;
    private transient VetoableChangeSupport vetoableChangeSupport_;
    private transient VObjectEventSupport objectEventSupport_;
    private transient WorkingEventSupport workingEventSupport_;

    private static TableColumnModel detailsColumnModel_;
    private static String nameColumnHeader_ = ResourceLoader.getText ("USER_LIST_NAME");
    private static String descriptionColumnHeader_ = ResourceLoader.getText ("USER_DESCRIPTION_PROMPT");
    private static String description_ = ResourceLoader.getText ("USER_USER_AND_GROUP"); 

    /**
    Static initializer.
    **/
    static 
    {
        detailsColumnModel_= new DefaultTableColumnModel(); 
        int i= 0; 
        
        VTableColumn vTable1= new VTableColumn(i++, NAME_PROPERTY); 
        vTable1.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
        vTable1.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
        vTable1.setHeaderValue(nameColumnHeader_); 
        vTable1.setPreferredCharWidth(10); 
        detailsColumnModel_.addColumn(((TableColumn)vTable1));     
        
        VTableColumn vTable2= new VTableColumn(i++, DESCRIPTION_PROPERTY); 
        vTable2.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
        vTable2.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
        vTable2.setHeaderValue(descriptionColumnHeader_); 
        vTable2.setPreferredCharWidth(70); 
        detailsColumnModel_.addColumn(((TableColumn)vTable2)); 
        
    }
    
    /**
    Constructs a VUserAndGroup object.
    @param as400   The system in which the user information resides.
    **/ 
    public VUserAndGroup(AS400 as400)
    {
        if (as400 == null)
        {
          throw new NullPointerException("system");
        }
         as400_ = as400;
         initializeTransient();             
    }

    /**
    Constructs a VUserAndGroup object.
    @param parent   The parent.
    @param system   The system in which the user information resides.
    **/
    public VUserAndGroup(VNode parent, AS400 system)
    {
      if (parent == null)
      {
        throw new NullPointerException("parent");
      }
      if (system == null)
      {
        throw new NullPointerException("system");
      }
      as400_ = system;
      parent_ = parent;
      initializeTransient();             
    }

    /**
    Adds a listener to be notified when an error occurs.
    @param  listener    The listener.
    **/
    public void addErrorListener(ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener); 
    }

    /**
     Adds a listener to be notified when the value of any
     bound property changes.
     @param  listener  The listener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener(listener); 
    }

    /**
    Adds a listener to be notified when the value of any
    constrained property changes.
    @param  listener  The listener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener(listener); 
    }

    /**
    Adds a listener to be notified when a VObject is changed,
    created, or deleted.
    @param  listener    The listener.
    **/    
    public void addVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener(listener); 
    }

    /**
    Adds a listener to be notified when work starts and stops
    on potentially long-running operations.
    @param  listener    The listener.
    **/
    public void addWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener(listener); 
    }

    /**
    Returns the children of the node.
    @return         The children.
    **/    
    public Enumeration children()
    {
        return (Enumeration)(new VEnumeration(this));
    }    

    /**
    Returns the list of actions that can be performed.
    @return Always null.  There are no actions.
    **/
    public VAction[] getActions()
    {
       return null;
    }

    /**
    Indiciates if the node allows children.
    @return  Always false.
    **/
    public boolean getAllowsChildren()
    {
        return true; 
    }

    /**
    Returns the child node at the specified index.
    @param  index   The index.
    @return         Always null.
    **/    
    public TreeNode getChildAt(int index)
    {
        if  ( (index < 0) || (index >= children_.length))
        {
           return null; 
        }
        return children_[index]; 
    }

    /**
    Returns the number of children.
    @return  Always 0.
    **/
    public int getChildCount()
    {
        return children_.length; 
    }

    /**
    Returns the default action.
    @return Always null.  There is no default action.
    **/    
    public VAction getDefaultAction()
    {    
       return null;
    }

    /**
    Returns the child for the details at the specified index.
    @param  index   The index.
    @return         The child, or null if the index is not
                    valid.
    **/    
    public VObject getDetailsChildAt(int index)
    {
        if  ( (index < 0) || (index >= children_.length))
        {
            return null;
        }
        return children_[index]; 
    }

    /**
    Returns the number of children for the details.
    @return  The number of children for the details.
    **/
    public int getDetailsChildCount()
    {
        return children_.length; 
    }

    
    /**
    Returns the table column model to use in the details
    when representing the children.  This column model
    describes the details values for the children.

    @return The details column model.
    **/
    public TableColumnModel getDetailsColumnModel()
    {
        return detailsColumnModel_; 
    }    

    /**
    Returns the index of the specified child for the details.
    @param  detailsChild   The details child.
    @return                The index, or -1 if the child is not found
                           in the details.
    **/    
    public int getDetailsIndex(VObject detailsChild)
    {
        for (int i = 0; i < children_.length; i++)
        {
            if (children_[i].equals(detailsChild))
            {
                return i; 
            }
        }
        return -1; 
    }

    /**
    Returns the icon.
    @param  size    The icon size, either 16 or 32.  If any other
                    value is given, then return a default.
    @param  open    This parameter has no effect.
    @return         The icon.
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
    Returns the index of the specified child.
    @param  child   The child.
    @return         Always -1.
    **/    
    public int getIndex(TreeNode child)
    {
        for (int index = 0; index < children_.length; index++)
        {
            if (children_[index].equals(child))
            {
                return index; 
            }
        }
        return -1; 
    }

    // @A1A : Gets user list name.
    private String getListName(UserList userList)
    {
        String userInfo = userList.getUserInfo();
        String groupInfo = userList.getGroupInfo();
        // @A3D if (userInfo == null)
        // @A3D {
        // @A3D     Trace.log(Trace.ERROR, "Parameter 'userInfo' of userList is null.");
        // @A3D     throw new NullPointerException("userInfo");
        // @A3D }
        // @A3D if (groupInfo == null)
        // @A3D {
        // @A3D     Trace.log(Trace.ERROR, "Parameter 'groupInfo' of userList is null.");
        // @A3D     throw new NullPointerException("groupInfo");
        // @A3D }

        if (userInfo.toLowerCase().equals("*all"))
        {
            if (groupInfo.toLowerCase().equals("*none"))
                return allUsersText_;
        }
        if (userInfo.toLowerCase().equals("*member"))
        {
            if (groupInfo.toLowerCase().equals("*nogroup"))
                return usersNotInGroupsText_;
        }
        if (userInfo.toLowerCase().equals("*group"))
        {
            if (groupInfo.toLowerCase().equals("*none"))
                return groupsText_;
        }

        Trace.log(Trace.ERROR,"userInfo or groupInfo's value is invalid");
        Trace.log(Trace.INFORMATION,"userInfo : "+userInfo);
        Trace.log(Trace.INFORMATION,"groupInfo : "+groupInfo);
        return "";
    }

    /**
    Returns the parent node.
    @return The parent node, or null if there is no parent.
    **/
    public TreeNode getParent()
    {
       return parent_; 
    }

    /**
    Returns the properties pane.
    @return The properties pane.
    **/    
    public VPropertiesPane getPropertiesPane()    
    {
       return null;
    }

    
    /**
    Returns a property value.
    @param propertyIdentifier  The property identifier.  
                               The choices are NAME_PROPERTY 
                               and DESCRIPTION_PROPERTY.
    @return     The property value, or null if the property 
                identifier is not recognized.
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
    Returns the system in which the user information resides.
    @return The system in which the user information resides.
    **/    
    public AS400 getSystem()
    {
        return as400_;
    }

    /**
    Returns the description text.
    @return The description text.
    **/    
    public String getText()
    {    
        return description_; 
    }

    /**
    Initializes the transient data.
    **/
    private void initializeTransient()
    {        
        errorEventSupport_= new ErrorEventSupport(this); 
        objectEventSupport_= new VObjectEventSupport(this); 
        propertyChangeSupport_= new PropertyChangeSupport(this); 
        vetoableChangeSupport_= new VetoableChangeSupport(this); 
        workingEventSupport_= new WorkingEventSupport(this); 
        
        children_= new VUserList[3];
        UserList[] listchild = new UserList[3];
        listchild[0] = new UserList(as400_, "*ALL", "*NONE");
        listchild[1] = new UserList(as400_, "*GROUP", "*NONE");
        listchild[2] = new UserList(as400_, "*MEMBER", "*NOGROUP");
        for (int i=0; i<3; ++i)
        {
            children_[i] = new VUserList(listchild[i], getListName(listchild[i]));
            //@B0 - Need to set the parent to this, otherwise will get NullPointerExceptions
            // when Swing tries to repaint.
            ((VUserList)children_[i]).parent_ = this; //@B0A
            children_[i].addErrorListener(errorEventSupport_);
            children_[i].addVObjectListener(objectEventSupport_); 
            children_[i].addWorkingListener(workingEventSupport_); 
        }
    }

    /**
    Indicates if the node is a leaf.
    @return  If the node is a leaf.
    **/    
    public boolean isLeaf()
    {
        return false; 
    }

    /**
    Indicates if the details children are sortable.
    @return Always false.
    **/    
    public boolean isSortable()
    {
        return false;
    }

    /**
    Loads information about the object from the system.
    **/
    public void load()
    {
      workingEventSupport_.fireStartWorking();
      try
      {
        // We make a connection here so that a signon dialog does
        // not appear when we are loading a VUserList. If
        // that should happen, it will hang.
        as400_.connectService(AS400.COMMAND);
        for(int i = 0; i < children_.length; ++i)           // @C1A
            children_[i].load();                            // @C1A
      }
      catch(Exception e)
      {
        errorEventSupport_.fireError(e);
      }
      workingEventSupport_.fireStopWorking();
    }

    /**
    Restores the state of the object from an input stream.
    This is used when deserializing an object.
    @param in   The input stream.
    **/
    private void readObject(ObjectInputStream in) 
            throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject(); 
        initializeTransient(); 
    }

    /**
    Removes an error listener.
    @param  listener    The listener.
    **/
    public void removeErrorListener(ErrorListener listener)
    {
        if (listener == null)
        {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
        }

        errorEventSupport_.removeErrorListener(listener); 
    }

    /**
    Removes a property change listener.
    @param  listener  The listener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {   
        if (listener == null)
        {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
        }
        propertyChangeSupport_.removePropertyChangeListener(listener); 
    }

    /**
    Removes a vetoable change listener.
    @param  listener  The listener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
        {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
        }
        vetoableChangeSupport_.removeVetoableChangeListener(listener); 
    }

    /**
    Removes a VObjectListener.
    @param  listener    The listener.
    **/
    public void removeVObjectListener(VObjectListener listener)
    {
        if (listener == null)
        {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
        }

        objectEventSupport_.removeVObjectListener(listener); 
    }

    /**
    Removes a working listener.
    @param  listener    The listener.
    **/
    public void removeWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener(listener); 
    }
    
    /**
    Sorts the children for the details.  Since sorting is not supported,
    this method does nothing.
    @param  propertyIdentifiers The property identifiers.
    @param  orders              The sort orders for each property
                                   identifier. true for ascending order;
                                false for descending order.
    **/
    public void sortDetailsChildren(Object[] propertyIdentifiers, boolean[] orders)
    {
      // No sorting here!
    }

    /**
    Returns the string representation of the description.
    @return The string representation of the description.
    **/
    public String toString()
    {
        return description_;
    }
}

