///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RSoftwareResource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;



/**
The RSoftwareResource class represents an AS/400 software product.

<a name="attributeIDs"><p>The following attribute IDs are supported:
<ul>
<li><a href="#LEVEL">LEVEL</a>
<li><a href="#LOAD_ERROR_INDICATOR">LOAD_ERROR_INDICATOR</a>
<li><a href="#LOAD_ID">LOAD_ID</a>
<li><a href="#LOAD_STATE">LOAD_STATE</a>
<li><a href="#LOAD_TYPE">LOAD_TYPE</a>
<li><a href="#MINIMUM_BASE_VRM">MINIMUM_BASE_VRM</a>
<li><a href="#MINIMUM_TARGET_RELEASE">MINIMUM_TARGET_RELEASE</a>
<li><a href="#PRIMARY_LANGUAGE_LOAD_ID">PRIMARY_LANGUAGE_LOAD_ID</a>
<li><a href="#PRODUCT_ID">PRODUCT_ID</a>
<li><a href="#PRODUCT_OPTION">PRODUCT_OPTION</a>
<li><a href="#REGISTRATION_TYPE">REGISTRATION_TYPE</a>
<li><a href="#REGISTRATION_VALUE">REGISTRATION_VALUE</a>
<li><a href="#RELEASE_LEVEL">RELEASE_LEVEL</a>
<li><a href="#REQUIREMENTS_MET">REQUIREMENTS_MET</a>
<li><a href="#SUPPORTED_FLAG">SUPPORTED_FLAG</a>
<li><a href="#SYMBOLIC_LOAD_STATE">SYMBOLIC_LOAD_STATE</a>
</ul>

<p>Use any of these attribute IDs with
<a href="ChangeableResource.html#getAttributeValue(java.lang.Object)">getAttributeValue()</a>
to access the attribute values for an RSoftwareResource.

<blockquote><pre>
// Create an RSoftwareResource object to refer to a specific software product.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RSoftwareResource product = new RSoftwareResource(system, "5722JC1");
<br>
// Get the load error indicator.
String loadErrorIndicator = (String)product.getAttributeValue(RSoftwareResource.LOAD_ERROR_INDICATOR);
</pre></blockquote>
**/
//
// Implementation notes:
//
// 1.  This class is not complete.  It should include the other formats for the
//     API as well as a complementing list class. 
//
// 2.  We should get a bean info and a icon for this.
//
public class RSoftwareResource
extends ChangeableResource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");
    private static final String         PRESENTATION_KEY_   = "SOFTWARE_RESOURCE";



//-----------------------------------------------------------------------------------------
// Attribute IDs.
//
// * If you add an attribute here, make sure and add it to the class javadoc.
//-----------------------------------------------------------------------------------------

    // Private data.
            static ResourceMetaDataTable        attributes_             = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);
    private static ProgramMap                   getterMap_              = new ProgramMap();

    private static final String                 QSZRTVPR_               = "qszrtvpr";



/**
Attribute ID for level.  This identifies a read-only String attribute, 
which represents the level identifier of the product.  The format is
<code>L<em>xx</em></code> for the operating system and Licensed Internal
Code, or "" for all other products.
**/
    public static final String LEVEL                      = "LEVEL";

    static {
        attributes_.add(LEVEL, String.class, true);
        getterMap_.add(LEVEL, QSZRTVPR_, "receiver.level");
    }



/**
Attribute ID for load error indicator.  This identifies a read-only String attribute, 
which indicates if there is a known error for this load.  Possible values are:
<ul>
<li><a href="#LOAD_ERROR_INDICATOR_ERROR">LOAD_ERROR_INDICATOR_ERROR</a>
    - An error was found the last time that the state of this load was checked
      or updated.
<li><a href="#LOAD_ERROR_INDICATOR_NONE">LOAD_ERROR_INDICATOR_NONE</a>
    - No error was found the last time that the state of this load was checked
      or updated.
</ul>
**/
    public static final String LOAD_ERROR_INDICATOR                      = "LOAD_ERROR_INDICATOR";

    /**
    Attribute value indicating that an error was found the last time that the state 
    of this load was checked or updated.

    @see #LOAD_ERROR_INDICATOR_ERROR
    **/
    public static final String LOAD_ERROR_INDICATOR_ERROR                = "*ERROR";

    /**
    Attribute value indicating that no error was found the last time that the state 
    of this load was checked or updated.

    @see #LOAD_ERROR_INDICATOR_NONE
    **/
    public static final String LOAD_ERROR_INDICATOR_NONE                 = "*NONE";

    static {
        attributes_.add(LOAD_ERROR_INDICATOR, String.class, true,
                        new Object[] { LOAD_ERROR_INDICATOR_ERROR, LOAD_ERROR_INDICATOR_NONE }, null, true);
        getterMap_.add(LOAD_ERROR_INDICATOR, QSZRTVPR_, "receiver.loadErrorIndicator");
    }



