import java.io.IOException;
import java.sql.SQLException;

public class ServerPOST {
    private DataBaseHandler dbHandler;
    private MailHandler mailHandler;

    public ServerPOST(DataBaseHandler dbHandler, MailHandler mailHandler){
        this.dbHandler = dbHandler;
        this.mailHandler = mailHandler;
    }

    public FormObject convertJsonToForm(String msg){
        // Convert Json to FormObject
        return new FormObject(msg);
    }

    public Boolean insertIntoDB(FormObject obj){
        // Insert into database
        try {
            this.dbHandler.insertIntoDB(obj);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean sendMailToAdmin(FormObject obj){
        Boolean success;
        try {
            success =  this.mailHandler.sendMailToAdmin(obj);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public Boolean sendMailToUser(FormObject obj){
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
