///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RJavaProgram.java
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
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;
import java.util.Date;



/**
The RJavaProgram class represents an OS/400 Java program.   This is supported
only when connecting to servers running OS/400 V5R1 or later.

In the context of this discussion, a "Java program" is the OS/400 executable object that is created when the CRTJVAPGM (Create Java Program) CL command is run against a class, JAR, or ZIP file.

<a name="attributeIDs"><p>The following attribute IDs are supported:
<ul>
<li><a href="#CLASSES_WITHOUT_CURRENT_JAVA_PROGRAMS">CLASSES_WITHOUT_CURRENT_JAVA_PROGRAMS</a>
<li><a href="#CLASSES_WITH_CURRENT_JAVA_PROGRAMS">CLASSES_WITH_CURRENT_JAVA_PROGRAMS</a>
<li><a href="#CLASSES_WITH_ERRORS">CLASSES_WITH_ERRORS</a>
<li><a href="#ENABLE_PERFORMANCE_COLLECTION">ENABLE_PERFORMANCE_COLLECTION</a>
<li><a href="#FILE_CHANGE">FILE_CHANGE</a>
<li><a href="#JAVA_PROGRAMS">JAVA_PROGRAMS</a>
<li><a href="#JAVA_PROGRAM_CREATION">JAVA_PROGRAM_CREATION</a>
<li><a href="#JAVA_PROGRAM_SIZE">JAVA_PROGRAM_SIZE</a>
<li><a href="#LICENSED_INTERNAL_CODE_OPTIONS">LICENSED_INTERNAL_CODE_OPTIONS</a>
<li><a href="#OPTIMIZATION">OPTIMIZATION</a>
<li><a href="#OWNER">OWNER</a>
<li><a href="#PROFILING_DATA">PROFILING_DATA</a>
<li><a href="#RELEASE_PROGRAM_CREATED_FOR">RELEASE_PROGRAM_CREATED_FOR</a>
<li><a href="#TOTAL_CLASSES_IN_SOURCE">TOTAL_CLASSES_IN_SOURCE</a>
<li><a href="#USE_ADOPTED_AUTHORITY">USE_ADOPTED_AUTHORITY</a>
<li><a href="#USER_PROFILE">USER_PROFILE</a>
</ul>

<p>Use any of these attribute IDs with
<a href="ChangeableResource.html#getAttributeValue(java.lang.Object)">getAttributeValue()</a>
and <a href="ChangeableResource.html#setAttributeValue(java.lang.Object, java.lang.Object)">setAttributeValue()</a>
to access the attribute values for an RJavaProgram.

<blockquote><pre>
// Create an RJavaProgram object to refer to a specific Java program.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RJavaProgram javaProgram = new RJavaProgram(system, "/home/mydir/HelloWorld.class");
<br>
// Get the optimization.
Integer optimization = (Integer)javaProgram.getAttributeValue(RJavaProgram.OPTIMIZATION);
<br>
// Set the enable peformance collection attribute value to full.
javaProgram.setAttributeValue(RJavaProgram.ENABLE_PERFORMANCE_COLLECTION, RJavaProgram.ENABLE_PERFORMANCE_COLLECTION_FULL);
<br>
// Commit the attribute change.
javaProgram.commitAttributeChanges();
</pre></blockquote>
**/
public class RJavaProgram
extends ChangeableResource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;




//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");
    private static final String         ICON_BASE_NAME_     = "RJavaProgram";
    private static final String         PRESENTATION_KEY_   = "JAVA_PROGRAM";



//-----------------------------------------------------------------------------------------
// Attribute values.
//-----------------------------------------------------------------------------------------

/**
Attribute value for yes.
**/
    public static final String YES             = "*YES";

/**
Attribute value for no.
**/
    public static final String NO              = "*NO";



//-----------------------------------------------------------------------------------------
// Attribute IDs.
//
// * If you add an attribute here, make sure and add it to the class javadoc.
//-----------------------------------------------------------------------------------------

    // Private data.
            static ResourceMetaDataTable        attributes_             = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);
    private static ProgramMap                   getterMap_              = new ProgramMap();
    private static CommandMap                   setterMap_              = new CommandMap();

    private static final String                 CRTJVAPGM_              = "CRTJVAPGM";
    private static final String                 CHGJVAPGM_              = "CHGJVAPGM";
    private static final String                 PATH_PARAMETER_         = "CLSF";
    private static final String                 QJVAMAT_                = "qjvamat";



