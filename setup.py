#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""Setup script for freedots."""

from setuptools import setup, find_packages
from distutils.cmd import Command
import commands
import os
import re
import sys

class doc(Command):
    """Implements the distutils doc extension command."""

    description = "generate the documentation (PDF, HTML and text)"
    user_options = []
    boolean_options = []
    help_options = []
    
    def initialize_options(self):
        self.doc_base = None
        self.bdoc_base = None
        self.build_base = None

    def finalize_options(self):
        self.doc_base = "doc"
        if self.bdoc_base is None:
            self.bdoc_base = os.path.join("build", self.doc_base)

    def run(self):
        self.set_undefined_options('build', ('build_base', 'build_base'))
        if not os.path.exists(self.bdoc_base):
            self.mkpath(self.bdoc_base)

        # Build the manual
        guideSource = "doc/manual.xml"
                
        guideOutputs = {}
        # Check the validity of the docbook code
        procSt, guideOutputs["validation"] = commands.getstatusoutput(
            "xmllint --noout --valid %s" % guideSource)
                
        # Generate the several manual formats
        procSt, guideOutputs["txt"] = commands.getstatusoutput(
            "xsltproc doc/html.xsl %s | lynx -nonumbers -dump -stdin >%s/manual.txt" \
                % (guideSource, self.bdoc_base))

        procSt, guideOutputs["html"] = commands.getstatusoutput(
            "xsltproc doc/html.xsl %s >%s/manual.html" \
                % (guideSource, self.bdoc_base))

        procSt, guideOutputs["fo"] = commands.getstatusoutput(
            "xsltproc doc/pdf-a4.xsl %s >%s/manual-a4.fo" \
                % (guideSource, self.bdoc_base))

        procSt, guideOutputs["pdf"] = commands.getstatusoutput(
            "fop -c doc/fop.xml -fo %s/manual-a4.fo -pdf %s/manual-a4.pdf" \
                % (self.bdoc_base, self.bdoc_base))

        procSt, guideOutputs["man"] = commands.getstatusoutput(
            "xsltproc --stringparam man.output.base.dir '%s/' --stringparam man.output.in.separate.dir 1 /usr/share/xml/docbook/stylesheet/nwalsh/manpages/docbook.xsl %s" \
                % (self.bdoc_base, guideSource))

        # Send out the outputs from the processes
        for output in guideOutputs.values():
            print output
                    

setup(
    cmdclass = {'doc': doc},
    name = "freedots",
    version = "0.5",
    url = "http://delysid.org/freedots.html",
    packages = find_packages(),
    install_requires = ['pygame'],
    package_data = {'freedots': ['COPYING']},
    entry_points = {
        'console_scripts': ['freedots = freedots.frontend:main']
    },
    author = "Mario Lang",
    author_email = "mlang@delysid.org",
    description = "MusicXML to Braille music translation",
    license = "GPL",
    keywords = "braille, music notation, musicxml, midi, gtk"
)

