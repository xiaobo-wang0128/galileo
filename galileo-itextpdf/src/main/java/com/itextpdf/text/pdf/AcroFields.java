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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.error_messages.MessageLocalization;
import com.itextpdf.text.io.RASInputStream;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.io.WindowRandomAccessSource;
import com.itextpdf.text.pdf.PRTokeniser.TokenType;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.xml.XmlToTxt;

/**
 * Query and change fields in existing documents either by method
 * calls or by FDF merging.
 *
 * @author Paulo Soares
 */
public class AcroFields {

    PdfReader reader;
    PdfWriter writer;
    Map<String, Item> fields;
    private int topFirst;
    private HashMap<String, int[]> sigNames;
    private boolean append;
    public static final int DA_FONT = 0;
    public static final int DA_SIZE = 1;
    public static final int DA_COLOR = 2;
    private HashMap<Integer, BaseFont> extensionFonts = new HashMap<Integer, BaseFont>();
    private XfaForm xfa;

    /**
     * A field type invalid or not found.
     */
    public static final int FIELD_TYPE_NONE = 0;

    /**
     * A field type.
     */
    public static final int FIELD_TYPE_PUSHBUTTON = 1;

    /**
     * A field type.
     */
    public static final int FIELD_TYPE_CHECKBOX = 2;

    /**
     * A field type.
     */
    public static final int FIELD_TYPE_RADIOBUTTON = 3;

    /**
     * A field type.
     */
    public static final int FIELD_TYPE_TEXT = 4;

    /**
     * A field type.
     */
    public static final int FIELD_TYPE_LIST = 5;

    /**
     * A field type.
     */
    public static final int FIELD_TYPE_COMBO = 6;

    /**
     * A field type.
     */
    public static final int FIELD_TYPE_SIGNATURE = 7;

    private boolean lastWasString;

    /** Holds value of property generateAppearances. */
    private boolean generateAppearances = true;

    private HashMap<String, BaseFont> localFonts = new HashMap<String, BaseFont>();

    private float extraMarginLeft;
    private float extraMarginTop;
    private ArrayList<BaseFont> substitutionFonts;

    AcroFields(PdfReader reader, PdfWriter writer) {
        this.reader = reader;
        this.writer = writer;
        try {
            xfa = new XfaForm(reader);
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        if (writer instanceof PdfStamperImp) {
            append = ((PdfStamperImp)writer).isAppend();
        }
        fill();
    }

    void fill() {
        fields = new LinkedHashMap<String, Item>();
        PdfDictionary top = (PdfDictionary)PdfReader.getPdfObjectRelease(reader.getCatalog().get(PdfName.ACROFORM));
        if (top == null)
            return;
        PdfBoolean needappearances = top.getAsBoolean(PdfName.NEEDAPPEARANCES);
        if (needappearances == null || !needappearances.booleanValue())
            setGenerateAppearances(true);
        else
            setGenerateAppearances(false);
        PdfArray arrfds = (PdfArray)PdfReader.getPdfObjectRelease(top.get(PdfName.FIELDS));
        if (arrfds == null || arrfds.size() == 0)
            return;
        for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
            PdfDictionary page = reader.getPageNRelease(k);
            PdfArray annots = (PdfArray)PdfReader.getPdfObjectRelease(page.get(PdfName.ANNOTS), page);
            if (annots == null)
                continue;
            for (int j = 0; j < annots.size(); ++j) {
                PdfDictionary annot = annots.getAsDict(j);
                if (annot == null) {
                    PdfReader.releaseLastXrefPartial(annots.getAsIndirectObject(j));
                    continue;
                }
                if (!PdfName.WIDGET.equals(annot.getAsName(PdfName.SUBTYPE))) {
                    PdfReader.releaseLastXrefPartial(annots.getAsIndirectObject(j));
                    continue;
                }
                PdfDictionary widget = annot;
                PdfDictionary dic = new PdfDictionary();
                dic.putAll(annot);
                String name = "";
                PdfDictionary value = null;
                PdfObject lastV = null;
                while (annot != null) {
                    dic.mergeDifferent(annot);
                    PdfString t = annot.getAsString(PdfName.T);
                    if (t != null)
                        name = t.toUnicodeString() + "." + name;
                    if (lastV == null && annot.get(PdfName.V) != null)
                        lastV = PdfReader.getPdfObjectRelease(annot.get(PdfName.V));
                    if (value == null &&  t != null) {
                        value = annot;
                        if (annot.get(PdfName.V) == null && lastV  != null)
                            value.put(PdfName.V, lastV); // TODO: seems to be bug (we are going up the hierarchy and setting parent's V entry to child's V value)
                    }
                    annot = annot.getAsDict(PdfName.PARENT);
                }
                if (name.length() > 0)
                    name = name.substring(0, name.length() - 1);
                Item item = fields.get(name);
                if (item == null) {
                    item = new Item();
                    fields.put(name, item);
                }
                if (value == null)
                    item.addValue(widget);
                else
                    item.addValue(value);
                item.addWidget(widget);
                item.addWidgetRef(annots.getAsIndirectObject(j)); // must be a reference
                if (top != null)
                    dic.mergeDifferent(top);
                item.addMerged(dic);
                item.addPage(k);
                item.addTabOrder(j);
            }
        }
        // some tools produce invisible signatures without an entry in the page annotation array
        // look for a single level annotation
        PdfNumber sigFlags = top.getAsNumber(PdfName.SIGFLAGS);
        if (sigFlags == null || (sigFlags.intValue() & 1) != 1)
            return;
        for (int j = 0; j < arrfds.size(); ++j) {
            PdfDictionary annot = arrfds.getAsDict(j);
            if (annot == null) {
                PdfReader.releaseLastXrefPartial(arrfds.getAsIndirectObject(j));
                continue;
            }
            if (!PdfName.WIDGET.equals(annot.getAsName(PdfName.SUBTYPE))) {
                PdfReader.releaseLastXrefPartial(arrfds.getAsIndirectObject(j));
                continue;
            }
            PdfArray kids = (PdfArray)PdfReader.getPdfObjectRelease(annot.get(PdfName.KIDS));
            if (kids != null)
                continue;
            PdfDictionary dic = new PdfDictionary();
            dic.putAll(annot);
            PdfString t = annot.getAsString(PdfName.T);
            if (t == null)
                continue;
            String name = t.toUnicodeString();
            if (fields.containsKey(name))
                continue;
            Item item = new Item();
            fields.put(name, item);
            item.addValue(dic);
            item.addWidget(dic);
            item.addWidgetRef(arrfds.getAsIndirectObject(j)); // must be a reference
            item.addMerged(dic);
            item.addPage(-1);
            item.addTabOrder(-1);
        }
    }

    /**
     * Gets the list of appearance names. Use it to get the names allowed
     * with radio and checkbox fields. If the /Opt key exists the values will
     * also be included. The name 'Off' may also be valid
     * even if not returned in the list.
     *
     * For Comboboxes it will return an array of display values. To extract the
     * export values of a Combobox, please refer to {@link AcroFields#getListOptionExport(String)}
     *
     * @param fieldName the fully qualified field name
     * @return the list of names or <CODE>null</CODE> if the field does not exist
     */
    public String[] getAppearanceStates(String fieldName) {
        Item fd = fields.get(fieldName);
        if (fd == null)
            return null;
        HashSet<String> names = new LinkedHashSet<String>();
        PdfDictionary vals = fd.getValue(0);
        PdfString stringOpt = vals.getAsString( PdfName.OPT );

        // should not happen according to specs
        if (stringOpt != null) {
        	names.add(stringOpt.toUnicodeString());
        }
        else {
            PdfArray arrayOpt = vals.getAsArray(PdfName.OPT);
            if (arrayOpt != null) {
            	for (int k = 0; k < arrayOpt.size(); ++k) {
                    PdfObject pdfObject = arrayOpt.getDirectObject(k);
                    PdfString valStr = null;

                    switch ( pdfObject.type() ) {
                        case PdfObject.ARRAY:
                            PdfArray pdfArray = (PdfArray) pdfObject;
                            valStr = pdfArray.getAsString( 1 );
                            break;
                        case PdfObject.STRING:
                            valStr = (PdfString) pdfObject;
                            break;
                    }

            		if (valStr != null)
            			names.add(valStr.toUnicodeString());
            	}
            }
        }
        for (int k = 0; k < fd.size(); ++k) {
            PdfDictionary dic = fd.getWidget( k );
            dic = dic.getAsDict(PdfName.AP);
            if (dic == null)
                continue;
            dic = dic.getAsDict(PdfName.N);
            if (dic == null)
                continue;
            for (Object element : dic.getKeys()) {
                String name = PdfName.decodeName(((PdfName)element).toString());
                names.add(name);
            }
        }
        String out[] = new String[names.size()];
        return names.toArray(out);
    }

    private String[] getListOption(String fieldName, int idx) {
        Item fd = getFieldItem(fieldName);
        if (fd == null)
            return null;
        PdfArray ar = fd.getMerged(0).getAsArray(PdfName.OPT);
        if (ar == null)
            return null;
        String[] ret = new String[ar.size()];
        for (int k = 0; k < ar.size(); ++k) {
            PdfObject obj = ar.getDirectObject( k );
            try {
                if (obj.isArray()) {
                    obj = ((PdfArray)obj).getDirectObject(idx);
                }
                if (obj.isString())
                    ret[k] = ((PdfString)obj).toUnicodeString();
                else
                    ret[k] = obj.toString();
            }
            catch (Exception e) {
                ret[k] = "";
            }
        }
        return ret;
    }

    /**
     * Gets the list of export option values from fields of type list or combo.
     * If the field doesn't exist or the field type is not list or combo it will return
     * <CODE>null</CODE>.
     *
     * @param fieldName the field name
     * @return the list of export option values from fields of type list or combo
     */
    public String[] getListOptionExport(String fieldName) {
        return getListOption(fieldName, 0);
    }

    /**
     * Gets the list of display option values from fields of type list or combo.
     * If the field doesn't exist or the field type is not list or combo it will return
     * <CODE>null</CODE>.
     *
     * @param fieldName the field name
     * @return the list of export option values from fields of type list or combo
     */
    public String[] getListOptionDisplay(String fieldName) {
        return getListOption(fieldName, 1);
    }

    /**
     * Sets the option list for fields of type list or combo. One of <CODE>exportValues</CODE>
     * or <CODE>displayValues</CODE> may be <CODE>null</CODE> but not both. This method will only
     * set the list but will not set the value or appearance. For that, calling <CODE>setField()</CODE>
     * is required.
     * <p>
     * An example:
     * <p>
     * <PRE>
     * PdfReader pdf = new PdfReader("input.pdf");
     * PdfStamper stp = new PdfStamper(pdf, new FileOutputStream("output.pdf"));
     * AcroFields af = stp.getAcroFields();
     * af.setListOption("ComboBox", new String[]{"a", "b", "c"}, new String[]{"first", "second", "third"});
     * af.setField("ComboBox", "b");
     * stp.close();
     * </PRE>
     *
     * @param fieldName the field name
     * @param exportValues the export values
     * @param displayValues the display values
     * @return <CODE>true</CODE> if the operation succeeded, <CODE>false</CODE> otherwise
     */
    public boolean setListOption(String fieldName, String[] exportValues, String[] displayValues) {
        if (exportValues == null && displayValues == null)
            return false;
        if (exportValues != null && displayValues != null && exportValues.length != displayValues.length)
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.export.and.the.display.array.must.have.the.same.size"));
        int ftype = getFieldType(fieldName);
        if (ftype != FIELD_TYPE_COMBO && ftype != FIELD_TYPE_LIST)
            return false;
        Item fd = fields.get(fieldName);
        String[] sing = null;
        if (exportValues == null && displayValues != null)
            sing = displayValues;
        else if (exportValues != null && displayValues == null)
            sing = exportValues;
        PdfArray opt = new PdfArray();
        if (sing != null) {
            for (int k = 0; k < sing.length; ++k)
                opt.add(new PdfString(sing[k], PdfObject.TEXT_UNICODE));
        }
        else {
            for (int k = 0; k < exportValues.length; ++k) {
                PdfArray a = new PdfArray();
                a.add(new PdfString(exportValues[k], PdfObject.TEXT_UNICODE));
                a.add(new PdfString(displayValues[k], PdfObject.TEXT_UNICODE));
                opt.add(a);
            }
        }
        fd.writeToAll( PdfName.OPT, opt, Item.WRITE_VALUE | Item.WRITE_MERGED );
        return true;
    }

