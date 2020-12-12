import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringSubstitutor;

public class MailHandler{
    private String to_env;
    private String subject;
    private String uri;
    private String html;
    private String user_template_path;
    private String admin_template_path;

    public MailHandler() {
        this.to_env = System.getenv("MAIL_TO");
        this.subject = System.getenv("MAIL_SUBJECT");
//        this.uri = "http://mail1:587/mail";
//        this.uri = "http://localhost:587/mail";
        this.uri = "http://fitjarmail:587/mail";
//        this.uri = System.getenv("MAIL_HANDLER_URL");

        this.user_template_path = "resources/template_user.html";
        this.admin_template_path = "resources/template_admin.html";
        this.html = null;
    }

    public Boolean sendMailToAdmin(FormObject form) throws IOException, InterruptedException {
        return this.sendMail(form, this.admin_template_path, this.to_env);
    }

    public Boolean sendMailToUser(FormObject form) throws IOException, InterruptedException {
        return this.sendMail(form, this.user_template_path, form.getValue("mail"));
    }

    private Boolean sendMail(FormObject form, String path, String to_address) throws IOException, InterruptedException {
        this.readHtmlFromTemplate(path);
        HashMap<String, String> values = new HashMap<>();
        values.put("subject", this.subject);
        values.put("to", to_address);
        values.put("text", this.convertFormToMailContent(form));

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper
                .writeValueAsString(values);

        return this.sendHttpMail(requestBody);
    }

    private void readHtmlFromTemplate(String path){
        StringBuilder stringBuilder = new StringBuilder(1000);
        try {
            File file = new File(path);
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                stringBuilder.append(sc.nextLine());
            }
            this.html = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String convertFormToMailContent(FormObject form){
        if(this.html != null) {
            StringSubstitutor sub = new StringSubstitutor(form.getJsonDict());
            return sub.replace(this.html);
        }else{
            return form.getJsonDict().keySet().stream()
                    .map(key -> key + " : " + form.getValue(key))
                    .collect(Collectors.joining(",<br/>", "", ""));
        }
    }

    private Boolean sendHttpMail(String requestBody) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(CookieHandler.getDefault())
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.uri))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        //creating response body handler
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client
                .send(request, bodyHandler);

        String requestOutcome = response.body();
        return this.checkRequestOutcome(requestOutcome);
    }

    private Boolean checkRequestOutcome(String requestOutcome){
        return requestOutcome.equals("success");
    }
}
