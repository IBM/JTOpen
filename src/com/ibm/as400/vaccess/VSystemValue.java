///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemValue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.SystemValue;
import com.ibm.as400.access.SystemValueList;

import javax.swing.Icon;

import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.beans.PropertyVetoException;

/**
 * The VSystemValue class defines the representation of a 
 * system value in an AS/400 for use in various models 
 * and panes in this package.
 * You must explicitly call load() to load the information from
 * the AS/400.
 * 
 * <p>Most errors are reported as ErrorEvents rather than
 * throwing exceptions.  Users should listen for ErrorEvents
 * in order to diagnose and recover from error conditions.
 * 
 * <p>VSystemValue objects generate the following events:
 * <ul>
 *     <li>ErrorEvent
 *     <li>PropertyChangeEvent
 *     <li>VObjectEvent
 *     <li>WorkingEvent
 * </ul>
**/
class VSystemValue implements VObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Property.    
    private SystemValue systemValue_;

    // Private data.
    private VObject[] detailsChildren_;
    private VObject[] children_;
      
    private VAction[] actions_;

    // Event support.
    VPropertiesPane propertiesPane_;    
    ErrorEventSupport errorEventSupport_;
    VObjectEventSupport objectEventSupport_;
    WorkingEventSupport workingEventSupport_;

    // MRI
    final static private Icon icon16_;
    final static private Icon icon32_;  
 	
    static
    {
        // The icon description is not MRI.
        icon16_= ResourceLoader.getIcon("VSystemValue16.gif", "System Values");
        icon32_= ResourceLoader.getIcon("VSystemValue32.gif", "System Values");
    }

    /**
     * Constants indicating property for system value information.
     *
    **/
    final public static String VALUE_PROPERTY = "Value";
	 
   
    /**
     * Constructs a VSystemValue object.
     * @param systemValue The SystemValue object.
    **/
    public VSystemValue(SystemValue systemValue)
    {
        systemValue_ = systemValue;

        errorEventSupport_= new ErrorEventSupport(this);
        objectEventSupport_= new VObjectEventSupport(this);
        workingEventSupport_= new WorkingEventSupport(this);

        actions_ = new VAction[1];
        actions_[0]= new VSystemValueModifyAction(this);
        if (systemValue_.isReadOnly())
          actions_[0].setEnabled(false);

        actions_[0].addErrorListener(errorEventSupport_);
        actions_[0].addVObjectListener(objectEventSupport_);
        actions_[0].addWorkingListener(workingEventSupport_);

        propertiesPane_= new VSystemValueDetailsPropertiesPane(this, systemValue_);

        propertiesPane_.addErrorListener(errorEventSupport_);
        propertiesPane_.addVObjectListener(objectEventSupport_);
        propertiesPane_.addWorkingListener(workingEventSupport_);
    }
     

   /**
    * Adds a listener to be notified when an error occurs.
    * @param listener      The error listener.
   **/    
    public void addErrorListener(ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener);
    }


   /**
    * Adds a listener to be notified when a VObject is changed,
    * created, or deleted.
    * @param listener      The VObject listener.
   **/
    public void addVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener(listener);
    }

   /**
    * Adds a listener to be notified when work starts and stops on 
    * potentially long-running operations.
    * @param listener      The working listener.
   **/
    public void addWorkingListener(WorkingListener workin1)
    {
        workingEventSupport_.addWorkingListener(workin1);
    }

    /**
     * Returns the list of actions that can be performed.
     *      
     * @return Always null.  There are no actions.
     * 
    **/
    public VAction[] getActions()
    {
        return actions_;
    }

    /** 
     * Returns the copyright.
    **/
    private static String getCopyright()
    {
        return Copyright_v.copyright;
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
    * Returns the description.
    * @return The system value description.
    *
   **/
   public String getDescription()
   {
      return systemValue_.getDescription();
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
    * Returns the system value name.
    * @return The system value name.
    *
   **/
   public String getName()
   {
      return systemValue_.getName();
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
     * @param      propertyIdentifier The property identifier.  
     *             The choices are
     *                  <ul>
     *                     <li>NAME_PROPERTY
     *                     <li>VALUE_PROPERTY
     *                     <li>DESCRIPTION_PROPERTY
     *                  </ul>
     * @return     The property value, or null if the
     *             property identifier is not recognized.
    **/    
    public Object getPropertyValue(Object propertyIdentifier)
    {
        if (propertyIdentifier == NAME_PROPERTY)
        {
          return this; // The renderer will call toString() on us.
        }
        if (propertyIdentifier == DESCRIPTION_PROPERTY)
        {
          return systemValue_.getDescription();
        }
        if (propertyIdentifier == VALUE_PROPERTY)
        {            
//@B0D            workingEventSupport_.fireStartWorking();
            Object value = null;
            try
            {
                value = systemValue_.getValue();
            }
            catch(Exception e)
            {
                errorEventSupport_.fireError(e);
            }
//@B0D            workingEventSupport_.fireStopWorking();
            if (value != null &&
                systemValue_.getType() == SystemValueList.TYPE_ARRAY)
            {
              String[] valStrs = (String[]) value;
              StringBuffer strBuf = new StringBuffer();
              for (int i=0; i<valStrs.length; i++)
              {
                String temp = valStrs[i].trim();
                // Does this item contain data?
                if (temp.length() > 0)
                {
                  // Have we added any items to the string yet?
                  if (strBuf.length() > 0)
                  {
                    strBuf.append(",");
                  }
                  strBuf.append(temp);
                }
              }
              return strBuf.toString();
            }
            return value;
        }
        return null;
    }

    /**
     * Returns the text. This is the system value name.
     * @return The text which is the system value name.
    **/    
    public String getText()
    {        
        return systemValue_.getName();
    }

    /**
     * Returns the system value type.
     * @return The system value type.
    **/
    public int getType()
    {
      return systemValue_.getType();
    }

   /**
    * Returns the system value information.
    * @return The system value information.
    *
   **/
   public Object getValue()
   {
      Object value = null;
      try
      {
//@B0D            workingEventSupport_.fireStartWorking();
            value = systemValue_.getValue();
      }
      catch(Exception e)
      {
            errorEventSupport_.fireError(e);
      }
//@B0D      workingEventSupport_.fireStopWorking();
      return value;      
   }
 

    /**
     * Loads information about the object from the AS/400.
    **/    
    public void load()
    {
      workingEventSupport_.fireStartWorking();
      try
      {
        systemValue_.clear(); // This clears the system value cache so
                              // that we get the new value from the 400.
        systemValue_.getValue(); // This causes a connection in case we haven't made one yet.
      }
      catch(Exception e)
      {
        errorEventSupport_.fireError(e);
      }
      workingEventSupport_.fireStopWorking();
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
    public void removeWorkingListener(WorkingListener workin1)
    {
        workingEventSupport_.removeWorkingListener(workin1);
    }

    /**
     * Sets the system value.
     * @param value The value to be set.
     * Example:
     * <pre>
     *   AS400 theSystem = new AS400("My400");
     *   VSystemValue sysVal = new VSystemValue(theSystem, "QUSRLIBL");
     *   // QUSRLIBL is of type TYPE_ARRAY
     *   String[] theList = new String[] { "QGPL", "QTEMP" };
     *   sysVal.setValue(theList);
     *
     *   sysVal = new VSystemValue(theSystem, "QTIME");
     *   // QTIME is of type TYPE_DATE
     *   java.sql.Time theTime = new java.sql.Time(12, 30, 0);
     *   sysVal.setValue(theTime);
     *
     *   sysVal = new VSystemValue(theSystem, "QCCSID");
     *   // QCCSID is of type TYPE_INTEGER
     *   Integer theCcsid = new Integer(37);
     *   sysVal.setValue(theCcsid);
     * </pre>
     *
    **/
   public void setValue(Object value)
   {
//@B0D         workingEventSupport_.fireStartWorking();
         try
         {
                systemValue_.setValue(value);
                // fireStopWorking should occur before fireObjectChanged
                // because VSystemValueGroup listens for the objectChanged
                // and could possibly "disconnect all of the wiring" of
                // the group, which has the result of disabling the
                // explorer pane.
//@B0D                workingEventSupport_.fireStopWorking();
                objectEventSupport_.fireObjectChanged(this);
         }
         catch(Exception e)
         {
                errorEventSupport_.fireError(e);
                // Need to fire a stopWorking, but don't want to
                // fire two of them.
//@B0D                workingEventSupport_.fireStopWorking();
         }
   }
}

