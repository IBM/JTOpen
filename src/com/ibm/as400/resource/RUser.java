///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RUser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ExtendedIllegalArgumentException; //@B1A
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;
import java.io.IOException;                                                         // @A3A
import java.net.InetAddress;                                                        // @A3A
import java.util.Date;



/**
The RUser class represents an OS/400 user profile and directory entry.

<a name="attributeIDs"><p>The following attribute IDs are supported:
<ul>
<li><a href="#ACCOUNTING_CODE">ACCOUNTING_CODE</a>
<li><a href="#ALLOW_SYNCHRONIZATION">ALLOW_SYNCHRONIZATION</a>
<li><a href="#ASSISTANCE_LEVEL">ASSISTANCE_LEVEL</a>
<li><a href="#ATTENTION_KEY_HANDLING_PROGRAM">ATTENTION_KEY_HANDLING_PROGRAM</a>
<li><a href="#BUILDING">BUILDING</a>
<li><a href="#CC_MAIL_ADDRESS">CC_MAIL_ADDRESS</a>
<li><a href="#CC_MAIL_COMMENT">CC_MAIL_COMMENT</a>
<li><a href="#CHARACTER_CODE_SET_ID">CHARACTER_CODE_SET_ID</a>
<li><a href="#CHARACTER_IDENTIFIER_CONTROL">CHARACTER_IDENTIFIER_CONTROL</a>
<li><a href="#COMPANY">COMPANY</a>
<li><a href="#COUNTRY_ID">COUNTRY_ID</a>
<li><a href="#CURRENT_LIBRARY_NAME">CURRENT_LIBRARY_NAME</a>
<li><a href="#DATE_PASSWORD_EXPIRES">DATE_PASSWORD_EXPIRES</a>
<li><a href="#DAYS_UNTIL_PASSWORD_EXPIRES">DAYS_UNTIL_PASSWORD_EXPIRES</a>
<li><a href="#DEPARTMENT">DEPARTMENT</a>
<li><a href="#DIGITAL_CERTIFICATE_INDICATOR">DIGITAL_CERTIFICATE_INDICATOR</a>
<li><a href="#DISPLAY_SIGN_ON_INFORMATION">DISPLAY_SIGN_ON_INFORMATION</a>
<li><a href="#FAX_TELEPHONE_NUMBER">FAX_TELEPHONE_NUMBER</a>
<li><a href="#FIRST_NAME">FIRST_NAME</a>
<li><a href="#FULL_NAME">FULL_NAME</a>
<li><a href="#GROUP_AUTHORITY">GROUP_AUTHORITY</a>
<li><a href="#GROUP_AUTHORITY_TYPE">GROUP_AUTHORITY_TYPE</a>
<li><a href="#GROUP_ID_NUMBER">GROUP_ID_NUMBER</a>
<li><a href="#GROUP_MEMBER_INDICATOR">GROUP_MEMBER_INDICATOR</a>
<li><a href="#GROUP_PROFILE_NAME">GROUP_PROFILE_NAME</a>
<li><a href="#HIGHEST_SCHEDULING_PRIORITY">HIGHEST_SCHEDULING_PRIORITY</a>
<li><a href="#HOME_DIRECTORY">HOME_DIRECTORY</a>
<li><a href="#INDIRECT_USER">INDIRECT_USER</a>
<li><a href="#INITIAL_MENU">INITIAL_MENU</a>
<li><a href="#INITIAL_PROGRAM">INITIAL_PROGRAM</a>
<li><a href="#JOB_DESCRIPTION">JOB_DESCRIPTION</a>
<li><a href="#JOB_TITLE">JOB_TITLE</a>
<li><a href="#KEYBOARD_BUFFERING">KEYBOARD_BUFFERING</a>
<li><a href="#LANGUAGE_ID">LANGUAGE_ID</a>
<li><a href="#LAST_NAME">LAST_NAME</a>
<li><a href="#LIMIT_CAPABILITIES">LIMIT_CAPABILITIES</a>
<li><a href="#LIMIT_DEVICE_SESSIONS">LIMIT_DEVICE_SESSIONS</a>
<li><a href="#LOCALE_JOB_ATTRIBUTES">LOCALE_JOB_ATTRIBUTES</a>
<li><a href="#LOCALE_PATH_NAME">LOCALE_PATH_NAME</a>
<li><a href="#LOCAL_DATA_INDICATOR">LOCAL_DATA_INDICATOR</a>
<li><a href="#LOCATION">LOCATION</a>
<li><a href="#MAILING_ADDRESS_LINE_1">MAILING_ADDRESS_LINE_1</a>
<li><a href="#MAILING_ADDRESS_LINE_2">MAILING_ADDRESS_LINE_2</a>
<li><a href="#MAILING_ADDRESS_LINE_3">MAILING_ADDRESS_LINE_3</a>
<li><a href="#MAILING_ADDRESS_LINE_4">MAILING_ADDRESS_LINE_4</a>
<li><a href="#MAIL_NOTIFICATION">MAIL_NOTIFICATION</a>
<li><a href="#MANAGER_CODE">MANAGER_CODE</a>
<li><a href="#MAXIMUM_ALLOWED_STORAGE">MAXIMUM_ALLOWED_STORAGE</a>
<li><a href="#MESSAGE_NOTIFICATION">MESSAGE_NOTIFICATION</a>
<li><a href="#MESSAGE_QUEUE_DELIVERY_METHOD">MESSAGE_QUEUE_DELIVERY_METHOD</a>
<li><a href="#MESSAGE_QUEUE">MESSAGE_QUEUE</a>
<li><a href="#MESSAGE_QUEUE_SEVERITY">MESSAGE_QUEUE_SEVERITY</a>
<li><a href="#MIDDLE_NAME">MIDDLE_NAME</a>
<li><a href="#NETWORK_USER_ID">NETWORK_USER_ID</a>
<li><a href="#NO_PASSWORD_INDICATOR">NO_PASSWORD_INDICATOR</a>
<li><a href="#OBJECT_AUDITING_VALUE">OBJECT_AUDITING_VALUE</a>
<li><a href="#OFFICE">OFFICE</a>
<li><a href="#OR_NAME">OR_NAME</a>
<li><a href="#OUTPUT_QUEUE">OUTPUT_QUEUE</a>
<li><a href="#OWNER">OWNER</a>
<li><a href="#PASSWORD_CHANGE_DATE">PASSWORD_CHANGE_DATE</a>
<li><a href="#PASSWORD_EXPIRATION_INTERVAL">PASSWORD_EXPIRATION_INTERVAL</a>
<li><a href="#PREFERRED_NAME">PREFERRED_NAME</a>
<li><a href="#PREVIOUS_SIGN_ON">PREVIOUS_SIGN_ON</a>
<li><a href="#PRINT_COVER_PAGE">PRINT_COVER_PAGE</a>
<li><a href="#PRINT_DEVICE">PRINT_DEVICE</a>
<li><a href="#PRIORITY_MAIL_NOTIFICATION">PRIORITY_MAIL_NOTIFICATION</a>
<li><a href="#SET_PASSWORD_TO_EXPIRE">SET_PASSWORD_TO_EXPIRE</a>
<li><a href="#SIGN_ON_ATTEMPTS_NOT_VALID">SIGN_ON_ATTEMPTS_NOT_VALID</a>
<li><a href="#SMTP_DOMAIN">SMTP_DOMAIN</a>
<li><a href="#SMTP_ROUTE">SMTP_ROUTE</a>
<li><a href="#SMTP_USER_ID">SMTP_USER_ID</a>
<li><a href="#SORT_SEQUENCE_TABLE">SORT_SEQUENCE_TABLE</a>
<li><a href="#SPECIAL_AUTHORITIES">SPECIAL_AUTHORITIES</a>
<li><a href="#SPECIAL_ENVIRONMENT">SPECIAL_ENVIRONMENT</a>
<li><a href="#STATUS">STATUS</a>
<li><a href="#STORAGE_USED">STORAGE_USED</a>
<li><a href="#SUPPLEMENTAL_GROUPS">SUPPLEMENTAL_GROUPS</a>
<li><a href="#TELEPHONE_NUMBER_1">TELEPHONE_NUMBER_1</a>
<li><a href="#TELEPHONE_NUMBER_2">TELEPHONE_NUMBER_2</a>
<li><a href="#TEXT">TEXT</a>
<li><a href="#TEXT_DESCRIPTION">TEXT_DESCRIPTION</a>
<li><a href="#USER_ACTION_AUDIT_LEVEL">USER_ACTION_AUDIT_LEVEL</a>
<li><a href="#USER_ADDRESS">USER_ADDRESS</a>
<li><a href="#USER_CLASS">USER_CLASS</a>
<li><a href="#USER_DESCRIPTION">USER_DESCRIPTION</a>
<li><a href="#USER_ID">USER_ID</a>
<li><a href="#USER_ID_NUMBER">USER_ID_NUMBER</a>
<li><a href="#USER_OPTIONS">USER_OPTIONS</a>
<li><a href="#USER_PROFILE_NAME">USER_PROFILE_NAME</a>
</ul>

<p>Use any of these attribute IDs with
<a href="ChangeableResource.html#getAttributeValue(java.lang.Object)">getAttributeValue()</a>
and <a href="ChangeableResource.html#setAttributeValue(java.lang.Object, java.lang.Object)">setAttributeValue()</a>
to access the attribute values for an RUser.

<blockquote><pre>
// Create an RUser object to refer to a specific user.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RUser user = new RUser(system, "AUSERID");
<br>
// Get the user status.
String status = (String)user.getAttributeValue(RUser.STATUS);
<br>
// Set the print device for a user to work station.
user.setAttributeValue(RUser.PRINT_DEVICE, RUser.PRINT_DEVICE_WORK_STATION);
<br>
// Commit the attribute change.
user.commitAttributeChanges();
</pre></blockquote>

@see RUserList
**/
public class RUser
extends ChangeableResource
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");
    private static final String         ICON_BASE_NAME_     = "RUser";
    private static final String         PRESENTATION_KEY_   = "USER";



//-----------------------------------------------------------------------------------------
// Attribute values.
//-----------------------------------------------------------------------------------------

/**
Attribute value for system value.
**/
    public static final String SYSTEM_VALUE    = "*SYSVAL";

/**
Attribute value for yes.
**/
    public static final String YES             = "*YES";

/**
Attribute value for no.
**/
    public static final String NO              = "*NO";

/**
Attribute value for none.
**/
    public static final String NONE            = "*NONE";

/**
Attribute value for no date.
**/
    public static final Date NO_DATE           = DateValueMap.NO_DATE;


//-----------------------------------------------------------------------------------------
// Attribute IDs.
//
// * If you add an attribute here, make sure and add it to the class javadoc.
//-----------------------------------------------------------------------------------------

    // Private data.
            static ResourceMetaDataTable        attributes_             = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);
    private static ProgramMap                   getterMap_              = new ProgramMap();
    private static CommandMap                   setterMap_              = new CommandMap();

    private static final ArrayValueMap          arrayValueMap_          = new ArrayValueMap();
    private static final ArrayValueMap          arrayValueMapNone_      = new ArrayValueMap(NONE);
    private static final ArrayTypeValueMap      arrayTypeValueMapString_= new ArrayTypeValueMap(String.class);
    private static final BooleanValueMap        booleanValueMap01_      = new BooleanValueMap("0", "1");
    private static final BooleanValueMap        booleanValueMapNY_      = new BooleanValueMap("N", "Y");
    private static final BooleanValueMap        booleanValueMapNoYes_   = new BooleanValueMap(NO, YES);
    private static final IntegerToLongValueMap  integerToLongValueMap_  = new IntegerToLongValueMap();
    private static final QuoteValueMap          quoteValueMap_          = new QuoteValueMap();
    private static final QuoteValueMap          quoteValueMapBlank_     = new QuoteValueMap("*BLANK");
    private static final QuoteValueMap          quoteValueMapNone_      = new QuoteValueMap(NONE);

//@B1D    private static final String                 ADDDIRE_                = "ADDDIRE";
    private static final String                 CHGDIRE_                = "CHGDIRE";
    private static final String                 CHGUSRPRF_              = "CHGUSRPRF";
    private static final String                 CHGUSRAUD_              = "CHGUSRAUD";
//@B1D    private static final String                 CRTUSRPRF_              = "CRTUSRPRF";
    private static final int[]                  INDEX_0_                = new int[] { 0 };
    private static final String                 QOKSCHD_                = "qokschd";
    private static final String                 USRI0100_               = "qsyrusri_usri0100";
    private static final String                 USRI0200_               = "qsyrusri_usri0200";
    private static final String                 USRI0300_               = "qsyrusri_usri0300";
    private static final String                 USRD_PARAMETER_         = "USRD";
    private static final String                 USRPRF_PARAMETER_       = "USRPRF";
    private static final String                 USRID_PARAMETER_        = "USRID";



/**
Attribute ID for accounting code.  This identifies a String
attribute, which represents the accounting code associated with this user.
**/
    public static final String ACCOUNTING_CODE                          = "ACCOUNTING_CODE";

    static {
        attributes_.add(ACCOUNTING_CODE, String.class, false);
        getterMap_.add(ACCOUNTING_CODE, USRI0300_, "receiverVariable.accountingCode");
        setterMap_.add(ACCOUNTING_CODE, CHGUSRPRF_, "ACGCDE", quoteValueMapBlank_);
    }



/**
Attribute ID for allow synchronization.  This identifies a Boolean
attribute, which indicates whether the user's directory entry should be
synchronized with directories other than the System Distribution Directory.
**/
    public static final String ALLOW_SYNCHRONIZATION                          = "ALLOW_SYNCHRONIZATION";

    static {
        attributes_.add(ALLOW_SYNCHRONIZATION, Boolean.class, false);
        getterMap_.add(ALLOW_SYNCHRONIZATION, QOKSCHD_, "receiverVariable.directoryEntries.allowSynchronization.fieldValue", INDEX_0_, booleanValueMap01_);
        setterMap_.add(ALLOW_SYNCHRONIZATION, CHGDIRE_, "ALWSYNC", booleanValueMapNoYes_);
    }




/**
Attribute ID for assistance level.  This identifies a String
attribute, which represents the user interface that the user will use.
Possible values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QASTLVL determines which user interface the user is using.
<li><a href="#ASSISTANCE_LEVEL_BASIC">ASSISTANCE_LEVEL_BASIC</a>
    - The Operational Assistant user interface.
<li><a href="#ASSISTANCE_LEVEL_INTERMEDIATE">ASSISTANCE_LEVEL_INTERMEDIATE</a>
    - The system user interface.
<li><a href="#ASSISTANCE_LEVEL_ADVANCED">ASSISTANCE_LEVEL_ADVANCED</a>
    - The expert system user interface.
</ul>
**/
    public static final String ASSISTANCE_LEVEL                   = "ASSISTANCE_LEVEL";

    /**
    Attribute value indicating the Operational Assistant user interface.

    @see #ASSISTANCE_LEVEL
    **/
    public static final String ASSISTANCE_LEVEL_BASIC                   = "*BASIC";

    /**
    Attribute value indicating the system user interface.

    @see #ASSISTANCE_LEVEL
    **/
    public static final String ASSISTANCE_LEVEL_INTERMEDIATE            = "*INTERMED";

    /**
    Attribute value indicating the expert system user interface.

    @see #ASSISTANCE_LEVEL
    **/
    public static final String ASSISTANCE_LEVEL_ADVANCED                = "*ADVANCED";

    static {
        attributes_.add(ASSISTANCE_LEVEL, String.class, false,
                        new Object[] {SYSTEM_VALUE,
                            ASSISTANCE_LEVEL_BASIC,
                            ASSISTANCE_LEVEL_INTERMEDIATE,
                            ASSISTANCE_LEVEL_ADVANCED}, null, true);
        getterMap_.add(ASSISTANCE_LEVEL, USRI0300_, "receiverVariable.assistanceLevel");
        setterMap_.add(ASSISTANCE_LEVEL, CHGUSRPRF_, "ASTLVL");
    }



