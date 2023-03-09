///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputQueueImplRemote.java
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
 * The OutputQueue class represents a server output queue.
 * An instance of this class can be used to manipulate an individual
 * IBM i output queue (hold, release, clear, and so on).
 *
 * See <a href="doc-files/OutputQueueAttrs.html">Output Queue Attributes</a> for
 * valid attributes.
 *
 **/

class OutputQueueImplRemote extends PrintObjectImplRemote
implements OutputQueueImpl
{
    private static final NPCPAttributeIDList attrsToRetrieve_  = new NPCPAttributeIDList();
    private static boolean fAttrIDsToRtvBuilt_ = false;

    private synchronized void buildAttrIDsToRtv()
    {
	    if (!fAttrIDsToRtvBuilt_)
	    {
	        fAttrIDsToRtvBuilt_ = true;
	        // 27 of these
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_AUTHCHCK);      // Authority to check
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATAQUELIB);    // Data queue library name
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATAQUE);       // Data queue name
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_DISPLAYANY);    // Display any file
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBSEPRATR);    // Job separators
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_NUMFILES);      // Number of files
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_NUMWRITERS);    // Number of writers started to queue
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_OPCNTRL);       // Operator controlled
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_ORDER);         // Order of files on queue
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUELIB);     // Output queue library name
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUE);        // Output queue name
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQSTS);       // Output queue status
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRINTER);       // Printer
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_RMTSYSTEM);     // Remote system name
                attrsToRetrieve_.addAttrID(PrintObject.ATTR_SEPPAGE);       // Separator page
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_DESCRIPTION);   // Text description
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOPT);     // user defined options
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJ);     // User defined object
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJLIB);  // User defined object library
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJTYP);  // User defined object type
                attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRTFM);        // User transform program name
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRTFMLIB);     // User transform program library
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERDRV);       // User driver program name
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDRVLIB);     // User driver program library
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBNAME);    // Writer job name
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBNUM);     // Writer job number
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBSTS);     // Writer job status
	        attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBUSER);    // Writer job user name
	    }
    }



    private static NPCPIDOutQ buildIDCodePoint(String IFSQueueName)
    {
	    QSYSObjectPathName ifsPath = new QSYSObjectPathName(IFSQueueName, "OUTQ");

	    return new NPCPIDOutQ(ifsPath.getObjectName(), ifsPath.getLibraryName());
    }



    /**
     * Clears the output queue on the server.
     *
     * @param clearOptions A PrintParameterList object that may have any of the
     *        following attributes set:
     * <UL>
     *   <LI> <A HREF="doc-files/PrintAttributes.html#HDRKEY60">
     *        ATTR_JOBUSER</A> - Clear output queue by a user id.  May be a specific userid, "*ALL" or
     *                       "*CURRENT".  "*CURRENT" is the default.
     *   <LI> <A HREF="doc-files/PrintAttributes.html#HDRKEY45">
     *        ATTR_FORMTYPE</A> - Clear output queue by a form type.  May be a specific form type, "*ALL" or
     *                        "*STD".  "*ALL" is the default.
     *   <LI> <A HREF="doc-files/PrintAttributes.html#HDRKEY119">
     *        ATTR_USERDATA</A> - Clear output queue by user data.  May be a specific user data or "*ALL".
     *                        "*ALL" is the default.
     * </UL>
     *  clearOptions may be null.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         server operating system is not at the correct level.
     **/
    public void clear(PrintParameterList clearOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        /*  checkRunTimeState(); */   // RunTime check done in OutputQueue

	    NPDataStream sendDS = new NPDataStream(NPConstants.OUTPUT_QUEUE);
	    NPDataStream returnDS = new NPDataStream(NPConstants.OUTPUT_QUEUE);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

	    NPCPAttribute  cpCPFMessage = new NPCPAttribute();

	    sendDS.setAction(NPDataStream.PURGE);
        sendDS.addCodePoint(getIDCodePoint());

	    if (clearOptions != null)
	    {
	        // create our own copy of the code point and change it into a selection code point
	        NPCPAttribute selectionCP = new NPCPAttribute(clearOptions.getAttrCodePoint());
	        selectionCP.setID(NPCodePoint.SELECTION);
	        sendDS.addCodePoint(selectionCP);
	    }

	    returnDS.addCodePoint(cpCPFMessage);

	    npSystem.makeRequest(sendDS, returnDS);

	    updateAttrs(getAttrIDsToRetrieve());

        /* fireOutputQueueEvent(OutputQueueEvent.CLEARED); */

    } // end clear



    // This method implements an abstract method of the superclass
    NPCPAttributeIDList getAttrIDsToRetrieve()
    {
	    if (!fAttrIDsToRtvBuilt_) {
	        buildAttrIDsToRtv();
	    }
	    return attrsToRetrieve_;
    }
    
   // retrieve only one attribute 
   NPCPAttributeIDList getAttrIDsToRetrieve(int attrToRtv)
    {
   if (!fAttrIDsToRtvBuilt_) {
	    attrsToRetrieve_.addAttrID(attrToRtv);
        }
        return attrsToRetrieve_;
    }


    /**
     * Holds the output queue on the server.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         server operating system is not at the correct level.
     **/
    public void hold()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        /*  checkRunTimeState(); */   // RunTime check done in OutputQueue

	    NPDataStream sendDS = new NPDataStream(NPConstants.OUTPUT_QUEUE);
	    NPDataStream returnDS = new NPDataStream(NPConstants.OUTPUT_QUEUE);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

	    NPCPAttribute cpCPFMessage = new NPCPAttribute();

	    sendDS.setAction(NPDataStream.HOLD);
        sendDS.addCodePoint(getIDCodePoint());

	    returnDS.addCodePoint(cpCPFMessage);

	    npSystem.makeRequest(sendDS, returnDS);

	    updateAttrs(getAttrIDsToRetrieve());

        /* fireOutputQueueEvent(OutputQueueEvent.HELD); */
    } // end hold



    /**
     * Releases a held output queue on the server.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         server operating system is not at the correct level.
     **/
    public void release()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        /*  checkRunTimeState(); */   // RunTime check done in OutputQueue

	    NPDataStream sendDS = new NPDataStream(NPConstants.OUTPUT_QUEUE);
	    NPDataStream returnDS = new NPDataStream(NPConstants.OUTPUT_QUEUE);
        NPSystem  npSystem = NPSystem.getSystem(getSystem());

	    NPCPAttribute  cpCPFMessage = new NPCPAttribute();

	    sendDS.setAction(NPDataStream.RELEASE);
        sendDS.addCodePoint(getIDCodePoint());

	    returnDS.addCodePoint(cpCPFMessage);

	    npSystem.makeRequest(sendDS, returnDS);

	    updateAttrs(getAttrIDsToRetrieve());

        /* fireOutputQueueEvent(OutputQueueEvent.RELEASED); */

    } // end release

}