/**
Attribute ID for total classes in source.  This identifies a read-only
Integer attribute, which represents the total number of classes located
within the ZIP or JAR file.
**/
    public static final String TOTAL_CLASSES_IN_SOURCE = "TOTAL_CLASSES_IN_SOURCE";  // @B1A

    static {
        try {
        attributes_.add(TOTAL_CLASSES_IN_SOURCE, Integer.class, true);
        getterMap_.add(TOTAL_CLASSES_IN_SOURCE, QJVAMAT_, "receiverVariable.numberOfTotalClassesInSource");
        }
        catch (Exception found) { found.printStackTrace(); }
    }



/**
Attribute ID for classes with errors.  This identifies a read-only
Integer attribute, which represents the number of classes in the
file that contain errors.
**/
    public static final String CLASSES_WITH_ERRORS = "CLASSES_WITH_ERRORS";  // @B1A

    static {
        try {
        attributes_.add(CLASSES_WITH_ERRORS, Integer.class, true);
        getterMap_.add(CLASSES_WITH_ERRORS, QJVAMAT_, "receiverVariable.numberOfClassesWithErrors");
        }
        catch (Exception found) { found.printStackTrace(); }
    }



/**
Attribute ID for classes with current Java programs.  This identifies
a read-only Integer attribute, which represents the number of classes
in the file which have current Java programs.
**/
    public static final String CLASSES_WITH_CURRENT_JAVA_PROGRAMS                      = "CLASSES_WITH_CURRENT_JAVA_PROGRAMS";

    static {
        attributes_.add(CLASSES_WITH_CURRENT_JAVA_PROGRAMS, Integer.class, true);
        getterMap_.add(CLASSES_WITH_CURRENT_JAVA_PROGRAMS, QJVAMAT_, "receiverVariable.numberOfClassesWithCurrentJavaPrograms");
    }



/**
Attribute ID for classes without current Java programs.  This identifies
a read-only Integer attribute, which represents the number of classes
in the file which do not have current Java programs.
**/
    public static final String CLASSES_WITHOUT_CURRENT_JAVA_PROGRAMS                      = "CLASSES_WITHOUT_CURRENT_JAVA_PROGRAMS";

    static {
        attributes_.add(CLASSES_WITHOUT_CURRENT_JAVA_PROGRAMS, Integer.class, true);
        getterMap_.add(CLASSES_WITHOUT_CURRENT_JAVA_PROGRAMS, QJVAMAT_, "receiverVariable.numberOfClassesWithoutCurrentJavaPrograms");
    }



