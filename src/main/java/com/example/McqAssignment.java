package com.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import org.apache.poi.xssf.usermodel.*;

public class McqAssignment {

    public static Map<String, Question> readQuestionsFromXML(String filePath) {
        Map<String, Question> questionMap = new HashMap<>();
        Map<String, Option> optionMap = new HashMap<>();
        Map<String, String> answerMap = new HashMap<>();

        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList questionNodes = doc.getElementsByTagName("Question");
            for (int i = 0; i < questionNodes.getLength(); i++) {
                Node node = questionNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getAttribute("id");
                    String value = element.getElementsByTagName("Value").item(0).getTextContent();
                    questionMap.put(id, new Question(id, value, null, null));
                }
            }

            NodeList optionNodes = doc.getElementsByTagName("Option");
            for (int i = 0; i < optionNodes.getLength(); i++) {
                Node node = optionNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getAttribute("id");
                    String valueOne = element.getElementsByTagName("ValueOne").item(0).getTextContent();
                    String valueTwo = element.getElementsByTagName("ValueTwo").item(0).getTextContent();
                    String valueThree = element.getElementsByTagName("ValueThree").item(0).getTextContent();
                    String valueFour = element.getElementsByTagName("ValueFour").item(0).getTextContent();
                    optionMap.put(id, new Option(id, valueOne, valueTwo, valueThree, valueFour));
                }
            }

            NodeList answerNodes = doc.getElementsByTagName("Answer");
            for (int i = 0; i < answerNodes.getLength(); i++) {
                Node node = answerNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getAttribute("id");
                    String value = element.getTextContent().split("\\|")[1];
                    String correctOptionNumber = getCorrectOptionNumber(optionMap, id, value);
                    answerMap.put(id, correctOptionNumber);
                }
            }

            for (String id : questionMap.keySet()) {
                questionMap.get(id).options = optionMap.get(id);
                questionMap.get(id).answer = answerMap.get(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questionMap;
    }

    public static String getCorrectOptionNumber(Map<String, Option> optionMap, String id, String value) {
        Option option = optionMap.get(id);
        if (option.getValueOne().substring(3).equals(value)) {
            return "a";
        } else if (option.getValueTwo().substring(3).equals(value)) {
            return "b";
        } else if (option.getValueThree().substring(3).equals(value)) {
            return "c";
        } else if (option.getValueFour().substring(3).equals(value)) {
            return "d";
        }
        return null;
    }

    public static void main(String[] args) {
        String filePath = "/Users/paramjotsingh/Desktop/JavaQuestions.xml";  // Path to your XML file
        Map<String, Question> questionMap = readQuestionsFromXML(filePath);

        // Step 2: Console Input for Name, Mobile Number, and Random Number Generation
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your mobile number: ");
        String mobileNumber = scanner.nextLine();

        Random random = new Random();
        int randomId = 100000 + random.nextInt(900000);
        System.out.println("Your assessment ID is: " + randomId);

        // Step 3: Prompt to Start Assessment
        System.out.print("To start the assessment enter 'yes': ");
        String startAssessment = scanner.nextLine();
        if (!startAssessment.equalsIgnoreCase("yes")) {
            System.out.println("Exiting the assessment.");
            return;
        }

        // Step 4: Display Questions and Get User Input
        Map<String, String> userAnswers = new HashMap<>();
        for (Question question : questionMap.values()) {
            System.out.println(question.value);
            System.out.println(question.options.valueOne);
            System.out.println(question.options.valueTwo);
            System.out.println(question.options.valueThree);
            System.out.println(question.options.valueFour);
            System.out.print("Your answer: ");
            String userAnswer = scanner.nextLine();
            userAnswers.put(question.id, userAnswer);
        }

        // Step 5: Calculate Marks
        int marks = 0;
        int wrongAnswers = 0;
        for (Question question : questionMap.values()) {
            if (userAnswers.get(question.id).equalsIgnoreCase(question.answer)) {
                marks += 2;
            } else {
                wrongAnswers++;
            }
        }

        // Step 6: Apply Negative Marking
        int negativeMarks = 0;
        if (wrongAnswers >= 3 && wrongAnswers <= 5) {
            negativeMarks = -1;
        } else if (wrongAnswers >= 6 && wrongAnswers <= 8) {
            negativeMarks = -2;
        } else if (wrongAnswers >= 9 && wrongAnswers <= 10) {
            negativeMarks = -3;
        }

        int totalMarks = marks + negativeMarks;
        System.out.println("Marks: " + marks);
        System.out.println("Negative Marks: " + negativeMarks);
        System.out.println("Total Marks: " + totalMarks);

        // Step 7: Write Data to Excel Sheet
        writeDataToExcel(randomId, name, mobileNumber, marks, negativeMarks, totalMarks);
    }

    public static void writeDataToExcel(int randomId, String name, String mobileNumber, int marks, int negativeMarks, int totalMarks) {
        String excelFilePath = "/Users/paramjotsingh/Desktop/MCQResult.xlsx";
        try {
            File file = new File(excelFilePath);
            XSSFWorkbook workbook;
            XSSFSheet sheet;

            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(excelFilePath);
                workbook = new XSSFWorkbook(fileInputStream);
                sheet = workbook.getSheetAt(0);
                fileInputStream.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("MCQ Results");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("RandomId");
                header.createCell(1).setCellValue("Name");
                header.createCell(2).setCellValue("MobileNumber");
                header.createCell(3).setCellValue("Marks");
                header.createCell(4).setCellValue("Negative Marks");
                header.createCell(5).setCellValue("Total Marks");
            }

            ExcelRow rowToBeAdded = new ExcelRow(randomId, name, mobileNumber, marks, negativeMarks, totalMarks);
            boolean rowAdded = false;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                ExcelRow existingRow = new ExcelRow();
                existingRow.setId((int) row.getCell(0).getNumericCellValue());
                existingRow.setName(row.getCell(1).getStringCellValue());
                existingRow.setMobile(row.getCell(2).getStringCellValue());
                existingRow.setMarks((int) row.getCell(3).getNumericCellValue());
                existingRow.setNegativeMarks((int) row.getCell(4).getNumericCellValue());
                existingRow.setTotalMarks((int) row.getCell(5).getNumericCellValue());
                if (rowToBeAdded.compareTo(existingRow) > 0) {
                    sheet.shiftRows(i, sheet.getLastRowNum(), 1);
                    Row newRow = sheet.getRow(i);
                    newRow.createCell(0).setCellValue(randomId);
                    newRow.createCell(1).setCellValue(name);
                    newRow.createCell(2).setCellValue(mobileNumber);
                    newRow.createCell(3).setCellValue(marks);
                    newRow.createCell(4).setCellValue(negativeMarks);
                    newRow.createCell(5).setCellValue(totalMarks);
                    rowAdded = true;
                    break;
                }
            }

            if (!rowAdded) {
                int lastRow = sheet.getLastRowNum();
                Row row = sheet.createRow(lastRow + 1);
                row.createCell(0).setCellValue(randomId);
                row.createCell(1).setCellValue(name);
                row.createCell(2).setCellValue(mobileNumber);
                row.createCell(3).setCellValue(marks);
                row.createCell(4).setCellValue(negativeMarks);
                row.createCell(5).setCellValue(totalMarks);
            }

            FileOutputStream outputStream = new FileOutputStream(excelFilePath);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Question {
    String id;
    String value;
    Option options;
    String answer;

    Question(String id, String value, Option options, String answer) {
        this.id = id;
        this.value = value;
        this.options = options;
        this.answer = answer;
    }
}

@Data
class Option {
    String id;
    String valueOne;
    String valueTwo;
    String valueThree;
    String valueFour;

    Option(String id, String valueOne, String valueTwo, String valueThree, String valueFour) {
        this.id = id;
        this.valueOne = valueOne;
        this.valueTwo = valueTwo;
        this.valueThree = valueThree;
        this.valueFour = valueFour;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ExcelRow implements Comparable<ExcelRow> {
    int id;
    String name;
    String mobile;
    int marks;
    int negativeMarks;
    int totalMarks;

    @Override
    public int compareTo(ExcelRow row) {
        int totalMarksComparison = Integer.compare(this.getTotalMarks(), row.getTotalMarks());
        if (totalMarksComparison != 0) {
            return totalMarksComparison;
        }
        return Integer.compare(this.negativeMarks, row.getNegativeMarks());
    }
}
