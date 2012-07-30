package com.ibm.jtopenlite.samples;

import com.ibm.jtopenlite.command.*;

public class CallCommand {
    public static void main(String args[]) {
	try {
	    String system = args[0];
	    String userid = args[1];
	    String password = args[2];
	    String command = args[3];
	    CommandConnection connection = CommandConnection.getConnection(
                                           system, userid, password);
	    CommandResult result = connection.execute(command);
	    System.out.println(result);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