/**
Attribute ID for attention key handling program.  This identifies a
String attribute, which represents the fully qualified integrated file system
path name of the attention key handling program for
this user.  Possible values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QATNPGM determines the user's attention key handling program.
<li><a href="#NONE">NONE</a>
    - No attention key handling program is used.
<li><a href="#ATTENTION_KEY_HANDLING_PROGRAM_ASSIST">ATTENTION_KEY_HANDLING_PROGRAM_ASSIST</a>
    - The Operational Assistant attention key handling program.
<li>The attention key handling program name.
</ul>

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String ATTENTION_KEY_HANDLING_PROGRAM = "ATTENTION_KEY_HANDLING_PROGRAM";

    /**
    Attribute value indicating the Operational Assistant attention key handling program.

    @see #ATTENTION_KEY_HANDLING_PROGRAM
    **/
    public static final String ATTENTION_KEY_HANDLING_PROGRAM_ASSIST           = "*ASSIST";

    static {
        attributes_.add(ATTENTION_KEY_HANDLING_PROGRAM, String.class, false,
                        new Object[] {SYSTEM_VALUE, NONE, ATTENTION_KEY_HANDLING_PROGRAM_ASSIST}, null, false);
        getterMap_.add(ATTENTION_KEY_HANDLING_PROGRAM, USRI0300_, "receiverVariable.attentionKeyHandlingProgram",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "PGM"));
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_CL, "PGM");
        setterMap_.add(ATTENTION_KEY_HANDLING_PROGRAM, CHGUSRPRF_, "ATNPGM", valueMap);
    }



/**
Attribute ID for building.  This identifies a String
attribute, which represents the building in the user's directory entry.
**/
    public static final String BUILDING                          = "BUILDING";

    static {
        attributes_.add(BUILDING, String.class, false);
        getterMap_.add(BUILDING, QOKSCHD_, "receiverVariable.directoryEntries.building.fieldValue", INDEX_0_);
        setterMap_.add(BUILDING, CHGDIRE_, "BLDG", quoteValueMapNone_);
    }



/**
Attribute ID for cc:Mail address.  This identifies a String
attribute, which represents the cc:Mail address in the user's directory entry.
**/
    public static final String CC_MAIL_ADDRESS                          = "CC_MAIL_ADDRESS";

    static {
        attributes_.add(CC_MAIL_ADDRESS, String.class, false);
        getterMap_.add(CC_MAIL_ADDRESS, QOKSCHD_, "receiverVariable.directoryEntries.ccMailAddress.fieldValue", INDEX_0_);
        setterMap_.add(CC_MAIL_ADDRESS, CHGDIRE_, "CCMAILADR", quoteValueMapNone_);
    }




/**
Attribute ID for cc:Mail comment.  This identifies a String
attribute, which represents the cc:Mail comment in the user's directory entry.
**/
    public static final String CC_MAIL_COMMENT                          = "CC_MAIL_COMMENT";

    static {
        attributes_.add(CC_MAIL_COMMENT, String.class, false);
        getterMap_.add(CC_MAIL_COMMENT, QOKSCHD_, "receiverVariable.directoryEntries.ccMailComment.fieldValue", INDEX_0_);
        setterMap_.add(CC_MAIL_COMMENT, CHGDIRE_, "CCMAILCMT", quoteValueMapNone_);
    }



/**
Attribute ID for character code set ID.  This identifies a Integer
attribute, which represents the character code set ID to be used by the system
for this user.
**/
    public static final String CHARACTER_CODE_SET_ID             = "CHARACTER_CODE_SET_ID";

    static {
        attributes_.add(CHARACTER_CODE_SET_ID, Integer.class, false);
        getterMap_.add(CHARACTER_CODE_SET_ID, USRI0300_, "receiverVariable.characterCodeSetID");
        ValueMap valueMap = new CharacterCodeSetIDValueMap_();
        setterMap_.add(CHARACTER_CODE_SET_ID, CHGUSRPRF_, "CCSID", valueMap);
    }

    private static class CharacterCodeSetIDValueMap_ extends AbstractValueMap
    {
        public Object ltop(Object logicalValue)
        {
            if (((Integer)logicalValue).intValue() == -2)
                return SYSTEM_VALUE;
            else
                return logicalValue;
        }
    }



/**
Attribute ID for character identifier control.  This identifies a String
attribute, which represents the character identifier control for the user.  Possible
values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QCHRIDCTL will be used to determine the character
    identifier control for this user.
<li><a href="#CHARACTER_IDENTIFIER_CONTROL_DEVICE_DESCRIPTION">CHARACTER_IDENTIFIER_CONTROL_DEVICE_DESCRIPTION</a>
    - Performs the same function as on the CHRID command parameter for display
    files, printer files, and panel groups.
<li><a href="#CHARACTER_IDENTIFIER_CONTROL_JOB_CCSID">CHARACTER_IDENTIFIER_CONTROL_JOB_CCSID</a>
    - Performs the same function as on the CHRID command parameter for display
    files, printer files, and panel groups.
</ul>
**/
    public static final String CHARACTER_IDENTIFIER_CONTROL                 = "CHARACTER_IDENTIFIER_CONTROL";

    /**
    Attribute value indicating the same function as on the CHRID command parameter for display
    files, printer files, and panel groups.

    @see #CHARACTER_IDENTIFIER_CONTROL
    **/
    public static final String CHARACTER_IDENTIFIER_CONTROL_DEVICE_DESCRIPTION  = "*DEVD";

    /**
    Attribute value indicating the same function as on the CHRID command parameter for display
    files, printer files, and panel groups.

    @see #CHARACTER_IDENTIFIER_CONTROL
    **/
    public static final String CHARACTER_IDENTIFIER_CONTROL_JOB_CCSID           = "*JOBCCSID";

    static {
        attributes_.add(CHARACTER_IDENTIFIER_CONTROL, String.class, false,
                        new Object[] {SYSTEM_VALUE,
                            CHARACTER_IDENTIFIER_CONTROL_DEVICE_DESCRIPTION,
                            CHARACTER_IDENTIFIER_CONTROL_JOB_CCSID}, null, true);
        getterMap_.add(CHARACTER_IDENTIFIER_CONTROL, USRI0300_, "receiverVariable.characterIdentifierControl");
        setterMap_.add(CHARACTER_IDENTIFIER_CONTROL, CHGUSRPRF_, "CHRIDCTL");
    }




/**
Attribute ID for company.  This identifies a String
attribute, which represents the company in the user's directory entry.
**/
    public static final String COMPANY                          = "COMPANY";

    static {
        attributes_.add(COMPANY, String.class, false);
        getterMap_.add(COMPANY, QOKSCHD_, "receiverVariable.directoryEntries.company.fieldValue", INDEX_0_);
        setterMap_.add(COMPANY, CHGDIRE_, "CMPNY", quoteValueMapNone_);
    }



/**
Attribute ID for country ID.  This identifies a String
attribute, which represents the country ID used by the system for this user.
Possible values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QCNTRYID will be used to determine the country ID.
<li>The country ID.
</ul>
**/
    public static final String COUNTRY_ID                 = "COUNTRY_ID";

    static {
        attributes_.add(COUNTRY_ID, String.class, false,
                        new Object[] {SYSTEM_VALUE}, null, false);
        getterMap_.add(COUNTRY_ID, USRI0300_, "receiverVariable.countryID");
        setterMap_.add(COUNTRY_ID, CHGUSRPRF_, "CNTRYID", quoteValueMap_);
    }



/**
Attribute ID for current library.  This identifies a String
attribute, which represents the name of the user's current library.
Possible values are:
<ul>
<li><a href="#CURRENT_LIBRARY_NAME_DEFAULT">CURRENT_LIBRARY_NAME_DEFAULT</a>
    - The user does not have a current library.
<li>The library name.
</ul>
**/
    public static final String CURRENT_LIBRARY_NAME                 = "CURRENT_LIBRARY_NAME";

    /**
    Attribute value indicating that the user does not have a current library.

    @see #CURRENT_LIBRARY_NAME
    **/
    public static final String CURRENT_LIBRARY_NAME_DEFAULT         = "*CRTDFT";

    static {
        attributes_.add(CURRENT_LIBRARY_NAME, String.class, false,
                        new Object[] {CURRENT_LIBRARY_NAME_DEFAULT}, null, false);
        getterMap_.add(CURRENT_LIBRARY_NAME, USRI0300_, "receiverVariable.currentLibraryName");
        setterMap_.add(CURRENT_LIBRARY_NAME, CHGUSRPRF_, "CURLIB");
    }



/**
Attribute ID for date password expires.  This identifies a read-only Date
attribute, which represents the date the user's password expires.  If the
password is not set to expire, or is already expired, then this will
be <a href="#NO_DATE">NO_DATE</a>.
**/
    public static final String DATE_PASSWORD_EXPIRES                 = "DATE_PASSWORD_EXPIRES";

    static {
        attributes_.add(DATE_PASSWORD_EXPIRES, Date.class, true);
        ValueMap valueMap = new DateValueMap(DateValueMap.FORMAT_DTS);
        getterMap_.add(DATE_PASSWORD_EXPIRES, USRI0100_, "receiverVariable.datePasswordExpires", valueMap);
        getterMap_.add(DATE_PASSWORD_EXPIRES, USRI0300_, "receiverVariable.datePasswordExpires", valueMap);
    }



/**
Attribute ID for days until password expires.  This identifies a read-only Integer
attribute, which represents the number of days until the password will expire.
Possible values are:
<ul>
<li>0 - The password is expired.
<li>1-7 - The number of days until the password expires.
<li>-1 - The password will not expire in the next 7 days.
</ul>
**/
    public static final String DAYS_UNTIL_PASSWORD_EXPIRES                 = "DAYS_UNTIL_PASSWORD_EXPIRES";

    static {
        attributes_.add(DAYS_UNTIL_PASSWORD_EXPIRES, Integer.class, true);
        getterMap_.add(DAYS_UNTIL_PASSWORD_EXPIRES, USRI0100_, "receiverVariable.daysUntilPasswordExpires");
        getterMap_.add(DAYS_UNTIL_PASSWORD_EXPIRES, USRI0300_, "receiverVariable.daysUntilPasswordExpires");
    }



/**
Attribute ID for department.  This identifies a String
attribute, which represents the department in the user's directory entry.
**/
    public static final String DEPARTMENT                          = "DEPARTMENT";

    static {
        attributes_.add(DEPARTMENT, String.class, false);
        getterMap_.add(DEPARTMENT, QOKSCHD_, "receiverVariable.directoryEntries.department.fieldValue", INDEX_0_);
        setterMap_.add(DEPARTMENT, CHGDIRE_, "DEPT", quoteValueMapNone_);
    }


/**
Attribute ID for digital certificate indicator.  This identifies a read-only Boolean
attribute, which indicates whether there are digital certificates associated with
this user.
**/
    public static final String DIGITAL_CERTIFICATE_INDICATOR                 = "DIGITAL_CERTIFICATE_INDICATOR";

    static {
        attributes_.add(DIGITAL_CERTIFICATE_INDICATOR, Boolean.class, true);
        getterMap_.add(DIGITAL_CERTIFICATE_INDICATOR, USRI0300_, "receiverVariable.digitalCertificateIndicator", booleanValueMap01_);
    }



/**
Attribute ID for display sign-on information.  This identifies a String
attribute, which represents whether the sign-on information display is shown when
the user signs on.  Possible values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QDSPSGNINF determines if the sign-on information display
    is shown when the user signs on.
<li><a href="#YES">YES</a>
    - The sign-on information display is shown when the user signs on.
<li><a href="#NO">NO</a>
    - The sign-on information display is not shown when the user signs on.
</ul>
**/
    public static final String DISPLAY_SIGN_ON_INFORMATION                 = "DISPLAY_SIGN_ON_INFORMATION";

    static {
        attributes_.add(DISPLAY_SIGN_ON_INFORMATION, String.class, false,
                        new Object[] {SYSTEM_VALUE, YES, NO }, null, true);
        getterMap_.add(DISPLAY_SIGN_ON_INFORMATION, USRI0100_, "receiverVariable.displaySignOnInformation");
        getterMap_.add(DISPLAY_SIGN_ON_INFORMATION, USRI0300_, "receiverVariable.displaySignOnInformation");
        setterMap_.add(DISPLAY_SIGN_ON_INFORMATION, CHGUSRPRF_, "DSPSGNINF");
    }



/**
Attribute ID for fax telephone number.  This identifies a String
attribute, which represents the fax telephone number in the user's directory entry.
**/
    public static final String FAX_TELEPHONE_NUMBER                          = "FAX_TELEPHONE_NUMBER";

    static {
        attributes_.add(FAX_TELEPHONE_NUMBER, String.class, false);
        getterMap_.add(FAX_TELEPHONE_NUMBER, QOKSCHD_, "receiverVariable.directoryEntries.faxTelephoneNumber.fieldValue", INDEX_0_);
        setterMap_.add(FAX_TELEPHONE_NUMBER, CHGDIRE_, "FAXTELNBR", quoteValueMapNone_);
    }


/**
Attribute ID for first name.  This identifies a String
attribute, which represents the first name in the user's directory entry.
**/
    public static final String FIRST_NAME                          = "FIRST_NAME";

    static {
        attributes_.add(FIRST_NAME, String.class, false);
        getterMap_.add(FIRST_NAME, QOKSCHD_, "receiverVariable.directoryEntries.firstName.fieldValue", INDEX_0_);
        setterMap_.add(FIRST_NAME, CHGDIRE_, "FSTNAM", quoteValueMapNone_);
    }


/**
Attribute ID for full name.  This identifies a String
attribute, which represents the full name in the user's directory entry.
**/
    public static final String FULL_NAME                          = "FULL_NAME";

    static {
        attributes_.add(FULL_NAME, String.class, false);
        getterMap_.add(FULL_NAME, QOKSCHD_, "receiverVariable.directoryEntries.fullName.fieldValue", INDEX_0_);
        setterMap_.add(FULL_NAME, CHGDIRE_, "FULNAM", quoteValueMapNone_);
    }



/**
Attribute ID for group authority.  This identifies a String
attribute, which represents the authority the user's group profile
has to objects the user creates.  This can only be set to
<a href="#NONE">NONE</a> if the user does not belong to a group.
Possible values are:
<ul>
<li><a href="#NONE">NONE</a>
    - The group profile has no authority to the objects the user creates,
    or the user does not have a group profile.
<li><a href="#GROUP_AUTHORITY_ALL">GROUP_AUTHORITY_ALL</a>
    - The group profile has all authority to the objects the user creates.
<li><a href="#GROUP_AUTHORITY_CHANGE">GROUP_AUTHORITY_CHANGE</a>
    - The group profile has change authority to the objects the user creates.
<li><a href="#GROUP_AUTHORITY_USE">GROUP_AUTHORITY_USE</a>
    - The group profile has use authority to the objects the user creates.
<li><a href="#GROUP_AUTHORITY_EXCLUDE">GROUP_AUTHORITY_EXCLUDE</a>
    - The group profile has exclude authority to the objects the user creates.
</ul>
**/
    public static final String GROUP_AUTHORITY                 = "GROUP_AUTHORITY";

    /**
    Attribute value indicating that the group profile has all authority to the objects the user creates.

    @see #GROUP_AUTHORITY
    **/
    public static final String GROUP_AUTHORITY_ALL             = "*ALL";

    /**
    Attribute value indicating that the group profile has change authority to the objects the user creates.

    @see #GROUP_AUTHORITY
    **/
    public static final String GROUP_AUTHORITY_CHANGE          = "*CHANGE";

    /**
    Attribute value indicating that the group profile has use authority to the objects the user creates.

    @see #GROUP_AUTHORITY
    **/
    public static final String GROUP_AUTHORITY_USE             = "*USE";

    /**
    Attribute value indicating that the group profile has exclude authority to the objects the user creates.

    @see #GROUP_AUTHORITY
    **/
    public static final String GROUP_AUTHORITY_EXCLUDE         = "*EXCLUDE";

    static {
        attributes_.add(GROUP_AUTHORITY, String.class, false,
                        new Object[] {NONE,
                            GROUP_AUTHORITY_ALL,
                            GROUP_AUTHORITY_CHANGE,
                            GROUP_AUTHORITY_USE,
                            GROUP_AUTHORITY_EXCLUDE }, null, true);
        getterMap_.add(GROUP_AUTHORITY, USRI0200_, "receiverVariable.groupAuthority");
        getterMap_.add(GROUP_AUTHORITY, USRI0300_, "receiverVariable.groupAuthority");
        setterMap_.add(GROUP_AUTHORITY, CHGUSRPRF_, "GRPAUT");
    }



