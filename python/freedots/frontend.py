import braillemusic
import musicxml
from optparse import OptionParser
import codecs
import sys

def main():
    parser = OptionParser("usage: %prog [options] filename")
    parser.add_option("", "--gui",
                      action="store_true", dest="gui", default=False,
                      help="Start the editor GUI (GTK)")
    parser.add_option("-p", "--play",
                      action="store_true", dest="play", default=False,
                      help="Play the score")
    parser.add_option("", "--shell",
                      action="store_true", dest="shell_mode", default=False,
                      help="Start a Python shell (for inspection)")
    parser.add_option("-w", "--width",
                      action="store", type="int", dest="width", default=40,
                      help="Maximum number of characters per line")
    (options, args) = parser.parse_args()
    if options.width < 8:
        parser.error("number of characters per line is too small")
    if options.gui:
        import gui
        inputfile = None
        if len(args)>0:
            inputfile = args[0]
        freedots = gui.Application(inputfile, width=options.width)
        freedots.run()
    else:
        if len(args) != 1:
            parser.error("incorrect number of arguments")
        inputfile = args[0]
        score = musicxml.Score(inputfile)
        if not score:
            parser.error("unable to parse input file %s (not a MusicXML file?)"
                         % inputfile)
        sys.stdout = codecs.getwriter('utf-8')(sys.stdout, 'replace')
        output = braillemusic.Embosser(width=options.width)
        output.format(score)
        sys.stdout.write(unicode(output)+"\n")
        if options.play:
            import playback
            playback.play(score)
            if not options.shell_mode:
                playback.spin()
        if options.shell_mode:
            import code
            del parser
            del args
            if options.play:
                play = playback.play
            else:
                try:
                    from playback import play
                except:
                    pass
            code.interact(banner="""FreeDots Python Shell
The variable 'score' holds an instance of your music.""",
                              local=locals())

if __name__ == '__main__':
    main()
