///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RPrinterList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;



/**
The RPrinterList class represents a list of AS/400 printers.

<a name="selectionIDs"><p>The following selection IDs are supported:
<ul>
<li>{@link #PRINTER_NAMES PRINTER_NAMES}
<li>{@link #OUTPUT_QUEUES OUTPUT_QUEUES}
</ul>
</a>

<p>Use one or more of these selection IDs with
{@link com.ibm.as400.resource.ResourceList#getSelectionValue getSelectionValue()}
and {@link com.ibm.as400.resource.ResourceList#setSelectionValue setSelectionValue()}
to access the selection values for an RPrinterList.

<p>RPrinterList objects generate {@link com.ibm.as400.resource.RPrinter RPrinter} objects.

<blockquote><pre>
// Create an RPrinterList object to represent a list of printers.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RPrinterList printerList = new RPrinterList(system);
<br>
// Set the selection so that only printers which are selecting
// spooled files from the listed output queues are included
// in the list.
printerList.setSelectionValue(RPrinterList.OUTPUT_QUEUES,
                              new String[] { "/QSYS.LIB/MYLIB.LIB/MYOUTQ1.OUTQ1",
                                             "/QSYS.LIB/MYLIB.LIB/MYOUTQ2.OUTQ2" });
<br>
// Open the list and wait for it to complete.
printerList.open();
printerList.waitForComplete();
<br>
// Read and print the device names and statuses
// for the printers in the list.
long numberOfPrinters = printerList.getListLength();
for(long i = 0; i &lt; numberOfPrinters; ++i)
{
    RPrinter printer = (RPrinter)printerList.resourceAt(i);
    System.out.println(printer.getAttributeValue(RPrinter.DEVICE_NAME));
    System.out.println(printer.getAttributeValue(RPrinter.DEVICE_STATUS));
    System.out.println();
}
<br>
// Close the list.
printerList.close();
</pre></blockquote>

@deprecated Use
{@link com.ibm.as400.access.PrinterList PrinterList} instead, as this package may be removed in the future.
@see RPrinter
**/
public class RPrinterList
extends SystemResourceList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static final String                 PRESENTATION_KEY_           = "PRINTER_LIST";
    private static final String                 ICON_BASE_NAME_             = "RPrinterList";
    private static PresentationLoader           presentationLoader_         = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");




//-----------------------------------------------------------------------------------------
// Selection IDs.
//
// * If you add a selection here, make sure and add it to the class javadoc
//   and in ResourceMRI.java.
//-----------------------------------------------------------------------------------------

    private static ResourceMetaDataTable selections_        = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);



/**
Selection ID for printer names.  This identifies a String selection,
which represents the name(s) to be included in the list.
**/
    public static final String PRINTER_NAMES                      = "PRINTER_NAMES";

    static {
        selections_.add(PRINTER_NAMES, String.class, false, null, null, false, true);
    }



/**
Selection ID for output queues.  This identifies a String selection,
which represents the output queues(s) to be included in the list.
**/
    public static final String OUTPUT_QUEUES                      = "OUTPUT_QUEUES";

    static {
        selections_.add(OUTPUT_QUEUES, String.class, false, null, null, false, true);
    }



