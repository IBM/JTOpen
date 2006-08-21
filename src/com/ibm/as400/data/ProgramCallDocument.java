///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramCallDocument.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.Trace;                          // @C4A
import com.ibm.as400.access.ProgramCall;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.ByteArrayOutputStream;                       //@E1A
import java.io.ByteArrayInputStream;                        //@E1A
import java.io.BufferedInputStream;                         //@E1A
import java.io.OutputStream;                                //@E1A
import java.io.InputStreamReader;                           //@E1A
import java.io.LineNumberReader;                            //@E1A



import java.util.MissingResourceException;

import java.util.zip.GZIPInputStream;

import javax.xml.transform.TransformerException;                    //@E1A
import org.xml.sax.SAXException;                                    //@E1A


/**
 * XML Document based program call.
 *
 * The <code>ProgramCallDocument</code> class uses a Program Call Markup Language (PCML) to
 * call i5/OS system programs.
 * PCML is an XML language for describing the input and output parameters
 * to the i5/OS system program.
 *
 * This class parses a PCML document and allows the application to call
 * i5/OS system programs described in the PCML document.
 *
 * <h3>Command Line Interface</h3>
 * The command line interface may be used to serialize
 * PCML document definitions. Note that XPCML documents cannot
 * be serialized.
 * <pre>
 * <kbd>java com.ibm.as400.data.ProgramCallDocument
 *     -serialize
 *     <i>pcml document name</i></kbd>
 * </pre>
 * Options:
 * <dl>
 * <dt><kbd>-serialize</kbd>
 * <dd>Parses the PCML document and creates a serialized version of the document.
 * The name of the serialized file will match the document name, and the file extension will be
 * <code><strong>.pcml.ser</code></strong> (lowercase).
 * <p><dt><kbd><i>pcml document name</i></kbd>
 * <dd>The fully-qualified resource name of the PCML document
 * which defines the program interface.
 * </dl>
 */
public class ProgramCallDocument implements Serializable, Cloneable
{                                                                   // @C1C @C3C
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = -1836686444079106483L;	    // @C1A

    /**
     * Constant indicating a serialized PCML or XPCML document is being streamed.
     * @see #ProgramCallDocument(AS400,String,InputStream,ClassLoader,InputStream,int)
    **/
    public static final int SERIALIZED = 0;

    /**
     * Constant indicating a source PCML document is being streamed.
    * @see #ProgramCallDocument(AS400,String,InputStream,ClassLoader,InputStream,int)
    **/
    public static final int SOURCE_PCML = 1;

    /**
     * Constant indicating a source XPCML document is being streamed.
    * @see #ProgramCallDocument(AS400,String,InputStream,ClassLoader,InputStream,int)
    **/
    public static final int SOURCE_XPCML = 2;

    private AS400 m_as400;
    private PcmlDocument m_pcmlDoc;

    /**
     Constructs a <code>ProgramCallDocument</code>.
    The PCML or XPCML document resource will be loaded from the classpath.
    If the document is a PCML document, the classpath will first be searched for a serialized resource.
    XPCML documents cannot be serialized.
    If a serialized resource is not found, the classpath will be
    searched for a PCML or XPCML source file.

    @param sys The system on which to run the program.
    @param docName The document resource name of the PCML document for the programs to be called.
     All PCML-related file extensions are assumed to be lowercase (for example, <tt>.pcml</tt> or <tt>.pcml.ser</tt>).
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"

	@exception PcmlException when the specified PCML document cannot be found
    */
    public ProgramCallDocument(AS400 sys, String docName)
    	throws PcmlException
   	{
        if (sys == null)     warnNull("sys");
        if (docName == null) warnNull("docName");

        m_as400 = sys;

        m_pcmlDoc = loadPcmlDocument(docName, null,null);        // @C8C @E1C
        if (m_as400 != null) m_pcmlDoc.setAs400(m_as400);
    }

  /**
    Constructs a <code>ProgramCallDocument</code>.
    The XPCML document resource will be loaded from the classpath and parsed using
    the XML schema definitions provided in the input XSD stream.

    @param sys The system on which to run the program.
    @param docName The document resource name of the PCML document for the programs to be called.
     All PCML-related file extensions are assumed to be lowercase (for example, <tt>.pcml</tt> or <tt>.pcml.ser</tt>).
    @param xsdStream An input stream that contains XML schema definitions that extend XPCML.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"

	@exception PcmlException when the specified PCML document cannot be found
    */

    public ProgramCallDocument(AS400 sys, String docName, InputStream xsdStream)
    	throws PcmlException
   	{
        if (sys == null)     warnNull("sys");
        if (docName == null) warnNull("docName");

        m_as400 = sys;

        m_pcmlDoc = loadPcmlDocument(docName, null,xsdStream);        // @C8C
        if (m_as400 != null) m_pcmlDoc.setAs400(m_as400);

    }

    /**
     Constructs a <code>ProgramCallDocument</code>.
    The PCML or XPCML document resource will be loaded from the classpath.
    If the document is a PCML document, the classpath will first be searched for a serialized resource.
    XPCML documents cannot be serialized.
    If a serialized resource is not found, the classpath will be
    searched for a PCML or XPCML source file.

     @param sys The system on which to run the program.
    @param docName The document resource name of the PCML document for the programs to be called.
     All PCML-related file extensions are assumed to be lowercase (for example, <tt>.pcml</tt> or <tt>.pcml.ser</tt>).
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"
    @param loader The ClassLoader that will be used when loading the specified document resource.

	@exception PcmlException when the specified PCML document cannot be found
    */
    public ProgramCallDocument(AS400 sys, String docName, ClassLoader loader)       // @C8A
    	throws PcmlException
   	{
        if (sys == null)     warnNull("sys");
        if (docName == null) warnNull("docName");

        m_as400 = sys;                                      // @C8A

        m_pcmlDoc = loadPcmlDocument(docName, loader,null);      // @C8A
        if (m_as400 != null) m_pcmlDoc.setAs400(m_as400);                        // @C8A
    }


  /**
    Constructs a <code>ProgramCallDocument</code>.
    The XPCML document resource will be loaded from the classpath and parsed using
    the XML schema definitions provided in the input XSD stream.
    @param sys The system on which to run the program.
    @param docName The document resource name of the PCML document for the programs to be called.
     All PCML-related file extensions are assumed to be lowercase (for example, <tt>.pcml</tt> or <tt>.pcml.ser</tt>).
    @param loader The ClassLoader that will be used when loading the specified document resource.
    @param xsdStream An input stream that contains XML schema definitions that extend XPCML
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"

    @exception PcmlException when the specified PCML document cannot be found
    */
     public ProgramCallDocument(AS400 sys, String docName, ClassLoader loader, InputStream xsdStream)       // @C8A
    	throws PcmlException
   	{
        if (sys == null)     warnNull("sys");
        if (docName == null) warnNull("docName");

        m_as400 = sys;                                      // @C8A

        m_pcmlDoc = loadPcmlDocument(docName, loader,xsdStream);      // @C8A
        if (m_as400 != null) m_pcmlDoc.setAs400(m_as400);                        // @C8A
    }

