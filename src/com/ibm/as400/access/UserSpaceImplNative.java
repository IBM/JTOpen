///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserSpaceImplNative.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

class UserSpaceImplNative extends UserSpaceImplRemote
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Load the service program.
    static
    {
        System.load("/QSYS.LIB/QYJSPART.SRVPGM");
    }

    // Set needed implementation properties.
    public void setProperties(AS400Impl system, String path, String name, String library, boolean mustUseProgramCall)
    {
        super.setProperties(system, path, name, library, true);
    }

    // Setup remote command object on first touch.  Synchronized to protect instance variables.  This method can safely be called multiple times because it checks for a previous call before changing the instance variables.
    protected synchronized void setupRemoteCommand() throws IOException
    {
        // If not setup.
        if (remoteCommand_ == null)
        {
            remoteCommand_ = new RemoteCommandImplNative();
            remoteCommand_.setSystem(system_);
        }
    }

    // declare constants for the attribute type.  These types
    // are used on getAttrs and setAttrs.  *** Warning *** these
    // values are duplicated in the "C" code.  Since there is no
    // way to share a common header file, it is manual process to
    // keep them in sync.  If you add, change or delete one of
    // these constants also change qyjspjus.C in yjsp.xpf.
    private final int INITIAL_VALUE   = 0;
    private final int LENGTH          = 1;
    private final int AUTO_EXTENDIBLE = 2;

    // declare constants for parameter type.
    private final short IN     = 0;
    private final short OUT    = 1;
    private final short INOUT  = 2;

    // declare constants for the entry points.  These constants
    // are used when converting Native exceptions to the exception
    // thrown to the user.
    private final int CREATE         = 0;
    private final int READ           = 1;
    private final int WRITE          = 2;
    private final int DELETE         = 3;
    private final int GET_ATTRIBUTES = 4;
    private final int SET_ATTRIBUTES = 5;

    // ----------------------------------------------------------------------
    //
    // Convert a native exception into the exception we really want to throw
    //
    // ----------------------------------------------------------------------

    private void buildException(NativeException e, int source)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        String id = converter_.byteArrayToString(e.data, 12, 7);

        int substitutionDataLength = BinaryConverter.byteArrayToInt(e.data, 80);
        int textLength = BinaryConverter.byteArrayToInt(e.data, 88);

        String text = converter_.byteArrayToString(e.data, 112 + substitutionDataLength, textLength);

        String idAndText = id + " " + text;

        if ((source == CREATE) ||
            (source == DELETE) ||
            (source == GET_ATTRIBUTES) ||
            (source == SET_ATTRIBUTES))
        {
            throw new IOException(idAndText);
        }

        else if ((source == READ) || (source == WRITE))
        {
            if ((source == READ) && (id.equals("CPF3C14")))
            {
            }

            else if ((id.equals("CPF9801")) ||
                     (id.equals("CPF9810")))
            {
                throw new ObjectDoesNotExistException("path",
                                                      ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
            }

            else if ((id.equals("CPF9802")) ||
                     (id.equals("CPF9820")) ||
                     (id.equals("CPF9830")))
            {
                throw new AS400SecurityException(AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }
            else
                throw new IOException(idAndText);
        }
        else
            throw new IOException(idAndText);
    }


    // ----------------------------------------------------------------------
    //
    // Declare the native methods
    //
    // Performance:
    //   - callProgramReturnInt has a built-in 4K programParameter buffer.
    //     If more than 4K is passed, it must do a malloc/free.  If your
    //     program uses only slightly more than 4K it is probably better
    //     to update the "C" code to have a bigger default buffer.
    //
    //   - callProgramReturnVoid and callProgramReturnBytes have a default
    //     programParameter buffer size of 128K.  If the buffer is larger
    //     128K, the service program will do a malloc/free.  If your
    //     program uses only slightly more than 128K it is probably better
    //     to update the "C" code to have a bigger default buffer.
    //
    // ----------------------------------------------------------------------

    private native void callProgramReturnVoid(byte[] programNameStructure,
                                              byte[] programParameterStructure,
                                              byte[] programParameters) throws NativeException;


    private native int callProgramReturnInt(byte[] programNameStructure,
                                            byte[] programParameterStructure,
                                            byte[] programParameters,
                                            int    valueToReturn) throws NativeException;


    private native byte[] callProgramReturnBytes(byte[] programNameStructure,
                                                 byte[] programParameterStructure,
                                                 byte[] programParameters,
                                                 int    dataLength,
                                                 int    offsetOfData) throws NativeException;

    // Creates a user space.
    public void createZ(String domain, int length, boolean replace, String extendedAttribute, byte initialValue, String textDescription, String authority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String replaceString = (replace) ? "*YES" : "*NO";

        // Set up the buffer that contains the program to call.
        byte [] programNameBuffer = createCommandNameBuffer("QUSCRTUS", 9);

        // Set up the parameter structure.  There is one structure
        // for each parameter.  The structure contains:
        //    4 bytes - the length of the parameter
        //    2 bytes - the parameters usage (input/output/inout)
        //    4 bytes - the offset into the parameter buffer

        byte[] programParameterStructure = new byte[90];

        // The first parameter is user space name (20 bytes)
        BinaryConverter.intToByteArray  (20, programParameterStructure,  0);
        BinaryConverter.shortToByteArray(IN, programParameterStructure,  4);
        BinaryConverter.intToByteArray  ( 0, programParameterStructure,  6);

        // The second parameter is the extended attribute (10 bytes)
        BinaryConverter.intToByteArray  (10, programParameterStructure, 10);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 14);
        BinaryConverter.intToByteArray  (20, programParameterStructure, 16);

        // The third parameter is the initial size (4 bytes)
        BinaryConverter.intToByteArray  ( 4, programParameterStructure, 20);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 24);
        BinaryConverter.intToByteArray  (30, programParameterStructure, 26);

        // The fourth parameter is the initial value (1 bytes)
        BinaryConverter.intToByteArray  ( 1, programParameterStructure, 30);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 34);
        BinaryConverter.intToByteArray  (34, programParameterStructure, 36);

        // The fifth parameter is the public authority (10 bytes)
        BinaryConverter.intToByteArray  (10, programParameterStructure, 40);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 44);
        BinaryConverter.intToByteArray  (35, programParameterStructure, 46);

        // The sixth parameter is the description (50 bytes)
        BinaryConverter.intToByteArray  (50, programParameterStructure, 50);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 54);
        BinaryConverter.intToByteArray  (45, programParameterStructure, 56);

        // The seventh parameter is the replace option (10 bytes)
        BinaryConverter.intToByteArray  (10, programParameterStructure, 60);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 64);
        BinaryConverter.intToByteArray  (95, programParameterStructure, 66);

        // The eighth parameter is the error code area (32 bytes)
        BinaryConverter.intToByteArray  (   32, programParameterStructure, 70);
        BinaryConverter.shortToByteArray(INOUT, programParameterStructure, 74);
        BinaryConverter.intToByteArray  (  105, programParameterStructure, 76);

        // The ninth parameter is the domain (10 bytes)
        BinaryConverter.intToByteArray  ( 10, programParameterStructure, 80);
        BinaryConverter.shortToByteArray( IN, programParameterStructure, 84);
        BinaryConverter.intToByteArray  (137, programParameterStructure, 86);



        // Set up the Parameter area.
        byte[] programParameters = new byte[147];



        // Put the first parm (the user space name) into the parm area.
        setBytes(programParameters, converter_.stringToByteArray(createUserSpaceName(name_, library_)), 0); // @C1C


        // Put the second parm (the extended attribute) into the parm
        // area.  It is a 10 character field
        StringBuffer EAName = new StringBuffer("          ");
        if (extendedAttribute != null)
            EAName.insert(0, extendedAttribute);
        EAName.setLength(10);
        String EANameString = EAName.toString();
        setBytes(programParameters, converter_.stringToByteArray(EANameString), 20); // @C1C


        // Put the third parm (the initial size) into the parm area.
        BinaryConverter.intToByteArray(length, programParameters, 30);


        // Put the fourth parm (the initial value) into the parm area.
        programParameters[34] = initialValue;


        // Put the fifth parm (the authority) into the parm
        // area.  It is a 10 character field
        StringBuffer authorityValue = new StringBuffer("          ");
        if (authority != null)
            authorityValue.insert(0, authority);
        authorityValue.setLength(10);
        String authorityString = authorityValue.toString();
        setBytes(programParameters, converter_.stringToByteArray(authorityString), 35); // @C1C


        // Put the sixth parm (the description) into the parm
        // area.  It is a 50 character field
        StringBuffer descriptionValue = new StringBuffer("                                                  ");
        if (textDescription != null)
            descriptionValue.insert(0, textDescription);
        descriptionValue.setLength(50);
        String descriptionString = descriptionValue.toString();
        setBytes(programParameters, converter_.stringToByteArray(descriptionString), 45); // @C1C


        // Put the seventh parm (replace) into the parm
        // area.  It is a 10 character field
        StringBuffer replaceValue = new StringBuffer("          ");
        replaceValue.insert(0, replaceString);
        replaceValue.setLength(10);
        String replaceString2 = replaceValue.toString();
        setBytes(programParameters, converter_.stringToByteArray(replaceString2), 95); // @C1C


        // Put the eighth parm (the Error Code) into the parm area.
        // This parm is the value 0.  This will cause the
        // program to generate exceptions that will be handled
        // by the service program.
        BinaryConverter.intToByteArray(0, programParameters, 105);



        // Put the ninth parm (the domain) into the parm
        // area.  It is a 10 character field
        StringBuffer domainValue = new StringBuffer("          ");
        if (domain != null)
            domainValue.insert(0, domain);
        domainValue.setLength(10);
        String domainString = domainValue.toString();
        setBytes(programParameters, converter_.stringToByteArray(domainString), 137); // @C1C




        // call the native method to carry out the request.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = ((AS400ImplRemote)system_).swapTo(swapToPH, swapFromPH);
        try
        {
            Trace.log(Trace.INFORMATION, "calling native method (create) ");

            callProgramReturnVoid(programNameBuffer,
                                  programParameterStructure,
                                  programParameters);

            Trace.log(Trace.INFORMATION, "back from native method (normal) (create) ");

        }
        catch (NativeException e)
        {

            Trace.log(Trace.INFORMATION, "back from native method (exception) (create) ");

            buildException(e, CREATE);
        }
        finally
        {
            if (didSwap) ((AS400ImplRemote)system_).swapBack(swapToPH, swapFromPH);
        }
    }

    // ----------------------------------------------------------------------
    //
    // Create the command-name buffer requred by the service program.  It
    // contains the program name, program lib (always QSYS) and the number
    // of parms.
    //
    // ----------------------------------------------------------------------

    private byte[] createCommandNameBuffer(String command, int numberOfParameters)

      throws IOException

    {


        // Set up the buffer that contains the program to call.  The
        // buffer contains three items:
        //    10 characters - the program to call
        //    10 characters - the library that contains the program
        //     4 bytes      - the number of parameters

        StringBuffer programName = new StringBuffer("                    ");
        programName.insert(0, command);
        programName.insert(10,"QSYS");
        programName.setLength(20);
        String programNameString = programName.toString();

        //      converter_ = Converter.getConverter(getCcsidToUse(), system_);                 //$C0D
        byte[] programNameBuffer = new byte[24];
        setBytes(programNameBuffer, converter_.stringToByteArray(programNameString), 0); // @C1C

        BinaryConverter.intToByteArray(numberOfParameters, programNameBuffer, 20);

        return programNameBuffer;
    }

    //   /**
    //     Returns the CCSID from the AS400 to be used for this user space.                //$C0D
    //   **/                                                                               //$C0D
    //   int getCcsidToUse() throws InterruptedException, IOException                      //$C0D
    //   {                                                                                 //$C0D
    //      return system_.getCcsid();                                                     //$C0D
    //      //return system_.getCcsidFromServer();                                         //$C0D
    //   }                                                                                 //$C0D



    // ----------------------------------------------------------------------
    //
    // Create a user space name in the format required by the APIs.
    //
    // ----------------------------------------------------------------------

    private String createUserSpaceName(String name, String library)
    {

        // Create the 20 character user space name.  The first 10
        // characters are the name, the second 10 are the library
        StringBuffer pathName = new StringBuffer("                    ");
        pathName.insert(0, name);
        pathName.insert(10, library);
        pathName.setLength(20);
        return pathName.toString();
    }


    // ----------------------------------------------------------------------
    //
    // Delete a user space                        (called from userSpaceImpl)
    //
    // ----------------------------------------------------------------------

    public void deleteZ()
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {

        // Set up the buffer that contains the program to call.
        byte[] programNameBuffer = createCommandNameBuffer("QUSDLTUS", 2);


        // Set up the parameter structure.  There is one structure
        // for each parameter.  The structure contains:
        //    4 bytes - the length of the parameter
        //    2 bytes - the parameters usage (input/output/inout)
        //    4 bytes - where to find this data (offset) in the parm buffer

        // The first parameter is the name of the user space
        byte[] programParameterStructure = new byte[20];
        BinaryConverter.intToByteArray  (20, programParameterStructure,  0);
        BinaryConverter.shortToByteArray(IN, programParameterStructure,  4);
        BinaryConverter.intToByteArray  ( 0, programParameterStructure,  6);

        // The second parameter is the Error Code area
        BinaryConverter.intToByteArray  (   32, programParameterStructure, 10);
        BinaryConverter.shortToByteArray(INOUT, programParameterStructure, 14);
        BinaryConverter.intToByteArray  (   20, programParameterStructure, 16);


        // Set up the Parameter area.
        byte[] programParameters = new byte[52];


        // Put the first parm (the user space name) into the parm area.
        setBytes(programParameters, converter_.stringToByteArray(createUserSpaceName(name_, library_)), 0); // @C1C


        // Put the second parm (the Error Code) into the parm area.
        // The second parm is the value 0.  This will cause the
        // program to generate exceptions that will be handled
        // by the service program.
        BinaryConverter.intToByteArray(0, programParameters, 20);


        // call the native method to carry out the request.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = ((AS400ImplRemote)system_).swapTo(swapToPH, swapFromPH);
        try
        {
            Trace.log(Trace.INFORMATION, "calling native method (delete) ");

            callProgramReturnVoid(programNameBuffer,
                                  programParameterStructure,
                                  programParameters);

            Trace.log(Trace.INFORMATION, "back from native method (normal) (delete) ");

        }
        catch (NativeException e)
        {

            Trace.log(Trace.INFORMATION, "back from native method (exception) (delete) ");

            buildException(e, DELETE);
        }
        finally
        {
            if (didSwap) ((AS400ImplRemote)system_).swapBack(swapToPH, swapFromPH);
        }
    }


    // ----------------------------------------------------------------------
    //
    // Get the attributes of the user space                  (called locally)
    //
    // ----------------------------------------------------------------------

    protected int getAttributesZ(int valueToReturn)
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        int returnValue = 0;


        // Set up the buffer that contains the program to call.
        byte [] programNameBuffer = createCommandNameBuffer("QUSRUSAT", 5);




        // Set up the parameter structure.  There is one structure
        // for each parameter.  The structure contains:
        //    4 bytes - the length of the parameter
        //    2 bytes - the parameters usage (input/output/inout)
        //    4 bytes - the offset into the parameter buffer

        byte[] programParameterStructure = new byte[60];

        // The first parameter is storage where the attributes are returned
        // (24 bytes)
        BinaryConverter.intToByteArray  ( 24, programParameterStructure,  0);
        BinaryConverter.shortToByteArray(OUT, programParameterStructure,  4);
        BinaryConverter.intToByteArray  (  0, programParameterStructure,  6);

        // The second parameter is the length of the first parameter
        // (a four byte number)
        BinaryConverter.intToByteArray  ( 4, programParameterStructure, 10);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 14);
        BinaryConverter.intToByteArray  (24, programParameterStructure, 16);

        // The third parameter is the format name (8 bytes)
        BinaryConverter.intToByteArray  ( 8, programParameterStructure, 20);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 24);
        BinaryConverter.intToByteArray  (28, programParameterStructure, 26);

        // The fourth parameter is the user space name (20 bytes)
        BinaryConverter.intToByteArray  (20, programParameterStructure, 30);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 34);
        BinaryConverter.intToByteArray  (36, programParameterStructure, 36);

        // The fifth parameter is the error code area
        BinaryConverter.intToByteArray  (   32, programParameterStructure, 40);
        BinaryConverter.shortToByteArray(INOUT, programParameterStructure, 44);
        BinaryConverter.intToByteArray  (   56, programParameterStructure, 46);





        // Set up the Parameter area.
        byte[] programParameters = new byte[88];


        // Note the first parm is an output parm so nothing needs to be
        // done to the parm area.


        // Put the second parm (the length of the output area) in the
        // parm area.
        BinaryConverter.intToByteArray(24, programParameters, 24);


        // Put the third parm (the attribute format) into the parm
        // area.  It is an 8 character field
        setBytes(programParameters, converter_.stringToByteArray("SPCA0100"), 28); // @C1C


        // Put the fourth parm (the user space name) into the parm area.
        setBytes(programParameters, converter_.stringToByteArray(createUserSpaceName(name_, library_)), 36); // @C1C


        // Put the fifth parm (the Error Code) into the parm area.
        // This parm is the value 0.  This will cause the
        // program to generate exceptions that will be handled
        // by the service program.
        BinaryConverter.intToByteArray(0, programParameters, 56);



        // call the native method to carry out the request.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = ((AS400ImplRemote)system_).swapTo(swapToPH, swapFromPH);
        try
        {
            Trace.log(Trace.INFORMATION, "calling native method (get attrs) ");

            returnValue = callProgramReturnInt(programNameBuffer,
                                               programParameterStructure,
                                               programParameters,
                                               valueToReturn);

            Trace.log(Trace.INFORMATION, "back from native method (normal) (get attrs) ");

        }
        catch (NativeException e)
        {

            Trace.log(Trace.INFORMATION, "back from native method (exception) (get attrs) ");

            buildException(e, GET_ATTRIBUTES);
        }
        finally
        {
            if (didSwap) ((AS400ImplRemote)system_).swapBack(swapToPH, swapFromPH);
        }
        return(returnValue);
    }




    // ----------------------------------------------------------------------
    //
    // Get the initial value of the user space    (called from UserSpaceImpl)
    //
    // ----------------------------------------------------------------------

    public byte getInitialValueZ()

      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        return ((byte) getAttributesZ(INITIAL_VALUE));
    }

    // ----------------------------------------------------------------------
    //
    // Get the length of the user space           (called from UserSpaceImpl)
    //
    // ----------------------------------------------------------------------

    public int getLengthZ()

      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {

        return (getAttributesZ(LENGTH));

    }




    // ---------------------------------------------------------------------------
    //
    // Get the auto-extenible value of the user space  (called from UserSpaceImpl)
    //
    // ---------------------------------------------------------------------------

    public boolean isAutoExtendibleZ()
      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {

        // The AS/400 indicator is a single character --
        // F0 is '0' in ebcdic which is false, F1 is '1'
        // in ebcdic which is true.
        if (getAttributesZ(AUTO_EXTENDIBLE) == 0x000000F0)
            return false;
        else
            return true;

    }

    // ----------------------------------------------------------------------
    //
    // Read from a user space                     (called from UserSpaceImpl)
    //
    // ----------------------------------------------------------------------

    public int readZ(byte dataBuffer[], int userSpaceOffset, int dataOffset, int length)

      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {

        int actualLength = readData(dataBuffer, userSpaceOffset, dataOffset, length);

        if (actualLength == 0)
        {
            int userSpaceLength = getAttributesZ(LENGTH);

            if (userSpaceLength < userSpaceOffset)
            {
                actualLength = -1;
            }
            else
            {
                actualLength = userSpaceLength - userSpaceOffset;
                actualLength = readData(dataBuffer, userSpaceOffset, dataOffset, actualLength);
            }
        }

        return actualLength;
    }





    // ----------------------------------------------------------------------
    //
    // Read from a user space                     (called internally)
    //
    // ----------------------------------------------------------------------

    private int readData(byte dataBuffer[], int userSpaceOffset, int dataOffset, int length)

      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        int actualLength = length;

        byte[] returnedData;

        // Set up the buffer that contains the program to call.
        byte[] programNameBuffer = createCommandNameBuffer("QUSRTVUS", 5);


        // Set up the parameter structure.  There is one structure
        // for each parameters.  The structure contains:
        //    4 bytes - the length of the parameter
        //    2 bytes - the parameters usage (input/output/inout)
        //    4 bytes - the offset into the parameter buffer

        byte[] programParameterStructure = new byte[50];

        // The first parameter is the name of the user space
        BinaryConverter.intToByteArray  (20, programParameterStructure,  0);
        BinaryConverter.shortToByteArray(IN, programParameterStructure,  4);
        BinaryConverter.intToByteArray  ( 0, programParameterStructure,  6);

        // The second parameter is the position where to write the data
        BinaryConverter.intToByteArray  ( 4, programParameterStructure, 10);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 14);
        BinaryConverter.intToByteArray  (20, programParameterStructure, 16);

        // The third parameter is the length
        BinaryConverter.intToByteArray  ( 4, programParameterStructure, 20);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 24);
        BinaryConverter.intToByteArray  (24, programParameterStructure, 26);

        // The fourth parameter is the data
        BinaryConverter.intToByteArray  (length,  programParameterStructure, 30);
        BinaryConverter.shortToByteArray(OUT,     programParameterStructure, 34);
        BinaryConverter.intToByteArray  (28,      programParameterStructure, 36);

        // The fifth parameter is the Error Code area
        BinaryConverter.intToByteArray  (32,          programParameterStructure, 40);
        BinaryConverter.shortToByteArray(INOUT,       programParameterStructure, 44);
        BinaryConverter.intToByteArray  (length + 28, programParameterStructure, 46);



        // Set up the Parameter area.
        byte[] programParameters = new byte[length + 60];


        // Put the first parm (the user space name) into the parm area.
        setBytes(programParameters, converter_.stringToByteArray(createUserSpaceName(name_, library_)), 0); // @C1C


        // Put the second parm (the position) into the parm area.  We
        // get an offset from our caller but the API we call needs a
        // position.  Add 1 to the offset to get a position.
        BinaryConverter.intToByteArray(userSpaceOffset + 1, programParameters, 20);


        // Put the third parm (the length) into the parm area.
        BinaryConverter.intToByteArray(length, programParameters, 24);


        // Put the last parm (the Error Code) into the parm area.
        // The second parm is the value 0.  This will cause the
        // program to generate exceptions that will be handled
        // by the service program.
        BinaryConverter.intToByteArray(0, programParameters, length + 28);

        // call the native method to carry out the request.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = ((AS400ImplRemote)system_).swapTo(swapToPH, swapFromPH);
        try
        {
            Trace.log(Trace.INFORMATION, "calling native method (read) ");


            returnedData = callProgramReturnBytes(programNameBuffer,
                                                  programParameterStructure,
                                                  programParameters,
                                                  length,
                                                  28);

            System.arraycopy(returnedData, 0, dataBuffer, dataOffset, length);

            Trace.log(Trace.INFORMATION, "back from native method (normal) (read) ");

        }
        catch (NativeException e)
        {

            Trace.log(Trace.INFORMATION, "back from native method (exception) (read) ");

            buildException(e, READ);

            actualLength = 0;
        }
        finally
        {
            if (didSwap) ((AS400ImplRemote)system_).swapBack(swapToPH, swapFromPH);
        }

        return actualLength;
    }








    // ----------------------------------------------------------------------
    //
    // Set the attributes of a user space                    (called locally)
    //
    // ----------------------------------------------------------------------

    protected void setAttributesZ(int valueToSet, int newValue)

      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {

        // Set up the buffer that contains the program to call.
        byte[] programNameBuffer = createCommandNameBuffer("QUSCUSAT", 4);




        // Set up the parameter structure.  There is one structure
        // for each parameter.  The structure contains:
        //    4 bytes - the length of the parameter
        //    2 bytes - the parameters usage (input/output/inout)
        //    4 bytes - the offset into the parameter buffer

        byte[] programParameterStructure = new byte[40];

        // The first parameter is storage where the library is returned
        // (don't ask me why the library is returned) - 10 bytes.
        BinaryConverter.intToByteArray  ( 10, programParameterStructure,  0);
        BinaryConverter.shortToByteArray(OUT, programParameterStructure,  4);
        BinaryConverter.intToByteArray  (  0, programParameterStructure,  6);

        // The second parameter is the user space name - 20 bytes.
        BinaryConverter.intToByteArray  (20, programParameterStructure, 10);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 14);
        BinaryConverter.intToByteArray  (10, programParameterStructure, 16);

        // The third parameter is the buffer of attributes to change -
        // a maximum of 16 bytes
        BinaryConverter.intToByteArray  (16, programParameterStructure, 20);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 24);
        BinaryConverter.intToByteArray  (30, programParameterStructure, 26);

        // The fourth parameter is the error space (32 bytes)
        BinaryConverter.intToByteArray  (   32, programParameterStructure, 30);
        BinaryConverter.shortToByteArray(INOUT, programParameterStructure, 34);
        BinaryConverter.intToByteArray  (   46, programParameterStructure, 36);




        // Set up the Parameter area.
        byte[] programParameters = new byte[78];


        // Put the second parm (the user space name) into the parm area.
        setBytes(programParameters, converter_.stringToByteArray(createUserSpaceName(name_, library_)), 10); // @C1C


        // Put the third parm (the attribute to set) into the parm area.
        //
        // Add the number of attributes to set.  We always set one
        // attribute at a time.
        BinaryConverter.intToByteArray(1, programParameters, 30);


        // Add the key
        if (valueToSet == LENGTH)
            BinaryConverter.intToByteArray(1, programParameters, 34);
        else if (valueToSet == INITIAL_VALUE)
            BinaryConverter.intToByteArray(2, programParameters, 34);
        else if (valueToSet == AUTO_EXTENDIBLE)
            BinaryConverter.intToByteArray(3, programParameters, 34);


        // Add the length of the value (four bytes for length,
        // one byte for initial value and auto-extendible)
        if (valueToSet == LENGTH)
            BinaryConverter.intToByteArray(4, programParameters, 38);
        else if (valueToSet == INITIAL_VALUE)
            BinaryConverter.intToByteArray(1, programParameters, 38);
        else if (valueToSet == AUTO_EXTENDIBLE)
            BinaryConverter.intToByteArray(1, programParameters, 38);


        // Add the value
        if (valueToSet == LENGTH)
        {
            BinaryConverter.intToByteArray(newValue, programParameters, 42);
        }
        else if (valueToSet == INITIAL_VALUE)
        {
            programParameters[42] = (byte) newValue;
        }
        else if (valueToSet == AUTO_EXTENDIBLE)
        {
            if (newValue == 0)
            {
                setBytes(programParameters, converter_.stringToByteArray("0"), 42);
            }
            else
            {
                setBytes(programParameters, converter_.stringToByteArray("1"), 42);
            }
        }


        // Put the fourth parm (the Error Code) into the parm area.
        // This parm is the value 0.  This will cause the
        // program to generate exceptions that will be handled
        // by the service program.
        BinaryConverter.intToByteArray(0, programParameters, 46);



        // call the native method to carry out the request.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = ((AS400ImplRemote)system_).swapTo(swapToPH, swapFromPH);
        try
        {
            Trace.log(Trace.INFORMATION, "calling native method (set attrs) ");

            callProgramReturnVoid(programNameBuffer,
                                  programParameterStructure,
                                  programParameters);


            Trace.log(Trace.INFORMATION, "back from native method (normal) (set attrs) ");

        }
        catch (NativeException e)
        {

            Trace.log(Trace.INFORMATION, "back from native method (exception) (set attrs) ");

            buildException(e, SET_ATTRIBUTES);
        }
        finally
        {
            if (didSwap) ((AS400ImplRemote)system_).swapBack(swapToPH, swapFromPH);
        }
    }




    // ----------------------------------------------------------------------
    //
    // set the autoExtendible attribute           (called from UserSpaceImpl)
    //
    // ----------------------------------------------------------------------

    public void setAutoExtendibleZ(boolean newValue)

      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {

        if (newValue)
            setAttributesZ(AUTO_EXTENDIBLE, 1);
        else
            setAttributesZ(AUTO_EXTENDIBLE, 0);
    }



    // ----------------------------------------------------------------------
    //
    // Utility routines
    //
    // ----------------------------------------------------------------------


    // @C1A
    private void setBytes(byte[] dest, byte[] src, int offset)
    {
        System.arraycopy(src, 0, dest, offset, src.length);
    }


    // ----------------------------------------------------------------------
    //
    // set the initial value attribute            (called from UserSpaceImpl)
    //
    // ----------------------------------------------------------------------

    public void setInitialValueZ(byte initialValue)

      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        setAttributesZ(INITIAL_VALUE, initialValue);
    }





    // ----------------------------------------------------------------------
    //
    // set the length of the user space           (called from UserSpaceImpl)
    //
    // ----------------------------------------------------------------------

    public void setLengthZ(int length)

      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException

    {
        setAttributesZ(LENGTH, length);
    }






    // ----------------------------------------------------------------------
    //
    // write to a user space                      (called from UserSpaceImpl)
    //
    // ----------------------------------------------------------------------

    public void writeZ(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary)

      throws AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {



        // Set up the buffer that contains the program to call.
        byte[] programNameBuffer = createCommandNameBuffer("QUSCHGUS", 6);


        // Set up the parameter structure.  There is one structure
        // for each parameters.  The structure contains:
        //    4 bytes - the length of the parameter
        //    2 bytes - the parameters usage (input/output/inout)
        //    4 bytes - the offset into the parameter buffer

        // The first parameter is the name of the user space
        byte[] programParameterStructure = new byte[60];
        BinaryConverter.intToByteArray  (20, programParameterStructure,  0);
        BinaryConverter.shortToByteArray(IN, programParameterStructure,  4);
        BinaryConverter.intToByteArray  ( 0, programParameterStructure,  6);

        // The second parameter is the position where to write the data
        BinaryConverter.intToByteArray  ( 4, programParameterStructure, 10);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 14);
        BinaryConverter.intToByteArray  (20, programParameterStructure, 16);

        // The third parameter is the length
        BinaryConverter.intToByteArray  ( 4, programParameterStructure, 20);
        BinaryConverter.shortToByteArray(IN, programParameterStructure, 24);
        BinaryConverter.intToByteArray  (24, programParameterStructure, 26);

        // The fourth parameter is the data
        BinaryConverter.intToByteArray  (length,  programParameterStructure, 30);
        BinaryConverter.shortToByteArray(IN,      programParameterStructure, 34);
        BinaryConverter.intToByteArray  (28,      programParameterStructure, 36);

        // The fifth parameter is the force option
        BinaryConverter.intToByteArray  (1,           programParameterStructure, 40);
        BinaryConverter.shortToByteArray(IN,          programParameterStructure, 44);
        BinaryConverter.intToByteArray  (length + 28, programParameterStructure, 46);

        // The sixth parameter is the Error Code area
        BinaryConverter.intToByteArray  (32,          programParameterStructure, 50);
        BinaryConverter.shortToByteArray(INOUT,       programParameterStructure, 54);
        BinaryConverter.intToByteArray  (length + 29, programParameterStructure, 56);



        // Set up the Parameter area.
        byte[] programParameters = new byte[length + 61];


        // Put the first parm (the user space name) into the parm area.
        setBytes(programParameters, converter_.stringToByteArray(createUserSpaceName(name_, library_)), 0); // @C1C


        // Put the second parm (the position) into the parm area.  We
        // get an offset from the user but our API needs a position.  We
        // add 1 to the offset to get a position.
        BinaryConverter.intToByteArray(userSpaceOffset + 1, programParameters, 20);


        // Put the third parm (the length) into the parm area.
        BinaryConverter.intToByteArray(length, programParameters, 24);


        // Put the fourth parm (the data) into the parm area.
        System.arraycopy(dataBuffer, dataOffset, programParameters, 28, length);


        // Put the fifth parm (the length) into the parm area.
        if (forceAuxiliary == 1)
            setBytes(programParameters, converter_.stringToByteArray("1"), length + 28); // @C1C
        else if (forceAuxiliary == 2)
            setBytes(programParameters, converter_.stringToByteArray("2"), length + 28); // @C1C
        else
            setBytes(programParameters, converter_.stringToByteArray("0"), length + 28); // @C1C


        // Put the last parm (the Error Code) into the parm area.
        // The second parm is the value 0.  This will cause the
        // program to generate exceptions that will be handled
        // by the service program.
        BinaryConverter.intToByteArray(0, programParameters, length + 29);

        // call the native method to carry out the request.
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = ((AS400ImplRemote)system_).swapTo(swapToPH, swapFromPH);
        try
        {
            Trace.log(Trace.INFORMATION, "calling native method (write) ");


            callProgramReturnVoid(programNameBuffer,
                                  programParameterStructure,
                                  programParameters);


            Trace.log(Trace.INFORMATION, "back from native method (normal) (write) ");

        }
        catch (NativeException e)
        {

            Trace.log(Trace.INFORMATION, "back from native method (exception) (write) ");

            buildException(e, WRITE);
        }
        finally
        {
            if (didSwap) ((AS400ImplRemote)system_).swapBack(swapToPH, swapFromPH);
        }
    }

















}