/**
Attribute ID for enable performance collection.  This identifies a
String attribute, which represents the level of performance data
collection allowed for this Java program.  Possible
values are:
<ul>
<li><a href="#ENABLE_PERFORMANCE_COLLECTION_NONE">ENABLE_PERFORMANCE_COLLECTION_NONE</a>
    - No performance collection is enabled for this Java program.
<li><a href="#ENABLE_PERFORMANCE_COLLECTION_ENTRY_EXIT">ENABLE_PERFORMANCE_COLLECTION_ENTRY_EXIT</a>
    - This gives the entry/exit information on all the procedures of the Java
      program (including those that are leaf procedures).  This includes the
      PEP routine. This is useful in capturing information on all procedures.
<li><a href="#ENABLE_PERFORMANCE_COLLECTION_FULL">ENABLE_PERFORMANCE_COLLECTION_FULL</a>
    -  This gives the entry/exit information on all procedures of the Java
       program (including those that are leaf procedures) and precall and postcall
       hooks around calls to other procedures.  This is useful in capturing
       information on all procedures.
</ul>
**/
    public static final String ENABLE_PERFORMANCE_COLLECTION                   = "ENABLE_PERFORMANCE_COLLECTION";

    /**
    Attribute value indicating that no performance collection is enabled for this Java program.

    @see #ENABLE_PERFORMANCE_COLLECTION
    **/
    public static final String ENABLE_PERFORMANCE_COLLECTION_NONE            = "*NONE";

    /**
    Attribute value indicating that the entry/exit information on all the procedures of the Java
    program (including those that are leaf procedures) is given.  This includes the
    PEP routine. This is useful in capturing information on all procedures.

    @see #ENABLE_PERFORMANCE_COLLECTION
    **/
    public static final String ENABLE_PERFORMANCE_COLLECTION_ENTRY_EXIT            = "*ENTRYEXIT";

    /**
    Attribute value indicating that the entry/exit information on all procedures of the Java
    program (including those that are leaf procedures) and precall and postcall
    hooks around calls to other procedures is given.  This is useful in capturing
    information on all procedures.

    @see #ENABLE_PERFORMANCE_COLLECTION
    **/
    public static final String ENABLE_PERFORMANCE_COLLECTION_FULL            = "*FULL";

    static {
        attributes_.add(ENABLE_PERFORMANCE_COLLECTION, String.class, false,
                        new Object[] {ENABLE_PERFORMANCE_COLLECTION_NONE,
                            ENABLE_PERFORMANCE_COLLECTION_ENTRY_EXIT,
                            ENABLE_PERFORMANCE_COLLECTION_FULL }, null, true);
        getterMap_.add(ENABLE_PERFORMANCE_COLLECTION, QJVAMAT_, "receiverVariable.performanceCollectionEnabledFlag",
                       new EnablePerformanceCollectionValueMap_());
        setterMap_.add(ENABLE_PERFORMANCE_COLLECTION, CHGJVAPGM_, "ENBPFRCOL");
    }

    private static class EnablePerformanceCollectionValueMap_ extends AbstractValueMap
    {
        public Object ptol(Object physicalValue)
        {
            if (physicalValue.equals("11"))
                return ENABLE_PERFORMANCE_COLLECTION_FULL;
            else if (physicalValue.equals("10"))
                return ENABLE_PERFORMANCE_COLLECTION_ENTRY_EXIT;
            else
                return ENABLE_PERFORMANCE_COLLECTION_NONE;
        }
    }



/**
Attribute ID for profiling data.  This identifies a 
String attribute, which indicates if the Java program is 
collecting profiling data.  Possible values are:
<ul>
<li><a href="#PROFILING_DATA_NOCOLLECTION">PROFILING_DATA_NOCOLLECTION</a>
    - No profiling data collection is enabled for this Java program.
<li><a href="#PROFILING_DATA_COLLECTION">PROFILING_DATA_COLLECTION</a>
    - Profiling data collection is enabled for this Java program.  This
      enablement can only occur if the optimization of the Java program is
      30 or higher.  Also, collection does not occur until the profiling 
      data is applied.
<li><a href="#PROFILING_DATA_APPLY">PROFILING_DATA_APPLY</a>
    - Profiling data collection is applied for this Java program.
<li><a href="#PROFILING_DATA_CLEAR">PROFILING_DATA_CLEAR</a>
    - All profiling data that has been collected for this Java program is to 
      be cleared.
</ul>
**/
    public static final String PROFILING_DATA = "PROFILING_DATA";  // @B1A
    
    /**
    Attribute value indicating that no profiling data collection is enabled for this Java program.

    @see #PROFILING_DATA
    **/
    public static final String PROFILING_DATA_NOCOLLECTION = "*NOCOL";  // @B1A
    
    /**
    Attribute value indicating that profiling data collection is enabled for this Java program.
    This enablement can only occur if the optimization of the Java program is 30 or higher.  
    Also, collection does not occur until the profiling data is applied.

    @see #PROFILING_DATA
    **/
    public static final String PROFILING_DATA_COLLECTION = "*COL";  // @B1A
    
    /**
    Attribute value indicating that profiling data collection is applied for this Java program.

    @see #PROFILING_DATA
    **/
    public static final String PROFILING_DATA_APPLY = "*APY";  // @B1A
    
    /**
    Attribute value indicating that all profiling data that has been collected 
    for this Java program is to be cleared.

    @see #PROFILING_DATA
    **/
    public static final String PROFILING_DATA_CLEAR = "*CLR";  // @B1A

    static { // @B1A
        attributes_.add(PROFILING_DATA, String.class, false,
                        new Object[] {PROFILING_DATA_NOCOLLECTION,
                            PROFILING_DATA_COLLECTION,
                            PROFILING_DATA_CLEAR,
                            PROFILING_DATA_APPLY }, null, true);
        getterMap_.add(PROFILING_DATA, QJVAMAT_, "receiverVariable.profilingDataStatus",
                       new ProfilingDataValueMap_());
        setterMap_.add(PROFILING_DATA, CHGJVAPGM_, "PRFDTA");
    }

    private static class ProfilingDataValueMap_ extends AbstractValueMap // @B1A
    {
        public Object ptol(Object physicalValue)
        {
            if (physicalValue.equals("2"))
                return PROFILING_DATA_APPLY;
            else if (physicalValue.equals("1"))
                return PROFILING_DATA_COLLECTION;
            else
                return PROFILING_DATA_NOCOLLECTION;
        }
    }