  /**
    Constructs a <code>ProgramCallDocument</code>.
    The XPCML document resource will be loaded from the classpath and parsed using
    the XML schema definitions provided in the input XSD stream.
    @param sys The system on which to run the program.
    @param docName The document resource name of the PCML document for the programs to be called.
     All PCML-related file extensions are assumed to be lowercase (for example, <tt>.pcml</tt> or <tt>.pcml.ser</tt>).
    @param docStream The InputStream from which to read the contents of the document.
    @param loader The ClassLoader that will be used when loading the DTD for PCML. This parameter can be null.
    @param xsdStream An input stream that contains XML schema definitions that extend XPCML
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml".  This parameter can be null.
    @param type The type of data contained in docStream. Possible values are:
    <UL>
    <LI>ProgramCallDocument.SERIALIZED - The docStream contains a serialized PCML or XPCML document.
    <LI>ProgramCallDocument.SOURCE_PCML - The docStream contains a PCML document.
    <LI>ProgramCallDocument.SOURCE_XPCML - The docStream contains an XPCML document.
    </UL>
    @exception PcmlException when the specified PCML document cannot be found
    */
     public ProgramCallDocument(AS400 sys, String docName, InputStream docStream, ClassLoader loader, InputStream xsdStream, int type)
    	throws PcmlException
   	{
        if (sys == null)     warnNull("sys");
        if (docName == null) warnNull("docName");

        if (type == ProgramCallDocument.SERIALIZED)
        {
          m_pcmlDoc = loadSerializedPcmlDocumentFromStream(docStream);
        }
        else if (type == ProgramCallDocument.SOURCE_PCML)
        {
          m_pcmlDoc = loadSourcePcmlDocumentFromStream(docName, docStream, loader, xsdStream, false);
        }
        else if (type == ProgramCallDocument.SOURCE_XPCML)
        {
          m_pcmlDoc = loadSourcePcmlDocumentFromStream(docName, docStream, loader, xsdStream, true);
        }
        else {
          throw new ExtendedIllegalArgumentException("type (" + type + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        m_as400 = sys;
        if (m_as400 != null) m_pcmlDoc.setAs400(m_as400);
    }

     /**
    Constructs a <code>ProgramCallDocument</code>.
    {@link #setSystem setSystem()} and {@link #setDocument setDocument()} must be called prior to using the object.

	@exception PcmlException when the specified PCML document cannot be found
    */
    public ProgramCallDocument()
    	throws PcmlException                                        // @C1A
   	{                                                               // @C1A
        m_as400 = null;                                             // @C1A
        m_pcmlDoc = null;                                           // @C1A
    }                                                               // @C1A

    /**
     Constructs a <code>ProgramCallDocument</code>.
     {@link #setSystem setSystem()} must be called prior to using the object.
     The PCML or XPCML document resource will be loaded from the classpath.
     If the document is a PCML document, the classpath will first be searched for a serialized resource.
     XPCML documents cannot be serialized.
     If a serialized resource is not found, the classpath will be
     searched for a PCML or XPCML source file.

     @param docName The document resource name of the PCML document for the programs to be called.
     All PCML-related file extensions are assumed to be lowercase (for example, <tt>.pcml</tt> or <tt>.pcml.ser</tt>).
     The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"

     @exception PcmlException when the specified PCML document cannot be found
     */
    public ProgramCallDocument(String docName)
      throws PcmlException
    {
      if (docName == null) {
        throw new NullPointerException("docName");
      }

      m_pcmlDoc = loadPcmlDocument(docName, null,null);
    }

    /**
    Clones the <code>ProgramCallDocument</code> and the objects contained in it.
    {@link #setSystem setSystem()} and {@link #setDocument setDocument()} must be called prior to using the object.
    */
    public Object clone()
   	{                                                               // @C3A
   	    ProgramCallDocument newPcml = null;                         // @C3A
        try                                                         // @C3A
        {                                                           // @C3A
            newPcml = (ProgramCallDocument) super.clone();          // @C3A
            if (m_pcmlDoc != null)                                  // @C3A
                newPcml.m_pcmlDoc = (PcmlDocument) m_pcmlDoc.clone();   // @C3A
        }                                                           // @C3A
        catch (CloneNotSupportedException e)                        // @C3A
        {}                                                          // @C3A

        return newPcml;                                             // @C3A
    }                                                               // @C3A

    // Custom serialization
    private void writeObject(ObjectOutputStream out)
        throws IOException                                          // @C1A
    {                                                               // @C1A
		synchronized (this)                                         // @C1A
		{                                                           // @C1A
            if (m_pcmlDoc != null)                                  // @C1A
            {                                                       // @C1A
                m_pcmlDoc.setSerializingWithData(true);             // @C1A
            }                                                       // @C1A

			// Perform default serialization
			out.defaultWriteObject();                               // @C1A

		} // end of synchronized code                               // @C1A
    }                                                               // @C1A

    /**
	 * Provides a command line interface to <code>ProgramCallDocument</code>.  See the class description.
    * Note that XPCML documents cannot be serialized.
	 *
     */
    public static void main(String[] args)
    {
		PcmlDocument pd = null;;

        System.setErr(System.out);
        final String errMsg = SystemResourceFinder.format(DAMRI.PCD_ARGUMENTS);

		if (args.length == 2)
        {
        	if (!args[0].equalsIgnoreCase("-SERIALIZE"))
        	{
        		System.out.println(errMsg);
        		System.exit(-1);
        	}

            // Load the document from source (previously serialized documents are ignored)
            try
            {
				pd = loadSourcePcmlDocument(args[1], null,null);         // @C8C
            }
			catch (PcmlException e)
			{
				System.out.println(e.getLocalizedMessage());
				System.exit(-1);
			}

            // Save the document as a serialized file
			try
			{
				savePcmlDocument(pd);
			}
			catch (Exception e)
			{
				System.out.println(e.getLocalizedMessage());
				System.exit(-1);
			}

        }
		else
		{
    		System.out.println(errMsg);
    		System.exit(-1);
		}

    }



    /**
     Calls the named program.

     @param name The name of the &lt;program&gt; element in the PCML document.
     @exception PcmlException
                If an error occurs.
    */
    public boolean callProgram(String name)
        throws PcmlException
    {
        try
        {
            return m_pcmlDoc.callProgram(name);
        }
        catch (AS400SecurityException e)
        {
            if (Trace.isTraceErrorOn())                         // @C4A
               e.printStackTrace(Trace.getPrintWriter());       // @C4C
            PcmlException pe = new PcmlException(e);
            throw pe;
        }
        catch (ObjectDoesNotExistException e)
        {
            if (Trace.isTraceErrorOn())                         // @C4A
               e.printStackTrace(Trace.getPrintWriter());       // @C4C
            PcmlException pe = new PcmlException(e);
            throw pe;
        }
        catch (InterruptedException e)
        {
            if (Trace.isTraceErrorOn())                         // @C4A
               e.printStackTrace(Trace.getPrintWriter());       // @C4C
            PcmlException pe = new PcmlException(e);
            throw pe;
        }
        catch (ErrorCompletingRequestException e)
        {
            if (Trace.isTraceErrorOn())                         // @C4A
               e.printStackTrace(Trace.getPrintWriter());       // @C4C
            PcmlException pe = new PcmlException(e);
            throw pe;
        }
        catch (IOException e)
        {
            if (Trace.isTraceErrorOn())                         // @C4A
               e.printStackTrace(Trace.getPrintWriter());       // @C4C
            PcmlException pe = new PcmlException(e);
            throw pe;
        }

    }

    /**
    Returns an "errno" value for the named service program element.
    <p>
    The named program element must be defined as service program entrypoint.
    The value returned is the "errno" value resulting from the most recent
    call to the program. If the program has not been called, zero is returned.

    @return The integer "errno" value for the named service program element.

    @param name The name of the &lt;program&gt; element in the PCML document.
    @exception PcmlException
               If an error occurs.
    */
    public int getErrno(String name)
        throws PcmlException                                        // @B1A
    {                                                               // @B1A
        return m_pcmlDoc.getErrno(name);                            // @B1A
    }                                                               // @B1A

    /**
    Returns a <code>Descriptor</code> for the specified pcml document.
    The PCML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for a PCML source file.

    @param docName The document resource name of the PCML document for which the Descriptor is returned.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"

    @return The Descriptor for the pcml element of the named pcml file.

	@exception PcmlException when the specified PCML document cannot be found
    */
    public static Descriptor getDescriptor(String docName)
        throws PcmlException                                        // @C5A
    {
        PcmlDocument pd = null;

        pd = loadPcmlDocument(docName, null,null);                       // @C8C

        return new PcmlDescriptor(pd);
    }                                                               // @C5A

     /**
    Returns a <code>Descriptor</code> for the specified xpcml document.
    The XPCML document resource will be loaded from the classpath.

    @param docName The document resource name of the XPCML document for which the Descriptor is returned.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"
    @param xsdStream An input stream that contains XML schema definitions that extend XPCML

    @return The Descriptor for the pcml element of the named pcml file.

	@exception PcmlException when the specified PCML document cannot be found
    */
    public static Descriptor getDescriptor(String docName, InputStream xsdStream)
        throws PcmlException                                        // @C5A
    {
        PcmlDocument pd = null;

        pd = loadPcmlDocument(docName, null, xsdStream);                       // @C8C

        return new PcmlDescriptor(pd);
    }                                                               // @C5A

    /**
    Returns a <code>Descriptor</code> for the specified pcml document.
    The PCML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for a PCML source file.

    @param docName The document resource name of the PCML document for which the Descriptor is returned.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"
    @param loader The ClassLoader that will be used when loading the specified document resource.
    @return The Descriptor for the pcml element of the named pcml file.

	@exception PcmlException when the specified PCML document cannot be found
    */
    public static Descriptor getDescriptor(String docName, ClassLoader loader)
        throws PcmlException                                        // @C8A
    {
        PcmlDocument pd = null;                                     // @C8A

        pd = loadPcmlDocument(docName, loader,null);                             // @C8A

        return new PcmlDescriptor(pd);                              // @C8A
    }

    /**
    Returns a <code>Descriptor</code> for the specified xpcml document.
    The XPCML document resource will be loaded from the classpath.

    @param docName The document resource name of the PCML document for which the Descriptor is returned.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"
    @param loader The ClassLoader that will be used when loading the specified document resource.
    @param xsdStream An input stream that contains XML schema definitions that extend XPCML.

    @return The Descriptor for the pcml element of the named pcml file.

	@exception PcmlException when the specified PCML document cannot be found
    */
    public static Descriptor getDescriptor(String docName, ClassLoader loader, InputStream xsdStream)
        throws PcmlException                                        // @C8A
    {
        PcmlDocument pd = null;                                     // @C8A

        pd = loadPcmlDocument(docName, loader,xsdStream);                             // @C8A

        return new PcmlDescriptor(pd);                              // @C8A
    }


    /**
    Returns a <code>Descriptor</code> for the current pcml document.

    @return The Descriptor for the pcml element of the current pcml file or
            null if the pcml document has not be set.
    */
    public Descriptor getDescriptor()                               // @C5A
    {

        if (m_pcmlDoc == null)
            return null;
        else
            return new PcmlDescriptor(m_pcmlDoc);
    }                                                               // @C5A

    /**
    Returns an int return value for the named service program element.
    <p>
    The named program element must be defined as service program entrypoint.
    The value returned is the integer return value from the most recent
    call to the program. If the program has not been called, zero is returned.

    @return The integer return value for the named service program element.

    @param name The name of the &lt;program&gt; element in the PCML document.
    @exception PcmlException
               If an error occurs.
    */
    public int getIntReturnValue(String name) throws PcmlException  // @B1A
    {                                                               // @B1A
        return m_pcmlDoc.getIntReturnValue(name);                   // @B1A
    }                                                               // @B1A

    /**
    Returns an int value for the named element.
    <p>
    If the named element is String or a Number output value of a program, the value will
    be converted to an int.

    @return The integer value for the named element.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @exception PcmlException
               If an error occurs.
    */
    public int getIntValue(String name)
        throws PcmlException
    {
        return m_pcmlDoc.getIntValue(name);
    }

    /**
    Returns an int value for the named element given indices to the data element.
    If the data element is an array or is an element in a structure array, an index
    must be specified for each dimension of the data.
    <p>
    If the named element is String or a Number output value of a program, the value will
    be converted to an int.

    @return The integer value for the named element.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @exception PcmlException
               If an error occurs.
    */
    public int getIntValue(String name, int[] indices)
        throws PcmlException
    {
        return m_pcmlDoc.getIntValue(name, new PcmlDimensions(indices));
    }


    /**
     Returns the ProgramCall object that was used in the most recent invocation of {@link #callProgram(String) callProgram()}.
     @return The ProgramCall object; null if callProgram() has not been called.
     **/
    public ProgramCall getProgramCall()
    {
      return ( m_pcmlDoc == null ? null : m_pcmlDoc.getProgramCall() );
    }

    /**
    Returns a String value for the named element.
    <p>
    This method is used when the string type cannot be determined until
    run-time.  In those cases, the PCML document cannot be used to indicate
    the string type so this method is used to get the value using the
    string type that is specified.
    <p>
    If the named element is String or a Number output value of a program, the value will
    be converted to a String.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param type The bidi string type, as defined by the CDRA (Character
                Data Representataion Architecture).
    @exception PcmlException
               If an error occurs.
    @see com.ibm.as400.access.BidiStringType
    */
    public String getStringValue(String name, int type)
        throws PcmlException                                            // @C9A
    {
        return m_pcmlDoc.getStringValue(name, type);                    // @C9A
    }

    /**
    Returns a String value for the named element given indices to the data element.
    If the data element is an array or is an element in a structure array, an index
    must be specified for each dimension of the data.
    <p>
    This method is used when the string type cannot be determined until
    run-time.  In those cases, the PCML document cannot be used to indicate
    the string type so this method is used to get the value using the
    string type that is specified.
    <p>
    If the named element is String or a Number output value of a program, the value will
    be converted to a String.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param indices An array of indices for setting the value of an element in an array.
    @param type The bidi string type, as defined by the CDRA (Character
                Data Representataion Architecture).
    @exception PcmlException
               If an error occurs.
    @see com.ibm.as400.access.BidiStringType
    */
    public String getStringValue(String name, int[] indices, int type)
        throws PcmlException                                            // @C9A
    {
        return m_pcmlDoc.getStringValue(name, new PcmlDimensions(indices), type);           // @C9A
    }

    /**
    Returns the list of i5/OS system messages returned from running the
    program. An empty list is returned if the program has not been run yet.

    @param name The name of the &lt;program&gt; element in the PCML document.
    @return The array of messages returned by the server for the program.
    @exception PcmlException
               If an error occurs.
    */
    public AS400Message[] getMessageList(String name)
        throws PcmlException
    {
        return m_pcmlDoc.getMessageList(name);
    }

    /**
    Returns the number of bytes reserved for output for the named element.

    @return The number of bytes reserved for output for the named element.
    @param name The name of the &lt;data&gt; or &lt;struct&gt; element in the PCML document.
    @exception PcmlException
               If an error occurs.
    */
    public int getOutputsize(String name)
        throws PcmlException
    {
        return m_pcmlDoc.getOutputsize(name);
    }

    /**
    Returns the number of bytes reserved for output for the named element and indices.

    @return The number of bytes reserved for output for the named element.
    @param name The name of the &lt;data&gt; or &lt;struct&gt; element in the PCML document.
    @param indices An array of indices for accessing the output size of an element in an array.
    @exception PcmlException
               If an error occurs.
    */
    public int getOutputsize(String name, int[] indices)
        throws PcmlException
    {
        return m_pcmlDoc.getOutputsize(name, new PcmlDimensions(indices));
    }

    /**
    Returns the Java object value for the named element.
    <p>
    If the named element is an output value of a program, the value will
    be converted from i5/OS system data to a Java Object.
    <p>
    The type of object returned depends on the description in the PCML document.
    <table border=1>
    <tr valign=top><th>PCML Description</th><th>Object Returned</th></tr>
    <tr valign=top><td><code>type=char</td><td><code>String</code></td></tr>
    <tr valign=top><td><code>type=byte</td><td><code>byte[]</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=2<br>
                             precision=15</td><td><code>Short</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=2<br>
                             precision=16</td><td><code>Integer</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=4<br>
                             precision=31</td><td><code>Integer</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=4<br>
                             precision=32</td><td><code>Long</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=8<br>
                             precision=63</td><td><code>Long</code></td></tr>
    <tr valign=top><td><code>type=packed</td><td><code>BigDecimal</code></td></tr>
    <tr valign=top><td><code>type=zoned</td><td><code>BigDecimal</code></td></tr>
    <tr valign=top><td><code>type=float<br>
                             length=4</td><td><code>Float</code></td></tr>
    <tr valign=top><td><code>type=float<br>
                             length=8</td><td><code>Double</code></td></tr>
    </table>

    @return The Java object value for the named &lt;data&gt; element in the PCML document.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @exception PcmlException
               If an error occurs.
    */
    public Object getValue(String name)
        throws PcmlException
    {
        return m_pcmlDoc.getValue(name);
    }

    /**
    Returns the Java object value for the named element given indices to the data element.
    If the data element is an array or is an element in a structure array, an index
    must be specified for each dimension of the data.
    <p>
    If the named element is an output value of a program, the value will
    be converted from i5/OS system data to a Java Object.
    <p>
    The type of object returned depends on the description in the PCML document.
    <table border=1>
    <tr valign=top><th>PCML Description</th><th>Object Returned</th></tr>
    <tr valign=top><td><code>type=char</td><td><code>String</code></td></tr>
    <tr valign=top><td><code>type=byte</td><td><code>byte[]</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=2<br>
                             precision=15</td><td><code>Short</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=2<br>
                             precision=16</td><td><code>Integer</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=4<br>
                             precision=31</td><td><code>Integer</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=4<br>
                             precision=32</td><td><code>Long</code></td></tr>
    <tr valign=top><td><code>type=int<br>
                             length=8<br>
                             precision=63</td><td><code>Long</code></td></tr>
    <tr valign=top><td><code>type=packed</td><td><code>BigDecimal</code></td></tr>
    <tr valign=top><td><code>type=zoned</td><td><code>BigDecimal</code></td></tr>
    <tr valign=top><td><code>type=float<br>
                             length=4</td><td><code>Float</code></td></tr>
    <tr valign=top><td><code>type=float<br>
                             length=8</td><td><code>Double</code></td></tr>
    </table>

    @return The Java object value for the named &lt;data&gt; element in the PCML document.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param indices An array of indices for accessing the value of an element in an array.
    @exception PcmlException
               If an error occurs.
    */
    public Object getValue(String name, int[] indices)
        throws PcmlException
    {
        return m_pcmlDoc.getValue(name, new PcmlDimensions(indices));
    }

    /**
    Gets the system on which programs are to be called.

    @return The current system for this ProgramCallDocument.

    @see #setSystem
    **/
    public AS400 getSystem()                                        // @C4A
    {                                                               // @C4A
        return m_as400;                                             // @C4A
    }                                                               // @C4A


    /**
     Serializes the ProgramCallDocument. Note that XPCML documents
     cannot be serialized.

     The filename of the serialized file will be of the form
     <pre>
     <kbd><i>docName</i>.pcml.ser</kbd>
     </pre>
     where <kbd><i>docName</i>.pcml.ser</kbd> (lowercase) is the name of the document used to
	 construct this object.

     @exception PcmlException If an error occurs.
     */
    public void serialize()
        throws PcmlException
    {
      if (m_pcmlDoc == null) {
        throw new PcmlException(DAMRI.DOCUMENT_NOT_SET);
      }
      try {
        savePcmlDocument(m_pcmlDoc);
      }
      catch (IOException e) {
        if (Trace.isTraceErrorOn()) e.printStackTrace(Trace.getPrintWriter());
        throw new PcmlException(e);
      }
    }


    /**
     Serializes the ProgramCallDocument to a stream.

     @param outputStream The output stream to which to serialize the object.
     @exception IOException  If an error occurs while writing to the stream.
     @exception PcmlException  If an error occurs while processing PCML.
     **/
    public void serialize(OutputStream outputStream)
      throws IOException, PcmlException
    {
      if (outputStream == null) {
        throw new NullPointerException("outputStream");
      }
      if (m_pcmlDoc == null) {
        throw new PcmlException(DAMRI.DOCUMENT_NOT_SET);
      }
      savePcmlDocument(m_pcmlDoc, outputStream);
    }

    /**
    Sets the Java object value for the named element using a int input.
    <p>
    The named element must be able to be set using a Integer object.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param value The int value for the named element.
    @exception PcmlException
               If an error occurs.
    */
    public void setIntValue(String name, int value)
        throws PcmlException
    {
        setValue(name, new Integer(value));
    }

    /**
    Sets the Java object value for the named element using an int input value
    given indices to the data element.
    <p>
    The named element must be able to be set using a Integer object.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param indices An array of indices for setting the value of an element in an array.
    @param value The int value for the named element.
    @exception PcmlException
               If an error occurs.
    */
    public void setIntValue(String name, int[] indices, int value)
        throws PcmlException
    {
        setValue(name, indices, new Integer(value));
    }

    /**
    Sets the Java object value for the named element using a String input.
    <p>
    This method is used when the string type cannot be determined until
    run-time.  In those cases, the PCML document cannot be used to indicate
    the string type so this method is used to set the value and the
    string type of the input value.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param value The int value for the named element.
    @param type The bidi string type, as defined by the CDRA (Character
                Data Representataion Architecture).
    @exception PcmlException
               If an error occurs.
    @see com.ibm.as400.access.BidiStringType
    */
    public void setStringValue(String name, String value, int type)
        throws PcmlException                                            // @C9A
    {
        m_pcmlDoc.setStringValue(name, value, type);                    // @C9A
    }

    /**
    Sets the Java object value for the named element using a String input value
    given indices to the data element.
    <p>
    This method is used when the string type cannot be determined until
    run-time.  In those cases, the PCML document cannot be used to indicate
    the string type so this method is used to set the value and the
    string type of the input value.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param indices An array of indices for setting the value of an element in an array.
    @param value The int value for the named element.
    @param type The bidi string type, as defined by the CDRA (Character
                Data Representataion Architecture).
    @exception PcmlException
               If an error occurs.
    @see com.ibm.as400.access.BidiStringType
    */
    public void setStringValue(String name, int[] indices, String value, int type)
        throws PcmlException                                            // @C9A
    {
        m_pcmlDoc.setStringValue(name, value, new PcmlDimensions(indices), type);           // @C9A
    }

    /**
    Sets the PCML or XPCML document resource.
    The PCML or XPCML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for a PCML or XPCML source file.

    @param docName The document resource name of the PCML document for the programs to be called.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"

	@exception PcmlException when the specified PCML document cannot be found
    **/
    public void setDocument(String docName)
        throws PcmlException                                        // @C1A
    {                                                               // @C1A
        if (m_pcmlDoc != null)                                      // @C1A
            throw new PcmlException(DAMRI.DOCUMENT_ALREADY_SET );   // @C1A

        if (docName == null)                                        // @C1A
            throw new NullPointerException("docName");              // @C1A

        m_pcmlDoc = loadPcmlDocument(docName, null,null);           // @C1A @C8C
        if (m_as400 != null) m_pcmlDoc.setAs400(m_as400);                                // @C1A
    }                                                               // @C1A


    /**
    Sets the XPCML document resource.
    The XPCML document resource will be loaded from the classpath.

    @param docName The document resource name of the PCML document for the programs to be called.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"
    @param xsdStream An input stream that contains XML schema definitions that extend XPCML

	@exception PcmlException when the specified PCML document cannot be found
    **/
    public void setDocument(String docName, InputStream xsdStream)
        throws PcmlException                                        // @C1A
    {                                                               // @C1A
        if (m_pcmlDoc != null)                                      // @C1A
            throw new PcmlException(DAMRI.DOCUMENT_ALREADY_SET );   // @C1A

        if (docName == null)                                        // @C1A
            throw new NullPointerException("docName");              // @C1A

        m_pcmlDoc = loadPcmlDocument(docName, null,xsdStream);      // @C1A @C8C
        if (m_as400 != null) m_pcmlDoc.setAs400(m_as400);                                // @C1A
    }                                                               // @C1A


    /**
    Sets the PCML or XPCML document resource.
    The PCML or XPCML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for a PCML or XPCML source file.

    @param docName The document resource name of the PCML document for the programs to be called.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"
    @param loader The ClassLoader that will be used when loading the specified document resource.

	@exception PcmlException when the specified PCML document cannot be found
    **/
    public void setDocument(String docName, ClassLoader loader)     // @C8A
        throws PcmlException                                        // @C8A
    {                                                               // @C8A
        if (m_pcmlDoc != null)                                      // @C8A
            throw new PcmlException(DAMRI.DOCUMENT_ALREADY_SET );   // @C8A

        if (docName == null)                                        // @C8A
            throw new NullPointerException("docName");              // @C8A

        m_pcmlDoc = loadPcmlDocument(docName, loader,null);         // @C8A
        if (m_as400 != null) m_pcmlDoc.setAs400(m_as400);                                // @C8A
    }                                                               // @C8A

    /**
    Sets the XPCML document resource.
    The XPCML document resource will be loaded from the classpath.

    @param docName The document resource name of the PCML document for the programs to be called.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myPcml"
    @param loader The ClassLoader that will be used when loading the specified document resource.
    @param xsdStream An input stream that contains XML schema definitions that extend XPCML

	@exception PcmlException when the specified PCML document cannot be found
    **/
    public void setDocument(String docName, ClassLoader loader,InputStream xsdStream)     // @C8A
        throws PcmlException                                        // @C8A
    {                                                               // @C8A
        if (m_pcmlDoc != null)                                      // @C8A
            throw new PcmlException(DAMRI.DOCUMENT_ALREADY_SET );   // @C8A

        if (docName == null)                                        // @C8A
            throw new NullPointerException("docName");              // @C8A

        m_pcmlDoc = loadPcmlDocument(docName, loader, xsdStream);   // @C8A
        m_pcmlDoc.setAs400(m_as400);                                // @C8A
    }                                                               // @C8A


    /**
    Sets the system on which to call programs.

    @param system  The system on which to call programs.

    **/
    public void setSystem(AS400 system)                             // @C1A
    {                                                               // @C1A
        if (system == null)                                         // @C1A
            throw new NullPointerException("system");               // @C1A

        m_as400 = system;                                           // @C1A
        m_pcmlDoc.setAs400(m_as400);                                // @C1A
    }                                                               // @C1A

    /**
    Sets the Java object value for the named element.
    <p>
    If the input value provided is not an instance of the
    correct Java class for the defined data type, it will be converted
    to the correct Java class. For example, an element defined as "<code>type=int length=2 precision=15</code>",
    will be converted to a Java Short object. In this case the value specified must be an instance of Number or String.
    <p>
    If the named element is an input value to a program, the value will
    be converted to i5/OS system data when <code>callProgram()</code> is called.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param value The java object value for the named element. The type of Object passed must be
    the correct type for the element definition or a String that can be converted to the correct type.
    @exception PcmlException
               If an error occurs.
    */
    public void setValue(String name, Object value)
        throws PcmlException
    {
        m_pcmlDoc.setValue(name, value);
    }

    /**
    Sets the Java object value for the named element
    given indices to the data element.
    If the data element is an array or is an element in a structure array, an index
    must be specified for each dimension of the data.
    <p>
    If the input value provided is not an instance of the
    correct Java class for the defined data type, it will be converted
    to the correct Java class. For example, an element defined as "<code>type=int length=2 precision=15</code>",
    will be converted to a Java Short object. In this case the value specified must be an instance of Number or String.
    <p>
    If the named element is an input value to a program, the value will
    be converted to i5/OS system data when <code>callProgram()</code> is called.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param indices An array of indices for setting the value of an element in an array.
    @param value The java object value for the named element. The type of Object passed must be
    the correct type for the element definition or a String that can be converted to the correct type.
    @exception PcmlException
               If an error occurs.
    */
    public void setValue(String name, int[] indices, Object value)
        throws PcmlException
    {
        m_pcmlDoc.setValue(name, value, new PcmlDimensions(indices));
    }

    /**
    Allows for dynamically specifying the program path.

    @param program The name of the &lt;program&gt; element in the PCML document.
    @param path A String containing the path to the program objectto be run on the server.
    @exception PcmlException
               If an error occurs.
    */
    public void setPath(String program, String path)                    // @D1A
        throws PcmlException                                            // @D1A
    {
        m_pcmlDoc.setPath(program, path);                               // @D1A
    }

    /**
    Allows the overriding of the threadsafe attribute of a program element.

    @param program The name of the &lt;program&gt; element in the PCML document.
    @param threadsafe A boolean indicating whether the named program element should be considered
    thread safe (true) or not (false).
    @exception PcmlException
               If an error occurs.
    */
    public void setThreadsafeOverride(String program, boolean threadsafe)
        throws PcmlException
    {
        m_pcmlDoc.setThreadsafeOverride(program, threadsafe);           // @C6A
    }

    /**
    Gets the value of the override of the threadsafe attribute of a program element.

    @param program The name of the &lt;program&gt; element in the PCML document.
    @exception PcmlException
               If an error occurs.
    */
    public boolean getThreadsafeOverride(String program)
        throws PcmlException
    {
        return m_pcmlDoc.getThreadsafeOverride(program);           // @C6A
    }


    /**
      Saves a PcmlDocument as a serialized resource.
    **/
    private static void savePcmlDocument(PcmlDocument pd)
        throws PcmlException, IOException
    {
      String outFileName = pd.getDocName() + SystemResourceFinder.m_pcmlSerializedExtension;
      BufferedOutputStream fos = null;
      try
      {
        fos = new BufferedOutputStream(new FileOutputStream(outFileName));

        savePcmlDocument(pd, fos);
      }
      finally
      {
        if (fos != null) fos.close();
      }
    }


    /**
      Saves a PcmlDocument as a serialized resource.
    **/
    private static void savePcmlDocument(PcmlDocument pd, OutputStream outStream)
        throws PcmlException, IOException
    {
        pd.setSerializingWithData(false);
        ObjectOutputStream out = null;

        try
        {
          out = new ObjectOutputStream(outStream);
          out.writeObject(pd);

          String outFileName = pd.getDocName() + SystemResourceFinder.m_pcmlSerializedExtension;
          Trace.log(Trace.PCML, SystemResourceFinder.format(DAMRI.PCML_SERIALIZED, new Object[] {outFileName} )); // @D2C
        }
        finally
        {
          if (out != null) out.close();
        }
    }


    /**
      Loads a serialized PcmlDocument or constructs the document from
      a PCML source file.
    **/
    private static PcmlDocument loadPcmlDocument(String docName, ClassLoader loader, InputStream xsdStream)        // @C8C
        throws PcmlException
    {
        PcmlDocument pd = null;

		pd = loadSerializedPcmlDocument(docName, loader);               // @C8C

        // If a PcmlDocument was successfully loaded from a serialized file
        // return the document loaded.
        if (pd != null)
            return pd;

		pd = loadZippedSerializedPcmlDocument(docName, loader);         // @C7A @C8C

        // If a PcmlDocument was successfully loaded from a zipped serialized file
        // return the document loaded.
        if (pd != null)                                         // @C7A
            return pd;                                          // @C7A

		pd = loadSourcePcmlDocument(docName, loader, xsdStream);                   // @C8C

        return pd;
    }

    private static PcmlDocument loadSerializedPcmlDocumentFromStream(InputStream docStream)
        throws PcmlException
    {
        PcmlDocument pd = null;
        ObjectInputStream in = null;

        try
        {
            // Try to open the serialized PCML document
            in = new ObjectInputStream(docStream);
            pd = (PcmlDocument)in.readObject();
        }
        catch (Exception e)
        {
          if (Trace.isTraceErrorOn())
             e.printStackTrace(Trace.getPrintWriter());
          throw new PcmlException(e);
        }
        finally
        {
          if (in != null) try { in.close(); } catch (Exception e) {}
        }

        return pd;
    }

    private static PcmlDocument loadSourcePcmlDocumentFromStream(String docName, InputStream docStream, ClassLoader loader, InputStream xsdStream, boolean isXPCML) throws PcmlException
    {
      PcmlDocument pd = null;

      // Construct the PCML document from a source file
      try
      {
          PcmlSAXParser psp = new PcmlSAXParser(docName, docStream, loader, xsdStream, isXPCML);
          pd = psp.getPcmlDocument();
      }
      catch (ParseException pe)
      {
          pe.reportErrors();
          throw new PcmlException(pe);
      }
      catch (PcmlSpecificationException pse)
      {
          pse.reportErrors();
          throw new PcmlException(pse);
      }
      catch (Exception e)
      {
        if (Trace.isTraceErrorOn())
           e.printStackTrace(Trace.getPrintWriter());
        throw new PcmlException(e);
      }

      return pd;
    }

    /**
      Loads a serialized PcmlDocument from a serialized file.
    **/
    private static PcmlDocument loadSerializedPcmlDocument(String docName, ClassLoader loader)  // @C8C
        throws PcmlException
    {
        PcmlDocument pd = null;
        InputStream is = null;
        ObjectInputStream in = null;

        // First try to find a serialized PCML document
        try
        {
            // Try to open the serialized PCML document
            is = SystemResourceFinder.getSerializedPCMLDocument(docName, loader);   // @C8C

            in = new ObjectInputStream(is);
            pd = (PcmlDocument)in.readObject();
        }
        catch (MissingResourceException e)
        {
            // Ignore exception and try looking for PCML source (below)
        }
        catch (StreamCorruptedException e)
        {
            // Ignore exception and try looking for zipped serialized PCML (below)
        }
        catch (IOException e)
        {
            if (Trace.isTraceErrorOn())                         // @C4A
               e.printStackTrace(Trace.getPrintWriter());       // @C4C
            throw new PcmlException(e);
        }
        catch (ClassNotFoundException e)
        {
            if (Trace.isTraceErrorOn())                         // @C4A
               e.printStackTrace(Trace.getPrintWriter());       // @C4C
            throw new PcmlException(e);
        }
        finally
        {
          if (in != null) try { in.close(); } catch (Exception e) {}
          if (is != null) try { is.close(); } catch (Exception e) {}
        }

        return pd;
    }

    /**
      Loads a serialized PcmlDocument from a serialized file.
    **/
    private static PcmlDocument loadZippedSerializedPcmlDocument(String docName, ClassLoader loader)    // @C8C
        throws PcmlException
    {
        PcmlDocument pd = null;
        InputStream is = null;
        GZIPInputStream gzis = null;
        ObjectInputStream in = null;

        // First try to find a serialized PCML document
        try
        {
            // Try to open the serialized PCML document
            is = SystemResourceFinder.getSerializedPCMLDocument(docName, loader);       // @C8C

            gzis = new GZIPInputStream(is);
            in = new ObjectInputStream(gzis);
            pd = (PcmlDocument)in.readObject();
        }
        catch (MissingResourceException e)
        {
            // Ignore exception and try looking for PCML source (below)
        }
        catch (IOException e)
        {
            if (Trace.isTraceErrorOn())                         // @C4A
               e.printStackTrace(Trace.getPrintWriter());       // @C4C
            throw new PcmlException(e);
        }
        catch (ClassNotFoundException e)
        {
            if (Trace.isTraceErrorOn())                         // @C4A
               e.printStackTrace(Trace.getPrintWriter());       // @C4C
            throw new PcmlException(e);
        }
        finally
        {
          if (in != null)   try { in.close(); } catch (Exception e) {}
          if (gzis != null) try { gzis.close(); } catch (Exception e) {}
          if (is != null)   try { is.close(); } catch (Exception e) {}
        }

        return pd;
    }

    /**
      Loads a PcmlDocument from a PCML source file.
    **/
    private static PcmlDocument loadSourcePcmlDocument(String docName, ClassLoader loader, InputStream xsdStream)      // @C8C
        throws PcmlException
    {

        PcmlDocument pd = null;

        // Construct the PCML document from a source file
        try
        {
            PcmlSAXParser psp = new PcmlSAXParser(docName, loader, xsdStream);         // @C2A @C8C
            pd = psp.getPcmlDocument();                             // @C2A
        }
        catch (ParseException pe)
        {
            pe.reportErrors();
            throw new PcmlException(pe);
        }
        catch (PcmlSpecificationException pse)
        {
            pse.reportErrors();
            throw new PcmlException(pse);
        }
        catch (Exception e) //@E0A
        {
          if (Trace.isTraceErrorOn()) //@E0A
             e.printStackTrace(Trace.getPrintWriter()); //@E0A
          throw new PcmlException(e);
        }

        return pd;
    }


    // @E1A -- ALL NEW XPCML methods....
    /**
     Generates XPCML representing the data associated with the passed in program name.
     XPCML is XML based on the XML schema defined in xpcml.xsd.   XPCML is similar
     to PCML but allows for better validation of parameters and allows parameter
     data to be input and output within an XML document.  PCML is data-less in
     that only parameter formats are input via PCML.  In PCML, data values are set using
     the setValue methods of the ProgramCallDocument class and data values are
     gotten using the getValue methods of ProgramCallDocument.  In XPCML, data values
     can be input directly within the XPCML document on construction and data values can be output
     as XML using the generateXPCML method.
     Throws an XmlException if this object contains no data.
     @param pgmName The program to generate XPCML for
     @param outputStream The output stream to which to write the text.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing XPCML.
     **/
    public void generateXPCML(String pgmName, OutputStream outputStream)
      throws IOException, XmlException
    {
      if (outputStream == null) {
        throw new NullPointerException("outputStream");
      }
      if (m_pcmlDoc == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
      m_pcmlDoc.generateXPCML(pgmName, outputStream);
    }


    // @E2C -- Added more info on XPCML.  Changed all RFML references to XPCML.
    /**
     Generates XPCML representing the data contained in the entire PCML node tree.
     XPCML is XML based on the XML schema defined in xpcml.xsd.   XPCML is similar
     to PCML but allows for better validation of parameters and allows parameter
     data to be input and output within an XML document.  PCML is data-less in
     that only parameter formats are input via PCML.  In PCML, data values are set using
     the setValue methods of the ProgramCallDocument class and data values are
     gotten using the getValue methods of ProgramCallDocument.  In XPCML, data values
     can be input directly within the XPCML document on construction and data values can be output
     as XML using the generateXPCML method.

     Throws an XmlException if this object contains no data.

     @param outputStream The output stream to which to write the text.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing XPCML.
     **/
    public void generateXPCML(OutputStream outputStream)
      throws IOException, XmlException
    {
      if (outputStream == null) {
        throw new NullPointerException("outputStream");
      }
      if (m_pcmlDoc == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
      m_pcmlDoc.generateXPCML(null, outputStream);
    }


    /**
     Generates XPCML representing the data contained in the entire PCML node tree.
     XPCML is XML based on the XML schema defined in xpcml.xsd.   XPCML is similar
     to PCML but allows for better validation of parameters and allows parameter
     data to be input and output within an XML document.  PCML is data-less in
     that only parameter formats are input via PCML.  In PCML, data values are set using
     the setValue methods of the ProgramCallDocument class and data values are
     gotten using the getValue methods of ProgramCallDocument.  In XPCML, data values
     can be input directly within the XPCML document on construction and data values can be output
     as XML using the generateXPCML method.
     Throws an XmlException if this object contains no data.

     @param fileName The pathname of the file to which to write the text.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing XPCML.
     **/
    public void generateXPCML(String fileName)
      throws IOException, XmlException
    {
      if (fileName == null) {
        throw new NullPointerException(fileName);
      }
      FileOutputStream fos = null;
      try
      {
        fos = new FileOutputStream(fileName);
        generateXPCML(null, fos);
      }
      finally {
        if (fos != null) fos.close();
      }
    }

    /**
     Generates XPCM representing the data contained for the passed in program name.
     XPCML is XML based on the XML schema defined in xpcml.xsd.   XPCML is similar
     to PCML but allows for better validation of parameters and allows parameter
     data to be input and output within an XML document.  PCML is data-less in
     that only parameter formats are input via PCML.  In PCML, data values are set using
     the setValue methods of the ProgramCallDocument class and data values are
     gotten using the getValue methods of ProgramCallDocument.  In XPCML, data values
     can be input directly within the XPCML document on construction and data values can be output
     as XML using the generateXPCML method.
     Throws an XmlException if this object contains no data.

     @param pgmName  The program name to generate XPCML for.
     @param fileName The pathname of the file to which to write the text.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing XPCML.
     **/
    public void generateXPCML(String pgmName,String fileName)
      throws IOException, XmlException
    {
      if (fileName == null) {
        throw new NullPointerException(fileName);
      }
      if (pgmName == null) {
        throw new NullPointerException(pgmName);
      }
      FileOutputStream fos = null;
      try
      {
        fos = new FileOutputStream(fileName);
        generateXPCML(pgmName, fos);
      }
      finally {
        if (fos != null) fos.close();
      }
    }

     // ******************************
     // @E0A -- New method           *
     // ******************************
     /**
     setXsdName --   Use prior to calling generateXPCML to set the xsd name that will
                     appear in the generated <xpcml tag from generateXPCML. If name is not
                     set then "xpcml.xsd" will appear in <xpcml tag.  This allows the user
                     to override the default and put in the name of their own xsd that was
                     used in condensing the XPCML output.

     @param xsdName  The XSD name to appear in the <xpcml tag when XPCML is output using the
                     generateXPCML method.
    **/

    public void setXsdName(String xsdName)
    {
        m_pcmlDoc.setXsdName(xsdName);
    }


     // ******************************
     // @E0A -- New method           *
     // ******************************
     /**
     getXsdName --   Returns the value of the xsdName to be used on the <xpcml tag when
                     generating xpcml

     @return The String "xsdName" value for this program object.
     **/

    public String getXsdName()
    {
        return m_pcmlDoc.getXsdName();
    }


    /**
     Transforms a PCML stream to its equivalent XPCML stream.
     Throws an XmlException if this object contains no data.

     @param pcmlStream The PCML input stream.
     @param xpcmlStream  The output XPCML stream.
     @exception IOException  If an error occurs while writing the data.
     @exception PcmlException  If an error occurs while processing XPCML.

     **/

    public static void transformPCMLToXPCML(InputStream pcmlStream, OutputStream xpcmlStream)
           throws IOException, PcmlException, TransformerException, SAXException	
    	{

           if (pcmlStream == null) {
             throw new NullPointerException();
           }

           if (xpcmlStream == null) {
             throw new NullPointerException();
           }

           // Transform the pcml document to its equivalent xpcml document
           XPCMLHelper.doTransform("pcml_xpcml.xsl",pcmlStream, xpcmlStream); //@CRS
      }

    /**
     Transforms a fully specified XPCML stream to a more condensed XPCML stream
     and an XSD stream representing the new type definitions created while condensing
     Throws an XmlException if this object contains no data.

     @param fullStream The full XPCML input stream.
     @param xsdStream  The output xsd stream.
     @param condensedStream  The output condensed XPCML stream.
     @param xsdStreamName  The name of the xsd stream ("name.xsd") that will be created
     @exception IOException  If an error occurs while writing the data.
     @exception PcmlException  If an error occurs while processing XPCML.

     **/

    public static void condenseXPCML(InputStream fullStream, OutputStream xsdStream, OutputStream condensedStream, String xsdStreamName)
           throws IOException, PcmlException, TransformerException, SAXException	
    	{
           String xpcmlName="";

           if (fullStream == null) {
             throw new NullPointerException();
           }

           if (xsdStream == null) {
             throw new NullPointerException();
           }

           if (condensedStream == null) {
             throw new NullPointerException();
           }

           if (xsdStreamName == null) {
             throw new NullPointerException();
           }


           // Copy input stream fullStream into twoOutputStream
           ByteArrayOutputStream outStream1 = new ByteArrayOutputStream();

           byte[] bytesIn = new byte[1000];
           int bytesRead = 0;
           bytesRead = fullStream.read(bytesIn);

           while (bytesRead != -1)
           {
              outStream1.write(bytesIn,0,bytesRead);
              bytesRead = fullStream.read(bytesIn);
           }

           outStream1.flush();
           outStream1.close();

           // Cache the line count of the header
           ByteArrayInputStream inStreamFull = new ByteArrayInputStream(outStream1.toByteArray());
           LineNumberReader lnr = new LineNumberReader(new InputStreamReader(inStreamFull));
           try
           {
             String line = lnr.readLine();
             boolean found=false;
             while (line != null && !found)
             {
               // Look for xpcml tag
               if (line.indexOf("xsi:noNamespaceSchemaLocation=") != -1)
               {
                  found = true;
                  int index1 = line.indexOf("xsi:noNamespaceSchemaLocation=");
                  int index2=0;
                  index2 = line.indexOf("'", index1);
                  int index3=0;
                  if (index2 == -1)
                  {
                     index2 = line.indexOf("\"",index1);
                     if (index2 != -1)
                       index3 = line.indexOf("\"", index2+1);
                  }
                  else
                  {
                     index3 = line.indexOf("'",index2+1);
                  }
                  xpcmlName = line.substring(index2+1,index3);
                  continue;
               }
               if (line.indexOf("xsi:noNamespaceSchemaLocation =") != -1)
               {
                  found = true;
                  int index1 = line.indexOf("xsi:noNamespaceSchemaLocation =");
                  int index2=0;
                  index2 = line.indexOf("'", index1);
                  int index3=0;
                  if (index2 == -1)
                  {
                     index2 = line.indexOf("\"",index1);
                     if (index2 != -1)
                       index3 = line.indexOf("\"", index2+1);
                  }
                  else
                  {
                     index3 = line.indexOf("'",index2+1);
                  }
                  xpcmlName = line.substring(index2+1,index3);
                  continue;
               }
               line = lnr.readLine();
             }
           }
           catch (IOException e)
           {
             Trace.log(Trace.PCML, "Error when reading input stream in condenseXPCML");
           }
           if (xpcmlName == "")
              xpcmlName="xpcml.xsd";

           // Write contents of ByteArrayOutputStream to ByteArrayInputStream
           ByteArrayInputStream inStream1 = new ByteArrayInputStream(outStream1.toByteArray());  // no need to close byte-array stream
           ByteArrayInputStream inStream2 = new ByteArrayInputStream(outStream1.toByteArray());  // no need to close byte-array stream

           // Create new XSD type definitions based on full XPCML stream
           XPCMLHelper.doCondenseTransform("xpcml_xsd.xsl",inStream1, xsdStream, xpcmlName); //@CRS
           // Create condensed XPCML using XSD and full XPCML stream
           XPCMLHelper.doCondenseTransform("xpcml_basic.xsl",inStream2, condensedStream, xsdStreamName); //@CRS

      }


    // Traces a warning message about a null-valued parameter.
    private static final void warnNull(String parmName)
    {
      if (Trace.isTraceOn()) Trace.log(Trace.WARNING, "Null value specified for '" + parmName + "' parameter on ProgramCallDocument constructor.");
    }


}
