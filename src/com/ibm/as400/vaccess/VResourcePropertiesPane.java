///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VResourcePropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import com.ibm.as400.resource.Resource;
import java.awt.Component;
import javax.swing.event.ChangeListener;


class VResourcePropertiesPane implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private VObject             object_;
    private ResourceProperties  properties_;
    private Resource            resource_;
    private ResourcePropertiesTabbedPane tabbedPane_;

    // Event support.
    private ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);
    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);


    
    public VResourcePropertiesPane (VObject object, Resource resource, ResourceProperties properties)
    {
        object_ = object;
        resource_ = resource;
        properties_ = properties;
        tabbedPane_ = null; 
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
        tabbedPane_.applyChanges();
    }



    /**
     * Returns the graphical component.
     *
     * @return The graphical component.
    **/
    public Component getComponent ()
    {
        workingEventSupport_.fireStartWorking();
        if (tabbedPane_ == null) {
            tabbedPane_ = new ResourcePropertiesTabbedPane(resource_, properties_);
            tabbedPane_.addErrorListener(errorEventSupport_);
            tabbedPane_.addChangeListener(changeEventSupport_);
        }
        workingEventSupport_.fireStopWorking();
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