/**
Attribute ID for load ID.  This identifies a read-only String attribute, 
which represents the load ID of the product load for which information was
returned.  Possible values are:
<ul>
<li>"5050" - The code load.
<li>A load ID.
</ul>
**/
    public static final String LOAD_ID                      = "LOAD_ID";

    /**
    Attribute value indicating the code load.                    
    
    @see #LOAD_ID
    **/
    public static final String LOAD_ID_CODE                = "*CODE";

    static {
        attributes_.add(LOAD_ID, String.class, true,
                        new Object[] { "5050" }, null, false);
        getterMap_.add(LOAD_ID, QSZRTVPR_, "receiver.loadID");
    }



/**
Attribute ID for load state.  This identifies a read-only String attribute, 
which represents the state of the load.  
**/
    public static final String LOAD_STATE               = "LOAD_STATE";

    static {
        attributes_.add(LOAD_STATE, String.class, true);
        getterMap_.add(LOAD_STATE, QSZRTVPR_, "receiver.loadState");
    }



/**
Attribute ID for load type.  This identifies a read-only String attribute, 
which represents the type of the load.  Possible values are:
<ul>
<li><a href="#LOAD_TYPE_CODE">LOAD_TYPE_CODE</a> - The load is a code load.
<li><a href="#LOAD_TYPE_LANGUAGE">LOAD_TYPE_LANGUAGE</a> - The load is a language load.
</ul>
**/
    public static final String LOAD_TYPE                      = "LOAD_TYPE";

    /**
    Attribute value indicating that the load is a code load.
    
    @see #LOAD_TYPE
    **/
    public static final String LOAD_TYPE_CODE                = "*CODE";

    /**
    Attribute value indicating that the load is a language load.
    
    @see #LOAD_TYPE
    **/
    public static final String LOAD_TYPE_LANGUAGE                 = "*LNG";

    static {
        attributes_.add(LOAD_TYPE, String.class, true,
                        new Object[] { LOAD_TYPE_CODE, LOAD_TYPE_LANGUAGE }, null, true);
        getterMap_.add(LOAD_TYPE, QSZRTVPR_, "receiver.loadType");
    }



/**
Attribute ID for minimum base VRM.  This identifies a read-only String attribute, 
which represents the minimum release level that is allowed for the *BASE option
that will run with the current level of the option for the product.  Possible values are:
<ul>
<li><a href="#MINIMUM_BASE_VRM_MATCH">MINIMUM_BASE_VRM_MATCH</a> - The release of the option
    matches that of the base.
<li>The release value in the form <code>V<em>x</em>R<em>x</em>M<em>x</em></code>.
</ul>
**/
    public static final String MINIMUM_BASE_VRM                      = "MINIMUM_BASE_VRM";

    /**
    Attribute value indicating the release of the option matches that of the base.
    
    @see #MINIMUM_BASE_VRM
    **/
    public static final String MINIMUM_BASE_VRM_MATCH                = "*MATCH";

    static {
        attributes_.add(MINIMUM_BASE_VRM, String.class, true,
                        new Object[] { MINIMUM_BASE_VRM_MATCH }, null, false );
        getterMap_.add(MINIMUM_BASE_VRM, QSZRTVPR_, "receiver.minimumVRMofBaseRequiredOption");
    }



/**
Attribute ID for minimum target release.  This identifies a read-only String attribute, 
which represents the minimum release of the operating system to which the Save
Licensed Program (SAVLICPGM) command will allow the product to be saved, 
in the form <code>V<em>x</em>R<em>x</em>M<em>x</em></code>.
**/
    public static final String MINIMUM_TARGET_RELEASE               = "MINIMUM_TARGET_RELEASE";

    static {
        attributes_.add(MINIMUM_TARGET_RELEASE, String.class, true);
        getterMap_.add(MINIMUM_TARGET_RELEASE, QSZRTVPR_, "receiver.minimumTargetRelease");
    }



