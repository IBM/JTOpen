///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPAttributeIFS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPAttributeIFS class - This class exists because we must expose all
  * object names as IFS path names instead of their object name, library
  * name and type but the underlying network print datastream uses
  * seperate attribute IDs for object name, library and sometimes type.
  * The easiest way to map back and forth between IFSpath and the object name,
  * library, and type for us it to handle it at the lowest level.  This is in
  * the NPCPAttributeIDList.java and NPAttributeValue.java code for us.
  *
  * The design to handle the QSYSObjectPathName was to create new Attribute IDs that
  * are negative for IFSPath attributes.  Each QSYSObjectPathName Attribute ID corresponds
  * to 2 attribute IDs (one for the object name and one for the library) and
  * then either 1 attribute ID for the type or a string if the type is contant.
  *
  * Instances of this class contain 3 attribute IDs and a string.  Two of the
  * attributes IDs (for the object name and library) will always be valid.  The
  * third is used for the object type and may be 0 if the type is always a constant.
  * The string is for the object type and will be a valid string if the object type
  * attribute ID is 0 or it will be null if the type is dynamic.
  * So, an instance of this class can be use to map between the IFSPath name
  * and the individual components of that path name.
  *
  * This class contains a static array of instances of this class called 'ifsAttrs'.
  * The IFS attributes IDs (the attribute IDs that are negative) can be used
  * to index into this array by taking the absolute value of that negative ID and
  * then subtracting 1 to make it 0 based.
  *
 **/
class NPAttributeIFS
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    //@A1C - Now there are 14 IFS attributes
    static final int NUM_IFS_ATTRS = 14;   // there are 14 IFS attributes thus far
    static final NPAttributeIFS[] ifsAttrs = new NPAttributeIFS[NUM_IFS_ATTRS];
    static
    {
        /* -1 is ATTR_BACK_OVERLAY   */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_BACK_OVERLAY) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_BKOVRLAY,
                                         PrintObject.ATTR_BKOVRLLIB,
                                         0,
                                         "OVL");

        /* -2 is ATTR_DATA_QUEUE   */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_DATA_QUEUE) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_DATAQUE,
                                         PrintObject.ATTR_DATAQUELIB,
                                         0,
                                         "DTAQ");

        /* -3 is ATTR_FORM_DEFINITION */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_FORM_DEFINITION) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_FORMDEF,
                                         PrintObject.ATTR_FORMDEFLIB,
                                         0,
                                         "FORMDF");

        /* -4 is ATTR_FRONT_OVERLAY */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_FRONT_OVERLAY) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_FTOVRLAY,
                                         PrintObject.ATTR_FTOVRLLIB,
                                         0,
                                         "OVL");

        /* -5 is ATTR_MESSAGE_QUEUE */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_MESSAGE_QUEUE) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_MSGQUE,
                                         PrintObject.ATTR_MSGQUELIB,
                                         0,
                                         "MSGQ");

        /* -6 is ATTR_OUTPUT_QUEUE */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_OUTPUT_QUEUE) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_OUTQUE,
                                         PrintObject.ATTR_OUTQUELIB,
                                         0,
                                         "OUTQ");

        /* -7 is ATTR_PRINTER_FILE */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_PRINTER_FILE) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_PRTFILE,
                                         PrintObject.ATTR_PRTFLIB,
                                         0,
                                         "FILE");

        /* -8 is ATTR_WORKSTATION_CUST_OBJECT */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_WORKSTATION_CUST_OBJECT) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_WSCUSTMOBJ,
                                         PrintObject.ATTR_WSCUSTMOBJL,
                                         0,
                                         "WSCST");

        /* -9 is ATTR_USER_DEFINED_OBJECT */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_USER_DEFINED_OBJECT) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_USRDEFOBJ,
                                         PrintObject.ATTR_USRDEFOBJLIB,
                                         PrintObject.ATTR_USRDEFOBJTYP,
                                         null);

        /* -10 is ATTR_USER_TRANSFORM_PROG */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_USER_TRANSFORM_PROG) - 1] =
                      new NPAttributeIFS(PrintObject.ATTR_USRTFM,
                                         PrintObject.ATTR_USRTFMLIB,
                                         0,
                                         "PGM");

        /* -11 is ATTR_USER_DRIVER_PROG */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_USER_DRIVER_PROG) - 1] =
                       new NPAttributeIFS(PrintObject.ATTR_USERDRV,
                                          PrintObject.ATTR_USRDRVLIB,
                                          0,
                                          "PGM");

        /* -12 is ATTR_AFP_RESOURCE */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_AFP_RESOURCE) - 1] =
                       new NPAttributeIFS(PrintObject.ATTR_RSCNAME,
                                          PrintObject.ATTR_RSCLIB,
                                          PrintObject.ATTR_RSCTYPE,
                                          null);

        /* -13 is ATTR_PAGE_DEFINITION */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_PAGE_DEFINITION) - 1] =
                       new NPAttributeIFS(PrintObject.ATTR_PAGDFN,
                                          PrintObject.ATTR_PAGDFNLIB,
                                          0,
                                          "PAGDFN");
                                         
        /* -14 is ATTR_SAVE_FILE */
        ifsAttrs[java.lang.Math.abs(PrintObject.ATTR_SAVE_FILE) - 1] =
                       new NPAttributeIFS(PrintObject.ATTR_SAVEFILE,
                                          PrintObject.ATTR_SAVEFILELIB,
                                          0,
                                          "FILE");

    }


    int  nameID_;
    int  libraryID_;
    int  typeID_;            // if typeID_ is 0 use the typeString_
    String typeString_;

    NPAttributeIFS(int objID, int libID, int typeID, String type)
    {
        nameID_ = objID;
        libraryID_ = libID;
        typeID_ = typeID;
        typeString_ = type;
    }

   
}
