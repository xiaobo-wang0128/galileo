/*
 *
 * This file is part of the iText (R) project.
    Copyright (c) 1998-2017 iText Group NV
 * Authors: Balder Van Camp, Emiel Ackermann, et al.
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

package com.itextpdf.tool.xml.html.head;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.Element;
import com.itextpdf.text.log.Level;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.tool.xml.Experimental;
import com.itextpdf.tool.xml.NoCustomContextException;
import com.itextpdf.tool.xml.Tag;
import com.itextpdf.tool.xml.WorkerContext;
import com.itextpdf.tool.xml.exceptions.LocaleMessages;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import com.itextpdf.tool.xml.html.AbstractTagProcessor;

/**
 * @author redlab_b
 *
 */
@Experimental
public class XML extends AbstractTagProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger("applicationLogger");
	/*
	 * (non-Javadoc)
	 *
	 * @see com.itextpdf.tool.xml.TagProcessor#startElement(com.itextpdf.tool.xml.Tag)
	 */
	@Override
	public List<Element> start(final WorkerContext ctx, final Tag tag) {
		String enc = tag.getAttributes().get("encoding");
		if (null != enc) {
				try {
					if (Charset.isSupported(enc)) {
					getHtmlPipelineContext(ctx).charSet(Charset.forName(enc));
					if (LOGGER.isLogging(Level.DEBUG)) {
						LOGGER.debug(
								String.format(LocaleMessages.getInstance().getMessage(LocaleMessages.META_CC), enc));
					}
					} else {
						if (LOGGER.isLogging(Level.DEBUG)) {
							LOGGER.debug(
									String.format(LocaleMessages.getInstance().getMessage(LocaleMessages.META_404), getHtmlPipelineContext(ctx)
											.charSet()));
						}
					}
				} catch (NoCustomContextException e) {
					throw new RuntimeWorkerException(LocaleMessages.getInstance().getMessage(LocaleMessages.NO_CUSTOM_CONTEXT));
				}

		}
		return new ArrayList<Element>(0);
	}

}
