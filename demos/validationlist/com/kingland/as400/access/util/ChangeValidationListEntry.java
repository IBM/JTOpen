package com.kingland.as400.access.util;

import com.ibm.as400.access.*;
import com.kingland.as400.access.*;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * Example program to change the contents of an AS/400 validation list entry.
 * <p>
 * Call syntax: ChangeValidationListEntry <i>validationListPath ccsid</i>
 * <i>oneWayEncrypted[*YES|*NO] entryID encryptedData unencryptedData</i>
 * <br>
 * Example: ChangeValidationListEntry /qsys.lib/qgpl.lib/test.vldl 37 *yes bob password data
 * 
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class ChangeValidationListEntry extends ValidationListUtil {
	static final String COPYRIGHT =
		"Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.";
/**
 * Default constructor.
 */
private ChangeValidationListEntry() {
	super();
}
/**
 * ChangeValidationListEntry constructor.
 */
public ChangeValidationListEntry(String vldlPath, int ccsid, boolean oneWayEncrypt) {
	this();
	ccsid_ = ccsid;
	oneWayEncrypt_ = oneWayEncrypt;
	vldl_ = new ValidationList(
		getAS400(), new QSYSObjectPathName(vldlPath));
}

/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Check for correct number of arguments
	if (args.length < 6) {
		syntaxNotCorrect();
		return;
	}
	try {
		// Run the app
		new ChangeValidationListEntry( args[0], new Integer(args[1]).intValue(), !args[2].equalsIgnoreCase("*NO")
			).chgwebusr(
				args[3],
				args[4],
				args[5]);
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
	System.out.println("ChangeValidationListUser validationListPath oneWayEncrypted[*YES|*NO] ccsid "+
		"entryID encryptedData unencryptedData");
	System.out.println("");
	System.out.println("For example:");
	System.out.println("");
	System.out.println("  ChangeValidationListUser /qsys.lib/qgpl.lib/test.vldl 37 *yes bob password newData");
	System.out.println("");
	System.out.println("");
}

/**
 * Services the request.
 *
 * @param id
 *		The entry ID to change.
 * @param pwd
 *		The new encrypted data.
 * @param data
 *		The new unencrypted data.
 */
protected void chgwebusr(String id, String pwd, String data) {
	ValidationListEntry vle = new ValidationListEntry();

	try {
		// Assign the entry ID
		vle.setEntryID(
			new ValidationListTranslatedData(id, ccsid_, getAS400()));
		// Assign the encrypted data (i.e. password)
		vle.setDataToEncrypt(
			new ValidationListDataToEncrypt(pwd, ccsid_, getAS400()));
		// Assign the attribute indicating one or two-way encryption
		vle.setAttributeInfo(
			new ValidationListAttributeInfo(
				new ValidationListAttribute[]
					{ getEncryptAttribute() }));
		// Assign the unencrypted data
		vle.setUnencryptedData(
			new ValidationListTranslatedData(data, ccsid_, getAS400()));
		// Perform the change
		vldl_.changeEntry(vle);
	}
	catch (PersistenceException pe) {
		pe.printStackTrace();
	}
}
}