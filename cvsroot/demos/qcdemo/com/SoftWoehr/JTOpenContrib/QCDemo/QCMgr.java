/*
 * QCMgr.java
 *
 * Created on July 12, 2000, 2:37 AM
 *
 * This is free open source software distributed under the IBM Public License found
 * on the World Wide Web at http://oss.software.ibm.com/developerworks/opensource/license10.html
 * Copyright *C* 2000, Jack J. Woehr, PO Box 51, Golden, CO 80402-0051 USA jax@well.com
 * Copyright *C* 2000, International Business Machines Corporation and others. All Rights Reserved.
 */

package com.SoftWoehr.JTOpenContrib.QCDemo;

import java.util.*;
import com.ibm.as400.access.AS400;

/** Simple manager for multiple shared AS400 connections. Clients of class
 * QCServiceClient make a request for a server and service. The server is
 * an AS400. The service is one of the services AS400.FILE etc. A service
 * record is returned, which is resubmitted by the client to relinquish
 * the service. The AS400 connection is a public data member of that
 * service record. The QCMgr instance can also indicate to the QCServiceClient
 * that its connection is about to become invalid by calling the client's
 * interface function {@link QCServiceClient.relinquish()}
 *
 * The URL for the service request is of the form:
 *
 * <pre> qcmgr:serverName/SERVICE </pre>
 *
 * e.g., qcmgr:ANAS400/FILE
 *
 * A QCMgr maintains one com.ibm.as400.access.AS400 object for each server.
 * If you wish to have multiple instances of server objects, instance multiple
 * QCMgr's.
 *
 * @author jax
 * @version 1.0
 */
public class QCMgr extends Object {

  private QCHash serverHash = new QCHash();
  private QCServiceHash serviceHash = new QCServiceHash();

  /** This exception is thrown if the "url" submitted for a service
   * request is unrecognized.
   */
  public class QCUnknownProtocolException extends java.lang.Exception {
  QCUnknownProtocolException(String s) { super(s); }
  }

  /** This is thrown if a request is made for an unknown or unsupported service.
   */
  public class QCUnknownServiceException extends java.lang.Exception {
  QCUnknownServiceException(String s) { super(s); }
  }

  /** Thrown if an invalid service record is returned by a client to the
   * call {@link freeService()}.
   */
  public class QCInvalidServiceRecordException extends java.lang.Exception {
  QCInvalidServiceRecordException(String s) { super(s); }
  }

  /** Class used to parse the requests made for service.
   */
  public class Locator {

    String locator;

  Locator (String representation) { locator = representation; }

    public String toHashName ()
    throws QCUnknownProtocolException
    {
      String hashName ="";
      StringTokenizer tokenizer = new StringTokenizer(locator);
      try
      {
        String protocol= tokenizer.nextToken(":");
        if (!protocol.equals("qcmgr"))
        {
          throw new QCUnknownProtocolException("QC Unknown protocol: " + protocol);
        }                                                       /* End if*/
      }                                                        /* End try*/

      catch (NoSuchElementException e)
      {
        e.printStackTrace(System.err);
      }                                                      /* End catch*/

      try
      {
        hashName = tokenizer.nextToken(":/");
      }                                                        /* End try*/

      catch (NoSuchElementException e)
      {
        e.printStackTrace(System.err);
      }                                                      /* End catch*/

      return hashName;
    }

    public String toServerName ()
    throws QCUnknownProtocolException
    {
      return toHashName();
    }


    public String toServiceName ()
    throws QCUnknownProtocolException
    {
      String serviceName = "";
      StringTokenizer tokenizer = new StringTokenizer(locator);
      try
      {
        String protocol= tokenizer.nextToken(":");
        if (!protocol.equals("qcmgr"))
        {
          throw new QCUnknownProtocolException("QC Unknown protocol: " + protocol);
        }                                                       /* End if*/
      }                                                        /* End try*/

      catch (NoSuchElementException e)
      {
        e.printStackTrace(System.err);
      }                                                      /* End catch*/

      try
      {
        tokenizer.nextToken("/");
      }                                                        /* End try*/

      catch (NoSuchElementException e)
      {
        e.printStackTrace(System.err);
      }                                                      /* End catch*/
      try
      {
        serviceName = tokenizer.nextToken("/");
      }                                                        /* End try*/

      catch (NoSuchElementException e)
      {
        e.printStackTrace(System.err);
      }                                                      /* End catch*/
      return serviceName;
    }