    /**
     * Gets the field type. The type can be one of: <CODE>FIELD_TYPE_PUSHBUTTON</CODE>,
     * <CODE>FIELD_TYPE_CHECKBOX</CODE>, <CODE>FIELD_TYPE_RADIOBUTTON</CODE>,
     * <CODE>FIELD_TYPE_TEXT</CODE>, <CODE>FIELD_TYPE_LIST</CODE>,
     * <CODE>FIELD_TYPE_COMBO</CODE> or <CODE>FIELD_TYPE_SIGNATURE</CODE>.
     * <p>
     * If the field does not exist or is invalid it returns
     * <CODE>FIELD_TYPE_NONE</CODE>.
     *
     * @param fieldName the field name
     * @return the field type
     */
    public int getFieldType(String fieldName) {
        Item fd = getFieldItem(fieldName);
        if (fd == null)
            return FIELD_TYPE_NONE;
        PdfDictionary merged = fd.getMerged( 0 );
        PdfName type = merged.getAsName(PdfName.FT);
        if (type == null)
            return FIELD_TYPE_NONE;
        int ff = 0;
        PdfNumber ffo = merged.getAsNumber(PdfName.FF);
        if (ffo != null) {
            ff = ffo.intValue();
        }
        if (PdfName.BTN.equals(type)) {
            if ((ff & PdfFormField.FF_PUSHBUTTON) != 0)
                return FIELD_TYPE_PUSHBUTTON;
            if ((ff & PdfFormField.FF_RADIO) != 0)
                return FIELD_TYPE_RADIOBUTTON;
            else
                return FIELD_TYPE_CHECKBOX;
        }
        else if (PdfName.TX.equals(type)) {
            return FIELD_TYPE_TEXT;
        }
        else if (PdfName.CH.equals(type)) {
            if ((ff & PdfFormField.FF_COMBO) != 0)
                return FIELD_TYPE_COMBO;
            else
                return FIELD_TYPE_LIST;
        }
        else if (PdfName.SIG.equals(type)) {
            return FIELD_TYPE_SIGNATURE;
        }
        return FIELD_TYPE_NONE;
    }

    /**
     * Export the fields as a FDF.
     *
     * @param writer the FDF writer
     */
    public void exportAsFdf(FdfWriter writer) {
        for (Map.Entry<String, Item> entry : fields.entrySet()) {
            Item item = entry.getValue();
            String name = entry.getKey();
            PdfObject v = item.getMerged(0).get(PdfName.V);
            if (v == null)
                continue;
            String value = getField(name);
            if (lastWasString)
                writer.setFieldAsString(name, value);
            else
                writer.setFieldAsName(name, value);
        }
    }

    /**
     * Renames a field. Only the last part of the name can be renamed. For example,
     * if the original field is "ab.cd.ef" only the "ef" part can be renamed.
     *
     * @param oldName the old field name
     * @param newName the new field name
     * @return <CODE>true</CODE> if the renaming was successful, <CODE>false</CODE>
     * otherwise
     */
    public boolean renameField(String oldName, String newName) {
        int idx1 = oldName.lastIndexOf('.') + 1;
        int idx2 = newName.lastIndexOf('.') + 1;
        if (idx1 != idx2)
            return false;
        if (!oldName.substring(0, idx1).equals(newName.substring(0, idx2)))
            return false;
        if (fields.containsKey(newName))
            return false;
        Item item = fields.get(oldName);
        if (item == null)
            return false;
        newName = newName.substring(idx2);
        PdfString ss = new PdfString(newName, PdfObject.TEXT_UNICODE);

        item.writeToAll( PdfName.T, ss, Item.WRITE_VALUE | Item.WRITE_MERGED);
        item.markUsed( this, Item.WRITE_VALUE );

        fields.remove(oldName);
        fields.put(newName, item);

        return true;
    }

    public static Object[] splitDAelements(String da) {
        try {
            PRTokeniser tk = new PRTokeniser(new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(PdfEncodings.convertToBytes(da, null))));
            ArrayList<String> stack = new ArrayList<String>();
            Object ret[] = new Object[3];
            while (tk.nextToken()) {
                if (tk.getTokenType() == TokenType.COMMENT)
                    continue;
                if (tk.getTokenType() == TokenType.OTHER) {
                    String operator = tk.getStringValue();
                    if (operator.equals("Tf")) {
                        if (stack.size() >= 2) {
                            ret[DA_FONT] = stack.get(stack.size() - 2);
                            ret[DA_SIZE] = new Float(stack.get(stack.size() - 1));
                        }
                    }
                    else if (operator.equals("g")) {
                        if (stack.size() >= 1) {
                            float gray = new Float(stack.get(stack.size() - 1)).floatValue();
                            if (gray != 0)
                                ret[DA_COLOR] = new GrayColor(gray);
                        }
                    }
                    else if (operator.equals("rg")) {
                        if (stack.size() >= 3) {
                            float red = new Float(stack.get(stack.size() - 3)).floatValue();
                            float green = new Float(stack.get(stack.size() - 2)).floatValue();
                            float blue = new Float(stack.get(stack.size() - 1)).floatValue();
                            ret[DA_COLOR] = new BaseColor(red, green, blue);
                        }
                    }
                    else if (operator.equals("k")) {
                        if (stack.size() >= 4) {
                            float cyan = new Float(stack.get(stack.size() - 4)).floatValue();
                            float magenta = new Float(stack.get(stack.size() - 3)).floatValue();
                            float yellow = new Float(stack.get(stack.size() - 2)).floatValue();
                            float black = new Float(stack.get(stack.size() - 1)).floatValue();
                            ret[DA_COLOR] = new CMYKColor(cyan, magenta, yellow, black);
                        }
                    }
                    stack.clear();
                }
                else
                    stack.add(tk.getStringValue());
            }
            return ret;
        }
        catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    public void decodeGenericDictionary(PdfDictionary merged, BaseField tx) throws IOException, DocumentException {
        int flags = 0;
        // the text size and color
        PdfString da = merged.getAsString(PdfName.DA);
        if (da != null) {
            boolean fontfallback = false;
            Object dab[] = splitDAelements(da.toUnicodeString());
            if (dab[DA_SIZE] != null)
                tx.setFontSize(((Float)dab[DA_SIZE]).floatValue());
            if (dab[DA_COLOR] != null)
                tx.setTextColor((BaseColor)dab[DA_COLOR]);
            if (dab[DA_FONT] != null) {
                PdfDictionary dr = merged.getAsDict(PdfName.DR);
                if (dr != null) {
                    PdfDictionary font = dr.getAsDict(PdfName.FONT);
                    if (font != null) {
                        PdfObject po = font.get(new PdfName((String)dab[DA_FONT]));
                        if (po != null && po.type() == PdfObject.INDIRECT) {
                            PRIndirectReference por = (PRIndirectReference)po;
                            BaseFont bp = new DocumentFont((PRIndirectReference)po, dr.getAsDict(PdfName.ENCODING));
                            tx.setFont(bp);
                            Integer porkey = Integer.valueOf(por.getNumber());
                            BaseFont porf = extensionFonts.get(porkey);
                            if (porf == null) {
                                if (!extensionFonts.containsKey(porkey)) {
                                    PdfDictionary fo = (PdfDictionary)PdfReader.getPdfObject(po);
                                    PdfDictionary fd = fo.getAsDict(PdfName.FONTDESCRIPTOR);
                                    if (fd != null) {
                                        PRStream prs = (PRStream)PdfReader.getPdfObject(fd.get(PdfName.FONTFILE2));
                                        if (prs == null)
                                            prs = (PRStream)PdfReader.getPdfObject(fd.get(PdfName.FONTFILE3));
                                        if (prs == null) {
                                            extensionFonts.put(porkey, null);
                                        }
                                        else {
                                            try {
                                                porf = BaseFont.createFont("font.ttf", BaseFont.IDENTITY_H, true, false, PdfReader.getStreamBytes(prs), null);
                                            }
                                            catch (Exception e) {
                                            }
                                            extensionFonts.put(porkey, porf);
                                        }
                                    }
                                }
                            }
                            if (tx instanceof TextField)
                                ((TextField)tx).setExtensionFont(porf);
                        }
                        else {
                            fontfallback = true;
                        }

                    }
                    else {
                        fontfallback = true;
                    }
                }
                else {
                    fontfallback = true;
                }
            }
            if (fontfallback) {
                BaseFont bf = localFonts.get(dab[DA_FONT]);
                if (bf == null) {
                    String fn[] = stdFieldFontNames.get(dab[DA_FONT]);
                    if (fn != null) {
                        try {
                            String enc = "winansi";
                            if (fn.length > 1)
                                enc = fn[1];
                            bf = BaseFont.createFont(fn[0], enc, false);
                            tx.setFont(bf);
                        }
                        catch (Exception e) {
                            // empty
                        }
                    }
                }
                else
                    tx.setFont(bf);
            }
        }
        //rotation, border and background color
        PdfDictionary mk = merged.getAsDict(PdfName.MK);
        if (mk != null) {
            PdfArray ar = mk.getAsArray(PdfName.BC);
            BaseColor border = getMKColor(ar);
            tx.setBorderColor(border);
            if (border != null)
                tx.setBorderWidth(1);
            ar = mk.getAsArray(PdfName.BG);
            tx.setBackgroundColor(getMKColor(ar));
            PdfNumber rotation = mk.getAsNumber(PdfName.R);
            if (rotation != null)
                tx.setRotation(rotation.intValue());
        }
        //flags
        PdfNumber nfl = merged.getAsNumber(PdfName.F);
        flags = 0;
        tx.setVisibility(BaseField.VISIBLE_BUT_DOES_NOT_PRINT);
        if (nfl != null) {
            flags = nfl.intValue();
            if ((flags & PdfFormField.FLAGS_PRINT) != 0 && (flags & PdfFormField.FLAGS_HIDDEN) != 0)
                tx.setVisibility(BaseField.HIDDEN);
            else if ((flags & PdfFormField.FLAGS_PRINT) != 0 && (flags & PdfFormField.FLAGS_NOVIEW) != 0)
                tx.setVisibility(BaseField.HIDDEN_BUT_PRINTABLE);
            else if ((flags & PdfFormField.FLAGS_PRINT) != 0)
                tx.setVisibility(BaseField.VISIBLE);
        }
        //multiline
        nfl = merged.getAsNumber(PdfName.FF);
        flags = 0;
        if (nfl != null)
            flags = nfl.intValue();
        tx.setOptions(flags);
        if ((flags & PdfFormField.FF_COMB) != 0) {
            PdfNumber maxLen = merged.getAsNumber(PdfName.MAXLEN);
            int len = 0;
            if (maxLen != null)
                len = maxLen.intValue();
            tx.setMaxCharacterLength(len);
        }
        //alignment
        nfl = merged.getAsNumber(PdfName.Q);
        if (nfl != null) {
            if (nfl.intValue() == PdfFormField.Q_CENTER)
                tx.setAlignment(Element.ALIGN_CENTER);
            else if (nfl.intValue() == PdfFormField.Q_RIGHT)
                tx.setAlignment(Element.ALIGN_RIGHT);
        }
        //border styles
        PdfDictionary bs = merged.getAsDict(PdfName.BS);
        if (bs != null) {
            PdfNumber w = bs.getAsNumber(PdfName.W);
            if (w != null)
                tx.setBorderWidth(w.floatValue());
            PdfName s = bs.getAsName(PdfName.S);
            if (PdfName.D.equals(s))
                tx.setBorderStyle(PdfBorderDictionary.STYLE_DASHED);
            else if (PdfName.B.equals(s))
                tx.setBorderStyle(PdfBorderDictionary.STYLE_BEVELED);
            else if (PdfName.I.equals(s))
                tx.setBorderStyle(PdfBorderDictionary.STYLE_INSET);
            else if (PdfName.U.equals(s))
                tx.setBorderStyle(PdfBorderDictionary.STYLE_UNDERLINE);
        }
        else {
            PdfArray bd = merged.getAsArray(PdfName.BORDER);
            if (bd != null) {
                if (bd.size() >= 3)
                    tx.setBorderWidth(bd.getAsNumber(2).floatValue());
                if (bd.size() >= 4)
                    tx.setBorderStyle(PdfBorderDictionary.STYLE_DASHED);
            }
        }
    }

