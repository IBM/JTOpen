package com.ibm.jtopenlite.samples;

import com.ibm.jtopenlite.ddm.*;
/**
 * Sample program to read a file using the DDM support of jtopenlite.
 * Note:  This program is not yet working.
 * Try running with arguments:   SYSTEM USERID PASSWORD QIWS QCUSTCDT *FIRST
 */
public class DDMRead {
public static void main(String[] args) {
	try {
		String system = args[0];
		String user = args[1];
		String password = args[2];
		String library= args[3];
		String file = args[4];
		String member = args[5];

		StringBuffer sb = new StringBuffer();
		DDMConnection connection = DDMConnection.getConnection(system, user, password);

		DDMRecordFormat recordFormat = connection.getRecordFormat(library, file);
		DDMFile ddmFile = connection.open(library, file, member, recordFormat.getName());
		for (int j = 0; j < 100; j++) {
		connection.read(ddmFile, null, j);
		byte[] recordDataBuffer = ddmFile.getRecordDataBuffer();
		sb.setLength(0);
		for (int i = 0; i < recordFormat.getFieldCount(); i++) {
			if (i > 0) sb.append(",");
			DDMField field = recordFormat.getField(i);
			field.getString(recordDataBuffer);
		}
		System.out.println("Record "+j+":"+sb.toString());
		}

	} catch (Exception e) {
		e.printStackTrace(System.out);
		System.out.println("Usage:  java com.ibm.jtopenlite.samples.DDMRead SYSTEM USERID PASSWORD LIBRARY FILE MEMBER)");
	}
}
}
