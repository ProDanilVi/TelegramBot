import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import java.io.IOException;

public class Bot extends TelegramLongPollingBot {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());
        }
        catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);

        try {
            new SetButtons(sendMessage);
            sendMessage(sendMessage);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Model model = new Model();
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            switch (message.getText()) {
                case "/start":
                    sendMsg(message, MessageText.START_TEXT);
                    break;
                case "/help":
                    sendMsg(message, MessageText.HELP_TEXT);
                    break;
                case "/setting":
                    sendMsg(message, MessageText.SETTING_TEXT);
                    break;
                case "Погода":
                    sendMsg(message, MessageText.WEATHER_TEXT);
                    break;
                case "/photo":
                    sendMsg(message, MessageText.PHOTO_TEXT);
                    break;
                case "/resultphoto":
                    String caption = "Image Detection Finished";
                    long chat_id = update.getMessage().getChatId();
                    SendPhoto msg = new SendPhoto()
                            .setChatId(chat_id)
                            .setPhoto("2_out.jpg")
                            .setCaption(caption);
                    try {
                        sendPhoto(msg);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                default:
                    try {
                        sendMsg(message, Weather.getWeather(message.getText(), model));
                    }
                    catch (IOException e) {
                        sendMsg(message, MessageText.ERROR_TEXT);
                    }
            }
        }

        else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            String imgFile = "images/1.jpg";
            Mat src = Imgcodecs.imread(imgFile);

            String xmlFile = "xml/lbpcascade_frontalface.xml";
            CascadeClassifier cc = new CascadeClassifier(xmlFile);

            MatOfRect faceDetection = new MatOfRect();
            cc.detectMultiScale(src, faceDetection);
            System.out.println(String.format("Detected faces: %d", faceDetection.toArray().length));

            for(Rect rect: faceDetection.toArray()) {
                Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 3);
            }

            Imgcodecs.imwrite("images/1_out.jpg", src);
            System.out.println("Image Detection Finished");
        }
    }

    public String getBotUsername() {
        return "ProDanilVi_TelegramBot";
    }

    public String getBotToken() {
        return "5639402347:AAH1bcrc7gymOGROeGvaLVZ1Cu8YKS9InvQ";
    }
}