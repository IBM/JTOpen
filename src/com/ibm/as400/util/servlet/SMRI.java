///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import java.util.ListResourceBundle;

/**
Locale-specific objects for the IBM Toolbox for Java.
**/
public class SMRI extends ListResourceBundle
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
   // NLS_MESSAGEFORMAT_NONE
   // Each string is assumed NOT to be processed by the MessageFormat class.
   // This means that a single quote must be coded as 1 single quote.

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



           // #TRANNOTE #####################################################
           // #TRANNOTE Short descriptions and display names for events.
           // #TRANNOTE Descriptions start with EVT_DESC_ prefix, display
           // #TRANNOTE names start with EVT_NAME.
           // #TRANNOTE #####################################################

      { "EVT_DESC_SC_EVENT", "A section completed event has occurred." },

           // #TRANNOTE A section completed has occurred.
      { "EVT_NAME_SC_EVENT", "sectionCompletedEvent" },
      { "EVT_DESC_RD_EVENT", "A row data event has occurred." },

           // #TRANNOTE A row data event has occurred.
      { "EVT_NAME_RD_EVENT", "rowDataEvent" },

           // #TRANNOTE A bound property has changed.
           // #TRANNOTE The meaning of 'property' here is like 'attribute'.
      { "EVT_NAME_PROPERTY_CHANGE", "propertyChange" },
      { "EVT_DESC_PROPERTY_CHANGE", "A bound property has changed." },

           // #TRANNOTE A constrained property has changed.
           // #TRANNOTE The meaning of 'property' here is like 'attribute'.
      { "EVT_NAME_PROPERTY_VETO", "vetoableChange" },
      { "EVT_DESC_PROPERTY_VETO", "A constrained property has changed." },

           // #TRANNOTE #########################################################
           // #TRANNOTE Common exception messages.
           // #TRANNOTE #########################################################
           // #TRANNOTE Each key starts with the prefix EXC_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE Example:
           // #TRANNOTE { "EXC_ACCESS_DENIED", "Access to request was denied." },
           // #TRANNOTE #########################################################





           // #TRANNOTE #####################################################
           // #TRANNOTE Short descriptions and display names for properties.
           // #TRANNOTE Descriptions start with PROP_DESC_ prefix, display
           // #TRANNOTE names start with PROP_NAME.
           // #TRANNOTE #####################################################
      { "PROP_NAME_METADATA", "metadata" },
      { "PROP_DESC_METADATA", "The metadata of the list." },

      { "PROP_NAME_CURRENTPOSITION", "currentPosition" },
      { "PROP_DESC_CURRENTPOSITION", "The current row position in the list." },

      { "PROP_NAME_LENGTH", "length" },
      { "PROP_DESC_LENGTH", "The length of the list." },

      { "PROP_NAME_RESULTSET", "resultSet" },
      { "PROP_DESC_RESULTSET", "The SQL ResultSet." },

      { "PROP_NAME_RECORDFORMAT", "recordFormat" },
      { "PROP_DESC_RECORDFORMAT", "The record format of the record list." },

      { "PROP_NAME_HEADERLINKS", "links" },
      { "PROP_TA_DESC_HEADERLINKS", "The HTML hyperlinks of the table header." },
      { "PROP_FO_DESC_HEADERLINKS", "The HTML hyperlinks of the form header." },

      { "PROP_NAME_TABLE", "table" },
      { "PROP_DESC_TABLE", "The HTMLTable object of the converter." },

      { "PROP_NAME_MAXTABLESIZE", "maxTableSize" },
      { "PROP_DESC_MAXTABLESIZE", "The maximum number of rows in the table." },

      { "PROP_NAME_COLUMNCOUNT", "columnCount" },
      { "PROP_DESC_COLUMNCOUNT", "The number of columns in the list." },

      { "PROP_NAME_PATH", "path" },                               //$B1A
      { "PROP_DESC_PATH", "The servlet path information." },      //$B1A

      { "PROP_NAME_RESPONSE", "response" },                       //$B1A
      { "PROP_DESC_RESPONSE", "The http servlet response." },     //$B1A

      { "PROP_DESC_SHUTDOWN", "The connection pool is shutting down..." },     //$B2A

      { "PROP_DESC_CLEANUP", "cleaning up connection pool..." },               //$B2A

      { "PROP_DESC_SHUTDOWNCOMP", "shutdown completed." },                     //$B2A

      { "PROP_DESC_USEPOOL", "using the connection pool" },                    //$B2A

      { "PROP_DESC_CREATEPOOL", "creating new connection pool..." },           //$B2A

      { "PROP_DESC_NOTUSEPOOL", "not using the connection pool" },             //$B2A

      { "PROP_DESC_CLEANUPEXT", "cleaning up existing connection pool..." },   //$B2A

      { "PROP_DESC_POOL", "setting connection pool..." },                      //$B3C

         // #TRANNOTE Authenticting a userid to a system.
      { "PROP_DESC_AUTHENTICATE", "authenticating &0 to &1..." },              //$B2A

         // #TRANNOTE Authenticting a system / userid.
      { "PROP_DESC_AUTHENTICATING", "authenticating &0 / &1..." },             //$B2A

         // #TRANNOTE system / userid has been authenticated.
      { "PROP_DESC_AUTHENTICATED", "&0 to &1 authenticated" },                 //$B2A

      { "PROP_DESC_AUTHFAILED", "System authentication failed" },              //$B2A @550

         // #TRANNOTE authentication failed for userid - (messages from error)
      { "PROP_DESC_AUTHENTICATEFAILED", "authentication failed for &0 - &1" }, //$B2A

      { "PROP_DESC_NEWVALIDATE", "new validation" },                           //$B2A

      { "PROP_DESC_OLDVALIDATE", "previously validated" },                     //$B2A

      { "PROP_DESC_INITFAILED", "failed to get host name for localhost - using local host as realm name" }, //$B2A

         // #TRANNOTE challenging the credentials to a system.
      { "PROP_DESC_CHALLENGE", "challenging credentials to &0..." },          //$B2A

         // #TRANNOTE servicing an HTTP request for a system at some IP address
      { "PROP_DESC_SERVICE", "servicing request for &0 &1..." },               //$B2A

         // #TRANNOTE  HTTP request failed for userid - (messages from error)
      { "PROP_DESC_REQFAILED", "request failed for &0 - &1" },                 //$B2A

         // #TRANNOTE  HTTP request completed for system and IP address
      { "PROP_DESC_REQCOMPLETED", "request completed for &0 &1" },             //$B2A

      { "PROP_DESC_REALMFAILED", "failed to get host name for localhost" },    //$B2A

      { "PROP_DESC_RL_CURRENTPOSITION", "The current row position in the resource list." },  //$B4A

      { "PROP_DESC_RL_LENGTH", "The length of the resource list." },           //$B4A

      { "PROP_NAME_RESOURCELIST", "resourceList" },                            //$B4A
      { "PROP_DESC_RESOURCELIST", "The resource list." },                      //$B4A

      { "PROP_NAME_RL_COLUMNATTRIBUTE", "columnAttributeIDs" },                 //$B4A
      { "PROP_DESC_RL_COLUMNATTRIBUTE", "The column attributes." },             //$B4A

      { "PROP_NAME_RL_NAME", "Name" }                                           //$B4A

   };

}