/**
Attribute ID for group authority type.  This identifies a String
attribute, which represents the type of authority the user's group has to
objects the user creates.  Possible values are:
<ul>
<li><a href="#GROUP_AUTHORITY_TYPE_PRIVATE">GROUP_AUTHORITY_TYPE_PRIVATE</a>
    - The group profile has a private authority to the objects the user creates,
    or the user does not have a group profile.
<li><a href="#GROUP_AUTHORITY_TYPE_PGP">GROUP_AUTHORITY_TYPE_PGP</a>
    - The group profile will be the primary group for objects the user creates.
</ul>
**/
    public static final String GROUP_AUTHORITY_TYPE                 = "GROUP_AUTHORITY_TYPE";

    /**
    Attribute value indicating that the group profile has a private authority to the objects the user creates,
    or the user does not have a group profile.

    @see #GROUP_AUTHORITY_TYPE
    **/
    public static final String GROUP_AUTHORITY_TYPE_PRIVATE         = "*PRIVATE";

    /**
    Attribute value indicating that the group profile will be the primary group for objects the user creates.

    @see #GROUP_AUTHORITY_TYPE
    **/
    public static final String GROUP_AUTHORITY_TYPE_PGP             = "*PGP";

    static {
        attributes_.add(GROUP_AUTHORITY_TYPE, String.class, false,
                        new Object[] {GROUP_AUTHORITY_TYPE_PRIVATE,
                            GROUP_AUTHORITY_TYPE_PGP }, null, true);
        getterMap_.add(GROUP_AUTHORITY_TYPE, USRI0200_, "receiverVariable.groupAuthorityType");
        getterMap_.add(GROUP_AUTHORITY_TYPE, USRI0300_, "receiverVariable.groupAuthorityType");
        setterMap_.add(GROUP_AUTHORITY_TYPE, CHGUSRPRF_, "GRPAUTTYP");
    }



/**
Attribute ID for group ID number.  This identifies a Long
attribute, which represents the group ID number for the user profile.
The group ID number is used to identify the user when it is a group and a
member of the group is using the integrated file system.  This will
be {@link #GROUP_ID_NUMBER_NONE GROUP_ID_NUMBER_NONE} if the user does not
have a group ID. It will be {@link #GROUP_ID_NUMBER_GENERATE GROUP_ID_NUMBER_GENERATE}
if it was set as such; in which case, call
{@link #refreshAttributeValues refreshAttributeValues()} to retrieve the actual group ID
that was generated by the system.
**/
    public static final String GROUP_ID_NUMBER                      = "GROUP_ID_NUMBER";

    static {
        attributes_.add(GROUP_ID_NUMBER, Long.class, false);
        getterMap_.add(GROUP_ID_NUMBER, USRI0300_, "receiverVariable.groupIDNumber", integerToLongValueMap_);
        ValueMap valueMap = new GroupIDNumberValueMap_();
        setterMap_.add(GROUP_ID_NUMBER, CHGUSRPRF_, "GID", valueMap);
    }

    private static class GroupIDNumberValueMap_ extends AbstractValueMap
    {
        public Object ltop(Object logicalValue)
        {
          long val = ((Long)logicalValue).longValue(); //@B2A
          if (val == 0) return "*NONE"; //@B2C
          if (val == -1) return "*GEN"; //@B2A
          return logicalValue; //@B2C
        }
    }

    /**
     * Attribute value indicating the system should generate a unique group ID number (*GEN).
     * @see #GROUP_ID_NUMBER
    **/
    public static final Long GROUP_ID_NUMBER_GENERATE = new Long(-1); //@B2A
    
    
    /**
     * Attribute value indicating the group ID number is *NONE.
     * @see #GROUP_ID_NUMBER
    **/
    public static final Long GROUP_ID_NUMBER_NONE = new Long(0); //@B2A



/**
Attribute ID for group member indicator.  This identifies a read-only Boolean
attribute, which indicates whether this user is a group that has members.
**/
    public static final String GROUP_MEMBER_INDICATOR                 = "GROUP_MEMBER_INDICATOR";

    static {
        attributes_.add(GROUP_MEMBER_INDICATOR, Boolean.class, true);
        getterMap_.add(GROUP_MEMBER_INDICATOR, USRI0300_, "receiverVariable.groupMemberIndicator", booleanValueMap01_);
    }


/**
Attribute ID for group profile name.  This identifies a String attribute,
which represents the name of the group profile.
Possible values are:
<ul>
<li><a href="#NONE">NONE</a>
    - If the user does not have a group profile.
<li>The group profile name.
</ul>
**/
    public static final String GROUP_PROFILE_NAME = "GROUP_PROFILE_NAME";

    static {
        attributes_.add(GROUP_PROFILE_NAME, String.class, false,
                        new Object[] {NONE }, null, false);
        getterMap_.add(GROUP_PROFILE_NAME, USRI0200_, "receiverVariable.groupProfileName");
        getterMap_.add(GROUP_PROFILE_NAME, USRI0300_, "receiverVariable.groupProfileName");
        setterMap_.add(GROUP_PROFILE_NAME, CHGUSRPRF_, "GRPPRF");
    }



/**
Attribute ID for highest scheduling priority.  This identifies a Integer
attribute, which represents the highest scheduling priority the user is allowed
to have for each job submitted to the system.  The priority is a value from 0
to 9, with 0 being the highest priority.
**/
    public static final String HIGHEST_SCHEDULING_PRIORITY                      = "HIGHEST_SCHEDULING_PRIORITY";

    static {
        attributes_.add(HIGHEST_SCHEDULING_PRIORITY, Integer.class, false);
        ValueMap valueMap = new IntegerValueMap();
        getterMap_.add(HIGHEST_SCHEDULING_PRIORITY, USRI0300_, "receiverVariable.highestSchedulingPriority", valueMap);
        setterMap_.add(HIGHEST_SCHEDULING_PRIORITY, CHGUSRPRF_, "PTYLMT");
    }



/**
Attribute ID for home directory.  This identifies a String
attribute, which represents the home directory for this user profile.
**/
    public static final String HOME_DIRECTORY                      = "HOME_DIRECTORY";

    static {
        attributes_.add(HOME_DIRECTORY, String.class, false);
        getterMap_.add(HOME_DIRECTORY, USRI0300_, "receiverVariable.homeDirectory.homeDirectoryNameValue");
        setterMap_.add(HOME_DIRECTORY, CHGUSRPRF_, "HOMEDIR", quoteValueMap_);
    }




/**
Attribute ID for indirect user.  This identifies a Boolean
attribute, which indicates whether the user is an indirect user
as specified in the user's directory entry.
**/
    public static final String INDIRECT_USER                          = "INDIRECT_USER";

    static {
        attributes_.add(INDIRECT_USER, Boolean.class, false);
        getterMap_.add(INDIRECT_USER, QOKSCHD_, "receiverVariable.directoryEntries.indirectUser.fieldValue", INDEX_0_, booleanValueMap01_);
        setterMap_.add(INDIRECT_USER, CHGDIRE_, "INDUSR", booleanValueMapNoYes_);
    }



/**
Attribute ID for initial menu.  This identifies a String attribute,
which represents the fully qualified integrated file system path name
of the initial menu for the user.  Possible values are:
<ul>
<li><a href="#INITIAL_MENU_SIGNOFF">INITIAL_MENU_SIGNOFF</a>
<li>The initial menu name.
</ul>

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String INITIAL_MENU = "INITIAL_MENU";

    /**
    Attribute value for initial menu signoff.

    @see #INITIAL_MENU
    **/
    public static final String INITIAL_MENU_SIGNOFF                            = "*SIGNOFF";

    static {
        attributes_.add(INITIAL_MENU, String.class, false,
                        new Object[] {INITIAL_MENU_SIGNOFF}, null, false);
        getterMap_.add(INITIAL_MENU, USRI0300_, "receiverVariable.initialMenu",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "MNU"));
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_CL, "MNU");
        setterMap_.add(INITIAL_MENU, CHGUSRPRF_, "INLMNU", valueMap);
    }



/**
Attribute ID for initial program.  This identifies a String attribute,
which represents the fully qualified integrated file system path name of
the initial program for the user.  Possible values are:
<ul>
<li><a href="#NONE">NONE</a>
<li>The initial program name.
</ul>

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String INITIAL_PROGRAM = "INITIAL_PROGRAM";

    static {
        attributes_.add(INITIAL_PROGRAM, String.class, false,
                        new Object[] {NONE}, null, false);
        getterMap_.add(INITIAL_PROGRAM, USRI0300_, "receiverVariable.initialProgram",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "PGM"));
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_CL, "PGM");
        setterMap_.add(INITIAL_PROGRAM, CHGUSRPRF_, "INLPGM", valueMap);
    }



/**
Attribute ID for job description.  This identifies a String attribute,
which represents the fully qualified integrated file system path name
of the job description used for jobs that start through
subsystem work station entries.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String JOB_DESCRIPTION = "JOB_DESCRIPTION";

    static {
        attributes_.add(JOB_DESCRIPTION, String.class, false);
        getterMap_.add(JOB_DESCRIPTION, USRI0300_, "receiverVariable.jobDescription",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "JOBD"));
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_CL, "JOBD");
        setterMap_.add(JOB_DESCRIPTION, CHGUSRPRF_, "JOBD", valueMap);
    }



/**
Attribute ID for job title.  This identifies a String
attribute, which represents the job title in the user's directory entry.
**/
    public static final String JOB_TITLE                          = "JOB_TITLE";

    static {
        attributes_.add(JOB_TITLE, String.class, false);
        getterMap_.add(JOB_TITLE, QOKSCHD_, "receiverVariable.directoryEntries.jobTitle.fieldValue", INDEX_0_);
        setterMap_.add(JOB_TITLE, CHGDIRE_, "TITLE", quoteValueMapNone_);
    }



/**
Attribute ID for keyboard buffering.  This identifies a String
attribute, which represents the keyboard buffering value that is
used when a job is initialized for this user.  Possible values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QKBDBUF determines the keybpard buffering value for
    this user.
<li><a href="#YES">YES</a>
    - The type-ahead and attention-key buffering options are both on.
<li><a href="#NO">NO</a>
    - The type-ahead and attention-key buffering options are not on.
<li><a href="#KEYBOARD_BUFFERING_TYPE_AHEAD">KEYBOARD_BUFFERING_TYPE_AHEAD</a>
    - The type-ahead option is on, but the attention-key buffering options is not.
</ul>
**/
    public static final String KEYBOARD_BUFFERING                 = "KEYBOARD_BUFFERING";

    /**
    Attribute value indicating the type-ahead option is on, but the attention-key buffering options is not.

    @see #KEYBOARD_BUFFERING
    **/
    public static final String KEYBOARD_BUFFERING_TYPE_AHEAD      = "*TYPEAHEAD";

    static {
        attributes_.add(KEYBOARD_BUFFERING, String.class, false,
                        new Object[] {SYSTEM_VALUE, YES, NO, KEYBOARD_BUFFERING_TYPE_AHEAD }, null, true);
        getterMap_.add(KEYBOARD_BUFFERING, USRI0300_, "receiverVariable.keyboardBuffering");
        setterMap_.add(KEYBOARD_BUFFERING, CHGUSRPRF_, "KBDBUF");
    }



/**
Attribute ID for language ID.  This identifies a String
attribute, which represents the language ID used by the system for this user.
Possible values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QLANGID will be used to determine the language ID.
<li>The language ID.
</ul>
**/
    public static final String LANGUAGE_ID                 = "LANGUAGE_ID";

    static {
        attributes_.add(LANGUAGE_ID, String.class, false,
                        new Object[] {SYSTEM_VALUE}, null, false);
        getterMap_.add(LANGUAGE_ID, USRI0300_, "receiverVariable.languageID");
        setterMap_.add(LANGUAGE_ID, CHGUSRPRF_, "LANGID");
    }



/**
Attribute ID for last name.  This identifies a String
attribute, which represents the last name in the user's directory entry.
**/
    public static final String LAST_NAME                          = "LAST_NAME";

    static {
        attributes_.add(LAST_NAME, String.class, false);
        getterMap_.add(LAST_NAME, QOKSCHD_, "receiverVariable.directoryEntries.lastName.fieldValue", INDEX_0_);
        setterMap_.add(LAST_NAME, CHGDIRE_, "LSTNAM", quoteValueMapNone_);
    }



/**
Attribute ID for limit capabilities.  This identifies a String
attribute, which indicates whether the user has limited capabilites.
Possible values are:
<ul>
<li><a href="#LIMIT_CAPABILITIES_PARTIAL">LIMIT_CAPABILITIES_PARTIAL</a>
    - The user cannot change the initial program or current library.
<li><a href="#YES">YES</a>
    - The user cannot change the initial menu, initial program,
    or current library.  The user cannot run commands from the
    command line.
<li><a href="#NO">NO</a>
    - The user is not limited.
</ul>
**/
    public static final String LIMIT_CAPABILITIES                 = "LIMIT_CAPABILITIES";

    /**
    Attribute value indicating the user cannot change the initial program or current library.

    @see #LIMIT_CAPABILITIES
    **/
    public static final String LIMIT_CAPABILITIES_PARTIAL         = "*PARTIAL";

    static {
        attributes_.add(LIMIT_CAPABILITIES, String.class, false,
                        new Object[] {LIMIT_CAPABILITIES_PARTIAL, YES, NO }, null, true);
        getterMap_.add(LIMIT_CAPABILITIES, USRI0200_, "receiverVariable.limitCapabilities");
        getterMap_.add(LIMIT_CAPABILITIES, USRI0300_, "receiverVariable.limitCapabilities");
        setterMap_.add(LIMIT_CAPABILITIES, CHGUSRPRF_, "LMTCPB");
    }



/**
Attribute ID for limit device sessions.  This identifies a String
attribute, which indicates whether the user is limited to one device
session.  Possible values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QLMTDEVSSN determines if the user is limited to one
    device session.
<li><a href="#YES">YES</a>
    - The user is limited to one session.
<li><a href="#NO">NO</a>
    - The user is not limited to one device session.
</ul>
**/
    public static final String LIMIT_DEVICE_SESSIONS                 = "LIMIT_DEVICE_SESSIONS";

    static {
        attributes_.add(LIMIT_DEVICE_SESSIONS, String.class, false,
                        new Object[] {SYSTEM_VALUE, YES, NO}, null, true);
        getterMap_.add(LIMIT_DEVICE_SESSIONS, USRI0300_, "receiverVariable.limitDeviceSessions");
        setterMap_.add(LIMIT_DEVICE_SESSIONS, CHGUSRPRF_, "LMTDEVSSN");
    }



