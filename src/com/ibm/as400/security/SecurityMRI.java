package com.ibm.as400.security;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SecurityMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import java.util.*;
/**
 * Defines locale-specific objects for security-related
 * components of the IBM Toolbox for Java.
 **/
public class SecurityMRI extends ListResourceBundle
{
     // NLS_MESSAGEFORMAT_NONE
     // Each string is assumed NOT to be processed by the MessageFormat class.
     // This means that a single quote must be coded as 1 single quote.

     // NLS_ENCODING=UTF-8
     // Instructs the translation tools to interpret the text as UTF-8.

     // Note: It is a build/translation requirement that this NOT refer
     // to the Copyright class. The requirement is that the MRI
     // builds do not depend on the code builds.)
     private final static Object[][] resources= {
          // #TRANNOTE #####################################################
          // #TRANNOTE Before you add a new resource, please make
          // #TRANNOTE sure you are not duplicating another. The goal is to
          // #TRANNOTE keep the amount of translatable text down.
          // #TRANNOTE
          // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI is
          // #TRANNOTE { "KEY", "value" },
          // #TRANNOTE
          // #TRANNOTE The key must be left alone. Translate only the value.
          // #TRANNOTE #####################################################

          // #TRANNOTE #####################################################
          // #TRANNOTE Short descriptions and display names for properties.
          // #TRANNOTE Descriptions start with PROP_DESC_ prefix, display
          // #TRANNOTE names start with PROP_NAME.
          // #TRANNOTE #####################################################

          // #TRANNOTE #####################################################
          // #TRANNOTE Common AS400Credential properties.
          // #TRANNOTE #####################################################
          { "PROP_NAME_CR_CURRENT", "current" },
          { "PROP_DESC_CR_CURRENT", "Valid for authentication purposes." },

          { "PROP_NAME_CR_DESTROYED", "destroyed" },
          { "PROP_DESC_CR_DESTROYED", "Is destroyed." },

          { "PROP_NAME_CR_PRINCIPAL", "principal" },
          { "PROP_DESC_CR_PRINCIPAL", "The associated principal." },

          { "PROP_NAME_CR_RENEWABLE", "renewable" },
          { "PROP_DESC_CR_RENEWABLE", "Capable of being refreshed." },

          { "PROP_NAME_CR_TIMED", "timed" },
          { "PROP_DESC_CR_TIMED", "Expires based on time." },

          { "PROP_NAME_CR_TIMETOEXPIRATION", "timeToExpiration" },
          { "PROP_DESC_CR_TIMETOEXPIRATION", "The number of seconds before expiration." },

          // #TRANNOTE #####################################################
          // #TRANNOTE Password credential properties.
          // #TRANNOTE #####################################################
          { "PROP_NAME_CR_PW_PASSWORD", "password" },
          { "PROP_DESC_CR_PW_PASSWORD", "The password value." },

          // #TRANNOTE #####################################################
          // #TRANNOTE Profile Handle credential properties.
          // #TRANNOTE #####################################################
          { "PROP_NAME_CR_PH_HANDLE", "handle" },
          { "PROP_DESC_CR_PH_HANDLE", "The profile handle bytes." },

          // #TRANNOTE #####################################################
          // #TRANNOTE Profile Token credential properties.
          // #TRANNOTE #####################################################
          { "PROP_NAME_CR_PT_TIMEOUTINTERVAL", "timeoutInterval" },
          { "PROP_DESC_CR_PT_TIMEOUTINTERVAL", "The number of seconds before expiration assigned when the token is created or refreshed." },

          { "PROP_NAME_CR_PT_TOKEN", "token" },
          { "PROP_DESC_CR_PT_TOKEN", "The profile token bytes." },

          { "PROP_NAME_CR_PT_TYPE", "type" },
          { "PROP_DESC_CR_PT_TYPE", "The profile token type." },

          // #TRANNOTE #####################################################
          // #TRANNOTE Common AS400Principal properties.
          // #TRANNOTE #####################################################
          { "PROP_NAME_PR_NAME", "name" },
          { "PROP_DESC_PR_NAME", "The principal name." },

          { "PROP_NAME_PR_USERPROFILENAME", "userProfileName" },
          { "PROP_DESC_PR_USERPROFILENAME", "The user profile name." },

          { "PROP_NAME_PR_USER", "user" },
          { "PROP_DESC_PR_USER", "The associated User object." },

          // #TRANNOTE #####################################################
          // #TRANNOTE Short descriptions and display names for events.
          // #TRANNOTE Descriptions start with EVT_DESC_ prefix, display
          // #TRANNOTE names start with EVT_NAME.
          // #TRANNOTE #####################################################
          { "EVT_NAME_CR_EVENT", "credentialEvent" },
          { "EVT_DESC_CR_EVENT", "A credential event has occurred." },

          // #TRANNOTE #####################################################
          // #TRANNOTE Dialog MRI.
          // #TRANNOTE #####################################################

          // #TRANNOTE #####################################################
          // #TRANNOTE Common exception messages.
          // #TRANNOTE #####################################################
          // #TRANNOTE Each key starts with the prefix EXC_ and
          // #TRANNOTE then a short identifier to describe the
          // #TRANNOTE message.
          // #TRANNOTE #####################################################

          // #TRANNOTE #####################################################
          // #TRANNOTE The following are error and informational messages.
          // #TRANNOTE #####################################################
   };
/**
 * Returns the array of all locale-specific
 * objects defined.
 *
 */
public Object[][] getContents() {
     return resources;
}
}

