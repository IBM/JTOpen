///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  PortMapper.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

import java.io.*;
import java.net.*;

/**
 * Connects to the System i Port Mapper host server (QSYSWRK/QZSOMAPD daemon job) listening on TCP port 449.
**/
public class PortMapper
{
  /**
   * Constant representing the System i Signon Host Server.
  **/
  public static final String SIGNON_SERVICE = "as-signon";

  /**
   * Constant representing the System i Remote Command Host Server.
  **/
  public static final String COMMAND_SERVICE = "as-rmtcmd";

  /**
   * Constant representing the System i DDM/DRDA Host Server.
  **/
  public static final String DDM_SERVICE = "drda"; //"as-ddm";

  /**
   * Constant representing the System i File Host Server.
  **/
  public static final String FILE_SERVICE = "as-file";

  /**
   * Constant representing the System i Database Host Server.
  **/
  public static final String DATABASE_SERVICE = "as-database";

  private PortMapper()
  {
  }

  /**
   * Issues a request to the Port Mapper host server on the specified system
   * to determine the TCP/IP port the specified service is listening on.
  **/
  public static int getPort(String system, String service) throws IOException
  {
    Socket portMapper = new Socket(system, 449);
    InputStream in = portMapper.getInputStream();
    OutputStream out = portMapper.getOutputStream();
    try
    {
      // Port mapper request.
      byte[] serviceName = service.getBytes("ISO_8859-1");
      out.write(serviceName);
      out.flush();

      // Unused variable
      // int portNum = -1;

      int i = in.read();
      if (i == 0x002B)
      {
        // unused variable
        // int num = 0;
        int b1 = in.read();
        if (b1 < 0) throw new EOFException();
        int b2 = in.read();
        if (b2 < 0) throw new EOFException();
        int b3 = in.read();
        if (b3 < 0) throw new EOFException();
        int b4 = in.read();
        if (b4 < 0) throw new EOFException();
        return ((b1 & 0x00FF) << 24) |
               ((b2 & 0x00FF) << 16) |
               ((b3 & 0x00FF) << 8) |
               (b4 & 0x00FF);
      }
      else
      {
        throw new IOException("Bad result from port mapper: "+i);
      }
    }
    finally
    {
      // The port mapper host server only handles one request per socket, apparently.
      in.close();
      out.close();
      portMapper.close();
    }
  }
}