/**
Attribute ID for locale job attributes.  This identifies a String array
attribute, which represents a list of attributes which are set from the locale path
name at the time a job is started for this user.  Possible values for the elements
of this array are:
<ul>
<li><a href="#NONE">NONE</a>
    - No job attributes are used from the locale path name at the time a job is
    started for this user profile.  If this is specified, then no other values
    can be specified.
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The job attributes assigned from the locale path name are determined by
    the system value QSETJOBATR at the time a job is started for this user profile.
    If this is specified, then no other values can be specified.
<li><a href="#LOCALE_JOB_ATTRIBUTES_CCSID">LOCALE_JOB_ATTRIBUTES_CCSID</a>
    - The coded character set identifier is set from the locale path name
    at the time a job is started for this user profile.
<li><a href="#LOCALE_JOB_ATTRIBUTES_DATE_FORMAT">LOCALE_JOB_ATTRIBUTES_DATE_FORMAT</a>
    - The date format is set from the locale path name
    at the time a job is started for this user profile.
<li><a href="#LOCALE_JOB_ATTRIBUTES_DATE_SEPARATOR">LOCALE_JOB_ATTRIBUTES_DATE_SEPARATOR</a>
    - The date separator is set from the locale path name
    at the time a job is started for this user profile.
<li><a href="#LOCALE_JOB_ATTRIBUTES_SORT_SEQUENCE">LOCALE_JOB_ATTRIBUTES_SORT_SEQUENCE</a>
    - The sort sequence is set from the locale path name
    at the time a job is started for this user profile.
<li><a href="#LOCALE_JOB_ATTRIBUTES_TIME_SEPARATOR">LOCALE_JOB_ATTRIBUTES_TIME_SEPARATOR</a>
    - The time separator is set from the locale path name
    at the time a job is started for this user profile.
<li><a href="#LOCALE_JOB_ATTRIBUTES_DECIMAL_FORMAT">LOCALE_JOB_ATTRIBUTES_DECIMAL_FORMAT</a>
    - The decimal format is set from the locale path name
    at the time a job is started for this user profile.
</ul>
**/
    public static final String LOCALE_JOB_ATTRIBUTES                 = "LOCALE_JOB_ATTRIBUTES";

    /**
    Attribute value indicating that the coded character set identifier is set from the locale path name
    at the time a job is started for this user profile.

    @see #LOCALE_JOB_ATTRIBUTES
    **/
    public static final String LOCALE_JOB_ATTRIBUTES_CCSID           = "*CCSID";

    /**
    Attribute value indicating that the date format is set from the locale path name
    at the time a job is started for this user profile.

    @see #LOCALE_JOB_ATTRIBUTES
    **/
    public static final String LOCALE_JOB_ATTRIBUTES_DATE_FORMAT     = "*DATFMT";

    /**
    Attribute value indicating that the date separator is set from the locale path name
    at the time a job is started for this user profile.

    @see #LOCALE_JOB_ATTRIBUTES
    **/
    public static final String LOCALE_JOB_ATTRIBUTES_DATE_SEPARATOR  = "*DATSEP";

    /**
    Attribute value indicating that the sort sequence is set from the locale path name
    at the time a job is started for this user profile.

    @see #LOCALE_JOB_ATTRIBUTES
    **/
    public static final String LOCALE_JOB_ATTRIBUTES_SORT_SEQUENCE   = "*SRTSEQ";

    /**
    Attribute value indicating that the time separator is set from the locale path name
    at the time a job is started for this user profile.

    @see #LOCALE_JOB_ATTRIBUTES
    **/
    public static final String LOCALE_JOB_ATTRIBUTES_TIME_SEPARATOR  = "*TIMSEP";

    /**
    Attribute value indicating that the decimal format is set from the locale path name
    at the time a job is started for this user profile.

    @see #LOCALE_JOB_ATTRIBUTES
    **/
    public static final String LOCALE_JOB_ATTRIBUTES_DECIMAL_FORMAT  = "*DECFMT";

    static {
        String[] possibleValues = new String[] {NONE,
                            SYSTEM_VALUE,
                            LOCALE_JOB_ATTRIBUTES_CCSID,
                            LOCALE_JOB_ATTRIBUTES_DATE_FORMAT,
                            LOCALE_JOB_ATTRIBUTES_DATE_SEPARATOR,
                            LOCALE_JOB_ATTRIBUTES_SORT_SEQUENCE,
                            LOCALE_JOB_ATTRIBUTES_TIME_SEPARATOR,
                            LOCALE_JOB_ATTRIBUTES_DECIMAL_FORMAT};
        attributes_.add(LOCALE_JOB_ATTRIBUTES, String.class, false,
                        possibleValues, null, true, true);
        ValueMap valueMap = new OptionsValueMap('N', 'Y', possibleValues);
        getterMap_.add(LOCALE_JOB_ATTRIBUTES, USRI0300_, "receiverVariable.localeJobAttributes", valueMap);
        setterMap_.add(LOCALE_JOB_ATTRIBUTES, CHGUSRPRF_, "SETJOBATR", arrayValueMapNone_); //@B1C
    }



/**
Attribute ID for locale path name.  This identifies a String
attribute, which represents the locale path name that is assigned to the
user profile when a job is started.
Possible values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The QLOCALE system value is used to determine the locale path name.
<li><a href="#NONE">NONE</a>
    - No locale path name is assigned.
<li><a href="#LOCALE_PATH_NAME_C">LOCALE_PATH_NAME_C</a>
    - The C locale path name is assigned.
<li><a href="#LOCALE_PATH_NAME_POSIX">LOCALE_PATH_NAME_POSIX</a>
    - The POSIX locale path name is assigned.
</ul>
**/
    public static final String LOCALE_PATH_NAME                 = "LOCALE_PATH_NAME";

    /**
    Attribute value indicating the C locale path name.

    @see #LOCALE_PATH_NAME
    **/
    public static final String LOCALE_PATH_NAME_C               = "*C";

    /**
    Attribute value indicating the POSIX locale path name.

    @see #LOCALE_PATH_NAME
    **/
    public static final String LOCALE_PATH_NAME_POSIX           = "*POSIX";

    static {
        attributes_.add(LOCALE_PATH_NAME, String.class, false,
                        new Object[] {SYSTEM_VALUE, NONE, LOCALE_PATH_NAME_C, LOCALE_PATH_NAME_POSIX }, null, true);
        getterMap_.add(LOCALE_PATH_NAME, USRI0300_, "receiverVariable.localePathName");
        setterMap_.add(LOCALE_PATH_NAME, CHGUSRPRF_, "LOCALE");
    }



/**
Attribute ID for local data indicator.  This identifies a read-only String
attribute, which indicates where this user was created.  Possible values are:
<ul>
<li><a href="#LOCAL_DATA_INDICATOR_LOCAL">LOCAL_DATA_INDICATOR_LOCAL</a>
    - If the user was created on this system or a remote user
    was created on this system.
<li><a href="#LOCAL_DATA_INDICATOR_SHADOWED">LOCAL_DATA_INDICATOR_SHADOWED</a>
    - If the user was shadowed from another system.
</ul>
**/
    public static final String LOCAL_DATA_INDICATOR                          = "LOCAL_DATA_INDICATOR";

    /**
    Attribute value indicating that the user was created on this system or if a remote user
    was created on this system.

    @see #LOCAL_DATA_INDICATOR
    **/
    public static final String LOCAL_DATA_INDICATOR_LOCAL                    = "0";

    /**
    Attribute value indicating that the user was shadowed from another system.

    @see #LOCAL_DATA_INDICATOR
    **/
    public static final String LOCAL_DATA_INDICATOR_SHADOWED                 = "1";

    static {
        attributes_.add(LOCAL_DATA_INDICATOR, String.class, true,
                        new Object[] {LOCAL_DATA_INDICATOR_LOCAL, LOCAL_DATA_INDICATOR_SHADOWED }, null, true);
        getterMap_.add(LOCAL_DATA_INDICATOR, QOKSCHD_, "receiverVariable.directoryEntries.localDataIndicator.fieldValue", INDEX_0_);
    }



/**
Attribute ID for location.  This identifies a String
attribute, which represents the location in the user's directory entry.
**/
    public static final String LOCATION                     = "LOCATION";

    static {
        attributes_.add(LOCATION, String.class, false);
        getterMap_.add(LOCATION, QOKSCHD_, "receiverVariable.directoryEntries.location.fieldValue", INDEX_0_);
        setterMap_.add(LOCATION, CHGDIRE_, "LOC", quoteValueMapNone_);
    }



/**
Attribute ID for mailing address line 1.  This identifies a String
attribute, which represents the mailing address line 1 in the user's directory entry.
**/
    public static final String MAILING_ADDRESS_LINE_1                     = "MAILING_ADDRESS_LINE_1";

    static {
        attributes_.add(MAILING_ADDRESS_LINE_1, String.class, false);
        getterMap_.add(MAILING_ADDRESS_LINE_1, QOKSCHD_, "receiverVariable.directoryEntries.mailingAddressLine1.fieldValue", INDEX_0_);
        setterMap_.add(MAILING_ADDRESS_LINE_1, CHGDIRE_, "ADDR1", quoteValueMapNone_);
    }



/**
Attribute ID for mailing address line 2.  This identifies a String
attribute, which represents the mailing address line 2 in the user's directory entry.
**/
    public static final String MAILING_ADDRESS_LINE_2                     = "MAILING_ADDRESS_LINE_2";

    static {
        attributes_.add(MAILING_ADDRESS_LINE_2, String.class, false);
        getterMap_.add(MAILING_ADDRESS_LINE_2, QOKSCHD_, "receiverVariable.directoryEntries.mailingAddressLine2.fieldValue", INDEX_0_);
        setterMap_.add(MAILING_ADDRESS_LINE_2, CHGDIRE_, "ADDR2", quoteValueMapNone_);
    }



/**
Attribute ID for mailing address line 3.  This identifies a String
attribute, which represents the mailing address line 3 in the user's directory entry.
**/
    public static final String MAILING_ADDRESS_LINE_3                     = "MAILING_ADDRESS_LINE_3";

    static {
        attributes_.add(MAILING_ADDRESS_LINE_3, String.class, false);
        getterMap_.add(MAILING_ADDRESS_LINE_3, QOKSCHD_, "receiverVariable.directoryEntries.mailingAddressLine3.fieldValue", INDEX_0_);
        setterMap_.add(MAILING_ADDRESS_LINE_3, CHGDIRE_, "ADDR3", quoteValueMapNone_);
    }



/**
Attribute ID for mailing address line 4.  This identifies a String
attribute, which represents the mailing address line 4 in the user's directory entry.
**/
    public static final String MAILING_ADDRESS_LINE_4                     = "MAILING_ADDRESS_LINE_4";

    static {
        attributes_.add(MAILING_ADDRESS_LINE_4, String.class, false);
        getterMap_.add(MAILING_ADDRESS_LINE_4, QOKSCHD_, "receiverVariable.directoryEntries.mailingAddressLine4.fieldValue", INDEX_0_);
        setterMap_.add(MAILING_ADDRESS_LINE_4, CHGDIRE_, "ADDR4", quoteValueMapNone_);
    }



/**
Attribute ID for mail notification.  This identifies a String
attribute, which indicates whether the user is notified of the
arrival of mail. This is part of the user's directory entry.
Possible values are:
<ul>
<li><a href="#MAIL_NOTIFICATION_SPECIFIC">MAIL_NOTIFICATION_SPECIFIC</a>
    - The user is notified of the arrival of specific types of mail.
    Use the <a href="#PRIORITY_MAIL_NOTIFICATION">PRIORITY_MAIL_NOTIFICATION</a>
    and <a href="#MESSAGE_NOTIFICATION">MESSAGE_NOTIFICATION</a> attributes
    for specific information.
<li><a href="#MAIL_NOTIFICATION_ALL">MAIL_NOTIFICATION_ALL</a>
    - The user is notified of the arrival of all types of mail.
<li><a href="#MAIL_NOTIFICATION_NONE">MAIL_NOTIFICATION_NONE</a>
    - The user is not notified of the arrival of mail.
</ul>
**/
    public static final String MAIL_NOTIFICATION                     = "MAIL_NOTIFICATION";

    /**
    Attribute value indicating that the user is notified of the arrival
    of specific types of mail.

    @see #MAIL_NOTIFICATION
    **/
    public static final String MAIL_NOTIFICATION_SPECIFIC               = "*SPECIFIC";

    /**
    Attribute value indicating that the user is notified of the arrival
    of all types of mail.

    @see #MAIL_NOTIFICATION
    **/
    public static final String MAIL_NOTIFICATION_ALL                    = "*ALLMAIL";

    /**
    Attribute value indicating that the user is not notified of the arrival of mail.

    @see #MAIL_NOTIFICATION
    **/
    public static final String MAIL_NOTIFICATION_NONE                   = "*NOMAIL";

    static {
        attributes_.add(MAIL_NOTIFICATION, String.class, false,
                        new Object[] {MAIL_NOTIFICATION_SPECIFIC,
                            MAIL_NOTIFICATION_ALL,
                            MAIL_NOTIFICATION_NONE }, null, true);
        getterMap_.add(MAIL_NOTIFICATION, QOKSCHD_, "receiverVariable.directoryEntries.mailNotification.fieldValue", INDEX_0_,
                       new MailNotificationValueMap_(0, new String[] { null, MAIL_NOTIFICATION_SPECIFIC, MAIL_NOTIFICATION_ALL, MAIL_NOTIFICATION_NONE }));
        setterMap_.add(MAIL_NOTIFICATION, CHGDIRE_, "NFYMAIL");
    }

    private static class MailNotificationValueMap_ extends AbstractValueMap
    {
        private int index_;
        private Object[] logicalValues_;

        MailNotificationValueMap_(int index, Object[] logicalValues)
        {
            index_ = index;
            logicalValues_ = logicalValues;
        }

        public Object ptol(Object physicalValue)
        {
            // The possible values are EBCDIC characters for '1', '2', '3;, etc.
            // Adding to 16 turns it into an int.
            return logicalValues_[16 + ((byte[])physicalValue)[index_]];
        }
    }



/**
Attribute ID for manager code.  This identifies a read-only Boolean
attribute, which represents the manager code in the user's directory entry.
**/
    public static final String MANAGER_CODE                     = "MANAGER_CODE";

    static {
        attributes_.add(MANAGER_CODE, Boolean.class, true);
        getterMap_.add(MANAGER_CODE, QOKSCHD_, "receiverVariable.directoryEntries.managerCode.fieldValue", INDEX_0_, booleanValueMap01_);
    }



/**
Attribute ID for maximum allowed storage.  This identifies a Integer
attribute, which represents the maximum amount of auxiliary storage (in
kilobytes) that can be assigned to store permanant objects owned by the user.
If the user does not have a maximum amount of allowed storage, this will
be -1.
**/
    public static final String MAXIMUM_ALLOWED_STORAGE                 = "MAXIMUM_ALLOWED_STORAGE";

    static {
        attributes_.add(MAXIMUM_ALLOWED_STORAGE, Integer.class, false);
        getterMap_.add(MAXIMUM_ALLOWED_STORAGE, USRI0300_, "receiverVariable.maximumAllowedStorage");
        ValueMap valueMap = new MaximumAllowedStorageValueMap_();
        setterMap_.add(MAXIMUM_ALLOWED_STORAGE, CHGUSRPRF_, "MAXSTG", valueMap);
    }

    private static class MaximumAllowedStorageValueMap_ extends AbstractValueMap
    {
        private static final String NOMAX = "*NOMAX";

        public Object ltop(Object logicalValue)
        {
            int asInt = ((Integer)logicalValue).intValue();
            switch(asInt) {
            case -1:
                return NOMAX;
            default:
                return logicalValue;
            }
        }
    }



