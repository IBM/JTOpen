///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserSpaceImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 The UserSpaceImplRemote is the remote implementation of the user space class.
 **/
class UserSpaceImplRemote extends UserSpaceImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     Force to Auxiliary Storage option that allow changes to be forced asynchronously.
     **/
    private static int FORCE_ASYNCHRONOUS = 1;

    /**
     Force to Auxiliary Storage option that does not allow changes to be forced.  It uses normal system writes.
     **/
    private static int FORCE_NONE = 0;

    /**
     Force to Auxiliary Storage option that allow changes to be forced synchronously.
     **/
    private static int FORCE_SYNCHRONOUS = 2;

    private int length_;                        // The size of the user space.
    private byte initialValue_;                 // The initial value for the future extension.
    private char autoExtend_;                   // The automatic extension value.
    private boolean replace_;                   // The object replace option, used if re-create is attempted.

    private RemoteCommandImplRemote rmtCmd_;  // Impl object for remote command server.
    //  delete, getAttributes, setAttributes
    private IFSRandomAccessFileImplRemote aUserSpace_;    // The integrated file system object used   $C0C
    //  for read and write.

    /**
     Closes the user space's random access file stream and releases any system resources associated with the stream.
     **/
    void close() throws IOException       // $B2
    {
        if (mustUseProgramCall_)           // E1a close only if using IFS to read/write
        {                                  // E1a
            if (Trace.isTraceOn())          // E1a
                Trace.log(Trace.INFORMATION, "Close ignored since using ProgramCall."); // E1a
            return;                         // E1a
        }                                  // E1a

        if (aUserSpace_ == null) {
            Trace.log(Trace.ERROR, "User space is not open.");
            throw new ExtendedIllegalStateException("user space",
                                                    ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
        }
        aUserSpace_.close();                      // close the random access file stream.
        aUserSpace_ = null;                       // clear the file connection info.
    }

    /**
     Creates a user space.

     @param domain  The domain into which the user space is created.
     Valid value are: *DEFAULT, *USER, or *SYSTEM.
     *DEFAULT uses the allow user domain system value to determine if *USER or *SYSTEM will be used.
     @param length  The initial size in bytes of the user space.
     Valid values are 1 through 16,776,704.
     @param replace The value indicating if an existing user space is to be replaced.
     @param extendedAttribute  The user-defined extended attribute of the user space.  This string must be 10 characters or less.
     @param initialValue  The value used in creation and extension.
     @param textDescription  The text describing the user space.  This string must be 50 characters or less.
     @param authority  The public authority for the user space.  This string must be 10 characters or less.
     Valid values are:
     <ul>
     <li>*ALL
     <li>*CHANGE
     <li>*EXCLUDE
     <li>*LIBCRTAUT
     <li>*USE
     <li>authorization-list name
     </ul>
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    void create(String domain,
                int length,
                boolean replace,
                String extendedAttribute,
                byte initialValue,
                String textDescription,
                String authority)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        // Close the user space if an existing object exists and the existing object had replace=true.
        if (aUserSpace_ != null && replace_ == true) {
            aUserSpace_.close();
        }

        // Validate the replace parameter
        String replaceString = (replace) ? "*YES":"*NO";

        // **** Setup the parameter list ****
        ProgramParameter[] parmlist = new ProgramParameter[9];

        // First parameter: input, is the user space name
        parmlist[0] = new ProgramParameter(userSpaceSystemPathName_);

        // Second parameter: input, is the extended attribute      @D1c
        // @C1D byte[] extendAttr = initializeByteArray(10);
        // @C1D if (extendedAttribute == null) {
        // @C1D    System.out.println("extendedAttribute is NULL");
        // @C1D }
        // @C1D if (extendAttr == null) {
        // @C1D    System.out.println("extendAttr is NULL");
        // @C1D }
        byte[] extendAttr = padByteArray(converter_.stringToByteArray(extendedAttribute), 10);         // @C1C
        parmlist[1] = new ProgramParameter(extendAttr);

        // Third parameter: input, is the initial size of the user space
        byte[] initialLength = new byte[4];
        BinaryConverter.intToByteArray(length, initialLength, 0);
        parmlist[2] = new ProgramParameter(initialLength);

        // Fourth parameter: input, is the initial value used after extension
        byte[] initialByte = new byte[1];
        initialByte[0] = initialValue;
        parmlist[3] = new ProgramParameter(initialByte);

        // Fifth parameter: input, is the public authority         @D1c
        // @C1D byte[] publicAuth = initializeByteArray(10);
        byte[] publicAuth = padByteArray(converter_.stringToByteArray(authority), 10);                 // @C1C
        parmlist[4] = new ProgramParameter(publicAuth);

        // Sixth parameter: input, is the Text Description         @D1c
        // @C1D byte[] description = initializeByteArray(50);
        byte[] description = padByteArray(converter_.stringToByteArray(textDescription), 50);          // @C1C
        parmlist[5] = new ProgramParameter(description);

        // Seventh parameter: input, is the Replace Attribute      @D1c
        // @C1D byte[] replaceAttr = initializeByteArray(10);
        byte[] replaceAttr = padByteArray(converter_.stringToByteArray(replaceString), 10);            // @C1C
        parmlist[6] = new ProgramParameter(replaceAttr);

        // Eighth parameter: input/output, is the error code array
        byte[] errorInfo = new byte[32];
        parmlist[7] = new ProgramParameter( errorInfo, 0 );

        // Ninth parameter: input, is the domain                   @D1c
        // @C1D byte[] domainAttr = initializeByteArray(10);
        byte[] domainAttr = padByteArray(converter_.stringToByteArray(domain), 10);                    // @C1C
        parmlist[8] = new ProgramParameter(domainAttr);

        // Create the pgm call object
        rmtCmd_ = new RemoteCommandImplRemote();                             //$C0C
        rmtCmd_.setSystem(system_);                                        //$C0A
        //try                                                               //$C0D
        //{
        //   pgmCall_.setProgram( "/QSYS.LIB/QUSCRTUS.PGM", parmlist );
        //}
        //catch (PropertyVetoException v) {}       // $B1

        // Run the program.  Failure is returned as a message list.
        if(rmtCmd_.runProgram("QSYS", "QUSCRTUS", parmlist, true) != true)  // This API is threadsafe.  @D3A $C0C @D2C
        {
            // Throw AS400MessageList
            AS400Message[] messageList = rmtCmd_.getMessageList();
            for (int msg = 0; msg < messageList.length; msg++)
                throw new IOException(messageList[msg].toStringM2());
        }

        // Construct the user space file object.
        open();

        // Set the objects replace value for future reference.
        replace_ = replace;
    }


    /**
     Deletes a user space.

     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    void delete()
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        // Close the user space
        if (aUserSpace_ != null) {
            aUserSpace_.close();
            aUserSpace_ = null;         // clear the file connection info.
        }

        // Setup the parameter list
        ProgramParameter[] parmlist = new ProgramParameter[2];

        // First parameter: input, is the user space name
        parmlist[0] = new ProgramParameter(userSpaceSystemPathName_);

        // Second parameter: input/output, is the error code array
        byte[] errorlist = new byte[32];
        parmlist[1] = new ProgramParameter(errorlist, 0);

        rmtCmd_ = new RemoteCommandImplRemote();                             //$C0C
        rmtCmd_.setSystem(system_);                                        //$C0A
        //try
        //{
        //   pgmCall_.setProgram("/QSYS.LIB/QUSDLTUS.PGM", parmlist );
        //}
        //catch (PropertyVetoException v) {}       // $B1

        if (rmtCmd_.runProgram("QSYS", "QUSDLTUS", parmlist, true) != true)  // This API is threadsafe.  @D3A $C0C @D2C
        {
            // failure occurred, throw AS400Message list
            AS400Message[] messageList = rmtCmd_.getMessageList();
            for (int msg = 0; msg < messageList.length; msg++)
                throw new IOException(messageList[msg].toStringM2());
        }
    }

    /**
     Returns the user space attribute list from the QUSRUSAT pgm call.

     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    void getAttributes()
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {

        // Close the user space
        if (aUserSpace_ != null)
            aUserSpace_.close();

        // **** Setup the parameter list ****
        ProgramParameter[] parmlist = new ProgramParameter[5];

        // First parameter: output, is the receiveByteArray
        byte[] rcvByteArray = new byte[24];
        parmlist[0] = new ProgramParameter(rcvByteArray.length);

        // Second parameter: input, is the length of the receiveByteArray
        byte[] statusLength = new byte[4];
        BinaryConverter.intToByteArray(rcvByteArray.length, statusLength, 0);
        parmlist[1] = new ProgramParameter(statusLength);

        // Third parameter: input, is return format name
        byte[] statusFormat = new byte[8];
        statusFormat = converter_.stringToByteArray("SPCA0100");              // @C1C
        parmlist[2] = new ProgramParameter( statusFormat );

        // Fourth parameter: input, is the user space name
        parmlist[3] = new ProgramParameter(userSpaceSystemPathName_);

        // Fifth parameter: input/output, is the error code array
        byte[] errorInfo = new byte[32];
        parmlist[4] = new ProgramParameter( errorInfo, 0 );

        rmtCmd_ = new RemoteCommandImplRemote();                             //$C0C
        rmtCmd_.setSystem(system_);                                        //$C0A
        //try                                                               //$C0D
        //{                                                                 //$C0D
        //   pgmCall_.setProgram("/QSYS.LIB/QUSRUSAT.PGM", parmlist );      //$C0D
        //}                                                                 //$C0D
        //catch (PropertyVetoException v) {}       // $B1                   //$C0D

        // Run the program.  Failure returns message list
        if(rmtCmd_.runProgram("QSYS", "QUSRUSAT", parmlist, true) != true)  // This API is threadsafe.  @D3A $C0C @D2C
        {
            // failure, Throw AS400MessageList
            AS400Message[] messageList = rmtCmd_.getMessageList();
            for (int msg = 0; msg < messageList.length; msg++)
                throw new IOException(messageList[msg].toStringM2());
        }
        else
        {
            // get the data returned from the program
            rcvByteArray = parmlist[0].getOutputData();
            Trace.log(Trace.DIAGNOSTIC, "byte array: ", rcvByteArray, 0, rcvByteArray.length);

            // extract the user space size
            length_ = BinaryConverter.byteArrayToInt(rcvByteArray, 8);
            // extract the autoExtend value
            String autoExtendString = converter_.byteArrayToString(rcvByteArray, 12, 1);
            autoExtend_ = autoExtendString.charAt(0);
            // extract the initial value
            initialValue_ = rcvByteArray[13];
        }

        // Reopen the user space
        open();
    }


    /**
     create a byte array of the specified length.  The byte
     array is initialized to EBCDIC spaces.
     **/
    /* @C1D
     byte[] initializeByteArray(int length)
     {
     byte[] result = new byte[length];

     for (int i=0; i<length; i++)
     result[i] = 0x40;

     return result;
     }
     */


    // @C1A
    private byte[] padByteArray(byte[] b, int length)
    {
        byte[] result = new byte[length];
        System.arraycopy(b, 0, result, 0, b.length);
        for(int i = b.length; i < length; ++i)
            result[i] = 0x40;
        return result;
    }


    /**
     Returns the initial value used for filling in the user space during creation and extension.

     @return The initial value used during user space creation and extension.
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    byte getInitialValue()
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        // run pgm call
        getAttributes();

        return initialValue_;
    }
    /**
     Returns the size in bytes of the user space.

     @return The size in bytes of the user space.
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    int getLength()
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        // run pgm call
        getAttributes();

        return length_;
    }
    /**
     Indicates if the user space is auto extendible.

     @return true if the user space is auto extendible; false otherwise.
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    boolean isAutoExtendible()
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {

        boolean returnValue = false;

        // run pgm call
        getAttributes();
        // return attribute
        if (autoExtend_ == '1')
            returnValue = true;

        return returnValue;
    }

    /**
     Open the user space.
     **/
    void open()
      throws AS400SecurityException,
    IOException
    {
        if (mustUseProgramCall_)           // E1a open IFS object only if using IFS to read/write
        {                                  // E1a
            if (Trace.isTraceOn())          // E1a
                Trace.log(Trace.INFORMATION, "Close ignored since using ProgramCall."); // E1a
            return;                         // E1a
        }                                  // E1a

        IFSFileDescriptorImplRemote fd_ = new IFSFileDescriptorImplRemote();                 //$C0A
        fd_.initialize(0, this, userSpacePathName_, IFSRandomAccessFile.SHARE_ALL, system_); //$C0A

        aUserSpace_ = new IFSRandomAccessFileImplRemote();                                   //$C0C
        aUserSpace_.setFD(fd_);                                                              //$C0A
        aUserSpace_.setMode("rw");                                                           //$C0A
        aUserSpace_.setExistenceOption(IFSRandomAccessFile.OPEN_OR_FAIL);                    //$C0A
        aUserSpace_.open();                                                                  //$C0A
    }


    // @E1 Method added to call the correct read method.  The signature existed before,
    //     it is the implementation of the method that is changed.
    int read(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        if (mustUseProgramCall_)
            return readViaProgramCall(dataBuffer, userSpaceOffset, dataOffset, length);
        else
            return readViaIFS(dataBuffer, userSpaceOffset, dataOffset, length);
    }




    /**
     Reads up to <i>length</i> bytes from the user space beginning at <i>userSpaceOffset</i> into <i>dataBuffer</i>
     beginning at <i>dataOffset</i>.

     @param dataBuffer The buffer into which the data from the user space is read.
     @param userSpaceOffset  The offset (0 based) in the user space from which to start reading.
     @param dataOffset  The data starting offset for the results of the read.
     @param length  The number of bytes to be read.
     @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.

     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    // E1c -- subroutine renamed
    int readViaIFS(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        int bytesRead;

        try
        {
            // Verify that the userSpace exists
            if (aUserSpace_ == null)
                open();                           // open or fail if not exist

            // Set the user space offset and reads from the user space
            aUserSpace_.seek(userSpaceOffset);
            bytesRead = aUserSpace_.read(dataBuffer,dataOffset,length, false);    //$C0C
        }
        catch(FileNotFoundException e)
        {
            Trace.log(Trace.ERROR, "Object does not exist (remote read).");
            throw new ObjectDoesNotExistException("path",
                                                  ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
        }
        catch(ExtendedIOException  e)
        {
            int returnCode = e.getReturnCode();
            if (returnCode == 5)                  // ACCESS_DENIED
            {
                Trace.log(Trace.ERROR, "Object authority insufficient (remote read).");
                throw new AS400SecurityException(AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }
            else
                throw new ExtendedIOException(returnCode);
        }

        return bytesRead;
    }


    /**
     Reads up to <i>length</i> bytes from the user space beginning at <i>userSpaceOffset</i> into <i>dataBuffer</i>
     beginning at <i>dataOffset</i>.

     @param dataBuffer The buffer into which the data from the user space is read.
     @param userSpaceOffset  The offset (0 based) in the user space from which to start reading.
     @param dataOffset  The data starting offset for the results of the read.
     @param length  The number of bytes to be read.
     @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.

     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    // @E1 new method
    int readViaProgramCall(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        int actualLength = length;

        AS400Message[] messageList = readViaProgramCall2(dataBuffer,
                                                         userSpaceOffset,
                                                         dataOffset,
                                                         length);

        if (messageList != null)
        {
            String message = messageList[0].toStringM2();

            if (message.startsWith("CPF3C14"))
            {
                int userSpaceLength = getLength();

                if (userSpaceLength < userSpaceOffset)
                {
                    actualLength = -1;
                }
                else
                {
                    actualLength = userSpaceLength - userSpaceOffset;
                    messageList  = readViaProgramCall2(dataBuffer,
                                                       userSpaceOffset,
                                                       dataOffset,
                                                       actualLength);
                    if (messageList != null)
                    {
                        for (int msg = 0; msg < messageList.length; msg++)
                            throw new IOException(messageList[msg].toStringM2());
                    }
                }
            }
            else if (message.startsWith("CPF9820") ||
                     message.startsWith("CPF9802"))
            {
                throw new AS400SecurityException(AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }
            else if (message.startsWith("CPF2209") ||
                     message.startsWith("CPF9810") ||
                     message.startsWith("CPF9801"))
            {
                throw new ObjectDoesNotExistException("path",
                                                      ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
            }
            else
            {
                for (int msg = 0; msg < messageList.length; msg++)
                    throw new IOException(messageList[msg].toStringM2());
            }
        }
        return actualLength;
    }





    /**
     Reads up to <i>length</i> bytes from the user space beginning at <i>userSpaceOffset</i> into <i>dataBuffer</i>
     beginning at <i>dataOffset</i>.

     @param dataBuffer The buffer into which the data from the user space is read.
     @param userSpaceOffset  The offset (0 based) in the user space from which to start reading.
     @param dataOffset  The data starting offset for the results of the read.
     @param length  The number of bytes to be read.
     @return List of AS400Messages objects we get back from the program call.
     If the program call returns no messages, null is returned.

     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    // @E1 new method
    AS400Message[] readViaProgramCall2(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        AS400Message[] messageList = null;

        byte[] returnedData;
        byte[] readOffset = new byte[4];
        byte[] readLength = new byte[4];

        // set the parameter list
        ProgramParameter[] parmlist = new ProgramParameter[5];

        // First parameter: input, is the user space name.
        parmlist[0] = new ProgramParameter(userSpaceSystemPathName_);

        // Create the second parm (the position).  We
        // get an offset from our caller but the API we call needs a
        // position.  Add 1 to the offset to get a position.
        BinaryConverter.intToByteArray(userSpaceOffset + 1, readOffset, 0);
        parmlist[1] = new ProgramParameter(readOffset);

        // Create the third parm (the length).
        BinaryConverter.intToByteArray(length, readLength, 0);
        parmlist[2] = new ProgramParameter(readLength);

        // create the fourth parm (space holder for read data)
        parmlist[3] = new ProgramParameter(length);

        // create the fifth parameter: input/output, is the error code array
        byte[] errorInfo = new byte[32];
        parmlist[4] = new ProgramParameter( errorInfo, 0 );

        rmtCmd_ = new RemoteCommandImplRemote();                              //$C0C
        rmtCmd_.setSystem(system_);                                         //$C0A

        // Run the program.
        if(rmtCmd_.runProgram("QSYS", "QUSRTVUS", parmlist, true) != true)  // This API is threadsafe.  @D3A $C0C @D2C
        {
            messageList = rmtCmd_.getMessageList();
        }
        else
            System.arraycopy(parmlist[3].getOutputData(), 0, dataBuffer, dataOffset, length);

        // call the native method to carry out the request.
        return messageList;
    }

    /**
     Sets the user space attributes

     @param attributeKey The user space attribute to be changed.
     @param data  The new attribute data.
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    void setAttributes(int attributeKey, byte[] data)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        // Close the user space
        if (aUserSpace_ != null)
            aUserSpace_.close();

        // set the parameter list
        ProgramParameter[] parmlist = new ProgramParameter[4];

        // First parameter: output, is a 10 char string, returns the library name.
        byte[] rcvChangeStatus = new byte[10];
        parmlist[0] = new ProgramParameter(10);

        // Second parameter: input, is the user space name.
        parmlist[1] = new ProgramParameter(userSpaceSystemPathName_);

        // Third parameter: input is the attribute to be changed.
        byte[] attributeParmData = new byte[12 + data.length];
        BinaryConverter.intToByteArray(1, attributeParmData, 0);             // change one attribute
        BinaryConverter.intToByteArray(attributeKey, attributeParmData, 4);  // attribute to be changed
        BinaryConverter.intToByteArray(data.length, attributeParmData, 8);   // length of new attribute
        for (int b = 0; b < data.length; b++)
            attributeParmData[12+b] = data[b];                               // attribute byte array value
        parmlist[2] = new ProgramParameter(attributeParmData);

        // Fourth parameter: input/output, is the error code array
        byte[] errorInfo = new byte[32];
        parmlist[3] = new ProgramParameter( errorInfo, 0 );

        rmtCmd_ = new RemoteCommandImplRemote();                              //$C0C
        rmtCmd_.setSystem(system_);                                         //$C0A
        //try                                                                //$C0D
        //{                                                                  //$C0D
        //   pgmCall_.setProgram("/QSYS.LIB/QUSCUSAT.PGM", parmlist );       //$C0D
        //}                                                                  //$C0D
        //catch(PropertyVetoException v) {}        // $B1

        // Run the program.
        if(rmtCmd_.runProgram("QSYS", "QUSCUSAT", parmlist, true) != true)  // This API is threadsafe.  @D3A $C0C @D2C
        {
            // Throw messageList
            AS400Message[] messageList = rmtCmd_.getMessageList();
            for (int msg = 0; msg < messageList.length; msg++)
                throw new IOException(messageList[msg].toStringM2());
        }

        // Reopen the user space
        open();
    }

    /**
     Sets the auto extend attribute.

     @param autoExtendibility  The attribute for user space auto extendibility.
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    void setAutoExtendible(boolean autoExtendibility)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {

        // convert value to AS400 data
        byte[] autoExtendValue = new byte[1];

        if (autoExtendibility == true)
            autoExtendValue = converter_.stringToByteArray("1");                 // @C1C
        else
            autoExtendValue = converter_.stringToByteArray("0");                 // @C1C

        // run pgm call
        setAttributes(AUTO_EXTEND, autoExtendValue);
    }

    /**
     Sets the initial value to be used during user space creation or extension.

     @param initialValue  The new initial value used during future extensions.
     For best performance set byte to zero.
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    void setInitialValue(byte initialValue)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        byte[] value_array = new byte[1];
        value_array[0] = initialValue;

        // run pgm call
        setAttributes(INITIAL_VALUE, value_array);
    }

    /**
     Sets the size of the user space.  Valid values are 1 through 1,6776,704.

     @param length  The new size of the user space.
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    void setLength(int length)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        // convert to AS400 data
        byte[] size = new byte[4];
        BinaryConverter.intToByteArray(length, size, 0);

        // run the change attribute program
        setAttributes(SPACE_SIZE, size);
    }

    // @E1 Method added to call the correct write method.  The signature existed before,
    //     it is the implementation of the method that is changed.
    void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int force)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        if (mustUseProgramCall_)
            writeViaProgramCall(dataBuffer, userSpaceOffset, dataOffset, length, force);
        else
            writeViaIFS(dataBuffer, userSpaceOffset, dataOffset, length, force);
    }



    /**
     Writes up to <i>length</i> bytes from <i>dataBuffer</i> beginning at <i>dataOffset</i> into the user
     space beginning at <i>userSpaceOffset</i>.

     @param dataBuffer  The data buffer to be written to the user space.
     @param userSpaceOffset  The offset (0 based) in the user space to start writing.
     @param dataOffset  The offset in the write data buffer from which to start copying.
     @param length  The length of data to be written.
     @param force  The method of forcing changes made to the user space to
     auxiliary storage.  Valid values are:
     <UL>
     <LI>FORCE_NONE
     <LI>FORCE_ASYNCHRONOUS
     <LI>FORCE_SYNCHRONOUS
     </UL>
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    // E1c -- subroutine renamed
    void writeViaIFS(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int force)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        try
        {
            // Verify that the userSpace exists
            if (aUserSpace_ == null)
                open();                          // open or fail if not exist

            // Set the user space offset and Write the data to the user space
            aUserSpace_.seek(userSpaceOffset);

            if (force == FORCE_SYNCHRONOUS)
                aUserSpace_.setForceToStorage(true);
            else if (force == FORCE_ASYNCHRONOUS)
                aUserSpace_.setForceToStorage(false);
            aUserSpace_.writeBytes(dataBuffer, dataOffset, length);          //$C0C
        }
        catch(FileNotFoundException e)
        {
            Trace.log(Trace.ERROR, "Object does not exist (remote write).");
            throw new ObjectDoesNotExistException("path",
                                                  ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
        }
        catch(ExtendedIOException  e)
        {
            int returnCode = e.getReturnCode();
            if (returnCode == ExtendedIOException.ACCESS_DENIED)
            {
                Trace.log(Trace.ERROR, "Object authority insufficient (remote write).");
                throw new AS400SecurityException(AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }
            else
                throw new ExtendedIOException(returnCode);
        }
    }


    /**
     Writes up to <i>length</i> bytes from <i>dataBuffer</i> beginning at <i>dataOffset</i> into the user
     space beginning at <i>userSpaceOffset</i>.

     @param dataBuffer  The data buffer to be written to the user space.
     @param userSpaceOffset  The offset (0 based) in the user space to start writing.
     @param dataOffset  The offset in the write data buffer from which to start copying.
     @param length  The length of data to be written.
     @param force  The method of forcing changes made to the user space to
     auxiliary storage.  Valid values are:
     <UL>
     <LI>FORCE_NONE
     <LI>FORCE_ASYNCHRONOUS
     <LI>FORCE_SYNCHRONOUS
     </UL>
     @exception AS400SecurityException If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException Signals that an I/O exception of some sort has occurred.
     @exception ObjectDoesNotExistException If the AS400 object does not exist.
     **/
    // @E1a New Method
    void writeViaProgramCall(byte[] dataBuffer, int userSpaceOffset, int dataOffset,
                             int length,          int force)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        byte[] writeOffset = new byte[4];
        byte[] writeLength = new byte[4];
        byte[] forceData   = new byte[1];

        // set the parameter list
        ProgramParameter[] parmlist = new ProgramParameter[6];

        // First parameter: input, is the user space name.
        parmlist[0] = new ProgramParameter(userSpaceSystemPathName_);

        // Create the second parm (the position).  We
        // get an offset from our caller but the API we call needs a
        // position.  Add 1 to the offset to get a position.
        BinaryConverter.intToByteArray(userSpaceOffset + 1, writeOffset, 0);
        parmlist[1] = new ProgramParameter(writeOffset);

        // Create the third parm (the length).
        BinaryConverter.intToByteArray(length, writeLength, 0);
        parmlist[2] = new ProgramParameter(writeLength);

        // create the fourth parm (the data)
        parmlist[3] = new ProgramParameter(dataBuffer);

        // create the fifth parm (force option).
        if (force == FORCE_ASYNCHRONOUS)
            forceData = converter_.stringToByteArray("1");
        else if (force == FORCE_SYNCHRONOUS)
            forceData = converter_.stringToByteArray("2");
        else
            forceData = converter_.stringToByteArray("0");

        parmlist[4] = new ProgramParameter(forceData);

        // create the sixth parameter: input/output, is the error code array
        byte[] errorInfo = new byte[32];
        parmlist[5] = new ProgramParameter( errorInfo, 0 );

        rmtCmd_ = new RemoteCommandImplRemote();                              //$C0C
        rmtCmd_.setSystem(system_);                                         //$C0A

        // Run the program.
        if(rmtCmd_.runProgram("QSYS", "QUSCHGUS", parmlist, true) != true)  // This API is threadsafe.   @D3A $C0C @D2C
        {
            // Throw messageList
            AS400Message[] messageList = rmtCmd_.getMessageList();

            String message = messageList[0].toStringM2();

            if (message.startsWith("CPF9820") ||
                message.startsWith("CPF9802"))
            {
                throw new AS400SecurityException(AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }
            else if (message.startsWith("CPF2209") ||
                     message.startsWith("CPF9810") ||
                     message.startsWith("CPF9801"))
            {
                throw new ObjectDoesNotExistException("path",
                                                      ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
            }
            else
            {
                for (int msg = 0; msg < messageList.length; msg++)
                    throw new IOException(messageList[msg].toStringM2());
            }
        }
    }





}

