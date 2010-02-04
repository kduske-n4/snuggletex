<?xml version="1.0"?>
<!--

$Id$

Overrides format-output.xsl to add in functionality for
demonstrating ASCIIMathML -> Presentation MathML -> Content MathML -> Maxima
up-conversion process.

Copyright (c) 2010, The University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs m s">

  <!-- Import basic formatting stylesheet and utilities -->
  <xsl:import href="format-output.xsl"/>
  <xsl:import href="demo-utilities.xsl"/>

  <xsl:param name="is-new-form" as="xs:boolean" required="yes"/>
  <xsl:param name="ascii-math-input" as="xs:string?" select="'e^(i pi)+1 = 0'"/>
  <xsl:param name="ascii-math-output" as="xs:string?"/>
  <xsl:param name="parallel-mathml-element" as="element()?"/>
  <xsl:param name="parallel-mathml" as="xs:string?"/>
  <xsl:param name="pmathml-upconverted" as="xs:string?"/>
  <xsl:param name="cmathml" as="xs:string?"/>
  <xsl:param name="maxima-input" as="xs:string?"/>

  <!-- Override page ID -->
  <xsl:variable name="pageId" select="'asciiMathMLUpConversionDemo'" as="xs:string"/>

  <xsl:template match="head" mode="extra-head">
    <script type="text/javascript" src="{$context-path}/includes/ASCIIMathML.js"></script>
    <script type="text/javascript" src="{$context-path}/includes/ASCIIMathMLeditor.js"></script>
    <script type="text/javascript" src="{$context-path}/includes/ASCIIMathMLwidget.js"></script>
  </xsl:template>

  <xsl:template match="body" mode="make-content">
    <xsl:choose>
      <xsl:when test="not($is-mathml-capable)">
        <div class="warning">
          <p>
            This demo requires you to be using a browser capable of handling
            MathML, such as Firefox or Internet Explorer with the MathPlayer plugin.
          </p>
          <xsl:if test="$is-internet-explorer">
            <p>
              As you currently appear to be using Internet Explorer, you might
              want to install the MathPlayer plugin.
            </p>
          </xsl:if>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <!-- Input Form -->
        <h3>Input</h3>
        <p>
          This demo is similar to the
          <a href="{s:fix-href('docs://upConversionDemo')}">MathML Semantic Enrichnment Demo</a>
          but uses
          <a href="http://www1.chapman.edu/~jipsen/asciimath.html">ASCIIMathML</a> as
          an alternative input format.
          SnuggleTeX can attempt to convert the raw output from ASCIIMathML to
          something equivalent to its own MathML, thereby allowing you to hook into the
          up-conversion process in the same way. (There are some caveats though...)
        </p>
        <p>
          Enter some ASCIIMathML into the box below. You should see a real time preview
          of this while you type. Hit <tt>Go!</tt> to see the resulting outputs, which take
          the MathML produced by ASCIIMathML and do interesting things to it.
        </p>
        <form method="post" class="input" action="{$context-path}/ASCIIMathMLUpConversionDemo">
          <div class="inputBox">
            ASCIIMath Input:
            <input id="asciiMathInput" name="asciiMathInput" type="text" value="{$ascii-math-input}"/>
            <input type="hidden" id="asciiMathML" name="asciiMathML"/>
            <input type="submit" value="Go!"/>
          </div>
        </form>

        <h3>Live Preview</h3>
        <p>
          This is generated by ASCIIMathML as you type.
        </p>
        <div class="result">
          <div id="preview"><xsl:text> </xsl:text></div>
        </div>
        <!-- Wire up the form to the preview box -->
        <script type="text/javascript">
          registerASCIIMathMLInputWidget('asciiMathInput', 'preview', 'asciiMathML');
        </script>

        <xsl:if test="not($is-new-form)">
          <xsl:apply-templates select="." mode="handle-successful-input"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  This template shows the details when the input has been
  successfully passed through SnuggleTeX, though with the
  possibility that up-conversion has not been entirely successful.
  -->
  <xsl:template match="body" mode="handle-successful-input">
    <xsl:variable name="content-failures" as="element(s:fail)*" select="$parallel-mathml-element/m:semantics/m:annotation-xml[@encoding='MathML-Content-upconversion-failures']/*"/>
    <xsl:variable name="maxima-failures" as="element(s:fail)*" select="$parallel-mathml-element/m:semantics/m:annotation-xml[@encoding='Maxima-upconversion-failures']/*"/>
    <h3>Raw ASCIIMathML Output</h3>
    <p>
      This is the raw Presentation MathML created by ASCIIMathML:
    </p>
    <pre class="result">
      <xsl:value-of select="$ascii-math-output"/>
    </pre>

    <h3>Enhanced Presentation MathML</h3>
    <p>
      This shows the result of attempting to infer semantics from the raw
      Presentation MathML:
    </p>
    <pre class="result">
      <xsl:value-of select="$pmathml-upconverted"/>
    </pre>

    <h3>Content MathML</h3>
    <p>
      This shows the result of an attempted
      <a href="documentation/content-mathml.html">conversion to Content MathML</a>:
    </p>
    <xsl:choose>
      <xsl:when test="exists($content-failures)">
        <p>
          The conversion from Presentation MathML to Content MathML was not successful
          for this input.
        </p>
        <xsl:call-template name="format-upconversion-failures">
          <xsl:with-param name="failures" select="$content-failures"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <pre class="result">
          <xsl:value-of select="$cmathml"/>
        </pre>
      </xsl:otherwise>
    </xsl:choose>

    <h3>Maxima Input Form</h3>
    <p>
      This shows the result of an attempted
      <a href="documentation/maxima-input.html">conversion to Maxima Input syntax</a>:
    </p>
    <xsl:choose>
      <xsl:when test="exists($content-failures)">
        <p>
          Conversion to Maxima Input is reliant on the conversion to Content MathML
          being successful, which was not the case here.
        </p>
      </xsl:when>
      <xsl:when test="exists($maxima-failures)">
        <p>
          The conversion from Content MathML to Maxima Input was not successful for
          this input.
        </p>
        <xsl:call-template name="format-upconversion-failures">
          <xsl:with-param name="failures" select="$maxima-failures"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <pre class="result">
          <xsl:value-of select="$maxima-input"/>
        </pre>
      </xsl:otherwise>
    </xsl:choose>

    <h3>MathML Parallel Markup</h3>
    <p>
      This shows the enhanced Presentation MathML with other forms encapsulated
      as annotations:
    </p>
    <pre class="result">
      <xsl:value-of select="$parallel-mathml"/>
    </pre>
  </xsl:template>

</xsl:stylesheet>
