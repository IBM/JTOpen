///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BaseDataQueueImplRemote.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

class BaseDataQueueImplRemote implements BaseDataQueueImpl
{
    // Identify all data queue reply data streams.
    static
    {
        AS400Server.addReplyStream(new DQExchangeAttributesNormalReplyDataStream(), AS400.DATAQUEUE);
        AS400Server.addReplyStream(new DQRequestAttributesNormalReplyDataStream(), AS400.DATAQUEUE);
        AS400Server.addReplyStream(new DQCommonReplyDataStream(), AS400.DATAQUEUE);
        AS400Server.addReplyStream(new DQReadNormalReplyDataStream(), AS400.DATAQUEUE);
    }

    AS400ImplRemote system_;
    private AS400Server server_ = null;  // The server job that processes requests.
    String path_;
    ConverterImplRemote converter_;
    byte[] queueNameBytes_ = { (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 };
    byte[] libraryBytes_ = { (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 };

    // Set needed implementation properties.
    public void setSystemAndPath(AS400Impl system, String path, String name, String library) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting up implementation object: " + path);
        system_ = (AS400ImplRemote)system;
        path_ = path;

        converter_ = ConverterImplRemote.getConverter(system_.getCcsid(), system_);
        converter_.stringToByteArray(name, queueNameBytes_);
        converter_.stringToByteArray(library, libraryBytes_);
    }

    // Remote implementation of connect.  Exchanges client/server attributes with the data queue server.
    private void open() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        // Connect to server.
        server_ = system_.getConnection(AS400.DATAQUEUE,  false /*forceNewConnection*/, false /*skip signon server */);

        // Exchange attributes with server job.  (This must be first exchange with server job to complete initialization.)  First check to see if server has already been initialized by another user.
        synchronized (server_)  // Close the window between getting and checking if exchange has been done.
        {
            DataStream baseReply = server_.getExchangeAttrReply();
            if (baseReply == null)
            {
                try
                {
                    baseReply = server_.sendExchangeAttrRequest(new DQExchangeAttributesDataStream());
                }
                catch (IOException e)
                {
                    Trace.log(Trace.ERROR, "IOException during exchange attributes:", e);
                    system_.disconnectServer(server_);
                    throw e;
                }

                switch (baseReply.hashCode())
                {
                    case 0x8000:  // DQExchangeAttributesNormalReplyDataStream.
                        // Means request completed OK.
                        return;  // Exchange attributes succeeded.
                    case 0x8002:  // DQCommonReplyDataStream.
                        Trace.log(Trace.ERROR, "Unexpected reply datastream:", baseReply.data_);
                        system_.disconnectServer(server_);
                        // Throw an appropriate exception.
                        DQCommonReplyDataStream reply = (DQCommonReplyDataStream)baseReply;
                        throw buildException(reply.getRC(), reply.getMessage());
                    default:  // Unknown data stream.
                        Trace.log(Trace.ERROR, "Unknown exchange attributes reply datastream:", baseReply.data_);
                        system_.disconnectServer(server_);
                        throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
                }
            }
        }
    }

