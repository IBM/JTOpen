package com.ibm.jtopenlite.samples;

import com.ibm.jtopenlite.file.*;

/**
 * <p>Sample program that uses JTOpenLite to write and read an IFS file.
 * <p>Usage:   java com.ibm.jtopenlite.samples.AccessIfsFile SYSTEM USERID PASSWORD FILENAME
 *
 */
public class AccessIfsFile {
	public static void main(String args[]) {
		try {
			String system = args[0];
			String userid = args[1];
			String password = args[2];
			String filename = args[3];
			FileConnection connection = FileConnection.getConnection(system,
					userid, password);

			FileHandle fhWrite = FileHandle.createEmptyHandle();

			connection.openFile(filename, fhWrite);
			byte[] stuff = "Hello world".getBytes("UTF-8");
			connection.writeFile(fhWrite, stuff, 0, stuff.length, true);
			connection.closeFile(fhWrite);

			FileHandle fhRead = FileHandle.createEmptyHandle();
			connection.openFile(filename, fhRead);
			byte[] buffer = new byte[100];
			int bytesRead = connection.readFile(fhRead, buffer, 0,
					buffer.length);
			connection.closeFile(fhRead);

			String output = new String(buffer, 0, bytesRead, "UTF-8");

			System.out.println("Read '" + output + "' from the file "
					+ filename);

			connection.deleteFile(filename);

			System.out.println("File " + filename + " has been deleted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
