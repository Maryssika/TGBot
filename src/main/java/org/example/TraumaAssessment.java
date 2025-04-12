package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraumaAssessment {
    private final List<Question> questions;
    private final Map<String, String> currentAnswers;
    private int totalScore;
    private int currentQuestionIndex;
    private String currentQuestionId;

    public TraumaAssessment() {
        this.questions = new ArrayList<>();
        this.currentAnswers = new HashMap<>();
        this.totalScore = 0;
        this.currentQuestionIndex = 0;
        loadQuestionsFromFile();
    }

    private void loadQuestionsFromFile() {
        File questionsFile = new File("questions.txt");
        if (!questionsFile.exists()) {
            throw new RuntimeException("Файл questions.txt не найден. Создайте его в корневой директории проекта.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(questionsFile))) {
            String line;
            Question currentQuestion = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("[Q]")) {
                    if (currentQuestion != null) {
                        questions.add(currentQuestion);
                    }
                    currentQuestion = new Question();
                    currentQuestion.id = line.substring(3).trim();
                } else if (line.startsWith("[T]")) {
                    if (currentQuestion != null) {
                        currentQuestion.text = line.substring(3).trim();
                    }
                } else if (line.startsWith("[O]")) {
                    if (currentQuestion != null) {
                        String[] parts = line.substring(3).split(":", 2);
                        if (parts.length == 2) {
                            // Удаляем все пробелы перед числовым значением
                            String valueStr = parts[1].trim().replaceAll("\\s+", "");
                            try {
                                int value = Integer.parseInt(valueStr);
                                currentQuestion.options.put(parts[0].trim(), value);
                            } catch (NumberFormatException e) {
                                System.err.println("Ошибка парсинга значения для варианта: " + line);
                            }
                        }
                    }
                }
            }
            if (currentQuestion != null) {
                questions.add(currentQuestion);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить вопросы из файла", e);
        }
    }
    public String getFirstQuestion() {
        if (questions.isEmpty()) {
            return "Нет доступных вопросов для оценки.";
        }
        currentQuestionIndex = 0;
        currentQuestionId = questions.get(0).id;
        return formatQuestion(questions.get(0));
    }

    public String processAnswer(String answer) {
        if (currentQuestionId == null || questions.isEmpty()) {
            return "Начните оценку с команды /assess";
        }

        try {
            int optionNumber = Integer.parseInt(answer.trim());
            Question currentQuestion = questions.get(currentQuestionIndex);

            if (optionNumber < 1 || optionNumber > currentQuestion.options.size()) {
                return "Некорректный номер ответа. Пожалуйста, выберите от 1 до " + currentQuestion.options.size() +
                        "\n\n" + formatQuestion(currentQuestion);
            }

            String selectedOption = new ArrayList<>(currentQuestion.options.keySet()).get(optionNumber - 1);
            int score = currentQuestion.options.get(selectedOption);
            totalScore += score;

            currentAnswers.put(currentQuestion.id, selectedOption + " (" + score + " баллов)");

            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                currentQuestionId = questions.get(currentQuestionIndex).id;
                return "Следующий вопрос:\n" + formatQuestion(questions.get(currentQuestionIndex));
            } else {
                return finishAssessment();
            }
        } catch (NumberFormatException e) {
            return "Пожалуйста, введите номер ответа (1, 2, 3...)\n\n" +
                    formatQuestion(questions.get(currentQuestionIndex));
        }
    }

    private String formatQuestion(Question question) {
        StringBuilder sb = new StringBuilder();
        sb.append(currentQuestionIndex + 1).append(". ").append(question.text).append("\n");

        int optionNum = 1;
        for (String option : question.options.keySet()) {
            sb.append(optionNum++).append(") ").append(option).append("\n");
        }

        return sb.toString();
    }

    private String finishAssessment() {
        try {
            saveResultsToFile();
        } catch (IOException e) {
            System.err.println("Ошибка сохранения результатов: " + e.getMessage());
        }

        String diagnosis = getDiagnosis();
        StringBuilder result = new StringBuilder();
        result.append("Оценка завершена. Результаты:\n\n");

        for (Question q : questions) {
            String answer = currentAnswers.get(q.id);
            if (answer != null) {
                result.append(q.text).append(": ").append(answer).append("\n");
            }
        }

        result.append("\nОбщий балл: ").append(totalScore).append("\n");
        result.append("Диагноз: ").append(diagnosis);

        resetAssessment();
        return result.toString();
    }

    private String getDiagnosis() {
        if (totalScore >= 9 && totalScore <= 12) {
            return "легкая травма";
        } else if (totalScore >= 6 && totalScore <= 8) {
            return "потенциальная угроза жизни";
        } else if (totalScore >= 0 && totalScore <= 5) {
            return "опасное для жизни состояние";
        } else {
            return "фатальная ситуация";
        }
    }

    private void saveResultsToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("answers.txt", true))) {
            writer.write("=== Результаты оценки травмы ===\n");
            writer.write("Время: " + new java.util.Date() + "\n");

            for (Question q : questions) {
                String answer = currentAnswers.get(q.id);
                if (answer != null) {
                    writer.write(q.text + ": " + answer + "\n");
                }
            }

            writer.write("Общий балл: " + totalScore + "\n");
            writer.write("Диагноз: " + getDiagnosis() + "\n\n");
        }
    }

    private void resetAssessment() {
        currentQuestionId = null;
        currentQuestionIndex = 0;
        currentAnswers.clear();
        totalScore = 0;
    }

    private static class Question {
        String id;
        String text;
        Map<String, Integer> options = new HashMap<>();
    }
}