///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CoreMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.ListResourceBundle;



/**
Locale-specific objects for IBM Toolbox for Java.
**/
//
// @B0A - Implementation note:
//        This MRI contains ONLY resources that are part of the "core".
//        This is the small set of access classes needed throughout the
//        Toolbox
//
public class CoreMRI extends ListResourceBundle
{
      public Object[][] getContents()
   {
       return resources;
   }

   private final static Object[][] resources= {

           // #TRANNOTE Before you add a new resource, please make
           // #TRANNOTE sure you are not duplicating another.  The
           // #TRANNOTE goal is to keep the amount of translatable
           // #TRANNOTE text down.
           // #TRANNOTE
           // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI
           // #TRANNOTE    is { "KEY", "value" },
           // #TRANNOTE
           // #TRANNOTE The key must be left alone so translate only the value.
           // #TRANNOTE
      { "DLG_PASSWORD_LABEL", "Password:" },
      { "DLG_SYSTEM_LABEL", "System:" },
      { "DLG_USER_ID_LABEL", "User ID:" },
      { "DLG_CANCEL_BUTTON", "Cancel" },
      { "DLG_OK_BUTTON", "OK" },
      { "DLG_CACHE_PASSWORD_CHECK_BOX", "Save password" },
      { "DLG_DEFAULT_PASSWORD_CHECK_BOX", "Default User ID" },

      { "EXC_COMMITMENT_CONTROL_ALREADY_STARTED", "Commitment control is already started." },
      { "EXC_FIELD_NOT_FOUND", "Field was not found." },
      { "EXC_LENGTH_NOT_VALID", "Length is not valid." },
      { "EXC_IMPLEMENTATION_NOT_FOUND", "Implementation class not found." }, // @B1A
      { "EXC_INFORMATION_NOT_AVAILABLE", "Information not available." }, // @B3A
      { "EXC_LICENSE_CAN_NOT_BE_REQUESTED", "License can not be requested." }, // @B3A
      { "EXC_OBJECT_CANNOT_BE_FOUND", "Object cannot be found."}, //@B2A
      { "EXC_OBJECT_CANNOT_BE_OPEN", "Object cannot be in an open state." },
      { "EXC_OBJECT_CANNOT_START_THREADS", "Object cannot start threads." },
      { "EXC_OBJECT_IS_READ_ONLY", "Object is read-only." }, //@B0A
      { "EXC_OBJECT_MUST_BE_OPEN", "Object must be open." },
      { "EXC_PARAMETER_VALUE_NOT_VALID", "Parameter value is not valid." },
      { "EXC_PROPERTY_NOT_CHANGED", "Property was not changed." },
      { "EXC_PROPERTY_NOT_SET", "Property is not set." },
      { "EXC_PATH_NOT_VALID", "Path name is not valid." },
      { "EXC_RANGE_NOT_VALID", "The parameter value is out of the allowed range." },
      { "EXC_SIGNON_CHAR_NOT_VALID", "The user ID or password contains a character that is not valid." },
      { "EXC_SIGNON_ALREADY_IN_PROGRESS", "A sign-on is already in progress." },  // @F4A
      { "EXC_UNKNOWN", "An unknown problem has occurred." },

      { "EXC_PROXY_SERVER_EVENT_NOT_FIRED", "The proxy server was not able to fire an event." },
      { "PROXY_SERVER_ALREADY_STARTED",     "Proxy server already started." },
      { "PROXY_SERVER_NOT_STARTED",         "Proxy server not started." },

   };

}