    // Remote implementation of clear, if key is null, do non-keyed clear.
    public void clear(byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Connect to the data queue server.
        open();

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Processing clear: " + path_);
        DQClearDataStream request = new DQClearDataStream(queueNameBytes_, libraryBytes_, key);

        try
        {
            DataStream baseReply = server_.sendAndReceive(request);
            switch (baseReply.hashCode())
            {
                case 0x8002:  // DQCommonReplyDataStream.
                    DQCommonReplyDataStream reply = (DQCommonReplyDataStream)baseReply;
                    int rc = reply.getRC();
                    if (rc != 0xF000)
                    {
                        // Throw an appropriate exception.
                        throw buildException(key != null, rc, reply.getMessage());
                    }
                    break;
                default:  // Unknown data stream.
                    Trace.log(Trace.ERROR, "Unknown clear reply datastream:", baseReply.data_);
                    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Lost connection to data queue server:", e);
            system_.disconnectServer(server_);
            throw e;
        }
    }

    // Remote implementation of create, keyLength == 0 means non-keyed queue.
    public void create(int maxEntryLength, String authority, boolean saveSenderInformation, boolean FIFO, int keyLength, boolean forceToAuxiliaryStorage, String description) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        // Connect to the data queue server.
        open();

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Processing create: " + path_);
        byte[] descriptionBytes = {(byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40};
        converter_.stringToByteArray(description, descriptionBytes);

        DQCreateDataStream request = new DQCreateDataStream(queueNameBytes_, libraryBytes_, maxEntryLength, authority, saveSenderInformation, FIFO, keyLength, forceToAuxiliaryStorage, descriptionBytes);

        try
        {
            DataStream baseReply = server_.sendAndReceive(request);
            switch (baseReply.hashCode())
            {
                case 0x8002:  // DQCommonReplyDataStream.
                    DQCommonReplyDataStream reply = (DQCommonReplyDataStream)baseReply;
                    int rc = reply.getRC();
                    if (rc != 0xF000)
                    {
                        // Throw an appropriate exception.
                        if (rc == 0xF001 && converter_.byteArrayToString(reply.getMessage(), 0, 7).equals("CPF9870"))
                        {
                            Trace.log(Trace.ERROR, "Data queue already exists: " + path_);
                            throw new ObjectAlreadyExistsException(path_, ObjectAlreadyExistsException.OBJECT_ALREADY_EXISTS);
                        }
                        throw buildException(rc, reply.getMessage()); // General errors.
                    }
                    break;
                default:  // Unknown data stream.
                    Trace.log(Trace.ERROR, "Unknown create reply datastream:", baseReply.data_);
                    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Lost connection to data queue server:", e);
            system_.disconnectServer(server_);
            throw e;
        }
    }

    // Remote implementaion of delete.
    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        // Connect to the data queue server.
        open();

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Processing delete: " + path_);
        DQDeleteDataStream request = new DQDeleteDataStream(queueNameBytes_, libraryBytes_);

        try
        {
            DataStream baseReply = server_.sendAndReceive(request);
            switch (baseReply.hashCode())
            {
                case 0x8002:  // DQCommonReplyDataStream.
                    DQCommonReplyDataStream reply = (DQCommonReplyDataStream)baseReply;
                    int rc = reply.getRC();
                    if (rc != 0xF000)
                    {
                        // Throw an appropriate exception.
                        throw buildException(rc, reply.getMessage());
                    }
                    break;
                default:  // Unknown data stream.
                    Trace.log(Trace.ERROR, "Unknown delete reply datastream:", baseReply.data_);
                    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Lost connection to data queue server:", e);
            system_.disconnectServer(server_);
            throw e;
        }
    }

    // Remote implementation of read for data queues, key == null means non-keyed queue, boolean peek determines peek or read, returns the entry read, or null if no entries on the queue.
    public DQReceiveRecord read(String search, int wait, boolean peek, byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Connect to the data queue server.
        open();

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Processing read: " + path_);
        byte[] searchBytes = (key == null) ? new byte[2] : converter_.stringToByteArray(search);
        DQReadDataStream request = new DQReadDataStream(queueNameBytes_, libraryBytes_, searchBytes, wait, peek, key);

        try
        {
            DataStream baseReply = server_.sendAndReceive(request);
            switch (baseReply.hashCode())
            {
                case 0x8003:  // DQReadNormalReplyDataStream.
                    DQReadNormalReplyDataStream reply = (DQReadNormalReplyDataStream)baseReply;
                    byte[] senderInformationBytes = reply.getSenderInformation();
                    return new DQReceiveRecord(senderInformationBytes[0] == 0x40 ? null : converter_.byteArrayToString(senderInformationBytes), reply.getEntry(), reply.getKey());
                case 0x8002:  // DQCommonReplyDataStream.
                    DQCommonReplyDataStream commonReply = (DQCommonReplyDataStream)baseReply;
                    int rc = commonReply.getRC();
                    if (rc == 0xF006)  // No data to return.
                    {
                        Trace.log(Trace.INFORMATION, "No entry on data queue.");
                        return null;
                    }
                    // Throw an appropriate exception.
                    throw buildException(key != null, rc, commonReply.getMessage()); // General errors.
                default:  // Unknown data stream.
                    Trace.log(Trace.ERROR, "Unknown read reply datastream ", baseReply.data_);
                    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Lost connection to data queue server:", e);
            system_.disconnectServer(server_);
            throw e;
        }
    }

    // Remote implementation for retrieve attributes, keyed is false for non-keyed queues
    public DQQueryRecord retrieveAttributes(boolean keyed) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Connect to the data queue server.
        open();

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Processing retrieve attributes: " + path_);
        DataStream request = new DQRequestAttributesDataStream(queueNameBytes_, libraryBytes_);

        try
        {
            DataStream baseReply = server_.sendAndReceive(request);
            switch (baseReply.hashCode())
            {
                case 0x8001:  // DQRequestAttributesNormalReplyDataStream.
                    DQRequestAttributesNormalReplyDataStream reply = (DQRequestAttributesNormalReplyDataStream)baseReply;
                    int type = reply.getType();
                    DQQueryRecord record = new DQQueryRecord();
                    if (keyed)
                    {
                        if (type != 2)  // Actual data queue is not a keyed data queue.
                        {
                            Trace.log(Trace.ERROR, "Using KeyedDataQueue for non-keyed data queue: " + path_);
                            throw new IllegalObjectTypeException(path_, IllegalObjectTypeException.DATA_QUEUE_NOT_KEYED);
                        }
                        record.FIFO_ = true; // Keyed queues always FIFO.
                    }
                    else
                    {
                        if (type == 0)
                        {
                            record.FIFO_ = true;
                        }
                        else if (type == 1)
                        {
                            record.FIFO_ = false;
                        }
                        else // Queue is keyed and this is not a KeyedDataQueue object.
                        {
                            Trace.log(Trace.ERROR, "Using DataQueue for keyed data queue: " + path_);
                            throw new IllegalObjectTypeException(path_, IllegalObjectTypeException.DATA_QUEUE_KEYED);
                        }
                    }
                    record.maxEntryLength_ = reply.getMaxEntryLength();
                    record.saveSenderInformation_ = reply.getSaveSenderInformation();
                    record.forceToAuxiliaryStorage_ = reply.getForceToAuxiliaryStorage();
                    record.description_ = converter_.byteArrayToString(reply.getDescription());
                    record.keyLength_ = reply.getKeyLength();
                    return record;
                case 0x8002:  // DQCommonReplyDataStream.
                    DQCommonReplyDataStream commonReply = (DQCommonReplyDataStream)baseReply;
                    // Throw an appropriate exception.
                    throw buildException(commonReply.getRC(), commonReply.getMessage()); // General errors.
                default:  // Unknown data stream.
                    Trace.log(Trace.ERROR, "Unknown retrieve attributes reply datastream:", baseReply.data_);
                    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Lost connection to data queue server:", e);
            system_.disconnectServer(server_);
            throw e;
        }
    }

    // Remote implementation for write, key is null for non-keyed queues.
    public void write(byte[] key, byte[] data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Connect to the data queue server.
        open();

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Processing write: " + path_);
        DQWriteDataStream request = new DQWriteDataStream(queueNameBytes_, libraryBytes_, key, data);

        try
        {
            DataStream baseReply = server_.sendAndReceive(request);
            switch (baseReply.hashCode())
            {
                case 0x8002:  // DQCommonReplyDataStream.
                    DQCommonReplyDataStream reply = (DQCommonReplyDataStream)baseReply;
                    int rc = reply.getRC();
                    if (rc != 0xF000)
                    {
                        // Throw an appropriate exception.
                        throw buildException(key != null, rc, reply.getMessage());
                    }
                    break;
                default:  // Unknown data stream.
                    Trace.log(Trace.ERROR, "Unknown write reply datastream:", baseReply.data_);
                    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Lost connection to data queue server:", e);
            system_.disconnectServer(server_);
            throw e;
        }
    }

    // Returns or throws the appropriate exception based on the return code and error message arguments.
    // rc  The return code from reply data stream.
    // messageBytes  The Message from the reply data stream.
    // This function returns an AS400Exception and throws all others.
    private AS400Exception buildException(int rc, byte[] messageBytes) throws AS400SecurityException, ErrorCompletingRequestException, ObjectDoesNotExistException
    {
        switch (rc)
        {
            case 0xF001:  // Command check.
            {
                if (messageBytes == null)
                {
                    Trace.log(Trace.ERROR, "Error completing data queue request, rc: 0xF001");
                    throw new ErrorCompletingRequestException(ErrorCompletingRequestException.UNKNOWN, rc + ":");
                }
                String messageID = converter_.byteArrayToString(messageBytes, 0, 7);
                String message = converter_.byteArrayToString(messageBytes);
                Trace.log(Trace.ERROR, "Error completing data queue request: " + message);
                if (messageID.equals("CPF9810"))
                {
                    Trace.log(Trace.ERROR, "Library does not exist: '" + converter_.byteArrayToString(libraryBytes_) + "'");
                    throw new ObjectDoesNotExistException(path_, ObjectDoesNotExistException.LIBRARY_DOES_NOT_EXIST);
                }
                if (messageID.equals("CPF9801") || messageID.equals("CPF2105"))
                {
                    throw new ObjectDoesNotExistException(path_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
                }
                if (messageID.equals("CPF9802") || messageID.equals("CPF2189"))
                {
                    throw new AS400SecurityException(path_, AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
                }
                if (messageID.equals("CPF9820") || messageID.equals("CPF2182"))
                {
                    throw new AS400SecurityException(path_, AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
                }
                return new AS400Exception(new AS400Message(messageID, message.substring(9)));
            }
            case 0xF002:  // Protocol error.
                Trace.log(Trace.ERROR, "Data queue protocol error.");
                throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
            case 0xF003:  // Syntax error.
                Trace.log(Trace.ERROR, "Data queue syntax error.");
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
            case 0xF004:  // Queue destroyed.
                Trace.log(Trace.ERROR, "Data queue has been destroyed.");
                throw new ObjectDoesNotExistException(path_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
            case 0xF005:  // Unsupported queue length.
                Trace.log(Trace.ERROR, "Unsupported length.");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.LENGTH_NOT_VALID);
            case 0xF007:  // Invalid data stream level.
                Trace.log(Trace.ERROR, "Data queue data stream level not valid.");
                throw new InternalErrorException(InternalErrorException.DATA_STREAM_LEVEL_NOT_VALID);
            case 0xF008:  // Invalid version/release/modification.
                Trace.log(Trace.ERROR, "Data queue VRM not valid.");
                throw new InternalErrorException(InternalErrorException.VRM_NOT_VALID);
            case 0xF009:  // Request rejected by user exit program.
                Trace.log(Trace.ERROR, "Exit program rejected request.");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_DENIED_REQUEST);
            case 0xF00A:  // Exit program not authorized.
                Trace.log(Trace.ERROR, "Exit program not authorized.");
                throw new AS400SecurityException(AS400SecurityException.EXIT_PROGRAM_NOT_AUTHORIZED);
            case 0xF00B:  // Exit program not found.
                Trace.log(Trace.ERROR, "Exit program not found.");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_NOT_FOUND);
            case 0xF00D:  // User exit program failed.
                Trace.log(Trace.ERROR, "Exit program error.");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_ERROR);
            case 0xF00E:  // Invalid number of exit programs.
                Trace.log(Trace.ERROR, "Exit program number not valid.");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_NUMBER_NOT_VALID);
            default:
            {
                byte[] rcBytes = new byte[2];
                BinaryConverter.unsignedShortToByteArray(rc, rcBytes, 0);
                if (messageBytes == null)
                {
                    Trace.log(Trace.ERROR, "Error completing data queue request, rc:", rcBytes);
                    throw new ErrorCompletingRequestException(ErrorCompletingRequestException.UNKNOWN, rc + ":");
                }
                String message = converter_.byteArrayToString(messageBytes);
                Trace.log(Trace.ERROR, "Error completing data queue request: " + message + ", rc:", rcBytes);
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.UNKNOWN, rc + " : " + message);
            }
        }
    }

    // Build Exception as above, plus detect object type mismatch.
    private AS400Exception buildException(boolean expectKeyed, int rc, byte[] messageBytes) throws AS400SecurityException, ErrorCompletingRequestException, IllegalObjectTypeException, ObjectDoesNotExistException
    {
        if (rc == 0xF001 && messageBytes != null)
        {
            String messageID = converter_.byteArrayToString(messageBytes, 0, 7);
            if (expectKeyed && messageID.equals("CPF9502"))
            {
                Trace.log(Trace.ERROR, "Error completing data queue request: " + converter_.byteArrayToString(messageBytes));
                Trace.log(Trace.ERROR, "Using KeyedDataQueue for non-keyed data queue: " + path_);
                throw new IllegalObjectTypeException(path_, IllegalObjectTypeException.DATA_QUEUE_NOT_KEYED);
            }
            if (!expectKeyed && messageID.equals("CPF9506"))
            {
                Trace.log(Trace.ERROR, "Error completing data queue request: " + converter_.byteArrayToString(messageBytes));
                Trace.log(Trace.ERROR, "Using DataQueue for keyed data queue: " + path_);
                throw new IllegalObjectTypeException(path_, IllegalObjectTypeException.DATA_QUEUE_KEYED);
            }
        }
        return buildException(rc, messageBytes);
    }
}