/**
Attribute ID for file change.  This identifies a read-only
Date attribute, which represents the date and time when the file
was last changed.  The Date value is converted using the default Java locale.
**/
    public static final String FILE_CHANGE                      = "FILE_CHANGE";

    static {
        attributes_.add(FILE_CHANGE, Date.class, true);
        getterMap_.add(FILE_CHANGE, QJVAMAT_, "receiverVariable.fileChangeModifyDateAndTime", new DateValueMap(DateValueMap.FORMAT_13));
    }



/**
Attribute ID for Java programs.  This identifies a read-only
Integer attribute, which represents the number of Java
programs associated with the file.
**/
    public static final String JAVA_PROGRAMS                      = "JAVA_PROGRAMS";

    static {
        attributes_.add(JAVA_PROGRAMS, Integer.class, true);
        getterMap_.add(JAVA_PROGRAMS, QJVAMAT_, "receiverVariable.numberOfJavaProgramsAttached");
    }



/**
Attribute ID for Java program creation.  This identifies a read-only
Date attribute, which represents the date and time when the Java
program was created.  If this is a zip or jar file, this is the
date and time when the first Java program was attached to the file.
If this is class file, this is the date and time when the Java
program was created.  The Date value is converted using the default Java locale.
**/
    public static final String JAVA_PROGRAM_CREATION                      = "JAVA_PROGRAM_CREATION";

    static {
        attributes_.add(JAVA_PROGRAM_CREATION, Date.class, true);
        getterMap_.add(JAVA_PROGRAM_CREATION, QJVAMAT_, "receiverVariable.javaProgramCreationDateAndTime", new DateValueMap(DateValueMap.FORMAT_13));
    }



/**
Attribute ID for Java program size.  This identifies
a read-only Long attribute, which represents the size, in kilobytes,
of the Java programs that are attached to the file.
**/
    public static final String JAVA_PROGRAM_SIZE                      = "JAVA_PROGRAM_SIZE";

    static {
        attributes_.add(JAVA_PROGRAM_SIZE, Long.class, true);
        getterMap_.add(JAVA_PROGRAM_SIZE, QJVAMAT_, "receiverVariable.sizeOfJavaProgramsAttached", new JavaProgramSizeValueMap_());
    }

    private static class JavaProgramSizeValueMap_ extends AbstractValueMap
    {

        public Object ptol(Object physicalValue)
        {
            if (physicalValue instanceof Integer)
                return new Long((long)((Integer)physicalValue).intValue());
            else
                return physicalValue;
        }
    }








/**
Attribute ID for licensed internal code options.  This identifies
a String attribute, which represents the selected licensed
internal code (LIC) compile-time options that are used when
the Java program was created.
**/
    public static final String LICENSED_INTERNAL_CODE_OPTIONS                      = "LICENSED_INTERNAL_CODE_OPTIONS";

    static {
        attributes_.add(LICENSED_INTERNAL_CODE_OPTIONS, String.class, false);
        getterMap_.add(LICENSED_INTERNAL_CODE_OPTIONS, QJVAMAT_, "receiverVariable.LICOptions");
        setterMap_.add(LICENSED_INTERNAL_CODE_OPTIONS, CHGJVAPGM_, "LICOPT", new QuoteValueMap());
    }



