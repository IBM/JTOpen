///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PoolItem.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.access.AS400SecurityException;  //@B4A
import java.beans.PropertyVetoException;
import java.io.IOException;  //@B4A
import java.util.Locale;      //@B2A

/**
  *  PoolItem manages a particular connection to a system.  The pool item is used to
  *  keep track of how long a connection has existed, when it was last used, and how
  *  long it has been inactive.
  **/
class PoolItem
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private AS400 AS400object_; 
  private PoolItemProperties properties_;
  private String locale_ = "";     //@B2A	what locale was used to create the AS400 object

  /**
   *
   *  Construct a PoolItem object using a system name and a user ID passed as
   *  parameters.  
   *
   *  @param systemName The name of the system where the PoolItem should exist.
   *  @param userID The name of the user.
    *  @param password The password of the user.
   *  @param secure Whether the AS400 connection should be a secure connection.
   *  @param locale The locale of the AS400 object being created.
    *  @param service The service to connect to.
    *  @param connect Whether to connect to the service number.
    *  @param threadUse Whether threads should be used to connect to the server.
    *  @param socketProperties The socket properties to assign to the new AS400 object.
    *  If null, this parameter is ignored.
   *
   **/
  PoolItem(String systemName, String userID, String password, boolean secure, Locale locale, 
           int service, boolean connect, boolean threadUse, SocketProperties socketProperties)  //@B4C
  throws AS400SecurityException, IOException  //@B4A
  {
    if (secure)
      AS400object_ = new SecureAS400(systemName, userID, password);  //@B4C
    else
      AS400object_ = new AS400(systemName, userID, password);   //@B4C
    if (locale != null)                                     //@B2A
    {
      //@B2A
      AS400object_.setLocale(locale);                     //@B2A
      locale_ = locale.toString();                        //@B2A
    }                                                       //@B2A
    else                                                    //@B2A
      locale_ = "";                                   //@B2A
    properties_ = new PoolItemProperties();
    try
    {
      AS400object_.setGuiAvailable(false);
    }
    catch (PropertyVetoException e)
    {
    }
    if (!threadUse)                                     //@B4A
    {
      //@B4A
      try                                             //@B4A
      {
        //@B4A                                                       
        AS400object_.setThreadUsed(false);          //@B4A
      }                                               //@B4A
      catch (java.beans.PropertyVetoException e)      //@B4A
      {
        //@B4A
        //Ignore                                    //@B4A    
      }                                               //@B4A
    }                                                   //@B4A
    if (socketProperties != null)
    {
      AS400object_.setSocketProperties(socketProperties);
    }
    if (connect)                                        //@B4A
    {
      //@B4A
      AS400object_.connectService(service);           //@B4A
    }                                                   //@B4A
    else                                                //@B4A
    {
      //@B4A                                                                                                    //@B4A
      AS400object_.connectService(AS400.SIGNON);      //@B4A
      AS400object_.disconnectService(AS400.SIGNON);   //@B4A
    }                                                   //@B4A
  }


  /**
  *  Returns the AS400 contained in the pool item.
  *  @return The AS400 object.
  **/
  AS400 getAS400Object()
  {
    return AS400object_;
  }

  /**
   *  Returns the elapsed time the connection has been idle waiting in the pool.
   *  @return The idle time.
   **/
  long getInactivityTime()
  {
    return properties_.getInactivityTime();
  }


  /**
   *  Returns the elapsed time the connection has been in use.
   *  @return The elapsed time.
   **/
  long getInUseTime()
  {
    return properties_.getInUseTime();
  }


  /**
   *  Returns the elapsed time the pooled connection has been alive.
   *  @return The elapsed time.
   **/
  long getLifeSpan()
  {
    return properties_.getLifeSpan();
  }


  //@B2A
  /**
   *  Returns the locale of the AS400 object.
   *  @return The locale of the AS400 object, null if none was used at connection time.
   **/
  String getLocale()
  {
    return locale_;
  }


  /**
   *  Returns the number of times the pooled connection has been used.
   *  @return The number of times used.
   **/
  int getUseCount()
  {
    return properties_.getUseCount();
  }


  /**
   *  Indicates if the pooled connection is in use.
   *  @return true if the pooled connection is in use; false otherwise.
   **/
  boolean isInUse()
  {
    return properties_.isInUse();
  }


  /**
   *  Sets the connection timer values based on the active usage state of the connection.
   *  @param inUse true if the connection is currently active; false otherwise.
   **/
  void setInUse(boolean inUse)
  {
    properties_.setInUse(inUse);
  }
}
