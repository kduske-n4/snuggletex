/* $Id:Globals.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.LR;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.MATH;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.PARAGRAPH;

import uk.ac.ed.ph.snuggletex.SerializationSpecifier;
import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathCharacterInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLSymbol;
import uk.ac.ed.ph.snuggletex.semantics.MathNegatableInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation.BracketType;
import uk.ac.ed.ph.snuggletex.semantics.MathCharacterInterpretation.CharacterType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

/**
 * Lists various useful (but internal) constants and helper methods.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class Globals {
    
    /**
     * @deprecated Use {@link XMLConstants#XML_NS_URI} instead
     */
    @Deprecated
    public static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    
    /**
     * @deprecated Use {@link XMLConstants#XMLNS_ATTRIBUTE_NS_URI} instead
     */
    @Deprecated
    public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";
    
    /**
     * @deprecated Use {@link W3CConstants#XHTML_NAMESPACE} instead
     */
    @Deprecated
    public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
    
    /**
     * @deprecated Use {@link W3CConstants#MATHML_NAMESPACE} instead
     */
    @Deprecated
    public static final String MATHML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";
    
    /**
     * @deprecated Use {@link W3CConstants#MATHML_PREF_NAMESPACE} instead
     */
    @Deprecated
    public static final String MATHML_PREF_NAMESPACE = "http://www.w3.org/2002/Math/preference";
    
    public static final String GENERAL_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/general-messages";
    public static final String CSS_PROPERTIES_NAME = "uk/ac/ed/ph/snuggletex/css.properties";
    
    public static final String MATH_CHARACTER_DEFS_RESOURCE_NAME = "uk/ac/ed/ph/snuggletex/math-characters.txt";
    
    /** Specifies the ClassPath location of the Stylesheet used by {@link XMLUtilities#serializeNodeChildren(uk.ac.ed.ph.snuggletex.utilities.StylesheetManager, org.w3c.dom.Node, SerializationSpecifier)} */
    public static final String EXTRACT_CHILD_NODES_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/extract-child-nodes.xsl";
    
    /** Specifies the location of the XSLT that converts MathML symbol characters to named entities */
    public static final String MATHML_ENTITIES_MAP_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/mathml-entities-map.xsl";
    
    /** Specifies the location of the XSLT that serializes and converts MathML symbol characters to named entities */
    public static final String SERIALIZE_WITH_NAMED_ENTITIES_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/serialize-with-named-entities.xsl";
    
    /** Specifies the ClassPath location of the XHTML -> HTML used for serializing as legacy HTML */
    public static final String XHTML_TO_HTML_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/xhtml-to-html.xsl";
    
    /** Specifies the ClassPath location of the MathML -> XHTML stylesheet to use for down-transforming */
    public static final String MATHML_TO_XHTML_XSL_RESOURCE_NAME = "classpath:/uk/ac/ed/ph/snuggletex/mathml-to-xhtml.xsl";
    
    /** URN used in MathML -> XHTML to call up XML containing current CSS Properties */
    public static final String CSS_PROPERTIES_DOCUMENT_URN = "urn:snuggletex-css-properties";

    /** Placeholder operator representing ^. This is replaced by an appropriate Command during token fixing */
    public static final String SUP_PLACEHOLDER = "^";
    
    /** Placeholder operator representing ^. This is replaced by an appropriate Command during token fixing */
    public static final String SUB_PLACEHOLDER = "_";
    
    public static final EnumSet<LaTeXMode> MATH_MODE_ONLY = EnumSet.of(MATH);
    public static final EnumSet<LaTeXMode> PARA_MODE_ONLY = EnumSet.of(PARAGRAPH);
    public static final EnumSet<LaTeXMode> TEXT_MODE_ONLY = EnumSet.of(PARAGRAPH, LR);
    
    /**
     * This represents all modes that LaTeX commands can be used in, so is currently a bit
     * of a misnomer as it excludes {@link LaTeXMode#VERBATIM}!
     * 
     * TODO: Think of a more sensible name for this!!
     */
    public static final EnumSet<LaTeXMode> ALL_MODES = EnumSet.of(PARAGRAPH, MATH, LR);
    
    //----------------------------------------------------------------------------
    // FIXME: This stuff must move elsewhere and become more extensive and extensible
    // FIXME: This is nasty as we're merging the nice "simple" character definitions in with
    // any more complex defs. Most chars don't have any complex defs so it's perhaps overly
    // complicated. And uses up too much memory 'cos of the number of EnumMaps with only 1 entry
    // in then. Refactor this signficantly!!!
    
    private static final Map<Integer, MathCharacterInterpretation> mathCharacterCoreInterpretationMap;
    private static final Map<Integer, EnumMap<InterpretationType, Interpretation>> mathCharacterInterpretationMap;
    
    /** 
     * Literal Math characters, mapped to any interpretation(s) over and above the standard
     * {@link MathCharacterInterpretation}s defined for them.
     */
    private static final Object[] mathCharacterAdditionalInterpretationData = new Object[] {
       '_', new MathOperatorInterpretation(Globals.SUB_PLACEHOLDER),
       '^', new MathOperatorInterpretation(Globals.SUP_PLACEHOLDER),
//       '+', new MathCharacterInterpretation(CharacterType.BIN, MathMLSymbol.ADD),
//       '-', new MathOperatorInterpretation(MathMLSymbol.SUBTRACT),
//       '=', new MathOperatorInterpretation(MathMLSymbol.EQUALS), new MathNegatableInterpretation(MathMLSymbol.NOT_EQUALS),
       '=', new MathNegatableInterpretation(MathMLSymbol.NOT_EQUALS),
//       ',', new MathOperatorInterpretation(MathMLSymbol.COMMA),
//       '/', new MathOperatorInterpretation(MathMLSymbol.SLASH),
//       '*', new MathOperatorInterpretation(MathMLSymbol.ASTERISK),
//       '!', new MathOperatorInterpretation(MathMLSymbol.FACTORIAL),
//       '.', new MathOperatorInterpretation(MathMLSymbol.DOT),
       '(', new MathBracketInterpretation(MathMLSymbol.OPEN_BRACKET, BracketType.OPENER, true),
       ')', new MathBracketInterpretation(MathMLSymbol.CLOSE_BRACKET, BracketType.CLOSER, true),
       '[', new MathBracketInterpretation(MathMLSymbol.OPEN_SQUARE_BRACKET, BracketType.OPENER, true),
       ']', new MathBracketInterpretation(MathMLSymbol.CLOSE_SQUARE_BRACKET, BracketType.CLOSER, true),
       '<', new MathNegatableInterpretation(MathMLSymbol.NOT_LESS_THAN), new MathBracketInterpretation(MathMLSymbol.OPEN_ANGLE_BRACKET, BracketType.OPENER, false),
       '>', new MathNegatableInterpretation(MathMLSymbol.NOT_GREATER_THAN), new MathBracketInterpretation(MathMLSymbol.CLOSE_ANGLE_BRACKET, BracketType.OPENER, false),
       '|', new MathNegatableInterpretation(MathMLSymbol.NOT_MID), new MathBracketInterpretation(MathMLSymbol.VERT_BRACKET, BracketType.OPENER_OR_CLOSER, false)
    };
    
    static {
        /* Read in "core" definitions */
        mathCharacterCoreInterpretationMap = new HashMap<Integer, MathCharacterInterpretation>();
        InputStream mathCharacterDefsStream = Globals.class.getClassLoader().getResourceAsStream(MATH_CHARACTER_DEFS_RESOURCE_NAME);
        if (mathCharacterDefsStream==null) {
            throw new SnuggleRuntimeException("Could not load resource " + MATH_CHARACTER_DEFS_RESOURCE_NAME);
        }
        try {
            BufferedReader mathCharacterDefsReader = new BufferedReader(new InputStreamReader(mathCharacterDefsStream, "US-ASCII"));
            String line;
            while ((line = mathCharacterDefsReader.readLine())!=null) {
                String[] fields = line.split(":"); /* codePoint:commandName:type */
                int codePoint = Integer.parseInt(fields[0], 16);
                CharacterType characterType = CharacterType.valueOf(fields[2]);
                mathCharacterCoreInterpretationMap.put(codePoint, new MathCharacterInterpretation(characterType, codePoint));
            }
            mathCharacterDefsReader.close();
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Got Exception while reading and parsing Math character definitions", e);
        }
        
        /* Some important chars aren't defined in the core defs for some reason */
        mathCharacterCoreInterpretationMap.put(Integer.valueOf('-'), new MathCharacterInterpretation(CharacterType.OP, '-'));
        mathCharacterCoreInterpretationMap.put(Integer.valueOf('*'), new MathCharacterInterpretation(CharacterType.OP, '*'));
        
        /* Then do any additional definitions. Not that some don't override core defs */
        mathCharacterInterpretationMap = new HashMap<Integer, EnumMap<InterpretationType, Interpretation>>();
        Object object;
        Integer currentCodePoint = null;
        EnumMap<InterpretationType, Interpretation> currentMapBuilder = null;
        for (int i=0; i<mathCharacterAdditionalInterpretationData.length; i++) {
            object = mathCharacterAdditionalInterpretationData[i];
            if (object instanceof Character) {
                if (currentCodePoint!=null) {
                    /* Save map previously accumulated */
                    mathCharacterInterpretationMap.put(currentCodePoint, currentMapBuilder);
                }
                /* Create new map */
                currentCodePoint = Integer.valueOf((Character) object);
                currentMapBuilder = new EnumMap<InterpretationType, Interpretation>(InterpretationType.class);
                
                /* Merge in core character interp into map, if available */
                MathCharacterInterpretation mathCharacterInterpretation = mathCharacterCoreInterpretationMap.get(currentCodePoint);
                if (mathCharacterInterpretation!=null) {
                    currentMapBuilder.put(InterpretationType.MATH_CHARACTER, mathCharacterInterpretation);
                }
            }
            else if (object instanceof MathInterpretation) {
                if (currentMapBuilder==null) {
                    throw new SnuggleLogicException("Expected a Character in data before this item " + object);
                }
                MathInterpretation currentInterpretation = (MathInterpretation) object;
                currentMapBuilder.put(currentInterpretation.getType(), currentInterpretation);
            }
            else {
                throw new SnuggleLogicException("Unexpected logic branch: got " + object);
            }
        }
        if (currentCodePoint!=null) {
            mathCharacterInterpretationMap.put(currentCodePoint, currentMapBuilder);
        }
    }
    
    public static EnumMap<InterpretationType, Interpretation> getMathCharacterInterpretations(int codePoint) {
        EnumMap<InterpretationType, Interpretation> result = mathCharacterInterpretationMap.get(Integer.valueOf(codePoint));
        if (result==null) {
            /* Try for just core interpretation */
            MathCharacterInterpretation mathCharacterInterpretation = mathCharacterCoreInterpretationMap.get(codePoint);
            if (mathCharacterInterpretation!=null) {
                result = new EnumMap<InterpretationType, Interpretation>(InterpretationType.class);
                result.put(InterpretationType.MATH_CHARACTER, mathCharacterInterpretation);
            }
        }
        return result;
    }
}
