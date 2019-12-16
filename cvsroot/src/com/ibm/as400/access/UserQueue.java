///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserQueue.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Arrays;

/**
 The UserQueue class represents an IBM i user queue object.
 
 This class currently will only function when running on the IBM i using native Methods. 
 
  <p>As a performance optimization, when running directly on IBM i, it is possible to use native
    methods to access the user space from the current job.  To enable this support, use the 
    {@link #setMustUseNativeMethods setMustUseNativeMethods()} method. 

 **/

public class UserQueue 
{
	/** Queue type is first-in first-out */ 
	public final static byte QUEUE_TYPE_FIRST_IN_FIRST_OUT = (byte) 0xc6; 
	/** Queue type is keyed  */ 
	public final static byte QUEUE_TYPE_KEYED              = (byte) 0xd2; 
	/** Queue type is last-in first-out */ 
	public final static byte QUEUE_TYPE_LAST_IN_FIRST_OUT  = (byte) 0xd3; 
	
    // Use native methods when running natively; 
    private boolean mustUseNativeMethods_ = false; 

    AS400 system_; 
    String path_; 
    int handle_ = 0; 
    byte[] objectNameBytes_;
    int dataSize_; 
    int keyLength_;
	private byte queueType_; 
	UserQueueImpl impl_; 
    /**
     Constructs a UserQueue object.
     @param  system  The system object representing the system on which the user queue exists.
     @param  path  The fully qualified integrated file system path name of the user queue.  
     **/
    public UserQueue(AS400 system, String path) throws Exception 
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing UserQueue object.");
        system_ = system; 
        path_ = path;
        
		objectNameBytes_ = new byte[20];
		
		Arrays.fill(objectNameBytes_, (byte) 0x40); 
		if (path.indexOf("/QSYS.LIB/") == 0) {
			path = path.substring(9); 
		}

		if (path.charAt(0) != '/') { 
			throw new Exception("Path does not begin with / : "+path); 
		}
		int libIndex = path.indexOf(".LIB/"); 
		if (libIndex < 2) { 
			throw new Exception("Path does not contain .LIB / : "+path); 
		}
		String lib = path.substring(1,libIndex); 
		byte[] libBytes = lib.getBytes("IBM-037"); 
		
		int usridxIndex = path.indexOf(".USRQ"); 
		if (usridxIndex < libIndex) { 
			throw new Exception("Path does not contain .USRQ / : "+path); 
		}
		
		String indexName = path.substring(libIndex+5, usridxIndex); 
		byte[] indexNameBytes = indexName.getBytes("IBM-037");

		System.arraycopy(indexNameBytes, 0, objectNameBytes_, 0, indexNameBytes.length); 
		System.arraycopy(libBytes, 0, objectNameBytes_, 10, libBytes.length);

		//
		// Pick the impl
		// 
		
