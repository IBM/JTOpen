///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.User;
// @A1A : Added group.
import com.ibm.as400.access.UserGroup;
import com.ibm.as400.access.Trace;
import java.util.Vector;
import java.util.Date;
import java.text.DateFormat;
import java.util.Enumeration;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;



/**
The UserPropertiesPane class represents the properties pane
for a user.
**/
class UserPropertiesPane
implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // MRI.
    private static final String descriptionText_        = ResourceLoader.getText ("USER_DESCRIPTION_PROMPT") + ": ";

    // @A1A
    private static final String generalTabText_            = ResourceLoader.getText ("TAB_GENERAL");
    private static final String sessionStartupTabText_    = ResourceLoader.getText ("TAB_SESSION_STARTUP");
    private static final String displaySessionTabText_    = ResourceLoader.getText ("TAB_DISPLAY_SESSION");
    private static final String outputTabText_            = ResourceLoader.getText ("TAB_OUTPUT");
    private static final String internationalTabText_    = ResourceLoader.getText ("TAB_INTERNATIONAL");
    private static final String otherTabText_            = ResourceLoader.getText ("TAB_OTHER");
    private static final String securityTabText_        = ResourceLoader.getText ("TAB_SECURITY");
    private static final String groupInformationTabText_    = ResourceLoader.getText ("TAB_GROUP_INFORMATION");

    private static final String groupProfileNameText_        = ResourceLoader.getText ("USER_GROUP_PROFILE_NAME")+" : ";
    private static final String groupIDNumberText_            = ResourceLoader.getText ("USER_GROUP_ID_NUMBER")+" : ";
    private static final String groupAuthorityText_            = ResourceLoader.getText ("USER_GROUP_AUTHORITY")+" : ";
    private static final String groupAuthorityTypeText_            = ResourceLoader.getText ("USER_GROUP_AUTHORITY_TYPE")+" : ";
    private static final String groupHasMemberText_            = ResourceLoader.getText ("USER_GROUP_HAS_MEMBER"); //@A2C
    private static final String groupMembersText_            = ResourceLoader.getText ("USER_GROUPS_MEMBERS")+" : ";

    private static final String languageIDText_            = ResourceLoader.getText ("USER_LANGUAGE_ID")+" : ";
    private static final String countryIDText_            = ResourceLoader.getText ("USER_COUNTRY_ID")+" : ";
    private static final String codedCharacterSetIDText_            = ResourceLoader.getText ("USER_CODED_CHARACTER_SET_ID")+" : ";
    private static final String localePathNameText_            = ResourceLoader.getText ("USER_LOCALE_PATH_NAME")+" : ";
    private static final String localeJobAttributesText_            = ResourceLoader.getText ("USER_LOCALE_JOB_ATTRIBUTES")+" : ";
    private static final String customText_            = ResourceLoader.getText ("USER_CUSTOM")+" : ";

    private static final String outputQueueText_                = ResourceLoader.getText ("USER_OUTPUT_QUEUE") + ": ";
//@A2D    private static final String outputQueueLibText_                = ResourceLoader.getText ("USER_OUTPUT_QUEUE_LIB") + ": ";
    private static final String messageQueueText_                = ResourceLoader.getText ("USER_MESSAGE_QUEUE") + ": ";
//@A2D    private static final String messageQueueLibText_            = ResourceLoader.getText ("USER_MESSAGE_QUEUE_LIB") + ": ";
    private static final String printDeviceText_                = ResourceLoader.getText ("USER_PRINT_DEVICE") + ": ";
    private static final String messageDeliveryText_            = ResourceLoader.getText ("USER_MESSAGE_DELIVERY") + ": ";
    private static final String messageSeverityLevelText_        = ResourceLoader.getText ("USER_MESSAGE_SEVERITY_LEVEL") + ": ";
    private static final String ownerText_                        = ResourceLoader.getText ("USER_OWNER") + ": ";
    private static final String objectAuditingValueText_        = ResourceLoader.getText ("USER_OBJECT_AUDITING_VALUE") + ": ";

    private static final String userActionAuditLevelText_        = ResourceLoader.getText ("USER_ACTION_AUDIT_LEVEL") + ": ";
    private static final String specialAuthorityText_            = ResourceLoader.getText ("USER_SPECIAL_AUTHORITY") + ": ";
    private static final String sortSequeneceTableText_            = ResourceLoader.getText ("USER_SORT_SEQUENCE_TABLE") + ": ";

