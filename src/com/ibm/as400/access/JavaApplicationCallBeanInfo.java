///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JavaApplicationCallBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.lang.reflect.Method;
import java.beans.*;
import com.ibm.as400.access.*;

/**
 * The JavaApplicationCallBeanInfo class provides bean information for the
 * JavaApplicationCall class.
**/
public class JavaApplicationCallBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private final static Class beanClass_ = JavaApplicationCall.class;
    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;
    private static ResourceBundleLoader rbl_;

    // Static initializer.
    static
    {
        try
        {
            // Events.
            EventSetDescriptor event1 =
              new EventSetDescriptor(beanClass_, "propertyChange",
                               PropertyChangeListener.class,
                               "propertyChange");
            event1.setDisplayName(rbl_.getText("EVT_NAME_PROPERTY_CHANGE"));
            event1.setShortDescription(rbl_.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor event2 = new EventSetDescriptor(beanClass_,
                         "actionCompleted",
                         ActionCompletedListener.class,
                         "actionCompleted");
            event2.setDisplayName(rbl_.getText("EVT_NAME_ACTION_COMPLETED"));
            event2.setShortDescription(rbl_.getText("EVT_DESC_ACTION_COMPLETED"));

            EventSetDescriptor event3 =
              new EventSetDescriptor(beanClass_, "vetoableChange",
                               VetoableChangeListener.class,
                               "vetoableChange");
            event3.setDisplayName(rbl_.getText("EVT_NAME_PROPERTY_VETO"));
            event3.setShortDescription(rbl_.getText("EVT_DESC_PROPERTY_VETO"));


            events_ = new EventSetDescriptor[] {event1, event2, event3};


            // Properties.
            PropertyDescriptor classPath =
                new PropertyDescriptor("classPath", beanClass_,
                                       "getClassPath", "setClassPath");
            classPath.setBound(true);
            classPath.setConstrained(true);
            classPath.setDisplayName(rbl_.getText("PROP_NAME_JAC_CLASSPATH"));
            classPath.setShortDescription(rbl_.getText("PROP_DESC_JAC_CLASSPATH"));



            PropertyDescriptor classPathSecurityChkLvl =
                new PropertyDescriptor("securityCheckLevel", beanClass_,
                                       "getSecurityCheckLevel", "setSecurityCheckLevel");
            classPathSecurityChkLvl.setBound(true);
            classPathSecurityChkLvl.setConstrained(true);
            classPathSecurityChkLvl.setDisplayName(rbl_.getText("PROP_NAME_JAC_SECCHKLVL"));
            classPathSecurityChkLvl.setShortDescription(rbl_.getText("PROP_DESC_JAC_SECCHKLVL"));



            PropertyDescriptor garbageCollectInitialSize =
                new PropertyDescriptor("garbageCollectionInitialSize", beanClass_,
                                       "getGarbageCollectionInitialSize", "setGarbageCollectionInitialSize");
            garbageCollectInitialSize.setBound(true);
            garbageCollectInitialSize.setConstrained(true);
            garbageCollectInitialSize.setDisplayName(rbl_.getText("PROP_NAME_JAC_GCINIT"));
            garbageCollectInitialSize.setShortDescription(rbl_.getText("PROP_DESC_JAC_GCINIT"));



            PropertyDescriptor garbageCollectMaximumSize =
                new PropertyDescriptor("garbageCollectionMaximumSize", beanClass_,
                                       "getGarbageCollectionMaximumSize", "setGarbageCollectionMaximumSize");
            garbageCollectMaximumSize.setBound(true);
            garbageCollectMaximumSize.setConstrained(true);
            garbageCollectMaximumSize.setDisplayName(rbl_.getText("PROP_NAME_JAC_GCMAX"));
            garbageCollectMaximumSize.setShortDescription(rbl_.getText("PROP_DESC_JAC_GCMAX"));



            PropertyDescriptor garbageCollectionFrequency =
                new PropertyDescriptor("garbageCollectionFrequency", beanClass_,
                                       "getGarbageCollectionFrequency", "setGarbageCollectionFrequency");
            garbageCollectionFrequency.setBound(true);
            garbageCollectionFrequency.setConstrained(true);
            garbageCollectionFrequency.setDisplayName(rbl_.getText("PROP_NAME_JAC_GCFREQ"));
            garbageCollectionFrequency.setShortDescription(rbl_.getText("PROP_DESC_JAC_GCFREQ"));



            PropertyDescriptor garbageCollectionPriority =
                new PropertyDescriptor("garbageCollectionPriority", beanClass_,
                                       "getGarbageCollectionPriority", "setGarbageCollectionPriority");
            garbageCollectionPriority.setBound(true);
            garbageCollectionPriority.setConstrained(true);
            garbageCollectionPriority.setDisplayName(rbl_.getText("PROP_NAME_JAC_GCPRIORITY"));
            garbageCollectionPriority.setShortDescription(rbl_.getText("PROP_DESC_JAC_GCPRIORITY"));



            PropertyDescriptor interpret =
                new PropertyDescriptor("interpret", beanClass_,
                                       "getInterpret", "setInterpret");
            interpret.setBound(true);
            interpret.setConstrained(true);
            interpret.setDisplayName(rbl_.getText("PROP_NAME_JAC_INTERPRET"));
            interpret.setShortDescription(rbl_.getText("PROP_DESC_JAC_INTERPRET"));



            PropertyDescriptor javaApplication =
                new PropertyDescriptor("javaApplication", beanClass_,
                                       "getJavaApplication", "setJavaApplication");
            javaApplication.setBound(true);
            javaApplication.setConstrained(true);
            javaApplication.setDisplayName(rbl_.getText("PROP_NAME_JAC_JAVAAPP"));
            javaApplication.setShortDescription(rbl_.getText("PROP_DESC_JAC_JAVAAPP"));



            PropertyDescriptor optimization =
                new PropertyDescriptor("optimization", beanClass_,
                                       "getOptimization", "setOptimization");
            optimization.setBound(true);
            optimization.setConstrained(true);
            optimization.setDisplayName(rbl_.getText("PROP_NAME_JAC_OPTIMIZE"));
            optimization.setShortDescription(rbl_.getText("PROP_DESC_JAC_OPTIMIZE"));



            PropertyDescriptor option =
                new PropertyDescriptor("option", beanClass_,
                                       "getOptions", "setOptions");
            option.setBound(true);
            option.setConstrained(true);
            option.setDisplayName(rbl_.getText("PROP_NAME_JAC_OPTION"));
            option.setShortDescription(rbl_.getText("PROP_DESC_JAC_OPTION"));



            PropertyDescriptor parameters =
                new PropertyDescriptor("parameters", beanClass_,
                                       "getParameters","setParameters");
            parameters.setBound(true);
            parameters.setConstrained(true);
            parameters.setDisplayName(rbl_.getText("PROP_NAME_JAC_PARAMETERS"));
            parameters.setShortDescription(rbl_.getText("PROP_DESC_JAC_PARAMETERS"));



            PropertyDescriptor portSearch =
                new PropertyDescriptor("findPort", beanClass_,
                                       "isFindPort", "setFindPort");
            portSearch.setBound(true);
            portSearch.setConstrained(true);
            portSearch.setDisplayName(rbl_.getText("PROP_NAME_JAC_PORTSEARCH"));
            portSearch.setShortDescription(rbl_.getText("PROP_DESC_JAC_PORTSEARCH"));

            properties_ =  new PropertyDescriptor[]{
                                           classPath,
                                           classPathSecurityChkLvl,
                                           garbageCollectInitialSize,
                                           garbageCollectMaximumSize,
                                           garbageCollectionFrequency,
                                           garbageCollectionPriority,
                                           interpret,
                                           javaApplication,
                                           parameters,
                                           optimization,
                                           option,
                                           portSearch,
                                           };
        }
        catch(IntrospectionException e)
        {
            throw new Error(e.toString());
        }
    }
    /**
     * Returns the bean descriptor.
     *
     * @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor ()
    {
        return new BeanDescriptor (beanClass_);
    }

    /**
     * Copyright.
    **/
    private static String getCopyright()
    {
        return Copyright.copyright;
    }

    /**
     * Returns the default event index.
     * @return The default event index (always 1).
     **/
    public int getDefaultEventIndex()
    {
        return 1;
    }

    /**
     * Returns the default property index.
     * @return The default property index (always 0).
     **/
    public int getDefaultPropertyIndex()
    {
        return 0;
    }

    /**
     * Returns the descriptors for all events.
     * @return The descriptors for all events.
     **/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return events_;
    }

    /**
     * Returns an Image for this bean's icon.
     * @param icon The desired icon size and color.
     * @return The Image for the icon.
     */
    public Image getIcon(int icon)
    {
       Image image = null;

       switch(icon)
       {
         case BeanInfo.ICON_MONO_16x16:
         case BeanInfo.ICON_COLOR_16x16:
         image = loadImage("JavaApplicationCall16.gif");
         break;

         case BeanInfo.ICON_MONO_32x32:
         case BeanInfo.ICON_COLOR_32x32:
         image = loadImage("JavaApplicationCall32.gif");
         break;
       }

       return image;
    }


    /**
     * Returns the descriptors for all properties.
     * @return The descriptors for all properties.
     **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }

}

