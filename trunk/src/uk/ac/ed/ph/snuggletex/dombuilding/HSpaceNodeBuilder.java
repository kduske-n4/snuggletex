/* $Id: SpaceNodeBuilder.java 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Handles the <tt>hspace</tt> and <tt>hspace*</tt> commands.
 * <p>
 * Note that we are going to handle <tt>hspace*</tt> in exactly the same way
 * as <tt>hspace</tt> since it is not easy to detect when we are at the end of
 * a line.
 * 
 * @author David McKain
 * @version $Revision: 3 $
 */
public final class HSpaceNodeBuilder implements CommandHandler {

    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws DOMException, SnuggleParseException {
        /*
         * First of all, decode the given length and convert to an appropriate
         * unit for CSS/MathML.
         */
        String resultSize = convertLaTeXSize(builder, parentElement, token.getArguments()[0]);
        if (resultSize==null) {
            /* Couldn't grok the size so do nothing */
            return;
        }
        if (token.getLatexMode() == LaTeXMode.MATH) {
            /* Create <mspace/> */
            Element mspace = builder.appendMathMLElement(parentElement, "mspace");
            mspace.setAttribute("width", resultSize);
        }
        else {
            /* Text mode, we'll cheat with a <span/> and a bit of CSS */
            Element span = builder.appendXHTMLElement(parentElement, "span");
            span.setAttribute("style", "margin-left:" + resultSize + ";font-size:0");
            span.appendChild(builder.getDocument().createTextNode("\u00a0"));
        }
    }

    public String convertLaTeXSize(final DOMBuilder builder, final Element parentElement, 
            final Token sizeToken) throws SnuggleParseException {
        CharSequence latexSize = sizeToken.getSlice().extract();
        Pattern sizePattern = Pattern.compile("\\s*(-?[\\d\\.]+)\\s*(\\w+)\\s*");
        Matcher sizeMatcher = sizePattern.matcher(latexSize);
        if (!sizeMatcher.matches()) {
            /* Error: bad sizing */
            builder.appendOrThrowError(parentElement, sizeToken, ErrorCode.TDEUN0,
                    latexSize);
            return null;
        }
        String incomingSizeString = sizeMatcher.group(1);
        String incomingUnits = sizeMatcher.group(2);
        
        /* Make sure we got a good number */
        float incomingSize;
        try {
            incomingSize = Float.parseFloat(incomingSizeString);
        }
        catch (NumberFormatException e) {
            /* Error: could not parse number */
            builder.appendOrThrowError(parentElement, sizeToken, ErrorCode.TDEUN1,
                    incomingSizeString, latexSize);
            return null;
        }
        /* Handle units */
        String resultUnits;
        float resultSize;
        if (incomingUnits.equals("pt") || incomingUnits.equals("in")
                || incomingUnits.equals("cm") || incomingUnits.equals("mm")
                || incomingUnits.equals("em") || incomingUnits.equals("ex")) {
            resultUnits = incomingUnits;
            resultSize = incomingSize;
        }
        else if (incomingUnits.equals("pc")) {
            /* 1pc = 12pt */
            resultUnits = "pt";
            resultSize = incomingSize * 12.0f;
        }
        else if (incomingUnits.equals("bp")) {
            /* 1in = 72bp */
            resultUnits = "in";
            resultSize = incomingSize / 72.0f;
        }
        else if (incomingUnits.equals("dd")) {
            /* 1157dd = 1238pt (yes, really!) */
            resultUnits = "pt";
            resultSize = incomingSize * 1238.0f / 1157.0f;
        }
        else if (incomingUnits.equals("sp")) {
            /* 1pt = 65536sp */
            resultUnits = "pt";
            resultSize = incomingSize / 65536.0f;
        }
        else {
            /* Error: bad units! */
            builder.appendOrThrowError(parentElement, sizeToken, ErrorCode.TDEUN2,
                    incomingUnits, latexSize);
            return null;
        }
        /* Convert back into a size String compatible with both CSS and MathML */
        return Float.valueOf(resultSize) + resultUnits;
    }
}
