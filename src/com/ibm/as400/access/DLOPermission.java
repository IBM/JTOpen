///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DLOPermission.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
<P>The DLOPermission class represents the permission for the specfied user for 
document library objects (DLO)  stored in QDLS.  
<P>A user can have one of the following authorities to a document library object 
can have one of the following values: 
<UL>
<LI>*ALL
<LI>*AUTL
<LI>*CHANGE
<LI>*EXCLUDE
<LI>*USE
<LI>USER DEFINED.
</UL>
Use <i>getAuthority()</i> to display the current value and the <i>setAuthority()</i> 
to set the  authority to one of the valid values.  Use <i>commit()</i> from the 
Permission class to send the changes to the server.
**/
public class DLOPermission extends UserPermission
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;

    /**
     * Constructs a DLOPermission object. This is the permission of the specified user.
     * @param userProfileName The name of the user profile.
     *
    **/
    public DLOPermission( String userProfileName )
    {
        super(userProfileName);
        dataAuthority_ = "*EXCLUDE";
        return;
    }

    
    // Deleted getCopyright().


    /** 
      Returns the data authority of the user.
      @return The data authority of the user. The possible values are:
      <UL>
      <LI>*ALL The user can perform all operations except those
          limited to the owner or controlled by authorization list
          management authority.  The user can control the object's
          existence, specify the security for the object, change the
          object, and perform basic functions on the object.  The 
          user can also change ownership of the object.
      <LI>*AUTL The authority of the authorization list is used for
          the document.  This is valid for user *PUBLIC.
      <LI>*CHANGE The user can change and perform basic functions
          on the object. Change authority provides object operational
          authority and all data authorities.  
      <LI>*EXCLUDE The user cannot access the object.
      <LI>*USE The user has object operational authority, read authority, and execute
          authority. 
      <LI>USER DEFINED The user has specifically defined authority to the object.
      </UL>
      @see #setDataAuthority(String)   
    **/
    public String getDataAuthority()
    {
        if (isFromAuthorizationList())
            return "*AUTL";
        parseBasic();
        switch (objectAuthority_)
        {
            case BASIC_ALL :
                return "*ALL";
            case BASIC_EXCLUDE :
                return "*EXCLUDE";
            case BASIC_USE :
                return "*USE";
            case BASIC_CHANGE :
                return "*CHANGE";
            case BASIC_USER_DEF:   //@A2A
                return "USER DEFINED"; //@A2A
            default :
//@A2D                return "*EXLUDE";
                return "USER DEFINED"; //@A2A
        }
    }

   
    /**
     * Parses the basic authority type.
    **/
    private synchronized void parseBasic()
    {
        for (int j=0;j<4;j++)
        {
            boolean match = true;
            for (int i=0;i<10;i++)
            {
                if (authorities_[i] != basicAutMapping[j][i])
                {
                    match = false;
                    break;
                }
            }
            if (match == true)
            {
                objectAuthority_= j;
                return;
            }
        }
//@A2D        objectAuthority_= 1;
        objectAuthority_= BASIC_USER_DEF; //@A2A
        return;
    }

   /**
     * Sets the add data authority.
     * @param authority  true to set the data authority of add on; 
     * false to set the data authority of add off.
     *
    **/
    synchronized void setAdd(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_ADD] = authority;
        return;
    }


   /**
     * Sets the alter object authority.
     * @param authority  true to set the object authority of alter on; 
     * false to set the object authority of alter off.
     *
    **/
    synchronized void setAlter(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_ALTER] = authority;
        return;
    }

     /**
      Sets the data authority of the user.
      @param authority The data authority of the user.
      <P>Valid values are:
      <UL>
      <LI>*ALL The user can perform all operations except those
          limited to the owner or controlled by authorization list
          management authority.  The user can control the object's
          existence, specify the security for the object, change the
          object, and perform basic functions on the object.  The 
          user can also change ownership of the object.
      <LI>*AUTL The authority of the authorization list is used for
          the document.  This is valid for user *PUBLIC.
      <LI>*CHANGE The user can change and perform basic functions
          on the object. Change authority provides object operational
          authority and all data authorities.  
      <LI>*EXCLUDE The user cannot access the object.
      <LI>*USE The user has object operational authority, read authority, and execute
          authority. 
      </UL>   
    **/
    public synchronized void setDataAuthority(String authority)
    {
        if (authority == null)
            throw new NullPointerException("authority");
        String aut = authority.trim().toUpperCase();
        changeAuthority();
        if (getDataAuthority().equals(aut) == true)
            return;
        if (aut.equals("*ALL"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_ALL][i];
            }
            objectAuthority_ = BASIC_ALL;
        } else if (aut.equals("*EXCLUDE"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_EXCLUDE][i];
            }
            objectAuthority_ = BASIC_EXCLUDE;
        } else if (aut.equals("*USE"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_USE][i];
            }
            objectAuthority_ = BASIC_USE;
        } else if (aut.equals("*CHANGE"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_CHANGE][i];
            }
            objectAuthority_ = BASIC_CHANGE;
        }
        return;
    }

    /**
     * Sets the delete data authority.
     * @param authority  true to set the data authority of delete on;
     * false to set the data authority of delete off.
     *
    **/
    synchronized void setDelete(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_DELETE] = authority;
        return;
    }


 /**
     * Sets the execute data authority.
     * @param authority  true to set the data authority of execute on;
     * false to set the data authority of execute off.
    **/
    synchronized void setExecute(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_EXECUTE] = authority;
        return;
    }


    /**
     * Sets the existence object authority.
     * @param authority  true to set the object authority of existence on;
     * false to set the object authority of existence off.
     *
    **/
    
    synchronized void setExistence(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_EXIST] = authority;
        return;
    }


   /**
     * Sets the management object authority.
     * @param authority  true to set the object authority of management on;
     * false to set the object authority of management off.
     *
    **/
    
    synchronized void setManagement(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_MANAGEMENT] = authority;
        return;
    }

    /**
     * Sets the operational object authority.
     * @param authority  true to set the object authority of operational on;
     * false to set the object authority of operational off.
     *
    **/
    synchronized void setOperational(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_OPERATION] = authority;
        return;
    }

   /**
     * Sets the read data authority.
     * @param authority  true to set the data authority of read on;
     * false to set the data authority of read off.
     *
    **/
    synchronized void setRead(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_READ] = authority;
        return;
    }

    /**
     * Sets the reference object authority.
     * @param authority true to set the object authority of reference on;
     * false to set the object authority of reference off.
    **/
    synchronized void setReference(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_REFERENCE] = authority;
        return;
    }

   
    /**
     * Sets the update data authority.
     * @param authority  true to set the data authority of update on;
     * false to set the data authority of update off.
     *
    **/
    synchronized void setUpdate(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_UPDATE] = authority;
        return;
    }

}
     
