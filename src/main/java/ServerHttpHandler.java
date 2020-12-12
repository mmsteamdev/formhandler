import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.SQLException;

public class ServerHttpHandler implements HttpHandler {
    private DataBaseHandler dbHandler;
    private MailHandler mailHandler;

    public ServerHttpHandler(DataBaseHandler dbHandler, MailHandler mailHandler){
        this.dbHandler = dbHandler;
        this.mailHandler = mailHandler;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // Handler for Http requests
        String requestParamValue=null;
        if("GET".equals(httpExchange.getRequestMethod())) {
            requestParamValue = handleGetRequest(httpExchange);
        }else if("POST".equals(httpExchange.getRequestMethod())) {
            requestParamValue = handlePostRequest(httpExchange);
        }
        handleResponse(httpExchange,requestParamValue);
    }
    private String handleGetRequest(HttpExchange httpExchange) {
        return httpExchange.
                getRequestURI()
                .toString()
                .split("\\?")[1]
                .split("=")[1];
    }

    private String handlePostRequest(HttpExchange httpExchange) throws IOException {
//        this.printAdditionalRequestAttribs(httpExchange);

        // Get request body
        String message = null;
        try (InputStream in = httpExchange.getRequestBody()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder msgbuilder = new StringBuilder();
            int c;
            while ((c = br.read()) > -1) {
                msgbuilder.append((char) c);
            }
            message = msgbuilder.toString();
            System.out.println("Message: " + message);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    private void printAdditionalRequestAttribs(HttpExchange httpExchange){
        // Get request Header
        Headers reqHeaders = httpExchange.getRequestHeaders();
        reqHeaders.forEach((key, value) -> System.out.println(key + ": " + value));

        // Get request address
        InetSocketAddress remoteAddress = httpExchange.getRemoteAddress();
        System.out.println(remoteAddress.toString());

        // Get request URI
        URI requestURI = httpExchange.getRequestURI();
        System.out.println(requestURI.toString());
    }

    private void handleResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {
        String htmlResponse;
        // Convert Json to FormObject
        FormObject form = this.convertJsonToForm(requestParamValue);

        // Insert into database
        Boolean success = this.insertIntoDB(form);
        Step stepName = Step.DATABASE_INSERT;
        if (success) {
            success = this.sendMailToAdmin(form);
            stepName = Step.MAIL_TO_ENV;
            if (success) {
                success = this.sendMailToUser(form);
                stepName = Step.MAIL_TO_SENDER;
            }
        }

        // Get outcome response
        if(success) {
            htmlResponse = "{ \"response\" : \"success\" }";
        } else {
            htmlResponse = "{ \" response\" : \"failure\" }";
            System.out.println(stepName);
        }
        System.out.println("Outcome: " + htmlResponse);

        httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers","x-prototype-version,x-requested-with");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods","GET,POST");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin","*");

        // Create http response
        httpExchange.sendResponseHeaders(200, htmlResponse.length());
        OutputStream outputStream = httpExchange.getResponseBody();

        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private FormObject convertJsonToForm(String msg){
        // Convert Json to FormObject
        return new FormObject(msg);
    }

    private Boolean insertIntoDB(FormObject obj){
        // Insert into database
        try {
            this.dbHandler.insertIntoDB(obj);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Boolean sendMailToAdmin(FormObject obj){
        Boolean success;
        try {
            success =  this.mailHandler.sendMailToAdmin(obj);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    private Boolean sendMailToUser(FormObject obj){
        Boolean success;
        try {
            success =  this.mailHandler.sendMailToUser(obj);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }
}

enum Step {
    DATABASE_INSERT,
    MAIL_TO_ENV,
    MAIL_TO_SENDER
}
