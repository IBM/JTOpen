///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2016 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class GenerateConverterTable {
  private static final String copyright = "Copyright (C) 1997-2016 International Business Machines Corporation and others.";
  static AS400 sys = null;

  static boolean compress_ = true; // Compress the conversion table
                                   // Note: turn this off for debugging purposes

  static boolean codePointPerLine_ = false; // Should only 1 code point be
                                            // printed per line
  static boolean ascii_ = false; // Indicates if listed ccsids are ascii tables
                                 // or not

  static boolean bidi_ = false; // Indicates if listed ccsids are bidi tables or
                                // not
  // Note: bidi_ and ascii_ cannot both be true

  static boolean showOffsets_ = false; // Indicates of the offsets should be
                                       // printed in the tables

  public static void main(String[] args) {
    if (args.length < 4) {
      System.out
          .println("Usage: java com.ibm.as400.access.GenerateConverterTable system uid pwd [-nocompress] [-ascii] [-bidi] [-showOffsets] [-codePointPerLine] ccsid [ccsid2] [ccsid3] [ccsid4] ...");
      System.exit(0);
    }

    try {
      sys = new AS400(args[0], args[1], args[2]);
      sys.connectService(AS400.CENTRAL);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }

    int start = 3;
    if (args[start].equals("-nocompress")) {
      compress_ = false;
      ++start;
    }
    if (args[start].equals("-ascii")) {
      ascii_ = true;
      ++start;
    }
    if (args[start].equals("-bidi")) {
      bidi_ = true;
      ++start;
    }

    if (args[start].equals("-showOffsets")) {
      showOffsets_ = true;
      ++start;
    }

    if (args[start].equals("-codePointPerLine")) {
      codePointPerLine_ = true;
      ++start;
    }

    for (int i = start; i < args.length; ++i) {
      go((new Integer(args[i])).intValue());
    }
  }

  static String formattedChar(char x) {
    int num = 0xFFFF & (int) x;

    String s = "\\u";
    if (num < 16)
      s += "0";
    if (num < 256)
      s += "0";
    if (num < 4096)
      s += "0";
    s += Integer.toHexString(num).toUpperCase();
    return s;
  }

  static void go(int ccsid) {
    char[] tableToUnicode = new char[0];
    char[] tableToEbcdic = new char[0];
    char[][] surrogateTable = null;

    // int numTables1 = 1;
    // int numTables2 = 1;

    boolean ebcdicIsDBCS = false;
    int doubleByteFormat = NLSTableDownload.DOUBLE_BYTE_FROM_CCSID; 
    int originalCcsid = ccsid; 
    
    try {
      AS400ImplRemote impl = (AS400ImplRemote) sys.getImpl();

      NLSTableDownload down = new NLSTableDownload(impl);
      down.connect();

       
      
      if (ccsid == 1089) // There are currently no tables for 1089->13488->1089;
                         // use 61952 instead, since it would be the same
                         // anyway.
      {
        System.out.println("Special case for ccsid 1089.");
        System.out.println("Retrieving " + ccsid + "->61952 table...");
        
        tableToUnicode = down.download(ccsid, 61952,
            NLSTableDownload.SINGLE_BYTE_FROM_CCSID);
      } else if (ccsid == 61175) {
        System.out.println("Special case for ccsid 61175.");
        System.out.println("Retrieving 1026->13488 table and adjusting...");
        tableToUnicode = down.download(1026, 13488,
            NLSTableDownload.SINGLE_BYTE_FROM_CCSID);
        tableToUnicode[0xFC] = tableToUnicode[0x7F];
        tableToUnicode[0x7F] = '"'; 
      } else if (ccsid == 1376) {
        // This is double byte so fall into bottom path
        tableToUnicode = null;
      } else if (ccsid == 1371) {
        tableToUnicode = null; 
        doubleByteFormat = NLSTableDownload.MIXED_BYTE_FROM_CCSID; 
      } else {
        System.out.println("Retrieving " + ccsid + "->13488 table...");
        tableToUnicode = down.download(ccsid,  13488 ,
            NLSTableDownload.SINGLE_BYTE_FROM_CCSID);
      }
      if (tableToUnicode == null || tableToUnicode.length == 0) {
        String reason = ""; 
        if (tableToUnicode == null) {
          reason ="tableToUnicode is null";
        } else {
          reason ="tableToUnicode.length is 0"; 
        }
        if (ccsid == 1175) {
          System.out.println("Aborting since CCSD 1175 failed to download");
          throw new Exception("Aborting since CCSD 1175 failed to download");
        }
        System.out
            .println(ccsid
                + " must be double-byte because download failed ("+reason+"). Performing secondary retrieve of "
                + ccsid + "->1200 table...");
        ebcdicIsDBCS = true;
        down.disconnect();
        down.connect();
        tableToUnicode = down.download(ccsid, 1200,
            doubleByteFormat);
      }

      System.out.println("  Size: " + tableToUnicode.length);
      if (tableToUnicode.length > 65536) {
        System.out.println("Size is > 65536.  Fixing table");
        int next = 0;
        int from = 0;
        char[] newTable = new char[65536];
        while (from < tableToUnicode.length && next < 65536) {

          int c = 0xFFFF & (int) tableToUnicode[from];
          if (next > 0xECAA && next <= 0xECD0) {
            System.out.println("Next=0x" + Integer.toHexString(next) + " to="
                + Integer.toHexString(c));
          }

          int nextchar = 0;
          if (from + 1 < tableToUnicode.length) {
            nextchar = 0xFFFF & (int) tableToUnicode[from + 1];
          }

          if (
          // in surrogate range
          ((c >= 0xD800) && (c <= 0xDFFF))
              ||
              // Uses combining character
              ((nextchar == 0x309A) && (c != 0x3099)) ||  /* In 835 there are two combining characters next to each other */ 
                                                        /* In that case, we do not combine */ 
              (c != 0xFFfd && nextchar == 0x300)
              || (c != 0xffd && c != 0x300 && nextchar == 0x301)
              ||
              // Weird cases..
              (c == 0x2e5 && nextchar == 0x2e9)
              || (c == 0x2e9 && nextchar == 0x2e5)) {
            // Mark as surrogate
            newTable[next] = (char) 0xD800;

            // add to surrogate table
            if (surrogateTable == null) {
              surrogateTable = new char[65536][];
            }
            char[] pair = new char[2];
            surrogateTable[next] = pair;
            pair[0] = (char) (0xFFFF & (int) tableToUnicode[from]);
            pair[1] = (char) (0xFFFF & (int) tableToUnicode[from + 1]);
            /*
             * System.out.println("Warning: Sub at offset "+Integer.toHexString(next
             * )+" for "+Integer.toHexString(0xFFFF & (int)
             * table1[from])+" "+Integer.toHexString(0xFFFF & (int)
             * table1[from+1]));
             */
            from += 2;
          } else {
            newTable[next] = (char) c;
            from++;
          }
          next++;
        }
        tableToUnicode = newTable;

      }
      down.disconnect();
      down.connect();
      
      if (ccsid == 1089) {
        System.out.println("Special case for ccsid 1089.");
        System.out.println("Retrieving 61952->" + ccsid + " table...");
        tableToEbcdic = down.download(61952, ccsid,
            NLSTableDownload.DOUBLE_BYTE_FROM_CCSID);
      } else {
        /* Use 1200 instead of 13488 */
        System.out.println("Retrieving 1200->" + ccsid + " table...");
        tableToEbcdic = down.download(1200, ccsid,
            NLSTableDownload.DOUBLE_BYTE_FROM_CCSID);
      }
      System.out.println("  Size: " + tableToEbcdic.length);

      // If a mixed CCSID, then we need to fix the tableToEbcdic
      // Convert the table to a byte array
      // Go through table and process SI/I)
      if (doubleByteFormat == NLSTableDownload.MIXED_BYTE_FROM_CCSID) {
        char[] newTableToEbcdic = new char[65536];
        byte[] oldTableToEbcdic = new byte[tableToEbcdic.length * 2]; 
        for (int i = 0; i < tableToEbcdic.length; i++) {
          oldTableToEbcdic[2*i] = (byte) ( 0xFF & (tableToEbcdic[i] >> 8)); 
          oldTableToEbcdic[2*i+1] = (byte)( tableToEbcdic[i] & 0xFF); 
        }
        boolean singleByte = true; 
        int newTableOffset = 0; 
        for (int i = 0; i < oldTableToEbcdic.length; i++) {
           int b = 0xFF & oldTableToEbcdic[i]; 
           while ((i > 0x0F) && (i < oldTableToEbcdic.length) && ((b == 0x0E) || (b == 0x0F)) ) {
             if (b == 0x0E) {
                singleByte = false;
                i++; 
             } else {
               singleByte = true;
               i++; 
             }
             if (i < oldTableToEbcdic.length) { 
                b = 0xFF & oldTableToEbcdic[i];
             }
           } 
          if (i < oldTableToEbcdic.length) {
            if (singleByte) {
              if (newTableOffset < newTableToEbcdic.length) { 
                newTableToEbcdic[newTableOffset] = (char) b;
                newTableOffset++;
              }
            } else {
              i++;
              if (i < oldTableToEbcdic.length) { 
                 int c =  0xFF & oldTableToEbcdic[i];
                 if (newTableOffset < newTableToEbcdic.length) {
                   if (newTableOffset == 0x431) {
                     newTableToEbcdic[newTableOffset] = (char) ((b << 8) + c);
                     newTableOffset++;
                     
                   } else { 
                     newTableToEbcdic[newTableOffset] = (char) ((b << 8) + c);
                     newTableOffset++;
                   }
                 }
              }

            }
          }
        } /* for i */ 
        while (newTableOffset < newTableToEbcdic.length) {
            newTableToEbcdic[newTableOffset]=(char) 0xFEFE; 
            newTableOffset++; 
        }
        tableToEbcdic=newTableToEbcdic;
      } /* If Mixed byte */ 
      sys.disconnectAllServices();
    } catch (Exception e) {
      e.printStackTrace(System.out);
    }

    // Do any necessary fixup
    if (ccsid == 290) {
      
      tableToUnicode[0xE1] = '\u20ac'; 
      char toEbcdic = tableToEbcdic[0x20ac / 2]; 
      toEbcdic = (char)((0xE1 << 8) | (toEbcdic & 0xFF)); 
      tableToEbcdic[0x20ac / 2] = toEbcdic; 
    }
    // Verify the mapping
    verifyRoundTrip(tableToUnicode, tableToEbcdic, ebcdicIsDBCS);

    System.out.println("****************************************");
    System.out.println("Verify round 2 ");
    System.out.println("****************************************");
    verifyRoundTrip(tableToUnicode, tableToEbcdic, ebcdicIsDBCS);

    // Compress the ccsid table
    if (ebcdicIsDBCS) {
      if (compress_) {
        System.out.println("Compressing " + ccsid
            + "->13488 conversion table...");
        char[] arr = compress(tableToUnicode);
        System.out.println("Old compression length: " + arr.length
            + " characters.");
        char[] temparr = compressBetter(tableToUnicode);
        System.out.println("New compression length: " + temparr.length
            + " characters.");
        if (temparr.length > arr.length) {
          System.out
              .println("WARNING: New algorithm WORSE than old algorithm!");
        }
        System.out.println("Verifying compressed table...");
        arr = decompressBetter(temparr);
        if (arr.length != tableToUnicode.length) {
          System.out.println("Verification failed, lengths not equal: "
              + arr.length + " != " + tableToUnicode.length);
          int c = 0;
          while (c < arr.length && arr[c] == tableToUnicode[c])
            ++c;
          System.out.println("First mismatch at index " + c + ": "
              + (int) arr[c] + " != " + (int) tableToUnicode[c]);
        } else {
          boolean bad = false;
          for (int c = 0; c < arr.length; ++c) {
            if (arr[c] != tableToUnicode[c]) {
              bad = true;
              System.out.println(c + ": " + Integer.toHexString((int) arr[c])
                  + " != " + Integer.toHexString((int) tableToUnicode[c]));
            }
          }
          if (bad) {
            System.out.println("Mismatches found in table.");
          } else {
            tableToUnicode = temparr;
            System.out.println("Table verified.");
          }
        }
      }
    }

    int fileCcsid = ccsid; 
    if ( doubleByteFormat  == NLSTableDownload.MIXED_BYTE_FROM_CCSID) {
        fileCcsid = ccsid + 1100000; 
        System.out.println("Create file using "+fileCcsid+" since MIXED CCSID "); 
    }
    // Write out the ccsid table
    try {
      String fName = "ConvTable" + fileCcsid + ".java";
      FileWriter f = new FileWriter(fName);
      writeHeader(f, fileCcsid, sys.getSystemName());
      if (ascii_) {
        f.write("class ConvTable" + fileCcsid + " extends ConvTableAsciiMap\n{\n");
      } else if (bidi_) {
        f.write("class ConvTable" + fileCcsid + " extends ConvTableBidiMap\n{\n");
      } else if (ebcdicIsDBCS) {
        f.write("class ConvTable" + fileCcsid + " extends ConvTableDoubleMap\n{\n");
      } else {
        f.write("class ConvTable" + fileCcsid + " extends ConvTableSingleMap\n{\n");
      }

      f.write("  private static char[] toUnicodeArray_;  \n");
      f.write("  private static final String copyright = \"Copyright (C) 1997-2016 International Business Machines Corporation and others.\";\n");
      f.write("  private static final String toUnicode_ = \n");
      System.out.print("Writing table for conversion from " + ccsid
          + " to 13488... to " + fName + "\n");
      for (int i = 0; i < tableToUnicode.length; i = i + 16) {
        if (showOffsets_) {
          f.write("/* " + Integer.toHexString(i) + " */ \"");
        } else {
          f.write("    \"");
        }
        for (int j = 0; j < 16 && (i + j) < tableToUnicode.length; ++j) {
          int num = (int) tableToUnicode[i + j];

          if (num == 0x0008)
            f.write("\\b");
          else if (num == 0x0009)
            f.write("\\t");
          // else if(num == 0x000A) f.write("\\r");
          else if (num == 0x000A)
            f.write("\\n");
          else if (num == 0x000C)
            f.write("\\f");
          // else if(num == 0x000D) f.write("\\n");
          else if (num == 0x000D)
            f.write("\\r");
          else if (num == 0x0022)
            f.write("\\\"");
          else if (num == 0x0027)
            f.write("\\'");
          else if (num == 0x005C)
            f.write("\\\\");
          else {
            String s = "\\u";
            if (num < 16)
              s += "0";
            if (num < 256)
              s += "0";
            if (num < 4096)
              s += "0";
            s += Integer.toHexString(num).toUpperCase();
            f.write(s);
          }
          if (codePointPerLine_) {
            if (j < 15) {
              if (showOffsets_) {
                f.write("\" +\n/* " + Integer.toHexString(i + j + 1) + " */ \"");
              } else {
                f.write("\" +\n    \"");
              }
            }
          }
        }
        if (i + 16 < tableToUnicode.length)
          f.write("\" +\n");
        else
          f.write("\";\n");
      } /* for i */
      f.write("\n");
      f.write("\n");

      // Write out the surrogateTable if it exists
      if (surrogateTable != null) {

        f.write("\n");

        f.write("  private static final char[][] toUnicodeSurrogateMappings = { \n");
        System.out.print("Writing surrogate table for conversion from " + ccsid
            + " to 13488... to " + fName + "\n");
        for (int i = 0; i < surrogateTable.length; i++) {
          char[] pair = surrogateTable[i];
          if (pair != null) {
            f.write("{'" + formattedChar((char) i) + "','"
                + formattedChar(pair[0]) + "','" + formattedChar(pair[1])
                + "'},\n");
          }
        } /* for i */
        f.write("};\n");
        f.write("\n");
        f.write("\n");

      } /* if surrogate table */

      f.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Compress the Unicode table
    if (compress_) {
      System.out
          .println("Compressing 13488->" + ccsid + " conversion table...");
      char[] arr = compress(tableToEbcdic);
      System.out.println("Old compression length: " + arr.length
          + " characters.");
      char[] temparr = compressBetter(tableToEbcdic);
      System.out.println("New compression length: " + temparr.length
          + " characters.");
      if (temparr.length > arr.length) {
        System.out.println("WARNING: New algorithm WORSE than old algorithm!");
      }
      System.out.println("Verifying compressed table...");
      arr = decompressBetter(temparr);
      if (arr.length != tableToEbcdic.length) {
        System.out.println("Verification failed, lengths not equal: "
            + arr.length + " != " + tableToEbcdic.length);
        int c = 0;
        while (c < arr.length && arr[c] == tableToEbcdic[c])
          ++c;
        System.out.println("First mismatch at index " + c + ": " + (int) arr[c]
            + " != " + (int) tableToEbcdic[c]);
        tableToEbcdic = temparr;
      } else {
        boolean bad = false;
        for (int c = 0; c < arr.length; ++c) {
          if (arr[c] != tableToEbcdic[c]) {
            bad = true;
            System.out.println(c + ": " + Integer.toHexString((int) arr[c])
                + " != " + Integer.toHexString((int) tableToEbcdic[c]));
          }
        }
        if (bad) {
          System.out.println("Mismatches found in table.");
        } else {
          tableToEbcdic = temparr;
          System.out.println("Table verified.");
        }
      }
    }

    // Write out the Unicode table
    try {
      String fName = "ConvTable" + fileCcsid + ".java";
      FileWriter f = new FileWriter(fName, true);

      f.write("  private static char[] fromUnicodeArray_; \n");
      f.write("  private static final String fromUnicode_ = \n");
      System.out.print("Writing table for conversion from 13488 to " + ccsid
          + "... to " + fName + "\n");
      for (int i = 0; i < tableToEbcdic.length; i = i + 16) {
        if (showOffsets_) {
          f.write("/* " + Integer.toHexString(i) + " */ \"");
        } else {
          f.write("    \"");
        }
        for (int j = 0; j < 16 && (i + j) < tableToEbcdic.length; ++j) {
          int num = (int) tableToEbcdic[i + j]; // these each contain 2 single
                                                // byte chars, but we write it
                                                // like this to save space
          if (num == 0x0008)
            f.write("\\b");
          else if (num == 0x0009)
            f.write("\\t");
          // else if(num == 0x000A) f.write("\\r");
          else if (num == 0x000A)
            f.write("\\n");
          else if (num == 0x000C)
            f.write("\\f");
          // else if(num == 0x000D) f.write("\\n");
          else if (num == 0x000D)
            f.write("\\r");
          else if (num == 0x0022)
            f.write("\\\"");
          else if (num == 0x0027)
            f.write("\\'");
          else if (num == 0x005C)
            f.write("\\\\");
          else {
            String s = "\\u";
            if (num < 16)
              s += "0";
            if (num < 256)
              s += "0";
            if (num < 4096)
              s += "0";
            s += Integer.toHexString(num).toUpperCase();
            f.write(s);
          }
          if (codePointPerLine_) {
            if (j < 15) {
              if (showOffsets_) {
                f.write("\" +\n/* " + Integer.toHexString(i + j + 1) + " */ \"");
              } else {
                f.write("\" +\n    \"");
              }
            }
          }

        }
        if (i + 16 < tableToEbcdic.length)
          f.write("\" +\n");
        else
          f.write("\";\n");
      }
      f.write("\n");

      f.write("  static {\n");
      f.write("    toUnicodeArray_ = toUnicode_.toCharArray();\n");
      f.write("    fromUnicodeArray_ = fromUnicode_.toCharArray();\n");
      f.write("  }\n");

      f.write("\n  ConvTable" + fileCcsid + "()\n  {\n");
      f.write("    super(" + fileCcsid + ", ");
      f.write("toUnicodeArray_, ");
      if (surrogateTable != null) {
        f.write("fromUnicodeArray_,");
        f.write("toUnicodeSurrogateMappings);\n");
      } else {
        f.write("fromUnicodeArray_);\n");
      }
      f.write("  }\n\n");

      f.write("\n  ConvTable" + fileCcsid + "(int ccsid)\n  {\n");
      f.write("    super(ccsid, ");
      f.write("toUnicodeArray_, ");
      if (surrogateTable != null) {
        f.write("fromUnicodeArray_,");
        f.write("toUnicodeSurrogateMappings);\n");
      } else {
        f.write("fromUnicodeArray_);\n");
      }

      f.write("  }\n}\n");

      f.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.print("Done.\n");
  }

  private static boolean verifyRoundTrip(char[] tableToUnicode,
      char[] tableToEbcdic, boolean ebcdicIsDBCS) {

    String ebcdicPrefix = "X";
    if (ebcdicIsDBCS) {
      ebcdicPrefix = "GX";
    }

    // Make sure substitution is correct for Unicode to ebcdic
    if (!ebcdicIsDBCS) {
      int piece = 0xFFFF & tableToEbcdic[0x1a / 2];
      if ((piece >> 8) != 0x3f) {
        System.out.println("Fixing sub char in tableToEbcdic == sub was 0x"
            + Integer.toHexString(piece >> 8));
        piece = (0x3f << 8) | (0xFF & piece);
        tableToEbcdic[0x1a / 2] = (char) piece;
      }
    }

    System.out.println("Checking round trip");
    boolean passed = true;
    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    StringBuffer sb3 = new StringBuffer();

    for (int i = 0; i < tableToUnicode.length; i++) {
      int unicodeChar = 0xFFFF & tableToUnicode[i];
      if (unicodeChar != 0xfffd && unicodeChar != 0xD800) {
        int ebcdicChar;
        if (ebcdicIsDBCS) {
          ebcdicChar = 0xFFFF & tableToEbcdic[unicodeChar];
        } else {
          int piece = 0xFFFF & tableToEbcdic[unicodeChar / 2];
          if (unicodeChar % 2 == 0) {
            ebcdicChar = piece >> 8;
          } else {
            ebcdicChar = piece & 0xFF;
          }
        }
        if (i != ebcdicChar) {
          if ((unicodeChar != 0x1a)
              && ((ebcdicChar == 0xFEFE) || (ebcdicChar == 0x3F))) {
            sb1.append("Fixing up EBCDIC RoundTrip Failure " + ebcdicPrefix
                + "'" + Integer.toHexString(i) + "'" + " -> UX'"
                + Integer.toHexString(unicodeChar) + "'" + " -> "
                + ebcdicPrefix + "'" + Integer.toHexString(ebcdicChar) + "'\n");
            if (ebcdicIsDBCS) {
              tableToEbcdic[unicodeChar] = (char) i;
            } else {
              int piece = 0xFFFF & tableToEbcdic[unicodeChar / 2];
              if (unicodeChar % 2 == 0) {

                piece = (i << 8) | (piece & 0x00FF);
              } else {
                piece = (piece & 0xFF00) | i;
              }
              tableToEbcdic[unicodeChar / 2] = (char) piece;
            }
            passed = false;
          } else {
            int unicodeChar2 = 0;
            try {
              unicodeChar2 = 0xFFFF & tableToUnicode[ebcdicChar];
            } catch (ArrayIndexOutOfBoundsException e) {
              System.out.println("ERROR.. ArrayIndexOutOfBounds");
              System.out.println("ebcdicChar=0x"
                  + Integer.toHexString((int) ebcdicChar));
              System.out.println("i=" + i);
              System.out.println("unicodeChar=0x"
                  + Integer.toHexString((int) unicodeChar));
              throw e;

            }
            if (unicodeChar2 == unicodeChar) {
              sb2.append("Secondary EBCDIC mapping " + ebcdicPrefix + "'"
                  + Integer.toHexString(i) + "'" + " -> UX'"
                  + Integer.toHexString(unicodeChar) + "'" + " -> "
                  + ebcdicPrefix + "'" + Integer.toHexString(ebcdicChar) + "'"
                  + " -> UX'" + Integer.toHexString(unicodeChar2) + "'\n");

            } else {
              sb3.append("EBCDIC RoundTrip Failure2 " + ebcdicPrefix + "'"
                  + Integer.toHexString(i) + "'" + " -> UX'"
                  + Integer.toHexString(unicodeChar) + "'" + " -> "
                  + ebcdicPrefix + "'" + Integer.toHexString(ebcdicChar) + "'"
                  + " -> UX'" + Integer.toHexString(unicodeChar2) + "'\n");
              passed = false;

            }
          }
        }
      }
    }
    System.out.println(sb2);
    System.out.println(sb1);
    System.out.println(sb3);

    sb1.setLength(0);
    sb2.setLength(0);
    sb3.setLength(0);

    for (int i = 0; i < tableToEbcdic.length; i++) {

      int ebcdicChar;
      if (ebcdicIsDBCS) {
        ebcdicChar = 0xFFFF & tableToEbcdic[i];
      } else {
        int piece = 0xFFFF & tableToEbcdic[i / 2];
        if (i % 2 == 0) {
          ebcdicChar = piece >> 8;
        } else {
          ebcdicChar = piece & 0xFF;
        }
      }

      if ((ebcdicChar != 0xfefe) && (ebcdicChar != 0x3f)) {
        int unicodeChar = 0xFFFF & tableToUnicode[ebcdicChar];
        if (i != unicodeChar) {
          if (unicodeChar == 0xFFFD) {
            sb1.append("Unicode RoundTrip Failure UX'" + Integer.toHexString(i)
                + "'" + " -> " + ebcdicPrefix + "'"
                + Integer.toHexString(ebcdicChar) + "'" + " -> UX'"
                + Integer.toHexString(unicodeChar) + "'\n");
            passed = false;
          } else {
            int ebcdicChar2;
            if (ebcdicIsDBCS) {
              ebcdicChar2 = 0xFFFF & tableToEbcdic[unicodeChar];
            } else {
              int piece = 0xFFFF & tableToEbcdic[unicodeChar / 2];
              if (unicodeChar % 2 == 0) {
                ebcdicChar2 = piece >> 8;
              } else {
                ebcdicChar2 = piece & 0xFF;
              }
            }

            if (ebcdicChar2 == ebcdicChar) {
              sb2.append("Secondary Unicode mapping UX'"
                  + Integer.toHexString(i) + "'" + " -> " + ebcdicPrefix + "'"
                  + Integer.toHexString(ebcdicChar) + "'" + " -> UX'"
                  + Integer.toHexString(unicodeChar) + "'" + " -> "
                  + ebcdicPrefix + "'" + Integer.toHexString(ebcdicChar2)
                  + "'\n");

            } else {
              sb3.append("Unicode RoundTrip Failure2 UX'"
                  + Integer.toHexString(i) + "'" + " -> " + ebcdicPrefix + "'"
                  + Integer.toHexString(ebcdicChar) + "'" + " -> UX'"
                  + Integer.toHexString(unicodeChar) + "'" + " -> "
                  + ebcdicPrefix + "'" + Integer.toHexString(ebcdicChar2)
                  + "'\n");
              passed = false;

            }

          }
        }
      }
    }
    System.out.println(sb2);
    System.out.println(sb1);
    System.out.println(sb3);

    return passed;

  }

  private static final char repSig = '\uFFFF'; // compression indication
                                               // character
  private static final char cic_ = repSig;

  private static final char rampSig = '\uFFFE'; // ramp indication character
  private static final char ric_ = rampSig;

  private static final char hbSig = '\u0000'; // high-byte compression
                                              // indication character
  private static final char pad = '\u0000'; // pad character

  static int repeatCheck(char[] arr, int startingIndex) {
    int index = startingIndex + 1;
    while (index < arr.length && arr[index] == arr[index - 1]) {
      ++index;
    }
    return (index - startingIndex);
  }

  static final int rampCheck(char[] arr, int startingIndex) {
    int index = startingIndex + 1;
    while (index < arr.length && arr[index] == arr[index - 1] + 1) {
      ++index;
    }
    return (index - startingIndex);
  }

  static int hbCheck(char[] arr, int startingIndex) {
    int index = startingIndex + 1;
    while (index < arr.length) {
      // check for repeat
      // for 6 repeated chars, we'd need either 3 hb-compressed chars or 3
      // repeatsig chars, so it's a toss up
      if (repeatCheck(arr, index) > 6)
        return (index - startingIndex); // at this point though, it's better to
                                        // stop and do the repeat

      // check for ramp, same reason
      if (rampCheck(arr, index) > 6)
        return (index - startingIndex);

      // OK, finally check for hb
      if ((arr[index] & 0xFF00) != (arr[index - 1] & 0xFF00))
        return (index - startingIndex);

      ++index;
    }
    return (index - startingIndex);
  }

  static int numRepeats;
  static int numRamps;
  static int hbRepeats;
  static int charRepeats;

  // This is the new way - 05/04/2000.
  static char[] compressBetter(char[] arr) {
    numRepeats = 0;
    numRamps = 0;
    hbRepeats = 0;
    charRepeats = 0;

    // This uses the "correct" compression scheme from my invention disclosure
    // It also employs high-byte compression, something that I did not include
    // in my disclosure.
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < arr.length; ++i) {
      int repNum = repeatCheck(arr, i);
      if (repNum > 3) // had enough repeats
      {
        numRepeats++;
        buf.append(repSig);
        buf.append((char) repNum);
        buf.append(arr[i]);
        i += repNum - 1;
      } else {
        int rampNum = rampCheck(arr, i);
        if (rampNum > 3) // had enough in the ramp
        {
          numRamps++;
          buf.append(rampSig);
          buf.append((char) rampNum);
          buf.append(arr[i]);
          i += rampNum - 1;
        } else {
          int hbNum = hbCheck(arr, i);
          --hbNum; // don't include the first char, since we always append it.
          if (hbNum >= 6) {
            // System.out.print("HBNUM is "+Integer.toHexString((int)hbNum)+"; ");
            hbRepeats++;
            // pattern is this: ss ss nn nn hh tt xx xx xx xx ...
            // where ss ss is hbSig
            // nn nn is hbNum
            // hh tt is the first char (hh is the repeated high byte)
            // xx is the lower byte of the next char in the sequence
            // xx repeats hbNum/2 times so that
            // hbNum is the total number of repeated db chars in the ciphertext,
            // not including the first char.
            // Note that there may be, in actuality, hbNum*2 +1 chars in the
            // cleartext that fit into the
            // conversion, but since we'd have to fill out the last char with an
            // empty byte, there's no point
            // in doing it anyway. Besides, it might be able to be compressed
            // via another scheme with itself as
            // the starting character.
            // int start = i;
            buf.append(hbSig);
            if (hbNum % 2 == 1) // odd number
            {
              --hbNum; // no point in doing the last char
            }
            // System.out.println("Appending "+Integer.toHexString((int)((char)(hbNum/2))));
            buf.append((char) (hbNum / 2)); // hbNum is always even, so this
                                            // comes out.
            // System.out.print("hb comp: "+Integer.toHexString(hbNum)+": ");
            // for (int b=0; b<hbNum; ++b)
            // {
            // System.out.print(Integer.toHexString((int)arr[i+b])+" ");
            // }
            // System.out.println();
            buf.append(arr[i++]);
            for (int j = 0; j < (hbNum / 2); ++j) {
              char x = (char) (((0x00FF & arr[i + (j * 2)]) * 256) + (0x00FF & arr[i
                  + (j * 2) + 1]));
              buf.append(x);
            }
            i = i + hbNum - 1;
            // System.out.print("row ("+start+","+i+"): ");
            // for (int b=start-1; b<=i; ++b)
            // {
            // System.out.print(Integer.toHexString((int)arr[b])+" ");
            // }
            // System.out.println();
          } else {
            buf.append(arr[i]);
            charRepeats++;
            if (arr[i] == repSig || arr[i] == rampSig || arr[i] == hbSig) {
              buf.append(pad); // pad
            }
          }
        }
      }
    }
    System.out.println("Compression stats: " + numRepeats + " repeats, "
        + numRamps + " ramps, " + hbRepeats + " highbytes, " + charRepeats
        + " regular.");
    numRepeats = 0;
    numRamps = 0;
    hbRepeats = 0;
    charRepeats = 0;
    return buf.toString().toCharArray();
  }

  static char[] decompressBetter(char[] arr) {
    // let's try decompressing for testing purposes
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < arr.length; ++i) {
      if (arr[i] == repSig) {
        if (arr[i + 1] == pad) {
          buf.append(repSig);
          i++;
        } else {
          numRepeats++;
          int repNum = arr[i + 1];
          char repChar = arr[i + 2];
          for (int j = 0; j < repNum; ++j) {
            buf.append(repChar);
          }
          i += 2;
        }
      } else if (arr[i] == rampSig) {
        if (arr[i + 1] == pad) {
          buf.append(rampSig);
          i++;
        } else {
          numRamps++;
          int rampNum = arr[i + 1];
          char rampStart = arr[i + 2];
          for (int j = 0; j < rampNum; ++j) {
            buf.append((char) (j + rampStart));
          }
          i += 2;
        }
      } else if (arr[i] == hbSig) {
        if (arr[i + 1] == pad) {
          buf.append(hbSig);
          i++;
        } else {
          hbRepeats++;
          int hbNum = (0x0000FFFF & arr[++i]);
          // System.out.print("hb decomp: "+Integer.toHexString(hbNum)+": ");
          char firstChar = arr[++i];
          char highByteMask = (char) (0xFF00 & firstChar);
          // System.out.print("found: "+Integer.toHexString((int)arr[i-2])+" ("+Integer.toHexString((int)arr[i-1])+") "+Integer.toHexString((int)firstChar)+" ");
          buf.append(firstChar);
          ++i;
          for (int j = 0; j < hbNum; ++j) {
            char both = arr[i + j];
            char c1 = (char) (highByteMask + ((0xFF00 & both) >>> 8));
            char c2 = (char) (highByteMask + (0x00FF & both));
            buf.append(c1);
            buf.append(c2);
            // System.out.print(Integer.toHexString((int)c1)+" "+Integer.toHexString((int)c2)+" ");
          }
          // System.out.println(Integer.toHexString((int)arr[i+hbNum]));
          i = i + hbNum - 1;
        }
      } else {
        buf.append(arr[i]);
        charRepeats++;
      }
    }
    System.out.println("Decompression stats: " + numRepeats + " repeats, "
        + numRamps + " ramps, " + hbRepeats + " highbytes, " + charRepeats
        + " regular.");
    numRepeats = 0;
    numRamps = 0;
    hbRepeats = 0;
    charRepeats = 0;
    return buf.toString().toCharArray();
  }

  // This is the old way
  static char[] compress(char[] arr) {

    if (arr.length < 3)
      return arr;
    StringBuffer buf = new StringBuffer();
    char oldold = arr[0];
    char old = arr[1];
    int count = 0;
    boolean inCompression = false; // this flags if we are repeating the same
                                   // character
    boolean inRamp = false; // this flags if each subsequent characters is the
                            // previous character + 1

    for (int i = 2; i < arr.length; ++i) {
      if (!inCompression && !inRamp) {
        if (arr[i] == old && arr[i] == oldold) // Check for repeating
        {
          inCompression = true;
          buf.append(cic_);
          buf.append(oldold);
          count = 3;
        } else if (arr[i] == old + 1 && arr[i] == oldold + 2) // Check for ramp
        {
          inRamp = true;
          buf.append(ric_);
          buf.append(oldold);
        } else if (oldold == cic_) // Check for duplicate cic
        {
          buf.append(cic_);
        } else if (oldold == ric_) // Check for duplicate ric
        {
          buf.append(ric_);
        } else // Just copy it normal
        {
          buf.append(oldold);
        }
        oldold = old;
        old = arr[i];
      } else if (inCompression) {
        if (arr[i] == old && arr[i] == oldold) // Still repeating?
        {
          ++count;
          oldold = old;
          old = arr[i];
        } else // Not repeating anymore
        {
          inCompression = false;
          char c = (char) count;
          if (count == 0x0008)
            c = '\b';
          else if (count == 0x0009)
            c = '\t';
          // else if(count == 0x000A) c = '\r'; // Had trouble with 0x0A and
          // 0x0D getting messed up.
          else if (count == 0x000A)
            c = '\n'; // Had trouble with 0x0A and 0x0D getting messed up.
          else if (count == 0x000C)
            c = '\f';
          // else if(count == 0x000D) c = '\n';
          else if (count == 0x000D)
            c = '\r';
          else if (count == 0x0022)
            c = '\"';
          else if (count == 0x0027)
            c = '\'';
          else if (count == 0x005C)
            c = '\\';
          buf.append(c);
          oldold = arr[i++]; // yes this is right... think about it.
          old = arr[i];
        }
      } else { // must be in ramp
        if (arr[i] == old + 1 && arr[i] == oldold + 2) {
          oldold = old;
          old = arr[i];
        } else {
          inRamp = false;
          buf.append(old);
          oldold = arr[i++];
          old = arr[i];
        }
      }
    }
    if (inCompression) {
      char c = (char) count;
      if (count == 0x0008)
        c = '\b';
      else if (count == 0x0009)
        c = '\t';
      else if (count == 0x000A)
        c = '\n';
      else if (count == 0x000C)
        c = '\f';
      // else if(count == 0x000D) c = '\n';
      else if (count == 0x000D)
        c = '\r';
      else if (count == 0x0022)
        c = '\"';
      else if (count == 0x0027)
        c = '\'';
      else if (count == 0x005C)
        c = '\\';
      buf.append(c);
    }
    if (inRamp) {
      buf.append(old);
    }

    return buf.toString().toCharArray();
  }

  static void writeHeader(FileWriter f, int ccsid, String system)
      throws Exception {
    int dotIndex = system.indexOf('.');
    if (dotIndex > 0) {
      system = system.substring(0, dotIndex);
    }
    Date currentDate = new Date();
    // Look up the version dynamically
    Class copyrightClass = Copyright.class;
    Field field = copyrightClass.getField("version");
    String jtopenVersion = (String) field.get(null);

    f.write("///////////////////////////////////////////////////////////////////////////////\n");
    f.write("//\n");
    f.write("// JTOpen (IBM Toolbox for Java - OSS version)\n");
    f.write("//\n");
    f.write("// Filename:  ConvTable" + ccsid + ".java\n");
    f.write("//\n");
    f.write("// The source code contained herein is licensed under the IBM Public License\n");
    f.write("// Version 1.0, which has been approved by the Open Source Initiative.\n");
    f.write("// Copyright (C) 1997-2016 International Business Machines Corporation and\n");
    f.write("// others.  All rights reserved.\n");
    f.write("//\n");
    f.write("// Generated " + currentDate + " from " + system + "\n");
    f.write("// Using " + jtopenVersion + "\n");
    f.write("///////////////////////////////////////////////////////////////////////////////\n\n");
    f.write("package com.ibm.as400.access;\n\n");
  }

}
