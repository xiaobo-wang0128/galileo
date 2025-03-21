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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.xml.XmlDomWriter;

/**
 * Processes XFA forms.
 * @author Paulo Soares
 */
public class XfaForm {

    private Xml2SomTemplate templateSom;
    private Node templateNode;
    private Xml2SomDatasets datasetsSom;
    private Node datasetsNode;
    private AcroFieldsSearch acroFieldsSom;
    private PdfReader reader;
    private boolean xfaPresent;
    private org.w3c.dom.Document domDocument;
    private boolean changed;
    public static final String XFA_DATA_SCHEMA = "http://www.xfa.org/schema/xfa-data/1.0/";

    /**
     * An empty constructor to build on.
     */
    public XfaForm() {
    }

    /**
     * Return the XFA Object, could be an array, could be a Stream.
     * Returns null f no XFA Object is present.
     * @param	reader	a PdfReader instance
     * @return	the XFA object
     * @since	2.1.3
     */
    public static PdfObject getXfaObject(PdfReader reader) {
    	PdfDictionary af = (PdfDictionary)PdfReader.getPdfObjectRelease(reader.getCatalog().get(PdfName.ACROFORM));
        if (af == null) {
            return null;
        }
        return PdfReader.getPdfObjectRelease(af.get(PdfName.XFA));
    }

    /**
     * A constructor from a <CODE>PdfReader</CODE>. It basically does everything
     * from finding the XFA stream to the XML parsing.
     * @param reader the reader
     * @throws java.io.IOException on error
     * @throws javax.xml.parsers.ParserConfigurationException on error
     * @throws org.xml.sax.SAXException on error
     */
    public XfaForm(PdfReader reader) throws IOException, ParserConfigurationException, SAXException {
        this.reader = reader;
        PdfObject xfa = getXfaObject(reader);
        if (xfa == null) {
        	xfaPresent = false;
        	return;
        }
        xfaPresent = true;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        if (xfa.isArray()) {
            PdfArray ar = (PdfArray)xfa;
            for (int k = 1; k < ar.size(); k += 2) {
                PdfObject ob = ar.getDirectObject(k);
                if (ob instanceof PRStream) {
                    byte[] b = PdfReader.getStreamBytes((PRStream)ob);
                    bout.write(b);
                }
            }
        }
        else if (xfa instanceof PRStream) {
            byte[] b = PdfReader.getStreamBytes((PRStream)xfa);
            bout.write(b);
        }
        bout.close();
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setNamespaceAware(true);
        DocumentBuilder db = fact.newDocumentBuilder();
        db.setEntityResolver(new SafeEmptyEntityResolver());
        domDocument = db.parse(new ByteArrayInputStream(bout.toByteArray()));
        extractNodes();
    }

    /**
     * Extracts the nodes from the domDocument.
     * @since	2.1.5
     */
    private void extractNodes() {
        Map<String, Node> xfaNodes = extractXFANodes(domDocument);

        if (xfaNodes.containsKey("template")) {
            templateNode = xfaNodes.get("template");
            templateSom = new Xml2SomTemplate(templateNode);
        }
        if (xfaNodes.containsKey("datasets")) {
            datasetsNode = xfaNodes.get("datasets");
            Node dataNode = findDataNode(datasetsNode);
            datasetsSom = new Xml2SomDatasets(dataNode != null ? dataNode : datasetsNode.getFirstChild());
        }
        if (datasetsNode == null)
        	createDatasetsNode(domDocument.getFirstChild());
    }

