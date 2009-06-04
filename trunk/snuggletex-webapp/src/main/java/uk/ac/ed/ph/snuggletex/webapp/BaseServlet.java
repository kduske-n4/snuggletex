/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.utilities.ClassPathURIResolver;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Trivial base class for servlets in the demo webapp
 *
 * @author  David McKain
 * @version $Revision$
 */
abstract class BaseServlet extends HttpServlet {
    
    private static final long serialVersionUID = -2577813908466694931L;

    /**
     * Helper that reads in a resource from the webapp hierarchy, throwing a {@link ServletException}
     * if the resource could not be found.
     * 
     * @param resourcePathInsideWebpp path of Resource to load, relative to base of webapp.
     * @return resulting {@link InputStream}, which will not be null
     * @throws ServletException
     */
    protected InputStream ensureReadResource(String resourcePathInsideWebpp) throws ServletException {
        InputStream result = getServletContext().getResourceAsStream(resourcePathInsideWebpp);
        if (result==null) {
            throw new ServletException("Could not read in required web resource at " + resourcePathInsideWebpp);
        }
        return result;
    }
    
    protected ClassPathURIResolver getURIResolver() {
        return ClassPathURIResolver.getInstance();
    }
    
    protected StylesheetManager getStylesheetManager() {
        return (StylesheetManager) getServletContext().getAttribute(ContextInitialiser.STYLESHEET_MANAGER_ATTRIBUTE_NAME);
    }
    
    protected StylesheetCache getStylesheetCache() {
        return getStylesheetManager().getStylesheetCache();
    }
    
    @SuppressWarnings("unchecked")
    protected TransformerFactory getTransformerFactory() {
        return ((ThreadLocal<TransformerFactory>) getServletContext().getAttribute(ContextInitialiser.TRANSFORMER_FACTORY_THREAD_LOCAL)).get();
    }
    
    /**
     * Compiles the XSLT stylesheet at the given location within the webapp,
     * using {@link ClassPathURIResolver} to locate the stylesheet and anything it wants to import.
     * 
     * @param classPathUri location of XSLT to compile.
     * 
     * @return resulting {@link Templates} representing the compiled stylesheet.
     * @throws ServletException if XSLT could not be found or could not be compiled.
     */
    protected Transformer getStylesheet(String classPathUri) throws ServletException {
        try {
            return getStylesheetManager().getStylesheet(classPathUri).newTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new ServletException("Could not create Transformer from Templates", e);
        }
    }

    protected Transformer createSerializer() throws TransformerConfigurationException {
        Transformer serializer = getTransformerFactory().newTransformer();
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII");
        return serializer;
    }
    
    /**
     * Convenience method which picks the most appropriate MathML-based {@link WebPageType}
     * for the current UserAgent, returning null if the UserAgent does not appear to support
     * MathML.
     * 
     * @param request
     */
    protected WebPageType chooseBestWebPageType(final HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        WebPageType result = null;
        if (userAgent!=null) {
            if (userAgent.contains("MathPlayer ")) {
                result  = WebPageType.MATHPLAYER_HTML;
            }
            else if (userAgent.contains("Gecko/")) {
                result  = WebPageType.MOZILLA;
            }
        }
        return result ;
    }
}