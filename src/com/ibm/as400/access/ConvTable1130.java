///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable1130.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// Table for CCSID 1130
class ConvTable1130 extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private final static int ccsid = 1130;
    private final static char[] table =
    {
	'\u0000', '\u0001', '\u0002', '\u0003', '\u009c', '\u0009', '\u0086', '\u007f',
	'\u0097', '\u008d', '\u008e', '\u000b', '\u000c', '\r',     '\u000e', '\u000f',
	'\u0010', '\u0011', '\u0012', '\u0013', '\u009d', '\u0085', '\u0008', '\u0087',
	'\u0018', '\u0019', '\u0092', '\u008f', '\u001c', '\u001d', '\u001e', '\u001f',
	'\u0080', '\u0081', '\u0082', '\u0083', '\u0084', '\n',     '\u0017', '\u001b',
	'\u0088', '\u0089', '\u008a', '\u008b', '\u008c', '\u0005', '\u0006', '\u0007',
	'\u0090', '\u0091', '\u0016', '\u0093', '\u0094', '\u0095', '\u0096', '\u0004',
	'\u0098', '\u0099', '\u009a', '\u009b', '\u0014', '\u0015', '\u009e', '\u001a',
	'\u0020', '\u00a0', '\u00e2', '\u00e4', '\u00e0', '\u00e1', '\u0103', '\u00e5',
	'\u00e7', '\u00f1', '\u005b', '\u002e', '\u003c', '\u0028', '\u002b', '\u0021',
	'\u0026', '\u00e9', '\u00ea', '\u00eb', '\u00e8', '\u00ed', '\u00ee', '\u00ef',
	'\u0303', '\u00df', '\u005d', '\u0024', '\u002a', '\u0029', '\u003b', '\u005e',
	'\u002d', '\u002f', '\u00c2', '\u00c4', '\u00c0', '\u00c1', '\u0102', '\u00c5',
	'\u00c7', '\u00d1', '\u00a6', '\u002c', '\u0025', '\u005f', '\u003e', '\u003f',
	'\u00f8', '\u00c9', '\u00ca', '\u00cb', '\u00c8', '\u00cd', '\u00ce', '\u00cf',
	'\u20ab', '\u0060', '\u003a', '\u0023', '\u0040', '\'',     '\u003d', '\u0022',
	'\u00d8', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065', '\u0066', '\u0067',
	'\u0068', '\u0069', '\u00ab', '\u00bb', '\u0111', '\u0309', '\u0300', '\u00b1',
	'\u00b0', '\u006a', '\u006b', '\u006c', '\u006d', '\u006e', '\u006f', '\u0070',
	'\u0071', '\u0072', '\u00aa', '\u00ba', '\u00e6', '\u0152', '\u00c6', '\u00a4',
	'\u00b5', '\u007e', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077', '\u0078',
	'\u0079', '\u007a', '\u00a1', '\u00bf', '\u0110', '\u0323', '\u0301', '\u00ae',
	'\u00a2', '\u00a3', '\u00a5', '\u00b7', '\u00a9', '\u00a7', '\u00b6', '\u00bc',
	'\u00bd', '\u00be', '\u00ac', '\u007c', '\u00af', '\u0153', '\u0178', '\u00d7',
	'\u007b', '\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
	'\u0048', '\u0049', '\u00ad', '\u00f4', '\u00f6', '\u01b0', '\u00f3', '\u01a1',
	'\u007d', '\u004a', '\u004b', '\u004c', '\u004d', '\u004e', '\u004f', '\u0050',
	'\u0051', '\u0052', '\u00b9', '\u00fb', '\u00fc', '\u00f9', '\u00fa', '\u00ff',
	'\\',     '\u00f7', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057', '\u0058',
	'\u0059', '\u005a', '\u00b2', '\u00d4', '\u00d6', '\u01af', '\u00d3', '\u01a0',
	'\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037',
	'\u0038', '\u0039', '\u00b3', '\u00db', '\u00dc', '\u00d9', '\u00da', '\u009f'
    };

    ConvTable1130()
    {
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for CCSID: 1130");
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
	    Trace.log(Trace.CONVERSION, "Converting byte to char for CCSID: 1130", source, offset, length);
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
	    Trace.log(Trace.CONVERSION, "Converting char to byte for CCSID: 1130", ConvTable.dumpCharArray(src));
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