/**
Attribute ID for optimization.  This identifies an Integer attribute,
which represents the optimization level of the AS/400 Java program.  Possible
values are:
<ul>
<li><a href="#OPTIMIZATION_INTERPRET">OPTIMIZATION_INTERPRET</a>
    -  The Java program is not optimized.  When invoked, the Java program
       interprets the class file byte codes.  Variables can be displayed and
       modified while debugging.
<li><a href="#OPTIMIZATION_10">OPTIMIZATION_10</a>
    - The Java program contains a compiled version of the class file byte codes
      but has only minimal additional compiler optimization.  Variables can be
      displayed and modified while debugging.
<li><a href="#OPTIMIZATION_20">OPTIMIZATION_20</a>
    - The Java program contains a compiled version of the class file byte codes
      and has some additional compiler optimization.  Variables can be displayed
      but not modified while debugging.
<li><a href="#OPTIMIZATION_30">OPTIMIZATION_30</a>
    - The Java program contains a compiled version of the class file byte codes
      and has more compiler optimization than optimization level 20.  During a
      debug session, user variables cannot be changed, but can be displayed.
      The presented values may not be the current values of the variables.
<li><a href="#OPTIMIZATION_40">OPTIMIZATION_40</a>
    - The Java program contains a compiled version of the class file byte codes
      and has more compiler optimization than optimization level 30.  All call and
      instruction tracing is disabled.
</ul>
**/
    public static final String OPTIMIZATION                   = "OPTIMIZATION";

    /**
    Attribute value indicating that the Java program is not optimized.  When invoked,
    the Java program interprets the class file byte codes.  Variables can be displayed and
    modified while debugging.

    @see #OPTIMIZATION
    **/
    public static final Integer OPTIMIZATION_INTERPRET            = new Integer(-1);
    private static final String OPTIMIZATION_INTERPRET_PHYSICAL   = "*INTERPRET";

    /**
    Attribute value indicating that the Java program contains a compiled version
    of the class file byte codes but has only minimal additional compiler optimization.
    Variables can be displayed and modified while debugging.

    @see #OPTIMIZATION
    **/
    public static final Integer OPTIMIZATION_10            = new Integer(10);

    /**
    Attribute value indicating that the Java program contains a compiled version
    of the class file byte codes and has some additional compiler optimization.
    Variables can be displayed but not modified while debugging.

    @see #OPTIMIZATION
    **/
    public static final Integer OPTIMIZATION_20            = new Integer(20);

    /**
    Attribute value indicating that the Java program contains a compiled version
    of the class file byte codes and has more compiler optimization than optimization
    level 20.  During a debug session, user variables cannot be changed, but can be
    displayed.  The presented values may not be the current values of the variables.

    @see #OPTIMIZATION
    **/
    public static final Integer OPTIMIZATION_30            = new Integer(30);

    /**
    Attribute value indicating that the Java program contains a compiled version
    of the class file byte codes and has more compiler optimization than optimization
    level 30.  All call and instruction tracing is disabled.

    @see #OPTIMIZATION
    **/
    public static final Integer OPTIMIZATION_40            = new Integer(40);

    static {
        attributes_.add(OPTIMIZATION, Integer.class, false,
                        new Object[] {OPTIMIZATION_INTERPRET,
                            OPTIMIZATION_10,
                            OPTIMIZATION_20,
                            OPTIMIZATION_30,
                            OPTIMIZATION_40 }, null, true);
        OptimizationValueMap_ valueMap = new OptimizationValueMap_();
        getterMap_.add(OPTIMIZATION, QJVAMAT_, "receiverVariable.optimizationLevel", valueMap);
        setterMap_.add(OPTIMIZATION, CHGJVAPGM_, "OPTIMIZE", valueMap);
    }

    private static class OptimizationValueMap_ extends AbstractValueMap
    {
        public Object ptol(Object physicalValue)
        {
            if (((Integer)physicalValue).intValue() == 0)
                return OPTIMIZATION_INTERPRET;
            else
                return physicalValue;
        }

        public Object ltop(Object logicalValue)
        {
            if (logicalValue.equals(OPTIMIZATION_INTERPRET))
                return OPTIMIZATION_INTERPRET_PHYSICAL;
            else
                return logicalValue.toString();
        }
    }




