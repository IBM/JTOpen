package com.kingland.as400.access.util;

import com.ibm.as400.access.*;
import com.kingland.as400.access.*;
import java.util.*;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * Example program to compare the contents of two AS/400 validation lists.
 * Differences are printed to standard output.
 * <p>
 * Call syntax: CompareValidationListEntries <i>validationListPath1</i> <i>validationListPath2</i>
 * <br>
 * Example: CompareValidationListEntries /qsys.lib/qgpl.lib/test1.vldl /qsys.lib/qgpl.lib/test2.vldl
 * 
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class CompareValidationListEntries extends ValidationListUtil {
	static final String COPYRIGHT =
		"Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.";
/**
 * Default constructor.
 */
public CompareValidationListEntries() {
	super();
}
/**
 * Print a list of differences (ids & non-encrypted data) to System.out
 */
public void compare(String vldlPath1, String vldlPath2) {
	AS400 as400 = getAS400();
	ValidationList vl1 = new ValidationList(as400, new QSYSObjectPathName(vldlPath1));
	ValidationList vl2 = new ValidationList(as400, new QSYSObjectPathName(vldlPath2));

	ValidationListEntry vle1, vle2;
	ValidationListEntry[] entries1, entries2 = new ValidationListEntry[0];
	Hashtable vl2Hash = null;

	// Cumulative statistics
	int addCount = 0;
	int deleteCount = 0;
	int changeCount = 0;
	
	try {
		// Print report header
		System.out.println("*** Comparing source... "+vldlPath1);
		System.out.println("*** To target ......... "+vldlPath2);
		System.out.println();

		// Retrieve all entries for each list	
		entries1 = vl1.getEntries();
		entries2 = vl2.getEntries();

		System.out.println("    Source contains "+entries1.length+" entries...");
		System.out.println("    Target contains "+entries2.length+" entries...");
		System.out.println();

		// Create a hash from the target list, keyed on ID
		vl2Hash = new Hashtable();
		for (int i=0; i<entries2.length; i++)
			vl2Hash.put(entries2[i].getEntryID().getString(as400), entries2[i]);
		
		// Now run through the source list and make comparisons
		String id1, d1, d2;
		Vector vl1IDs = new Vector();
		for (int i=0; i<entries1.length; i++) {
			vle1 = entries1[i];
			try {
				id1= entries1[i].getEntryID().getString(as400);

				// Add to list of IDs for later check of deleted entries
				vl1IDs.add(id1);

				// Check if entry was added
				if (!vl2Hash.containsKey(id1))
				{
					addCount++;
					System.out.println("*** Entry "+id1+" exists in the source but not the target");
					System.out.println();
				}
				else
				{
					d1 = entries1[i].getUnencryptedData().getString(as400);
					d2 = ((ValidationListEntry) vl2Hash.get(id1)).getUnencryptedData().getString(as400);
					if (!d1.equals(d2))
					{
						changeCount++;
						System.out.println("*** Entry "+id1+" exists in the source and target but differs");
						System.out.println("  * Source data: "+d1);
						System.out.println("  * Target data: "+d2);
						System.out.println();
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		// Finally, list any that were in the target but not in the source
		Set deleted = vl2Hash.keySet();
		deleted.removeAll(vl1IDs);
		Iterator it = deleted.iterator();
		while (it.hasNext())
		{
			deleteCount++;
			System.out.println("*** Entry "+it.next()+" exists in the target but not the source");
			System.out.println();
		}
	} catch (Exception e) {
		e.printStackTrace();
	}

	System.out.println("*****************************");
	System.out.println("Number of changed entries..................................: "+changeCount);
	System.out.println("Number of entries in source but not in target..............: "+addCount);
	System.out.println("Number of entries in target but not in source..............: "+deleteCount);
	System.out.println("*****************************");
	System.out.println();
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Check for correct number of arguments
	if (args.length < 2) {
		syntaxNotCorrect();
		return;
	}
	try {
		// Run the app
		new CompareValidationListEntries().compare(args[0], args[1]);
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
	System.out.println("  CompareValidationListEntries validationListPath1 validationListPath2");
	System.out.println("");
	System.out.println("For example:");
	System.out.println("");
	System.out.println("  CompareValidationListEntries /qsys.lib/qgpl.lib/test1.vldl /qsys.lib/qgpl.lib/test2.vldl");
	System.out.println("");
	System.out.println("");
}
}