///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RJobList.java
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
The RJobList class represents a list of AS/400 jobs.

<a name="selectionIDs"><p>The following selection IDs are supported:
<ul>
<li><a href="#JOB_NAME">JOB_NAME</a>
<li><a href="#JOB_NUMBER">JOB_NUMBER</a>
<li><a href="#JOB_TYPE">JOB_TYPE</a>
<li><a href="#PRIMARY_JOB_STATUSES">PRIMARY_JOB_STATUSES</a>
<li><a href="#USER_NAME">USER_NAME</a>
</ul>
</a>

<p>Use one or more of these selection IDs with 
{@link com.ibm.as400.resource.ResourceList#getSelectionValue(java.lang.Object) getSelectionValue()}
and {@link com.ibm.as400.resource.ResourceList#setSelectionValue(java.lang.Object, java.lang.Object) setSelectionValue()}
to access the selection values for an RJobList.

<a name="sortIDs"><p>The following sort IDs are supported:
<ul>
<li>{@link com.ibm.as400.resource.RJob#JOB_NAME RJob.JOB_NAME}
<li>{@link com.ibm.as400.resource.RJob#USER_NAME RJob.USER_NAME}
<li>{@link com.ibm.as400.resource.RJob#JOB_NUMBER RJob.JOB_NUMBER}
<li>{@link com.ibm.as400.resource.RJob#JOB_TYPE RJob.JOB_TYPE}
<li>{@link com.ibm.as400.resource.RJob#JOB_SUBTYPE RJob.JOB_SUBTYPE}
</ul>
</a>                                                            

<p>Use one or more of these sort IDs with 
{@link com.ibm.as400.resource.ResourceList#getSortValue() getSortValue()}
and {@link com.ibm.as400.resource.ResourceList#setSortValue(java.lang.Object[]) setSortValue()}
to access the sort values for an RJobList.
       
<p>RJobList objects generate {@link com.ibm.as400.resource.RJob RJob} objects. 

<blockquote><pre>
// Create an RJobList object to represent a list of jobs.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RJobList jobList = new RJobList(system);
<br>
// Set the selection so that only active jobs with the name
// "QZDASOINIT" are included in the list.
jobList.setSelectionValue(RJobList.PRIMARY_JOB_STATUSES, new String[] { RJob.JOB_STATUS_ACTIVE });
jobList.setSelectionValue(RJobList.JOB_NAME, "QZDASOINIT");
<br>
// Set the sort value so that the list is sorted by
// user name and job type.
Object[] sortValue = new Object[] { RJob.USER_NAME, RJob.JOB_TYPE };
jobList.setSortValue(sortValue);
<br>
// Open the list and wait for it to complete.
jobList.open();
jobList.waitForComplete();
<br>
// Read and print the job numbers for the jobs in the list.
long numberOfJobs = jobList.getListLength();
for(long i = 0; i &lt; numberOfJobs; ++i)
{
    RJob job = (RJob)jobList.resourceAt(i);
    System.out.println(job.getAttributeValue(RJob.JOB_NUMBER));
}
<br>
// Close the list.
jobList.close();
</pre></blockquote>
                                                                                     
@see RJob
**/
public class RJobList
extends SystemResourceList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static final String                 PRESENTATION_KEY_           = "JOB_LIST";
    private static final String                 ICON_BASE_NAME_             = "RJobList";
    private static final String                 SORTS_PRESENTATION_KEY_     = "JOB";
    private static PresentationLoader           presentationLoader_         = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");



//-----------------------------------------------------------------------------------------
// Public constants.
//-----------------------------------------------------------------------------------------

/**
Constant indicating that all jobs are returned.
**/
    public static final String ALL = "*ALL";


/**
Constant indicating that a blank value is used.
**/
    public static final String BLANK = "*BLANK";


/**
Constant indicating that the current value is used.
**/
    public static final String CURRENT = "*CURRENT";



//-----------------------------------------------------------------------------------------
// Selection IDs.
//
// * If you add a selection here, make sure and add it to the class javadoc
//   and in ResourceMRI.java.
//-----------------------------------------------------------------------------------------

    private static ResourceMetaDataTable selections_        = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);



/**
Selection ID for job name.  This identifies a String selection,
which represents a specific job name.  Possible values are:
<ul>
<li>"*" - Only the job that this program is running in. 
<li><a href="#CURRENT">CURRENT</a> - All jobs with the current job's name.    
<li><a href="#ALL">ALL</a> - All job names.  
<li>A job name.
</ul>
The default is ALL.
**/
    public static final String JOB_NAME                        = "JOB_NAME";

    static {
        selections_.add(JOB_NAME, String.class, false,
                     new String[] { "*", CURRENT, ALL }, ALL, false);
    }



/**
Selection ID for job number.  This identifies a String selection,
which represents a specific job number.  Possible values are:
<ul>
<li><a href="#ALL">ALL</a> - All job numbers.  
<li>A job number.
</ul>
The default is ALL.
**/
    public static final String JOB_NUMBER                       = "JOB_NUMBER";

    static {
        selections_.add(JOB_NUMBER, String.class, false,
                     new String[] { ALL }, ALL, false);
    }



/**
Selection ID for job type.  This identifies a String selection,
which represents the type of job to be listed.  Possible values are:
<ul>
<li><a href="#ALL">ALL</a> - All job types.  
<li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_AUTOSTART RJob.JOB_TYPE_AUTOSTART} - The job is an autostart job.
<li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_BATCH RJob.JOB_TYPE_BATCH} - The job is a batch job.
<li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_INTERACTIVE RJob.JOB_TYPE_INTERACTIVE} - The job is an interactive job.
<li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SUBSYSTEM_MONITOR RJob.JOB_TYPE_SUBSYSTEM_MONITOR} - The job is a subsystem monitor job.
<li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SPOOLED_READER RJob.JOB_TYPE_SPOOLED_READER} - The job is a spooled reader job.
<li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SYSTEM RJob.JOB_TYPE_SYSTEM} - The job is a system job.
<li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SPOOLED_WRITER RJob.JOB_TYPE_SPOOLED_WRITER} - The job is a spooled writer job.
<li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SCPF_SYSTEM RJob.JOB_TYPE_SCPF_SYSTEM} - The job is the SCPF system job.
</ul>
The default is ALL.
**/
    public static final String JOB_TYPE                        = "JOB_TYPE";

    static {
        selections_.add(JOB_TYPE, String.class, false,
                     new String[] { ALL,
                         RJob.JOB_TYPE_AUTOSTART, 
                         RJob.JOB_TYPE_BATCH, 
                         RJob.JOB_TYPE_INTERACTIVE, 
                         RJob.JOB_TYPE_SUBSYSTEM_MONITOR, 
                         RJob.JOB_TYPE_SPOOLED_READER, 
                         RJob.JOB_TYPE_SYSTEM,
                         RJob.JOB_TYPE_SPOOLED_WRITER, 
                         RJob.JOB_TYPE_SCPF_SYSTEM }, ALL, true);
    }



/**
Selection ID for jobs on primary job statuses.  This identifies a String array selection,
which represents the primary statuses of the jobs to be included in the list.  
Possible values for each element of the array are:
<ul>
<li>{@link com.ibm.as400.resource.RJob#JOB_STATUS_ACTIVE RJob.JOB_STATUS_ACTIVE} - The job is active.
<li>{@link com.ibm.as400.resource.RJob#JOB_STATUS_JOBQ RJob.JOB_STATUS_JOBQ} - The job is currently on a job queue.
<li>{@link com.ibm.as400.resource.RJob#JOB_STATUS_OUTQ RJob.JOB_STATUS_OUTQ} - The job has completed running, but still has output
    on an output queue.
</ul>
**/
    public static final String PRIMARY_JOB_STATUSES                        = "PRIMARY_JOB_STATUSES";

    static {
        selections_.add(PRIMARY_JOB_STATUSES, String.class, false,
                     new String[] { RJob.JOB_STATUS_ACTIVE, 
                         RJob.JOB_STATUS_JOBQ, 
                         RJob.JOB_STATUS_OUTQ }, null, true, true);
    }



/**
Selection ID for user name.  This identifies a String selection,
which represents a specific user profile name.  Possible values are:
<ul>
<li><a href="#CURRENT">CURRENT</a> - All jobs with the current job's user profile.    
<li><a href="#ALL">ALL</a> - All jobs regardless of user name.  
<li>A user profile name.
</ul>
The default is ALL.
**/
    public static final String USER_NAME                        = "USER_NAME";

    static {
        selections_.add(USER_NAME, String.class, false,
                     new String[] { CURRENT, ALL }, ALL, false);
    }



//-----------------------------------------------------------------------------------------
// Sort IDs.
//
// * If you add a sort here, make sure and add it to the class javadoc
//   and in ResourceMRI.java.
//-----------------------------------------------------------------------------------------

    private static ResourceMetaDataTable sorts_        = new ResourceMetaDataTable(presentationLoader_, SORTS_PRESENTATION_KEY_);

    static {
        sorts_.add(RJob.JOB_NAME, String.class, true);
        sorts_.add(RJob.USER_NAME, String.class, true);
        sorts_.add(RJob.JOB_NUMBER, String.class, true);
        sorts_.add(RJob.JOB_TYPE, String.class, true);
        sorts_.add(RJob.JOB_SUBTYPE, String.class, true);
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_      = "com.ibm.as400.resource.RJobList";
    private static final String             formatName_         = "oljb0100";
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

    private static final String openListProgramName_    = "qgyoljob";



//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs an RJobList object.
**/
    public RJobList()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_),
              RJob.attributes_,
              selections_, 
              sorts_, 
              openListProgramName_,
              formatName_,
              null);
    }



/**
Constructs an RJobList object.

@param system   The system.
**/
    public RJobList(AS400 system)
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
    }



//-----------------------------------------------------------------------------------------
// List implementation.
//-----------------------------------------------------------------------------------------

    void setOpenParameters(ProgramCallDocument document)
        throws PcmlException, ResourceException
    {
        super.setOpenParameters(document);

        // Set the selections.
        document.setValue("qgyoljob.jobSelectionInformation.jobName", ((String)getSelectionValue(RJobList.JOB_NAME)).toUpperCase());
        document.setValue("qgyoljob.jobSelectionInformation.userName", ((String)getSelectionValue(RJobList.USER_NAME)).toUpperCase());
        document.setValue("qgyoljob.jobSelectionInformation.jobNumber", (String)getSelectionValue(RJobList.JOB_NUMBER));

        String jobType = (String)getSelectionValue(RJobList.JOB_TYPE);
        document.setValue("qgyoljob.jobSelectionInformation.jobType", (jobType.equals(ALL) ? "*" : jobType));

        int offset = 108;

        String[] primaryJobStatuses = (String[])getSelectionValue(RJobList.PRIMARY_JOB_STATUSES);
        if (primaryJobStatuses == null)
            primaryJobStatuses = new String[0];
        document.setIntValue("qgyoljob.jobSelectionInformation.offsetToPrimaryJobStatusArray", offset);
        document.setIntValue("qgyoljob.jobSelectionInformation.numberOfPrimaryJobStatusEntries", primaryJobStatuses.length);
        for (int i = 0; i < primaryJobStatuses.length; ++i) {
            document.setValue("qgyoljob.jobSelectionInformation.primaryJobStatus", new int[] { i }, primaryJobStatuses[i]);
            offset += 10;
        }

        // Set the sorts.
        Object[] sort = getSortValue();
        document.setIntValue("qgyoljob.sortInformation.numberOfKeysToSortOn", sort.length);
        for(int i = 0; i < sort.length; ++i) {
            int startingPosition;
            int length;
            int dataType = 4;   // Single byte character data.
            boolean sortOrder = getSortOrder(sort[i]);
            // The offsets used here are 1-based.  This seems strange
            // to me, but the API gave me an error when I used 0!
            if (sort[i].equals(RJob.JOB_NAME)) {
                startingPosition = 1;
                length = 10;
            }
            else if (sort[i].equals(RJob.USER_NAME)) {
                startingPosition = 11;
                length = 10;
            }
            else if (sort[i].equals(RJob.JOB_NUMBER)) {
                startingPosition = 21;
                length = 6;
            }
            else if (sort[i].equals(RJob.JOB_TYPE)) {
                startingPosition = 53;
                length = 1;
            }
            else if (sort[i].equals(RJob.JOB_SUBTYPE)) {
                startingPosition = 54;
                length = 1;
            }
            else {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Bad sort value specified: " + sort[i] + "(" + sort[i].getClass() + ")");
                throw new ExtendedIllegalStateException("sortValue[" + i + "]", ExtendedIllegalStateException.UNKNOWN);
            }

            int[] index = new int[] { i };
            document.setIntValue("qgyoljob.sortInformation.sortKey.sortKeyFieldStartingPosition", index, startingPosition);
            document.setIntValue("qgyoljob.sortInformation.sortKey.sortKeyFieldLength", index, length);
            document.setIntValue("qgyoljob.sortInformation.sortKey.sortKeyFieldDataType", index, dataType);
            document.setValue("qgyoljob.sortInformation.sortKey.sortOrder", index, sortOrder ? "1" : "2");
        }
    }



    Resource newResource(String programName, int[] indices)
        throws PcmlException, ResourceException
    {
        ProgramCallDocument document = getDocument();

        // Gather information from the return.
        String jobNameUsed = (String)document.getValue(programName + ".receiverVariable.jobNameUsed", indices);                
        String userNameUsed = (String)document.getValue(programName + ".receiverVariable.userNameUsed", indices);                
        String jobNumberUsed = (String)document.getValue(programName + ".receiverVariable.jobNumberUsed", indices);                
        byte[] internalJobIdentifier = (byte[])document.getValue(programName + ".receiverVariable.internalJobIdentifier", indices);                
        String status = (String)document.getValue(programName + ".receiverVariable.status", indices);                
        String jobType = (String)document.getValue(programName + ".receiverVariable.jobType", indices);                
        String jobSubtype = (String)document.getValue(programName + ".receiverVariable.jobSubtype", indices);                
                     
        // Create a Job object.
        AS400 system = getSystem();
        Object resourceKey = RJob.computeResourceKey(system, jobNameUsed, userNameUsed, jobNumberUsed, internalJobIdentifier);
        RJob resource = (RJob)ResourcePool.GLOBAL_RESOURCE_POOL.getResource(resourceKey);
        if (resource == null) {
            try {                
                // We could instantiate the RJob with either the name/user/number
                // combination or the internal job id.  The internal job id is supposed
                // to be faster, so I chose to use it.  The name, user, and number
                // can be retrieved using the attribute values initialized below.
                resource = new RJob(system, internalJobIdentifier);
                resource.setResourceKey(resourceKey);
                resource.setName(jobNameUsed);
                resource.setUser(userNameUsed);
                resource.setNumber(jobNumberUsed);
                resource.freezeProperties();
            }
            catch(Exception e) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Exception while creating user from user list", e);
                throw new ResourceException(e);
            }
        }

        // Set the loaded attributes.
        resource.initializeAttributeValue(RJob.INTERNAL_JOB_ID, internalJobIdentifier);
        resource.initializeAttributeValue(RJob.JOB_NAME, jobNameUsed);
        resource.initializeAttributeValue(RJob.USER_NAME, userNameUsed);
        resource.initializeAttributeValue(RJob.JOB_NUMBER, jobNumberUsed);
        resource.initializeAttributeValue(RJob.JOB_STATUS, status);
        resource.initializeAttributeValue(RJob.JOB_TYPE, jobType);
        resource.initializeAttributeValue(RJob.JOB_SUBTYPE, jobSubtype);

        return resource;
    }



}

