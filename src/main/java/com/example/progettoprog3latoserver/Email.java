package com.example.progettoprog3latoserver;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
 * The Email class is used by istances of the messages of the emails
 *
 *
 * @author Marvel Asuenimhen
 * @version 1.0
 *
 * */
public class Email {

    private String ID;
    private String sender;
    private String receiver_;
    private String object_;
    private String text_;
    private String date;

    public Email(){}
    public Email(String ID, String sender, String receiver, String text, String object, String date){

        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        //LocalDateTime now = LocalDateTime.now();
        //dtf.format(now)
        this.date = date;
        this.sender = sender;
        this.receiver_ = receiver;
        this.text_ = text;
        this.object_ = object;
        this.ID = ID;

    }
    public static boolean isCorrectEmailFormat(String emails){


        if (emails.isEmpty()) return false;
        String[] email_list = emails.split(" ");
        for (String s : email_list) {

            String regex = "^(.+)@(\\S+)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(s);
            if (!matcher.matches()) return false;
        }
        return true;
    }


    /*
     * We will make a call to the server
     * which will give us back our inbox list as a JSON file
     * @return the JSON array containing our Inbox List
     * */
    public ArrayList<Email> getUserEmails(String User) throws IOException, ParseException {

        String src = "/Users/marvel/Programming/Uni/ProgettoProg3/src/main/java/com/example/progettoprog3/MOCK_DATA.json";
        ArrayList<Email> emails = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();

        for (Object o : (JSONArray) jsonParser.parse(new FileReader(src))) {

            JSONObject rootObj = (JSONObject) o;
            //

            String ID = (String) rootObj.get("ID");
            String sender = (String) rootObj.get("sender");
            String receiver = (String) rootObj.get("receiver");
            String text = (String) rootObj.get("text");
            String object = (String) rootObj.get("object");
            String date = (String) rootObj.get("date");
            //
            emails.add(new Email(ID, sender,receiver,text, object, date));
        }
        return emails;
    }

    public Email readNewEmail(String  emailID) {

        Email email = null;
        try {

            String src = "/Users/marvel/Programming/Uni/ProgettoProg3/src/main/java/com/example/progettoprog3/MOCK_DATA.json";
            JSONParser json = new JSONParser();

            JSONArray jsonArray = (JSONArray) json.parse(new FileReader(src));

            for (Object o : (JSONArray) json.parse(new FileReader(src))) {

                JSONObject jsonObject = (JSONObject) o;
                if (emailID.equalsIgnoreCase(jsonObject.get("ID").toString())){

                    String sender = (String) jsonObject.get("sender");
                    String receiver = (String) jsonObject.get("receiver");
                    String text = (String) jsonObject.get("text");
                    String object = (String) jsonObject.get("object");
                    String date = (String) jsonObject.get("date");

                    email = new Email(emailID,sender,receiver,text,object,date);
                }
                //
                //
            }
            return email;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDate() {
        return date;
    }

    public String getObject_() {
        return object_;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver_() {
        return receiver_;
    }

    public String getText_() {
        return text_;
    }

    /*
     * @param receiverEmail  we use the receiver's email to identify him
     * @param text the email's text
     * @param argument the email's argument
     *
     * this function is used to send an email, if the search function throws an error,
     * we are going to catch it,
     * and we will notify the sender that there was an error
     * */

    public void reply(Email email, String reply){

        String text = email.getText_() + reply;
    }
    public void sendEmail(String text, String argument, String... receiverEmail){

        if (search(receiverEmail)){

            /*
             * if the email is present then we proceed to send it, otherwise we
             * notify the user with a graphic error
             * */
        }
    }

    /*
     * @param receiver the object of the receiver
     * @return boolean true if the receiver is found
     * if the receiver is not found the server is going to throw an error
     * -----------------------------------------------------------------
     * we are going to ask the server to
     * look in its "database"(wich is going to be a json file),
     * if the sender is among the list of its avalible user
     * */
    public boolean search(String[] receiverEmail){

        return true;
    }
}