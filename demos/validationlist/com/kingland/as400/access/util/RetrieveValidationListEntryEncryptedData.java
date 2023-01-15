package com.kingland.as400.access.util;

import com.ibm.as400.access.*;
import com.kingland.as400.access.*;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * Example program to retrieve and display the encrypted information stored
 * in a validation list entry.
 * <p>
 * Note: Encrypted information cannot be retrieved if one-way encrypted on insertion into
 * the validation list.
 * <p>
 * Call syntax: RetrieveValidationListEntryEncryptedData <i>validationListPath ccsid entryID</i>
 * <br>
 * Example: RetrieveValidationListEntryEncryptedData /qsys.lib/qgpl.lib/test.vldl 37 bob
 * 
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class RetrieveValidationListEntryEncryptedData {
	static final String COPYRIGHT =
		"Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.";
/**
 * Default constructor.
 */
public RetrieveValidationListEntryEncryptedData() {
	super();
}

/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Check for correct number of arguments
	if (args.length < 3) {
		syntaxNotCorrect();
		return;
	}
	try {
		// Run the app
		new RetrieveValidationListEntryEncryptedData().findEntry(
			args[0],
			new Integer(args[1]).intValue(),
			args[2]);
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		System.exit(0);
	}
}
/**
 * Displays output indicating the expected syntax.
 */
private static void syntaxNotCorrect() {
	System.out.println("");
	System.out.println("Parameters are not correct.  Expected syntax is:");
	System.out.println("");
	System.out.println("  RetrieveValidationListEntryEncryptedData validationListPath ccsid entryID");
	System.out.println("");
	System.out.println("For example:");
	System.out.println("");
	System.out.println("  RetrieveValidationListEntryEncryptedData /qsys.lib/qgpl.lib/test.vldl 37 bob");
	System.out.println("");
	System.out.println("");
}

/**
 * Find & print the entry (id & encrypted data) to System.out
 */
public void findEntry(String vldlPath, int ccsid, String id) {
	AS400 as400 = new AS400();
	ValidationList vldl = new ValidationList(as400, new QSYSObjectPathName(vldlPath));

	ValidationListAttributeData attrData = new ValidationListAttributeData();
	attrData.setCcsid(-1);
	attrData.setBytes(new AS400Text(1, 65535, as400).toBytes("1"));

	ValidationListAttribute[] attrs = new ValidationListAttribute[1];
	ValidationListAttribute attr = new ValidationListAttribute(as400);
	attr.setIdentifier("QsyEncryptData");
	attrs[0] = attr;

	try {
		ValidationListEntry entry = vldl.findEntry(id, ccsid, attrs);
		System.out.println("Encrypted data==>"+entry.getEncryptedData().getString(as400));
	} catch (Exception e) {
		System.out.println("Encrypted data not retrieved.");
		e.printStackTrace();
	}
}
}