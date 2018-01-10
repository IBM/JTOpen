///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDMRI2.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.*;

/**
Locale-specific objects for IBM Toolbox for Java.
**/
//
//        Implementation note:
//        This MRI contains ONLY resources that are NOT needed
//        in the proxy jar file.  Resources needed in the proxy jar
//        file belong in the JDMRI class.
//

public class JDMRI2 extends ListResourceBundle
{
   // NLS_MESSAGEFORMAT_NONE
   // Each string is assumed NOT to be processed by the MessageFormat class.
   // This means that a single quote must be coded as 1 single quote.

   // NLS_ENCODING=UTF-8
   // Instructs the translation tools to interpret the text as UTF-8.

   public Object[][] getContents()
   {
       return resources;
   }

   private final static Object[][] resources= {
           // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI
           // #TRANNOTE    is { "KEY", "value" },
           // #TRANNOTE
           // #TRANNOTE The key must be left alone so translate only the value.
           // #TRANNOTE



           // #TRANNOTE JDBC exception and warning messages.
      { "JD01608", "An unsupported value was replaced." },
      { "JD01H10", "Extra URL elements are ignored." },
      { "JD01H11", "Extended dynamic support is not being used." },
      { "JD01H12", "Package caching is not being used." },
      { "JD01H13", "The URL default library is not valid." },
      { "JD01H20", "Extra connection property is ignored." },
      { "JD01H30", "The active transaction was committed." },
      { "JD01S02", "Option value changed." },
      { "JD07001", "The number of parameter values set or registered does not match the number of parameters." },   // @C1C
      { "JD07006", "Data type mismatch." },
      { "JD07009", "Descriptor index not valid." },
      //@A4D  These messages are needed by proxy classes.
//    { "JD08001", "The application requester cannot establish the connection." },
//    { "JD08004", "The application server rejected the connection." },
      { "JD08003", "The connection does not exist." },
      { "JD08S01", "Communication link failure." },                   // @F1A
      { "JD22522", "CCSID value is not valid." },
      { "JD22524", "Character conversion resulted in truncation." },
      { "JD24000", "Cursor state not valid." },
      { "JD25000", "Transaction state not valid." },
      { "JD34000", "Cursor name not valid." },
      { "JD3C000", "Cursor name is ambiguous." },
      { "JD42505", "Connection authorization failure occurred." },
      { "JD42601", "A character, token, or clause is not valid or is missing." },
      { "JD42703", "An undefined column name was detected." },
      { "JD42705", "Relational database not in relational database directory." }, // @J2a
      { "JD43617", "A string parameter value with zero length was detected." }, // @A3A
      { "JDHY000", "Internal driver error." },
      { "JDHY001", "Internal server error." },
      { "JDHY004", "Data type not valid." },
      { "JDHY008", "Operation cancelled." },
      { "JDHY010", "Function sequence error." },
      { "JDHY014", "Limit on number of statements exceeded." },
      { "JDHY024", "Attribute value not valid." },
      { "JDHY090", "String or buffer length not valid." },
      { "JDHY094", "Scale not valid." },
      { "JDHY105", "Parameter type not valid." },
      { "JDHY108", "Concurrency or type option not valid." },
      { "JDHY109", "Cursor position not valid." },
      { "JDIM001", "The driver does not support this function." },
      { "JD54001", "SQL statement too long or complex." },               // @E9a
      { "JD3B001", "Savepoint does not exist or is invalid in this context." },  // @E10a
      { "JD3B501", "Savepoint already exists." },                                // @E10a     
      { "JD3B502", "Savepoint does not exist." },                                // @E10a
      { "JD01G00", "1000 open statements on this connection."},                 //@K1A
      { "JD2200M", "XML parsing error."},  //@xml2



           // #TRANNOTE Field attributes.
      { "MAXLENGTH", "MAX LENGTH" },
      { "PRECISION", "PRECISION" },
      { "SCALE", "SCALE" },


           // #TRANNOTE Text for the various terms returned by DatabaseMetaData.getXXXTerm().
      { "CATALOG_TERM", "Database" },
      { "PROCEDURE_TERM", "Procedure" },
      { "SCHEMA_TERM", "Library" },

           // #TRANNOTE Text for the various descriptions returned by DatabaseMetaData.getClientInfoProperties().
      { "CLIENT_INFO_DESC_APPLICATIONNAME", "The name of the application currently utilizing the connection." }, //@PDA jdbc40
      { "CLIENT_INFO_DESC_CLIENTUSER", "The name of the user that the application using the connection is performing work for.  This may not be the same as the user name that was used in establishing the connection." }, //@PDA jdbc40
      { "CLIENT_INFO_DESC_CLIENTHOSTNAME", "The hostname of the system the application using the connection is running on." }, //@PDA jdbc40
      { "CLIENT_INFO_DESC_CLIENTACCOUNTING", "Accounting information." }, //@PDA jdbc40
      { "CLIENT_INFO_DESC_CLIENTPROGRAMID", "Program identification information." }, //@PDA 550


   };


}

