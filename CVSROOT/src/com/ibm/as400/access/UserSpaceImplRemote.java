///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM  Toolbox for Java - OSS version)
//
// Filename:  UserSpaceImplRemote.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;

// The UserSpaceImplRemote class is the remote implementation of the user space class.
class UserSpaceImplRemote implements UserSpaceImpl
{
    // The server where the user space is located.
    protected AS400ImplRemote system_ = null;
    // The full path name of the user space.
    protected String path_ = null;
    // The library that contains the user space.
    protected String library_ = null;
    // The name of the user space.
    protected String name_ = null;
    // Use ProgramCall instead of IFS.
    protected boolean mustUseProgramCall_ = false;
    // Use sockets instead of native methods when running natively.
    protected boolean mustUseSockets_ = false;
    // The string to byte data converter.
    protected ConverterImplRemote converter_;
    // Qualified user space name parameter, set on first touch.
    private ProgramParameter nameParameter_ = null;
    // Error code parameter for API's, set on first touch.
    private ProgramParameter errorCodeParameter_ = null;

    // Impl object for remote command server delete, getAttributes, setAttributes.
    protected RemoteCommandImpl remoteCommand_;
    // Whether to call remote commands on-thread.
    private Boolean runOnThread_ = RemoteCommandImpl.OFF_THREAD;

    // The integrated file system object used for read and write.
    private IFSRandomAccessFileImplRemote file_;

    // Throw or return an exception based on the message list from a remote program call.
    private AS400Exception buildException() throws AS400SecurityException, ObjectDoesNotExistException
    {
        // Get the message list.
        AS400Message[] messageList = remoteCommand_.getMessageList();
        // Get the message id of the first message.
        String id = messageList[0].getID();

        // Throw appropriate exceptions for not existing or not authorized.
        if (id.equals("CPF9801") || id.equals("CPF2105"))
        {
            Trace.log(Trace.ERROR, "Object does not exist: " + path_);
            throw new ObjectDoesNotExistException(path_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
        }
        if (id.equals("CPF9802") || id.equals("CPF2189"))
        {
            Trace.log(Trace.ERROR, "User is not authorized to object: " + path_);
            throw new AS400SecurityException(path_, AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
        }
        if (id.equals("CPF9810") || id.equals("CPF2209") || id.equals("CPF2110"))
        {
            Trace.log(Trace.ERROR, "Library does not exist: " + path_);
            throw new ObjectDoesNotExistException(path_, ObjectDoesNotExistException.LIBRARY_DOES_NOT_EXIST);
        }
        if (id.equals("CPF9820") || id.equals("CPF2182"))
        {
            Trace.log(Trace.ERROR, "User is not authorized to library: " + path_);
            throw new AS400SecurityException(path_, AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
        }
        if (id.equals("CPF2283"))
        {
            String authorizationListName = "/QSYS.LIB/" + converter_.byteArrayToString(messageList[0].getSubstitutionData()).trim() + ".AUTL";
            Trace.log(Trace.ERROR, "Object does not exist: " + authorizationListName);
            throw new ObjectDoesNotExistException(authorizationListName, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
        }
        // Else return exception for messages.
        return new AS400Exception(messageList);
    }

    // Closes our file stream to the user space (if a stream has been created),
    // and releases any system resources associated with the stream.
    public void close() throws IOException
    {
        if (file_ != null)
        {
            // Close the random access file stream.
            file_.close();
            // Clear the file connection info.
            file_ = null;
        }
    }

    // Creates a user space.
    public void create(byte[] domainBytes, int length, boolean replace, String extendedAttribute, byte initialValue, String textDescription, String authority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Close the file stream to the user space (if one already exists).
        close();

        // Setup qualified user space name parameter.
        setupNameParameter();
        // Setup error code parameter.
        setupErrorCodeParameter();
        // Setup program call parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Qualified user space name, input, char(20).
            nameParameter_,
            // Extended attributes, input, char(10).
            new ProgramParameter(padByteArray(converter_.stringToByteArray(extendedAttribute), 10)),
            // Initial size, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(length)),
            // Initial value, input, char(1).
            new ProgramParameter(new byte[] { initialValue }),
            // Public authority, input, char(10).
            new ProgramParameter(padByteArray(converter_.stringToByteArray(authority), 10)),
            // Text description, input, char(50).
            new ProgramParameter(padByteArray(converter_.stringToByteArray(textDescription), 50)),
            // Replace, input, char(10), EBCDIC "*YES" or "*NO".
            new ProgramParameter((replace) ? new byte[] { 0x5C, (byte)0xE8, (byte)0xC5, (byte)0xE2, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 } : new byte[] { 0x5C, (byte)0xD5, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 } ),
            // Error code, input/output, char(*).
            errorCodeParameter_,
            // Domain, input, char(10).
            new ProgramParameter(domainBytes),
            // Transfer size request, input, binary(4).
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00 } ),
            // Optimum space alignment, input, char(1).
            new ProgramParameter(new byte[] { length > 16773120 ? (byte)0xF0 : (byte)0xF1 } )
        };

        // Setup for remote program call.
        if (remoteCommand_ == null) {
          setupRemoteCommand();
        }
        // Run create user space (QUSCRTUS) API.  This is a threadsafe API.
        if (!remoteCommand_.runProgram("QSYS", "QUSCRTUS", parameters, runOnThread_))
        {
            // Throw the returned messages.
            throw buildException();
        }
    }

    // Return a byte array padded to the correct length.
    private static byte[] padByteArray(byte[] bytes, int length)
    {
        byte[] result = new byte[length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        for (int i = bytes.length; i < length; ++i) result[i] = 0x40;
        return result;
    }

    // Deletes a user space.
    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Close the file stream to the user space (if one exists), to avoid locking problems.
        close();

        // Setup qualified user space name parameter.
        setupNameParameter();
        // Setup error code parameter.
        setupErrorCodeParameter();
        // Setup program call parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Qualified user space name, input, char(20).
            nameParameter_,
            // Error code, input/output, char(*).
            errorCodeParameter_
        };

        // Setup for remote program call.
        if (remoteCommand_ == null) {
          setupRemoteCommand();
        }
        // Run delete user space (QUSDLTUS) API.  This is a threadsafe API.
        if (!remoteCommand_.runProgram("QSYS", "QUSDLTUS", parameters, runOnThread_))
        {
            // Throw the returned messages.
            throw buildException();
        }
    }

