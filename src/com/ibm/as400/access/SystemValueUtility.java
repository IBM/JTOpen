///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SystemValueUtility.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Calendar;
import java.io.IOException;
import java.net.UnknownHostException;
import java.beans.PropertyVetoException;
import java.math.BigDecimal;

/**
Contains static methods for setting and getting system values
from an AS/400. Used by the SystemValue and SystemValueList classes.
**/
class SystemValueUtility
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   /**
    Returns the copyright.
   **/
   private static String getCopyright()
   {
     return Copyright.copyright;
   }

    /**
    Parses the data stream returned by the Program Call.
    Extracts the system value information tables from the data stream.
      @return A Vector of Java objects corresponding to the values
      retrieved from the data stream.
    **/
    private static Vector parse(byte[] data, AS400 system) //@A3C
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   ObjectDoesNotExistException,
                   IOException,
                   PropertyVetoException
    {
        Vector allValues = new Vector();

        if (Trace.isTraceOn() && Trace.isTraceDatastreamOn())
        {
          Trace.log(Trace.DATASTREAM, "Parsing system value data received: ", data);
        }
        // Separates the return values,
        // see AS/400 API reference for detail.
        int[] valuesOffsets;
        AS400Bin4 bin4 = new AS400Bin4();
        AS400Array offsetsArray;

        Integer iValuesNumber = (Integer)bin4.toObject(data,0);
        int valuesNumber = iValuesNumber.intValue();
        if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
        {
          Trace.log(Trace.DIAGNOSTIC,"values count : "+valuesNumber);
        }

        offsetsArray = new AS400Array(new AS400Bin4(),valuesNumber);
        Object[] iOffsets = (Object[]) offsetsArray.toObject(data,4);
        valuesOffsets = new int[valuesNumber];
        
        // Loop through the information tables returned and set the
        // appropriate system values to their corresponding data values.
        for (int i=0; i<valuesNumber; ++i)
        {
          valuesOffsets[i] = ((Integer)iOffsets[i]).intValue();
          // AS400 data structure.
          AS400Bin4 bin = new AS400Bin4();
          AS400Text text10, text1, text;

          // Gets system value name
          text10 = new AS400Text(10, system.getCcsid(), system); //@A3C
          String name = ((String)text10.toObject(data, valuesOffsets[i])).trim();
          SystemValueInfo obj = SystemValueList.lookup(name);

          // Gets system value type
          text1 = new AS400Text(1, system.getCcsid(), system); //@A3C
          char type = ((String)text1.toObject(data, valuesOffsets[i]+10)).charAt(0);
          if (type != obj.type_)
          {
            throw new ExtendedIllegalStateException("type",
                ExtendedIllegalStateException.PROPERTY_NOT_SET);
          }

          // Gets system value information status
          char infoStatus = ((String)text1.toObject(data, valuesOffsets[i]+11)).charAt(0);
          // If the value is locked then throw an exception.
          if (infoStatus == 'L')
          {
            throw new ExtendedIOException("infoStatus",
                ExtendedIOException.LOCK_VIOLATION);
          } 

          // Gets value data
          int size = ((Integer)bin.toObject(data, valuesOffsets[i]+12)).intValue();

          Object value = null;

          switch (type)
          {
            case SystemValueList.AS400TYPE_CHAR:
              text = new AS400Text(size, system.getCcsid(), system); //@A3C
              value = text.toObject(data, valuesOffsets[i]+16);
              break;
            case SystemValueList.AS400TYPE_BINARY:
              value = bin.toObject(data, valuesOffsets[i]+16);
              break;
            default:
              value = null;
              throw new ExtendedIOException("type",
                  ExtendedIOException.DATA_STREAM_SYNTAX_ERROR);
          }

          if (obj.returnType_ == SystemValueList.TYPE_DECIMAL)
          {
            Integer temp = (Integer)value;
            String tempStr = temp.toString();
            int offset = tempStr.length() - obj.decimalPositions_;
            String rightHalf = "."+tempStr.substring(offset);
            String leftHalf = "";
            if (offset > 0)
            {
              leftHalf = tempStr.substring(0, offset);
            }
            value = (new BigDecimal(leftHalf+rightHalf)).setScale(obj.decimalPositions_, BigDecimal.ROUND_HALF_UP);
          }
          if (obj.returnType_ == SystemValueList.TYPE_ARRAY)
          {
            Vector tempVec = new Vector();
            for (int t=0; t<obj.arraySize_; ++t)
            {
              StringBuffer buf = new StringBuffer();
              for (int r=(t*obj.size_); r<((t+1)*obj.size_); ++r)
              {
                buf.append(((String)value).charAt(r));
              }
              String arrElem = buf.toString().trim();
              if (arrElem.length() > 0)
                tempVec.addElement(arrElem);
            }
            String[] temp = new String[tempVec.size()];
            tempVec.copyInto(temp);
            value = temp;
          }

          // Handle special cases
          if (obj.name_.equals("QDATE"))
          {
            // Need to parse the String into a Date object
            // AS/400 API format is CYYMMDD
            String as400Date = null;
            try
            {
              as400Date = (String)value;
            }
            catch(ClassCastException cce)
            {
              throw new ExtendedIOException("QDATE",
                   ExtendedIOException.CANNOT_CONVERT_VALUE);
            }
            if (as400Date.length() != 7)
            {
              throw new ExtendedIOException("QDATE (length)",
                   ExtendedIOException.CANNOT_CONVERT_VALUE);
            }
            int century = (new Integer(as400Date.substring(0,1))).intValue();
            int year = (new Integer(as400Date.substring(1,3))).intValue();
            int month = (new Integer(as400Date.substring(3,5))).intValue()-1;
            int day = (new Integer(as400Date.substring(5,7))).intValue();
            year = year + 1900 + (100*century); // C=0 or C=1
            Calendar cal = Calendar.getInstance();
            //@A2D cal.clear();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            value = new java.sql.Date(cal.getTime().getTime());
          }
          else if (obj.name_.equals("QTIME"))
          {
            // Need to parse the String into a Date object
            // AS/400 API format is HHMMSSSSS
            String as400Date = null;
            try
            {
              as400Date = (String)value;
            }
            catch(ClassCastException cce)
            {
              throw new ExtendedIOException("QTIME",
                   ExtendedIOException.CANNOT_CONVERT_VALUE);
            }
            if (as400Date.length() != 9)
            {
              throw new ExtendedIOException("QTIME (length)",
                   ExtendedIOException.CANNOT_CONVERT_VALUE);
            }
            int hour = (new Integer(as400Date.substring(0,2))).intValue();
            int minute = (new Integer(as400Date.substring(2,4))).intValue();
            int second = (new Integer(as400Date.substring(4,6))).intValue();
            int millisecond = (new Integer(as400Date.substring(6,9))).intValue();
            Calendar cal = Calendar.getInstance();
            //@A2D cal.clear();
            cal.set(Calendar.HOUR_OF_DAY, hour); //@B2C
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            cal.set(Calendar.MILLISECOND, millisecond);
            value = new java.sql.Time(cal.getTime().getTime());
          }

          // Handle QLOCALE as a special case
          if (obj.name_.equals("QLOCALE"))
          {
            //offset  length     description
            //  0     bin  4     ccsid
            //  4     char 2     country id
            //  6     char 3     language id
            //  9     char 3     reserved
            //  12    bin  4     flag byte
            //  16    bin  4     number of bytes in locale path name
            //  20    char 2     locale delimiter
            //  22    char 10    reserved
            //  32    char 2048  locale path name

            int offset = valuesOffsets[i]+16;

            int localeCcsid = ((Integer)bin.toObject(data, offset)).intValue();
            int localeLen = ((Integer)bin.toObject(data, offset+16)).intValue();

            if (localeLen == 0) // *NONE
            {
              value = "*NONE";
            }
            else if (localeLen == 1) // *POSIX or *C
            {
              text = new AS400Text(20, localeCcsid, system); //@A3C
              value = text.toObject(data, offset+32);
            }
            else // a real path name
            {
              text = new AS400Text(localeLen, localeCcsid, system); //@A3C
              value = text.toObject(data, offset+32);
            }
          }
//          if (value instanceof String)
//            value = ((String)value).trim();
          allValues.addElement(value);
        }
        return allValues;
    }


    /**
    Retrieves a system value from the AS/400.
      @return A Java object representing the current setting for
      the system value specified by <i>info</i> on <i>system</i>.
    **/
    static Object retrieve(AS400 system, SystemValueInfo info)
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               PropertyVetoException,
               UnknownHostException
    {
      Vector vec = new Vector();
      vec.addElement(info);
      Vector ret = retrieveFromSystem(system, vec, info.group_ == SystemValueList.GROUP_NET);
      return ret.elementAt(0);
    }


    /**
    Retrieves several system values from the AS/400.
      @param system The AS/400.
      @param values The enumeration of SystemValueInfo objects to retrieve values for.
      @return A Vector of SystemValue objects representing the current settings for
      the system values specified by <i>values</i> on <i>system</i>.
    **/
    static Vector retrieve(AS400 system, Enumeration values)
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               PropertyVetoException,
               UnknownHostException
    {
      Vector valVec = new Vector(); // System values
      Vector attrVec = new Vector(); // Network attributes
      while (values.hasMoreElements())
      {
        SystemValueInfo svi = (SystemValueInfo)values.nextElement();
        if (svi.group_ == SystemValueList.GROUP_NET)
          attrVec.addElement(svi); // attrVec contains the net attributes
        else
          valVec.addElement(svi); // valVec contains the system values
      }

      Vector valObj = new Vector(); // Actual values for system values
      Vector attrObj = new Vector(); // Actual values for network attributes
      if (valVec.size() > 0)
      {
        // Get the system value values
        valObj = retrieveFromSystem(system, valVec, false);
      }
      if (attrVec.size() > 0)
      {
        // Get the network attribute values
        attrObj = retrieveFromSystem(system, attrVec, true);
      }

      // Build new SystemValue objects to hold the data
      Vector systemValues = new Vector(valVec.size()+attrVec.size());
      for (int c=0; c<valVec.size(); ++c)
      {
        systemValues.addElement(new SystemValue(system, (SystemValueInfo)valVec.elementAt(c),
                                                valObj.elementAt(c)));
      }
      for (int c=0; c<attrVec.size(); ++c)
      {
        systemValues.addElement(new SystemValue(system, (SystemValueInfo)attrVec.elementAt(c),
                                                attrObj.elementAt(c)));
      }
      return systemValues;
    }


    /**
    This method does the actual retrieving with a Program Call.
      @param values The Vector of SystemValueInfo objects.
      @return A Vector of SystemValue objects containing the retrieved data.
    **/
    private static Vector retrieveFromSystem(AS400 system, Vector values, boolean isNetA)
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               PropertyVetoException,
               UnknownHostException
    {
        int valuesCount = values.size();

        // Constructs parameters.
        ProgramParameter[] parameters = new ProgramParameter[5];

        int i,rLength;
        
        AS400Bin4 bin4 = new AS400Bin4();

        // Calculates the length of the return values.
        rLength =0;
        for (i=0; i<valuesCount; ++i)
        {
            SystemValueInfo svi = (SystemValueInfo)values.elementAt(i);
            rLength += svi.size_ * svi.arraySize_;
            rLength += 24;
        }
        rLength += 4;

        // Constructs parameters, see AS/400 API reference for detail.
        parameters[0] = new ProgramParameter( rLength );

        byte[]  receiverLength = bin4.toBytes( rLength);
        parameters[1] = new ProgramParameter( receiverLength );
        
        byte[]  valuesNumber = bin4.toBytes( valuesCount);
        parameters[2] = new ProgramParameter( valuesNumber );

        String[] text = new String[valuesCount];
        for (i=0; i<valuesCount; ++i)
        {
            text[i] = ((SystemValueInfo)values.elementAt(i)).name_;
        }

        AS400Array array = new AS400Array(new AS400Text(10, system.getCcsid(), system), valuesCount); //@A3C
        byte[] valuesName = array.toBytes(text);
        parameters[3] = new ProgramParameter( valuesName );

        byte[] errorInfo = new byte[32];
        parameters[4] = new ProgramParameter( errorInfo, 0 );
        
        QSYSObjectPathName programName;
        if (!isNetA)
        {
            // Sets program to retrieve system value.
            programName = new QSYSObjectPathName("QSYS",
                                                 "QWCRSVAL",
                                                 "PGM");
        }
        else
        {
            // Sets program to retrieve network attributes.
            programName = new QSYSObjectPathName("QSYS",
                                                 "QWCRNETA",
                                                 "PGM");
        }
        
        ProgramCall prog = new ProgramCall(system);
        prog.setProgram(programName.getPath(), parameters );
            
        if (!prog.run())
        {
          AS400Message[] msgList = prog.getMessageList();
          throw new AS400Exception(msgList);
        }

        // Parse the returned data
        byte[] as400Data = parameters[0].getOutputData();
        return parse(as400Data, system); //@A3C
    }
          

  /**
  Sets the system value on the AS/400.
    @param system The AS/400.
    @param info The system value to set.
    @param value The data that the system value is to contain.
  **/
  static void set(AS400 system, SystemValueInfo info, Object value)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             PropertyVetoException,
             UnknownHostException
  {
    if (info.readOnly_)
    {
      throw new ExtendedIllegalStateException(info.name_+" is read only",
          ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
    boolean isNetA = (info.group_ == SystemValueList.GROUP_NET);

    // String containing command
    String command = null;
    // String containing value for command
    String valueString = new String();
    if (isNetA)
    {
      command = "CHGNETA "+info.name_;
    }
    else
    {
      // We've only passed in one object if it's a system value
      command = "CHGSYSVAL SYSVAL("+info.name_+")";
    }

    // First check to see if the value being set is *SOMETHING
    // since that can occur on any type of parameter, not just strings.
    boolean starcmd = false;
    try
    {
      String star = (String)value;
      starcmd = (star.indexOf("*") == 0);
    }
    catch(Exception e)
    {
      starcmd = false;
    }
        
    if (!starcmd)
    {
      try
      {
        switch (info.returnType_)
        {
          case SystemValueList.TYPE_STRING:
            String string = (String)value;
            // Normal String value.
            if (!isNetA)
              valueString = "'"+string+"'";
            else
              valueString = string;
            break;

          case SystemValueList.TYPE_DECIMAL:
            // Converts BigDecimal value to String value.
            BigDecimal bd = ((BigDecimal)value).setScale(info.decimalPositions_, BigDecimal.ROUND_HALF_UP);
            valueString = bd.toString();
            break;

          case SystemValueList.TYPE_INTEGER:
            // Converts Integer value to String value.
            valueString = ((Integer)value).toString();
            break;

          case SystemValueList.TYPE_ARRAY:
            Object[] objarr = (Object[])value;
            // Converts an array of string to a single line String.
            int length = objarr.length;
            if (!isNetA)
              valueString = "'";
            for (int j=0;j<length;j++)
            {
              valueString += (String)objarr[j]+" ";
            }
            if (!isNetA)
              valueString += "'";
            break;

          case SystemValueList.TYPE_DATE:
            //Get date information.
            Calendar dateTime = Calendar.getInstance();
            dateTime.clear();
            dateTime.setTime((java.util.Date)value);

            if (info.name_.equals("QDATE"))
            {
              //String values of year,month and day.
              String century,year,month,day;
              //Converts year's information to QYEAR and QCENTURY.
              int iYear=dateTime.get(Calendar.YEAR);
              if (iYear < 1900 || iYear > 2100)
              {
                throw new ExtendedIllegalArgumentException("QYEAR",
                  ExtendedIllegalArgumentException.RANGE_NOT_VALID );
              }
              if (iYear < 2000) 
              {
                century = "0";
                year = Integer.toString(iYear-1900).trim();
              }
              else 
              {
                century = "1";
                year = Integer.toString(iYear-2000).trim();
              }
              if (year.length()==1)
              {
                year="0"+year;
              }
               
              // Converts month's information to QMONTH.
              month = Integer.toString(dateTime.get(Calendar.MONTH)+1).trim();
              if (month.length()==1)
              {
                month="0"+month;
              }

              // Converts day's information to QDAY.
              day = Integer.toString(dateTime.get(Calendar.DAY_OF_MONTH)).trim();
              if (day.length()==1)
              {
                day="0"+day;
              }

              // Sets QCENTURY, QYEAR, QMONTH and QDAY.
              set(system, SystemValueList.lookup("QCENTURY"), century);
              set(system, SystemValueList.lookup("QYEAR"), year);
              set(system, SystemValueList.lookup("QMONTH"), month);
              set(system, SystemValueList.lookup("QDAY"), day);

              return;
            }
            valueString = "'";

            // It is either QTIME or QIPLDATTIM
            int hour = dateTime.get(Calendar.HOUR_OF_DAY);
            if (hour<10) 
              valueString += "0";
            valueString += Integer.toString(hour);

            int minute = dateTime.get(Calendar.MINUTE);
            if (minute<10) 
              valueString += "0";
            valueString += Integer.toString(minute);

            int second = dateTime.get(Calendar.SECOND);
            if (second<10) 
              valueString += "0";
            valueString += Integer.toString(second);
         
            valueString +="'"; 
            break;
        } // end switch
      }
      catch(ClassCastException cce)
      {
        throw new ExtendedIllegalArgumentException(info.name_,
            ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
    }
    else
    {
      // It is a starcmd
      if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "System value command is special star value: '"+(String)value+"'");
      }
      valueString = ((String)value).trim();
    }

    if (isNetA)
      command += "("+valueString+")";
    else
      command +=" VALUE("+valueString+")";

    CommandCall cmd = new CommandCall(system);
    cmd.setCommand(command);
    if (Trace.isTraceOn() && Trace.isTraceDatastreamOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Running system value command: "+command);
    }

    if (!cmd.run())
    {
      AS400Message[] msgList = cmd.getMessageList();
      throw new AS400Exception(msgList);
    }
  }
}
