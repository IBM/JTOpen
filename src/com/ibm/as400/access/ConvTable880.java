///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable880.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// table for CCSID 880
class ConvTable880 extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private final static int ccsid = 880;
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
/*40*/	'\u0020', '\u00a0', '\u0452', '\u0453', '\u0451', '\u0454', '\u0455', '\u0456',
/*48*/	'\u0457', '\u0458', '\u005b', '\u002e', '\u003c', '\u0028', '\u002b', '\u0021',
/*50*/	'\u0026', '\u0459', '\u045a', '\u045b', '\u045c', '\u045e', '\u045f', '\u042a',
/*58*/	'\u2116', '\u0402', '\u005d', '\u0024', '\u002a', '\u0029', '\u003b', '\u005e',
/*60*/	'\u002d', '\u002f', '\u0403', '\u0401', '\u0404', '\u0405', '\u0406', '\u0407',
/*68*/	'\u0408', '\u0409', '\u007c', '\u002c', '\u0025', '\u005f', '\u003e', '\u003f',
/*70*/	'\u040a', '\u040b', '\u040c', '\u00ad', '\u040e', '\u040f', '\u044e', '\u0430',
/*78*/	'\u0431', '\u0060', '\u003a', '\u0023', '\u0040', '\'',     '\u003d', '\u0022',
/*80*/	'\u0446', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065', '\u0066', '\u0067',
/*88*/	'\u0068', '\u0069', '\u0434', '\u0435', '\u0444', '\u0433', '\u0445', '\u0438',
/*90*/	'\u0439', '\u006a', '\u006b', '\u006c', '\u006d', '\u006e', '\u006f', '\u0070',
/*98*/	'\u0071', '\u0072', '\u043a', '\u043b', '\u043c', '\u043d', '\u043e', '\u043f',
/*A0*/	'\u044f', '\u007e', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077', '\u0078',
/*A8*/	'\u0079', '\u007a', '\u0440', '\u0441', '\u0442', '\u0443', '\u0436', '\u0432',
/*B0*/	'\u044c', '\u044b', '\u0437', '\u0448', '\u044d', '\u0449', '\u0447', '\u044a',
/*B8*/	'\u042e', '\u0410', '\u0411', '\u0426', '\u0414', '\u0415', '\u0424', '\u0413',
/*C0*/	'\u007b', '\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
/*C8*/	'\u0048', '\u0049', '\u0425', '\u0418', '\u0419', '\u041a', '\u041b', '\u041c',
/*D0*/	'\u007d', '\u004a', '\u004b', '\u004c', '\u004d', '\u004e', '\u004f', '\u0050',
/*D8*/	'\u0051', '\u0052', '\u041d', '\u041e', '\u041f', '\u042f', '\u0420', '\u0421',
/*E0*/	'\\',     '\u00a4', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057', '\u0058',
/*E8*/	'\u0059', '\u005a', '\u0422', '\u0423', '\u0416', '\u0412', '\u042c', '\u042b',
/*F0*/	'\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037',
/*F8*/	'\u0038', '\u0039', '\u0417', '\u0428', '\u042d', '\u0429', '\u0427', '\u009f'
    };

    ConvTable880()
    {
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for CCSID: 880");
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
	    Trace.log(Trace.CONVERSION, "Converting byte to char for CCSID: 880", source, offset, length);
	}
	char[] dest = new char[length];

	for (int i=0; i<length; ++i)
	{
	    dest[i] = this.table[source[i+offset] & 0xFF];
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
	    Trace.log(Trace.CONVERSION, "Converting char to byte for CCSID: 880", ConvTable.dumpCharArray(src));
	}
	byte[] dest = new byte[src.length];

	for (int i=0; i<src.length; ++i)
	{
	    int ii = 0;
	    for (; ii <= 0xFF; ++ii)
	    {
		if (src[i] == this.table[ii])
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
