import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class oper{
    String opcode_address;
    String operand1;
    int LC;

    public oper(String opcode_address, String operand1, int LC) {
        this.opcode_address = opcode_address;
        this.operand1 = operand1;
        this.LC = LC;
    }
}
public class OnePassHandler {
    HashMap<String,ArrayList<Integer>> net_label = new HashMap<String,ArrayList<Integer>>();

    // key = operation  -> value = opcode
    private HashMap<String,String> opcode_address = new HashMap<String,String>();

    //operation -> no of operands it needs
    private HashMap<String,Integer> no_of_op = new HashMap<String,Integer>();

    //operation ->  can create new var or not
    private HashMap<String,Boolean> create_new_var = new HashMap<String,Boolean>();

    public boolean is_error = false;

    /*
    opcode table :
        Key -> operation name
        value =
            index :
                0 -> opcode
                1 -> opnd1
                2 -> LC value
    */
    private ArrayList<oper> opcode_table = new ArrayList<oper>();

    /*
    operands table :
        Key -> operands name
        value =
        index :
            0 -> address(default = -1)
            1 -> value(default = 0)
            2 -> LC value
    */
    private HashMap<String,ArrayList<Integer>> operands_table = new HashMap<String,ArrayList<Integer>>();


    /*
    label table :
        Key -> label name
        value =
        index :
            0 -> LC value (of declaration)
            1 onwards -> LC of labels
    */
    private HashMap<String,Integer> label_table = new HashMap<String,Integer>();

    OnePassHandler(){
        // HARD CODING ALL THE BASIC OPERATIONS SUPPORTED BY THE ASSEMBLER

        //Clear accumulator
        opcode_address.put("CLA","0000");
        no_of_op.put("CLA",0);
        create_new_var.put("CLA",false);

        //Load into accumulator from address
        opcode_address.put("LAC","0001");
        no_of_op.put("LAC",1);
        create_new_var.put("LAC",false);

        //Store accumulator contents into address
        opcode_address.put("SAC","0010");
        no_of_op.put("SAC",1);
        create_new_var.put("SAC",true);

        //Add address contents to accumulator contents
        opcode_address.put("ADD","0011");
        no_of_op.put("ADD",1);
        create_new_var.put("ADD",false);

        //Subtract address contents from accumulator contents
        opcode_address.put("SUB","0100");
        no_of_op.put("SUB",1);
        create_new_var.put("SUB",false);

        //Branch to address if accumulator contains zero
        opcode_address.put("BRZ","0101");
        no_of_op.put("BRZ",1);
        create_new_var.put("BRZ",false);

        //Branch to address if accumulator contains negative value
        opcode_address.put("BRN","0110");
        no_of_op.put("BRN",1);
        create_new_var.put("BRN",false);

        //Branch to address if accumulator contains positive value
        opcode_address.put("BRP","0111");
        no_of_op.put("BRP",1);
        create_new_var.put("BRP",false);

        //Read from terminal and put in address
        opcode_address.put("INP","1000");
        no_of_op.put("INP",1);
        create_new_var.put("INP",true);

        //Display value in address on terminal
        opcode_address.put("DSP","1001");
        no_of_op.put("DSP",1);
        create_new_var.put("DSP",false);

        //Multiply accumulator and address contents
        opcode_address.put("MUL","1010");
        no_of_op.put("MUL",1);
        create_new_var.put("MUL",false);

        //Divide accumulator contents by address content. Quotient in R1 and remainder in R2
        opcode_address.put("DIV","1011");
        no_of_op.put("DIV",1);
        create_new_var.put("DIV",false);

        //Stop execution
        opcode_address.put("STP","1100");
        no_of_op.put("STP",0);
        create_new_var.put("STP",false);

    }

