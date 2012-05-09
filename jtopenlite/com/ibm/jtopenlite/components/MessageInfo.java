///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  MessageInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

/**
 * Represents message information returned by the ListQSYSOPRMessages class.
 * The toString() and toString2() methods will print
 * the various fields in a format similar to what DSPMSG QSYSOPR does.
**/
public class MessageInfo
{
  private int severity_;
  private String identifier_;
  private String type_;
  private int key_;
  private String date_;
  private String time_;
  private String microseconds_;

  private String replyStatus_;
  private String text_;

  MessageInfo(int sev, String id, String type, int key, String date, String time, String micro)
  {
    severity_ = sev;
    identifier_ = id;
    type_ = type;
    key_ = key;
    date_ = date;
    time_ = time;
    microseconds_ = micro;
  }

  public int getSeverity()
  {
    return severity_;
  }

  public String getIdentifier()
  {
    return identifier_;
  }

  public String getType()
  {
    return type_;
  }

  public int getKey()
  {
    return key_;
  }

  public String getDate()
  {
    return date_;
  }

  public String getTime()
  {
    return time_;
  }

  public String getMicroseconds()
  {
    return microseconds_;
  }

  void setReplyStatus(String s)
  {
    replyStatus_ = s;
  }

  public String getReplyStatus()
  {
    return replyStatus_;
  }

  void setText(String s)
  {
    text_ = s;
  }

  public String getText()
  {
    return text_;
  }

  public String toString()
  {
    return identifier_+": "+text_;
  }

  private String getTypeString()
  {
    char c0 = type_.charAt(0);
    char c1 = type_.charAt(1);
    switch (c0)
    {
      case '0':
        switch (c1)
        {
          case '1': return "Completion";
          case '2': return "Diagnostic";
          case '4': return "Informational";
          case '5': return "Inquiry";
          case '6': return "Sender's copy";
          case '8': return "Request";
        }
        break;
      case '1':
        switch (c1)
        {
          case '0': return "Request with prompting";
          case '4': return "Notify, exception already handled when API is called";
          case '5': return "Escape, exception already handled when API is called";
          case '6': return "Notify, exception not handled when API is called";
          case '7': return "Escape, exception not handled when API is called";
        }
        break;
      case '2':
        switch (c1)
        {
          case '1': return "Reply, not checked for validity";
          case '2': return "Reply, checked for validity";
          case '3': return "Reply, message default used";
          case '4': return "Reply, system default used";
          case '5': return "Reply, from system reply list";
          case '6': return "Reply, from exit program";
        }
        break;
    }
    return "Unknown";
  }

  private String formatDate()
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

  public String toString2()
  {
    return "Message ID: "+identifier_+"\t Severity: "+severity_+"\n"+
           "Message type: "+getTypeString()+"\n"+
           "Date sent: "+formatDate()+"\t Time sent: "+formatTime()+"\n"+
           "Message: "+text_+"\n";
  }
}
