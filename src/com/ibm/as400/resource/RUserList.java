///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RUserList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;



/**
The RUserList class represents a list of AS/400 users.

<a name="selectionIDs"><p>The following selection IDs are supported:
<ul>
<li>{@link #SELECTION_CRITERIA SELECTION_CRITERIA}
<li>{@link #GROUP_PROFILE GROUP_PROFILE}
<li>{@link #USER_PROFILE USER_PROFILE}
</ul>
</a>

<p>Use one or more of these selection IDs with
{@link com.ibm.as400.resource.ResourceList#getSelectionValue getSelectionValue()}
and {@link com.ibm.as400.resource.ResourceList#setSelectionValue setSelectionValue()}
to access the selection values for an RUserList.

<p>RUserList objects generate {@link com.ibm.as400.resource.RUser RUser} objects.

<blockquote><pre>
// Create an RUserList object to represent a list of users.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RUserList userList = new RUserList(system);
<br>
// Set the selection so that only user profiles
// that are group profiles are included in the list.
userList.setSelectionValue(RUserList.SELECTION_CRITERIA, RUserList.GROUP);
<br>
// Open the list and wait for it to complete.
userList.open();
userList.waitForComplete();
<br>
// Read and print the user profile names and text
// descriptions for the users in the list.
long numberOfUsers = userList.getListLength();
for(long i = 0; i &lt; numberOfUsers; ++i)
{
    RUser user = (RUser)userList.resourceAt(i);
    System.out.println(user.getAttributeValue(RUser.USER_PROFILE_NAME));
    System.out.println(user.getAttributeValue(RUser.TEXT_DESCRIPTION));
    System.out.println();
}
<br>
// Close the list.
userList.close();
</pre></blockquote>

@see RUser
**/
public class RUserList
extends SystemResourceList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Selection values.
//-----------------------------------------------------------------------------------------

/**
Selection value indicating that the list contains all user profiles
and group profiles.
**/
    public static final String ALL = "*ALL";

/**
Selection value indicating that the list contains only user profiles
that are not group profiles.  These are user profiles that do not have
a group identifier specified.
**/
    public static final String USER = "*USER";

/**
Selection value indicating that the list contains only user profiles
that are group profiles.  These are user profiles that have a group
identifier specified.
**/
    public static final String GROUP = "*GROUP";

/**
Selection value indicating that the list contains only user profiles
that are members of a specified group.
**/
    public static final String MEMBER = "*MEMBER";


/**
Selection value indicating that no group profile is specified.
**/
    public static final String NONE = "*NONE";

/**
Selection value indicating that the list contains only user profiles
that are not group profiles.  These are user profiles that do not have
a group identifier specified.
**/
    public static final String NOGROUP = "*NOGROUP";




//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static final String                 PRESENTATION_KEY_           = "USER_LIST";
    private static final String                 ICON_BASE_NAME_             = "RUserList";
    private static PresentationLoader           presentationLoader_         = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");




//-----------------------------------------------------------------------------------------
// Selection IDs.
//
// * If you add a selection here, make sure and add it to the class javadoc
//   and in ResourceMRI.java.
//-----------------------------------------------------------------------------------------

    private static ResourceMetaDataTable selections_        = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);
    private static ProgramMap            selectionMap_      = new ProgramMap();



/**
Selection ID for selection criteria.  This identifies a String selection,
which represents which users are returned.  Possible values are:
<ul>
<li>{@link #ALL ALL} - All user profiles and group profiles are
    returned.
<li>{@link #USER USER} - Only user profiles that are not group
    profiles are returned.  These are user profiles that do not have
    a group identifier specified.
<li>{@link #GROUP GROUP} - Only user profiles that are group
    profiles are returned.  These are user profiles that have
    a group identifier specified.
<li>{@link #MEMBER MEMBER} - User profiles that are members
    of the group specified for the {@link #GROUP_PROFILE GROUP_PROFILE}
    selection value are returned.
</ul>
**/
    public static final String SELECTION_CRITERIA                      = "SELECTION_CRITERIA";

    static {
        selections_.add(SELECTION_CRITERIA, String.class, false,
                        new String[] { ALL, USER, GROUP, MEMBER }, ALL, true);
        selectionMap_.add(SELECTION_CRITERIA, null, "selectionCriteria");
    }