		impl_ = new UserQueueImplILE(); 
		
    }

    /**
     Creates a user  queue on the system.  The queue will be created with the attributes provided.
     @param  extendedAttribute  The extended attribute of the user queue to be created.
     @param  queueType          The type of the queue, which indicates the sequences in which messages are to be
                                 dequeued from the queue.  Valid values are QUEUE_TYPE_FIRST_IN_FIRST_OUT, 
                                 QUEUE_TYPE_KEYED, QUEUE_TYPE_LAST_IN_FIRST_OUT.
     @param  keyLength          The length in bytes of the message key from 1 to 256, if the type of the queue is
                                QUEUE_TYPE_KEYED.  Otherwise, the value must be 0.
     @param  maximumMessageSize  The maximum allowed size of messages to be placed on the queue.  The maximum size allowed is
                                 64,0000 bytes. 
     @param  initialNumberOfMessages The initial number of messages that the queue can contain. 
     @param  additionalNumberOfMessages The amount to increase the maximum number of messages value when the queue is full.
                                        If set to 0, the queue cannot be extended and an error message is returned when
                                        attempting to enqueue an additional entry. 
     @param  authority  The public authority for the user space.  This string must be 10 characters or less.  
              Valid values are:
     <ul>
     <li>*ALL
     <li>*CHANGE
     <li>*EXCLUDE
     <li>*LIBCRTAUT
     <li>*USE
     <li>authorization-list name
     </ul>
              
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the object already exists on the system.
     @exception  ObjectDoesNotExistException  If the library does not exist on the system.
     **/
    public void create(String extendedAttribute, 
		    byte queueType, 
		    int keyLength,
		    int maximumMessageSize, 
	        int initialNumberOfMessages,
	        int additionalNumberOfMessages, 
	        String authority,
		    String description, 
		    String replace
    		) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
    	dataSize_ = maximumMessageSize;
    	
	    byte[] extendedAttributeBytes = extendedAttribute.getBytes("IBM-037"); 
        byte[] publicAuthorityBytes = authority.getBytes("IBM-037"); 
        byte[] descriptionBytes = description.getBytes("IBM-037");
        byte[] replaceBytes     = replace.getBytes("IBM-037");
        
        keyLength_ = keyLength; 
        queueType_ = queueType; 

        
	    handle_ = impl_.create(objectNameBytes_,
			 extendedAttributeBytes,
			 queueType,  
			 keyLength,
			 dataSize_, 
			 initialNumberOfMessages, 
			 additionalNumberOfMessages, 
			 publicAuthorityBytes,
			 descriptionBytes, 
			 replaceBytes);
		
	    
    	
    	
    }

    
    /**
     * Deletes the user queue. 
     */
    public void delete() { 
	    impl_.delete(handle_, objectNameBytes_); 	
    }
    
    /**
     * Dequeues the next entry from a FIFO or LIFO queue as a string 
     */
    public String dequeue() throws Exception  {
		if (handle_ == 0) {
			open(); 
		}
		if (queueType_ == QUEUE_TYPE_FIRST_IN_FIRST_OUT || 
				queueType_ == QUEUE_TYPE_LAST_IN_FIRST_OUT ) {
			// Queue type is ok 
		} else { 
    		// TODO:  Throw invalid queue type exception
    		throw new Exception("Invalid queue type.  QUEUE must be FIFO or LIFO"); 
    	}
    	if (keyLength_ == 0) { 
    	byte[] deqMsgPrefixBytes = new byte[20];
    	byte[] outputBytes = new byte[dataSize_]; 

    	int messageDequeued = impl_.dequeue(handle_,
    					    deqMsgPrefixBytes,
    					    outputBytes);

    	if (messageDequeued == 0) {
    	    return null; 
    	}  else { 
    	    int outputLength =
    	      (0xFF & deqMsgPrefixBytes[16]) * 256 * 16 +
    	      (0xFF & deqMsgPrefixBytes[17]) * 256  +
    	      (0xFF & deqMsgPrefixBytes[18]) * 16 +
    	      (0xFF & deqMsgPrefixBytes[19]);

    	    return new String(outputBytes, 0, outputLength, "IBM-037");
    	}

    	} else {
    		throw new Exception("not supported "); 
    	}
    }
    /**
     Dequeues an entry from the user queue.  
     @param dequeueMessagePrefixBytes  A Dequeue Message Prefix byte array used by the DEQI MI instruction. 
     This provides both input and output information for the dequeue operation. The format of this byte array
     is as follows.    For more information, see http://publib.boulder.ibm.com/infocenter/iseries/v7r1m0/index.jsp?topic=/rzatk/DEQ.htm.
     <pre>
     Offset 	
     Dec    Hex     Field Name    	                  	Data Type and Length
       0 	0   	Timestamp of enqueue of message 	Char(8) ++
	   8 	8 		Dequeue wait time-out value         Char(8) +
	   16 	10 		Size of message dequeued        	Bin(4) ++
	   20 	14   	Access state modification option    Char(1) + 
	                indicator and message selection 
	                criteria 	
       21 	15 	    Search key (ignored for FIFO/LIFO  Char(key length) + 
                    queues but must be present for 
                    FIFO/LIFO queues with nonzero key 
                     length values) 	
	    * 	* 	   	Message key  	                    Char(key length) ++

     Note:	Fields shown here with one plus sign (+) indicate input to the instruction, 
     and fields shown here with two plus signs (++) are returned by the machine. 
     </pre>
     @param outputBytes      Byte array to receive the bytes contained in the queue entry.  
     @return  Returns 1 if a message was dequeued, 0 if a message was not dequeued.  
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int dequeue(byte[] dequeueMessagePrefixBytes, byte[] outputBytes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
		if (handle_ == 0) {
			open(); 
		}
    	return impl_.dequeue(handle_, dequeueMessagePrefixBytes, outputBytes); 
    }


    /**
     Returns the String representation of this user queue object.
     @return  The String representation of this user queue object.
     **/
    public String toString()
    {
        return "UserQueue " + path_;
    }


    
    /**
     * Enqueues a string on the user queue
     */
    public void enqueue(String value) throws Exception  {
        byte[] valueBytes = value.getBytes("IBM-037");
		if (handle_ == 0) {
			open(); 
		}
		if (queueType_ == QUEUE_TYPE_FIRST_IN_FIRST_OUT || 
				queueType_ == QUEUE_TYPE_LAST_IN_FIRST_OUT ) {
			// Queue type is ok 
		} else { 
    		// TODO:  Throw invalid queue type exception
    		throw new Exception("Invalid queue type.  QUEUE must be FIFO or LIFO"); 
    	}
 	    impl_.enqueue(handle_, null, valueBytes); 
    }

    
    
    /**
    Enqueues an entry on the user queue using the ENQ MI instruction.   
    @param      enqueueMessagePrefixBytes  A byte array representing the message prefix passed to the ENQ MI instruction.
                      A value of null may be passed when using a FIFO to LIFO queue.  
     <p>The format of the message prefix is the following.  For more information, see
     http://publib.boulder.ibm.com/infocenter/iseries/v7r1m0/index.jsp?topic=/rzatk/ENQ.htm
     <pre>
     Offset 	
     Dec 	Hex 	Field Name 						Data Type and Length
		0 	0 		Size of message to be enqueued 	Bin(4)
		4 	4		Enqueue key value (ignored for  Char(key length)  
			        FIFO/LIFO queues with key 
			        lengths equal to 0) 	
     </pre> 
   
    The static method {@link #setEnqueueMessagePrefixBytesMessageSize setEnqueueMessagePrefixBytesMessageSize()} can be used to set the size. 
    The static method {@link #setEnqueueMessagePrefixBytesEnqueueKey setEnqueueMessagePrefixBytesEnqueueKey()} can be used to set the size. 
                      
    @param      entryBytes   A byte array representing the entry to add to the queue. 
    @exception  AS400SecurityException  If a security or authority error occurs.
    @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
    @exception  IOException  If an error occurs while communicating with the system.
    @exception  IllegalObjectTypeException  If the object on the system is not the required type.
    @exception  InterruptedException  If this thread is interrupted.
    @exception  ObjectDoesNotExistException  If the object does not exist on the system.
    **/
   
   public void enqueue(byte[] enqueueMessagePrefixBytes, byte[] entryBytes) throws Exception {
		if (handle_ == 0) {
			open(); 
		}
	   if (enqueueMessagePrefixBytes == null) { 
		   impl_.enqueue(handle_, null,  entryBytes); 
	   } else {
		   throw new Exception("Keyed message queue not yet supported"); 
	   }
   	
   }

    
    
    /**
    Indicates if the native methods will be used internally to perform user queue dequeue and enqueue requests.  
    @return  true if user dequeue and enqueue requests will be performed via native methods; false otherwise.
    @see #setMustUseNativeMethods
    **/
    
    public boolean isMustUseNativeMethods()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if user queue must use native methods:", mustUseNativeMethods_);
        return mustUseNativeMethods_;
    }

    /**
    Specifies whether native methods are used by the current job to perform user queue operations.
    This option can only be set to true when the application is running on the System i.  
    @param  useNativeMethods  Internally use native methods to perform read and write requests.
    @see #isMustUseNativeMethods
    **/
   public void setMustUseNativeMethods(boolean useNativeMethods)
   {
       if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting if user queue must use native methods:", useNativeMethods);
       if (useNativeMethods) {
    	   if (!mustUseNativeMethods_) { 
              mustUseNativeMethods_ = useNativeMethods;
              impl_ = new UserQueueImplILE(); 
    	   }
    	   
       } else {
    	   if (mustUseNativeMethods_) {
    		   mustUseNativeMethods_ = useNativeMethods;
    		   if (impl_ != null) { 
    		   impl_.close(handle_);
    		   impl_ = null; 
    		   handle_ = 0; 
    		   }
    	   }
       }
           
   }


   /* 
    * private methods 
    */
   
   private void open() {
	   
		handle_ = impl_.open(objectNameBytes_); 
		// TODO :  Determine  attributes. 
		byte[] outputBytes = new byte[128]; 
		impl_.getAttributes(handle_, outputBytes);
		//
		// 96 	60 	Queue type 	Bits 1-2
		//  	00 = 	Keyed
	    //      01 = 	Last in, first out (LIFO)
	    //      10 = 	First in, first out (FIFO)
		if ((outputBytes[96] & 0x60) == 0 ) {
			queueType_ = QUEUE_TYPE_KEYED; 
		} else if ((outputBytes[96] & 0x60) == 0x20 ) {
			queueType_ = QUEUE_TYPE_LAST_IN_FIRST_OUT;  
		} else if ((outputBytes[96] & 0x60) == 0x40 ) {
			queueType_ = QUEUE_TYPE_FIRST_IN_FIRST_OUT;  
		}
		// 111 	6F Maximum size of message to be enqueued Bin(4) 
		dataSize_ = 0x1000000 * (0xFF & outputBytes[111]) + 
		            0x10000   * (0xFF & outputBytes[112]) + 
		            0x100     * (0xFF & outputBytes[113]) + 
		                        (0xFF & outputBytes[114]);  
	    // 109 	6D 	Key length		Bin(2) 
	    keyLength_ = 0x100 * (0xFF & outputBytes[109])  + (0xFF & outputBytes[110]);
		
		
   }


   /*
    * Static methods for working with dequeue message prefix bytes.  
    */
   
   /**
    * Resets the dequeueMessagePrefixBytes to zero
    */
   public static void resetDequeueMessagePrefixBytes(byte[] dequeueMessagePrefixBytes) {
	   Arrays.fill(dequeueMessagePrefixBytes, (byte) 0x0);  
   }
    
   /**
    *  Returns the output length from the deqMsgPrefixBytes buffer 
    */
   public static int getDequeueMessagePrefixBytesLength(byte[] deqMsgPrefixBytes) {
	   return  (0xFF & deqMsgPrefixBytes[16]) * 256 * 16 +
	      (0xFF & deqMsgPrefixBytes[17]) * 256  +
	      (0xFF & deqMsgPrefixBytes[18]) * 16 +
	      (0xFF & deqMsgPrefixBytes[19]);
   }
   
   /**
    * Resets the enqueueMessagePrefixBytes to zero 
    * 
    * The format of the enqueueMessagePrefix is the following
    * Offset 	
    * Dec 	Hex	Field Name	                        Data Type and Length
    * 0 	0 	Size of message to be enqueued 		Bin(4)
    * 4 	4 	Enqueue key value                   Char(key length) 
    *           (ignored for FIFO/LIFO queues 
    *              with key lengths equal to 0) 	
	*
    */
   public static void resetEnqueueMessagePrefixBytes(byte[] enqueueMessagePrefixBytes) {
	   Arrays.fill(enqueueMessagePrefixBytes, (byte) 0x0);  
   }

   public static void setEnqueueMessagePrefixBytesMessageSize(byte[] enqueueMessagePrefixBytes, int messageSize) {
	   enqueueMessagePrefixBytes[0] = (byte) ( messageSize / 0x1000000);
	   enqueueMessagePrefixBytes[1] = (byte) ((messageSize / 0x10000) & 0xFF); 
	   enqueueMessagePrefixBytes[2] = (byte) ((messageSize / 0x100) & 0xFF); 
	   enqueueMessagePrefixBytes[3] = (byte) ( messageSize & 0xFF); 
   }
   
   public static void setEnqueueMessagePrefixBytesEnqueueKey(byte[] enqueueMessagePrefixBytes, byte[] keyBytes) {
	System.arraycopy(keyBytes, 0, enqueueMessagePrefixBytes, 4, keyBytes.length) ;
   }
}
