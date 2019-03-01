///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserIndex.java
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
 The UserIndex class represents an IBM i user index object.
 
 This class currently will only function when running on the IBM i using native Methods. 
 
  <p>As a performance optimization, when running directly on IBM i, it is possible to use native
    methods to access the user space from the current job.  To enable this support, use the 
    {@link #setMustUseNativeMethods setMustUseNativeMethods()} method. 

 **/

public class UserIndex 
{
    /* search rules */ 
    public static final int  FIND_EQUALS      = 0x0001;	
    public static final int  FIND_GREATER     = 0x0002 ; 
    public static final int  FIND_LESSER             = 0x0003;
    public static final int  FIND_NOT_LESSER         = 0x0004;
    public static final int  FIND_NOT_GREATER        = 0x0005;
    public static final int  FIND_FIRST              = 0x0006;
    public static final int  FIND_LAST               = 0x0007;
    public static final int  FIND_BETWEEN            = 0x0008;

	/* Entry length attribute */ 
	public static final byte FIXED_LENGTH_ENTRIES = (byte) 0xc6; 
	public static final byte VARIABLE_LENGTH_ENTRIES = (byte) 0xe5;
	/* Key insertion */ 
	public static final byte INSERTION_BY_KEY = (byte) 0xf1;
	public static final byte NO_INSERTION_BY_KEY = (byte) 0xf0; 
    /* Immediate update options */ 	
	public static final byte IMMEDIATE_UPDATE = (byte) 0xF1;
	public static final byte NO_IMMEDIATE_UPDATE= (byte) 0xF0;
	/* optimization options */ 
	public static final byte OPTIMIZE_FOR_RANDOM_REFERENCES = (byte) 0xF0; 
	public static final byte OPTIMIZE_FOR_SEQUENTIAL_REFERENCES = (byte) 0xF1; 
	
	/* insertion rules */ 
	/** 
	 * Insert unique argument.  The RULE_INSERT is valid only for indexes not containing keys 
	 * */ 
	public static final int RULE_INSERT =              0x0001;          
	/**
	 * Insert argument, replacing the nonkey portion if the key is already in the index 
	 */ 
	public static final int RULE_INSERT_REPLACE =           0x0002;
	/**
	 * Insert argument only if the key is not already in the index 
	 */
    public static final int RULE_INSERT_NO_REPLACE =        0x0003;                                      

    
    private static byte[] IDXA0100 = {(byte)0xc9, (byte)0xc4, (byte)0xe7, (byte)0xc1, (byte)0xf0, (byte)0xf1, (byte)0xf0, (byte)0xf0}; 
    // Use native methods when running natively; 
    private boolean mustUseNativeMethods_ = false; 

    
    AS400 system_; 
    String path_;
	private byte[] insertKeyValueBytes_;
	private byte[] insertOptionBytes_;
	private byte entryLengthAttribute_; 
	private int entryLength_; 
	private int keyLength_; 
	private int valueLength_; 
    private int handle_;              /* handle for native method */
	private byte[] objectNameBytes_;  /* name of the object in ebcdic */
	private byte[] findOptionBytes_; 
	UserIndexNativeImpl impl_; 
	
    /**
     Constructs a UserIndex object.
     @param  system  The system object representing the system on which the user index exists.
     @param  path  The fully qualified integrated file system path name of the user index.  
                  
     **/
    public UserIndex(AS400 system, String path) throws Exception
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing UserIndex object.");
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
		
		int usridxIndex = path.indexOf(".USRIDX"); 
		if (usridxIndex < libIndex) { 
			throw new Exception("Path does not contain .USRIDX / : "+path); 
		}
		
		String indexName = path.substring(libIndex+5, usridxIndex); 
		byte[] indexNameBytes = indexName.getBytes("IBM-037");

		System.arraycopy(indexNameBytes, 0, objectNameBytes_, 0, indexNameBytes.length); 
		System.arraycopy(libBytes, 0, objectNameBytes_, 10, libBytes.length);

		handle_ = 0; 
        
		
		//
		// Pick the impl
		// 
		
		impl_ = new UserIndexNativeImplILE(); 
    }

    /**
     Creates a user  index on the system.  The index will be created with the attributes provided.
     @param  extendedAttribute  The extended attribute of the user index to be created.
     @param  entryLengthAttribute Length attribute of the queue entries.  Must be set to 
              FIXED_LENGTH_ENTRIES or VARIABLE_LENGTH_ENTRIES.  Currently only FIXED_LENGTH_ENTRIES is supported. 
	 @param  entryLength     The length of entries in the index. 
	                         The valid values for fixed-length entries are from 1 through 2000.
                             Valid values for variable length entries are 0 or -1. 
                             A value of 0 enables a maximum entry length of 120 bytes and a key length from 1 through 120. 
                             A value of -1 enables a maximum entry length of 2000 and a key length from 1 through 2000. 
	 @param  keyInsertion    Whether the inserts to the index are by key. The valid values are: 
	                         INSERTION_BY_KEY and NO_INSERTION_BY_KEY 
     @param  keyLength       The length of the key where the first byte of an entry is the beginning of the key for the index entries.
                             The value for this parameter must be 0 for NO_INSERTION_BY_KEY. 
                             If you specify key length insertion, this value is from 1 through 2000
	 @param  immediateUpdate Whether the updates to the index are written synchronously to auxiliary storage on each 
	                         update to the index. The valid values are: IMMEDIATE_UPDATE and NO_IMMEDIATE_UPDATE.
     @param  optimization    The type of access in which to optimize the index. The valid values are: OPTIMIZE_FOR_RANDOM_REFERENCES and OPTIMIZE_FOR_SEQUENTIAL_REFERENCES.
                             
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
	 @param description
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectAlreadyExistsException  If the object already exists on the system.
     **/
	public void create(String extendedAttribute, 
			byte entryLengthAttribute,
			int entryLength, 
			byte keyInsertion, 
			int keyLength,
			byte immediateUpdate, 
			byte optimization, 
			String authority,
			String description
	) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectAlreadyExistsException,  Exception
    {
    	entryLengthAttribute_ = entryLengthAttribute; 
    	entryLength_ = entryLength; 
    	keyLength_ = keyLength; 
    	valueLength_ = entryLength_ - keyLength ; 
    	
	    byte[] extendedAttributeBytes = extendedAttribute.getBytes("IBM-037"); 
        byte[] publicAuthorityBytes = authority.getBytes("IBM-037"); 
        byte[] descriptionBytes = description.getBytes("IBM-037");
        
        int valueLength = entryLength - keyLength;
	if (valueLength <= 0) {
	    throw new Exception("Entry lentgh attribute of "+entryLength+" not correct for key length of "+keyLength); 
	} 

        if (entryLengthAttribute_ != FIXED_LENGTH_ENTRIES) {
        	throw new Exception("Entry lenth attribute of "+entryLengthAttribute+" not supported:  Valid values = "+FIXED_LENGTH_ENTRIES);
        }
        

		handle_ = impl_.createAndOpen(
	    		objectNameBytes_,
			 extendedAttributeBytes,
			 entryLengthAttribute,
			 entryLength,
			 keyInsertion,
			 keyLength,
			 immediateUpdate,
			 optimization,
			 publicAuthorityBytes,
			 descriptionBytes);
    	
    	
    	
    }

    
    /**
     * Deletes the user index. 
     */
    public void delete() { 
    	impl_.delete(handle_, objectNameBytes_);
    	handle_ = 0; 
    }

	/**
	 * Inserts key and value represented as Strings into the INDEX. The values
	 * are stored in the index using the translation CCSID (Currently IBM-037)
	 * 
	 * @param key
	 *            The key for the new entry
	 * @param value
	 *            The value portion of the new entry
	 * @param insertionRuleOption
	 *            The rule used for inserting the entries. Valid values are : 	 
	 *            RULE_INSERT, RULE_INSERT_REPLACE, RULE_INSERT_NO_REPLACE                                      
	 */
	public void insertEntry(String key, String value, int insertionRuleOption) throws Exception {
		byte[] keyBytes = key.getBytes("IBM-037");
		byte[] valueBytes = value.getBytes("IBM-037");

		if (handle_ == 0) {
			open(); 
		}

		if (entryLengthAttribute_ == FIXED_LENGTH_ENTRIES) {
			if (valueBytes.length > entryLength_ - keyLength_) {
				throw new Exception("value too long " + value);
			}
		} else {
			throw new Exception("EntryLengthAttribute of "
					+ entryLengthAttribute_ + " Not yet supported");
		}

		if (insertOptionBytes_ == null) {
			insertOptionBytes_ = new byte[20];
		}
		if (insertKeyValueBytes_ == null) {
			insertKeyValueBytes_ = new byte[entryLength_];
		}
		synchronized (insertKeyValueBytes_) {
			Arrays.fill(insertKeyValueBytes_, (byte) 0x40);
			if (entryLengthAttribute_ == FIXED_LENGTH_ENTRIES) {
				System.arraycopy(keyBytes, 0, insertKeyValueBytes_, 0,
						keyBytes.length);
				System.arraycopy(valueBytes, 0, insertKeyValueBytes_,
						keyLength_, valueBytes.length);
			}
			resetOptionBytes(insertOptionBytes_);
			setOptionBytesRule(insertOptionBytes_, insertionRuleOption);
			setOptionBytesOccCount(insertOptionBytes_, 1);
			if (handle_ == 0) {
				open(); 
			}
			impl_.insertEntry(handle_, insertKeyValueBytes_, insertOptionBytes_);
		} /* synchronized */
	}

	
	/**
     Inserts one or more entries into the user index using the insinxen MI instruction.   For more information, see the following:  
     http://publib.boulder.ibm.com/infocenter/iseries/v7r1m0/index.jsp?topic=/rzatk/INSINXEN.htm
     
     @param entryBytes       Byte array containing the bytes of the entry or entries to be inserted. 
     @param optionBytes      Byte array containing insertion options. 
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  IllegalObjectTypeException  If the object on the system is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void insertEntry(byte[] entryBytes, byte[] optionBytes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
		if (handle_ == 0) {
			open(); 
		}
    	impl_.insertEntry(handle_, entryBytes, optionBytes); 
    }

    
    /**
     * Returns one or more entries from the user index.  The entries are returned as two element string
     * arrays, where the first entry of each array is the key and the second entry is the value. 
     * The translation is done using the translation CCSID (Currently IBM-037). 
     * 
     *  Currently only supported for fixed length keys. 
     */
       
    public String[][] findEntries(String key, int rule, int occCount) throws Exception { 
    	String[][] returnValue = null; 

    	if (handle_ == 0) {
			open(); 
		}
    	
    	if (entryLengthAttribute_ != FIXED_LENGTH_ENTRIES) {
    		throw new Exception("Unsupported entryLengthAttribute=0x"+Integer.toHexString(0xFF & entryLengthAttribute_)); 
    	}
    	byte[] keyBytes = key.getBytes("IBM-037");
    	byte[] outputBuffer = new byte[occCount * (entryLength_)];
		byte[] findOptionBytes; 
		if (occCount == 1) { 
			if (findOptionBytes_ == null) { 
				findOptionBytes_ = new byte[40]; 
			}
			findOptionBytes = findOptionBytes_; 
		} else {
			findOptionBytes = new byte[10+4*occCount]; 
		}
				
    	synchronized (findOptionBytes) {
			setOptionBytesOccCount(findOptionBytes, occCount);
			setOptionBytesRule(findOptionBytes, rule);
			setOptionBytesArgLength(findOptionBytes, keyBytes.length); 

			impl_.findEntries(handle_, outputBuffer, findOptionBytes, keyBytes);

			int returnCount = getOptionBytesReturnCount(findOptionBytes);

			// Divide string into arrays. This is to avoid excessive object
			// creation
			// by the native code.

			returnValue = new String[returnCount][];
			if (entryLengthAttribute_ == FIXED_LENGTH_ENTRIES) {
				for (int i = 0; i < returnCount; i++) {
					int entryStart = i * entryLength_;
					returnValue[i] = new String[2];
					returnValue[i][0] = new String(outputBuffer, entryStart,
							keyLength_, "IBM-037");
					returnValue[i][1] = new String(outputBuffer, entryStart
							+ keyLength_, valueLength_, "IBM-037");
				}

			} else {
				throw new Exception("EntryLengthAttribute of "
						+ entryLengthAttribute_ + " Not yet supported");
			}
		}
		return returnValue;

    	
    }
    



    /** 
     * Returns one or more entries from the user index using the FNDINXEN MI instruction.  
     * For more information, 
     * see http://publib.boulder.ibm.com/infocenter/iseries/v7r1m0/index.jsp?topic=/rzatk/FNDINXEN.htm.
     * 
     *  @param outputBytes Byte array into which the returned entries are placed. 
     *  @param optionBytes Byte array containing the options for the query.  Also returns
     *                     information about the retrieved entries. 
     *  @param keyBytes    Byte array containing the key used to find the entries.
     */
    public void findEntries(byte[] outputBytes, byte[] optionBytes, byte[] keyBytes) throws Exception {
		if (handle_ == 0) {
			open(); 
		}
    	impl_.findEntries(handle_, outputBytes, optionBytes, keyBytes); 
    }
    
    
    
    
	/**
     Returns the String representation of this user index object.
     @return  The String representation of this user index object.
     **/
    public String toString()
    {
        return "UserIndex " + path_;
    }


    /**
    Indicates if the native methods will be used internally to perform user index insertion and retrieval requests.  
    @return  true if user index insertion and retrieval requests will be performed via native methods; false otherwise.
    @see #setMustUseNativeMethods
    **/
    
    public boolean isMustUseNativeMethods()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if user index must use native methods:", mustUseNativeMethods_);
        return mustUseNativeMethods_;
    }

    /**
    Specifies whether native methods are used by the current job to perform user index operations.
    This option can only be set to true when the application is running on the System i.  
    @param  useNativeMethods  Internally use ProgramCall to perform read and write requests.
    @see #isMustUseNativeMethods
    **/
   public void setMustUseNativeMethods(boolean useNativeMethods)
   {
       if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting if user index must use native methods:", useNativeMethods);
           mustUseNativeMethods_ = useNativeMethods;
   }

   private void open() {
	   
	handle_ = impl_.open(objectNameBytes_); 
	
	
	byte[] outputBytes = new byte[60]; 
	
	// Output is the following
	// 
	//	Offset 	Type 		Field
	//	Dec	Hex
	//	0 	0 	BINARY(4) 	Bytes returned
	//	4 	4 	BINARY(4) 	Bytes available
	//	8 	8 	CHAR(10) 	User index name
	//	18 	12 	CHAR(10) 	User index library name
	//	28 	1C 	CHAR(1) 	Entry length attribute
	//	29 	1D 	CHAR(1) 	Immediate update
	//	30 	1E 	CHAR(1) 	Key insertion
	//	31 	1F 	CHAR(1) 	Optimized processing mode
	//	32 	20 	CHAR(4) 	Reserved
	//	36 	24 	BINARY(4) 	Entry length
	//	40 	28 	BINARY(4) 	Maximum entry length
	//	44 	2C 	BINARY(4) 	Key length
	//	48 	30 	BINARY(4) 	Number of entries added
	//	52 	34 	BINARY(4) 	Number of entries removed
	//	56 	38 	BINARY(4) 	Number of retrieve operations
	
	impl_.getAttributes(outputBytes, IDXA0100, objectNameBytes_);
	
	entryLengthAttribute_ = outputBytes[28];
	entryLength_          = (0xFF & outputBytes[36]) * 0x1000000+
	                        (0xFF & outputBytes[37]) * 0x10000+
	                        (0xFF & outputBytes[38]) *0x100+
	                        (0xFF & outputBytes[39]);
	keyLength_ = (0xFF & outputBytes[44])*0x1000000+
	             (0xFF & outputBytes[45])*0x10000+
	             (0xFF & outputBytes[46])*0x100+
	             (0xFF & outputBytes[47]);
	valueLength_ = entryLength_ - keyLength_;
	
   }

   
   
   /** Utility method to reset the optionBytes. The layout of the optionBytes is the following
    *  Offset 	
    * Dec  Hex   Field Name            Data Type and Length
    * 0    0     Rule option           short
    * 2 	2 	  Argument length       unsigned short
    * 4 	4 	  Argument offset       short
    * 6 	6 	  Occurrence count      short 	
    * 8 	8 	  Return count          short
    * 10 	A     Returned index entry  (unsigned short,short)	
	             (Repeated return count times)
    * 10 	A 	 Entry length 	        unsigned short 
    * 12 	C 	 Entry offset           short
    * 
    * The occurrence count is limited to a maximum value of 4095.
    * The number of bytes in the optionBytes must be at least
    * 10 + 4 * "Occurrence Count" 
    */ 
   public static void resetOptionBytes(byte[] optionBytes) {
       Arrays.fill(optionBytes, (byte) 0x0);  
   }
   
   /**
    * Utility method to set the rule option in the option bytes 
    */
   public static void setOptionBytesRule(byte[] optionBytes, int insertionRuleOption) { 
   	optionBytes[0] = 0; 
   	optionBytes[1] = (byte) (insertionRuleOption & 0xFF); 
   }
   /**
    * Utility method to set the argument length in the option bytes 
    */
   public static void setOptionBytesArgLength(byte[] optionBytes, int argLength) {
	   	optionBytes[2] = (byte) ((argLength / 0x100) & 0xFF); 
	   	optionBytes[3] = (byte) (argLength  & 0xFF); 
  }
   /**
    * Utility method to set the occurrence count in the option bytes 
    */
   public static void setOptionBytesOccCount(byte[] optionBytes, int occurrenceCount) {
   	optionBytes[6] = (byte) ((occurrenceCount / 0x100) & 0xFF); 
   	optionBytes[7] = (byte) (occurrenceCount  & 0xFF); 
   }


   /**
    * Utility method to obtain the return count from the option bytes.
    * @param optionBytes
    * @return return count
    */
   public static int getOptionBytesReturnCount(byte[] optionBytes) { 
   	return (((int) optionBytes[8]) & 0xFF) * 0x100 + (((int) optionBytes[9]) & 0xFF);  
   }

     
    
    
}
