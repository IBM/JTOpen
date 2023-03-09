/* QCServiceRecord.java
 *
 * This is free open source software distributed under the IBM Public License found
 * on the World Wide Web at http://oss.software.ibm.com/developerworks/opensource/license10.html
 * Copyright *C* 2000, Jack J. Woehr, PO Box 51, Golden, CO 80402-0051 USA jax@well.com
 * Copyright *C* 2000, International Business Machines Corporation and others. All Rights Reserved.
 */

package com.SoftWoehr.JTOpenContrib.QCDemo;

public class QCServiceRecord {
  public final String serverName;
  public final int service;
  public final com.ibm.as400.access.AS400 as400;
  public final QCServiceClient client;

  QCServiceRecord (String svrName,
                 int svc,
                 com.ibm.as400.access.AS400 a400,
                 QCServiceClient cli
                 )
    {
      serverName = svrName;
      service = svc;
      as400 = a400;
      client = cli;
    }
  }
