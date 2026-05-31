package com.example.todoapp;

import com.example.todoapp.presentation.TaskController;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Main class of the application. Managing routing and HTTP layer.
 *
 * @author StudentMapper
 * @version 1.0
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final int PORT = 8080;

    /**
     * Entry point of the application.
     * Initialises the HTTP server and registers the task controller.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if the server fails to start
     */
    public static void main(String[] args) throws Exception {
        log.info("In-memory repository initialised");

        TaskController controller = new TaskController();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", controller);
        server.setExecutor(null);
        server.start();

        log.info("HTTP server started on http://localhost:{}", PORT);
    }
}