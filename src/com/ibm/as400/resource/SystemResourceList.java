///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemResourceList.java
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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;



/**
The SystemResourceList class represents a subclass of
of the <a href="BufferedResourceList.html">BufferedResourceList</a>
class which retrieves list items using AS/400 System Open List
Application Programming Interfaces (APIs).  This class is intended
to be extended and customized by subclasses.
**/
public class SystemResourceList
extends BufferedResourceList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private static final String closeProgramName_                       = "qgyclst";
    private static final String requestHandleDataNameSuffix_            = ".requestHandle";
    private static final String closeRequestHandleDataName_             = "qgyclst.requestHandle";
    private static final String getProgramName_                         = "qgygtle";
    private static final String getNumberOfRecordsToReturnDataName_     = "qgygtle.numberOfRecordsToReturn";
    private static final String getRequestHandleDataName_               = "qgygtle.requestHandle";
    private static final String getStartingRecordDataName_              = "qgygtle.startingRecord";
    private static final String listInformationDataNameSuffix_          = ".listInformation";
    private static final String listStatusIndicatorDataNameSuffix_      = ".listInformation.listStatusIndicator";
    private static final String receiverVariableLengthDataName_         = "qgygtle.receiverVariableLength";
    private static final String recordsReturnedDataNameSuffix_          = ".listInformation.recordsReturned";
    private static final String requestHandleDataNameSuffix2_           = ".listInformation.requestHandle";
    private static final String totalRecordsDataNameSuffix_             = ".listInformation.totalRecords";

    private String              formatName_                         = null;
    private String              openListProgramName_                = null;
    private ProgramMap          selectionMap_                       = null;
    private Hashtable           selectionBidiStringTypes_           = new Hashtable();             // @A2A

    private transient ProgramCallDocument document_               = null;
    private transient int                 formatLength_           = -1;
    private transient byte[]              requestHandle_          = null;



/**
Constructs a SystemResourceList object.

@param presentation         The presentation, or null if not applicable.
@param attributes           The attributes, or null if not applicable.
@param selections           The selections, or null if not applicable.
@param sorts                The sorts, or null if not applicable.
@param openListProgramName  The open list program name.
@param formatName           The format name for each item in the list,
                            or null if not needed.
@param selectionMap         The program map which maps selection values in the
                            resource list to parameters in the PCML document,
                            or null if this should not be used.
**/
    SystemResourceList(Presentation presentation,
                       ResourceMetaDataTable attributes,
                       ResourceMetaDataTable selections,
                       ResourceMetaDataTable sorts,
                       String openListProgramName,
                       String formatName,
                       ProgramMap selectionMap)
    {
        super(presentation, attributes, selections, sorts);
        initializeTransient();

        if (openListProgramName == null)
            throw new NullPointerException("openListProgramName");

        openListProgramName_    = openListProgramName;
        formatName_             = formatName;
        selectionMap_           = selectionMap;
    }



/**
Closes the list.  No further resources can be loaded.   The list
must be closed in order to clean up resources appropriately.
This method has no effect if the list is already closed.
This method fires a listClosed() ResourceListEvent.

@exception ResourceException                If an error occurs.
**/
     public void close()
    throws ResourceException
    {
        if (!isOpen())
            return;

        super.close();

        fireBusy();
        try {
            // Close the list by calling the close list API.
            if ((document_ != null) && (requestHandle_ != null)) {
                document_.setValue(closeRequestHandleDataName_, requestHandle_);
                if (document_.callProgram(closeProgramName_) == false)
                    throw new ResourceException(document_.getMessageList(closeProgramName_));
            }

            // Done with the request handle.
            requestHandle_ = null;
        }
        catch(PcmlException e) {
            throw new ResourceException(e);
        }
        finally {
            fireIdle();
        }
    }



/**
Returns the PCML document.

@return The PCML document.
**/
    ProgramCallDocument getDocument()
    {
        return document_;
    }



/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
        formatLength_ = -1;

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
Indicates if the resource is available.  This means that the
resource has been loaded.

@param index    The index.
@return         true if the resource is available,
                false if the resource is not available
                or the list is not open.

@exception ResourceException                If an error occurs.
**/
    public boolean isResourceAvailable(long index)
    throws ResourceException
    {
        // First check to see if the resource is already buffered in the superclass.
        boolean isAvailable = super.isResourceAvailable(index);

        // If not... and if the list is open...
        if ((! isAvailable) && (isOpen())) {

            // Refresh the status, which will include the current list length.
            loadResources(0, 0);

            // If this index is less than the length, then its available.
            isAvailable = (index < getListLength());
        }

        return isAvailable;
    }




