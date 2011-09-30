///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JavaProgram.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

/**
Represents an IBM i Java program.   This class is supported
only when connecting to systems running IBM i V5R1 or higher, and is not supported beyond IBM i 7.1.
<p>
In the context of this discussion, a "Java program" is the IBM i executable object that is created when the CRTJVAPGM (Create Java Program) CL command is run against a class, JAR, or ZIP file.
<br>
Using the JavaProgram class, you can obtain the following information about an IBM i Java program:
<ul>
<li>Adopted authority profile</li>
<li>File change date</li>
<li>File owner</li>
<li>Java program creation date</li>
<li>Release program was created for</li>
<li>Licensed Internal Code options</li>
<li>Number of attached java programs</li>
<li>Number of classes</li>
<li>Number of classes with current java programs</li>
<li>Number of classes without current java programs</li>
<li>Number of classes with errors</li>
<li>Optimization level</li>
<li>Path used</li>
<li>Performance Collection Enabled flag</li>
<li>Performance Collection type</li>
<li>Profiling data status</li>
<li>Size of attached java programs</li>
<li>Use adopted authority</li>
</ul>
<br>

<br>
An Example using the JavaProgram class:
<br>
<blockquote><pre>
// Create a JavaProgram object to refer to a specific Java program.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
JavaProgram javaProgram = new JavaProgram(system, "/home/mydir/HelloWorld.class");
<br>
// Get the optimization.
int optimization = javaProgram.getOptimizationLevel();
<br>
// Get the file owner.
String owner = javaProgram.getFileOwner();
</pre></blockquote>
**/

public class JavaProgram implements Serializable
{
    static final long serialVersionUID = -209990140140936884L;

    private boolean loaded_ = false; // Have we retrieved values from the system yet?
    
    private AS400 system_;
    private String path_;

    private String fileOwner_;
    private Date fileChangeDate_;
    private Date javaProgramCreationDate_;
    private int numberOfAttachedPrograms_;
    private int numberOfClassesWithJavaPrograms_;
    private int numberOfClassesWithoutPrograms_;
    private int numberOfClassesWithErrors_;
    private int numberOfClasses_;
    private int optimizationLevel_;
    private String performanceCollectionEnabledFlag_;
    private String performanceCollectionType_;
    private boolean useAdoptedAuthority_;
    private String adoptedAuthorityProfile_;
    private int sizeOfAttachedPrograms_;
    private String javaProgramVersion_;
    private String profilingDataStatus_;
    private String LICoptions_;

    /**
    Constant indicating that the profile to use when the use adopted authority field is set is *USER.
    **/
    public static final String ADOPTED_AUTHORITY_PROFILE_USER = "*USER";

    /**
    Constant indicating that the profile to use when the use adopted authority field is set is *OWNER.
    **/
    public static final String ADOPTED_AUTHORITY_PROFILE_OWNER = "*OWNER";

    /**
    Constant indicating the type of performance collection is *ENTRYEXIT.
    **/
    public static final String PERFORMANCE_COLLECTION_TYPE_ENTRYEXIT = "*ENTRYEXIT";

    /**
    Constant indicating the type of performance collection is *FULL.
    **/
    public static final String PERFORMANCE_COLLECTION_TYPE_FULL = "*FULL";

    /**
    Constant indicating that profile data collection is not enabled for the the Java program(s).
    **/
    public static final String PROFILING_DATA_STATUS_NOCOL = "*NOCOL";

    /**
    Constant indicating that profile data collection is enabled for the attached Java program(s).
    **/
    public static final String PROFILING_DATA_STATUS_COL = "*COL";

    /**
    Constant indicating that profile data has been applied to the attached Java program(s).
    **/
    public static final String PROFILING_DATA_STATUS_APY = "*APY";
    

    /**
    Creates a JavaProgram
    **/
    public JavaProgram()
    {
    }

    /**
    Creates a JavaProgram
    
    @param system       The system.
    @param path         The path.  This can specify any class, jar, or zip file.
    **/
    public JavaProgram(AS400 system, String path)
    {
        setSystem(system);
        setPath(path);
    }