    //distributes instruction into opcode label , address
    /* returns a hashmap
        key = "token","label"
        value = arraylist of tokens,arraylist of labels
     */
    public HashMap<String,ArrayList<String>> clean(String inst){
        String[] temp = (inst.trim()).split(":");

        //labels
        ArrayList<String> label = new ArrayList<String>();
        for (int i = 0; i < temp.length - 1; i++) {
            label.add(temp[i]);
        }

        // tokens
        ArrayList<String> tokens = new ArrayList<String>();
        String[] temp2 = temp[temp.length - 1].trim().split(" ");
        for (int j = 0;j < temp2.length; j++) {
            tokens.add(temp2[j]);
        }

        HashMap<String,ArrayList<String>> ret = new HashMap<String,ArrayList<String>>();
        ret.put("label",label);
        ret.put("token",tokens);

        return ret;

    }

    // checks if string is alpha numeric or not
    public boolean isalpha(String s){
        return s != null && s.matches("^[a-zA-Z0-9]*$");
    }

    //checks if a string is digit or not
    public boolean isdigit(String s){
        return s != null && s.matches("^[0-9]*$");
    }

    /* checks label for error
        params : label(string)
        returns flags:
        flag -> -1 = label name is not valid.
        flag -> 1 = unexpected use of " ".
        flag -> 2 = operand cannot be used as a label.
        flag -> 3 = operand cannot be used as a operation.
        flag -> 0 = no error found.
        flag -> -2 = label already declared.
     */
    public int check_label(String label){
        label = label.trim();
        if(opcode_address.get(label) != null) return 3;
        if(operands_table.get(label) != null) return 2;
        if(label.split(" ").length > 1) return 1;
        String[] temp = label.split("_");
        boolean flag =false;
        for (int i = 0; i < temp.length; i++) {
            if(isalpha(temp[i]) && !isdigit(temp[i])) continue;
            flag = true;
        }
        if(flag) return -1;
        if(label_table.get(label) != null) return -2;
        return 0;
    }
    /* checks operand for error
        params : opn(operand : string),op(operation : string)
        returns flags:
        flag -> -1 = operand name is not valid.
        flag -> 1 = operand not declared.
        flag -> 2 = label cannot be used as a operands.
        flag -> 3 = operation cannot be used as a operand.
        flag -> 0 = no error found.
     */
    public int check_operand(String opn,String op){
        opn = opn.trim();
        if(opcode_address.get(opn) != null) return 3;
        if(label_table.get(opn) != null) return 2;
        String[] temp = opn.split("_");
        boolean flag =false;
        for (int i = 0; i < temp.length; i++) {
            if((isalpha(temp[i]) || !create_new_var.get(op)) && !isdigit(temp[i])) continue;
            flag = true;
        }
        if(flag) return -1;
        if(!(op.equals("BRN") || op.equals("BRP") || op.equals("BRZ")) && !create_new_var.get(op) && operands_table.get(opn) == null) return 1;
        return 0;
    }
    //process operand
    public void process_operand(String opn,String op,int LC){
        if(op.equals("BRP") || op.equals("BRN") || op.equals("BRZ")){
            opcode_table.add(new oper(op,opn,LC));
            if(net_label.get(opn) == null){
                net_label.put(opn,new ArrayList<Integer>());
            }
            net_label.get(opn).add(LC);
        }
        else{
            opcode_table.add(new oper(op,opn,LC));
            if(operands_table.get(opn) == null && create_new_var.get(op)){
                operands_table.put(opn,new ArrayList<Integer>());
                operands_table.get(opn).add(-1);
                operands_table.get(opn).add(0);
                operands_table.get(opn).add(LC);
            }
        }
    }
    /* checks opcode for error
        params : label(string)
        returns flags:
        flag -> -1 = no opcode found.
        flag -> 0 = no error found.
     */
    public int check_opcode(String op){
        if(opcode_address.get(op) != null) return 0;
        else return -1;
    }

    // process label
    public void process_label(String label,int LC){
        label = label.trim();
        label_table.put(label,LC);
    }

