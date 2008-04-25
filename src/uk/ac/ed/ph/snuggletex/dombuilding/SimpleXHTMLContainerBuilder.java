/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * This trivial handler takes an Environment or 1-argument Command and simply maps it to
 * an XHTML element with the given name and CSS class. It then descends into the content as
 * normal.
 *
 * @author  David McKain
 * @version $Revision$
 */
public class SimpleXHTMLContainerBuilder implements CommandHandler, EnvironmentHandler {
    
    private final String xhtmlElementName;
    private final String cssClassName;
    
    public SimpleXHTMLContainerBuilder(final String xhtmlElementName) {
        this(xhtmlElementName, null);
    }
    
    public SimpleXHTMLContainerBuilder(final String xhtmlElementName, final String cssClassName) {
        this.xhtmlElementName = xhtmlElementName;
        this.cssClassName = cssClassName;
    }

    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws DOMException, SnuggleParseException {
        handleContent(builder, parentElement, token.getArguments()[0]);
    }
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException, SnuggleParseException {
        handleContent(builder, parentElement, token.getContent());
    }
    
    protected void handleContent(DOMBuilder builder, Element parentElement, ArgumentContainerToken contentToken)
            throws DOMException, SnuggleParseException {
        Element containerElement = builder.appendXHTMLElement(parentElement, xhtmlElementName);
        if (cssClassName!=null) {
            builder.applyCSSStyle(containerElement, cssClassName);
        }
        /* Descend into content */
        builder.handleTokens(containerElement, contentToken.getContents(), true);
    }
}