//-----------------------------------------------------------------------------------------
// Open list attribute map.
//-----------------------------------------------------------------------------------------

    private static ProgramMap            openListAttributeMap_  = new ProgramMap();
    private static final String          openListProgramName_    = "qgyrprtl";

    static {
        ValueMap boolean01Map = new BooleanValueMap("0", "1");
        openListAttributeMap_.add(RPrinter.DEVICE_NAME, null, "receiverVariable.deviceName");
        openListAttributeMap_.add(RPrinter.TEXT_DESCRIPTION, null, "receiverVariable.textDescription");
        openListAttributeMap_.add(RPrinter.OVERALL_STATUS, null, "receiverVariable.overallStatus");
        openListAttributeMap_.add(RPrinter.DEVICE_STATUS, null, "receiverVariable.deviceStatus");
        openListAttributeMap_.add(RPrinter.OUTPUT_QUEUE, null, "receiverVariable.outputQueue");
        openListAttributeMap_.add(RPrinter.OUTPUT_QUEUE_STATUS, null, "receiverVariable.outputQueueStatus");
        openListAttributeMap_.add(RPrinter.WRITER_STATUS, null, "receiverVariable.writerStatus");
        openListAttributeMap_.add(RPrinter.WRITER_STARTED, null, "receiverVariable.writerStarted", boolean01Map);
        openListAttributeMap_.add(RPrinter.FORM_TYPE, null, "receiverVariable.formType");
        openListAttributeMap_.add(RPrinter.SPOOLED_FILE_NAME, null, "receiverVariable.currentFileName");
        openListAttributeMap_.add(RPrinter.USER_NAME, null, "receiverVariable.currentFileUser");
        openListAttributeMap_.add(RPrinter.PUBLISHED_STATUS, null, "receiverVariable.publishingStatus", boolean01Map, new ResourceLevel(ResourceLevel.V5R1M0));
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_      = "com.ibm.as400.resource.RPrinterList";
    private static ProgramCallDocument      staticDocument_     = null;
    private static final String             formatName_         = "prtl0200";

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

    private static final String deviceNameDataName_            = ".receiverVariable.deviceName";




//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs an RPrinterList object.
**/
    public RPrinterList()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_),
              RPrinter.attributes_,
              selections_,
              null,
              openListProgramName_,
              formatName_,
              null);
    }



/**
Constructs an RPrinterList object.

@param system   The system.
**/
    public RPrinterList(AS400 system)
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

        // Set the printer names.
        Object printerNamesSV = getSelectionValue(RPrinterList.PRINTER_NAMES);
        String[] printerNames;
        if (printerNamesSV == null)
            printerNames = new String[0];
        else if (printerNamesSV instanceof String)
            printerNames = new String[] { (String)printerNamesSV };
        else
            printerNames = (String[])printerNamesSV;
        document.setIntValue("qgyrprtl.filterInformation.numberOfPrinterNames", printerNames.length);
        for(int i = 0; i < printerNames.length; ++i)
            document.setValue("qgyrprtl.filterInformation.printerName", new int[] { i }, printerNames[i].toUpperCase());

        // Set the output queues.
        Object outputQueuesSV = getSelectionValue(RPrinterList.OUTPUT_QUEUES);
        String[] outputQueues;
        if (outputQueuesSV == null)
            outputQueues = new String[0];
        else if (outputQueuesSV instanceof String)
            outputQueues = new String[] { (String)outputQueuesSV };
        else
            outputQueues = (String[])outputQueuesSV;
        document.setIntValue("qgyrprtl.filterInformation.numberOfOutputQueues", outputQueues.length);
        for(int i = 0; i < outputQueues.length; ++i) {
            QSYSObjectPathName path = new QSYSObjectPathName(outputQueues[i].toUpperCase());
            document.setValue("qgyrprtl.filterInformation.outputQueue.objectName", new int[] { i }, path.getObjectName());
            document.setValue("qgyrprtl.filterInformation.outputQueue.libraryName", new int[] { i }, path.getLibraryName());
        }

    }



    Resource newResource(String programName, int[] indices)
    throws PcmlException, ResourceException
    {
        ProgramCallDocument document = getDocument();

        String name = (String)document.getValue(programName + deviceNameDataName_, indices);
        AS400 system = getSystem();
        Object resourceKey = RPrinter.computeResourceKey(system, name);
        RPrinter resource = (RPrinter)ResourcePool.GLOBAL_RESOURCE_POOL.getResource(resourceKey);
        if (resource == null) {
            try {
                resource = new RPrinter(system, name);
                resource.setResourceKey(resourceKey);
                resource.freezeProperties();
            }
            catch(Exception e) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Exception while creating printer from printer list", e);
                throw new ResourceException(e);
            }
        }

        // Copy the information from the API record to the RPrinter attributes.
        try {
            Object[] attributeIDs = openListAttributeMap_.getIDs(ResourceLevel.vrmToLevel(system.getVRM()));
            Object[] values = openListAttributeMap_.getValues(attributeIDs, system, document, programName, indices);
            for(int i = 0; i < values.length; ++i) {
                resource.initializeAttributeValue(attributeIDs[i], values[i]);
            }
        }
        catch(Exception e) {
            throw new ResourceException(e);
        }

        return resource;
    }



}
