///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VResourceListPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import com.ibm.as400.resource.ResourceList;
import java.awt.Component;
import javax.swing.event.ChangeListener;


class VResourceListPropertiesPane implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private VObject             object_;
    private ResourceList        resourceList_;
    private ResourceListPropertiesTabbedPane tabbedPane_;

    // Event support.
    private ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);
    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);


    
    public VResourceListPropertiesPane (VObject object, ResourceList resourceList)
    {
        object_ = object;
        tabbedPane_ = null; 
        resourceList_ = resourceList;
    }



    public void addChangeListener (ChangeListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      changeEventSupport_.addChangeListener (listener);
    }

    

    public void addErrorListener (ErrorListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      errorEventSupport_.addErrorListener (listener);
    }

    
    public void addVObjectListener (VObjectListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      objectEventSupport_.addVObjectListener (listener);
    }


    public void addWorkingListener (WorkingListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      workingEventSupport_.addWorkingListener (listener);
    }

    

    public void applyChanges ()
        throws Exception
    {
        tabbedPane_.applyChanges(false); // @A1C
        object_.load();
    }



    /**
     * Returns the graphical component.
     *
     * @return The graphical component.
    **/
    public Component getComponent ()
    {
        if (tabbedPane_ == null) {
            tabbedPane_ = new ResourceListPropertiesTabbedPane(resourceList_);
            tabbedPane_.addErrorListener(errorEventSupport_);
            tabbedPane_.addChangeListener(changeEventSupport_);
        }
        return tabbedPane_;
    }



    /**
     * Removes a change listener.
     *
     * @param  listener    The listener.
    **/
    public void removeChangeListener (ChangeListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      changeEventSupport_.removeChangeListener (listener);
    }

    /**
     * Removes a listener to be notified when an error occurs.
     *
     * @param  listener    The listener.
    **/
    public void removeErrorListener (ErrorListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      errorEventSupport_.removeErrorListener (listener);
    }

    /**
     * Removes a listener to be notified when a VObject is changed,
     * created, or deleted.
     *
     * @param  listener    The listener.
    **/
    public void removeVObjectListener (VObjectListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      objectEventSupport_.removeVObjectListener (listener);
    }

    /**
     * Removes a listener to be notified when work in a different thread
     * starts and stops.
     *
     * @param  listener    The listener.
    **/
    public void removeWorkingListener (WorkingListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
      workingEventSupport_.removeWorkingListener (listener);
    }

}
