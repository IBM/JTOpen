///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: UserGroupAccess.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.Calendar;
import java.util.Date;
 
   
/**
 * The UserGroupAccess class is used to access the user and group information in the remote 
 * AS/400 system through QGYOLAUS.PGM (Retrieve all users) and QSYRUSRI.PGM (Retrive user).    
**/     
class UserGroupAccess implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    protected AS400 as400_;

    /**
    * Vector where user information is stored. 
    */
    private Vector userInformation=new Vector();
    
    private static AS400Bin4 bin4 = new AS400Bin4();
    
    private int supplementalGroupsOffset;
    private int homeDirectoryOffset;
    private int localePathNameOffset;
    private int supplementalGroupsNum;
    private int homeDirectoryLength;
    private int localePathNameLength;
    private final int INITIAL_LENGTH=1200; //@A5C (was 900: too small, was chopping off locale path name)
    private static Vector userI0300Info_=new Vector(); 

    //Constant indicating that the date format is the format CYYMMDD.
    protected static final int CYYMMDD_FORMAT=0;

    //Constant indicating that the date format is the format CYYMMDDHHMMSS.
    protected static final int CYYMMDDHHMMSS_FORMAT=1;

    //Constant indicating that the date format is the system timestamp format.
    protected static final int SYSTEM_TIMESTAMP_FORMAT=2;


    public static int AS400TYPE_BIN4=0;
    public static int AS400TYPE_CHAR=1;
    static
    {
        userI0300Info_.addElement(new UserInfo(0,"Bytes returned",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(1,"Bytes available",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(2,"User profile name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(3,"Previous sign-on date and time",AS400TYPE_CHAR,13));
        userI0300Info_.addElement(new UserInfo(4,"Reserved1",AS400TYPE_CHAR,1));
        userI0300Info_.addElement(new UserInfo(5,"Sign-on attempts not valid",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(6,"Status",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(7,"Password change date",AS400TYPE_CHAR,8));
        userI0300Info_.addElement(new UserInfo(8,"No password indicator",AS400TYPE_CHAR,1));
        userI0300Info_.addElement(new UserInfo(9,"Reserved2",AS400TYPE_CHAR,1));
        userI0300Info_.addElement(new UserInfo(10,"Password expiration interval",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(11,"Date password expires",AS400TYPE_CHAR,8));
        userI0300Info_.addElement(new UserInfo(12,"Days until password expires",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(13,"Set password to expire",AS400TYPE_CHAR,1));
        userI0300Info_.addElement(new UserInfo(14,"User class name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(15,"Special authorities",AS400TYPE_CHAR,15));
        userI0300Info_.addElement(new UserInfo(16,"Group profile name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(17,"Owner",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(18,"Group authority",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(19,"Assistance level",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(20,"Current library name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(21,"Initial menu name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(22,"Initial menu library name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(23,"Initial program name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(24,"Initial program library name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(25,"Limit capabilities",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(26,"Text description",AS400TYPE_CHAR,50));
        userI0300Info_.addElement(new UserInfo(27,"Display sign-on information",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(28,"Limit device sessions",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(29,"Keyboard buffering",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(30,"Reserved3",AS400TYPE_CHAR,2));
        userI0300Info_.addElement(new UserInfo(31,"Maximum allowed storage",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(32,"Storage used",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(33,"Highest scheduling priority",AS400TYPE_CHAR,1));
        userI0300Info_.addElement(new UserInfo(34,"Job description name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(35,"Job description library name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(36,"Accounting code",AS400TYPE_CHAR,15));
        userI0300Info_.addElement(new UserInfo(37,"Message queue name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(38,"Message queue library name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(39,"Message queue delivery method",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(40,"Reserved4",AS400TYPE_CHAR,2));
        userI0300Info_.addElement(new UserInfo(41,"Message queue severity",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(42,"Output queue name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(43,"Output queue library name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(44,"Print device",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(45,"Special environment",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(46,"Attention-key-handling program name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(47,"Attention-key-handling program library name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(48,"Language ID",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(49,"Country ID",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(50,"Character code set ID",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(51,"User options",AS400TYPE_CHAR,36));
        userI0300Info_.addElement(new UserInfo(52,"Sort sequence table name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(53,"Sort sequence table library name",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(54,"Object auditing value",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(55,"User action audit level",AS400TYPE_CHAR,64));
        userI0300Info_.addElement(new UserInfo(56,"Group authority type",AS400TYPE_CHAR,10));
        userI0300Info_.addElement(new UserInfo(57,"Offset to array of supplemental groups",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(58,"Number of supplemental groups",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(59,"User ID number",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(60,"Group ID number",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(61,"Offset to home directory",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(62,"Length of home directory",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(63,"Locale job attributes",AS400TYPE_CHAR,16));
        userI0300Info_.addElement(new UserInfo(64,"Offset to locale path name",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(65,"Length of locale path name",AS400TYPE_BIN4,4));
        userI0300Info_.addElement(new UserInfo(66,"Group member indicator",AS400TYPE_CHAR,1));
        userI0300Info_.addElement(new UserInfo(67,"Digital certificate indicator",AS400TYPE_CHAR,1));
//@A5D        userI0300Info_.addElement(new UserInfo(68,"Character identifier control",AS400TYPE_CHAR,10)); //@A5A
    }
    
    /**
    * Constructs a UserGroupAccess object. 
    *
    **/
    public UserGroupAccess()
    {
        
    }

   /**
     Returns the copyright.
   **/
   private static String getCopyright()
   {
      return Copyright.copyright;
   }



    /**
    * Constructs a UserGroupAccess object. 
    *
    * @param system The AS/400 system. 
    *
    **/
    public UserGroupAccess(AS400 as400)
    {
        this.as400_=as400;
    }
    
    /**
    * Returns the minimum length of the receiver to hold all the data returned.
    * @param data The byte array that contains the data from AS/400.
    **/
    private int dataToUserInfo(byte[] data)
           throws UnsupportedEncodingException
    {
       UserInfo userInfo;
       RecordFormat rf0=getRecordFormat0();
       Record  record=null;
       // @A4D try
       // @A4D {
         record=rf0.getNewRecord(data);
       // @A4D }
       // @A4D catch(Exception e)
       // @A4D {
       // @A4D 	    Trace.log(Trace.ERROR," dataToUserInfo" + e);
       // @A4D }

       int num=userI0300Info_.size(); 
       
       for(int i=0;i<num;i++)
       {
           userInfo=(UserInfo)userI0300Info_.elementAt(i);
           Object object=record.getField(userInfo.getFieldDescription());
           if(userInfo.getValueType()==AS400TYPE_CHAR &&
             !userInfo.getFieldDescription().equals("Password change date") && //@A5A
             !userInfo.getFieldDescription().equals("Date password expires"))  //@A5A
              object=((String) object).trim();
                       
           userInformation.addElement(object);
       }
       
       supplementalGroupsOffset=((Integer)record.getField("Offset to array of supplemental groups")).intValue();
       
       homeDirectoryOffset=((Integer)record.getField("Offset to home directory")).intValue();
       localePathNameOffset=((Integer)record.getField("Offset to locale path name")).intValue(); 
      
       supplementalGroupsNum=((Integer)record.getField("Number of supplemental groups")).intValue();
       homeDirectoryLength=((Integer)record.getField("Length of home directory")).intValue();
       localePathNameLength=((Integer)record.getField("Length of locale path name")).intValue();

       int length = localePathNameOffset;   //@A5A
       if (localePathNameLength == 10)      //@A5A
         length += 10;                      //@A5A
       else                                 //@A5A
         length += localePathNameLength+32; //@A5A
//@A5D       int length=localePathNameOffset+localePathNameLength+32; //@A5C
       return length;       
    } 


    /**
    * Returns the vector that stores the default user's information.
    * @return The vector that stores the default user's information.
    **/
    public Vector getDefaultUserInformation()
    {
       int num=userI0300Info_.size(); 
       Object object;
       UserInfo userInfo;
       for(int i=0;i<num;i++)
       {
           userInfo=(UserInfo)userI0300Info_.elementAt(i);
           if(userInfo.getValueType()==AS400TYPE_CHAR)
              object="";
           else
              object=new Integer(0); 
                       
           userInformation.addElement(object);
       }
       String[] supplementalGroups={"",""};
       String homeDirectory="";
       String localePathName="";
       userInformation.addElement(supplementalGroups);
       userInformation.addElement(homeDirectory);
       userInformation.addElement(localePathName);
       return userInformation;
       
    }   
    /**
    * Returns the date for a date string in certain format.
    * @param str The date string to be parsed.
    * @param format The format.
    * @return The date for a date string in certain format.
    **/
    protected Date parseDate(String str,int format)
    {
        // @A4D if(str==null)
        // @A4D     throw new NullPointerException("dateStringNull"); 
        // @A4D else 
        if(str.trim().equals(""))
            return null;
        else
        {
           Calendar dateTime = Calendar.getInstance();
           dateTime.clear();
           if(format==CYYMMDD_FORMAT) // @A4C
           {
               dateTime.set (
                             Integer.parseInt(str.substring(0,3)) + 1900,// year
                             Integer.parseInt(str.substring(3,5))-1,     // month is zero based
                             Integer.parseInt(str.substring(5,7)));       // day
               return dateTime.getTime();
           }  
           else if(format==CYYMMDDHHMMSS_FORMAT) // @A4C
           {
                dateTime.set (
                             Integer.parseInt(str.substring(0,3)) + 1900,// year
                             Integer.parseInt(str.substring(3,5))-1,     // month is zero based
                             Integer.parseInt(str.substring(5,7)),       // day
                             Integer.parseInt(str.substring(7,9)),       // hour
                             Integer.parseInt(str.substring(9,11)),      // minute
                             Integer.parseInt(str.substring(11,13)));    // second
                return dateTime.getTime();
           }
           else if(format==SYSTEM_TIMESTAMP_FORMAT) // @A4C
           {
               AS400Text as400Text=new AS400Text(str.length(), as400_.getCcsid(), as400_); //@A5C
               byte[] dateByte = as400Text.toBytes(str);
               AS400DateTimeConverter converter=new AS400DateTimeConverter(as400_);
               Date date=null;
               // @A3D try
               // @A3D {
                  date=converter.getDate(dateByte,"*DTS");
               // @A3D } 
               // @A3D catch (UnsupportedEncodingException e)
               // @A3D {
               // @A3D     Trace.log(Trace.ERROR,"Error in convert time : "+e);
               // @A3D }
               return date;
           } 
           return null;
        }   
   } 


//@A5A
    protected Date parseDate(byte[] str, int format)
    {
           Calendar dateTime = Calendar.getInstance();
           dateTime.clear();
           AS400DateTimeConverter converter=new AS400DateTimeConverter(as400_);
           Date date=null;
           date=converter.getDate(str,"*DTS");
           return date;
    } 


    /**
    * Returns the parameter group for QSYRUSRI API to retrieve the information about 
    * the specified user on the AS/400.
    *
    * @param userProfile Name The user profile name.
    * @param format The format for the QSYRUSRI API.
    * @param length The receiver variable length. 
    * 
    * @return The parameter group for QSYRUSRI API to retrieve the information 
    * about a specified user on the AS/400.
    **/
    private ProgramParameter[] getParameters(String format,String userProfileName,int length)
    { 
        ProgramParameter[] parmList=new ProgramParameter[5];

        AS400Bin4 bin4=new AS400Bin4();
        parmList[0]=new ProgramParameter(length);
        parmList[1]=new ProgramParameter(bin4.toBytes(length));
        
        AS400Text text;
        text = new AS400Text(8, as400_.getCcsid(), as400_); //@A5C

        parmList[2]=new ProgramParameter(text.toBytes(format));
        
        text = new AS400Text(10, as400_.getCcsid(), as400_); //@A5C
        parmList[3]=new ProgramParameter(text.toBytes(userProfileName));
        
        byte[] errorInfo = new byte[32];
        parmList[4] = new ProgramParameter( errorInfo, 0 );
       
        return parmList;
    } 
    /**
    * Returns the record format for the user information data 
    * retrieved from the AS/400 by calling QSYRUSRI API.
    * @return The record format for the user information data 
    * retrieved from the AS/400 by calling QSYRUSRI API.
    **/
    private RecordFormat getRecordFormat0()
    {  
        UserInfo userInfo;
        RecordFormat rf0=new RecordFormat();

        int num=userI0300Info_.size(); 
        FieldDescription[] fd=new FieldDescription[num];

        for(int i=0;i<num;i++)
        {
            userInfo=(UserInfo)userI0300Info_.elementAt(i);
            if(userInfo.getValueType()==0)
                fd[i]=new BinaryFieldDescription(new AS400Bin4(),userInfo.getFieldDescription());
            else if (userInfo.getFieldDescription().equals("Password change date") ||  //@A5A
                     userInfo.getFieldDescription().equals("Date password expires"))   //@A5A
              fd[i] = new HexFieldDescription(new AS400ByteArray(userInfo.getValueLength()), userInfo.getFieldDescription()); //@A5A
            else
                fd[i]=new CharacterFieldDescription(new AS400Text(userInfo.getValueLength(), as400_.getCcsid(), as400_),userInfo.getFieldDescription()); //@A5C
            rf0.addFieldDescription(fd[i]);
        }
        return rf0;
    } 

    /**
    * Returns the vector that stores the user's information.
    * @return The vector that stores the user's information.
    **/
    public Vector getUserInformation()
    {
        return userInformation;
    }
   
    /**
    * Returns the information about a specified user and stores it into 
    * the <i>userInformation_</i> vector.    
    * @param userProfileName The user profile name. 
    *
    * @exception AS400Exception                  If the AS/400 system returns an error message.
    * @exception AS400SecurityException          If a security or authority error occurs.
    * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
    * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    * @exception InterruptedException            If this thread is interrupted.
    * @exception IOException                     If an error occurs while communicating with the AS/400.
    * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
    * @exception PropertyVetoException           If a proposed change to a property represents an unacceptable value.
    * @exception UnsupportedEncodingException    If the Character Encoding is not supported. 
    **/ 
   
     public void retrieveUserInformation(String userProfileName)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   ObjectDoesNotExistException,
                   IOException,
                   PropertyVetoException,
                   UnsupportedEncodingException
                   
    {
        
        
        byte[] userInf_Data=null;
        int length;
        int length0;
        
        QSYSObjectPathName prgName=new QSYSObjectPathName("QSYS","QSYRUSRI","PGM");
        ProgramParameter[] parmList=new ProgramParameter[5];
        ProgramCall rtvUserInf=new ProgramCall(as400_);
        
        
        length0=INITIAL_LENGTH;
        parmList=getParameters("USRI0300",userProfileName,length0);
        
        rtvUserInf.setProgram(prgName.getPath(),parmList);
       
        if (rtvUserInf.run()!=true)
        {
           AS400Message[] msgList = rtvUserInf.getMessageList();
           for (int i=0;i<msgList.length;i++)
           {
               Trace.log(Trace.ERROR,"Error: "+msgList[i]);
           }
           throw new AS400Exception(msgList);
        }
        else
        {
           userInf_Data = parmList[0].getOutputData();
        }
        
        length=dataToUserInfo(userInf_Data);
        // @A4D if(length>length0)
        // @A4D {
        // @A4D 
        // @A4D     parmList=getParameters("USRI0300",userProfileName,length);
	    // @A4D     ProgramCall pgm=new ProgramCall(as400_);
        // @A4D     pgm.setProgram(prgName.getPath(),parmList);
        // @A4D 
        // @A4D     if (pgm.run()!=true)
        // @A4D     {   
        // @A4D 
        // @A4D         AS400Message[] msgList = rtvUserInf.getMessageList();
        // @A4D     }
        // @A4D     else
        // @A4D     {
        // @A4D         userInf_Data = parmList[0].getOutputData();
        // @A4D     }
        // @A4D     
        // @A4D } 

        Object[] objArray=new String[2];
        for(int i=0;i<2;i++)
           objArray[i]="";
        
        if(supplementalGroupsNum>0)   
        {
           AS400Array as400Array=new AS400Array(new AS400Text(10, as400_.getCcsid(), as400_),supplementalGroupsNum); //@A5C
           objArray=(Object[])as400Array.toObject(userInf_Data,supplementalGroupsOffset);
        }       
              
        // Get the ccsid for the home directory name
        int dirCcsid = ((Integer)bin4.toObject(userInf_Data, homeDirectoryOffset)).intValue(); // @A2A
        // Get the length of the home directory name
        int dirLength = ((Integer)bin4.toObject(userInf_Data,homeDirectoryOffset+16)).intValue(); // @A2C
        // Convert
        String obj1 = (new CharConverter(dirCcsid)).byteArrayToString(userInf_Data, homeDirectoryOffset+32, dirLength); //@A2A
        // @A2D text=new AS400Text(i3);
        // @A2D Object obj1=text.toObject(userInf_Data,homeDirectoryOffset+32);
        // @A2D String str2 = obj1.toString();
                
        Object obj2; //@A5A
        if (localePathNameLength == 10) // It is a * value //@A5A
        {
          AS400Text text = new AS400Text(localePathNameLength, as400_.getCcsid(), as400_); //@A5C //@A5A
          obj2 = text.toObject(userInf_Data, localePathNameOffset); //@A5A
        }
        else //@A5A
        {
          // Get the ccsid for the locale path name
          int localeCcsid = ((Integer)bin4.toObject(userInf_Data, localePathNameOffset)).intValue(); //@A5A
          // Get the length of the locale path name
          int localeLength = ((Integer)bin4.toObject(userInf_Data, localePathNameOffset+16)).intValue(); //@A5A
          // Convert
          obj2 = (new CharConverter(localeCcsid)).byteArrayToString(userInf_Data, localePathNameOffset+32, localeLength); //@A5A
//@A5D        AS400Text text = new AS400Text(localePathNameLength, as400_.getCcsid(), as400_); //@A5C
//@A5D        Object obj2=text.toObject(userInf_Data,localePathNameOffset);
        }       
        userInformation.addElement(objArray);
        userInformation.addElement(obj1);
        userInformation.addElement(obj2);        
    }  


    /**
    * Returns the vector that stores the received data about users 
    * on the AS/400. 
    * @param userInfo The user information that describes what user 
    * are retrieved.
    * @param groupInfo The group information that describes what users 
    * are retrieved.
    * @return The vector that stores the received data about users on 
    * the AS/400. 
    *
    * @exception AS400Exception                  If the AS/400 system returns an error message.
    * @exception AS400SecurityException          If a security or authority error occurs.
    * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    * @exception InterruptedException            If this thread is interrupted.
    * @exception IOException                     If an error occurs while communicating with the AS/400.
    * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
    * @exception PropertyVetoException           If a proposed change to a property represents an unacceptable value.
    * @exception RequestNotSupportedException    If the request is not supported. 
    **/ 
    
    public Vector retrieveUsersData(String userInfo,
                                String groupInfo)
           throws AS400Exception, 
                  AS400SecurityException, 
                  ErrorCompletingRequestException,
                  InterruptedException, 
                  IOException, 
                  ObjectDoesNotExistException,
                  PropertyVetoException,
                  RequestNotSupportedException
    {
        // @A4D if (this.as400_ == null)
        // @A4D {
        // @A4D     Trace.log(Trace.ERROR, "System is null");
        // @A4D     throw new ExtendedIllegalStateException("system", 
        // @A4D               ExtendedIllegalStateException.PROPERTY_NOT_SET);
        // @A4D }
        
        ProgramCall pgm = new ProgramCall( this.as400_ );
        
        ProgramParameter[] parms = new ProgramParameter[8];

        // 1 receiver variable
        parms[0] = new ProgramParameter( 1200 );

        // 2 receiver len
        parms[1] = new ProgramParameter( bin4.toBytes(1200) );

        // 3 list information
        parms[2] = new ProgramParameter( 80 );

        // 4 number of records to return
        parms[3] = new ProgramParameter( bin4.toBytes(new Integer(-1)) );

        // 5 format name
        parms[4] = new ProgramParameter( 
                       new AS400Text (8, 
                                      this.as400_.getCcsid(), 
                                      this.as400_).toBytes("AUTU0100") );

        // 6 selection criteria
        parms[5] = new ProgramParameter( 
                       new AS400Text (10, 
                                      this.as400_.getCcsid(), 
                                      this.as400_).toBytes(userInfo) );

        // 7 group profile name
        parms[6] = new ProgramParameter( 
                       new AS400Text (10, 
                                      this.as400_.getCcsid(), 
                                      this.as400_).toBytes(groupInfo) );

        // 8 error code ? inout, char*
        parms[7] = new ProgramParameter( bin4.toBytes( new Integer(0) ));

        // do it
        byte[] listInfoData = null;
        byte[] receiverData = null;
        try
        {
            if (pgm.run( "/QSYS.LIB/QGY.LIB/QGYOLAUS.PGM", parms )==false)
            {
                 // Error on running.
                 throw new AS400Exception( pgm.getMessageList() );
            }
            listInfoData = parms[2].getOutputData();
            receiverData = parms[0].getOutputData();

            // If default length is shorter than required
            // change length of the return variable and retrieve.
            Integer bytesReturned = ((Integer)bin4.toObject(receiverData,0));
            Integer bytesAvailable = ((Integer)bin4.toObject(receiverData,4));

        }
        catch (ObjectDoesNotExistException e)
        {
            	 throw new RequestNotSupportedException( "V3R7M0", 
                   RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT );
        }

        Vector vector = new Vector();
        vector.addElement(listInfoData);
        vector.addElement(receiverData);
        
        return vector;
    }
   
    
}    