    //ONEPASS
    // all errors are written in error.txt
    public void run(BufferedReader out) throws IOException {
        FileWriter error = new FileWriter("error.txt");
        String inst = "";
        int LC = 0;
        int line = 0;
        boolean isend = false;
        ArrayList<String> write = new ArrayList<String>();
        while(((inst = out.readLine()) != null)){
            //empty instruction
            if(inst.length() <= 2) continue;

            write.add(inst);
            line++;
            LC++;

            //STP already reached
            if(isend){
                String e = "    (error : unreachable statements = line ->  " + String.valueOf(line) + "\n";
                is_error = true;
                write.set(write.size() - 1,inst + e);
                continue;
            }

            //cleaning instruction
            HashMap<String,ArrayList<String>> data = clean(inst);

            //checking label for error
            boolean no_label = false;
            if(data.get("label").size() > 1){
                String e = "     (error : unexpected use of \":\" = line ->  " + String.valueOf(line) + "\n";
                is_error = true;
                write.set(write.size() - 1,inst + e);
                continue;
            }
            else if(data.get("label").size() == 0) no_label = true;

            //extracting label
            String label = "";
            if(!no_label)
                label = data.get("label").get(0);

            //checking label for error
            String e;
            if(!no_label) {
                boolean iserror = false;
                switch (check_label(label)) {
                    case -1:
                        e = "     (error :   " + label + " <- label name is not valid = line ->  " + String.valueOf(line) + "\n";
                        is_error = true;
                        write.set(write.size() - 1,inst + e);
                        iserror = true;
                        break;
                    case 1:
                        e = "     (error : labels cannot be space seperated   = line ->  " + String.valueOf(line) + "\n";
                        is_error = true;
                        write.set(write.size() - 1,inst + e);
                        iserror = true;
                        break;
                    case 2:
                        e = "     (error :   " +label + " <- already declared as a operand = line ->  " + String.valueOf(line) + "\n";
                        is_error = true;
                        write.set(write.size() - 1,inst + e);
                        iserror = true;
                        break;
                    case 3:
                        e = "     (error :   " +label + "  <- already declared as a operation = line ->  " + String.valueOf(line) + "\n";
                        is_error = true;
                        iserror = true;
                        write.set(write.size() - 1,inst + e);
                        break;
                    case -2:
                        e = "     (error :   " +label + " <- label already declared = line ->  " + String.valueOf(line) + "\n";
                        is_error = true;
                        iserror = true;
                        write.set(write.size() - 1,inst + e);
                        break;
                }
                if (iserror) continue;

                //processing label
                process_label(label, LC);
            }
            //extracting operation
            String op = data.get("token").get(0);

            // check for operation error
            if(check_opcode(op) == -1){
                e = "    (error : no operation with name = " + op + " found = line ->  " + String.valueOf(line) + "\n";
                write.set(write.size() - 1,inst + e);
                is_error = true;
                continue;
            }

            //process opcode
            if(no_of_op.get(op) == 1){
//                System.out.println(LC);
                String opn = data.get("token").get(1);
                boolean iserror = false;
                switch(check_operand(opn,op)){
                    case -1:
                        e = "     (error :   " +opn + " <- operand name is not valid = line ->  " + String.valueOf(line) + "\n";
                        write.set(write.size() - 1,inst + e);
                        is_error = true;
                        iserror = true;
                        break;
                    case 1:
                        e = "     (error :   " +opn + " <- operand not declared  = line ->  " + String.valueOf(line) + "\n";
                        write.set(write.size() - 1,inst + e);
                        is_error = true;
                        iserror = true;
                        break;
                    case 2:
                        e = "     (error :   " +opn + "  <- label cannot be used as a operand = line ->  " + String.valueOf(line) + "\n";
                        write.set(write.size() - 1,inst + e);
                        is_error = true;
                        iserror = true;
                        break;
                    case 3:
                        e = "     (error :   " +opn + " <- operation cannot be used as a operand = line ->  " + String.valueOf(line) + "\n";
                        is_error = true;
                        iserror = true;
                        write.set(write.size() - 1,inst + e);
                        break;
                }
                if(iserror) continue;
                if(LC > (1  << 8) - 1 && operands_table.size() >= 1){
//                    System.out.println(LC);
                    e = "     (error : code length exceeded  : too long a code for a 12 bit assembler line -> " + String.valueOf(line) + "\n";
                    write.set(write.size() - 1,inst + e);
                    is_error = true;
                    continue;
                }

                // removing comment from the instruction
                if(data.get("token").size() > 2) {
                    String comment = data.get("token").get(2);
                    String temp = String.valueOf(comment.charAt(0));

                    //checking if comment is valid or not
                    if (!temp.equals(";")) {
                        e = "     (error : too many operands given for operation " + op + " = line ->  " + String.valueOf(line) + "\n";
                        write.set(write.size() - 1,inst + e);
                        is_error = true;
                        continue;
                    }
                }

                //process operands
                if(op.equals("STP")){
                    isend = true;
                }
                process_operand(opn,op,LC);
            }
            else{
                //processing operations
                if(op.equals("STP")) isend= true;
                opcode_table.add(new oper(op,"",LC));
                if(data.get("token").size() > 1) {
                    String comment = data.get("token").get(1);
                    String temp = String.valueOf(comment.charAt(0));

                    //checking if comment is valid or not
                    if (!temp.equals(";")) {
                        e = "     (error : too many operands given for operation " + op + " = line ->  " + String.valueOf(line) + "\n";
                        is_error = true;
                        write.set(write.size() - 1,inst + e);
                        continue;
                    }
                }
            }
        }

        //check if operands donot take more than 8 bits
        if(LC > (1 << 8) - 1 && operands_table.size() >= 1){
            write.add(" (error : code length exceeded  : too long a code for a 12 bit assembler");
        }

        //check if any label was left undeclared
        for (Map.Entry mapElement : net_label.entrySet()){
            if(label_table.get(mapElement.getKey()) == null){
                ArrayList<Integer> m = net_label.get(mapElement.getKey());
                for (int i = 0; i < m.size(); i++) {
                    String s = "    (error : " + mapElement.getKey() + " <- Label not declared = line -> " + m.get(i) + "\n";
                    is_error = true;
                    write.set(m.get(i) - 1 ,write.get(m.get(i) - 1) + s);
                }
            }
        }
//        System.out.println(is_error);
        //writing errors in error.txt
//        System.out.println(LC);
//        System.out.println("Write : " + write.size());
        for (int i = 0;i < write.size();i++){
//            System.out.print(write.get(i));
            if(String.valueOf(write.get(i).charAt(write.get(i).length() - 1)).equals("\n"))
                error.write(write.get(i));
            else
                error.write(write.get(i) + "\n");
        }
        error.close();
    }

    //getters
    public HashMap<String, ArrayList<Integer>> getNet_label() {
        return net_label;
    }

    public HashMap<String, Integer> getNo_of_op() {
        return no_of_op;
    }

    public HashMap<String, Boolean> getCreate_new_var() {
        return create_new_var;
    }

    public boolean isIs_error() {
        return is_error;
    }

    public HashMap<String, String> getOpcode_address() {
        return opcode_address;
    }

    public ArrayList<oper> getOpcode_table() {
        return opcode_table;
    }

    public HashMap<String, ArrayList<Integer>> getOperands_table() {
        return operands_table;
    }

    public HashMap<String, Integer> getLabel_table() {
        return label_table;
    }

    // to check if it works write assembly in input.txt.
    public static void main(String args[]) throws IOException{
        OnePassHandler one = new OnePassHandler();
        BufferedReader out  = new BufferedReader(new FileReader("input.txt"));
        one.run(out);
    }
}
