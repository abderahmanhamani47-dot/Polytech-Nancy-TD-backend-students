package com.example.todoapp.presentation;



import com.example.todoapp.JsonUtils;
import com.example.todoapp.business.service.TaskService;
import com.example.todoapp.dto.ErrorDto;
import com.example.todoapp.dto.Outputdto;
import com.example.todoapp.dto.POSTdto;
import com.example.todoapp.dto.PUTdto;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TaskController implements HttpHandler {

    private final TaskService taskService = new TaskService();

    // regex pour recuperer id dans /tasks/{id}
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");

    public void handle(HttpExchange exchange) throws IOException {

        // autoriser les appel depuis le frontend (CORS)
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        // gestion de /tasks
        if ("/tasks".equals(path)) {

            // gérer POST /tasks
            if("POST".equals(method)) {
                POSTdto input = JsonUtils.deserialize(
                        new String(exchange.getRequestBody().readAllBytes(), UTF_8),
                        POSTdto.class
                );

                // verification du title
                if (input.title() == null || input.title().isEmpty()) {
                    ErrorDto error = new ErrorDto("title", "Title should not be empty.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                if (input.title() != null && input.title().length() > 50) {
                    ErrorDto error = new ErrorDto("title", "Title should not exceed 50 characters.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                if (input.description() != null && input.description().length() > 255) {
                    ErrorDto error = new ErrorDto("description", "Description should not exceed 255 characters.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                // creation de la tache
                Outputdto createdTask = this.taskService.createTask(input);

                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }

            // gérer GET /tasks
            if ("GET".equals(method)) {

                boolean todoOnly = query != null && query.equals("todo-only=true");

                Collection<Outputdto> output = this.taskService.getTasksList(todoOnly);

                if (output.isEmpty()) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 200, JsonUtils.serialize(output));
                }
                return;
            }
        }

        // gestion de /tasks/{id}
        Matcher m = ID_PATH.matcher(path);

        if (m.matches()) {

            int id = Integer.parseInt(m.group(1));

            Optional<Outputdto> output = this.taskService.getTaskById(id);

            if (output.isEmpty()) {
                sendResponse(exchange, 404, null);
                return;
            }

            // GET task par id
            if ("GET".equals(method)) {
                sendResponse(exchange, 200, JsonUtils.serialize(output));
                return;
            }

            // DELETE task
            if ("DELETE".equals(method)){
                this.taskService.deleteTaskById(id);
                sendResponse(exchange, 204, null);
                return;
            }

            // PUT task (update)
            if ("PUT".equals(method)) {

                InputStream bodyStream = exchange.getRequestBody();
                String body = new String(bodyStream.readAllBytes());

                PUTdto input = JsonUtils.deserialize(body, PUTdto.class);

                // verification title
                if (input.title() == null || input.title().isEmpty()) {
                    ErrorDto error = new ErrorDto("title", "Title should not be empty.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                if (input.title() != null && input.title().length() > 50) {
                    ErrorDto error = new ErrorDto("title", "Title should not exceed 50 characters.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                if (input.description() != null && input.description().length() > 255) {
                    ErrorDto error = new ErrorDto("description", "Description should not exceed 255 characters.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                // update task
                this.taskService.updateTaskById(id, input.title(), input.description(), input.done());
                sendResponse(exchange, 204, null);
                return;
            }
        }

        // sinon 404
        sendResponse(exchange, 404, null);
    }

    // methode helper pour envoyer la reponse
    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        if(nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }
}
