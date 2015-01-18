package com.dsp.voicecontrolmenu;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aleksander on 2015-01-17.
 */
@WebServlet(urlPatterns = "/voice-control-menu", asyncSupported = true, loadOnStartup = 1)
public class VoiceControlServlet extends HttpServlet {
    private ExecutorService executorService;

    private String oldV = "";
    private String newV = "";

    @Override
    public void init() throws ServletException {
        executorService = Executors.newSingleThreadExecutor();

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (int i = 1; i <= 1000; i++) {
                        if (i == 300 || i == 100) {
                            oldV = newV;
                            newV = String.valueOf(i);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void destroy() {
        executorService.shutdown();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/event-stream;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");

        PrintWriter printWriter = resp.getWriter();

        while (true) {
            printWriter.print("data: " + newV + " old " + oldV + "\n\n");
            printWriter.flush();
            try {
                Thread.currentThread().sleep(5000);
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
