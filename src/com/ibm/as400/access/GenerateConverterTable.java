///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  GenerateConverterTable.java
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
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class GenerateConverterTable {
  private static final String copyright = "Copyright (C) 1997-2016 International Business Machines Corporation and others.";
  private static final int MAX_SURROGATE_LENGTH = 2000;
  private static final int MAX_TO_EBCDIC_LENGTH = 20000;
  static AS400 sys = null;
  static Connection connection_ = null;
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

  static boolean useJdbc_ = false; // Use JDBC to retrieve the table information

  public static void main(String[] args) {
    if (args.length < 4) {
      System.out.println(
          "Usage: java com.ibm.as400.access.GenerateConverterTable system uid pwd [-nocompress] [-ascii] [-bidi] [-showOffsets] [-codePointPerLine] [-useJdbc] ccsid [ccsid2] [ccsid3] [ccsid4] ...");
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

    if (args[start].equals("-useJdbc")) {
      useJdbc_ = true;

      try {
        Class.forName("com.ibm.as400.access.AS400JDBCDriver");
        connection_ = DriverManager.getConnection("jdbc:as400:" + args[0],
            args[1], args[2]);

      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }

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
    char[] tableToUnicodeSpaces = new char[0];
    char[] tableToEbcdic = new char[0];
    char[][] surrogateTable = null;
    char[][] tripletTable = null;
    char[][] quadTable = null;

    // int numTables1 = 1;
    // int numTables2 = 1;

    boolean ebcdicIsDBCS = false;
    int doubleByteFormat = NLSTableDownload.DOUBLE_BYTE_FROM_CCSID;
    int originalCcsid = ccsid;

    try {
      if (useJdbc_) {
        if (ConvTable.isMixedCCSID(originalCcsid)) {
          //
          // If we have a mixed CCSID then we will download in two pieces
          // We use the convention that a CCSID that starts with 10xxxxx is
          // SINGLE BYTE part of mixed
          // and a CCSID that starts with 20xxxxx is DOUBLE BYTE part of mixed
          go(1000000 + ccsid);
          go(2000000 + ccsid);

          return;
        } else {
          Class.forName("com.ibm.as400.access.AS400JDBCDriver");
          try {
            if (ccsid > 2000000) {
              ebcdicIsDBCS = true;
            } else if (ccsid > 1000000) {
              ebcdicIsDBCS = false;
            } else {
              ebcdicIsDBCS = jdbcIsDBCS(connection_, ccsid);
            }

            if (ebcdicIsDBCS) {
              tableToUnicodeSpaces = jdbcToUnicodeSpacesDBCS(connection_,
                  ccsid);
              tableToEbcdic = jdbcToEbcdicDBCS(connection_, ccsid);
            } else {
              tableToUnicode = jdbcToUnicode(connection_, ccsid);
              tableToEbcdic = jdbcToEbcdic(connection_, ccsid);
            }
          } catch (Exception e) {
            System.out.println("Error downloading table using JDBC ");
            e.printStackTrace(System.out);
            System.exit(1);
          }

        }
      } else {
        AS400ImplRemote impl = (AS400ImplRemote) sys.getImpl();

        NLSTableDownload down = new NLSTableDownload(impl);
        down.connect();

        if (ccsid == 1089) // There are currently no tables for
                           // 1089->13488->1089;
                           // use 61952 instead, since it would be the same
                           // anyway.
        {
          System.out.println("Special case for ccsid 1089.");
          System.out.println("Retrieving " + ccsid + "->61952 table...");

          tableToUnicode = down.download(ccsid, 61952,
              NLSTableDownload.SINGLE_BYTE_FROM_CCSID);
        } else if (ccsid == 1376) {
          // This is double byte so fall into bottom path
          tableToUnicode = null;
        } else if (ccsid == 1371) {
          tableToUnicode = null;
          doubleByteFormat = NLSTableDownload.MIXED_BYTE_FROM_CCSID;
        } else {
          System.out.println("Retrieving " + ccsid + "->13488 table...");
          tableToUnicode = down.download(ccsid, 13488,
              NLSTableDownload.SINGLE_BYTE_FROM_CCSID);
        }
        if (tableToUnicode == null || tableToUnicode.length == 0) {
          String reason = "";
          if (tableToUnicode == null) {
            reason = "tableToUnicode is null";
          } else {
            reason = "tableToUnicode.length is 0";
          }
          if (ccsid == 1175) {
            System.out.println("Aborting since CCSD 1175 failed to download");
            throw new Exception("Aborting since CCSD 1175 failed to download");
          }
          System.out
              .println(ccsid + " must be double-byte because download failed ("
                  + reason + "). Performing secondary retrieve of " + ccsid
                  + "->1200 table...");
          ebcdicIsDBCS = true;
          down.disconnect();
          down.connect();
          tableToUnicode = down.download(ccsid, 1200, doubleByteFormat);
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

      }
      System.out.println("  Size: " + tableToUnicode.length + " or "
          + tableToUnicodeSpaces.length);
      if (tableToUnicode.length > 65536
          || tableToUnicodeSpaces.length > 131072) {
        System.out.println("Size is > 65536 or 131072.  Fixing table");
        int next = 0;
        int from = 0;
        char[] newTable = new char[65536];
        int lastFrom = 0;
        int lastTo = 0;
        boolean hasSpaces = false;

        int tableLength = tableToUnicode.length;
        if (tableLength > 0) { /* Old logic without spaces */
          while (from < tableLength && next < 65536) {

            int c = 0xFFFF & (int) tableToUnicode[from];

            // If we didn't process a variation selector, ignore it.
            while (c >= 0xFE00 && c <= 0xFE0F) {
              from++;
              c = 0xFFFF & (int) tableToUnicode[from];
            }

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
            ((c >= 0xD800) && (c <= 0xDFFF)) ||
            // Uses Variation selector
                ((nextchar >= 0xFE00) && (nextchar <= 0xFE0F))

                ||
                // Uses combining character
                ((nextchar == 0x309A) && (c != 0x3099))
                || /*
                    * In 835 there are two combining characters next to each
                    * other
                    */
                /* In that case, we do not combine */
                (c != 0xFFfd && nextchar == 0x300)
                || (c != 0xffd && c != 0x300 && nextchar == 0x301) ||
                // Weird cases..
                (c == 0x2e5 && nextchar == 0x2e9)
                || (c == 0x2e9 && nextchar == 0x2e5)) {
              // Mark as surrogate

              newTable[next] = (char) 0xD800;
              lastFrom = next;
              lastTo = 0xD800;

              // add to surrogate table
              if (surrogateTable == null) {
                surrogateTable = new char[65536][];
              }
              char[] pair = new char[2];
              surrogateTable[next] = pair;
              pair[0] = (char) (0xFFFF & (int) tableToUnicode[from]);
              pair[1] = (char) (0xFFFF & (int) tableToUnicode[from + 1]);
              /*
               * System.out.println("Warning: Sub at offset "+Integer.
               * toHexString(next )+" for "+Integer.toHexString(0xFFFF & (int)
               * table1[from])+" "+Integer.toHexString(0xFFFF & (int)
               * table1[from+1]));
               */
              from += 2;
            } else {
              newTable[next] = (char) c;
              if (c != 0xFFFD) {
                lastFrom = next;
                lastTo = c;
              }
              from++;
            }
            next++;
          } /* while */
        } else {
          /* New logic with spaces */
          tableLength = tableToUnicodeSpaces.length;
          while (from < tableLength && next < 65536) {
            int c0 = 0;
            int c1 = 0;
            int c2 = 0;
            int c3 = 0;
            int c4 = 0;
            int characterCount = 0;

            c0 = 0xFFFF & (int) tableToUnicodeSpaces[from];

            // If we didn't process a variation selector, ignore it.
            // In CCSID 1371 FE60 => FE00 and FE61 => FE01, etc
            if (ccsid != 2001371) {
              while (c0 >= 0xFE00 && c0 <= 0xFE0F) {
                from++;
                c0 = 0xFFFF & (int) tableToUnicodeSpaces[from];
              }
            }
            c1 = 0xFFFF & (int) tableToUnicodeSpaces[from + 1];
            if (c1 == 0x3000) {
              characterCount = 1;
            } else {
              c2 = 0xFFFF & (int) tableToUnicodeSpaces[from + 2];
              if (c2 == 0x3000) {
                characterCount = 2;
              } else {
                c3 = 0xFFFF & (int) tableToUnicodeSpaces[from + 3];
                if (c3 == 0x3000) {
                  characterCount = 3;
                } else {
                  c4 = 0xFFFF & (int) tableToUnicodeSpaces[from + 4];
                  if (c4 == 0x3000) {
                    characterCount = 4;
                  } else {
                    throw new Exception(
                        "Character count is too large for from=0x"
                            + Integer.toHexString(from));
                  }
                }
              }
            }

            if (characterCount == 1) {
              newTable[next] = (char) c0;
              from += 2;
            } else if (characterCount == 2) {
              newTable[next] = (char) 0xD800;
              // add to surrogate table
              if (surrogateTable == null) {
                surrogateTable = new char[65536][];
              }
              char[] pair = new char[2];
              surrogateTable[next] = pair;
              pair[0] = (char) (0xFFFF & (int) c0);
              pair[1] = (char) (0xFFFF & (int) c1);
              from += 3;
            } else if (characterCount == 3) { /* Character count must be 3 */
              newTable[next] = (char) 0xD801;
              // add to triple table
              if (tripletTable == null) {
                tripletTable = new char[65536][];
              }
              char[] triple = new char[3];
              tripletTable[next] = triple;
              triple[0] = (char) (0xFFFF & (int) c0);
              triple[1] = (char) (0xFFFF & (int) c1);
              triple[2] = (char) (0xFFFF & (int) c2);
              from += 4;
            } else if (characterCount == 4) { /* character count must be 4 */
              newTable[next] = (char) 0xD802;
              // add to triple table
              if (quadTable == null) {
                quadTable = new char[65536][];
              }
              char[] quad = new char[4];
              quadTable[next] = quad;
              quad[0] = (char) (0xFFFF & (int) c0);
              quad[1] = (char) (0xFFFF & (int) c1);
              quad[2] = (char) (0xFFFF & (int) c2);
              quad[3] = (char) (0xFFFF & (int) c3);
              from += 5;
            } else {
              throw new Exception(
                  "Character count is invalid " + characterCount);
            }

            next++;
          } /* while */
          /* Fill out the rest of the table */
          while (next < 65536) {
            newTable[next] = (char) 0xFFFD;
            next++;
          }

        }
        tableToUnicode = newTable;

      } else if (tableToUnicode.length == 0) {
        // Create a tableToUnicode from tableToUnicodeSpaces
        int len = tableToUnicodeSpaces.length / 2;
        tableToUnicode = new char[len];
        for (int i = 0; i < len; i++) {
          tableToUnicode[i] = tableToUnicodeSpaces[i * 2];
        }

      }
      System.out.println("  Size: " + tableToEbcdic.length);

      // If a mixed CCSID, then we need to fix the tableToEbcdic
      // Convert the table to a byte array
      // Go through table and process SI/I)
      if (doubleByteFormat == NLSTableDownload.MIXED_BYTE_FROM_CCSID) {
        char[] newTableToEbcdic = new char[65536];
        byte[] oldTableToEbcdic = new byte[tableToEbcdic.length * 2];
        for (int i = 0; i < tableToEbcdic.length; i++) {
          oldTableToEbcdic[2 * i] = (byte) (0xFF & (tableToEbcdic[i] >> 8));
          oldTableToEbcdic[2 * i + 1] = (byte) (tableToEbcdic[i] & 0xFF);
        }
        boolean singleByte = true;
        int newTableOffset = 0;
        for (int i = 0; i < oldTableToEbcdic.length; i++) {
          int b = 0xFF & oldTableToEbcdic[i];
          while ((i > 0x0F) && (i < oldTableToEbcdic.length)
              && ((b == 0x0E) || (b == 0x0F))) {
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
                int c = 0xFF & oldTableToEbcdic[i];
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
          newTableToEbcdic[newTableOffset] = (char) 0xFEFE;
          newTableOffset++;
        }
        tableToEbcdic = newTableToEbcdic;
      } /* If Mixed byte */
      sys.disconnectAllServices();
    } catch (Exception e) {
      e.printStackTrace(System.out);
    }

    // Do any necessary fixup
    if (ccsid == 290) {

      tableToUnicode[0xE1] = '\u20ac';
      char toEbcdic = tableToEbcdic[0x20ac / 2];
      toEbcdic = (char) ((0xE1 << 8) | (toEbcdic & 0xFF));
      tableToEbcdic[0x20ac / 2] = toEbcdic;
    }
    // Verify the mapping
    verifyRoundTrip(tableToUnicode, tableToEbcdic, ebcdicIsDBCS, ccsid,
        surrogateTable, tripletTable, quadTable);

    System.out.println("****************************************");
    System.out.println("Verify round 2 ");
    System.out.println("****************************************");
    verifyRoundTrip(tableToUnicode, tableToEbcdic, ebcdicIsDBCS, ccsid,
        surrogateTable, tripletTable, quadTable);

    // Compress the ccsid table
    if (ebcdicIsDBCS) {
      if (compress_) {
        System.out
            .println("Compressing " + ccsid + "->13488 conversion table...");
        char[] arr = compress(tableToUnicode);
        System.out
            .println("Old compression length: " + arr.length + " characters.");
        char[] temparr = compressBetter(tableToUnicode);
        System.out.println(
            "New compression length: " + temparr.length + " characters.");
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
    if (doubleByteFormat == NLSTableDownload.MIXED_BYTE_FROM_CCSID) {
      fileCcsid = ccsid + 1100000;
      System.out
          .println("Create file using " + fileCcsid + " since MIXED CCSID ");
    }
    // Write out the ccsid table
    StringBuffer surrogateInitStringBuffer = new StringBuffer();

    try {
      String fName = "ConvTable" + fileCcsid + ".java";
      FileWriter f = new FileWriter(fName);
      writeHeader(f, fileCcsid, sys.getSystemName());
      if (ascii_) {
        f.write(
            "class ConvTable" + fileCcsid + " extends ConvTableAsciiMap\n{\n");
      } else if (bidi_) {
        f.write(
            "class ConvTable" + fileCcsid + " extends ConvTableBidiMap\n{\n");
      } else if (ebcdicIsDBCS) {
        f.write(
            "class ConvTable" + fileCcsid + " extends ConvTableDoubleMap\n{\n");
      } else {
        f.write(
            "class ConvTable" + fileCcsid + " extends ConvTableSingleMap\n{\n");
      }

      f.write("  private static char[] toUnicodeArray_;  \n");
      f.write(
          "  private static final String copyright = \"Copyright (C) 1997-2016 International Business Machines Corporation and others.\";\n");
      f.write("  // toUnicode_ length is " + tableToUnicode.length + "\n");
      f.write("  private static final String toUnicode_ = \n");
      System.out.print("Writing table for conversion from " + ccsid
          + " to 13488... to " + fName + "\n");
      writeTable(f, tableToUnicode, 0, tableToUnicode.length);
      f.write("\n");
      f.write("\n");

      // Write out the surrogateTable if it exists
      int surrogateLength = 0;
      if (surrogateTable != null) {

        f.write("\n");

        for (int i = 0; i < surrogateTable.length; i++) {
          char[] pair = surrogateTable[i];
          if (pair != null) {
            surrogateLength++;
          }
        } /* for i */

        int surrogateCount = 0;
        char[][] compressedSurrogateTable = new char[surrogateLength][];
        for (int i = 0; i < surrogateTable.length; i++) {
          char[] pair = surrogateTable[i];
          if (pair != null) {
            char[] triplet = new char[3];
            triplet[0] = (char) i;
            triplet[1] = pair[0];
            triplet[2] = pair[1];
            compressedSurrogateTable[surrogateCount] = triplet;
            surrogateCount++;
          }
        } /* for i */

        f.write(
            "  // Number of surrogateMappings is " + surrogateLength + "\n");
        if (surrogateLength < MAX_SURROGATE_LENGTH) {
          f.write(
              "  private static final char[][] toUnicodeSurrogateMappings = { \n");
          System.out.print("Writing surrogate table for conversion from "
              + ccsid + " to 1200... to " + fName + "\n");
          for (int i = 0; i < compressedSurrogateTable.length; i++) {
            char[] triplet = compressedSurrogateTable[i];
            if (triplet != null) {
              f.write("{'" + formattedChar((char) triplet[0]) + "','"
                  + formattedChar(triplet[1]) + "','"
                  + formattedChar(triplet[2]) + "'},\n");
            }
          } /* for i */
          f.write("};\n");
          f.write("\n");
          f.write("\n");
        } else {
          // We must break into pieces
          f.write(
              "  private static char[][] toUnicodeSurrogateMappings = new char["
                  + surrogateLength + "][];\n");
          int startIndex = 0;
          while (startIndex < surrogateLength) {
            f.write("  private static void initToUnicodeSurrogateMappings"
                + startIndex + "() { \n");
            f.write("  char[][] toUnicodeSurrogateMappingsPiece = {\n");
            for (int i = 0; i < MAX_SURROGATE_LENGTH
                && (i + startIndex < surrogateLength); i++) {
              char[] triplet = compressedSurrogateTable[startIndex + i];
              if (triplet != null) {
                f.write("    {'" + formattedChar((char) (triplet[0])) + "','"
                    + formattedChar(triplet[1]) + "','"
                    + formattedChar(triplet[2]) + "'},\n");
              }
            } /* for i */
            f.write("  };\n");

            f.write(
                "    for (int j = 0; j < toUnicodeSurrogateMappingsPiece.length ; j++) {\n");
            f.write("      toUnicodeSurrogateMappings[" + startIndex
                + "+j]= new char[3];\n");
            f.write("      toUnicodeSurrogateMappings[" + startIndex
                + "+j][0] = toUnicodeSurrogateMappingsPiece[j][0];\n");
            f.write("      toUnicodeSurrogateMappings[" + startIndex
                + "+j][1] = toUnicodeSurrogateMappingsPiece[j][1];\n");
            f.write("      toUnicodeSurrogateMappings[" + startIndex
                + "+j][2] = toUnicodeSurrogateMappingsPiece[j][2];\n");
            f.write("    }\n");
            f.write("  }\n");
            f.write("\n");
            surrogateInitStringBuffer.append(
                "   initToUnicodeSurrogateMappings" + startIndex + "();\n");

            startIndex += MAX_SURROGATE_LENGTH;
          }
        }
      } /* if surrogate table */

      // Write out the tripleTable if it exists
      int tripletLength = 0;
      if (tripletTable != null) {

        f.write("\n");

        for (int i = 0; i < tripletTable.length; i++) {
          char[] pair = tripletTable[i];
          if (pair != null) {
            tripletLength++;
          }
        } /* for i */

        int tripletCount = 0;
        char[][] compressedTripletTable = new char[tripletLength][];
        for (int i = 0; i < tripletTable.length; i++) {
          char[] triplet = tripletTable[i];
          if (triplet != null) {
            char[] entry = new char[4];
            entry[0] = (char) i;
            entry[1] = triplet[0];
            entry[2] = triplet[1];
            entry[3] = triplet[2];
            compressedTripletTable[tripletCount] = entry;
            tripletCount++;
          }
        } /* for i */

        f.write("  // Number of tripletMappings is " + tripletLength + "\n");
        if (tripletLength < MAX_SURROGATE_LENGTH) {
          f.write(
              "  private static final char[][] toUnicodeTripletMappings = { \n");
          System.out.print("Writing triplet table for conversion from " + ccsid
              + " to 13488... to " + fName + "\n");
          for (int i = 0; i < compressedTripletTable.length; i++) {
            char[] entry = compressedTripletTable[i];
            if (entry != null) {
              f.write("{'" + formattedChar((char) entry[0]) + "','"
                  + formattedChar(entry[1]) + "','" + formattedChar(entry[2])
                  + "','" + formattedChar(entry[3]) + "'},\n");
            }
          } /* for i */
          f.write("};\n");
          f.write("\n");
          f.write("\n");
        } else {
          // We must break into pieces
          f.write(
              "  private static char[][] toUnicodeTripletMappings = new char["
                  + tripletLength + "][];\n");
          int startIndex = 0;
          while (startIndex < tripletLength) {
            f.write("  private static void initToUnicodeTripletMappings"
                + startIndex + "() { \n");
            f.write("  char[][] toUnicodeTripletMappingsPiece = {\n");
            for (int i = 0; i < MAX_SURROGATE_LENGTH
                && (i + startIndex < tripletLength); i++) {
              char[] entry = compressedTripletTable[startIndex + i];
              if (entry != null) {
                f.write("    {'" + formattedChar((char) (entry[0])) + "','"
                    + formattedChar(entry[1]) + "','" + formattedChar(entry[2])
                    + "','" + formattedChar(entry[3]) + "'},\n");
              }
            } /* for i */
            f.write("  };\n");

            f.write(
                "    for (int j = 0; j < toUnicodeTripletMappingsPiece.length ; j++) {\n");
            f.write("      toUnicodeTripletMappings[" + startIndex
                + "+j]= new char[3];\n");
            f.write("      toUnicodeTripletMappings[" + startIndex
                + "+j][0] = toUnicodeTripletMappingsPiece[j][0];\n");
            f.write("      toUnicodeTripletMappings[" + startIndex
                + "+j][1] = toUnicodeTripletMappingsPiece[j][1];\n");
            f.write("      toUnicodeTripletMappings[" + startIndex
                + "+j][2] = toUnicodeTripletMappingsPiece[j][2];\n");
            f.write("    }\n");
            f.write("  }\n");
            f.write("\n");
            surrogateInitStringBuffer.append(
                "   initToUnicodeTripletMappings" + startIndex + "();\n");

            startIndex += MAX_SURROGATE_LENGTH;
          }
        }
      } /* if triplet table */

      // Write out the quadTable if it exists
      int quadLength = 0;
      if (quadTable != null) {

        f.write("\n");

        for (int i = 0; i < quadTable.length; i++) {
          char[] pair = quadTable[i];
          if (pair != null) {
            quadLength++;
          }
        } /* for i */

        int quadCount = 0;
        char[][] compressedquadTable = new char[quadLength][];
        for (int i = 0; i < quadTable.length; i++) {
          char[] quad = quadTable[i];
          if (quad != null) {
            char[] entry = new char[5];
            entry[0] = (char) i;
            entry[1] = quad[0];
            entry[2] = quad[1];
            entry[3] = quad[2];
            entry[4] = quad[3];
            compressedquadTable[quadCount] = entry;
            quadCount++;
          }
        } /* for i */

        f.write("  // Number of quadMappings is " + quadLength + "\n");
        if (quadLength < MAX_SURROGATE_LENGTH) {
          f.write(
              "  private static final char[][] toUnicodeQuadMappings = { \n");
          System.out.print("Writing quad table for conversion from " + ccsid
              + " to 1200... to " + fName + "\n");
          for (int i = 0; i < compressedquadTable.length; i++) {
            char[] entry = compressedquadTable[i];
            if (entry != null) {
              f.write("{'" + formattedChar((char) entry[0]) + "','"
                  + formattedChar(entry[1]) + "','" + formattedChar(entry[2])
                  + "','" + formattedChar(entry[3]) + "','"
                  + formattedChar(entry[4]) + "'},\n");
            }
          } /* for i */
          f.write("};\n");
          f.write("\n");
          f.write("\n");
        } else {
          // We must break into pieces
          f.write("  private static char[][] toUnicodeQuadMappings = new char["
              + quadLength + "][];\n");
          int startIndex = 0;
          while (startIndex < quadLength) {
            f.write("  private static void initToUnicodeQuadMappings"
                + startIndex + "() { \n");
            f.write("  char[][] toUnicodeQuadMappingsPiece = {\n");
            for (int i = 0; i < MAX_SURROGATE_LENGTH
                && (i + startIndex < quadLength); i++) {
              char[] entry = compressedquadTable[startIndex + i];
              if (entry != null) {
                f.write("    {'" + formattedChar((char) (entry[0])) + "','"
                    + formattedChar(entry[1]) + "','" + formattedChar(entry[2])
                    + "','" + formattedChar(entry[3]) + "','"
                    + formattedChar(entry[4]) + "'},\n");
              }
            } /* for i */
            f.write("  };\n");

            f.write(
                "    for (int j = 0; j < toUnicodeQuadMappingsPiece.length ; j++) {\n");
            f.write("      toUnicodeQuadMappings[" + startIndex
                + "+j]= new char[3];\n");
            f.write("      toUnicodeQuadMappings[" + startIndex
                + "+j][0] = toUnicodeQuadMappingsPiece[j][0];\n");
            f.write("      toUnicodeQuadMappings[" + startIndex
                + "+j][1] = toUnicodeQuadMappingsPiece[j][1];\n");
            f.write("      toUnicodeQuadMappings[" + startIndex
                + "+j][2] = toUnicodeQuadMappingsPiece[j][2];\n");
            f.write("      toUnicodeQuadMappings[" + startIndex
                + "+j][3] = toUnicodeQuadMappingsPiece[j][3];\n");
            f.write("    }\n");
            f.write("  }\n");
            f.write("\n");
            surrogateInitStringBuffer
                .append("   initToUnicodeQuadMappings" + startIndex + "();\n");

            startIndex += MAX_SURROGATE_LENGTH;
          }
        }
      } /* if quad table */

      f.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Compress the Unicode table
    if (compress_) {
      System.out
          .println("Compressing 13488->" + ccsid + " conversion table...");
      char[] arr = compress(tableToEbcdic);
      System.out
          .println("Old compression length: " + arr.length + " characters.");
      char[] temparr = compressBetter(tableToEbcdic);
      System.out.println(
          "New compression length: " + temparr.length + " characters.");
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

      System.out.print("Writing table for conversion from 13488 to " + ccsid
          + "... to " + fName + "\n");
      f.write("  private static char[] fromUnicodeArray_; \n");
      f.write("  // fromUnicode length = " + tableToEbcdic.length + "\n");
      if (tableToEbcdic.length < MAX_TO_EBCDIC_LENGTH) {
        f.write("  private static final String fromUnicode_ = \n");
        writeTable(f, tableToEbcdic, 0, tableToEbcdic.length);
        f.write("\n");
      } else {
        int oneFourth = tableToEbcdic.length / 4;
        f.write("  private static final String fromUnicode0_ = \n");
        writeTable(f, tableToEbcdic, 0, oneFourth);
        f.write("\n");
        f.write("  private static final String fromUnicode1_ = \n");
        writeTable(f, tableToEbcdic, oneFourth, oneFourth);
        f.write("\n");
        f.write("  private static final String fromUnicode2_ = \n");
        writeTable(f, tableToEbcdic, 2 * oneFourth, oneFourth);
        f.write("\n");

        f.write("  private static final String fromUnicode3_ = \n");
        writeTable(f, tableToEbcdic, 3 * oneFourth,
            tableToEbcdic.length - 3 * oneFourth);
        f.write("\n");

      }

      f.write("  static {\n");
      f.write("    toUnicodeArray_ = toUnicode_.toCharArray();\n");
      if (tableToEbcdic.length < MAX_TO_EBCDIC_LENGTH) {
        f.write("    fromUnicodeArray_ = fromUnicode_.toCharArray();\n");
      } else {
        /*
         * Note: recent compilers try to optimized and add
         * fromUnicode0_+fromUnicode1_ to the constant pool
         */
        /* Using sb.append disables this optimization */
        f.write("    StringBuffer sb = new StringBuffer(); \n");
        f.write("    sb.append(fromUnicode0_); \n");
        f.write("    sb.append(fromUnicode1_); \n");
        f.write("    sb.append(fromUnicode2_); \n");
        f.write("    sb.append(fromUnicode3_); \n");
        f.write("    fromUnicodeArray_ = sb.toString().toCharArray();\n");
      }
      f.write(surrogateInitStringBuffer.toString());
      f.write("  }\n");

      f.write("\n  ConvTable" + fileCcsid + "()\n  {\n");
      f.write("    super(" + fileCcsid + ", ");
      f.write("toUnicodeArray_, ");
      if (surrogateTable != null) {
        f.write("fromUnicodeArray_,");
        if (quadTable != null) {
          f.write(
              "toUnicodeSurrogateMappings,toUnicodeTripletMappings,toUnicodeQuadMappings);\n");
        } else if (tripletTable != null) {
          f.write("toUnicodeSurrogateMappings,toUnicodeTripletMappings);\n");
        } else {
          f.write("toUnicodeSurrogateMappings,null);\n");
        }
      } else {
        f.write("fromUnicodeArray_);\n");
      }
      f.write("  }\n\n");

      f.write("\n  ConvTable" + fileCcsid + "(int ccsid)\n  {\n");
      f.write("    super(ccsid, ");
      f.write("toUnicodeArray_, ");
      if (surrogateTable != null) {
        f.write("fromUnicodeArray_,");
        if (quadTable != null) {
          f.write(
              "toUnicodeSurrogateMappings,toUnicodeTripletMappings,toUnicodeQuadMappings);\n");
        } else if (tripletTable != null) {
          f.write("toUnicodeSurrogateMappings,toUnicodeTripletMappings);\n");
        } else {
          f.write("toUnicodeSurrogateMappings,null);\n");
        }
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

  private static void writeTable(FileWriter f, char[] table, int start,
      int length) throws IOException {
    for (int i = start; i < start + length; i = i + 16) {
      if (showOffsets_) {
        f.write("/* " + Integer.toHexString(i) + " */ \"");
      } else {
        f.write("    \"");
      }
      for (int j = 0; j < 16 && (i + j) < start + length; ++j) {
        int num = (int) table[i + j]; // these each contain 2 single
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
      if (i + 16 < start + length)
        f.write("\" +\n");
      else
        f.write("\";\n");
    }
  }

  private static boolean verifyRoundTrip(char[] tableToUnicode,
      char[] tableToEbcdic, boolean ebcdicIsDBCS, int ccsid,
      char[][] surrogateTable, char[][] tripletTable, char[][] quadTable) {

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

    System.out.println("Checking round trip from EBCDIC for CCSID " + ccsid);
    boolean passed = true;
    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    StringBuffer sb3 = new StringBuffer();
    char[] char1Buffer = new char[1];
    char[] char1Buffer2 = new char[1];
    char unicodeChars[];

    for (int i = 0; i < tableToUnicode.length; i++) {
      char1Buffer[0] = tableToUnicode[i];
      if ((char1Buffer[0] == 0xd800) && (surrogateTable != null)) {
        unicodeChars = surrogateTable[i];
      } else if ((char1Buffer[0] == 0xd801) && (tripletTable != null)) {
        unicodeChars = tripletTable[i];
      } else if ((char1Buffer[0] == 0xd802) && (quadTable != null)) {
        unicodeChars = quadTable[i];
      } else {
        unicodeChars = char1Buffer;
      }
      if (unicodeChars[0] != 0xfffd) {
        int ebcdicChar = 0;
        if (ebcdicIsDBCS) {
          if (unicodeChars.length == 1) {
            ebcdicChar = 0xFFFF & tableToEbcdic[unicodeChars[0]];
          } else if (unicodeChars.length == 2) {
            ebcdicChar = findEbcdicSurrogate(surrogateTable, unicodeChars);
          } else if (unicodeChars.length == 3) {
            ebcdicChar = findEbcdicTriplet(tripletTable, unicodeChars);
          } else if (unicodeChars.length == 4) {
            ebcdicChar = findEbcdicQuad(quadTable, unicodeChars);
          }
        } else {
          int piece = 0xFFFF & tableToEbcdic[unicodeChars[0] / 2];
          if (unicodeChars[0] % 2 == 0) {
            ebcdicChar = piece >> 8;
          } else {
            ebcdicChar = piece & 0xFF;
          }
        }
        if (i != ebcdicChar) {
          if ((unicodeChars[0] != 0x1a) && (unicodeChars[0] != 0xd800)
              && (unicodeChars[0] != 0xd801)
              && ((ebcdicChar == 0xFEFE) || (ebcdicChar == 0x3F))) {
            sb1.append("Fixing up EBCDIC RoundTrip Failure " + ebcdicPrefix
                + "'" + Integer.toHexString(i) + "'" + " -> UX'"
                + Integer.toHexString(unicodeChars[0]) + "'" + " -> "
                + ebcdicPrefix + "'" + Integer.toHexString(ebcdicChar) + "'\n");
            if (ebcdicIsDBCS) {
              tableToEbcdic[unicodeChars[0]] = (char) i;
            } else {
              int piece = 0xFFFF & tableToEbcdic[unicodeChars[0] / 2];
              if (unicodeChars[0] % 2 == 0) {

                piece = (i << 8) | (piece & 0x00FF);
              } else {
                piece = (piece & 0xFF00) | i;
              }
              tableToEbcdic[unicodeChars[0] / 2] = (char) piece;
            }
            passed = false;
          } else {
            char[] unicodeChars2;
            try {
              char1Buffer2[0] = tableToUnicode[ebcdicChar];
              if (char1Buffer2[0] == 0xd800) {
                unicodeChars2 = surrogateTable[ebcdicChar];
              } else if (char1Buffer2[0] == 0xd801) {
                unicodeChars2 = tripletTable[ebcdicChar];
              } else {
                unicodeChars2 = char1Buffer2;
              }

            } catch (ArrayIndexOutOfBoundsException e) {
              System.out.println("ERROR.. ArrayIndexOutOfBounds");
              System.out.println(
                  "ebcdicChar=0x" + Integer.toHexString((int) ebcdicChar));
              System.out.println("i=" + i);
              System.out.println("unicodeChar=0x"
                  + Integer.toHexString((int) unicodeChars[0]));
              throw e;
            }
            boolean matches = true;
            if (unicodeChars2.length != unicodeChars.length) {
              matches = false;
            } else {
              for (int j = 0; j < unicodeChars.length; j++) {
                if (unicodeChars[j] != unicodeChars2[j]) {
                  matches = false;
                }
              }
            }
            if (matches) {
              sb2.append("Secondary EBCDIC mapping " + ebcdicPrefix + "'"
                  + Integer.toHexString(i) + "'" + " -> UX'");
              for (int j = 0; j < unicodeChars.length; j++) {
                sb2.append(Integer.toHexString(unicodeChars[j]) + ".");
              }
              sb2.append("'" + " -> " + ebcdicPrefix + "'"
                  + Integer.toHexString(ebcdicChar) + "'" + " -> UX'");
              for (int j = 0; j < unicodeChars2.length; j++) {
                sb2.append(Integer.toHexString(unicodeChars2[j]) + ".");
              }
              sb2.append("'\n");

            } else {
              sb3.append("EBCDIC RoundTrip Failure 2 " + ebcdicPrefix + "'"
                  + Integer.toHexString(i) + "'" + " -> UX'");
              for (int j = 0; j < unicodeChars.length; j++) {
                sb3.append(Integer.toHexString(unicodeChars[j]) + ".");
              }
              sb3.append("'" + " -> " + ebcdicPrefix + "'"
                  + Integer.toHexString(ebcdicChar) + "'" + " -> UX'");
              for (int j = 0; j < unicodeChars2.length; j++) {
                sb3.append(Integer.toHexString(unicodeChars2[j]) + ".");
              }
              sb3.append("'\n");
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

        char1Buffer[0] = tableToUnicode[ebcdicChar];
        if (char1Buffer[0] == 0xd800) {
          unicodeChars = surrogateTable[ebcdicChar];
        } else if (char1Buffer[0] == 0xd801) {
          unicodeChars = tripletTable[ebcdicChar];
        } else {
          unicodeChars = char1Buffer;
        }

        if (i != unicodeChars[0]) {
          if (unicodeChars[0] == 0xFFFD) {
            sb1.append("Unicode RoundTrip Failure UX'" + Integer.toHexString(i)
                + "'" + " -> " + ebcdicPrefix + "'"
                + Integer.toHexString(ebcdicChar) + "'" + " -> UX'");
            for (int j = 0; j < unicodeChars.length; j++) {
              sb1.append(Integer.toHexString(unicodeChars[j]) + ".");
            }
            sb1.append("'\n");

            if (tableToUnicode[ebcdicChar] == 0xfffd) {

              tableToUnicode[ebcdicChar] = (char) i;
              char1Buffer[0] = tableToUnicode[ebcdicChar];
              unicodeChars = char1Buffer;

              sb1.append("Fixed up ................ UX'"
                  + Integer.toHexString(i) + "'" + " -> " + ebcdicPrefix + "'"
                  + Integer.toHexString(ebcdicChar) + "'" + " -> UX'");
              for (int j = 0; j < unicodeChars.length; j++) {
                sb1.append(Integer.toHexString(unicodeChars[j]) + ".");
              }
              sb1.append("'\n");

            }
            passed = false;
          } else {
            int ebcdicChar2 = 0;
            if (ebcdicIsDBCS) {

              if (unicodeChars.length == 1) {
                ebcdicChar2 = 0xFFFF & tableToEbcdic[unicodeChars[0]];
              } else if (unicodeChars.length == 2) {
                ebcdicChar2 = findEbcdicSurrogate(surrogateTable, unicodeChars);
              } else if (unicodeChars.length == 3) {
                ebcdicChar2 = findEbcdicTriplet(tripletTable, unicodeChars);
              }

            } else {
              int piece = 0xFFFF & tableToEbcdic[unicodeChars[0] / 2];
              if (unicodeChars[0] % 2 == 0) {
                ebcdicChar2 = piece >> 8;
              } else {
                ebcdicChar2 = piece & 0xFF;
              }
            }

            if (ebcdicChar2 == ebcdicChar) {
              sb2.append("Secondary Unicode mapping UX'"
                  + Integer.toHexString(i) + "'" + " -> " + ebcdicPrefix + "'"
                  + Integer.toHexString(ebcdicChar) + "'" + " -> UX'");
              for (int j = 0; j < unicodeChars.length; j++) {
                sb2.append(Integer.toHexString(unicodeChars[j]) + ".");
              }
              sb2.append("'" + " -> " + ebcdicPrefix + "'"
                  + Integer.toHexString(ebcdicChar2) + "'\n");

            } else {
              sb3.append("Unicode RoundTrip Failure 2 UX'"
                  + Integer.toHexString(i) + "'" + " -> " + ebcdicPrefix + "'"
                  + Integer.toHexString(ebcdicChar) + "'" + " -> UX'");
              for (int j = 0; j < unicodeChars.length; j++) {
                sb3.append(Integer.toHexString(unicodeChars[j]) + ".");
              }

              sb3.append("'" + " -> " + ebcdicPrefix + "'"
                  + Integer.toHexString(ebcdicChar2) + "'\n");
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

  private static int findEbcdicQuad(char[][] quadTable, char[] unicodeChars) {
    for (int i = 0; i < quadTable.length; i++) {
      if (quadTable[i] != null) {
        if ((quadTable[i][0] == unicodeChars[0])
            && (quadTable[i][1] == unicodeChars[1])
            && (quadTable[i][2] == unicodeChars[2])
            && (quadTable[i][3] == unicodeChars[3])) {
          return i;
        }
      }
    }
    return 0;
  }

  private static int findEbcdicTriplet(char[][] tripletTable,
      char[] unicodeChars) {
    for (int i = 0; i < tripletTable.length; i++) {
      if (tripletTable[i] != null) {
        if ((tripletTable[i][0] == unicodeChars[0])
            && (tripletTable[i][1] == unicodeChars[1])
            && (tripletTable[i][2] == unicodeChars[2])) {
          return i;
        }
      }
    }
    return 0;
  }

  private static int findEbcdicSurrogate(char[][] surrogateTable,
      char[] unicodeChars) {
    for (int i = 0; i < surrogateTable.length; i++) {
      if (surrogateTable[i] != null) {
        if ((surrogateTable[i][0] == unicodeChars[0])
            && (surrogateTable[i][1] == unicodeChars[1])) {
          return i;
        }
      }
    }
    return 0;
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
            // System.out.print("HBNUM is "+Integer.toHexString((int)hbNum)+";
            // ");
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
            if ((0xFFFFFFFFl & hbNum) % 2 == 1) // odd number
            {
              --hbNum; // no point in doing the last char
            }
            // System.out.println("Appending
            // "+Integer.toHexString((int)((char)(hbNum/2))));
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
              char x = (char) (((0x00FF & arr[i + (j * 2)]) * 256)
                  + (0x00FF & arr[i + (j * 2) + 1]));
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
          // System.out.print("found: "+Integer.toHexString((int)arr[i-2])+"
          // ("+Integer.toHexString((int)arr[i-1])+")
          // "+Integer.toHexString((int)firstChar)+" ");
          buf.append(firstChar);
          ++i;
          for (int j = 0; j < hbNum; ++j) {
            char both = arr[i + j];
            char c1 = (char) (highByteMask + ((0xFF00 & both) >>> 8));
            char c2 = (char) (highByteMask + (0x00FF & both));
            buf.append(c1);
            buf.append(c2);
            // System.out.print(Integer.toHexString((int)c1)+"
            // "+Integer.toHexString((int)c2)+" ");
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
          if (i < arr.length) {
            old = arr[i];
          } else {
            old = 0;
          }
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

    f.write(
        "///////////////////////////////////////////////////////////////////////////////\n");
    f.write("//\n");
    f.write("// JTOpen (IBM Toolbox for Java - OSS version)\n");
    f.write("//\n");
    f.write("// Filename:  ConvTable" + ccsid + ".java\n");
    f.write("//\n");
    f.write(
        "// The source code contained herein is licensed under the IBM Public License\n");
    f.write(
        "// Version 1.0, which has been approved by the Open Source Initiative.\n");
    f.write(
        "// Copyright (C) 1997-2016 International Business Machines Corporation and\n");
    f.write("// others.  All rights reserved.\n");
    f.write("//\n");
    f.write("// Generated " + currentDate + " from " + system + "\n");

    StringBuffer sb = new StringBuffer();
    if (compress_ == false)
      sb.append(" -nocompress");
    if (ascii_ == true)
      sb.append(" -ascii");
    if (bidi_ == true)
      sb.append(" -bidi");
    if (showOffsets_ == true)
      sb.append(" -showOffsets");
    if (codePointPerLine_ == true)
      sb.append(" -codePointPerLine");
    if (useJdbc_ == true)
      sb.append(" -useJdbc");
    if (sb.length() > 0) {
      f.write("// Generation Options:" + sb.toString() + "\n");
    }
    f.write("// Using " + jtopenVersion + "\n");
    f.write(
        "///////////////////////////////////////////////////////////////////////////////\n\n");
    f.write("package com.ibm.as400.access;\n\n");
  }

  //
  // Methods for using JDBC to handle the translation tables
  //

  static boolean jdbcIsDBCS(Connection connection, int ccsid)
      throws SQLException {
    boolean isDBCS;

    Statement stmt = connection.createStatement();
    try {
      stmt.executeUpdate("CREATE TABLE QTEMP.GENERATE" + ccsid
          + "(C1 VARCHAR(80) CCSID " + ccsid + ")");
      isDBCS = false;
    } catch (SQLException sqlex) {
      int sqlcode = sqlex.getErrorCode();
      if (sqlcode == -189) { // CCSID not valid
        stmt.executeUpdate("CREATE TABLE QTEMP.GENERATE" + ccsid
            + "(C1 VARGRAPHIC(80) CCSID " + ccsid + ")");
        isDBCS = true;
      } else {
        throw sqlex;
      }
    }

    stmt.close();
    return isDBCS;

  }

  private static char[] jdbcToEbcdic(Connection connection, int ccsid)
      throws Exception {
    if (ccsid > 1000000) {
      ccsid = ccsid - 1000000;
    }
    PreparedStatement ps = connection.prepareStatement(
        "select cast(CAST(CAST(? AS DBCLOB(1M) CCSID 1200) AS CLOB(1M) CCSID "
            + ccsid + ") as BLOB(1M)) from sysibm.sysdummy1");

    char[] allChar65536 = new char[65536];
    for (int i = 0; i < 0xD800; i++) {
      allChar65536[i] = (char) i;
    }
    // Fill in the surrogate range with substitution characters
    for (int i = 0xD800; i < 0xF900; i++) {
      allChar65536[i] = '\u001a';
    }
    for (int i = 0xF900; i < 0x10000; i++) {
      allChar65536[i] = (char) i;
    }

    Clob clob = ((AS400JDBCConnection) connection).createClob();
    clob.setString(1, new String(allChar65536));
    ps.setClob(1, clob);
    ResultSet rs = ps.executeQuery();
    rs.next();
    byte[] byteAnswer = rs.getBytes(1);
    if (byteAnswer.length != 65536) {
      // We must have shift.out shift in combination that we don't use for
      // single byte conversions
      // Remove them.
      byteAnswer = removeDoubleByteEbcdic(byteAnswer);
    }
    rs.close();
    ps.close();

    char[] answer = new char[byteAnswer.length / 2];

    for (int i = 0; i < byteAnswer.length; i += 2) {
      answer[i
          / 2] = (char) ((byteAnswer[i] << 8) | (0xFF & byteAnswer[i + 1]));
    }

    return answer;

  }

  private static byte[] removeDoubleByteEbcdic(byte[] inBytes)
      throws Exception {
    byte[] outBytes = new byte[65536];

    // Just copy the control characters
    for (int i = 0; i < 0x20; i++) {
      outBytes[i] = inBytes[i];
    }
    int toIndex = 0x20;
    boolean singleByte = true;
    for (int i = 0x20; i < inBytes.length; i++) {
      byte b = inBytes[i];
      if (singleByte) {
        if (b == 0x0E) {
          singleByte = false;
        } else if (b == 0x0F) {
          throw new Exception("Illegal 0x0f found in singleByte mode");
        } else {
          if (toIndex >= outBytes.length) {
            throw new Exception("ERROR:  toIndexInvalid");
          }
          outBytes[toIndex] = b;
          toIndex++;
        }
      } else {
        if (b == 0x0F) {
          singleByte = true;
        } else if (b == 0x0e) {
          throw new Exception("Illegal 0x0e found in doubleByte mode");
        } else {
          if (toIndex >= outBytes.length) {
            throw new Exception("ERROR:  toIndexInvalid");
          }
          outBytes[toIndex] = 0x3f;
          toIndex++;
          i++; // Skip extra DB charater
        }
      }

    }
    if (toIndex != 65536) {
      throw new Exception("To index is " + toIndex + " should be 65536");
    }

    return outBytes;

  }

  private static char[] removeSingleByteEbcdic(byte[] inBytes,
      boolean mixedCcsid) throws Exception {
    char[] outChars = new char[65536];

    boolean singleByte = false;
    if (mixedCcsid)
      singleByte = true;
    int toIndex = 0x0;
    for (int i = 0x0; i < inBytes.length; i++) {
      byte b = inBytes[i];
      if (singleByte) {
        if (b == 0x0E) {
          singleByte = false;
        } else if (b == 0x0F) {
          throw new Exception("Illegal 0x0f found in singleByte mode");
        } else {
          outChars[toIndex] = '\uFEFE';
          toIndex++;
        }
      } else {
        if (b == 0x0F && mixedCcsid) {
          singleByte = true;
        } else if (b == 0x0e && mixedCcsid) {
          throw new Exception("Illegal 0x0e found in doubleByte mode");
        } else {
          outChars[toIndex] = (char) ((b << 8) | (0xFF & inBytes[i + 1]));
          toIndex++;
          i++;
        }
      }

    }
    if (toIndex != 65536) {
      throw new Exception("To index is " + toIndex + " should be 65536");
    }

    return outChars;

  }

  
  private static char[] removeSingleByteEbcdicAndSpaces(byte[] inBytes,
      boolean mixedCcsid) throws Exception {
    char[] outChars = new char[65536];

    boolean singleByte = false;
    if (mixedCcsid)
      singleByte = true;
    int toIndex = 0x0;
    for (int i = 0x0; i < inBytes.length; i++) {
      byte b = inBytes[i];
      if (singleByte) {
        if (b == 0x0E) {
          singleByte = false;
        } else if (b == 0x0F) {
          throw new Exception("Illegal 0x0f found in singleByte mode");
        } else {
          outChars[toIndex] = '\uFEFE';
          toIndex++;
          // Skip over next space if there
          if (inBytes[i+1] == 0x40) {
            i++; 
          }
        }
      } else {
        if (b == 0x0F && mixedCcsid) {
          singleByte = true;
          // Skip over the next space if there
          if (inBytes[i+1] == 0x40) {
            i++; 
          }
        } else if (b == 0x0e && mixedCcsid) {
          throw new Exception("Illegal 0x0e found in doubleByte mode");
        } else {
          outChars[toIndex] = (char) ((b << 8) | (0xFF & inBytes[i + 1]));
          toIndex++;
          i++;
        }
      }

    }
    if (toIndex != 65536) {
      throw new Exception("To index is " + toIndex + " should be 65536");
    }

    return outChars;

  }

  
  private static char[] jdbcToUnicode(Connection connection, int ccsid)
      throws SQLException {

    if (ccsid > 1000000) {
      ccsid = ccsid - 1000000;
    }
    PreparedStatement ps = connection.prepareStatement(
        "select cast(CAST(CAST(? AS VARCHAR(256) FOR BIT DATA) AS VARCHAR(256) CCSID "
            + ccsid + ") as VARGRAPHIC(256) CCSID 1200) from sysibm.sysdummy1");
    byte[] all256 = new byte[256];
    for (int i = 0; i < 256; i++) {
      // For 0x0E and 0x0F, they do not translate correctly for mixed CCSID
      // We will fix them up below.
      if (i == 0x0E) {
        all256[i] = (byte) 0x3F;
      } else if (i == 0x0F) {
        all256[i] = (byte) 0x3F;
      } else {
        all256[i] = (byte) i;
      }
    }
    ps.setBytes(1, all256);
    ResultSet rs = ps.executeQuery();
    rs.next();
    String answer = rs.getString(1);
    rs.close();
    ps.close();
    char[] charAnswer = answer.toCharArray();
    // Fix 0x0e/ 0x0F
    charAnswer[0x0e] = '\u000e';
    charAnswer[0x0f] = '\u000f';
    return charAnswer;

  }

  private static char[] jdbcToEbcdicDBCS(Connection connection, int ccsid)
      throws Exception {

    boolean mixedCcsid = false;
    if (ccsid > 2000000) {
      ccsid = ccsid - 2000000;
      mixedCcsid = true;
    }
    PreparedStatement ps;
    try {
      ps = connection.prepareStatement(
          "select cast(CAST(CAST(? AS DBCLOB(1M) CCSID 1200) AS DBCLOB(1M) CCSID "
              + ccsid + ") as BLOB(1M)) from sysibm.sysdummy1");

    } catch (SQLException sqlex) {
      String message = sqlex.toString();
      // If DBCLOB is not valid, try clob
      if (message.indexOf("SQL0189") >= 0) {
        ps = connection.prepareStatement(
            "select cast(CAST(CAST(? AS DBCLOB(1M) CCSID 1200) AS CLOB(1M) CCSID "
                + ccsid + ") as BLOB(1M)) from sysibm.sysdummy1");

      } else {
        throw sqlex;
      }
    }

    char[] answer;

    // Workaround for CCSID 1399 bug
    if (ccsid != 1399) {

      char[] allChar65536 = new char[65536];
      for (int i = 0; i < 0xD800; i++) {
        if (i <= 0x80) {
          allChar65536[i] = (char) '\u001a';
        } else {
          allChar65536[i] = (char) i;
        }
      }
      // Fill in the surrogate range with double width substitution characters
      for (int i = 0xD800; i < 0xE000; i++) {
        allChar65536[i] = '\ufffd';
      }
      for (int i = 0xE000; i < 0x10000; i++) {
        // Don't translate Variation selectors
        if (i >= 0xFE00 && i <= 0xFF0F) {
          allChar65536[i] = '\ufffd';
        } else {
          allChar65536[i] = (char) i;
        }
      }

      Clob clob = ((AS400JDBCConnection) connection).createClob();
      clob.setString(1, new String(allChar65536));
      ps.setClob(1, clob);
      ResultSet rs = ps.executeQuery();
      rs.next();
      byte[] byteAnswer = rs.getBytes(1);

      rs.close();
      ps.close();

      if (byteAnswer.length != 2 * 65536) {
        // We must have shift.out shift.in combinations.
        // We need to remove the single bytes.

        answer = removeSingleByteEbcdic(byteAnswer, mixedCcsid);
      } else {
        answer = new char[byteAnswer.length / 2];
        for (int i = 0; i < byteAnswer.length; i += 2) {
          answer[i
              / 2] = (char) ((byteAnswer[i] << 8) | (0xFF & byteAnswer[i + 1]));
        }

      }
    } else {
      // CCSID 1399 path - Put spaces between to fix problem
      char[] allChar65536 = new char[65536 * 2];
      for (int i = 0; i < 0xD800; i++) {
        if (i <= 0x80) {
          allChar65536[2*i] = (char) '\u001a';
        } else {
          allChar65536[2*i] = (char) i;
        }
        allChar65536[2*i+1]=' '; 
      }
      // Fill in the surrogate range with double width substitution characters
      for (int i = 0xD800; i < 0xE000; i++) {
        allChar65536[2*i] = '\ufffd';
        allChar65536[2*i+1]=' '; 
      }
      for (int i = 0xE000; i < 0x10000; i++) {
        // Don't translate Variation selectors
        if (i >= 0xFE00 && i <= 0xFF0F) {
          allChar65536[2*i] = '\ufffd';
        } else {
          allChar65536[2*i] = (char) i;
        }
        allChar65536[2*i+1]=' '; 
      }

      Clob clob = ((AS400JDBCConnection) connection).createClob();
      clob.setString(1, new String(allChar65536));
      ps.setClob(1, clob);
      ResultSet rs = ps.executeQuery();
      rs.next();
      byte[] byteAnswer = rs.getBytes(1);

      rs.close();
      ps.close();


      answer = removeSingleByteEbcdicAndSpaces(byteAnswer, mixedCcsid);

    }
    return answer;

  }

  private static char[] jdbcToUnicodeSpacesDBCS(Connection connection,
      int ccsid) throws SQLException {

    // The database doesn't allow CLOB CCSID 65535 or BLOB to be converted to
    // CLOB / DBCLOB. We'll need to process this in pieces.
    // The database doesn't allow VARCHAR CCSID 65535 to be convert to GRAPHIC.
    // Need to figure out how to handle that also.

    // Create a huge SQL statement with the necessary literals.
    // For this to work, the job must be changed to the specified CCSID if mixed
    // or the associated CCSID if no double byte

    boolean mixed = false;
    // Start at 0x00 and handle 8192 double byte characters at at time
    // int BLOCKSIZE = 8192;
    int BLOCKSIZE = 1024;

    String sql = "select cast(INTERPRET(CAST(? AS CHAR(" + (BLOCKSIZE * 4)
        + ") FOR BIT DATA) AS GRAPHIC(" + (BLOCKSIZE * 2) + ") CCSID " + ccsid
        + ") as VARGRAPHIC(8200) CCSID 1200) from sysibm.sysdummy1";
    if (ccsid > 2000000) {
      ccsid = ccsid - 2000000;
      mixed = true;
      sql = "select cast(CAST(CAST(? AS VARCHAR(16390) FOR BIT DATA) AS VARCHAR(16390) CCSID "
          + ccsid + ") as VARGRAPHIC(8200) CCSID 1200) from sysibm.sysdummy1";
    }
    PreparedStatement ps = connection.prepareStatement(sql);

    int OUTERLOOP = 65536 / BLOCKSIZE;
    byte[] piece8192;
    if (mixed) {
      piece8192 = new byte[BLOCKSIZE * 4 + 2];
    } else {
      piece8192 = new byte[BLOCKSIZE * 4];
    }
    int offset = 0;
    if (mixed) {
      piece8192[0] = 0x0E;
      piece8192[BLOCKSIZE * 4 + 1] = 0x0F;
      offset = 1;
    }

    StringBuffer sb = new StringBuffer();
    int cp;
    for (int i = 0; i < OUTERLOOP; i++) {
      for (int j = 0; j < BLOCKSIZE; j++) {
        cp = i * BLOCKSIZE + j;
        // Filter out all the 0E/0F for mixed CCSIDS
        // Filter out the lower 0x100 for mixed CCSIDS

        if (mixed) {
          if (cp < 0x100 || (cp / 256) == 0x0E || (cp / 256) == 0x0F
              || (cp % 256) == 0x0E || (cp % 256) == 0x0F) {
            cp = 0xFEFE;
          }
        }
        piece8192[offset + 4 * j] = (byte) (cp / 256);
        piece8192[offset + 4 * j + 1] = (byte) cp;
        // Separate the translated characters with double byte spaces.
        piece8192[offset + 4 * j + 2] = (byte) 0x40;
        piece8192[offset + 4 * j + 3] = (byte) 0x40;
      } /* for j */
      if (offset == 1) {
        piece8192[BLOCKSIZE * 4 + 1] = 0x0F;
      }

      ps.setBytes(1, piece8192);
      ResultSet rs = ps.executeQuery();
      rs.next();
      String answer = rs.getString(1);
      if (answer == null) {
        System.out.println(
            "ERROR: got null processing block " + i + " of size " + BLOCKSIZE);
        System.out.println("INPUT BYTES: = " + dumpBytes(" ", piece8192));
      } else if (answer.length() != BLOCKSIZE) {
        // This is OK since there may be surrogate pairs which will be handled
        // later
        //
        // System.out.println("ERROR: got size = "+answer.length()+" processing
        // block "+i+" of size "+BLOCKSIZE);
        // System.out.println("INPUT BYTES: = "+dumpBytes(" ",piece8192));
        // System.out.println("OUTPUT STRING: = "+dumpUnicodeString(" ",
        // answer));

      }
      sb.append(answer);
      rs.close();

    } /* for i */
    ps.close();

    return sb.toString().toCharArray();
  } /* jdbcToUnicodeDBCS */

  private static String dumpUnicodeString(String pad, String data) {
    StringBuffer sb = new StringBuffer();
    char[] charArray = data.toCharArray();
    for (int i = 0; i < charArray.length; i++) {
      sb.append(pad);
      int value = 0xffff & charArray[i];
      if (value < 0x10)
        sb.append("0");
      if (value < 0x100)
        sb.append("0");
      if (value < 0x1000)
        sb.append("0");
      sb.append(Integer.toHexString(value));
    }

    return sb.toString();

  }

  private static String dumpBytes(String pad, byte[] block) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < block.length; i++) {
      sb.append(pad);
      int value = 0xff & block[i];
      if (value < 0x10)
        sb.append("0");
      sb.append(Integer.toHexString(value));
    }

    return sb.toString();
  }

}
