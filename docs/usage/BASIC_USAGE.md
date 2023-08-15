# Basic Usage

In general, functional operations require an `AS400` object. The exception is JDBC, which
follows the JDBC specifications for providing a driver to a driver manager. Please reference
[the JDBC examples](examples/JDBC.md).

There are several ways to obtain an `AS400` object. 

## Acquiring a Connection 

### Connecting locally 

If your Java code is running on IBM i and you are hoping to simply connect to the local
system as the current user profile, one can construct an AS400 object without providing
a username and password. For instance:

```java
AS400 conn = new AS400();
```

### Connecting (locally or remotely) with credentials

If you are connecting remotely, or wish to use credentials acquired programmatically, 
the credentials can be passed to the AS400 constructor. 

```java
AS400 conn = new AS400("myibmi.mydomain.net", "MYUSR", "password".toCharArray());
```

### Using DotEnv-Java-IBMi

An open source project exists that allows you to store credentials in a `.env` file or
environment variables. This is useful for several use cases. For instance:
- Developing code locally and deploying on IBM i can be done with a single code base
- Deploying applications into cloud environments where credentials are accessible via
  environment variables.

See [the GitHub project page](https://github.com/ThePrez/DotEnv-Java-IBMi) for more information

### Other ways to create the connection

Consult [the JavaDoc for the `AS400` class](https://javadoc.io/doc/net.sf.jt400/jt400/latest/com/ibm/as400/access/AS400.html)

## Using a connection

Once you have a connection (`AS400` object), you typically use that object to perform other operations. 

For example, to access a system value:

```java
AS400 conn = new AS400();
SystemValue mySysVal = new SystemValue(conn, "QHSTLOGSIZ");
Object value = mySysVal.getValue();
```