    private Node findDataNode(Node datasetsNode) {
        NodeList childNodes = datasetsNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeName().equals("xfa:data")) {
                return childNodes.item(i);
            }
        }
        return null;
    }

    public static Map<String, Node> extractXFANodes(Document domDocument) {
        Map<String, Node> xfaNodes = new HashMap<String, Node>();
        Node n = domDocument.getFirstChild();
        while (n.getChildNodes().getLength() == 0) {
        	n = n.getNextSibling();
        }
        n = n.getFirstChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String s = n.getLocalName();
                xfaNodes.put(s, n);
            }
            n = n.getNextSibling();
        }

        return xfaNodes;
    }
    /**
     * Some XFA forms don't have a datasets node.
     * If this is the case, we have to add one.
     */
    private void createDatasetsNode(Node n) {
    	while (n.getChildNodes().getLength() == 0) {
    		n = n.getNextSibling();
    	}
    	if (n != null) {
            Element e = n.getOwnerDocument().createElement("xfa:datasets");
            e.setAttribute("xmlns:xfa", XFA_DATA_SCHEMA);
            datasetsNode = e;
            n.appendChild(datasetsNode);
    	}
    }

    /**
     * Sets the XFA key from a byte array. The old XFA is erased.
     * @param form the data
     * @param reader the reader
     * @param writer the writer
     * @throws java.io.IOException on error
     */
    public static void setXfa(XfaForm form, PdfReader reader, PdfWriter writer) throws IOException {
        PdfDictionary af = (PdfDictionary)PdfReader.getPdfObjectRelease(reader.getCatalog().get(PdfName.ACROFORM));
        if (af == null) {
            return;
        }
        PdfObject xfa = getXfaObject(reader);
        if (xfa.isArray()) {
            PdfArray ar = (PdfArray)xfa;
            int t = -1;
            int d = -1;
            for (int k = 0; k < ar.size(); k += 2) {
                PdfString s = ar.getAsString(k);
                if ("template".equals(s.toString())) {
                	t = k + 1;
                }
                if ("datasets".equals(s.toString())) {
                	d = k + 1;
                }
            }
            if (t > -1 && d > -1) {
                reader.killXref(ar.getAsIndirectObject(t));
                reader.killXref(ar.getAsIndirectObject(d));
                PdfStream tStream = new PdfStream(serializeDoc(form.templateNode));
                tStream.flateCompress(writer.getCompressionLevel());
                ar.set(t, writer.addToBody(tStream).getIndirectReference());
                PdfStream dStream = new PdfStream(serializeDoc(form.datasetsNode));
                dStream.flateCompress(writer.getCompressionLevel());
                ar.set(d, writer.addToBody(dStream).getIndirectReference());
                af.put(PdfName.XFA, new PdfArray(ar));
            	return;
            }
        }
        reader.killXref(af.get(PdfName.XFA));
        PdfStream str = new PdfStream(serializeDoc(form.domDocument));
        str.flateCompress(writer.getCompressionLevel());
        PdfIndirectReference ref = writer.addToBody(str).getIndirectReference();
        af.put(PdfName.XFA, ref);
    }

    /**
     * Sets the XFA key from the instance data. The old XFA is erased.
     * @param writer the writer
     * @throws java.io.IOException on error
     */
    public void setXfa(PdfWriter writer) throws IOException {
        setXfa(this, reader, writer);
    }

    /**
     * Serializes a XML document to a byte array.
     * @param n the XML document
     * @throws java.io.IOException on error
     * @return the serialized XML document
     */
    public static byte[] serializeDoc(Node n) throws IOException {
        XmlDomWriter xw = new XmlDomWriter();
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        xw.setOutput(fout, null);
        xw.setCanonical(false);
        xw.write(n);
        fout.close();
        return fout.toByteArray();
    }

    /**
     * Returns <CODE>true</CODE> if it is a XFA form.
     * @return <CODE>true</CODE> if it is a XFA form
     */
    public boolean isXfaPresent() {
        return xfaPresent;
    }

    /**
     * Gets the top level DOM document.
     * @return the top level DOM document
     */
    public org.w3c.dom.Document getDomDocument() {
        return domDocument;
    }

    /**
     * Finds the complete field name contained in the "classic" forms from a partial
     * name.
     * @param name the complete or partial name
     * @param af the fields
     * @return the complete name or <CODE>null</CODE> if not found
     */
    public String findFieldName(String name, AcroFields af) {
        Map<String, AcroFields.Item> items = af.getFields();
        if (items.containsKey(name))
            return name;
        if (acroFieldsSom == null) {
        	if (items.isEmpty() && xfaPresent)
        		acroFieldsSom = new AcroFieldsSearch(datasetsSom.getName2Node().keySet());
        	else
        		acroFieldsSom = new AcroFieldsSearch(items.keySet());
        }
        if (acroFieldsSom.getAcroShort2LongName().containsKey(name))
            return acroFieldsSom.getAcroShort2LongName().get(name);
        return acroFieldsSom.inverseSearchGlobal(Xml2Som.splitParts(name));
    }

    /**
     * Finds the complete SOM name contained in the datasets section from a
     * possibly partial name.
     * @param name the complete or partial name
     * @return the complete name or <CODE>null</CODE> if not found
     */
    public String findDatasetsName(String name) {
        if (datasetsSom.getName2Node().containsKey(name))
            return name;
        return datasetsSom.inverseSearchGlobal(Xml2Som.splitParts(name));
    }

    /**
     * Finds the <CODE>Node</CODE> contained in the datasets section from a
     * possibly partial name.
     * @param name the complete or partial name
     * @return the <CODE>Node</CODE> or <CODE>null</CODE> if not found
     */
    public Node findDatasetsNode(String name) {
        if (name == null)
            return null;
        name = findDatasetsName(name);
        if (name == null)
            return null;
        return datasetsSom.getName2Node().get(name);
    }

    /**
     * Gets all the text contained in the child nodes of this node.
     * @param n the <CODE>Node</CODE>
     * @return the text found or "" if no text was found
     */
    public static String getNodeText(Node n) {
        if (n == null)
            return "";
        return getNodeText(n, "");

    }

    private static String getNodeText(Node n, String name) {
        Node n2 = n.getFirstChild();
        while (n2 != null) {
            if (n2.getNodeType() == Node.ELEMENT_NODE) {
                name = getNodeText(n2, name);
            }
            else if (n2.getNodeType() == Node.TEXT_NODE) {
                name += n2.getNodeValue();
            }
            n2 = n2.getNextSibling();
        }
        return name;
    }

    /**
     * Sets the text of this node. All the child's node are deleted and a new
     * child text node is created.
     * @param n the <CODE>Node</CODE> to add the text to
     * @param text the text to add
     */
    public void setNodeText(Node n, String text) {
        if (n == null)
            return;
        Node nc = null;
        while ((nc = n.getFirstChild()) != null) {
            n.removeChild(nc);
        }
        if (n.getAttributes().getNamedItemNS(XFA_DATA_SCHEMA, "dataNode") != null)
            n.getAttributes().removeNamedItemNS(XFA_DATA_SCHEMA, "dataNode");
        n.appendChild(domDocument.createTextNode(text));
        changed = true;
    }

    /**
     * Sets the XFA form flag signaling that this is a valid XFA form.
     * @param xfaPresent the XFA form flag signaling that this is a valid XFA form
     */
    public void setXfaPresent(boolean xfaPresent) {
        this.xfaPresent = xfaPresent;
    }

    /**
     * Sets the top DOM document.
     * @param domDocument the top DOM document
     */
    public void setDomDocument(org.w3c.dom.Document domDocument) {
        this.domDocument = domDocument;
        extractNodes();
    }

    /**
     * Gets the <CODE>PdfReader</CODE> used by this instance.
     * @return the <CODE>PdfReader</CODE> used by this instance
     */
    public PdfReader getReader() {
        return reader;
    }

    /**
     * Sets the <CODE>PdfReader</CODE> to be used by this instance.
     * @param reader the <CODE>PdfReader</CODE> to be used by this instance
     */
    public void setReader(PdfReader reader) {
        this.reader = reader;
    }

    /**
     * Checks if this XFA form was changed.
     * @return <CODE>true</CODE> if this XFA form was changed
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * Sets the changed status of this XFA instance.
     * @param changed the changed status of this XFA instance
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    /**
     * A structure to store each part of a SOM name and link it to the next part
     * beginning from the lower hierarchy.
     */
    public static class InverseStore {
        protected ArrayList<String> part = new ArrayList<String>();
        protected ArrayList<Object> follow = new ArrayList<Object>();

        /**
         * Gets the full name by traversing the hierarchy using only the
         * index 0.
         * @return the full name
         */
        public String getDefaultName() {
            InverseStore store = this;
            while (true) {
                Object obj = store.follow.get(0);
                if (obj instanceof String)
                    return (String)obj;
                store = (InverseStore)obj;
            }
        }

        /**
         * Search the current node for a similar name. A similar name starts
         * with the same name but has a different index. For example, "detail[3]"
         * is similar to "detail[9]". The main use is to discard names that
         * correspond to out of bounds records.
         * @param name the name to search
         * @return <CODE>true</CODE> if a similitude was found
         */
        public boolean isSimilar(String name) {
            int idx = name.indexOf('[');
            name = name.substring(0, idx + 1);
            for (int k = 0; k < part.size(); ++k) {
                if (part.get(k).startsWith(name))
                    return true;
            }
            return false;
        }
    }

    /**
     * Another stack implementation. The main use is to facilitate
     * the porting to other languages.
     */
    public static class Stack2<T> extends ArrayList<T> {
        private static final long serialVersionUID = -7451476576174095212L;

		/**
         * Looks at the object at the top of this stack without removing it from the stack.
         * @return the object at the top of this stack
         */
        public T peek() {
            if (size() == 0)
                throw new EmptyStackException();
            return get(size() - 1);
        }

        /**
         * Removes the object at the top of this stack and returns that object as the value of this function.
         * @return the object at the top of this stack
         */
        public T pop() {
            if (size() == 0)
                throw new EmptyStackException();
            T ret = get(size() - 1);
            remove(size() - 1);
            return ret;
        }

        /**
         * Pushes an item onto the top of this stack.
         * @param item the item to be pushed onto this stack
         * @return the <CODE>item</CODE> argument
         */
        public T push(T item) {
            add(item);
            return item;
        }

        /**
         * Tests if this stack is empty.
         * @return <CODE>true</CODE> if and only if this stack contains no items; <CODE>false</CODE> otherwise
         */
        public boolean empty() {
            return size() == 0;
        }
    }

    /**
     * A class for some basic SOM processing.
     */
    public static class Xml2Som {
        /**
         * The order the names appear in the XML, depth first.
         */
        protected ArrayList<String> order;
        /**
         * The mapping of full names to nodes.
         */
        protected HashMap<String, Node> name2Node;
        /**
         * The data to do a search from the bottom hierarchy.
         */
        protected HashMap<String, InverseStore> inverseSearch;
        /**
         * A stack to be used when parsing.
         */
        protected Stack2<String> stack;
        /**
         * A temporary store for the repetition count.
         */
        protected int anform;

        /**
         * Escapes a SOM string fragment replacing "." with "\.".
         * @param s the unescaped string
         * @return the escaped string
         */
        public static String escapeSom(String s) {
        	if (s == null)
        		return "";
            int idx = s.indexOf('.');
            if (idx < 0)
                return s;
            StringBuffer sb = new StringBuffer();
            int last = 0;
            while (idx >= 0) {
                sb.append(s.substring(last, idx));
                sb.append('\\');
                last = idx;
                idx = s.indexOf('.', idx + 1);
            }
            sb.append(s.substring(last));
            return sb.toString();
        }

        /**
         * Unescapes a SOM string fragment replacing "\." with ".".
         * @param s the escaped string
         * @return the unescaped string
         */
        public static String unescapeSom(String s) {
            int idx = s.indexOf('\\');
            if (idx < 0)
                return s;
            StringBuffer sb = new StringBuffer();
            int last = 0;
            while (idx >= 0) {
                sb.append(s.substring(last, idx));
                last = idx + 1;
                idx = s.indexOf('\\', idx + 1);
            }
            sb.append(s.substring(last));
            return sb.toString();
        }

        /**
         * Outputs the stack as the sequence of elements separated
         * by '.'.
         * @return the stack as the sequence of elements separated by '.'
         */
        protected String printStack() {
            if (stack.empty())
                return "";
            StringBuffer s = new StringBuffer();
            for (int k = 0; k < stack.size(); ++k)
                s.append('.').append(stack.get(k));
            return s.substring(1);
        }

        /**
         * Gets the name with the <CODE>#subform</CODE> removed.
         * @param s the long name
         * @return the short name
         */
        public static String getShortName(String s) {
            int idx = s.indexOf(".#subform[");
            if (idx < 0)
                return s;
            int last = 0;
            StringBuffer sb = new StringBuffer();
            while (idx >= 0) {
                sb.append(s.substring(last, idx));
                idx = s.indexOf("]", idx + 10);
                if (idx < 0)
                    return sb.toString();
                last = idx + 1;
                idx = s.indexOf(".#subform[", last);
            }
            sb.append(s.substring(last));
            return sb.toString();
        }

        /**
         * Adds a SOM name to the search node chain.
         * @param unstack the SOM name
         */
        public void inverseSearchAdd(String unstack) {
            inverseSearchAdd(inverseSearch, stack, unstack);
        }

        /**
         * Adds a SOM name to the search node chain.
         * @param inverseSearch the start point
         * @param stack the stack with the separated SOM parts
         * @param unstack the full name
         */
        public static void inverseSearchAdd(HashMap<String, InverseStore> inverseSearch, Stack2<String> stack, String unstack) {
            String last = stack.peek();
            InverseStore store = inverseSearch.get(last);
            if (store == null) {
                store = new InverseStore();
                inverseSearch.put(last, store);
            }
            for (int k = stack.size() - 2; k >= 0; --k) {
                last = stack.get(k);
                InverseStore store2;
                int idx = store.part.indexOf(last);
                if (idx < 0) {
                    store.part.add(last);
                    store2 = new InverseStore();
                    store.follow.add(store2);
                }
                else
                    store2 = (InverseStore)store.follow.get(idx);
                store = store2;
            }
            store.part.add("");
            store.follow.add(unstack);
        }

        /**
         * Searches the SOM hierarchy from the bottom.
         * @param parts the SOM parts
         * @return the full name or <CODE>null</CODE> if not found
         */
        public String inverseSearchGlobal(ArrayList<String> parts) {
            if (parts.isEmpty())
                return null;
            InverseStore store = inverseSearch.get(parts.get(parts.size() - 1));
            if (store == null)
                return null;
            for (int k = parts.size() - 2; k >= 0; --k) {
                String part = parts.get(k);
                int idx = store.part.indexOf(part);
                if (idx < 0) {
                    if (store.isSimilar(part))
                        return null;
                    return store.getDefaultName();
                }
                store = (InverseStore)store.follow.get(idx);
            }
            return store.getDefaultName();
        }

        /**
         * Splits a SOM name in the individual parts.
         * @param name the full SOM name
         * @return the split name
         */
        public static Stack2<String> splitParts(String name) {
            while (name.startsWith("."))
                name = name.substring(1);
            Stack2<String> parts = new Stack2<String>();
            int last = 0;
            int pos = 0;
            String part;
            while (true) {
                pos = last;
                while (true) {
                    pos = name.indexOf('.', pos);
                    if (pos < 0)
                        break;
                    if (name.charAt(pos - 1) == '\\')
                        ++pos;
                    else
                        break;
                }
                if (pos < 0)
                    break;
                part = name.substring(last, pos);
                if (!part.endsWith("]"))
                    part += "[0]";
                parts.add(part);
                last = pos + 1;
            }
            part = name.substring(last);
            if (!part.endsWith("]"))
                part += "[0]";
            parts.add(part);
            return parts;
        }

        /**
         * Gets the order the names appear in the XML, depth first.
         * @return the order the names appear in the XML, depth first
         */
        public ArrayList<String> getOrder() {
            return order;
        }

        /**
         * Sets the order the names appear in the XML, depth first
         * @param order the order the names appear in the XML, depth first
         */
        public void setOrder(ArrayList<String> order) {
            this.order = order;
        }

        /**
         * Gets the mapping of full names to nodes.
         * @return the mapping of full names to nodes
         */
        public HashMap<String, Node> getName2Node() {
            return name2Node;
        }

        /**
         * Sets the mapping of full names to nodes.
         * @param name2Node the mapping of full names to nodes
         */
        public void setName2Node(HashMap<String, Node> name2Node) {
            this.name2Node = name2Node;
        }

        /**
         * Gets the data to do a search from the bottom hierarchy.
         * @return the data to do a search from the bottom hierarchy
         */
        public HashMap<String, InverseStore> getInverseSearch() {
            return inverseSearch;
        }

        /**
         * Sets the data to do a search from the bottom hierarchy.
         * @param inverseSearch the data to do a search from the bottom hierarchy
         */
        public void setInverseSearch(HashMap<String, InverseStore> inverseSearch) {
            this.inverseSearch = inverseSearch;
        }
    }

    /**
     * Processes the datasets section in the XFA form.
     */
    public static class Xml2SomDatasets extends Xml2Som {
        /**
         * Creates a new instance from the datasets node. This expects
         * not the datasets but the data node that comes below.
         * @param n the datasets node
         */
        public Xml2SomDatasets(Node n) {
            order = new ArrayList<String>();
            name2Node = new HashMap<String, Node>();
            stack = new Stack2<String>();
            anform = 0;
            inverseSearch = new HashMap<String, InverseStore>();
            processDatasetsInternal(n);
        }

        /**
         * Inserts a new <CODE>Node</CODE> that will match the short name.
         * @param n the datasets top <CODE>Node</CODE>
         * @param shortName the short name
         * @return the new <CODE>Node</CODE> of the inserted name
         */
        public Node insertNode(Node n, String shortName) {
            Stack2<String> stack = splitParts(shortName);
            org.w3c.dom.Document doc = n.getOwnerDocument();
            Node n2 = null;
            n = n.getFirstChild();
            while (n.getNodeType() != Node.ELEMENT_NODE)
                n = n.getNextSibling();
            for (int k = 0; k < stack.size(); ++k) {
                String part = stack.get(k);
                int idx = part.lastIndexOf('[');
                String name = part.substring(0, idx);
                idx = Integer.parseInt(part.substring(idx + 1, part.length() - 1));
                int found = -1;
                for (n2 = n.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
                    if (n2.getNodeType() == Node.ELEMENT_NODE) {
                        String s = escapeSom(n2.getLocalName());
                        if (s.equals(name)) {
                            ++found;
                            if (found == idx)
                                break;
                        }
                    }
                }
                for (; found < idx; ++found) {
                    n2 = doc.createElementNS(null, name);
                    n2 = n.appendChild(n2);
                    Node attr = doc.createAttributeNS(XFA_DATA_SCHEMA, "dataNode");
                    attr.setNodeValue("dataGroup");
                    n2.getAttributes().setNamedItemNS(attr);
                }
                n = n2;
            }
            inverseSearchAdd(inverseSearch, stack, shortName);
            name2Node.put(shortName, n2);
            order.add(shortName);
            return n2;
        }

        private static boolean hasChildren(Node n) {
            Node dataNodeN = n.getAttributes().getNamedItemNS(XFA_DATA_SCHEMA, "dataNode");
            if (dataNodeN != null) {
                String dataNode = dataNodeN.getNodeValue();
                if ("dataGroup".equals(dataNode))
                    return true;
                else if ("dataValue".equals(dataNode))
                    return false;
            }
            if (!n.hasChildNodes())
                return false;
            Node n2 = n.getFirstChild();
            while (n2 != null) {
                if (n2.getNodeType() == Node.ELEMENT_NODE) {
                    return true;
                }
                n2 = n2.getNextSibling();
            }
            return false;
        }

        private void processDatasetsInternal(Node n) {
            if (n != null) {
                HashMap<String, Integer> ss = new HashMap<String, Integer>();
                Node n2 = n.getFirstChild();
                while (n2 != null) {
                    if (n2.getNodeType() == Node.ELEMENT_NODE) {
                        String s = escapeSom(n2.getLocalName());
                        Integer i = ss.get(s);
                        if (i == null)
                            i = 0;
                        else
                            i = i + 1;
                        ss.put(s, i);
                        stack.push(s + "[" + i.toString() + "]");
                        if (hasChildren(n2)) {
                            processDatasetsInternal(n2);
                        }
                        String unstack = printStack();
                        order.add(unstack);
                        inverseSearchAdd(unstack);
                        name2Node.put(unstack, n2);
                        stack.pop();
                    }
                    n2 = n2.getNextSibling();
                }
            }
        }
    }

    /**
     * A class to process "classic" fields.
     */
    public static class AcroFieldsSearch extends Xml2Som {
        private HashMap<String, String> acroShort2LongName;

        /**
         * Creates a new instance from a Collection with the full names.
         * @param items the Collection
         */
        public AcroFieldsSearch(Collection<String> items) {
            inverseSearch = new HashMap<String, InverseStore>();
            acroShort2LongName = new HashMap<String, String>();
            for (String string : items) {
                String itemName = string;
                String itemShort = getShortName(itemName);
                acroShort2LongName.put(itemShort, itemName);
                inverseSearchAdd(inverseSearch, splitParts(itemShort), itemName);
            }
        }

        /**
         * Gets the mapping from short names to long names. A long
         * name may contain the #subform name part.
         * @return the mapping from short names to long names
         */
        public HashMap<String, String> getAcroShort2LongName() {
            return acroShort2LongName;
        }

        /**
         * Sets the mapping from short names to long names. A long
         * name may contain the #subform name part.
         * @param acroShort2LongName the mapping from short names to long names
         */
        public void setAcroShort2LongName(HashMap<String, String> acroShort2LongName) {
            this.acroShort2LongName = acroShort2LongName;
        }
    }

    /**
     * Processes the template section in the XFA form.
     */
    public static class Xml2SomTemplate extends Xml2Som {
        private boolean dynamicForm;
        private int templateLevel;

        /**
         * Creates a new instance from the datasets node.
         * @param n the template node
         */
        public Xml2SomTemplate(Node n) {
            order = new ArrayList<String>();
            name2Node = new HashMap<String, Node>();
            stack = new Stack2<String>();
            anform = 0;
            templateLevel = 0;
            inverseSearch = new HashMap<String, InverseStore>();
            processTemplate(n, null);
        }

        /**
         * Gets the field type as described in the <CODE>template</CODE> section of the XFA.
         * @param s the exact template name
         * @return the field type or <CODE>null</CODE> if not found
         */
        public String getFieldType(String s) {
            Node n = name2Node.get(s);
            if (n == null)
                return null;
            if ("exclGroup".equals(n.getLocalName()))
                return "exclGroup";
            Node ui = n.getFirstChild();
            while (ui != null) {
                if (ui.getNodeType() == Node.ELEMENT_NODE && "ui".equals(ui.getLocalName())) {
                    break;
                }
                ui = ui.getNextSibling();
            }
            if (ui == null)
                return null;
            Node type = ui.getFirstChild();
            while (type != null) {
                if (type.getNodeType() == Node.ELEMENT_NODE && !("extras".equals(type.getLocalName()) && "picture".equals(type.getLocalName()))) {
                    return type.getLocalName();
                }
                type = type.getNextSibling();
            }
            return null;
        }

        private void processTemplate(Node n, HashMap<String, Integer> ff) {
            if (ff == null)
                ff = new HashMap<String, Integer>();
            HashMap<String, Integer> ss = new HashMap<String, Integer>();
            Node n2 = n.getFirstChild();
            while (n2 != null) {
                if (n2.getNodeType() == Node.ELEMENT_NODE) {
                    String s = n2.getLocalName();
                    if ("subform".equals(s)) {
                        Node name = n2.getAttributes().getNamedItem("name");
                        String nn = "#subform";
                        boolean annon = true;
                        if (name != null) {
                            nn = escapeSom(name.getNodeValue());
                            annon = false;
                        }
                        Integer i;
                        if (annon) {
                            i = Integer.valueOf(anform);
                            ++anform;
                        }
                        else {
                            i = ss.get(nn);
                            if (i == null)
                                i = Integer.valueOf(0);
                            else
                                i = Integer.valueOf(i.intValue() + 1);
                            ss.put(nn, i);
                        }
                        stack.push(nn + "[" + i.toString() + "]");
                        ++templateLevel;
                        if (annon)
                            processTemplate(n2, ff);
                        else
                            processTemplate(n2, null);
                        --templateLevel;
                        stack.pop();
                    }
                    else if ("field".equals(s) || "exclGroup".equals(s)) {
                        Node name = n2.getAttributes().getNamedItem("name");
                        if (name != null) {
                            String nn = escapeSom(name.getNodeValue());
                            Integer i = ff.get(nn);
                            if (i == null)
                                i = Integer.valueOf(0);
                            else
                                i = Integer.valueOf(i.intValue() + 1);
                            ff.put(nn, i);
                            stack.push(nn + "[" + i.toString() + "]");
                            String unstack = printStack();
                            order.add(unstack);
                            inverseSearchAdd(unstack);
                            name2Node.put(unstack, n2);
                            stack.pop();
                        }
                    }
                    else if (!dynamicForm && templateLevel > 0 && "occur".equals(s)) {
                        int initial = 1;
                        int min = 1;
                        int max = 1;
                        Node a = n2.getAttributes().getNamedItem("initial");
                        if (a != null)
                            try{initial = Integer.parseInt(a.getNodeValue().trim());}catch(Exception e){}
                        a = n2.getAttributes().getNamedItem("min");
                        if (a != null)
                            try{min = Integer.parseInt(a.getNodeValue().trim());}catch(Exception e){}
                        a = n2.getAttributes().getNamedItem("max");
                        if (a != null)
                            try{max = Integer.parseInt(a.getNodeValue().trim());}catch(Exception e){}
                        if (initial != min || min != max)
                            dynamicForm = true;
                    }
                }
                n2 = n2.getNextSibling();
            }
        }

        /**
         * <CODE>true</CODE> if it's a dynamic form; <CODE>false</CODE>
         * if it's a static form.
         * @return <CODE>true</CODE> if it's a dynamic form; <CODE>false</CODE>
         * if it's a static form
         */
        public boolean isDynamicForm() {
            return dynamicForm;
        }

        /**
         * Sets the dynamic form flag. It doesn't change the template.
         * @param dynamicForm the dynamic form flag
         */
        public void setDynamicForm(boolean dynamicForm) {
            this.dynamicForm = dynamicForm;
        }
    }

    /**
     * Gets the class that contains the template processing section of the XFA.
     * @return the class that contains the template processing section of the XFA
     */
    public Xml2SomTemplate getTemplateSom() {
        return templateSom;
    }

    /**
     * Sets the class that contains the template processing section of the XFA
     * @param templateSom the class that contains the template processing section of the XFA
     */
    public void setTemplateSom(Xml2SomTemplate templateSom) {
        this.templateSom = templateSom;
    }

    /**
     * Gets the class that contains the datasets processing section of the XFA.
     * @return the class that contains the datasets processing section of the XFA
     */
    public Xml2SomDatasets getDatasetsSom() {
        return datasetsSom;
    }

    /**
     * Sets the class that contains the datasets processing section of the XFA.
     * @param datasetsSom the class that contains the datasets processing section of the XFA
     */
    public void setDatasetsSom(Xml2SomDatasets datasetsSom) {
        this.datasetsSom = datasetsSom;
    }

    /**
     * Gets the class that contains the "classic" fields processing.
     * @return the class that contains the "classic" fields processing
     */
    public AcroFieldsSearch getAcroFieldsSom() {
        return acroFieldsSom;
    }

    /**
     * Sets the class that contains the "classic" fields processing.
     * @param acroFieldsSom the class that contains the "classic" fields processing
     */
    public void setAcroFieldsSom(AcroFieldsSearch acroFieldsSom) {
        this.acroFieldsSom = acroFieldsSom;
    }

    /**
     * Gets the <CODE>Node</CODE> that corresponds to the datasets part.
     * @return the <CODE>Node</CODE> that corresponds to the datasets part
     */
    public Node getDatasetsNode() {
        return datasetsNode;
    }

    public void fillXfaForm(File file) throws IOException {
    	fillXfaForm(file, false);
    }
    public void fillXfaForm(File file, boolean readOnly) throws IOException {
		fillXfaForm(new FileInputStream(file), readOnly);
    }

    public void fillXfaForm(InputStream is) throws IOException {
    	fillXfaForm(is, false);
    }
    public void fillXfaForm(InputStream is, boolean readOnly) throws IOException {
    	fillXfaForm(new InputSource(is), readOnly);
    }

    public void fillXfaForm(InputSource is) throws IOException {
    	fillXfaForm(is, false);
    }
    public void fillXfaForm(InputSource is, boolean readOnly) throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
            db.setEntityResolver(new SafeEmptyEntityResolver());
	    	Document newdoc = db.parse(is);
	    	fillXfaForm(newdoc.getDocumentElement(), readOnly);
		} catch (ParserConfigurationException e) {
			throw new ExceptionConverter(e);
		} catch (SAXException e) {
			throw new ExceptionConverter(e);
		}
    }

    public void fillXfaForm(Node node) {
    	fillXfaForm(node, false);
    }
    /**
     * Replaces the data under datasets/data.
     * @since	iText 5.0.0
     */
    public void fillXfaForm(Node node, boolean readOnly) {
    	if (readOnly) {
        	NodeList nodeList = domDocument.getElementsByTagName("field");
        	for (int i = 0; i < nodeList.getLength(); i++) {
    			((Element)nodeList.item(i)).setAttribute("access", "readOnly");
    		}
    	}
        NodeList allChilds = datasetsNode.getChildNodes();
        int len = allChilds.getLength();
        Node data = null;
        for (int k = 0; k < len; ++k) {
            Node n = allChilds.item(k);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals("data") && XFA_DATA_SCHEMA.equals(n.getNamespaceURI())) {
                data = n;
                break;
            }
        }
        if (data == null) {
            data = datasetsNode.getOwnerDocument().createElementNS(XFA_DATA_SCHEMA, "xfa:data");
            datasetsNode.appendChild(data);
        }
		NodeList list = data.getChildNodes();
		if (list.getLength() == 0) {
			data.appendChild(domDocument.importNode(node, true));
		}
		else {
// There's a possibility that first child node of XFA data is not an ELEMENT but simply a TEXT. In this case data will be duplicated.
//			data.replaceChild(domDocument.importNode(node, true), data.getFirstChild());
            Node firstNode = getFirstElementNode(data);
            if (firstNode != null)
                data.replaceChild(domDocument.importNode(node, true), firstNode);
		}
        extractNodes();
		setChanged(true);
    }

    private Node getFirstElementNode(Node src) {
        Node result = null;
        NodeList list = src.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                result = list.item(i);
                break;
            }
        }
        return result;
    }

    private static class SafeEmptyEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
    }

}
