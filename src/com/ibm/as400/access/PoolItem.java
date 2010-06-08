///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PoolItem.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @C1 - 2008-06-06 - Added support for ProfileTokenCredential authentication
//                    by using AS400ConnectionPoolAuthentication class.
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.security.auth.ProfileTokenCredential;
import com.ibm.as400.access.AS400SecurityException;  //@B4A
import java.beans.PropertyVetoException;
import java.io.IOException;  //@B4A
import java.util.Locale;      //@B2A

/**
  *  Manages a particular connection to a system.  The pool item is used to
  *  keep track of how long a connection has existed, when it was last used, and how
  *  long it has been inactive.
  **/
class PoolItem
{
  private AS400 AS400object_; // after object construction, never null
  private PoolItemProperties properties_;
  private Locale locale_ = null; //@C1C
  //private String locale_ = "";     //@B2A	what locale was used to create the AS400 object

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
    *  @param ccsid The CCSID to use for the new connection.
   *
   **/
  PoolItem(String systemName, String userID, AS400ConnectionPoolAuthentication poolAuth, boolean secure, Locale locale, //@C1C
           int service, boolean connect, boolean threadUse, SocketProperties socketProperties, int ccsid)  //@B4C
  throws AS400SecurityException, IOException  //@B4A
  {
    String password = null; //@C1A
    if (poolAuth.getAuthenticationScheme() == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN) //@C1A
    {
      ProfileTokenCredential profileToken = poolAuth.getProfileToken(); //@C1A
      if (secure) //@C1A
      {
        AS400object_ = new SecureAS400(systemName, profileToken); //@C1A
      }
      else
      {
        AS400object_ = new AS400(systemName, profileToken); //@C1A
      }
    }
    else // AS400.AUTHENTICATION_SCHEME_PASSWORD
    {
      password = poolAuth.getPassword(); //@C1A
      if (secure)
      {
        if (password==null)										//Stevers
        {
          if (userID.equals("*CURRENT"))							//Stevers   
            AS400object_ = new SecureAS400(systemName);			//Stevers
          else													//Stevers
            AS400object_ = new SecureAS400(systemName, userID);	//Stevers
        }
        else														//Stevers
          AS400object_ = new SecureAS400(systemName, userID, password);  //@B4C
      }
      else
      {
        if (password==null)										//Stevers
        {
          if (userID.equals("*CURRENT"))							//Stevers
            AS400object_ = new AS400(systemName);				//Stevers
          else													//Stevers
            AS400object_ = new AS400(systemName, userID);		//Stevers
        }
        else														//Stevers
          AS400object_ = new AS400(systemName, userID, password);   //@B4C
      }
    }
    
    
    if (locale != null)                                     //@B2A
    {
      //@B2A
      AS400object_.setLocale(locale);                     //@B2A
      locale_ = locale;                        //@B2A @C1C
    }                                                       //@B2A
    else                                                    //@B2A
      locale_ = null;                                   //@B2A @C1C

    properties_ = new PoolItemProperties();
    try
    {
      if ((poolAuth.getAuthenticationScheme() == AS400.AUTHENTICATION_SCHEME_PASSWORD) && (password!=null))	//Stevers //@C1C 
      {
          AS400object_.setGuiAvailable(false);
      }
      if (!threadUse)                                     //@B4A
      {
          AS400object_.setThreadUsed(false);          //@B4A
      }                                                   //@B4A
      if (socketProperties != null)
      {
        AS400object_.setSocketProperties(socketProperties);
      }
      if (ccsid != ConnectionPoolProperties.CCSID_DEFAULT)
      {
        AS400object_.setCcsid(ccsid);
      }
      if (connect)                                        //@B4A
      {
        AS400object_.connectService(service);           //@B4A
      }                                                   //@B4A
      else                                                //@B4A
      { // validate the connection
        AS400object_.connectService(AS400.SIGNON);      //@B4A
        AS400object_.disconnectService(AS400.SIGNON);   //@B4A
      }                                                   //@B4A
    }
    catch (PropertyVetoException e)  // this should never happen
    {
      Trace.log(Trace.ERROR, e);  //Ignore    
    }
  }


  /**
  *  Returns the AS400 contained in the pool item.
  *  @return The AS400 object.  Never returns null.
  **/
  AS400 getAS400Object()
  {
    return AS400object_;
  }

  /**
   *  Returns the elapsed time the connection has been idle waiting in the pool.
   *  @return The idle time (milliseconds).
   *  If the connection is currently in use, 0 is returned.
   **/
  long getInactivityTime()
  {
    return properties_.getInactivityTime();
  }


  /**
   *  Returns the elapsed time the connection has been in use
   *  since it was most recently allocated from the pool.
   *  @return The elapsed time (milliseconds).
   *  If the connection is not currently in use, 0 is returned.
   **/
  long getInUseTime()
  {
    return properties_.getInUseTime();
  }


  /**
   *  Returns the elapsed time the pooled connection has been alive.
   *  @return The elapsed time (milliseconds).
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
  Locale getLocale() //@C1C (Previously returned a String representation of Locale)
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
