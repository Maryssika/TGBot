package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            // Загружаем переменные окружения
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            String botToken = dotenv.get("TELEGRAM_BOT_TOKEN");
            String botName = dotenv.get("TELEGRAM_BOT_NAME");

            if (botToken == null || botName == null) {
                throw new IllegalStateException("Не найдены переменные окружения для бота");
            }

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            Bot bot = new Bot(botToken, botName);

            botsApi.registerBot(bot);
            System.out.println("Бот успешно запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.err.println("Ошибка при запуске бота: " + e.getMessage());
        }
    }
}