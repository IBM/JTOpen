///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserPermission.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The UserPermission class represents the authority
 * of a specific user.
**/
public class UserPermission 
       implements  Cloneable,
                   Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    /**
     * Constant indicating that no operation has been done.
    **/
    final static int COMMIT_NONE = -1;

    /**
     * Constant indicating that this user has just been added.
    **/
    final static int COMMIT_ADD = 0;

    /**
     * Constant indicating that this item has been changed for the object.
    **/
    final static int COMMIT_REMOVE = 1;

    /**
     * Constant indicating that this user has just been removed.
    **/
    final static int COMMIT_CHANGE = 2;

    /**
     * Constant indicating that this user permission has just been set from authorization list.
    **/
    final static int COMMIT_FROM_AUTL = 3;

    /**
     * Constant indicating that this user is neither a user nor a group.
     * It may be a *PUBLIC, *NTWIRF, or * NTWEFF.
    **/
    public final static int GROUPINDICATOR_SPECIALVALUE = 0;

    /**
     * Constant indicating that this user profile is a user.
    **/
    public final static int GROUPINDICATOR_USER = 1;

    /**
     * Constant indicating that this user profile is a group.
    **/
    public final static int GROUPINDICATOR_GROUP = 2;

    // Basic authorities mapping to detailed authorities.
    final static boolean[][] basicAutMapping = {
                            {true, true, true, true, true,
                             true, true, true, true, true  },
                            {false,false,false,false,false,
                             false,false,false,false,false },
                            {true, false,false,false,false,
                             true, false,false,false,true  },
                            {true, false,false,false,false,
                             true, true, true, true, true  }
                             };

    //Constant indicating basic authority type.
    final static int BASIC_USER_DEF = -1;
    final static int BASIC_ALL = 0;
    final static int BASIC_EXCLUDE = 1;
    final static int BASIC_USE = 2;
    final static int BASIC_CHANGE = 3;

    //Constant indicating data authority type.
    final static int DATA_READ = 5;
    final static int DATA_ADD = 6;
    final static int DATA_UPDATE = 7;
    final static int DATA_DELETE = 8;
    final static int DATA_EXECUTE = 9; 

    //Constant indicating object authority type.
    final static int OBJECT_OPERATION = 0;
    final static int OBJECT_MANAGEMENT = 1;
    final static int OBJECT_EXIST = 2;
    final static int OBJECT_ALTER = 3;
    final static int OBJECT_REFERENCE = 4;

    //Authorities information.
    boolean[] authorities_;

    //Data authority information.
    String dataAuthority_;

    //Object authority informatin.
    int objectAuthority_;
    
    // Authorization list management flag.
    private boolean autListMgt_;
    
    // Commit flag
    private int committed_;
    
    // The value indicating whether this user profile is a user 
    // profile or a group profile.
    private int groupIndicator_;

    // User profile name.
    private String userName_;

    // Indicates whether the user is from authorization list.
    private boolean fromAuthorizationList_;

    /**
     * Constructs a UserPermission object.
     * @param userProfileName The name of the user profile.
     *
    **/
    UserPermission(String userProfileName)
    {
        authorities_ = new boolean[10];
        for(int i=0;i<authorities_.length;i++)
        {
            authorities_[i] = false;
        }
        dataAuthority_="*EXCLUDE";
        objectAuthority_=BASIC_EXCLUDE;
        userName_ = userProfileName.toUpperCase();
        groupIndicator_ = GROUPINDICATOR_SPECIALVALUE;
        committed_ = COMMIT_NONE;
        autListMgt_ = false;
        fromAuthorizationList_ = false;
        return;
    }
    
    /**
     * Changes commit flag and backup original information when
     * authority is changed.
     *
    **/
    void changeAuthority()
    {
        if (isFromAuthorizationList())
            setFromAuthorizationList(false);
        switch (getCommitted())
        {
            case COMMIT_NONE :
            case COMMIT_FROM_AUTL :
                setCommitted(COMMIT_CHANGE);
            default :
                break;
        }
        return;
    }

   
    /**
     * Returns if the content is already committed in the AS/400.
     * @return if the content is already committed in the AS/400.
     * <UL>
     *     <LI>-1  COMMIT_NONE means no change and any operation to the object.
     *        <LI> 0 COMMIT_ADD means this user permission which was added 
     * to the object has not been committed.
     *     <LI> 1  COMMIT_REMOVE means this user permission which was 
     * removed from the object has not been committed.
     *     <LI> 2  COMMIT_CHANGE means the changes of the content have 
     * not committed.
     * </UL>
     * @see #setCommitted
    **/
    int getCommitted()
    {
        return committed_;
    }

    /** 
     * Returns the copyright.
    **/
    private static String getCopyright()
    {
        return Copyright.copyright;
    }
    
    /**
     * Returns the value indicating if the user profile is a group profile. 
     * @return The value indicating if the user profile is a group profile.
     *
    **/
    public int getGroupIndicator()
    {
        return groupIndicator_;
    }

    

    /**
     * Returns the user profile name for this permission.
     * @return The user profile name for this permission.
     *
    **/
    public String getUserID()
    {
        return userName_;
    }

    /**
     * Indicates whether the user permissoin is from authorization list.
     * @return true if the user is from authorization list;
     * false otherwise.
    **/
    public boolean isFromAuthorizationList()
    {
        return fromAuthorizationList_;
    }

    /**
     * Indicates whether the user has the authority of authorization list management.
     * It is valid only for the object which is an authorization list. 
     * @return true if the user has the authority of authorization list
     * management; false otherwise. 
     *
    **/
    public boolean isAuthorizationListManagement()
    {
        return autListMgt_;
    }

    /**
     * Serialization support.  
    **/
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException 
    {
        s.defaultReadObject();
        s.readObject();
    }

    /**
     * Sets the authority of authorization list management. 
     * It is valid only for the object which is an authorizaiton list. 
     * @param autListMgt true if the user has the authority 
     * of authorization list management; false otherwise.
     *
    **/
    public synchronized void setAuthorizationListManagement(boolean autListMgt )
    {
        autListMgt_ = autListMgt;
        if (getCommitted() == COMMIT_NONE)
        {
            setCommitted(COMMIT_CHANGE);
        }
        return;
    }

    /**
     * Sets the committed signal.
     * @param commit The committed signal.
     * @see #isCommitted
    **/
    void setCommitted(int commit)
    {
      
        committed_ = commit;
        return;
    }

    /**
     * Sets the permission of user as coming from an authorization list. 
     * This is valid only if the user is *PUBLIC and the authorization list
     * exists. If set to true, all of the other authorities will be set to false.
     * If any of the other authorities are set to true,
     * this value is automatically set to false.
     * @param fromAutList true if the user is from the authorization list;
     * false otherwise.
     *
    **/
    public synchronized void setFromAuthorizationList(boolean fromAutList)
    {
        if (fromAuthorizationList_ == fromAutList)
            return;
        if (fromAutList == true)
        {
            fromAuthorizationList_ = fromAutList;       
            for(int i=0;i<authorities_.length;i++)
            {
                authorities_[i] = false;
            }
            dataAuthority_="*EXCLUDE";
            setCommitted(COMMIT_FROM_AUTL);
        } else
        {
            fromAuthorizationList_ = fromAutList;       
            if (getCommitted() == COMMIT_FROM_AUTL)
                setCommitted(COMMIT_CHANGE);
        }
        return;
    }
    
    /**
     * The value indicating if the user is a user profile or a 
     * group profile.
     * @param indicator The value indicating if the user is a user 
     *  profile or a group profile.
    **/
    synchronized void setGroupIndicator(int indicator)
    {
        groupIndicator_ = indicator;
    }

    /**
     * Serialization support.  
    **/
    private void writeObject(ObjectOutputStream s) 
                 throws IOException
    {
        s.defaultWriteObject();
        s.writeObject(null);
    }

}

