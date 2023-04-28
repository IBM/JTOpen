package com.ibm.as400.access;

import java.util.Hashtable;

public class IFSGetEAsRep extends IFSDataStream {
	private static final String copyright = "Copyright (C) 2004-2004 International Business Machines Corporation and others.";
	private static final int TEMPLATE_LENGTH = 4;
	private static final boolean DEBUG = true;
	
	public Object getNewDataStream()
	  {
	    return new IFSGetEAsRep();
	  }
	

	public Hashtable<String,Object> getExtendedAttributeValues()
	  {
	    Hashtable<String,Object> results = new Hashtable<String,Object>();

	    // The offset to the start of the "optional/variable section" depends on the datastream level.
	    int optionalSectionOffset = HEADER_LENGTH + TEMPLATE_LENGTH;

	    // Step through the optional fields, looking for the "EA list" field (code point 0x0009).
	    Trace.log(Trace.INFORMATION, "Extended Attribute returned: data length " + data_.length);
	    int curLL_offset = optionalSectionOffset;
	    int curLL = get32bit(curLL_offset);   // list length
	    int curCP = get16bit(curLL_offset+4); // code point
	    int eaListOffset;  // offset to start of Extended Attr list
	    
	    while (curCP != 0x0009 && (curLL_offset+curLL+6 <= data_.length))
	    {
	      curLL_offset += curLL;
	      curLL = get32bit(curLL_offset);
	      curCP = get16bit(curLL_offset+4);
	    }

	    if (curCP == 0x0009)
	    {
	      // We found the start of the Extended Attributes list.
	      eaListOffset = curLL_offset;  // offset to "EA List Length" field
	    }
	    else
	    {
	      if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "No Extended Attributes were returned.");
	      return results;  // empty hashtable
	    }      

	    byte[] eaVal = null;
	    int eaCount = get16bit(eaListOffset+6);  // number of EA structures returned
	    if (DEBUG) System.out.println("DEBUG Number of EA structures returned: " + eaCount);

	    // Advance the offset, to point to the start of first repeating EA struct.
	    int offset = eaListOffset+8;

	    for (int i=0; i<eaCount; i++)
	    {
	      int eaCcsid = get16bit(offset);      // The 2-byte CCSID for the EA name.
	      int eaNameLL= get16bit(offset+2);    // The 2-byte length of the EA name.
	      // Note: eaNameLL does *not* include length of the LL field itself.
	      //int eaFlags = get16bit(offset+4);  // The flags for the EA.
	      int eaValLL = get32bit(offset+6);    // The 4-byte length of the EA value.
	      // Note: eaValLL includes the 4 "mystery bytes" that precede the name.
	      byte[] eaName = new byte[eaNameLL];  // The EA name.
	      System.arraycopy(data_, offset+10, eaName, 0, eaNameLL);
	      if (eaValLL <= 4)
	      {
	        if (DEBUG) System.out.println("DEBUG Warning: eaValLL<=4: " + eaValLL);
	      }
	      else
	      {
	        eaVal = new byte[eaValLL-4];  // omit the 4 leading mystery bytes
	        System.arraycopy(data_, offset+10+eaNameLL+4, eaVal, 0, eaValLL-4);
	        try
	        {
	          String eaNameString = CharConverter.byteArrayToString(eaCcsid, eaName);
	          results.put(eaNameString, eaVal);
	          if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Extended Attribute returned: " + eaNameString, eaVal);
	        }
	        catch (java.io.UnsupportedEncodingException e) { Trace.log(Trace.ERROR, e); }
	      }
	      // Advance the offset, to point to the start of next EA struct.
	      offset += (10 + eaNameLL + eaValLL);
	    }

	    return results;
	  }
	
	public int hashCode()
	  {
	    return 0x8013;
	  }
}
