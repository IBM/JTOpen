///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: UserListParser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

class UserListParser
      extends ListParser 
      implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private AS400Structure types_ = null;

    // @A1A : Adds constants indicating user or group profile.
    private static String USER = "0";
    private static String GROUP = "1";
    
    private static final User[] NULL_LIST={};
    
    private User[] userList_ = NULL_LIST;
    
    /**
     * Lists of names of fields
    **/
    String[] ElemNames = {
        "user name",
        "user/group indicator",
        "group members indicator"
    };
    
    
    // ************
    // * Data Types
    
    AS400Structure buildElemType(AS400 system)
    {
        setSystem (system);
        if (types_ == null)
        {
            AS400DataType[] types = new AS400DataType[]
            {
                new AS400Text(10, as400_.getCcsid(), as400_), // qualified user name
                new AS400Text( 1, as400_.getCcsid(), as400_), // user or group indicator
                new AS400Text( 1, as400_.getCcsid(), as400_)  // group members indicator
            };
            types_ = new AS400Structure(types);
        }
        return types_;
    }
    
    /**
     * Copyright.
    **/
    private static String getCopyright ()
    {
        return Copyright.copyright;
    }
    
    // *************
    // * Parse lists
    void parseLists( AS400 system, 
                     byte[] listInfoData, 
                     byte[] receiverData ) 
                     throws UnsupportedEncodingException
    {
        setSystem (system);
        parseListInfo( listInfoData );
        userList_ = new User[returnedRecs_];
        rowPos_ =0;
        parseReceiverData( system, receiverData );
    }
        
    User parseElemHeader( Object[] elem )
    {
        User user=null;
        // @A1C : Constructs user or group object.
        try {
            if(((String)elem[1]).trim().equals(USER))
            {
                   user = new User(as400_,((String)elem[0]).trim());
           
            }
            else if (((String)elem[1]).trim().equals(GROUP))
            {
               user = new UserGroup(as400_,((String)elem[0]).trim());
           
            }
        }
        catch(Exception e)
        {
             Trace.log(Trace.ERROR,"Error creating User object.");
        }

        userList_[rowPos_]=user;
        return user;
    }
    
    int parseFields( Object[] header, 
                     int nextReceiverPos, 
                     byte[] receiverData )
    {
        User user = parseElemHeader( header );

        // @A1C : Uses format AUTU0100 instead of AUTU0150 to retrieve.
        // next field at...  This is the size of the record defined above.  Could be cleaned up(?).
        nextReceiverPos += 12;
        
        return nextReceiverPos;
    }
    
    
    // @A2D /**
    // @A2D **/
    // @A2D User[] clear()
    // @A2D {
    // @A2D     basicClear();
    // @A2D     userList_ = NULL_LIST;
    // @A2D     return userList_;
    // @A2D }
    
    User[] getUserList()
    {
        return userList_;
    }
}
