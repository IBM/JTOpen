///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PermissionAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.Permission;

/**
The PermissionAction class defines a permission change action to be performed on an AS/400 resource.

Most errors are reported as ErrorEvents rather than throwing exceptions. Users should listen for ErrorEvents in
order to diagnose and recover from error conditions.

<p>PermissionAction objects generate the following events:
<ul>
<li> ErrorEvent
<li> VObjectEvent
<li> WorkingEvent
</ul>
**/
class PermissionAction 
      implements VAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     *  Private variable representing whether the item is enabled on the popup menu.
     **/
    private boolean enabled_;

    /**
     * Private variable representing object whose permission will be displayed
     * and modified.
     **/
    private IFSFile file_;

    /**
     * Private variable representing event support.
     **/
    private ErrorEventSupport errorEventSupport_= new ErrorEventSupport(this);
    private VObjectEventSupport objectEventSupport_= new VObjectEventSupport(this);
    private WorkingEventSupport workingEventSupport_= new WorkingEventSupport(this);

    // The Permission object for this action.
    // @B1D private Permission permission_ = null;  //@A2C

    /**
     * Constructs a PermissionAction object.
     * @param file  The IFSFile object.
     **/
    public PermissionAction(IFSFile file)
    {
        enabled_= true;
        file_= file;

        // Postpone initializing the Permission object until it's needed.  //@A2C
    }

    /**
     * Adds a listener to be notified when an error occurs.
     * @param  listener    The listener.
    **/
    public void addErrorListener(ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener);
    }

    /**
     * Adds a VObjectListener.
     * @param  listener    The listener.
     **/
    public void addVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener(listener);
    }

    /**
     * Adds a working listener.
     * @param  listener    The listener.
    **/

    public void addWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener(listener);
    }

    //@A2A
    /**
     * Returns the Permission object.
     * @return The Permission object.
     **/
    Permission getPermission()
    {
      // @B1D if (permission_ == null) {
        try {
          return new Permission (file_); // @B1C
        }
        catch(Exception e)
        {
          Trace.log(Trace.ERROR, "construct Permission : "+e);
          errorEventSupport_.fireError(e);
          return null;                  // @B1A
        }
      // @B1D }
      // @B1D return permission_;
    }

     /**
     * Returns the text.
     * @return The text that displays as an item in the popup menu .
    **/
    public String getText()
    {
        return ResourceLoader.getText("OBJECT_PERMISSION2"); // @C1C
    }

    /**
     * Indicates if the action is enabled.
     * @return True if the action is enabled.
     **/
    public boolean isEnabled()
    {
        return enabled_;
    }

    /**
     * Performs the action.
     * @param  vAction   The action context.
     **/
    public void perform(VActionContext vAction)
    {
        workingEventSupport_.fireStartWorking();
        PermissionMainPanel mainPanel = new PermissionMainPanel(getPermission()); //@A2C
        mainPanel.addErrorListener(errorEventSupport_);
        mainPanel.addWorkingListener(workingEventSupport_);
        mainPanel.addVObjectListener(objectEventSupport_);
        mainPanel.createPermissionMainPanel();
        workingEventSupport_.fireStopWorking();
    }

    /**
     * Removes an error listener.
     * @param  listener    The listener.
    **/
    public void removeErrorListener(ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener(listener);
    }

    /**
     * Removes a VObjectListener.
     * @param  listener    The listener.
     **/
    public void removeVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener(listener);
    }

    /**
     * Removes a working listener.
     * @param  listener    The listener.
    **/
    public void removeWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener(listener);
    }

    /**
     * Sets the enabled state.
     * @param enable   True if a user can select it.
    **/
    public void setEnabled(boolean enable)
    {
        enabled_ = enable;
    }
}
