///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PTFGroupList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

 
/**
* Retrieves a list of all PTF groups that are known to a system. You can then use PTFGroup to
* get detailed information for a specific PTF group.
**/
public class PTFGroupList
{
  private AS400 system_;  

                                                         
  /**
   * Constructs a PTFGroupList object. 
   * @param system The system.
  **/
  public PTFGroupList(AS400 system)
  {
      if(system == null) throw new NullPointerException("system");
      setSystem(system);
  }

                       
  /**
   * Returns a list of all PTF groups that are known to the system.
   * @return The array of PTFGroups. 
  **/
  public PTFGroup[] getPTFGroup()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    try
    {
      int ccsid = system_.getCcsid();
      ConvTable conv = ConvTable.getTable(ccsid, null);

      ProgramParameter[] parms = new ProgramParameter[4];
      parms[0] = new ProgramParameter(conv.stringToByteArray(PTFGroup.USERSPACE_NAME));        //qualified user space name
      parms[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);
      parms[1] = new ProgramParameter(conv.stringToByteArray("LSTG0100"));        //Format name
      parms[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE);
      parms[2] = new ProgramParameter(BinaryConverter.intToByteArray(ccsid));     //CCSID
      parms[2].setParameterType(ProgramParameter.PASS_BY_REFERENCE);
      parms[3] = new ProgramParameter(new byte[4]);                               // error code
      parms[3].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      ServiceProgramCall pc = new ServiceProgramCall(system_, "/QSYS.LIB/QPZGROUP.SRVPGM", "QpzListPtfGroups", ServiceProgramCall.NO_RETURN_VALUE, parms);

      // Determine the needed scope of synchronization.
      Object lockObject;
      boolean willRunProgramsOnThread = pc.isStayOnThread();
      if (willRunProgramsOnThread) {
        // The calls will run in the job of the JVM, so lock for entire JVM.
        lockObject = PTFGroup.USERSPACE_NAME;
      }
      else {
        // The calls will run in the job of the Remote Command Host Server, so lock on the connection.
        lockObject = system_;
      }

      byte[] buf = null;
      synchronized(lockObject)        
      {
        UserSpace us = new UserSpace(system_, PTFGroup.USERSPACE_PATH);
        us.setMustUseProgramCall(true);
        if (!willRunProgramsOnThread)
        {
          us.setMustUseSockets(true);
          // Force the use of sockets when running natively but not on-thread.
          // We have to do it this way since UserSpace will otherwise make a native ProgramCall, and will use a different QTEMP library than that used by the host server.
        }
        us.create(256*1024, true, "", (byte)0, "User space for PTF Group list", "*EXCLUDE");
        try
        {
          if (!pc.run())
          {
            throw new AS400Exception(pc.getMessageList());
          }
          int size = us.getLength();
          buf = new byte[size];
          us.read(buf, 0);
        }
        finally
        {
          try { us.close(); }
          catch (Exception e) {
            Trace.log(Trace.ERROR, "Exception while closing temporary userspace", e);
          }
        }
      }
      int startingOffset = BinaryConverter.byteArrayToInt(buf, 124);      
      int numEntries = BinaryConverter.byteArrayToInt(buf, 132);
      int entrySize = BinaryConverter.byteArrayToInt(buf, 136);
      int entryCCSID = BinaryConverter.byteArrayToInt(buf, 140);
      conv = ConvTable.getTable(entryCCSID, null);
      int offset = 0;
      PTFGroup[] ptfs = new PTFGroup[numEntries];
      for (int i=0; i<numEntries; ++i)
      {
        offset = startingOffset + (i*entrySize);
        String ptfGroupName = conv.byteArrayToString(buf, offset, 60);
        offset += 60;
        String ptfGroupDescription = conv.byteArrayToString(buf, offset, 100);
        offset += 100;
        int ptfGroupLevel = BinaryConverter.byteArrayToInt(buf, offset);
        offset += 4;
        int ptfGroupStatus = BinaryConverter.byteArrayToInt(buf, offset);
        offset += 4;
        ptfs[i] = new PTFGroup(system_, ptfGroupName, ptfGroupDescription, ptfGroupLevel, ptfGroupStatus);
      }
      return ptfs;
    }
    catch (PropertyVetoException pve) { // will never happen, but the compiler doesn't know that
      Trace.log(Trace.ERROR, pve);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, pve.getMessage());
    }
  }
  
  /**
   * Sets the system.
   * @param system The system used to get a list of PTF groups.
  **/
  public void setSystem(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");

    system_ = system;
  }

  /**
  * Returns the system used to get a list of PTF groups.
  * @return the system name.
  **/
  public AS400 getSystem()
  {
      return system_;
  }

}