/**
Attribute ID for message notification.  This identifies a Boolean
attribute, which indicates whether the user is notified of the
arrival of messages.
This is part of the user's directory entry.
**/
    public static final String MESSAGE_NOTIFICATION                     = "MESSAGE_NOTIFICATION";

    static {
        attributes_.add(MESSAGE_NOTIFICATION, Boolean.class, false, null, null, false);
        getterMap_.add(MESSAGE_NOTIFICATION, QOKSCHD_, "receiverVariable.directoryEntries.mailNotification.fieldValue", INDEX_0_,
                       new MailNotificationValueMap_(2, new Boolean[] { Boolean.FALSE, Boolean.TRUE }));
        setterMap_.add(MESSAGE_NOTIFICATION, CHGDIRE_, "NFYMSGS", booleanValueMapNoYes_);
    }



/**
Attribute ID for message queue delivery method.  This identifies a String
attribute, which represents how the messages are delivered to the message queue
used by the user.  Possible values are:
<ul>
<li><a href="#MESSAGE_QUEUE_DELIVERY_METHOD_BREAK">MESSAGE_QUEUE_DELIVERY_METHOD_BREAK</a>
    - The job to which the message queue is assigned is interrupted when a message
    arrives on the message queue.
<li><a href="#MESSAGE_QUEUE_DELIVERY_METHOD_DEFAULT">MESSAGE_QUEUE_DELIVERY_METHOD_DEFAULT</a>
    - Messages requiring replies are answered with their default reply.
<li><a href="#MESSAGE_QUEUE_DELIVERY_METHOD_HOLD">MESSAGE_QUEUE_DELIVERY_METHOD_HOLD</a>
    - The messages are held in the message queue until they are requested by the
    user or program.
<li><a href="#MESSAGE_QUEUE_DELIVERY_METHOD_NOTIFY">MESSAGE_QUEUE_DELIVERY_METHOD_NOTIFY</a>
    - The job to which the message queue is assigned is notified when a message arrives
    on the message queue.
</ul>
**/
    public static final String MESSAGE_QUEUE_DELIVERY_METHOD                 = "MESSAGE_QUEUE_DELIVERY_METHOD";

    /**
    Attribute value indicating that the job to which the message queue is
    assigned is interrupted when a message arrives on the message queue.

    @see #MESSAGE_QUEUE_DELIVERY_METHOD
    **/
    public static final String MESSAGE_QUEUE_DELIVERY_METHOD_BREAK          = "*BREAK";

    /**
    Attribute value indicating that messages requiring replies are
    answered with their default reply.

    @see #MESSAGE_QUEUE_DELIVERY_METHOD
    **/
    public static final String MESSAGE_QUEUE_DELIVERY_METHOD_DEFAULT        = "*DFT";

    /**
    Attribute value indicating that the messages are held in the message
    queue until they are requested by the user or program.

    @see #MESSAGE_QUEUE_DELIVERY_METHOD
    **/
    public static final String MESSAGE_QUEUE_DELIVERY_METHOD_HOLD           = "*HOLD";

    /**
    Attribute value indicating that the job to which the message queue is
    assigned is notified when a message arrives on the message queue.

    @see #MESSAGE_QUEUE_DELIVERY_METHOD
    **/
    public static final String MESSAGE_QUEUE_DELIVERY_METHOD_NOTIFY         = "*NOTIFY";

    static {
        attributes_.add(MESSAGE_QUEUE_DELIVERY_METHOD, String.class, false,
                        new Object[] {MESSAGE_QUEUE_DELIVERY_METHOD_BREAK,
                            MESSAGE_QUEUE_DELIVERY_METHOD_DEFAULT,
                            MESSAGE_QUEUE_DELIVERY_METHOD_HOLD,
                            MESSAGE_QUEUE_DELIVERY_METHOD_NOTIFY }, null, true);
        getterMap_.add(MESSAGE_QUEUE_DELIVERY_METHOD, USRI0300_, "receiverVariable.messageQueueDeliveryMethod");
        setterMap_.add(MESSAGE_QUEUE_DELIVERY_METHOD, CHGUSRPRF_, "DLVRY");
    }



/**
Attribute ID for message queue.  This identifies a String attribute,
which represents the fully qualified integrated file system path name
of the message queue that is used by this user.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String MESSAGE_QUEUE = "MESSAGE_QUEUE";

    static {
        attributes_.add(MESSAGE_QUEUE, String.class, false);
        getterMap_.add(MESSAGE_QUEUE, USRI0300_, "receiverVariable.messageQueue",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "MSGQ"));
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_CL, "MSGQ");
        setterMap_.add(MESSAGE_QUEUE, CHGUSRPRF_, "MSGQ", valueMap);
    }



/**
Attribute ID for message queue severity.  This identifies an Integer attribute,
which represents the lowest severity that a message can have and still be delivered to
a user in break or notify mode.
**/
    public static final String MESSAGE_QUEUE_SEVERITY = "MESSAGE_QUEUE_SEVERITY";

    static {
        attributes_.add(MESSAGE_QUEUE_SEVERITY, Integer.class, false);
        getterMap_.add(MESSAGE_QUEUE_SEVERITY, USRI0300_, "receiverVariable.messageQueueSeverity");
        setterMap_.add(MESSAGE_QUEUE_SEVERITY, CHGUSRPRF_, "SEV");
    }



/**
Attribute ID for middle name.  This identifies a String
attribute, which represents the middle name in the user's directory entry.
**/
    public static final String MIDDLE_NAME                     = "MIDDLE_NAME";

    static {
        attributes_.add(MIDDLE_NAME, String.class, false);
        getterMap_.add(MIDDLE_NAME, QOKSCHD_, "receiverVariable.directoryEntries.middleName.fieldValue", INDEX_0_);
        setterMap_.add(MIDDLE_NAME, CHGDIRE_, "MIDNAM", quoteValueMapNone_);
    }



/**
Attribute ID for network user ID.  This identifies a String
attribute, which represents the network user ID in the user's directory entry.
**/
    public static final String NETWORK_USER_ID                     = "NETWORK_USER_ID";

    static {
        attributes_.add(NETWORK_USER_ID, String.class, false);
        getterMap_.add(NETWORK_USER_ID, QOKSCHD_, "receiverVariable.directoryEntries.networkUserID.fieldValue", INDEX_0_);
        setterMap_.add(NETWORK_USER_ID, CHGDIRE_, "NETUSRID", quoteValueMapNone_);
    }


/**
Attribute ID for no password indicator.  This identifies a read-only Boolean
attribute, which indicates whether there is no password.
**/
    public static final String NO_PASSWORD_INDICATOR                 = "NO_PASSWORD_INDICATOR";

    static {
        attributes_.add(NO_PASSWORD_INDICATOR, Boolean.class, true);
        getterMap_.add(NO_PASSWORD_INDICATOR, USRI0100_, "receiverVariable.noPasswordIndicator", booleanValueMapNY_);
        getterMap_.add(NO_PASSWORD_INDICATOR, USRI0300_, "receiverVariable.noPasswordIndicator", booleanValueMapNY_);
    }



/**
Attribute ID for object auditing value.  This identifies a String
attribute, which represents the user's object auditing value.  Possible values are:
<ul>
<li><a href="#NONE">NONE</a>
    - No additional object auditing is done for the user.
<li><a href="#OBJECT_AUDITING_VALUE_CHANGE">OBJECT_AUDITING_VALUE_CHANGE</a>
    - Object changes are audited for the user if the object's auditing
    value is *USRPRF.
<li><a href="#OBJECT_AUDITING_VALUE_ALL">OBJECT_AUDITING_VALUE_ALL</a>
    - Object read and change operations are audited for the user
    if the object's auditing value is *USRPRF.
</ul>
**/
    public static final String OBJECT_AUDITING_VALUE                 = "OBJECT_AUDITING_VALUE";

    /**
    Attribute value indicating that object changes are audited for the user if the object's auditing
    value is *USRPRF.

    @see #OBJECT_AUDITING_VALUE
    **/
    public static final String OBJECT_AUDITING_VALUE_CHANGE          = "*CHANGE";

    /**
    Attribute value indicating that object read and change operations are audited for the user
    if the object's auditing value is *USRPRF.

    @see #OBJECT_AUDITING_VALUE
    **/
    public static final String OBJECT_AUDITING_VALUE_ALL             = "*ALL";

    static {
        attributes_.add(OBJECT_AUDITING_VALUE, String.class, false,
                        new Object[] {NONE,
                            OBJECT_AUDITING_VALUE_CHANGE,
                            OBJECT_AUDITING_VALUE_ALL }, null, true);
        getterMap_.add(OBJECT_AUDITING_VALUE, USRI0300_, "receiverVariable.objectAuditingValue");
        setterMap_.add(OBJECT_AUDITING_VALUE, CHGUSRAUD_, "OBJAUD");
    }



/**
Attribute ID for office.  This identifies a String
attribute, which represents the office in the user's directory entry.
**/
    public static final String OFFICE                     = "OFFICE";

    static {
        attributes_.add(OFFICE, String.class, false);
        getterMap_.add(OFFICE, QOKSCHD_, "receiverVariable.directoryEntries.office.fieldValue", INDEX_0_);
        setterMap_.add(OFFICE, CHGDIRE_, "OFC", quoteValueMapNone_);
    }



/**
Attribute ID for O/R name.  This identifies a read-only String
attribute, which represents the paper representation of the X.400 O/R
name in the user's directory entry.
**/
    public static final String OR_NAME                     = "OR_NAME";

    static {
        attributes_.add(OR_NAME, String.class, true);
        getterMap_.add(OR_NAME, QOKSCHD_, "receiverVariable.directoryEntries.orName.fieldValue", INDEX_0_);
    }



/**
Attribute ID for output queue.  This identifies a String attribute,
which represents the fully qualified integrated file system path name
of the output queue that is used by this user. Possible values are:
<ul>
<li><a href="#OUTPUT_QUEUE_WORK_STATION">OUTPUT_QUEUE_WORK_STATION</a>
    - The output queue assigned to the user's work station is used.
<li><a href="#OUTPUT_QUEUE_DEVICE">OUTPUT_QUEUE_DEVICE</a>
    - An output queue with the same name as the device specified
    in the printer device parameter is used.
<li>The output queue name.
</ul>

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String OUTPUT_QUEUE = "OUTPUT_QUEUE";

    /**
    Attribute value indicating that the output queue assigned to the user's work station is used.

    @see #OUTPUT_QUEUE
    **/
    public static final String OUTPUT_QUEUE_WORK_STATION   = "*WRKSTN";

    /**
    Attribute value indicating that an output queue with the same name as the device specified
    in the printer device parameter is used.

    @see #OUTPUT_QUEUE
    **/
    public static final String OUTPUT_QUEUE_DEVICE         = "*DEV";

    static {
        attributes_.add(OUTPUT_QUEUE, String.class, false,
                        new Object[] {OUTPUT_QUEUE_WORK_STATION, OUTPUT_QUEUE_DEVICE }, null, false);
        getterMap_.add(OUTPUT_QUEUE, USRI0300_, "receiverVariable.outputQueue",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "OUTQ"));
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_CL, "OUTQ");
        setterMap_.add(OUTPUT_QUEUE, CHGUSRPRF_, "OUTQ", valueMap);
    }



/**
Attribute ID for owner.  This identifies a String attribute,
which indicates who is to own objects created by this user. Possible values are:
<ul>
<li><a href="#OWNER_USER_PROFILE">OWNER_USER_PROFILE</a>
    - The user owns any objects the user creates.  If the user does not
    have a group profile, the field contains this value.
<li><a href="#OWNER_GROUP_PROFILE">OWNER_GROUP_PROFILE</a>
    - The user's group profile owns any objects the user creates.
</ul>
**/
    public static final String OWNER                    = "OWNER";

    /**
    Attribute value indicating that the user owns any objects the user creates.
    If the user does not have a group profile, the field contains this value.

    @see #OWNER
    **/
    public static final String OWNER_USER_PROFILE       = "*USRPRF";

    /**
    Attribute value indicating that the user's group profile owns any objects the user creates.

    @see #OWNER
    **/
    public static final String OWNER_GROUP_PROFILE      = "*GRPPRF";

    static {
        attributes_.add(OWNER, String.class, false,
                        new Object[] {OWNER_USER_PROFILE, OWNER_GROUP_PROFILE }, null, true);
        getterMap_.add(OWNER, USRI0300_, "receiverVariable.owner");
        setterMap_.add(OWNER, CHGUSRPRF_, "OWNER");
    }



/**
Attribute ID for password change date.  This identifies a read-only Date attribute,
which represents the date the user's password was last changed.
The Date value is converted using the default Java locale.
**/
    public static final String PASSWORD_CHANGE_DATE = "PASSWORD_CHANGE_DATE";

    static {
        attributes_.add(PASSWORD_CHANGE_DATE, Date.class, true);
        ValueMap valueMap = new DateValueMap(DateValueMap.FORMAT_DTS);
        getterMap_.add(PASSWORD_CHANGE_DATE, USRI0100_, "receiverVariable.passwordChangeDate", valueMap);
        getterMap_.add(PASSWORD_CHANGE_DATE, USRI0300_, "receiverVariable.passwordChangeDate", valueMap);
    }


/**
Attribute ID for password expiration interval.  This identifies a Integer
attribute, which represents the number of days the user's password can remain
active before it must be changed.  Possible values are:
<ul>
<li>0 - The system value QPWDEXPITV is used to determine the user's
    password expiration interval.
<li>-1 - The user's password does not expire.
<li>The number of days the user's password can remain active before it must
    be changed.
</ul>
**/
    public static final String PASSWORD_EXPIRATION_INTERVAL = "PASSWORD_EXPIRATION_INTERVAL";

    static {
        attributes_.add(PASSWORD_EXPIRATION_INTERVAL, Integer.class, false);
        getterMap_.add(PASSWORD_EXPIRATION_INTERVAL, USRI0100_, "receiverVariable.passwordExpirationInterval");
        getterMap_.add(PASSWORD_EXPIRATION_INTERVAL, USRI0300_, "receiverVariable.passwordExpirationInterval");
        ValueMap valueMap = new PasswordExpirationIntervalValueMap_();
        setterMap_.add(PASSWORD_EXPIRATION_INTERVAL, CHGUSRPRF_, "PWDEXPITV", valueMap);
    }

    private static class PasswordExpirationIntervalValueMap_ extends AbstractValueMap
    {
        private static final String NOMAX = "*NOMAX";

        public Object ltop(Object logicalValue)
        {
            int asInt = ((Integer)logicalValue).intValue();
            switch(asInt) {
            case 0:
                return SYSTEM_VALUE;
            case -1:
                return NOMAX;
            default:
                return logicalValue;
            }
        }
    }



/**
Attribute ID for preferred name.  This identifies a String
attribute, which represents the preferred name in the user's directory entry.
**/
    public static final String PREFERRED_NAME                     = "PREFERRED_NAME";

    static {
        attributes_.add(PREFERRED_NAME, String.class, false);
        getterMap_.add(PREFERRED_NAME, QOKSCHD_, "receiverVariable.directoryEntries.preferredName.fieldValue", INDEX_0_);
        setterMap_.add(PREFERRED_NAME, CHGDIRE_, "PREFNAM", quoteValueMapNone_);
    }



/**
Attribute ID for previous sign-on.  This identifies a read-only Date attribute,
which represents the date and time the user last signed on.  If the
user has never signed on, then this will be <a href="#NO_DATE">NO_DATE</a>.
The Date value is converted using the default Java locale.
**/
    public static final String PREVIOUS_SIGN_ON = "PREVIOUS_SIGN_ON";

    static {
        attributes_.add(PREVIOUS_SIGN_ON, Date.class, true);
        ValueMap valueMap = new DateValueMap(DateValueMap.FORMAT_13);
        getterMap_.add(PREVIOUS_SIGN_ON, USRI0100_, "receiverVariable.previousSignOnDateAndTime", valueMap);
        getterMap_.add(PREVIOUS_SIGN_ON, USRI0300_, "receiverVariable.previousSignOnDateAndTime", valueMap);
    }



