///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400FileImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Vector;
import java.math.BigDecimal; //@D0A 7/15/99

class AS400FileImplRemote extends AS400FileImplBase implements Serializable //@C0C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  //////////////////////////////////////////////////////////////////////////
  // VARIABLES
  //////////////////////////////////////////////////////////////////////////
  // Flag indicating to ignore replys (used by close and execute).
  //@D1D (moved to AS400FileImplBase) private boolean discardReplys_ = false;
  // The declared file name. This is an alias for the file which allows
  // DDM to process some file requests more quickly.  The declared file name is
  // determined upon construction and set when the file is opened.
  byte[] dclName_ = new byte[8];
  // Static variable used to ensure that each AS400File object has a unique
  // declared file name.
  static long nextDCLName_ = 1;
  // S38BUF data for force end of data calls.  Contain S38BUF LL, CP and value.
  static private byte[] s38Buffer = {0x00, 0x05, (byte)0xD4, 0x05, 0x00};
  // Server
  AS400Server server_ = null;

  // @B1A
  private static int lastCorrelationId_ = 0; //@B6C
  private static Object correlationIdLock_ = new Object(); //@B6A
  
  // Identify the DDM reply data streams to the AS400Server class.
  static
  {
    AS400Server.addReplyStream(new DDMObjectDataStream(), AS400.RECORDACCESS); //@B5C
    AS400Server.addReplyStream(new DDMReplyDataStream(), AS400.RECORDACCESS); //@B5C
  }

  // Instantiate a default AS400File remote implementation object.
  public AS400FileImplRemote()
  {
    setDCLName();
  }


  /**
   *Closes the file on the AS400.  All file locks held by this connection
   *are released.  All
   *uncommitted transactions against the file are rolled back if commitment
   *control has been started.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
  **/
  public void close()
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    super.close(); //@C0A

    // Send the close file data stream request
    if (discardReplys_)
    {
      server_.sendAndDiscardReply(DDMRequestDataStream.getRequestS38CLOSE(dclName_));
    }
    else
    {
      Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38CLOSE(dclName_), /*server_.*/newCorrelationId());  // @B1M
      // Reply expected: S38MSGRM, severity code 0
      if (!(replys.size() == 1 && verifyS38MSGRM((DDMReplyDataStream)replys.elementAt(0), null, 0)))
      {
        handleErrorReply(replys, 0);
      }
    }
  }

  /**
   *Commits all transactions since the last commit boundary.  Invoking this
   *method will cause all transactions under commitment control for this
   *connection to be committed.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped
   *unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public void commit()
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    // Connect to the AS400.  Note: If we have already connected, that connection
    // will be used.
    connect();

    // Send the commit data stream request
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestCMMUOW(), newCorrelationId()); //@B6C
    // Reply expected: ENDUOWRM, with UOWDSP parameter = 1
    if (replys.size() == 1 && ((DDMDataStream)replys.elementAt(0)).getCodePoint() == DDMTerm.ENDUOWRM)
    {
      // Check the UOWDSP term value
      DDMEndUnitOfWorkReply uowReply = new DDMEndUnitOfWorkReply(((DDMDataStream)replys.elementAt(0)).data_);
      if (uowReply.getStatus() != 0x01)
      {
        // Status of logical unit of work committed not returned; should not happen unless
        // we are constructing the request wrong.
        if (Trace.isTraceOn() && Trace.isTraceErrorOn())
        {
          Trace.log(Trace.ERROR, "Wrong status returned from commit ds", uowReply.data_);
        }
        throw new InternalErrorException(InternalErrorException.UNKNOWN, uowReply.getStatus());
      }
    }
    else
    {
      handleErrorReply(replys, 0);
    }
  }

  /**
   *Connects to the AS400.  The name and system must have been
   *set at this point via the constructor or the setters.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped
   *unexpectedly.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   *@exception InterruptedException If this thread is interrupted.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public void connect()
  throws AS400SecurityException,
      ConnectionDroppedException,
      IOException,
      InterruptedException,
      ServerStartupException,
      UnknownHostException
  {
    server_ = system_.getConnection(AS400.RECORDACCESS, false); //@C0C @B5C

//@C1 - In mod3, we don't support pre-V4R2 connections.
//@C1D - begin deleted block
/* 

    // Send the exchange attributes request and receive the reply if pre-V4R2 and haven't already
    // connected.  If we are connecting to a V4R2 or later system, the exchange of attributes
    // takes place within the AS400 object.
    if (server_.getExchangeAttrReply() == null)
    {
      // First time exchange of attributes.  If we get here, we are connecting to a pre-v4r2 system.
      try
      { server_.sendExchangeAttrRequest(DDMRequestDataStream.getRequestEXCSAT("PREV4R2"));
      }
      catch(IOException e)
      {
        // Unable to exchange attributes.  Disconnect server and rethrow
        if (Trace.isTraceOn() && Trace.isTraceErrorOn())
        {
          Trace.log(Trace.ERROR, "IOException on EXCSAT for pre-v4r2 system.");
        }
        system_.disconnectServer(server_); //@C0C
        server_ = null;
        resetState();
        throw e;
      }
      catch(InterruptedException e)
      {
        // Unable to exchange attributes.  Disconnect server and rethrow
        if (Trace.isTraceOn() && Trace.isTraceErrorOn())
        {
          Trace.log(Trace.ERROR, "InterruptedException on EXCSAT for pre-v4r2 system.");
        }
        system_.disconnectServer(server_); //@C0C
        server_ = null;
        resetState();
        throw e;
      }

      DDMDataStream reply = (DDMDataStream)server_.getExchangeAttrReply();
      if (reply.getCodePoint() != DDMTerm.EXCSATRD)
      {
        if (Trace.isTraceOn() && Trace.isTraceErrorOn())
        {
          Trace.log(Trace.ERROR, "EXCSAT for pre-v4r2 system failed:", reply.data_);
        }
        // Exchange failed; disconnect service
        system_.disconnectServer(server_); //@C0C
        server_ = null;
        resetState();
        throw new InternalErrorException(InternalErrorException.UNKNOWN, reply.getCodePoint());
      }
    }
*/
//@C1D - end of deleted block

  }



  //@D0M 7/15/99 - Moved this code out of ImplBase to here
  /**
   *Creates the DDS source file to be used to create a physical file based on a user
   *supplied RecordFormat.<br>
   *The name of the file and the AS400 system to which to connect must be set prior
   *to invoking this method.
   *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400File#setPath
   *@see AS400File#setSystem
   *@param recordFormat The record format to describe in the DDS source file.
   *@param altSeq The value to be specified for the file-level keyword ALTSEQ.  If no
   *value is to be specified, null may be specified.
   *@param ccsid The value to be specified for the file-level keyword CCSID.  If no
   *value is to be specified, null may be specified.
   *@param order The value to be specified to indicate in which order records are to be
   *retrieved from the file.  Valid values are one of the following file-level keywords:
   *<ul>
   *<li>FIFO - First in, first out
   *<li>LIFO - Last in, first out
   *<li>FCFO - First changed, first out
   *</ul>
   *If no ordering value is to be specified, null may be specified.
   *@param ref The value to be specified for the file-level keyword REF.  If no
   *value is to be specified, null may be specified.
   *@param unique Indicates if the file-level keyword UNIQUE is to be specified. True
   *indicates that the UNIQUE keyword should be specified; false indicates that it
   *should not be specified.
   *@param format The value to be specified for the record-level keyword FORMAT.  If no
   *value is to be specified, null may be specified.
   *@param text The value to be specified for the record-level keyword TEXT.  If no
   *value is to be specified, null may be specified.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public synchronized void createDDSSourceFile(RecordFormat recordFormat,
                                                String altSeq,
                                                String ccsid,
                                                String order,
                                                String ref,
                                                boolean unique,
                                                String format,
                                                String text)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    // Create the source physical file to hold the DDS source.  Note that we create the
    // file in library QTEMP.  Each AS400 job has its own QTEMP library which is created
    // when the job starts and is deleted when the job ends.  Using QTEMP allows
    // the file to be created regardless of the user's authority and also eliminates
    // name collision problems when different jobs are creating files from a record
    // format.
    AS400Message[] msgs = execute("QSYS/CRTSRCPF FILE(QTEMP/JT400DSSRC) MBR(*FILE) TEXT('JT400 TEMPORARY DDS SOURCE FILE')"); //@B0C
    if (msgs.length > 0)
    {
      if (msgs[0].getID().equals("CPF5813"))
      {
        // File already exists from a previous create; clear it.
        msgs = execute("QSYS/CLRPFM QTEMP/JT400DSSRC"); //@B0C
        if (msgs.length > 0)
        {
          if (!msgs[0].getID().equals("CPC3101"))
          {
            // Clear failed.  Throw exception.
            Trace.log(Trace.ERROR, "QSYS/CLRPFM QTEMP/JT400DSSRC");
            throw new AS400Exception(msgs);
          }
        }
        else
        {
          throw new InternalErrorException("QSYS/CLRPFM QTEMP/JT400DSSRC",
                                           InternalErrorException.UNKNOWN);
        }
      }
      else if (!msgs[0].getID().equals("CPC7301"))
      {
        // File does not exist and we were unable to create; throw exception
        // (CPC7301 = Successful create)
        Trace.log(Trace.ERROR, "QSYS/CRTSRCPF FILE(QTEMP/JT400DSSRC) MBR(*FILE) TEXT('JT400 TEMPORARY DDS SOURCE FILE')");
        throw new AS400Exception(msgs);
      }
    }
    else
    {
      // No messages.  This shouldn't happen.
      throw new InternalErrorException("No AS/400 messages.",
                                       InternalErrorException.UNKNOWN);
    }

      /////////////////////////////////////////////////////////////////////////////////
      // Create the records to be written to the file.  These records will contain the
      // DDS based on the supplied RecordFormat object.
      /////////////////////////////////////////////////////////////////////////////////
      // Create a RecordFormat object which describes the record format of a source
      // physical file.
      RecordFormat srcRF = new RecordFormat("JT400DSSRC");
      srcRF.addFieldDescription(new ZonedDecimalFieldDescription(new AS400ZonedDecimal(6, 2), "SRCSEQ"));
      srcRF.addFieldDescription(new ZonedDecimalFieldDescription(new AS400ZonedDecimal(6, 0), "SRCDAT"));
      // - Can't use the system object here because we have no way of filling in the converter
      srcRF.addFieldDescription(new CharacterFieldDescription(new AS400Text(80, system_.getCcsid()), "SRCDTA")); //@D0C
      Vector lines = new Vector();  // Contains DDS lines to write to source file
      String line; // A single line of DDS source
      // Create line(s) for any file level keywords - file level keywords must precede
      // the line specifying the record format name.
      if (altSeq != null)
      {
        line = STR44 + "ALTSEQ(" + altSeq + ")";
        lines.addElement(line);
      }
      if (ccsid != null)
      {
        line = STR44 + "CCSID(" + ccsid + ")";
        lines.addElement(line);
      }
      if (order != null)
      {
        line = STR44 + order;
        lines.addElement(line);
      }
      if (ref != null)
      {
        line = STR44 + "REF(" + ref + ")";
        lines.addElement(line);
      }
      if (unique)
      {
        line = STR44 + "UNIQUE";
        lines.addElement(line);
      }

      // Create line for the record format name
      line = STR16 + "R ";
      // The record format name cannot exceed 10 characters and must be in upper case
      if (recordFormat.getName().length() > 10)
      {
        if (Trace.isTraceOn() && Trace.isTraceWarningOn())
        {
          Trace.log(Trace.WARNING, "Record format name '"+recordFormat.getName()+"' too long. Using '"+recordFormat.getName().substring(0,10)+"' instead.");
        }
        line += recordFormat.getName().substring(0, 10);
      }
      else
      {
        line += recordFormat.getName();
      }
      lines.addElement(line);

      // Create line(s) for any record level keywords.  The record level keywords
      // must be on the same line or on the lines immediately following the record
      // format line.
      if (format != null)
      {
        line = STR44 + "FORMAT(" + format + ")";
        lines.addElement(line);
      }
      if (text != null)
      {
        if (text.length() > 32)
        { // Text exceeds length left on line - need to continue on next line
          line = STR44 + "TEXT('" + text.substring(0, 33) + "-";
          lines.addElement(line);
          // Add the remainder of the TEXT keyword
          line = STR44 + text.substring(34) + "')";
          lines.addElement(line);
        }
        else
        { // Text fits on one line
          line = STR44 + "TEXT('" + text + "')";
          lines.addElement(line);
        }
      }

      // Create lines for each field description and any keywords for the field
      int numberOfFields = recordFormat.getNumberOfFields();
      FieldDescription f = null;
      String ddsDesc;
      String[] dds = null;
      int length;
      int beginningOffset;
      for (int i = 0; i < numberOfFields; ++i)
      {
        f = recordFormat.getFieldDescription(i);
        // Specify the DDS description of the field.  The DDS description returned
        // from FieldDescription contains the field level keywords as well as the
        // description of the field.  It is formatted properly for DDS except for
        // the preceding blanks.  Therefore, we add 18 blanks so that the field
        // description starts in column 19 of the line.
        dds = f.getDDSDescription();
        // Add the fixed portion of the DDS description for the field to the vector
        ddsDesc = STR18 + dds[0];
        lines.addElement(ddsDesc);
        // Add lines containing field level keywords
        for (int j = 1; j < dds.length; ++j)
        {
          ddsDesc = STR44 + dds[j];
          length = ddsDesc.length();
          beginningOffset = 0;
          if (length > 80)
          { // Need to continue the line on the next line
            line = ddsDesc.substring(beginningOffset, 79) + "-";
            lines.addElement(line);
            length -= 79;
            beginningOffset = 79;
            line = STR44 + ddsDesc.substring(beginningOffset);
            lines.addElement(line);
          }
          else
          { // It all fits on one line
            lines.addElement(ddsDesc);
          }
        }
      }
      // Create lines for key fields and key field level keywords
      numberOfFields = recordFormat.getNumberOfKeyFields();
      for (int i = 0; i < numberOfFields; ++i)
      {
        f = recordFormat.getKeyFieldDescription(i);
        // Specify the name of the field
        line = STR16 + "K ";
        line += f.getDDSName();
        lines.addElement(line);
        // Specify any key field level keywords
        String[] keyFuncs = f.getKeyFieldFunctions();
        if (keyFuncs != null)
        {
          for (short j = 0; j < keyFuncs.length; ++j)
          {
            line = STR44 + keyFuncs[j];
            lines.addElement(line);
          }
        }
      }

      // Create an array of records representing each line to be written
      // to the file.
      Record[] records = new Record[lines.size()];
      for (int i = 0; i < records.length; ++i)
      {
        records[i] = srcRF.getNewRecord();
        records[i].setField("SRCSEQ", new BigDecimal(i));

        records[i].setField("SRCDAT", new BigDecimal(i));
        records[i].setField("SRCDTA", lines.elementAt(i));
      }

      // Open the DDS source file and write the records.  We will write all the records
      // at one time, so we specify a blocking factor of records.length on the open().
    AS400FileImplRemote src = null; //@B5C @D0C 7/15/99
    try
    {
      //@B5D src = new SequentialFile(system_, "/QSYS.LIB/QTEMP.LIB/JT400DSSRC.FILE");
      //@B5 - Can't create a new AS400FileImplBase because it's abstract.
      //@B5 - Other alternative is to create a new ImplRemote or 
      //      ImplNative based on the type of this. But can't create
      //      a new AS400FileImplNative since it doesn't exist at
      //      compile time.
//@D0D 7/15/99      src = (AS400FileImplBase)this.clone(); //@B5A
      //@B5D try
      //@B5D {
      //@B5D  src.setRecordFormat(srcRF);
      //@B5D }
      //@B5D catch(PropertyVetoException e)
      //@B5D { // This is to quiet the compiler
      //@B5D }
      src = new AS400FileImplRemote(); //@D0A 7/15/99
      src.setAll(system_, "/QSYS.LIB/QTEMP.LIB/JT400DSSRC.FILE", srcRF, false, false); //@B5A
      
      //@B5D src.open(AS400File.WRITE_ONLY, records.length, AS400File.COMMIT_LOCK_LEVEL_NONE);
      src.openFile2(AS400File.WRITE_ONLY, records.length, AS400File.COMMIT_LOCK_LEVEL_NONE, false); //@B5A
      src.write(records);
      // Close the file
      src.close();
    }
    catch(Exception e)
    { // log error and rethrow exception
      Trace.log(Trace.ERROR, "Unable to write records to DDS source file:"+e);
      try
      {
        if (src != null)
        {
          src.close();
        }
      }
      catch(Exception e1)
      { // Ignore it; we will throw the original exception
      }
      if (e instanceof AS400Exception)
      {
        throw (AS400Exception) e;
      }
      else
      {
        throw new InternalErrorException(e.getMessage(),
                                         InternalErrorException.UNKNOWN);
      }
    }
  }


  /**
   *Deletes the record at the current cursor position.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void deleteCurrentRecord()
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    connect();

    // Send the delete record data stream request
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38DEL(dclName_), newCorrelationId()); //@B6C
    // Reply expected: S38IOFB
    if (!(replys.size() == 1 && ((DDMDataStream)replys.elementAt(0)).getCodePoint() == DDMTerm.S38IOFB))
    {
      handleErrorReply(replys, 1);
    }
  }


  /**
   Executes a command on the AS/400.
   @param cmd the command
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/ 
  public AS400Message[] execute(String cmd)
  throws AS400SecurityException, InterruptedException, IOException
  {
    connect();

    // Send the submit command data stream request
    Vector replys = null;
    if (discardReplys_)
    {
      server_.sendAndDiscardReply(DDMRequestDataStream.getRequestS38CMD(cmd, system_)); //@C0C
    }
    else
    {
      replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38CMD(cmd, system_), newCorrelationId()); //@C0C @B6C
    }

    return processReplys(replys);
  }


  /**
   *Throws an appropriate exception based on the type of reply
   *data stream specified.
   *@param replys The replys to be checked to determine the error.
   *@param index The index within replys at which to start.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void handleErrorReply(Vector replys, int index)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    int size = replys.size();
    DDMDataStream reply;
    int codePoint;
    Vector as400MsgList = new Vector();

    for (int i = index; i < size; ++i)
    {
      reply = (DDMDataStream)replys.elementAt(i);
      codePoint = reply.getCodePoint();
      switch (codePoint)
      {
        case DDMTerm.S38MSGRM:
          // Because we will normally get more than one AS400Message for an
          // error condition, we build a vector of AS400Message arrays and
          // throw an AS400Exception with all messages once we have finished
          // parsing the replies.  Note that the DDMAS400MessageReply class
          // extracts all the AS400 messages contained in reply.data_.  I.e.
          // a single reply may contain more than one AS400 message.
          DDMAS400MessageReply msgReply = new DDMAS400MessageReply(system_, reply.data_); //@C0C
          as400MsgList.addElement(msgReply.getAS400MessageList());
          break;
          // If any of the following cases occur, we throw the exception and are done.
        case DDMTerm.AGNPRMRM:
        case DDMTerm.CMDCHKRM:
        case DDMTerm.CMDNSPRM:
        case DDMTerm.DCLNAMRM:
        case DDMTerm.PRCCNVRM:
        case DDMTerm.PRMNSPRM:
        case DDMTerm.RSCLMTRM:
        case DDMTerm.SYNTAXRM:
        case DDMTerm.VALNSPRM:
          if (Trace.isTraceOn() && Trace.isTraceErrorOn())
          {
            Trace.log(Trace.ERROR, "handleErrorReply()", reply.data_);
          }
          throw new InternalErrorException(codePoint);
        default:
          // We don't know what the reply is.  Throw exception and be done.
          if (Trace.isTraceOn() && Trace.isTraceErrorOn())
          {
            Trace.log(Trace.ERROR, "handleErrorReply()", reply.data_);
          }
          throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, codePoint);
      }
    }
    // If we get to here, we should have a list of AS400 messages to throw
    if (as400MsgList.size() > 0)
    {
      // We need to expand out the Vector of AS400Message[]'s to individual
      // AS400Message's in a single array in order to construct our AS400Exception.
      int numberOfMessages = 0;
      int msgListSize = as400MsgList.size();
      int msgNumber = 0;
      // Determine number of AS400Message objects we have
      for (int i = 0; i < msgListSize; ++i)
      {
        numberOfMessages += ((AS400Message[])as400MsgList.elementAt(i)).length;
      }
      // Now populate the single AS400Message[] with the AS400Message objects
      AS400Message[] msgs = new AS400Message[numberOfMessages];
      AS400Message[] m;
      for (int i = 0; i < msgListSize; ++i)
      {
        m = (AS400Message[])as400MsgList.elementAt(i);
        for (int j = 0; j < m.length; ++j)
        {
          msgs[msgNumber++] = m[j];
        }
      }
      throw new AS400Exception(msgs);
    }
    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
  }

  /**
   *Logs warning messages if tracing is on.  This method should be used to log
   *warning messages (trace.WARNING) when an operation is successful yet additional
   *AS400 messages are sent with the reply.
   *@param v The vector of replies containing AS400 messages to log.
   *@param index The index into <i>v</i> at which to stop pulling out AS400 messages.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void logWarningMessages(Vector v, int index)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    if (Trace.isTraceOn() && Trace.isTraceWarningOn())
    {
      DDMAS400MessageReply msg;
      for (int i = 0; i < index; ++i)
      {
        msg = new DDMAS400MessageReply(system_, ((DDMDataStream)v.elementAt(i)).data_); //@C0C
        AS400Message[] msgs = msg.getAS400MessageList();
        Trace.log(Trace.WARNING, "AS400FileImplRemote.logWarningMessages():");
        for (int j = 0; i < msgs.length; ++j)
        {
          Trace.log(Trace.WARNING, msgs[j].toString());
        }
      }
    }
  }

  /**
   *Looks for a particular code point in a vector of data streams.
   *@param cp The code point for which to look.
   *@param v The data streams in which to look.
   *@return The index of the datastream in <i>v</i> in which the code point was found.
   *If the code point is not found, -1 is returned.
  **/
  private int lookForCodePoint(int cp, Vector v)
  {
    int index = -1;
    int size = v.size();
    DDMDataStream ds;
    int offset;
    // Search each datastream in v for the code point.
    for (int i = 0; i < size && index == -1; ++i)
    {
      ds = (DDMDataStream)v.elementAt(i);
      offset = 8;
      // Search through all the code points in an element of v for cp.
      while (index == -1 && offset < ds.data_.length)
      {
        if (ds.get16bit(offset) == cp)
        { // Found the code point
          index = i;
        }
        else
        { // Move on to the next code point
          offset += ds.get16bit(offset - 2);
        }
      }
    }
    return index;
  }

  
  /**
   *Opens the file.  Helper function to open file for keyed or
   *sequential files.
   *@param openType
   *@param bf blocking factor
   *@param access The type of file access for which to open the file.
   *@return the open feedback data
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started..
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public DDMS38OpenFeedback openFile(int openType, int bf, String access)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    // Ensure that we are connected to the AS/400.
    connect();

    // Create the user file control block.
    byte[] ufcb = createUFCB(openType, bf, access, false);

    // Send the open file data stream request
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38OPEN(ufcb, dclName_), newCorrelationId()); //@B6C
    // Reply expected: S38OPNFB
    int index = lookForCodePoint(DDMTerm.S38OPNFB, replys);
    DDMDataStream reply;
    if (index != -1)
    {
      reply = (DDMDataStream)replys.elementAt(index);
      openFeedback_ = new DDMS38OpenFeedback(system_, reply.data_); //@C0C
      if (Trace.isTraceOn() && Trace.isTraceInformationOn())
      {
        Trace.log(Trace.INFORMATION, "AS400FileImplRemote.openFile()\n" + openFeedback_.toString());
      }
      if (index != 0)
      { // AS400 informational and/or diagnostic messages were issued.  Log them.
        logWarningMessages(replys, index);
      }
    }
    else
    {
      handleErrorReply(replys, 0);
    }

    return openFeedback_;
  }

  /**
   *Pads <i>data</i> with <i>padByte</i> <i>numBytes</i> starting at <i>start</i>.
   *@param data The data to pad.
   *@param start The offset in <i>data</i> at which we begin padding.
   *@param numBytes The number of bytes to pad.
   *@param padChar The character with which to pad.
  **/
  public void padBytes(byte[] data, int start, int numBytes, byte padChar)
  {
    for (int i = 0; i < numBytes; ++i)
    {
      data[start + i] = padChar;
    }
  }

  /**
   *Positions the cursor for the file.  Which record to position the cursor to is
   *determined by the <i>type</i>
   *argument.
   *@param type The type of get to execute.  Valid values are:
   *<ul>
   *<li>TYPE_GET_FIRST
   *<li>TYPE_GET_NEXT
   *<li>TYPE_GET_LAST
   *<li>TYPE_GET_PREV
   *<li>TYPE_GET_SAME
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record[] positionCursorAt(int type)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    int shr;  // Type of locking for the record

    // @A1C
    if ((openType_ == AS400File.READ_ONLY) || //@C0C
        ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) // @A1A //@C0C
    {
      // Read only
      shr = SHR_READ_NORM;
    }
    else
    { // READ_WRITE, lock the record for update
      shr = SHR_UPD_NORM;
    }
    // Send the request to read.  Ignore the data; don't specify
    // DATA_NODTA_DTARCD because it is one of the most inefficient
    // paths per the DDM server guys.  More efficient to get the data and ignore
    // it they say.
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GET(dclName_, type, shr, DATA_DTA_DTARCD), newCorrelationId()); //@B6C
    int codePoint = ((DDMDataStream)replys.elementAt(0)).getCodePoint();
    if (codePoint == DDMTerm.S38IOFB && replys.size() > 1)
    {
      handleErrorReply(replys, 1);
    }
    else if (codePoint != DDMTerm.S38BUF)
    {
      handleErrorReply(replys, 0);
    }
    Record[] returned = processReadReply(replys, false);    // @A1C

    return returned;
  }

  /**
   *Positions the file cursor to after the last record.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursorAfterLast()
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    // If we are caching records and the cache contains the last record,
    // position the cache.  Otherwise, position the file and refresh the
    // cache if we are caching records.
    if (cacheRecords_ && cache_.containsLastRecord())
    {
      cache_.setPositionAfterLast();
      return;
    }
    
    if (cacheRecords_)
    {
      // Invalidate the cache
      cache_.setIsEmpty();
    }
    
    // Send the request to force end of data specifying end of the file for
    // positioning.
    // Need correlation id as we will be chaining an S38BUF object to this request
    // and the ids must match.
    int id = newCorrelationId(); //@B6C
    DDMRequestDataStream req = DDMRequestDataStream.getRequestS38FEOD(dclName_, TYPE_GET_LAST, SHR_READ_NORM_RLS, DATA_NODTA_DTARCD);
    req.setIsChained(true); // We will be chaining an S38BUF to this request
    req.setHasSameRequestCorrelation(true); // When chaining, must indicate
    // that the correlation ids are
    // the same.
    Vector replys = null;
    try
    {
      server_.send(req, id);  // Send the S38FEOD request
      // Although the S38FEOD term description states that
      // the S38BUF object is optional, it is not for our purposes.
      // Create an empty S38BUF object to send after the S38FEOD
      DDMObjectDataStream s38buf = new DDMObjectDataStream(11);
      // The class contains static variable s38Buffer which is the S38BUF term with
      // a value of 1 byte set to 0x00.
      System.arraycopy(s38Buffer, 0, s38buf.data_, 6, 5);
      // Send the S38BUF
      replys = sendRequestAndReceiveReplies(s38buf, id);
    }
    catch (ConnectionDroppedException e)
    {
      // UConnection dropped.  Disconnect server and rethrow
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.ERROR, "ConnectionDroppedException.");
      }
      system_.disconnectServer(server_); //@C0C
//@C1 - Setting the server_ object to null means that
//      any operations on this AS400File object after the connection has been
//      dropped will result in a NullPointerException. By leaving the server_ object
//      around, any subsequent operations should also throw a ConnectionDroppedException.
//@C1D      server_ = null;
      resetState();
      throw e;
    }

    // Reply expected: S38IOFB, which may be followed by an S38MSGRM
    // with id CPF5001 which indicates that the end of file was reached.
    // Any other S38MSGRM replies indicate that an error has occurred.
    if (((DDMDataStream)replys.elementAt(0)).getCodePoint() == DDMTerm.S38IOFB)
    {
      if (replys.size() != 1)
      {
        if (!verifyS38MSGRM((DDMReplyDataStream)replys.elementAt(1), "CPF5001", 0))
        {
          handleErrorReply(replys, 1);
        }
      }
    }
    else
    {
      handleErrorReply(replys, 0);
    }
  }

  /**
   *Positions the file cursor to before the first record.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursorBeforeFirst()
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    // If we are caching records and the cache contains the first record,
    // position the cache.  Otherwise, position the file and refresh the
    // cache if we are caching records.
    if (cacheRecords_ && cache_.containsFirstRecord())
    {
      cache_.setPositionBeforeFirst();
      return;
    }
    
    if (cacheRecords_)
    { // Invalidate the cache
      cache_.setIsEmpty();
    }
    
    
    // Send the request to force end of data specifying beginning of the
    // file for positioning; this is how we position the cursor to
    // before the first record.
    // Need correlation id as we will be chaining an S38BUF object to this request
    // and the ids must match.
    int id = newCorrelationId(); //@B6C
    DDMRequestDataStream req = DDMRequestDataStream.getRequestS38FEOD(dclName_, TYPE_GET_FIRST, SHR_READ_NORM_RLS, DATA_NODTA_DTARCD);
    req.setIsChained(true); // We will be chaining an S38BUF to this request
    req.setHasSameRequestCorrelation(true); // When chaining, must indicate
    // that the correlation ids are
    // the same.
    Vector replys = null;
    try
    {
      server_.send(req, id);  // Send the S38FEOD request
      // Although the S38FEOD term description states that
      // the S38BUF object is optional, it is not for our purposes.
      // Create an empty S38BUF object to send after the S38FEOD
      DDMObjectDataStream s38buf = new DDMObjectDataStream(11);
      // The class contains static variable s38Buffer which is the S38BUF term with
      // a value of 1 byte set to 0x00.
      System.arraycopy(s38Buffer, 0, s38buf.data_, 6, 5);
      // Send the S38BUF
      replys = sendRequestAndReceiveReplies(s38buf, id);
    }
    catch (ConnectionDroppedException e)
    {
      // UConnection dropped.  Disconnect server and rethrow
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.ERROR, "ConnectionDroppedException.");
      }
      system_.disconnectServer(server_); //@C0C
//@C1 - Setting the server_ object to null means that
//      any operations on this AS400File object after the connection has been
//      dropped will result in a NullPointerException. By leaving the server_ object
//      around, any subsequent operations should also throw a ConnectionDroppedException.
//@C1D      server_ = null;
      resetState();
      throw e;
    }
    // Reply expected: S38IOFB, which may be followed by an S38MSGRM
    // with id CPF5001 which indicates that the end of file was reached.
    // Any other S38MSGRM replies indicate that an error has occurred.
    if (((DDMDataStream)replys.elementAt(0)).getCodePoint() == DDMTerm.S38IOFB)
    {
      if (replys.size() != 1)
      {
        if (!verifyS38MSGRM((DDMReplyDataStream)replys.elementAt(1), "CPF5001", 0))
        {
          handleErrorReply(replys, 1);
        }
      }
    }
    else
    {
      handleErrorReply(replys, 0);
    }
  }


  /**
   *Positions the file cursor to the specified record number in the file on the AS/400.
   *@param recordNumber The record number to which to position the file cursor.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record positionCursorToIndex(int recordNumber)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    int shr;  // Type of locking for the record

    // @A1C
    if ((openType_ == AS400File.READ_ONLY) ||   //@C0C
        ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) // @A1A //@C0C
    {
      // Read only
      shr = SHR_READ_NORM;
    }
    else
    { // READ_WRITE; get for update
      shr = SHR_UPD_NORM;
    }
    // Send the request to read. Specify 0x08 for type
    // to indicate that the record number is determined from the start of the file.
    // Note that even though we are only positioning the cursor, we must specify
    // DATA_DTA_DTARCD which causes the record to be retrieved.  This is because
    // the cursor will not be positioned properly if we do not actually get the
    // record.  This situation occurs when accessing by record number or by key but
    // not when accessing sequentially.
//    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETD(dclName_, recordFormat_, 0x08, shr, DATA_DTA_DTARCD, recordNumber, system_), server_.newCorrelationId());          // @A1D //@C0C
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETD(dclName_, recordFormatCTLLName_, 0x08, shr, DATA_DTA_DTARCD, recordNumber, system_), newCorrelationId());    // @A1A //@C0C @B6C

    // Reply expected: S38BUF
    int codePoint = ((DDMDataStream)replys.elementAt(0)).getCodePoint();
    if (codePoint == DDMTerm.S38IOFB && replys.size() > 1)
    {
      handleErrorReply(replys, 1);
    }
    else if (codePoint != DDMTerm.S38BUF)
    {
      handleErrorReply(replys, 0);
    }
    Record[] returned = processReadReply(replys, false);    // @A1C
    return returned[0];  // @A1A
//    return null;       // @A1D
  }

  /**
   *Positions the cursor to the first record with the specified key based on the specified
   *type of read.
   *@param key The values that make up the key with which to find the record.
   *@param type The type of read.  This value is one of the TYPE_GETKEY_* constants.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record positionCursorToKey(Object[] key, int type)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    int shr;  // Type of locking for the record

    // @A1C
    if ((openType_ == AS400File.READ_ONLY) ||  //@C0C
        ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) // @A1A //@C0C
    {
      // Read only
      shr = SHR_READ_NORM;
    }
    else
    { // READ_WRITE
      shr = SHR_UPD_NORM;
    }
    // In order to have the file cursor remain properly positioned, we specify that the record
    // is to be returned on the GETK request as opposed to specifying DATA_NODTA_DTARCD.  This
    // is necessary for the caching support.
//    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETK(dclName_, recordFormat_, type, shr, DATA_DTA_DTARCD, key, system_), server_.newCorrelationId());   // @A1D
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETK(dclName_, recordFormat_, recordFormatCTLLName_, type, shr, DATA_DTA_DTARCD, key, system_), newCorrelationId());   // @A1A @B6C

    int codePoint = ((DDMDataStream)replys.elementAt(0)).getCodePoint();
    if (codePoint == DDMTerm.S38IOFB && replys.size() > 1)
    {
      handleErrorReply(replys, 1);
    }
    else if (codePoint != DDMTerm.S38BUF)
    {
      handleErrorReply(replys, 0);
    }
    Record[] returned = processReadReply(replys, true);     // @A1C
    return null;            // @A1A
//    return returned[0];   // @A1D
  }


  // @A1A
  /**
   *Positions the cursor to the first record with the specified key based on the specified
   *type of read.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *@param type The type of read.  This value is one of the TYPE_GETKEY_* constants.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record positionCursorToKey(byte[] key, int type, int numberOfKeyFields)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    int shr;  // Type of locking for the record
    if ((openType_ == AS400File.READ_ONLY) || 
        ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) // @A1A
    {
      // Read only
      shr = SHR_READ_NORM;
    }
    else
    { // READ_WRITE
      shr = SHR_UPD_NORM;
    }
    // In order to have the file cursor remain properly positioned, we specify that the record
    // is to be returned on the GETK request as opposed to specifying DATA_NODTA_DTARCD.  This
    // is necessary for the caching support.
//    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETK(dclName_, recordFormat_, type, shr, DATA_DTA_DTARCD, key, system_, numberOfKeyFields), server_.newCorrelationId());  // @A1D
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETK(dclName_, recordFormatCTLLName_, type, shr, DATA_DTA_DTARCD, key, system_, numberOfKeyFields), newCorrelationId());  // @A1A @B6C
    int codePoint = ((DDMDataStream)replys.elementAt(0)).getCodePoint();
    if (codePoint == DDMTerm.S38IOFB && replys.size() > 1)
    {
      handleErrorReply(replys, 1);
    }
    else if (codePoint != DDMTerm.S38BUF)
    {
      handleErrorReply(replys, 0);
    }
    Record[] returned = processReadReply(replys, true);     // @A1C
    return null;  // @A1A
//    return returned[0];  // @A1D
  }




  /**
   *Processes the <i>replys</i> vector for records read.  Throws exceptions
   *in error cases.
   *@param replys The reply datastream(s) containing the record(s) read.
   *@return The records read from the system.  Returns null if no records were
   *read.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
//  Record[] processReadReply(Vector replys)  // @A1D
  public Record[] processReadReply(Vector replys, boolean discardRecords)  // @A1A
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    Record[] returned = null;  // Will contain the records read.
    int recordIncrement = openFeedback_.getRecordIncrement();

    // The format of the reply(s) should be one or more S38BUF objects
    // followed by an S38IOFB.  However if the end of file was reached or
    // if the record to be read was not found in the file, an S38IOFB followed
    // be an S38MSGRM indicating CPF5006 or CPF5001 will be returned.  If this is
    // the case, we return null instead of throwing an exception.
    // If an error occurs we may get an S38IOFB followed by S38MSGRM objects indicating
    // the AS400 errors that occurred.  In that case we throw an exception via
    // handleErrorReply.  If we only get an S38IOFB back, we also throw an
    // exception as an error must have occurred.  This case should not happen.
    int codePoint = ((DDMDataStream)replys.elementAt(0)).getCodePoint();
    if (codePoint == DDMTerm.S38IOFB)
    { // The end of file was reached, the record to be read was not found or an
      // error occurred.
      if (replys.size() > 1)
      {
        codePoint = ((DDMDataStream)replys.elementAt(1)).getCodePoint();
        if (codePoint == DDMTerm.S38MSGRM)
        { // Check for end of file or record not found messages
          DDMAS400MessageReply err = new DDMAS400MessageReply(system_, ((DDMDataStream)replys.elementAt(1)).data_);
          String msgId = err.getAS400Message().getID();
          if (msgId.equals("CPF5006") || msgId.equals("CPF5001"))
          { // End of file reached or record not found; return null record
            return returned;
          }
          else
          { // Error occurred
            handleErrorReply(replys, 1);
          }
        }
        else
        { // Some other error (other than an AS400 error) occurred
          handleErrorReply(replys, 1);
        }
      }
      else
      { // Only an S38IOFB object was returned.  Error situation.  Should not occur.
        handleErrorReply(replys, 0);
      }
    }
    else if (codePoint == DDMTerm.S38BUF)
    {
      if (discardRecords)
      {  // @A1A
        return returned;   // @A1A
      }                      // @A1A

      // Records were read. Extract format them
      // Extract the returned records and the io feedback info
      // The S38IOFB will be the last object in the reply(s)
      DDMDataStream reply = (DDMDataStream)replys.elementAt(0);
      DDMS38IOFB ioFeedback;
      boolean largeBuffer;
      // If the length of the S38BUF term is greater than 0x7FFF, there
      // will be an extra 4 bytes after the S38BUF code point which indicate the length of
      // the S38BUF data.  We need to special handle these instances, so we use largeBuffer
      // to indicate when such a case has occurred.
      largeBuffer = (reply.get16bit(6) <= 0x7FFF)? false : true;
      if (reply.isChained())
      { // The IO feedback is in the next reply.  The S38IOFB data starts at offset
        // 10 in the next reply.
        ioFeedback = new DDMS38IOFB(((DDMDataStream)replys.elementAt(1)).data_, 10);
      }
      else
      { // The io feedback info is in this reply.
        // if (largeBuffer)
        //   The length of the record data is in the 4 bytes following the S38BUF
        //   code point.  The S38IOFB data will start at that length + 6 for the header,
        //   + 8 for the S38BUF LL-CP-extra LL, + 4 for the S38IOFB LL-CP.
        // else
        //   The length of the record data is contained in the LL preceding
        //   the S38BUF codepoint.  The S38IOFB data will start at that length + 6
        //   for the header, + 4 for the LL-CP of the S38BUF, + 4 for the S38IOFB LL-CP.
        int offset = (largeBuffer)? reply.get32bit(10) + 18 : reply.get16bit(6) + 10;
        ioFeedback = new DDMS38IOFB(reply.data_, offset);
      }

      // Extract the record(s) returned; because we are in the if (S38BUF code point found)
      // code, we know that there was at least one record returned.
      // The S38IOFB contains the number of records read.
      int numberOfRecords = ioFeedback.getNumberOfRecordsReturned();
      returned = new Record[numberOfRecords];

      // if (largeBuffer), the S38BUF CP is followed by 4 bytes of record length info,
      // then the record data; otherwise the record data follows the code point
      int recordOffset = (largeBuffer)? 14 : 10;
      // Determine the offset of the record number within the S38BUF for each record.
      // The S38IOFB contains the record length for the record(s).  The record length
      // consists of the length of the data plus a variable size gap plus the null byte
      // field map.  The record number offset is 2 bytes more than the record length.
      int recordNumberOffset = recordOffset + ioFeedback.getRecordLength() + 2;
      // Determine the null byte field map offset.  When we opened the file, the
      // the S38OPNFB reply contained the offset of the null byte field map in a record.
      int nullFieldMapOffset = recordOffset + openFeedback_.getNullFieldByteMapOffset();
      // Determine the number of fields in a record from the RecordFormat for this file.
      int numFields = recordFormat_.getNumberOfFields();
      // If the file has null capable fields, we will need to check the null byte field
      // map and set the fields within the Record object as appropriate.
      boolean isNullCapable = openFeedback_.isNullCapable();

      for (int i = 0; i < numberOfRecords; ++i)
      { // Extract the records from the datastream reply.
        returned[i] = recordFormat_.getNewRecord(reply.data_, recordOffset + i * recordIncrement);
        // Set any null fields to null
        if (isNullCapable)
        { // File has null capable fields
          for (int j = 0; j < numFields; ++j)
          { // 0xF1 = field is null, 0xF0 = field is not null
            if (reply.data_[nullFieldMapOffset + j + i * recordIncrement] == (byte)0xF1)
            {
              returned[i].setField(j, null);
            }
          }
        }
        // Set the record number.  The record number is two bytes after the end of the
        // record data and is four bytes long.
        try
        {
          returned[i].setRecordNumber(BinaryConverter.byteArrayToInt(reply.data_, recordNumberOffset + i * recordIncrement));
        }
        catch (PropertyVetoException e)
        { // We created the Record objects.  There is no one to veto anything
          
        } // so this is here to quit the compiler
      }
    }
    else
    { // Error occurred
      handleErrorReply(replys, 0);
    }
    return returned;
  }

  /**
   Process replys.
   @param replys the replys from a request.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public AS400Message[] processReplys(Vector replys)
  throws AS400SecurityException, InterruptedException, IOException
  {
    AS400Message[] msgs = null;
    Vector as400MsgList = new Vector();

    for (int i = 0; i < replys.size(); ++i)
    {
      DDMDataStream reply = (DDMDataStream)replys.elementAt(i);
      int codePoint = reply.getCodePoint();
      switch (codePoint)
      {
        case DDMTerm.S38MSGRM:
          // Because we will normally get more than one AS400Message for an
          // error condition, we build a vector of AS400Message arrays and
          // throw an AS400Exception with all messages once we have finished
          // parsing the replies.  Note that the DDMAS400MessageReply class
          // extracts all the AS400 messages contained in reply.data_.  I.e.
          // a single reply may contain more than one AS400 message.
          DDMAS400MessageReply msgReply = new DDMAS400MessageReply(system_, reply.data_);
          as400MsgList.addElement(msgReply.getAS400MessageList());
          break;
          // If any of the following cases occur, we throw the exception and are done.
        case DDMTerm.AGNPRMRM:
        case DDMTerm.CMDCHKRM:
        case DDMTerm.CMDNSPRM:
        case DDMTerm.DCLNAMRM:
        case DDMTerm.PRCCNVRM:
        case DDMTerm.PRMNSPRM:
        case DDMTerm.RSCLMTRM:
        case DDMTerm.SYNTAXRM:
        case DDMTerm.VALNSPRM:
          if (Trace.isTraceOn() && Trace.isTraceErrorOn())
          {
            Trace.log(Trace.ERROR, "handleErrorReply()", reply.data_);
          }
          throw new InternalErrorException(codePoint);
        default:
          // We don't know what the reply is.  Throw exception and be done.
          if (Trace.isTraceOn() && Trace.isTraceErrorOn())
          {
            Trace.log(Trace.ERROR, "handleErrorReply()", reply.data_);
          }
          throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, codePoint);
      }
    }
    // If we get to here, we should have a list of AS400 messages to throw
    if (as400MsgList.size() > 0)
    {
      // We need to expand out the Vector of AS400Message[]'s to individual
      // AS400Message's in a single array in order to construct our AS400Exception.
      int numberOfMessages = 0;
      int msgListSize = as400MsgList.size();
      int msgNumber = 0;
      // Determine number of AS400Message objects we have
      for (int i = 0; i < msgListSize; ++i)
      {
        numberOfMessages += ((AS400Message[])as400MsgList.elementAt(i)).length;
      }
      // Now populate the single AS400Message[] with the AS400Message objects
      msgs = new AS400Message[numberOfMessages];
      AS400Message[] m;
      for (int i = 0; i < msgListSize; ++i)
      {
        m = (AS400Message[])as400MsgList.elementAt(i);
        for (int j = 0; j < m.length; ++j)
        {
          msgs[msgNumber++] = m[j];
        }
      }
    }

    return msgs;
  }


  /**
   *Reads the record with the specified record number.
   *@param recordNumber The record number of the record to be read.  The
   *<i>recordNumber</i> must be greater than zero.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public Record read(int recordNumber)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    if (cacheRecords_) //@C0A
      return super.read(recordNumber); //@C0A

    int shr;  // Type of locking for the record

    // @A1C
    if ((openType_ == AS400File.READ_ONLY) || 
        ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) // @A1A
    {
      // Read only
      shr = SHR_READ_NORM;
    }
    else
    { // READ_WRITE
      shr = SHR_UPD_NORM;
    }
//    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETD(dclName_, recordFormat_, 0x08, shr, DATA_DTA_DTARCD,recordNumber,  // @A1D
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETD(dclName_, recordFormatCTLLName_, 0x08, shr, DATA_DTA_DTARCD,recordNumber, system_), newCorrelationId());  // @A1A @B6C
    Record[] returned = processReadReply(replys, false);    // @A1C

    return (returned == null)? null : returned[0];
  }

  /**
   *Reads all the records in the file. Helper function.
   *@param fileType The type of file.  Valid values are: key or seq
   *@return The records read.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public Record[] readAll(String fileType, int bf) //@D0C
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
//@B0A: Changed readAll() to not use the ULDRECF codepoint in the DDM data stream.
//      This is because ULDRECF (Unload all records from file) does not handle
//      null field values in records. (It throws us a data mapping error on
//      the data stream, which would then cause an OutOfMemory exception
//      or other strange errors in our code, because the data stream format
//      was no longer correct.)
//      readAll() now just uses the S38 extensions. It gets the first record
//      and then gets the next record in a loop until null is returned.
//      Using the S38 extensions gives better performance anyway (supposedly).
//      See SequentialFile.readAll() and KeyedFile.readAll() for other changes.
//      Other changes were also made in DDMRequestDataStream and DDMTerm.

//@B0A: start block
    
    // readAll is supposed to return at least a Record[] of size 0, never null
    Record[] recArray = new Record[0];
    
    synchronized(this) // We synchronize because this file object
    {                  // isn't supposed to be open (as far as the user knows).
      // Use a calculated blocking factor, else use a large blocking factor
//@D0M      int bf = 2048/(recordFormat_.getNewRecord().getRecordLength() + 16); //@D0C
//@D0M      if (bf <= 0) bf = 1; //@D0C
      //@E0 - We don't want to use COMMIT_LOCK_LEVEL_ALL in case commitment control is on because
      // inside readAll(), the file isn't supposed to be open, so we should treat it as such.
      openFile2(AS400File.READ_ONLY, bf, AS400File.COMMIT_LOCK_LEVEL_NONE, fileType); //@D0C @E0C
      
      // The vector to hold the records as we retrieve them.
      Vector allRecords = new Vector();

      // The following block was copied from readRecord()
      int shr;  // Type of locking for the record
      if ((openType_ == AS400File.READ_ONLY) || 
          ((openType_ == AS400File.READ_WRITE) && readNoUpdate_))
      { // Read only
        shr = SHR_READ_NORM;
      }
      else
      { // READ_WRITE; get the record for update
        shr = SHR_UPD_NORM;
      }

      // Get the records
      // Initialize returned to be of TYPE_GET_FIRST
      // As the loop continues, returned is of TYPE_GET_NEXT
      for (Record[] returned = processReadReply(sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GET(dclName_, TYPE_GET_FIRST, shr, DATA_DTA_DTARCD), newCorrelationId()), false); //@B6C
          returned != null;
          returned = processReadReply(sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GET(dclName_, TYPE_GET_NEXT, shr, DATA_DTA_DTARCD), newCorrelationId()), false)) //@B6C
      {
        // The reply is an array of records, so add each of them to the vector
        for (int i=0; i<returned.length; i++)
        {
          allRecords.addElement(returned[i]);
        }
      }

      // Copy the records in the vector into a Record[] object that we can return
      int numRecs = allRecords.size();
      if (numRecs > 0)
      {
        recArray = new Record[numRecs];
        allRecords.copyInto(recArray);
      }
      close(); // Need to close the file since we opened it earlier.
    }          // The file is not supposed to be open to the user.

    return recArray;
//@B0A: end block

//@B0D: start block
/*
    // Connect to the AS400.  Note: If we have already connected, that connection
    // will be used.
    connect();
    // Send the request to read all records.  Because the reply object may contain many records
    // we need to deal with the possibility of chained replies.
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestULDRECF(fileType, library_, file_, member_,
       system_), server_.newCorrelationId());
    Record[] records = null;
    int index = lookForCodePoint(DDMTerm.RECORD, replys);
    if (index == -1)
    { // Check for any RECAL terms.  These contain the active record if we encounter an inactive record.  This covers the
      // case where records 1, 3, 5, 7, ... have been deleted.  In this event we would have only RECAL's returned
      // which would contain records 2, 4, 6, 8, ...
      index = lookForCodePoint(DDMTerm.RECAL, replys);
    }

    if (index != -1)
    {
      // Records returned.  Process the reply object(s)
      // The last DDMTERM of the last reply received is a RECCNT and contains the number of records read.
      int numReplies = replys.size();
      DDMDataStream reply = (DDMDataStream)replys.elementAt(numReplies - 1);
      int numRead = reply.get32bit(reply.data_.length - 4);
      records = new Record[numRead];
      if (numRead > 0)
      {
        int recordNumber = 1;
        int recOffset;
        int offset;
        boolean done;
        for (int i = index, j = 0; i < numReplies; ++i)
        {
          reply = (DDMDataStream)replys.elementAt(i);
          done = false;
          offset = 10;
          while (!done)
          {
            // Find the next occurrence of a RECORD or RECAL term.  We get a RECAL which will contain a
            // RECORD when we encounter an inactive (deleted) record position.  Otherwise we should just
            // get a RECORD term
            while (offset < reply.data_.length && ((reply.get16bit(offset - 2) != DDMTerm.RECORD) && (reply.get16bit(offset - 2) != DDMTerm.RECAL)))
            {
              offset += reply.get16bit(offset - 4);
            }
            if (offset >= reply.data_.length)
            {
              break;
            }
            // Determine offset of the record data and determine the record number
            if (reply.get16bit(offset - 2) == DDMTerm.RECAL)
            { // We have encountered a spot for an inactive record.  The RECAL term consists
              // of the record number of the next active record along with the  record data of the
              // next active record.
              recOffset = offset + 12;  // Skip ahead to the record data
              recordNumber = reply.get32bit(offset + 4); // Set record number to the record number of the
                                                         // active record
            }
            else
            { // This is an active record; we are already pointing to the record data and the record number
              // was set appropriately last time through the loop.
              recOffset = offset;
            }

            if (reply.get16bit(offset - 4) <= 0x7FFF)
            { // The record data immediately follows the code point if the
              // record length + 4 is <= 32767
              records[j] = recordFormat_.getNewRecord(reply.data_, recOffset);
            }
            else
            { // The record data starts 4 bytes after the code point if the
              // record length + 4 is > 32767.  The four bytes immediately
              // after the code point contain the actual length of the record
              // record data that follows.
              records[j] = recordFormat_.getNewRecord(reply.data_, recOffset + 4);
            }
            // Set the record number of the record
            try
            {
              records[j++].setRecordNumber(recordNumber++);
            }
            catch(PropertyVetoException e)
            { // We created the Record objects.  There is no one to veto anything
            } // so this is here to quit the compiler

            if (j == numRead)
            {
              done = true;
            }
            // Get offset of next record
            offset += reply.get16bit(offset - 4);
          }
        }
      }
    }
    else if (replys.size() == 1 && ((DDMDataStream)replys.elementAt(0)).getCodePoint() == DDMTerm.RECCNT)
    {
      // If the file contains all deleted records, the only thing returned
      // is a RECCNT.  If this is the case, return an empty Record[]
      return new Record[0];
    }
    else
    { // Error occurred
      handleErrorReply(replys, 0);
    }

    return (records == null ? new Record[0] : records);
*/
//@B0D: end block
  }

  /**
   *Reads the first record with the specified key based on the specified type of read.
   *@param key The values that make up the key with which to find the record.
   *@param type The type of read.  This value is one of the TYPE_GETKEY_* constants.
   *@return The record read.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record read(Object[] key, int type)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    int shr;  // Type of locking for the record
    if ((openType_ == AS400File.READ_ONLY) || 
        ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) // @A1A
    {
      // Read only
      shr = SHR_READ_NORM;
    }
    else
    { // READ_WRITE
      shr = SHR_UPD_NORM;
    }

//    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETK(dclName_, recordFormat_, type, shr, DATA_DTA_DTARCD, key, system_), server_.newCorrelationId());  // @A1D
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETK(dclName_, recordFormat_, recordFormatCTLLName_, type, shr, DATA_DTA_DTARCD, key, system_), newCorrelationId());  // @A1A @B6C
    // Call processReadReply to extract the records read (or throw an
    // exception if appropriate)
    Record[] returned = processReadReply(replys, false);    // @A1C
    
    if (cacheRecords_) //@C0A
    {                  //@C0A
      cache_.setIsEmpty(); //@C0A
    }                  //@C0A
    
    return (returned == null)? null : returned[0];
  }



  // @A1A
  /**
   *Reads the first record with the specified key based on the specified type of read.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *@param type The type of read.  This value is one of the TYPE_GETKEY_* constants.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@return The record read.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record read(byte[] key, int type, int numberOfKeyFields)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    int shr;  // Type of locking for the record
    if ((openType_ == AS400File.READ_ONLY) || 
        ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) // @A1A
    {
      // Read only
      shr = SHR_READ_NORM;
    }
    else
    { // READ_WRITE
      shr = SHR_UPD_NORM;
    }

//    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETK(dclName_, recordFormat_, type, shr, DATA_DTA_DTARCD, key, system_, numberOfKeyFields), server_.newCorrelationId());  // @A1D
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETK(dclName_, recordFormatCTLLName_, type, shr, DATA_DTA_DTARCD, key, system_, numberOfKeyFields), newCorrelationId());  // @A1A @B6C
    // Call processReadReply to extract the records read (or throw an
    // exception if appropriate)
    Record[] returned = processReadReply(replys, false);    // @A1C
    
    if (cacheRecords_) //@C0A
    {                  //@C0A
      cache_.setIsEmpty(); //@C0A
    }                  //@C0A
    
    return (returned == null)? null : returned[0];
  }

  /**
   *Reads a record from the file.  Which record to read is determined by the <i>type</i>
   *argument.
   *@param type The type of get to execute.  Valid values are:
   *<ul>
   *<li>TYPE_GET_FIRST
   *<li>TYPE_GET_NEXT
   *<li>TYPE_GET_LAST
   *<li>TYPE_GET_PREV
   *@return the record read
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readRecord(int type)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    int shr;  // Type of locking for the record

    // @A1C
    if ((openType_ == AS400File.READ_ONLY) || 
        ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) // @A1A
    {
      // Read only
      shr = SHR_READ_NORM;
    }
    else
    { // READ_WRITE; get the record for update
      shr = SHR_UPD_NORM;
    }

    // Send the get S38GET request
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GET(dclName_, type, shr, DATA_DTA_DTARCD), newCorrelationId()); //@B6C
    // Call processReadReply to extract the records read (or throw an
    // exception if appropriate)
    Record[] returned = processReadReply(replys, false);    // @A1C
    return (returned == null)? null : returned[0];
  }

  /**
   *Reads records from the file.  The next or previous 'blockingFactor_'
   *records are retrieved depending on the direction specified.
   *@param direction
   *@return the records read
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public Record[] readRecords(int direction)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    int type = (direction == DDMRecordCache.FORWARD ? TYPE_GET_NEXT :
                TYPE_GET_PREV);

    // Send the S38GETM request
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestS38GETM(dclName_, blockingFactor_, type, SHR_READ_NORM, DATA_DTA_DTARCD, 0x01), newCorrelationId()); //@B6C

    // Call processReadReply to extract the records read (or throw an
    // exception if appropriate)
    return processReadReply(replys, false);     // @A1C
  }




//@C1 - This method is not used anywhere.

//  /**
//   *Resets the state instance variables of this object to the appropriate
//   *values for the file being closed.  This method is used to reset the
//   *the state of the object when the connection has been ended abruptly.
//  **/
//@C1D - begin deleted block
/*
  void resetStateForReadObject()
  {
    dclName_ = new byte[8];

    // Generate the declared file name for the object
    setDCLName();

    server_ = null;
  }
*/
//@C1D - end deleted block

  /**
   *Rolls back any transactions since the last commit/rollback boundary.  Invoking this
   *method will cause all transactions under commitment control for this connection
   *to be rolled back.  This means that any AS400File object for which a commit
   *lock level was specified and that was opened under this connection will have
   *outstanding transactions rolled back.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public void rollback()
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    // Connect to the AS400.  Note: If we have already connected, that connection
    // will be used.
    connect();

    // Send the rollback data stream request
    Vector replys = sendRequestAndReceiveReplies(DDMRequestDataStream.getRequestRLLBCKUOW(), newCorrelationId()); //@B6C
    // Reply expected: ENDUOWRM, with UOWDSP parameter = 1
    if (replys.size() == 1 && ((DDMDataStream)replys.elementAt(0)).getCodePoint() == DDMTerm.ENDUOWRM)
    {
      DDMEndUnitOfWorkReply uowReply = new DDMEndUnitOfWorkReply(((DDMDataStream)replys.elementAt(0)).data_);
      if (uowReply.getStatus() != 0x02)
      {
        // Status of logical unit of work committed not returned; should not happen unless
        // we are constructing the request wrong.
        if (Trace.isTraceOn() && Trace.isTraceErrorOn())
        {
          Trace.log(Trace.ERROR, "AS400FileImplRemote.rollback()",
                    uowReply.data_);
        }
        throw new InternalErrorException(uowReply.getStatus());
      }
    }
    else
    {
      handleErrorReply(replys, 0);
    }
  }

  /**
   *Sends a request and receives all the replies.  This method is used when the potential
   *for chained replies exists.
   *@param req The request to be sent.
   *@param correlationId The correlation id for the request.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Vector sendRequestAndReceiveReplies(DDMDataStream req, int correlationId)
  throws /* AS400Exception,*/
      InterruptedException,
      IOException
  {
    DDMDataStream reply = null;
    try
    {
      server_.send(req, correlationId);
      reply = (DDMDataStream)server_.receive(correlationId);
    }
    catch (ConnectionDroppedException e)
    {
      // UConnection dropped.  Disconnect server and rethrow
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.ERROR, "ConnectionDroppedException.");
      }
      system_.disconnectServer(server_);
