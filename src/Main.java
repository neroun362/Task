import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static double dollarToRublesRate;
    private static double rublesToDollarRate;

    private static void performArithmeticOperation(Scanner scanner, String operation) {
        System.out.print("Введите первое значение: ");
        String input = scanner.nextLine();
        double amount = parseCurrencyValue(input);

        System.out.print("Введите второе значение: ");
        String secondInput = scanner.nextLine();
        double secondAmount = parseCurrencyValue(secondInput);

        if (input.startsWith("$") && secondInput.startsWith("$")) {
            double result = performOperation(amount, operation, secondAmount);
            System.out.println("Результат: $" + roundToTwoDecimalPlaces(result));
        } else if (input.endsWith("р") && secondInput.endsWith("р")) {
            double result = performOperation(amount, operation, secondAmount);
            System.out.println("Результат: " + roundToTwoDecimalPlaces(result) + "р");
        } else {
            System.out.println("Некорректные символы валюты. Оба значения должны быть в одной валюте.");
        }
    }

    private static double parseCurrencyValue(String input) {
        input = input.replace(",", ".");
        if (input.startsWith("$")) {
            String valueString = input.replaceAll("[^0-9.]", "");
            return Double.parseDouble(valueString);
        } else if (input.endsWith("р")) {
            String valueString = input.replaceAll("[^0-9.]", "");
            return Double.parseDouble(valueString);
        } else {
            throw new IllegalArgumentException("Некорректный символ валюты.");
        }
    }

    private static double performOperation(double operand1, String operator, double operand2) {
        if (operator.equals("+")) {
            return operand1 + operand2;
        } else {
            return operand1 - operand2;
        }
    }

    private static double convertToRubles(double dollars) {
        return dollars * dollarToRublesRate;
    }

    private static double convertToDollars(double rubles) {
        return rubles / dollarToRublesRate;
    }

    private static void evaluateAndPrintExpressionResult(String operation) {
        try {
            double result = evaluateExpression(operation);
            if (operation.startsWith("toDollars(")) {
                System.out.println("Результат: $" + formatCurrency(result));
            } else if (operation.startsWith("toRubles(")) {
                System.out.println("Результат: " + formatCurrency(result) + "р");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static double evaluateExpression(String expression) {
        expression = expression.trim();
        expression = expression.replace(",", ".");


        if (expression.startsWith("toDollars(")) {
            String innerExpression = expression.substring(10, expression.length() - 1).trim();
            double result = evaluateExpression(innerExpression);
            if (innerExpression.startsWith("$")) {
                throw new IllegalArgumentException("Некорректная операция. Нельзя переводить доллары в доллары.");
            }
            return convertToDollars(result);
        } else if (expression.startsWith("toRubles(")) {
            String innerExpression = expression.substring(9, expression.length() - 1).trim();
            double result = evaluateExpression(innerExpression);
            if (innerExpression.endsWith("р")) {
                throw new IllegalArgumentException("Некорректная операция. Нельзя переводить рубли в рубли.");
            }
            return convertToRubles(result);
        } else if (expression.contains("+") || expression.contains("-")) {
            return evaluateArithmeticExpression(expression);
        } else if (expression.startsWith("$")) {
            String valueString = expression.substring(1);
            return Double.parseDouble(valueString);
        } else if (expression.endsWith("р")) {
            String valueString = expression.substring(0, expression.length() - 1);
            return Double.parseDouble(valueString);
        } else {
            throw new IllegalArgumentException("Некорректный символ валюты или выражение.");
        }
    }

    private static double evaluateArithmeticExpression(String expression) {
        expression = expression.trim();

        int operatorIndex = -1;
        int nestedExpressionCount = 0;

        for (int i = expression.length() - 1; i >= 0; i--) {
            char currentChar = expression.charAt(i);

            if (currentChar == ')') {
                nestedExpressionCount++;
            } else if (currentChar == '(') {
                nestedExpressionCount--;
            } else if ((currentChar == '+' || currentChar == '-') && nestedExpressionCount == 0) {
                operatorIndex = i;
                break;
            }
        }

        if (operatorIndex != -1) {
            String leftOperandString = expression.substring(0, operatorIndex).trim();
            String rightOperandString = expression.substring(operatorIndex + 1).trim();

            double leftOperand = evaluateExpression(leftOperandString);
            double rightOperand = evaluateExpression(rightOperandString);

            if (leftOperandString.startsWith("$") && rightOperandString.endsWith("р")) {
                rightOperand = convertToDollars(rightOperand);
            } else if (leftOperandString.endsWith("р") && rightOperandString.startsWith("$")) {
                rightOperand = convertToRubles(rightOperand);
            }

            return performOperation(leftOperand, String.valueOf(expression.charAt(operatorIndex)), rightOperand);
        }

        return evaluateExpression(expression);
    }


    private static double roundToTwoDecimalPlaces(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static String formatCurrency(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return String.format("%.2f", bd);
    }

    private static void loadExchangeRates() {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties.txt")) {
            properties.load(fis);
            dollarToRublesRate = Double.parseDouble(properties.getProperty("dollarToRublesRate"));
            rublesToDollarRate = Double.parseDouble(properties.getProperty("rublesToDollarRate"));
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке файла конфигурации.");
            System.exit(1);
        } catch (NumberFormatException e) {
            System.out.println("Некорректный формат курсов валют в файле конфигурации.");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        loadExchangeRates();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Введите операцию (+, -, toRubles(значение) или toDollars(значение)),exit: ");
            String operation = scanner.nextLine();
            if (operation.equals("exit")) {
                break;
            }
            if (operation.equals("+") || operation.equals("-")) {
                performArithmeticOperation(scanner, operation);
            } else if (operation.startsWith("toDollars(") || operation.startsWith("toRubles(")) {
                evaluateAndPrintExpressionResult(operation);
            } else {
                System.out.println("Некорректная операция. Введите +, -, toRubles(значение) или toDollars(значение),exit.");
            }
        }
    }
}