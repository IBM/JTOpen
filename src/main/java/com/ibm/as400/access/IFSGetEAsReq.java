package com.ibm.as400.access;

public class IFSGetEAsReq extends IFSDataStreamReq {
	private static final int TEMPLATE_LENGTH = 6;
	private static final int FILE_HANDLE_OFFSET = 22;
	private static final int FILE_OPEN_FLAGS_OFFSET = 26;
	private static final int EAS_OFFSET = 8;
	
	IFSGetEAsReq (int handle, byte[][] eaNames, int eaNameBytesLength, int fileNameCCSID, int serverDataStreamLevel) {
		super(HEADER_LENGTH + TEMPLATE_LENGTH + (serverDataStreamLevel < 2? 4:8) + 8 + 10*eaNames.length + eaNameBytesLength);
		setLength(data_.length);
		setTemplateLen(TEMPLATE_LENGTH + (serverDataStreamLevel < 2? 4:8));
		setReqRepID(0x0027);
		set32bit(handle,FILE_HANDLE_OFFSET);
		set64bit(0x0000000000040001L, FILE_OPEN_FLAGS_OFFSET);
		int offset = HEADER_LENGTH + TEMPLATE_LENGTH + (serverDataStreamLevel < 2? 4:8);
		
		if (eaNames != null)
    {
      // Set EA List Length:
      //     8 bytes for single fixed header for entire list
      //  + 10 bytes for each repeating header (for each EA structure)
      //  + total number of bytes for all EA names in list
      int eaNameListLength = 8 + 10*eaNames.length + eaNameBytesLength;
      set32bit(eaNameListLength, offset+0);        // EA name list length
      set16bit(0x0008, offset+4);                  // EA name list code point
      set16bit(eaNames.length & 0x00FF, offset+6); // EA count
      // Advance the offset, to point to the start of first repeating EA struct.
      offset += 8;
      for (int i=0; i<eaNames.length; i++)
      {
        set16bit(fileNameCCSID, offset);       // ccsid for EA name
        set16bit(eaNames[i].length, offset+2); // length of EA name
        set16bit(0x0000, offset+4);            // flags for the EA (0)
        set32bit(0x0000, offset+6);            // length of the EA value (0)
        System.arraycopy(eaNames[i], 0, data_, offset + 10,
                         eaNames[i].length);         // @D2A
        offset += (10 + eaNames[i].length);
        // Advance the offset, to point to the start of next EA struct.
      }
    }
	}
}