    PdfAppearance getAppearance(PdfDictionary merged, String values[], String fieldName) throws IOException, DocumentException {
        PdfName fieldType = merged.getAsName(PdfName.FT);

        if (PdfName.BTN.equals(fieldType)) {
            PdfNumber fieldFlags = merged.getAsNumber(PdfName.FF);
            boolean isRadio = fieldFlags != null && (fieldFlags.intValue() & PdfFormField.FF_RADIO) != 0;
            RadioCheckField field = new RadioCheckField(writer, null, null, null);
            decodeGenericDictionary(merged, field);
            //rect
            PdfArray rect = merged.getAsArray(PdfName.RECT);
            Rectangle box = PdfReader.getNormalizedRectangle(rect);
            if (field.getRotation() == 90 || field.getRotation() == 270)
                box = box.rotate();
            field.setBox(box);
            if (!isRadio)
                field.setCheckType(RadioCheckField.TYPE_CROSS);
            return field.getAppearance(isRadio, !(merged.getAsName(PdfName.AS).equals(PdfName.Off)));
        }

        topFirst = 0;
        String text = values.length > 0 ? values[0] : null;

        TextField tx = null;
        if (fieldCache == null || !fieldCache.containsKey(fieldName)) {
            tx = new TextField(writer, null, null);
            tx.setExtraMargin(extraMarginLeft, extraMarginTop);
            tx.setBorderWidth(0);
            tx.setSubstitutionFonts(substitutionFonts);
            decodeGenericDictionary(merged, tx);
            //rect
            PdfArray rect = merged.getAsArray(PdfName.RECT);
            Rectangle box = PdfReader.getNormalizedRectangle(rect);
            if (tx.getRotation() == 90 || tx.getRotation() == 270)
                box = box.rotate();
            tx.setBox(box);
            if (fieldCache != null)
                fieldCache.put(fieldName, tx);
        }
        else {
            tx = fieldCache.get(fieldName);
            tx.setWriter(writer);
        }
        if (PdfName.TX.equals(fieldType)) {
            if (values.length > 0 && values[0] != null) {
                tx.setText(values[0]);
            }
            return tx.getAppearance();
        }
        if (!PdfName.CH.equals(fieldType))
            throw new DocumentException(MessageLocalization.getComposedMessage("an.appearance.was.requested.without.a.variable.text.field"));
        PdfArray opt = merged.getAsArray(PdfName.OPT);
        int flags = 0;
        PdfNumber nfl = merged.getAsNumber(PdfName.FF);
        if (nfl != null)
            flags = nfl.intValue();
        if ((flags & PdfFormField.FF_COMBO) != 0 && opt == null) {
            tx.setText(text);
            return tx.getAppearance();
        }
        if (opt != null) {
            String choices[] = new String[opt.size()];
            String choicesExp[] = new String[opt.size()];
            for (int k = 0; k < opt.size(); ++k) {
                PdfObject obj = opt.getPdfObject(k);
                if (obj.isString()) {
                    choices[k] = choicesExp[k] = ((PdfString)obj).toUnicodeString();
                }
                else {
                    PdfArray a = (PdfArray) obj;
                    choicesExp[k] = a.getAsString(0).toUnicodeString();
                    choices[k] = a.getAsString(1).toUnicodeString();
                }
            }
            if ((flags & PdfFormField.FF_COMBO) != 0) {
                for (int k = 0; k < choices.length; ++k) {
                    if (text.equals(choicesExp[k])) {
                        text = choices[k];
                        break;
                    }
                }
                tx.setText(text);
                return tx.getAppearance();
            }
            ArrayList<Integer> indexes = new ArrayList<Integer>();
            for (int k = 0; k < choicesExp.length; ++k) {
            	for (int j = 0; j < values.length; ++j) {
            		String val = values[j];
            		if (val != null && val.equals(choicesExp[k])) {
            			indexes.add( Integer.valueOf( k ) );
            			break;
            		}
            	}
            }
            tx.setChoices(choices);
            tx.setChoiceExports(choicesExp);
            tx.setChoiceSelections( indexes );
        }
        PdfAppearance app = tx.getListAppearance();
        topFirst = tx.getTopFirst();
        return app;
    }

    PdfAppearance getAppearance(PdfDictionary merged, String text, String fieldName) throws IOException, DocumentException {
      String valueArr[] = new String[1];
      valueArr[0] = text;
      return getAppearance( merged, valueArr, fieldName );
    }

    BaseColor getMKColor(PdfArray ar) {
        if (ar == null)
            return null;
        switch (ar.size()) {
            case 1:
                return new GrayColor(ar.getAsNumber(0).floatValue());
            case 3:
                return new BaseColor(ExtendedColor.normalize(ar.getAsNumber(0).floatValue()), ExtendedColor.normalize(ar.getAsNumber(1).floatValue()), ExtendedColor.normalize(ar.getAsNumber(2).floatValue()));
            case 4:
                return new CMYKColor(ar.getAsNumber(0).floatValue(), ar.getAsNumber(1).floatValue(), ar.getAsNumber(2).floatValue(), ar.getAsNumber(3).floatValue());
            default:
                return null;
        }
    }

    /**
     * Retrieve the rich value for the given field
     * @param name
     * @return The rich value if present, or null.
     * @since 5.0.6
     */
    public String getFieldRichValue(String name) {
    	if (xfa.isXfaPresent()) {
    		return null;
    	}

    	Item item = fields.get(name);
    	if (item == null) {
    		return null;
    	}

    	PdfDictionary merged = item.getMerged(0);
    	PdfString rich = merged.getAsString(PdfName.RV);

    	String markup = null;
    	if (rich != null) {
    		markup = rich.toString();
    	}

    	return markup;
    }
    /**
     * Gets the field value.
     *
     * @param name the fully qualified field name
     * @return the field value
     */
    public String getField(String name) {
        if (xfa.isXfaPresent()) {
            name = xfa.findFieldName(name, this);
            if (name == null)
                return null;
            name = XfaForm.Xml2Som.getShortName(name);
            return XfaForm.getNodeText(xfa.findDatasetsNode(name));
        }
        Item item = fields.get(name);
        if (item == null)
            return null;
        lastWasString = false;
        PdfDictionary mergedDict = item.getMerged( 0 );

        // Jose A. Rodriguez posted a fix to the mailing list (May 11, 2009)
        // explaining that the value can also be a stream value
        // the fix was made against an old iText version. Bruno adapted it.
        PdfObject v = PdfReader.getPdfObject(mergedDict.get(PdfName.V));
        if (v == null)
        	return "";
        if (v instanceof PRStream) {
                byte[] valBytes;
				try {
					valBytes = PdfReader.getStreamBytes((PRStream)v);
	                return new String(valBytes);
				} catch (IOException e) {
					throw new ExceptionConverter(e);
				}
        }

        PdfName type = mergedDict.getAsName(PdfName.FT);
        if (PdfName.BTN.equals(type)) {
            PdfNumber ff = mergedDict.getAsNumber(PdfName.FF);
            int flags = 0;
            if (ff != null)
                flags = ff.intValue();
            if ((flags & PdfFormField.FF_PUSHBUTTON) != 0)
                return "";
            String value = "";
            if (v instanceof PdfName)
                value = PdfName.decodeName(v.toString());
            else if (v instanceof PdfString)
                value = ((PdfString)v).toUnicodeString();
            PdfArray opts = item.getValue(0).getAsArray(PdfName.OPT);
            if (opts != null) {
                int idx = 0;
                try {
                    idx = Integer.parseInt(value);
                    PdfString ps = opts.getAsString(idx);
                    value = ps.toUnicodeString();
                    lastWasString = true;
                }
                catch (Exception e) {
                }
            }
            return value;
        }
        if (v instanceof PdfString) {
            lastWasString = true;
            return ((PdfString)v).toUnicodeString();
        } else if (v instanceof PdfName) {
            return PdfName.decodeName(v.toString());
        } else
            return "";
    }

    /**
     * Gets the field values of a Choice field.
     *
     * @param name the fully qualified field name
     * @return the field value
     * @since 2.1.3
     */
    public String[] getListSelection(String name) {
    	String[] ret;
    	String s = getField(name);
    	if (s == null) {
    		ret = new String[]{};
    	}
    	else {
    		ret = new String[]{ s };
    	}
        Item item = fields.get(name);
        if (item == null)
            return ret;
        //PdfName type = (PdfName)PdfReader.getPdfObject(((PdfDictionary)item.merged.get(0)).get(PdfName.FT));
        //if (!PdfName.CH.equals(type)) {
        //	return ret;
        //}
        PdfArray values = item.getMerged(0).getAsArray(PdfName.I);
        if (values == null)
            return ret;
        ret = new String[values.size()];
        String[] options = getListOptionExport(name);
        PdfNumber n;
        int idx = 0;
        for (Iterator<PdfObject> i = values.listIterator(); i.hasNext(); ) {
        	n = (PdfNumber)i.next();
        	ret[idx++] = options[n.intValue()];
        }
        return ret;
    }


