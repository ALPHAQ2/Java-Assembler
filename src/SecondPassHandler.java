import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SecondPassHandler {
    public static OnePassHandler firstpass = new OnePassHandler();
    public static ArrayList<oper> opcodes = firstpass.getOpcode_table();
    public static HashMap<String, String> op = firstpass.getOpcode_address();
    public static HashMap<String,Integer> no_of_op = firstpass.getNo_of_op();
    public static HashMap<String,ArrayList<Integer>> net_label = firstpass.getNet_label();
    public static HashMap<String,Boolean> create_new_var = firstpass.getCreate_new_var();
    public static HashMap<String, ArrayList<Integer>> Operands_table = firstpass.getOperands_table();
    public static HashMap<String, Integer> Label_table = firstpass.getLabel_table();
    public static boolean galti  = firstpass.is_error;


    public static void main(String[] args) throws IOException {
        BufferedReader out = new BufferedReader(new FileReader("sample_input.txt"));
        PrintWriter fout = null;
        firstpass.run(out);
        if(firstpass.is_error) return;
        System.out.println(firstpass.is_error);
        opcodes = firstpass.getOpcode_table();
        out = new BufferedReader(new FileReader("sample_input.txt"));
        String cline = "";
        int count = 0;
        int lc = 0;
        try {
            fout = new PrintWriter("output.txt");
            if (!galti) {
                while ((cline = out.readLine()) != null) {
                    String[] linec = cline.trim().split(" ");
                    if (op.containsKey(linec[0])) {
                        fout.print("" + String.format("%4s", Integer.toBinaryString(count++)).replace(' ', '0') + "");
//                System.out.print(linec[0] + " ");
                        fout.print(op.get(linec[0]));
//                System.out.print(no_of_op.get(linec[0]));
                        if (no_of_op.get(linec[0]) > 0) {
                            for (int i = 0; i < opcodes.size(); i++) {
                                if (lc + 1 == opcodes.get(i).LC) {
                                    if (Operands_table.get(opcodes.get(i).operand1) != null) {
//                                System.out.print(opcodes.get(i).operand1);
                                        fout.print("" + String.format("%8s", Integer.toBinaryString(Operands_table.get(opcodes.get(i).operand1).get(2))).replace(' ', '0') + "\n");
                                    }
                                }
                            }
                            if (Label_table.containsKey(linec[1])) {
                                fout.print("" + String.format("%8s", Integer.toBinaryString(Label_table.get(linec[1]))).replace(' ', '0') + "\n");
                            }
                        } else if (no_of_op.get(linec[0]) == 0) {
                            fout.print("00000000" + "\n");
                        } else {
                            fout.print("\n");
                        }
//                System.out.println();
                    }
                    lc++;
                }
                fout.print("" + String.format("%4s", Integer.toBinaryString(count++)).replace(' ', '0') + "" + op.get("STP") + "00000000");
            } else {
                fout.print("");
            }
        }
        finally{
            fout.close();
        }

    }
}
