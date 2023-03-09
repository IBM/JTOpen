///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemPool.java
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
import com.ibm.as400.access.SystemPool;
import com.ibm.as400.access.SystemStatus;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;

/**
 * The VSystemPool class defines the representation of system pool on a
 * system for use in various models and panes in this package.
 *
 * <p>Most errors are reported as ErrorEvents rather than throwing exceptions.
 *    Users should listen for ErrorEvents in order to diagnose and recover
 *    from error conditions.
 *
 * <p>VSystemPool objects generate the following events:
 * <ul>
 *     <li> ErrorEvent
 *     <li> VObjectEvent
 *     <li> WorkingEvent
 * </ul>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VSystemPool
implements VObject, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    private static final Icon icon16_ = ResourceLoader.getIcon("VSystemPool16.gif");
    private static final Icon icon32_ = ResourceLoader.getIcon("VSystemPool32.gif");

    /**
       The property identifier for the system pool identifier.
    **/
    public static String IDENTIFIER_PROPERTY    = "System pool";
    /**
       The property identifier for the pool size.
    **/
    public static String POOL_SIZE_PROPERTY     = "Pool size";
    /**
       The property identifier for the reserved size.
    **/
    public static String RESERVED_SIZE_PROPERTY = "Reserved size";

    private SystemPool systemPool_ = null;
    private transient VAction[] actions_;

    transient private VPropertiesPane   propertiesPane_;

    // Event support.
    transient private ErrorEventSupport      errorEventSupport_;
    transient private PropertyChangeSupport  propertyChangeSupport_;
    transient private VetoableChangeSupport  vetoableChangeSupport_;
    transient private VObjectEventSupport    objectEventSupport_;
    transient private WorkingEventSupport    workingEventSupport_;

    // The variable representing if the modify action is allowed.
    private boolean allowModify_ = false;
    
    /**
     * Constructs a VSystemPool object.
    **/
    public VSystemPool ()
    {
        systemPool_ = null;
        initializeTransient ();
    }

    /**
     * Constructs a VSystemPool object.
     *
     * @param systemPool The SystemPool object.
    **/
    public VSystemPool (SystemPool systemPool)
    {
        if (systemPool == null)
            throw new NullPointerException ("systemPool");

        systemPool_ = systemPool;
        systemPool_.setCaching(true); //@B1A
      
        initializeTransient ();
    }

    /**
     * Adds a listener to be notified when an error occurs.
     *
     * @param listener The listener.
    **/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }

    /**
     * Adds a listener to be notified when the value of any bound property
     * changes.
     *
     * @param listener The listener.
    **/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }

    /**
     * Adds a listener to be notified when the value of any constrained
     * property changes.
     *
     * @param listener The listener.
    **/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }

    /**
     * Adds a listener to be notified when a VObject is changed, created,
     * or deleted.
     *
     * @param listener The listener.
    **/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }

    /**
     * Adds a listener to be notified when work starts and stops
     * on potentially long-running operations.
     *
     * @param  listener    The listener.
    **/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }

    /**
     * Invoked when an error has occurred. 
     *
     * @param event The error event.
    **/
    public void errorOccurred (ErrorEvent event)
    {
        errorEventSupport_.errorOccurred(event);
    }

    /**
     * Returns the list of actions that can be performed.
     *
     * @return Always null. There are no actions.
    **/
    public VAction[] getActions ()
    {
        return actions_;
    }

    /**
     * Returns the default action.
     *
     * @return Always null. There is no default action.
    **/
    public VAction getDefaultAction ()
    {
      return null;
    }

    /**
     * Returns the icon.
     *
     * @param size The icon's size, either 16 or 32. If any other value
     *             is given, a default one will be returned.
     * @param open This parameter has no effect.
     * @return     The icon.
    **/
    public Icon getIcon (int size, boolean open)
    {
        if (size == 32)
            return icon32_;
        else
            return icon16_;
    }

    /**
     * Returns the properties pane.
     *
     * @return The properties pane.
    **/
    public VPropertiesPane getPropertiesPane ()
    {
        return propertiesPane_;
    }

    /**
     * Returns a property value.
     *
     * @param  propertyIdentifier The property identifier.
     * @return   The property value, or null if the property identifier
     *           is not recognized.
    **/
    public Object getPropertyValue (Object propertyIdentifier)
    {
      try //@B1A
      {
        // Get the name.
        if (propertyIdentifier==NAME_PROPERTY)
            return this;
        else if(propertyIdentifier==DESCRIPTION_PROPERTY)
        {
            return systemPool_.getDescription();
        }        
        else if(propertyIdentifier==IDENTIFIER_PROPERTY)
        {
             return Integer.toString(systemPool_.getPoolIdentifier());
        }  
        else if(propertyIdentifier==POOL_SIZE_PROPERTY)
        {
            return Integer.toString(systemPool_.getPoolSize());
        } 
        else if(propertyIdentifier==RESERVED_SIZE_PROPERTY)
        {
            return Integer.toString(systemPool_.getReservedSize());
        }        
        else
            return null; // By default, return null.
      }
      catch(Exception e) //@B1A
      {
        Trace.log(Trace.ERROR, "Unable to getPropertyValue for VSystemPool.", e); //@B1A
        errorEventSupport_.fireError(e); //@B1A
        return null;
      }
    }

    /**
     * Returns the system in which the system pool information resides.
     *
     * @return The system in which the system pool information resides.
    **/
    public AS400 getSystem ()
    {
        return systemPool_.getSystem();
    }

    /**
     * Returns the systemPool.
     *
     * @return The systemPool, or null if it has not been set.
    **/
    public SystemPool getSystemPool ()
    {
        return systemPool_;
    }

    /**
     * Returns the text which is the system pool name.
     *
     * @return The text which is the system pool name.
    **/
    public String getText ()
    {
        if (systemPool_ != null)
            return systemPool_.getPoolName ();
        else
            return "";
    }

    /**
     * Initializes the transient data.
    **/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_     = new ErrorEventSupport (this);
        objectEventSupport_    = new VObjectEventSupport (this);
        propertyChangeSupport_ = new PropertyChangeSupport (this);
        vetoableChangeSupport_ = new VetoableChangeSupport (this);
        workingEventSupport_   = new WorkingEventSupport (this);

        // Initialize the properties pane.
        propertiesPane_ = new SystemPoolPropertiesPane (this, systemPool_);

        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addWorkingListener (workingEventSupport_);
        actions_ = new VAction[1];
        actions_[0]= new VSystemPoolModifyAction(this,systemPool_);

        actions_[0].addErrorListener(((ErrorListener)errorEventSupport_));
        actions_[0].addWorkingListener(((WorkingListener)workingEventSupport_));
    }

    /**
     * Returns true if the modify action is allowed, false otherwise.
     *
     * @return true if the modify action is allowed, false otherwise.
    **/
    public boolean isAllowModify()
    {
        return allowModify_;
    }


    /**
     * Loads information about the object from the system.
    **/
    public void load ()
    {
      if (systemPool_ != null) systemPool_.refreshCache(); //@B1A
    }

    /**
     * Restores the state of the object from an input stream.
     * This is used when deserializing an object.
     *
     * @param in The input stream.
    **/
    private void readObject (ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
    }

    /**
     * Removes an error listener.
     *
     * @param listener The listener.
    **/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }

    /**
     * Removes a property change listener.
     *
     * @param listener The listener.
    **/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }

    /**
     * Removes a vetoable change listener.
     *
     * @param listener The listener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }

    /**
     * Removes a VObjectListener.
     *
     * @param listener The listener.
    **/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }

    /**
     * Removes a working listener.
     *
     * @param listener The listener.
    **/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }

    /**
     * Set the state of modify action.
     *
     * @param allowModify The boolean value.
    **/
    public void setAllowModify(boolean allowModify)
    {
        allowModify_ = allowModify;
        for(int i=0; i<actions_.length; i++)
        {
            if(actions_[i] != null) 
            {
                ((VSystemPoolModifyAction)actions_[i]).setEnabled(allowModify);
            }
        }    
    }

    /**
     * Sets the SystemPool object.
     *
     * @param systemPool The SystemPool object.
     *
     * @exception PropertyVetoException If the change is vetoed.
    **/
    public void setSystemPool (SystemPool systemPool)
           throws PropertyVetoException
    {
        if (systemPool == null)
            throw new NullPointerException ("systemPool");

        if (systemPool_ != systemPool)
        {
            SystemPool oldValue = systemPool_;
            SystemPool newValue = systemPool;
            vetoableChangeSupport_.fireVetoableChange ("systemPool", oldValue, newValue);

            systemPool_ = systemPool;
            systemPool_.setCaching(true); //@B1A

            propertyChangeSupport_.firePropertyChange ("systemPool", oldValue, newValue);
            objectEventSupport_.fireObjectChanged(this);
        }
    }

    /**
     * Returns the string representation of the system pool name.
     *
     * @return The string representation of the system pool name.
    **/
    public String toString ()
    {
        return getText ();
    }

}
