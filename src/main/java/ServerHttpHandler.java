import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class ServerHttpHandler implements HttpHandler {
    private ServerPOST serverPOST;

    public ServerHttpHandler(DataBaseHandler dbHandler, MailHandler mailHandler){
        this.serverPOST = new ServerPOST(dbHandler, mailHandler);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // Handler for Http requests
        String htmlResponse;
        switch(httpExchange.getRequestMethod()) {
            case "GET":
                htmlResponse = handleGetRequest(httpExchange);
                break;
            case "POST":
                htmlResponse = handlePostRequest(httpExchange);
                break;
            default:
                htmlResponse = "{\"response\" : \"failure\"}";
                break;

        }
        handleResponse(httpExchange, htmlResponse);
    }
    private String handleGetRequest(HttpExchange httpExchange) {
        String uri = httpExchange.
                        getRequestURI()
                        .toString()
                        .split("\\?")[1]
                        .split("=")[1];
        System.out.println(uri);
        return "{\"response\" : \"failure\"}";
    }

    private String handlePostRequest(HttpExchange httpExchange) {
        String htmlResponse;

        // Get request body
        String msg = this.parseMsg(httpExchange);
        // Convert Json to FormObject
        FormObject form = this.serverPOST.convertJsonToForm(msg);

        // Insert into database
        Boolean success = this.serverPOST.insertIntoDB(form);
        Step stepName = Step.DATABASE_INSERT;
        if (success) {
            success = this.serverPOST.sendMailToAdmin(form);
            stepName = Step.MAIL_TO_ENV;
            if (success) {
                success = this.serverPOST.sendMailToUser(form);
                stepName = Step.MAIL_TO_SENDER;
            }
        }

        // Get outcome response
        if(success) {
            htmlResponse = "{\"response\" : \"success\"}";
        } else {
            htmlResponse = "{\"response\" : \"failure\"}";
            System.out.println(stepName);
        }
        System.out.println("Outcome: " + htmlResponse);

        return htmlResponse;
    }

    private void handleResponse(HttpExchange httpExchange, String htmlResponse)  throws  IOException {
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

    private String parseMsg(HttpExchange httpExchange) {
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


}

enum Step {
    DATABASE_INSERT,
    MAIL_TO_ENV,
    MAIL_TO_SENDER
}
