/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
package freedots.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.logging.Logger;

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
import freedots.braille.BrailleEncoding;
import freedots.musicxml.Score;
import freedots.transcription.Transcriber;

@SuppressWarnings("serial")
public class MusicXML2BrailleServlet extends javax.servlet.http.HttpServlet {
  private static final Logger LOG =
    Logger.getLogger(MusicXML2BrailleServlet.class.getName());

  public void doGet(HttpServletRequest req,
                    HttpServletResponse resp) throws IOException {
    String uri = req.getParameter("uri");
    if (uri != null && uri.length() > 0) {
      URL url = new URL(uri);
      String extension = "xml";
      BrailleEncoding brailleEncoding = BrailleEncoding.UnicodeBraille;

      String ext = uri.substring(uri.length() - 3);
      if (ext.compareTo("mxl") == 0) {
        extension = ext;
      }

      String encodingParam = req.getParameter("encoding");
      if (encodingParam != null && !encodingParam.isEmpty()) {
        try {
          brailleEncoding = Enum.valueOf(BrailleEncoding.class, encodingParam);
        } catch (IllegalArgumentException e) {
          LOG.info("Unknown encoding "+encodingParam+", falling back to default");
        }
      }

      Score score = parseMusicXML(url.openStream(), extension);
      if (score != null)
        writeResult(score, 40, 25, Method.SectionBySection, brailleEncoding,
                    resp);
      else
        resp.sendError(500);
    } else {
      LOG.info("Bad URI error");
      resp.sendError(500);
    }
  }

  public void doPost(HttpServletRequest req,
                     HttpServletResponse resp) throws IOException {
    Score score = null;
    BrailleEncoding brailleEncoding = BrailleEncoding.UnicodeBraille;
    InputStream stream = null;
    ServletFileUpload upload = new ServletFileUpload();
    try {
      FileItemIterator iterator = upload.getItemIterator(req);
      while (iterator.hasNext()) {
        final FileItemStream item = iterator.next();
        if (item.getFieldName().compareTo("file.xml") == 0) {
          String extension = "xml";
          if (item.getName().endsWith(".mxl")) extension = "mxl";
          score = parseMusicXML(item.openStream(), extension);
        } else if (item.getFieldName().compareTo("encoding") == 0) {
          final BufferedReader reader =
            new BufferedReader(new InputStreamReader(item.openStream()));
          final String line = reader.readLine();
          if (line != null) {
            try {
              brailleEncoding = Enum.valueOf(BrailleEncoding.class, line);
            } catch (IllegalArgumentException e) {
              LOG.info("Unknown encoding "+line+", falling back to default");
            }            
          }
        }
      }
    } catch (org.apache.commons.fileupload.FileUploadException e) {
      LOG.info("FileUploadException error");
      resp.sendError(500);
    }

    if (score != null) {
      writeResult(score, 40, 25, Method.SectionBySection, brailleEncoding,
                  resp);
    } else {
      resp.sendRedirect("/");
    }
  }

  private Score parseMusicXML(InputStream stream, String extension) throws IOException {
    Score score = null;
    try {
      score = new Score(stream, extension);
    } catch (XPathExpressionException e) {
      LOG.info("XPathExpressionException error");
    } catch (ParserConfigurationException e) {
      LOG.info("ParserConfigurationException error");
    } catch (SAXException e) {
      LOG.info("SAXException error");
    }
    return score;
  }

  private void writeResult(Score score,
                           int width, int height, Method method,
                           BrailleEncoding encoding,
                           HttpServletResponse resp) throws IOException {
    if (score != null) {
      String[] args = {};
      Options options = new Options(args);

      options.setPageWidth(width);
      options.setPageHeight(height);
      options.setMethod(method);

      final Transcriber transcriber = new Transcriber(options);
      transcriber.setScore(score);
      final String result = transcriber.toString(encoding);

      String title = score.getMovementTitle();
      String filename = "output."+encoding.getExtension();
      if (title != null && !title.isEmpty())
        filename = title + "."+encoding.getExtension();

      if (encoding == BrailleEncoding.HTML) {
        resp.setHeader("Content-Type", "text/html; charset=utf-8");
      } else {
        resp.setHeader("Content-Type", "application/force-download; name=\""
                       + filename + "\"");
        resp.setHeader("Content-Transfer-Encoding", "binary");
        resp.setHeader("Content-Disposition", "attachment; filename=\""
                       + filename + "\"");
      }
      BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(),
                                                  "UTF-8"));

      writer.write(result);
      writer.flush();
      writer.close();
    }
  }
}
