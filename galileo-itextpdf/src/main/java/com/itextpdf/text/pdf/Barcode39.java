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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.error_messages.MessageLocalization;

/** Implements the code 39 and code 39 extended. The default parameters are:
 * <pre>
 *x = 0.8f;
 *n = 2;
 *font = BaseFont.createFont("Helvetica", "winansi", false);
 *size = 8;
 *baseline = size;
 *barHeight = size * 3;
 *textAlignment = Element.ALIGN_CENTER;
 *generateChecksum = false;
 *checksumText = false;
 *startStopText = true;
 *extended = false;
 * </pre>
 *
 * @author Paulo Soares
 */
public class Barcode39 extends Barcode{

    /** The bars to generate the code.
     */    
    private static final byte BARS[][] = 
    {
        {0,0,0,1,1,0,1,0,0},
        {1,0,0,1,0,0,0,0,1},
        {0,0,1,1,0,0,0,0,1},
        {1,0,1,1,0,0,0,0,0},
        {0,0,0,1,1,0,0,0,1},
        {1,0,0,1,1,0,0,0,0},
        {0,0,1,1,1,0,0,0,0},
        {0,0,0,1,0,0,1,0,1},
        {1,0,0,1,0,0,1,0,0},
        {0,0,1,1,0,0,1,0,0},
        {1,0,0,0,0,1,0,0,1},
        {0,0,1,0,0,1,0,0,1},
        {1,0,1,0,0,1,0,0,0},
        {0,0,0,0,1,1,0,0,1},
        {1,0,0,0,1,1,0,0,0},
        {0,0,1,0,1,1,0,0,0},
        {0,0,0,0,0,1,1,0,1},
        {1,0,0,0,0,1,1,0,0},
        {0,0,1,0,0,1,1,0,0},
        {0,0,0,0,1,1,1,0,0},
        {1,0,0,0,0,0,0,1,1},
        {0,0,1,0,0,0,0,1,1},
        {1,0,1,0,0,0,0,1,0},
        {0,0,0,0,1,0,0,1,1},
        {1,0,0,0,1,0,0,1,0},
        {0,0,1,0,1,0,0,1,0},
        {0,0,0,0,0,0,1,1,1},
        {1,0,0,0,0,0,1,1,0},
        {0,0,1,0,0,0,1,1,0},
        {0,0,0,0,1,0,1,1,0},
        {1,1,0,0,0,0,0,0,1},
        {0,1,1,0,0,0,0,0,1},
        {1,1,1,0,0,0,0,0,0},
        {0,1,0,0,1,0,0,0,1},
        {1,1,0,0,1,0,0,0,0},
        {0,1,1,0,1,0,0,0,0},
        {0,1,0,0,0,0,1,0,1},
        {1,1,0,0,0,0,1,0,0},
        {0,1,1,0,0,0,1,0,0},
        {0,1,0,1,0,1,0,0,0},
        {0,1,0,1,0,0,0,1,0},
        {0,1,0,0,0,1,0,1,0},
        {0,0,0,1,0,1,0,1,0},
        {0,1,0,0,1,0,1,0,0}
    };
 
    /** The index chars to <CODE>BARS</CODE>.
     */    
    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%*";
    
    /** The character combinations to make the code 39 extended.
     */    
    private static final String EXTENDED = "%U" +
        "$A$B$C$D$E$F$G$H$I$J$K$L$M$N$O$P$Q$R$S$T$U$V$W$X$Y$Z" +
        "%A%B%C%D%E  /A/B/C/D/E/F/G/H/I/J/K/L - ./O" +
        " 0 1 2 3 4 5 6 7 8 9/Z%F%G%H%I%J%V" +
        " A B C D E F G H I J K L M N O P Q R S T U V W X Y Z" +
        "%K%L%M%N%O%W" +
        "+A+B+C+D+E+F+G+H+I+J+K+L+M+N+O+P+Q+R+S+T+U+V+W+X+Y+Z" +
        "%P%Q%R%S%T";
        
