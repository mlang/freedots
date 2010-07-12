package freedots.web;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.xml.sax.SAXException;

import freedots.Options;
import freedots.Options.Method;
import freedots.musicxml.Score;
import freedots.transcription.Transcriber;

@SuppressWarnings("serial")
public class MusicXML2BrailleServlet extends HttpServlet {
	private static final Logger LOG = Logger
			.getLogger(MusicXML2BrailleServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String uri = req.getParameter("uri");
		if (uri != null && uri.length() > 0) {

			URL url = new URL(uri);
			String extension = "xml";
			String ext = uri.substring(uri.length() - 3);
			if(ext.compareTo("mxl") == 0){
				extension = ext;
			}
			Score score = null;
			try {
				score = new Score(url.openStream(), extension);
			} catch (XPathExpressionException e) {
				LOG.info("XPathExpressionException error");
				resp.sendError(500);
			} catch (ParserConfigurationException e) {
				LOG.info("ParserConfigurationException error");
				resp.sendError(500);
			} catch (SAXException e) {
				LOG.info("SAXException error");
				resp.sendError(500);
			}
			if (score != null) {
				String[] args = {};
				Options options = new Options(args);
				options.setMethod(Method.SectionBySection);
				Transcriber transcriber = new Transcriber(options);
				transcriber.setScore(score);
				String result = transcriber.toString();
				writeResult(score, result, resp);
			}
		} else {
			LOG.info("Bad URI error");
			resp.sendError(500);
		}

	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Score score = null;
		try {
			ServletFileUpload upload = new ServletFileUpload();
			FileItemIterator iterator = upload.getItemIterator(req);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				InputStream stream = item.openStream();

				if (item.getFieldName().compareTo("file.xml") == 0) {
					score = new Score(stream, "xml");
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (score != null) {
			String[] args = {};
			Options options = new Options(args);
			options.setMethod(Method.SectionBySection);
			Transcriber transcriber = new Transcriber(options);
			transcriber.setScore(score);
			String result = transcriber.toString();

			writeResult(score, result, resp);
		} else {
			resp.sendRedirect("/");
		}
	}

	private void writeResult(Score score, String result,
			HttpServletResponse resp) throws IOException {
		String title = score.getMovementTitle();
		String filename = "output.txt";
		if (title != null && !title.isEmpty())
			filename = title + ".txt";

		resp.setHeader("Content-Type", "application/force-download; name=\""
				+ filename + "\"");
		resp.setHeader("Content-Transfer-Encoding", "binary");
		resp.setHeader("Content-Disposition", "attachment; filename=\""
				+ filename + "\"");

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(resp
				.getOutputStream(), "UTF-8"));

		writer.write(result);
		writer.flush();
		writer.close();

	}
}
