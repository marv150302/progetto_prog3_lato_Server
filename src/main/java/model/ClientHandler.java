package model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    public ServerModel model;
    public ClientHandler(Socket socket, ServerModel model){

        try{

            this.socket = socket;
            this.model = model;

            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //this.username = bufferedReader.readLine();
            //this.model.writeLog(this.username);



        }catch(IOException e){

            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    private void addClient(String username, ClientHandler new_client){

        for (ClientHandler client : clientHandlers) {

            if (client.username.equals(username)){

                System.out.println("There is already another client");
                client.socket =this.socket;
                return;
            }
        }
        clientHandlers.add(new_client);
    }

    public void sendMessage(String message){

        try{
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedWriter.write(message);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }catch (IOException e){

            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){

        try{

            if (bufferedReader!=null){

                bufferedReader.close();
            }
            if (bufferedWriter!=null){

                bufferedWriter.close();
            }
            if (socket != null){

                socket.close();
            }
        }catch (IOException e){e.printStackTrace();}
    }

    private void displayUsers(){


        for (ClientHandler clientHandler : clientHandlers) {

            System.out.println("Client: " + clientHandler.username);
        }
        System.out.println("------------------------");
    }

    public void run(){

        String messageFromClient;
        boolean interrupt = false;
        while (socket.isConnected() && !interrupt){

            try{

                messageFromClient = bufferedReader.readLine();

                if (messageFromClient!=null){

                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(messageFromClient);
                    switch ((String) json.get("action")){

                        case "login":{
                            if (this.username==null)this.username = (String) json.get("user");
                            model.writeLog(this.username + " has conncted");
                            if (this.username==null) break;

                            addClient(username, this);
                            displayUsers();
                            System.out.println("Client size: " + clientHandlers.size());
                            String filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/"+this.username+".json";
                            File user_file = new File(filePathString);
                            if (!user_file.createNewFile()){

                                /*
                                 * The user already existed, so we send him back his inbox
                                 * otherwise we create a new empty json file
                                 * */
                                String usr_inbox = new String(Files.readAllBytes(Paths.get(filePathString)));
                                JSONObject json_to_send = new JSONObject();
                                json_to_send.put("action", "inbox");
                                json_to_send.put("emails", usr_inbox);
                                this.sendMessage(json_to_send.toString());
                            }else {

                                createUserPrivateFile();
                            }
                            break;
                        }

                        case "send email":{

                            String message = (String) json.get("text");
                            String object = (String) json.get("object");
                            String receiver =(String) json.get("receiver");
                            String sender = (String) json.get("sender");
                            String[] receivers = receiver.split("\\s+");
                            String failed_clients = "";
                            for (String client : receivers) {

                                if (searchClient(client)){
                                    /*
                                    * if we have found the client
                                    * we proceed to send the email
                                    * */

                                    JSONObject json_to_send = new JSONObject();
                                    JSONObject email = new JSONObject();
                                    json_to_send.put("action","receiving email");

                                    email.put("text", message);
                                    email.put("object", object);
                                    email.put("receiver", receiver);
                                    email.put("sender", sender);

                                    String uniqueID = UUID.randomUUID().toString();
                                    email.put("ID", uniqueID);

                                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                                    LocalDateTime now = LocalDateTime.now();
                                    email.put("date", dtf.format(now));

                                    json_to_send.put("email", email);

                                    this.updateUserEmails(receiver, email);
                                    /*
                                    * we retrieve the client socket if he is online
                                    * */
                                    model.writeLog(sender + " has sent an email to: " + client);
                                    ClientHandler client_socket = searchClientSocket(client);
                                    if (client_socket!=null){

                                        //System.out.println(json_to_send);
                                        client_socket.sendMessage(json_to_send.toString());
                                        model.writeLog(receiver + " has received an email from: " + sender);
                                    }

                                    json_to_send = new JSONObject();
                                    json_to_send.put("action", "confirmed delivery");
                                    this.sendMessage(json_to_send.toString());
                                }else {

                                    /*
                                    * we save the wrong emails and report them to the log and the user
                                    * */
                                    failed_clients += client + " - ";
                                }
                            }
                            if (!failed_clients.isEmpty()){

                                /*
                                *
                                * we notify both the server log and the user
                                * */
                                model.writeLog(sender + " failed to send email to: " + failed_clients);
                                JSONObject json_to_send = new JSONObject();
                                json_to_send.put("action", "error in delivery");
                                json_to_send.put("failed-emails", failed_clients);
                                this.sendMessage(json_to_send.toString());
                            }
                            break;
                        }

                    }
                }else {

                    /*
                    *
                    * if the user has disconnected for whatever reason, we remove him from our handlers
                    * */
                    clientHandlers.remove(this);
                    model.writeLog(this.username  + " has disconnected");
                    interrupt=true;
                    //Thread.currentThread().interrupt();
                }

            }catch (IOException e){

                clientHandlers.remove(this);
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createUserPrivateFile(){

        try{

            String filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/"+this.username+".json";
            String jsonFileStr = new String(
                    Files.readAllBytes(Paths
                            .get(filePathString)),
                    StandardCharsets.UTF_8);
            JSONArray jsonArray = (JSONArray) new JSONParser().parse(jsonFileStr);

            //String strPath = "PATH TO EMPLOYEE JSON";
            File strFile = new File(filePathString);
            //boolean fileCreated = strFile.createNewFile();
            Writer objWriter = new BufferedWriter(new FileWriter(strFile));
            objWriter.write(jsonArray.toString());
            objWriter.flush();
            objWriter.close();
            //File user_file = new File(filePathString);
            // String usr_inbox = new String(Files.readAllBytes(Paths.get(filePathString)));

            //JSONArray jsonarray = new JSONArray(usr_inbox); //from the file
            // JSONParser parser = new JSONParser();
            //JSONObject jsonObj = (JSONObject) parser.parse(usr_inbox);
            //JSONObject jsonobject = new JSONObject(yourNewlyJsonObject);
            //jsonarray.put(jsonobject);

            // JSONArray root = new JSONArray();
            /*JSONObject obj = new JSONObject();
            obj.put("submitted","");
            obj.put("limit", 0);
            obj.put("ID", 123);
            obj.put("target", 3);*/

            //root.add(email);
            //Files.write(Paths.get(filePathString), root.toString().getBytes(), StandardOpenOption.APPEND);
        }catch (Exception e){}
    }

    private void updateUserEmails(String receiver, JSONObject email){

        try{

            String filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/"+receiver+".json";
            String jsonFileStr = new String(
                    Files.readAllBytes(Paths
                            .get(filePathString)),
                    StandardCharsets.UTF_8);
            JSONArray jsonArray = (JSONArray) new JSONParser().parse(jsonFileStr);
            jsonArray.add(email);

            //String strPath = "PATH TO EMPLOYEE JSON";
            File strFile = new File(filePathString);
            boolean fileCreated = strFile.createNewFile();
            Writer objWriter = new BufferedWriter(new FileWriter(strFile));
            objWriter.write(jsonArray.toString());
            objWriter.flush();
            objWriter.close();
            //File user_file = new File(filePathString);
           // String usr_inbox = new String(Files.readAllBytes(Paths.get(filePathString)));

            //JSONArray jsonarray = new JSONArray(usr_inbox); //from the file
           // JSONParser parser = new JSONParser();
            //JSONObject jsonObj = (JSONObject) parser.parse(usr_inbox);
            //JSONObject jsonobject = new JSONObject(yourNewlyJsonObject);
            //jsonarray.put(jsonobject);

           // JSONArray root = new JSONArray();
            /*JSONObject obj = new JSONObject();
            obj.put("submitted","");
            obj.put("limit", 0);
            obj.put("ID", 123);
            obj.put("target", 3);*/

            //root.add(email);
            //Files.write(Paths.get(filePathString), root.toString().getBytes(), StandardOpenOption.APPEND);
        }catch (Exception e){}
    }



    private Boolean searchClient(String user_email){

        for (ServerModel.Client client : ServerModel.users){

            if (client.email.equals(user_email)){

                return true;
            }
        }
        return false;
    }
    private ClientHandler searchClientSocket(String user_email){

        for (ClientHandler client : clientHandlers){

            /*
            * if the receiver has his socket open
            * he must be among our users
            * otherwise it means he is not online
            * */
            if (client.username.equals(user_email)){

                return client;
            }
        }
        return null;
    }







}