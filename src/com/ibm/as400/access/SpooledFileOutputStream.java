///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileOutputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
  * The SpooledFileOutputStream class is used to write data into a server spooled file.
  **/
public class SpooledFileOutputStream extends OutputStream
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    //Private Data
    
    private transient AS400 system_;
    private transient SpooledFileOutputStreamImpl  impl_;

    /**
      * Constructs a SpooledFileOutputStream object.
      * Use this object to create a new spooled file on the given system
      * with the specified parameters.
      * @param system The system on which to create the spooled file.
      * @param options       Optional.  A print parameter list that contains
      *                          a list of attributes with which to create the spooled file.
      *                          The attributes set in <I>options</I> will
      *                          override those attributes in the printer file that is used.
      *                          The printer file used will be the one specified with the
      *                          <I>printerFile</I> parameter, or if that parameter is null,
      *                          it will be the default network print server printer file (QPNPSPRTF).
      *                          If the output queue is specified in <I>options</I>, it
      *                          will override any output queue passed in the <I>outputQueue</I>
      *                          parameter.
      *                          The following parameters may be set:
      * <ul>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY2">
      *          ATTR_ALIGN - Align page
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_1">
      *          ATTR_BACK_OVERLAY - Back overlay integrated file system Name
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY12">
      *          ATTR_BKOVL_DWN - Back overlay offset down
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY11">
      *          ATTR_BKOVL_ACR - Back overlay offset across
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY13">
      *          ATTR_CPI - Characters per inch
      *       </A>
      *  <li> (1) <A HREF="doc-files/PrintAttributes.html#HDRKEY14">
      *          ATTR_CODEPAGE - Code page
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY17.5">
      *          ATTR_CONTROLCHAR - Control character
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY177">
      *          ATTR_CONVERT_LINEDATA - Convert line data
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY191">
      *          ATTR_CORNER_STAPLE - Corner staple		
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY17">
      *          ATTR_COPIES - Copies
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY24">
      *          ATTR_DBCSDATA - User-specified DBCS data
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY25">
      *          ATTR_DBCSEXTENSN - DBCS extension characters
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY26">
      *          ATTR_DBCSROTATE - DBCS character rotation
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY27">
      *          ATTR_DBCSCPI - DBCS characters per inch
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY28">
      *          ATTR_DBCSSISO - DBCS SO/SI spacing
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY29">
      *          ATTR_DFR_WRITE - Defer write
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY31">
      *          ATTR_PAGRTT - Degree of page rotation
      *       </A>
      *   <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY194">
      *       ATTR_EDGESTITCH_NUMSTAPLES - Edge Stitch Number of Staples
      *       </A>
      *   <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY192">
      *         ATTR_EDGESTITCH_REF - Edge Stitch Reference	
      *       </A>
      *   <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY193">
      *        ATTR_EDGESTITCH_REFOFF - Edge Stitch Reference Offset
      *       </A> 	
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY40">
      *          ATTR_ENDPAGE - Ending page
      *       </A>
      *  <li> (2) <A HREF="doc-files/PrintAttributes.html#HDRKEY41">
      *          ATTR_FILESEP - File separators
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY42">
      *         ATTR_FOLDREC - Fold records
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY43">
      *         ATTR_FONTID - Font identifier
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_3">
      *         ATTR_FORM_DEFINITION - Form definition integrated file system name
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY44">
      *         ATTR_FORMFEED - Form feed
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY45">
      *         ATTR_FORMTYPE - Form type
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_4">
      *         ATTR_FRONT_OVERLAY - Front overlay integrated file system same
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY51">
      *         ATTR_FTOVL_ACR - Front overlay offset across
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY52">
      *         ATTR_FTOVL_DWN - Front overlay offset down
      *       </A>
      *  <li> (1) <A HREF="doc-files/PrintAttributes.html#HDRKEY53">
      *         ATTR_CHAR_ID - Graphic character set
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY54">
      *         ATTR_JUSTIFY - Hardware justification
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY55">
      *         ATTR_HOLD - Hold spool file
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY55.5">
      *         ATTR_HOLDPNDSTS - Hold Pending Status
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY180">
      *         ATTR_IPP_ATTR_CCSID - IPP Attributes-ccsid
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY181">
      *         ATTR_IPP_JOB_ID - IPP Job ID
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY182">
      *         ATTR_IPP_JOB_NAME - IPP Job Name
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY183">
      *         ATTR_IPP_JOB_NAME_NL - IPP Job Name NL
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY184">
      *         ATTR_IPP_JOB_ORIGUSER - IPP Job Originating User Name
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY185">
      *         ATTR_IPP_JOB_ORIGUSER_NL - IPP Job Originating User Name NL
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY186">
      *         ATTR_IPP_PRINTER_NAME - IPP Printer Name
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY180.5">
      *         ATTR_IPP_ATTR_NL - IPP Natural Language
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY64">
      *         ATTR_LPI - Lines per inch
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY66">
      *         ATTR_MAXRCDS - Maximum spooled output records
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY80">
      *        ATTR_OUTPTY - Output priority
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_6">
      *         ATTR_OUTPUT_QUEUE - Output queue integrated file system name
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY84">
      *         ATTR_OVERFLOW - Overflow line number
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_D">
      *         ATTR_PAGE_DEFINITION - Page definition integrated file system name
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY62">
      *         ATTR_PAGELEN - Length of page
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY67">
      *         ATTR_MEASMETHOD - Measurement method
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY126">
      *         ATTR_PAGEWIDTH - Width of page
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY85">
      *         ATTR_MULTIUP - Pages per side
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY86">
      *         ATTR_POINTSIZE - Point size
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY87">
      *         ATTR_FIDELITY - Print fidelity
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY88">
      *         ATTR_DUPLEX - Print on both sides
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY89">
      *         ATTR_PRTQUALITY - Print quality
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY91">
      *         ATTR_PRTTEXT - Print text
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY92">
      *         ATTR_PRINTER - Printer
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY93">
      *         ATTR_PRTDEVTYPE - Printer device type
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY99">
      *         ATTR_RPLUNPRT - Replace unprintable characters
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY100">
      *         ATTR_RPLCHAR - Replacement character
      *       </A>
      *   <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY197">
      *        ATTR_SADDLESTITCH_NUMSTAPLES - Saddle Stitch Number of Staples		
      *       </A>
      *   <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY196">
      *         ATTR_SADDLESTITCH_REF - Saddle Stitch Reference		
      *       </A>
      *   <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY102">
      *         ATTR_SAVE - Save spooled file
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY106">
      *         ATTR_SRCDRWR - Source drawer
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY107">
      *         ATTR_SPOOL - Spool the data
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY108">
      *         ATTR_SPOOLFILE - Spooled file name
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY111">
      *         ATTR_SCHEDULE - Spooled output schedule
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY112">
      *         ATTR_STARTPAGE - Starting page
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY117">
      *         ATTR_UNITOFMEAS - Unit of measure
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY118">
      *         ATTR_USERCMT - User comment
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY119">
      *         ATTR_USERDATA - User data
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY1061">
      *         ATTR_SPLSCS - Spool SCS
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEY1191">
      *         ATTR_USRDEFDATA - User defined data
      *       </A>
      *  <li> (3) <A HREF="doc-files/PrintAttributes.html#HDRKEY1192">
      *         ATTR_USRDEFOPT - User defined options
      *       </A>
      *  <li> <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_9">
      *         ATTR_USER_DEFINED_OBJECT - User defined object integrated file system name
      *       </A>
      *
      * </ul>
      * Note 1: Code page and graphical character set are dependent upon each
      *  other.  If you set one you must set the other.
      * <br>
      * Note 2: The special value of *FILE is not allowed when creating a new
      *  spooled file.
      * <br>
      * Note 3: Up to 4 user-defined options may be specified.
      * <br>
      * Note 4: A page definition can be specified with *LINE data.
      *<p>
      * @param printerFile   Optional.  The printer file that should be used
      *                          to create the spooled file.  This printer file
      *                          must reside on the same server that the
      *                          spooled file is being created on.
      * @param outputQueue   Optional.  The output queue on which to create the
      *                          spooled file.  The output queue must reside on
      *                          the same server that the spooled file
      *                          is being created on.
      * @exception AS400Exception If the server system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      **/

    public SpooledFileOutputStream(AS400 system,
                                   PrintParameterList options,
                                   PrinterFile printerFile,
                                   OutputQueue outputQueue)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException
    {
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
        if (impl_ == null)
            chooseImpl();
        // Do connect here because it may throw Exceptions.    
        system_.connectService(AS400.PRINT);
      
        PrinterFileImpl pfi = null;
        if (printerFile != null) {
            if (printerFile.getImpl() == null) {
                printerFile.chooseImpl();
            }
            pfi = (PrinterFileImpl) printerFile.getImpl();
        }
        OutputQueueImpl oqi = null;
        if (outputQueue != null) {
            if (outputQueue.getImpl() == null) {
                outputQueue.chooseImpl();
            }
            oqi = (OutputQueueImpl) outputQueue.getImpl();
        }
        impl_.createSpooledFileOutputStream(system_.getImpl(),
                                            options,
                                            pfi,
                                            oqi);
    }



    // A1A - Added method
    private void chooseImpl()
    {
        if (system_ == null) {
            Trace.log( Trace.ERROR, "Attempt to use SpooledFileOutputStream before setting system.");
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (SpooledFileOutputStreamImpl) system_.loadImpl2("com.ibm.as400.access.SpooledFileOutputStreamImplRemote",
                                                                "com.ibm.as400.access.SpooledFileOutputStreamImplProxy");
    }



    /**
      * Closes the stream.
      * It must be called to release any resources associated with the stream.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void close()
       throws IOException
    {
        impl_.close();
    }



    /** Flushes the stream.  This will write any buffered output bytes.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void flush()
        throws IOException
    {
        impl_.flush();
    }



    /** Returns the spooled file that was created (or is being created) with
      * this output stream.
      * @return A reference to the spooled file object.
      **/
    public SpooledFile getSpooledFile()
       throws IOException
    {
        SpooledFile sf = null;
        NPCPIDSplF spID = impl_.getSpooledFile();
        try {
            spID.setConverter((new Converter(system_.getCcsid(), system_)).impl);
        }
        catch(UnsupportedEncodingException e) {
            if (Trace.isTraceErrorOn())
                Trace.log(Trace.ERROR, "Error initializing converter for print object", e);
        }
        sf = new SpooledFile(system_, spID, null);
        return sf;
    }



    /** Writes a byte of data.
      * @param b The byte to be written.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void write(int b)
        throws IOException
    {
        byte[] buffer1Byte_ = new byte[1];
        buffer1Byte_[0] = (byte)b;
        write(buffer1Byte_, 0, 1);
    }



    /** Writes <i>data.length</i> bytes of data from the byte array
      *  <i>data</i> to the spooled file.
      *
      * @param data The data to be written.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void write(byte[] data)
        throws IOException
    {
        write(data, 0, data.length);
    }



    /**
      * Writes up to <i>length</i> bytes of data from the byte array <i>data</i>,
      * starting at <i>offset</i>, to this spooled file.
      *
      * @param data   The data to be written.
      * @param offset The start offset in the data.
      * @param length The number of bytes that are written.
      *
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void write(byte data[], int offset, int length)
        throws IOException
    {
        impl_.write(data, offset, length);
    }
}