    private void checkVRM()
      throws UnsupportedOperationException
    {
      try
      {
        // See if the system VRM is higher than IBM i 7.1.
        if (getSystem() != null && getSystem().getVRM() > 0x00070100) {
          Trace.log(Trace.ERROR, "JavaProgram is not supported beyond IBM i 7.1.");
          throw new UnsupportedOperationException("JavaProgram");
        }
      }
      catch (UnsupportedOperationException e) { throw e; }
      catch (Exception e) {
        if (Trace.isTraceOn())
          Trace.log(Trace.ERROR, "Error when checking system VRM.", e);
        throw new UnsupportedOperationException(e.getMessage());
      }
    }

    /**
    Returns the name of the system.
    @return system name
    **/
    public AS400 getSystem()
    {
        return system_;
    }

    /**
    Returns the path to a class, jar, or zip file used to create the Java program.
    @return path
    **/
    public String getPath()
    {
        return path_;
    }

    /**
    Returns the profile to use when the "Use Adopted Authority" field is set.

    @return the profile to use.  Possible values are:
    <ul>
    <li>{@link #ADOPTED_AUTHORITY_PROFILE_USER ADOPTED_AUTHORITY_PROFILE_USER}
    <li>{@link #ADOPTED_AUTHORITY_PROFILE_OWNER ADOPTED_AUTHORITY_PROFILE_OWNER}
    </ul>
    **/
    public String getAdoptedAuthorityProfile()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if (!loaded_) refresh();
        if(adoptedAuthorityProfile_.equals("0"))
            return ADOPTED_AUTHORITY_PROFILE_USER;
        else
            return ADOPTED_AUTHORITY_PROFILE_OWNER;
    }

    /**
    Returns the date and time the file was last modified or changed.
    
    @return the last-changed date and time
    **/
    public Date getFileChangeDate()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return (Date)fileChangeDate_.clone();
    }

    /**
    Returns the name of the owner of the file.
    
    The string is in job CCSID
    @return the file owner
    **/
    public String getFileOwner()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return fileOwner_;
    }

    /**
    Returns the date and time the Java program was created for the file.

    @return the creation date
    **/
    public Date getJavaProgramCreationDate()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return (Date)javaProgramCreationDate_.clone();
    }

    /**
    Returns the IBM i version the Java program was created for.

    @return the version
    **/
    public String getJavaProgramVersion()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
       if(!loaded_) refresh();
       return javaProgramVersion_;
    }

    /**
    Returns the number of classes.

    @return the number of classes.
    **/
    public int getNumberOfClasses()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return numberOfClasses_;
    }

    /**
    Returns the number of classes with representations up-to-date in the attached Java programs.

    @return the number of classes with current java programs.
    **/
    public int getNumberOfClassesWithCurrentJavaPrograms()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return numberOfClassesWithJavaPrograms_;
    }

    /**
    Returns the number of classes containing errors.

    @return the number of classes with errors.
    **/
    public int getNumberOfClassesWithErrors()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return numberOfClassesWithErrors_;
    }

    /**
    Returns the number of classes with representations out-of-date.

    @return the number of classes without current java programs.
    **/
    public int getNumberOfClassesWithoutCurrentJavaPrograms()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return numberOfClassesWithoutPrograms_;
    }

    /**
    Returns the number of Java prgroams attached to the .class or .jar/sip file.

    @return the number of programs
    **/
    public int getNumberOfAttachedPrograms()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return numberOfAttachedPrograms_;
    }

    /**
    Returns the optimization level used to create the java program.

    @return the optimization level.  Possible values are:
    <ul>
    <li>0</li>
    <li>10</li>
    <li>20</li>
    <li>30</li>
    <li>40</li>
    </ul>
    **/
    public int getOptimizationLevel()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return optimizationLevel_;
    }

    /**
    Returns whether or not performance collection is enabled.

    @return the performance collection enabled flag.  Possible values are:
    <ul>
    <li>0 - "NONE</li>
    <li>1 - on</li>
    </ul>
    **/
    public String getPerformanceCollectionEnabledFlag()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return performanceCollectionEnabledFlag_;
    }

    /**
    Returns the type of performance collection if the performance collection flag is set.

    @return the type of performance collection.  Possible values are:
    <ul>
    <li>{@link #PERFORMANCE_COLLECTION_TYPE_ENTRYEXIT PERFORMANCE_COLLECTION_TYPE_ENTRYEXIT}</li>
    <li>{@link #PERFORMANCE_COLLECTION_TYPE_FULL PERFORMANCE_COLLECTION_TYPE_FULL}</li>
    </ul>
    **/
    public String getPerformanceCollectionType()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        if(performanceCollectionType_.equals("0"))
            return PERFORMANCE_COLLECTION_TYPE_ENTRYEXIT;
        else
            return PERFORMANCE_COLLECTION_TYPE_FULL;
    }

    /**
    Returns whether or not the used adopted authority is set.

    @return true if the use adopted authority is set, false otherwise
    **/
    public boolean isUseAdoptedAuthority()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return useAdoptedAuthority_;
    }

    /**
    Returns the size in kilobytes of all the attached java programs.

    @return the size
    **/
    public int getSizeOfAttachedJavaPrograms() 
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return sizeOfAttachedPrograms_;
    }

    /**
    Returns whether profiling data is enabled or applied.

    @return whether profiling data is enabled or applied.  Possible values are:
    <ul>
    <li>{@link #PROFILING_DATA_STATUS_NOCOL PROFILING_DATA_STATUS_NOCOL}</li>
    <li>{@link #PROFILING_DATA_STATUS_COL PROFILING_DATA_STATUS_COL}</li>
    <li>{@link #PROFILING_DATA_STATUS_APY PROFILING_DATA_STATUS_APY}</li>
    </ul>
    **/
    public String getProfilingDataStatus()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        if(profilingDataStatus_.equals("0"))
            return PROFILING_DATA_STATUS_NOCOL;
        else if(profilingDataStatus_.equals("1"))
            return PROFILING_DATA_STATUS_COL;
        else
            return PROFILING_DATA_STATUS_APY;
    }

    /**
    Returns the LIC options string specified when the java program was last modified. 
    
    @return the LIC options.
    **/
    public String getLICOptions()
    throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
    {
        if(!loaded_) refresh();
        return LICoptions_;
    }

    /**
    Refreshes all the values for this PTF by retrieving them from the system.
    **/
    public void refresh()
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException,
           UnsupportedEncodingException
    {
        checkVRM();

        int ccsid = system_.getCcsid();
        ConvTable conv = ConvTable.getTable(ccsid, null);
        int len=4096;
        ProgramParameter[] parms = new ProgramParameter[15];
        parms[0] = new ProgramParameter(len);                                   // receiver variable
        parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));   // length of receiver variable
        parms[2] = new ProgramParameter(conv.stringToByteArray("RJPI0100"));        // format name
        parms[3] = new ProgramParameter(0);                                     //Class list receiver variable
        parms[4] = new ProgramParameter(BinaryConverter.intToByteArray(0));     //Length of class list receiver variable
        parms[5] = new ProgramParameter(conv.stringToByteArray("RJPC0100"));    //format of class list receiver variable
        parms[6] = new ProgramParameter(conv.stringToByteArray(path_));         //path name
        parms[7] = new ProgramParameter(BinaryConverter.intToByteArray(path_.length()));  //length of path name
        parms[8] = new ProgramParameter(conv.stringToByteArray(" "));           //classpath
        parms[9] = new ProgramParameter(BinaryConverter.intToByteArray(0));     //length of classpath
        parms[10] = new ProgramParameter(0);                                    //classpath used receiver variable
        parms[11] = new ProgramParameter(BinaryConverter.intToByteArray(0));    //length of classpath used receiver variable
        parms[12] = new ProgramParameter(BinaryConverter.intToByteArray(1));    //status of calsses to return in the class list
        parms[13] = new ProgramParameter(conv.stringToByteArray("*PGM"));       //JDK version
        parms[14] = new ProgramParameter(BinaryConverter.intToByteArray(0));    // error code

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QJVAMAT.PGM", parms);
        // Assumption of thread-safety defaults to false, or to the value of the "threadSafe" system property (if it has been set).
        //pc.setThreadSafe(false);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        byte[] output = parms[0].getOutputData();
        //int bytesReturned = BinaryConverter.byteArrayToInt(output, 0);
        //int bytesAvailable = BinaryConverter.byteArrayToInt(output, 4);
        fileOwner_ = conv.byteArrayToString(output, 8, 10);
        String d = conv.byteArrayToString(output, 18, 13);
        // Parse the "file change" date
        if (d.trim().length() == 13)
        {
            Calendar cal = AS400Calendar.getGregorianInstance();
            cal.clear();
            cal.set(Integer.parseInt(d.substring(0,3)) + 1900, // year
                Integer.parseInt(d.substring(3,5))-1,     // month is zero-based
                Integer.parseInt(d.substring(5,7)),       // day
                Integer.parseInt(d.substring(7,9)),       // hour
                Integer.parseInt(d.substring(9,11)),      // minute
                Integer.parseInt(d.substring(11,13)));    // second
            fileChangeDate_ = cal.getTime();
        }
        else
        {
            fileChangeDate_ = null;
        }
        d = conv.byteArrayToString(output, 31, 13);
        //Parse the "Java program creation" date
        if (d.trim().length() == 13)
        {
            Calendar cal = AS400Calendar.getGregorianInstance();
            cal.clear();
            cal.set(Integer.parseInt(d.substring(0,3)) + 1900, // year
                Integer.parseInt(d.substring(3,5))-1,     // month is zero-based
                Integer.parseInt(d.substring(5,7)),       // day
                Integer.parseInt(d.substring(7,9)),       // hour
                Integer.parseInt(d.substring(9,11)),      // minute
                Integer.parseInt(d.substring(11,13)));    // second
            javaProgramCreationDate_ = cal.getTime();
        }
        else
        {
            javaProgramCreationDate_ = null;
        }
        numberOfAttachedPrograms_ = BinaryConverter.byteArrayToInt(output, 44);
        numberOfClassesWithJavaPrograms_ = BinaryConverter.byteArrayToInt(output, 48);
        numberOfClassesWithoutPrograms_ = BinaryConverter.byteArrayToInt(output, 52);
        numberOfClassesWithErrors_ = BinaryConverter.byteArrayToInt(output, 56);
        numberOfClasses_ = BinaryConverter.byteArrayToInt(output, 60);
        optimizationLevel_ = BinaryConverter.byteArrayToInt(output, 64);
        performanceCollectionEnabledFlag_ = conv.byteArrayToString(output, 68, 1);
        performanceCollectionType_ = conv.byteArrayToString(output, 69, 1);
        String useAdopAuthority = conv.byteArrayToString(output, 70, 1);
        adoptedAuthorityProfile_ = conv.byteArrayToString(output, 71, 1);
        sizeOfAttachedPrograms_ = BinaryConverter.byteArrayToInt(output, 72);
        String version = Integer.toString( (output[76] & 0xff ) + 0x100, 16 /* radix */ ) .substring( 1 );  //get version
        String release = Integer.toString( (output[77] & 0xff ) + 0x100, 16 /* radix */ ) .substring( 1 );  //get release and modification
        javaProgramVersion_ = getVersion((version + release).toCharArray());
        profilingDataStatus_ = Byte.toString(output[78]);
        int offsetToLICOptions = BinaryConverter.byteArrayToInt(output, 80);
        int lengthOfLIC = BinaryConverter.byteArrayToInt(output, 84);
        int lengthOfAvailableLIC = BinaryConverter.byteArrayToInt(output, 88);
        LICoptions_ = conv.byteArrayToString(output, offsetToLICOptions, lengthOfLIC);
        if(useAdopAuthority.equals("0"))
            useAdoptedAuthority_ = false;
        else
            useAdoptedAuthority_ = true;
        loaded_= true;
    }

    /**
    Gets the IBM i version.
    **/
    private String getVersion(char[] version)
    {
        String v = "V";
        String r = "R";
        String m = "M";
        if(version[0] != '0')
            v += version[0];
        v += version[1];
        r += version[2];
        m += version[3];

        return v+r+m;
    }

    /**
    Sets the name of the system to search for a Java Program.
    
    @param system  The system
    **/
    public void setSystem(AS400 system)
    {
        if(loaded_)
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        if(system == null)
            throw new NullPointerException("system");

        system_ = system;
    }

    /**
    Sets the qualified path name to use.
    
    @param path - the qualified path name.
    **/
    public void setPath(String path)
    {
        if(loaded_)
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        if (path == null)
            throw new NullPointerException("path");
        
        path_ = path;
    }
}
