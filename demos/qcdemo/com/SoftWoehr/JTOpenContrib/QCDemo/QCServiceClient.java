/*
 * QCServiceClient.java
 *
 * Created on July 12, 2000, 3:54 PM
 *
 * This is free open source software distributed under the IBM Public License found
 * on the World Wide Web at http://oss.software.ibm.com/developerworks/opensource/license10.html
 * Copyright *C* 2000, Jack J. Woehr, PO Box 51, Golden, CO 80402-0051 USA jax@well.com
 * Copyright *C* 2000, International Business Machines Corporation and others. All Rights Reserved.
 */

package com.SoftWoehr.JTOpenContrib.QCDemo;

/** An interface representing application objects which request services from
 * the QCMgr.
 * @author jax
 * @version 1.0
 */
public interface QCServiceClient {

  /** Called by a QCMgr, this function indicates that a service record supplied
   * earlier by the QCMGr to the QCServiceClient instance will no longer be valid
   * after this call completes.
   * @param sr The service record which the QCMgr is indicating will no longer be valid after the <B>relinquish</B> completes.
   */
  public void relinquish(QCServiceRecord sr);
}
