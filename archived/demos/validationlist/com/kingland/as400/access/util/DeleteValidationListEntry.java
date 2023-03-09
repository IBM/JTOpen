package com.kingland.as400.access.util;

import com.ibm.as400.access.*;
import com.kingland.as400.access.*;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * Example program to delete a specific entry from a validation list.
 * <p>
 * Call syntax: DeleteValidationListEntry <i>validationListPath ccsid entryID</i>
 * <br>
 * Example: DeleteValidationListEntry /qsys.lib/qgpl.lib/test.vldl 37 bob
 * 
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class DeleteValidationListEntry extends ValidationListUtil {
	static final String COPYRIGHT =
		"Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.";
/**
 * Default constructor.
 */
private DeleteValidationListEntry() {
	super();
}
/**
 * DeleteValidationListEntry constructor.
 */
public DeleteValidationListEntry(String vldlPath, int ccsid) {
	this();
	ccsid_ = ccsid;
	vldl_ = new ValidationList(
		getAS400(), new QSYSObjectPathName(vldlPath));
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
		new DeleteValidationListEntry( args[0], new Integer(args[1]).intValue()
			).dltEntry(args[2]);
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
	System.out.println("DeleteValidationListEntry validationListPath ccsid entryID");
	System.out.println("");
	System.out.println("For example:");
	System.out.println("");
	System.out.println("  DeleteValidationListEntry /qsys.lib/qgpl.lib/test.vldl 37 bob");
	System.out.println("");
	System.out.println("");
}

/**
 * Services the request.
 *
 * @param id
 *		The ID of the entry to delete.
 */
protected void dltEntry(String id) {
	try {
		ValidationListEntry entry = new ValidationListEntry();
		entry.setEntryID(
			new ValidationListTranslatedData(id, ccsid_, getAS400()));
		vldl_.removeEntry(entry);
	}
	catch (PersistenceException pe) {
		pe.printStackTrace();
	}
}
}