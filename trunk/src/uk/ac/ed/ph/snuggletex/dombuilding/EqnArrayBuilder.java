/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleTeX;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder.OutputContext;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Handles the <tt>eqnarray*</tt> environment.
 * 
 * @see MathEnvironmentBuilder
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class EqnArrayBuilder implements EnvironmentHandler {

    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException, SnuggleParseException {
        /* Compute the geometry of the table and make sure its content model is OK */
        int[] geometry = TabularBuilder.computeTableDimensions(token.getContent());
        int numColumns = geometry[1];
        
        /* This is the element we'll append the <mtable/> to. It will be either <math/>
         * (if no annotations) or <semantics/> (if annotations) */
        Element mtableParent; 

        /* Build MathML container and structure */
        builder.setOutputContext(OutputContext.MATHML_BLOCK);
        Element mathElement = builder.appendMathMLElement(parentElement, "math");
        mathElement.setAttribute("display", "block");
        if (builder.getOptions().isAddingMathAnnotations()) {
            /* This is similar to what we do in MathEnvironmentBuilder. Things are a bit
             * simpler here, though, as we are only going to generate a single <mtable/>
             * element so there's no need to consider multiple child elements.
             */
            Element semantics = builder.appendMathMLElement(mathElement, "semantics");
            mtableParent = semantics;
        }
        else {
            mtableParent = mathElement;
        }
        
        /* Build <mtable/> */
        Element mtableElement = builder.appendMathMLElement(mtableParent, "mtable");
        Element mtrElement, mtdElement;
        for (FlowToken rowToken : token.getContent()) {
            mtrElement = builder.appendMathMLElement(mtableElement, "mtr");
            List<FlowToken> columns = ((CommandToken) rowToken).getArguments()[0].getContents();
            for (FlowToken columnToken : columns) {
                mtdElement = builder.appendMathMLElement(mtrElement, "mtd");
                builder.handleTokens(mtdElement, ((CommandToken) columnToken).getArguments()[0].getContents(), true);
            }
            /* Add empty <td/> for missing columns */
            for (int i=0; i<numColumns-columns.size(); i++) {
                builder.appendMathMLElement(mtrElement, "mtd");
            }
        }
        
        /* Maybe create MathML annotation */
        if (builder.getOptions().isAddingMathAnnotations()) {
            Element annotation = builder.appendMathMLTextElement(mtableParent, "annotation",
                    token.getSlice().extract().toString(), true);
            annotation.setAttribute("encoding", SnuggleTeX.SNUGGLETEX_MATHML_ANNOTATION_ENCODING);
        }
        
        /* Reset output context back to XHTML */
        builder.setOutputContext(OutputContext.XHTML);
    }
}