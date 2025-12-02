# Add SOCK5 and UNIX channels support to JT400. 

SSH client can operate in two "daemon" modes. 

1. as port forwarding - used by desktop software in general, for simple usage. Vanilla JT400, no changes.

2. as SOCK5 proxy daemon - used by server software, for example, when JT400 is used in web server. Updated JT400 to support SOCK5.

----

## Port Frowarding - simple example (no jt400 changes required)

Start SSH to the IBM server in port forwarding mode.

```bash
ssh -L 23:localhost:23 -L 449:localhost:449 -L 8470:localhost:8470 -L 8471:localhost:8471 -L 8473:localhost:8473 -L 8474:localhost:8474 -L 8475:localhost:8475 -L 8476:localhost:8476 ibmuser@ibmserver -p22 -Nf
```

Use JT400 to connect to the SSH client daemon insted of IBM server. All communication is encrypted with SSH.

```java
final AS400 as400 = get ("localhost", "demo", "demo");
System.out.println(as400.validateSignon());
as400.close();
```

----

## SOCK5 mode - for server environment (requires jt400 SOCK5 support)

Start SSH to the IBM server in SOCK5 proxy mode. Liste on port 5000 and forward all requests to remote IBM side.

```bash
ssh -D 5000 ibmuser@ibmserver -p22 -gfTN
```

Use JT400 to connect to the SSH client daemon insted of IBM server. All communication is encrypted with SSH.

```java
final AS400 as400 = get ("localhost", "demo", "demo");
final SocketProperties sp = as400.getSocketProperties();
  sp.setProxySock("127.0.0.1");
  sp.setProxyPort(5000);
as400.setSocketProperties(sp);
System.out.println(as400.validateSignon());
as400.close();
```

```java
			final AS400JPing pingObj = new AS400JPing("localhost", AS400.CENTRAL, false);

      final SocketProperties sp = pingObj.getSocketProperties();
        sp.setProxySock("127.0.0.1");
        sp.setProxyPort(5000);

      pingObj.setPrintWriter(System.out);

			pingObj.ping(AS400.CENTRAL);
			pingObj.ping(AS400.SIGNON);
			pingObj.ping(AS400.COMMAND);
			pingObj.ping(AS400.FILE);
			pingObj.ping(AS400.PRINT);
			pingObj.ping(AS400.RECORDACCESS);

```
----

## Code changes

 - New package __com.ibm.as400.socket__
 - Changed Classes in __com.ibm.as400.access__ 
    - AS400ImplRemote (only for FTP connection)
    - SocketProperties (add SOCK5 address and port)
    - PortMapper (socket creation override; used by all JT400 connections )
    - AS400JPing (expose socket properties to allow SOCK5 settings)

