///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemStatusBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;


/**
 * The SystemStatusBeanInfo class provides bean information for the 
 * SystemStatus class.
**/
public class SystemStatusBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private final static Class beanClass_ = SystemStatus.class;
    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;
    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;

    // Static initializer.
    static
    {
        try 
        {
            // Property change events
            EventSetDescriptor changed = new EventSetDescriptor(beanClass_,
                                                               "propertyChange",
                                                               java.beans.PropertyChangeListener.class,
                                                               "propertyChange");

            changed.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
            changed.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));
           
            // Vetoable change events
            EventSetDescriptor veto = new EventSetDescriptor(beanClass_, 
                                                            "vetoableChange",
                                                            java.beans.VetoableChangeListener.class,
                                                            "vetoableChange");

            veto.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
            veto.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

            events_ = new EventSetDescriptor[] { changed, veto };

            
            
            // Properties.
            PropertyDescriptor dtg = new PropertyDescriptor("dateAndTimeStatusGathered", beanClass_, 
                                                            "getDateAndTimeStatusGathered", null);
            dtg.setBound(false);
            dtg.setConstrained(false);
            dtg.setDisplayName(loader_.getText("PROP_NAME_SS_DTG"));
            dtg.setShortDescription(loader_.getText("PROP_DESC_SS_DTG"));

            
            PropertyDescriptor ucso = new PropertyDescriptor("usersCurrentSignedOn", beanClass_, 
                                                             "getUsersCurrentSignedOn", null);
            ucso.setBound(false);
            ucso.setConstrained(false);
            ucso.setDisplayName(loader_.getText("PROP_NAME_SS_UCSO"));
            ucso.setShortDescription(loader_.getText("PROP_DESC_SS_UCSO"));
            
            PropertyDescriptor utso = new PropertyDescriptor("usersTemporarilySignedOff",
                                                             beanClass_, 
                                                             "getUsersTemporarilySignedOff", 
                                                             null);
            utso.setBound(false);
            utso.setConstrained(false);
            utso.setDisplayName(loader_.getText("PROP_NAME_SS_UTSO"));
            utso.setShortDescription(loader_.getText("PROP_DESC_SS_UTSO"));
            
            PropertyDescriptor usbs = new PropertyDescriptor("usersSuspendedBySystemRequest", 
                                                             beanClass_, 
                                                             "getUsersSuspendedBySystemRequest", 
                                                             null);
            usbs.setBound(false);
            usbs.setConstrained(false);
            usbs.setDisplayName(loader_.getText("PROP_NAME_SS_USBS"));
            usbs.setShortDescription(loader_.getText("PROP_DESC_SS_USBS"));

            PropertyDescriptor usowp = new PropertyDescriptor("usersSignedOffWithPrinterOutputWaitingToPrint", 
                                                              beanClass_, 
                                                              "getUsersSignedOffWithPrinterOutputWaitingToPrint", 
                                                              null);
            usowp.setBound(false);
            usowp.setConstrained(false);
            usowp.setDisplayName(loader_.getText("PROP_NAME_SS_USOWP"));
            usowp.setShortDescription(loader_.getText("PROP_DESC_SS_USOWP"));
            
            PropertyDescriptor bjwm = new PropertyDescriptor("batchJobsWaitingForMessage", beanClass_, 
                                                             "getBatchJobsWaitingForMessage", null);
            bjwm.setBound(false);
            bjwm.setConstrained(false);
            bjwm.setDisplayName(loader_.getText("PROP_NAME_SS_BJWM"));
            bjwm.setShortDescription(loader_.getText("PROP_DESC_SS_BJWM"));
            
            PropertyDescriptor bjr = new PropertyDescriptor("batchJobsRunning", beanClass_, 
                                                            "getBatchJobsRunning", null);
            bjr.setBound(false);
            bjr.setConstrained(false);
            bjr.setDisplayName(loader_.getText("PROP_NAME_SS_BJR"));
            bjr.setShortDescription(loader_.getText("PROP_DESC_SS_BJR"));
            
            PropertyDescriptor bjhr = new PropertyDescriptor("batchJobsHeldWhileRunning", beanClass_, 
                                                             "getBatchJobsHeldWhileRunning", null);
            bjhr.setBound(false);
            bjhr.setConstrained(false);
            bjhr.setDisplayName(loader_.getText("PROP_NAME_SS_BJHR"));
            bjhr.setShortDescription(loader_.getText("PROP_DESC_SS_BJHR"));
            
            PropertyDescriptor bje = new PropertyDescriptor("batchJobsEnding", beanClass_, 
                                                            "getBatchJobsEnding", null);  
            bje.setBound(false);
            bje.setConstrained(false);
            bje.setDisplayName(loader_.getText("PROP_NAME_SS_BJE"));
            bje.setShortDescription(loader_.getText("PROP_DESC_SS_BJE"));
            
            PropertyDescriptor bjwr = new PropertyDescriptor("batchJobsWaitingToRunOrAlreadyScheduled", 
                                                             beanClass_, 
                                                             "getBatchJobsWaitingToRunOrAlreadyScheduled", 
                                                             null); 
            bjwr.setBound(false);
            bjwr.setConstrained(false);
            bjwr.setDisplayName(loader_.getText("PROP_NAME_SS_BJWR"));
            bjwr.setShortDescription(loader_.getText("PROP_DESC_SS_BJWR"));
            
            PropertyDescriptor bjh = new PropertyDescriptor("batchJobsHeldOnJobQueue", beanClass_, 
                                                            "getBatchJobsHeldOnJobQueue", null); 
            bjh.setBound(false);
            bjh.setConstrained(false);
            bjh.setDisplayName(loader_.getText("PROP_NAME_SS_BJH"));
            bjh.setShortDescription(loader_.getText("PROP_DESC_SS_BJH"));
            
            PropertyDescriptor bju = new PropertyDescriptor("batchJobsOnUnassignedJobQueue", beanClass_, 
                                                            "getBatchJobsOnUnassignedJobQueue", null); 
            bju.setBound(false);
            bju.setConstrained(false);
            bju.setDisplayName(loader_.getText("PROP_NAME_SS_BJU"));
            bju.setShortDescription(loader_.getText("PROP_DESC_SS_BJU"));
            
            PropertyDescriptor et = new PropertyDescriptor("elapsedTime", beanClass_, 
                                                           "getElapsedTime", null); 
            et.setBound(false);
            et.setConstrained(false);
            et.setDisplayName(loader_.getText("PROP_NAME_SS_ET"));
            et.setShortDescription(loader_.getText("PROP_DESC_SS_ET"));
            
            PropertyDescriptor mus = new PropertyDescriptor("maximumUnprotectedStorageUsed", beanClass_, 
                                                            "getMaximumUnprotectedStorageUsed", null);                            
            mus.setBound(false);
            mus.setConstrained(false);
            mus.setDisplayName(loader_.getText("PROP_NAME_SS_MUS"));
            mus.setShortDescription(loader_.getText("PROP_DESC_SS_MUS"));
            
            PropertyDescriptor ppa = new PropertyDescriptor("percentPermanentAddresses", beanClass_, 
                                                            "getPercentPermanentAddresses", null); 
            ppa.setBound(false);
            ppa.setConstrained(false);
            ppa.setDisplayName(loader_.getText("PROP_NAME_SS_PPA"));
            ppa.setShortDescription(loader_.getText("PROP_DESC_SS_PPA"));
            
            PropertyDescriptor ppu = new PropertyDescriptor("percentProcessingUnitUsed", beanClass_, 
                                                            "getPercentProcessingUnitUsed", null); 
            ppu.setBound(false);
            ppu.setConstrained(false);
            ppu.setDisplayName(loader_.getText("PROP_NAME_SS_PPU"));
            ppu.setShortDescription(loader_.getText("PROP_DESC_SS_PPU"));
            
            PropertyDescriptor sasp = new PropertyDescriptor("percentSystemASPUsed", beanClass_, 
                                                             "getPercentSystemASPUsed", null); 
            sasp.setBound(false);
            sasp.setConstrained(false);
            sasp.setDisplayName(loader_.getText("PROP_NAME_SS_SASP"));
            sasp.setShortDescription(loader_.getText("PROP_DESC_SS_SASP"));
            
            PropertyDescriptor pta = new PropertyDescriptor("percentTemporaryAddresses", beanClass_, 
                                                            "getPercentTemporaryAddresses", null); 
            pta.setBound(false);
            pta.setConstrained(false);
            pta.setDisplayName(loader_.getText("PROP_NAME_SS_PTA"));
            pta.setShortDescription(loader_.getText("PROP_DESC_SS_PTA"));
            
            PropertyDescriptor pn = new PropertyDescriptor("poolsNumber", beanClass_,
                                                           "getPoolsNumber", null); 
            pn.setBound(false);
            pn.setConstrained(false);
            pn.setDisplayName(loader_.getText("PROP_NAME_SS_PN"));
            pn.setShortDescription(loader_.getText("PROP_DESC_SS_PN"));
            
            PropertyDescriptor rsf = new PropertyDescriptor("restrictedStateFlag", beanClass_,
                                                            "getRestrictedStateFlag", null); 
            rsf.setBound(false);
            rsf.setConstrained(false);
            rsf.setDisplayName(loader_.getText("PROP_NAME_SS_RSF"));
            rsf.setShortDescription(loader_.getText("PROP_DESC_SS_RSF"));
            
            PropertyDescriptor system = new PropertyDescriptor("system", beanClass_, 
                                                               "getSystem", "setSystem"); 
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(loader_.getText("PROP_NAME_AS400_SYSTEM"));
            system.setShortDescription(loader_.getText("PROP_DESC_AS400_SYSTEM"));
            
            PropertyDescriptor systemASP = new PropertyDescriptor("systemASP", beanClass_, 
                                                                  "getSystemASP", null);  
            systemASP.setBound(false);
            systemASP.setConstrained(false);
            systemASP.setDisplayName(loader_.getText("PROP_NAME_SS_SYSTEMASP"));
            systemASP.setShortDescription(loader_.getText("PROP_DESC_SS_SYSTEMASP"));
            
            PropertyDescriptor syspool = new PropertyDescriptor("systemPools", beanClass_,
                                                                "getSystemPools", null);  
            syspool.setBound(false);
            syspool.setConstrained(false);
            syspool.setDisplayName(loader_.getText("PROP_NAME_SS_SYSPOOL"));
            syspool.setShortDescription(loader_.getText("PROP_DESC_SS_SYSPOOL"));
            
            PropertyDescriptor tas = new PropertyDescriptor("totalAuxiliaryStorage", beanClass_, 
                                                            "getTotalAuxiliaryStorage", null);  
            tas.setBound(false);
            tas.setConstrained(false);
            tas.setDisplayName(loader_.getText("PROP_NAME_SS_TAS"));
            tas.setShortDescription(loader_.getText("PROP_DESC_SS_TAS"));

            properties_ =  new PropertyDescriptor[]{
                                           bje,
                                           bjh,
                                           bjhr,
                                           bju,
                                           bjr,
                                           bjwm,
                                           bjwr,
                                           dtg,
                                           et,
                                           mus,
                                           ppa,
                                           ppu,
                                           sasp,
                                           pta,
                                           pn,
                                           rsf,
                                           system,
                                           systemASP,
                                           syspool,
                                           tas,
                                           ucso,
                                           usowp,
                                           usbs,
                                           utso
                                           };
        }
        catch(IntrospectionException e) 
        {   e.printStackTrace();
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
        if(icon== BeanInfo.ICON_MONO_16x16) 
        {
            java.awt.Image img = loadImage("SystemStatus16.gif");
            return img;
        }
        else if(icon== BeanInfo.ICON_COLOR_16x16) 
        {
            java.awt.Image img = loadImage("SystemStatus16.gif");
            return img;
        }
        else if(icon == BeanInfo.ICON_MONO_32x32) 
        {
            java.awt.Image img = loadImage("SystemStatus32.gif");
            return img;
        }
        else if(icon == BeanInfo.ICON_COLOR_32x32) 
        {
            java.awt.Image img = loadImage("SystemStatus32.gif");
            return img;
        }        
        return null;
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
