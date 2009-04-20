DATESTAMP=$(shell date +%Y%m%d)
WIN32FILE=FreeDots-$(DATESTAMP).exe
JARFILE=FreeDots-$(DATESTAMP).jar
GC_PASSWORD=$(shell head -8 $$(grep "Google Code" ~/.subversion/auth/svn.simple/*|awk -F: '{print $$1}')|tail -1)

documentation: doc/manual.xml

installer: dist/$(JARFILE) UBraille.ttf
	makensis -DJARFILE=$(JARFILE) -DOUTFILE=$(WIN32FILE) WindowsInstaller.nsi

distribute:
	@python googlecode_upload.py -s 'Java 1.6 JAR' -p freedots -u mlang@tugraz.at -w $(GC_PASSWORD) java/dist/$(JARFILE)
	@python googlecode_upload.py -s 'MS Windows installer' -p freedots -u mlang@tugraz.at -w $(GC_PASSWORD) $(WIN32FILE)

UBraille.ttf:
	wget http://yudit.org/download/fonts/UBraille/$@

