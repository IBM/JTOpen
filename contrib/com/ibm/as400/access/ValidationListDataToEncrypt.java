package com.ibm.as400.access;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * The ValidationListDataToEncrypt class is used to specify the data to
 * encrypt when adding or changing a validation list entry.
 *
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class ValidationListDataToEncrypt extends ValidationListTranslatedData {
	static final String COPYRIGHT =
		"Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.";
/**
 * Constructs a ValidationListTranslatedData.
 */
public ValidationListDataToEncrypt() {
	super();
}
/**
 * Constructs a ValidationListTranslatedData from a structure stored as AS/400 bytes.
 * <p>
 * The <i>offset</i> indicates the starting position of the structure in the
 * given <i>buffer</i>.
 *
 * @param buffer byte[]
 * @param offset int
 */
public ValidationListDataToEncrypt(byte[] buffer, int offset) {
	super(buffer, offset);
}
/**
 * Constructs a ValidationListTranslatedData from the specified AS/400 <i>bytes</i>
 * which are encoded in the given <i>ccsid</i>.
 *
 * @param ccsid int
 * @param bytes byte[]
 */
public ValidationListDataToEncrypt(int ccsid, byte[] bytes) {
	super(ccsid, bytes);
}
/**
 * Constructs a ValidationListTranslatedData from the given string.
 * <p>
 * The translated bytes are derived by converting the string to AS/400 bytes
 * using the given <i>ccsid</i>. The <i>as400</i> is required to perform the
 * conversion from text to bytes. A ccsid of 0 indicates to use the ccsid
 * of the current AS/400 user.
 *
 * @param s java.lang.String
 * @param ccsid int
 * @param as400 com.ibm.as400.access.AS400
 */
public ValidationListDataToEncrypt(String s, int ccsid, AS400 as400) {
	super(s, ccsid, as400);
}
/**
 * Returns the length to be specified in the written AS/400 byte structure
 * if the assigned data is null.
 * <p>
 * Typically this value is 0. However, there are some cases where
 * other values must be returned to maintain proper behavior. For
 * example, when changing an entry a structure must be specified for the
 * data to encrypt, even if the encrypted data should not be changed.
 * However, if the data length in the structure is set to 0 instead of -1,
 * the existing encrypted data is wiped out. This is undesirable since
 * we don't always want the encrypted data changed. We might want to
 * modify the unencrypted data (i.e. user statistics) while leaving
 * the encrypted data (i.e. user password) unchanged.
 *
 * @return int
 */
protected int getWriteNullDataLength() {
	return -1;
}
}