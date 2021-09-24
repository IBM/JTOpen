package com.ibm.jtopenlite.samples;

import com.ibm.jtopenlite.command.*;
import com.ibm.jtopenlite.command.program.security.RetrieveAuthorizedUsers;
import com.ibm.jtopenlite.command.program.security.RetrieveAuthorizedUsersListener;

/**
 * Sample program that uses JTOpenLite to retrieve the list of authorized 
 * users on the system. 
 * @see com.ibm.jtopenlite.command.CommandConnection 
 */
public class CallRetrieveAuthorizedUsers implements RetrieveAuthorizedUsersListener {
    public static void main(String args[]) {
	try {
	    String system = args[0];
	    String userid = args[1];
	    String password = args[2];
	    CommandConnection connection = CommandConnection.getConnection(
                                           system, userid, password);
	    RetrieveAuthorizedUsers retrieveAuthorizedUsers = 
	    		new RetrieveAuthorizedUsers(RetrieveAuthorizedUsers.FORMAT_AUTU0150, 
	    				65535, 
	    				RetrieveAuthorizedUsers.SELECTION_ALL,
	    				RetrieveAuthorizedUsers.STARTING_PROFILE_FIRST, 
	    				true, 
	    				RetrieveAuthorizedUsers.GROUP_NONE,
	    				RetrieveAuthorizedUsers.ENDING_PROFILE_LAST);
	    RetrieveAuthorizedUsersListener listener = new CallRetrieveAuthorizedUsers();
		retrieveAuthorizedUsers.setListener(listener ); 
		System.out.println("Calling command"); 
	    CommandResult result = connection.call(retrieveAuthorizedUsers);
	    System.out.println("Printing result"); 
	    System.out.println(result);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

	public void newEntry(String profileName, boolean isGroup,
			boolean hasMembers, String textDescription, String[] groupProfiles) {
		System.out.println("Entry: "+profileName+" isGroup="+isGroup+" description="+textDescription); 
	}
}
