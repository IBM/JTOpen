///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FileImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal; //@C1A
import java.util.Vector; //@C1A

//@C0C: We now extend AS400FileImplBase.
class AS400FileImplNative extends AS400FileImplBase implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // File handle.
    transient int handle_;
    // Static synchronization variable for command execution
    static String synch_execute_ = "";
    // Static synchronization variable for open/close of files.
    // The qyjsprl.C smalltalk C module keeps track of the handles allocated
    // and freed for files globally.  Therefore we synchronize opens and closes
    // at the class level for processes using RLA
    static String synch_open_close_ = "";

    static
    {
        System.load("/QSYS.LIB/QYJSPART.SRVPGM");
        try
        {
            resetStaticStorage(); //@E3A
        }
        catch(Throwable t) //@E3A In case they don't have the new service program change to match.
        {
            if (Trace.isTraceOn() && Trace.isTraceWarningOn()) //@E3A
            {
                Trace.log(Trace.WARNING, "Exception occurred while resetting static storage for DDM: ", t); //@E3A
            }
        }
    }

    //@C1A - Need this for the new createDDSSourceFile() method.
    public AS400FileImplNative()
    {
        isNative_ = true; //@E2A
    }

    /**
     *Closes the file on the AS400.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the
     *AS/400.
     **/
    public void close()
      throws AS400Exception, AS400SecurityException, InterruptedException,  IOException
    {
        super.close(); //@C0A

        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            synchronized(synch_open_close_)
            {
                // Close the file.
                closeNtv(handle_);
            }
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    /**
     *Closes the file on the AS400.
     *@param handle the file handle.
     *@return the message feedback data
     **/
    native void closeNtv(int handle)
      throws NativeException;

    /**
     *Commits all transactions since the last commit boundary.  Invoking this
     *method will cause all transactions under commitment control for this
     *connection to be committed.  This means that any AS400File object opened
     *under this connection, for which a commit lock level was specified, will
     *have outstanding transactions committed.  If commitment control has not been
     *started for the connection, no action is taken.<br>
     *The AS400 system to which to connect must be set prior to invoking this
     *method.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the
     *AS/400.
     **/
    public void commit()
      throws AS400Exception, AS400SecurityException, InterruptedException,  IOException
    {
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // Commit transactions under commitment control.
            commitNtv();
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    /**
     *Commits all transactions since the last commit boundary.  Invoking this
     *method will cause all transactions under commitment control for this
     *connection to be committed.  This means that any AS400File object opened
     *under this connection, for which a commit lock level was specified, will
     *have outstanding transactions committed.  If commitment control has not been
     *started for the connection, no action is taken.<br>
     *The AS400 system to which to connect must be set prior to invoking this
     *method.
     **/
    native void commitNtv()
      throws NativeException;


    //@C1M 7/16/99 - Moved this code out of ImplBase to here
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
        //@C2 - This is how it should be done, but it will work the deprecated way because
        //      we are running on the 400.
        //@C2      AS400Text text80 = new AS400Text(80, system_.getCcsid(), system_); //@C2A
        //@C2      if (converter_ == null) setConverter(); //@C2A
        //@C2      text80.setConverter(converter_); //@C2A
        srcRF.addFieldDescription(new CharacterFieldDescription(new AS400Text(80, system_.getCcsid()), "SRCDTA"));
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

        //@C1 - This is why we had to move this code out of the ImplBase, so we can instantiate.
        AS400FileImplNative src = null;
        try
        {
            src = new AS400FileImplNative();
            src.setAll(system_, "/QSYS.LIB/QTEMP.LIB/JT400DSSRC.FILE", srcRF, false, false, false);

            src.openFile2(AS400File.WRITE_ONLY, records.length, AS400File.COMMIT_LOCK_LEVEL_NONE, false);
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
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public void deleteCurrentRecord()
      throws AS400Exception, AS400SecurityException, InterruptedException,  IOException
    {
        byte[] optl = {0x00, SHR_READ_NORM_RLS, DATA_DTA_DTARCD, 0x08};
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // Delete the current record.
            deleteCurrentRecordNtv(handle_, optl);
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    /**
     *Deletes the record at the current cursor position.  The file must be open and
     *the cursor must be positioned on an active record.
     *@param handle the file handle
     *@return the message feedback data and the I/O feedback data
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    native void deleteCurrentRecordNtv(int handle, byte[] optl)
      throws NativeException;

    /**
     *Executes a command on the AS/400.
     *@param cmd the command
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public AS400Message[] execute(String cmd)
      throws AS400SecurityException, InterruptedException, IOException
    {
        if (converter_ == null) setConverter(); //@C2A

        // Execute the command.
        // Note the execution of the command is synchronized on the static
        // variable synch_execute_
        BytesWithOffset data = null;
        synchronized(synch_execute_)
        {
            data =
              new BytesWithOffset(executeNtv(converter_.stringToByteArray(cmd)));
        }

        // Parse the message feedback data.
        AS400Message[] msgs = parseMsgFeedback(data.data_);
        return msgs;
    }

    /**
     *Executes a command on the AS/400.
     *@param cmd the command
     *@return the message feedback data
     **/
    native byte[] executeNtv(byte[] cmd);

    //@C0A
    /**
     *Prepare this object for garbage collection.
     *@exception Throwable If an exception is thrown while cleaning up.
     **/
    public void finalization()
      throws Throwable
    {
        // This method does nothing. It is here because finalization() is
        // declared abstract in AS400FileImplBase.
    }


    /**
     *Changes the position of the file cursor to either before the first
     *record or after the last record.
     *@param handle the file handle
     *@param optl the options list
     *@return the message feedback data and the I/O feedback data
     **/
    native void forceEndOfData(int handle,
                               byte[] optl)
      throws NativeException;

    /**
     *Read record(s) from the file.  Does not screen out CPF5001, CPF5006.
     *@param handle the file handle
     *@param optl option list
     *@param ctll control list
     *@param length length of all record data
     *@return message feedback data, I/O feedback data, record data.
     **/
    native byte[] getForPosition(int handle,
                                 byte[] optl,
                                 byte[] ctll,
                                 int length)
      throws NativeException;

    /**
     *Read record(s) from the file. Screens out CPF5001, CPF5006
     *@param handle the file handle
     *@param optl option list
     *@param ctll control list
     *@param length length of all record data
     *@return message feedback data, I/O feedback data, record data.
     **/
    native byte[] getForRead(int handle,
                             byte[] optl,
                             byte[] ctll,
                             int length)
      throws NativeException;

    /**
     *Read record(s) from the file by record number. Does not screen out
     *CPF5001, CPF5006.
     *@param handle the file handle
     *@param optl option list
     *@param ctll control list
     *@param length length of all record data
     *@return message feedback data, I/O feedback data, record data.
     **/
    native byte[] getdForPosition(int handle,
                                  byte[] optl,
                                  byte[] ctll,
                                  int length)
      throws NativeException;

    /**
     *Read record(s) from the file by record number. Screens out CPF5001,
     *CPF5006.
     *@param handle the file handle
     *@param optl option list
     *@param ctll control list
     *@param length length of all record data
     *@return message feedback data, I/O feedback data, record data.
     **/
    native byte[] getdForRead(int handle,
                              byte[] optl,
                              byte[] ctll,
                              int length)
      throws NativeException;

    /**
     *Read record(s) from the file by key. Does not screen out CPF5001,
     *CPF5006.
     *@param handle the file handle
     *@param optl option list
     *@param ctll control list
     *@param length length of all record data
     *@return message feedback data, I/O feedback data, record data.
     **/
    native byte[] getkForPosition(int handle,
                                  byte[] optl,
                                  byte[] ctll,
                                  int length)
      throws NativeException;

    /**
     *Read record(s) from the file by key. Screens out CPF5001, CPF5006.
     *@param handle the file handle
     *@param optl option list
     *@param ctll control list
     *@param length length of all record data
     *@return message feedback data, I/O feedback data, record data.
     **/
    native byte[] getkForRead(int handle,
                              byte[] optl,
                              byte[] ctll,
                              int length)
      throws NativeException;

    /**
     *Opens the file.
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
      throws AS400Exception, AS400SecurityException, InterruptedException, IOException
    {
        // Create the user file control block.
        byte[] ufcb = createUFCB(openType, bf, access, true);

        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            synchronized(synch_open_close_)
            {
                // Open the file.
                data = new BytesWithOffset(openFileNtv(ufcb));
            }
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
        // Parse the message feedback data.
        AS400Message[] msgs = parseMsgFeedback(data);

        // Parse the file handle.
        handle_ = BinaryConverter.byteArrayToInt(data.data_, data.offset_);
        data.offset_ += 4;

        // Throw an exception if the open failed and there are no AS400Messages
        if (handle_ <= 0)
        {
            throw new InternalErrorException("Invalid handle returned from QYSTRART",
                                             InternalErrorException.UNKNOWN);
        }
        // Log warning messages if the open was successful (handle_ > 0)
        // and there are AS400Messages
        if (msgs.length > 0)
        {
            if (Trace.isTraceOn() && Trace.isTraceWarningOn())
            {
                Trace.log(Trace.WARNING, "AS400FileImplNative.openFile:");
                for (int i = 0; i < msgs.length; ++i)
                {
                    Trace.log(Trace.WARNING, msgs[i].toString());
                }
            }
        }

        // Parse the open feedback data.
        openFeedback_ = new LocalOpenFeedback(system_, //@C0C
                                              data.data_,
                                              data.offset_);

        return openFeedback_;
    }

    /**
     *Opens the file.
     *@param ufcb user file control block.
     *@return message feedback data, file handle, and open feedback data
     *(in that order)
     **/
    native byte[] openFileNtv(byte[] ufcb)
      throws NativeException;

    /**
     *Parse the record data into records.
     *@param iofb I/O feedback data
     *@param data record data and offset.
     *@return the records contained in the record data
     **/
    private Record[] parseRecordData(LocalIOFB iofb,
                                     BytesWithOffset data)
      throws UnsupportedEncodingException
    {
        int numberOfRecords = iofb.getNumberOfRecordsReturned();
        Record[] records = new Record[numberOfRecords];
        // Determine the null byte field map offset.  When we opened the file, the
        // the S38OPNFB reply contained the offset of the null byte field map in a record.
        int nullFieldMapOffset = openFeedback_.getNullFieldByteMapOffset();
        // Determine the number of fields in a record from the RecordFormat for this file.
        int numFields = recordFormat_.getNumberOfFields(); //@C0C
        // If the file has null capable fields, we will need to check the null byte field
        // map and set the fields within the Record object as appropriate.
        boolean isNullCapable = openFeedback_.isNullCapable();

        for (int i = 0; i < numberOfRecords; ++i,
        data.offset_ += openFeedback_.getRecordIncrement())
        {
            // Create the next record.
            records[i] = recordFormat_.getNewRecord(data.data_, //@C0C
                                                    data.offset_);

            // Set any null fields to null
            if (isNullCapable)
            {
                for (int j = 0; j < numFields; ++j)
                { // 0xF1 = field is null, 0xF0 = field is not null
                    if (data.data_[nullFieldMapOffset + j + data.offset_] == (byte)0xF1)
                    {
                        records[i].setField(j, null);
                    }
                }
            }

            // Set the record number.  The record number is two bytes after the end
            // of the record data and is four bytes long.
            try
            {
                records[i].setRecordNumber(BinaryConverter.byteArrayToInt(data.data_, data.offset_ + iofb.getRecordLength() + 2));
            }
            catch(PropertyVetoException e)
            { // We created the Record objects.  There is no one to veto anything
            } // so this is here to quite the compiler
        }
        return records;
    }


    /**
     *Parse the message feedback data into an array of AS400Message objects.
     *@param data the message feedback data and offset.
     *@return array of AS400Message objects (zero length array if no messages are available
     **/
    private AS400Message[] parseMsgFeedback(byte[] data)
      throws IOException
    {
        AS400Message[] msgs = new AS400Message[0];

        // Determine if any messages are available.
        Record msgFB =
          (new MessageFBFormat()).getNewRecord(data);
        Short messagesOccurred = (Short) msgFB.getField("messagesOccurred");
        if (messagesOccurred.intValue() == 1)
        {
            // Determine the number of messages.
            int numMsgs = ((Short) msgFB.getField("numberOfMessages")).intValue();
            if (numMsgs == 0)
            {
                return msgs;
            }

            // Loop through the array of message feedback data structures,
            // building an AS400Message object for each one.
            msgs = new AS400Message[numMsgs];
            MessageFBDataFormat msgFBDataFormat =
              new MessageFBDataFormat(system_.getCcsid()); //@C0C
            Object[] feedbackData = (Object[]) msgFB.getField("feedbackData");
            for (int i = 0; i < numMsgs; i++)
            {
                // Extract the severity, type, ID, and text from the message
                // feedback.
                Record msg = msgFBDataFormat.getNewRecord((byte[]) feedbackData[i]);
                int severity = ((Integer) msg.getField("severityCode")).intValue();
                String typeString = (String)msg.getField("messageType");
                char[] typeChars = typeString.toCharArray();
                int type = (typeChars[0] & 0x0F) * 10 + (typeChars[1] & 0x0F);
                String id = (String) msg.getField("messageID");
                int textLength = ((Integer) msg.getField("messageTextLength")).intValue();
                int substitutionDataLength = ((Integer) msg.getField("replacementTextLength")).intValue();

                //@B0A: We don't use the messageText field of the record format because
                //      it hardcodes a length of 256 bytes. If the messageText is shorter
                //      than 256 bytes, then we are translating junk bytes, which can
                //      cause problems when running under a double-byte ccsid where
                //      weird characters would not translate correctly.
                //      Instead, we use the messageTextLength field to determine how
                //      long the messageText is and translate it ourselves using a
                //      CharConverter object.
                byte[] arr = (byte[])feedbackData[i]; //@B0A
                String text = (new CharConverter(system_.getCcsid())).byteArrayToString(arr, 112 + substitutionDataLength, textLength); //@B0A //@C0C
                //@B0D        String text =
                //@B0D          ((String) msg.getField("messageText")).substring(0, textLength);

                // Construct an AS400Message.
                msgs[i] = new AS400Message(id, text);
                msgs[i].setSeverity(severity);
                msgs[i].setType(type);
            }
        }

        return msgs;
    }

    /**
     *Parse the message feedback data into an array of AS400Message objects.
     *@param data the message feedback data and offset.
     *@return array of AS400Message objects (zero length array if no messages are available
     **/
    private AS400Message[] parseMsgFeedback(BytesWithOffset data)
      throws IOException
    {
        AS400Message[] msgs = new AS400Message[0];

        // Determine if any messages are available.
        Record msgFB =
          (new MessageFBFormat()).getNewRecord(data.data_, data.offset_);
        data.offset_ += msgFB.getRecordLength();
        Short messagesOccurred = (Short) msgFB.getField("messagesOccurred");
        if (messagesOccurred.intValue() == 1)
        {
            // Determine the number of messages.
            int numMsgs = ((Short) msgFB.getField("numberOfMessages")).intValue();
            if (numMsgs == 0)
            {
                return msgs;
            }

            // Loop through the array of message feedback data structures,
            // building an AS400Message object for each one.
            msgs = new AS400Message[numMsgs];
            MessageFBDataFormat msgFBDataFormat =
              new MessageFBDataFormat(system_.getCcsid()); //@C0C
            Object[] feedbackData = (Object[]) msgFB.getField("feedbackData");
            for (int i = 0; i < numMsgs; i++)
            {
                // Extract the severity, type, ID, and text from the message
                // feedback.
                Record msg = msgFBDataFormat.getNewRecord((byte[]) feedbackData[i]);
                int severity = ((Integer) msg.getField("severityCode")).intValue();
                String typeString = (String)msg.getField("messageType");
                char[] typeChars = typeString.toCharArray();
                int type = (typeChars[0] & 0x0F) * 10 + (typeChars[1] & 0x0F);
                String id = (String) msg.getField("messageID");
                int textLength = ((Integer) msg.getField("messageTextLength")).intValue();
                int substitutionDataLength = ((Integer) msg.getField("replacementTextLength")).intValue();

                //@B0A: We don't use the messageText field of the record format because
                //      it hardcodes a length of 256 bytes. If the messageText is shorter
                //      than 256 bytes, then we are translating junk bytes, which can
                //      cause problems when running under a double-byte ccsid where
                //      weird characters would not translate correctly.
                //      Instead, we use the messageTextLength field to determine how
                //      long the messageText is and translate it ourselves using a
                //      CharConverter object.
                byte[] arr = (byte[])feedbackData[i]; //@B0A
                String text = (new CharConverter(system_.getCcsid())).byteArrayToString(arr, 112 + substitutionDataLength, textLength); //@B0A //@C0C
                //@B0D        String text =
                //@B0D          ((String) msg.getField("messageText")).substring(0, textLength);

                // Construct an AS400Message.
                msgs[i] = new AS400Message(id, text);
                msgs[i].setSeverity(severity);
                msgs[i].setType(type);
            }
        }

        return msgs;
    }
    /**
     *Positions the file cursor to after the last record.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public void positionCursorAfterLast()
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
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

        // Use force end of data.
        byte[] optl = { TYPE_GET_LAST, SHR_READ_NORM_RLS, DATA_NODTA_DTARCD, OPER_GET };
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            forceEndOfData(handle_, optl);
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    /**
     *Positions the file cursor to the specified position (first, last, next,
     *previous).
     *@param type the type of position operation
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record[] positionCursorAt(int type)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        // Use GET to position the cursor.
        // byte shr = (as400File_.openType_ == AS400File.READ_ONLY ?  // @A2D
        //             SHR_READ_NORM : SHR_UPD_NORM);                 // @A2D

        // Start of @A2A
        byte shr;
        if ((openType_ == AS400File.READ_ONLY) ||  //@C0C
            ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) //@C0C
        { // Read only
            shr = SHR_READ_NORM;
        }
        else
        { // READ_WRITE; get for update
            shr = SHR_UPD_NORM;
        }
        // End of @A2A

        byte[] optl = { (byte) type, shr, DATA_DTA_DTARCD, OPER_GET };
        byte[] ctll = new byte[6];
        ctll[0] = 0x10; // ID for number of records
        // Length of value field
        BinaryConverter.shortToByteArray((short) 2, ctll, 1);
        // Number of records to get
        BinaryConverter.shortToByteArray((short) blockingFactor_, ctll, 3); //@C0C
        ctll[5] = (byte)0xFF;  // End of control list
        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            data =
              new BytesWithOffset(getForPosition(handle_, optl, ctll,
                                                 blockingFactor_ * //@C0C
                                                 openFeedback_.getRecordIncrement()));
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Parse the I/O feedback data.
        LocalIOFB iofb = new LocalIOFB(data.data_, data.offset_);
        data.offset_ += 14;

        // Parse the record data.
        Record[] records = parseRecordData(iofb, data);

        return records;
    }

    /**
     *Positions the file cursor to before the first record.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public void positionCursorBeforeFirst()
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
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

        // Use Force end of data to set the cursor.
        byte[] optl = { TYPE_GET_FIRST, SHR_READ_NORM_RLS, DATA_NODTA_DTARCD, OPER_GET };
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            forceEndOfData(handle_, optl);
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    /**
     *Positions the cursor to the record at the specified file position.
     *@parm index the file position
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record positionCursorToIndex(int index)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        // OPTL
        // byte share = (as400File_.openType_ == AS400File.READ_ONLY ?  // @A2D
        //               SHR_READ_NORM : SHR_UPD_NORM);                 // @A2D

        // Start of @A2A
        byte share;
        if ((openType_ == AS400File.READ_ONLY) ||  //@C0C
            ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) //@C0C
        { // Read only
            share = SHR_READ_NORM;
        }
        else
        { // READ_WRITE; get for update
            share = SHR_UPD_NORM;
        }
        // End of @A2A

        byte[] optl = { TYPE_GETD_ABSRRN, share, DATA_DTA_DTARCD, OPER_GETD };

        // CTLL
        // ----------------------------------
        // Offset Description
        // ----------------------------------
        // 0      record format ID
        // 1      record format length
        // 3      record format name
        // 13     member number ID
        // 14     member number length
        // 16     member number value
        // 18     relative record number ID
        // 19     relative record number length
        // 21     relative record number
        // 25     control list end
        byte[] ctll = new byte[26];
        ctll[0] = 1;
        BinaryConverter.shortToByteArray((short) 35, ctll, 1);

        // Start of @A2D
        /*
         StringBuffer recordName =
         new StringBuffer(as400File_.recordFormat_.getName());
         while (recordName.length() < 10) recordName.append(' ');
         converter_.stringToByteArray(recordName.toString(), ctll, 3);
         */
        // End of @A2D

        System.arraycopy(recordFormatCTLLName_, 0, ctll, 3, recordFormatCTLLName_.length);  // @A2A //@C0C

        ctll[13] = 0xf;
        BinaryConverter.shortToByteArray((short) 2, ctll, 14);
        BinaryConverter.shortToByteArray((short) 0, ctll, 16);
        ctll[18] = 2;
        BinaryConverter.shortToByteArray((short) 4, ctll, 19);
        BinaryConverter.intToByteArray(index, ctll, 21);
        ctll[25] = (byte) 0xff;

        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // GETD.
            data =
              new BytesWithOffset(getdForPosition(handle_, optl, ctll,
                                                  openFeedback_.getRecordIncrement() *
                                                  blockingFactor_)); //@C0C
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Parse the I/O feedback data.
        LocalIOFB iofb = new LocalIOFB(data.data_, data.offset_);
        data.offset_ += 14;

        // Parse the record data.
        Record[] records = parseRecordData(iofb, data);

        return (records.length == 0)? null : records[0];
    }

    /**
     *Positions the cursor to the first record in the file that matches the
     *specified key.
     *@param key the key
     *@param searchType the way to compare keys
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record positionCursorToKey(Object[] keys,
                                      int searchType)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        // OPTL
        // byte share = (as400File_.openType_ == AS400File.READ_ONLY ?  // @A2D
        //               SHR_READ_NORM : SHR_UPD_NORM);                 // @A2D

        // Start of @A2A
        byte share;
        if ((openType_ == AS400File.READ_ONLY) ||  //@C0C
            ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) //@C0C
        { // Read only
            share = SHR_READ_NORM;
        }
        else
        { // READ_WRITE; get for update
            share = SHR_UPD_NORM;
        }
        // End of @A2A

        byte[] optl = { (byte)searchType, share, DATA_DTA_DTARCD, OPER_GETK };

        // Determine the total length of all data in keyFields.
        FieldDescription description;
        ByteArrayOutputStream keyAsBytes = new ByteArrayOutputStream();
        //    Converter conv;      // @A1D
        byte[] fieldAsBytes;  // Will contain each key field's data as bytes
        byte[] lengthBytes = new byte[2]; // Used for variable length fields
        for (int i = 0; i < keys.length; i++)
        {
            try
            {
                // Convert each key field to AS400 data writing it to keyAsBytes
                description = recordFormat_.getKeyFieldDescription(i); //@C0C

                // Check if field is a variable length field.  This means that the field
                // is either a hex field or a character (DNCS* included) field.
                // The field has the potential of being variable length, but may be
                // fixed length.  We account for both cases in this if.
                if (description instanceof VariableLengthFieldDescription)
                {
                    boolean varLength = ((VariableLengthFieldDescription)description).isVariableLength();
                    if (description instanceof HexFieldDescription)
                    { // Hex field
                        if (varLength)
                        {
                            // Need to write two bytes of length info prior to writing the data
                            BinaryConverter.shortToByteArray((short)((byte[])keys[i]).length, lengthBytes, 0);
                            keyAsBytes.write(lengthBytes, 0, lengthBytes.length);
                        }
                        keyAsBytes.write((byte[])keys[i], 0, ((byte[])keys[i]).length);
                        if (varLength)
                        {
                            // We need to send the maximum field length number of bytes for the
                            // field, even though only keyFields[i].length bytes of the data will
                            // be looked at by DDM
                            int fieldLength = description.getDataType().getByteLength();
                            for (int j = ((byte[])keys[i]).length; j < fieldLength; ++j)
                            {
                                keyAsBytes.write(0);
                            }
                        }
                    }
                    else
                    { // Character field
                        // Use Converter object to translate the key field passed in.  If
                        // we use the data type associated with the field description, the data
                        // for the key field will be padded with blanks to the byteLength of
                        // the field description.  This can cause a match of the key to occur in the
                        // case that the user specifies a value that does not exactly match the key
                        // field in the record.
                        if (varLength)
                        {
                            // Need to write two bytes of length info prior to writing the
                            // data
                            BinaryConverter.shortToByteArray((short)((String)keys[i]).length(), lengthBytes, 0);
                            keyAsBytes.write(lengthBytes, 0, lengthBytes.length);
                        }

                        // @A1D
                        // Modified code to use the AS400Text object to do the conversion.
                        //            conv = Converter.getConverter(((AS400Text)description.getDataType()).getCcsid());    // @A1D
                        //            fieldAsBytes = conv.stringToByteArray((String)keys[i]);                              // @A1D

                        fieldAsBytes = description.getDataType().toBytes(keys[i]);                             // @A1A

                        keyAsBytes.write(fieldAsBytes, 0, fieldAsBytes.length);

                        // We need to get rid of this now since AS400Text does the padding for us.
                        // @A1D
                        /*
                         if (varLength)
                         {
                         // We need to send the maximum field length number of bytes for the
                         // field, even though only keyFields[i].length bytes of the data will
                         // be looked at by DDM
                         int fieldLength = description.getDataType().getByteLength();
                         for (int j = ((String)keys[i]).length(); j < fieldLength; ++j)
                         {
                         keyAsBytes.write(0x40);
                         }
                         }
                         */
                    }
                }
                else
                {
                    // Numeric field
                    fieldAsBytes = description.getDataType().toBytes(keys[i]);
                    keyAsBytes.write(fieldAsBytes, 0, fieldAsBytes.length);
                }
            }
            catch(NullPointerException e)
            {
                // One of the key fields was null
                throw new ExtendedIllegalArgumentException("key", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
        }
        int keyLength = keyAsBytes.size();

        // CTLL
        // ----------------------------------
        // Offset Description
        // ----------------------------------
        // 0      record format ID
        // 1      record format length
        // 3      record format name
        // 13     member number ID
        // 14     member number length
        // 16     member number value
        // 18     number of fields ID
        // 19     number of fields length
        // 21     number of fields value
        // 25     key fields ID
        // 26     key fields length,
        // 28     key field values
        // ??     control list end
        byte[] ctll = new byte[29 + keyLength];
        ctll[0] = 1;
        BinaryConverter.shortToByteArray((short) 35, ctll, 1);

        // Start of @A2D
        /*
         StringBuffer recordName =
         new StringBuffer(as400File_.recordFormat_.getName());
         while (recordName.length() < 10) recordName.append(' ');
         converter_.stringToByteArray(recordName.toString(), ctll, 3);
         */
        // End of @A2D

        System.arraycopy(recordFormatCTLLName_, 0, ctll, 3, recordFormatCTLLName_.length);  // @A2A //@C0C

        ctll[13] = 0xf;
        BinaryConverter.shortToByteArray((short) 2, ctll, 14);
        BinaryConverter.shortToByteArray((short) 0, ctll, 16);
        ctll[18] = 8;
        BinaryConverter.shortToByteArray((short) 4, ctll, 19);
        BinaryConverter.intToByteArray(keys.length, ctll, 21);
        ctll[25] = 7;
        BinaryConverter.shortToByteArray((short) keyLength, ctll, 26);
        System.arraycopy(keyAsBytes.toByteArray(), 0, ctll, 28, keyLength);
        ctll[28 + keyLength] = (byte) 0xff;

        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // GETK
            data =
              new BytesWithOffset(getkForPosition(handle_, optl, ctll,
                                                  openFeedback_.getRecordIncrement() *
                                                  blockingFactor_)); //@C0C
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Parse the I/O feedback data.
        // LocalIOFB iofb = new LocalIOFB(data.data_, data.offset_);  // @A2D
        // data.offset_ += 14;  // @A2D

        // Parse the record data.
        // Record[] records = parseRecordData(iofb, data);  // @A2D

        // return (records.length == 0)? null : records[0];  // @A2D
        return null;
    }


    // @A2A
    /**
     *Positions the cursor to the first record in the file that matches the
     *specified key.
     *@param key the key
     *@param searchType the way to compare keys
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record positionCursorToKey(byte[] keys,
                                      int searchType, int numberOfKeyFields)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        // OPTL
        // byte share = (as400File_.openType_ == AS400File.READ_ONLY ?
        //              SHR_READ_NORM : SHR_UPD_NORM);

        // Start of @A2A
        byte share;
        if ((openType_ == AS400File.READ_ONLY) ||  //@C0C
            ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) //@C0C
        { // Read only
            share = SHR_READ_NORM;
        }
        else
        { // READ_WRITE; get for update
            share = SHR_UPD_NORM;
        }
        // End of @A2A

        byte[] optl = { (byte)searchType, share, DATA_DTA_DTARCD, OPER_GETK };

        // Determine the total length of all data in keyFields.
        FieldDescription description;
        int keyLength = keys.length;

        // CTLL
        // ----------------------------------
        // Offset Description
        // ----------------------------------
        // 0      record format ID
        // 1      record format length
        // 3      record format name
        // 13     member number ID
        // 14     member number length
        // 16     member number value
        // 18     number of fields ID
        // 19     number of fields length
        // 21     number of fields value
        // 25     key fields ID
        // 26     key fields length,
        // 28     key field values
        // ??     control list end
        byte[] ctll = new byte[29 + keyLength];
        ctll[0] = 1;
        BinaryConverter.shortToByteArray((short) 35, ctll, 1);

        // Start of @A2D
        /*
         StringBuffer recordName =
         new StringBuffer(as400File_.recordFormat_.getName());
         while (recordName.length() < 10) recordName.append(' ');
         converter_.stringToByteArray(recordName.toString(), ctll, 3);
         */
        // End of @A2D

        System.arraycopy(recordFormatCTLLName_, 0, ctll, 3, recordFormatCTLLName_.length);  // @A2A //@C0C

        ctll[13] = 0xf;
        BinaryConverter.shortToByteArray((short) 2, ctll, 14);
        BinaryConverter.shortToByteArray((short) 0, ctll, 16);
        ctll[18] = 8;
        BinaryConverter.shortToByteArray((short) 4, ctll, 19);
        BinaryConverter.intToByteArray(numberOfKeyFields, ctll, 21);
        ctll[25] = 7;
        BinaryConverter.shortToByteArray((short) keyLength, ctll, 26);
        System.arraycopy(keys, 0, ctll, 28, keyLength);
        ctll[28 + keyLength] = (byte) 0xff;

        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // GETK
            data =
              new BytesWithOffset(getkForPosition(handle_, optl, ctll,
                                                  openFeedback_.getRecordIncrement() *
                                                  blockingFactor_)); //@C0C
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Parse the I/O feedback data.
        // LocalIOFB iofb = new LocalIOFB(data.data_, data.offset_);  // @A2D
        // data.offset_ += 14;  // @A2D

        // Parse the record data.
        // Record[] records = parseRecordData(iofb, data);  // @A2D

        // return (records.length == 0)? null : records[0];  // @A2D
        return null;
    }



    /**
     *Write records to the file.
     *@param handle the file handle.
     *@param optl option list
     *@param ctll control list
     *@param data record data
     *@return message feedback data, I/O feedback data
     **/
    native void put(int handle,
                    byte[] optl,
                    byte[] ctll,
                    byte[] data)
      throws NativeException;

    /**
     Reads the record at the specified file position.
     @param index the file position
     @return the record read.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record read(int index)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        if (cacheRecords_) //@C0A
            return super.read(index); //@C0A

        // OPTL
        // byte share = (as400File_.openType_ == AS400File.READ_ONLY ?  // @A2D
        //               SHR_READ_NORM : SHR_UPD_NORM);                 // @A2D

        // Start of @A2A
        byte share;
        if ((openType_ == AS400File.READ_ONLY) ||  //@C0C
            ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) //@C0C
        { // Read only
            share = SHR_READ_NORM;
        }
        else
        { // READ_WRITE; get for update
            share = SHR_UPD_NORM;
        }
        // End of @A2A

        byte[] optl = { TYPE_GETD_ABSRRN, share, DATA_DTA_DTARCD, OPER_GETD };

        // CTLL
        // ----------------------------------
        // Offset Description
        // ----------------------------------
        // 0      record format ID
        // 1      record format length
        // 3      record format name
        // 13     member number ID
        // 14     member number length
        // 16     member number value
        // 18     relative record number ID
        // 19     relative record number length
        // 21     relative record number
        // 25     control list end
        byte[] ctll = new byte[26];
        ctll[0] = 1;
        BinaryConverter.shortToByteArray((short) 35, ctll, 1);

        // Start of @A2D
        /*
         StringBuffer recordName =
         new StringBuffer(as400File_.recordFormat_.getName());
         while (recordName.length() < 10) recordName.append(' ');
         converter_.stringToByteArray(recordName.toString(), ctll, 3);
         */
        // End of @A2D

        System.arraycopy(recordFormatCTLLName_, 0, ctll, 3, recordFormatCTLLName_.length);  // @A2A //@C0C

        ctll[13] = 0xf;
        BinaryConverter.shortToByteArray((short) 2, ctll, 14);
        BinaryConverter.shortToByteArray((short) 0, ctll, 16);
        ctll[18] = 2;
        BinaryConverter.shortToByteArray((short) 4, ctll, 19);
        BinaryConverter.intToByteArray(index, ctll, 21);
        ctll[25] = (byte) 0xff;

        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // GETD
            data =
              new BytesWithOffset(getdForRead(handle_, optl, ctll,
                                              openFeedback_.getRecordIncrement() *
                                              blockingFactor_)); //@C0C
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Parse the I/O feedback data.
        LocalIOFB iofb = new LocalIOFB(data.data_, data.offset_);
        data.offset_ += 14;

        // Parse the record data.
        Record[] records = parseRecordData(iofb, data);

        return (records.length == 0)? null : records[0];
    }

    /**
     *Reads the first record with the specified key based on the specified search type.
     *@param key The values that make up the key with which to find the record.
     *@param type The type of read.  This value is one of the TYPE_GETKEY_* constants.
     *@return The record read.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record read(Object[] key,
                       int searchType)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        // OPTL
        // byte share = (as400File_.openType_ == AS400File.READ_ONLY ?  // @A2D
        //               SHR_READ_NORM : SHR_UPD_NORM);                 // @A2D

        // Start of @A2A
        byte share;
        if ((openType_ == AS400File.READ_ONLY) ||  //@C0C
            ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) //@C0C
        { // Read only
            share = SHR_READ_NORM;
        }
        else
        { // READ_WRITE; get for update
            share = SHR_UPD_NORM;
        }
        // End of @A2A

        byte[] optl = { (byte)searchType, share, DATA_DTA_DTARCD, OPER_GETK };

        // Determine the total length of all data in keyFields.
        FieldDescription description;
        ByteArrayOutputStream keyAsBytes = new ByteArrayOutputStream();
        //    Converter conv;         // @A1D
        byte[] fieldAsBytes;  // Will contain each key field's data as bytes
        byte[] lengthBytes = new byte[2]; // Used for variable length fields
        for (int i = 0; i < key.length; i++)
        {
            try
            {
                // Convert each key field to AS400 data writing it to keyAsBytes
                description = recordFormat_.getKeyFieldDescription(i); //@C0C

                // Check if field is a variable length field.  This means that the field
                // is either a hex field or a character (DNCS* included) field.
                // The field has the potential of being variable length, but may be
                // fixed length.  We account for both cases in this if.
                if (description instanceof VariableLengthFieldDescription)
                {
                    boolean varLength = ((VariableLengthFieldDescription)description).isVariableLength();
                    if (description instanceof HexFieldDescription)
                    { // Hex field
                        if (varLength)
                        {
                            // Need to write two bytes of length info prior to writing the data
                            BinaryConverter.shortToByteArray((short)((byte[])key[i]).length, lengthBytes, 0);
                            keyAsBytes.write(lengthBytes, 0, lengthBytes.length);
                        }
                        keyAsBytes.write((byte[])key[i], 0, ((byte[])key[i]).length);
                        if (varLength)
                        {
                            // We need to send the maximum field length number of bytes for the
                            // field, even though only keyFields[i].length bytes of the data will
                            // be looked at by DDM
                            int fieldLength = description.getDataType().getByteLength();
                            for (int j = ((byte[])key[i]).length; j < fieldLength; ++j)
                            {
                                keyAsBytes.write(0);
                            }
                        }
                    }
                    else
                    { // Character field
                        // Use Converter object to translate the key field passed in.  If
                        // we use the data type associated with the field description, the data
                        // for the key field will be padded with blanks to the byteLength of
                        // the field description.  This can cause a match of the key to occur in the
                        // case that the user specifies a value that does not exactly match the key
                        // field in the record.
                        if (varLength)
                        {
                            // Need to write two bytes of length info prior to writing the
                            // data
                            BinaryConverter.shortToByteArray((short)((String)key[i]).length(), lengthBytes, 0);
                            keyAsBytes.write(lengthBytes, 0, lengthBytes.length);
                        }
                        //            conv = Converter.getConverter(((AS400Text)description.getDataType()).getCcsid());   // @A1D
                        //            fieldAsBytes = conv.stringToByteArray((String)key[i]);                              // @A1D

                        fieldAsBytes = description.getDataType().toBytes(key[i]);                             // @A1A

                        keyAsBytes.write(fieldAsBytes, 0, fieldAsBytes.length);

                        // @A1D
                        /*
                         if (varLength)
                         {
                         // We need to send the maximum field length number of bytes for the
                         // field, even though only keyFields[i].length bytes of the data will
                         // be looked at by DDM
                         int fieldLength = description.getDataType().getByteLength();
                         for (int j = ((String)key[i]).length(); j < fieldLength; ++j)
                         {
                         keyAsBytes.write(0x40);
                         }
                         }
                         */
                    }
                }
                else
                {
                    // Numeric field
                    fieldAsBytes = description.getDataType().toBytes(key[i]);
                    keyAsBytes.write(fieldAsBytes, 0, fieldAsBytes.length);
                }
            }
            catch(NullPointerException e)
            {
                // One of the key fields was null
                throw new ExtendedIllegalArgumentException("key", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
        }
        int keyLength = keyAsBytes.size();

        // CTLL
        // ----------------------------------
        // Offset Description
        // ----------------------------------
        // 0      record format ID
        // 1      record format length
        // 3      record format name
        // 13     member number ID
        // 14     member number length
        // 16     member number value
        // 18     number of fields ID
        // 19     number of fields length
        // 21     number of fields value
        // 25     key fields ID
        // 26     key fields length,
        // 28     key field values
        // ??     control list end
        byte[] ctll = new byte[29 + keyLength];
        ctll[0] = 1;
        BinaryConverter.shortToByteArray((short) 35, ctll, 1);

        // Start of @A2D
        /*
         StringBuffer recordName =
         new StringBuffer(as400File_.recordFormat_.getName());
         while (recordName.length() < 10) recordName.append(' ');
         converter_.stringToByteArray(recordName.toString(), ctll, 3);
         */
        // End of @A2D

        System.arraycopy(recordFormatCTLLName_, 0, ctll, 3, recordFormatCTLLName_.length);  // @A2A //@C0C

        ctll[13] = 0xf;
        BinaryConverter.shortToByteArray((short) 2, ctll, 14);
        BinaryConverter.shortToByteArray((short) 0, ctll, 16);
        ctll[18] = 8;
        BinaryConverter.shortToByteArray((short) 4, ctll, 19);
        BinaryConverter.intToByteArray(key.length, ctll, 21);
        ctll[25] = 7;
        BinaryConverter.shortToByteArray((short) keyLength, ctll, 26);
        System.arraycopy(keyAsBytes.toByteArray(), 0, ctll, 28, keyLength);
        ctll[28 + keyLength] = (byte) 0xff;

        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // GETK.
            data =
              new BytesWithOffset(getkForRead(handle_, optl, ctll,
                                              openFeedback_.getRecordIncrement() *
                                              blockingFactor_)); //@C0C
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Parse the I/O feedback data.
        LocalIOFB iofb = new LocalIOFB(data.data_, data.offset_);
        data.offset_ += 14;

        // Parse the record data.
        Record[] records = parseRecordData(iofb, data);

        if (cacheRecords_) //@C0A
        {                  //@C0A
            cache_.setIsEmpty(); //@C0A
        }                  //@C0A

        return (records.length == 0)? null : records[0];
    }


    // @A2A
    /**
     *Reads the first record with the specified key based on the specified search type.
     *@param key The values that make up the key with which to find the record.
     *@param type The type of read.  This value is one of the TYPE_GETKEY_* constants.
     *@return The record read.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record read(byte[] key,
                       int searchType, int numberOfKeyFields)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        // OPTL
        // byte share = (as400File_.openType_ == AS400File.READ_ONLY ?
        //               SHR_READ_NORM : SHR_UPD_NORM);

        // Start of @A2A
        byte share;
        if ((openType_ == AS400File.READ_ONLY) ||  //@C0C
            ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) //@C0C
        { // Read only
            share = SHR_READ_NORM;
        }
        else
        { // READ_WRITE; get for update
            share = SHR_UPD_NORM;
        }
        // End of @A2A

        byte[] optl = { (byte)searchType, share, DATA_DTA_DTARCD, OPER_GETK };

        // Determine the total length of all data in keyFields.
        FieldDescription description;
        int keyLength = key.length;

        // CTLL
        // ----------------------------------
        // Offset Description
        // ----------------------------------
        // 0      record format ID
        // 1      record format length
        // 3      record format name
        // 13     member number ID
        // 14     member number length
        // 16     member number value
        // 18     number of fields ID
        // 19     number of fields length
        // 21     number of fields value
        // 25     key fields ID
        // 26     key fields length,
        // 28     key field values
        // ??     control list end
        byte[] ctll = new byte[29 + keyLength];
        ctll[0] = 1;
        BinaryConverter.shortToByteArray((short) 35, ctll, 1);

        // Start of @A2D
        /*
         StringBuffer recordName =
         new StringBuffer(as400File_.recordFormat_.getName());
         while (recordName.length() < 10) recordName.append(' ');
         converter_.stringToByteArray(recordName.toString(), ctll, 3);
         */
        // Start of @A2D

        System.arraycopy(recordFormatCTLLName_, 0, ctll, 3, recordFormatCTLLName_.length);  // @A2A //@C0C

        ctll[13] = 0xf;
        BinaryConverter.shortToByteArray((short) 2, ctll, 14);
        BinaryConverter.shortToByteArray((short) 0, ctll, 16);
        ctll[18] = 8;
        BinaryConverter.shortToByteArray((short) 4, ctll, 19);
        BinaryConverter.intToByteArray(numberOfKeyFields, ctll, 21);
        ctll[25] = 7;
        BinaryConverter.shortToByteArray((short) keyLength, ctll, 26);
        System.arraycopy(key, 0, ctll, 28, keyLength);
        ctll[28 + keyLength] = (byte) 0xff;

        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // GETK.
            data =
              new BytesWithOffset(getkForRead(handle_, optl, ctll,
                                              openFeedback_.getRecordIncrement() *
                                              blockingFactor_)); //@C0C
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Parse the I/O feedback data.
        LocalIOFB iofb = new LocalIOFB(data.data_, data.offset_);
        data.offset_ += 14;

        // Parse the record data.
        Record[] records = parseRecordData(iofb, data);

        if (cacheRecords_) //@C0A
        {                  //@C0A
            cache_.setIsEmpty(); //@C0A
        }                  //@C0A

        return (records.length == 0)? null : records[0];
    }


    /**
     *Reads all the records in the file.
     *@param fileType The type of file.  Valid values are: key or seq
     *@return The records read.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record[] readAll(String fileType, int bf) //@C1C
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        // Open the file with a blocking factor of 100.
        openFile2(AS400File.READ_ONLY, bf, AS400File.COMMIT_LOCK_LEVEL_NONE, fileType); //@C0C @C1C

        Record[] records = null;
        try
        {
            // Determine how many records are in the file.
            records = new Record[openFeedback_.getNumberOfRecords()];

            // Read the entire file contents one record at a time.
            for (int i = 0; i < records.length; i++)
            {
                records[i] = readNext(); //@C0C
            }
        }
        finally
        {
            // Close the file we opened.
            try
            {
                close(); //@C0C
            }
            catch(AS400Exception e)
            {
                resetState(); //@C0C
                throw e;
            }
            catch(AS400SecurityException e)
            {
                resetState(); //@C0C
                throw e;
            }
            catch(InterruptedException e)
            {
                resetState(); //@C0C
                throw e;
            }
            catch(IOException e)
            {
                resetState(); //@C0C
                throw e;
            }
        }

        return records;
    }

    /**
     *Reads the record at the current file position.
     *@param type type of read (first, last, next, previous)
     *@return the record read.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record readRecord(int type)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        // OPTL
        // byte share = (as400File_.openType_ == AS400File.READ_ONLY ?  // @A2D
        //               SHR_READ_NORM : SHR_UPD_NORM);                 // @A2D

        // Start of @A2A
        byte share;
        if ((openType_ == AS400File.READ_ONLY) ||  //@C0C
            ((openType_ == AS400File.READ_WRITE) && readNoUpdate_)) //@C0C
        { // Read only
            share = SHR_READ_NORM;
        }
        else
        { // READ_WRITE; get for update
            share = SHR_UPD_NORM;
        }
        // End of @A2A

        byte[] optl = { (byte) type, share, DATA_DTA_DTARCD, OPER_GET };
        byte[] ctll = new byte[6];
        ctll[0] = 0x10; // ID for number of records
        // Length of value field
        BinaryConverter.shortToByteArray((short) 2, ctll, 1);
        // Number of records to get
        BinaryConverter.shortToByteArray((short) blockingFactor_, ctll, 3); //@C0C
        ctll[5] = (byte)0xFF;  // End of control list

        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // GET
            data =
              new BytesWithOffset(getForRead(handle_, optl, ctll,
                                             openFeedback_.getRecordIncrement() *
                                             blockingFactor_)); //@C0C
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Parse the I/O feedback data.
        LocalIOFB iofb = new LocalIOFB(data.data_, data.offset_);
        data.offset_ += 14;

        // Parse the record data.
        Record[] records = parseRecordData(iofb, data);

        return (records.length == 0)? null : records[0];
    }

    /**
     *Reads records from the file.  The next or previous 'blockingFactor_'
     *records are retrieved depending on the direction specified.
     *@param direction (DDMRecordCache.FORWARD or DDMRecordCache.BACKWARD)
     *@return the records read
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public Record[] readRecords(int direction)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        // OPTL
        byte type = (direction == DDMRecordCache.FORWARD ? TYPE_GET_NEXT :
                     TYPE_GET_PREV);
        byte[] optl = { type, SHR_READ_NORM, DATA_DTA_DTARCD, OPER_GET };
        byte[] ctll = new byte[6];
        ctll[0] = 0x10; // ID for number of records
        // Length of value field
        BinaryConverter.shortToByteArray((short) 2, ctll, 1);
        // Number of records to get
        BinaryConverter.shortToByteArray((short) blockingFactor_, ctll, 3); //@C0C
        ctll[5] = (byte)0xFF;  // End of control list

        // GET.
        int length = openFeedback_.getRecordIncrement() *
          blockingFactor_; //@C0C
        BytesWithOffset data = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            data =
              new BytesWithOffset(getForRead(handle_, optl, ctll, length));
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        // Parse the I/O feedback data.
        LocalIOFB iofb = new LocalIOFB(data.data_, data.offset_);
        data.offset_ += 14;

        // Parse the record data.
        Record[] records = parseRecordData(iofb, data);

        return records;
    }

    //@E3A
    /**
     * Used to reset the static hashtable of file handles that is
     * stored in the native code. We do this now because the
     * service program does not get reactivated when
     * another JVM is instantiated (as of the release after v4r5).
     **/
    private static native void resetStaticStorage();

    /**
     *Rolls back any transactions since the last commit/rollback boundary.  Invoking this
     *method will cause all transactions under commitment control for this connection
     *to be rolled back.  This means that any AS400File object for which a commit
     *lock level was specified and that was opened under this connection will have
     *outstanding transactions rolled back.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public void rollback()
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // Rollback.
            rollbackNtv();
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    /**
     *Rollback changes to files under commitment control.
     *@return message feedback data.
     **/
    native void rollbackNtv()
      throws NativeException;

    /**
     *Updates the specified record.
     *@param handle the file handle
     *@param optl option list
     *@param ctll control list
     *@param recordData record data
     *@return message feedback data, I/O feedback
     **/
    native void updat(int handle,
                      byte[] optl,
                      byte[] ctll,
                      byte[] recordData)
      throws NativeException;

    /**
     *Updates the record at the current cursor position.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public void update(Record record)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
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

        // OPTL
        byte[] optl = { TYPE_GET_SAME, SHR_UPD_NORM, DATA_DTA_DTARCD, OPER_UPDATE };

        // Create the record data to be sent
        int recordIncrement = openFeedback_.getRecordIncrement();
        byte[] recordData = new byte[recordIncrement];
        System.arraycopy(record.getContents(), 0, recordData, 0, record.getRecordLength());
        int numFields = record.getNumberOfFields();
        int fieldOffset = recordIncrement - numFields;
        for (int i = 0; i < numFields; ++i, ++fieldOffset)
        {
            recordData[fieldOffset] = (record.isNullField(i))? (byte)0xF1 : (byte)0xF0;
        }

        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // UPDAT
            updat(handle_, optl, null, recordData);
        }
        catch(NativeException e)
        {
            // Parse the message feedback data and throw AS400Exception
            throw new AS400Exception(parseMsgFeedback(e.data));
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    /**
     *Writes an array of records to the end of the file.
     *The cursor is positioned to after the last record of the file as a result
     *of invoking this method.
     *@param records The records to write.  The records must have a format
     *which matches the record format of this object.  To ensure that this
     *requirement is met, use the
     *<a href="RecordFormat.html">RecordFormat.getNewRecord()</a>
     *method to obtain default records whose fields can be set appropriately by
     *the Java program and then written to the file.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public void write(Record[] records)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
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

        // PUT the record data to the file, blocking factor records at a time.
        int recordIncrement = openFeedback_.getRecordIncrement();
        byte[] recordData =
          new byte[(records.length < blockingFactor_ ? //@C0C
                    records.length : blockingFactor_) * //@C0C
                   recordIncrement];
        for (int offset = 0, r = 1; r <= records.length; r++)
        {
            // Copy the record data to the record data buffer.
            byte[] record = records[r-1].getContents();
            System.arraycopy(record, 0, recordData, offset, record.length);

            // Write the null field byte map array after the record data.  It
            // immediately preceeds the next record. 0xf1 = null, 0xf0 != null
            // There may be a gap between the end of the record data and the
            // start of the null field byte map.
            int numFields = records[r-1].getNumberOfFields();
            for (int f = 0, fieldOffset = offset +
                 (recordIncrement - numFields); f < numFields; fieldOffset++, f++)
            {
                recordData[fieldOffset] =
                  (records[r-1].isNullField(f) ? (byte) 0xf1 : (byte) 0xf0);
            }

            // If we've accumulated the data from blocking factor records or
            // have added data from the last record, PUT the record data.
            if (r == records.length && r > blockingFactor_ && r % blockingFactor_ != 0) //@C0C
            {
                byte[] dataToWrite = new byte[offset + recordIncrement];
                System.arraycopy(recordData, 0, dataToWrite, 0, dataToWrite.length);
                byte[] swapToPH = new byte[12];
                byte[] swapFromPH = new byte[12];
                boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
                try
                {
                    // PUT
                    put(handle_, null, null, dataToWrite);
                }
                catch(NativeException e)
                {
                    // Parse the message feedback data and throw AS400Exception
                    throw new AS400Exception(parseMsgFeedback(e.data));
                }
                finally
                {
                    if (didSwap) system_.swapBack(swapToPH, swapFromPH);
                }
            }
            else if (r == records.length || (r % blockingFactor_ == 0)) //@C0C
            {
                byte[] swapToPH = new byte[12];
                byte[] swapFromPH = new byte[12];
                boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
                try
                {
                    // PUT
                    put(handle_, null, null, recordData);
                }
                catch(NativeException e)
                {
                    // Parse the message feedback data and throw AS400Exception
                    throw new AS400Exception(parseMsgFeedback(e.data));
                }
                finally
                {
                    if (didSwap) system_.swapBack(swapToPH, swapFromPH);
                }

                // Reset the offset.
                offset = 0;
            }
            else
            {
                // Increment the offset.
                offset += recordIncrement;
            }
        }
    }
}




