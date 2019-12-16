package com.kingland.as400.access.util;

import com.ibm.as400.access.*;
import com.kingland.as400.access.*;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * Abstract superclass for validation list utilities & examples.
 * 
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
abstract class ValidationListUtil {
	static final String COPYRIGHT =
		"Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.";

	protected int ccsid_ = 37;
	protected ValidationList vldl_ = null;
	protected boolean oneWayEncrypt_ = true;
	
	protected AS400 as400_ = null;

/**
 * Return the AS/400 system object.
 * @return com.ibm.as400.access.AS400
 */
protected AS400 getAS400() {
	if (as400_ == null)
		as400_ = new AS400();
	return as400_;
}

/**
 * Returns a QsyEncryptData attribute based on the <code>oneWayEncrypt_</code> field.
 * <p>
 * When the attribute value is '0', the encrypted data (i.e. password) cannot be retrieved.
 * <p>
 * Note: The system value QRETSVRSEC (Retain server security data) still has the final say
 * in determining if the original data is allowed to be stored with the entry.
 *
 * @return
 *		com.kingland.as400.access.ValidationListAttribute
 */
protected ValidationListAttribute getEncryptAttribute() {
	ValidationListAttribute attr = null;

	attr = new ValidationListAttribute(getAS400());
	attr.setIdentifier("QsyEncryptData");
	ValidationListAttributeData attrData = new ValidationListAttributeData();
	attrData.setCcsid(-1);
	attrData.setBytes(new AS400Text(1, 65535, getAS400()
		).toBytes(oneWayEncrypt_ ? "0" : "1"));
	attr.setData(attrData);

	return attr;
}
}