///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VUserList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.table.TableCellRenderer;//@A1A

import javax.swing.table.TableColumn;//@A1A
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.User;
import com.ibm.as400.access.UserList;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.tree.TreeNode;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;



/**
The VUserList class defines the representation of a user list
on an AS/400 for use in various models and panes in this package.
You must explicitly call load() to load the information from
the AS/400.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VUserList objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>
**/
public class VUserList
implements VNode, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static final String genericDescription_         = ResourceLoader.getText ("USER_LIST_DESCRIPTION"); // @A5A
    private  String           description_                  = genericDescription_;                              // @A5C

    // MRI.
    private static Icon             icon16_                     = ResourceLoader.getIcon ("VUserList16.gif", ResourceLoader.getText ("USER_LIST_DESCRIPTION"));
    private static Icon             icon32_                     = ResourceLoader.getIcon ("VUserList32.gif", ResourceLoader.getText ("USER_LIST_DESCRIPTION"));
    private static String           nameColumnHeader_           = ResourceLoader.getText ("USER_USER_NAME");// @A1A
    private static String           descriptionColumnHeader_    =ResourceLoader.getText ("USER_DESCRIPTION_PROMPT");// @A1A

    // @A1A
    private static final String allUsersText_               =ResourceLoader.getText("USER_ALL_USERS").trim();// @A1A
    private static final String allUsersDescription_        =ResourceLoader.getText("USER_ALL_USERS_DES").trim();// @A1A
    private static final String usersNotInGroupsText_       =ResourceLoader.getText("USER_USERS_NOT_IN_GROUPS").trim();// @A1A
    private static final String usersNotInGroupsDescription_=ResourceLoader.getText("USER_USERS_NOT_IN_GROUPS_DES").trim();// @A1A
    private static final String groupsText_                 =ResourceLoader.getText("USER_GROUPS").trim();// @A1A
    private static final String groupsDescription_          =ResourceLoader.getText("USER_GROUPS_DES").trim();// @A1A
    // end


    // Properties.
    private UserList                userList_           = null;
    //@B0 - Made parent_ package scope so that VUserAndGroup can set it.
    VNode                   parent_             = null; //@B0C - made package scope
    private boolean                 isLoaded_             = false; // @A3A


    // Static data.
    private static TableColumnModel detailsColumnModel_     = null;



    // Private data.
    transient private VObject[]         detailsChildren_;
    transient private Enumeration       enum_;
    transient private int               loaded_;
    transient private VPropertiesPane   propertiesPane_;



    // Event support.
    transient private ErrorEventSupport           errorEventSupport_;
    transient private PropertyChangeSupport       propertyChangeSupport_;
    transient private VetoableChangeSupport       vetoableChangeSupport_;
    transient private VObjectEventSupport         objectEventSupport_;
    transient private WorkingEventSupport         workingEventSupport_;



    /**
    Static initializer.
    **/
    //
    // Implementation note:
    //
    // * The column widths are completely arbitrary.
    //
   static
    {
        detailsColumnModel_= null;
        detailsColumnModel_= new DefaultTableColumnModel();
        int i= 0;

        VTableColumn vTable1= new VTableColumn(i++, NAME_PROPERTY);
        vTable1.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer()));
        vTable1.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer()));
        vTable1.setHeaderValue(nameColumnHeader_);
        vTable1.setPreferredCharWidth(15);
        detailsColumnModel_.addColumn(((TableColumn)vTable1));


        VTableColumn vTable2= new VTableColumn(i++, DESCRIPTION_PROPERTY);
        vTable2.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer()));
        vTable2.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer()));
        vTable2.setHeaderValue(descriptionColumnHeader_);
        vTable2.setPreferredCharWidth(70);
        detailsColumnModel_.addColumn(((TableColumn)vTable2));

    }


    /**
    Constructs a VUserList object.
    **/
    public VUserList()
    {
        userList_ = new UserList ();
        initializeTransient ();
    }



    /**
    Constructs a VUserList object.

    @param system   The AS/400 system from which the user will be retrieved.
    **/
    public VUserList (AS400 system)
    {
        if (system == null)
            throw new NullPointerException ("system");

        userList_ = new UserList (system);
        initializeTransient ();
    }
    // @A1A
    /**
     * Constructs a VUserList object.
     * @param userList The UserList object.
     * @param listName The name of the user list.
     *
    **/
    public VUserList (UserList userList,String listName)
    {

        description_=listName;
        userList_ =userList;
        initializeTransient ();
    }

    /**
    Constructs a VUserList object.

    @param parent   The parent.
    @param system   The AS/400 system from which the user will be retrieved..
    **/
    public VUserList (VNode parent, AS400 system)
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (system == null)
            throw new NullPointerException ("system");

        parent_   = parent;
        userList_ = new UserList (system);
        initializeTransient ();
    }



    /**
    Adds a listener to be notified when an error occurs.

    @param  listener    The listener.
    **/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



    /**
    Adds a listener to be notified when the value of any
    bound property changes.

    @param  listener  The listener.
    **/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        if (listener == null)
        {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
        }
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }



    /**
    Adds a listener to be notified when the value of any
    constrained property changes.

    @param  listener  The listener.
    **/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        if (listener == null)
        {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
        }
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



    /**
    Adds a listener to be notified when a VObject is changed,
    created, or deleted.

    @param  listener    The listener.
    **/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



    /**
    Adds a listener to be notified when work starts and stops
    on potentially long-running operations.

    @param  listener    The listener.
    **/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



    /**
    Returns the children of the node.

    @return         The children.
    **/
    public Enumeration children ()
    {
       return new VEnumeration (this);
    }



    /**
    Returns the list of actions that can be performed.

    @return Always null.  There are no actions.
    **/
    public VAction[] getActions ()
    {
       return null;
    }



    /**
    Indiciates if the node allows children.

    @return  Always false.
    **/
    public boolean getAllowsChildren ()
    {
       return false;
    }



    /**
    Returns the child node at the specified index.

    @param  index   The index.
    @return         Always null.
    **/
    public TreeNode getChildAt (int index)
    {
       return null;
    }



    /**
    Returns the number of children.

    @return  Always 0.
    **/
    public /* @A3D synchronized */ int getChildCount ()
    {
       return 0;
    }



    /**
    Returns the default action.

    @return Always null.  There is no default action.
    **/
    public VAction getDefaultAction ()
    {
       return null;
    }

    // @A1A : Gets user list description.
    private String getDescription(UserList userList)
    {
        String userInfo = userList.getUserInfo();
        String groupInfo = userList.getGroupInfo();
        // @A5D if (userInfo == null)
        // @A5D {
        // @A5D     Trace.log(Trace.ERROR, "Parameter 'userInfo' of userList is null.");
        // @A5D     throw new NullPointerException("userInfo");
        // @A5D }
        // @A5D if (groupInfo == null)
        // @A5D {
        // @A5D     Trace.log(Trace.ERROR, "Parameter 'groupInfo' of userList is null.");
        // @A5D     throw new NullPointerException("groupInfo");
        // @A5D }

        if (userInfo.toLowerCase().equals("*all"))
        {
            if (groupInfo.toLowerCase().equals("*none"))
                return allUsersDescription_;
        }
        if (userInfo.toLowerCase().equals("*member"))
        {
            if (groupInfo.toLowerCase().equals("*nogroup"))
                return usersNotInGroupsDescription_;
        }
        if (userInfo.toLowerCase().equals("*group"))
        {
            if (groupInfo.toLowerCase().equals("*none"))
                return groupsDescription_;
        }

        Trace.log(Trace.ERROR,"userInfo or groupInfo's value is invalid");
        Trace.log(Trace.INFORMATION,"userInfo : "+userInfo);
        Trace.log(Trace.INFORMATION,"groupInfo : "+groupInfo);
        return "";
    }

    /**
    Returns the child for the details at the specified index.

    @param  index   The index.
    @return         The child, or null if the index is not
                    valid.
    **/
    public VObject getDetailsChildAt (int index)
    {
       if ((index < 0) || (index >= detailsChildren_.length))
           return null;
        loadMore (index);
        return detailsChildren_[index];
    }



    /**
    Returns the number of children for the details.

    @return  The number of children for the details.
    **/
    public /* @A3D synchronized */ int getDetailsChildCount ()
    {
        // @C1D if (!isLoaded_) //@A3A
        // @C1D    load();       //@A3A
        return detailsChildren_.length;
    }



    /**
    Returns the table column model to use in the details
    when representing the children.  This column model
    describes the details values for the children.

    @return The details column model.
    **/
    public TableColumnModel getDetailsColumnModel ()
    {
        return detailsColumnModel_;
    }



    /**
    Returns the index of the specified child for the details.

    @param  detailsChild   The details child.
    @return                The index, or -1 if the child is not found
                           in the details.
    **/
    public /* @A3D synchronized */ int getDetailsIndex (VObject detailsChild)
    {
        // @C1D if (!isLoaded_) //@A3A
        // @C1D   load();       //@A3A
        for (int i = 0; i < loaded_; ++i)
            if (detailsChildren_[i].equals(detailsChild))
                return i;
        return -1;
    }


    /**
    Returns the group information.

    @return The group information.

    @see com.ibm.as400.access.UserList#getGroupInfo
    **/
    public String getGroupInfo ()
    {
        return userList_.getGroupInfo ();
    }



    /**
    Returns the icon.

    @param  size    The icon size, either 16 or 32.  If any other
                value is given, then return a default.
    @param  open    This parameter has no effect.
    @return         The icon.
    **/
    public Icon getIcon (int size, boolean open)
    {
        if (size == 32)
            return icon32_;
        else
            return icon16_;
    }



    /**
    Returns the index of the specified child.

    @param  child   The child.
    @return         Always -1.
    **/
    public /* @A3D synchronized */ int getIndex (TreeNode child)
    {
       return -1;
    }



    /**
    Returns the parent node.

    @return The parent node, or null if there is no parent.
    **/
    public TreeNode getParent ()
    {
       return parent_;
    }

    /* @A5D
    // @A1A : Gets user list name.
    private String getListName(UserList userList)
    {
        String userInfo = userList.getUserInfo();
        String groupInfo = userList.getGroupInfo();
        if (userInfo == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userInfo' of userList is null.");
            throw new NullPointerException("userInfo");
        }
        if (groupInfo == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupInfo' of userList is null.");
            throw new NullPointerException("groupInfo");
        }

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
    */

    /**
    Returns the properties pane.

    @return The properties pane.
    **/
    public VPropertiesPane getPropertiesPane ()
    {
        return propertiesPane_;
    }



    /**
    Returns a property value.

    @param      propertyIdentifier  The property identifier.  The choices are
                                NAME_PROPERTY and DESCRIPTION_PROPERTY.
    @return                         The property value, or null if the
                                property identifier is not recognized.
    **/
    public Object getPropertyValue(
        Object propertyIdentifier)
    {
        if( propertyIdentifier==NAME_PROPERTY)
        {
            // @A5D return getListName(userList_);
            return this; // @A5A
        }
        else if( propertyIdentifier==DESCRIPTION_PROPERTY)
        {
            return getDescription(userList_);
        }
        return null;
    }


    /**
    Returns the AS/400 system from which the user will be retrieved.

    @return The AS/400 system from which the user will be retrieved.
    **/
    public AS400 getSystem ()
    {
        return userList_.getSystem ();
    }



    /**
    Returns the description text.

    @return The description text.
    **/
    public String getText ()
    {
        return description_;
    }



    /**
    Returns the user information.

    @return The user information.

    @see com.ibm.as400.access.UserList#getUserInfo
    **/
    public String getUserInfo ()
    {
        return userList_.getUserInfo ();
    }



    /**
    Initializes the transient data.
    **/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);

        workingEventSupport_    = new WorkingEventSupport (this);

        userList_.addPropertyChangeListener (propertyChangeSupport_);
        userList_.addVetoableChangeListener (vetoableChangeSupport_);

        // Initialize the private data.
        detailsChildren_        = new VObject[0];
        enum_                   = null;
        loaded_                 = -1;

        // Initialize the properties pane.
        propertiesPane_ = new UserListPropertiesPane (this,userList_);// @A1C

        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addWorkingListener (workingEventSupport_);

    }



    /**
    Indicates if the node is a leaf.

    @return  Always true.
    **/
    public boolean isLeaf ()
    {
        return true;
    }



    /**
    Indicates if the details children are sortable.

    @return Always false.
    **/
    //
    // Implementation note: We do not allow sorting because of the fact
    // that we load the list incrementally.
    //
    public boolean isSortable ()
    {
        return false;
    }



    /**
    Loads information about the object from the AS/400.
    **/
    public void load()
    {
        if (Trace.isTraceOn ())
            Trace.log (Trace.INFORMATION, "Loading users for user list.");

        workingEventSupport_.fireStartWorking ();

        Exception error = null;

        try {                                                       // @A2A
            enum_ = userList_.getUsers();                           // @A2A
        }                                                           // @A2A
        catch (Exception e) {                                       // @A2A
            error = e;                                              // @A2A
        }                                                           // @A2A

        synchronized (this) {

            // Stop listening to the previous children.
            for (int i = 0; i < loaded_; ++i) {
                detailsChildren_[i].removeErrorListener (errorEventSupport_);
                detailsChildren_[i].removeVObjectListener (objectEventSupport_);
                detailsChildren_[i].removeWorkingListener (workingEventSupport_);
            }

            // Refresh the children based on the user list.
            loaded_ = 0;
            // @A2D try {
            // @A2D    enum_ = userList_.getUsers ();
            if (error == null)                                      // @A2A
            // @A2D    detailsChildren_ = new VObject[userList_.getLength ()];// @A1C
                detailsChildren_ = new VUser[userList_.getLength()];// @A2A
            else                                                    // @A2A
                detailsChildren_ = new VUser[0];                    // @A2A
            // @A2D }
            // @A2D catch (Exception e) {
            // @A2D    error = e;
            // @A2D    detailsChildren_ = new VObject[0];// @A1C
            // @A2D }
        }

        // This line used to be the last line in the method but
        // that caused an infininte loop.  The event support
        // calls load().  Since isLoaded_ is still
        // false, userList ran to the AS/400 to get user info again.
        // My moving isLoaded=true to here, the event support
        // knows we have the info and there is no need to go to the AS/400.
        isLoaded_ = true;                                           // @B1m

        if (error != null)
            errorEventSupport_.fireError (error);

        workingEventSupport_.fireStopWorking (); //@B0M - moved this to before fireObjectChanged

        objectEventSupport_.fireObjectChanged (this, true);         // @A2A

    }




    /**
    Loads more messages from the enumeration, if needed

    @param index    The index needed.
    **/
    private void loadMore (int index)
    {
        // @A5D if (enum_ == null) // @A3A
        // @A5D   load();          // @A3A

        if (index >= loaded_) {

            workingEventSupport_.fireStartWorking ();

            Exception error = null;
            // @A2D synchronized (this) {

                for (int i = loaded_; i <= index; ++i) {
                    User user = (User) enum_.nextElement ();

                    detailsChildren_[i] = new VUser (user);

                    detailsChildren_[i].addErrorListener (errorEventSupport_);
                    detailsChildren_[i].addVObjectListener (objectEventSupport_);
                    detailsChildren_[i].addWorkingListener (workingEventSupport_);
                }

            // @A2D }

            loaded_ = index + 1;

            if (error != null)
                errorEventSupport_.fireError (error);

            workingEventSupport_.fireStopWorking ();
        }
    }



    /**
    Restores the state of the object from an input stream.
    This is used when deserializing an object.

    @param in   The input stream.
    **/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
    }




    /**
    Removes an error listener.

    @param  listener    The listener.
    **/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



    /**
    Removes a property change listener.

    @param  listener  The listener.
    **/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        if (listener == null)
        {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
        }
        propertyChangeSupport_.removePropertyChangeListener (listener);
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
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



    /**
    Removes a VObjectListener.

    @param  listener    The listener.
    **/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }



    /**
    Removes a working listener.

    @param  listener    The listener.
    **/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



    /**
    Sets the group information.  The default is UserList.NONE.

    @param groupInfo The group information, either
                 a group name, UserList.NOGROUP
                 or UserList.NONE.


    @exception PropertyVetoException If the change is vetoed.

    @see com.ibm.as400.access.UserList#setGroupInfo
    **/
    public void setGroupInfo (String groupInfo)
        throws PropertyVetoException
    {
        if (groupInfo == null)
            throw new NullPointerException ("groupInfo");

        userList_.setGroupInfo (groupInfo);
    }



    /**
    Sets the AS/400 system in which the user information resides.

    @param system The AS/400 system in which the user information resides.

    @exception PropertyVetoException If the change is vetoed.
    **/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        if (system == null)
            throw new NullPointerException ("system");
        AS400 oldValue = userList_.getSystem ();// @A1C
        AS400 newValue = system;// @A1C
        vetoableChangeSupport_.fireVetoableChange ("system", oldValue, newValue);// @A1C

        if (oldValue != newValue)
        {
           try
           {
               userList_.setSystem (system);
           }
           catch (PropertyVetoException e)
           {
                // Ignore.
           }
        }// @A1C

        propertyChangeSupport_.firePropertyChange ("system", oldValue, newValue);// @A1C

    }



    /**
    Sets the user information.  The default is UserList.ALL.

    @param userInfo The user information, one of:
                <ul>
                  <li>UserList.ALL
                  <li>UserList.USER
                  <li>UserList.GROUP
                  <li>UserList.MEMBER
                </ul>


    @exception PropertyVetoException If the change is vetoed.

    @see com.ibm.as400.access.UserList#setUserInfo
    **/
    public void setUserInfo (String userInfo)
        throws PropertyVetoException
    {
        if (userInfo == null)
            throw new NullPointerException ("userInfo");

        userList_.setUserInfo (userInfo);
    }




    /**
    Sorts the children for the details.  Since sorting is not supported,
    this method does nothing.

    @param  propertyIdentifiers The property identifiers.
    @param  orders              The sort orders for each property
                                identifier. true for ascending order;
                                false for descending order.
    **/
    public void sortDetailsChildren (Object[] propertyIdentifiers,
                                                  boolean[] orders)
    {
      // No sorting here!
    }



    /**
    Returns the string representation of the description.

    @return The string representation of the description.
    **/
    public String toString ()
    {
        return description_;
    }



}
