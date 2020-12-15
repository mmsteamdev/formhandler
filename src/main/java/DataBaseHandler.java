import java.sql.*;
import java.util.Map;

public class DataBaseHandler {
    private Connection conn;
    private String url;
    private String user;
    private String password;
    private String root_user;
    private String root_password;
    private String driverClass;

    public DataBaseHandler() {
        this.url = System.getenv("MYSQL_URL") +
                "/" +
                System.getenv("MYSQL_DATABASE") +
                "?characterEncoding=utf8";
        this.user = System.getenv("MYSQL_USER");
        this.password = System.getenv("MYSQL_PASSWORD");
        // this.root_user = "root";
        // this.root_password = System.getenv("MYSQL_ROOT_PASSWORD");
        this.driverClass = "com.mysql.cj.jdbc.Driver";

        this.connectToDB();
    }

    public void connectToDB(){
        // Create connection with database
        try {
            Class.forName(this.driverClass);
            this.conn = DriverManager.getConnection(this.url, this.user, this.password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertIntoDB(FormObject obj) throws SQLException {
        if(!this.conn.isValid(10)){
            System.out.println("Reconnecting to db");
            this.connectToDB();
        }
        int index = 1;
        String table = System.getenv("MYSQL_TABLE");
        PreparedStatement pst = null;

        pst = this.conn.prepareStatement("SET NAMES utf8");
        pst.execute();
        pst = this.conn.prepareStatement("set character set utf8");
        pst.execute();

        // Create statement Insert into database with data from FormObject
        pst = this.conn.prepareStatement(
                "INSERT INTO " +
                        table +
                        " (" +
                        obj.getKeysInsert() +
                        ") VALUES (?" +
                        this.getValuesQuestionMarks(obj) +
                        ")"
        );

        for (Map.Entry<String, String> entry : obj.getJsonDict().entrySet()){
            pst.setString(index, entry.getValue());
            index ++;
        }
        pst.executeUpdate();
    }

    private String getValuesQuestionMarks(FormObject obj){
        // returns String of ? separated by , of Json Dict
        return new String(new char[obj.getJsonDictLen() - 1]).replace("\0", ", ?");
    }

}
