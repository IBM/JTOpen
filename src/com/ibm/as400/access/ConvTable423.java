///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable423.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// table for CCSID 423
class ConvTable423 extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private final static int ccsid = 423;
    private final static char[] table =
    {
/*00*/	'\u0000', '\u0001', '\u0002', '\u0003', '\u009c', '\u0009', '\u0086', '\u007f',
/*08*/	'\u0097', '\u008d', '\u008e', '\u000b', '\u000c', '\r',     '\u000e', '\u000f',
/*10*/	'\u0010', '\u0011', '\u0012', '\u0013', '\u009d', '\u0085', '\u0008', '\u0087',
/*18*/	'\u0018', '\u0019', '\u0092', '\u008f', '\u001c', '\u001d', '\u001e', '\u001f',
/*20*/	'\u0080', '\u0081', '\u0082', '\u0083', '\u0084', '\n',     '\u0017', '\u001b',
/*28*/	'\u0088', '\u0089', '\u008a', '\u008b', '\u008c', '\u0005', '\u0006', '\u0007',
/*30*/	'\u0090', '\u0091', '\u0016', '\u0093', '\u0094', '\u0095', '\u0096', '\u0004',
/*38*/	'\u0098', '\u0099', '\u009a', '\u009b', '\u0014', '\u0015', '\u009e', '\u001a',
/*40*/	'\u0020', '\u0391', '\u0392', '\u0393', '\u0394', '\u0395', '\u0396', '\u0397',
/*48*/	'\u0398', '\u0399', '\u005b', '\u002e', '\u003c', '\u0028', '\u002b', '\u0021',
/*50*/	'\u0026', '\u039a', '\u039b', '\u039c', '\u039d', '\u039e', '\u039f', '\u03a0',
/*58*/	'\u03a1', '\u03a3', '\u005d', '\u0024', '\u002a', '\u0029', '\u003b', '\u005e',
/*60*/	'\u002d', '\u002f', '\u03a4', '\u03a5', '\u03a6', '\u03a7', '\u03a8', '\u03a9',
/*68*/	'\u001a', '\u001a', '\u007c', '\u002c', '\u0025', '\u005f', '\u003e', '\u003f',
/*70*/	'\u001a', '\u0386', '\u0388', '\u0389', '\u00a0', '\u038a', '\u038c', '\u038e',
/*78*/	'\u038f', '\u0060', '\u003a', '\u00a3', '\u00a7', '\'',     '\u003d', '\u0022',
/*80*/	'\u00c4', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065', '\u0066', '\u0067',
/*88*/	'\u0068', '\u0069', '\u03b1', '\u03b2', '\u03b3', '\u03b4', '\u03b5', '\u03b6',
/*90*/	'\u00d6', '\u006a', '\u006b', '\u006c', '\u006d', '\u006e', '\u006f', '\u0070',
/*98*/	'\u0071', '\u0072', '\u03b7', '\u03b8', '\u03b9', '\u03ba', '\u03bb', '\u03bc',
/*A0*/	'\u00dc', '\u00a8', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077', '\u0078',
/*A8*/	'\u0079', '\u007a', '\u03bd', '\u03be', '\u03bf', '\u03c0', '\u03c1', '\u03c3',
/*B0*/	'\u001a', '\u03ac', '\u03ad', '\u03ae', '\u03ca', '\u03af', '\u03cc', '\u03cd',
/*B8*/	'\u03cb', '\u03ce', '\u03c2', '\u03c4', '\u03c5', '\u03c6', '\u03c7', '\u03c8',
/*C0*/	'\u00b8', '\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
/*C8*/	'\u0048', '\u0049', '\u00ad', '\u03c9', '\u00e2', '\u00e0', '\u00e4', '\u00ea',
/*D0*/	'\u00b4', '\u004a', '\u004b', '\u004c', '\u004d', '\u004e', '\u004f', '\u0050',
/*D8*/	'\u0051', '\u0052', '\u00b1', '\u00e9', '\u00e8', '\u00eb', '\u00ee', '\u00ef',
/*E0*/	'\u00b0', '\u001a', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057', '\u0058',
/*E8*/	'\u0059', '\u005a', '\u00bd', '\u00f6', '\u00f4', '\u00fb', '\u00f9', '\u00fc',
/*F0*/	'\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037',
/*F8*/	'\u0038', '\u0039', '\u00ff', '\u00e7', '\u00c7', '\u001a', '\u001a', '\u009f'
    };

    ConvTable423()
    {
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for CCSID: 423");
    }

    /**
     * Returns the ccsid of this conversion object.
     * @return the ccsid.
     **/
    int getCcsid()
    {
	return ccsid;
    }

    /**
     * Returns the encoding of this conversion object.
     * @return the encoding.
     **/
    String getEncoding()
    {
	return String.valueOf(ccsid);
    }

    String byteArrayToString(byte[] source, int offset, int length)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting byte to char for CCSID: 423", source, offset, length);
	}
	char[] dest = new char[length];

	for (int i=0; i<length; ++i)
	{
	    dest[i] = table[source[i+offset] & 0xFF];
	}
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Byte to char output: ", ConvTable.dumpCharArray(dest));
	}
	return new String(dest);
    }

    byte[] stringToByteArray(String source)
    {
	char[] src = source.toCharArray();
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting char to byte for CCSID: 423", ConvTable.dumpCharArray(src));
	}
	byte[] dest = new byte[src.length];

	for (int i=0; i<src.length; ++i)
	{
	    int ii = 0;
	    for (; ii <= 0xFF; ++ii)
	    {
		if (src[i] == table[ii])
		{
		    dest[i] = (byte)ii;
		    break;
		}
	    }
	    if (ii == 0x100) dest[i] = 0x6F;
	}
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Char to byte output: ", dest);
	}
	return dest;
    }
}
