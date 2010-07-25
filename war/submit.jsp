<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>
<h2>MusicXML to Braille converter</h2>
<p>Pick a MusicXML file on your disk and press "Convert to Braille", you'll be prompted to
download a text file if the conversion is successful.</p>
<form action="musicxml2braille" enctype="multipart/form-data" method="post">
<div><input type="file" NAME="file.xml"></div>
<div><select NAME="encoding">
       <option value="UnicodeBraille">Unicode braille (txt)</option>
       <option value="NorthAmericanBrailleComputerCode">Braille ASCII (brf)</option>
       <option value="HTML>Annotated braille (html)</option>
     </select>
</div>
<div><input type="submit" name="submit" value="Convert to Braille" onClick="pageTracker._trackEvent('Submit', 'Click');" /></div>
</form>
<br />
<p>Powered by <a href="http://code.google.com/p/freedots/">Freedots</a> by Mario Lang<br />
Edit your MusicXML with <a href="http://musescore.org/">MuseScore</a>,
the free score editor.<br />
Contact me : <img align="middle"
	src="http://services.nexodyne.com/email/icon/%2BypSWF0oXvg%3D/9qTVKK4%3D/R01haWw%3D/0/image.png" />
or <a href="http://twitter.com/lasconic">@lasconic</a></p>
<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-10723381-X");
pageTracker._trackPageview();
} catch(err) {}</script>
</body>
</html>
