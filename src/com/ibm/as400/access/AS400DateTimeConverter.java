///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400DateTimeConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.io.UnsupportedEncodingException;

/**
 * The AS400DateTimeConverter class represents a converted date and time.
   The AS/400 System API QWCCVTDT is used to convert a date and time value
   from one format to another format.
 @deprecated This class has been replaced by the DateTimeConverter class.
 @see com.ibm.as400.access.DateTimeConverter
 **/

public class AS400DateTimeConverter
{
    /**
       The system.
    **/
    protected static AS400 as400_;

    /**
    * Constructs a AS400DateTimeConverter object.
    *
    * @param system The AS/400 system.
    **/
    public AS400DateTimeConverter(AS400 system)
    {
//@A2D        Trace.log(Trace.INFORMATION,"constructor");
        if (system == null)                         //@A2A
          throw new NullPointerException("system"); //@A2A
        this.as400_=system;
    }

    /**
     * Converts date and time values from the input format to the requested output format.
     *
     * @param system The AS/400 system.
     * @param in The date and time value to be converted.
     * @param inFormat The input date and time format.  Possible values are:
       <UL>
       <LI>*DTS
       <LI>*JOB
       <LI>*SYSVAL
       <LI>*YMD
       <LI>*YYMD
       <LI>*MDY
       <LI>*MDYY
       <LI>*DMY
       <LI>*DMYY
       <LI>*JUL
       <LI>*LONGJUL
       </UL>
     * @param outFormat The output date and time format.  Possible values are:
       <UL>
       <LI>*DTS
       <LI>*JOB
       <LI>*SYSVAL
       <LI>*YMD
       <LI>*YYMD
       <LI>*MDY
       <LI>*MDYY
       <LI>*DMY
       <LI>*DMYY
       <LI>*JUL
       <LI>*LONGJUL
       </UL>
     * @return The converted date and time value.
    **/

    public static byte[] convert(AS400 system, byte[] in, String inFormat, String outFormat)
    {
        byte[] out = null;
        Trace.log(Trace.INFORMATION,"convert");
        Trace.log(Trace.INFORMATION,"parameters : "+in+" | "+out+" | "
                                                   +inFormat+" | "+outFormat);
        if(system == null)
        {
            throw new NullPointerException("system");
        }


        ProgramCall pgm = new ProgramCall( system );
        try
        {
            // Initialize the name of the program to run
            String progName = "/QSYS.LIB/QWCCVTDT.PGM";
            AS400Text text10 = new AS400Text(10);

            // Setup the 5 parameters
            ProgramParameter[] parmlist = new ProgramParameter[5];

            // First parameter is the input format.
            parmlist[0] = new ProgramParameter( text10.toBytes(inFormat) );


            // Second parameter is the input variable.
            parmlist[1] = new ProgramParameter( in );

            // Third parameter is the output format.
            parmlist[2] = new ProgramParameter( text10.toBytes(outFormat) );


            // Fourth parameter is the output variable.
            parmlist[3] = new ProgramParameter(17);


            // Fifth parameter is the error format.
            byte[] errorCode = new byte[70];
            parmlist[4] = new ProgramParameter( errorCode );

            // Set the program name and parameter list
            pgm.setProgram( progName, parmlist );
            //pgm.suggestThreadsafe();  // QWCCVTDT is thread-safe.  @B1A

            // Run the program
            if (pgm.run()!=true)
            {
                // Note that there was an error
                Trace.log(Trace.ERROR, "program failed!" );

                // Show the messages
                AS400Message[] messagelist = pgm.getMessageList();
                for (int i=0; i < messagelist.length; i++)
                {
                   // Trace.log(Trace.INFORMATION, messagelist[i] );
                }
                throw new AS400Exception(messagelist);
            }
            else
            {
                out=parmlist[3].getOutputData();
            }
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR,"convert failed : "+e);
        }

