///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemValueUtility.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

// Contains static methods for setting and getting system values from the system.  Used by the SystemValue, SystemValueGroup, and SystemValueList classes.
class SystemValueUtility
{
  static boolean jdk14 = false;
  static {
    jdk14 = JVMInfo.isJDK14();
  }

    // Parses the data stream returned by the Program Call.  Extracts the system value information tables from the data stream.
    // @return  A Vector of Java objects corresponding to the values retrieved from the data stream.
    private static Vector parse(byte[] data, AS400 system, Converter conv) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException, IOException
    {
        Vector values = new Vector();

        // Separates the return values, see API reference for detail.
        int valueNumber = BinaryConverter.byteArrayToInt(data, 0);

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC,"Number of values:", valueNumber);

        // Loop through the information tables returned and set the appropriate system values to their corresponding data values.
        for (int i = 0; i < valueNumber; ++i)
        {
            int valueOffset = BinaryConverter.byteArrayToInt(data, (i + 1) * 4);

            // Get system value name.
            String name = conv.byteArrayToString(data, valueOffset, 10).trim();
            SystemValueInfo obj = SystemValueList.lookup(name);

            // Get system value information status.
            if (data[valueOffset + 11] == (byte)0xD3)
            {
                // If the value is locked then throw an exception.
                Trace.log(Trace.ERROR, "System value is locked, name: " + name);
                throw new ExtendedIOException(name, ExtendedIOException.LOCK_VIOLATION);
            }
            // Get system value type.
            if (data[valueOffset + 10] != obj.serverDataType_)
            {
                Trace.log(Trace.ERROR, "System value type mismatch, name: " + name);
                throw new ExtendedIllegalStateException("type", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }

            // Gets value data.
            int size = BinaryConverter.byteArrayToInt(data, valueOffset + 12);

            Object value = null;

            switch (obj.serverDataType_)
            // Valid values are SystemValueList.SERVER_TYPE_BINARY and SERVER_TYPE_CHAR.
            {
                case SystemValueList.SERVER_TYPE_CHAR:
                    if (obj.type_ == SystemValueList.TYPE_ARRAY)
                    {
                        int arrayCount = 0;
                        int dataOffset = valueOffset + 16;
                        while (arrayCount < obj.arraySize_ && data[dataOffset + (arrayCount * obj.size_)] != 0x40) ++arrayCount;
                        String[] valueArray = new String[arrayCount];
                        for (int ii = 0; ii < arrayCount; ++ii)
                        {
                            valueArray[ii] = conv.byteArrayToString(data, dataOffset + (ii * obj.size_), obj.size_);
                        }
                        value = valueArray;
                    }
                    else
                    {
                        value = conv.byteArrayToString(data, valueOffset + 16, size);
                    }
                    break;
                case SystemValueList.SERVER_TYPE_BINARY:
                    if (obj.type_ == SystemValueList.TYPE_DECIMAL)
                    {
                        byte[] valueBytes = new byte[4];
                        System.arraycopy(data, valueOffset + 16, valueBytes, 0, 4);
                        value = new BigDecimal(new BigInteger(valueBytes), obj.decimalPositions_);
                    }
                    else
                    {
                        value = new Integer(BinaryConverter.byteArrayToInt(data, valueOffset + 16));
                    }
                    break;
                default:
                    Trace.log(Trace.WARNING, "Invalid value for SystemValueInfo.serverDataType_: " + obj.serverDataType_);
            }

            // Handle special cases.
            if (obj.name_.equals("QDATE"))
            {
                // Need to parse the String into a Date object, API format is CYYMMDD.
                String stringValue = value.toString();
                Calendar cal = AS400Calendar.getGregorianInstance();
                cal.set(1900 + (100 * Integer.parseInt(stringValue.substring(0, 1))) + Integer.parseInt(stringValue.substring(1, 3)), Integer.parseInt(stringValue.substring(3, 5)) - 1, Integer.parseInt(stringValue.substring(5, 7)));
                long millis;
                if (jdk14) { millis =cal.getTimeInMillis(); } else { millis = cal.getTime().getTime(); }
                value = new java.sql.Date(millis);
            }
            else if (obj.name_.equals("QTIME"))
            {
                // Need to parse the String into a Date object, API format is HHMMSSXXX.
                String stringValue = value.toString();
                Calendar cal = AS400Calendar.getGregorianInstance();
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(stringValue.substring(0, 2)));
                cal.set(Calendar.MINUTE, Integer.parseInt(stringValue.substring(2, 4)));
                cal.set(Calendar.SECOND, Integer.parseInt(stringValue.substring(4, 6)));
                cal.set(Calendar.MILLISECOND, Integer.parseInt(stringValue.substring(6, 9)));
                long millis;
                if (jdk14) { millis =cal.getTimeInMillis(); } else { millis = cal.getTime().getTime(); }
                value = new Time(millis);
            }

            // Sometimes hex zeros get returned for the PNDSYSNAME which shows up as square boxes in the visual system value components.  So we replace the hex zeros with Unicode spaces.
            else if (obj.name_.equals("PNDSYSNAME"))
            {
                value = ((String)value).replace((char)0x00, ' ');
            }

            // Handle QLOCALE as a special case.
            else if (obj.name_.equals("QLOCALE"))
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

                int offset = valueOffset + 16;

                int localeCcsid = BinaryConverter.byteArrayToInt(data, offset);
                int localeLen = BinaryConverter.byteArrayToInt(data, offset + 16);

                if (localeLen == 0) // *NONE
                {
                    value = "*NONE";
                }
                else if (localeLen == 1) // *POSIX or *C
                {
                    value = conv.byteArrayToString(data, offset + 32, 20);
                }
                else // A real path name.
                {
                    value = (new Converter(localeCcsid, system)).byteArrayToString(data, offset + 32, localeLen);
                }
            }
            values.addElement(value);
        }
        return values;
    }

    // Retrieves a system value from the system.
    // @return  A Java object representing the current setting for the system value specified by <i>info</i> on <i>system</i>.  Never returns null.
    static Object retrieve(AS400 system, SystemValueInfo info) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        Vector infos = new Vector();
        infos.addElement(info);
        Vector values = retrieveFromSystem(system, infos, info.group_ == SystemValueList.GROUP_NET);
        return values.elementAt(0);
    }

    // Retrieves several system values from the system.  If a value is not supported by the provided system, then it is not retrieved.
    // @param  system  The system.
    // @param  infos  The enumeration of SystemValueInfo objects for which to retrieve values.
    // @return  A Vector of SystemValue objects representing the current settings for the system values specified by <i>infos</i> on <i>system</i>.
    static Vector retrieve(AS400 system, Enumeration infos, String groupName, String groupDescription) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        Vector svInfos = new Vector();  // System values.
        Vector naInfos = new Vector();  // Network attributes.
        int vrm = system.getVRM();
        while (infos.hasMoreElements())
        {
            SystemValueInfo svi = (SystemValueInfo)infos.nextElement();
            if (svi.release_ <= vrm)
            {
                if (svi.group_ == SystemValueList.GROUP_NET)
                {
                    naInfos.addElement(svi); // naInfos contains the net attributes.
                }
                else
                {
                    svInfos.addElement(svi); // svInfos contains the system values.
                }
            }
        }

        Vector svValues = new Vector(); // Actual values for system values.
        Vector naValues = new Vector(); // Actual values for network attributes.
        if (svInfos.size() > 0)
        {
            // Get the system value values.
            svValues = retrieveFromSystem(system, svInfos, false);
        }
        if (naInfos.size() > 0)
        {
            // Get the network attribute values.
            naValues = retrieveFromSystem(system, naInfos, true);
        }

        // Build new SystemValue objects to hold the data.
        Vector systemValues = new Vector(svInfos.size() + naInfos.size());
        for (int i = 0; i < svInfos.size(); ++i)
        {
            systemValues.addElement(new SystemValue(system, (SystemValueInfo)svInfos.elementAt(i), svValues.elementAt(i), groupName, groupDescription));
        }
        for (int i = 0; i < naInfos.size(); ++i)
        {
            systemValues.addElement(new SystemValue(system, (SystemValueInfo)naInfos.elementAt(i), naValues.elementAt(i), groupName, groupDescription));
        }
        return systemValues;
    }

    // This method does the actual retrieving with a Program Call.
    // @param  values  The Vector of SystemValueInfo objects.
    // @return  A Vector of SystemValue objects containing the retrieved data.
    static Vector retrieveFromSystem(AS400 system, Vector infos, boolean isNetA) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Calculates the length of the return values.
        int valuesCount = infos.size();
        int rLength = 4;
        byte[] names = new byte[valuesCount * 10];
        for (int i = 0; i < names.length; ++i) names[i] = 0x40;
        Converter conv = new Converter(system.getJobCcsid(), system);
        for (int i = 0; i < valuesCount; ++i)
        {
            SystemValueInfo svi = (SystemValueInfo)infos.elementAt(i);
            rLength += svi.size_ * svi.arraySize_ + 24;
            conv.stringToByteArray(svi.name_, names, i * 10);
        }

        // Construct parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable - Output - Char(*).
            new ProgramParameter(rLength),
            // Length of receiver variable - Input - Binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(rLength)),
            // Number of network attributes or system values to retrieve - Input - Binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(valuesCount)),
            // Network attribute or system value names - Input - Array(*) of Char(10).
            new ProgramParameter(names),
            // Error code - I/0 - Char(*).
            new ProgramParameter(new byte[8])
        };

        ProgramCall prog = new ProgramCall(system, isNetA ? "/QSYS.LIB/QWCRNETA.PGM" : "/QSYS.LIB/QWCRSVAL.PGM", parameters);
        // Both QWCRNETA and QWCRSVAL are threadsafe.
        prog.suggestThreadsafe();

        if (!prog.run())
        {
            throw new AS400Exception(prog.getMessageList());
        }

        // Parse the returned data.
        return parse(parameters[0].getOutputData(), system, conv);
    }

    // Sets the system value on the system.
    // @param  system  The system.
    // @param  info  The system value to set.
    // @param  value  The data that the system value is to contain.
    static void set(AS400 system, SystemValueInfo info, Object value) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (info.readOnly_)
        {
            Trace.log(Trace.ERROR, "Cannot set value of read only system value, name: " + info.name_);
            throw new ExtendedIllegalStateException(info.name_, ExtendedIllegalStateException.OBJECT_IS_READ_ONLY);
        }
        boolean isNetA = info.group_ == SystemValueList.GROUP_NET;

        // String containing command.
        StringBuffer command = new StringBuffer(
             (isNetA) ? "QSYS/CHGNETA " + info.name_ + "(" :
                        "QSYS/CHGSYSVAL SYSVAL(" + info.name_ + ") VALUE(");

        // String containing value for command.
        String valueString = value.toString();

        // First check to see if the value being set is *SOMETHING since that can occur on any type of parameter, not just strings.
        if (valueString.indexOf("*") == 0)
        {
            // It is a starcmd.
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "System value's value is special star value: '" + (String)value + "'");
            command.append(valueString.trim());
        }
        else
        {
            try
            {
                switch (info.type_)
                // Valid values are TYPE_STRING, TYPE_DECIMAL, TYPE_INTEGER, TYPE_ARRAY, and TYPE_DATE.
                {
                    case SystemValueList.TYPE_STRING:
                        // Add string to command.
                        command.append(
                           (isNetA) ? valueString : "'" + valueString + "'");
                        break;

                    case SystemValueList.TYPE_DECIMAL:
                        // Convert BigDecimal value to String value, add to command.
                        command.append(
                           ((BigDecimal)value).setScale(info.decimalPositions_, BigDecimal.ROUND_HALF_UP).toString());
                        break;

                    case SystemValueList.TYPE_INTEGER:
                        // Convert Integer value to String value.
                        command.append(((Integer)value).toString());
                        break;

                    case SystemValueList.TYPE_ARRAY:
                        // Convert an array of strings to a single line String, add to command.
                        Object[] objarr = (Object[])value;
                        int length = objarr.length;
                        if (!isNetA) command.append("'");
                        for (int j = 0; j < length; ++j)
                        {
                            command.append((String)objarr[j]);
                            command.append(" ");
                        }
                        if (!isNetA) command.append("'");
                        break;

                    case SystemValueList.TYPE_DATE:
                        // Get date information.
                        Calendar dateTime = AS400Calendar.getGregorianInstance();
                        dateTime.clear();
                        dateTime.setTime((java.util.Date)value);

                        if (info.name_.equals("QDATE"))
                        {
                            //String values of year, month and day.
                            String century, year, month, day;
                            //Converts year's information to QYEAR and QCENTURY.
                            int iYear = dateTime.get(Calendar.YEAR);
                            if (iYear < 1900 || iYear > 2100)
                            {
                                Trace.log(Trace.ERROR, "Value of system value 'QYEAR' is not valid:", iYear);
                                throw new ExtendedIllegalArgumentException("QYEAR  (" + iYear + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
                            }
                            if (iYear < 2000)
                            {
                                century = "0";
                                year = Integer.toString(iYear - 1900);
                            }
                            else
                            {
                                century = "1";
                                year = Integer.toString(iYear - 2000);
                            }
                            if (year.length() == 1) year = "0" + year;

                            // Converts month's information to QMONTH.
                            month = Integer.toString(dateTime.get(Calendar.MONTH) + 1);
                            if (month.length() == 1) month = "0" + month;

                            // Converts day's information to QDAY.
                            day = Integer.toString(dateTime.get(Calendar.DAY_OF_MONTH));
                            if (day.length() == 1) day = "0" + day;

                            // Sets QCENTURY, QYEAR, QMONTH and QDAY.
                            set(system, SystemValueList.lookup("QCENTURY"), century);
                            set(system, SystemValueList.lookup("QYEAR"), year);
                            set(system, SystemValueList.lookup("QMONTH"), month);
                            set(system, SystemValueList.lookup("QDAY"), day);

                            return;
                        }
                        command.append("'");

                        // It is either QTIME or QIPLDATTIM.
                        int hour = dateTime.get(Calendar.HOUR_OF_DAY);
                        if (hour < 10) command.append("0");
                        command.append(Integer.toString(hour));

                        int minute = dateTime.get(Calendar.MINUTE);
                        if (minute < 10) command.append("0");
                        command.append(Integer.toString(minute));

                        int second = dateTime.get(Calendar.SECOND);
                        if (second < 10) command.append("0");
                        command.append(Integer.toString(second));

                        command.append("'");
                        break;

                    default:
                        Trace.log(Trace.ERROR, "Invalid value for SystemValueInfo.type_: " + info.type_);
                        throw new InternalErrorException(InternalErrorException.UNKNOWN);
                }
            }
            catch (ClassCastException cce)
            {
                Trace.log(Trace.ERROR, "Cannot set system value, value is of incorrect type, name: " + info.name_ + ", value: " + value, cce);
                throw new ExtendedIllegalArgumentException(info.name_, ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
        }

        command.append(")");

        CommandCall cmd = new CommandCall(system, command.toString());
        // Neither CHGSYSVAL nor CHGNETA is threadsafe.
        cmd.suggestThreadsafe(false);

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Running system value command: " + command.toString());

        if (!cmd.run())
        {
            throw new AS400Exception(cmd.getMessageList());
        }
    }
}
