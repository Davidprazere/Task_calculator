import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Model
class CalculatorModel {
    private String expression;
    private double result;

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void calculate() {
        try {
            // Заменяем '//' на 'Math.floor()' для целочисленного деления
            expression = expression.replaceAll("//", "Math.floor(1.0*");
            // Добавляем закрывающую скобку после каждого Math.floor
            expression = expression.replaceAll("Math\\.floor\\(1\\.0\\*(\\d+)/(\\d+)", "Math.floor(1.0*$1/$2)");
            // Заменяем '^' на 'Math.pow'
            expression = expression.replaceAll("(\\d+)\\^(\\d+)", "Math.pow($1,$2)");
            result = eval(expression);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public double getResult() {
        return result;
    }

    // Простой эвалуатор выражений
    private double eval(String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else {
                        x = parseFactor();
                    }

                    if (func.equals("Math.pow")) x = Math.pow(x, parseFactor());
                    else if (func.equals("Math.floor")) x = Math.floor(x);
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                return x;
            }
        }.parse();
    }
}

// View
class CalculatorView {
    private final Scanner scanner = new Scanner(System.in);

    public String getInput() {
        System.out.print("Введите математическое выражение: ");
        return scanner.nextLine();
    }

    public void displayResult(double result) {
        System.out.println("Результат: " + result);
    }

    public void displayError(String message) {
        System.out.println("Ошибка: " + message);
    }
}

// Controller
class CalculatorController {
    private final CalculatorModel model;
    private final CalculatorView view;

    public CalculatorController(CalculatorModel model, CalculatorView view) {
        this.model = model;
        this.view = view;
    }

    public void run() {
        while (true) {
            String expression = view.getInput();
            if (expression.equalsIgnoreCase("выход")) {
                break;
            }
            if (validateExpression(expression)) {
                model.setExpression(expression);
                model.calculate();
                view.displayResult(model.getResult());
            } else {
                view.displayError("Некорректное выражение");
            }
        }
    }

    private boolean validateExpression(String expression) {
        // Проверка на начало и конец числом
        if (!expression.matches("^-?\\d.*\\d+$")) {
            return false;
        }

        // Проверка на количество операций
        Pattern pattern = Pattern.compile("[-+*/^]|//");
        Matcher matcher = pattern.matcher(expression);
        int count = 0;
        while (matcher.find()) {
            count++;
            if (count > 99) {
                return false;
            }
        }

        return true;
    }
}

//Main
public class Calculator {
    public static void main(String[] args) {
        CalculatorModel model = new CalculatorModel();
        CalculatorView view = new CalculatorView();
        CalculatorController controller = new CalculatorController(model, view);
        controller.run();
    }
}