/**
Attribute ID for owner.  This identifies a read-only
String attribute, which represents the owner of the
Java program.
**/
    public static final String OWNER                      = "OWNER";

    static {
        attributes_.add(OWNER, String.class, true);
        getterMap_.add(OWNER, QJVAMAT_, "receiverVariable.fileOwnerName");
    }



/**
Attribute ID for release program created for.  This identifies
a read-only String attribute, which represents the release of the
operating system for which the object was created.
**/
    public static final String RELEASE_PROGRAM_CREATED_FOR                      = "RELEASE_PROGRAM_CREATED_FOR";

    static {
        attributes_.add(RELEASE_PROGRAM_CREATED_FOR, String.class, true);
        getterMap_.add(RELEASE_PROGRAM_CREATED_FOR, QJVAMAT_, "receiverVariable.javaProgramVersion", new ReleaseProgramCreatedForValueMap_());
    }

    private static class ReleaseProgramCreatedForValueMap_ extends AbstractValueMap
    {
        public Object ptol(Object physicalValue)
        {
            byte[] asBytes = (byte[])physicalValue;
            StringBuffer buffer = new StringBuffer("V");
            buffer.append(asBytes[0]);
            buffer.append("R");
            buffer.append(asBytes[1] >> 4);
            buffer.append("M");
            buffer.append(asBytes[1] & 0x0F);
            return buffer.toString();
        }
    }



/**
Attribute ID for use adopted authority.  This identifies a read-only
String attribute, which indicates if the Java programs use adopted authority
from previous call levels in the stack.  Possible values are:
<ul>
<li><a href="#YES">YES</a>
<li><a href="#YES">NO</a>
</ul>
**/
    public static final String USE_ADOPTED_AUTHORITY                   = "USE_ADOPTED_AUTHORITY";

    static {
        attributes_.add(USE_ADOPTED_AUTHORITY, String.class, true,
                        new Object[] {YES, NO } ,null, true);
        getterMap_.add(USE_ADOPTED_AUTHORITY, QJVAMAT_, "receiverVariable.useAdoptedAuthority", new UseAdoptedAuthorityValueMap_());
    }

    private static class UseAdoptedAuthorityValueMap_ extends AbstractValueMap
    {
        public Object ptol(Object physicalValue)
        {
            return (physicalValue.equals("1") ? YES : NO);
        }
    }



/**
Attribute ID for user profile.  This identifies a read-only
String attribute, which represents who the authority checking
that was done while this program is running should include.
Possible values are:
<ul>
<li><a href="#USER_PROFILE_USER">USER_PROFILE_USER</a>
    - Indicates that the authority checking that was done while
      this program is running should include only the user
      who is running the program.
<li><a href="#USER_PROFILE_OWNER">USER_PROFILE_OWNER</a>
    - Indicates that the authority checking that was done while
      this program is running should include both the use
      who is running the program and the program owner.
</ul>
**/
    public static final String USER_PROFILE                   = "USER_PROFILE";

    /**
    Attribute value indicating that the authority checking that was done while
    this program is running should include only the user who is running the program.

    @see #USER_PROFILE
    **/
    public static final String USER_PROFILE_USER            = "*USER";

    /**
    Attribute value indicating that the authority checking that was done while
    this program is running should include both the use who is running the program
    and the program owner.

    @see #USER_PROFILE
    **/
    public static final String USER_PROFILE_OWNER            = "*OWNER";

    static {
        attributes_.add(USER_PROFILE, String.class, true,
                        new Object[] {USER_PROFILE_USER, USER_PROFILE_OWNER } ,null, true);
        getterMap_.add(USER_PROFILE, QJVAMAT_, "receiverVariable.adoptedAuthorityProfile", new UserProfileValueMap_());
    }

    private static class UserProfileValueMap_ extends AbstractValueMap
    {
        public Object ptol(Object physicalValue)
        {
            return (physicalValue.equals("1") ? USER_PROFILE_OWNER : USER_PROFILE_USER);
        }
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_      = "com.ibm.as400.resource.RJavaProgram";
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

    private String                          path_               = null;

    private ProgramAttributeGetter          attributeGetter_    = null;
    private CommandAttributeSetter          attributeSetter_    = null;
    private ProgramCallDocument             document_           = null;


//-----------------------------------------------------------------------------------------
// Constructors.
//-----------------------------------------------------------------------------------------

/**
Constructs an RJavaProgram object.
**/
    public RJavaProgram()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);
    }



