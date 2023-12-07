package model;

import javafx.beans.property.SimpleStringProperty;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerModel {

    private ServerSocket server   = null;

    public static ArrayList<Client> users = new ArrayList<>();
    private DataInputStream in  =  null;
    private SimpleStringProperty log = null;
    private SimpleStringProperty serverSwitch = null;
    public int PORT;

    public final ServerModel model;

    public ServerModel(int port){

        this.log = new SimpleStringProperty();
        this.serverSwitch = new SimpleStringProperty();
        this.PORT = port;
        this.model = this;
       try {
           loadUsers();
       }catch (Exception e){}

    }

    private void loadUsers() throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        JSONArray a = (JSONArray) parser.parse(new FileReader("/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/client_list.json"));
        for (Object o : a)
        {
            JSONObject person = (JSONObject) o;

            String name = (String) person.get("name");
            String surname = (String) person.get("surname");
            String email = (String) person.get("email");
            String id = (String) person.get("id");

            users.add(new Client(name, surname, email, id));

        }

    }

    public void run(){

        ExecutorService service = Executors.newFixedThreadPool(10);
        service.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    server = new ServerSocket(PORT);
                    writeLog("Server Started");
                    writeLog("Waiting for a client ...");
                    writeLog("--------------------------------------------");
                    //System.out.println("Waiting for a client ...");
                    while(!server.isClosed()){


                        Socket socket = server.accept();
                        Thread thread = new Thread( new ClientHandler(socket, model));
                        thread.start();
                        //Runnable client = new ClientHandler(socket, dis, dos);
                        //service.execute(client);

                    }


                } catch (Exception e) {closeServerSocket();}
            }
        });


    }


    public void writeLog(String message){

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        if (this.getLog().getValue()==null) {

            this.getLog().set(dtf.format(now) + ":  " + message);
            return;
        }
        this.getLog().set(this.getLog().getValue()+"\n"+ dtf.format(now) + ":  " + message);
    }
    public void closeServerSocket(){

        try {
            if (server!=null){
                server.close();
            }
        }catch (IOException e){

            e.printStackTrace();
        }
    }


    public SimpleStringProperty getLog(){
        return this.log;
    }

    public class Client{

        String name;
        String surname;
        String email;
        String id;

        public Client(String name, String surname, String email, String id) {
            this.name = name;
            this.surname = surname;
            this.email = email;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