    public int toService ()
    throws QCUnknownProtocolException
    {
      return convertServiceName(toServiceName());
    }


    public int convertServiceName(String serviceName) {
      int service = -1;
      if (serviceName.equals ("COMMAND"))
      {
        service = AS400.COMMAND;
      }
      else if (serviceName.equals ("DATAQUEUE"))
      {
        service = AS400.DATAQUEUE;
      }
      else if (serviceName.equals ("FILE"))
      {
        service = AS400.FILE;
      }                                                         /* End if*/
      else if (serviceName.equals ("PRINT"))
      {
        service = AS400.PRINT;
      }                                                         /* End if*/
      else if (serviceName.equals ("RECORDACCESS"))
      {
        service = AS400.RECORDACCESS;
      }                                                         /* End if*/
      return service;
    }

    public String toProtocol ()
    {
      String protocol = ":NONE:";
      StringTokenizer tokenizer = new StringTokenizer(locator);
      try
      {
        protocol= tokenizer.nextToken(":");
      }                                                          /* End try*/
      catch (NoSuchElementException e)
      {
        e.printStackTrace(System.err);
      }                                                        /* End catch*/
      return protocol;
    }
  }

  /** Creates new QCMgr */
  public QCMgr() {

  }

  /** Find a server if we have already created it.
   *  Create it if not already present.
   */
  public AS400 getServer(String serverName,String hashName) {
    AS400 a = serverHash.get(hashName);
    if (null == a) {
      a = new AS400 (serverName);
      serverHash.put(serverName, a);
    }
    return a;
  }

  /** Create a new service record */
  public QCServiceRecord createServiceRecord(String serverName,int service,AS400 as400,QCServiceClient client) throws QCUnknownServiceException {
    switch (service)
    {
      case AS400.COMMAND:   // Constant indicating the Command AS/400 service.
      case AS400.DATAQUEUE: // Constant indicating the Dataqueue AS/400 service.
      case AS400.FILE:      // Constant indicating the File AS/400 service.
      case AS400.PRINT:     // Constant indicating the Print AS/400 service.
      case AS400.RECORDACCESS: // Constant indicating the Record Access AS/400 service.
      break;
      default:
      throw new QCUnknownServiceException("Unknown service: " + service);
    }                                                         /* End switch*/

    QCServiceRecord sr = new QCServiceRecord (serverName, service, as400, client);
    return sr;
  }

  /** This function accepts a URL-like request from a {@link QCServiceClient} for a
   * com.ibm.as400.access.AS400 service on a server. A service record is
   * returned which contains the reference to the server. The service record
   * is passed back to QCMgr when the QCServiceClient is done with the service.
   * @param representation A string of the form <CODE>qcmgr:server/service</CODE>
   *
   * where <I>server</I> is the name of the desired server and
   * <I>service</I> is one of the service names for the AS400
   * object, e.g., FILE PRINT etc.
   * @param client The client must implement the interface {@link QCServiceClient}.
   * @throws QCUnknownServiceException If the name of the service is not recoqnized.
   * @throws QCUnknownProtocolException If the "protocol" portion of the URL is not <CODE>qcmgr</CODE>.
   * @return A QCServiceRecord which can be examined to obtain reference to the AS400 instance being represented. This record is passed back to the QCMgr in the functi{@link on freeServ}ice when the client is done with the service
   */
  public QCServiceRecord getService(String representation,QCServiceClient client) throws QCUnknownServiceException, QCUnknownProtocolException {
    Locator locator = new Locator(representation);

    final String serverName    = locator.toServerName();
    final String hashName      = locator.toHashName();
    final int    service       = locator.toService();

    AS400 as400 = getServer(serverName,hashName);

    QCServiceRecord sr =
    createServiceRecord(serverName,
    service,
    as400,
    client);

    serviceHash.put(sr, as400);
    return sr;
  }

