/**
 * Provide SOCK5 and UNIX socket support.
 *
 * <p>
 * These classes allows JT400 objects such as AS400, AS400JPing, AS400FTP 
 * connect through network encryption daemons supporting SOCK5 protocol such as
 * ssh, shadowsock and others.
 * </p>
 * 
 * <p>Example for SHH - start ssh daemon in sock5 forwarding mode</p>
 * <code>
 * ssh -D 5000 ibmuser@ibmserver -p22 -gfTN
 * </code>
 * 
 * <p>Use JT400 to connect through SOCK5 channel</p>
 * <pre>
 * final AS400 as400 = get ("localhost", "demo", "demo");
 * as400.setSock5Server("localhost:5000");
 * </pre>
 * <p>NOTE: SSH daemon determines exit point, an IBM i server at the "other side".</p>
 * 
 * <p>
 * NOTE: <br>
 * AF_UNIX connection is supported only at the unix or linux based systems.<br>
 * Listening daemon (shadowsock etc.) must support AF_UNIX bind.<br>
 * The previous example differs only in sock5Server address (example: "/tmp/shadow.sock").
 * </p>
 */
package com.ibm.as400.socket;