/**
Loads a part of the list.

@param startIndex   The start index (1-based).  If this is 0,
                    then immediately refresh the list status.
                    If this is -1, then wait for the entire
                    list to complete.  If this is greater than
                    0, then wait for the indexed list item
                    to be built.
@param length       The number of resources to load.  If this
                    is 0, then refresh only the list status.

@exception ResourceException                If an error occurs.
**/
    private void loadResources(long startIndex, long length)
    throws ResourceException
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Loading resources (" + startIndex + ", " + length + ") from list " + this + ".");

        fireBusy();
        try {
            // Set the receiver variable length.  This only needs
            // to be set if a format name was provided in the constructor.
            // Otherwise, we will assume its been initialized in the
            // PCML.
            if (formatName_ != null) {
                if (formatLength_ == -1)
                    formatLength_ = document_.getOutputsize(formatName_);
                if (length == 0)
                    document_.setIntValue(receiverVariableLengthDataName_, 1); // 0 causes problems.
                else
                    document_.setIntValue(receiverVariableLengthDataName_, (int)length*formatLength_);
            }

            // Set some other API parameters.
            document_.setValue(getRequestHandleDataName_, requestHandle_);
            document_.setIntValue(getNumberOfRecordsToReturnDataName_, (int)length);
            document_.setIntValue(getStartingRecordDataName_, (int)startIndex);

            // Call the API.
            if (document_.callProgram(getProgramName_) == false)
                throw new ResourceException(document_.getMessageList(getProgramName_));

            // Process the returned information.
            process(getProgramName_, startIndex - 1); // process() is 0-based.
        }
        catch(PcmlException e) {
            throw new ResourceException(e);
        }
        finally {
            fireIdle();
        }
    }



/**
Creates and returns a new resource based on a record returned
from an API call.  A subclass must implement this method.

@param programName  The program name.
@param indices      The indices.  These indicate which record
                    to use.
@return             The new resource.

@exception PcmlException If a PCML error occurs.
@exception ResourceException If an error occurs.
**/
    Resource newResource(String programName,
                         int[] indices)
    throws PcmlException, ResourceException
    {
        throw new ResourceException();
    }



/**
Opens the list.  The list must be open in order to
perform most operations.  This method has no effect
if the list is already opened.

@exception ResourceException                If an error occurs.
**/
     public void open()
    throws ResourceException
    {
        if (isOpen())
            return;

        super.open();

        fireBusy();
        try {
            synchronized(this) {

                // Establish the connection if needed.
                if (! isConnectionEstablished())
                    establishConnection();

                try {
                    // Set the open parameters.
                    setOpenParameters(document_);

                    // Call the program.
                    boolean success = document_.callProgram(openListProgramName_);
                    if (!success)
                        throw new ResourceException(document_.getMessageList(openListProgramName_));
                    requestHandle_ = (byte[])document_.getValue(openListProgramName_ + requestHandleDataNameSuffix2_);
                    process(openListProgramName_, 0);
                }
                catch(Exception e) {
                    // We will mostly catch PcmlExceptions here, but there will be
                    // occasion when IllegalArguementExceptions are thrown by the
                    // text conversion classes.
                    throw new ResourceException(e);
                }
            }
        }
        finally {
            fireIdle();
        }
    }



/**
Process information returned from either the open or get APIs.  This method
will take that information and create new Resources for it.

@param programName      The program name.
@param startIndex   The start index (0-based).

@exception PcmlException If a PCML error occurs.
@exception ResourceException If an error occurs.
**/
    private void process(String programName, long startIndex)
    throws PcmlException, ResourceException
    {
        // For each record that was returned...
        int recordsReturned = document_.getIntValue(programName + recordsReturnedDataNameSuffix_);
        for(int i = 0; i < recordsReturned; ++i) {
            int[] indices = { i };

            // Manufacture a new resource.
            Resource resource = newResource(programName, indices);

            // Notify listeners.
            fireResourceAdded(resource, startIndex + i);
        }

        // Fire any other necessary events.
        String listStatusIndicator = (String)document_.getValue(programName + listStatusIndicatorDataNameSuffix_);
        long newLength = document_.getIntValue(programName + totalRecordsDataNameSuffix_);

        if (Trace.isTraceOn()) {
            Trace.log(Trace.INFORMATION, "API List status indicator = " + listStatusIndicator);
            Trace.log(Trace.INFORMATION, "API List length = " + newLength);
        }

        if (listStatusIndicator.equals("2"))
            fireListCompleted();
        else if (listStatusIndicator.equals("3"))
            fireListInError();
        if (newLength != getListLength())
            fireLengthChanged(newLength);
    }




/**
Deserializes the resource list.
**/
    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient ();
    }



/**
Refreshes the contents of the list.

<p>This will implicitly open the list if needed.

@exception ResourceException                If an error occurs.
**/
     public void refreshContents()
    throws ResourceException
    {
        // Establish the connection if needed.
        if (! isConnectionEstablished())
            establishConnection();

        // Close to force a re-open.
        if (isOpen())
            close();

        super.refreshContents();
    }