    /** Creates a new Barcode39.
     */    
    public Barcode39() {
        try {
            x = 0.8f;
            n = 2;
            font = BaseFont.createFont("Helvetica", "winansi", false);
            size = 8;
            baseline = size;
            barHeight = size * 3;
            textAlignment = Element.ALIGN_CENTER;
            generateChecksum = false;
            checksumText = false;
            startStopText = true;
            extended = false;
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    /** Creates the bars.
     * @param text the text to create the bars. This text does not include the start and
     * stop characters
     * @return the bars
     */    
    public static byte[] getBarsCode39(String text) {
        text = "*" + text + "*";
        byte bars[] = new byte[text.length() * 10 - 1];
        for (int k = 0; k < text.length(); ++k) {
            int idx = CHARS.indexOf(text.charAt(k));
            if (idx < 0)
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.character.1.is.illegal.in.code.39", text.charAt(k)));
            System.arraycopy(BARS[idx], 0, bars, k * 10, 9);
        }
        return bars;
    }
    
    /** Converts the extended text into a normal, escaped text,
     * ready to generate bars.
     * @param text the extended text
     * @return the escaped text
     */    
    public static String getCode39Ex(String text) {
        StringBuilder out = new StringBuilder("");
        for (int k = 0; k < text.length(); ++k) {
            char c = text.charAt(k);
            if (c > 127)
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.character.1.is.illegal.in.code.39.extended", c));
            char c1 = EXTENDED.charAt(c * 2);
            char c2 = EXTENDED.charAt(c * 2 + 1);
            if (c1 != ' ')
                out.append(c1);
            out.append(c2);
        }
        return out.toString();
    }
    
    /** Calculates the checksum.
     * @param text the text
     * @return the checksum
     */    
    static char getChecksum(String text) {
        int chk = 0;
        for (int k = 0; k < text.length(); ++k) {
            int idx = CHARS.indexOf(text.charAt(k));
            if (idx < 0)
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.character.1.is.illegal.in.code.39", text.charAt(k)));
            chk += idx;
        }
        return CHARS.charAt(chk % 43);
    }
    
    /** Gets the maximum area that the barcode and the text, if
     * any, will occupy. The lower left corner is always (0, 0).
     * @return the size the barcode occupies.
     */    
    public Rectangle getBarcodeSize() {
        float fontX = 0;
        float fontY = 0;
        String fCode = code;
        if (extended)
            fCode = getCode39Ex(code);
        if (font != null) {
            if (baseline > 0)
                fontY = baseline - font.getFontDescriptor(BaseFont.DESCENT, size);
            else
                fontY = -baseline + size;
            String fullCode = code;
            if (generateChecksum && checksumText)
                fullCode += getChecksum(fCode);
            if (startStopText)
                fullCode = "*" + fullCode + "*";
            fontX = font.getWidthPoint(altText != null ? altText : fullCode, size);
        }
        int len = fCode.length() + 2;
        if (generateChecksum)
            ++len;
        float fullWidth = len * (6 * x + 3 * x * n) + (len - 1) * x;
        fullWidth = Math.max(fullWidth, fontX);
        float fullHeight = barHeight + fontY;
        return new Rectangle(fullWidth, fullHeight);
    }
    
    /** Places the barcode in a <CODE>PdfContentByte</CODE>. The
     * barcode is always placed at coordinates (0, 0). Use the
     * translation matrix to move it elsewhere.<p>
     * The bars and text are written in the following colors:<p>
     * <P><TABLE BORDER=1>
     * <TR>
     *    <TH><P><CODE>barColor</CODE></TH>
     *    <TH><P><CODE>textColor</CODE></TH>
     *    <TH><P>Result</TH>
     *    </TR>
     * <TR>
     *    <TD><P><CODE>null</CODE></TD>
     *    <TD><P><CODE>null</CODE></TD>
     *    <TD><P>bars and text painted with current fill color</TD>
     *    </TR>
     * <TR>
     *    <TD><P><CODE>barColor</CODE></TD>
     *    <TD><P><CODE>null</CODE></TD>
     *    <TD><P>bars and text painted with <CODE>barColor</CODE></TD>
     *    </TR>
     * <TR>
     *    <TD><P><CODE>null</CODE></TD>
     *    <TD><P><CODE>textColor</CODE></TD>
     *    <TD><P>bars painted with current color<br>text painted with <CODE>textColor</CODE></TD>
     *    </TR>
     * <TR>
     *    <TD><P><CODE>barColor</CODE></TD>
     *    <TD><P><CODE>textColor</CODE></TD>
     *    <TD><P>bars painted with <CODE>barColor</CODE><br>text painted with <CODE>textColor</CODE></TD>
     *    </TR>
     * </TABLE>
     * @param cb the <CODE>PdfContentByte</CODE> where the barcode will be placed
     * @param barColor the color of the bars. It can be <CODE>null</CODE>
     * @param textColor the color of the text. It can be <CODE>null</CODE>
     * @return the dimensions the barcode occupies
     */    
    public Rectangle placeBarcode(PdfContentByte cb, BaseColor barColor, BaseColor textColor) {
        String fullCode = code;
        float fontX = 0;
        String bCode = code;
        if (extended)
            bCode = getCode39Ex(code);
        if (font != null) {
            if (generateChecksum && checksumText)
                fullCode += getChecksum(bCode);
            if (startStopText)
                fullCode = "*" + fullCode + "*";
            fontX = font.getWidthPoint(fullCode = altText != null ? altText : fullCode, size);
        }
        if (generateChecksum)
            bCode += getChecksum(bCode);
        int len = bCode.length() + 2;
        float fullWidth = len * (6 * x + 3 * x * n) + (len - 1) * x;
        float barStartX = 0;
        float textStartX = 0;
        switch (textAlignment) {
            case Element.ALIGN_LEFT:
                break;
            case Element.ALIGN_RIGHT:
                if (fontX > fullWidth)
                    barStartX = fontX - fullWidth;
                else
                    textStartX = fullWidth - fontX;
                break;
            default:
                if (fontX > fullWidth)
                    barStartX = (fontX - fullWidth) / 2;
                else
                    textStartX = (fullWidth - fontX) / 2;
                break;
        }
        float barStartY = 0;
        float textStartY = 0;
        if (font != null) {
            if (baseline <= 0)
                textStartY = barHeight - baseline;
            else {
                textStartY = -font.getFontDescriptor(BaseFont.DESCENT, size);
                barStartY = textStartY + baseline;
            }
        }
        byte bars[] = getBarsCode39(bCode);
        boolean print = true;
        if (barColor != null)
            cb.setColorFill(barColor);
        for (int k = 0; k < bars.length; ++k) {
            float w = (bars[k] == 0 ? x : x * n);
            if (print)
                cb.rectangle(barStartX, barStartY, w - inkSpreading, barHeight);
            print = !print;
            barStartX += w;
        }
        cb.fill();
        if (font != null) {
            if (textColor != null)
                cb.setColorFill(textColor);
            cb.beginText();
            cb.setFontAndSize(font, size);
            cb.setTextMatrix(textStartX, textStartY);
            cb.showText(fullCode);
            cb.endText();
        }
        return getBarcodeSize();
    }

    // AWT related methods (remove this if you port to Android / GAE)
    
    /** Creates a <CODE>java.awt.Image</CODE>. This image only
     * contains the bars without any text.
     * @param foreground the color of the bars
     * @param background the color of the background
     * @return the image
     */    
    public java.awt.Image createAwtImage(java.awt.Color foreground, java.awt.Color background) {
        int f = foreground.getRGB();
        int g = background.getRGB();
        java.awt.Canvas canvas = new java.awt.Canvas();

        String bCode = code;
        if (extended)
            bCode = getCode39Ex(code);
        if (generateChecksum)
            bCode += getChecksum(bCode);
        int len = bCode.length() + 2;
        int nn = (int)n;
        int fullWidth = len * (6 + 3 * nn) + (len - 1);
        byte bars[] = getBarsCode39(bCode);
        boolean print = true;
        int ptr = 0;
        int height = (int)barHeight;
        int pix[] = new int[fullWidth * height];
        for (int k = 0; k < bars.length; ++k) {
            int w = (bars[k] == 0 ? 1 : nn);
            int c = g;
            if (print)
                c = f;
            print = !print;
            for (int j = 0; j < w; ++j)
                pix[ptr++] = c;
        }
        for (int k = fullWidth; k < pix.length; k += fullWidth) {
            System.arraycopy(pix, 0, pix, k, fullWidth); 
        }
        java.awt.Image img = canvas.createImage(new java.awt.image.MemoryImageSource(fullWidth, height, pix, 0, fullWidth));
        
        return img;
    }    
}
