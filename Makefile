JAVA=java
DATESTAMP=$(shell date +%Y%m%d)
WIN32FILE=FreeDots-$(DATESTAMP).exe
JARFILE=FreeDots-$(DATESTAMP).jar
GC_PASSWORD=$(shell head -8 $$(grep "Google Code" ~/.subversion/auth/svn.simple/*|awk -F: '{print $$1}')|tail -1)

documentation: doc/manual.xml
	xsltproc --xinclude --stringparam man.output.base.dir 'doc/' \
	         --stringparam man.output.in.separate.dir 1 \
		/usr/share/xml/docbook/stylesheet/nwalsh/manpages/docbook.xsl $<

installer: dist/$(JARFILE) UBraille.ttf
	makensis -DJARFILE=$(JARFILE) -DOUTFILE=$(WIN32FILE) WindowsInstaller.nsi

distribute:
	@python googlecode_upload.py -s 'Java 1.6 JAR' -p freedots -u mlang@tugraz.at -w $(GC_PASSWORD) java/dist/$(JARFILE)
	@python googlecode_upload.py -s 'MS Windows installer' -p freedots -u mlang@tugraz.at -w $(GC_PASSWORD) $(WIN32FILE)

clean:
	cd java && ant clean
	rm -f doc/man1/freedots.1
	rm -f FreeDots-*.exe

UBraille.ttf:
	wget http://yudit.org/download/fonts/UBraille/$@