/**
Attribute ID for print cover page.  This identifies a Boolean
attribute, which indicates whether to print a cover page as specified
in the user's directory entry.
**/
    public static final String PRINT_COVER_PAGE                          = "PRINT_COVER_PAGE";

    static {
        attributes_.add(PRINT_COVER_PAGE, Boolean.class, false);
        getterMap_.add(PRINT_COVER_PAGE, QOKSCHD_, "receiverVariable.directoryEntries.printCoverPage.fieldValue", INDEX_0_, booleanValueMap01_);
        setterMap_.add(PRINT_COVER_PAGE, CHGDIRE_, "PRTCOVER", booleanValueMapNoYes_);
    }



/**
Attribute ID for print device.  This identifies a String attribute,
which represents the printer used to print for this user. Possible values are:
<ul>
<li><a href="#PRINT_DEVICE_WORK_STATION">PRINT_DEVICE_WORK_STATION</a>
    - The printer assigned to the user's work station is used.
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The default system printer specified in the system value QPRTDEV
    is used.
<li>The print device.
</ul>
**/
    public static final String PRINT_DEVICE                 = "PRINT_DEVICE";

    /**
    Attribute value indicating that the printer assigned to the user's work station is used.

    @see #PRINT_DEVICE
    **/
    public static final String PRINT_DEVICE_WORK_STATION    = "*WRKSTN";

    static {
        attributes_.add(PRINT_DEVICE, String.class, false,
                        new Object[] {PRINT_DEVICE_WORK_STATION, SYSTEM_VALUE }, null, false);
        getterMap_.add(PRINT_DEVICE, USRI0300_, "receiverVariable.printDevice");
        setterMap_.add(PRINT_DEVICE, CHGUSRPRF_, "PRTDEV");
    }



/**
Attribute ID for priority mail notification.  This identifies a Boolean
attribute, which indicates whether the user is notified of the
arrival of priority, private, and important mail.
This is part of the user's directory entry.
**/
    public static final String PRIORITY_MAIL_NOTIFICATION                     = "PRIORITY_MAIL_NOTIFICATION";

    static {
        attributes_.add(PRIORITY_MAIL_NOTIFICATION, Boolean.class, false, null, null, false);
        getterMap_.add(PRIORITY_MAIL_NOTIFICATION, QOKSCHD_, "receiverVariable.directoryEntries.mailNotification.fieldValue", INDEX_0_,
                       new MailNotificationValueMap_(1, new Boolean[] { Boolean.FALSE, Boolean.TRUE }));
        setterMap_.add(PRIORITY_MAIL_NOTIFICATION, CHGDIRE_, "NFYPTYPERS", booleanValueMapNoYes_);
    }



/**
Attribute ID for set password to expire.  This identifies a Boolean
attribute, which indicates whether the user's password is set to expire,
requiring the user to change the password when signing on.
**/
    public static final String SET_PASSWORD_TO_EXPIRE                     = "SET_PASSWORD_TO_EXPIRE";

    static {
        attributes_.add(SET_PASSWORD_TO_EXPIRE, Boolean.class, false);
        getterMap_.add(SET_PASSWORD_TO_EXPIRE, USRI0100_, "receiverVariable.setPasswordToExpire", booleanValueMapNY_);
        getterMap_.add(SET_PASSWORD_TO_EXPIRE, USRI0300_, "receiverVariable.setPasswordToExpire", booleanValueMapNY_);
        setterMap_.add(SET_PASSWORD_TO_EXPIRE, CHGUSRPRF_, "PWDEXP", booleanValueMapNoYes_);
    }



/**
Attribute ID for sign-on attempts not valid.  This identifies a read-only Integer
attribute, which represents the number of sign-on attempts that were not valid
since the last successful sign-on.
**/
    public static final String SIGN_ON_ATTEMPTS_NOT_VALID                     = "SIGN_ON_ATTEMPTS_NOT_VALID";

    static {
        attributes_.add(SIGN_ON_ATTEMPTS_NOT_VALID, Integer.class, true);
        getterMap_.add(SIGN_ON_ATTEMPTS_NOT_VALID, USRI0100_, "receiverVariable.signOnAttemptsNotValid");
        getterMap_.add(SIGN_ON_ATTEMPTS_NOT_VALID, USRI0300_, "receiverVariable.signOnAttemptsNotValid");
    }



/**
Attribute ID for SMTP domain.  This identifies a read-only String
attribute, which represents the SMTP domain as specified
in the user's directory entry.
**/
    public static final String SMTP_DOMAIN                          = "SMTP_DOMAIN";

    static {
        attributes_.add(SMTP_DOMAIN, String.class, true);
        getterMap_.add(SMTP_DOMAIN, QOKSCHD_, "receiverVariable.directoryEntries.smtpDomain.fieldValue", INDEX_0_);
    }



/**
Attribute ID for SMTP route.  This identifies a read-only String
attribute, which represents the SMTP route as specified
in the user's directory entry.
**/
    public static final String SMTP_ROUTE                          = "SMTP_ROUTE";

    static {
        attributes_.add(SMTP_ROUTE, String.class, true);
        getterMap_.add(SMTP_ROUTE, QOKSCHD_, "receiverVariable.directoryEntries.smtpRoute.fieldValue", INDEX_0_);
    }



/**
Attribute ID for SMTP user ID.  This identifies a read-only String
attribute, which represents the SMTP user ID as specified
in the user's directory entry.
**/
    public static final String SMTP_USER_ID                          = "SMTP_USER_ID";

    static {
        attributes_.add(SMTP_USER_ID, String.class, true);
        getterMap_.add(SMTP_USER_ID, QOKSCHD_, "receiverVariable.directoryEntries.smtpUserID.fieldValue", INDEX_0_);
    }



/**
Attribute ID for sort sequence table.  This identifies a String attribute,
which represents the fully qualified integrated file system path name of
the sort sequence table used for string comparisons.
Possible values are:
<ul>
<li><a href="#SORT_SEQUENCE_TABLE_HEX">SORT_SEQUENCE_TABLE_HEX</a>
    - The hexadecimal values of the characters are used to determine the
    sort sequence.
<li><a href="#SORT_SEQUENCE_TABLE_UNIQUE">SORT_SEQUENCE_TABLE_UNIQUE</a>
    - A unique-weight sort table associated with the language specified.
<li><a href="#SORT_SEQUENCE_TABLE_SHARED">SORT_SEQUENCE_TABLE_SHARED</a>
    - A shared-weight sort table associated with the language specified.
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QSRTSEQ.
<li>The sort sequence table name.
</ul>

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String SORT_SEQUENCE_TABLE         = "SORT_SEQUENCE_TABLE";

    /**
    Attribute value indicating that the hexadecimal values of the characters are used to determine the
    sort sequence.

    @see #SORT_SEQUENCE_TABLE
    **/
    public static final String SORT_SEQUENCE_TABLE_HEX     = "*HEX";

    /**
    Attribute value indicating that a unique-weight sort table associated with the language specified

    @see #SORT_SEQUENCE_TABLE
    **/
    public static final String SORT_SEQUENCE_TABLE_UNIQUE  = "*LANGIDUNQ";

    /**
    Attribute value indicating that a shared-weight sort table associated with the language specified

    @see #SORT_SEQUENCE_TABLE
    **/
    public static final String SORT_SEQUENCE_TABLE_SHARED  = "*LANGIDSHR";

    static {
        attributes_.add(SORT_SEQUENCE_TABLE, String.class, false,
                        new Object[] {SORT_SEQUENCE_TABLE_HEX,
                            SORT_SEQUENCE_TABLE_UNIQUE,
                            SORT_SEQUENCE_TABLE_SHARED,
                            SYSTEM_VALUE }, null, false);
        getterMap_.add(SORT_SEQUENCE_TABLE, USRI0300_, "receiverVariable.sortSequenceTable",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "FILE"));
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_CL, "FILE");
        setterMap_.add(SORT_SEQUENCE_TABLE, CHGUSRPRF_, "SRTSEQ", valueMap);
    }



/**
Attribute ID for special authorities.  This identifies a String array
attribute, which represents a list of special authorities that the user has.
Possible values for the elements of this array are:
<ul>
<li><a href="#SPECIAL_AUTHORITIES_ALL_OBJECT">SPECIAL_AUTHORITIES_ALL_OBJECT</a>
    - All object.
<li><a href="#SPECIAL_AUTHORITIES_SECURITY_ADMINISTRATOR">SPECIAL_AUTHORITIES_SECURITY_ADMINISTRATOR</a>
    - Security administrator.
<li><a href="#SPECIAL_AUTHORITIES_JOB_CONTROL">SPECIAL_AUTHORITIES_JOB_CONTROL</a>
    - Job control.
<li><a href="#SPECIAL_AUTHORITIES_SPOOL_CONTROL">SPECIAL_AUTHORITIES_SPOOL_CONTROL</a>
    - Spool control.
<li><a href="#SPECIAL_AUTHORITIES_SAVE_SYSTEM">SPECIAL_AUTHORITIES_SAVE_SYSTEM</a>
    - Save system.
<li><a href="#SPECIAL_AUTHORITIES_SERVICE">SPECIAL_AUTHORITIES_SERVICE</a>
    - Service.
<li><a href="#SPECIAL_AUTHORITIES_AUDIT">SPECIAL_AUTHORITIES_AUDIT</a>
    - Audit.
<li><a href="#SPECIAL_AUTHORITIES_IO_SYSTEM_CONFIGURATION">SPECIAL_AUTHORITIES_IO_SYSTEM_CONFIGURATION</a>
    - Input/output system configuration.
</ul>
**/
    public static final String SPECIAL_AUTHORITIES                              = "SPECIAL_AUTHORITIES";

    /**
    Attribute value indicating all object special authorities.

    @see #SPECIAL_AUTHORITIES
    **/
    public static final String SPECIAL_AUTHORITIES_ALL_OBJECT                   = "*ALLOBJ";

    /**
    Attribute value indicating security administrator special authorities.

    @see #SPECIAL_AUTHORITIES
    **/
    public static final String SPECIAL_AUTHORITIES_SECURITY_ADMINISTRATOR       = "*SECADM";

    /**
    Attribute value indicating job control special authorities.

    @see #SPECIAL_AUTHORITIES
    **/
    public static final String SPECIAL_AUTHORITIES_JOB_CONTROL                  = "*JOBCTL";

    /**
    Attribute value indicating spool control special authorities.

    @see #SPECIAL_AUTHORITIES
    **/
    public static final String SPECIAL_AUTHORITIES_SPOOL_CONTROL                = "*SPLCTL";

    /**
    Attribute value indicating save system special authorities.

    @see #SPECIAL_AUTHORITIES
    **/
    public static final String SPECIAL_AUTHORITIES_SAVE_SYSTEM                  = "*SAVSYS";

    /**
    Attribute value indicating service special authorities.

    @see #SPECIAL_AUTHORITIES
    **/
    public static final String SPECIAL_AUTHORITIES_SERVICE                      = "*SERVICE";

    /**
    Attribute value indicating audit special authorities.

    @see #SPECIAL_AUTHORITIES
    **/
    public static final String SPECIAL_AUTHORITIES_AUDIT                        = "*AUDIT";

    /**
    Attribute value indicating I/O system configuration special authorities.

    @see #SPECIAL_AUTHORITIES
    **/
    public static final String SPECIAL_AUTHORITIES_IO_SYSTEM_CONFIGURATION      = "*IOSYSCFG";

    static {
        String[] possibleValues = new String[] {SPECIAL_AUTHORITIES_ALL_OBJECT,
                                                SPECIAL_AUTHORITIES_SECURITY_ADMINISTRATOR,
                                                SPECIAL_AUTHORITIES_JOB_CONTROL,
                                                SPECIAL_AUTHORITIES_SPOOL_CONTROL,
                                                SPECIAL_AUTHORITIES_SAVE_SYSTEM,
                                                SPECIAL_AUTHORITIES_SERVICE,
                                                SPECIAL_AUTHORITIES_AUDIT,
                                                SPECIAL_AUTHORITIES_IO_SYSTEM_CONFIGURATION};
        attributes_.add(SPECIAL_AUTHORITIES, String.class, false,
                        possibleValues, null, true, true);
        ValueMap valueMap = new OptionsValueMap('N', 'Y', possibleValues);
        getterMap_.add(SPECIAL_AUTHORITIES, USRI0200_, "receiverVariable.specialAuthorities", valueMap);
        getterMap_.add(SPECIAL_AUTHORITIES, USRI0300_, "receiverVariable.specialAuthorities", valueMap);
        setterMap_.add(SPECIAL_AUTHORITIES, CHGUSRPRF_, "SPCAUT", arrayValueMapNone_); //@B1C
    }



/**
Attribute ID for special environment.  This identifies a String attribute,
which represents the special environment the user operates in after signing on.
Possible values are:
<ul>
<li><a href="#SYSTEM_VALUE">SYSTEM_VALUE</a>
    - The system value QSPCENV is used to determine the user's special
    environment.
<li><a href="#NONE">NONE</a>
    - The user operates in the OS/400 environment.
<li><a href="#SPECIAL_ENVIRONMENT_SYSTEM_36">SPECIAL_ENVIRONMENT_SYSTEM_36</a>
    - The user operates in the System/36 environment.
</ul>
**/
    public static final String SPECIAL_ENVIRONMENT         = "SPECIAL_ENVIRONMENT";

    /**
    Attribute value indicating that the user operates in the System/36 environment.

    @see #SPECIAL_ENVIRONMENT
    **/
    public static final String SPECIAL_ENVIRONMENT_SYSTEM_36     = "*S36";

    static {
        attributes_.add(SPECIAL_ENVIRONMENT, String.class, false,
                        new Object[] {SYSTEM_VALUE,
                            NONE,
                            SPECIAL_ENVIRONMENT_SYSTEM_36 }, null, true);
        getterMap_.add(SPECIAL_ENVIRONMENT, USRI0300_, "receiverVariable.specialEnvironment");
        setterMap_.add(SPECIAL_ENVIRONMENT, CHGUSRPRF_, "SPCENV");
    }



/**
Attribute ID for status.  This identifies a String attribute,
which represents the status of the user profile.
Possible values are:
<ul>
<li><a href="#STATUS_ENABLED">STATUS_ENABLED</a>
    - The user profile is enabled.
<li><a href="#STATUS_NOT_ENABLED">STATUS_NOT_ENABLED</a>
    - The user profile is not enabled.
</ul>
**/
    public static final String STATUS         = "STATUS";

    /**
    Attribute value indicating that the user profile is enabled.

    @see #STATUS
    **/
    public static final String STATUS_ENABLED               = "*ENABLED";

    /**
    Attribute value indicating that the user profile is not enabled.

    @see #STATUS
    **/
    public static final String STATUS_NOT_ENABLED           = "*DISABLED";

    static {
        attributes_.add(STATUS, String.class, false,
                        new Object[] {STATUS_ENABLED, STATUS_NOT_ENABLED }, null, true);
        getterMap_.add(STATUS, USRI0300_, "receiverVariable.status");
        setterMap_.add(STATUS, CHGUSRPRF_, "STATUS");
    }



/**
Attribute ID for storage used.  This identifies a read-only Integer attribute,
which represents the amount of auxiliary storage (in kilobytes) occupied
by this user's owned objects.
**/
    public static final String STORAGE_USED         = "STORAGE_USED";

    static {
        attributes_.add(STORAGE_USED, Integer.class, true);
        getterMap_.add(STORAGE_USED, USRI0300_, "receiverVariable.storageUsed");
    }