//@A2D    private static final String sortSequeneceTableLibText_        = ResourceLoader.getText ("USER_SORT_SEQUENCE_TABLE_LIB") + ": ";
    private static final String storageUsedText_                = ResourceLoader.getText ("USER_STORAGE_USED") + ": ";
    private static final String supplementalGroupsNumberText_    = ResourceLoader.getText ("USER_SUPPLEMENTAL_GROUPS_NUMBER") + ": ";
    private static final String supplementalGroupsText_            = ResourceLoader.getText ("USER_SUPPLEMENTAL_GROUPS") + ": ";
    private static final String specialEnvironmentText_            = ResourceLoader.getText ("USER_SPECIAL_ENVIRONMENT") + ": ";

    private static final String daysUntilPasswordExpireText_    = ResourceLoader.getText ("USER_DAYS_UNTIL_PASSWORD_EXPIRE")+" : ";
    private static final String isNoPasswordText_                = ResourceLoader.getText ("USER_IS_NO_PASSWORD")+" : ";
    private static final String isPasswordSetExpireText_        = ResourceLoader.getText ("USER_IS_PASSWORD_SET_EXPIRE")+" : ";
    private static final String isWithDigitalCertificatesText_    = ResourceLoader.getText ("USER_IS_WITH_DIGITAL_CERTIFICATES")+" : ";
    private static final String passwordExpireDateText_            = ResourceLoader.getText ("USER_PASSWORD_EXPIRE_DATE")+" : ";
    private static final String passwordExpirationIntervalText_    = ResourceLoader.getText ("USER_PASSWORD_EXPIRATION_INTERVAL")+" : ";
    private static final String passwordLastChangedDateText_    = ResourceLoader.getText ("USER_PASSWORD_LAST_CHANGED_DATE")+" : ";
    private static final String previousSignedOnDateText_        = ResourceLoader.getText ("USER_PREVIOUS_SIGNED_ON_DATE")+" : ";
    private static final String signedOnAttemptsNotValidText_    = ResourceLoader.getText ("USER_SIGNED_ON_ATTEMPTS_NOT_VALID")+" : ";

    private static final String assistanceLevelText_            = ResourceLoader.getText ("USER_ASSISTANCE_LEVEL")+" : ";
//@A2D    private static final String attentionProgramLibText_        = ResourceLoader.getText ("USER_ATTENTION_PROGRAM_LIB")+" : ";
    private static final String attentionProgramNameText_        = ResourceLoader.getText ("USER_ATTENTION_PROGRAM_NAME")+" : ";
    private static final String limitCapabilitiesText_            = ResourceLoader.getText ("USER_LIMIT_CAPABILITIES")+" : ";

    private static final String initialProgramText_                = ResourceLoader.getText ("USER_INITIAL_PROGRAM")+" : ";
//@A2D    private static final String initialProgramLibText_            = ResourceLoader.getText ("USER_INITIAL_PROGRAM_LIB")+" : ";
    private static final String initialMenuText_                = ResourceLoader.getText ("USER_INITIAL_MENU")+" : ";
