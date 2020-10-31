package com.ibm.jtopenlite.samples;

import com.ibm.jtopenlite.command.*;

/**
 * Sample program that uses JTOpenLite to call a command on an IBM i server.
 * 
 * @see com.ibm.jtopenlite.command.CommandConnection
 */
public class CallCommand {
	public static void main(String args[]) {
		try {
			String system = args[0];
			String userid = args[1];
			String password = args[2];
			String command;
			CommandConnection connection = CommandConnection.getConnection(
					system, userid, password);
			System.out.println("Info: The NLV of the connection is "
					+ connection.getNLV());
			for (int i = 3; i < args.length; i++) {
				command = args[i];
				System.out.println("Executing: " + command);
				CommandResult result = connection.execute(command);
				System.out.println(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
