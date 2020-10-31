package com.ibm.jtopenlite.samples;

import com.ibm.jtopenlite.command.*;
import com.ibm.jtopenlite.command.program.journal.RetrieveJournalEntries;
import com.ibm.jtopenlite.command.program.journal.RetrieveJournalEntriesListener;
import com.ibm.jtopenlite.command.program.journal.RetrieveJournalEntriesSelection;

/**
 * Sample program that uses JTOpenLite to retrieve the list of authorized user on the system. 
 * @see com.ibm.jtopenlite.command.CommandConnection 
 */
public class CallRetrieveJournalEntries implements RetrieveJournalEntriesListener {
    public static void main(String args[]) {
	try {
	    String system = args[0];
	    String userid = args[1];
	    String password = args[2];
	    String journalLibrary = args[3]; 
	    String journalName = args[4]; 
	    CommandConnection connection = CommandConnection.getConnection(
                                           system, userid, password);
	    RetrieveJournalEntriesListener listener = new CallRetrieveJournalEntries();
	    String format=RetrieveJournalEntries.FORMAT_RJNE0100; 
	    RetrieveJournalEntries retrieveJournalEntries  = 
	    		new RetrieveJournalEntries(160000, journalName, journalLibrary, format, listener);
	    RetrieveJournalEntriesSelection selection = new RetrieveJournalEntriesSelection(); 
	    selection.addEntry(RetrieveJournalEntries.KEY_NUMBER_OF_ENTRIES, 100);
	    retrieveJournalEntries.setSelectionListener(selection);
		System.out.println("Calling command"); 
	    CommandResult result = connection.call(retrieveJournalEntries);
	    System.out.println("Printing result"); 
	    System.out.println(result);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    
	public void newJournalEntries(int numberOfEntriesRetrieved,
			char continuationHandle) {
		
	}

	public void newEntryData(int pointerHandle, long sequenceNumber,
			char journalCode, String entryType, String timestamp,
			String jobName, String userName, String jobNumber,
			String programName, String object, int count, char indicatorFlag,
			long commitCycleIdentifier, String userProfile, String systemName,
			String journalIdentifier, char referentialConstraint, char trigger,
			char incompleteData, char objectNameIndicator,
			char ignoreDuringJournalChange, char minimizedEntrySpecificData) {
		
		System.out.println("Retrieved entry #"+sequenceNumber+
				" CODE="+journalCode+
				" TYPE="+entryType+
				" TS="+timestamp+
				" JOB="+jobName+"/"+userName+"/"+jobNumber+
				" PROGRAM="+programName+
				" OBJECT="+object+
				" USER="+userProfile); 
		
	}

}