/**
Refreshes the status of the list.  The status includes
the length and whether the list is completed or in error.
If the list is complete, this method has no effect.

<p>This method does not refresh the contents of the list.  Use
<a href="#refreshContents()">refreshContents()</a> to refresh
the contents of the list.

<p>This will implicitly open the list if needed.

@exception ResourceException                If an error occurs.
**/
    public void refreshStatus()
        throws ResourceException
    {
        super.refreshStatus();
        loadResources(0, 0);
    }



/**
Returns the resource specified by the index.

<p>This will implicitly open the list if needed.

@param  index   The index.
@return         The resource specified by the index, or null
                if the resource is not yet available.

@exception ResourceException                If an error occurs.
**/
     public Resource resourceAt(long index)
    throws ResourceException
    {
        synchronized(this) {

            // It may already be here.
            Resource resource = super.resourceAt(index);

            // If not, try to load it.
            if (resource == null) {
                loadResources(index + 1, getPageSize()); // loadResources() is 1-based
                resource = super.resourceAt(index);

                // If the resource is STILL null, it means that the buffer
                // swapped out the resource during loading.  This happens
                // only when the number of pages is 1.  The simplest
                // workaround is to reload the single resource.
                if (resource == null) {
                    if (Trace.isTraceOn())
                        Trace.log(Trace.INFORMATION, "Double load scenario, index=" + index +
                                  ", number of pages= " + getNumberOfPages() + ", page size="
                                  + getPageSize());

                    loadResources(index + 1, getPageSize()); // loadResource() is 1-based
                    resource = super.resourceAt(index);
                }
            }

            return resource;
        }
    }



/**
Sets the parameters for the open API.  This uses the selection map
to copy selection values from the resource list to the parameters
in the PCML document.

<p>If different settings are needed, then override this method.

@param document     The document.

@exception PcmlException        If a PCML error occurs.
@exception ResourceException    If a Resource error occurs.
**/
    void setOpenParameters(ProgramCallDocument document)
    throws PcmlException, ResourceException
    {
        if (selectionMap_ != null) {

            // Copy the selection values from the resource list to the
            // parameters in the PCML document.
            ResourceMetaData[] selectionMetaData = getSelectionMetaData();
            Object[] ids = new Object[selectionMetaData.length];
            Object[] values = new Object[selectionMetaData.length];
            int[] bidiStringTypes = new int[selectionMetaData.length];                                                  // @A2A
            for(int i = 0; i < selectionMetaData.length; ++i) {
                ids[i] = selectionMetaData[i].getID();
                values[i] = getSelectionValue(ids[i]);
                Integer selectionBidiStringType = (Integer)selectionBidiStringTypes_.get(ids[i]);   // @A2A
                if (selectionBidiStringType == null)                                                // @A2A
                    bidiStringTypes[i] = getDefaultBidiStringType();                                // @A2A
                else                                                                                // @A2A
                    bidiStringTypes[i] = selectionBidiStringType.intValue();                        // @A2A
            }
            selectionMap_.setValues(ids, values, getSystem(), document, openListProgramName_, null, bidiStringTypes);   // @A2C
        }
    }



/**
Sets the PCML document.

@param document               The PCML document.
**/
    void setDocument(ProgramCallDocument document)
    {
        if (document == null)
            throw new NullPointerException("document");

        document_                 = document;

        AS400 system = getSystem();
        if (system != null)
            document_.setSystem(system);
    }



    void setOpenListProgramName(String openListProgramName)
    {
        if (openListProgramName == null)
            throw new NullPointerException("openListProgramName");

        openListProgramName_ = openListProgramName;
    }



// @A2A
/**
Sets the current value of a selection.  The changed selection
value will take effect the next time the list is opened
or refreshed.

@param selectionID      Identifies the selection.
@param value            The selection value, or null to remove
                        the selection.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
                        
@exception ResourceException                If an error occurs.
**/
    public void setSelectionValue(Object selectionID, Object value, int bidiStringType)
        throws ResourceException
    {
        super.setSelectionValue(selectionID, value, bidiStringType);

        selectionBidiStringTypes_.put(selectionID, new Integer(bidiStringType));
    }




/**
Waits until the list is completely loaded.

<p>This will implicitly open the list if needed.

@exception ResourceException                If an error occurs.
**/
     public void waitForComplete()
    throws ResourceException
    {
        super.waitForComplete();
        if (!isComplete())
            loadResources(-1, 0);
    }



/**
Waits until the resource is available or the list is
complete.

<p>This will implicitly open the list if needed.

@param index    The index.

@exception ResourceException                If an error occurs.
**/
     public void waitForResource(long index)
    throws ResourceException
    {
        super.waitForResource(index);
        if (isComplete())
            return;
        if (!isResourceAvailable(index))
            loadResources(index + 1, 0); // loadResource() is 1-based
    }


}
