///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProgramCall.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.IOException;
import com.ibm.as400.access.MEConstants;


/**
 The ProgramCall class allows a user to call an iSeries program and access data returned after the program runs.
 
 <P>The following example demonstrates the use of Program Call:
 <br>
 <pre>
    // Call programs.
    AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
    
    // See the PCML example in the Toolbox programmer's guide.
    String pcmlName = "qsyrusri.pcml"; // The PCML file we want to use.
    String apiName = "qsyrusri";
    String[] parmsToSet = { "qsyrusri.receiverLength", "qsyrusri.profileName" };
    String[] valuesToSet = { "2048", "JOHNDOE" };
    String[] parmsToGet = { "qsyrusri.receiver.userProfile",  
                "qsyrusri.receiver.previousSignonDate", 
                "qsyrusri.receiver.previousSignonTime",
                "qsyrusri.receiver.displaySignonInfo" };
                
    String[] displayParm = { "Profile", "Last signon Date", "Last signon Time", "Signon Info" };
                
    String[] valuesToGet = null;

    try
    {
        valuesToGet = ProgramCall.run(system, pcmlName, apiName, parmsToSet, valuesToSet, parmsToGet);
        
        for (int i=0; i<valuesToGet.length; ++i)
        {
            System.out.println(displayParm[i] + ": "+ valuesToGet[i] + "\n");
        }
    }
    catch (MEException te)
    {
        te.printStackTrace();
    }
    catch (IOException ioe)
    {
        ioe.printStackTrace();
    }
    
    // Done with the system.
    system.disconnect();
 </pre>
 **/
public final class ProgramCall
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    /**
     *  Private ProgramCall constructor.
     **/
    private ProgramCall()
    {  }


    /**
     *  Constructs a ProgramCall object using the specified <i>system</i>, <i>pgmName</i>, <i>parmsToSet</i>, <i>parmValues</i>, and <i>parmsToGet</i>.
     *
     *  @return  the data requested returned from the program.
     *
     *  @param  system       The system on which to run the program.
     *  @param  pgmName     The name of the program.
     *  @param  parmsToSet  The program parameter names.
     *  @param  parmsValues The program parameter values.
     *  @param  parmsToGet  The data returned from the program call.
     *
     *  @exception  IOException  If an error occurs while communicating with the system.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static String[] run(AS400 system, String pcmlName, String pgmName, String[] parmsToSet, String[] parmValues, String[] parmsToGet) throws IOException, MEException
    {
        synchronized(system)
        {
            system.signon();

            system.toServer_.writeInt(MEConstants.PROGRAM_CALL);

            system.toServer_.writeUTF(pcmlName);
            system.toServer_.writeUTF(pgmName);

            int numParmsToSet = (parmsToSet == null ? 0 : parmsToSet.length);
            system.toServer_.writeInt(numParmsToSet);
            
            for (int i=0; i<numParmsToSet; ++i)
            {
                system.toServer_.writeUTF(parmsToSet[i]);
                system.toServer_.writeUTF(parmValues[i]);
            }
            
            int numParmsToGet = (parmsToGet == null ? 0 : parmsToGet.length);
            system.toServer_.writeInt(numParmsToGet);
            system.toServer_.flush();
            
            for (int i=0; i<numParmsToGet; ++i)
            {
                system.toServer_.writeUTF(parmsToGet[i]);
            }
            
            system.toServer_.flush();

            int registered = system.fromServer_.readInt();

            if (registered == MEConstants.EXCEPTION_OCCURRED)
            {   
                int rc = system.fromServer_.readInt();
                String msg = system.fromServer_.readUTF();
                throw new MEException(msg,rc);
            }
            
            boolean retVal = system.fromServer_.readBoolean(); // Maybe we can remove this from both the client and server?

            // if the program did not run, then create an MEException out
            // of the AS400Message sent back from the MEServer.
            if (!retVal)
            {
                int rc = system.fromServer_.readInt();
               String msg = system.fromServer_.readUTF();
               throw new MEException(msg,rc);
            }

            String[] values = new String[numParmsToGet];

            for (int i=0; i<numParmsToGet; ++i)
            {
                values[i] = system.fromServer_.readUTF();
            }
            
            return values;
        }
    }
}