    // Returns the initial value used for filling in the user space during creation and extension.
    public byte getInitialValue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return retrieveAttributes()[13];
    }

    // Returns the size in bytes of the user space.
    public int getLength() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return BinaryConverter.byteArrayToInt(retrieveAttributes(), 8);
    }

    // Indicates if the user space is auto extendible.
    public boolean isAutoExtendible() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return retrieveAttributes()[12] == (byte)0xF1;
    }

    // Retrieve the user space attributes.
    protected byte[] retrieveAttributes() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Close the file stream to the user space (if one exists), to avoid locking problems.
        close();

        // Setup qualified user space name parameter.
        setupNameParameter();
        // Setup error code parameter.
        setupErrorCodeParameter();
        // Setup program call parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*), ask for 24 bytes.
            new ProgramParameter(24),
            // Length of receiver variable, input, binary(4), 24 bytes.
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x18 } ),
            // Format name, input, char(8), EBCDIC "SPCA0100".
            new ProgramParameter(new byte[] { (byte)0xE2, (byte)0xD7, (byte)0xC3, (byte)0xC1, (byte)0xF0, (byte)0xF1, (byte)0xF0, (byte)0xF0 } ),
            // Qualified user space name, input, char(20).
            nameParameter_,
            // Error code, input/output, char(*).
            errorCodeParameter_
        };

        // Setup for remote program call.
        if (remoteCommand_ == null) {
          setupRemoteCommand();
        }
        // Run retrieve user space attributes (QUSRUSAT) API.  This is a threadsafe API.
        if (!remoteCommand_.runProgram("QSYS", "QUSRUSAT", parameters, runOnThread_))
        {
            // Throw the returned messages.
            throw buildException();
        }
        // Return the data returned from the program.
        return parameters[0].getOutputData();
    }

    // Read from user space.
    public int read(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Use remote program call implementation or file implementation.
        if (mustUseProgramCall_)
        {
            // Setup qualified user space name parameter.
            setupNameParameter();
            // Setup program call parameters.
            ProgramParameter[] parameters = new ProgramParameter[]
            {
                // Qualified user space name, input, char(20).
                nameParameter_,
                // Starting position, input, binary(4), add 1 for 1 based offset.
                new ProgramParameter(BinaryConverter.intToByteArray(userSpaceOffset + 1)),
                // Length of data, input, binary(4).
                new ProgramParameter(BinaryConverter.intToByteArray(length)),
                // Receiver variable, output, char(*).
                new ProgramParameter(length)
                // Omit error code optional parameter.
            };

            // Setup for remote program call.
            if (remoteCommand_ == null) {
              setupRemoteCommand();
            }
            // Run retrieve user space (QUSRTVUS) API.  This is a threadsafe API.
            if (!remoteCommand_.runProgram("QSYS", "QUSRTVUS", parameters, runOnThread_))
            {
                String id = remoteCommand_.getMessageList()[0].getID();
                if (!id.equals("CPF3C14") && !id.equals("CPD3C14"))
                {
                    // Throw the returned messages.
                    throw buildException();
                }
                int userSpaceLength = getLength();
                if (userSpaceLength < userSpaceOffset) return -1;
                length = userSpaceLength - userSpaceOffset;
                try
                {
                    parameters[2].setInputData(BinaryConverter.intToByteArray(length));
                    parameters[3].setOutputDataLength(length);
                }
                catch (PropertyVetoException e)
                {
                    Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
                    throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
                }
                if (!remoteCommand_.runProgram("QSYS", "QUSRTVUS", parameters, runOnThread_))
                {
                    // Throw the returned messages.
                    throw buildException();
                }
            }
            // Copy output data into user's array.
            System.arraycopy(parameters[3].getOutputData(), 0, dataBuffer, dataOffset, length);
            return length;
        }
        else
        {
            // Setup for file.
            setupFile();

            // Seek to correct offset.
            file_.seek(userSpaceOffset);
            // Read from the user space, return number of bytes read.
            return file_.read(dataBuffer, dataOffset, length, false);
        }
    }

    // Sets the auto extend attribute.
    public void setAutoExtendible(boolean autoExtendibility) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Number of attributes is 1, key is 3, length of attribute is 1 byte, value is EBCDIC "1" or "0".
        changeAttributes(new byte[] { 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, autoExtendibility ? (byte)0xF1 : (byte)0xF0 });
    }

    // Sets the initial value to be used during user space creation or extension.
    public void setInitialValue(byte initialValue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Number of attributes is 1, key is 2, length of attribute is 1 byte, value is as specified.
        changeAttributes(new byte[] { 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, initialValue });
    }

    // Sets the size of the user space.  Valid values are 1 through 1,6776,704.
    public void setLength(int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Number of attributes is 1, key is 1, length of attribute is 4 bytes, use 4 bytes of  0 to hold space for value.
        byte[] attributeBytes = new byte[] { 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00 };
        // Set the length into the 12th position of the byte array.
        BinaryConverter.intToByteArray(length, attributeBytes, 12);
        changeAttributes(attributeBytes);
    }

    // Change the user space attributes.
    protected void changeAttributes(byte[] attributeBytes) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Close the file stream to the user space (if one exists), to avoid locking problems.
        close();

        // Setup qualified user space name parameter.
        setupNameParameter();
        // Setup error code parameter.
        setupErrorCodeParameter();
        // Setup program call parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Returned library name, output, char(10).
            new ProgramParameter(10),
            // Qualified user space name, input, char(20).
            nameParameter_,
            // Attribtes to change, input, char(*).
            new ProgramParameter(attributeBytes),
            // ErrorCode, input/output, char(*).
            errorCodeParameter_
        };

        // Setup for remote program call.
        if (remoteCommand_ == null) {
          setupRemoteCommand();
        }
        // Run change user space attributes (QUSCUSAT) API.  This is a threadsafe API.
        if (!remoteCommand_.runProgram("QSYS", "QUSCUSAT", parameters, runOnThread_))
        {
            // Throw the returned messages.
            throw buildException();
        }
    }

    // Set needed implementation properties.
    public void setProperties(AS400Impl system, String path, String name, String library, boolean mustUseProgramCall, boolean mustUseSockets)
    {
        system_ = (AS400ImplRemote)system;
        path_ = path;
        library_ = library;
        name_ = name;
        mustUseProgramCall_ = mustUseProgramCall;
        mustUseSockets_ = mustUseSockets;
    }

    // Setup error code program parameter object on first touch.  Synchronized to protect instance variables.  This method can safely be called multiple times because it checks for a previous call before changing the instance variables.
    private void setupErrorCodeParameter()
    {
        // If not already setup.
        if (errorCodeParameter_ == null)
        {
            // Set error code parameter to eight bytes of zero's.  This causes the messages to be issued to the API caller.
            errorCodeParameter_ = new ProgramParameter(new byte[8]);
        }
    }

    // Setup file object on first touch.  Synchronized to protect instance variables.  This method can safely be called multiple times because it checks for a previous call before changing the instance variables.
    private synchronized void setupFile() throws AS400SecurityException, IOException, ObjectDoesNotExistException
    {
        // If not already setup.
        if (file_ == null)
        {
            try
            {
                // Create the file descriptor implementation object.
                IFSFileDescriptorImplRemote fd_ = new IFSFileDescriptorImplRemote();
                // Set the necessary properties into it.
                fd_.initialize(0, this, path_, IFSRandomAccessFile.SHARE_ALL, system_);

                // Create the file implementation object.
                file_ = new IFSRandomAccessFileImplRemote();
                // Set the necessary properties into it.
                file_.setFD(fd_);
                file_.setMode("rw");
                file_.setExistenceOption(IFSRandomAccessFile.OPEN_OR_FAIL);
                // Open the file.
                file_.open();
            }
            catch (FileNotFoundException e)
            {
                Trace.log(Trace.ERROR, "Object does not exist: " + path_, e);
                throw new ObjectDoesNotExistException(path_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
            }
            catch (ExtendedIOException e)
            {
                if (e.getReturnCode() == ExtendedIOException.ACCESS_DENIED)
                {
                    Trace.log(Trace.ERROR, "User is not authorized to object: " + path_, e);
                    throw new AS400SecurityException(path_, AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
                }
                if (e.getReturnCode() == ExtendedIOException.REQUEST_NOT_SUPPORTED && library_.equals("QTEMP"))
                {
                    // File server cannot access QTEMP library.
                    Trace.log(Trace.WARNING, "File server cannot access QTEMP, use mustUseProgramCall option.");
                }
                Trace.log(Trace.ERROR, "Error opening file: " + path_, e);
                throw e;
            }
        }
    }

    // Setup qualified user space name program parameter object on first touch.  Synchronized to protect instance variables.  This method can safely be called multiple times because it checks for a previous call before changing the instance variables.
    private synchronized void setupNameParameter() throws IOException
    {
        // If not already setup.
        if (nameParameter_ == null)
        {
            // Get a converter for the system objects CCSID.
            converter_ = ConverterImplRemote.getConverter((system_).getCcsid(), system_);
            // The name and library of the user space used in program call.  Start with 20 EBCDIC spaces (" ").
            byte[] qualifiedUserSpaceName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
            // Put the converted object name at the beginning of the array.
            converter_.stringToByteArray(name_, qualifiedUserSpaceName, 0);
            // Put the converted library name at position ten.
            converter_.stringToByteArray(library_, qualifiedUserSpaceName, 10);
            // Create the program parameter object.
            nameParameter_ = new ProgramParameter(qualifiedUserSpaceName);
        }
    }

    // Setup remote command object on first touch.  Synchronized to protect instance variables.  This method can safely be called multiple times because it checks for a previous call before changing the instance variables.
    protected synchronized void setupRemoteCommand() throws IOException
    {
      // If not already setup.
      if (remoteCommand_ == null)
      {
        boolean runningNatively = false;
        if (system_.canUseNativeOptimizations())
        {
          try
          {
            remoteCommand_ = (RemoteCommandImpl)Class.forName("com.ibm.as400.access.RemoteCommandImplNative").newInstance();
            // Avoid direct reference - it can cause NoClassDefFoundError at class loading time on Sun JVM's.
            runningNatively = true;
          }
          catch (Throwable e) {
            // A ClassNotFoundException would be unexpected, since canUseNativeOptions() returned true.
            Trace.log(Trace.WARNING, "Unable to instantiate class RemoteCommandImplNative.", e);
          }
        }
        if (remoteCommand_ == null)
        {
          remoteCommand_ = new RemoteCommandImplRemote();
        }
        remoteCommand_.setSystem(system_);

        // Note: All the API's that are called from this class, are threadsafe API's.
        // However, we need to stay consistent with the Toolbox's default threadsafety behavior.
        // So we'll indicate that the remote commands can safely be run on-thread (but only if the threadSafe property isn't set to 'false').  This will enable applications to use UserSpace when running on IBM i and using a profile that is disabled or has password *NONE.

        if (runningNatively && !mustUseSockets_)
        {
          // Abide by the setting of the thread-safety property (if it's set).
          String propVal = ProgramCall.getThreadSafetyProperty();
          if (propVal == null || !propVal.equals("false"))
          {
            runOnThread_ = RemoteCommandImpl.ON_THREAD;
          }
        }
      }
    }

    // Remote implementation of write.
    public void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int force) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Use remote program call implementation or file implementation.
        if (mustUseProgramCall_)
        {
            // Setup qualified user space name parameter.
            setupNameParameter();
            // Allocate parameter array bytes.
            byte[] inputData = new byte[length];
            // Copy data into parameter array.
            System.arraycopy(dataBuffer, dataOffset, inputData, 0, length);
            // Setup program call parameters.
            ProgramParameter[] parameters = new ProgramParameter[]
            {
                // Qualified user space name, input, char(20).
                nameParameter_,
                // Starting position, input, binary(4), add 1 for 1 based offset.
                new ProgramParameter(BinaryConverter.intToByteArray(userSpaceOffset + 1)),
                // Length of data, input, binary(4).
                new ProgramParameter(BinaryConverter.intToByteArray(length)),
                // Input data, input, char(*).
                new ProgramParameter(inputData),
                // Force changes to auxiliary storage, input, char(1).
                new ProgramParameter(new byte[] { (byte)(0xF0 | force) } )
                // Omit error code optional parameter.
            };

            // Setup for remote program call.
            if (remoteCommand_ == null) {
              setupRemoteCommand();
            }
            // Run change user space (QUSCHGUS) API.  This is a threadsafe API.
            if (!remoteCommand_.runProgram("QSYS", "QUSCHGUS", parameters, runOnThread_))
            {
                // Throw the returned messages.
                throw buildException();
            }
        }
        else
        {
            // Setup for file.
            setupFile();
            // Seek to correct offset.
            file_.seek(userSpaceOffset);
            // Set force option.
            switch (force)
            {
                case UserSpace.FORCE_SYNCHRONOUS:
                    file_.setForceToStorage(true);
                    break;
                case UserSpace.FORCE_ASYNCHRONOUS:
                    file_.setForceToStorage(false);
                    break;
            }
            // Write data.
            file_.writeBytes(dataBuffer, dataOffset, length);
        }
    }
}
