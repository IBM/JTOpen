///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: BaseDataQueueImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// Interface that supports native, remote, and proxy implementations of data queues.
interface BaseDataQueueImpl
{
    // Set needed impl properties.
    void setSystemAndPath(AS400Impl system, String path, String name, String library) throws IOException;
    // Provide an implementation of connect.
    void processConnect() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Provide an implementation of clear, key is null, if not a keyed clear.
    void processClear(byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Provide an implementation of create, keyLength == 0 means non-keyed queue.
    void processCreate(int maxEntryLength, String authority, boolean saveSenderInformation, boolean FIFO, int keyLength, boolean forceToAuxiliaryStorage, String description) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException;
    // Provide an implementation of delete.
    void processDelete() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Provide an implementation of read, boolean peek determines peek or read, key is null for non-keyed queues, returns the entry read, or null if no entries on the queue.
    DQReceiveRecord processRead(String search, int wait, boolean peek, byte[] key, boolean saveSenderInformation) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Provide an implementation of retrieve attributes, keyed is false for non-keyed queues.
    DQQueryRecord processRetrieveAttrs(boolean keyed) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException;
    // Provide an implementation of write, key is null for non-keyed queues.
    void processWrite(byte[] key, byte[] data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
}
