///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: WriterJob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 * The WriterJob class represents an AS/400 writer job.
 * An instance of this class can be used to manipulate an individual
 * AS/400 writer.  Use the start method to obtain a instance of this class.
 *
 * See <a HREF="doc-files/WriterJobAttrs.html">Writer Job Attributes</a> for
 * valid attributes.
 *
 **/

public class WriterJob extends PrintObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    static final long serialVersionUID = 4L;



    // We have decided that writer jobs are too transient to
    // be a JavaBean.

    // constructor used internally (not externalized since it takes
    // an ID code point
    WriterJob(AS400 system, NPCPIDWriter id, NPCPAttribute attrs)
    {
       super(system, id, attrs, NPConstants.WRITER_JOB); // @B1C
    }



    // A1A - Added chooseImpl() method
    /**
     * Chooses the appropriate implementation.
     **/
    void chooseImpl()
    throws IOException, AS400SecurityException                              // @B1A
    {
        // We need to get the system to connect to...
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use WriterJob before setting system." );
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (WriterJobImpl) system.loadImpl2("com.ibm.as400.access.WriterJobImplRemote",
                                                 "com.ibm.as400.access.WriterJobImplProxy");
        super.setImpl();
    }



    /**
     * Ends a writer on the AS/400.
     *
     * @param endType When to end the writer.
     *  May be any of the following values:
     * <UL>
     *   <LI> *CNTRLD - The writer is ended at the end of the current spooled file.
     *   <LI> *IMMED - The writer is ended immediately.
     *   <LI> *PAGEEND - The writer is ended at the end of the current page.
     * </UL>
     *  <i>endType</i> may be null.  If <i>endType</i> is not specified, the default is
     * *IMMED.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
     **/
    public void end(String endType)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        if (impl_ == null)                              // @A1A
            chooseImpl();                               // @A1A
        ((WriterJobImpl) impl_).end(endType);           // @A1A
    } // end end



    /**
     * Returns the name of the writer.
     *
     * @return The name of the writer.
     **/
    public String getName()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(ATTR_WTRJOBNAME);
        }
    }



    /**
     * Starts a writer on the AS/400.
     * Use this method to start a new writer job on the given AS/400
     * with the specified parameters.
     * @param system The system on which to start the writer job.
     * @param printer The printer that should be used
     *                to start the writer job.  This printer
     *                must reside on the same AS/400 system that the
     *                writer job is being started.
     * @param options Optional.  A print parameter list that contains
     *                          a list of attributes to start the writer job.
     *                          The output queue parameters set in this list override the
     *                          output queue parameter.
     *                          The following parameters may be set:
     * <ul>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY2">
     *         ATTR_ALIGN - Align page
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY3">
     *         ATTR_ALWDRTPRT - Allow direct print
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY6">
     *         ATTR_AUTOEND - Automatically end writer
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY39">
     *         ATTR_DRWRSEP - Drawer for separators
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY41">
     *         ATTR_FILESEP - File separators
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY45">
     *         ATTR_FORMTYPE - Form type
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY551">
     *         ATTR_WTRINIT - Initialize the writer
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY57">
     *         ATTR_JOBNAME - Job name
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY58">
     *         ATTR_JOBNUMBER - Job number
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY60">
     *         ATTR_JOBUSER - Job user
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY46">
     *         ATTR_FORMTYPEMSG - Form type message option
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_5">
     *         ATTR_MESSAGE_QUEUE - Message queue integrated file system  name
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_6">
     *         ATTR_OUTPUT_QUEUE - Output queue integrated file system  name
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY108">
     *         ATTR_SPOOLFILE - Spooled file name
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY109">
     *         ATTR_SPLFNUM - Spooled file number
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY123">
     *         ATTR_WTRAUTOEND - When to automatically end writer
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY133">
     *         ATTR_WTRSTRPAGE - Writer starting page
     *      </A>
     * <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY129">
     *         ATTR_WTRJOBNAME - Writer job name
     *      </A>
     *
     * </ul>
     * <br>
     *
     * @param outputQueue Optional.  The output queue to start the
     *                               writer job.  The output queue must reside on
     *                               the same AS/400 system that the writer job
     *                               is being created.
     *
     * @return A writer job object that was created.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     **/
    public static WriterJob start(AS400 system,
                  Printer printer,
                  PrintParameterList options,
                  OutputQueue outputQueue)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException

    {
        // note: This is a static method

        // First check the required parameters of system and printer
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }

        if (printer == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'printer' is null.");
            throw new NullPointerException("printer");
        }

        if (printer.getImpl() == null) {                    // @A2A
            printer.chooseImpl();                           // @A2A
        }                                                   // @A2A
    
        OutputQueueImpl oqi = null;                         // @A2A
        if (outputQueue != null) {                          // @A2A
            if (outputQueue.getImpl() == null) {            // @A2A
                outputQueue.chooseImpl();                   // @A2A
            }                                               // @A2A
            oqi = (OutputQueueImpl) outputQueue.getImpl();  // @A2A
        }                                                   // @A2A
    
        WriterJobImpl impl = (WriterJobImpl) system.loadImpl2("com.ibm.as400.access.WriterJobImplRemote",
                                                              "com.ibm.as400.access.WriterJobImplProxy");  // @A1A

        // @A2A Changed below line to send in the Impls, and receive the
        //      NPCPIDWriter instead of a WriterJob
        NPCPIDWriter cpWriterID = ((WriterJobImpl) impl).start(system.getImpl(),
                                                            (PrintObjectImpl) printer.getImpl(),            
                                                            options,
                                                            oqi);   
       
        return new WriterJob(system, cpWriterID, null);     // @A2A
        
    } // end start

} // end WriterJob class
