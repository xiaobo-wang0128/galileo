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
package com.itextpdf.text.pdf.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;

import com.itextpdf.text.log.Level;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

/**
 * Class that allows you to verify a certificate against
 * one or more OCSP responses.
 */
public class OCSPVerifier extends RootStoreVerifier {

	/** The Logger instance */
	protected final static Logger LOGGER = LoggerFactory.getLogger(OCSPVerifier.class);

    protected final static String id_kp_OCSPSigning = "1.3.6.1.5.5.7.3.9";

	/** The list of OCSP responses. */
	protected List<BasicOCSPResp> ocsps;

	/**
	 * Creates an OCSPVerifier instance.
	 * @param verifier	the next verifier in the chain
	 * @param ocsps a list of OCSP responses
	 */
	public OCSPVerifier(CertificateVerifier verifier, List<BasicOCSPResp> ocsps) {
		super(verifier);
		this.ocsps = ocsps;
	}

	/**
	 * Verifies if a a valid OCSP response is found for the certificate.
	 * If this method returns false, it doesn't mean the certificate isn't valid.
	 * It means we couldn't verify it against any OCSP response that was available.
	 * @param signCert	the certificate that needs to be checked
	 * @param issuerCert	its issuer
	 * @return a list of <code>VerificationOK</code> objects.
	 * The list will be empty if the certificate couldn't be verified.
	 * @see RootStoreVerifier#verify(java.security.cert.X509Certificate, java.security.cert.X509Certificate, java.util.Date)
	 */
	public List<VerificationOK> verify(X509Certificate signCert,
			X509Certificate issuerCert, Date signDate)
			throws GeneralSecurityException, IOException {
		List<VerificationOK> result = new ArrayList<VerificationOK>();
		int validOCSPsFound = 0;
		// first check in the list of OCSP responses that was provided
		if (ocsps != null) {
            for (BasicOCSPResp ocspResp : ocsps) {
                if (verify(ocspResp, signCert, issuerCert, signDate))
                    validOCSPsFound++;
            }
        }
		// then check online if allowed
		boolean online = false;
		if (onlineCheckingAllowed && validOCSPsFound == 0) {
			if (verify(getOcspResponse(signCert, issuerCert), signCert, issuerCert, signDate)) {
				validOCSPsFound++;
				online = true;
			}
		}
		// show how many valid OCSP responses were found
		LOGGER.info("Valid OCSPs found: " + validOCSPsFound);
		if (validOCSPsFound > 0)
			result.add(new VerificationOK(signCert, this.getClass(), "Valid OCSPs Found: " + validOCSPsFound + (online ? " (online)" : "")));
		if (verifier != null)
			result.addAll(verifier.verify(signCert, issuerCert, signDate));
		// verify using the previous verifier in the chain (if any)
		return result;
	}


