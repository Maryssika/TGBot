package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {
    private final String BOT_TOKEN;
    private final String BOT_NAME;
    private final TraumaAssessment traumaAssessment;
    private boolean isAssessmentInProgress;

    public Bot(String botToken, String botName) {
        this.BOT_TOKEN = botToken;
        this.BOT_NAME = botName;
        this.traumaAssessment = new TraumaAssessment();
        this.isAssessmentInProgress = false;
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        String text = message.getText().trim();
        String response;

        if (text.equals("/start")) {
            response = "Добро пожаловать в бот для оценки педиатрической травмы!\n\n"
                    + "Используйте /help для списка команд";
        }
        else if (text.equals("/help")) {
            response = "Доступные команды:\n\n"
                    + "/start - Начать работу с ботом\n"
                    + "/assess - Начать оценку травмы\n"
                    + "/help - Эта справка";
        }
        else if (text.equals("/assess")) {
            isAssessmentInProgress = true;
            response = traumaAssessment.getFirstQuestion();
        }
        else if (isAssessmentInProgress) {
            if (text.matches("\\d+")) {
                response = traumaAssessment.processAnswer(text);
                if (response.contains("Оценка завершена")) {
                    isAssessmentInProgress = false;
                }
            } else {
                response = "Пожалуйста, вводите только номер ответа (1, 2, 3...)";
            }
        }
        else {
            response = "Неизвестная команда. Введите /help для справки";
        }

        sendResponse(chatId, response);
    }

    private void sendResponse(String chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        execute(message);
    }
}