///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemValueModifyAction.java
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
import com.ibm.as400.access.IFSFileOutputStream;
import com.ibm.as400.access.Trace;

import java.util.Date;
import java.util.Vector;
import java.util.EventObject;


/**
 * The VSystemValueModifyAction class defines the modify action 
 * of a system value on an AS/400 .
 * 
 * <p>Most errors are reported as ErrorEvents rather than
 * throwing exceptions.  Users should listen for ErrorEvents
 * in order to diagnose and recover from error conditions.
 * 
 * <p>VSystemValueModifyAction objects generate the following events:
 * <ul>
 *     <li>ErrorEvent
 *     <li>VObjectEvent
 *     <li>WorkingEvent
 * </ul>
**/
class VSystemValueModifyAction implements VAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private boolean enabled_;    
    private VSystemValue systemValue_;
    
    // Events support.
    private ErrorEventSupport errorEventSupport_;
    private VObjectEventSupport objectEventSupport_;
    private WorkingEventSupport workingEventSupport_;


    // MRI
    final private static String modifyActionText_;

    static
    {
      modifyActionText_ = (String)ResourceLoader.getText("ACTION_MODIFY");
    }
        

    /**
     * Constructs a VSystemValueModifyAction object.
     * @param       systemValue     The VSystemValue to modify.
     **/
    public VSystemValueModifyAction(VSystemValue systemValue)
    {
        systemValue_ = systemValue;
        errorEventSupport_= new ErrorEventSupport(this);
        objectEventSupport_= new VObjectEventSupport(this);
        workingEventSupport_= new WorkingEventSupport(this);
        enabled_ = true;
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
     * Returns the copyright.
    **/
    private static String getCopyright()
    {
        return Copyright_v.copyright;
    }
    
    /**
     * Returns the display text of this action.
     * @return The text string to be displayed.
     **/
    public String getText()
    {
        return modifyActionText_;
    }

    /**
     * Indicates whether the action is enabled or not.
     * @return  The value which indicates whether the action is
     *          enabled or not.
     **/
    public boolean isEnabled()
    {
        return enabled_;
    }

    /**
     * The action is performed.
     * @param   vAction     The action performed.
     **/
    public void perform(VActionContext vAction)
    {
        int type = systemValue_.getType();
        switch(type)
        {
            case SystemValueList.TYPE_INTEGER:
            case SystemValueList.TYPE_STRING:
            case SystemValueList.TYPE_DECIMAL:
            {
                VSysvalTextDialog propertiesDialog 
                    = new VSysvalTextDialog(systemValue_, vAction.getFrame());
                propertiesDialog.setVisible(true);
            }
                 break;
            case SystemValueList.TYPE_ARRAY:
                 VSysvalArrayDialog arrayDialog 
                        = new VSysvalArrayDialog(vAction.getFrame(), systemValue_);
                 arrayDialog.setVisible(true);
                 break;
            case SystemValueList.TYPE_DATE:
                 VSysvalDateDialog dateDlg 
                     = new VSysvalDateDialog(systemValue_, vAction.getFrame());
                 dateDlg.setVisible(true);
                break;
        }
    }

    /**
     * Removes an error listener.
     * 
     * @param  listener    The listener.
    **/
    public void removeErrorListener(ErrorListener errorL1)
    {
        errorEventSupport_.removeErrorListener(errorL1);
    }

    /**
     * Removes a VObjectListener.
     * 
     * @param  listener    The listener.
    **/
    public void removeVObjectListener(VObjectListener vObjec1)
    {
        objectEventSupport_.removeVObjectListener(vObjec1);
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
     * Sets the component to be enabled.
     * 
     * @param  enabled  The value indicating whether the
     *                  component is enable or not.
     **/
    public void setEnabled(boolean enabled)
    {
        enabled_= enabled;
    }
}

