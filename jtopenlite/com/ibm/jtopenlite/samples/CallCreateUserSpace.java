package com.ibm.jtopenlite.samples;

import com.ibm.jtopenlite.command.CommandConnection;
import com.ibm.jtopenlite.command.CommandResult;
import com.ibm.jtopenlite.command.program.object.CreateUserSpace;

public class CallCreateUserSpace {
	public static void usage() {
		System.out.println("Usage:  java com.ibm.jtopenlite.samples.CallCreateUserSpace "+
	         "<SYSTEM> <USERID> <PASSWORD> <LIBRARY> <SPACENAME>"
	     ); 
	}
	public static void main(String[] args) {
			try { 
				CommandConnection connection = CommandConnection.getConnection(args[0],  args[1],  args[2]);
				CreateUserSpace createUserSpace = new CreateUserSpace(
						args[4], /* userSpaceName */
						args[3], /* userSpaceLibrary */
						CreateUserSpace.EXTENDED_ATTRIBUTE_NONE,      /* extendedAttribute */
						100,     /* initialSize       */
						CreateUserSpace.INITIAL_VALUE_BEST_PERFORMANCE, /* initialValue */
						CreateUserSpace.PUBLIC_AUTHORITY_USE, /* publicAuthority */
						"", /*textDescription */
						CreateUserSpace.REPLACE_NO,  /*replace*/ 
						CreateUserSpace.DOMAIN_DEFAULT, /*domain */
						CreateUserSpace.TRANSFER_SIZE_REQUEST_DEFAULT, /*transferSizeRequest */
						CreateUserSpace.OPTIMUM_SPACE_ALIGNMENT_YES /* optimumSpaceAlignment */);
				
				CommandResult result = connection.call(createUserSpace); 
				System.out.println("Command completed with "+result); 
			} catch (Exception e) {
				e.printStackTrace(System.out); 
				usage(); 
			}
	}

}