/**
Attribute ID for primary language load ID.  This identifies a read-only String attribute, 
which represents the primary language of the product option for code loads, or "" if
no language is installed, or for language loads.
**/
    public static final String PRIMARY_LANGUAGE_LOAD_ID               = "PRIMARY_LANGUAGE_LOAD_ID";

    static {
        attributes_.add(PRIMARY_LANGUAGE_LOAD_ID, String.class, true);
        getterMap_.add(PRIMARY_LANGUAGE_LOAD_ID, QSZRTVPR_, "receiver.primaryLanguageLoadID");
    }



/**
Attribute ID for product ID.  This identifies a read-only String attribute, 
which represents the product ID. 
**/
    public static final String PRODUCT_ID               = "PRODUCT_ID";

    /**
    Attribute value indicating the product ID for the operating system.
    
    @see #PRODUCT_ID
    **/
    public static final String PRODUCT_ID_OPERATING_SYSTEM                 = "*OPSYS";

    static {
        attributes_.add(PRODUCT_ID, String.class, true );
        getterMap_.add(PRODUCT_ID, QSZRTVPR_, "receiver.productID");
    }



/**
Attribute ID for product option.  This identifies a read-only String attribute, 
which represents the product option.  Possible values are:
<ul>
<li><a href="#PRODUCT_OPTION_BASE">PRODUCT_OPTION_BASE</a> - The 
    base option.
<li>A product option.
</ul>
**/
    public static final String PRODUCT_OPTION               = "PRODUCT_OPTION";

    /**
    Attribute value indicating the base option.
    
    @see #PRODUCT_OPTION
    **/
    public static final String PRODUCT_OPTION_BASE                 = "0000";

    static {
        attributes_.add(PRODUCT_OPTION, String.class, true,
                        new Object[] { PRODUCT_OPTION_BASE }, null, false );
        getterMap_.add(PRODUCT_OPTION, QSZRTVPR_, "receiver.productOption");
    }



/**
Attribute ID for registration type.  This identifies a read-only String attribute, 
which represents the registration type, which makes up part of the registration
ID of the product.
**/
    public static final String REGISTRATION_TYPE               = "REGISTRATION_TYPE";

    static {
        attributes_.add(REGISTRATION_TYPE, String.class, true);
        getterMap_.add(REGISTRATION_TYPE, QSZRTVPR_, "receiver.registrationType");
    }



/**
Attribute ID for registration value.  This identifies a read-only String attribute, 
which represents the registration value, which makes up part of the registration
ID of the product.
**/
    public static final String REGISTRATION_VALUE               = "REGISTRATION_VALUE";

    static {
        attributes_.add(REGISTRATION_VALUE, String.class, true);
        getterMap_.add(REGISTRATION_VALUE, QSZRTVPR_, "receiver.registrationValue");
    }



/**
Attribute ID for release level.  This identifies a read-only String attribute, 
which represents the release level.  
**/
    public static final String RELEASE_LEVEL               = "RELEASE_LEVEL";

    /**
    Attribute value indicating the release level of the currently installed operating
    system.
    
    @see #RELEASE_LEVEL
    **/
    public static final String RELEASE_LEVEL_CURRENT                 = "*CUR";

    /**
    Attribute value indicating the only release level
    for which a product load is found.
    
    @see #RELEASE_LEVEL
    **/
    public static final String RELEASE_LEVEL_ONLY                 = "*ONLY";

    /**
    Attribute value indicating the previous
    release with modification level 0 of the operating system.
    
    @see #RELEASE_LEVEL
    **/
    public static final String RELEASE_LEVEL_PREVIOUS                 = "*PRV";

    static {
        attributes_.add(RELEASE_LEVEL, String.class, true);
        getterMap_.add(RELEASE_LEVEL, QSZRTVPR_, "receiver.releaseLevel");
    }



