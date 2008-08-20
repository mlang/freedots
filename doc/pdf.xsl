<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		version="1.0">
  <!-- Parameters -->
  <xsl:param name="fop1.extensions" select="'1'" />
  <xsl:template match="symbol[@role = 'symbolfont']">
    <fo:inline font-family="DejaVu Serif">
      <xsl:call-template name="inline.charseq"/>
    </fo:inline>
  </xsl:template>
</xsl:stylesheet>
