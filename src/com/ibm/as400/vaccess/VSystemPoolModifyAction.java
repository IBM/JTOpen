///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemPoolModifyAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.SystemPool;
import com.ibm.as400.access.IFSFileOutputStream;
import com.ibm.as400.access.Trace;

import javax.swing.border.*; 
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.EventObject;

/**
 * The VSystemPoolModifyAction class represents the VSystemPool modify action.
**/
class VSystemPoolModifyAction implements VAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private SystemPool systemPool_;
    
    private ErrorEventSupport errorEventSupport_ = new ErrorEventSupport(this);
    private VObjectEventSupport objectEventSupport_ = new VObjectEventSupport(this);
    private WorkingEventSupport workingEventSupport_ = new WorkingEventSupport(this);
    
    private boolean enabled_ = true;
    
    /**
     * Constructs a VSystemPoolModifyAction object.
     *
     * @param vObject      The VObject object.
     * @param systemPool   The SystemPool object.
    **/
    public VSystemPoolModifyAction(VObject vObject, SystemPool systemPool)
    {
        systemPool_ = systemPool;
    }

    /**
     * Adds a listener to be notified when an error occurs.
     *
     * @param  listener    The listener.
    **/
    public void addErrorListener(ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener);
    }

    /**
     * Adds a listener to be notified when a VObject is changed, created,
     * or deleted.
     *
     * @param listener The listener.
    **/
    public void addVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener(listener);
    }

    /**
     * Adds a listener to be notified when work starts and stops on
     * potentially long-running operations.
     *
     * @param listener The listener.
    **/
    public void addWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener(listener);
    }

    /**
     * Invoked when an error has occurred. 
     *
     * @param event The error event.
    **/
    public void errorOccurred(ErrorEvent event)
    {
        errorEventSupport_.errorOccurred(event);
    }

    /**
     * Returns the text.
     *
     * @return The text.
    **/
    public String getText()
    {
        return ResourceLoader.getText("ACTION_MODIFY");
    }


    /**
     * Returns true if the action is enabled, false otherwise.
     *
     * @return true if the action is enabled, false otherwise.
    **/
    public boolean isEnabled()
    {
        return enabled_;
    }

    /**
     * Perform the modify action.
     *
     * @param vAction The Vaction context.
    **/
    public void perform(VActionContext context)
    {
      VSystemPoolModifyDialog systemPoolDialog = new VSystemPoolModifyDialog(context.getFrame(), systemPool_);
      systemPoolDialog.addErrorListener(errorEventSupport_);
      systemPoolDialog.addWorkingListener(workingEventSupport_); //@B1A
      systemPoolDialog.show(); //@B1A
    }

    /**
     * Removes an error listener.
     *
     * @param listener The listener.
    **/
    public void removeErrorListener(ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener(listener);
    }

    /**
     * Removes a VObjectListener.
     *
     * @param listener The listener.
    **/
    public void removeVObjectListener(VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener(listener);
    }

    /**
     * Removes a working listener.
     *
     * @param listener The listener.
    **/
    public void removeWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener(listener);
    }


    /**
     * Set the modify action enabled.
     *
     * @param enabled The boolean value.
    **/
    public void setEnabled(boolean enabled)
    {
        enabled_= enabled;
    }

    /**
     * Invoked when a potentially long-running unit of work is about to begin.
     *
     * @param event The event.
    **/
    public void startWorking(WorkingEvent event)
    {
        workingEventSupport_.startWorking(event);
    }

    /**
     * Invoked when a potentially long-running unit of work has completed. 
     *
     * @param event The event.
    **/
    public void stopWorking(WorkingEvent event)
    {
        workingEventSupport_.stopWorking(event);
    }
    
    /**
     * Returns the string representation of this object.
     *
     * @return The string representation of this object.
    **/
    public String toString()
    {
        return getText();
    }

}
