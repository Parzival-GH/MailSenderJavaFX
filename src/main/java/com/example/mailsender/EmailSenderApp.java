package com.example.mailsender;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Properties;

public class EmailSenderApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        TextField emailField = new TextField();
        emailField.setPromptText("Введите адрес электронной почты");

        TextField chatIdField = new TextField();
        chatIdField.setPromptText("Введите chat_id Telegram");

        TextField subjectField = new TextField();
        subjectField.setPromptText("Введите тему");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Введите сообщение");

        Button sendButton = new Button("Отправить");
        Button attachButton = new Button("Прикрепить");
        FileChooser fileChooser = new FileChooser();

        CheckBox emailCheckBox = new CheckBox("Отправить на почту");
        CheckBox telegramCheckBox = new CheckBox("Отправить в Telegram");
        emailCheckBox.setSelected(true);
        telegramCheckBox.setSelected(false);

        final File[] attachment = {null};

        attachButton.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                attachment[0] = selectedFile;
                attachButton.setText("Файл: " + selectedFile.getName());
            }
        });

        sendButton.setOnAction(e -> {
            String email = emailField.getText();
            String subject = subjectField.getText();
            String message = messageArea.getText();
            String chatId = chatIdField.getText();

            if (emailCheckBox.isSelected()) {
                sendEmail(email, subject, message, attachment[0]);
            }

            if (telegramCheckBox.isSelected()) {
                sendTelegramMessage(chatId, message, attachment[0]);
            }

            if (!emailCheckBox.isSelected() && !telegramCheckBox.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Предупреждение");
                alert.setHeaderText("Не выбран ни один метод отправки");
                alert.setContentText("Пожалуйста, выберите хотя бы один метод отправки.");
                alert.showAndWait();
            }
        });

        Image image = new Image("https://cdn-icons-png.freepik.com/512/9916/9916040.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        imageView.setTranslateX(470);
        imageView.setTranslateY(5);

        VBox layout = new VBox(15,
                imageView,
                emailField,
                chatIdField,
                subjectField,
                messageArea,
                attachButton,
                emailCheckBox,
                telegramCheckBox,
                sendButton);
        Scene scene = new Scene(layout, 1000, 500);

        primaryStage.setTitle("Email&Telegram Sender");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void sendEmail(String to,
                           String subject,
                           String body,
                           File attachment) {
        String from = "bugucievb@gmail.com";
        final String username = "bugucievb@gmail.com";
        final String password = "ffelamptqbnrdkmr";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);
            multipart.addBodyPart(textPart);

            if (attachment != null) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachment);
                multipart.addBodyPart(attachmentPart);
            }

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Письмо отправлено успешно!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendTelegramMessage(String chatId, String message, File attachment) {
        String token = "6849590370:AAGKWXj3y8PVBkiePkuEqSGJ_rrwIDCUTMk";
        String urlString;
        String charset = "UTF-8";

        if (attachment != null) {
            urlString = "https://api.telegram.org/bot" + token + "/sendDocument";
        } else {
            urlString = "https://api.telegram.org/bot" + token + "/sendMessage";
        }
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            if (attachment != null) {
                String boundary = Long.toHexString(System.currentTimeMillis());
                con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                String CRLF = "\r\n";
                charset = "UTF-8";

                try (
                        OutputStream output = con.getOutputStream();
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true)
                ) {
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"chat_id\"").append(CRLF);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                    writer.append(CRLF).append(chatId).append(CRLF).flush();

                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"caption\"").append(CRLF);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                    writer.append(CRLF).append(message).append(CRLF).flush();

                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"document\"; filename=\"" + attachment.getName() + "\"").append(CRLF);
                    writer.append("Content-Type: application/octet-stream").append(CRLF);
                    writer.append(CRLF).flush();
                    Files.copy(attachment.toPath(), output);
                    output.flush();
                    writer.append(CRLF).flush();

                    writer.append("--" + boundary + "--").append(CRLF).flush();
                }
            } else {
                String urlParameters = "chat_id=" + chatId + "&text=" + URLEncoder.encode(message, "UTF-8");
                con.getOutputStream().write(urlParameters.getBytes(charset));
            }

            int responseCode = con.getResponseCode();
            System.out.println("Письмо отправлено успешно !");
            System.out.println("Response Code : " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