	/**
	 * Verifies a certificate against a single OCSP response
	 * @param ocspResp the OCSP response
	 * @param signCert the certificate that needs to be checked
	 * @param issuerCert the certificate of CA
	 * @param signDate sign date
	 * @return {@code true}, in case successful check, otherwise false.
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public boolean verify(BasicOCSPResp ocspResp, X509Certificate signCert, X509Certificate issuerCert, Date signDate) throws GeneralSecurityException, IOException {
		if (ocspResp == null)
			return false;
		// Getting the responses
		SingleResp[] resp = ocspResp.getResponses();
		for (int i = 0; i < resp.length; i++) {
			// check if the serial number corresponds
			if (!signCert.getSerialNumber().equals(resp[i].getCertID().getSerialNumber())) {
				continue;
			}
			// check if the issuer matches
			try {
				if (issuerCert == null) issuerCert = signCert;
				if (!resp[i].getCertID().matchesIssuer(new X509CertificateHolder(issuerCert.getEncoded()), new BcDigestCalculatorProvider())) {
					LOGGER.info("OCSP: Issuers doesn't match.");
					continue;
				}
			} catch (OCSPException e) {
				continue;
			}
			// check if the OCSP response was valid at the time of signing
			Date nextUpdate = resp[i].getNextUpdate();
			if (nextUpdate == null) {
				nextUpdate = new Date(resp[i].getThisUpdate().getTime() + 180000l);
				if (LOGGER.isLogging(Level.INFO)) {
					LOGGER.info(String.format("No 'next update' for OCSP Response; assuming %s", nextUpdate));
				}
			}
			if (signDate.after(nextUpdate)) {
				if (LOGGER.isLogging(Level.INFO)) {
					LOGGER.info(String.format("OCSP no longer valid: %s after %s", signDate, nextUpdate));
				}
				continue;
			}
			// check the status of the certificate
			Object status = resp[i].getCertStatus();
			if (status == CertificateStatus.GOOD) {
				// check if the OCSP response was genuine
				isValidResponse(ocspResp, issuerCert);
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifies if an OCSP response is genuine
 	 * If it doesn't verify against the issuer certificate and response's certificates, it may verify
     * using a trusted anchor or cert.
     * @param ocspResp the OCSP response
	 * @param issuerCert the issuer certificate
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public void isValidResponse(BasicOCSPResp ocspResp, X509Certificate issuerCert) throws GeneralSecurityException, IOException {
        //OCSP response might be signed by the issuer certificate or
        //the Authorized OCSP responder certificate containing the id-kp-OCSPSigning extended key usage extension
        X509Certificate responderCert = null;

        //first check if the issuer certificate signed the response
        //since it is expected to be the most common case
        if (isSignatureValid(ocspResp, issuerCert)) {
            responderCert = issuerCert;
        }

        //if the issuer certificate didn't sign the ocsp response, look for authorized ocsp responses
        // from properties or from certificate chain received with response
        if (responderCert == null) {
            if (ocspResp.getCerts() != null) {
                //look for existence of Authorized OCSP responder inside the cert chain in ocsp response
                X509CertificateHolder[] certs = ocspResp.getCerts();
                for (X509CertificateHolder cert : certs) {
                    X509Certificate tempCert;
                    try {
                        tempCert = new JcaX509CertificateConverter().getCertificate(cert);
                    } catch (Exception ex) {
                        continue;
                    }
                    List<String> keyPurposes = null;
                    try {
                        keyPurposes = tempCert.getExtendedKeyUsage();
                        if ((keyPurposes != null) && keyPurposes.contains(id_kp_OCSPSigning) && isSignatureValid(ocspResp, tempCert)) {
                            responderCert = tempCert;
                            break;
                        }
                    } catch (CertificateParsingException ignored) {
                    }
                }
                // Certificate signing the ocsp response is not found in ocsp response's certificate chain received
                // and is not signed by the issuer certificate.
                if (responderCert == null) {
                    throw new VerificationException(issuerCert, "OCSP response could not be verified");
                }
            } else {
                //certificate chain is not present in response received
                //try to verify using rootStore
                if (rootStore != null) {
                    try {
                        for (Enumeration<String> aliases = rootStore.aliases(); aliases.hasMoreElements(); ) {
                            String alias = aliases.nextElement();
                            try {
                                if (!rootStore.isCertificateEntry(alias))
                                    continue;
                                X509Certificate anchor = (X509Certificate) rootStore.getCertificate(alias);
                                if (isSignatureValid(ocspResp, anchor)) {
                                    responderCert = anchor;
                                    break;
                                }
                            } catch (GeneralSecurityException ignored) {
                            }
                        }
                    } catch (KeyStoreException e) {
                        responderCert = null;
                    }
                }

                // OCSP Response does not contain certificate chain, and response is not signed by any
                // of the rootStore or the issuer certificate.
                if (responderCert == null) {
                    throw new VerificationException(issuerCert, "OCSP response could not be verified");
                }
            }
        }

        //check "This certificate MUST be issued directly by the CA that issued the certificate in question".
        responderCert.verify(issuerCert.getPublicKey());

        // validating ocsp signers certificate
        // Check if responders certificate has id-pkix-ocsp-nocheck extension,
        // in which case we do not validate (perform revocation check on) ocsp certs for lifetime of certificate
        if (responderCert.getExtensionValue(OCSPObjectIdentifiers.id_pkix_ocsp_nocheck.getId()) == null) {
            CRL crl;
            try {
                crl = CertificateUtil.getCRL(responderCert);
            } catch (Exception ignored) {
                crl = null;
            }
            if (crl != null && crl instanceof X509CRL) {
                CRLVerifier crlVerifier = new CRLVerifier(null, null);
                crlVerifier.setRootStore(rootStore);
                crlVerifier.setOnlineCheckingAllowed(onlineCheckingAllowed);
                crlVerifier.verify((X509CRL)crl, responderCert, issuerCert, new Date());
                return;
            }
        }

        //check if lifetime of certificate is ok
        responderCert.checkValidity();
	}

	/**
	 * Verifies if the response is valid.
	 * If it doesn't verify against the issuer certificate and response's certificates, it may verify
	 * using a trusted anchor or cert.
     * NOTE. Use {@code isValidResponse()} instead.
	 * @param ocspResp	the response object
	 * @param issuerCert the issuer certificate
	 * @return	true if the response can be trusted
	 */
    @Deprecated
	public boolean verifyResponse(BasicOCSPResp ocspResp, X509Certificate issuerCert) {
        try {
            isValidResponse(ocspResp, issuerCert);
            return true;
        } catch (Exception e) {
            return false;
        }
	}

	/**
	 * Checks if an OCSP response is genuine
	 * @param ocspResp	the OCSP response
	 * @param responderCert	the responder certificate
	 * @return	true if the OCSP response verifies against the responder certificate
	 */
	public boolean isSignatureValid(BasicOCSPResp ocspResp, Certificate responderCert) {
		try {
			ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder()
					.setProvider("BC").build(responderCert.getPublicKey());
			return ocspResp.isSignatureValid(verifierProvider);
		} catch (OperatorCreationException e) {
			return false;
		} catch (OCSPException e) {
			return false;
		}
	}

	/**
	 * Gets an OCSP response online and returns it if the status is GOOD
	 * (without further checking).
	 * @param signCert	the signing certificate
	 * @param issuerCert	the issuer certificate
	 * @return an OCSP response
	 */
	public BasicOCSPResp getOcspResponse(X509Certificate signCert, X509Certificate issuerCert) {
		if (signCert == null && issuerCert == null) {
			return null;
		}
		OcspClientBouncyCastle ocsp = new OcspClientBouncyCastle();
		BasicOCSPResp ocspResp = ocsp.getBasicOCSPResp(signCert, issuerCert, null);
		if (ocspResp == null) {
			return null;
		}
		SingleResp[] resp = ocspResp.getResponses();
		for (int i = 0; i < resp.length; i++) {
			Object status = resp[i].getCertStatus();
			if (status == CertificateStatus.GOOD) {
				return ocspResp;
			}
		}
		return null;
	}
}