/**
Constructs an RJavaProgram object.

@param system       The system.
@param path         The path.  This can be any class, jar, or zip file.
**/
    public RJavaProgram(AS400 system, String path)
    {
        this();

        try {
            setSystem(system);
            setPath(path);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



/**
Commits the specified attribute changes.

@exception ResourceException                If an error occurs.
**/
    protected void commitAttributeChanges(Object[] attributeIDs, Object[] values)
    throws ResourceException
    {
        super.commitAttributeChanges(attributeIDs, values);

        // Establish the connection if needed.
        if (! isConnectionEstablished())
            establishConnection();

        attributeSetter_.setValues(attributeIDs, values);
    }



/**
Computes the resource key.

@param system       The system.
@param path         The path.
**/
    static Object computeResourceKey(AS400 system, String path)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(RJavaProgram.class);
        buffer.append(':');
        buffer.append(system.getSystemName());
        buffer.append(':');
        buffer.append(system.getUserId());
        buffer.append(':');
        buffer.append(path);
        return buffer.toString();
    }



/**
Deletes the Java program.  This does not delete the class, jar, or zip file.

@exception ResourceException                If an error occurs.
**/
    public void delete()
        throws ResourceException
    {
        // Establish the connection if needed.
        if (!isConnectionEstablished())
            establishConnection();

        StringBuffer buffer = new StringBuffer("DLTJVAPGM CLSF('");
        buffer.append(path_);
        buffer.append("')");
        try {
            fireBusy();
            CommandCall dltjvapgm = new CommandCall(getSystem(), buffer.toString());
            if (dltjvapgm.run() == false)
                throw new ResourceException(dltjvapgm.getMessageList());
        }
        catch (Exception e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when deleting a Java program", e);
            throw new ResourceException(e);
        }
        finally {
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
        if (path_ == null)
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Initialize the PCML document.
        document_ = (ProgramCallDocument)staticDocument_.clone();
        AS400 system = getSystem();
        try {
            document_.setSystem(system);
            document_.setIntValue("qjvamat.lengthOfPathName", path_.length());
            document_.setValue("qjvamat.pathName", path_);
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting PCML document values", e);
        }

        // Initialize the attribute getter.
        attributeGetter_ = new ProgramAttributeGetter(system, document_, getterMap_);

        // Initialize the attribute setter.
        attributeSetter_ = new CommandAttributeSetter(system, setterMap_);
        attributeSetter_.setParameterValue(CHGJVAPGM_, PATH_PARAMETER_, '\'' + path_ + '\'');
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

        // Validate if we can establish the connection.
        if (path_ == null)
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Update the presentation.
        Presentation presentation = getPresentation();
        IFSFile f = new IFSFile(getSystem(), path_);
        presentation.setName(f.getName());
        presentation.setFullName(f.getPath());

        // Update the resource key.
        if (getResourceKey() == null)
            setResourceKey(computeResourceKey(getSystem(), path_));
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

            /* @A1D
            // Check to see if the Java program exists (the API will not result
            // in an exception in this case).
            try {
                if (document_.getIntValue("qjvalibjvm.receiverVariable.bytesAvailable") == 0)
                    throw new ResourceException(ResourceException.ATTRIBUTES_NOT_RETURNED);
            }
            catch(PcmlException e) {
                throw new ResourceException(e);
            }
            */
        }
        return value;
    }




/**
Returns the path.

@return The path.
**/
    public String getPath()
    {
        return path_;
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
Sets the path.  This does not change the Java program
on the AS/400.  Instead, it changes the Java program
that this object references.

@param path         The path.  This can be any class, jar, or zip file.
**/
    public void setPath(String path)
    {
        if (path == null)
            throw new NullPointerException("path");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = path_;
        path_ = path;
        firePropertyChange("path", oldValue, path_);
    }



/**
Returns the path.

@return The path.
**/
    public String toString()
    {
        return (path_ == null) ? "" : path_;
    }



}
