import java.io.*;
import java.util.*;

public class HackAssembler {

    //分解命令参数
    public static String[] parse(String input) {
        if (input.charAt(0) == '@') {
            String value = input.split("@")[1];
            return new String[]{value};
        } else {
            String comp = "";
            String dest = "";
            String jump = "";
            if (input.contains("=")) {
                String[] split = input.split("=");
                dest = split[0];
                input = split[1];
            } else {
                dest = "null";

            }
            if (input.contains(";")) {
                String[] split = input.split(";");
                comp = split[0];
                jump = split[1];
            } else {
                comp = input;
                jump = "null";
            }
            //System.out.println(comp + " " + jump + " " + dest);
            return new String[]{comp, dest, jump};
        }
    }

    //A命令
    public static String codeA(String[] command) {
        int num = Integer.parseInt(command[0]);
        String binaryNum = String.format("%16s", Integer.toBinaryString(num)).replace(' ', '0');
        return binaryNum;
    }

    //C命令
    public static String codeC(String[] command) {
        //映射表
        Map<String, String> destMap = Map.ofEntries(
                Map.entry("null", "000"), Map.entry("M", "001"), Map.entry("D", "010"), Map.entry("MD", "011"),
                Map.entry("A", "100"), Map.entry("AM", "101"), Map.entry("AD", "110"), Map.entry("AMD", "111")
        );

        Map<String, String> compMap = Map.ofEntries(
                Map.entry("0", "0101010"), Map.entry("1", "1111111"), Map.entry("-1", "0111010"), Map.entry("D", "0001100"),
                Map.entry("A", "0110000"), Map.entry("!D", "0001101"), Map.entry("!A", "0110001"), Map.entry("-D", "0001111"),
                Map.entry("-A", "0110011"), Map.entry("D+1", "0011111"), Map.entry("A+1", "0110111"), Map.entry("D-1", "0001110"),
                Map.entry("A-1", "0110010"), Map.entry("D+A", "0000010"), Map.entry("D-A", "0010011"), Map.entry("A-D", "0000111"),
                Map.entry("D&A", "0000000"), Map.entry("D|A", "0010101"), Map.entry("M", "1110000"), Map.entry("!M", "1110001"),
                Map.entry("-M", "1110011"), Map.entry("M+1", "1110111"), Map.entry("M-1", "1110010"), Map.entry("D+M", "1000010"),
                Map.entry("D-M", "1010011"), Map.entry("M-D", "1000111"), Map.entry("D&M", "1000000"), Map.entry("D|M", "1010101")
        );

        Map<String, String> jumpMap = Map.ofEntries(
                Map.entry("null", "000"), Map.entry("JGT", "001"), Map.entry("JEQ", "010"), Map.entry("JGE", "011"),
                Map.entry("JLT", "100"), Map.entry("JNE", "101"), Map.entry("JLE", "110"), Map.entry("JMP", "111")
        );

        String binaryCode = "111" + compMap.get(command[0]) + destMap.get(command[1]) + jumpMap.get(command[2]);
        return binaryCode;
    }

    public static String asmToBinary(String input) {
        //分解命令参数
        String[] command = parse(input);
        if (command == null)
            return null;

        //翻译命令
        if (command.length == 1) {
            return codeA(command);
        } else {
            return codeC(command);
        }
    }

    public static void doLabels(String[] lines, SymbolTable symbolTable) {
        int nowAddress = 0;
        for (String line : lines) {
            //去除空行及注释及Label
            if (line.isEmpty() || line.split("//")[0].trim().isEmpty())
                continue;
            line = line.trim();//去除空格
            if (line.startsWith("(") && line.endsWith(")")) {
                String label = line.substring(1, line.length() - 1);
                symbolTable.addSymbol(label, nowAddress);
            } else
                nowAddress++;
        }
    }

    public static void doVariables(String[] lines, SymbolTable symbolTable) {
        int nextAddress = 16;
        for (int i = 0; i < lines.length; i++) {
            //去除空行及注释
            if (lines[i].isEmpty() || lines[i].split("//")[0].trim().isEmpty())
                continue;
            lines[i] = lines[i].trim();//去除空格
            if (lines[i].startsWith("(") && lines[i].endsWith(")"))
                continue;
            if (lines[i].startsWith("@")) {
                String symbol = lines[i].substring(1);
                if (!symbol.matches("\\d+")) {
                    if (!symbolTable.containsSymbol(symbol))
                        symbolTable.addSymbol(symbol, nextAddress++);
                    lines[i] = "@" + symbolTable.getAddress(symbol);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //判断是否有asm文件参数
        if (args.length != 1) {
            System.out.println("Usage: java HackAssembler <input.asm>");
            System.exit(1);
        }

        //创建hack文件与缓冲流
        String inFile = args[0];
        String outFile = inFile.replace(".asm", ".hack");
        BufferedReader reader = new BufferedReader(new FileReader(inFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        //初始化符号表
        SymbolTable symbolTable = new SymbolTable();

        //获取asm文件内容
        String[] lines = reader.lines().toArray(String[]::new);
        reader.close();

        //第一遍扫描处理Label
        doLabels(lines, symbolTable);
//        for (String line : lines) {
//            System.out.println(line);
//        }
//        System.out.println("999999999999999999999999999999999999");
        //第二遍扫描处理variable
        doVariables(lines, symbolTable);
//        for (String line : lines) {
//            System.out.println(line);
//        }

        //翻译指令
        for (String line : lines) {
            //去除空行及注释
            if (line.isEmpty() || line.split("//")[0].trim().isEmpty() || line.charAt(0) == '(')
                continue;
            line = line.trim();//去除空格
            String binaryCode = asmToBinary(line);
            if (binaryCode != null) {
                writer.write(binaryCode);
                writer.newLine();
            }
        }

        //关闭资源
        reader.close();
        writer.close();
        System.out.println("complete the hack file: " + outFile);
    }
}

class SymbolTable {
    public final Map<String, Integer> symbolTable;

    public SymbolTable() {
        symbolTable = new HashMap<>();
        for (int i = 0; i <= 15; i++) {
            symbolTable.put("R" + i, i);
        }
        symbolTable.put("SCREEN", 16384);
        symbolTable.put("KBD", 24576);
        symbolTable.put("SP", 0);
        symbolTable.put("LCL", 1);
        symbolTable.put("ARG", 2);
        symbolTable.put("THIS", 3);
        symbolTable.put("THAT", 4);
    }

    //添加符号
    public void addSymbol(String symbol, int address) {
        symbolTable.put(symbol, address);
    }

    //判断符号是否存在
    public boolean containsSymbol(String symbol) {
        return symbolTable.containsKey(symbol);
    }

    //获取符号对应地址
    public int getAddress(String symbol) {
        return symbolTable.get(symbol);
    }
}