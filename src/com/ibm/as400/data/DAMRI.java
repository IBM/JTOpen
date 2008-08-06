///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DAMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.util.ListResourceBundle;

public class DAMRI extends ListResourceBundle
{
    // NLS_MESSAGEFORMAT_ALL
    // Each string is assumed to be processed by the MessageFormat class.
    // This means that a single quote must be coded as 2 consecutive single quotes ''. 

    // NLS_ENCODING=UTF-8
    // Instructs the translation tools to interpret the text as UTF-8.

    // Define constants so compiler can catch invalid keys
    public final static String    PCML_EXCEPTION_TITLE      = "PCML_EXCEPTION_TITLE";
    public final static String    MISSING_KEY               = "MISSING_KEY";
    public final static String    INPUT_VALUE_NOT_SET       = "INPUT_VALUE_NOT_SET";
    public final static String    NULL_VALUE                = "NULL_VALUE";
    public final static String    STRING_OR_NUMBER          = "STRING_OR_NUMBER";
    public final static String    BYTE_ARRAY                = "BYTE_ARRAY";
    public final static String    UNSUPPORTED_CCSID         = "UNSUPPORTED_CCSID";
    public final static String    STRING_TO_BYTES           = "STRING_TO_BYTES";
    public final static String    ELEMENT_NOT_FOUND         = "ELEMENT_NOT_FOUND";
    public final static String    WRONG_ELEMENT_TYPE        = "WRONG_ELEMENT_TYPE";
    public final static String    TOO_FEW_INDICES           = "TOO_FEW_INDICES";
    public final static String    INDEX_OUT_OF_BOUNDS       = "INDEX_OUT_OF_BOUNDS";
    public final static String    FAILED_TO_PARSE           = "FAILED_TO_PARSE";
    public final static String    ONE_PARSE_ERROR           = "ONE_PARSE_ERROR";
    public final static String    MANY_PARSE_ERRORS         = "MANY_PARSE_ERRORS";
    public final static String    FAILED_TO_VALIDATE        = "FAILED_TO_VALIDATE";
    public final static String    SERIALIZED_PCML_NOT_FOUND = "SERIALIZED_PCML_NOT_FOUND";
    public final static String    PCML_NOT_FOUND            = "PCML_NOT_FOUND";
    public final static String    PCML_DTD_NOT_FOUND        = "PCML_DTD_NOT_FOUND";
    public final static String    PCML_SERIALIZED           = "PCML_SERIALIZED";
    public final static String    EXCEPTION_RECEIVED        = "EXCEPTION_RECEIVED";
    public final static String    OFFSETFROM_NOT_FOUND      = "OFFSETFROM_NOT_FOUND";
    public final static String    CIRCULAR_REFERENCE        = "CIRCULAR_REFERENCE";
    public final static String    REF_NOT_FOUND             = "REF_NOT_FOUND";
    public final static String    REF_WRONG_TYPE            = "REF_WRONG_TYPE";
    public final static String    MULTIPLE_DEFINE           = "MULTIPLE_DEFINE";
    public final static String    BAD_ATTRIBUTE_SYNTAX      = "BAD_ATTRIBUTE_SYNTAX";
    public final static String    BAD_ATTRIBUTE_VALUE       = "BAD_ATTRIBUTE_VALUE";
    public final static String    ATTRIBUTE_NOT_ALLOWED     = "ATTRIBUTE_NOT_ALLOWED";
    public final static String    INITIAL_VALUE_ERROR       = "INITIAL_VALUE_ERROR";
    public final static String    PARSEORDER_NOT_FOUND      = "PARSEORDER_NOT_FOUND";
    public final static String    PARSEORDER_NOT_CHILD      = "PARSEORDER_NOT_CHILD";
    public final static String    BAD_TAG                   = "BAD_TAG";
    public final static String    ATTR_REF_NOT_FOUND        = "ATTR_REF_NOT_FOUND";
    public final static String    ATTR_REF_WRONG_NODETYPE   = "ATTR_REF_WRONG_NODETYPE";
    public final static String    ATTR_REF_WRONG_DATATYPE   = "ATTR_REF_WRONG_DATATYPE";
    public final static String    BAD_DATA_LENGTH           = "BAD_DATA_LENGTH";
    public final static String    BAD_OFFSET_VALUE          = "BAD_OFFSET_VALUE";
    public final static String    BAD_TOTAL_OFFSET          = "BAD_TOTAL_OFFSET";
    public final static String    NOT_ENOUGH_DATA           = "NOT_ENOUGH_DATA";
    public final static String    READ_DATA                 = "READ_DATA";
    public final static String    READ_DATA_W_INDICES       = "READ_DATA_W_INDICES";
    public final static String    WRITE_DATA                = "WRITE_DATA";
    public final static String    WRITE_DATA_W_INDICES      = "WRITE_DATA_W_INDICES";
	public final static String    PCD_ARGUMENTS             = "PCD_ARGUMENTS";
	
