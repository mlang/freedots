all: program documentation

program:
	cd java && ant

documentation: doc/manual.xml
	xsltproc doc/html.xsl $< | lynx -nonumbers -dump -stdin >doc/manual.txt
	xsltproc --stringparam man.output.base.dir 'doc/' \
	         --stringparam man.output.in.separate.dir 1 \
	/usr/share/xml/docbook/stylesheet/nwalsh/manpages/docbook.xsl $<

clean:
	cd java && ant clean
	rm -f doc/manual.txt doc/man1/freedots.1