/**
Selection ID for group profile.  This identifies a String selection,
which represents the group profile whose members are to be returned.
Possible values are:
<ul>
<li>{@link #NONE NONE} - No group profile is specified.
<li>{@link #NOGROUP NOGROUP} - Users who are not a member of
    any group are returned.
<li>The group profile name - Users who are a member of this group are
    returned.
</ul>

<p>This must be set to a group profile name or {@link #NOGROUP NOGROUP}
if {@link #SELECTION_CRITERIA SELECTION_CRITERIA} is set to
{@link #MEMBER MEMBER}.  This must be set to {@link #NONE NONE}
if {@link #SELECTION_CRITERIA SELECTION_CRITERIA} is not set to
{@link #MEMBER MEMBER}.
**/
    public static final String GROUP_PROFILE                      = "GROUP_PROFILE";

    static {
        selections_.add(GROUP_PROFILE, String.class, false,
                        new String[] { NONE, NOGROUP }, NONE, false);
        selectionMap_.add(GROUP_PROFILE, null, "groupProfileName", new UpperCaseValueMap());
    }




/**
Selection ID for user profile.  This identifies a String selection,
which represents the user profile specification that describes the
users to be included in the list.  The user profile can be specified
only when connecting to servers running OS/400 V5R1 or later.
Possible values are:
<ul>
<li>{@link #ALL ALL} - All users are specified.
<li>The generic user profile name.  A generic name is
    a String which contains one or more characters followed by
    an '*'.
<li>The user profile name.
</ul>
**/
    public static final String USER_PROFILE                      = "USER_PROFILE";

    static {
        ResourceMetaData rmd = selections_.add(USER_PROFILE, String.class, false,
                                                new String[] { ALL }, ALL, false);
        rmd.setLevel(new ResourceLevel(ResourceLevel.V5R1M0));
        selectionMap_.add(USER_PROFILE, null, "profileName", new UpperCaseValueMap());
    }




//-----------------------------------------------------------------------------------------
// Open list attribute map.
//-----------------------------------------------------------------------------------------

    private static ProgramMap            openListAttributeMap_  = new ProgramMap();
    private static final String          openListProgramName_    = "qgyolaus";

    static {
        openListAttributeMap_.add(RUser.USER_PROFILE_NAME, null, "receiverVariable.profileName");
        openListAttributeMap_.add(RUser.GROUP_MEMBER_INDICATOR, null, "receiverVariable.userOrGroupIndicator", new BooleanValueMap("0", "1"));
        openListAttributeMap_.add(RUser.TEXT_DESCRIPTION, null, "receiverVariable.textDescription");
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_      = "com.ibm.as400.resource.RUserList";
    private static ProgramCallDocument      staticDocument_     = null;
    private static final String             formatName_         = "autu0150";

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

    private static final String profileNameDataName_            = ".receiverVariable.profileName";
    private static final String userOrGroupIndicatorDataName_   = ".receiverVariable.userOrGroupIndicator";



//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs an RUserList object.
**/
    public RUserList()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_),
              RUser.attributes_,
              selections_,
              null,
              openListProgramName_,
              formatName_,
              selectionMap_);
    }



/**
Constructs an RUserList object.

@param system   The system.
**/
    public RUserList(AS400 system)
    {
        this();
        try {
            setSystem(system);
        }
        catch(PropertyVetoException e) {
            // Ignore.
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

        // Initialize the PCML document.
        setDocument((ProgramCallDocument)staticDocument_.clone());
        setOpenListProgramName(openListProgramName_);
    }




//-----------------------------------------------------------------------------------------
// List implementation.
//-----------------------------------------------------------------------------------------

    Resource newResource(String programName, int[] indices)
    throws PcmlException, ResourceException
    {
        ProgramCallDocument document = getDocument();

        String name = (String)document.getValue(programName + profileNameDataName_, indices);
        AS400 system = getSystem();
        Object resourceKey = RUser.computeResourceKey(system, name);
        RUser resource = (RUser)ResourcePool.GLOBAL_RESOURCE_POOL.getResource(resourceKey);
        if (resource == null) {
            try {
                resource = new RUser(system, name);
                resource.setResourceKey(resourceKey);
                resource.freezeProperties();
            }
            catch(Exception e) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Exception while creating user from user list", e);
                throw new ResourceException(e);
            }
        }

        // Copy the information from the API record to the RUser attributes.
        Object[] attributeIDs = openListAttributeMap_.getIDs();
        Object[] values = openListAttributeMap_.getValues(attributeIDs, system, document, programName, indices);
        for(int i = 0; i < values.length; ++i) {
            resource.initializeAttributeValue(attributeIDs[i], values[i]);
        }

        return resource;
    }



    void setOpenParameters(ProgramCallDocument document)
        throws PcmlException, ResourceException
    {
        // If a group profile is specified, but no selection criteria is specified,
        // then default the selection criteria to MEMBER.
        String groupProfile = (String)getSelectionValue(GROUP_PROFILE);
        if (groupProfile != null) {
            if (!groupProfile.equals(NONE)) {
                if (getSelectionValue(SELECTION_CRITERIA) == null)
                    setSelectionValue(SELECTION_CRITERIA, MEMBER);
            }
        }

        super.setOpenParameters(document);
    }

}
