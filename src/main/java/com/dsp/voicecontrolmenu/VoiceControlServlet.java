package com.dsp.voicecontrolmenu;

import com.google.common.collect.Sets;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aleksander on 2015-01-17.
 */
@WebServlet(urlPatterns = "/voice-control-menu", asyncSupported = true, loadOnStartup = 1)
public class VoiceControlServlet extends HttpServlet {
    private static final Set<String> availableCommands = Sets.newHashSet("jeden", "dwa", "trzy", "cztery", "piec", "szesc");

    private static final String MODEL_PATH = "resource:/cmusphinx-5prealpha-en-us-2.0";
    private static final String DICTIONARY_PATH = "resource:/dictionary/numbers.dict";
    private static final String GRAMMAR_PATH = "resource:/grammar";
    private static final String GRAMMAR_NAME = "numbers";
    private static final String CONTENT_TYPE = "text/event-stream;charset=UTF-8";
    private static final String CACHE_CONTROL_NAME = "Cache-Control";
    private static final String CACHE_CONTROL_VALUE = "no-cache";
    private static final String CONNECTION_NAME = "Connection";
    private static final String CONNECTION_VALUE = "keep-alive";
    private static final String DATA_PREFIX = "data: ";
    private static final String DATA_SUFFIX = "\n\n";
    private static final int SLEEP_TIME = 500;

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private LiveSpeechRecognizer recognizer;
    private volatile SpeechResult result;

    @Override
    public void init() throws ServletException {
        Configuration configuration = new Configuration();

        configuration.setAcousticModelPath(MODEL_PATH);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setGrammarPath(GRAMMAR_PATH);
        configuration.setGrammarName(GRAMMAR_NAME);
        configuration.setUseGrammar(true);

        try {
            recognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException e) {
            throw new ServletException(e);
        }

        recognizer.startRecognition(true);

        service.execute(new Runnable() {
            @Override
            public void run() {
                for (Diode diode : Diode.values()) {
                    diode.disableTrigger();
                    diode.turnOff();
                }

                while ((result = recognizer.getResult()) != null) {
                    String command = result.getHypothesis();

                    //skip if current isn't in available commands
                    if (!availableCommands.contains(command)) {
                        continue;
                    }

                    //turn off if current is out of range
                    if (!Diode.isDiodeAvailable(command)) {
                        for (Diode diode : Diode.values()) {
                            if (diode.isTurnOn()) {
                                diode.turnOff();
                            }
                        }
                        continue;
                    }

                    //do nothing if current is already turn on
                    if (Diode.valueOfCommand(command).isTurnOn()) {
                        continue;
                    }

                    //turn off if current is different
                    for (Diode diode : Diode.values()) {
                        if (diode.isTurnOn()) {
                            diode.turnOff();
                        }
                    }

                    //turn on current
                    Diode.valueOfCommand(result.getHypothesis()).turnOn();
                }
            }
        });
    }

    @Override
    public void destroy() {
        recognizer.stopRecognition();
        service.shutdownNow();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENT_TYPE);
        resp.setHeader(CACHE_CONTROL_NAME, CACHE_CONTROL_VALUE);
        resp.setHeader(CONNECTION_NAME, CONNECTION_VALUE);

        PrintWriter printWriter = resp.getWriter();

        while (result != null) {
            printWriter.print(DATA_PREFIX + result.getHypothesis() + DATA_SUFFIX);
            printWriter.flush();

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