/**
Attribute ID for supplemental groups.  This identifies a String array
attribute, which represents the supplemental groups for the user profile.
**/
    public static final String SUPPLEMENTAL_GROUPS         = "SUPPLEMENTAL_GROUPS";

    static {
        attributes_.add(SUPPLEMENTAL_GROUPS, String[].class, false, null, null, false, false);
        getterMap_.add(SUPPLEMENTAL_GROUPS, USRI0200_, "receiverVariable.supplementalGroups", "receiverVariable.numberOfSupplementalGroups", arrayTypeValueMapString_);
        getterMap_.add(SUPPLEMENTAL_GROUPS, USRI0300_, "receiverVariable.supplementalGroups", "receiverVariable.numberOfSupplementalGroups", arrayTypeValueMapString_);
        setterMap_.add(SUPPLEMENTAL_GROUPS, CHGUSRPRF_, "SUPGRPPRF", arrayValueMapNone_);
    }



/**
Attribute ID for telephone number 1.  This identifies a String
attribute, which represents the telephone number 1 as specified
in the user's directory entry.
**/
    public static final String TELEPHONE_NUMBER_1                          = "TELEPHONE_NUMBER_1";

    static {
        attributes_.add(TELEPHONE_NUMBER_1, String.class, false);
        getterMap_.add(TELEPHONE_NUMBER_1, QOKSCHD_, "receiverVariable.directoryEntries.telephoneNumber1.fieldValue", INDEX_0_);
        setterMap_.add(TELEPHONE_NUMBER_1, CHGDIRE_, "TELNBR1", quoteValueMapNone_);
    }



/**
Attribute ID for telephone number 2.  This identifies a String
attribute, which represents the telephone number 2 as specified
in the user's directory entry.
**/
    public static final String TELEPHONE_NUMBER_2                          = "TELEPHONE_NUMBER_2";

    static {
        attributes_.add(TELEPHONE_NUMBER_2, String.class, false);
        getterMap_.add(TELEPHONE_NUMBER_2, QOKSCHD_, "receiverVariable.directoryEntries.telephoneNumber2.fieldValue", INDEX_0_);
        setterMap_.add(TELEPHONE_NUMBER_2, CHGDIRE_, "TELNBR2", quoteValueMapNone_);
    }



/**
Attribute ID for text.  This identifies a String
attribute, which represents the text as specified
in the user's directory entry. This is not the same
as the directory entry description or user profile description.
@see #USER_DESCRIPTION
@see #TEXT_DESCRIPTION
**/
    public static final String TEXT                          = "TEXT";

    static {
        attributes_.add(TEXT, String.class, false);
        getterMap_.add(TEXT, QOKSCHD_, "receiverVariable.directoryEntries.text.fieldValue", INDEX_0_);
        setterMap_.add(TEXT, CHGDIRE_, "TEXT", quoteValueMapNone_);
    }



/**
Attribute ID for text description.  This identifies a String
attribute, which represents the descriptive text for the user profile.
This is not the same as the directory entry text or directory entry description.
@see #TEXT
@see #USER_DESCRIPTION
**/
    public static final String TEXT_DESCRIPTION         = "TEXT_DESCRIPTION";

    static {
        attributes_.add(TEXT_DESCRIPTION, String.class, false);
        getterMap_.add(TEXT_DESCRIPTION, USRI0300_, "receiverVariable.textDescription");
        setterMap_.add(TEXT_DESCRIPTION, CHGUSRPRF_, "TEXT", quoteValueMapBlank_);
    }



/**
Attribute ID for user action audit level.  This identifies a String array
attribute, which represents a list of action audit levels for the user.
Possible values for the elements of this array are:
<ul>
<li><a href="#USER_ACTION_AUDIT_LEVEL_COMMAND">USER_ACTION_AUDIT_LEVEL_COMMAND</a>
    - The user has the *CMD audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_CREATE">USER_ACTION_AUDIT_LEVEL_CREATE</a>
    - The user has the *CREATE audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_DELETE">USER_ACTION_AUDIT_LEVEL_DELETE</a>
    - The user has the *DELETE audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_JOB_DATA">USER_ACTION_AUDIT_LEVEL_JOB_DATA</a>
    - The user has the *JOBDTA audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_OBJECT_MANAGEMENT">USER_ACTION_AUDIT_LEVEL_OBJECT_MANAGEMENT</a>
    - The user has the *OBJMGT audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_OFFICE_SERVICES">USER_ACTION_AUDIT_LEVEL_OFFICE_SERVICES</a>
    - The user has the *OFCSRV audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_OPTICAL">USER_ACTION_AUDIT_LEVEL_OPTICAL</a>
    - The user has the *OPTICAL audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_PROGRAM_ADOPTION">USER_ACTION_AUDIT_LEVEL_PROGRAM_ADOPTION</a>
    - The user has the *PGMADP audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_SAVE_RESTORE">USER_ACTION_AUDIT_LEVEL_SAVE_RESTORE</a>
    - The user has the *SAVRST audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_SECURITY">USER_ACTION_AUDIT_LEVEL_SECURITY</a>
    - The user has the *SECURITY audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_SERVICE">USER_ACTION_AUDIT_LEVEL_SERVICE</a>
    - The user has the *SERVICE audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_SPOOLED_FILE_DATA">USER_ACTION_AUDIT_LEVEL_SPOOLED_FILE_DATA</a>
    - The user has the *SPLFDTA audit value specified in the user profile.
<li><a href="#USER_ACTION_AUDIT_LEVEL_SYSTEM_MANAGEMENT">USER_ACTION_AUDIT_LEVEL_SYSTEM_MANAGEMENT</a>
    - The user has the *SYSMGT audit value specified in the user profile.
</ul>
**/
    public static final String USER_ACTION_AUDIT_LEVEL                              = "USER_ACTION_AUDIT_LEVEL";

    /**
    Attribute value indicating that the user has the *CMD audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_COMMAND                  = "*CMD";

    /**
    Attribute value indicating that the user has the *CREATE audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_CREATE                   = "*CREATE";

    /**
    Attribute value indicating that the user has the *DELETE audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_DELETE                   = "*DELETE";

    /**
    Attribute value indicating that the user has the *JOBDTA audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_JOB_DATA                 = "*JOBDTA";

    /**
    Attribute value indicating that the user has the *OBJMGT audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_OBJECT_MANAGEMENT        = "*OBJMGT";

    /**
    Attribute value indicating that the user has the *OFCSRV audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_OFFICE_SERVICES          = "*OFCSRV";

    /**
    Attribute value indicating that the user has the *OPTICAL audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_OPTICAL                  = "*OPTICAL";

    /**
    Attribute value indicating that the user has the *PGMADP audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_PROGRAM_ADOPTION         = "*PGMADP";

    /**
    Attribute value indicating that the user has the *SAVRST audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_SAVE_RESTORE             = "*SAVRST";

    /**
    Attribute value indicating that the user has the *SECURITY audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_SECURITY                 = "*SECURITY";

    /**
    Attribute value indicating that the user has the *SERVICE audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_SERVICE                  = "*SERVICE";

    /**
    Attribute value indicating that the user has the *SPLFDTA audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_SPOOLED_FILE_DATA        = "*SPLFDTA";

    /**
    Attribute value indicating that the user has the *SYSMGT audit value specified in the user profile.

    @see #USER_ACTION_AUDIT_LEVEL
    **/
    public static final String USER_ACTION_AUDIT_LEVEL_SYSTEM_MANAGEMENT        = "*SYSMGT";

    static {
        String[] possibleValues = new String[] {USER_ACTION_AUDIT_LEVEL_COMMAND,
                                                USER_ACTION_AUDIT_LEVEL_CREATE,
                                                USER_ACTION_AUDIT_LEVEL_DELETE,
                                                USER_ACTION_AUDIT_LEVEL_JOB_DATA,
                                                USER_ACTION_AUDIT_LEVEL_OBJECT_MANAGEMENT,
                                                USER_ACTION_AUDIT_LEVEL_OFFICE_SERVICES,
                                                USER_ACTION_AUDIT_LEVEL_PROGRAM_ADOPTION,
                                                USER_ACTION_AUDIT_LEVEL_SAVE_RESTORE,
                                                USER_ACTION_AUDIT_LEVEL_SECURITY,
                                                USER_ACTION_AUDIT_LEVEL_SERVICE,
                                                USER_ACTION_AUDIT_LEVEL_SPOOLED_FILE_DATA,
                                                USER_ACTION_AUDIT_LEVEL_SYSTEM_MANAGEMENT,
                                                USER_ACTION_AUDIT_LEVEL_OPTICAL };
                                                // The API shows optical in the middle of the list,
                                                // but experimental data proves otherwise (at least
                                                // on V4R3).
        attributes_.add(USER_ACTION_AUDIT_LEVEL, String.class, false,
                        possibleValues, null, true, true);
        ValueMap valueMap = new OptionsValueMap('N', 'Y', possibleValues);
        getterMap_.add(USER_ACTION_AUDIT_LEVEL, USRI0300_, "receiverVariable.userActionAuditLevel", valueMap);
        setterMap_.add(USER_ACTION_AUDIT_LEVEL, CHGUSRAUD_, "AUDLVL", arrayValueMapNone_);
    }



/**
Attribute ID for user address.  This identifies a read-only String
attribute, which represents the user address as specified
in the user's directory entry.
**/
    public static final String USER_ADDRESS                          = "USER_ADDRESS";

    static {
        attributes_.add(USER_ADDRESS, String.class, true);
        getterMap_.add(USER_ADDRESS, QOKSCHD_, "receiverVariable.directoryEntries.userAddress.fieldValue", INDEX_0_);
    }




/**
Attribute ID for user class name.  This identifies a String attribute,
which represents the user class name.   Possible values are:
<ul>
<li><a href="#USER_CLASS_SECURITY_OFFICER">USER_CLASS_SECURITY_OFFICER</a>
    - The user has a class of security officer.
<li><a href="#USER_CLASS_SECURITY_ADMINISTRATOR">USER_CLASS_SECURITY_ADMINISTRATOR</a>
    - The user has a class of security administrator.
<li><a href="#USER_CLASS_PROGRAMMER">USER_CLASS_PROGRAMMER</a>
    - The user has a class of programmer.
<li><a href="#USER_CLASS_SYSTEM_OPERATOR">USER_CLASS_SYSTEM_OPERATOR</a>
    - The user has a class of system operator.
<li><a href="#USER_CLASS_USER">USER_CLASS_USER</a>
    - The user has a class of end user.
</ul>
**/
    public static final String USER_CLASS                          = "USER_CLASS";

    /**
    Attribute value indicating that the user has a class of security officer.

    @see #USER_CLASS
    **/
    public static final String USER_CLASS_SECURITY_OFFICER         = "*SECOFR";

    /**
    Attribute value indicating that the user has a class of security administrator.

    @see #USER_CLASS
    **/
    public static final String USER_CLASS_SECURITY_ADMINISTRATOR   = "*SECADM";

    /**
    Attribute value indicating that the user has a class of programmer.

    @see #USER_CLASS
    **/
    public static final String USER_CLASS_PROGRAMMER               = "*PGMR";

    /**
    Attribute value indicating that the user has a class of system operator.

    @see #USER_CLASS
    **/
    public static final String USER_CLASS_SYSTEM_OPERATOR          = "*SYSOPR";

    /**
    Attribute value indicating that the user has a class of end user.

    @see #USER_CLASS
    **/
    public static final String USER_CLASS_USER                     = "*USER";

    static {
        attributes_.add(USER_CLASS, String.class, false,
                        new Object[] {USER_CLASS_SECURITY_OFFICER,
                            USER_CLASS_SECURITY_ADMINISTRATOR,
                            USER_CLASS_PROGRAMMER,
                            USER_CLASS_SYSTEM_OPERATOR,
                            USER_CLASS_USER }, null, true);
        getterMap_.add(USER_CLASS, USRI0200_, "receiverVariable.userClassName");
        getterMap_.add(USER_CLASS, USRI0300_, "receiverVariable.userClassName");
        setterMap_.add(USER_CLASS, CHGUSRPRF_, "USRCLS");
    }


//@B1A
/**
Attribute ID for user description.  This identifies a read-only String
attribute, which represents the user description as specified
in the user's directory entry. This is not the same as the directory
entry text or the user profile description.
@see #TEXT
@see #TEXT_DESCRIPTION
**/
    public static final String USER_DESCRIPTION                          = "USER_DESCRIPTION";

    static
    {
        attributes_.add(USER_DESCRIPTION, String.class, true);
        getterMap_.add(USER_DESCRIPTION, QOKSCHD_, "receiverVariable.directoryEntries.userDescription.fieldValue", INDEX_0_);
    }


/**
Attribute ID for user ID.  This identifies a read-only String
attribute, which represents the user ID as specified
in the user's directory entry.
**/
    public static final String USER_ID                          = "USER_ID";

    static {
        attributes_.add(USER_ID, String.class, true);
        getterMap_.add(USER_ID, QOKSCHD_, "receiverVariable.directoryEntries.userID.fieldValue", INDEX_0_);
    }



//@B1C - This is a Long, not an Integer.
/**
Attribute ID for user ID number.  This identifies a Long attribute,
which represents the user ID number for the user profile. This is used
to identify the user when using the integrated file system.
**/
    public static final String USER_ID_NUMBER                          = "USER_ID_NUMBER";

    static {
        attributes_.add(USER_ID_NUMBER, Long.class, false);
        getterMap_.add(USER_ID_NUMBER, USRI0300_, "receiverVariable.userIDNumber", integerToLongValueMap_);
        setterMap_.add(USER_ID_NUMBER, CHGUSRPRF_, "UID");
    }



/**
Attribute ID for user options.  This identifies a String array
attribute, which represents a list of options for users to customize their
environment.  Possible values for the elements of this array are:
<ul>
<li><a href="#USER_OPTIONS_KEYWORDS">USER_OPTIONS_KEYWORDS</a>
    - Keywords are shown when a CL command is displayed.
<li><a href="#USER_OPTIONS_EXPERT">USER_OPTIONS_EXPERT</a>
    - More detailed information is shown when the user is defining or changing
    the system using edit or display object authority.
<li><a href="#USER_OPTIONS_FULL_SCREEN_HELP">USER_OPTIONS_FULL_SCREEN_HELP</a>
    - UIM online help is to be displayed on a full screen or a window.
<li><a href="#USER_OPTIONS_STATUS_MESSAGE">USER_OPTIONS_STATUS_MESSAGE</a>
    - Status messages sent to the user are shown.
<li><a href="#USER_OPTIONS_NO_STATUS_MESSAGE">USER_OPTIONS_NO_STATUS_MESSAGE</a>
    - Status messages sent to the user are not shown.
<li><a href="#USER_OPTIONS_ROLL_KEY">USER_OPTIONS_ROLL_KEY</a>
    - The opposite action from the system default for roll keys is taken.
<li><a href="#USER_OPTIONS_PRINT_COMPLETE_MESSAGE">USER_OPTIONS_PRINT_COMPLETE_MESSAGE</a>
    - A message is sent to the user when a spooled file is printed.
</ul>
**/
    public static final String USER_OPTIONS                              = "USER_OPTIONS";

    /**
    Attribute value indicating that keywords are shown when a CL command is displayed.

    @see #USER_OPTIONS
    **/
    public static final String USER_OPTIONS_KEYWORDS                    = "*CLKWD";

    /**
    Attribute value indicating that more detailed information is shown when the user is defining or changing
    the system using edit or display object authority.

    @see #USER_OPTIONS
    **/
    public static final String USER_OPTIONS_EXPERT                      = "*EXPERT";

    /**
    Attribute value indicating that UIM online help is to be displayed on a full screen or a window.

    @see #USER_OPTIONS
    **/
    public static final String USER_OPTIONS_FULL_SCREEN_HELP            = "*HLPFULL";

    /**
    Attribute value indicating that status messages sent to the user are shown.

    @see #USER_OPTIONS
    **/
    public static final String USER_OPTIONS_STATUS_MESSAGE              = "*STSMSG";

    /**
    Attribute value indicating that status messages sent to the user are not shown.

    @see #USER_OPTIONS
    **/
    public static final String USER_OPTIONS_NO_STATUS_MESSAGE           = "*NOSTSMSG";

    /**
    Attribute value indicating that the opposite action from the system default for roll keys is taken.

    @see #USER_OPTIONS
    **/
    public static final String USER_OPTIONS_ROLL_KEY                    = "*ROLLKEY";

    /**
    Attribute value indicating that a message is sent to the user when a spooled file is printed.

    @see #USER_OPTIONS
    **/
    public static final String USER_OPTIONS_PRINT_COMPLETE_MESSAGE      = "*PRTMSG";

    static {
        String[] possibleValues = new String[] {USER_OPTIONS_KEYWORDS,
                                                USER_OPTIONS_EXPERT,
                                                USER_OPTIONS_FULL_SCREEN_HELP,
                                                USER_OPTIONS_STATUS_MESSAGE,
                                                USER_OPTIONS_NO_STATUS_MESSAGE,
                                                USER_OPTIONS_ROLL_KEY,
                                                USER_OPTIONS_PRINT_COMPLETE_MESSAGE};
        attributes_.add(USER_OPTIONS, String.class, false,
                        possibleValues, null, true, true);
        ValueMap valueMap = new OptionsValueMap('N', 'Y', possibleValues);
        getterMap_.add(USER_OPTIONS, USRI0300_, "receiverVariable.userOptions", valueMap);
        setterMap_.add(USER_OPTIONS, CHGUSRPRF_, "USROPT", arrayValueMapNone_); //@B1C
    }