  /** A  QCServiceClient uses this function to inform the QCMgr that the
   * service which was obtained is no longer needed by the client.
   * @param sr The QCServiceRecord obtained by a call to {@link QCMgr.getService()}
   * @throws QCInvalidServiceRecordException If the service record isn't valid.
   */
  public void freeService(QCServiceRecord sr) throws QCInvalidServiceRecordException {
    if (!serviceHash.containsServiceRecord(sr))
    {
      throw new QCInvalidServiceRecordException("Invalid service record: " + sr.toString());
    }
    serviceHash.remove(sr);
    if (!serviceHash.containsServer(sr.as400))
    {
      AS400 as400 = serverHash.remove(sr.serverName);
      if (as400.isConnected())
      {
        as400.disconnectAllServices();
      }                                                           /* End if*/
    }                                                             /* End if*/
    else if (!serviceHash.anyServiceClients(sr.as400, sr.service)) {
      if (sr.as400.isConnected(sr.service))
      {
        sr.as400.disconnectService(sr.service);
      }                                                           /* End if*/
    }                                                             /* End if*/
  }

  /**
   * Extract protocol portion of the representation of a service request.
   * @param representation
   * @return
   */
  public String toProtocol(String representation) {
    return new Locator(representation).toProtocol();
  }

  /**
   * Extract server portion of the representation of a service request.
   * @param representation
   * @throws QCUnknownProtocolException
   * @return
   */
  public String toServerName(String representation) throws QCUnknownProtocolException {
    return new Locator(representation).toServerName();
  }

  /**
   * Extract service portion of the representation of a service request.
   * @param representation
   * @throws QCUnknownProtocolException
   * @return
   */
  public String toServiceName(String representation) throws QCUnknownProtocolException {
    return new Locator(representation).toServiceName();
  }

  /**
   * Extract service portion of the representation of a service request
   * and convert it to an AS400 service number.
   * @param representation
   * @throws QCUnknownProtocolException
   * @return
   */
  public int toService(String representation) throws QCUnknownProtocolException {
    return new Locator(representation).toService();
  }

  /** Return a string representing an AS400 service from its integer value.
   * @param service Integer representing an AS400 service constant.
   * @return The string name of the service represented by the int.
   */
  public String intToServiceName (int service) {
    String result = "Unknown";
    switch (service)
    {
      case AS400.COMMAND:   // Constant indicating the Command AS/400 service.
      result = "COMMAND";
      break;
      case AS400.DATAQUEUE: // Constant indicating the Dataqueue AS/400 service.
      result = "DATAQUEUE";
      break;
      case AS400.FILE:      // Constant indicating the File AS/400 service.
      result = "FILE";
      break;
      case AS400.PRINT:     // Constant indicating the Print AS/400 service.
      result = "PRINT";
      break;
      case AS400.RECORDACCESS: // Constant indicating the Record Access AS/400 service.
      result = "RECORDACCESS";
      break;
    }
    return result;
  }

  /** Return a string illustrating all current service records.
   * @return A string of the form <CODE>server,service\n</CODE> ... repeated
   * for as many service records as exist.
   */
  public String serviceRecordsToString () {
    StringBuffer result = new StringBuffer();
    for (Enumeration e = serviceHash.keys(); e.hasMoreElements();) {
      QCServiceRecord sr = (QCServiceRecord) e.nextElement();
      result.append(sr.serverName + "," + intToServiceName(sr.service) + "\n");
    }
    return result.toString();
  }

  /** Return a string listing the servers active */
  public String serversToString () {
    StringBuffer result = new StringBuffer();
    for (Enumeration e = serverHash.keys(); e.hasMoreElements();) {
      result.append((String) e.nextElement() + ",");
    }
    return result.toString();
  }

  /** Test QCMgr.
   * @param argv Not used.
   */
  public static void main(String[] argv) {
    QCMgr q = new QCMgr();
    try
    {
      System.out.println("Protocol == " + q.toProtocol(argv[0]));
      System.out.println("System   == " + q.toServerName(argv[0]));
      System.out.println("Service  == " + q.toServiceName(argv[0]));
      System.out.println("Service# == " + q.toService(argv[0]));
    }                                                            /* End try*/
    catch (Exception e)
    {
      e.printStackTrace(System.err);
    }                                                          /* End catch*/
  }
}