/**
Attribute ID for requirements met.  This identifies a read-only String attribute, 
which indicates whether then product requirements are met between a base and option
value.  Possible values are:
<ul>
<li><a href="#REQUIREMENTS_MET_UNKNOWN">REQUIREMENTS_MET_UNKNOWN</a> - There is
    not enough information available to determine if the release requirements have
    been met, or if <a href="#LOAD_TYPE">LOAD_TYPE</a> is set to
    <a href="#LOAD_TYPE_LANGUAGE">LOAD_TYPE_LANGUAGE</a>.
<li><a href="#REQUIREMENTS_MET_ALL">REQUIREMENTS_MET_ALL</a> - The releases
    of the base and option meet all requirements.  
<li><a href="#REQUIREMENTS_MET_TOO_OLD_COMPARED_TO_BASE">REQUIREMENTS_MET_TOO_OLD_COMPARED_TO_BASE</a> - 
    The release of the option is too old compared to the base.
<li><a href="#REQUIREMENTS_MET_TOO_OLD_COMPARED_TO_OPTION">REQUIREMENTS_MET_TOO_OLD_COMPARED_TO_BASE</a> - 
    The release of the base is too old compared to the option.
</ul>
**/
    public static final String REQUIREMENTS_MET                      = "REQUIREMENTS_MET";

    /**
    Attribute value indicating that not enough information is available to determine 
    if the release requirements have been met.
    
    @see #REQUIREMENTS_MET
    **/
    public static final String REQUIREMENTS_MET_UNKNOWN              = "0";

    /**
    Attribute value indicating that the releases
    of the base and option meet all requirements.
    
    @see #REQUIREMENTS_MET
    **/
    public static final String REQUIREMENTS_MET_ALL                = "1";

    /**
    Attribute value indicating that the release of the option is too old compared to the base.
    
    @see #REQUIREMENTS_MET
    **/
    public static final String REQUIREMENTS_MET_TOO_OLD_COMPARED_TO_BASE             = "2";

    /**
    Attribute value indicating that the release of the base is too old compared to the option.
    
    @see #REQUIREMENTS_MET
    **/
    public static final String REQUIREMENTS_MET_TOO_OLD_COMPARED_TO_OPTION                = "3";

    static {
        attributes_.add(REQUIREMENTS_MET, String.class, true,
                        new Object[] { REQUIREMENTS_MET_UNKNOWN, 
                            REQUIREMENTS_MET_ALL, 
                            REQUIREMENTS_MET_TOO_OLD_COMPARED_TO_BASE, 
                            REQUIREMENTS_MET_TOO_OLD_COMPARED_TO_OPTION }, null, true);
        getterMap_.add(REQUIREMENTS_MET, QSZRTVPR_, "receiver.requirementsMetBetweenBaseAndOptionValue");
    }



/**
Attribute ID for requirements met.  This identifies a read-only Boolean attribute, 
which indicates whether this load is currently supported.
**/
    public static final String SUPPORTED_FLAG               = "SUPPORTED_FLAG";

    static {
        attributes_.add(SUPPORTED_FLAG, Boolean.class, true);
        getterMap_.add(SUPPORTED_FLAG, QSZRTVPR_, "receiver.supportedFlag", new BooleanValueMap("0", "1"));
    }



