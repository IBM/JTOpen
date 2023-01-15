package com.ibm.jtopenlite.samples;

import com.ibm.jtopenlite.command.*;

public class CallCommandSSL {
    public static void main(String args[]) {
	try {
	    String system = args[0];
	    String userid = args[1];
	    String password = args[2];
	    String command = args[3];
	    // The first boolean parameter indicates that SSL should be used.
	    CommandConnection connection = CommandConnection.getConnection(
                                           true, system, userid, password);
	    CommandResult result = connection.execute(command);
	    System.out.println(result);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
