///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable1132.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// Table for CCSID 1132
class ConvTable1132 extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }
    private final static int ccsid = 1132;
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
	'\u0020', '\u00a0', '\u0e81', '\u0e82', '\u0e84', '\u0e87', '\u0e88', '\u0eaa',
	'\u0e8a', '\u005b', '\u00a2', '\u002e', '\u003c', '\u0028', '\u002b', '\u007c',
	'\u0026', '\u001a', '\u0e8d', '\u0e94', '\u0e95', '\u0e96', '\u0e97', '\u0e99',
	'\u0e9a', '\u005d', '\u0021', '\u0024', '\u002a', '\u0029', '\u003b', '\u00ac',
	'\u002d', '\u002f', '\u0e9b', '\u0e9c', '\u0e9d', '\u0e9e', '\u0e9f', '\u0ea1',
	'\u0ea2', '\u005e', '\u00a6', '\u002c', '\u0025', '\u005f', '\u003e', '\u003f',
	'\u006b', '\u001a', '\u0ea3', '\u0ea5', '\u0ea7', '\u0eab', '\u0ead', '\u0eae',
	'\u001a', '\u0060', '\u003a', '\u0023', '\u0040', '\'',     '\u003d', '\u0022',
	'\u001a', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065', '\u0066', '\u0067',
	'\u0068', '\u0069', '\u001a', '\u001a', '\u0eaf', '\u0eb0', '\u0eb2', '\u0eb3',
	'\u001a', '\u006a', '\u006b', '\u006c', '\u006d', '\u006e', '\u006f', '\u0070',
	'\u0071', '\u0072', '\u0eb4', '\u0eb5', '\u0eb6', '\u0eb7', '\u0eb8', '\u0eb9',
	'\u001a', '\u007e', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077', '\u0078',
	'\u0079', '\u007a', '\u0ebc', '\u0eb1', '\u0ebb', '\u0ebd', '\u001a', '\u001a',
	'\u0ed0', '\u0ed1', '\u0ed2', '\u0ed3', '\u0ed4', '\u0ed5', '\u0ed6', '\u0ed7',
	'\u0ed8', '\u0ed9', '\u001a', '\u0ec0', '\u0ec1', '\u0ec2', '\u0ec3', '\u0ec4',
	'\u007b', '\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
	'\u0048', '\u0049', '\u001a', '\u0ec8', '\u0ec9', '\u0eca', '\u0ecb', '\u0ecc',
	'\u007d', '\u004a', '\u004b', '\u004c', '\u004d', '\u004e', '\u004f', '\u0050',
	'\u0051', '\u0052', '\u0ecd', '\u0ec6', '\u001a', '\u0edc', '\u0edd', '\u001a',
	'\\',     '\u001a', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057', '\u0058',
	'\u0059', '\u005a', '\u001a', '\u001a', '\u001a', '\u001a', '\u001a', '\u001a',
	'\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037',
	'\u0038', '\u0039', '\u001a', '\u001a', '\u001a', '\u001a', '\u001a', '\u009f'
    };

    ConvTable1132()
    {
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for CCSID: 1132");
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
	    Trace.log(Trace.CONVERSION, "Converting byte to char for CCSID: 1132", source, offset, length);
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
	    Trace.log(Trace.CONVERSION, "Converting char to byte for CCSID: 1132", ConvTable.dumpCharArray(src));
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
