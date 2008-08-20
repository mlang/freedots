<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		version="1.0">
  <xsl:import href="/usr/share/xml/docbook/stylesheet/nwalsh/fo/docbook.xsl"/>
  <xsl:include href="pdf.xsl"/>
  <!-- Parameters -->
  <xsl:param name="paper.type" select="'A4'" />
</xsl:stylesheet>

