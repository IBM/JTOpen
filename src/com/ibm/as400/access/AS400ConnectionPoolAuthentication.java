///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ConnectionPool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2008 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
import com.ibm.as400.security.auth.ProfileTokenCredential;


/**
 *  The AS400ConnectionPoolAuthentication class is used to contain information regarding 
 *  the type of authentication being used.
 *  This class is constructed by the AS400ConnectionPool class and passed to a number of underlying classes.
 *  By containing the authentication information in this class, only the public methods of AS400ConnectionPool 
 *  need to expose the authentication method (e.g. user/password, ProfileTokenCredential) to the user.  
 *  The methods of the underlying classes may begin to utilize the AS400ConnectionPoolAuthentication as a 
 *  method parameter.
 **/
class AS400ConnectionPoolAuthentication // Package scoped class
{
  // This variable will be set to one of the AS400.AUTHENTICATION_SCHEME_xxx values
  // to indicate the authentication scheme used for this object.
  private int authenticationScheme_ = -1;
  
  // For AS400.AUTHENTICATION_SCHEME_PASSWORD retain the password.
  private String password_  = null;
  
  // For AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN retain the token.
  private ProfileTokenCredential profileToken_;

  /**
   *  Constructs an AS400ConnectionPool for AS400.AUTHENTICATION_SCHEME_PASSWORD
   **/
  AS400ConnectionPoolAuthentication(String password)
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400ConnectionPoolAuthentication object (password)");
    if (password == null)
    {
        Trace.log(Trace.DIAGNOSTIC, "Parameter 'password' is null (password prompt may be used).");
        //throw new NullPointerException("password");
    }

    password_ = password;
    authenticationScheme_ = AS400.AUTHENTICATION_SCHEME_PASSWORD;
  }

  /**
   *  Constructs an AS400ConnectionPool for AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN
   **/
  AS400ConnectionPoolAuthentication(ProfileTokenCredential profileToken)
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400ConnectionPoolAuthentication object (ProfileTokenCredential)");
    if (profileToken == null)
    {
        Trace.log(Trace.ERROR, "Parameter 'profileToken' is null.");
        throw new NullPointerException("profileToken");
    }
    profileToken_ = profileToken;
    authenticationScheme_ = AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN;
  }

  /**
   *  Retrieve the authentication scheme 
   *  Current supported values: 
   *     AS400.AUTHENTICATION_SCHEME_PASSWORD
   *     AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN
   **/
  int getAuthenticationScheme()
  {
    return (authenticationScheme_);
  }
  
  /**
   *  Retrieve the password if using AS400.AUTHENTICATION_SCHEME_PASSWORD
   *  else null will be returned.
   *  @return The password
   **/
  String getPassword()
  {
    if (authenticationScheme_ == AS400.AUTHENTICATION_SCHEME_PASSWORD)
    {
      return (password_);
    }
    else
    {
      return (null);
    }
  }
  
  /**
   *  Retrieve the ProfileTokenCredential if using AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN
   *  else null will be returned.
   *  @return The profile token.
   **/
  ProfileTokenCredential getProfileToken()
  {
    if (authenticationScheme_ == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN)
    {
      return (profileToken_);
    }
    else
    {
      return (null);
    }
  }
  
  
} //End of AS400ConnectionPoolAuthentication class