//@C1 - Setting the server_ object to null means that
//      any operations on this AS400File object after the connection has been
//      dropped will result in a NullPointerException. By leaving the server_ object
//      around, any subsequent operations should also throw a ConnectionDroppedException.
//@C1D      server_ = null;
      resetState();
      throw e;
    }

    // Receive all replies from the read into a vector.
    Vector replys = new Vector();
    while (reply.isChained())
    {
      replys.addElement(reply);
      try
      {
        reply = (DDMDataStream)server_.receive(correlationId);
      }
      catch (ConnectionDroppedException e)
      {
        // UConnection dropped.  Disconnect server and rethrow
        if (Trace.isTraceOn() && Trace.isTraceErrorOn())
        {
          Trace.log(Trace.ERROR, "ConnectionDroppedException.");
        }
        system_.disconnectServer(server_);
//@C1 - Setting the server_ object to null means that
//      any operations on this AS400File object after the connection has been
//      dropped will result in a NullPointerException. By leaving the server_ object
//      around, any subsequent operations should also throw a ConnectionDroppedException.
//@C1D        server_ = null;
        resetState();
        throw e;
      }
    }
    // Add the unchained reply to the vector of replys
    replys.addElement(reply);
    return replys;
  }


  /**
   *Sets the declared file name (DCLNAM). The declared file name for each
   *file object must be unique.  This method will generate a unique declared file
   *name.
  **/
  /*@D2D protected*/public void setDCLName()
  {
    // Convert nextDCLName_ to a Long and then to a string
    String nextDCLNameAsString = Long.toString(nextDCLName_++);

    // Copy EBCDIC version of nextDCLName to dclNAme_ and blank pad dclName_
    // to 8 bytes
    for (int i = 0; i < nextDCLNameAsString.length(); i++)
    {
      char c = nextDCLNameAsString.charAt(i);
      dclName_[i] = (byte) (c & 0x000f | 0x00f0);
    }
    padBytes(dclName_, nextDCLNameAsString.length(),
             8 - nextDCLNameAsString.length(), (byte)0x40);
  }


  /**
   *Updates the record at the current cursor position.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void update(Record record)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    if (record.getRecordLength() != openFeedback_.getRecordLength())
    {
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.ERROR, "Incorrect record length for file :");
        Trace.log(Trace.ERROR, "record.getRecordLength() :" + String.valueOf(record.getRecordLength()));
      }
      throw new ExtendedIllegalArgumentException("record", ExtendedIllegalArgumentException. PARAMETER_VALUE_NOT_VALID);
    }
    
    // getObjectS38BUF requires an array of records
    Record[] records = new Record[1];
    records[0] = record;
    // We will be chaining an S38BUF to the S38UPDAT request.  This requires that
    // the correlation ids be the same.
    int correlationId = newCorrelationId(); //@B6C
    // Send the S38UPDAT request followed by the S38BUF object containing the
    // record with which to update.
    DDMRequestDataStream req = DDMRequestDataStream.getRequestS38UPDAT( TYPE_GET_SAME, SHR_UPD_NORM, DATA_DTA_DTARCD, dclName_);
    req.setIsChained(true);  // Indicate we are chaining an object to the request
    req.setHasSameRequestCorrelation(true); // Indicate that they have the same
                                            // correlation ids
    // Get the S38BUF object(s) to send after the request
    // Because we are updating, there will only be one item in dataToSend
    DDMObjectDataStream[] dataToSend =
        DDMObjectDataStream.getObjectS38BUF(records, openFeedback_);
    try
    {
      server_.send(req, correlationId);
    }
    catch (ConnectionDroppedException e)
    {
      // UConnection dropped.  Disconnect server and rethrow
      Trace.log(Trace.ERROR, "ConnectionDroppedException.");
      system_.disconnectServer(server_);
//@C1 - Setting the server_ object to null means that
//      any operations on this AS400File object after the connection has been
//      dropped will result in a NullPointerException. By leaving the server_ object
//      around, any subsequent operations should also throw a ConnectionDroppedException.
//@C1D      server_ = null;
      resetState();
      throw e;
    }
    Vector replys = sendRequestAndReceiveReplies(dataToSend[0], correlationId);
    // Reply expected: S38IOFB
    if (((DDMDataStream)replys.elementAt(0)).getCodePoint() == DDMTerm.S38IOFB)
    {
      if (replys.size() != 1)
      {
        handleErrorReply(replys, 1);
      }
    }
    else
    { // Error occurred
      handleErrorReply(replys, 0);
    }
  }

  /**
   *Verifies the reply datastream as an S38MSGRM with the specified
   *information.  If msgID is not null, svrCode is not checked.
   *If msgId is null, svrCode is verified.
   @param reply The DDM reply data stream.
   @param msgId The AS/400 message id that this reply should contain.
   @param svrCode The severity code that this reply should contain.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public boolean verifyS38MSGRM(DDMReplyDataStream reply, String msgId, int svrCode)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    DDMAS400MessageReply msgReply = new DDMAS400MessageReply(system_, reply.data_);
    AS400Message[] msgs = msgReply.getAS400MessageList();
    for (int i = 0; i < msgs.length; ++i)
    {
      if (msgId != null)
      { // Verify based on msgid only
        if (msgs[i].getID().equalsIgnoreCase(msgId))
        {
          return true;
        }
      }
      else
      { // Verify based on severity code only
        if (msgs[i].getSeverity() == svrCode)
        {
          return true;
        }
      }
    }
    return false;
  }



  /**
   *Writes an array of records to the file.
   *@param records The records to write.  The records must have a format
   *which matches the record format of this object.  To ensure that this
   *requirement is met, use the
   *<a href="com.ibm.as400.access.RecordFormat.html">RecordFormat.getNewRecord()</a>
   *method to obtain default records whose fields can be set appropriately by
   *the Java program and then written to the file.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void write(Record[] records)
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    if (records[0].getRecordLength() != openFeedback_.getRecordLength())
    {
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.ERROR, "Incorrect record length for file :");
        Trace.log(Trace.ERROR, "record.getRecordLength() :" + String.valueOf(records[0].getRecordLength()));
      }
      throw new ExtendedIllegalArgumentException("records", ExtendedIllegalArgumentException. PARAMETER_VALUE_NOT_VALID);
    }
    
    // We will be chaining the S38BUF to the request, so the correlation ids must match
    int correlationId = newCorrelationId(); //@B6C
    DDMRequestDataStream req = DDMRequestDataStream.getRequestS38PUTM(dclName_);
    req.setIsChained(true);  // Indicate that the request is chained
    req.setHasSameRequestCorrelation(true); // Indicate hat the ids will match
    // Get the S38BUF object(s) containing the records to write.
    DDMObjectDataStream[] dataToSend =
        DDMObjectDataStream.getObjectS38BUF(records, openFeedback_);

    // It is possible that we will have more than one S38BUF to send.  This case
    // occurs when the blocking factor is less than the number records to be written.
    // In that case we do multiple of S38PUTMs of blocking factor number of records.
    Vector replys;
    for (int i = 0; i < dataToSend.length; ++i)
    { // For each S38BUF object, send the S38PUTM followed by the S38BUF
      server_.send(req, correlationId);
      replys = sendRequestAndReceiveReplies(dataToSend[i], correlationId);
      // Reply expected: S38IOFB
      if (((DDMDataStream)replys.elementAt(0)).getCodePoint() == DDMTerm.S38IOFB)
      {
        if (replys.size() != 1)
        {
          handleErrorReply(replys, 1);
        }
      }
      else
      { // Error occurred
        handleErrorReply(replys, 0);
      }
    }
  }

  // @B1A
  // @B6C
  private int newCorrelationId()
  {
    synchronized(correlationIdLock_)
    {
      if (lastCorrelationId_ == 0x7fff) lastCorrelationId_ = 0;
      return ++lastCorrelationId_;
    }
  }
}