	// The following are new in Toolbox mod3
	
	public final static String    BAD_PCML_VERSION          = "BAD_PCML_VERSION";    // @B1A
	public final static String    NOT_CHILD_OF_PGM          = "NOT_CHILD_OF_PGM";    // @B1A
	public final static String    NOT_SRVPGM                = "NOT_SRVPGM";          // @B1A
	public final static String    NO_ENTRYPOINT             = "NO_ENTRYPOINT";       // @B1A
	public final static String    TOO_MANY_PARMS            = "TOO_MANY_PARMS";      // @B1A
	public final static String    NOT_SERVICE_PGM           = "NOT_SERVICE_PGM";     // @B1A

	// The following are new in Toolbox mod4
	
	public final static String    DOCUMENT_ALREADY_SET      = "DOCUMENT_ALREADY_SET"; // @C1A
	public final static String    DOCUMENT_NOT_SET          = "DOCUMENT_NOT_SET"; // @C2A
	public final static String    DATATYPE_NOT_SUPPORTED    = "DATATYPE_NOT_SUPPORTED"; // @C2A
	public final static String    MULTI_ARRAY_NOT_SUPPORTED = "MULTI_ARRAY_NOT_SUPPORTED"; // @C2A
	public final static String    NO_STRUCT                 = "NO_STRUCT"; // @C2A
	public final static String    NO_LENGTH                 = "NO_LENGTH"; // @C2A
	public final static String    INSUFFICIENT_INPUT_DATA   = "INSUFFICIENT_INPUT_DATA"; // @C2A
	public final static String    EXCESS_INPUT_DATA         = "EXCESS_INPUT_DATA"; // @C2A
	public final static String    RECORD_NOT_INITIALIZED    = "RECORD_NOT_INITIALIZED"; // @C2A
	public final static String    RECORDFORMAT_NOT_INITIALIZED = "RECORDFORMAT_NOT_INITIALIZED"; // @C2A
	public final static String    SERIALIZED_XML_NOT_FOUND  = "SERIALIZED_XML_NOT_FOUND"; // @C2A
	public final static String    XML_NOT_FOUND             = "XML_NOT_FOUND"; // @C2A
	public final static String    DTD_NOT_FOUND             = "DTD_NOT_FOUND"; // @C2A
	public final static String    XML_SERIALIZED            = "XML_SERIALIZED"; // @C2A
	public final static String    STRUCT_VALUE              = "STRUCT_VALUE"; // @C2A
	public final static String    DUPLICATE_FIELD_NAME      = "DUPLICATE_FIELD_NAME"; // @C3A

    // Internal errors
    public final static String    BAD_DATA_TYPE             = "BAD_DATA_TYPE";
    public final static String    BAD_NODE_TYPE             = "BAD_NODE_TYPE";
    public final static String    CLASS_NOT_FOUND           = "CLASS_NOT_FOUND";
    public final static String    ERROR_ACCESSING_VALUE     = "ERROR_ACCESSING_VALUE";
    
    
    
	public Object[][] getContents()
	{
		return contents;
	}
	
