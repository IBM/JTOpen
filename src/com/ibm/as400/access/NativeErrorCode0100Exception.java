///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  NativeException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * This class represents an error obtained via an format ERRC0100 errorcode
 * from an OS/400 api.  The byte[] data is the compete OS/400 
 * ErrorCode information. 
 * <p>
 * Information about the OS/400 ErroroCode can be found at
 * http://pic.dhe.ibm.com/infocenter/iseries/v6r1m0/topic/apiref/error.htm
 * </p>
 *
 */
public class NativeErrorCode0100Exception extends Exception
{
    static final long serialVersionUID = 4L;
    private byte[] data = null;

    public NativeErrorCode0100Exception(byte[] data)
    {
        this.data = data;
    }

    
    /**
     * Throws the corresponding toolbox exception for the current exception. 
     * @param converter 
     * @throws ObjectDoesNotExistException  If the object does not exist.
     * @throws  AS400SecurityException  If a security or authority error occurs.
     * @throws AS400Exception If an error occurs.
     */
  public void throwMappedException(Converter converter) throws ObjectDoesNotExistException, AS400SecurityException, AS400Exception {
    String messageID = converter.byteArrayToString(data, 8, 7);
    if (messageID.equals("CPF9810")) {
      String libraryName = converter.byteArrayToString(data, 16, 10);
      Trace.log(Trace.ERROR, "Library does not exist: '" + libraryName + "'");
      throw new ObjectDoesNotExistException(
          "/QSYS.LIB/" + libraryName + ".LIB",
          ObjectDoesNotExistException.LIBRARY_DOES_NOT_EXIST);
    } else if (messageID.equals("CPF9801")) {
      String objectName = converter.byteArrayToString(data, 16, 10);
      String libraryName = converter.byteArrayToString(data, 26, 10);

      throw new ObjectDoesNotExistException("/QSYS.LIB/" + libraryName
          + ".LIB/" + objectName + ".OBJ",
          ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
    } else if (messageID.equals("CPF2105")) {
      String objectName = converter.byteArrayToString(data, 16, 10);
      String libraryName = converter.byteArrayToString(data, 26, 10);
      String typeName = converter.byteArrayToString(data, 36, 6);
      throw new ObjectDoesNotExistException("/QSYS.LIB/" + libraryName
          + ".LIB/" + objectName + "." + typeName,
          ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
    } else if (messageID.equals("CPF9802")) {
        String objectName = converter.byteArrayToString(data,16,10); 
        String libraryName = converter.byteArrayToString(data,26,10); 
        
        throw new AS400SecurityException("/QSYS.LIB/"+libraryName+".LIB/"+objectName+".OBJ", AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
        
      } else if (messageID.equals("CPF2189"))       {
        String objectName = converter.byteArrayToString(data,16,10); 
        String libraryName = converter.byteArrayToString(data,26,10);
        String typeName    =converter.byteArrayToString(data,36,6); 
          throw new AS400SecurityException("/QSYS.LIB/"+libraryName+".LIB/"+objectName+"."+typeName, AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
      } else       if (messageID.equals("CPF9820") || messageID.equals("CPF2182")) {
        String libraryName = converter.byteArrayToString(data,16,10);
        throw new AS400SecurityException("/QSYS.LIB/"+libraryName+".LIB", AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
      }
      int substitutionDataLength = data.length - 16; 
      byte[] substitutionData = new byte[substitutionDataLength];
      for (int i = 0; i < substitutionDataLength; i++) {
        substitutionData[i] = data[16+i]; 
      }
      throw new AS400Exception(new AS400Message(messageID, 
          null, /* text*/ 
          null, /* fileName*/
          null, /* libraryName */
          30,   /* severity */ 
          AS400Message.ESCAPE,    /* type */ 
          substitutionData, 
          null, /* help */ 
          null, /* date */ 
          null, /* time */
          null)); /* defaultReply */ 

    }
    
    
}