/**
Attribute ID for symbolic load state.  This identifies a read-only String attribute, 
which represents the symbolic state of the load.  Possible values are:
<ul>
<li><a href="#SYMBOLIC_LOAD_STATE_DEFINED">SYMBOLIC_LOAD_STATE_DEFINED</a> - The
    load is defined.  The product load object for this load does not exist.
<li><a href="#SYMBOLIC_LOAD_STATE_CREATED">SYMBOLIC_LOAD_STATE_CREATED</a> - The
    product load object for this load exists.
<li><a href="#SYMBOLIC_LOAD_STATE_PACKAGED">SYMBOLIC_LOAD_STATE_PACKAGED</a> - The
    product load object for this has been packaged.
<li><a href="#SYMBOLIC_LOAD_STATE_DAMAGED">SYMBOLIC_LOAD_STATE_DAMAGED</a> - The
    product load object for this has been damaged.
<li><a href="#SYMBOLIC_LOAD_STATE_LOADED">SYMBOLIC_LOAD_STATE_LOADED</a> - The
    product is being loaded or deleted.
<li><a href="#SYMBOLIC_LOAD_STATE_INSTALLED">SYMBOLIC_LOAD_STATE_INSTALLED</a> - The
    product load object was loaded.
</ul>
**/
    public static final String SYMBOLIC_LOAD_STATE                      = "SYMBOLIC_LOAD_STATE";

    /**
    Attribute value indicating that the
    load is defined.  The product load object for this load does not exist.
    
    @see #SYMBOLIC_LOAD_STATE
    **/
    public static final String SYMBOLIC_LOAD_STATE_DEFINED              = "*DEFINED";

    /**
    Attribute value indicating that the product load object for this load exists.
    
    @see #SYMBOLIC_LOAD_STATE
    **/
    public static final String SYMBOLIC_LOAD_STATE_CREATED              = "*CREATED";

    /**
    Attribute value indicating that the product load object has been packaged.
    
    @see #SYMBOLIC_LOAD_STATE
    **/
    public static final String SYMBOLIC_LOAD_STATE_PACKAGED              = "*PACKAGED";

    /**
    Attribute value indicating that the product load object has been damaged.
    
    @see #SYMBOLIC_LOAD_STATE
    **/
    public static final String SYMBOLIC_LOAD_STATE_DAMAGED              = "*DAMAGED";

    /**
    Attribute value indicating that the product load object is being loaded or deleted.
    
    @see #SYMBOLIC_LOAD_STATE
    **/
    public static final String SYMBOLIC_LOAD_STATE_LOADED              = "*LOADED";

    /**
    Attribute value indicating that the product load object was loaded.
    
    @see #SYMBOLIC_LOAD_STATE
    **/
    public static final String SYMBOLIC_LOAD_STATE_INSTALLED              = "*INSTALLED";

    static {
        attributes_.add(SYMBOLIC_LOAD_STATE, String.class, true,
                        new Object[] { SYMBOLIC_LOAD_STATE_DEFINED, 
                            SYMBOLIC_LOAD_STATE_CREATED, 
                            SYMBOLIC_LOAD_STATE_PACKAGED, 
                            SYMBOLIC_LOAD_STATE_DAMAGED, 
                            SYMBOLIC_LOAD_STATE_LOADED,
                            SYMBOLIC_LOAD_STATE_INSTALLED }, null, true);
        getterMap_.add(SYMBOLIC_LOAD_STATE, QSZRTVPR_, "receiver.symbolicLoadState");
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_      = "com.ibm.as400.resource.RSoftwareResource";
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

    private String                          productID_          = PRODUCT_ID_OPERATING_SYSTEM;
    private String                          releaseLevel_       = RELEASE_LEVEL_CURRENT;
    private String                          productOption_      = PRODUCT_OPTION_BASE;
    private String                          loadID_             = LOAD_ID_CODE;

    private ProgramAttributeGetter          attributeGetter_    = null;
    private ProgramCallDocument             document_           = null;


//-----------------------------------------------------------------------------------------
// Constructors.
//-----------------------------------------------------------------------------------------

/**
Constructs an RSoftwareResource object.
**/
    public RSoftwareResource()
    {
        super(presentationLoader_.getPresentation(PRESENTATION_KEY_), null, attributes_);
    }



/**
Constructs an RSoftwareResource object. 

@param system       The system.
@param productID    The product ID, or <a href="#PRODUCT_ID_OPERATING_SYSTEM">PRODUCT_ID_OPERATING_SYSTEM</a>
                    to refer to the operating system.
**/
    public RSoftwareResource(AS400 system, String productID)
    {
        this();

        try {
            setSystem(system);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }

        setProductID(productID);
    }


/**
Constructs an RSoftwareResource object.

@param system       The system.
@param productID    The product ID, or <a href="#PRODUCT_ID_OPERATING_SYSTEM">PRODUCT_ID_OPERATING_SYSTEM</a>
                    to refer to the operating system.
@param releaseLevel The release level.  Possible values are:
                    <ul>
                    <li><a href="#RELEASE_LEVEL_CURRENT">RELEASE_LEVEL_CURRENT</a> - Use the release level of
                        the currently installed operating system.                    
                    <li><a href="#RELEASE_LEVEL_ONLY">RELEASE_LEVEL_ONLY</a> - Use the only release level
                        for which a product load is found.  
                    <li><a href="#RELEASE_LEVEL_PREVIOUS">RELEASE_LEVEL_PREVIOUS</a> - Use the previous
                        release with modification level 0 of the operating system.
                    <li>A release level, in the form <code>V<em>x</em>R<em>x</em>M<em>x</em></code>.
                    </ul>
@param productOption The option number for which is being requested, or 
                    <a href="#PRODUCT_OPTION_BASE">PRODUCT_OPTION_BASE</a> for the base option.
**/
    public RSoftwareResource(AS400 system, String productID, String releaseLevel, String productOption)
    {
        this();

        try {
            setSystem(system);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }

        setProductID(productID);
        setReleaseLevel(releaseLevel);
        setProductOption(productOption);
    }


/**
Constructs an RSoftwareResource object.

@param system       The system.
@param productID    The product ID, or <a href="#PRODUCT_ID_OPERATING_SYSTEM">PRODUCT_ID_OPERATING_SYSTEM</a>
                    to refer to the operating system.
@param releaseLevel The release level.  Possible values are:
                    <ul>
                    <li><a href="#RELEASE_LEVEL_CURRENT">RELEASE_LEVEL_CURRENT</a> - Use the release level of
                        the currently installed operating system.                    
                    <li><a href="#RELEASE_LEVEL_ONLY">RELEASE_LEVEL_ONLY</a> - Use the only release level
                        for which a product load is found.  
                    <li><a href="#RELEASE_LEVEL_PREVIOUS">RELEASE_LEVEL_PREVIOUS</a> - Use the previous
                        release with modification level 0 of the operating system.
                    <li>A release level, in the form <code>V<em>x</em>R<em>x</em>M<em>x</em></code>.
                    </ul>
@param productOption The option number for which is being requested, or 
                    <a href="#PRODUCT_OPTION_BASE">PRODUCT_OPTION_BASE</a> for the base option.
@param loadID       The load ID, or <a href="#LOAD_ID_CODE">LOAD_ID_CODE</a> for the code load.                    
**/
    public RSoftwareResource(AS400 system, String productID, String releaseLevel, String productOption, String loadID)
    {
        this();

        try {
            setSystem(system);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }

        setProductID(productID);
        setReleaseLevel(releaseLevel);
        setProductOption(productOption);
        setLoadID(loadID);
    }




/**
Computes the resource key.

@param system           The system.
@param productID        The product ID.
@param releaseLevel     The release level.
@param productOption    The product option.
@param loadID           The load ID.
**/
    static Object computeResourceKey(AS400 system, String productID, String releaseLevel, String productOption, String loadID)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(RSoftwareResource.class);
        buffer.append(':');
        buffer.append(system.getSystemName());
        buffer.append(':');
        buffer.append(system.getUserId());
        buffer.append(':');
        buffer.append(productID);
        buffer.append(':');
        buffer.append(releaseLevel);
        buffer.append(':');
        buffer.append(productOption);
        buffer.append(':');
        buffer.append(loadID);
        return buffer.toString();
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
        document_ = (ProgramCallDocument)staticDocument_.clone();
        AS400 system = getSystem();
        try {
            document_.setSystem(system);
            document_.setValue("qszrtvpr.productInformation.productID", productID_);
            document_.setValue("qszrtvpr.productInformation.releaseLevel", releaseLevel_);
            document_.setValue("qszrtvpr.productInformation.productOption", productOption_);
            document_.setValue("qszrtvpr.productInformation.loadID", loadID_);
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting PCML document values", e);
        }

        // Initialize the attribute getter.
        attributeGetter_ = new ProgramAttributeGetter(system, document_, getterMap_);
    }


