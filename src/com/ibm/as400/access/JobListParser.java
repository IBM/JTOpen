///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobListParser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.access.Trace;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Vector;
 
class JobListParser
      extends ListParser 
      implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final boolean DEBUG = false;

  private AS400Structure types_      = null;
  private AS400Structure jobInfType_ = null;

  static final Job[] NULL_LIST={};

  private Job[]           jobList_ = NULL_LIST;
  private Job             job_;

  //Used for trace/debug (no MRI)
  String[] ElemNames0 = {
          "qualified job name",
          "user name used",
          "job number used",
          "internal job number",
          "status",
          "job type",
          "Job SubType",
          "reserved",
          "Job Information Status",
          "reserved",
          "Number of Fields Returned"
  };
  /**
    * lists of names of fields
    **/
  JobListParser()
  {
    ElemNames = ElemNames0;
  }

  // *****

  AS400Structure buildElemType(AS400 system)
  {
    setSystem (system);
    if (types_ == null)
    {
      AS400DataType[] types = new AS400DataType[]
      {
        new AS400Text(10, as400_.getCcsid(), as400_),              // qualified job name     (beginning of header)
        new AS400Text(10, as400_.getCcsid(), as400_),              // User name used
        new AS400Text( 6, as400_.getCcsid(), as400_),              // Job number used        (end of header)
        new AS400ByteArray(16),                                    // internal job number    (this is JOBL0100 info)
        new AS400Text(10, as400_.getCcsid(), as400_),              // status
        new AS400Text( 1, as400_.getCcsid(), as400_),              // job type
        new AS400Text( 1, as400_.getCcsid(), as400_),              // Job subtype
        new AS400Text( 2, as400_.getCcsid(), as400_),              // reserved               (end  of JOBL0100 info)
        new AS400Text( 1, as400_.getCcsid(), as400_),              // Job information status (this is JOBL0200 info)
        new AS400Text( 3, as400_.getCcsid(), as400_),              // reserved
        intType,                                                   // number of fields returned
      };
      types_ = new AS400Structure(types);
    }
    return types_;
  }

  byte[] buildJobSelectionInfo( AS400 system,
                                String jobName,
                                String userName,
                                String jobNumber)
  {
    setSystem (system);
    if (jobInfType_ == null)
    {
      AS400DataType[] types = new AS400DataType[]
      {
        new AS400Text(10, as400_.getCcsid(), as400_), // qualified job name
        new AS400Text(10, as400_.getCcsid(), as400_),
        new AS400Text( 6, as400_.getCcsid(), as400_),
        new AS400Text( 1, as400_.getCcsid(), as400_), // job type
        new AS400Text( 1, as400_.getCcsid(), as400_), // filter on signed on user indicator
        intType,                                      // number of qual job q names
        new AS400Text(20, as400_.getCcsid(), as400_),
        intType,                                      // job q statuses
        new AS400Text(12, as400_.getCcsid(), as400_),
        intType,                                      // statuses on job q
        new AS400Text(12, as400_.getCcsid(), as400_),
        intType,                                      // active statuses
        new AS400Text(12, as400_.getCcsid(), as400_),
        intType,                                      // statuses on output q
        new AS400Text(12, as400_.getCcsid(), as400_),
      };
      jobInfType_ = new AS400Structure(types);
    }

    Object[] objs = new Object[]
    {
      jobName,   // "*ALL",
      userName,  // "*CURRENT",
      jobNumber, // "*ALL",
      "*",
      "0",
      new Integer(1),
      "*ALL",
      new Integer(1),
      "*ALL",
      new Integer(1),
      "*ALL",
      new Integer(1),
      "*ALL",
      new Integer(1),
      "*NONE"
    };
    return jobInfType_.toBytes( objs );
  }

  AS400Structure buildFieldType()
  {
    AS400DataType[] types = new AS400DataType[]
    {
      intType,    // len of field
      intType,    // key field
      new AS400Text( 1, as400_.getCcsid(), as400_), // type of data
      new AS400Text( 3, as400_.getCcsid(), as400_), // reserved
      intType     // len of data
    };
    return new AS400Structure(types);
  }

  /**
   * Copyright.
   **/
