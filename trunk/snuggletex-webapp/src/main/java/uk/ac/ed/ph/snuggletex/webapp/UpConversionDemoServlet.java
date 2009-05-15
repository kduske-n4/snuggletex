/* $Id:TryOutServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Servlet demonstrating the up-conversion process on user-entered MATH mode
 * inputs.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class UpConversionDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4376587500238353176L;
    
    /** Logger so that we can log what users are trying out to allow us to improve things */
    private Logger log = Logger.getLogger(UpConversionDemoServlet.class.getName());
    
    /** Default input to use when first showing the page */
    private static final String DEFAULT_INPUT = "\\frac{2x-y^2}{\\sin xy(x-2)}";
    
    /** Location of XSLT controlling page layout */
    private static final String DISPLAY_XSLT_LOCATION = "classpath:/upconversion-demo.xsl";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        doRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response);
    }
    
    private void doRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* Read in input LaTeX, using some placeholder text if nothing was provided */
        String rawInputLaTeX = request.getParameter("input");
        String resultingInputLaTeX;
        if (rawInputLaTeX!=null) {
            /* Normalise any input space */
            resultingInputLaTeX = rawInputLaTeX.replaceAll("\\s+", " ");
        }
        else {
            resultingInputLaTeX = DEFAULT_INPUT;
        }
        
        /* Parse the LaTeX */
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        SnuggleInput input = new SnuggleInput("\\[ " + resultingInputLaTeX + " \\]", "Form Input");
        session.parseInput(input);
        
        /* Set up web output options */
        MathMLWebPageOptions options = new MathMLWebPageOptions();
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setPageType(WebPageType.CROSS_BROWSER_XHTML);
        options.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        options.setTitle("LaTeX to MathML and Maxima");
        options.setAddingTitleHeading(false); /* We'll put our own title in */
        options.setIndenting(true);
        options.setCSSStylesheetURLs(
                request.getContextPath() + "/includes/physics.css"
        );
        
        /* Do up-conversion. We will leave error extraction to the XSLT, as all (parsing) errors
         * will have been nicely formatted as XHTML by then. */
        UpConvertingPostProcessor upConvertingPostProcessor = new UpConvertingPostProcessor();
        options.setDOMPostProcessor(upConvertingPostProcessor);
        NodeList result = session.buildDOMSubtree(options);
        boolean isParsingSuccess = result.getLength()==1
            && result.item(0).getNodeType()==Node.ELEMENT_NODE
            && session.getErrors().isEmpty();
        
        /* Create XSLT to generate the resulting page */
        Transformer viewStylesheet = getStylesheet(DISPLAY_XSLT_LOCATION);
        viewStylesheet.setParameter("context-path", request.getContextPath());
        viewStylesheet.setParameter("latex-input", resultingInputLaTeX);
        viewStylesheet.setParameter("is-success", Boolean.valueOf(isParsingSuccess));
        if (isParsingSuccess) {
            /* Create various versions of the resulting MathML */
            Element mathElement = (Element) result.item(0);
            viewStylesheet.setParameter("mathml-annotated", MathMLUtilities.serializeElement(mathElement));
            viewStylesheet.setParameter("cmathml", MathMLUtilities.serializeDocument(MathMLUtilities.isolateAnnotationXML(mathElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME)));
            viewStylesheet.setParameter("maxima", MathMLUtilities.extractAnnotationString(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME));
        }
        options.setStylesheet(viewStylesheet);
        
        /* Generate and serve the resulting web page */
        try {
            session.writeWebPage(options, response, response.getOutputStream());
        }
        catch (Exception e) {
            throw new ServletException("Unexpected Exception", e);
        }
    }
}