/**
Freezes any property changes.  After this is called, property
changes should not be made.  Properties are not the same thing
as attributes.  Properties are basic pieces of information
which must be set to make the object usable, such as the system
and path.

<p>The method is called by the resource framework automatically
when the properties need to be frozen.

@exception ResourceException                If an error occurs.
**/
    protected void freezeProperties()
    throws ResourceException
    {
        // Call the superclass.
        super.freezeProperties();

        // Update the presentation.
        Presentation presentation = getPresentation();
        presentation.setName(productID_);
        StringBuffer buffer = new StringBuffer(productID_);
        buffer.append('-');
        buffer.append(releaseLevel_);
        buffer.append('-');
        buffer.append(productOption_);
        buffer.append('-');
        buffer.append(loadID_);
        presentation.setFullName(buffer.toString());

        // Update the resource key.
        if (getResourceKey() == null)
            setResourceKey(computeResourceKey(getSystem(), productID_, releaseLevel_, productOption_, loadID_));
    }



/**
Returns the unchanged value of an attribute.   If the attribute
value has a uncommitted change, this returns the unchanged value.
If the attribute value does not have a uncommitted change, this
returns the same value as <b>getAttributeValue()</b>.

@param attributeID  Identifies the attribute.
@return             The attribute value, or null if the attribute
                    value is not available.

@exception ResourceException                If an error occurs.
**/
    public Object getAttributeUnchangedValue(Object attributeID)
    throws ResourceException
    {
        Object value = super.getAttributeUnchangedValue(attributeID);
        if (value == null) {

            // Establish the connection if needed.
            if (! isConnectionEstablished())
                establishConnection();

            value = attributeGetter_.getValue(attributeID);

            // Check to see if the software resource exists (the API will not result
            // in an exception in this case).
            try {
                if (document_.getIntValue("qszrtvpr.receiver.bytesAvailable") == 0)
                    throw new ResourceException(ResourceException.ATTRIBUTES_NOT_RETURNED);
            }
            catch(PcmlException e) {
                throw new ResourceException(e);
            }
        }
        return value;
    }