        return out;

    }




    /**
     * Returns the Date object in the YYMD format.
     * @param in The date and time value to be converted.
     * @param format The format of the date and time value being provided.
     * @return The Date object in the YYMD format.
     *
    **/
    public static Date getDate(byte[] in, String format)

    {
        Trace.log(Trace.INFORMATION,"getDate");
        String outFormat = new String("*YYMD");
        byte[] out = convert(as400_, in, format, outFormat);

        RecordFormat recordFormat=new RecordFormat();

        CharacterFieldDescription[] cfd=new CharacterFieldDescription[7];;
        cfd[0] = new CharacterFieldDescription(new AS400Text(4),"year");
        cfd[1] = new CharacterFieldDescription(new AS400Text(2),"month");
        cfd[2] = new CharacterFieldDescription(new AS400Text(2),"day");
        cfd[3] = new CharacterFieldDescription(new AS400Text(2),"hour");
        cfd[4] = new CharacterFieldDescription(new AS400Text(2),"minute");
        cfd[5] = new CharacterFieldDescription(new AS400Text(2),"second");
        cfd[6] = new CharacterFieldDescription(new AS400Text(3),"millisecond");
        for(int i=0;i<7;i++)
            recordFormat.addFieldDescription(cfd[i]);
        try
        {
            Record record = recordFormat.getNewRecord(out);
            Calendar dateTime = Calendar.getInstance();
            dateTime.clear();
            dateTime.set (
                Integer.parseInt(((String)record.getField("year")).trim()),// year
                Integer.parseInt(((String)record.getField("month")).trim())-1,  // month
                Integer.parseInt(((String)record.getField("day")).trim()),   // day
                Integer.parseInt(((String)record.getField("hour")).trim()),  //hour
                Integer.parseInt(((String)record.getField("minute")).trim()), //minute
                Integer.parseInt(((String)record.getField("second")).trim()));//second
            return dateTime.getTime();

        } catch (UnsupportedEncodingException e)
        {
            Trace.log(Trace.INFORMATION,"convert Date/Time : "+e);
        }

        return null;
    }

    /**
     * Returns the converted date and time in a byte array.
     * @param date The Date object to be converted. It must be in the format YYMD.
     * @param format The output date and time format.
     * @return The converted date and time in a byte array.
     *
    **/
    public static byte[] getByteArray(Date date,String format)
    {
        Trace.log(Trace.INFORMATION,"getByteArray");
        Calendar dateTime=Calendar.getInstance();
        dateTime.setTime(date);

        RecordFormat recordFormat=new RecordFormat();

        CharacterFieldDescription[] cfd=new CharacterFieldDescription[7];;
        cfd[0] = new CharacterFieldDescription(new AS400Text(4),"year");
        cfd[1] = new CharacterFieldDescription(new AS400Text(2),"month");
        cfd[2] = new CharacterFieldDescription(new AS400Text(2),"day");
        cfd[3] = new CharacterFieldDescription(new AS400Text(2),"hour");
        cfd[4] = new CharacterFieldDescription(new AS400Text(2),"minute");
        cfd[5] = new CharacterFieldDescription(new AS400Text(2),"second");
        cfd[6] = new CharacterFieldDescription(new AS400Text(3),"millisecond");
        for(int i=0;i<7;i++)
                        recordFormat.addFieldDescription(cfd[i]);

        Record record = recordFormat.getNewRecord();
        record.setField("year",Integer.toString(dateTime.get(Calendar.YEAR)));
        record.setField("month",Integer.toString(dateTime.get(Calendar.MONTH)+1));
        record.setField("day",Integer.toString(dateTime.get(Calendar.DAY_OF_MONTH)));
        record.setField("hour",Integer.toString(dateTime.get(Calendar.HOUR_OF_DAY)));
        record.setField("minute",Integer.toString(dateTime.get(Calendar.MINUTE)));
        record.setField("second",Integer.toString(dateTime.get(Calendar.SECOND)));
        record.setField("millisecond",Integer.toString(dateTime.get(Calendar.MILLISECOND)));

        byte[] in = null;
        try
        {
            in = record.getContents();
            AS400Text text17 = new AS400Text(17);
        }
        catch(Exception e)
        {
            Trace.log(Trace.ERROR,"Error constructing program parameters");
        }
        String inFormat = new String("*YYMD");
        return convert(as400_, in,inFormat, format);

    }
}