/**
Attribute ID for user profile name.  This identifies a read-only String
attribute, which represents the name of the user profile.
**/
    public static final String USER_PROFILE_NAME                              = "USER_PROFILE_NAME";

    static {
        attributes_.add(USER_PROFILE_NAME, String.class, true);
        getterMap_.add(USER_PROFILE_NAME, USRI0100_, "receiverVariable.userProfileName");
        getterMap_.add(USER_PROFILE_NAME, USRI0200_, "receiverVariable.userProfileName");
        getterMap_.add(USER_PROFILE_NAME, USRI0300_, "receiverVariable.userProfileName");
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_      = "com.ibm.as400.resource.RUser";
    private static ProgramCallDocument      staticDocument_     = null;

    static {
        // Create a static version of the PCML document, then clone it for each document.
        // This will improve performance, since we will only have to deserialize the PCML
        // object once.
        try {
            staticDocument_ = new ProgramCallDocument();
            staticDocument_.setDocument(DOCUMENT_NAME_);
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error instantiating ProgramCallDocument", e);
        }
    }



//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private String                          name_               = null;

    private ProgramAttributeGetter          attributeGetter_    = null;
    private CommandAttributeSetter          attributeSetter_    = null;




//-----------------------------------------------------------------------------------------
// Constructors.
//-----------------------------------------------------------------------------------------

/**
Constructs an RUser object.
**/
    public RUser()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);
    }



/**
Constructs an RUser object.

@param system   The system.
@param name     The user profile name.
**/
    public RUser(AS400 system,String name)
    {
        this();

        try {
            setSystem(system);
            setName(name);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



// @A2C
/**
Commits the specified attribute changes.

@param attributeIDs     The attribute IDs for the specified attribute changes.
@param values           The specified attribute changes
@param bidiStringTypes  The bidi string types as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
                        
@exception ResourceException                If an error occurs.
**/
    protected void commitAttributeChanges(Object[] attributeIDs, Object[] values, int[] bidiStringTypes)
    throws ResourceException
    {
        super.commitAttributeChanges(attributeIDs, values);

        // Establish the connection if needed.
        if (! isConnectionEstablished())
            establishConnection();

        attributeSetter_.setValues(attributeIDs, values, bidiStringTypes); // @A2C
    }




/**
Computes a resource key.

@param system       The system.
@param name         The user profile name.
@return             The resource key.
**/
    static Object computeResourceKey(AS400 system, String name)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(RUser.class);
        buffer.append(':');
        buffer.append(system.getSystemName());
        buffer.append(':');
        buffer.append(system.getUserId());
        buffer.append(':');
        buffer.append(name);
        return buffer.toString();
    }



/**
Deletes the user.

@exception ResourceException                If an error occurs.
**/
    public void delete()
        throws ResourceException
    {
        // Establish the connection if needed.
        if (!isConnectionEstablished())
            establishConnection();

        try //@B1M
        {
            fireBusy();
        // Remove the directory entry.
        StringBuffer buffer = new StringBuffer("RMVDIRE USRID(");
        //@B1D buffer.append(name_);
        buffer.append(getAttributeValue(RUser.USER_ID)); //@B1A
        buffer.append(' '); //@B1A
        buffer.append(getAttributeValue(RUser.USER_ADDRESS)); //@B1A
        buffer.append(") USRD("); //@B1C
        String desc = (String)getAttributeValue(RUser.USER_DESCRIPTION); //@B1A
        if (desc == "") desc = "*FIRST"; //@B1A
        buffer.append(desc); //@B1A
        buffer.append(')'); //@B1A
            CommandCall rmvdire = new CommandCall(getSystem(), buffer.toString());
            if (rmvdire.run() == false)
                throw new ResourceException(rmvdire.getMessageList());
        }
        catch (Exception e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when removing a directory entry", e);
            // Don't throw an exception here... maybe there is no directory entry.
        }
        finally
        {
            fireIdle();
        }

        // Delete the user profile.
        StringBuffer buffer = new StringBuffer("DLTUSRPRF USRPRF(");
        buffer.append(name_);
        buffer.append(')');
        try
        {
            fireBusy();
            CommandCall dltusrprf = new CommandCall(getSystem(), buffer.toString());
            if (dltusrprf.run() == false)
                throw new ResourceException(dltusrprf.getMessageList());
            fireResourceDeleted();
        }
        catch (Exception e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when deleting a user profile", e);
            throw new ResourceException(e);
        }
        finally
        {
            fireIdle();
        }

    }




/**
Establishes the connection to the AS/400.

<p>The method is called by the resource framework automatically
when the connection needs to be established.

@exception ResourceException                If an error occurs.
**/
    protected void establishConnection()
    throws ResourceException
    {
        // Call the superclass.
        super.establishConnection();

        // Validate if we can establish the connection.
        if (name_ == null)
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Update the PCML document.
        String nameUpper = name_.toUpperCase();
        AS400 system = getSystem();
        ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
        try {
            document.setSystem(system);
            document.setValue("qsyrusri_usri0100.userProfileName", nameUpper);
            document.setValue("qsyrusri_usri0200.userProfileName", nameUpper);
            document.setValue("qsyrusri_usri0300.userProfileName", nameUpper);
            document.setValue("qokschd.requestVariable.searchRequestArray.valueToMatch", INDEX_0_, nameUpper);
            document.setIntValue("qokschd.requestVariableLength", document.getOutputsize("qokschd.requestVariable"));
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting PCML document values", e);
        }

        // Initialize the attribute getter.
        attributeGetter_ = new ProgramAttributeGetter(system, document, getterMap_);

        // Initialize the attribute setter.
        attributeSetter_ = new CommandAttributeSetter(system, setterMap_);
        attributeSetter_.setParameterValue(CHGUSRPRF_, USRPRF_PARAMETER_, nameUpper);
        attributeSetter_.setParameterValue(CHGUSRAUD_, USRPRF_PARAMETER_, nameUpper);
        
        String address = ""; //@B1A
        String userid = ""; //@B1A
        String description = ""; //@B1A
        try //@B1A
        {
          address = (String)attributeGetter_.getValue(USER_ADDRESS); //@B1A
          userid = (String)attributeGetter_.getValue(USER_ID); //@B1A
          description = (String)attributeGetter_.getValue(USER_DESCRIPTION); //@B1A
        }
        catch(Exception e) //@B1A
        {
          if (Trace.isTraceOn()) //@B1A
          {
            Trace.log(Trace.ERROR, "Error retrieving directory entry user id and address.", e); //@B1A
          }
        }

        //@B1D String address;                                                                             // @A3A
        if (address == "") //@B1A
        {
        if (!system.isLocal())                                                                      // @A3A
            address = system.getSystemName();                                                       // @A3A
        else {                                                                                      // @A3A
            try {                                                                                   // @A3A
                String hostName = InetAddress.getLocalHost().getHostName();                         // @A3A
                int dot = hostName.indexOf('.');                                                    // @A3A
                if (dot == -1)                                                                      // @A3A
                    address = hostName;                                                             // @A3A
                else                                                                                // @A3A
                    address = hostName.substring(0, dot);                                           // @A3A
            }                                                                                       // @A3A
            catch(IOException e) {                                                                  // @A3A
                throw new ResourceException(e);                                                     // @A3A
            }                                                                                       // @A3A
        }                                                                                           // @A3A
        }
        if (userid == "") //@B1A
        {
          userid = nameUpper; //@B1A
        }
        
        if (description != "") //@B1A
        {
          attributeSetter_.setParameterValue(CHGDIRE_, USRD_PARAMETER_, "'"+description+"' '"+description+"'"); //@B1A
        }

        //@B1:
        // Note that a directory entry is uniquely identified by its 8-character user id, 8-character address,
        // and 50-character description (because there can be duplicate userid/address pairs, but each
        // description must be unique for a given userid/address pair).
        // Also note that the user profile name can be up to 10 characters, and is not necessarily equal to
        // the directory entry user id.

        //@B1D attributeSetter_.setParameterValue(CHGDIRE_, USRID_PARAMETER_, nameUpper + " " + address);  // @A3C
        attributeSetter_.setParameterValue(CHGDIRE_, USRID_PARAMETER_, userid+" "+address);
    }


/**
Freezes any property changes.  After this is called, property
changes should not be made.  Properties are not the same thing
as attributes.  Properties are basic pieces of information
which must be set to make the object usable, such as the system
and the name.

<p>The method is called by the resource framework automatically
when the properties need to be frozen.

@exception ResourceException                If an error occurs.
**/
    protected void freezeProperties()
    throws ResourceException
    {
        // Validate if we can establish the connection.
        if (name_ == null)
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Update the presentation.
        Presentation presentation = getPresentation();
        presentation.setName(name_);

        // Update the resource key.
        if (getResourceKey() == null)
            setResourceKey(computeResourceKey(getSystem(), name_));

        // Call the superclass.
        super.freezeProperties();
    }



// @A2C
/**
Returns the unchanged value of an attribute.   If the attribute
value has an uncommitted change, this returns the unchanged value.
If the attribute value does not have an uncommitted change, this
returns the same value as <b>getAttributeValue()</b>.

@param attributeID  Identifies the attribute.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 

@return             The attribute value, or null if the attribute
                    value is not available.

@exception ResourceException                If an error occurs.
**/
    public Object getAttributeUnchangedValue(Object attributeID, int bidiStringType)
    throws ResourceException
    {
        Object value = super.getAttributeUnchangedValue(attributeID, bidiStringType);
        if (value == null) {

            // Establish the connection if needed.
            if (! isConnectionEstablished())
                establishConnection();

            value = attributeGetter_.getValue(attributeID, bidiStringType); 
        }
        return value;
    }




/**
Returns the user profile name.

@return The user profile name.
**/
    public String getName()
    {
        return name_;
    }


//@B0A
  /**
   * Indicates if this user profile has been granted the specified authority, or
   * belongs to a group profile that has been granted the specified authority.
   * @param authority The authority to check. It must be one of the special authority
   * constants:
<ul>
<li><a href="#SPECIAL_AUTHORITIES_ALL_OBJECT">SPECIAL_AUTHORITIES_ALL_OBJECT</a>
    - All object.
<li><a href="#SPECIAL_AUTHORITIES_SECURITY_ADMINISTRATOR">SPECIAL_AUTHORITIES_SECURITY_ADMINISTRATOR</a>
    - Security administrator.
<li><a href="#SPECIAL_AUTHORITIES_JOB_CONTROL">SPECIAL_AUTHORITIES_JOB_CONTROL</a>
    - Job control.
<li><a href="#SPECIAL_AUTHORITIES_SPOOL_CONTROL">SPECIAL_AUTHORITIES_SPOOL_CONTROL</a>
    - Spool control.
<li><a href="#SPECIAL_AUTHORITIES_SAVE_SYSTEM">SPECIAL_AUTHORITIES_SAVE_SYSTEM</a>
    - Save system.
<li><a href="#SPECIAL_AUTHORITIES_SERVICE">SPECIAL_AUTHORITIES_SERVICE</a>
    - Service.
<li><a href="#SPECIAL_AUTHORITIES_AUDIT">SPECIAL_AUTHORITIES_AUDIT</a>
    - Audit.
<li><a href="#SPECIAL_AUTHORITIES_IO_SYSTEM_CONFIGURATION">SPECIAL_AUTHORITIES_IO_SYSTEM_CONFIGURATION</a>
    - Input/output system configuration.
</ul>
   * @return true if this user has the authority or belongs to a group that has
   * the authority; false otherwise.
   * @exception ResourceException If an error occurs.
  **/
  public boolean hasSpecialAuthority(String authority) throws ResourceException
  {
    if (authority == null) throw new NullPointerException("authority");
    
    // Check to see if this user is authorized.
    String[] specialAuthorities = (String[])getAttributeValue(SPECIAL_AUTHORITIES);
    if (specialAuthorities != null)
    {
      for (int i=0; i<specialAuthorities.length; ++i)
      {
        if (specialAuthorities[i].equals(authority))
        {
          return true;
        }
      }
    }
    // Check to see if a group this user belongs to is authorized.
    String primaryGroup = (String)getAttributeValue(GROUP_PROFILE_NAME);
    if (primaryGroup != null && !primaryGroup.equals(NONE))
    {
      RUser group = new RUser(getSystem(), primaryGroup);
      if (group.hasSpecialAuthority(authority))
      {
        return true;
      }
    }
    // Check the supplemental groups.
    String[] supplementalGroups = (String[])getAttributeValue(SUPPLEMENTAL_GROUPS);
    if (supplementalGroups != null)
    {
      for (int i=0; i<supplementalGroups.length; ++i)
      {
        RUser group = new RUser(getSystem(), supplementalGroups[i]);
        if (group.hasSpecialAuthority(authority))
        {
          return true;
        }
      }
    }
    return false; // Not authorized.
  }


// @A2A
/**
Indicates if this resource is enabled for bidirectional character conversion.
This always returns true.

@return Always true.
**/
    protected boolean isBidiEnabled()
    {
        return true;
    }



/**
Refreshes the values for all attributes.  This does not cancel
uncommitted changes.  This method fires an attributeValuesRefreshed()
ResourceEvent.

@exception ResourceException                If an error occurs.
**/
    public void refreshAttributeValues()
    throws ResourceException
    {
        super.refreshAttributeValues();

        if (attributeGetter_ != null)
            attributeGetter_.clearBuffer();
    }



/**
Sets the user profile name.  This does not change the user profile on
the AS/400.  Instead, it changes the user profile to which
this object references.  This cannot be changed
if the object has established a connection to the AS/400.

@param name    The user profile name.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setName(String name)
        throws PropertyVetoException
    {
        if (name == null)
            throw new NullPointerException("name");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = name_;
        fireVetoableChange("name", oldValue, name);
        name_ = name;
        firePropertyChange("name", oldValue, name);
    }



}