    /**
     * Sets a field property. Valid property names are:
     * <p>
     * <ul>
     * <li>textfont - sets the text font. The value for this entry is a <CODE>BaseFont</CODE>.<br>
     * <li>textcolor - sets the text color. The value for this entry is a <CODE>BaseColor</CODE>.<br>
     * <li>textsize - sets the text size. The value for this entry is a <CODE>Float</CODE>.
     * <li>bgcolor - sets the background color. The value for this entry is a <CODE>BaseColor</CODE>.
     *     If <code>null</code> removes the background.<br>
     * <li>bordercolor - sets the border color. The value for this entry is a <CODE>BaseColor</CODE>.
     *     If <code>null</code> removes the border.<br>
     * </ul>
     *
     * @param field the field name
     * @param name the property name
     * @param value the property value
     * @param inst an array of <CODE>int</CODE> indexing into <CODE>AcroField.Item.merged</CODE> elements to process.
     * Set to <CODE>null</CODE> to process all
     * @return <CODE>true</CODE> if the property exists, <CODE>false</CODE> otherwise
     */
    public boolean setFieldProperty(String field, String name, Object value, int inst[]) {
        if (writer == null)
            throw new RuntimeException(MessageLocalization.getComposedMessage("this.acrofields.instance.is.read.only"));
        try {
            Item item = fields.get(field);
            if (item == null)
                return false;
            InstHit hit = new InstHit(inst);
            PdfDictionary merged;
            PdfString da;
            if (name.equalsIgnoreCase("textfont")) {
                for (int k = 0; k < item.size(); ++k) {
                    if (hit.isHit(k)) {
                        merged = item.getMerged( k );
                        da = merged.getAsString(PdfName.DA);
                        PdfDictionary dr = merged.getAsDict(PdfName.DR);
                        if (da != null) {
                            if (dr == null) {
                            	dr = new PdfDictionary();
                            	merged.put(PdfName.DR, dr);
                            }
                            Object dao[] = splitDAelements(da.toUnicodeString());
                            PdfAppearance cb = new PdfAppearance();
                            if (dao[DA_FONT] != null) {
                                BaseFont bf = (BaseFont)value;
                                PdfName psn = PdfAppearance.stdFieldFontNames.get(bf.getPostscriptFontName());
                                if (psn == null) {
                                    psn = new PdfName(bf.getPostscriptFontName());
                                }
                                PdfDictionary fonts = dr.getAsDict(PdfName.FONT);
                                if (fonts == null) {
                                    fonts = new PdfDictionary();
                                    dr.put(PdfName.FONT, fonts);
                                }
                                PdfIndirectReference fref = (PdfIndirectReference)fonts.get(psn);
                                PdfDictionary top = reader.getCatalog().getAsDict(PdfName.ACROFORM);
                                markUsed(top);
                                dr = top.getAsDict(PdfName.DR);
                                if (dr == null) {
                                    dr = new PdfDictionary();
                                    top.put(PdfName.DR, dr);
                                }
                                markUsed(dr);
                                PdfDictionary fontsTop = dr.getAsDict(PdfName.FONT);
                                if (fontsTop == null) {
                                    fontsTop = new PdfDictionary();
                                    dr.put(PdfName.FONT, fontsTop);
                                }
                                markUsed(fontsTop);
                                PdfIndirectReference frefTop = (PdfIndirectReference)fontsTop.get(psn);
                                if (frefTop != null) {
                                    if (fref == null)
                                        fonts.put(psn, frefTop);
                                }
                                else if (fref == null) {
                                    FontDetails fd;
                                    if (bf.getFontType() == BaseFont.FONT_TYPE_DOCUMENT) {
                                        fd = new FontDetails(null, ((DocumentFont)bf).getIndirectReference(), bf);
                                    }
                                    else {
                                        bf.setSubset(false);
                                        fd = writer.addSimple(bf);
                                        localFonts.put(psn.toString().substring(1), bf);
                                    }
                                    fontsTop.put(psn, fd.getIndirectReference());
                                    fonts.put(psn, fd.getIndirectReference());
                                }
                                ByteBuffer buf = cb.getInternalBuffer();
                                buf.append(psn.getBytes()).append(' ').append(((Float)dao[DA_SIZE]).floatValue()).append(" Tf ");
                                if (dao[DA_COLOR] != null)
                                    cb.setColorFill((BaseColor)dao[DA_COLOR]);
                                PdfString s = new PdfString(cb.toString());
                                item.getMerged(k).put(PdfName.DA, s);
                                item.getWidget(k).put(PdfName.DA, s);
                                markUsed(item.getWidget(k));
                            }
                        }
                    }
                }
            }
            else if (name.equalsIgnoreCase("textcolor")) {
                for (int k = 0; k < item.size(); ++k) {
                    if (hit.isHit(k)) {
                        merged = item.getMerged( k );
                        da = merged.getAsString(PdfName.DA);
                        if (da != null) {
                            Object dao[] = splitDAelements(da.toUnicodeString());
                            PdfAppearance cb = new PdfAppearance();
                            if (dao[DA_FONT] != null) {
                                ByteBuffer buf = cb.getInternalBuffer();
                                buf.append(new PdfName((String)dao[DA_FONT]).getBytes()).append(' ').append(((Float)dao[DA_SIZE]).floatValue()).append(" Tf ");
                                cb.setColorFill((BaseColor)value);
                                PdfString s = new PdfString(cb.toString());
                                item.getMerged(k).put(PdfName.DA, s);
                                item.getWidget(k).put(PdfName.DA, s);
                                markUsed(item.getWidget(k));
                            }
                        }
                    }
                }
            }
            else if (name.equalsIgnoreCase("textsize")) {
                for (int k = 0; k < item.size(); ++k) {
                    if (hit.isHit(k)) {
                        merged = item.getMerged( k );
                        da = merged.getAsString(PdfName.DA);
                        if (da != null) {
                            Object dao[] = splitDAelements(da.toUnicodeString());
                            PdfAppearance cb = new PdfAppearance();
                            if (dao[DA_FONT] != null) {
                                ByteBuffer buf = cb.getInternalBuffer();
                                buf.append(new PdfName((String)dao[DA_FONT]).getBytes()).append(' ').append(((Float)value).floatValue()).append(" Tf ");
                                if (dao[DA_COLOR] != null)
                                    cb.setColorFill((BaseColor)dao[DA_COLOR]);
                                PdfString s = new PdfString(cb.toString());
                                item.getMerged(k).put(PdfName.DA, s);
                                item.getWidget(k).put(PdfName.DA, s);
                                markUsed(item.getWidget(k));
                            }
                        }
                    }
                }
            }
            else if (name.equalsIgnoreCase("bgcolor") || name.equalsIgnoreCase("bordercolor")) {
                PdfName dname = name.equalsIgnoreCase("bgcolor") ? PdfName.BG : PdfName.BC;
                for (int k = 0; k < item.size(); ++k) {
                    if (hit.isHit(k)) {
                        merged = item.getMerged( k );
                        PdfDictionary mk = merged.getAsDict(PdfName.MK);
                        if (mk == null) {
                            if (value == null)
                                return true;
                            mk = new PdfDictionary();
                            item.getMerged(k).put(PdfName.MK, mk);
                            item.getWidget(k).put(PdfName.MK, mk);
                            markUsed(item.getWidget(k));
                        } else {
                            markUsed( mk );
                        }
                        if (value == null)
                            mk.remove(dname);
                        else
                            mk.put(dname, PdfFormField.getMKColor((BaseColor)value));
                    }
                }
            }
            else
                return false;
            return true;
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Sets a field property. Valid property names are:
     * <p>
     * <ul>
     * <li>flags - a set of flags specifying various characteristics of the field's widget annotation.
     * The value of this entry replaces that of the F entry in the form's corresponding annotation dictionary.<br>
     * <li>setflags - a set of flags to be set (turned on) in the F entry of the form's corresponding
     * widget annotation dictionary. Bits equal to 1 cause the corresponding bits in F to be set to 1.<br>
     * <li>clrflags - a set of flags to be cleared (turned off) in the F entry of the form's corresponding
     * widget annotation dictionary. Bits equal to 1 cause the corresponding
     * bits in F to be set to 0.<br>
     * <li>fflags - a set of flags specifying various characteristics of the field. The value
     * of this entry replaces that of the Ff entry in the form's corresponding field dictionary.<br>
     * <li>setfflags - a set of flags to be set (turned on) in the Ff entry of the form's corresponding
     * field dictionary. Bits equal to 1 cause the corresponding bits in Ff to be set to 1.<br>
     * <li>clrfflags - a set of flags to be cleared (turned off) in the Ff entry of the form's corresponding
     * field dictionary. Bits equal to 1 cause the corresponding bits in Ff
     * to be set to 0.<br>
     * </ul>
     *
     * @param field the field name
     * @param name the property name
     * @param value the property value
     * @param inst an array of <CODE>int</CODE> indexing into <CODE>AcroField.Item.merged</CODE> elements to process.
     * Set to <CODE>null</CODE> to process all
     * @return <CODE>true</CODE> if the property exists, <CODE>false</CODE> otherwise
     */
    public boolean setFieldProperty(String field, String name, int value, int inst[]) {
        if (writer == null)
            throw new RuntimeException(MessageLocalization.getComposedMessage("this.acrofields.instance.is.read.only"));
        Item item = fields.get(field);
        if (item == null)
            return false;
        InstHit hit = new InstHit(inst);
        if (name.equalsIgnoreCase("flags")) {
            PdfNumber num = new PdfNumber(value);
            for (int k = 0; k < item.size(); ++k) {
                if (hit.isHit(k)) {
                    item.getMerged(k).put(PdfName.F, num);
                    item.getWidget(k).put(PdfName.F, num);
                    markUsed(item.getWidget(k));
                }
            }
        }
        else if (name.equalsIgnoreCase("setflags")) {
            for (int k = 0; k < item.size(); ++k) {
                if (hit.isHit(k)) {
                    PdfNumber num = item.getWidget(k).getAsNumber(PdfName.F);
                    int val = 0;
                    if (num != null)
                        val = num.intValue();
                    num = new PdfNumber(val | value);
                    item.getMerged(k).put(PdfName.F, num);
                    item.getWidget(k).put(PdfName.F, num);
                    markUsed(item.getWidget(k));
                }
            }
        }
        else if (name.equalsIgnoreCase("clrflags")) {
            for (int k = 0; k < item.size(); ++k) {
                if (hit.isHit(k)) {
                    PdfDictionary widget = item.getWidget( k );
                    PdfNumber num = widget.getAsNumber(PdfName.F);
                    int val = 0;
                    if (num != null)
                        val = num.intValue();
                    num = new PdfNumber(val & ~value);
                    item.getMerged(k).put(PdfName.F, num);
                    widget.put(PdfName.F, num);
                    markUsed(widget);
                }
            }
        }
        else if (name.equalsIgnoreCase("fflags")) {
            PdfNumber num = new PdfNumber(value);
            for (int k = 0; k < item.size(); ++k) {
                if (hit.isHit(k)) {
                    item.getMerged(k).put(PdfName.FF, num);
                    item.getValue(k).put(PdfName.FF, num);
                    markUsed(item.getValue(k));
                }
            }
        }
        else if (name.equalsIgnoreCase("setfflags")) {
            for (int k = 0; k < item.size(); ++k) {
                if (hit.isHit(k)) {
                    PdfDictionary valDict = item.getValue( k );
                    PdfNumber num = valDict.getAsNumber( PdfName.FF );
                    int val = 0;
                    if (num != null)
                        val = num.intValue();
                    num = new PdfNumber(val | value);
                    item.getMerged(k).put(PdfName.FF, num);
                    valDict.put(PdfName.FF, num);
                    markUsed(valDict);
                }
            }
        }
        else if (name.equalsIgnoreCase("clrfflags")) {
            for (int k = 0; k < item.size(); ++k) {
                if (hit.isHit(k)) {
                    PdfDictionary valDict = item.getValue( k );
                    PdfNumber num = valDict.getAsNumber(PdfName.FF);
                    int val = 0;
                    if (num != null)
                        val = num.intValue();
                    num = new PdfNumber(val & ~value);
                    item.getMerged(k).put(PdfName.FF, num);
                    valDict.put(PdfName.FF, num);
                    markUsed(valDict);
                }
            }
        }
        else
            return false;
        return true;
    }

    /**
     * Merges an XML data structure into this form.
     *
     * @param n the top node of the data structure
     * @throws java.io.IOException on error
     * @throws DocumentException o error
     */
    public void mergeXfaData(Node n) throws IOException, DocumentException {
        XfaForm.Xml2SomDatasets data = new XfaForm.Xml2SomDatasets(n);
        for (String string : data.getOrder()) {
            String name = string;
            String text = XfaForm.getNodeText(data.getName2Node().get(name));
            setField(name, text);
        }
    }

    /**
     * Sets the fields by FDF merging.
     *
     * @param fdf the FDF form
     * @throws IOException on error
     * @throws DocumentException on error
     */
    public void setFields(FdfReader fdf) throws IOException, DocumentException {
        HashMap<String, PdfDictionary> fd = fdf.getFields();
        for (String f: fd.keySet()) {
            String v = fdf.getFieldValue(f);
            if (v != null)
                setField(f, v);
        }
    }

    /**
     * Sets the fields by XFDF merging.
     *
     * @param xfdf the XFDF form
     * @throws IOException on error
     * @throws DocumentException on error
     */
    public void setFields(XfdfReader xfdf) throws IOException, DocumentException {
        HashMap<String, String> fd = xfdf.getFields();
        for (String f: fd.keySet()) {
            String v = xfdf.getFieldValue(f);
            if (v != null)
                setField(f, v);
            List<String> l = xfdf.getListValues(f);
            if (l != null)
            	setListSelection(v, l.toArray(new String[l.size()]));
        }
    }

    /**
     * Regenerates the field appearance.
     * This is useful when you change a field property, but not its value,
     * for instance form.setFieldProperty("f", "bgcolor", BaseColor.BLUE, null);
     * This won't have any effect, unless you use regenerateField("f") after changing
     * the property.
     *
     * @param name the fully qualified field name or the partial name in the case of XFA forms
     * @throws IOException on error
     * @throws DocumentException on error
     * @return <CODE>true</CODE> if the field was found and changed,
     * <CODE>false</CODE> otherwise
     */
    public boolean regenerateField(String name) throws IOException, DocumentException {
    	String value = getField(name);
        return setField(name, value, value);
    }

    /**
     * Sets the field value.
     *
     * @param name the fully qualified field name or the partial name in the case of XFA forms
     * @param value the field value
     * @throws IOException on error
     * @throws DocumentException on error
     * @return <CODE>true</CODE> if the field was found and changed,
     * <CODE>false</CODE> otherwise
     */
    public boolean setField(String name, String value) throws IOException, DocumentException {
        return setField(name, value, null);
    }

    /**
     * Sets the field value.
     *
     * @param name the fully qualified field name or the partial name in the case of XFA forms
     * @param value the field value
     * @param saveAppearance save the current appearance of the field or not
     * @throws IOException on error
     * @throws DocumentException on error
     * @return <CODE>true</CODE> if the field was found and changed,
     * <CODE>false</CODE> otherwise
     */
    public boolean setField(String name, String value, boolean saveAppearance) throws IOException, DocumentException {
        return setField(name, value, null, saveAppearance);
    }

    /**
     * Sets the rich value for the given field.  See <a href="http://www.adobe.com/content/dam/Adobe/en/devnet/pdf/pdfs/PDF32000_2008.pdf">PDF Reference</a> chapter
     * 12.7.3.4 (Rich Text) and 12.7.4.3 (Text Fields) for further details. Note that iText doesn't create an appearance for Rich Text fields.
     * So you either need to use XML Worker to create an appearance (/N entry in the /AP dictionary), or you need to use setGenerateAppearances(false) to tell the viewer
     * that iText didn't create any appearances.
     * @param name  Field name
     * @param richValue html markup
     * @return success/failure (will fail if the field isn't found, isn't a text field, or doesn't support rich text)
     * @throws DocumentException
     * @throws IOException
     * @since 5.0.6
     */
    public boolean setFieldRichValue(String name, String richValue) throws DocumentException, IOException {
        if (writer == null) {
        	// can't set field values: fail
            throw new DocumentException(MessageLocalization.getComposedMessage("this.acrofields.instance.is.read.only"));
        }

    	Item item = getFieldItem(name);
    	if (item == null) {
    		// can't find the field: fail.
    		return false;
    	}

    	if (getFieldType(name) != FIELD_TYPE_TEXT) {
    		// field isn't a text field: fail
    		return false;
    	}

    	PdfDictionary merged = item.getMerged(0);
    	PdfNumber ffNum = merged.getAsNumber(PdfName.FF);
    	int flagVal = 0;
    	if (ffNum != null) {
    		flagVal = ffNum.intValue();
    	}
    	if ((flagVal & PdfFormField.FF_RICHTEXT) == 0) {
    		// text field doesn't support rich text: fail
    		return false;
    	}

    	PdfString richString = new PdfString(richValue);
    	item.writeToAll(PdfName.RV, richString, Item.WRITE_MERGED | Item.WRITE_VALUE);

		InputStream is = new ByteArrayInputStream(richValue.getBytes());
		PdfString valueString = new PdfString(XmlToTxt.parse(is));
		item.writeToAll(PdfName.V, valueString, Item.WRITE_MERGED | Item.WRITE_VALUE);
    	return true;
    }

    /**
     * Sets the field value and the display string. The display string
     * is used to build the appearance in the cases where the value
     * is modified by Acrobat with JavaScript and the algorithm is
     * known.
     *
     * @param name the fully qualified field name or the partial name in the case of XFA forms
     * @param value the field value
     * @param display the string that is used for the appearance. If <CODE>null</CODE>
     * the <CODE>value</CODE> parameter will be used
     * @return <CODE>true</CODE> if the field was found and changed,
     * <CODE>false</CODE> otherwise
     * @throws IOException on error
     * @throws DocumentException on error
     */
    public boolean setField(String name, String value, String display) throws IOException, DocumentException {
        return setField(name, value, display, false);
    }

    /**
     * Sets the field value and the display string. The display string
     * is used to build the appearance in the cases where the value
     * is modified by Acrobat with JavaScript and the algorithm is
     * known.
     *
     * @param name the fully qualified field name or the partial name in the case of XFA forms
     * @param value the field value
     * @param display the string that is used for the appearance. If <CODE>null</CODE>
     * the <CODE>value</CODE> parameter will be used
     * @param saveAppearance save the current appearance of the field or not
     * @return <CODE>true</CODE> if the field was found and changed,
     * <CODE>false</CODE> otherwise
     * @throws IOException on error
     * @throws DocumentException on error
     */
    public boolean setField(String name, String value, String display, boolean saveAppearance) throws IOException, DocumentException {
        if (writer == null)
            throw new DocumentException(MessageLocalization.getComposedMessage("this.acrofields.instance.is.read.only"));
        if (xfa.isXfaPresent()) {
            name = xfa.findFieldName(name, this);
            if (name == null)
                return false;
            String shortName = XfaForm.Xml2Som.getShortName(name);
            Node xn = xfa.findDatasetsNode(shortName);
            if (xn == null) {
                xn = xfa.getDatasetsSom().insertNode(xfa.getDatasetsNode(), shortName);
            }
            xfa.setNodeText(xn, value);
        }
        Item item = fields.get(name);
        if (item == null)
            return false;
        PdfDictionary merged = item.getMerged( 0 );
        PdfName type = merged.getAsName(PdfName.FT);
        if (PdfName.TX.equals(type)) {
            PdfNumber maxLen = merged.getAsNumber(PdfName.MAXLEN);
            int len = 0;
            if (maxLen != null)
                len = maxLen.intValue();
            if (len > 0)
                value = value.substring(0, Math.min(len, value.length()));
        }
        if (display == null)
            display = value;
        if (PdfName.TX.equals(type) || PdfName.CH.equals(type)) {
            PdfString v = new PdfString(value, PdfObject.TEXT_UNICODE);
            for (int idx = 0; idx < item.size(); ++idx) {
                PdfDictionary valueDic = item.getValue(idx);
                valueDic.put(PdfName.V, v);
                valueDic.remove(PdfName.I);
                markUsed(valueDic);
                merged = item.getMerged(idx);
                merged.remove(PdfName.I);
                merged.put(PdfName.V, v);
                PdfDictionary widget = item.getWidget(idx);
                if (generateAppearances) {
                    PdfAppearance app = getAppearance(merged, display, name);
                    if (PdfName.CH.equals(type)) {
                        PdfNumber n = new PdfNumber(topFirst);
                        widget.put(PdfName.TI, n);
                        merged.put(PdfName.TI, n);
                    }
                    PdfDictionary appDic = widget.getAsDict(PdfName.AP);
                    if (appDic == null) {
                        appDic = new PdfDictionary();
                        widget.put(PdfName.AP, appDic);
                        merged.put(PdfName.AP, appDic);
                    }
                    appDic.put(PdfName.N, app.getIndirectReference());
                    writer.releaseTemplate(app);
                }
                else {
                    widget.remove(PdfName.AP);
                    merged.remove(PdfName.AP);
                }
                markUsed(widget);
            }
            return true;
        }
        else if (PdfName.BTN.equals(type)) {
            PdfNumber ff = item.getMerged(0).getAsNumber(PdfName.FF);
            int flags = 0;
            if (ff != null)
                flags = ff.intValue();
            if ((flags & PdfFormField.FF_PUSHBUTTON) != 0) {
                //we'll assume that the value is an image in base64
                Image img;
                try {
                    img = Image.getInstance(Base64.decode(value));
                }
                catch (Exception e) {
                    return false;
                }
                PushbuttonField pb = getNewPushbuttonFromField(name);
                pb.setImage(img);
                replacePushbuttonField(name, pb.getField());
                return true;
            }
            PdfName v = new PdfName(value);
            ArrayList<String> lopt = new ArrayList<String>();
            PdfArray opts = item.getValue(0).getAsArray(PdfName.OPT);
            if (opts != null) {
                for (int k = 0; k < opts.size(); ++k) {
                    PdfString valStr = opts.getAsString(k);
                    if (valStr != null)
                        lopt.add(valStr.toUnicodeString());
                    else
                        lopt.add(null);
                }
            }
            int vidx = lopt.indexOf(value);
            PdfName vt;
            if (vidx >= 0)
                vt = new PdfName(String.valueOf(vidx));
            else
                vt = v;
            for (int idx = 0; idx < item.size(); ++idx) {
                merged = item.getMerged(idx);
                PdfDictionary widget = item.getWidget(idx);
                PdfDictionary valDict = item.getValue(idx);
                markUsed(item.getValue(idx));
                valDict.put(PdfName.V, vt);
                merged.put(PdfName.V, vt);
                markUsed(widget);
                PdfDictionary appDic = widget.getAsDict(PdfName.AP);
                if (appDic == null)
                	return false;
                PdfDictionary normal = appDic.getAsDict(PdfName.N);
                if (isInAP(normal,  vt) || normal == null) {
                    merged.put(PdfName.AS, vt);
                    widget.put(PdfName.AS, vt);
                }
                else {
                    merged.put(PdfName.AS, PdfName.Off);
                    widget.put(PdfName.AS, PdfName.Off);
                }
                if (generateAppearances && !saveAppearance) {
                    PdfAppearance app = getAppearance(merged, display, name);
                    if (normal != null)
                        normal.put(merged.getAsName(PdfName.AS), app.getIndirectReference());
                    else
                        appDic.put(PdfName.N, app.getIndirectReference());
                    writer.releaseTemplate(app);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Sets different values in a list selection.
     * No appearance is generated yet; nor does the code check if multiple select is allowed.
     *
     * @param	name	the name of the field
     * @param	value	an array with values that need to be selected
     * @return	true only if the field value was changed
     * @since 2.1.4
     */
	public boolean setListSelection(String name, String[] value) throws IOException, DocumentException {
        Item item = getFieldItem(name);
        if (item == null)
            return false;
        PdfDictionary merged = item.getMerged( 0 );
        PdfName type = merged.getAsName(PdfName.FT);
        if (!PdfName.CH.equals(type)) {
        	return false;
        }
        String[] options = getListOptionExport(name);
        PdfArray array = new PdfArray();
        for (String element : value) {
        	for (int j = 0; j < options.length; j++) {
        		if (options[j].equals(element)) {
        			array.add(new PdfNumber(j));
        			break;
        		}
        	}
        }
        item.writeToAll(PdfName.I, array, Item.WRITE_MERGED | Item.WRITE_VALUE);

        PdfArray vals = new PdfArray();
        for (int i = 0; i < value.length; ++i) {
        	vals.add( new PdfString( value[i] ) );
        }
        item.writeToAll(PdfName.V, vals, Item.WRITE_MERGED | Item.WRITE_VALUE);

        PdfAppearance app = getAppearance( merged, value, name );

        PdfDictionary apDic = new PdfDictionary();
        apDic.put( PdfName.N, app.getIndirectReference() );
        item.writeToAll(PdfName.AP, apDic, Item.WRITE_MERGED | Item.WRITE_WIDGET);

        writer.releaseTemplate( app );

        item.markUsed( this, Item.WRITE_VALUE | Item.WRITE_WIDGET );
        return true;
	}

    boolean isInAP(PdfDictionary nDic, PdfName check) {
        return nDic != null && nDic.get(check) != null;
    }

    /**
     * Gets all the fields. The fields are keyed by the fully qualified field name and
     * the value is an instance of <CODE>AcroFields.Item</CODE>.
     *
     * @return all the fields
     */
    public Map<String, Item> getFields() {
        return fields;
    }

    /**
     * Gets the field structure.
     *
     * @param name the name of the field
     * @return the field structure or <CODE>null</CODE> if the field
     * does not exist
     */
    public Item getFieldItem(String name) {
        if (xfa.isXfaPresent()) {
            name = xfa.findFieldName(name, this);
            if (name == null)
                return null;
        }
        return fields.get(name);
    }

    /**
     * Gets the long XFA translated name.
     *
     * @param name the name of the field
     * @return the long field name
     */
    public String getTranslatedFieldName(String name) {
        if (xfa.isXfaPresent()) {
            String namex = xfa.findFieldName(name, this);
            if (namex != null)
                name = namex;
        }
        return name;
    }

    /**
     * Gets the field box positions in the document. The return is an array of <CODE>float</CODE>
     * multiple of 5. For each of this groups the values are: [page, llx, lly, urx,
     * ury]. The coordinates have the page rotation in consideration.
     *
     * @param name the field name
     * @return the positions or <CODE>null</CODE> if field does not exist
     */
    public List<FieldPosition> getFieldPositions(String name) {
        Item item = getFieldItem(name);
        if (item == null)
            return null;
        ArrayList<FieldPosition> ret = new ArrayList<FieldPosition>();
        for (int k = 0; k < item.size(); ++k) {
            try {
                PdfDictionary wd = item.getWidget(k);
                PdfArray rect = wd.getAsArray(PdfName.RECT);
                if (rect == null)
                    continue;
                Rectangle r = PdfReader.getNormalizedRectangle(rect);
                int page = item.getPage(k).intValue();
                int rotation = reader.getPageRotation(page);
                FieldPosition fp = new FieldPosition();
                fp.page = page;
                if (rotation != 0) {
                    Rectangle pageSize = reader.getPageSize(page);
                    switch (rotation) {
                        case 270:
                            r = new Rectangle(
                                pageSize.getTop() - r.getBottom(),
                                r.getLeft(),
                                pageSize.getTop() - r.getTop(),
                                r.getRight());
                            break;
                        case 180:
                            r = new Rectangle(
                                pageSize.getRight() - r.getLeft(),
                                pageSize.getTop() - r.getBottom(),
                                pageSize.getRight() - r.getRight(),
                                pageSize.getTop() - r.getTop());
                            break;
                        case 90:
                            r = new Rectangle(
                                r.getBottom(),
                                pageSize.getRight() - r.getLeft(),
                                r.getTop(),
                                pageSize.getRight() - r.getRight());
                            break;
                    }
                    r.normalize();
                }
                fp.position = r;
                ret.add(fp);
            }
            catch (Exception e) {
                // empty on purpose
            }
        }
        return ret;
    }

    private int removeRefFromArray(PdfArray array, PdfObject refo) {
        if (refo == null || !refo.isIndirect())
            return array.size();
        PdfIndirectReference ref = (PdfIndirectReference)refo;
        for (int j = 0; j < array.size(); ++j) {
            PdfObject obj = array.getPdfObject(j);
            if (!obj.isIndirect())
                continue;
            if (((PdfIndirectReference)obj).getNumber() == ref.getNumber())
                array.remove(j--);
        }
        return array.size();
    }

    /**
     * Removes all the fields from <CODE>page</CODE>.
     *
     * @param page the page to remove the fields from
     * @return <CODE>true</CODE> if any field was removed, <CODE>false otherwise</CODE>
     */
    public boolean removeFieldsFromPage(int page) {
        if (page < 1)
            return false;
        String names[] = new String[fields.size()];
        fields.keySet().toArray(names);
        boolean found = false;
        for (int k = 0; k < names.length; ++k) {
            boolean fr = removeField(names[k], page);
            found = found || fr;
        }
        return found;
    }

    /**
     * Removes a field from the document. If page equals -1 all the fields with this
     * <CODE>name</CODE> are removed from the document otherwise only the fields in
     * that particular page are removed.
     *
     * @param name the field name
     * @param page the page to remove the field from or -1 to remove it from all the pages
     * @return <CODE>true</CODE> if the field exists, <CODE>false otherwise</CODE>
     */
    public boolean removeField(String name, int page) {
        Item item = getFieldItem(name);
        if (item == null)
            return false;
        PdfDictionary acroForm = (PdfDictionary)PdfReader.getPdfObject(reader.getCatalog().get(PdfName.ACROFORM), reader.getCatalog());

        if (acroForm == null)
            return false;
        PdfArray arrayf = acroForm.getAsArray(PdfName.FIELDS);
        if (arrayf == null)
            return false;
        for (int k = 0; k < item.size(); ++k) {
            int pageV = item.getPage(k).intValue();
            if (page != -1 && page != pageV)
                continue;
            PdfIndirectReference ref = item.getWidgetRef(k);
            PdfDictionary wd = item.getWidget( k );
            PdfDictionary pageDic = reader.getPageN(pageV); //Returns null for floating, orphaned fields
            PdfArray annots = pageDic != null ? pageDic.getAsArray(PdfName.ANNOTS): null; //Account for floating, orphaned fields
            if (annots != null) {
                if (removeRefFromArray(annots, ref) == 0) {
                    pageDic.remove(PdfName.ANNOTS);
                    markUsed(pageDic);
                }
                else
                    markUsed(annots);
            }
            PdfReader.killIndirect(ref);
            PdfIndirectReference kid = ref;
            while ((ref = wd.getAsIndirectObject(PdfName.PARENT)) != null) {
                wd = wd.getAsDict( PdfName.PARENT );
                if(wd == null) break;
                PdfArray kids = wd.getAsArray(PdfName.KIDS);
                if (removeRefFromArray(kids, kid) != 0)
                    break;
                kid = ref;
                PdfReader.killIndirect(ref);
            }
            if (ref == null) {
                removeRefFromArray(arrayf, kid);
                markUsed(arrayf);
            }
            if (page != -1) {
                item.remove( k );
                --k;
            }
        }
        if (page == -1 || item.size() == 0)
            fields.remove(name);
        return true;
    }

    /**
     * Removes a field from the document.
     *
     * @param name the field name
     * @return <CODE>true</CODE> if the field exists, <CODE>false otherwise</CODE>
     */
    public boolean removeField(String name) {
        return removeField(name, -1);
    }

    /**
     * Gets the property generateAppearances.
     *
     * @return the property generateAppearances
     */
    public boolean isGenerateAppearances() {
        return generateAppearances;
    }

    /**
     * Sets the option to generate appearances. Not generating appearances
     * will speed-up form filling but the results can be
     * unexpected in Acrobat. Don't use it unless your environment is well
     * controlled. The default is <CODE>true</CODE>.
     *
     * @param generateAppearances the option to generate appearances
     */
    public void setGenerateAppearances(boolean generateAppearances) {
        this.generateAppearances = generateAppearances;
        PdfDictionary top = reader.getCatalog().getAsDict(PdfName.ACROFORM);
        if (generateAppearances)
            top.remove(PdfName.NEEDAPPEARANCES);
        else
            top.put(PdfName.NEEDAPPEARANCES, PdfBoolean.PDFTRUE);
    }

    /** The field representations for retrieval and modification. */
    public static class Item {

        /**
         * <CODE>writeToAll</CODE> constant.
         *
         *  @since 2.1.5
         */
        public static final int WRITE_MERGED = 1;

        /**
         * <CODE>writeToAll</CODE> and <CODE>markUsed</CODE> constant.
         *
         *  @since 2.1.5
         */
        public static final int WRITE_WIDGET = 2;

        /**
         * <CODE>writeToAll</CODE> and <CODE>markUsed</CODE> constant.
         *
         *  @since 2.1.5
         */
        public static final int WRITE_VALUE = 4;

        /**
         * This function writes the given key/value pair to all the instances
         * of merged, widget, and/or value, depending on the <code>writeFlags</code> setting
         *
         * @since 2.1.5
         *
         * @param key        you'll never guess what this is for.
         * @param value      if value is null, the key will be removed
         * @param writeFlags ORed together WRITE_* flags
         */
        public void writeToAll(PdfName key, PdfObject value, int writeFlags) {
            int i;
            PdfDictionary curDict = null;
            if ((writeFlags & WRITE_MERGED) != 0) {
                for (i = 0; i < merged.size(); ++i) {
                    curDict = getMerged(i);
                    curDict.put(key, value);
                }
            }
            if ((writeFlags & WRITE_WIDGET) != 0) {
                for (i = 0; i < widgets.size(); ++i) {
                    curDict = getWidget(i);
                    curDict.put(key, value);
                }
            }
            if ((writeFlags & WRITE_VALUE) != 0) {
                for (i = 0; i < values.size(); ++i) {
                    curDict = getValue(i);
                    curDict.put(key, value);
                }
            }
        }

        /**
         * Mark all the item dictionaries used matching the given flags
         *
         * @since 2.1.5
         * @param writeFlags WRITE_MERGED is ignored
         */
        public void markUsed( AcroFields parentFields, int writeFlags ) {
            if ((writeFlags & WRITE_VALUE) != 0) {
                for (int i = 0; i < size(); ++i) {
                    parentFields.markUsed( getValue( i ) );
                }
            }
            if ((writeFlags & WRITE_WIDGET) != 0) {
                for (int i = 0; i < size(); ++i) {
                    parentFields.markUsed(getWidget(i));
                }
            }
        }

        /**
         * An array of <CODE>PdfDictionary</CODE> where the value tag /V
         * is present.
         *
         * @since 5.0.2 public is now protected
         */
        protected ArrayList<PdfDictionary> values = new ArrayList<PdfDictionary>();

        /**
         * An array of <CODE>PdfDictionary</CODE> with the widgets.
         *
         * @since 5.0.2 public is now protected
         */
        protected ArrayList<PdfDictionary> widgets = new ArrayList<PdfDictionary>();

        /**
         * An array of <CODE>PdfDictionary</CODE> with the widget references.
         *
         * @since 5.0.2 public is now protected
         */
        protected ArrayList<PdfIndirectReference> widget_refs = new ArrayList<PdfIndirectReference>();

        /**
         * An array of <CODE>PdfDictionary</CODE> with all the field
         * and widget tags merged.
         *
         * @since 5.0.2 public is now protected
         */
        protected ArrayList<PdfDictionary> merged = new ArrayList<PdfDictionary>();

        /**
         * An array of <CODE>Integer</CODE> with the page numbers where
         * the widgets are displayed.
         *
         * @since 5.0.2 public is now protected
         */
        protected ArrayList<Integer> page = new ArrayList<Integer>();
        /**
         * An array of <CODE>Integer</CODE> with the tab order of the field in the page.
         *
         * @since 5.0.2 public is now protected
         */
        protected ArrayList<Integer> tabOrder = new ArrayList<Integer>();

        /**
         * Preferred method of determining the number of instances
         * of a given field.
         *
         * @since 2.1.5
         * @return number of instances
         */
        public int size() {
            return values.size();
        }

        /**
         * Remove the given instance from this item.  It is possible to
         * remove all instances using this function.
         *
         * @since 2.1.5
         * @param killIdx
         */
        void remove(int killIdx) {
            values.remove(killIdx);
            widgets.remove(killIdx);
            widget_refs.remove(killIdx);
            merged.remove(killIdx);
            page.remove(killIdx);
            tabOrder.remove(killIdx);
        }

        /**
         * Retrieve the value dictionary of the given instance
         *
         * @since 2.1.5
         * @param idx instance index
         * @return dictionary storing this instance's value.  It may be shared across instances.
         */
        public PdfDictionary getValue(int idx) {
            return values.get(idx);
        }

        /**
         * Add a value dict to this Item
         *
         * @since 2.1.5
         * @param value new value dictionary
         */
        void addValue(PdfDictionary value) {
            values.add(value);
        }

        /**
         * Retrieve the widget dictionary of the given instance
         *
         * @since 2.1.5
         * @param idx instance index
         * @return The dictionary found in the appropriate page's Annot array.
         */
        public PdfDictionary getWidget(int idx) {
            return widgets.get(idx);
        }

        /**
         * Add a widget dict to this Item
         *
         * @since 2.1.5
         * @param widget
         */
        void addWidget(PdfDictionary widget) {
            widgets.add(widget);
        }

        /**
         * Retrieve the reference to the given instance
         *
         * @since 2.1.5
         * @param idx instance index
         * @return reference to the given field instance
         */
        public PdfIndirectReference getWidgetRef(int idx) {
            return widget_refs.get(idx);
        }

        /**
         * Add a widget ref to this Item
         *
         * @since 2.1.5
         * @param widgRef
         */
        void addWidgetRef(PdfIndirectReference widgRef) {
            widget_refs.add(widgRef);
        }

        /**
         * Retrieve the merged dictionary for the given instance.  The merged
         * dictionary contains all the keys present in parent fields, though they
         * may have been overwritten (or modified?) by children.
         * Example: a merged radio field dict will contain /V
         *
         * @since 2.1.5
         * @param idx  instance index
         * @return the merged dictionary for the given instance
         */
        public PdfDictionary getMerged(int idx) {
            return merged.get(idx);
        }

        /**
         * Adds a merged dictionary to this Item.
         *
         * @since 2.1.5
         * @param mergeDict
         */
        void addMerged(PdfDictionary mergeDict) {
            merged.add(mergeDict);
        }

        /**
         * Retrieve the page number of the given instance
         *
         * @since 2.1.5
         * @param idx
         * @return remember, pages are "1-indexed", not "0-indexed" like field instances.
         */
        public Integer getPage(int idx) {
            return page.get(idx);
        }

        /**
         * Adds a page to the current Item.
         *
         * @since 2.1.5
         * @param pg
         */
        void addPage(int pg) {
            page.add(Integer.valueOf(pg));
        }

        /**
         * forces a page value into the Item.
         *
         * @since 2.1.5
         * @param idx
         */
        void forcePage(int idx, int pg) {
            page.set(idx, Integer.valueOf( pg ));
        }

        /**
         * Gets the tabOrder.
         *
         * @since 2.1.5
         * @param idx
         * @return tab index of the given field instance
         */
        public Integer getTabOrder(int idx) {
            return tabOrder.get(idx);
        }

        /**
         * Adds a tab order value to this Item.
         *
         * @since 2.1.5
         * @param order
         */
        void addTabOrder(int order) {
            tabOrder.add(Integer.valueOf(order));
        }
    }

    private static class InstHit {
        IntHashtable hits;
        public InstHit(int inst[]) {
            if (inst == null)
                return;
            hits = new IntHashtable();
            for (int k = 0; k < inst.length; ++k)
                hits.put(inst[k], 1);
        }

        public boolean isHit(int n) {
            if (hits == null)
                return true;
            return hits.containsKey(n);
        }
    }

    /**
     * Clears a signed field.
     * @param name the field name
     * @return true if the field was signed, false if the field was not signed or not found
     * @since 5.0.5
     */
    public boolean clearSignatureField(String name) {
        sigNames = null;
        getSignatureNames();
        if (!sigNames.containsKey(name))
            return false;
        Item sig = fields.get(name);
        sig.markUsed(this, Item.WRITE_VALUE | Item.WRITE_WIDGET);
        int n = sig.size();
        for (int k = 0; k < n; ++k) {
            clearSigDic(sig.getMerged(k));
            clearSigDic(sig.getWidget(k));
            clearSigDic(sig.getValue(k));
        }
        return true;
    }

    private static void clearSigDic(PdfDictionary dic) {
        dic.remove(PdfName.AP);
        dic.remove(PdfName.AS);
        dic.remove(PdfName.V);
        dic.remove(PdfName.DV);
        dic.remove(PdfName.SV);
        dic.remove(PdfName.FF);
        dic.put(PdfName.F, new PdfNumber(PdfAnnotation.FLAGS_PRINT));
    }

    private ArrayList<String> orderedSignatureNames;

    /**
     * Gets the field names that have signatures and are signed.
     *
     * @return the field names that have signatures and are signed
     */
    public ArrayList<String> getSignatureNames() {
        if (sigNames != null)
            return new ArrayList<String>(orderedSignatureNames);
        sigNames = new HashMap<String, int[]>();
        orderedSignatureNames = new ArrayList<String>();
        ArrayList<Object[]> sorter = new ArrayList<Object[]>();
        for (Map.Entry<String, Item> entry: fields.entrySet()) {
            Item item = entry.getValue();
            PdfDictionary merged = item.getMerged(0);
            if (!PdfName.SIG.equals(merged.get(PdfName.FT)))
                continue;
            PdfDictionary v = merged.getAsDict(PdfName.V);
            if (v == null)
                continue;
            PdfString contents = v.getAsString(PdfName.CONTENTS);
            if (contents == null)
                continue;
            PdfArray ro = v.getAsArray(PdfName.BYTERANGE);
            if (ro == null)
                continue;
            int rangeSize = ro.size();
            if (rangeSize < 2)
                continue;
            int length = ro.getAsNumber(rangeSize - 1).intValue() + ro.getAsNumber(rangeSize - 2).intValue();
            sorter.add(new Object[]{entry.getKey(), new int[]{length, 0}});
        }
        Collections.sort(sorter, new SorterComparator());
        if (!sorter.isEmpty()) {
            if (((int[])sorter.get(sorter.size() - 1)[1])[0] == reader.getFileLength())
                totalRevisions = sorter.size();
            else
                totalRevisions = sorter.size() + 1;
            for (int k = 0; k < sorter.size(); ++k) {
                Object objs[] = sorter.get(k);
                String name = (String)objs[0];
                int p[] = (int[])objs[1];
                p[1] = k + 1;
                sigNames.put(name, p);
                orderedSignatureNames.add(name);
            }
        }
        return new ArrayList<String>(orderedSignatureNames);
    }

    /**
     * Gets the field names that have blank signatures.
     *
     * @return the field names that have blank signatures
     */
    public ArrayList<String> getBlankSignatureNames() {
        getSignatureNames();
        ArrayList<String> sigs = new ArrayList<String>();
        for (Map.Entry<String, Item> entry: fields.entrySet()) {
            Item item = entry.getValue();
            PdfDictionary merged = item.getMerged(0);
            if (!PdfName.SIG.equals(merged.getAsName(PdfName.FT)))
                continue;
            if (sigNames.containsKey(entry.getKey()))
                continue;
            sigs.add(entry.getKey());
        }
        return sigs;
    }

    /**
     * Gets the signature dictionary, the one keyed by /V.
     *
     * @param name the field name
     * @return the signature dictionary keyed by /V or <CODE>null</CODE> if the field is not
     * a signature
     */
    public PdfDictionary getSignatureDictionary(String name) {
        getSignatureNames();
        name = getTranslatedFieldName(name);
        if (!sigNames.containsKey(name))
            return null;
        Item item = fields.get(name);
        PdfDictionary merged = item.getMerged(0);
        return merged.getAsDict(PdfName.V);
    }

    /**
     * Gets a reference to the normal appearance of a field.
     *
     * @param name the field name
     * @return a reference to the /N entry of the /AP dictionary or <CODE>null</CODE> if the field is not found
     */
    public PdfIndirectReference getNormalAppearance(String name) {
        getSignatureNames();
        name = getTranslatedFieldName(name);
        Item item = fields.get(name);
        if (item == null)
        	return null;
        PdfDictionary merged = item.getMerged(0);
        PdfDictionary ap = merged.getAsDict(PdfName.AP);
        if (ap == null)
        	return null;
        PdfIndirectReference ref = ap.getAsIndirectObject(PdfName.N);
        if (ref == null)
        	return null;
        return ref;
    }

    /**
     * Checks is the signature covers the entire document or just part of it.
     *
     * @param name the signature field name
     * @return <CODE>true</CODE> if the signature covers the entire document,
     * <CODE>false</CODE> otherwise
     */
    public boolean signatureCoversWholeDocument(String name) {
        getSignatureNames();
        name = getTranslatedFieldName(name);
        if (!sigNames.containsKey(name))
            return false;
        return sigNames.get(name)[0] == reader.getFileLength();
    }

    /**
     * Verifies a signature. An example usage is:
     * <p>
     * <pre>
     * KeyStore kall = PdfPKCS7.loadCacertsKeyStore();
     * PdfReader reader = new PdfReader("my_signed_doc.pdf");
     * AcroFields af = reader.getAcroFields();
     * ArrayList names = af.getSignatureNames();
     * for (int k = 0; k &lt; names.size(); ++k) {
     *    String name = (String)names.get(k);
     *    System.out.println("Signature name: " + name);
     *    System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
     *    PdfPKCS7 pk = af.verifySignature(name);
     *    Calendar cal = pk.getSignDate();
     *    Certificate pkc[] = pk.getCertificates();
     *    System.out.println("Subject: " + PdfPKCS7.getSubjectFields(pk.getSigningCertificate()));
     *    System.out.println("Document modified: " + !pk.verify());
     *    Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
     *    if (fails == null)
     *        System.out.println("Certificates verified against the KeyStore");
     *    else
     *        System.out.println("Certificate failed: " + fails[1]);
     * }
     * </pre>
     *
     * @param name the signature field name
     * @return a <CODE>PdfPKCS7</CODE> class to continue the verification
     */
    public PdfPKCS7 verifySignature(String name) {
        return verifySignature(name, null);
    }

    /**
     * Verifies a signature. An example usage is:
     * <p>
     * <pre>
     * KeyStore kall = PdfPKCS7.loadCacertsKeyStore();
     * PdfReader reader = new PdfReader("my_signed_doc.pdf");
     * AcroFields af = reader.getAcroFields();
     * ArrayList names = af.getSignatureNames();
     * for (int k = 0; k &lt; names.size(); ++k) {
     *    String name = (String)names.get(k);
     *    System.out.println("Signature name: " + name);
     *    System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
     *    PdfPKCS7 pk = af.verifySignature(name);
     *    Calendar cal = pk.getSignDate();
     *    Certificate pkc[] = pk.getCertificates();
     *    System.out.println("Subject: " + PdfPKCS7.getSubjectFields(pk.getSigningCertificate()));
     *    System.out.println("Document modified: " + !pk.verify());
     *    Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
     *    if (fails == null)
     *        System.out.println("Certificates verified against the KeyStore");
     *    else
     *        System.out.println("Certificate failed: " + fails[1]);
     * }
     * </pre>
     *
     * @param name the signature field name
     * @param provider the provider or <code>null</code> for the default provider
     * @return a <CODE>PdfPKCS7</CODE> class to continue the verification
     */
    public PdfPKCS7 verifySignature(String name, String provider) {
        PdfDictionary v = getSignatureDictionary(name);
        if (v == null)
            return null;
        try {
            PdfName sub = v.getAsName(PdfName.SUBFILTER);
            PdfString contents = v.getAsString(PdfName.CONTENTS);
            PdfPKCS7 pk = null;
            if (sub.equals(PdfName.ADBE_X509_RSA_SHA1)) {
                PdfString cert = v.getAsString(PdfName.CERT);
                if (cert == null)
                    cert = v.getAsArray(PdfName.CERT).getAsString(0);
                if(!reader.isEncrypted()) {
                    pk = new PdfPKCS7(contents.getOriginalBytes(), cert.getBytes(), provider);
                }else{
                    pk = new PdfPKCS7(contents.getBytes(), cert.getBytes(), provider);
                }
            }
            else{
                if(!reader.isEncrypted()){
                    pk = new PdfPKCS7(contents.getOriginalBytes(), sub, provider);
                }else{
                    pk = new PdfPKCS7(contents.getBytes(),sub,provider);
                }
            }

            updateByteRange(pk, v);
            PdfString str = v.getAsString(PdfName.M);
            if (str != null)
                pk.setSignDate(PdfDate.decode(str.toString()));
            PdfObject obj = PdfReader.getPdfObject(v.get(PdfName.NAME));
            if (obj != null) {
              if (obj.isString())
                pk.setSignName(((PdfString)obj).toUnicodeString());
              else if(obj.isName())
                pk.setSignName(PdfName.decodeName(obj.toString()));
            }
            str = v.getAsString(PdfName.REASON);
            if (str != null)
                pk.setReason(str.toUnicodeString());
            str = v.getAsString(PdfName.LOCATION);
            if (str != null)
                pk.setLocation(str.toUnicodeString());
            return pk;
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void updateByteRange(PdfPKCS7 pkcs7, PdfDictionary v) {
        PdfArray b = v.getAsArray(PdfName.BYTERANGE);
        RandomAccessFileOrArray rf = reader.getSafeFile();
    	InputStream rg = null;
        try {
        	rg = new RASInputStream(new RandomAccessSourceFactory().createRanged(rf.createSourceView(), b.asLongArray()));
            byte buf[] = new byte[8192];
            int rd;
            while ((rd = rg.read(buf, 0, buf.length)) > 0) {
                pkcs7.update(buf, 0, rd);
            }
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        } finally {
        	try {
				if (rg != null) rg.close();
			} catch (IOException e) {
				// this really shouldn't ever happen - the source view we use is based on a Safe view, which is a no-op anyway
				throw new ExceptionConverter(e);
			}
        }
    }

    private void markUsed(PdfObject obj) {
        if (!append)
            return;
        ((PdfStamperImp)writer).markUsed(obj);
    }

    /**
     * Gets the total number of revisions this document has.
     *
     * @return the total number of revisions
     */
    public int getTotalRevisions() {
        getSignatureNames();
        return this.totalRevisions;
    }

    /**
     * Gets this <CODE>field</CODE> revision.
     *
     * @param field the signature field name
     * @return the revision or zero if it's not a signature field
     */
    public int getRevision(String field) {
        getSignatureNames();
        field = getTranslatedFieldName(field);
        if (!sigNames.containsKey(field))
            return 0;
        return sigNames.get(field)[1];
    }

    /**
     * Extracts a revision from the document.
     *
     * @param field the signature field name
     * @return an <CODE>InputStream</CODE> covering the revision. Returns <CODE>null</CODE> if
     * it's not a signature field
     * @throws IOException on error
     */
    public InputStream extractRevision(String field) throws IOException {
        getSignatureNames();
        field = getTranslatedFieldName(field);
        if (!sigNames.containsKey(field))
            return null;
        int length = sigNames.get(field)[0];
        RandomAccessFileOrArray raf = reader.getSafeFile();
        return new RASInputStream(new WindowRandomAccessSource(raf.createSourceView(), 0, length));
    }

    /**
     * Gets the appearances cache.
     *
     * @return the appearances cache
     * @since	2.1.5	this method used to return a HashMap
     */
    public Map<String, TextField> getFieldCache() {
        return this.fieldCache;
    }

    /**
     * Sets a cache for field appearances. Parsing the existing PDF to
     * create a new TextField is time expensive. For those tasks that repeatedly
     * fill the same PDF with different field values the use of the cache has dramatic
     * speed advantages. An example usage:
     * <p>
     * <pre>
     * String pdfFile = ...;// the pdf file used as template
     * ArrayList xfdfFiles = ...;// the xfdf file names
     * ArrayList pdfOutFiles = ...;// the output file names, one for each element in xpdfFiles
     * HashMap cache = new HashMap();// the appearances cache
     * PdfReader originalReader = new PdfReader(pdfFile);
     * for (int k = 0; k &lt; xfdfFiles.size(); ++k) {
     *    PdfReader reader = new PdfReader(originalReader);
     *    XfdfReader xfdf = new XfdfReader((String)xfdfFiles.get(k));
     *    PdfStamper stp = new PdfStamper(reader, new FileOutputStream((String)pdfOutFiles.get(k)));
     *    AcroFields af = stp.getAcroFields();
     *    af.setFieldCache(cache);
     *    af.setFields(xfdf);
     *    stp.close();
     * }
     * </pre>
     *
     * @param fieldCache a Map that will carry the cached appearances
     * @since	2.1.5	this method used to take a HashMap as parameter
     */
    public void setFieldCache(Map<String, TextField> fieldCache) {
        this.fieldCache = fieldCache;
    }

    /**
     * Sets extra margins in text fields to better mimic the Acrobat layout.
     *
     * @param extraMarginLeft the extra margin left
     * @param extraMarginTop the extra margin top
     */
    public void setExtraMargin(float extraMarginLeft, float extraMarginTop) {
        this.extraMarginLeft = extraMarginLeft;
        this.extraMarginTop = extraMarginTop;
    }

    /**
     * Adds a substitution font to the list. The fonts in this list will be used if the original
     * font doesn't contain the needed glyphs.
     *
     * @param font the font
     */
    public void addSubstitutionFont(BaseFont font) {
        if (substitutionFonts == null)
            substitutionFonts = new ArrayList<BaseFont>();
        substitutionFonts.add(font);
    }

    private static final HashMap<String, String[]> stdFieldFontNames = new HashMap<String, String[]>();

    /**
     * Holds value of property totalRevisions.
     */
    private int totalRevisions;

    /**
     * Holds value of property fieldCache.
     *
     * @since	2.1.5	this used to be a HashMap
     */
    private Map<String, TextField> fieldCache;

    static {
        stdFieldFontNames.put("CoBO", new String[]{"Courier-BoldOblique"});
        stdFieldFontNames.put("CoBo", new String[]{"Courier-Bold"});
        stdFieldFontNames.put("CoOb", new String[]{"Courier-Oblique"});
        stdFieldFontNames.put("Cour", new String[]{"Courier"});
        stdFieldFontNames.put("HeBO", new String[]{"Helvetica-BoldOblique"});
        stdFieldFontNames.put("HeBo", new String[]{"Helvetica-Bold"});
        stdFieldFontNames.put("HeOb", new String[]{"Helvetica-Oblique"});
        stdFieldFontNames.put("Helv", new String[]{"Helvetica"});
        stdFieldFontNames.put("Symb", new String[]{"Symbol"});
        stdFieldFontNames.put("TiBI", new String[]{"Times-BoldItalic"});
        stdFieldFontNames.put("TiBo", new String[]{"Times-Bold"});
        stdFieldFontNames.put("TiIt", new String[]{"Times-Italic"});
        stdFieldFontNames.put("TiRo", new String[]{"Times-Roman"});
        stdFieldFontNames.put("ZaDb", new String[]{"ZapfDingbats"});
        stdFieldFontNames.put("HySm", new String[]{"HYSMyeongJo-Medium", "UniKS-UCS2-H"});
        stdFieldFontNames.put("HyGo", new String[]{"HYGoThic-Medium", "UniKS-UCS2-H"});
        stdFieldFontNames.put("KaGo", new String[]{"HeiseiKakuGo-W5", "UniKS-UCS2-H"});
        stdFieldFontNames.put("KaMi", new String[]{"HeiseiMin-W3", "UniJIS-UCS2-H"});
        stdFieldFontNames.put("MHei", new String[]{"MHei-Medium", "UniCNS-UCS2-H"});
        stdFieldFontNames.put("MSun", new String[]{"MSung-Light", "UniCNS-UCS2-H"});
        stdFieldFontNames.put("STSo", new String[]{"STSong-Light", "UniGB-UCS2-H"});
    }

    private static class SorterComparator implements Comparator<Object[]> {
        public int compare(Object[] o1, Object[] o2) {
            int n1 = ((int[])o1[1])[0];
            int n2 = ((int[])o2[1])[0];
            return n1 - n2;
        }
    }

    /**
     * Gets the list of substitution fonts. The list is composed of <CODE>BaseFont</CODE> and can be <CODE>null</CODE>. The fonts in this list will be used if the original
     * font doesn't contain the needed glyphs.
     *
     * @return the list
     */
    public ArrayList<BaseFont> getSubstitutionFonts() {
        return substitutionFonts;
    }

    /**
     * Sets a list of substitution fonts. The list is composed of <CODE>BaseFont</CODE> and can also be <CODE>null</CODE>. The fonts in this list will be used if the original
     * font doesn't contain the needed glyphs.
     *
     * @param substitutionFonts the list
     */
    public void setSubstitutionFonts(ArrayList<BaseFont> substitutionFonts) {
        this.substitutionFonts = substitutionFonts;
    }

    /**
     * Gets the XFA form processor.
     *
     * @return the XFA form processor
     */
    public XfaForm getXfa() {
        return xfa;
    }

    /**
     * Removes the XFA stream from the document.
     */
    public void removeXfa() {
		PdfDictionary root = reader.getCatalog();
		PdfDictionary acroform = root.getAsDict(PdfName.ACROFORM);
		acroform.remove(PdfName.XFA);
		try {
			xfa = new XfaForm(reader);
		}
		catch(Exception e) {
            throw new ExceptionConverter(e);
		}
    }

    private static final PdfName[] buttonRemove = {PdfName.MK, PdfName.F , PdfName.FF , PdfName.Q , PdfName.BS , PdfName.BORDER};

    /**
     * Creates a new pushbutton from an existing field. If there are several pushbuttons with the same name
     * only the first one is used. This pushbutton can be changed and be used to replace
     * an existing one, with the same name or other name, as long is it is in the same document. To replace an existing pushbutton
     * call {@link #replacePushbuttonField(String,PdfFormField)}.
     *
     * @param field the field name that should be a pushbutton
     * @return a new pushbutton or <CODE>null</CODE> if the field is not a pushbutton
     */
    public PushbuttonField getNewPushbuttonFromField(String field) {
        return getNewPushbuttonFromField(field, 0);
    }

    /**
     * Creates a new pushbutton from an existing field. This pushbutton can be changed and be used to replace
     * an existing one, with the same name or other name, as long is it is in the same document. To replace an existing pushbutton
     * call {@link #replacePushbuttonField(String,PdfFormField,int)}.
     *
     * @param field the field name that should be a pushbutton
     * @param order the field order in fields with same name
     * @return a new pushbutton or <CODE>null</CODE> if the field is not a pushbutton
     *
     * @since 2.0.7
     */
    public PushbuttonField getNewPushbuttonFromField(String field, int order) {
        try {
            if (getFieldType(field) != FIELD_TYPE_PUSHBUTTON)
                return null;
            Item item = getFieldItem(field);
            if (order >= item.size())
                return null;
            List<FieldPosition> pos = getFieldPositions(field);
            Rectangle box = pos.get(order).position;
            PushbuttonField newButton = new PushbuttonField(writer, box, null);
            PdfDictionary dic = item.getMerged(order);
            decodeGenericDictionary(dic, newButton);
            PdfDictionary mk = dic.getAsDict(PdfName.MK);
            if (mk != null) {
                PdfString text = mk.getAsString(PdfName.CA);
                if (text != null)
                    newButton.setText(text.toUnicodeString());
                PdfNumber tp = mk.getAsNumber(PdfName.TP);
                if (tp != null)
                    newButton.setLayout(tp.intValue() + 1);
                PdfDictionary ifit = mk.getAsDict(PdfName.IF);
                if (ifit != null) {
                    PdfName sw = ifit.getAsName(PdfName.SW);
                    if (sw != null) {
                        int scale = PushbuttonField.SCALE_ICON_ALWAYS;
                        if (sw.equals(PdfName.B))
                            scale = PushbuttonField.SCALE_ICON_IS_TOO_BIG;
                        else if (sw.equals(PdfName.S))
                            scale = PushbuttonField.SCALE_ICON_IS_TOO_SMALL;
                        else if (sw.equals(PdfName.N))
                            scale = PushbuttonField.SCALE_ICON_NEVER;
                        newButton.setScaleIcon(scale);
                    }
                    sw = ifit.getAsName(PdfName.S);
                    if (sw != null) {
                        if (sw.equals(PdfName.A))
                            newButton.setProportionalIcon(false);
                    }
                    PdfArray aj = ifit.getAsArray(PdfName.A);
                    if (aj != null && aj.size() == 2) {
                        float left = aj.getAsNumber(0).floatValue();
                        float bottom = aj.getAsNumber(1).floatValue();
                        newButton.setIconHorizontalAdjustment(left);
                        newButton.setIconVerticalAdjustment(bottom);
                    }
                    PdfBoolean fb = ifit.getAsBoolean(PdfName.FB);
                    if (fb != null && fb.booleanValue())
                        newButton.setIconFitToBounds(true);
                }
                PdfObject i = mk.get(PdfName.I);
                if (i != null && i.isIndirect())
                    newButton.setIconReference((PRIndirectReference)i);
            }
            return newButton;
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Replaces the first field with a new pushbutton. The pushbutton can be created with
     * {@link #getNewPushbuttonFromField(String)} from the same document or it can be a
     * generic PdfFormField of the type pushbutton.
     *
     * @param field the field name
     * @param button the <CODE>PdfFormField</CODE> representing the pushbutton
     * @return <CODE>true</CODE> if the field was replaced, <CODE>false</CODE> if the field
     * was not a pushbutton
     */
    public boolean replacePushbuttonField(String field, PdfFormField button) {
        return replacePushbuttonField(field, button, 0);
    }

    /**
     * Replaces the designated field with a new pushbutton. The pushbutton can be created with
     * {@link #getNewPushbuttonFromField(String,int)} from the same document or it can be a
     * generic PdfFormField of the type pushbutton.
     *
     * @param field the field name
     * @param button the <CODE>PdfFormField</CODE> representing the pushbutton
     * @param order the field order in fields with same name
     * @return <CODE>true</CODE> if the field was replaced, <CODE>false</CODE> if the field
     * was not a pushbutton
     *
     * @since 2.0.7
     */
    public boolean replacePushbuttonField(String field, PdfFormField button, int order) {
        if (getFieldType(field) != FIELD_TYPE_PUSHBUTTON)
            return false;
        Item item = getFieldItem(field);
        if (order >= item.size())
            return false;
        PdfDictionary merged = item.getMerged(order);
        PdfDictionary values = item.getValue(order);
        PdfDictionary widgets = item.getWidget(order);
        for (int k = 0; k < buttonRemove.length; ++k) {
            merged.remove(buttonRemove[k]);
            values.remove(buttonRemove[k]);
            widgets.remove(buttonRemove[k]);
        }
        for (Object element : button.getKeys()) {
            PdfName key = (PdfName)element;
            if (key.equals(PdfName.T))
                continue;
            if (key.equals(PdfName.FF))
                values.put(key, button.get(key));
            else
                widgets.put(key, button.get(key));
            merged.put(key, button.get(key));
            markUsed(values);
            markUsed(widgets);
        }
        return true;
    }

    /**
     * Checks whether a name exists as a signature field or not. It checks both signed fields and blank signatures.
     * @param name String
     * @return boolean does the signature field exist
     * @since 5.5.1
     */
    public boolean doesSignatureFieldExist(String name) {
        return getBlankSignatureNames().contains(name) || getSignatureNames().contains(name);
    }

    /**
     * A class representing a field position
     * @since 5.0.2
     */
    public static class FieldPosition {
        public int page;
        public Rectangle position;
    }
}
