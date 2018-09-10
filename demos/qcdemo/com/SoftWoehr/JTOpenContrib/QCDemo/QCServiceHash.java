/*
 * QCServiceHash.java
 *
 * Created on July 12, 2000, 3:50 PM
 *
 * This is free open source software distributed under the IBM Public License found
 * on the World Wide Web at http://oss.software.ibm.com/developerworks/opensource/license10.html
 * Copyright *C* 2000, Jack J. Woehr, PO Box 51, Golden, CO 80402-0051 USA jax@well.com
 * Copyright *C* 2000, International Business Machines Corporation and others. All Rights Reserved.
 */

package com.SoftWoehr.JTOpenContrib.QCDemo;

import java.util.*;

/**
 *
 * @author  jax
 * @version
 */
public class QCServiceHash extends Object {

  private Hashtable serviceSessions;

  /** Creates new QCServiceHash */
  public QCServiceHash() {
    serviceSessions = new Hashtable();
  }

  /** Clear all keys
   * @throws UnsupportedOperationException
   */
  public void clear () throws UnsupportedOperationException {
    serviceSessions.clear();
  }

  /** Map in an object
   * @param key
   * @param object
   * @return
   */
  public com.ibm.as400.access.AS400 put(QCServiceRecord key, com.ibm.as400.access.AS400 object) {
    serviceSessions.put(key, object);
    return object;
  }

  /** Get a server by name
   * @param key
   * @return
   */
  public com.ibm.as400.access.AS400 get(QCServiceRecord key) {
    return (com.ibm.as400.access.AS400) serviceSessions.get(key);
  }

  /** Remove a server by name
   * @param key
   * @return
   */
  public com.ibm.as400.access.AS400 remove(QCServiceRecord key) {
    return (com.ibm.as400.access.AS400) serviceSessions.remove(key);
  }

  /** Contains the server named?
   * @param key
   * @return
   */
  public boolean containsServiceRecord(QCServiceRecord key) {
    return serviceSessions.containsKey(key);
  }

  /** Contains reference to the identical unique AS400 object
   * @param as400
   * @return
   */
  public boolean containsServer(com.ibm.as400.access.AS400 as400) {
    return serviceSessions.containsValue(as400);
  }

  /** Are any clients left of system <I>as400</I> using service <I>service</I>?
   * @param as400 An AS400 system which might currently be connected.
   * @param service One of the AS400 service constants.
   * @return <B>true</B> if any client is still active on system <I>as400</I> using service <I>service</I>.
   */
  public boolean anyServiceClients(com.ibm.as400.access.AS400 as400,int service) {
    return false;
  }
  /** Number of elements
   * @return
   */
  public int size () {
    return serviceSessions.size();
  }

  /** All service records */
  public Enumeration keys () {
    return serviceSessions.keys();
  }

  /** Enumeration of elements elements
   * @return
   */
  public Enumeration elements () {
    return serviceSessions.elements();
  }

  /** Is empty?
   * @return
   */
  public boolean isEmpty () {
    return serviceSessions.isEmpty();
  }

}
