package model;

import javafx.application.Platform;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Future;

class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    Future<?> future;

    public ClientHandler(Socket socket) {

        try {

            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {

            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    private void addClient(String username, ClientHandler new_client) {

        for (ClientHandler client : clientHandlers) {

            if (client.username.equals(username)) {

                client.socket = this.socket;
                future.cancel(true);
                return;
            }
        }
        clientHandlers.add(new_client);
    }

    private void saveClientToFile(String user_email) {

        for (ServerModel.Client client : ServerModel.users) {

            if (client.email.equals(user_email)) {

                /*
                 * if the client was already present in the file we get out of the function
                 * */
                return;
            }
        }
        try {

            String filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/client_list.json";
            String jsonFileStr = Files.readString(Paths
                    .get(filePathString));
            JSONArray jsonArray = (JSONArray) new JSONParser().parse(jsonFileStr);
            JSONObject new_user = new JSONObject();
            new_user.putIfAbsent("name", "test");
            new_user.putIfAbsent("surname", "test");
            new_user.putIfAbsent("email", user_email);

            ServerModel.users.add(new ServerModel.Client("test", "test", user_email, "random"));
            jsonArray.add(new_user);

            File strFile = new File(filePathString);
            Writer objWriter = new BufferedWriter(new FileWriter(strFile));
            objWriter.write(jsonArray.toString());
            objWriter.flush();
            objWriter.close();
        } catch (Exception ignored) {
        }
    }

    public void sendMessage(String message) {

        try {
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedWriter.write(message);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (IOException e) {

            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

        try {

            if (bufferedReader != null) {

                bufferedReader.close();
            }
            if (bufferedWriter != null) {

                bufferedWriter.close();
            }
            if (socket != null) {

                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void displayUsers() {


        for (ClientHandler clientHandler : clientHandlers) {

            System.out.println("Client: " + clientHandler.username);
        }
        System.out.println("------------------------");
    }

    public void run() {

        String messageFromClient;
        try {

            messageFromClient = bufferedReader.readLine();

            if (messageFromClient != null) {

                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(messageFromClient);
                switch ((String) json.get("action")) {

                    case "login": {
                        if (this.username == null) this.username = (String) json.get("user");
                        Platform.runLater(() -> ServerModel.writeLog(username + " has connected"));
                        if (this.username == null) break;

                        addClient(username, this);
                        saveClientToFile(this.username);
                        String filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/" + this.username + ".json";
                        File user_file = new File(filePathString);
                        if (!user_file.createNewFile()) {

                            /*
                             * The user already existed, so we send him back his inbox
                             * otherwise we create a new empty json file
                             * */
                            JSONArray usr_inbox = (JSONArray) parser.parse(new FileReader(filePathString));
                            //String usr_inbox = new String(Files.readAllBytes(Paths.get(filePathString)));
                            JSONObject json_to_send = new JSONObject();
                            json_to_send.put("action", "inbox");
                            json_to_send.put("emails", usr_inbox);
                            this.sendMessage(json_to_send.toString());
                        } else {

                            String path = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/" + this.username + ".json";
                            createUserPrivateFile(path);
                        }

                        /*
                         * creating the user cache
                         * */
                        filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/cache/" + this.username + ".json";
                        createUserPrivateFile(filePathString);
                        break;
                    }

                    case "send email": {

                        String message = (String) json.get("text");
                        String object = (String) json.get("object");
                        String receiver = (String) json.get("receiver");
                        String sender = (String) json.get("sender");
                        String[] receivers = receiver.split(" ");
                        /*
                         * if we successfully sent the email to all users,
                         * the function is going to return an empty string
                         * otherwise it's going to return a string containing the
                         * list of failed emails
                         * */
                        String failed_clients = sendEmail(receivers, message, object, receiver, sender);
                        /*
                         *
                         * if the sending function returns failed email
                         * we handle it by notifying the sender
                         * */
                        if (!failed_clients.isEmpty()) {
                            /*
                             *
                             * we notify both the server log and the user
                             * */
                            ServerModel.writeLog(sender + " failed to send email to: " + failed_clients);
                            JSONObject json_to_send = new JSONObject();
                            json_to_send.put("action", "error in delivery");
                            json_to_send.put("failed-emails", failed_clients);
                            this.sendMessage(json_to_send.toString());
                        }
                        break;
                    }

                    case "reconnect": {


                        if (this.username == null) this.username = (String) json.get("user");
                        //Platform.runLater(() -> ServerModel.writeLog(username + " has connected"));
                        if (this.username == null) break;
                        addClient(username, this);
                        String filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/cache/" + this.username + ".json";
                        JSONArray usr_inbox = (JSONArray) parser.parse(new FileReader(filePathString));
                        /*
                         * if it's an empty file
                         * it means the user has no new emails,
                         * and we send nothing
                         * */
                        if (usr_inbox.toString().equals("[]")) break;
                        JSONObject json_to_send = new JSONObject();
                        json_to_send.put("action", "receiving email");
                        json_to_send.put("emails", usr_inbox);
                        this.sendMessage(json_to_send.toJSONString());
                        Platform.runLater(() -> ServerModel.writeLog(username + " has received new emails"));
                        /*we empty the file*/
                        emptyFile(filePathString);
                        break;
                    }
                    case "closing": {

                        //System.out.println("inside closing case");
                        this.username = (String) json.get("user");
                        Platform.runLater(() -> ServerModel.writeLog(username + " has disconnected"));
                        break;
                    }

                }
            }

        } catch (IOException ignored) {
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private String sendEmail(String[] receivers, String message, String object, String receiver, String sender) {

        String failed_clients = "";
        for (String client : receivers) {

            if (searchClient(client)) {
                /*
                 * if we have found the client
                 * we proceed to send the email
                 * */

                JSONObject json_to_send = new JSONObject();
                JSONObject email = new JSONObject();
                json_to_send.put("action", "receiving email");

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

                /*
                 * storing in the main file
                 * */
                String filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/" + client + ".json";
                this.updateUserEmails(email, filePathString);

                /*
                 * Storing in the cache file
                 * */
                filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/cache/" + client + ".json";
                this.updateUserEmails(email, filePathString);

                /*
                 * we retrieve the client socket if he is online
                 * */
                ServerModel.writeLog(sender + " has sent an email to: " + client);
                json_to_send = new JSONObject();
                json_to_send.put("action", "confirmed delivery");
                this.sendMessage(json_to_send.toString());
            } else {

                /*
                 * we save the wrong emails and report them to the log and the user
                 * */
                failed_clients += client + " - ";
            }
        }

        return failed_clients;
    }

    private void emptyFile(String path) {

        try {

            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write("[]");
            fileWriter.close();
        } catch (IOException ignored) {}

    }

    private void createUserPrivateFile(String filepath) {

        try {

            FileWriter myWriter = new FileWriter(filepath);
            myWriter.write("[]");
            myWriter.close();
        } catch (Exception ignored) {
        }
    }

    private void updateUserEmails(JSONObject email, String filepath) {

        try {
            String jsonFileStr = Files.readString(Paths
                    .get(filepath));
            JSONArray jsonArray = (JSONArray) new JSONParser().parse(jsonFileStr);
            jsonArray.add(email);
            File strFile = new File(filepath);
            Writer objWriter = new BufferedWriter(new FileWriter(strFile));
            objWriter.write(jsonArray.toString());
            objWriter.flush();
            objWriter.close();
        } catch (Exception ignored) {
        }
    }
    private Boolean searchClient(String user_email) {

        for (ServerModel.Client client : ServerModel.users) {

            if (client.email.equals(user_email)) {

                return true;
            }
        }
        return false;
    }
}