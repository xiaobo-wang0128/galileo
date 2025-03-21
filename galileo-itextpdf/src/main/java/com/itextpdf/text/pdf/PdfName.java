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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.error_messages.MessageLocalization;

/**
 * <CODE>PdfName</CODE> is an object that can be used as a name in a PDF-file.
 * <P>
 * A name, like a string, is a sequence of characters.
 * It must begin with a slash followed by a sequence of ASCII characters in
 * the range 32 through 136 except %, (, ), [, ], &lt;, &gt;, {, }, / and #.
 * Any character except 0x00 may be included in a name by writing its
 * two character hex code, preceded by #. The maximum number of characters
 * in a name is 127.<BR>
 * This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.2.4 (page 56-58).
 * <P>
 *
 * @see		PdfObject
 * @see		PdfDictionary
 * @see		BadPdfFormatException
 */

public class
        PdfName extends PdfObject implements Comparable<PdfName> {

    // CLASS CONSTANTS (a variety of standard names used in PDF))
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName _3D = new PdfName("3D");
    /** A name */
    public static final PdfName A = new PdfName("A");
    /**
     * A name
     * @since 5.0.3
     */
    public static final PdfName A85 = new PdfName("A85");
    /** A name */
    public static final PdfName AA = new PdfName("AA");
    /**
     * A name
     * @since 2.1.5 renamed from ABSOLUTECALORIMETRIC
     */
    public static final PdfName ABSOLUTECOLORIMETRIC = new PdfName("AbsoluteColorimetric");
    /** A name */
    public static final PdfName AC = new PdfName("AC");
    /** A name */
    public static final PdfName ACROFORM = new PdfName("AcroForm");
    /** A name */
    public static final PdfName ACTION = new PdfName("Action");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName ACTIVATION = new PdfName("Activation");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName ADBE = new PdfName("ADBE");
    /**
     * a name used in PDF structure
     * @since 2.1.6
     */
    public static final PdfName ACTUALTEXT = new PdfName("ActualText");
    /** A name */
    public static final PdfName ADBE_PKCS7_DETACHED = new PdfName("adbe.pkcs7.detached");
    /** A name */
    public static final PdfName ADBE_PKCS7_S4 =new PdfName("adbe.pkcs7.s4");
    /** A name */
    public static final PdfName ADBE_PKCS7_S5 =new PdfName("adbe.pkcs7.s5");
    /** A name */
    public static final PdfName ADBE_PKCS7_SHA1 = new PdfName("adbe.pkcs7.sha1");
    /** A name */
    public static final PdfName ADBE_X509_RSA_SHA1 = new PdfName("adbe.x509.rsa_sha1");
    /** A name */
    public static final PdfName ADOBE_PPKLITE = new PdfName("Adobe.PPKLite");
    /** A name */
    public static final PdfName ADOBE_PPKMS = new PdfName("Adobe.PPKMS");
    /** A name */
    public static final PdfName AESV2 = new PdfName("AESV2");
    /** A name */
    public static final PdfName AESV3 = new PdfName("AESV3");
    /**
     * A name
     * @since 5.5.2
     */
    public static final PdfName AF = new PdfName("AF");
    /**
     * A name
     * @since 5.4.5
     */
    public static final PdfName AFRELATIONSHIP = new PdfName("AFRelationship");
    /**
     * A name
     * @since 5.0.3
     */
    public static final PdfName AHX = new PdfName("AHx");
    /** A name */
    public static final PdfName AIS = new PdfName("AIS");
    /** A name */
    public static final PdfName ALL = new PdfName("All");
    /** A name */
    public static final PdfName ALLPAGES = new PdfName("AllPages");
    /**
     * Use ALT to specify alternate texts in Tagged PDF.
     * For alternate ICC profiles, use {@link #ALTERNATE}
     */
    public static final PdfName ALT = new PdfName("Alt");
    /**
     * Use ALTERNATE only in ICC profiles. It specifies an alternative color
     * space, in case the primary one is not supported, for legacy purposes.
     * For various types of alternate texts in Tagged PDF, use {@link #ALT}
     */
    public static final PdfName ALTERNATE = new PdfName("Alternate");
    /**
     * A name
     * @since 5.4.5
     */
    public static final PdfName ALTERNATEPRESENTATION = new PdfName("AlternatePresentations");
    /**
     * A name.
     * @since 5.4.3
     */
    public static final PdfName ALTERNATES = new PdfName("Alternates");
    /**
     * A name.
     * @since 5.0.2
     */
    public static final PdfName AND = new PdfName("And");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName ANIMATION = new PdfName("Animation");
    /** A name */
    public static final PdfName ANNOT = new PdfName("Annot");
    /** A name */
    public static final PdfName ANNOTS = new PdfName("Annots");
    /** A name */
    public static final PdfName ANTIALIAS = new PdfName("AntiAlias");
    /** A name */
    public static final PdfName AP = new PdfName("AP");
    /** A name */
    public static final PdfName APP = new PdfName("App");
    /** A name */
    public static final PdfName APPDEFAULT = new PdfName("AppDefault");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName ART = new PdfName("Art");
    /** A name */
    public static final PdfName ARTBOX = new PdfName("ArtBox");
    /**
     * A name
     * @since 5.4.2
     */
    public static final PdfName ARTIFACT = new PdfName("Artifact");
    /** A name */
    public static final PdfName ASCENT = new PdfName("Ascent");
    /** A name */
    public static final PdfName AS = new PdfName("AS");
    /** A name */
    public static final PdfName ASCII85DECODE = new PdfName("ASCII85Decode");
    /** A name */
    public static final PdfName ASCIIHEXDECODE = new PdfName("ASCIIHexDecode");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName ASSET = new PdfName("Asset");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName ASSETS = new PdfName("Assets");
    /**
     * A name
     * @since 5.4.2
     */
    public static final PdfName ATTACHED = new PdfName("Attached");
    /** A name */
    public static final PdfName AUTHEVENT = new PdfName("AuthEvent");
    /** A name */
    public static final PdfName AUTHOR = new PdfName("Author");
    /** A name */
    public static final PdfName B = new PdfName("B");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName BACKGROUND = new PdfName("Background");
    /**
     * A name
     * @since	5.3.5
     */
    public static final PdfName BACKGROUNDCOLOR = new PdfName("BackgroundColor");
    /** A name */
    public static final PdfName BASEENCODING = new PdfName("BaseEncoding");
    /** A name */
    public static final PdfName BASEFONT = new PdfName("BaseFont");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName BASEVERSION = new PdfName("BaseVersion");
    /** A name */
    public static final PdfName BBOX = new PdfName("BBox");
    /** A name */
    public static final PdfName BC = new PdfName("BC");
    /** A name */
    public static final PdfName BG = new PdfName("BG");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName BIBENTRY = new PdfName("BibEntry");
    /** A name */
    public static final PdfName BIGFIVE = new PdfName("BigFive");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName BINDING = new PdfName("Binding");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName BINDINGMATERIALNAME = new PdfName("BindingMaterialName");
    /** A name */
    public static final PdfName BITSPERCOMPONENT = new PdfName("BitsPerComponent");
    /** A name */
    public static final PdfName BITSPERSAMPLE = new PdfName("BitsPerSample");
    /** A name */
    public static final PdfName BL = new PdfName("Bl");
    /** A name */
    public static final PdfName BLACKIS1 = new PdfName("BlackIs1");
    /** A name */
    public static final PdfName BLACKPOINT = new PdfName("BlackPoint");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName BLOCKQUOTE = new PdfName("BlockQuote");
    /** A name */
    public static final PdfName BLEEDBOX = new PdfName("BleedBox");
    /** A name */
    public static final PdfName BLINDS = new PdfName("Blinds");
    /** A name */
    public static final PdfName BM = new PdfName("BM");
    /** A name */
    public static final PdfName BORDER = new PdfName("Border");
    /**
     * A name
     * @since 5.4.0
     */
    public static final PdfName BOTH = new PdfName("Both");
    /** A name */
    public static final PdfName BOUNDS = new PdfName("Bounds");
    /** A name */
    public static final PdfName BOX = new PdfName("Box");
    /** A name */
    public static final PdfName BS = new PdfName("BS");
    /** A name */
    public static final PdfName BTN = new PdfName("Btn");
    /** A name */
    public static final PdfName BYTERANGE = new PdfName("ByteRange");
    /** A name */
    public static final PdfName C = new PdfName("C");
    /** A name */
    public static final PdfName C0 = new PdfName("C0");
    /** A name */
    public static final PdfName C1 = new PdfName("C1");
    /** A name */
    public static final PdfName CA = new PdfName("CA");
    /** A name */
    public static final PdfName ca = new PdfName("ca");
    /** A name */
    public static final PdfName CALGRAY = new PdfName("CalGray");
    /** A name */
    public static final PdfName CALRGB = new PdfName("CalRGB");
    /** A name */
    public static final PdfName CAPHEIGHT = new PdfName("CapHeight");
    /**
     * A name
     * @since 5.4.5
     */
    public static final PdfName CARET = new PdfName("Caret");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName CAPTION = new PdfName("Caption");
    /** A name */
    public static final PdfName CATALOG = new PdfName("Catalog");
    /** A name */
    public static final PdfName CATEGORY = new PdfName("Category");
    /**
     * A name
     * @since 5.4.4
     */
    public static final PdfName CB = new PdfName("cb");
    /** A name */
    public static final PdfName CCITTFAXDECODE = new PdfName("CCITTFaxDecode");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName CENTER = new PdfName("Center");
    /** A name */
    public static final PdfName CENTERWINDOW = new PdfName("CenterWindow");
    /** A name */
    public static final PdfName CERT = new PdfName("Cert");
    /** A name
     * @since 5.1.3
     */
    public static final PdfName CERTS = new PdfName("Certs");
    /** A name */
    public static final PdfName CF = new PdfName("CF");
    /** A name */
    public static final PdfName CFM = new PdfName("CFM");
    /** A name */
    public static final PdfName CH = new PdfName("Ch");
    /** A name */
    public static final PdfName CHARPROCS = new PdfName("CharProcs");
    /** A name */
    public static final PdfName CHECKSUM = new PdfName("CheckSum");
    /** A name */
    public static final PdfName CI = new PdfName("CI");
    /** A name */
    public static final PdfName CIDFONTTYPE0 = new PdfName("CIDFontType0");
    /** A name */
    public static final PdfName CIDFONTTYPE2 = new PdfName("CIDFontType2");
    /**
     * A name
     * @since 2.0.7
     */
    public static final PdfName CIDSET = new PdfName("CIDSet");
    /** A name */
    public static final PdfName CIDSYSTEMINFO = new PdfName("CIDSystemInfo");
    /** A name */
    public static final PdfName CIDTOGIDMAP = new PdfName("CIDToGIDMap");
    /** A name */
    public static final PdfName CIRCLE = new PdfName("Circle");
    /**
     * A name.
     * @since 5.3.4
     */
    public static final PdfName CLASSMAP = new PdfName("ClassMap");
    /**
     * A name.
     * @since 5.1.0
     */
    public static final PdfName CLOUD = new PdfName("Cloud");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName CMD = new PdfName("CMD");
    /** A name */
    public static final PdfName CO = new PdfName("CO");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName CODE = new PdfName("Code");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName COLOR = new PdfName("Color");
    public static final PdfName COLORANTS = new PdfName("Colorants");
    /** A name */
    public static final PdfName COLORS = new PdfName("Colors");
    /** A name */
    public static final PdfName COLORSPACE = new PdfName("ColorSpace");
    /** 
     * A name
     * @since 5.4.4 
     */
    public static final PdfName COLORTRANSFORM = new PdfName("ColorTransform");
    /** A name */
    public static final PdfName COLLECTION = new PdfName("Collection");
    /** A name */
    public static final PdfName COLLECTIONFIELD = new PdfName("CollectionField");
    /** A name */
    public static final PdfName COLLECTIONITEM = new PdfName("CollectionItem");
    /** A name */
    public static final PdfName COLLECTIONSCHEMA = new PdfName("CollectionSchema");
    /** A name */
    public static final PdfName COLLECTIONSORT = new PdfName("CollectionSort");
    /** A name */
    public static final PdfName COLLECTIONSUBITEM = new PdfName("CollectionSubitem");
    /**
     * A name.
     * @since 5.4.0
     */
    public static final PdfName COLSPAN = new PdfName("ColSpan");
    /**
     * A name.
     * @since 5.4.0
     */
    public static final PdfName COLUMN = new PdfName("Column");
    /** A name */
    public static final PdfName COLUMNS = new PdfName("Columns");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName CONDITION = new PdfName("Condition");
    /**
     * A name.
     * @since 5.4.2
     */
    public static final PdfName CONFIGS = new PdfName("Configs");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName CONFIGURATION = new PdfName("Configuration");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName CONFIGURATIONS = new PdfName("Configurations");
    /** A name */
    public static final PdfName CONTACTINFO = new PdfName("ContactInfo");
    /** A name */
    public static final PdfName CONTENT = new PdfName("Content");
    /** A name */
    public static final PdfName CONTENTS = new PdfName("Contents");
    /** A name */
    public static final PdfName COORDS = new PdfName("Coords");
    /** A name */
    public static final PdfName COUNT = new PdfName("Count");
    /** A name of a base 14 type 1 font */
    public static final PdfName COURIER = new PdfName("Courier");
    /** A name of a base 14 type 1 font */
    public static final PdfName COURIER_BOLD = new PdfName("Courier-Bold");
    /** A name of a base 14 type 1 font */
    public static final PdfName COURIER_OBLIQUE = new PdfName("Courier-Oblique");
    /** A name of a base 14 type 1 font */
    public static final PdfName COURIER_BOLDOBLIQUE = new PdfName("Courier-BoldOblique");
    /** A name */
    public static final PdfName CREATIONDATE = new PdfName("CreationDate");
    /** A name */
    public static final PdfName CREATOR = new PdfName("Creator");
    /** A name */
    public static final PdfName CREATORINFO = new PdfName("CreatorInfo");
    /** A name
     * @since 5.1.3
     */
    public static final PdfName CRL = new PdfName("CRL");
    /** A name
     * @since 5.1.3
     */
    public static final PdfName CRLS = new PdfName("CRLs");
    /** A name */
    public static final PdfName CROPBOX = new PdfName("CropBox");
    /** A name */
    public static final PdfName CRYPT = new PdfName("Crypt");
    /** A name */
    public static final PdfName CS = new PdfName("CS");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName CUEPOINT = new PdfName("CuePoint");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName CUEPOINTS = new PdfName("CuePoints");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName CYX = new PdfName("CYX");
    /** A name */
    public static final PdfName D = new PdfName("D");
    /** A name */
    public static final PdfName DA = new PdfName("DA");
    /** A name */
    public static final PdfName DATA = new PdfName("Data");
    /** A name */
    public static final PdfName DC = new PdfName("DC");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName DCS = new PdfName("DCS");
    /** A name */
    public static final PdfName DCTDECODE = new PdfName("DCTDecode");
    /**
     * A name.
     * @since 5.4.0
     */
    public static final PdfName DECIMAL = new PdfName("Decimal");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName DEACTIVATION = new PdfName("Deactivation");
    /** A name */
    public static final PdfName DECODE = new PdfName("Decode");
    /** A name */
    public static final PdfName DECODEPARMS = new PdfName("DecodeParms");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName DEFAULT = new PdfName("Default");
    /**
     * A name
     * @since	2.1.5 renamed from DEFAULTCRYPTFILER
     */
    public static final PdfName DEFAULTCRYPTFILTER = new PdfName("DefaultCryptFilter");
    /** A name */
    public static final PdfName DEFAULTCMYK = new PdfName("DefaultCMYK");
    /** A name */
    public static final PdfName DEFAULTGRAY = new PdfName("DefaultGray");
    /** A name */
    public static final PdfName DEFAULTRGB = new PdfName("DefaultRGB");
    /** A name */
    public static final PdfName DESC = new PdfName("Desc");
    /** A name */
    public static final PdfName DESCENDANTFONTS = new PdfName("DescendantFonts");
    /** A name */
    public static final PdfName DESCENT = new PdfName("Descent");
    /** A name */
    public static final PdfName DEST = new PdfName("Dest");
    /** A name */
    public static final PdfName DESTOUTPUTPROFILE = new PdfName("DestOutputProfile");
    /** A name */
    public static final PdfName DESTS = new PdfName("Dests");
    /** A name */
    public static final PdfName DEVICEGRAY = new PdfName("DeviceGray");
    /** A name */
    public static final PdfName DEVICERGB = new PdfName("DeviceRGB");
    /** A name */
    public static final PdfName DEVICECMYK = new PdfName("DeviceCMYK");
    /**
     * A name
     * @since 5.2.1
     */
    public static final PdfName DEVICEN = new PdfName("DeviceN");
    /** A name */
    public static final PdfName DI = new PdfName("Di");
    /** A name */
    public static final PdfName DIFFERENCES = new PdfName("Differences");
    /** A name */
    public static final PdfName DISSOLVE = new PdfName("Dissolve");
    /** A name */
    public static final PdfName DIRECTION = new PdfName("Direction");
    /** A name */
    public static final PdfName DISPLAYDOCTITLE = new PdfName("DisplayDocTitle");
    /** A name */
    public static final PdfName DIV = new PdfName("Div");
    /** A name */
    public static final PdfName DL = new PdfName("DL");
    /** A name */
    public static final PdfName DM = new PdfName("Dm");
    /** A name */
    public static final PdfName DOCMDP = new PdfName("DocMDP");
    /** A name */
    public static final PdfName DOCOPEN = new PdfName("DocOpen");
    /**
     * A name.
     * @since 5.1.3
     */
    public static final PdfName DOCTIMESTAMP = new PdfName( "DocTimeStamp" );
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName DOCUMENT = new PdfName( "Document" );
    /** A name */
    public static final PdfName DOMAIN = new PdfName("Domain");
    /**
     * A name.
     * @since 5.2.1
     */
    public static final PdfName DOS = new PdfName("DOS");
    /** A name */
    public static final PdfName DP = new PdfName("DP");
    /** A name */
    public static final PdfName DR = new PdfName("DR");
    /** A name */
    public static final PdfName DS = new PdfName("DS");
    /** A name
     * @since 5.1.3
     */
    public static final PdfName DSS = new PdfName("DSS");
    /** A name */
    public static final PdfName DUR = new PdfName("Dur");
    /** A name */
    public static final PdfName DUPLEX = new PdfName("Duplex");
    /** A name */
    public static final PdfName DUPLEXFLIPSHORTEDGE = new PdfName("DuplexFlipShortEdge");
    /** A name */
    public static final PdfName DUPLEXFLIPLONGEDGE = new PdfName("DuplexFlipLongEdge");
    /** A name */
    public static final PdfName DV = new PdfName("DV");
    /** A name */
    public static final PdfName DW = new PdfName("DW");
    /** A name */
    public static final PdfName E = new PdfName("E");
    /** A name */
    public static final PdfName EARLYCHANGE = new PdfName("EarlyChange");
    /** A name */
    public static final PdfName EF = new PdfName("EF");
    /**
     * A name
     * @since	2.1.3
     */
    public static final PdfName EFF = new PdfName("EFF");
    /**
     * A name
     * @since	2.1.3
     */
    public static final PdfName EFOPEN = new PdfName("EFOpen");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName EMBEDDED = new PdfName("Embedded");
    /** A name */
    public static final PdfName EMBEDDEDFILE = new PdfName("EmbeddedFile");
    /** A name */
    public static final PdfName EMBEDDEDFILES = new PdfName("EmbeddedFiles");
    /** A name */
    public static final PdfName ENCODE = new PdfName("Encode");
    /** A name */
    public static final PdfName ENCODEDBYTEALIGN = new PdfName("EncodedByteAlign");
    /** A name */
    public static final PdfName ENCODING = new PdfName("Encoding");
    /** A name */
    public static final PdfName ENCRYPT = new PdfName("Encrypt");
    /** A name */
    public static final PdfName ENCRYPTMETADATA = new PdfName("EncryptMetadata");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName END = new PdfName("End");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName ENDINDENT = new PdfName("EndIndent");
    /** A name */
    public static final PdfName ENDOFBLOCK = new PdfName("EndOfBlock");
    /** A name */
    public static final PdfName ENDOFLINE = new PdfName("EndOfLine");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName EPSG = new PdfName("EPSG");
    /**
     *  A name
     *  @since 5.4.3
     *  */
    public static final PdfName ESIC = new PdfName("ESIC");
    /** A name
     * @since 5.1.3
     */
    public static final PdfName ETSI_CADES_DETACHED = new PdfName("ETSI.CAdES.detached");
    /** A name */
    public static final PdfName ETSI_RFC3161 = new PdfName("ETSI.RFC3161");
    /** A name */
    public static final PdfName EXCLUDE = new PdfName("Exclude");
    /** A name */
    public static final PdfName EXTEND = new PdfName("Extend");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName EXTENSIONS = new PdfName("Extensions");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName EXTENSIONLEVEL = new PdfName("ExtensionLevel");
    /** A name */
    public static final PdfName EXTGSTATE = new PdfName("ExtGState");
    /** A name */
    public static final PdfName EXPORT = new PdfName("Export");
    /** A name */
    public static final PdfName EXPORTSTATE = new PdfName("ExportState");
    /** A name */
    public static final PdfName EVENT = new PdfName("Event");
    /** A name */
    public static final PdfName F = new PdfName("F");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName FAR = new PdfName("Far");
    /** A name */
    public static final PdfName FB = new PdfName("FB");
    /**
     * A name
     * @since 5.1.0
     */
    public static final PdfName FD = new PdfName("FD");
    /** A name */
    public static final PdfName FDECODEPARMS = new PdfName("FDecodeParms");
    /** A name */
    public static final PdfName FDF = new PdfName("FDF");
    /** A name */
    public static final PdfName FF = new PdfName("Ff");
    /** A name */
    public static final PdfName FFILTER = new PdfName("FFilter");
    /**
     * A name
     * @since 5.0.2
     */
    public static final PdfName FG = new PdfName("FG");
    /** A name */
    public static final PdfName FIELDMDP = new PdfName("FieldMDP");
    /** A name */
    public static final PdfName FIELDS = new PdfName("Fields");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName FIGURE = new PdfName( "Figure" );
    /** A name */
    public static final PdfName FILEATTACHMENT = new PdfName("FileAttachment");
    /** A name */
    public static final PdfName FILESPEC = new PdfName("Filespec");
    /** A name */
    public static final PdfName FILTER = new PdfName("Filter");
    /** A name */
    public static final PdfName FIRST = new PdfName("First");
    /** A name */
    public static final PdfName FIRSTCHAR = new PdfName("FirstChar");
    /** A name */
    public static final PdfName FIRSTPAGE = new PdfName("FirstPage");
    /** A name */
    public static final PdfName FIT = new PdfName("Fit");
    /** A name */
    public static final PdfName FITH = new PdfName("FitH");
    /** A name */
    public static final PdfName FITV = new PdfName("FitV");
    /** A name */
    public static final PdfName FITR = new PdfName("FitR");
    /** A name */
    public static final PdfName FITB = new PdfName("FitB");
    /** A name */
    public static final PdfName FITBH = new PdfName("FitBH");
    /** A name */
    public static final PdfName FITBV = new PdfName("FitBV");
    /** A name */
    public static final PdfName FITWINDOW = new PdfName("FitWindow");
    /**
     * A name
     * @since 5.0.3
     */
    public static final PdfName FL = new PdfName("Fl");
    /** A name */
    public static final PdfName FLAGS = new PdfName("Flags");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName FLASH = new PdfName("Flash");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName FLASHVARS = new PdfName("FlashVars");
    /** A name */
    public static final PdfName FLATEDECODE = new PdfName("FlateDecode");
    /** A name */
    public static final PdfName FO = new PdfName("Fo");
    /** A name */
    public static final PdfName FONT = new PdfName("Font");
    /** A name */
    public static final PdfName FONTBBOX = new PdfName("FontBBox");
    /** A name */
    public static final PdfName FONTDESCRIPTOR = new PdfName("FontDescriptor");
    /** A name */
    public static final PdfName FONTFAMILY = new PdfName("FontFamily");
    /** A name */
    public static final PdfName FONTFILE = new PdfName("FontFile");
    /** A name */
    public static final PdfName FONTFILE2 = new PdfName("FontFile2");
    /** A name */
    public static final PdfName FONTFILE3 = new PdfName("FontFile3");
    /** A name */
    public static final PdfName FONTMATRIX = new PdfName("FontMatrix");
    /** A name */
    public static final PdfName FONTNAME = new PdfName("FontName");
    /** A name */
    public static final PdfName FONTWEIGHT = new PdfName("FontWeight");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName FOREGROUND = new PdfName("Foreground");
    /** A name */
    public static final PdfName FORM = new PdfName("Form");
    /** A name */
    public static final PdfName FORMTYPE = new PdfName("FormType");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName FORMULA = new PdfName( "Formula" );
    /** A name */
    public static final PdfName FREETEXT = new PdfName("FreeText");
    /** A name */
    public static final PdfName FRM = new PdfName("FRM");
    /** A name */
    public static final PdfName FS = new PdfName("FS");
    /** A name */
    public static final PdfName FT = new PdfName("FT");
    /** A name */
    public static final PdfName FULLSCREEN = new PdfName("FullScreen");
    /** A name */
    public static final PdfName FUNCTION = new PdfName("Function");
    /** A name */
    public static final PdfName FUNCTIONS = new PdfName("Functions");
    /** A name */
    public static final PdfName FUNCTIONTYPE = new PdfName("FunctionType");
    /** A name of an attribute. */
    public static final PdfName GAMMA = new PdfName("Gamma");
    /** A name of an attribute. */
    public static final PdfName GBK = new PdfName("GBK");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName GCS = new PdfName("GCS");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName GEO = new PdfName("GEO");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName GEOGCS = new PdfName("GEOGCS");
    /** A name of an attribute. */
    public static final PdfName GLITTER = new PdfName("Glitter");
    /** A name of an attribute. */
    public static final PdfName GOTO = new PdfName("GoTo");
    /**
     * A name
     * @since 5.4.5
     */
    public static final PdfName GOTO3DVIEW= new PdfName("GoTo3DView");
    /** A name of an attribute. */
    public static final PdfName GOTOE = new PdfName("GoToE");
    /** A name of an attribute. */
    public static final PdfName GOTOR = new PdfName("GoToR");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName GPTS = new PdfName("GPTS");
    /** A name of an attribute. */
    public static final PdfName GROUP = new PdfName("Group");
    /** A name of an attribute. */
    public static final PdfName GTS_PDFA1 = new PdfName("GTS_PDFA1");
    /** A name of an attribute. */
    public static final PdfName GTS_PDFX = new PdfName("GTS_PDFX");
    /** A name of an attribute. */
    public static final PdfName GTS_PDFXVERSION = new PdfName("GTS_PDFXVersion");
    /** A name of an attribute. */
    public static final PdfName H = new PdfName("H");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName H1 = new PdfName( "H1" );
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName H2 = new PdfName("H2");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName H3 = new PdfName("H3");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName H4 = new PdfName("H4");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName H5 = new PdfName("H5");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName H6 = new PdfName("H6");
    /**
     * A name
     * @since 5.4.5
     */
    public static final PdfName HALFTONENAME = new PdfName("HalftoneName");
    /**
     * A name
     * @since 5.4.5
     */
    public static final PdfName HALFTONETYPE = new PdfName("HalftoneType");
    /**
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName HALIGN = new PdfName("HAlign");
    /**
     * A name.
     * @since 5.4.0
     */
    public static final PdfName HEADERS = new PdfName("Headers");
    /** A name of an attribute. */
    public static final PdfName HEIGHT = new PdfName("Height");
    /** A name */
    public static final PdfName HELV = new PdfName("Helv");
    /** A name of a base 14 type 1 font */
    public static final PdfName HELVETICA = new PdfName("Helvetica");
    /** A name of a base 14 type 1 font */
    public static final PdfName HELVETICA_BOLD = new PdfName("Helvetica-Bold");
    /** A name of a base 14 type 1 font */
    public static final PdfName HELVETICA_OBLIQUE = new PdfName("Helvetica-Oblique");
    /** A name of a base 14 type 1 font */
    public static final PdfName HELVETICA_BOLDOBLIQUE = new PdfName("Helvetica-BoldOblique");
    /**
     * A name
     * @since 5.0.2
     */
    public static final PdfName HF = new PdfName("HF");
    /** A name */
    public static final PdfName HID = new PdfName("Hid");
    /** A name */
    public static final PdfName HIDE = new PdfName("Hide");
    /** A name */
    public static final PdfName HIDEMENUBAR = new PdfName("HideMenubar");
    /** A name */
    public static final PdfName HIDETOOLBAR = new PdfName("HideToolbar");
    /** A name */
    public static final PdfName HIDEWINDOWUI = new PdfName("HideWindowUI");
    /** A name */
    public static final PdfName HIGHLIGHT = new PdfName("Highlight");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName HOFFSET = new PdfName("HOffset");
    /**
     * A name
     * @since 5.4.5
     */
    public static final PdfName HT = new PdfName("HT");
    /**
     * A name
     * @since 5.4.5
     */
    public static final PdfName HTP = new PdfName("HTP");
    /** A name */
    public static final PdfName I = new PdfName("I");
    /**
     * A name
     * @since 5.4.3
     */
    public static final PdfName IC = new PdfName("IC");
    /** A name */
    public static final PdfName ICCBASED = new PdfName("ICCBased");
    /** A name */
    public static final PdfName ID = new PdfName("ID");
    /** A name */
    public static final PdfName IDENTITY = new PdfName("Identity");
    /** A name */
    public static final PdfName IDTREE = new PdfName("IDTree");
    /** A name */
    public static final PdfName IF = new PdfName("IF");
    /**
     * A name
     * @since 5.5.3
     */
    public static final PdfName IM = new PdfName("IM");
    /** A name */
    public static final PdfName IMAGE = new PdfName("Image");
    /** A name */
    public static final PdfName IMAGEB = new PdfName("ImageB");
    /** A name */
    public static final PdfName IMAGEC = new PdfName("ImageC");
    /** A name */
    public static final PdfName IMAGEI = new PdfName("ImageI");
    /** A name */
    public static final PdfName IMAGEMASK = new PdfName("ImageMask");
    /** A name */
    public static final PdfName INCLUDE = new PdfName("Include");
    /**
     * A name
     * @since 5.0.2
     */
    public static final PdfName IND = new PdfName("Ind");
    /** A name */
    public static final PdfName INDEX = new PdfName("Index");
    /** A name */
    public static final PdfName INDEXED = new PdfName("Indexed");
    /** A name */
    public static final PdfName INFO = new PdfName("Info");
    /** A name */
    public static final PdfName INK = new PdfName("Ink");
    /** A name */
    public static final PdfName INKLIST = new PdfName("InkList");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName INSTANCES = new PdfName("Instances");
    /** A name */
    public static final PdfName IMPORTDATA = new PdfName("ImportData");
    /** A name */
    public static final PdfName INTENT = new PdfName("Intent");
    /** A name */
    public static final PdfName INTERPOLATE = new PdfName("Interpolate");
    /** A name */
    public static final PdfName ISMAP = new PdfName("IsMap");
    /** A name */
    public static final PdfName IRT = new PdfName("IRT");
    /** A name */
    public static final PdfName ITALICANGLE = new PdfName("ItalicAngle");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName ITXT = new PdfName("ITXT");
    /** A name */
    public static final PdfName IX = new PdfName("IX");
    /** A name */
    public static final PdfName JAVASCRIPT = new PdfName("JavaScript");
    /**
     * A name
     * @since	2.1.5
     */
    public static final PdfName JBIG2DECODE = new PdfName("JBIG2Decode");
    /**
     * A name
     * @since	2.1.5
     */
    public static final PdfName JBIG2GLOBALS = new PdfName("JBIG2Globals");
    /** A name */
    public static final PdfName JPXDECODE = new PdfName("JPXDecode");
    /** A name */
    public static final PdfName JS = new PdfName("JS");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName JUSTIFY = new PdfName("Justify");
    /** A name */
    public static final PdfName K = new PdfName("K");
    /** A name */
    public static final PdfName KEYWORDS = new PdfName("Keywords");
    /** A name */
    public static final PdfName KIDS = new PdfName("Kids");
    /** A name */
    public static final PdfName L = new PdfName("L");
    /** A name */
    public static final PdfName L2R = new PdfName("L2R");
    /**
     * A name
     * @since 5.2.1
     */
    public static final PdfName LAB = new PdfName("Lab");
    /**
     * An entry specifying the natural language, and optionally locale. Use this
     * to specify the Language attribute on a Tagged Pdf element.
     * For the content usage dictionary, use {@link #LANGUAGE}
     */
    public static final PdfName LANG = new PdfName("Lang");
    /**
     * A dictionary type, strictly for use in the content usage dictionary. For
     * dictionary entries in Tagged Pdf, use {@link #LANG}
     */
    public static final PdfName LANGUAGE = new PdfName("Language");
    /** A name */
    public static final PdfName LAST = new PdfName("Last");
    /** A name */
    public static final PdfName LASTCHAR = new PdfName("LastChar");
    /** A name */
    public static final PdfName LASTPAGE = new PdfName("LastPage");
    /** A name */
    public static final PdfName LAUNCH = new PdfName("Launch");
    /**
     * A name
     * @since 5.5.0
     */
    public static final PdfName LAYOUT = new PdfName("Layout");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName LBL = new PdfName("Lbl");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName LBODY = new PdfName("LBody");
    /** A name */
    public static final PdfName LENGTH = new PdfName("Length");
    /** A name */
    public static final PdfName LENGTH1 = new PdfName("Length1");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName LI = new PdfName("LI");
    /** A name */
    public static final PdfName LIMITS = new PdfName("Limits");
    /** A name */
    public static final PdfName LINE = new PdfName("Line");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName LINEAR = new PdfName("Linear");
    /**
     * A name.
     * @since 5.3.5
     */
    public static final PdfName LINEHEIGHT = new PdfName("LineHeight");
    /** A name */
    public static final PdfName LINK = new PdfName("Link");
    /**
     * A name
     * @since 5.4.0
     */
    public static final PdfName LIST = new PdfName("List");
    /** A name */
    public static final PdfName LISTMODE = new PdfName("ListMode");
    /** A name */
    public static final PdfName LISTNUMBERING = new PdfName("ListNumbering");
    /** A name */
    public static final PdfName LOCATION = new PdfName("Location");
    /** A name */
    public static final PdfName LOCK = new PdfName("Lock");
    /**
     * A name
     * @since	2.1.2
     */
    public static final PdfName LOCKED = new PdfName("Locked");
    /**
     * A name
     * @since	5.4.0
     */
    public static final PdfName LOWERALPHA = new PdfName("LowerAlpha");
    /**
     * A name
     * @since	5.4.0
     */
    public static final PdfName LOWERROMAN = new PdfName("LowerRoman");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName LPTS = new PdfName("LPTS");
    /** A name */
    public static final PdfName LZWDECODE = new PdfName("LZWDecode");
    /** A name */
    public static final PdfName M = new PdfName("M");
    /**
     * A name.
     * @since 5.2.1
     */
    public static final PdfName MAC = new PdfName("Mac");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName MATERIAL = new PdfName("Material");
    /** A name */
    public static final PdfName MATRIX = new PdfName("Matrix");
    /** A name of an encoding */
    public static final PdfName MAC_EXPERT_ENCODING = new PdfName("MacExpertEncoding");
    /** A name of an encoding */
    public static final PdfName MAC_ROMAN_ENCODING = new PdfName("MacRomanEncoding");
    /** A name */
    public static final PdfName MARKED = new PdfName("Marked");
    /** A name */
    public static final PdfName MARKINFO = new PdfName("MarkInfo");
    /** A name */
    public static final PdfName MASK = new PdfName("Mask");
    /**
     * A name
     * @since	2.1.6 renamed from MAX
     */
    public static final PdfName MAX_LOWER_CASE = new PdfName("max");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName MAX_CAMEL_CASE = new PdfName("Max");
    /** A name */
    public static final PdfName MAXLEN = new PdfName("MaxLen");
    /** A name */
    public static final PdfName MEDIABOX = new PdfName("MediaBox");
    /** A name */
    public static final PdfName MCID = new PdfName("MCID");
    /** A name */
    public static final PdfName MCR = new PdfName("MCR");
    /**
     * A name
     * @since	5.1.0
     */
    public static final PdfName MEASURE = new PdfName("Measure");
    /** A name */
    public static final PdfName METADATA = new PdfName("Metadata");
    /**
     * A name
     * @since	2.1.6 renamed from MIN
     */
    public static final PdfName MIN_LOWER_CASE = new PdfName("min");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName MIN_CAMEL_CASE = new PdfName("Min");
    /** A name */
    public static final PdfName MK = new PdfName("MK");
    /** A name */
    public static final PdfName MMTYPE1 = new PdfName("MMType1");
    /** A name */
    public static final PdfName MODDATE = new PdfName("ModDate");
    /**
     * A name
     * @since	5.4.3
     */
    public static final PdfName MOVIE = new PdfName("Movie");
    /** A name */
    public static final PdfName N = new PdfName("N");
    /** A name */
    public static final PdfName N0 = new PdfName("n0");
    /** A name */
    public static final PdfName N1 = new PdfName("n1");
    /** A name */
    public static final PdfName N2 = new PdfName("n2");
    /** A name */
    public static final PdfName N3 = new PdfName("n3");
    /** A name */
    public static final PdfName N4 = new PdfName("n4");
    /** A name */
    public static final PdfName NAME = new PdfName("Name");
    /** A name */
    public static final PdfName NAMED = new PdfName("Named");
    /** A name */
    public static final PdfName NAMES = new PdfName("Names");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName NAVIGATION = new PdfName("Navigation");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName NAVIGATIONPANE = new PdfName("NavigationPane");
    public static final PdfName NCHANNEL = new PdfName("NChannel");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName NEAR = new PdfName("Near");
    /** A name */
    public static final PdfName NEEDAPPEARANCES = new PdfName("NeedAppearances");
    /**
     * A name.
     * @since 5.4.5
     */
    public static final PdfName NEEDRENDERING= new PdfName("NeedsRendering");
    /** A name */
    public static final PdfName NEWWINDOW = new PdfName("NewWindow");
    /** A name */
    public static final PdfName NEXT = new PdfName("Next");
    /** A name */
    public static final PdfName NEXTPAGE = new PdfName("NextPage");
    /** A name */
    public static final PdfName NM = new PdfName("NM");
    /** A name */
    public static final PdfName NONE = new PdfName("None");
    /** A name */
    public static final PdfName NONFULLSCREENPAGEMODE = new PdfName("NonFullScreenPageMode");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName NONSTRUCT = new PdfName("NonStruct");
    /**
     * A name.
     * @since 5.0.2
     */
    public static final PdfName NOT = new PdfName("Not");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName NOTE = new PdfName("Note");
    /**
     * A name
     * @since 5.1.0
     */
    public static final PdfName NUMBERFORMAT = new PdfName("NumberFormat");
    /** A name */
    public static final PdfName NUMCOPIES = new PdfName("NumCopies");
    /** A name */
    public static final PdfName NUMS = new PdfName("Nums");
    /** A name */
    public static final PdfName O = new PdfName("O");
    /**
     * A name used with Document Structure
     * @since 2.1.5
     */
    public static final PdfName OBJ = new PdfName("Obj");
    /**
     * a name used with Document Structure
     * @since 2.1.5
     */
    public static final PdfName OBJR = new PdfName("OBJR");
    /** A name */
    public static final PdfName OBJSTM = new PdfName("ObjStm");
    /** A name */
    public static final PdfName OC = new PdfName("OC");
    /** A name */
    public static final PdfName OCG = new PdfName("OCG");
    /** A name */
    public static final PdfName OCGS = new PdfName("OCGs");
    /** A name */
    public static final PdfName OCMD = new PdfName("OCMD");
    /** A name */
    public static final PdfName OCPROPERTIES = new PdfName("OCProperties");
    /** A name
     * @since 5.1.3
     */
    public static final PdfName OCSP = new PdfName("OCSP");
    /** A name
     * @since 5.1.3
     */
    public static final PdfName OCSPS = new PdfName("OCSPs");
    /** A name */
    public static final PdfName OE = new PdfName("OE");
    /** A name */
    public static final PdfName Off = new PdfName("Off");
    /** A name */
    public static final PdfName OFF = new PdfName("OFF");
    /** A name */
    public static final PdfName ON = new PdfName("ON");
    /** A name */
    public static final PdfName ONECOLUMN = new PdfName("OneColumn");
    /** A name */
    public static final PdfName OPEN = new PdfName("Open");
    /** A name */
    public static final PdfName OPENACTION = new PdfName("OpenAction");
    /** A name */
    public static final PdfName OP = new PdfName("OP");
    /** A name */
    public static final PdfName op = new PdfName("op");
    /** A name
     * @since 5.4.3
     */
    public static final PdfName OPI = new PdfName("OPI");
    /** A name */
    public static final PdfName OPM = new PdfName("OPM");
    /** A name */
    public static final PdfName OPT = new PdfName("Opt");
    /**
     * A name.
     * @since 5.0.2
     */
    public static final PdfName OR = new PdfName("Or");
    /** A name */
    public static final PdfName ORDER = new PdfName("Order");
    /** A name */
    public static final PdfName ORDERING = new PdfName("Ordering");
    /**
     * A name
     * @since 5.0.2
     */
    public static final PdfName ORG = new PdfName("Org");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName OSCILLATING = new PdfName("Oscillating");

    /** A name */
    public static final PdfName OUTLINES = new PdfName("Outlines");
    /** A name */
    public static final PdfName OUTPUTCONDITION = new PdfName("OutputCondition");
    /** A name */
    public static final PdfName OUTPUTCONDITIONIDENTIFIER = new PdfName("OutputConditionIdentifier");
    /** A name */
    public static final PdfName OUTPUTINTENT = new PdfName("OutputIntent");
    /** A name */
    public static final PdfName OUTPUTINTENTS = new PdfName("OutputIntents");
    /**
     * A name
     * @since 5.5.4
     */
    public static final PdfName OVERLAYTEXT = new PdfName("OverlayText");
    /** A name */
    public static final PdfName P = new PdfName("P");
    /** A name */
    public static final PdfName PAGE = new PdfName("Page");
    /**
     * A name
     * @since 5.0.2
     */
    public static final PdfName PAGEELEMENT = new PdfName("PageElement");
    /** A name */
    public static final PdfName PAGELABELS = new PdfName("PageLabels");
    /** A name */
    public static final PdfName PAGELAYOUT = new PdfName("PageLayout");
    /** A name */
    public static final PdfName PAGEMODE = new PdfName("PageMode");
    /** A name */
    public static final PdfName PAGES = new PdfName("Pages");
    /** A name */
    public static final PdfName PAINTTYPE = new PdfName("PaintType");
    /** A name */
    public static final PdfName PANOSE = new PdfName("Panose");
    /** A name */
    public static final PdfName PARAMS = new PdfName("Params");
    /** A name */
    public static final PdfName PARENT = new PdfName("Parent");
    /** A name */
    public static final PdfName PARENTTREE = new PdfName("ParentTree");
    /**
     * A name used in defining Document Structure.
     * @since 2.1.5
     */
    public static final PdfName PARENTTREENEXTKEY = new PdfName( "ParentTreeNextKey" );
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName PART = new PdfName( "Part" );
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName PASSCONTEXTCLICK = new PdfName("PassContextClick");
    /** A name */
    public static final PdfName PATTERN = new PdfName("Pattern");
    /** A name */
    public static final PdfName PATTERNTYPE = new PdfName("PatternType");
    /**
     * A name
     * @since 5.4.4
     */
    public static final PdfName PB = new PdfName("pb");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName PC = new PdfName("PC");
    /** A name */
    public static final PdfName PDF = new PdfName("PDF");
    /** A name */
    public static final PdfName PDFDOCENCODING = new PdfName("PDFDocEncoding");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName PDU = new PdfName("PDU");
    /** A name */
    public static final PdfName PERCEPTUAL = new PdfName("Perceptual");
    /** A name */
    public static final PdfName PERMS = new PdfName("Perms");
    /** A name */
    public static final PdfName PG = new PdfName("Pg");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName PI = new PdfName("PI");
    /** A name */
    public static final PdfName PICKTRAYBYPDFSIZE = new PdfName("PickTrayByPDFSize");
    /**
     * A name
     * @since 5.5.0
     */
    public static final PdfName PIECEINFO = new PdfName("PieceInfo");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName PLAYCOUNT = new PdfName("PlayCount");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName PO = new PdfName("PO");
    /**
     * A name.
     * @since 5.0.2
     */
    public static final PdfName POLYGON = new PdfName("Polygon");
    /**
     * A name.
     * @since 5.0.2
     */
    public static final PdfName POLYLINE = new PdfName("PolyLine");
    /** A name */
    public static final PdfName POPUP = new PdfName("Popup");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName POSITION = new PdfName("Position");
    /** A name */
    public static final PdfName PREDICTOR = new PdfName("Predictor");
    /** A name */
    public static final PdfName PREFERRED = new PdfName("Preferred");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName PRESENTATION = new PdfName("Presentation");
    /** A name */
    public static final PdfName PRESERVERB = new PdfName("PreserveRB");
    /**
     * A name.
     * @since 5.4.5
     */
    public static final PdfName PRESSTEPS = new PdfName("PresSteps");
    /** A name */
    public static final PdfName PREV = new PdfName("Prev");
    /** A name */
    public static final PdfName PREVPAGE = new PdfName("PrevPage");
    /** A name */
    public static final PdfName PRINT = new PdfName("Print");
    /** A name */
    public static final PdfName PRINTAREA = new PdfName("PrintArea");
    /** A name */
    public static final PdfName PRINTCLIP = new PdfName("PrintClip");
    /**
     * A name
     * @since 5.4.3
     */
    public static final PdfName PRINTERMARK = new PdfName("PrinterMark");
    /**
     * A name
     * @since 5.4.4
     */
    public static final PdfName PRINTFIELD = new PdfName("PrintField");
    /** A name */
    public static final PdfName PRINTPAGERANGE = new PdfName("PrintPageRange");
    /** A name */
    public static final PdfName PRINTSCALING = new PdfName("PrintScaling");
    /** A name */
    public static final PdfName PRINTSTATE = new PdfName("PrintState");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName PRIVATE = new PdfName("Private");
    /** A name */
    public static final PdfName PROCSET = new PdfName("ProcSet");
    /** A name */
    public static final PdfName PRODUCER = new PdfName("Producer");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName PROJCS = new PdfName("PROJCS");
    /** A name */
    public static final PdfName PROP_BUILD = new PdfName("Prop_Build");
    /** A name */
    public static final PdfName PROPERTIES = new PdfName("Properties");
    /** A name */
    public static final PdfName PS = new PdfName("PS");
    /**
     * A name
     * @since 5.1.0
     */
    public static final PdfName PTDATA = new PdfName("PtData");
    /** A name */
    public static final PdfName PUBSEC = new PdfName("Adobe.PubSec");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName PV = new PdfName("PV");
    /** A name */
    public static final PdfName Q = new PdfName("Q");
    /** A name */
    public static final PdfName QUADPOINTS = new PdfName("QuadPoints");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName QUOTE = new PdfName("Quote");
    /** A name */
    public static final PdfName R = new PdfName("R");
    /** A name */
    public static final PdfName R2L = new PdfName("R2L");
    /** A name */
    public static final PdfName RANGE = new PdfName("Range");
    /**
     * A name
     * @since 5.4.3
     */
    public static final PdfName RB = new PdfName("RB");
    /**
     * A name
     * @since 5.4.4
     */
    public static final PdfName rb = new PdfName("rb");
    /** A name */
    public static final PdfName RBGROUPS = new PdfName("RBGroups");
    /** A name */
    public static final PdfName RC = new PdfName("RC");
    /**
     * A name
     * @since 5.1.0
     */
    public static final PdfName RD = new PdfName("RD");
    /** A name */
    public static final PdfName REASON = new PdfName("Reason");
    /** A name */
    public static final PdfName RECIPIENTS = new PdfName("Recipients");
    /** A name */
    public static final PdfName RECT = new PdfName("Rect");
    /**
     * A name
     * @since 5.4.4
     */
    public static final PdfName REDACT = new PdfName("Redact");
    /** A name */
    public static final PdfName REFERENCE = new PdfName("Reference");
    /** A name */
    public static final PdfName REGISTRY = new PdfName("Registry");
    /** A name */
    public static final PdfName REGISTRYNAME = new PdfName("RegistryName");
    /**
     * A name
     * @since	2.1.5 renamed from RELATIVECALORIMETRIC
     */
    public static final PdfName RELATIVECOLORIMETRIC = new PdfName("RelativeColorimetric");
    /** A name */
    public static final PdfName RENDITION = new PdfName("Rendition");
    /**
     * A name
     * @since 5.5.4
     */
    public static final PdfName REPEAT = new PdfName("Repeat");
    /** A name */
    public static final PdfName RESETFORM = new PdfName("ResetForm");
    /** A name */
    public static final PdfName RESOURCES = new PdfName("Resources");
    public static final PdfName REQUIREMENTS = new PdfName("Requirements");
    public static final PdfName REVERSEDCHARS = new PdfName("ReversedChars");
    /** A name */
    public static final PdfName RI = new PdfName("RI");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIA = new PdfName("RichMedia");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIAACTIVATION = new PdfName("RichMediaActivation");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIAANIMATION = new PdfName("RichMediaAnimation");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName RICHMEDIACOMMAND = new PdfName("RichMediaCommand");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIACONFIGURATION = new PdfName("RichMediaConfiguration");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIACONTENT = new PdfName("RichMediaContent");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIADEACTIVATION = new PdfName("RichMediaDeactivation");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIAEXECUTE = new PdfName("RichMediaExecute");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIAINSTANCE = new PdfName("RichMediaInstance");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIAPARAMS = new PdfName("RichMediaParams");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIAPOSITION = new PdfName("RichMediaPosition");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIAPRESENTATION = new PdfName("RichMediaPresentation");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIASETTINGS = new PdfName("RichMediaSettings");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName RICHMEDIAWINDOW = new PdfName("RichMediaWindow");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName RL = new PdfName("RL");
    /**
     * A name
     * @since 5.4.4
     */
    public static final PdfName ROLE = new PdfName("Role");
    /**
     * A name
     * @since 5.4.4
     */
    public static final PdfName RO = new PdfName("RO");
    /** A name */
    public static final PdfName ROLEMAP = new PdfName("RoleMap");
    /** A name */
    public static final PdfName ROOT = new PdfName("Root");
    /** A name */
    public static final PdfName ROTATE = new PdfName("Rotate");
    /**
     * A name
     * @since 5.4.0
     */
    public static final PdfName ROW = new PdfName("Row");
    /** A name */
    public static final PdfName ROWS = new PdfName("Rows");
    /**
     * A name
     * @since 5.4.0
     */
    public static final PdfName ROWSPAN = new PdfName("RowSpan");
    /**
     * A name
     * @since 5.4.3
     */
    public static final PdfName RP = new PdfName("RP");
    /**
     * A name
     * @since 5.1.0
     */
    public static final PdfName RT = new PdfName("RT");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName RUBY = new PdfName( "Ruby" );
    /** A name */
    public static final PdfName RUNLENGTHDECODE = new PdfName("RunLengthDecode");
    /** A name */
    public static final PdfName RV = new PdfName("RV");
    /** A name */
    public static final PdfName S = new PdfName("S");
    /** A name */
    public static final PdfName SATURATION = new PdfName("Saturation");
    /** A name */
    public static final PdfName SCHEMA = new PdfName("Schema");
    /**
     * A name.
     * @since 5.4.0
     */
    public static final PdfName SCOPE = new PdfName("Scope");
    /** A name */
    public static final PdfName SCREEN = new PdfName("Screen");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName SCRIPTS = new PdfName("Scripts");
    /** A name */
    public static final PdfName SECT = new PdfName("Sect");
    /** A name */
    public static final PdfName SEPARATION = new PdfName("Separation");
    /** A name */
    public static final PdfName SETOCGSTATE = new PdfName("SetOCGState");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName SETTINGS = new PdfName("Settings");
    /** A name */
    public static final PdfName SHADING = new PdfName("Shading");
    /** A name */
    public static final PdfName SHADINGTYPE = new PdfName("ShadingType");
    /** A name */
    public static final PdfName SHIFT_JIS = new PdfName("Shift-JIS");
    /** A name */
    public static final PdfName SIG = new PdfName("Sig");
    /** A name */
    public static final PdfName SIGFIELDLOCK = new PdfName("SigFieldLock");
    /** A name */
    public static final PdfName SIGFLAGS = new PdfName("SigFlags");
    /** A name */
    public static final PdfName SIGREF = new PdfName("SigRef");
    /** A name */
    public static final PdfName SIMPLEX = new PdfName("Simplex");
    /** A name */
    public static final PdfName SINGLEPAGE = new PdfName("SinglePage");
    /** A name */
    public static final PdfName SIZE = new PdfName("Size");
    /** A name */
    public static final PdfName SMASK = new PdfName("SMask");

    public static final PdfName SMASKINDATA = new PdfName("SMaskInData");
    /** A name */
    public static final PdfName SORT = new PdfName("Sort");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName SOUND = new PdfName("Sound");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName SPACEAFTER = new PdfName("SpaceAfter");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName SPACEBEFORE = new PdfName("SpaceBefore");
    /** A name */
    public static final PdfName SPAN = new PdfName("Span");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName SPEED = new PdfName("Speed");
    /** A name */
    public static final PdfName SPLIT = new PdfName("Split");
    /** A name */
    public static final PdfName SQUARE = new PdfName("Square");
    /**
     * A name
     * @since 2.1.3
     */
    public static final PdfName SQUIGGLY = new PdfName("Squiggly");
    /**
     * A name
     * @since 5.1.0
     */
    public static final PdfName SS = new PdfName("SS");
    /** A name */
    public static final PdfName ST = new PdfName("St");
    /** A name */
    public static final PdfName STAMP = new PdfName("Stamp");
    /** A name */
    public static final PdfName STATUS = new PdfName("Status");
    /** A name */
    public static final PdfName STANDARD = new PdfName("Standard");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName START = new PdfName("Start");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName STARTINDENT = new PdfName("StartIndent");
    /** A name */
    public static final PdfName STATE = new PdfName("State");
    /** A name */
    public static final PdfName STDCF = new PdfName("StdCF");
    /** A name */
    public static final PdfName STEMV = new PdfName("StemV");
    /** A name */
    public static final PdfName STMF = new PdfName("StmF");
    /** A name */
    public static final PdfName STRF = new PdfName("StrF");
    /** A name */
    public static final PdfName STRIKEOUT = new PdfName("StrikeOut");
    /**
     * A name.
     * @since iText 5.0.6
     */
    public static final PdfName STRUCTELEM = new PdfName("StructElem");
    /** A name */
    public static final PdfName STRUCTPARENT = new PdfName("StructParent");
    /** A name */
    public static final PdfName STRUCTPARENTS = new PdfName("StructParents");
    /** A name */
    public static final PdfName STRUCTTREEROOT = new PdfName("StructTreeRoot");
    /** A name */
    public static final PdfName STYLE = new PdfName("Style");
    /** A name */
    public static final PdfName SUBFILTER = new PdfName("SubFilter");
    /** A name */
    public static final PdfName SUBJECT = new PdfName("Subject");
    /** A name */
    public static final PdfName SUBMITFORM = new PdfName("SubmitForm");
    /** A name */
    public static final PdfName SUBTYPE = new PdfName("Subtype");
    /**
     * A name
     */
    public static final PdfName SUMMARY = new PdfName("Summary");
    /** A name */
    public static final PdfName SUPPLEMENT = new PdfName("Supplement");
    /** A name */
    public static final PdfName SV = new PdfName("SV");
    /** A name */
    public static final PdfName SW = new PdfName("SW");
    /** A name of a base 14 type 1 font */
    public static final PdfName SYMBOL = new PdfName("Symbol");
    /**
     * T is very commonly used for various dictionary entries, including title
     * entries in a Tagged PDF element dictionary, and target dictionaries.
     */
    public static final PdfName T = new PdfName("T");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName TA = new PdfName("TA");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName TABLE = new PdfName("Table");
    /**
     * A name
     * @since	2.1.5
     */
    public static final PdfName TABS = new PdfName("Tabs");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName TBODY = new PdfName("TBody");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName TD = new PdfName("TD");
    /**
     * A name
     * @since 5.3.5
     */
    public static final PdfName TR = new PdfName("TR");
    /**
     * A name
     * @since 5.4.3
     */
    public static final PdfName TR2 = new PdfName("TR2");
    /** A name */
    public static final PdfName TEXT = new PdfName("Text");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName TEXTALIGN = new PdfName("TextAlign");
    /**
     * A name
     * @since 5.3.5
     */
    public static final PdfName TEXTDECORATIONCOLOR = new PdfName("TextDecorationColor");
    /**
     * A name
     * @since 5.3.5
     */
    public static final PdfName TEXTDECORATIONTHICKNESS = new PdfName("TextDecorationThickness");
    /**
     * A name
     * @since 5.3.5
     */
    public static final PdfName TEXTDECORATIONTYPE = new PdfName("TextDecorationType");
    /**
     * A name
     * @since 5.3.4
     */
    public static final PdfName TEXTINDENT = new PdfName("TextIndent");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName TFOOT = new PdfName("TFoot");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName TH = new PdfName("TH");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName THEAD = new PdfName("THead");
    /** A name */
    public static final PdfName THUMB = new PdfName("Thumb");
    /** A name */
    public static final PdfName THREADS = new PdfName("Threads");
    /** A name */
    public static final PdfName TI = new PdfName("TI");
    /**
     * A name
     * @since	2.1.6
     */
    public static final PdfName TIME = new PdfName("Time");
    /** A name */
    public static final PdfName TILINGTYPE = new PdfName("TilingType");
    /** A name of a base 14 type 1 font */
    public static final PdfName TIMES_ROMAN = new PdfName("Times-Roman");
    /** A name of a base 14 type 1 font */
    public static final PdfName TIMES_BOLD = new PdfName("Times-Bold");
    /** A name of a base 14 type 1 font */
    public static final PdfName TIMES_ITALIC = new PdfName("Times-Italic");
    /** A name of a base 14 type 1 font */
    public static final PdfName TIMES_BOLDITALIC = new PdfName("Times-BoldItalic");
    /**
     * Use Title for the document's top level title (optional), and for document
     * outline dictionaries, which can store bookmarks.
     * For all other uses of a title entry, including Tagged PDF, use {@link #T}
     */
    public static final PdfName TITLE = new PdfName("Title");
    /** A name */
    public static final PdfName TK = new PdfName("TK");
    /** A name */
    public static final PdfName TM = new PdfName("TM");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName TOC = new PdfName("TOC");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName TOCI = new PdfName("TOCI");
    /** A name */
    public static final PdfName TOGGLE = new PdfName("Toggle");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName TOOLBAR = new PdfName("Toolbar");
    /** A name */
    public static final PdfName TOUNICODE = new PdfName("ToUnicode");
    /** A name */
    public static final PdfName TP = new PdfName("TP");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName TABLEROW = new PdfName( "TR" );
    /** A name */
    public static final PdfName TRANS = new PdfName("Trans");
    /** A name */
    public static final PdfName TRANSFORMPARAMS = new PdfName("TransformParams");
    /** A name */
    public static final PdfName TRANSFORMMETHOD = new PdfName("TransformMethod");
    /** A name */
    public static final PdfName TRANSPARENCY = new PdfName("Transparency");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName TRANSPARENT = new PdfName("Transparent");
    /**
     * A name
     * @since 5.4.3
     */
    public static final PdfName TRAPNET = new PdfName("TrapNet");
    /** A name */
    public static final PdfName TRAPPED = new PdfName("Trapped");
    /** A name */
    public static final PdfName TRIMBOX = new PdfName("TrimBox");
    /** A name */
    public static final PdfName TRUETYPE = new PdfName("TrueType");
    /**
     * A name
     * @since 5.1.3
     */
    public static final PdfName TS = new PdfName("TS");
    /**
     * A name
     * @since 5.0.2
     */
    public static final PdfName TTL = new PdfName("Ttl");
    /** A name */
    public static final PdfName TU = new PdfName("TU");
    /**
     * A name
     * @since 5.4.4
     */
    public static final PdfName TV = new PdfName("tv");
    /** A name */
    public static final PdfName TWOCOLUMNLEFT = new PdfName("TwoColumnLeft");
    /** A name */
    public static final PdfName TWOCOLUMNRIGHT = new PdfName("TwoColumnRight");
    /** A name */
    public static final PdfName TWOPAGELEFT = new PdfName("TwoPageLeft");
    /** A name */
    public static final PdfName TWOPAGERIGHT = new PdfName("TwoPageRight");
    /** A name */
    public static final PdfName TX = new PdfName("Tx");
    /** A name */
    public static final PdfName TYPE = new PdfName("Type");
    /** A name */
    public static final PdfName TYPE0 = new PdfName("Type0");
    /** A name */
    public static final PdfName TYPE1 = new PdfName("Type1");
    /** A name of an attribute. */
    public static final PdfName TYPE3 = new PdfName("Type3");
    /** A name of an attribute. */
    public static final PdfName U = new PdfName("U");
    /** A name */
    public static final PdfName UE = new PdfName("UE");
    /** A name of an attribute. */
    public static final PdfName UF = new PdfName("UF");
    /** A name of an attribute. */
    public static final PdfName UHC = new PdfName("UHC");
    /** A name of an attribute. */
    public static final PdfName UNDERLINE = new PdfName("Underline");
    /**
     * A name.
     * @since 5.2.1
     */
    public static final PdfName UNIX = new PdfName("Unix");
    /**
     * A name.
     * @since 5.4.0
     */
    public static final PdfName UPPERALPHA = new PdfName("UpperAlpha");
    /**
     * A name.
     * @since 5.4.0
     */
    public static final PdfName UPPERROMAN = new PdfName("UpperRoman");
    /** A name */
    public static final PdfName UR = new PdfName("UR");
    /** A name */
    public static final PdfName UR3 = new PdfName("UR3");
    /** A name */
    public static final PdfName URI = new PdfName("URI");
    /** A name */
    public static final PdfName URL = new PdfName("URL");
    /** A name */
    public static final PdfName USAGE = new PdfName("Usage");
    /** A name */
    public static final PdfName USEATTACHMENTS = new PdfName("UseAttachments");
    /** A name */
    public static final PdfName USENONE = new PdfName("UseNone");
    /** A name */
    public static final PdfName USEOC = new PdfName("UseOC");
    /** A name */
    public static final PdfName USEOUTLINES = new PdfName("UseOutlines");
    /** A name */
    public static final PdfName USER = new PdfName("User");
    /** A name */
    public static final PdfName USERPROPERTIES = new PdfName("UserProperties");
    /** A name */
    public static final PdfName USERUNIT = new PdfName("UserUnit");
    /** A name */
    public static final PdfName USETHUMBS = new PdfName("UseThumbs");
    /**
     * A name.
     * @since 5.4.0
     */
    public static final PdfName UTF_8 = new PdfName("utf_8");
    /** A name */
    public static final PdfName V = new PdfName("V");
    /** A name */
    public static final PdfName V2 = new PdfName("V2");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName VALIGN = new PdfName("VAlign");
    /**
     * A name.
     * @since 5.0.2
     */
    public static final PdfName VE = new PdfName("VE");
    /** A name */
    public static final PdfName VERISIGN_PPKVS = new PdfName("VeriSign.PPKVS");
    /** A name */
	public static final PdfName VERSION = new PdfName("Version");
    /**
     * A name.
     * @since 5.0.2
     */
    public static final PdfName VERTICES = new PdfName("Vertices");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName VIDEO = new PdfName("Video");
    /** A name */
    public static final PdfName VIEW = new PdfName("View");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName VIEWS = new PdfName("Views");
    /** A name */
    public static final PdfName VIEWAREA = new PdfName("ViewArea");
    /** A name */
    public static final PdfName VIEWCLIP = new PdfName("ViewClip");
    /** A name */
    public static final PdfName VIEWERPREFERENCES = new PdfName("ViewerPreferences");
    /**
     * A name
     * @since 5.1.0
     */
    public static final PdfName VIEWPORT = new PdfName("Viewport");
    /** A name */
    public static final PdfName VIEWSTATE = new PdfName("ViewState");
    /** A name */
    public static final PdfName VISIBLEPAGES = new PdfName("VisiblePages");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName VOFFSET = new PdfName("VOffset");
    /**
     * A name
     * @since 5.1.0
     */
    public static final PdfName VP = new PdfName("VP");
    /**
     * A name
     * @since 5.1.3
     */
    public static final PdfName VRI = new PdfName("VRI");
    /** A name of an attribute. */
    public static final PdfName W = new PdfName("W");
    /** A name of an attribute. */
    public static final PdfName W2 = new PdfName("W2");
    /**
     * A name
     * @since 2.1.6
     */
    public static final PdfName WARICHU = new PdfName("Warichu");
    /**
     * A name
     * @since 5.4.5
     */
    public static final PdfName WATERMARK = new PdfName("Watermark");
    /** A name of an attribute. */
    public static final PdfName WC = new PdfName("WC");
    /** A name of an attribute. */
    public static final PdfName WIDGET = new PdfName("Widget");
    /** A name of an attribute. */
    public static final PdfName WIDTH = new PdfName("Width");
    /** A name */
    public static final PdfName WIDTHS = new PdfName("Widths");
    /** A name of an encoding */
    public static final PdfName WIN = new PdfName("Win");
    /** A name of an encoding */
    public static final PdfName WIN_ANSI_ENCODING = new PdfName("WinAnsiEncoding");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName WINDOW = new PdfName("Window");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName WINDOWED = new PdfName("Windowed");
    /** A name of an encoding */
    public static final PdfName WIPE = new PdfName("Wipe");
    /** A name */
    public static final PdfName WHITEPOINT = new PdfName("WhitePoint");
    /**
     * A name of an attribute.
     * @since 5.1.0
     */
    public static final PdfName WKT = new PdfName("WKT");
    /** A name */
    public static final PdfName WP = new PdfName("WP");
    /** A name of an encoding */
    public static final PdfName WS = new PdfName("WS");
    /**
     * A name
     * @since 5.4.3
     */
    public static final PdfName WT = new PdfName("WT");
    /** A name */
    public static final PdfName X = new PdfName("X");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName XA = new PdfName("XA");
    /**
     * A name.
     * @since 2.1.6
     */
    public static final PdfName XD = new PdfName("XD");
    /** A name */
    public static final PdfName XFA = new PdfName("XFA");
    /** A name */
    public static final PdfName XML = new PdfName("XML");
    /** A name */
    public static final PdfName XOBJECT = new PdfName("XObject");
    /**
     * A name
     * @since 5.1.0
     */
    public static final PdfName XPTS = new PdfName("XPTS");
    /** A name */
    public static final PdfName XREF = new PdfName("XRef");
    /** A name */
    public static final PdfName XREFSTM = new PdfName("XRefStm");
    /** A name */
    public static final PdfName XSTEP = new PdfName("XStep");
    /** A name */
    public static final PdfName XYZ = new PdfName("XYZ");
    /** A name */
    public static final PdfName YSTEP = new PdfName("YStep");
    /** A name */
    public static final PdfName ZADB = new PdfName("ZaDb");
    /** A name of a base 14 type 1 font */
    public static final PdfName ZAPFDINGBATS = new PdfName("ZapfDingbats");
    /** A name */
    public static final PdfName ZOOM = new PdfName("Zoom");

    /**
     * map strings to all known static names
     * @since 2.1.6
     */
    public static Map<String, PdfName> staticNames;

    /**
     * Use reflection to cache all the static public final names so
     * future <code>PdfName</code> additions don't have to be "added twice".
     * A bit less efficient (around 50ms spent here on a 2.2ghz machine),
     *  but Much Less error prone.
     * @since 2.1.6
     */

    static {
        Field fields[] = PdfName.class.getDeclaredFields();
        staticNames = new HashMap<String, PdfName>( fields.length );
        final int flags = Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL;
        try {
            for (int fldIdx = 0; fldIdx < fields.length; ++fldIdx) {
                Field curFld = fields[fldIdx];
                if ((curFld.getModifiers() & flags) == flags &&
                    curFld.getType().equals( PdfName.class )) {
                    PdfName name = (PdfName)curFld.get( null );
                    staticNames.put( decodeName( name.toString() ), name );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // CLASS VARIABLES

    private int hash = 0;

    // CONSTRUCTORS

    /**
     * Constructs a new <CODE>PdfName</CODE>. The name length will be checked.
     *
     * @param name the new name
     */
    public PdfName(String name) {
        this(name, true);
    }

    /**
     * Constructs a new <CODE>PdfName</CODE>.
     * @param name the new name
     * @param lengthCheck if <CODE>true</CODE> check the length validity,
     * if <CODE>false</CODE> the name can have any length
     */
    public PdfName(String name, boolean lengthCheck) {
        super(PdfObject.NAME);
        // The minimum number of characters in a name is 0, the maximum is 127 (the '/' not included)
        int length = name.length();
        if (lengthCheck && length > 127)
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.name.1.is.too.long.2.characters", name, String.valueOf(length)));
        bytes = encodeName(name);
    }

    /**
     * Constructs a PdfName.
     *
     * @param bytes the byte representation of the name
     */
    public PdfName(byte bytes[]) {
        super(PdfObject.NAME, bytes);
    }

    // CLASS METHODS

    /**
     * Compares this object with the specified object for order.
     * Returns a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.<p>
     *
     * @param name the PdfName to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     * from being compared to this Object.
     */
    public int compareTo(PdfName name) {
        byte myBytes[] = bytes;
        byte objBytes[] = name.bytes;
        int len = Math.min(myBytes.length, objBytes.length);
        for(int i = 0; i < len; i++) {
            if (myBytes[i] > objBytes[i])
                return 1;
            if (myBytes[i] < objBytes[i])
                return -1;
        }
        if (myBytes.length < objBytes.length)
            return -1;
        if (myBytes.length > objBytes.length)
            return 1;
        return 0;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof PdfName)
            return compareTo((PdfName)obj) == 0;
        return false;
    }

    /**
     * Returns a hash code value for the object.
     * This method is supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     *
     * @return  a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            int ptr = 0;
            int len = bytes.length;
            for (int i = 0; i < len; i++)
                h = 31*h + (bytes[ptr++] & 0xff);
            hash = h;
        }
        return h;
    }

    /**
     * Encodes a plain name given in the unescaped form "AB CD" into "/AB#20CD".
     *
     * @param name the name to encode
     * @return the encoded name
     * @since	2.1.5
     */
    public static byte[] encodeName(String name) {
    	int length = name.length();
    	ByteBuffer buf = new ByteBuffer(length + 20);
    	buf.append('/');
    	char c;
    	char chars[] = name.toCharArray();
    	for (int k = 0; k < length; k++) {
    		c = (char)(chars[k] & 0xff);
    		// Escape special characters
    		switch (c) {
    			case ' ':
    			case '%':
    			case '(':
    			case ')':
    			case '<':
    			case '>':
    			case '[':
    			case ']':
    			case '{':
    			case '}':
    			case '/':
    			case '#':
    				buf.append('#');
    				buf.append(Integer.toString(c, 16));
    				break;
    			default:
    				if (c >= 32 && c <= 126)
    					buf.append(c);
    				else {
    					buf.append('#');
    					if (c < 16)
    						buf.append('0');
    					buf.append(Integer.toString(c, 16));
    				}
    				break;
    			}
    		}
    	return buf.toByteArray();
    }

    /**
     * Decodes an escaped name given in the form "/AB#20CD" into "AB CD".
     *
     * @param name the name to decode
     * @return the decoded name
     */
    public static String decodeName(String name) {
        StringBuffer buf = new StringBuffer();
        try {
            int len = name.length();
            for (int k = 1; k < len; ++k) {
                char c = name.charAt(k);
                if (c == '#') {
                	char c1 = name.charAt(k + 1);
                	char c2 = name.charAt(k + 2);
                    c = (char)((PRTokeniser.getHex(c1) << 4) + PRTokeniser.getHex(c2));
                    k += 2;
                }
                buf.append(c);
            }
        }
        catch (IndexOutOfBoundsException e) {
            // empty on purpose
        }
        return buf.toString();
    }
}
