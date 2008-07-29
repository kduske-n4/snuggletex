/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.samples;

import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleTeXEngine;
import uk.ac.ed.ph.snuggletex.SnuggleTeXSession;

import java.io.IOException;

/**
 * Example demonstrating a minimal example use of SnuggleTeX.
 * <p>
 * This simply converts a fixed input String of LaTeX to XML. 
 * (In this case, the result is a fragment of MathML.)
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class MinimalExample {
    
    public static void main(String[] args) throws IOException {
        SnuggleTeXEngine engine = new SnuggleTeXEngine();
        SnuggleTeXSession session = engine.createSession();
        
        SnuggleInput input = new SnuggleInput("$$1+2=3$$");
        session.parseInput(input);
        String xmlString = session.buildXMLString();
        
        System.out.println("Input " + input.getString()
                + " was converted to:\n" + xmlString);
    }
}