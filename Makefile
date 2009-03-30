JAVA=java
DATESTAMP=$(shell date +%Y%m%d)
WIN32FILE=FreeDots-$(DATESTAMP).exe
JARFILE=FreeDots-$(DATESTAMP).jar
GC_PASSWORD=$(shell head -8 $$(grep "Google Code" ~/.subversion/auth/svn.simple/*|awk -F: '{print $$1}')|tail -1)

all: program documentation installer

program:
	cd java && ant

doc/manual.xml: doc/example1-2.brl doc/example1-2.mid \
		doc/example1-11.brl doc/example1-11.mid

doc/example%.brl: input/nimobmn-examples/example%.xml
	$(JAVA) -jar java/dist/$(JARFILE) $< >$@

doc/example%.mid: input/nimobmn-examples/example%.xml
	$(JAVA) -jar java/dist/$(JARFILE) -emf $@ $<

documentation: doc/manual.xml
	xsltproc --xinclude doc/html.xsl $< > doc/manual.html
	xsltproc --xinclude doc/html.xsl $< | lynx -nonumbers -dump -stdin >doc/manual.txt
	xsltproc --xinclude --stringparam man.output.base.dir 'doc/' \
	         --stringparam man.output.in.separate.dir 1 \
		/usr/share/xml/docbook/stylesheet/nwalsh/manpages/docbook.xsl $<

installer: java/dist/$(JARFILE) UBraille.ttf
	makensis -DJARFILE=$(JARFILE) -DOUTFILE=$(WIN32FILE) WindowsInstaller.nsi

distribute:
	@python googlecode_upload.py -s 'Java 1.6 JAR' -p freedots -u mlang@tugraz.at -w $(GC_PASSWORD) java/dist/$(JARFILE)
	@python googlecode_upload.py -s 'MS Windows installer' -p freedots -u mlang@tugraz.at -w $(GC_PASSWORD) $(WIN32FILE)

clean:
	cd java && ant clean
	rm -f doc/manual.txt doc/man1/freedots.1
	rm -f FreeDots-*.exe

UBraille.ttf:
	wget http://yudit.org/download/fonts/UBraille/$@

