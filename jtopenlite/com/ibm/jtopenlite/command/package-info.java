
/**
 * <p>
 * This package provides the ability to call commands on an IBM i host.
 *
 *<p>
 *  The {@link com.ibm.jtopenlite.command.CommandConnection } class is used to establish
 *  a connection to the command server.  The following is a simple example of a program to
 *  call a command on the server.
 *
 *
 <pre>
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

 </pre>
 */
package com.ibm.jtopenlite.command;
