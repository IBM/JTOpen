///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VUser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;//@A1A
import com.ibm.as400.access.User;

import com.ibm.as400.access.QueuedMessage;//@A1A
import com.ibm.as400.access.Trace;//@A1A
import javax.swing.Icon;
import javax.swing.SwingConstants;//@A1A
import javax.swing.table.DefaultTableColumnModel;//@A1A
import javax.swing.table.TableColumnModel;//@A1A
import javax.swing.tree.TreeNode;//@A1A
import java.beans.PropertyChangeListener;//@A1A
import java.beans.VetoableChangeListener;//@A1A
import java.beans.PropertyVetoException;//@A1A
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;//@A1A



/**
The VUser class defines the representation of a user on a
system for use in various models and panes in this package.
You must explicitly call load() to load the information from
the system.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VUser objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VUser
implements VObject, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


         
    private static final String description_    = ResourceLoader.getText ("USER_DESCRIPTION");
    private static final Icon   icon16_         = ResourceLoader.getIcon ("VUser16.gif", description_);
    private static final Icon   icon32_         = ResourceLoader.getIcon ("VUser32.gif", description_);
    
    private User                     user_        = null;
   
       
    
    
    transient private VPropertiesPane   propertiesPane_;


    
    // Event support.
    transient private ErrorEventSupport           errorEventSupport_;
    transient private PropertyChangeSupport       propertyChangeSupport_;//@A1A
    transient private VetoableChangeSupport       vetoableChangeSupport_;//@A1A
    transient private VObjectEventSupport         objectEventSupport_;
    transient private WorkingEventSupport         workingEventSupport_;







    //@A1A
	/**
	Constructs a VUser object.
	**/
    public VUser ()
    {
        user_    = null;
        
        initializeTransient ();
    }



	/**
	Constructs a VUser object.

	@param user          The user.
	**/
    public VUser (User user)
    {
        
        if (user == null)
            throw new NullPointerException ("user");

        user_     = user;
      
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
    //@A1A
    /**
	Adds a listener to be notified when the value of any
	bound property changes.

	@param  listener  The listener.
	**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }


    //@A1A
	/**
	Adds a listener to be notified when the value of any
	constrained property changes.

	@param  listener  The listener.
	**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
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
	Returns the default action.

	@return Always null.  There is no default action.
	**/
    public VAction getDefaultAction ()
    {
        return null;
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
	Returns the description.

	@return The description.

	@see com.ibm.as400.access.User#getDescription
	**/
    public String getDescription ()
    {
        return user_.getDescription ();
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
    //@A1C Changed from return user_.getName(); because if new a VUser object with the
    //VUser() constructor, user_ is null.
	/**
	Returns the user name. 

	@return The user name.
	**/
    public String getName ()
    {
        if (user_ != null)
            return user_.getName ();
        else
            return "";
    }


    


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
    public Object getPropertyValue (Object propertyIdentifier)
    {
        // Get the name.
        if (propertyIdentifier== NAME_PROPERTY)
            return this;

       
        else if (propertyIdentifier== DESCRIPTION_PROPERTY)
            return user_.getDescription(); 
        // By default, return null.
        return null;
    }

    //@A1A
	/**
	Returns the system on which the user resides.

	@return The system on which the user resides.

	**/
    public AS400 getSystem ()
    {
        return user_.getSystem();
    }

	/**
	Returns the text.  This is the user name.

	@return The text which is the user name.
	**/
    public String getText ()
    {
        if (user_ != null)
            return user_.getName ();
        else
            return "";
    }
    //@A1A
	/**
	Returns the user.

	@return The user, or null if it has not been set.
	**/
    public User getUser ()
    {
        return user_;
    }
	/**
	Initializes the transient data.
	**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        propertyChangeSupport_  = new PropertyChangeSupport (this);//@A1A
        vetoableChangeSupport_  = new VetoableChangeSupport (this);//@A1A
        workingEventSupport_    = new WorkingEventSupport (this);

        

        // Initialize the properties pane.
        propertiesPane_ = new UserPropertiesPane (this, user_);

        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addWorkingListener (workingEventSupport_);
    }


	/**
	Loads information about the object from the system.
	**/
    public void load ()
    {
        
       
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
    //@A1A
	/**
	Removes a property change listener.

	@param  listener  The listener.
	**/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }

    //@A1A
	/**
	Removes a vetoable change listener.

	@param  listener  The listener.
	**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
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

    //@A1A
	/**
	Sets the user.

	@param user The user.

	@exception PropertyVetoException If the change is vetoed.
	**/
    public void setUser (User user)
        throws PropertyVetoException
    {
        if (user == null)
            throw new NullPointerException ("user");

        User oldValue = user_;
        User newValue = user;
        vetoableChangeSupport_.fireVetoableChange ("user", oldValue, newValue);

        if (oldValue != newValue) {

            user_ = user;

            
        }

        propertyChangeSupport_.firePropertyChange ("user", oldValue, newValue);
    }

	/**
	Returns the string representation of the user name.

	@return The string representation of the user name.
	**/
    public String toString ()
    {
        return getText ();
    }


}
