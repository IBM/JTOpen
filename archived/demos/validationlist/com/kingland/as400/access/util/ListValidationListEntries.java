package com.kingland.as400.access.util;

import com.ibm.as400.access.*;
import com.kingland.as400.access.*;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * Example program to list the entries in an AS/400 validation list.
 * <p>
 * Call syntax: ListValidationListEntries <i>validationListPath</i>
 * <br>
 * Example: ListValidationListEntries /qsys.lib/qgpl.lib/test.vldl
 * 
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class ListValidationListEntries extends ValidationListUtil {
	static final String COPYRIGHT =
		"Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.";
/**
 * Default constructor.
 */
public ListValidationListEntries() {
	super();
}
/**
 * Print a list of entries (ids & non-encrypted data) to System.out
 */
public void displayEntries(String vldlPath) {
	AS400 as400 = getAS400();
	ValidationList vl = new ValidationList(as400, new QSYSObjectPathName(vldlPath));
	ValidationListEntry vle;
	ValidationListEntry[] entries = new ValidationListEntry[0];

	try {
		// Print report header
		System.out.println("*** Listing source... "+vldlPath);
		// Retrieve all entries	
		entries = vl.getEntries();
		System.out.println("    Source contains "+entries.length+" entries...");
		System.out.println();

		// Now run through the list and display
		String id;
		for (int i=0; i<entries.length; i++) {
			vle = entries[i];
			try {
				id = entries[i].getEntryID().getString(as400);
				System.out.print("ID==>"+id+"	");
				System.out.print("Unencrypted data==>"+entries[i].getUnencryptedData().getString(as400));
				System.out.println();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Check for correct number of arguments
	if (args.length < 1) {
		syntaxNotCorrect();
		return;
	}
	try {
		// Run the app
		new ListValidationListEntries().displayEntries(args[0]);
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
	System.out.println("  ListValidationListEntries validationListPath");
	System.out.println("");
	System.out.println("For example:");
	System.out.println("");
	System.out.println("  ListValidationListEntries /qsys.lib/qgpl.lib/test.vldl");
	System.out.println("");
	System.out.println("");
}
}