	static final Object[][] contents = {
	// BEGIN TRANSLATION
	
	// Messages for PcmlException class
    { PCML_EXCEPTION_TITLE,        "Error" },
    { MISSING_KEY,                 "Text not available for error message key ''{0}''" },
    { INPUT_VALUE_NOT_SET,         "Value is not set. Processing <data> element ''{0}''." },
    { NULL_VALUE,                  "Cannot set a value to ''null''. Processing <data> element ''{0}''." },  //@D1C
    { STRING_OR_NUMBER,            "Invalid data type ''{0}''. String or Number expected. Processing <data> element ''{1}''." },
    { BYTE_ARRAY,                  "Invalid data type ''{0}''. byte[] expected. Processing <data> element ''{1}''." },
    { UNSUPPORTED_CCSID,           "Unsupported CCSID {0} for system {1}. Processing <data> element ''{2}''." },          //@550
    { STRING_TO_BYTES,             "Error converting String to data using CCSID {0}. Processing <data> element ''{1}''." },
    { ELEMENT_NOT_FOUND,           "{1} element named ''{0}'' not found in document." },
    { WRONG_ELEMENT_TYPE,          "Element named ''{0}'' in document is not a {1} element." },
    { TOO_FEW_INDICES,             "The number of indices required is {1}. The number of indices specified is {0}. Processing <data> element ''{2}''." },
    { INDEX_OUT_OF_BOUNDS,         "An index specified is out of bounds (0 - {0}). The index in error is index number {1} of the indices specified, {2}. Processing <data> element ''{3}''." },
    { FAILED_TO_PARSE,             "File ''{0}'' failed to parse." },
    { ONE_PARSE_ERROR,             "1 error detected parsing pcml document." },
    { MANY_PARSE_ERRORS,           "{0} errors detected parsing pcml document." },
    { FAILED_TO_VALIDATE,          "File ''{0}'' contains PCML specification errors." },
    { SERIALIZED_PCML_NOT_FOUND,   "Serialized PCML document ''{0}'' cannot be found." },
    { PCML_NOT_FOUND,              "PCML document source ''{0}'' cannot be found." },
    { PCML_DTD_NOT_FOUND,          "PCML document type definition (DTD) ''{0}'' cannot be found" },
    { PCML_SERIALIZED,             "PCML document ''{0}'' saved." },
    { EXCEPTION_RECEIVED,          "Exception received: ''{0}''." },      // @C4C
    { OFFSETFROM_NOT_FOUND,        "Element ''{0}'' for offsetfrom= attribute is not a parent of this element. Processing <data> element ''{1}''." },
    { CIRCULAR_REFERENCE,          "Structure referenced, ''{0}'', is a circular reference. Processing {1} element ''{2}''." },
    { REF_NOT_FOUND,               "{1} element named ''{0}'' not found in document. Processing {2} element ''{3}''." },
    { REF_WRONG_TYPE,              "Element named ''{0}'' in document is not a {1} element. Processing {2} element ''{3}''." },
    { MULTIPLE_DEFINE,             "More than one element named ''{0}'' in document." },
    { BAD_ATTRIBUTE_SYNTAX,        "Syntax of attribute {0} is not correct. Processing {1} element ''{2}''." },
    { BAD_ATTRIBUTE_VALUE,         "Value of attribute {0} is not correct. Processing {1} element ''{2}''." },
    { ATTRIBUTE_NOT_ALLOWED,       "Attribute {0} is not allowed when {1} is specified. Processing {2} element ''{3}''." },
    { INITIAL_VALUE_ERROR,         "Initial value {0} is not correct for the data type specified. Processing {1} element ''{2}''." },
    { PARSEORDER_NOT_FOUND,        "{0} specified but ''{1}'' cannot be found in document. Processing {2} element ''{3}''." },
    { PARSEORDER_NOT_CHILD,        "{0} specified but ''{1}'' is not a child of this element. Processing {2} element ''{3}''." },
    { BAD_TAG,                     "''{0}'' is an unrecognized tag name. Processing element ''{1}''." },
    { ATTR_REF_NOT_FOUND,          "Element specified by ''{0}'' not found in document. Processing element ''{1}''." },
    { ATTR_REF_WRONG_NODETYPE,     "Element specified by ''{0}'' found in document as ''{1}'' but is not a {2} element. Processing element ''{3}''." },
    { ATTR_REF_WRONG_DATATYPE,     "Element specified by ''{0}'' found in document as ''{1}'' but is not a defined as {2}. Processing element ''{3}''." },
    { BAD_DATA_LENGTH,             "Data length, {0}, is either negative or exceeds maximum supported length of {1}. Processing {2} element \"{3}\"." },
    { BAD_OFFSET_VALUE,            "Offset to data, {0}, is either negative or exceeds number of bytes available, {1}. Processing {2} element \"{3}\"." },
    { BAD_TOTAL_OFFSET,            "Offset to data, {0}, is either negative or exceeds number of bytes available, {1}. Offest is calculated as {2} bytes from document element {3}. Processing {4} element \"{5}\"." },
    { NOT_ENOUGH_DATA,             "Not enough output data available for this document element. Processing {0} element \"{1}\"." },
	{ PCD_ARGUMENTS,               "Arguments are: [-serialize] <resource name>" },

	// The following are new in v4r5m0

	{ BAD_PCML_VERSION,            "Attribute, {0}, only allowed with pcml version=\"{1}\" or later.  Processing {2} element \"{3}\"." },                                   // @B1A
	{ NOT_CHILD_OF_PGM,            "Attribute, {0}, only allowed when this element is a child of a <program> element.  Processing {1} element \"{2}\"." },                  // @B1A
	{ NOT_SRVPGM,                  "Attribute, {0}, only allowed when path= attribute specifies a service program object.  Processing {1} element \"{2}\"." },              // @B1A
	{ NO_ENTRYPOINT,               "The entrypoint= attribute is required when path= attribute specifies a service program object.  Processing {1} element \"{2}\"." },     // @B1A
	{ TOO_MANY_PARMS,              "Attribute, {0}, only allowed for programs with {1} or fewer parameters.  Processing {2} element \"{3}\"." },                            // @B1A
	{ NOT_SERVICE_PGM,             "Operation allowed only for {0} elements with {1} specified.  Processing {2} element \"{3}\"." },                                        // @B1A

	// The following are new in Toolbox mod4

	{ DOCUMENT_ALREADY_SET,        "Document is already set and cannot be set more than once." },  // @C1A
	{ DOCUMENT_NOT_SET,            "Document has not been set." }, // @C2A
	{ DATATYPE_NOT_SUPPORTED,      "Data type {0} is not supported by RFML." }, // @C2A
	{ MULTI_ARRAY_NOT_SUPPORTED,   "Multidimensional AS400Array is not supported by RFML." }, // @C2A
	{ NO_STRUCT,                   "The struct= attribute is required when type=''struct''.  Processing {1} element \"{2}\"." }, // @C2A @D1C
	{ NO_LENGTH,                   "The length= attribute is required when the type= attribute has a value other than ''struct''.  Processing {1} element \"{2}\"." }, // @C2A @D1C
	{ INSUFFICIENT_INPUT_DATA,     "Insufficient input data available for this document element. Bytes required: {0}\tBytes provided: {1}\tProcessing {2} element \"{3}\"." }, // @C2A
	{ EXCESS_INPUT_DATA,           "Excess input data was provided for this document element. Bytes required: {0}\tBytes provided: {1}\tProcessing {2} element \"{3}\"." }, // @C2A
	{ RECORD_NOT_INITIALIZED,      "The Record object is not initialized." }, // @C2A
	{ RECORDFORMAT_NOT_INITIALIZED, "The RecordFormat object is not initialized." }, // @C2A

	{ SERIALIZED_XML_NOT_FOUND,    "Serialized {0} document ''{1}'' cannot be found." }, // @C2A
	{ XML_NOT_FOUND,               "{0} document source ''{1}'' cannot be found." }, // @C2A
	{ DTD_NOT_FOUND,               "{0} document type definition (DTD) ''{1}'' cannot be found" }, // @C2A
	{ XML_SERIALIZED,              "{0} document ''{1}'' saved." }, // @C2A
	{ STRUCT_VALUE,                "Cannot set or get the value of a <data> with type=''struct''. Processing <data> element ''{0}''." }, // @C2A
	{ DUPLICATE_FIELD_NAME,        "RecordFormat ''{0}'' has duplicate field name ''{1}''." }, // @C3A

	// NOTE TO TRANSLATORS: The following four messages are trace messages used for debugging
    { READ_DATA,                   "Reading data -- Offset: {0}\tLength: {1}\tName: \"{2}\"\tByte data: {3}"},
    { READ_DATA_W_INDICES,         "Reading data -- Offset: {0}\tLength: {1}\tName: \"{2}\" Indices: {3}\tByte data: {4}"},
    { WRITE_DATA,                  "Writing data -- Offset: {0}\tLength: {1}\tName: \"{2}\"\tByte data: {3}"},
    { WRITE_DATA_W_INDICES,        "Writing data -- Offset: {0}\tLength: {1}\tName: \"{2}\" Indices: {3}\tByte data: {4}"},
    

    { "com.ibm.as400.data.ParseException",             "A parse error occurred." },
    { "com.ibm.as400.data.PcmlSpecificationException", "A PCML specification error occurred." },
    { "java.io.IOException",                           "An I/O error occurred." },
    { "java.lang.ClassNotFound",                       "A ClassNotFound error occurred." },


    // Internal errors
    { BAD_DATA_TYPE,               "Internal Error: Unknown data type, {0}. Processing <data> element ''{1}''." },
    { BAD_NODE_TYPE,               "Internal Error: Unknown document node type, {0}. Processing document item ''{1}''." },
    { CLASS_NOT_FOUND,             "Internal Error: Class not found for data type {0}. Processing document item ''{1}''." },
    { ERROR_ACCESSING_VALUE,       "Internal Error: Error accessing data value. Indices: {0}, Dimensions: {1}. Processing <data> element ''{2}''." },

	// END TRANSLATION
	};
}
