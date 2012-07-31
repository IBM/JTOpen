package com.ibm.jtopenlite.samples;

import java.io.IOException;

import com.ibm.jtopenlite.ddm.*;

/**
 * Sample program to read a file using the DDM support of jtopenlite. Note: This
 * program is not yet working. Try running with arguments: SYSTEM USERID
 * PASSWORD QIWS QCUSTCDT *FIRST
 */
public class DDMRead {
	public static void main(String[] args) {
		try {
			String system = args[0];
			String user = args[1];
			String password = args[2];
			String library = args[3];
			String file = args[4];
			String member = args[5];

			DDMConnection connection = DDMConnection.getConnection(system,
					user, password);
			final DDMRecordFormat recordFormat = connection.getRecordFormat(
					library, file);
			DDMFile ddmFile = connection.open(library, file, member,
					recordFormat.getName());

			// Read records until the end.
			DDMReadCallbackAdapter reader = new DDMReadCallbackAdapter() {

				@Override
				public void newRecord(int recordNumber, byte[] recordData,
						boolean[] nullFieldMap) throws IOException {
					StringBuffer sb = new StringBuffer();
					// Initialize before reading a new record
					sb.setLength(0);
					for (int i = 0; i < recordFormat.getFieldCount(); i++) {
						DDMField field = recordFormat.getField(i);
						if (i > 0)
							sb.append(",");
						sb.append(field.getString(recordData));
					}

					System.out.println("Record " + recordNumber + ":" + sb);
				}

			};

			while (!reader.isDone()) {
				connection.readNext(ddmFile, reader);
			}
			
			// Close file.
			connection.close(ddmFile);

		} catch (Exception e) {
			e.printStackTrace(System.out);
			System.out
					.println("Usage:  java com.ibm.jtopenlite.samples.DDMRead SYSTEM USERID PASSWORD LIBRARY FILE MEMBER)");
		} 
	}
}