/**
Returns the load ID.

@return The load ID.
**/
    public String getLoadID()
    {
        return loadID_;
    }



/**
Returns the product ID.

@return The product ID.
**/
    public String getProductID()
    {
        return productID_;
    }



/**
Returns the product option.

@return The product option.
**/
    public String getProductOption()
    {
        return productOption_;
    }



/**
Returns the release level.

@return The release level.
**/
    public String getReleaseLevel()
    {
        return releaseLevel_;
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
Sets the load ID.  This does not change the software product
on the AS/400.  Instead, it changes the software product
to which this object references.

<p>The default value is LOAD_ID_CODE.

@param loadID       The load ID, or <a href="#LOAD_ID_CODE">LOAD_ID_CODE</a> for the code load.                    
**/
    public void setLoadID(String loadID)
    {
        if (loadID == null)
            throw new NullPointerException("loadID");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = loadID_;
        loadID_ = loadID;
        firePropertyChange("loadID", oldValue, loadID_);
    }



/**
Sets the product ID.  This does not change the software product
on the AS/400.  Instead, it changes the software product
to which this object references.

<p>The default value is PRODUCT_ID_OPERATING_SYSTEM.

@param productID    The product ID, or <a href="#PRODUCT_ID_OPERATING_SYSTEM">PRODUCT_ID_OPERATING_SYSTEM</a>
                    to refer to the operating system.
**/
    public void setProductID(String productID)
    {
        if (productID == null)
            throw new NullPointerException("productID");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = productID_;
        productID_ = productID;
        firePropertyChange("productID", oldValue, productID_);
    }



/**
Sets the product option.  This does not change the software product
on the AS/400.  Instead, it changes the software product
to which this object references.

<p>The default value is PRODUCT_OPTION_BASE.

@param productOption The option number for which is being requested, or 
                    <a href="#PRODUCT_OPTION_BASE">PRODUCT_OPTION_BASE</a> for the base option.
**/
    public void setProductOption(String productOption)
    {
        if (productOption == null)
            throw new NullPointerException("productOption");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = productOption_;
        productOption_ = productOption;
        firePropertyChange("productOption", oldValue, productOption_);
    }



/**
Sets the release level.  This does not change the software product
on the AS/400.  Instead, it changes the software product
to which this object references.

<p>The default value is RELEASE_LEVEL_CURRENT.

@param releaseLevel The release level.  Possible values are:
                    <ul>
                    <li><a href="#RELEASE_LEVEL_CURRENT">RELEASE_LEVEL_CURRENT</a> - Use the release level of
                        the currently installed operating system.                    
                    <li><a href="#RELEASE_LEVEL_ONLY">RELEASE_LEVEL_ONLY</a> - Use the only release level
                        for which a product load is found.  
                    <li><a href="#RELEASE_LEVEL_PREVIOUS">RELEASE_LEVEL_PREVIOUS</a> - Use the previous
                        release with modification level 0 of the operating system.
                    <li>A release level, in the form <code>V<em>x</em>R<em>x</em>M<em>x</em></code>.
                    </ul>
**/
    public void setReleaseLevel(String releaseLevel)
    {
        if (releaseLevel == null)
            throw new NullPointerException("releaseLevel");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = releaseLevel_;
        releaseLevel_ = releaseLevel;
        firePropertyChange("releaseLevel", oldValue, releaseLevel_);
    }



}
