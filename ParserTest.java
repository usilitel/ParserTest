package ru.lesson.lessons.ParserTest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParserTest {

    String strFileText=""; // строка, в которую копируется содержимое исходного файла
    List<NodeTest> nodesList = new ArrayList<NodeTest>(); // массив с узлами
    int currentStrPosition=0; // текущая позиция курсора
    int currentNodeId=0; // id текущего элемента
    int currentNodeParentId=0; // parentid текущего элемента
    NodeTest currentNode;



    public static void main(String[] args){
         new ParserTest(args[0], args[1]);
    }


    ParserTest(String inputFile, String outputFile){
        processInputFile(inputFile); // записываем все узлы из исходного файла в массив
        writeNodesToTextFile(outputFile); // выводим все узлы в текстовый файл
    }





    // записываем все узлы из исходного файла в массив
    public void processInputFile(String textFile) {
        readTextFile(textFile); // теперь содержание исходного файла записано в строку strFileText
        //System.out.println(strFileText);

        while((currentStrPosition < strFileText.length()) && (!isFileEnd())){
            addNode();
        }
    }



    // переводим текстовый файл в строковую переменную
    public void readTextFile(String textFile) {

        try{
            FileInputStream fstream = new FileInputStream(textFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;

            while ((strLine = br.readLine()) != null){
                strFileText+=strLine + "\n";
            }
        }catch (IOException e){
            System.out.println("Ошибка при доступе к файлу " + textFile);
            System.exit(0);
        }
    }





    public boolean isFileEnd() { // если дошли до конца файла - возвращаем true
        int EndPosition;
        EndPosition = strFileText.indexOf("=", currentStrPosition);

        if (EndPosition >= 0){
            return false;
        }
        else{
            return true;
        }
    }


    // добавляем элемент в массив
    public void addNode() {
        if (!isListEnd()){
            currentNode = new NodeTest();
            nodesList.add(currentNode);
            //printNode();
        }
    }


    // обрабатываем конец списка
    public boolean isListEnd() {
        int EndPosition, EndListPosition;
        EndPosition = strFileText.indexOf("=", currentStrPosition);
        EndListPosition = strFileText.indexOf("}", currentStrPosition);
        if(EndListPosition < EndPosition){ // конец списка
            currentStrPosition = EndListPosition + 1;

            if(currentNode.nodeType==0){ // здесь currentNode - последний добавленный элемент
                currentNodeParentId = nodesList.get(nodesList.get(currentNodeId-1).nodeParentId).nodeParentId; // вычисляем currentNodeParentId (берем nodeParentId с более высокого уровня)
            }
            else{
                currentNodeParentId = nodesList.get(currentNodeId-1).nodeParentId;
            }

            //System.out.println("currentNodeParentId = " + currentNodeParentId + "; nodeType = " + currentNode.nodeType + "; ");
            return true;
        }
        else {return false;}
    }


    // записываем массив в файл
    public void writeNodesToTextFile(String outputFile) {
        try{
            FileWriter writer = new FileWriter(outputFile, false);
            writer.write("");
            for(NodeTest nodeTemp: nodesList){
                if(nodeTemp.nodeType==1){ // список
                    writer.append(Integer.toString(nodeTemp.nodeId) + ", " + Integer.toString(nodeTemp.nodeParentId) + ", " + nodeTemp.getNodeName() + ", " + "{}" + '\n'); // для списка выводим: id узла, id вышестоящего узла, имя узла, "{}"
                }
                else{ // значение
                    writer.append(Integer.toString(nodeTemp.nodeId) + ", " + Integer.toString(nodeTemp.nodeParentId) + ", " + nodeTemp.getNodeName() + ", " + nodeTemp.getNodeValue() + '\n'); // для значения выводим: id узла, id вышестоящего узла, имя узла, значение в узле
                }
            }
            writer.flush();
            System.out.println("запись в файл " + outputFile + " завершена");
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }









//----------------------------------

    // класс - элемент (узел или список)
    class NodeTest{

        private int nodeId; // id узла
        private int nodeParentId; // id узла-родителя
        private String nodeName; // имя узла
        private int nodeType; // тип узла: 0 - значение, 1 - список
        private String nodeValue; // значение узла (для списка = "")


        public int getNodeId() {
            return nodeId;
        }

        public void setNodeId(int nodeId) {
            this.nodeId = nodeId;
        }

        public int getNodeParentId() {
            return nodeParentId;
        }

        public void setNodeParentId(int nodeParentId) {
            this.nodeParentId = nodeParentId;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public int getNodeType() {
            return nodeType;
        }

        public void setNodeType(int nodeType) {
            this.nodeType = nodeType;
        }

        public String getNodeValue() {
            return nodeValue;
        }

        public void setNodeValue(String nodeValue) {
            this.nodeValue = nodeValue;
        }




        public NodeTest(){
            this.setNodeId(currentNodeId);
            this.setNodeName(getNodeNameFromString());
            this.setNodeType(getNodeTypeFromString());
            this.setNodeParentId(currentNodeParentId);


            if (this.getNodeType()==0){ // значение
                this.setNodeValue(getNodeValueFromString());
            }
            else{ // список
                this.setNodeValue("");
                currentNodeParentId = this.getNodeId(); // потом у элементов списка будем проставлять этот ParentId
            }

            if(!isCorrectNodeName(this.getNodeName())){
                System.out.println("Неверный формат данных: некорректрое имя:(" + this.getNodeName() + ")");
                System.exit(0);
            }

            if(this.getNodeType()==0 && !isCorrectNodeValue(this.getNodeValue())){
                System.out.println("Неверный формат данных: некорректрое значение:(" + this.getNodeValue() + ")");
                System.exit(0);
            }

            currentNodeId++;
        }




        // получаем nodeName из строки
        public String getNodeNameFromString() {
            int EndPosition;
            EndPosition = strFileText.indexOf("=", currentStrPosition);
            return strFileText.substring(currentStrPosition, EndPosition).trim();
        }

        // вычисляем nodeType из строки
        public int getNodeTypeFromString() {
            int beginListPosition = strFileText.indexOf("{", currentStrPosition);
            if ((beginListPosition >= 0) && (beginListPosition < (strFileText.indexOf("\"", currentStrPosition)))) { // список
                currentStrPosition = beginListPosition + 1;
                return 1;
            }
            else { // значение
                return 0;
            }
        }

        // получаем nodeValue из строки
        public String getNodeValueFromString() {
            int StartPosition, EndPosition;

            StartPosition = strFileText.indexOf("\"", currentStrPosition);
            EndPosition = strFileText.indexOf("\"", StartPosition+1);

            currentStrPosition = EndPosition + 1;

            return strFileText.substring(StartPosition+1, EndPosition);
        }




        // проверяем строку по маске с использованием регулярных выражений (https://habrahabr.ru/post/267205/)
        public boolean isCorrectString(String stringPattern, String stringToCompare){
            Pattern pattern = Pattern.compile(stringPattern);
            Matcher matcher = pattern.matcher(stringToCompare);
            return matcher.matches();
        }

        public boolean isCorrectNodeName(String nodeName){
            return isCorrectString("[A-Za-zА-Яа-я\\_]{1}[A-Za-zА-Яа-я0-9\\_]{0,}", nodeName); // имя_узла – строка из букв, цифр, и символа '_', начинающаяся не с цифры
        }

        public boolean isCorrectNodeValue(String nodeValue){
            return (nodeValue.indexOf("\n")==-1)?true:false; // значение_узла – произвольная строка в двойных кавычках, не содержащая символов перевода строки и двойных кавычек
        }
    }

}
