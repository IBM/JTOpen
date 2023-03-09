///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  The ProgramCall class allows a user to call a program and access data returned after the program runs
 *  from a wireless device.  This class provides a modified subset of the functions available in 
 *  com.ibm.as400.access.ProgramCall.<p>
 *  
 *  Each PCML document must be registered with the MEserver.  The document can be
 *  registered during runtime when the PCML document name parameter is provided or as an argument when the
 *  MEServer is started or reconfigured.<p>  The registration is simply telling the system which PCML-defined program(s)
 *  to run.  <p>
 *
 *  A hashtable is used to specify the names and values of the parameters to set.  The key in the hashtable
 *  is the name of the parameter to set and the key value is the value of the corresponding parameter to set.
 *  There is also a string array which specifies the name of the PCML output parameter(s) to return.
 *
 *  <P>The following example demonstrates the use of Program Call:
 *  <br>
 *  <pre>
 *   // Call programs.
 *   AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
 *   
 *   // See the PCML example in the Toolbox programmer's guide.
 *   String pcmlName = "qsyrusri.pcml"; // The PCML document describing the program we want to use.
 *   String apiName = "qsyrusri";
 *
 *   Hashtable parametersToSet = new Hashtable();
 *   parametersToSet.put("qsyrusri.receiverLength", "2048");
 *   parametersToSet.put("qsyrusri.profileName", "JOHNDOE" };
 *
 *   String[] parametersToGet = { "qsyrusri.receiver.userProfile",  
 *               "qsyrusri.receiver.previousSignonDate", 
 *               "qsyrusri.receiver.previousSignonTime",
 *               "qsyrusri.receiver.displaySignonInfo" };
 *               
 *   String[] valuesToGet = null;
 *
 *   try
 *   {
 *       valuesToGet = ProgramCall.run(system, pcmlName, apiName, parametersToSet, parametersToGet);
 *       
 *       // Get and display the user profile.
 *       System.out.println("User profile: " + valuesToGet[0]);
 *
 *       // Get and display the date in a readable format.
 *       char[] c = valuesToGet[1].toCharArray();
 *       System.out.println("Last Signon Date: " + c[3]+c[4]+"/"+c[5]+c[6]+"/"+c[1]+c[2] );
 *
 *       // Get and display the time in a readable format.
 *       char[] d = valuesToGet[2].toCharArray();
 *       System.out.println("Last Signon Time: " + d[0]+d[1]+":"+d[2]+d[3]);
 *
 *       // Get and display the signon info.
 *       System.out.println("Signon Info: " + valuesToGet[3] );
 *   }
 *   catch (MEException te)
 *   {
 *       // Handle the exception.
 *   }
 *   catch (IOException ioe)
 *   {
 *       // Handle the exception
 *   }
 *   
 *   // Done with the system object.
 *   system.disconnect();
 *  </pre>
 *
 *  @see com.ibm.as400.access.ProgramCall
 **/
public final class ProgramCall
{
    /**
     *  Private ProgramCall constructor.
     **/
    private ProgramCall()
    {  }


    /**
     *  Constructs a ProgramCall object using the specified <i>system</i>, <i>pgmName</i>, <i>parametersToSet</i>, <i>parameterValues</i>, and <i>parametersToGet</i>.
     *
     *  @param  system                The system on which to run the program.
     *  @param  pcmlName           The name of the PCML document.
     *  @param  program               The name of the program.
     *  @param  parametersToSet  The hashtable containg the key, which is the name of the program parameter, and the key value, 
     *                                          which is the corresponding program parameter value..
     *  @param  parametersToGet  The data returned from the program call.
     *
     *  @return  the data requested returned from the program.  An empty array is returned if no data is returned.
     *
     *  @exception  IOException  If an error occurs while communicating with the system.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static String[] run(AS400 system, String pcmlName, String program, Hashtable parametersToSet, String[] parametersToGet) throws IOException, MEException
    {
        if (system == null)
            throw new NullPointerException("system");
        
        if (pcmlName == null)
            throw new NullPointerException("pcmlName");
        
        if (program == null)
            throw new NullPointerException("program");

        synchronized(system)
        {
            system.connect();

            system.toServer_.writeInt(MEConstants.PROGRAM_CALL);

            system.toServer_.writeUTF(pcmlName);
            system.toServer_.writeUTF(program);

            // Write the number parameter name/value pairs specified in the hashtable.
            system.toServer_.writeInt(parametersToSet.size());
            system.toServer_.flush();

            // Loop through the hashtable and write out the key (parameter name)
            // and the key value (parameter value)
            for (Enumeration e = parametersToSet.keys() ; e.hasMoreElements() ;) 
            {
                String key = (String)e.nextElement();
                system.toServer_.writeUTF(key);
                system.toServer_.writeUTF(parametersToSet.get(key).toString());
            }

            system.toServer_.flush();

            // Write the number of parameters to get or return to the caller.
            int numparametersToGet = (parametersToGet == null ? 0 : parametersToGet.length);
            system.toServer_.writeInt(numparametersToGet);
            system.toServer_.flush();
            
            // Write the parameter names to get.
            for (int i=0; i<numparametersToGet; ++i)
            {
                system.toServer_.writeUTF(parametersToGet[i]);
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

            String[] values = new String[numparametersToGet];

            for (int i=0; i<numparametersToGet; ++i)
            {
                values[i] = system.fromServer_.readUTF();
            }
            
            return values;
        }
    }
}
