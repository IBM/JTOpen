///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileViewerBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;



/**
The SpooledFileViewerBeanInfo class provides bean information
for the SpooledFileViewer class.

@see SpooledFileViewer
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/


public class SpooledFileViewerBeanInfo
extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private final static Class              beanClass_   = SpooledFileViewer.class;
    private static EventSetDescriptor[]     events_;
    private static PropertyDescriptor[]     properties_;



/**
Static initializer.
**/
    static
    {
        try {

            // Events.
            EventSetDescriptor error = new EventSetDescriptor (beanClass_,
                                                               "error",
                                                               ErrorListener.class,
                                                               "errorOccurred");
            error.setDisplayName (ResourceLoader.getText ("EVT_NAME_ERROR"));
            error.setShortDescription (ResourceLoader.getText ("EVT_DESC_ERROR"));

            EventSetDescriptor propertyChange = new EventSetDescriptor (beanClass_,
                                                                        "propertyChange",
                                                                        PropertyChangeListener.class,
                                                                        "propertyChange");
            propertyChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_CHANGE"));
            propertyChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor vetoableChange = new EventSetDescriptor (beanClass_,
            		"propertyChange",
                                                                        VetoableChangeListener.class,
                                                                        "vetoableChange");
            vetoableChange.setDisplayName (ResourceLoader.getText ("EVT_NAME_PROPERTY_VETO"));
            vetoableChange.setShortDescription (ResourceLoader.getText ("EVT_DESC_PROPERTY_VETO"));

            String[] workingMethods = { "startWorking", "stopWorking" };
            EventSetDescriptor working = new EventSetDescriptor (beanClass_,
                                                                 "working",
                                                                 WorkingListener.class,
                                                                 workingMethods,
                                                                 "addWorkingListener",
                                                                 "removeWorkingListener");
            working.setDisplayName (ResourceLoader.getText ("EVT_NAME_WORKING"));
            working.setShortDescription (ResourceLoader.getText ("EVT_DESC_WORKING"));

            events_ = new EventSetDescriptor[] { error, propertyChange, vetoableChange, working };


            // Properties.
            PropertyDescriptor currentPage = new PropertyDescriptor ("currentPage",
                                                                     beanClass_,
                                                                     "getCurrentPage",
                                                                     "setCurrentPage");
            currentPage.setBound (true);
            currentPage.setConstrained (true);
            currentPage.setDisplayName (ResourceLoader.getPrintText ("PROP_NAME_CURRENT_PAGE"));
            currentPage.setShortDescription (ResourceLoader.getPrintText ("PROP_DESC_CURRENT_PAGE"));

            PropertyDescriptor numberOfPages = new PropertyDescriptor ("numberOfPages",
                                                                     beanClass_,
                                                                     "getNumberOfPages",
                                                                     null);
            numberOfPages.setBound (true);
            numberOfPages.setConstrained (true);
            numberOfPages.setDisplayName (ResourceLoader.getPrintText ("PROP_NAME_NUMBER_OF_PAGES"));
            numberOfPages.setShortDescription (ResourceLoader.getPrintText ("PROP_DESC_NUMBER_OF_PAGES"));

            PropertyDescriptor numberOfPagesEstimated = new PropertyDescriptor ("numberOfPagesEstimated",
                                                                            beanClass_,
                                                                            "isNumberOfPagesEstimated",
                                                                            null);
            numberOfPagesEstimated.setBound (true);
            numberOfPagesEstimated.setConstrained (true);
            numberOfPagesEstimated.setDisplayName (ResourceLoader.getPrintText ("PROP_NAME_NUMBER_OF_PAGES_ESTIMATED"));
            numberOfPagesEstimated.setShortDescription (ResourceLoader.getPrintText ("PROP_DESC_NUMBER_OF_PAGES_ESTIMATED"));

            PropertyDescriptor paperSize = new PropertyDescriptor ("paperSize",
                                                                   beanClass_,
                                                                   "getPaperSize",
                                                                   "setPaperSize");
            paperSize.setBound (true);
            paperSize.setConstrained (true);
            paperSize.setDisplayName (ResourceLoader.getPrintText ("PROP_NAME_PAPER_SIZE"));
            paperSize.setShortDescription (ResourceLoader.getPrintText ("PROP_DESC_PAPER_SIZE"));

            PropertyDescriptor spooledFile = new PropertyDescriptor ("spooledFile",
                                                                     beanClass_,
                                                                     "getSpooledFile",
                                                                     "setSpooledFile");
            spooledFile.setBound (true);
            spooledFile.setConstrained (true);
            spooledFile.setDisplayName (ResourceLoader.getPrintText ("PROP_NAME_SPLF"));
            spooledFile.setShortDescription (ResourceLoader.getPrintText ("PROP_DESC_SPLF"));

            PropertyDescriptor viewingFidelity = new PropertyDescriptor ("viewingFidelity",
                                                                         beanClass_,
                                                                         "getViewingFidelity",
                                                                         "setViewingFidelity");
            viewingFidelity.setBound (true);
            viewingFidelity.setConstrained (true);
            viewingFidelity.setDisplayName (ResourceLoader.getPrintText ("PROP_NAME_VIEWING_FIDELITY"));
            viewingFidelity.setShortDescription (ResourceLoader.getPrintText ("PROP_DESC_VIEWING_FIDELITY"));

            properties_ = new PropertyDescriptor[] { currentPage,
                                                     numberOfPages,
                                                     numberOfPagesEstimated,
                                                     paperSize,
                                                     spooledFile,
                                                     viewingFidelity };
        }
        catch (Exception e) {
            throw new Error (e.toString ());
        }
    }



/**
Returns the bean descriptor.

@return The bean descriptor.
**/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor (beanClass_);
    }



/**
Returns the index of the default event.

@return The index of the default event.
**/
    public int getDefaultEventIndex()
    {
        return 0; // error.
    }



/**
Returns the index of the default property.

@return The index of the default property.
**/
    public int getDefaultPropertyIndex()
    {
        return 0; // currentPage.
    }



/**
Returns the descriptors for all events.

@return The descriptors for all events.
**/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return events_;
    }



/**
Returns an image for the icon.

@param icon    The icon size and color.
@return        The image.
**/
    public Image getIcon(int icon)
    {
        Image image = null;
        switch (icon) {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage ("SpooledFileViewer16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("SpooledFileViewer32.gif");
                break;
        }
        return image;
    }



/**
Returns the descriptors for all properties.

@return The descriptors for all properties.
**/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }

}
