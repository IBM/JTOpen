///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  SpooledFileInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.components;

/**
 * Represents spooled file information returned by the ListSpooledFiles class.
 * The various toString() methods will print
 * the various fields in a format similar to what WRKSPLF does.
**/
public class SpooledFileInfo
{
  private final String jobName_;
  private final String jobUser_;
  private final String jobNumber_;
  private final String spooledFileName_;
  private final int spooledFileNumber_;
  private final int fileStatus_;
  private final String dateOpened_;
  private final String timeOpened_;
  private final String userData_;
  private final String formType_;
  private final String outputQueueName_;
  private final String outputQueueLibrary_;
  private final int asp_;
  private final long size_;
  private final int pages_;
  private final int copiesLeft_;
  private final String priority_;

  SpooledFileInfo(String jobName, String jobUser, String jobNumber, String spooledFileName,
                  int spooledFileNumber, int fileStatus, String dateOpened, String timeOpened,
                  String userData, String formType, String outputQueueName, String outputQueueLibrary,
                  int asp, long size, int pages, int copiesLeft, String priority)
  {
    jobName_ = jobName;
    jobUser_ = jobUser;
    jobNumber_ = jobNumber;
    spooledFileName_ = spooledFileName;
    spooledFileNumber_ = spooledFileNumber;
    fileStatus_ = fileStatus;
    dateOpened_ = dateOpened;
    timeOpened_ = timeOpened;
    userData_ = userData;
    formType_ = formType;
    outputQueueName_ = outputQueueName;
    outputQueueLibrary_ = outputQueueLibrary;
    asp_ = asp;
    size_ = size;
    pages_ = pages;
    copiesLeft_ = copiesLeft;
    priority_ = priority;
  }

  public String getJobName()
  {
    return jobName_;
  }

  public String getJobUser()
  {
    return jobUser_;
  }

  public String getJobNumber()
  {
    return jobNumber_;
  }

  public String getSpooledFileName()
  {
    return spooledFileName_;
  }

  public int getSpooledFileNumber()
  {
    return spooledFileNumber_;
  }

  public int getFileStatus()
  {
    return fileStatus_;
  }

  public String getDateOpened()
  {
    return dateOpened_;
  }

  public String getTimeOpened()
  {
    return timeOpened_;
  }

  public String getUserData()
  {
    return userData_;
  }

  public String getFormType()
  {
    return formType_;
  }

  public String getOutputQueueName()
  {
    return outputQueueName_;
  }

  public String getOutputQueueLibrary()
  {
    return outputQueueLibrary_;
  }

  public int getASP()
  {
    return asp_;
  }

  public long getSize()
  {
    return size_;
  }

  public int getPageCount()
  {
    return pages_;
  }

  public int getCopiesLeftToPrint()
  {
    return copiesLeft_;
  }

  public String getPriority()
  {
    return priority_;
  }

  public String getFileStatusString()
  {
    switch (fileStatus_)
    {
      case 1: return "RDY";
      case 2: return "OPN";
      case 3: return "CLO";
      case 4: return "SAV";
      case 5: return "WTR";
      case 6: return "HLD";
      case 7: return "MSGW";
      case 8: return "PND";
      case 9: return "PRT";
      case 10: return "FIN";
      case 11: return "SND";
      case 12: return "DFR";
    }
    return "";
  }

  public String toString()
  {
    String pageSpaces = "";
    if (pages_ < 10000) pageSpaces += " ";
    if (pages_ < 1000) pageSpaces += " ";
    if (pages_ < 100) pageSpaces += " ";
    if (pages_ < 10) pageSpaces += " ";
    return spooledFileName_+"  "+jobUser_+"  "+outputQueueName_+"  "+userData_+"  "+getFileStatusString()+"  "+pageSpaces+pages_+"  "+copiesLeft_;
  }

  public String toString2()
  {
    return spooledFileName_+"  "+jobUser_+"  "+formType_+"  "+priority_+"  "+dateOpened_+"  "+timeOpened_;
  }

  public String toString3()
  {
    String numSpaces = "";
    if (spooledFileNumber_ < 10000) numSpaces += " ";
    if (spooledFileNumber_ < 1000) numSpaces += " ";
    if (spooledFileNumber_ < 100) numSpaces += " ";
    if (spooledFileNumber_ < 10) numSpaces += " ";
    return spooledFileName_+"  "+numSpaces+spooledFileNumber_+"  "+jobName_+"  "+jobUser_+"  "+jobNumber_;
  }

  public String toString4()
  {
    String aspSpaces = "";
    if (asp_ < 100) aspSpaces += " ";
    if (asp_ < 10) aspSpaces += " ";
    return spooledFileName_+"  "+outputQueueName_+"  "+outputQueueLibrary_+"  "+aspSpaces+asp_+"  "+size_;
  }

  public String getTimestamp()
  {
    char[] t = new char[19];
    t[0] = '2';
    t[1] = '0';
    t[2] = dateOpened_.charAt(1);
    t[3] = dateOpened_.charAt(2);
    t[4] = '-';
    t[5] = dateOpened_.charAt(3);
    t[6] = dateOpened_.charAt(4);
    t[7] = '-';
    t[8] = dateOpened_.charAt(5);
    t[9] = dateOpened_.charAt(6);
    t[10] = ' ';
    t[11] = timeOpened_.charAt(0);
    t[12] = timeOpened_.charAt(1);
    t[13] = ':';
    t[14] = timeOpened_.charAt(2);
    t[15] = timeOpened_.charAt(3);
    t[16] = ':';
    t[17] = timeOpened_.charAt(4);
    t[18] = timeOpened_.charAt(5);
    return new String(t);
  }

/*  private String formatDate()
  {
    String year = date_.substring(1,3);
    String month = date_.substring(3,5);
    String day = date_.substring(5);
    return month+"/"+day+"/"+year;
  }

  private String formatTime()
  {
    String hour = time_.substring(0,2);
    String min = time_.substring(2,4);
    String sec = time_.substring(4);
    return hour+":"+min+":"+sec;
  }
*/
}
