///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveJournalEntriesSelectionListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2014 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.journal;


import com.ibm.jtopenlite.Conv;

/**
 * Sample implementation of RetrieveJournalEntriesSelectionListener.
 * 
 * Note: This implementation does not check the validity of the input parameters. 
 * Refer to the API documentation 
 * (http://publib.boulder.ibm.com/infocenter/iseries/v5r4/index.jsp?topic=%2Fapis%2FQJORJRNE.htm) 
 * for information about the parameters. 
 */
public class RetrieveJournalEntriesSelection implements RetrieveJournalEntriesSelectionListener
{
   int allocatedSize; 
   int recordCount;
   int[] keys;
   byte[][] data; 
	

   public RetrieveJournalEntriesSelection() { 
	   allocatedSize = 10; 
	   recordCount = 0; 
	   keys = new int[allocatedSize]; 
	   data = new byte[allocatedSize][]; 
   }
	
   /**
    * check that entry can be safely added.  If not
    * expand the storage. 
    */
   private void beforeAdd() { 
	 if (recordCount == allocatedSize) { 
		 int newAllocatedSize = allocatedSize + 10; 
		 int[] newKeys = new int[newAllocatedSize]; 
		 byte[][] newData = new byte[newAllocatedSize][];

		 for (int i = 0 ; i < recordCount; i++) { 
			 newKeys[i] = keys[i]; 
			 newData[i] = data[i]; 
		 }
		 keys = newKeys; 
		 data = newData;
		 allocatedSize = newAllocatedSize; 
		 
	 }
   }
   
   synchronized public void addEntry(int key, int value) {
	  beforeAdd();
	  keys[recordCount] = key;
	  data[recordCount] = Conv.intToByteArray(value); 
	  recordCount++; 
   }
   
   synchronized public void addEntry(int key, String value) { 
	   beforeAdd();
		  keys[recordCount] = key;
		  data[recordCount] = Conv.stringToEBCDICByteArray37(value);  
		  recordCount++; 

   }

   synchronized public void addEntry(int key, byte[] value) {
	  beforeAdd();
	  keys[recordCount] = key;
	  data[recordCount] = value; 
	  recordCount++; 
   }
   

   synchronized public int getNumberOfVariableLengthRecords() {
	   return recordCount; 
  }
   synchronized public int getVariableLengthRecordKey(int index) {
	   return keys[index]; 
  }

   synchronized public int getVariableLengthRecordDataLength(int index) {
	   return data[index].length; 
   }
   
   synchronized public void setVariableLengthRecordData(int index, byte[] buffer, int offset) {
	   byte[] fromData = data[index]; 
	   System.arraycopy(fromData, 0, buffer, offset, fromData.length);
   }
  
}
