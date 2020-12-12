import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FormHandler{
  private DataBaseHandler dbHandler;
  private MailHandler mailHandler;

  public FormHandler(){
    this.dbHandler = new DataBaseHandler();
    this.mailHandler = new MailHandler();
  }

  public void run()  {
    CookieHandler.setDefault(new CookieManager());
    this.createServer();
  }

  private void createServer(){
    int port = 4000;
    try{
      // Create HttpServer
      HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
      ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

      server.createContext("/form", new  ServerHttpHandler(this.dbHandler, this.mailHandler));
      server.setExecutor(threadPoolExecutor);

      //Start HttpServer
      server.start();
      System.out.println("Server started on port " + port);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}