//@A2D    private static final String initialMenuLibText_                = ResourceLoader.getText ("USER_INITIAL_MENU_LIB")+" : ";
    private static final String displaySignOnInformationText_    = ResourceLoader.getText ("USER_DISPLAY_SIGNON_INFORMATION")+" : ";
    private static final String limitDeviceSessionsText_        = ResourceLoader.getText ("USER_LIMIT_DEVICE_SESSIONS")+" : ";

    private static final String accountingCodeText_                = ResourceLoader.getText ("USER_ACCOUNTING_CODE")+" : ";
    private static final String currentLibText_                    = ResourceLoader.getText ("USER_CURRENT_LIB")+" : ";

    private static final String highestSchedulePriorityText_    = ResourceLoader.getText ("USER_HIGHEST_SCHEDULE_PRIORITY")+" : ";
    private static final String homeDirectoryText_                = ResourceLoader.getText ("USER_HOME_DIRECTORY")+" : ";
    private static final String jobDescriptionNameText_            = ResourceLoader.getText ("USER_JOB_DESCRIPTION_NAME")+" : ";
//@A2D    private static final String jobDescriptionLibText_            = ResourceLoader.getText ("USER_JOB_DESCRIPTION_LIB")+" : ";
    private static final String maximumAllowedStorageText_        = ResourceLoader.getText ("USER_MAXIMUM_ALLOWED_STORAGE")+" : ";
    private static final String statusText_                        = ResourceLoader.getText ("USER_STATUS")+" : ";
    private static final String userClassNameText_                = ResourceLoader.getText ("USER_CLASS_NAME")+" : ";
    private static final String userIDNumberText_                = ResourceLoader.getText ("USER_ID_NUMBER")+" : ";
    private static final String userProfileNameText_            = ResourceLoader.getText ("USER_PROFILE_NAME")+" : ";

    // Static data.
