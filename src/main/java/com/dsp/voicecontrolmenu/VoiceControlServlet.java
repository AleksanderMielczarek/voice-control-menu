package com.dsp.voicecontrolmenu;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Aleksander on 2015-01-17.
 */
@WebServlet(urlPatterns = "/voice-control-menu", asyncSupported = true, loadOnStartup = 1)
public class VoiceControlServlet extends HttpServlet {
    private LiveSpeechRecognizer recognizer;

    @Override
    public void init() throws ServletException {
        Configuration configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/cmusphinx-5prealpha-en-us-2.0");
        configuration.setDictionaryPath("resource:/dictionary/numbers.dict");
        configuration.setGrammarPath("resource:/grammar");
        configuration.setGrammarName("numbers");
        configuration.setUseGrammar(true);

        try {
            recognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException e) {
            throw new ServletException(e);
        }

        recognizer.startRecognition(true);
    }

    @Override
    public void destroy() {
        recognizer.stopRecognition();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/event-stream;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");

        PrintWriter printWriter = resp.getWriter();

        while (true) {
            SpeechResult result = recognizer.getResult();
            if (result != null) {
                printWriter.print("data: " + result.getHypothesis() + "\n\n");
                printWriter.flush();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
