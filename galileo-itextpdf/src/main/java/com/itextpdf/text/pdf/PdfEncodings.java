/*
 *
 * This file is part of the iText (R) project.
    Copyright (c) 1998-2017 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.HashMap;

import com.itextpdf.text.ExceptionConverter;
/** Supports fast encodings for winansi and PDFDocEncoding.
 * Supports conversions from CJK encodings to CID.
 * Supports custom encodings.
 * @author Paulo Soares
 */
public class PdfEncodings {
    static final char winansiByteToChar[] = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
        32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
        48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
        64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
        80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
        96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
        112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127,
        8364, 65533, 8218, 402, 8222, 8230, 8224, 8225, 710, 8240, 352, 8249, 338, 65533, 381, 65533,
        65533, 8216, 8217, 8220, 8221, 8226, 8211, 8212, 732, 8482, 353, 8250, 339, 65533, 382, 376,
        160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175,
        176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191,
        192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207,
        208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223,
        224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239,
        240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255};

    static final char pdfEncodingByteToChar[] = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
        32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
        48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
        64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
        80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
        96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
        112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127,
        0x2022, 0x2020, 0x2021, 0x2026, 0x2014, 0x2013, 0x0192, 0x2044, 0x2039, 0x203a, 0x2212, 0x2030, 0x201e, 0x201c, 0x201d, 0x2018,
        0x2019, 0x201a, 0x2122, 0xfb01, 0xfb02, 0x0141, 0x0152, 0x0160, 0x0178, 0x017d, 0x0131, 0x0142, 0x0153, 0x0161, 0x017e, 65533,
        0x20ac, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175,
        176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191,
        192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207,
        208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223,
        224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239,
        240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255};

    static final IntHashtable winansi = new IntHashtable();

    static final IntHashtable pdfEncoding = new IntHashtable();

    static HashMap<String, ExtraEncoding> extraEncodings = new HashMap<String, ExtraEncoding>();

    static {
        for (int k = 128; k < 161; ++k) {
            char c = winansiByteToChar[k];
            if (c != 65533)
                winansi.put(c, k);
        }

        for (int k = 128; k < 161; ++k) {
            char c = pdfEncodingByteToChar[k];
            if (c != 65533)
                pdfEncoding.put(c, k);
        }

        addExtraEncoding("Wingdings", new WingdingsConversion());
        addExtraEncoding("Symbol", new SymbolConversion(true));
        addExtraEncoding("ZapfDingbats", new SymbolConversion(false));
        addExtraEncoding("SymbolTT", new SymbolTTConversion());
        addExtraEncoding("Cp437", new Cp437Conversion());
    }

    /** Converts a <CODE>String</CODE> to a </CODE>byte</CODE> array according
     * to the font's encoding.
     * @return an array of <CODE>byte</CODE> representing the conversion according to the font's encoding
     * @param encoding the encoding
     * @param text the <CODE>String</CODE> to be converted
     */
    public static final byte[] convertToBytes(String text, String encoding) {
        if (text == null)
            return new byte[0];
        if (encoding == null || encoding.length() == 0) {
            int len = text.length();
            byte b[] = new byte[len];
            for (int k = 0; k < len; ++k)
                b[k] = (byte)text.charAt(k);
            return b;
        }
        ExtraEncoding extra = extraEncodings.get(encoding.toLowerCase());
        if (extra != null) {
            byte b[] = extra.charToByte(text, encoding);
            if (b != null)
                return b;
        }
        IntHashtable hash = null;
        if (encoding.equals(BaseFont.WINANSI))
            hash = winansi;
        else if (encoding.equals(PdfObject.TEXT_PDFDOCENCODING))
            hash = pdfEncoding;
        if (hash != null) {
            char cc[] = text.toCharArray();
            int len = cc.length;
            int ptr = 0;
            byte b[] = new byte[len];
            int c = 0;
            for (int k = 0; k < len; ++k) {
                char char1 = cc[k];
                if (char1 < 128 || char1 > 160 && char1 <= 255)
                    c = char1;
                else
                    c = hash.get(char1);
                if (c != 0)
                    b[ptr++] = (byte)c;
            }
            if (ptr == len)
                return b;
            byte b2[] = new byte[ptr];
            System.arraycopy(b, 0, b2, 0, ptr);
            return b2;
        }
        if (encoding.equals(PdfObject.TEXT_UNICODE)) {
            // workaround for jdk 1.2.2 bug
            char cc[] = text.toCharArray();
            int len = cc.length;
            byte b[] = new byte[cc.length * 2 + 2];
            b[0] = -2;
            b[1] = -1;
            int bptr = 2;
            for (int k = 0; k < len; ++k) {
                char c = cc[k];
                b[bptr++] = (byte)(c >> 8);
                b[bptr++] = (byte)(c & 0xff);
            }
            return b;
        }
        try {
            Charset cc = Charset.forName(encoding);
            CharsetEncoder ce = cc.newEncoder();
            ce.onUnmappableCharacter(CodingErrorAction.IGNORE);
            CharBuffer cb = CharBuffer.wrap(text.toCharArray());
            java.nio.ByteBuffer bb = ce.encode(cb);
            bb.rewind();
            int lim = bb.limit();
            byte[] br = new byte[lim];
            bb.get(br);
            return br;
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /** Converts a <CODE>String</CODE> to a </CODE>byte</CODE> array according
     * to the font's encoding.
     * @return an array of <CODE>byte</CODE> representing the conversion according to the font's encoding
     * @param encoding the encoding
     * @param char1 the <CODE>char</CODE> to be converted
     */
    public static final byte[] convertToBytes(char char1, String encoding) {
        if (encoding == null || encoding.length() == 0)
            return new byte[]{(byte)char1};
        ExtraEncoding extra = extraEncodings.get(encoding.toLowerCase());
        if (extra != null) {
            byte b[] = extra.charToByte(char1, encoding);
            if (b != null)
                return b;
        }
        IntHashtable hash = null;
        if (encoding.equals(BaseFont.WINANSI))
            hash = winansi;
        else if (encoding.equals(PdfObject.TEXT_PDFDOCENCODING))
            hash = pdfEncoding;
        if (hash != null) {
            int c = 0;
            if (char1 < 128 || char1 > 160 && char1 <= 255)
                c = char1;
            else
                c = hash.get(char1);
            if (c != 0)
                return new byte[]{(byte)c};
            else
                return new byte[0];
        }
        if (encoding.equals(PdfObject.TEXT_UNICODE)) {
            // workaround for jdk 1.2.2 bug
            byte b[] = new byte[4];
            b[0] = -2;
            b[1] = -1;
            b[2] = (byte)(char1 >> 8);
            b[3] = (byte)(char1 & 0xff);
            return b;
        }
        try {
            Charset cc = Charset.forName(encoding);
            CharsetEncoder ce = cc.newEncoder();
            ce.onUnmappableCharacter(CodingErrorAction.IGNORE);
            CharBuffer cb = CharBuffer.wrap(new char[]{char1});
            java.nio.ByteBuffer bb = ce.encode(cb);
            bb.rewind();
            int lim = bb.limit();
            byte[] br = new byte[lim];
            bb.get(br);
            return br;
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /** Converts a </CODE>byte</CODE> array to a <CODE>String</CODE> according
     * to the some encoding.
     * @param bytes the bytes to convert
     * @param encoding the encoding
     * @return the converted <CODE>String</CODE>
     */
    public static final String convertToString(byte bytes[], String encoding) {
        if (bytes == null)
            return PdfObject.NOTHING;
        if (encoding == null || encoding.length() == 0) {
            char c[] = new char[bytes.length];
            for (int k = 0; k < bytes.length; ++k)
                c[k] = (char)(bytes[k] & 0xff);
            return new String(c);
        }
        ExtraEncoding extra = extraEncodings.get(encoding.toLowerCase());
        if (extra != null) {
            String text = extra.byteToChar(bytes, encoding);
            if (text != null)
                return text;
        }
        char ch[] = null;
        if (encoding.equals(BaseFont.WINANSI))
            ch = winansiByteToChar;
        else if (encoding.equals(PdfObject.TEXT_PDFDOCENCODING))
            ch = pdfEncodingByteToChar;
        if (ch != null) {
            int len = bytes.length;
            char c[] = new char[len];
            for (int k = 0; k < len; ++k) {
                c[k] = ch[bytes[k] & 0xff];
            }
            return new String(c);
        }
        try {
            return new String(bytes, encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new ExceptionConverter(e);
        }
    }

    /** Checks is <CODE>text</CODE> only has PdfDocEncoding characters.
     * @param text the <CODE>String</CODE> to test
     * @return <CODE>true</CODE> if only PdfDocEncoding characters are present
     */
    public static boolean isPdfDocEncoding(String text) {
        if (text == null)
            return true;
        int len = text.length();
        for (int k = 0; k < len; ++k) {
            char char1 = text.charAt(k);
            if (char1 < 128 || char1 > 160 && char1 <= 255)
                continue;
            if (!pdfEncoding.containsKey(char1))
                return false;
        }
        return true;
    }

    /** Adds an extra encoding.
     * @param name the name of the encoding. The encoding recognition is case insensitive
     * @param enc the conversion class
     */
    @SuppressWarnings("unchecked")
    public static void addExtraEncoding(String name, ExtraEncoding enc) {
        synchronized (extraEncodings) { // This serializes concurrent updates
            HashMap<String, ExtraEncoding> newEncodings = (HashMap<String, ExtraEncoding>)extraEncodings.clone();
            newEncodings.put(name.toLowerCase(), enc);
            extraEncodings = newEncodings;  // This swap does not require synchronization with reader
        }
    }

    private static class WingdingsConversion implements ExtraEncoding {

        public byte[] charToByte(char char1, String encoding) {
            if (char1 == ' ')
                return new byte[]{(byte)char1};
            else if (char1 >= '\u2701' && char1 <= '\u27BE') {
                byte v = table[char1 - 0x2700];
                if (v != 0)
                    return new byte[]{v};
            }
            return new byte[0];
        }

        public byte[] charToByte(String text, String encoding) {
            char cc[] = text.toCharArray();
            byte b[] = new byte[cc.length];
            int ptr = 0;
            int len = cc.length;
            for (int k = 0; k < len; ++k) {
                char c = cc[k];
                if (c == ' ')
                    b[ptr++] = (byte)c;
                else if (c >= '\u2701' && c <= '\u27BE') {
                    byte v = table[c - 0x2700];
                    if (v != 0)
                        b[ptr++] = v;
                }
            }
            if (ptr == len)
                return b;
            byte b2[] = new byte[ptr];
            System.arraycopy(b, 0, b2, 0, ptr);
            return b2;
        }

        public String byteToChar(byte[] b, String encoding) {
            return null;
        }

        private final static byte table[] = {
            0, 35, 34, 0, 0, 0, 41, 62, 81, 42,
            0, 0, 65, 63, 0, 0, 0, 0, 0, -4,
            0, 0, 0, -5, 0, 0, 0, 0, 0, 0,
            86, 0, 88, 89, 0, 0, 0, 0, 0, 0,
            0, 0, -75, 0, 0, 0, 0, 0, -74, 0,
            0, 0, -83, -81, -84, 0, 0, 0, 0, 0,
            0, 0, 0, 124, 123, 0, 0, 0, 84, 0,
            0, 0, 0, 0, 0, 0, 0, -90, 0, 0,
            0, 113, 114, 0, 0, 0, 117, 0, 0, 0,
            0, 0, 0, 125, 126, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, -116, -115,
            -114, -113, -112, -111, -110, -109, -108, -107, -127, -126,
            -125, -124, -123, -122, -121, -120, -119, -118, -116, -115,
            -114, -113, -112, -111, -110, -109, -108, -107, -24, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, -24, -40, 0, 0, -60, -58, 0, 0, -16,
            0, 0, 0, 0, 0, 0, 0, 0, 0, -36,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0
        };
    }

    private static class Cp437Conversion implements ExtraEncoding {
        private static IntHashtable c2b = new IntHashtable();

        public byte[] charToByte(String text, String encoding) {
            char cc[] = text.toCharArray();
            byte b[] = new byte[cc.length];
            int ptr = 0;
            int len = cc.length;
            for (int k = 0; k < len; ++k) {
                char c = cc[k];
                if (c < 128)
                    b[ptr++] = (byte)c;
                else {
                    byte v = (byte)c2b.get(c);
                    if (v != 0)
                        b[ptr++] = v;
                }
            }
            if (ptr == len)
                return b;
            byte b2[] = new byte[ptr];
            System.arraycopy(b, 0, b2, 0, ptr);
            return b2;
        }

        public byte[] charToByte(char char1, String encoding) {
            if (char1 < 128)
                return new byte[]{(byte)char1};
            else {
                byte v = (byte)c2b.get(char1);
                if (v != 0)
                    return new byte[]{v};
                else
                    return new byte[0];
            }
        }

        public String byteToChar(byte[] b, String encoding) {
            int len = b.length;
            char cc[] = new char[len];
            int ptr = 0;
            for (int k = 0; k < len; ++k) {
                int c = b[k] & 0xff;
                if (c < ' ')
                    continue;
                if (c < 128)
                    cc[ptr++] = (char)c;
                else {
                    char v = table[c - 128];
                    cc[ptr++] = v;
                }
            }
            return new String(cc, 0, ptr);
        }

        private final static char table[] = {
            '\u00C7', '\u00FC', '\u00E9', '\u00E2', '\u00E4', '\u00E0', '\u00E5', '\u00E7', '\u00EA', '\u00EB', '\u00E8', '\u00EF', '\u00EE', '\u00EC', '\u00C4', '\u00C5',
            '\u00C9', '\u00E6', '\u00C6', '\u00F4', '\u00F6', '\u00F2', '\u00FB', '\u00F9', '\u00FF', '\u00D6', '\u00DC', '\u00A2', '\u00A3', '\u00A5', '\u20A7', '\u0192',
            '\u00E1', '\u00ED', '\u00F3', '\u00FA', '\u00F1', '\u00D1', '\u00AA', '\u00BA', '\u00BF', '\u2310', '\u00AC', '\u00BD', '\u00BC', '\u00A1', '\u00AB', '\u00BB',
            '\u2591', '\u2592', '\u2593', '\u2502', '\u2524', '\u2561', '\u2562', '\u2556', '\u2555', '\u2563', '\u2551', '\u2557', '\u255D', '\u255C', '\u255B', '\u2510',
            '\u2514', '\u2534', '\u252C', '\u251C', '\u2500', '\u253C', '\u255E', '\u255F', '\u255A', '\u2554', '\u2569', '\u2566', '\u2560', '\u2550', '\u256C', '\u2567',
            '\u2568', '\u2564', '\u2565', '\u2559', '\u2558', '\u2552', '\u2553', '\u256B', '\u256A', '\u2518', '\u250C', '\u2588', '\u2584', '\u258C', '\u2590', '\u2580',
            '\u03B1', '\u00DF', '\u0393', '\u03C0', '\u03A3', '\u03C3', '\u00B5', '\u03C4', '\u03A6', '\u0398', '\u03A9', '\u03B4', '\u221E', '\u03C6', '\u03B5', '\u2229',
            '\u2261', '\u00B1', '\u2265', '\u2264', '\u2320', '\u2321', '\u00F7', '\u2248', '\u00B0', '\u2219', '\u00B7', '\u221A', '\u207F', '\u00B2', '\u25A0', '\u00A0'
        };

        static {
            for (int k = 0; k < table.length; ++k)
                c2b.put(table[k], k + 128);
        }
    }

    private static class SymbolConversion implements ExtraEncoding {

        private static final IntHashtable t1 = new IntHashtable();
        private static final IntHashtable t2 = new IntHashtable();
        private IntHashtable translation;
        private final char[] byteToChar;

        SymbolConversion(boolean symbol) {
            if (symbol) {
                translation = t1;
                byteToChar = table1;
            } else {
                translation = t2;
                byteToChar = table2;
            }
        }

        public byte[] charToByte(String text, String encoding) {
            char cc[] = text.toCharArray();
            byte b[] = new byte[cc.length];
            int ptr = 0;
            int len = cc.length;
            for (int k = 0; k < len; ++k) {
                char c = cc[k];
                byte v = (byte)translation.get(c);
                if (v != 0)
                    b[ptr++] = v;
            }
            if (ptr == len)
                return b;
            byte b2[] = new byte[ptr];
            System.arraycopy(b, 0, b2, 0, ptr);
            return b2;
        }

        public byte[] charToByte(char char1, String encoding) {
            byte v = (byte)translation.get(char1);
            if (v != 0)
                return new byte[]{v};
            else
                return new byte[0];
        }

        public String byteToChar(byte[] b, String encoding) {
            int len = b.length;
            char cc[] = new char[len];
            int ptr = 0;
            for (int k = 0; k < len; ++k) {
                int c = b[k] & 0xff;
                char v = byteToChar[c];
                cc[ptr++] = v;
            }
            return new String(cc, 0, ptr);
        }

        private final static char table1[] = {
            '\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0',
            '\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0',
            ' ','!','\u2200','#','\u2203','%','&','\u220b','(',')','*','+',',','-','.','/',
            '0','1','2','3','4','5','6','7','8','9',':',';','<','=','>','?',
            '\u2245','\u0391','\u0392','\u03a7','\u0394','\u0395','\u03a6','\u0393','\u0397','\u0399','\u03d1','\u039a','\u039b','\u039c','\u039d','\u039f',
            '\u03a0','\u0398','\u03a1','\u03a3','\u03a4','\u03a5','\u03c2','\u03a9','\u039e','\u03a8','\u0396','[','\u2234',']','\u22a5','_',
            '\u0305','\u03b1','\u03b2','\u03c7','\u03b4','\u03b5','\u03d5','\u03b3','\u03b7','\u03b9','\u03c6','\u03ba','\u03bb','\u03bc','\u03bd','\u03bf',
            '\u03c0','\u03b8','\u03c1','\u03c3','\u03c4','\u03c5','\u03d6','\u03c9','\u03be','\u03c8','\u03b6','{','|','}','~','\0',
            '\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0',
            '\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0',
            '\u20ac','\u03d2','\u2032','\u2264','\u2044','\u221e','\u0192','\u2663','\u2666','\u2665','\u2660','\u2194','\u2190','\u2191','\u2192','\u2193',
            '\u00b0','\u00b1','\u2033','\u2265','\u00d7','\u221d','\u2202','\u2022','\u00f7','\u2260','\u2261','\u2248','\u2026','\u2502','\u2500','\u21b5',
            '\u2135','\u2111','\u211c','\u2118','\u2297','\u2295','\u2205','\u2229','\u222a','\u2283','\u2287','\u2284','\u2282','\u2286','\u2208','\u2209',
            '\u2220','\u2207','\u00ae','\u00a9','\u2122','\u220f','\u221a','\u22c5','\u00ac','\u2227','\u2228','\u21d4','\u21d0','\u21d1','\u21d2','\u21d3',
            '\u25ca','\u2329','\0','\0','\0','\u2211','\u239b','\u239c','\u239d','\u23a1','\u23a2','\u23a3','\u23a7','\u23a8','\u23a9','\u23aa',
            '\0','\u232a','\u222b','\u2320','\u23ae','\u2321','\u239e','\u239f','\u23a0','\u23a4','\u23a5','\u23a6','\u23ab','\u23ac','\u23ad','\0'
        };

        private final static char table2[] = {
            '\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0',
            '\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0',
            '\u0020','\u2701','\u2702','\u2703','\u2704','\u260e','\u2706','\u2707','\u2708','\u2709','\u261b','\u261e','\u270C','\u270D','\u270E','\u270F',
            '\u2710','\u2711','\u2712','\u2713','\u2714','\u2715','\u2716','\u2717','\u2718','\u2719','\u271A','\u271B','\u271C','\u271D','\u271E','\u271F',
            '\u2720','\u2721','\u2722','\u2723','\u2724','\u2725','\u2726','\u2727','\u2605','\u2729','\u272A','\u272B','\u272C','\u272D','\u272E','\u272F',
            '\u2730','\u2731','\u2732','\u2733','\u2734','\u2735','\u2736','\u2737','\u2738','\u2739','\u273A','\u273B','\u273C','\u273D','\u273E','\u273F',
            '\u2740','\u2741','\u2742','\u2743','\u2744','\u2745','\u2746','\u2747','\u2748','\u2749','\u274A','\u274B','\u25cf','\u274D','\u25a0','\u274F',
            '\u2750','\u2751','\u2752','\u25b2','\u25bc','\u25c6','\u2756','\u25d7','\u2758','\u2759','\u275A','\u275B','\u275C','\u275D','\u275E','\u0000',
            '\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0',
            '\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0',
            '\u0000','\u2761','\u2762','\u2763','\u2764','\u2765','\u2766','\u2767','\u2663','\u2666','\u2665','\u2660','\u2460','\u2461','\u2462','\u2463',
            '\u2464','\u2465','\u2466','\u2467','\u2468','\u2469','\u2776','\u2777','\u2778','\u2779','\u277A','\u277B','\u277C','\u277D','\u277E','\u277F',
            '\u2780','\u2781','\u2782','\u2783','\u2784','\u2785','\u2786','\u2787','\u2788','\u2789','\u278A','\u278B','\u278C','\u278D','\u278E','\u278F',
            '\u2790','\u2791','\u2792','\u2793','\u2794','\u2192','\u2194','\u2195','\u2798','\u2799','\u279A','\u279B','\u279C','\u279D','\u279E','\u279F',
            '\u27A0','\u27A1','\u27A2','\u27A3','\u27A4','\u27A5','\u27A6','\u27A7','\u27A8','\u27A9','\u27AA','\u27AB','\u27AC','\u27AD','\u27AE','\u27AF',
            '\u0000','\u27B1','\u27B2','\u27B3','\u27B4','\u27B5','\u27B6','\u27B7','\u27B8','\u27B9','\u27BA','\u27BB','\u27BC','\u27BD','\u27BE','\u0000'
        };

        static {
            for (int k = 0; k < 256; ++k) {
                int v = table1[k];
                if (v != 0)
                    t1.put(v, k);
            }
            for (int k = 0; k < 256; ++k) {
                int v = table2[k];
                if (v != 0)
                    t2.put(v, k);
            }
        }
    }

    private static class SymbolTTConversion implements ExtraEncoding {

        public byte[] charToByte(char char1, String encoding) {
            if ((char1 & 0xff00) == 0 || (char1 & 0xff00) == 0xf000)
                return new byte[]{(byte)char1};
            else
                return new byte[0];
        }

        public byte[] charToByte(String text, String encoding) {
            char ch[] = text.toCharArray();
            byte b[] = new byte[ch.length];
            int ptr = 0;
            int len = ch.length;
            for (int k = 0; k < len; ++k) {
                char c = ch[k];
                if ((c & 0xff00) == 0 || (c & 0xff00) == 0xf000)
                    b[ptr++] = (byte)c;
            }
            if (ptr == len)
                return b;
            byte b2[] = new byte[ptr];
            System.arraycopy(b, 0, b2, 0, ptr);
            return b2;
        }

        public String byteToChar(byte[] b, String encoding) {
            return null;
        }

    }
}
