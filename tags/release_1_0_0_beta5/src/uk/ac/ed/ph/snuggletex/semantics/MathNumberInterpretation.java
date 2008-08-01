/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.aardvark.commons.util.ObjectUtilities;

/**
 * Represents the interpretation of a mathematical number.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathNumberInterpretation implements MathInterpretation {
    
    private final CharSequence number;
    
    public MathNumberInterpretation(final CharSequence number) {
        this.number = number;
    }
    
    public CharSequence getNumber() {
        return number;
    }
    
    public InterpretationType getType() {
        return InterpretationType.MATH_NUMBER;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