private static String getCopyright ()
{
  return Copyright.copyright;
}

  // ***
  void parseLists( AS400 system, byte[] listInfoData, byte[] receiverData ) 
  throws UnsupportedEncodingException
  {
    setSystem (system);
    parseListInfo( listInfoData );
    jobList_ = new Job[returnedRecs_];
    rowPos_ =0;
    parseReceiverData( system, receiverData );
  }

  int parseFields( Object[] header, int nextReceiverPos, byte[] receiverData )
  {
    Job job=null;

    // @A1A : Construct job object by new constract method.
    try
    {
      job = parseElemHeader( header );
    } catch (Exception e)
    {
        Trace.log(Trace.ERROR,"JobListParser parseFields : "+e);
    }

    // @A2 - Check for job information - might not have *JOBCTL authority (Blank indicates all info is returned)
    if (DEBUG) System.out.println("Char is: '" + (char)((String)header[8]).charAt(0) + "'");
    if ( (char)((String)header[8]).charAt(0) != ' ' && recLength_ != 0 )     
    {       
       return (nextReceiverPos + recLength_);
    }

    // next field at
    nextReceiverPos += 64;

    int n = ((Integer)header[10]).intValue();

    AS400Structure fieldType = buildFieldType();
    Vector initValues = new Vector(15);
    for (int i=0;i<15;i++)
        initValues.addElement(null);
    for (int i=0; i<n; i++)
    {
      Object[] fieldInfo = (Object[])fieldType.toObject( receiverData, nextReceiverPos );
    
      int keyField = ((Integer)fieldInfo[1]).intValue();

      String str = "";
      int val = 0;
      if (receiverData[nextReceiverPos+8] == (byte)0xc3)    // 'C'
      {   // Char field
        AS400Text strType = new AS400Text( ((Integer)fieldInfo[4]).intValue(), as400_.getCcsid(), as400_ );
        str = (String)strType.toObject( receiverData, nextReceiverPos+16 );

        String str2 = str.trim();
        if (keyField==101)
        {
        } else if (keyField==402)
        {
           Calendar dateTime = Calendar.getInstance();
           dateTime.clear();
           dateTime.set (Integer.parseInt(str.substring(0,3)) + 1900,// year
                         Integer.parseInt(str.substring(3,5))-1,     // month is zero based
                         Integer.parseInt(str.substring(5,7)),       // day
                         Integer.parseInt(str.substring(7,9)),       // hour
                         Integer.parseInt(str.substring(9,11)),      // minute
                         Integer.parseInt(str.substring(11,13)));    // second

             initValues.setElementAt(dateTime.getTime(),0);
        } else if (keyField==502)
        {
          // $$$ end status
        } else if (keyField==601)
        {   
          // $$$ maybe remove "*" from front of string?
          initValues.setElementAt(str2,1);
        } else if (keyField==602)
        {   // expand function type
          initValues.setElementAt( str2,2 );
        } else if (keyField==1004)
        { // if there is not complete information about a queue, don't call set queue
          initValues.setElementAt(str.substring(10),3);
          initValues.setElementAt(str.substring(0,10),4);
        } else if (keyField==1005)
        {
          // jobq priority 0-9,
          initValues.setElementAt( str2 ,5);
        } else if (keyField==1502)
        {
          //  outq priority 0-9
         initValues.setElementAt( str2 ,6);
        } else if (keyField==1903)
        {
          // $$$ status on jobQ (scheduled, held, ready to be selected)
        } else if (keyField==1906)
        {
          initValues.setElementAt(str.substring(10), 7);  // subsystem lib
          initValues.setElementAt(str.substring(0,10), 8); // subsystem name
        }
      } else
      {   // Integer field
        Integer ingeter = (Integer)intType.toObject( receiverData, nextReceiverPos+16 );

        if (keyField==304)
        {
          initValues.setElementAt(( ingeter ),9); // CPU used
        }
        else if (keyField==1401)
        {
          initValues.setElementAt(( ingeter ),10); // auxiliary IO requests
        }
        else if (keyField==1402)
        {
          initValues.setElementAt(( ingeter ),11); // interactive transactions
        }
        else if (keyField==1801)
        {
          initValues.setElementAt(( ingeter ),12); // total response time
        }
        else if (keyField==1802)
        {
          initValues.setElementAt(( ingeter ),13); // run priority
        }
        else if (keyField==1907)
        {
          initValues.setElementAt(( ingeter ),14); // pool identifier
        }
      }

      nextReceiverPos += ((Integer)fieldInfo[0]).intValue();
    }
    
    return nextReceiverPos;

  }

  Job parseElemHeader( Object[] oljl )
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   ObjectDoesNotExistException,
                   IOException,
                   UnsupportedEncodingException,
                   PropertyVetoException
  {
    String jobName=((String)oljl[0]);
    String userName=((String)oljl[1]);
    String jobNumber=((String)oljl[2]);


    String jobStatus = ((String)oljl[4]);
    String jobType = (String)oljl[5];
    String jobSubtype = (String)oljl[6];

  // @A1A : Construct Job by using new construct method.
    Job job = new Job(getSystem(),jobName,userName,jobNumber,jobStatus,jobType,jobSubtype);
    job_ = job;
    jobList_[rowPos_]=job;
    return job;
  }

  Job[] clear()
  {
    basicClear();
    jobList_ = NULL_LIST;
    return jobList_;
  }

  Job[] getJobList()
  {
    return jobList_;
  }
}
