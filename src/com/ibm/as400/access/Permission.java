///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Permission.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.Enumeration;


/**
 * The Permission class is provided to retrieve user's authority to 
 * an object.<br>
 * To improve performance, the Permission object caches authority changes 
 * until the <i>commit()</i> method is called. When <i>commit()</i>is called, 
 * all changes up to that point are sent to the AS/400.<br>
 * The permission of an object is a collection of many users' authority to that object,
 * and the UserPermission class is used to represent a user's authority to a object. 
 * Because there are three kinds of object in the AS/400, three subclasses of UserPermission 
 * are defined:
 * <ul>
 *      <li> DLOPermission  - Represents a user's authority to a Document Library Objects (DLO)
 *                            stored in QDLS.
 *      <li> QSYSPermission - Represents a user's authority to the object which is contained in the AS/400 library
 *                            structure and stored in QSYS.LIB.
 *      <li> RootPermission - Represents a user's authority to the object which is contained in the root directory 
 *                            structure. This includes everything that is not in QSYS.LIB or QDLS.
 * </ul>
 * Here is a simple example:
 * <p><blockquote><pre>
 * AS400 as400 = new AS400();
 * Permission permission = new Permission(as400,"/QSYS.LIB/QJAVA.LIB");
 * permission.addAuthorizedUser("user1");
 * QSYSPermission userPermission = permission.getUserPermission("user1");
 * userPermission.setObjectAuthority("*CHANGE");
 * permission.commit();
 * </pre></blockquote></p>
 * @see UserPermission
 * @see DLOPermission
 * @see QSYSPermission
 * @see RootPermission
**/
public class Permission
       implements Cloneable ,
                  Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;


    /**
     * Constant indicating the object is a Document Library Objects (DLO)
     * stored in QDLS.
     *
    **/
    public static final int TYPE_DLO = 0;

    /**
     * Constant indicating the object is contained in the AS/400 library
     * structure and stored in QSYS.LIB.
     *
    **/
    public static final int TYPE_QSYS = 1;


    /**
     * Constant indicating that the object is contained in the root directory 
     * structure. This includes everything that is not in QSYS.LIB or QDLS.
     *
    **/
    public static final int TYPE_ROOT = 2;

    private AS400 as400_;
    private String authorizationList_;
    private String autListBackup_;
    private boolean autListChanged_;
    private String name_;
    private String owner_;
    private String path_;

    private String primaryGroup_;
    private boolean sensitivityChanged_;
    private int sensitivityLevel_;
    private int type_;

    private transient Vector userPermissionsBuffer_;          
    private transient Vector userPermissions_;               

    private transient PermissionAccess access_;
    private transient PropertyChangeSupport changes_;
    
    

    /**
     * Constructs a Permission object.
     * @param file The IFSFile object. For example, The IFSFile object which represents the object "QSYS.LIB/FRED.LIB".
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception ObjectDoesNotExistException If the AS/400 object does not exist.
     *
    **/
    public Permission(IFSFile file)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        this(file.getSystem(),file.getPath());
    }

    /**
     * Constructs a Permission object.
     * @param system The AS/400 system.
     * @param fileName The full path of the object. For example, "/QSYS.LIB/FRED.LIB".
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception ObjectDoesNotExistException If the AS/400 object does not exist.
     *
    **/
    public Permission(AS400 as400, String fileName)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {   
        if (as400 == null)
        {
            throw new NullPointerException("system");
        }
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        as400_ = as400;
        int separator;
        path_ = fileName.trim(); //@A2C
        separator = path_.lastIndexOf('/');
        name_ = path_.substring(separator+1);
        type_ = parseType(path_);

        switch(type_)
        {
            case TYPE_QSYS :
                access_ = new PermissionAccessQSYS(as400_);
                break;
            case TYPE_DLO :
                access_ = new PermissionAccessDLO(as400_);
                break;
            case TYPE_ROOT :
            default :
                access_ = new PermissionAccessRoot(as400_);
                break;
        }

        Vector vector = null; 
        try {
           vector = access_.getAuthority(path_);
        }
        catch (PropertyVetoException e)
        {
           Trace.log( Trace.ERROR, "unexpected exception, " + e.toString(), e );
        }

        changes_ = new PropertyChangeSupport(this);

        owner_ = (String)vector.elementAt(0);
        primaryGroup_ = (String)vector.elementAt(1);
        authorizationList_ = (String)vector.elementAt(2);
        autListChanged_ = false;
        sensitivityLevel_ = ((Integer)vector.elementAt(3)).intValue();
        sensitivityChanged_ = false;

        userPermissionsBuffer_ = new Vector ();
        userPermissions_ = new Vector();
        int count = vector.size();
        for (int i=4;i<count;i++)
        {
            UserPermission userPermission = (UserPermission)vector.elementAt(i);
            if (userPermission != null)
            {
                userPermission.setCommitted(UserPermission.COMMIT_NONE);
                userPermissionsBuffer_.addElement(userPermission);
                userPermissions_.addElement(userPermission);
            }
        }
    }

    /**
     * Adds an authorized user. The user added will have "*EXCLUDE" authorities 
     * on the object.
     * @userProfileName The authorized user profile name.
     *
    **/
    public void addAuthorizedUser(String userProfileName)
    {
        if (userProfileName == null) 
            throw new NullPointerException("userProfileName");
        int index;
        String userName = userProfileName.trim().toUpperCase();
        if (getUserIndex(userName,userPermissions_) != -1)
        {
            throw new ExtendedIllegalArgumentException("userProfileName",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        } 
        else
        {
            index=getUserIndex(userName,userPermissionsBuffer_);
            if (index != -1)
            {
                UserPermission usrAut = (UserPermission)
                    userPermissionsBuffer_.elementAt(index);
                usrAut.setCommitted(UserPermission.COMMIT_CHANGE);
                userPermissions_.addElement(usrAut);
            } 
            else
            {
                UserPermission userPermission;
                switch (type_)
                {
                    case TYPE_DLO : 
                        userPermission = new DLOPermission(userName);
                        break;
                    case TYPE_QSYS : 
                        userPermission = new QSYSPermission(userName);
                        break;
                    case TYPE_ROOT :
                    default : 
                        userPermission = new RootPermission(userName);
                        break;
                }
                userPermission.setGroupIndicator(UserPermission.GROUPINDICATOR_USER);
                userPermission.setCommitted(UserPermission.COMMIT_ADD);
                userPermissionsBuffer_.addElement(userPermission);
                userPermissions_.addElement(userPermission);
            }
        }
    }

   /**
    *  Adds a property change listener.
    *  @param listener The property change listener to add.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
       if (listener == null)
       {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
       }
       
       changes_.addPropertyChangeListener(listener);
    }
  
    /**
     * Adds a user permission.
     * @param userPermission The UserPermission object.
     *
    **/
    public void addUserPermission(UserPermission userPermission)
    {
        if (userPermission == null)
            throw new NullPointerException("userPermission");

        switch (type_)
        {
            case TYPE_DLO : 
                if (userPermission instanceof com.ibm.as400.access.DLOPermission)
                    break;
            case TYPE_QSYS : 
                if (userPermission instanceof com.ibm.as400.access.QSYSPermission)
                    break;
            case TYPE_ROOT : 
                if (userPermission instanceof com.ibm.as400.access.RootPermission)
                    break;
            default :
                throw new ExtendedIllegalArgumentException("userPermission",
                          ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        String user = userPermission.getUserID();

        int index;
        if (getUserIndex(user,userPermissions_) != -1)
        {
            throw new ExtendedIllegalArgumentException("userProfileName",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        } 
        else
        {
            index=getUserIndex(user,userPermissionsBuffer_);
            if (index != -1)
            {
                UserPermission usrAut = (UserPermission)
                               userPermissionsBuffer_.elementAt(index);
                userPermission.setCommitted(UserPermission.COMMIT_CHANGE);
                userPermissionsBuffer_.setElementAt(userPermission,index);
                userPermissions_.addElement(userPermission);
            } 
            else
            {
                userPermission.setCommitted(UserPermission.COMMIT_ADD);
                userPermissionsBuffer_.addElement(userPermission);
                userPermissions_.addElement(userPermission);
            }
        }
    }

    /**
     * Commits the permission changes to the AS/400 system.
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception ObjectDoesNotExistException If the AS/400 object does not exist.
     * @exception ServerStartupException If the AS/400 server cannot be started.
     *
    **/
    public synchronized void commit()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   ServerStartupException
    {
        if (isCommitted())
            return;
        try 
        {
           if (autListChanged_)
           {
              access_.setAuthorizationList(path_,authorizationList_,autListBackup_);
              autListChanged_ = false;
           }
           if (sensitivityChanged_)
           {
              access_.setSensitivity(path_,sensitivityLevel_);
              sensitivityChanged_ = false;
           }
           
           int count = userPermissionsBuffer_.size();
           for (int i=count-1;i>=0;i--)
           {
              UserPermission userPermission = (UserPermission)
                             userPermissionsBuffer_.elementAt(i);
              switch(userPermission.getCommitted())
              {
                 case UserPermission.COMMIT_FROM_AUTL :
                    access_.setFromAuthorizationList(path_,userPermission.isFromAuthorizationList());
                    userPermission.setCommitted(UserPermission.COMMIT_NONE);
                    break;
                 case UserPermission.COMMIT_ADD :
                    access_.addUser(path_,userPermission);
                    userPermission.setCommitted(UserPermission.COMMIT_NONE);
                    break;
                 case UserPermission.COMMIT_CHANGE :
                    access_.setAuthority(path_,userPermission);
                    userPermission.setCommitted(UserPermission.COMMIT_NONE);
                    break;
                 case UserPermission.COMMIT_REMOVE :
                    access_.removeUser(path_,userPermission.getUserID());
                    userPermission.setCommitted(UserPermission.COMMIT_NONE);
                    userPermissionsBuffer_.removeElement(userPermission);
                    break;
                 case UserPermission.COMMIT_NONE :
                 default :
                    break;
              }
           }
        }
        catch (PropertyVetoException e)
        {
           Trace.log (Trace.ERROR, "unexpected exception, " + e.toString(), e);
        }
        
        changes_.firePropertyChange("permission",null,this);
    }

    /**
     * Returns the authorizations list of the object.
     * @return The authorizations list of the object.
     * @see #setAuthorizationList(String)
     *
    **/
    public String getAuthorizationList()
    {
        return authorizationList_;
    }

    /**
     * Returns an enumeration of authorized users.
     * @return An enumeration of authorized users.
     *
    **/
    public Enumeration getAuthorizedUsers()
    {
        int count = userPermissions_.size();
        Vector names = new Vector();
        for (int i=0;i<count;i++)
        {
            UserPermission userPermission = (UserPermission)userPermissions_.elementAt(i);
            names.addElement(userPermission.getUserID());
        }
        return names.elements();
    }

    
    /** 
     * Returns the path of the integrated file system object whose permission is represented by this object.
     * @return The integrated file system path name.
     *
    **/
    public String getObjectPath()
    {
        return path_;
    }

    /**
     * Returns the name of the object whose permission is represented by this object.
     * @return The name of the object.
     *
    **/
    public String getName()
    {
        return name_;
    }

    /**
     * Returns the object owner.
     * @return The object owner.
     *
    **/
    public String getOwner()
    {
        return owner_;
    }

    /**
     * Returns the primary group of the object.
     * @return The primary group of the object.
     *
    **/
    public String getPrimaryGroup()
    {
        return primaryGroup_;
    }

    /**
     * Returns the sensitivity level of the object.
     * @return The sensitivity level of the object.
     * <UL>
     * <LI>0 : This value does not apply to this object.
     * <LI>1 : (*NONE) The document has no sensitivity restrictions.
     * <LI>2 : (*PERSONAL) The document is intended for the user as an
     *      individual.
     * <LI>3 : (*PRIVATE) The document contains information that should be
     *      accessed only by the owner.  This value cannot be
     *      specified if the access code zero (0) is assigned to
     *      the object.
     * <LI>4 : (*CONFIDENTIAL) The document contains information that should
     *      be handled according to company procedures.
     * </UL>
     * @see #setSensitivityLevel
     *
    **/
    public int getSensitivityLevel()
    {
        return sensitivityLevel_;
    }

    /**
     * Returns the AS/400 system
     * @return The AS/400 system instance. 
     *
    **/
    public AS400 getSystem()
    {
        return as400_;
    }

    /**
     * Returns the object type.
     * @return The object type. The possible values are:
     * <ul>
     *    <li> TYPE_DLO - Indicating the object is a Document Library Objects (DLO)
     * stored in QDLS.
     *    <li> TYPE_QSYS - Indicating the object is contained in the AS/400 library
     * structure and stored in QSYS.LIB.
     *    <li> TYPE_ROOT - Indicating the object is contained in the root directory 
     * structure. This includes everything that is not in QSYS.LIB or QDLS.
     * </ul>
     *
    **/
    public int getType()
    {
        return type_;
    }

    /*
     Searches a user in specified vector.
    */
    private int getUserIndex(String userProfileName,Vector vector)
    {
        int count = vector.size();
        for (int i=0;i<count;i++)
        {
            UserPermission userPermission =
                           (UserPermission)vector.elementAt(i);
            if (userPermission.getUserID().equals(userProfileName))
                return i;
        }
        return -1;
    }


    /**
     * Returns the specific Permission object.
     * If the specified user profile has no explicit authority to the object,
     * returns null.
     * @param userProfileName The name of the user profile.
     * @return The specific Permission object.
     *
    **/
    public UserPermission getUserPermission(String userProfileName)
    {
        String userName = userProfileName.toUpperCase();
        int index = getUserIndex(userName,userPermissions_);

        if (index != -1)
        {
            return (UserPermission)userPermissions_.elementAt(index);
        }
        return null;
    }

    /**
     * Returns an enumeration of Permission object.
     * @return An enumeration of Permission object.
     *
    **/
    public Enumeration getUserPermissions()
    {
        return userPermissions_.elements();
    }

    
    /**
     * Returns a flag indicating whether the change has been committed.
     * @return The flag indicating whether the change has been committed.
    **/
    public boolean isCommitted()
    {
        boolean committed = true;
        int count = userPermissionsBuffer_.size();
        if (sensitivityChanged_ == true)
        {
            return false;
        }
        if (autListChanged_ == true)
        {
            return false;
        }
        for (int i=0;i<count;i++)
        {
            UserPermission userPermission = (UserPermission)
                                        userPermissionsBuffer_.elementAt(i);
            if (userPermission.getCommitted() !=
                UserPermission.COMMIT_NONE)
            {
                    committed = false;
                    break;
            }
        }
        return committed;
    }

    /*
     Parses object's type by full path name.
    */
    private int parseType(String objectName)
    {
        if(objectName.toUpperCase().startsWith("/QSYS.LIB/"))
        {
            return TYPE_QSYS;
        }
        if(objectName.toUpperCase().startsWith("/QDLS/"))
        {
            return TYPE_DLO;
        }
        return  TYPE_ROOT;
    }


    /**
     * Serialization support.  
     * @exception Thrown when an application tries to load in a class through its string name,
     *            but no definition for the class with the specifed name could be found. 
     * @exception IOException If an error occurs while communicationg with the AS/400.
     *
    **/
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException 
    {   
        int size;
        s.defaultReadObject();
        switch(type_)
        {
            case TYPE_QSYS :
                access_ = new PermissionAccessQSYS(as400_);
                break;
            case TYPE_DLO :
                access_ = new PermissionAccessDLO(as400_);
                break;
            case TYPE_ROOT :
            default :
                access_ = new PermissionAccessRoot(as400_);
                break;
        }

        
        userPermissionsBuffer_ = new Vector ();
        userPermissions_ = new Vector();
        size = ((Integer)s.readObject()).intValue();
        for (int i=0;i<size;i++)
        {
            userPermissionsBuffer_.addElement(s.readObject());
        }
        size = ((Integer)s.readObject()).intValue();
        for (int i=0;i<size;i++)
        {
            userPermissions_.addElement(s.readObject());
        }
        changes_ = new PropertyChangeSupport(this);
        s.readObject();
    }

    /**
     * Removes an authorized user.
     * @userProfileName The authorized user profile name.
     *
    **/
    public void removeAuthorizedUser(String userProfileName)
    {
        if (userProfileName == null)
        {
            throw new NullPointerException("userProfileName");
        }
        String userName = userProfileName.trim().toUpperCase();
        UserPermission userPermission = getUserPermission(userName);
        if (userPermission != null)
        {
            removeUserPermission(userPermission);
        }else
        {
            throw new ExtendedIllegalArgumentException("userProfileName",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }

   

   /**
    *  Removes a property change listener.
    *  @param listener The property change listener to remove.
   **/
   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
     if (listener == null)
     {
       Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
       throw new NullPointerException("listener");
     }

     changes_.removePropertyChangeListener(listener);
   }
   
    /**
     * Removes a user permission.
     * @permission The UserPermission object.
     *
    **/
    public void removeUserPermission(UserPermission permission)
    {   
        if (permission == null)
            throw new NullPointerException("permission");
        if (userPermissions_.indexOf(permission) == -1)
        {
            throw new ExtendedIllegalArgumentException
                 ("User permission dose not exists",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        } 
        else
        {
            switch (permission.getCommitted())
            {
                case UserPermission.COMMIT_ADD :
                    userPermissions_.removeElement(permission);
                    userPermissionsBuffer_.removeElement(permission);
                    permission.setCommitted(UserPermission.COMMIT_NONE);
                    break;
                case UserPermission.COMMIT_REMOVE :
                case UserPermission.COMMIT_CHANGE :
                case UserPermission.COMMIT_NONE :
                default :
                    permission.setCommitted(UserPermission.COMMIT_REMOVE);
                    userPermissions_.removeElement(permission);
                    break;
            }
        }
    }

    /**
     * Sets the authorizations list of the object. For example:
     * <p><blockquote><pre>
     * Permission permisson = new Permisson(new AS400(),"/QSYS.LIB/FRED.LIB";
     * permission.setAuthorizatinList("testautl");
     * System.out.println("The authorization list of fred.lib is " + permissin.geAuthorizationList();
     * permission.setAuthorizatinList("*NONE");
     * System.out.println("The authorization list of fred.lib is " + permissin.geAuthorizationList();
     * </pre></blockquote></p>
     * @param authorizationList The authorizations list of the object.
     *
    **/
    public synchronized void setAuthorizationList(String autList)
    {
        if (autList == null)
        {
            throw new NullPointerException("autList");
        }
        if (autList.trim().toUpperCase().equals(authorizationList_))
            return;
        if (autListChanged_== false)
            autListBackup_ = authorizationList_;
        authorizationList_ = autList.trim().toUpperCase();
        autListChanged_ = true;
    }

    /**
     * Sets the sensitivity level of the object.
     * @param sensitivityLevel The sensitivity level of the object.The
     * possible values :
     * <UL>
     * <LI>0 : This value does not apply to this object.
     * <LI>1 : (*NONE) The document has no sensitivity restrictions.
     * <LI>2 : (*PERSONAL) The document is intended for the user as an
     *      individual.
     * <LI>3 : (*PRIVATE) The document contains information that should be
     *      accessed only by the owner.  This value cannot be
     *      specified if the access code zero (0) is assigned to
     *      the object.
     * <LI>4 : (*CONFIDENTIAL) The document contains information that should
     *      be handled according to company procedures.
     * </UL>
     *
     * @see #getSensitivityLevel
    **/
    public synchronized void setSensitivityLevel(int sensitivityLevel)
    {
        if (sensitivityLevel < 0 || sensitivityLevel > 4)
        {
            throw new ExtendedIllegalArgumentException("sensitivityLevel",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID );
        }
        sensitivityLevel_ = sensitivityLevel;
        sensitivityChanged_ = true;
    }

    /**
     * Sets the system where system value is retrieved.
     *
     * @param   system The AS/400 system object.
     * @see     #getSystem
    **/
    public synchronized void setSystem(AS400 system)
    {   
        if (system == null)
           throw new NullPointerException("system");
        
        if (as400_ == null)                        //$B1C
           as400_ = system;                        //$B1C
        else                                       //$B1C
        {  
            if (as400_.isConnected())
            {
                throw new ExtendedIllegalStateException("system",
                    ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
            }
            if (as400_.equals(system)==true)
               return;
        }

        switch(type_)
        {
            case TYPE_QSYS :
                access_ = new PermissionAccessQSYS(as400_);
                break;
            case TYPE_DLO :
                access_ = new PermissionAccessDLO(as400_);
                break;
            case TYPE_ROOT :
            default :
                access_ = new PermissionAccessRoot(as400_);
                break;
        }

    }
    
    /**
     * Serialization support.  
     * @exception IOException If an error occurs while communicationg with the AS/400.
     *
    **/
    private void writeObject(ObjectOutputStream s) 
                 throws IOException
    {
        s.defaultWriteObject();

        s.writeObject(new Integer(userPermissionsBuffer_.size()));
        for (int i=0;i<userPermissionsBuffer_.size();i++)
        {
            s.writeObject(userPermissionsBuffer_.elementAt(i));
        }
        s.writeObject(new Integer(userPermissions_.size()));
        for (int i=0;i<userPermissions_.size();i++)
        {
            s.writeObject(userPermissions_.elementAt(i));
        }

        s.writeObject(null);
    }
    
}
