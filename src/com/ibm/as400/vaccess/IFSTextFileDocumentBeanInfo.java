///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSTextFileDocumentBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.FileListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;



/**
The IFSTextFileDocumentBeanInfo class provides bean information
for the IFSTextFileDocument class.

@see IFSTextFileDocument
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class IFSTextFileDocumentBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private final static Class              beanClass_      = IFSTextFileDocument.class;
    private static EventSetDescriptor[]     events_;
    private static PropertyDescriptor[]     properties_;



/**
Static initializer.
**/
    static
    {
        try {

            // Events.
            String[] documentMethods = { "changedUpdate", "insertUpdate", "removeUpdate" };
            EventSetDescriptor document = new EventSetDescriptor (beanClass_,
                "document", DocumentListener.class, documentMethods,
                "addDocumentListener", "removeDocumentListener");
            document.setDisplayName (ResourceLoader.getText ("EVT_NAME_DOCUMENT"));
            document.setShortDescription (ResourceLoader.getText ("EVT_DESC_DOCUMENT"));

            EventSetDescriptor error = new EventSetDescriptor (beanClass_,
                "error", ErrorListener.class, "errorOccurred");
            error.setDisplayName (ResourceLoader.getText ("EVT_NAME_ERROR"));
            error.setShortDescription (ResourceLoader.getText ("EVT_DESC_ERROR"));

            String[] fileMethods = { "fileClosed", "fileCreated", "fileDeleted",
                "fileModified", "fileOpened" };
            EventSetDescriptor file = new EventSetDescriptor (beanClass_,
                "file", FileListener.class, fileMethods,
                "addFileListener", "removeFileListener");
            file.setDisplayName (ResourceLoader.getText ("EVT_NAME_FILE"));
            file.setShortDescription (ResourceLoader.getText ("EVT_DESC_FILE"));

            EventSetDescriptor propertyChange = new EventSetDescriptor (beanClass_,
                "propertyChange", PropertyChangeListener.class, "propertyChange");
            propertyChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor undoableEdit = new EventSetDescriptor (beanClass_,
                "undoableEdit", UndoableEditListener.class, "undoableEditHappened");
            undoableEdit.setDisplayName (ResourceLoader.getText ("EVT_NAME_UNDOABLE_EDIT"));
            undoableEdit.setShortDescription (ResourceLoader.getText ("EVT_DESC_UNDOABLE_EDIT"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor (beanClass_,
            		"propertyChange", VetoableChangeListener.class, "vetoableChange");
            vetoableChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_VETO"));

            String[] workingMethods = { "startWorking", "stopWorking" };
            EventSetDescriptor working = new EventSetDescriptor (beanClass_,
                "working", WorkingListener.class, workingMethods,
                "addWorkingListener", "removeWorkingListener");
            working.setDisplayName (ResourceLoader.getText ("EVT_NAME_WORKING"));
            working.setShortDescription (ResourceLoader.getText ("EVT_DESC_WORKING"));

            events_ = new EventSetDescriptor[] { document, error, file, propertyChange,
                undoableEdit, vetoableChange, working };

            // Properties.
            PropertyDescriptor path = new PropertyDescriptor ("path", beanClass_);
            path.setBound (true);
            path.setConstrained (true);
            path.setDisplayName (ResourceLoader.getText ("PROP_NAME_PATH"));
            path.setShortDescription (ResourceLoader.getText ("PROP_DESC_PATH"));

            PropertyDescriptor system = new PropertyDescriptor ("system", beanClass_);
            system.setBound (true);
            system.setConstrained (true);
            system.setDisplayName (ResourceLoader.getText ("PROP_NAME_SYSTEM"));
            system.setShortDescription (ResourceLoader.getText ("PROP_DESC_SYSTEM"));

            properties_ = new PropertyDescriptor[] { path, system };
        }
        catch (Exception e) {
            throw new Error (e.toString ());
        }
    }



/**
Returns the bean descriptor.

@return The bean descriptor.
**/
    public BeanDescriptor getBeanDescriptor ()
    {
        return new BeanDescriptor (beanClass_);
    }



/**
Returns the index of the default event.

@return The index of the default event.
**/
    public int getDefaultEventIndex ()
    {
        return 1; // ErrorEvent.
    }



/**
Returns the index of the default property.

@return The index of the default property.
**/
    public int getDefaultPropertyIndex ()
    {
        return 0; // path.
    }



/**
Returns the descriptors for all events.

@return The descriptors for all events.
**/
    public EventSetDescriptor[] getEventSetDescriptors ()
    {
        return events_;
    }



/**
Returns an image for the icon.

@param icon    The icon size and color.
@return        The image.
**/
    public Image getIcon (int icon)
    {
        Image image = null;
        switch (icon) {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage ("IFSTextFileDocument16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("IFSTextFileDocument32.gif");
                break;
        }
        return image;
    }



/**
Returns the descriptors for all properties.

@return The descriptors for all properties.
**/
    public PropertyDescriptor[] getPropertyDescriptors ()
    {
        return properties_;
    }



}