//@D2D    private static  SimpleDateFormat  dateFormat_ = new SimpleDateFormat(ResourceLoader.getText("DATE_FORMAT"));
//@D2D    private static  SimpleDateFormat  timeFormat_ = new SimpleDateFormat(ResourceLoader.getText("TIME_FORMAT"));
    private static final DateFormat dateFormat_ = DateFormat.getDateInstance();  //@D2A
    private static final DateFormat timeFormat_ = DateFormat.getTimeInstance();  //@D2A

    private static final String trueText_    = ResourceLoader.getText ("DLG_TRUE");     //@D1A
    private static final String falseText_   = ResourceLoader.getText ("DLG_FALSE");    //@D1A

    //MRI end
    // @A1A end


    // Private data.
    private VUser               object_;
    private User                user_;

    // Event support.
    private ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);
    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);

    /**
    Constructs an UserPropertiesPane object.

    @param  object      The object.
    @param  user        The user.
    **/
    public UserPropertiesPane (VUser object, User user)
    {
        object_     = object;
        user_       = user;

    }

    /**
    Adds a change listener.

    @param  listener    The listener.
    **/
    public void addChangeListener (ChangeListener listener)
    {
        changeEventSupport_.addChangeListener (listener);
    }

    /**
    Adds a listener to be notified when an error occurs.

    @param  listener    The listener.
    **/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }

    /**
    Adds a listener to be notified when a VObject is changed,
    created, or deleted.

    @param  listener    The listener.
    **/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }

    /**
    Adds a listener to be notified when work in a different thread
    starts and stops.

    @param  listener    The listener.
    **/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }

    /**
    Applies the changes made by the user.

    @throws Exception   If an error occurs.
    **/
    public void applyChanges ()
        throws Exception
    {
        // No changes are allowed.
    }

    /**
    Returns the graphical component.

    @return             The graphical component.
    **/
    public Component getComponent ()
    {
        // @A1C : Uses JTabbedPane instead JPanel
        JTabbedPane tabbedPane = new JTabbedPane ();

        // @A1A : Adds tabs.
        tabbedPane.addTab (generalTabText_, null, getGeneralTab ());

        tabbedPane.addTab (sessionStartupTabText_, null, getSessionStartupTab ());

        tabbedPane.addTab (displaySessionTabText_, null, getDisplaySessionTab ());

        tabbedPane.addTab (outputTabText_, null, getOutputTab ());

        tabbedPane.addTab (internationalTabText_, null, getInternationalTab ());

        tabbedPane.addTab (securityTabText_, null, getSecurityTab ());

        tabbedPane.addTab (groupInformationTabText_, null, getGroupInformationTab ());

        tabbedPane.addTab (otherTabText_, null, getOtherTab ());

        tabbedPane.setSelectedIndex (0);

        return tabbedPane;
    }

    // @A1A
    /**
     *Returns the display session tab component.
     *
     *@return The component for the display session tab.
    **/
    private Component getDisplaySessionTab()
    {
        // Initialize the display session tab.
        JPanel panel = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        panel.setLayout (layout);
        panel.setBorder (new EmptyBorder (10, 10, 10, 10));

        int row = 0;

        String fullPathName    = user_.getAttentionKeyHandlingProgram();

        VUtilities.constrain (attentionProgramNameText_,
            fullPathName,
            panel, layout, row++);


        VUtilities.constrain (limitCapabilitiesText_,
            user_.getLimitCapabilities(),
            panel, layout, row++);

        VUtilities.constrain (assistanceLevelText_,
            user_.getAssistanceLevel(),
            panel, layout, row++);

        return panel;
    }

    // @A1A
    /**
     * Returns the general tab component.
     *
     * @return The component for the general tab.
    **/
    private Component getGeneralTab()
    {

        String fullPathName;

        // Initialize the general tab.
        JPanel panel= new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        panel.setLayout (layout);
        panel.setBorder (new EmptyBorder (10, 10, 10, 10));

        // Icon and name.
        int row = 0;
        VUtilities.constrain (
            new JLabel (userProfileNameText_,object_.getIcon (32, false),  SwingConstants.LEFT),
            new JLabel (object_.getText ()),
            panel, layout, row++);


        VUtilities.constrain (descriptionText_,
            user_.getDescription(),
            panel, layout, row++);

        VUtilities.constrain (userClassNameText_,
            user_.getUserClassName(),
            panel, layout, row++);

        VUtilities.constrain (userIDNumberText_,
            Integer.toString(user_.getUserIDNumber()),
            panel, layout, row++);

        VUtilities.constrain (statusText_,
            user_.getStatus(),
            panel, layout, row++);

        VUtilities.constrain (currentLibText_,
            user_.getCurrentLibraryName(),
            panel, layout, row++);
        String str;
        int maxStorageUsed=user_.getMaximumStorageAllowed();
        if(maxStorageUsed==-1)
           str="*NOMAX";
        else
           str=Integer.toString(maxStorageUsed);
        VUtilities.constrain (maximumAllowedStorageText_,
            str,
            panel, layout, row++);

        // @A3A - moved from other tab
        // Storage used
        VUtilities.constrain (storageUsedText_,                 // @A3A
            Integer.toString(user_.getStorageUsed()),           // @A3A
            panel, layout, row++);                              // @A3A

        VUtilities.constrain (highestSchedulePriorityText_,
            Integer.toString(user_.getHighestSchedulingPriority()),
            panel, layout, row++);

        VUtilities.constrain (accountingCodeText_,
            user_.getAccountingCode(),
            panel, layout, row++);

        fullPathName = user_.getJobDescription();

        VUtilities.constrain (jobDescriptionNameText_,
            fullPathName,
            panel, layout, row++);



        VUtilities.constrain (homeDirectoryText_,
            user_.getHomeDirectory(),
            panel, layout, row++);

        return panel;
    }

    // @A1A
    /**
     * Returns the group information tab component.
     *
     * @return The component for the group information tab.
    **/
    private Component getGroupInformationTab()
    {
        // Initialize the group information tab.
        JPanel panel = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        panel.setLayout (layout);
        panel.setBorder (new EmptyBorder (10, 10, 10, 10));

        int row = 0;

        VUtilities.constrain (groupProfileNameText_,
            user_.getGroupProfileName(),
            panel, layout, row++);

        String grpstr = "";                                  //@A2A
        int grp = user_.getGroupIDNumber();                  //@A2A
        if (grp == 0)                                        //@A2A
          grpstr = "*NONE";                                  //@A2A
        else                                                 //@A2A
          grpstr = ""+grp;                                   //@A2A
        VUtilities.constrain (groupIDNumberText_, grpstr,    //@A2C
            panel, layout, row++);

        VUtilities.constrain (groupAuthorityText_,
            user_.getGroupAuthority(),
            panel, layout, row++);

        VUtilities.constrain (groupAuthorityTypeText_,
            user_.getGroupAuthorityType(),
            panel, layout, row++);

//@A2D        JCheckBox checkBox=new JCheckBox(groupHasMemberText_, user_.isGroupHasMember());
//@A2D        checkBox.setEnabled(false);
//@A2D        VUtilities.constrain (checkBox,
//@A2D            panel, layout, row++);

        String value = falseText_;                            //@D1A
        if (user_.isGroupHasMember())                         //@D1A
           value = trueText_;                                 //@D1A

        VUtilities.constrain (groupHasMemberText_,               //@A2A
            value,                                               //@A2A @D1C
            panel, layout, row++);                               //@A2A

        if(user_.isGroupHasMember())
        {
            Enumeration users;
            Vector userList = new Vector();
            try
            {
                UserGroup userGroup = new UserGroup(user_.getSystem(), user_.getName());    // @E1A
                for (users = userGroup.getMembers();users.hasMoreElements();)               // @E1C
                {
                    String userName=((User)users.nextElement()).getUserProfileName();
                    userList.addElement(userName);
                }
            } catch (Exception err)
            {
                Trace.log(Trace.ERROR,"error when retrieve members : "+err);
                errorEventSupport_.fireError(err);
            }

            JList list = new JList(userList);
            list.setBackground(Color.lightGray);
            VUtilities.constrain (
                new JLabel(groupMembersText_),
                list,
                panel, layout, row++);
        }

        return panel;
    }

    // @A1A
    /**
     * Returns the international tab component.
     *
     * @return The component for the international tab.
    **/
    private Component getInternationalTab()
    {
        // Initialize the international tab.
        JPanel panel = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        panel.setLayout (layout);
        panel.setBorder (new EmptyBorder (10, 10, 10, 10));

        int row = 0;

        VUtilities.constrain (languageIDText_,
            user_.getLanguageID(),
            panel, layout, row++);

        VUtilities.constrain (countryIDText_,
            user_.getCountryID(),
            panel, layout, row++);

        int id=user_.getCCSID();
        String idStr;
        if(id==-2)
            idStr="QCCSID";
        else
            idStr=Integer.toString(id);

        VUtilities.constrain (codedCharacterSetIDText_,
            idStr,
            panel, layout, row++);

        VUtilities.constrain (localePathNameText_,
            user_.getLocalePathName(),
            panel, layout, row++);

        //Local job attributes.
        VUtilities.constrain (new JLabel(localeJobAttributesText_),
            panel, layout, row++);
        String[] localJobAttributes = user_.getLocaleJobAttributes();   // @A3C

        JRadioButton[] radioButton=new JRadioButton[3];
        JCheckBox[] checkBox = new JCheckBox[6];
        boolean[] radio = {false,false,false};
        boolean[] check = {false,false,false,false,false,false};

        if (searchArray (localJobAttributes, "*NONE"))                  // @A3C
        {
            radio[0] = true;
        } else if (searchArray (localJobAttributes, "*SYSVAL"))         // @A3C
        {
            radio[1] = true;
        } else
        {
            radio[2] = true;
        }

        String[] localJobAttributesStr={"*NONE","*SYSVAL","CUSTOM","*CCSID", "*DATFMT","*DATSEP",
                                        "*DECFMT","*SRTSEQ","*TIMSEP"}; // @A3C
        for(int i=0;i<6;i++)
        {
            if (searchArray (localJobAttributes, localJobAttributesStr[i+3]))   // @A3C
                 check[i]=true;
        }
        for(int i=0;i<3;i++)
        {
            radioButton[i]=new JRadioButton(localJobAttributesStr[i],radio[i]);
            radioButton[i].setEnabled(false);
        }

        // @A3C - Removed the "custom" bullet and header to match the green screen.
        for(int i=0;i<2;i++)                                        // @A3C
            VUtilities.constrain (
                radioButton[i],
                panel, layout, 1, row++, 1, 1);
        for(int i=0;i<6;i++)
        {
            checkBox[i]=new JCheckBox(localJobAttributesStr[i+3],check[i]);
            checkBox[i].setEnabled(false);
        }

        // @A3D VUtilities.constrain (new JLabel(customText_),
        // @A3D     panel, layout, 0, row++, 1, 1);

        for(int i=0;i<2;i++)
        {
          for(int j=0;j<3;j++)
          {
             VUtilities.constrain (checkBox[i*3+j],
                  panel, layout, j+1, row, 1, 1);
          }
          row++;
        }

        return panel;
    }

    // @A1A
    /**
     * Returns the other tab component.
     *
     * @return The component for the other tab.
    **/
    private Component getOtherTab()
    {
        // Initializes the other tab.
        JPanel otherTab = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        otherTab.setLayout (layout);
        otherTab.setBorder (new EmptyBorder (10, 10, 10, 10));

        int row = 0;

        // Object owner
        VUtilities.constrain (ownerText_,
            user_.getOwner(),
            otherTab, layout, row++);
        // Object auditing value
        VUtilities.constrain (objectAuditingValueText_,
            user_.getObjectAuditingValue(),
            otherTab, layout, row++);
        // @A3D - moved to general tab
        // @A3D // Storage used
        // @A3D VUtilities.constrain (storageUsedText_,
        // @A3D     Integer.toString(user_.getStorageUsed()),
        // @A3D     otherTab, layout, row++);
        // Special environment
        VUtilities.constrain (specialEnvironmentText_,
            user_.getSpecialEnvironment(),
            otherTab, layout, row++);
        // Sort sequence table
        String str=user_.getSortSequenceTable();

        VUtilities.constrain (sortSequeneceTableText_,
            str,
            otherTab, layout, row++);


        //Supplement groups number
        int number=user_.getSupplementalGroupsNumber();
        String suplgrp = "";                                          //@A2A
        if (number == 0)                                              //@A2A
          suplgrp = "*NONE";                                          //@A2A
        else                                                          //@A2A
          suplgrp = ""+number;                                        //@A2A
        VUtilities.constrain (supplementalGroupsNumberText_, suplgrp, //@A2C
            otherTab, layout, row++);

        //Supplement groups
        if(number>0)
        {
            String[] libl = user_.getSupplementalGroups();
            JList list = new JList(libl);
            JScrollPane scroll = new JScrollPane(list);
            int height = Math.max(1,libl.length);
            scroll.setSize(120,20*height);
            VUtilities.constrain (
                 new JLabel(supplementalGroupsText_),
                 list,
                 otherTab, layout, row++);
        }

        //User action audit level
        String[] levelStr=user_.getUserActionAuditLevel();          // @A3C

        JCheckBox[] levelCheckBox=new JCheckBox[13];
        boolean[] b=new boolean[13];
        for(int i=0;i<13;i++)
            b[i]=false;
        String[] checkStr={"*CMD","*CREATE","*DELETE","*JOBDTA","*OBJMGT",
                           "*OFCSRV","*OPTICAL","*PGMADP","*SAVRST","*SECURITY",
                           "*SERVICE","*SPLFDTA","*SYSMGT"};
        for(int i=0;i<13;i++)
        {
          if(searchArray (levelStr, checkStr[i]))                   // @A3C
             b[i]=true;
        }
        for(int i=0;i<13;i++)
        {
            levelCheckBox[i]=new JCheckBox(checkStr[i],b[i]);
            levelCheckBox[i].setEnabled(false);
        }


        VUtilities.constrain (new JLabel(userActionAuditLevelText_),
            otherTab, layout, 0, row++, 1, 1);

        for(int i=0;i<4;i++)
        {
          for(int j=0;j<3;j++)
          {
             VUtilities.constrain (levelCheckBox[i*3+j],
                  otherTab, layout, j+1, row, 1, 1);
          }
          row++;
        }

        VUtilities.constrain (levelCheckBox[12],
                  otherTab, layout,1, row++,1,1);

        // Special authorities
        String[] autStr=user_.getSpecialAuthority();                    // @A3C

        JCheckBox[] autCheckBox=new JCheckBox[8];

        for(int i=0;i<8;i++)
            b[i]=false;
        String[] autCheckStr={"*ALLOBJ","*AUDIT","*IOSYSCFG","*JOBCTL", // @A3C
                            "*SAVSYS","*SECADM","*SERVICE","*SPLCTL" }; // @A3C
        for(int i=0;i<8;i++)
        {
          if(searchArray (autStr, autCheckStr[i]))                      // @A3C
             b[i]=true;
        }
        for(int i=0;i<8;i++)
        {
            autCheckBox[i]=new JCheckBox(autCheckStr[i],b[i]);
            autCheckBox[i].setEnabled(false);
        }

        VUtilities.constrain (new JLabel(specialAuthorityText_),
            otherTab, layout, 0, row++, 1, 1);

        for(int i=0;i<2;i++)
        {
          for(int j=0;j<3;j++)
          {
             VUtilities.constrain (autCheckBox[i*3+j],
                  otherTab, layout, j+1, row, 1, 1);
          }
          row++;
        }

        VUtilities.constrain (autCheckBox[6],
                  otherTab, layout,1,row,1,1);
        VUtilities.constrain (autCheckBox[7],
                  otherTab, layout,2,row++,1,1);
        return otherTab;
    }

    // @A1A
     /**
     * Returns the output tab component.
     *
     * @return The component for the output tab.
    **/
    private Component getOutputTab ()
    {
        // Initializes the active tab.
        JPanel outputTab = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        outputTab.setLayout (layout);
        outputTab.setBorder (new EmptyBorder (10, 10, 10, 10));

        int row = 0;

        String fullPathName;


        // Print device name
        VUtilities.constrain (printDeviceText_,
            user_.getPrintDevice(),
            outputTab, layout, row++);

        // Output queue ane output queue priority
        fullPathName = user_.getOutputQueue();

        VUtilities.constrain (outputQueueText_,
            fullPathName,
            outputTab, layout, row++);



        fullPathName=user_.getMessageQueue();

        VUtilities.constrain (messageQueueText_,
            fullPathName,
            outputTab, layout, row++);



        VUtilities.constrain (messageDeliveryText_,
            user_.getMessageQueueDeliveryMethod(),
            outputTab, layout, row++);

        VUtilities.constrain (messageSeverityLevelText_,
            Integer.toString(user_.getMessageQueueSeverity()),
            outputTab, layout, row++);


        return outputTab;
    }

    // @A1A
    /**
     *Returns the security tab component.
     *
     *@return The component for the security tab.
     **/
    private Component getSecurityTab()
    {
        // Initialize the security tab.
        JPanel panel = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        panel.setLayout (layout);
        panel.setBorder (new EmptyBorder (10, 10, 10, 10));

        int row = 0;

        String pwdexpitv = "";                              //@A2A
        int days = user_.getPasswordExpirationInterval();   //@A2A
        if (days == 0)                                      //@A2A
          pwdexpitv = "*SYSVAL";                            //@A2A
        else if (days == -1)                                //@A2A
          pwdexpitv = "*NOMAX";                             //@A2A
        else                                                //@A2A
          pwdexpitv = ""+days;                              //@A2A
        VUtilities.constrain (passwordExpirationIntervalText_, pwdexpitv, //@A2C
            panel, layout, row++);

        Date date = user_.getPasswordExpireDate();
        String dateTimeString = "NONE";
        if (date!=null)
        {
            dateTimeString = dateFormat_.format(date)+" "+timeFormat_.format(date);

        }
        VUtilities.constrain (passwordExpireDateText_,
            dateTimeString,
            panel, layout, row++);

        VUtilities.constrain (daysUntilPasswordExpireText_,
            Integer.toString(user_.getDaysUntilPasswordExpire()),
            panel, layout, row++);

        date = user_.getPasswordLastChangedDate();
        if (date!=null)
        {
            dateTimeString = dateFormat_.format(date)+" "+timeFormat_.format(date);
        }
        else
        {
            dateTimeString = "NONE";
        }
        VUtilities.constrain (passwordLastChangedDateText_,
            dateTimeString,
            panel, layout, row++);

        date = user_.getPreviousSignedOnDate();
        if (date!=null)
        {
            dateTimeString = dateFormat_.format(date)+" "+timeFormat_.format(date);
        }
        else
        {
            dateTimeString = "NONE";
        }
        VUtilities.constrain (previousSignedOnDateText_,
            dateTimeString,
            panel, layout, row++);

        VUtilities.constrain (signedOnAttemptsNotValidText_,
            Integer.toString(user_.getSignedOnAttemptsNotValid()),
            panel, layout, row++);

        String value = falseText_;                            //@D1A
        if (user_.isNoPassword())                             //@D1A
           value = trueText_;                                 //@D1A

        VUtilities.constrain (isNoPasswordText_,
            value,                                            //@D1C
            panel, layout, row++);


        value = falseText_;                                   //@D1A
        if (user_.isPasswordSetExpire())                      //@D1A
           value = trueText_;                                 //@D1A

        VUtilities.constrain (isPasswordSetExpireText_,
            value,                                            //@D1A
            panel, layout, row++);


        value = falseText_;                                   //@D1A
        if (user_.isWithDigitalCertificates())                //@D1A
           value = trueText_;                                 //@D1A

        VUtilities.constrain (isWithDigitalCertificatesText_,
            value,
            panel, layout, row++);

        return panel;
    }

    // @A1A
    /**
     * Returns the sessionStartup tab component.
     *
     * @return The component for the sessionStartup tab.
    **/
    private Component getSessionStartupTab ()
    {
        // Initializes the active tab.
        JPanel sessionStartupTab = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        sessionStartupTab.setLayout (layout);
        sessionStartupTab.setBorder (new EmptyBorder (10, 10, 10, 10));

        int row = 0;

        String string;


        // Initial program.
        string=user_.getInitialProgram();


        VUtilities.constrain (initialProgramText_,
            string,
            sessionStartupTab, layout, row++);


        string=user_.getInitialMenu();

        VUtilities.constrain (initialMenuText_,
            string,
            sessionStartupTab, layout, row++);

        VUtilities.constrain (displaySignOnInformationText_,
            user_.getDisplaySignOnInformation(),
            sessionStartupTab, layout, row++);

        VUtilities.constrain (limitDeviceSessionsText_,
            user_.getLimitDeviceSessions(),
            sessionStartupTab, layout, row++);


        return sessionStartupTab;
    }

    /**
    Removes a change listener.

    @param  listener    The listener.
    **/
    public void removeChangeListener (ChangeListener listener)
    {
        changeEventSupport_.removeChangeListener (listener);
    }



    /**
    Removes a listener to be notified when an error occurs.

    @param  listener    The listener.
    **/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



    /**
    Removes a listener to be notified when a VObject is changed,
    created, or deleted.

    @param  listener    The listener.
    **/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }



    /**
    Removes a listener to be notified when work in a different thread
    starts and stops.

    @param  listener    The listener.
    **/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



    // @A3A
    private boolean searchArray (String[] array, String check)
    {
        for (int i = 0; i < array.length; ++i)
            if (array[i].equalsIgnoreCase (check))
                return true;
        return false;
    }


}

