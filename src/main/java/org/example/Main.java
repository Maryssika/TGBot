package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Замените "YOUR_BOT_TOKEN" и "YOUR_BOT_NAME" на реальные значения
            Bot bot = new Bot("7607166310:AAHqFOb3UohwNCEnKlAw_4RpAVITcv86jno", "pediatric_scales_bot");

            botsApi.registerBot(bot);
            System.out.println("Бот успешно запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.err.println("Ошибка при запуске бота: " + e.getMessage());
        }
    }
}