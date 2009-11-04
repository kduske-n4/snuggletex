<?xml version="1.0"?>
<!--

$Id$

Overrides format-output.xsl to add in functionality for
doing a simple Math Input -> MathML web page result.

Copyright (c) 2009 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:mu="ext://uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs m s mu">

  <!-- Import basic formatting stylesheet -->
  <xsl:import href="format-output.xsl"/>
  <xsl:import href="demo-utilities.xsl"/>

  <xsl:param name="latex-input" as="xs:string" required="yes"/>
  <xsl:param name="is-bad-input" as="xs:boolean" required="yes"/>
  <xsl:param name="parsing-errors" as="element(s:error)*"/>
  <xsl:param name="result-mathml" as="xs:string?"/>

  <!-- Override page ID -->
  <xsl:variable name="pageId" select="'mathInputDemo'" as="xs:string"/>

  <xsl:template match="body" mode="make-content">
    <!-- Now do input form -->
    <h3>Input</h3>
    <p>
      This demo lets you enter some math mode LaTeX for SnuggleTeX to convert into
      MathML and display back to you
      (either as MathML or images if your browser doesn't support
      MathML).
    </p>
    <p>
      Simply enter a LaTeX math mode expression into the box below and hit
      <tt>Go!</tt> to see the resulting output and MathML.
    </p>
    <form method="post" class="input">
      <div>
        LaTeX Math Mode Input: <input id="input" name="input" type="text" value="{$latex-input}"/>
        <input type="submit" value="Go!" />
        <input type="button" value="Clear" onclick="document.getElementById('input').value=''" />
      </div>
    </form>
    <xsl:choose>
      <xsl:when test="$is-bad-input">
        <!-- Bad input -->
        <xsl:apply-templates select="." mode="handle-bad-input"/>
      </xsl:when>
      <xsl:when test="exists($parsing-errors)">
        <!-- SnuggleTeX Parsing Error(s) -->
        <xsl:apply-templates select="." mode="handle-failed-input"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Successful Parsing -->
        <xsl:apply-templates select="." mode="handle-successful-input"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  This template shows the details when the input has been
  successfully passed through SnuggleTeX, though with the
  possibility that up-conversion has not been entirely successful.
  -->
  <xsl:template match="body" mode="handle-successful-input">
    <h3>Output (as rendered by your browser)</h3>
    <xsl:call-template name="maybe-make-mathml-legacy-output-warning"/>
    <div class="result">
      <xsl:copy-of select="node()"/>
    </div>

    <h3>Resulting MathML</h3>
    <pre class="result">
      <xsl:value-of select="$result-mathml"/>
    </pre>
  </xsl:template>

  <!-- Show SnuggleTeX failure details. -->
  <xsl:template match="body" mode="handle-failed-input">
    <h3>Result: LaTeX Errors in Input</h3>
    <p>
      The input you entered contained LaTeX errors as follows:
    </p>
    <xsl:call-template name="format-parsing-errors">
      <xsl:with-param name="parsing-errors" select="$parsing-errors"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Show bad input details -->
  <xsl:template match="body" mode="handle-bad-input">
    <h3>Result: Bad Input</h3>
    <p>
      Sorry, your input was not successfully parsed as Math Mode input.
    </p>
  </xsl:template>

</xsl:stylesheet>