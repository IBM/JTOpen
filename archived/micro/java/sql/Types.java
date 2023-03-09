///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Types.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package java.sql;

/**
 * The class that defines the constants that are used to identify generic SQL types, called JDBC types. The actual
 *  type constant values are equivalent to those in XOPEN. 
 **/
public class Types
{
    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type BIT.
     **/
    public final static int BIT = -7;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type TINYINT.
     **/
    public final static int TINYINT = -6;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type SMALLINT.
     **/
    public final static int SMALLINT =   5;
    
    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type INTEGER.
     **/
    public final static int INTEGER  =   4;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type BIGINT.
     **/
    public final static int BIGINT      =  -5;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type FLOAT.
     **/
    public final static int FLOAT       =   6;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type REAL.
     **/
    public final static int REAL     =   7;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type DOUBLE.
     **/
    public final static int DOUBLE      =   8;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type NUMERIC.
     **/
    public final static int NUMERIC  =   2;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type DECIMAL.
     **/
    public final static int DECIMAL     =   3;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type CHAR.
     **/
    public final static int CHAR     =   1;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type VARCHAR.
     **/
    public final static int VARCHAR  =  12;
    
    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type LONGVARCHAR.
     **/
    public final static int LONGVARCHAR    =  -1;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type DATE.
     **/
    public final static int DATE     =  91;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type TIME.
     **/
    public final static int TIME     =  92;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type TIMESTAMP.
     **/
    public final static int TIMESTAMP   =  93;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type BINARY.
     **/
    public final static int BINARY      =  -2;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type VARBINARY.
     **/
    public final static int VARBINARY   =  -3;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type LONGVARBINARY.
     **/
    public final static int LONGVARBINARY  =  -4;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type NULL.
     **/
    public final static int NULL     =   0;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type JAVA_OBJECT.
     **/
    public final static int JAVA_OBJECT         = 2000;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type DISTINCT.
     **/
    public final static int DISTINCT            = 2001;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type STRUCT.
     **/
    public final static int STRUCT              = 2002;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type ARRAY.
     **/
    public final static int ARRAY               = 2003;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type BLOB.
     **/
    public final static int BLOB                = 2004;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type CLOB.
     **/
    public final static int CLOB                = 2005;

    /**
     *  The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type REF.
     **/
    public final static int REF                 = 2006;
}
