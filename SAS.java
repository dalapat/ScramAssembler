package csfhomework3;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Class to simulate Scram assembler.
 * @author Anish
 *
 */
final class SAS {
    
    /**
     * Count the total number of lines including comments and blank.
     */
    static int totalLines = 0;
    
    /**
     * Maximum memory value allowed. For out-of-range error purposes. 
     */
    static final int MAX_MEM = 16;
    
    /**
     * Max integer value for address.
     */
    static final int MAX_NUM = 16;
    
    /**
     * bit shift val.
     */
    
    static final int SHIFT = 4;
    
    /**
     * binary address for DAT.
     */
    static final short DAT = 0b0000;
    /**
     * binary address for LDA.
     */
    static final short LDA = 0b0001;
    /**
     * binary address for LDI.
     */
    static final short LDI = 0b0010;
    /**
     * binary address for STA.
     */
    static final short STA = 0b0011;
    /**
     * binary address for STI.
     */
    static final short STI = 0b0100;
    /**
     * binary address for ADD.
     */
    static final short ADD = 0b0101;
    /**
     * binary address for SUB.
     */
    static final short SUB = 0b0110;
    /**
     * binary address for JMP.
     */
    static final short JMP = 0b0111;
    /**
     * binary address for JMZ.
     */
    static final short JMZ = 0b1000;
    
    
    /**
     * Private constructor for checkstyle purposes.
     */
    private SAS() {
        
    }

    /**
     * Main method to assemble program.
     * @param args
             system arguments.
     * @throws IOException
             ioexception.
     */
    public static void main(String[] args) throws IOException {

        Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
        File f = new File(args[0]);
        ArrayList<String> al = readFile(f, ht);
        ArrayList<String> assembly = parseList(al, ht);

        byte[] d = convert(assembly);
        FileOutputStream fos = new FileOutputStream("loop.scram");
        fos.write(d);
        fos.close();
    }

    /**
     * Read the .s file, calculate label values, 
     * parse file for assembly instruction.
     * @param f
             .s file to input.
     * @param ht
             empty hashtable to store label values.
     * @return
             array list of assembly code with labels attached.
     * @throws IOException
             io exception.
     */
    public static ArrayList<String> readFile(File f,
            Hashtable<String, Integer> ht) throws IOException {
        ArrayList<String> al = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = "";
        int ctr = 0;
        while ((line = br.readLine()) != null) {
            totalLines++;
            if (ctr >= MAX_MEM) {
                System.err.print("Label out of range. Line "
                        + Integer.toString(totalLines)
                        + " in file. Program exiting.");
                System.exit(0);
            }
            if (!line.startsWith("#") && line.length() != 0) {
                String line2 = line.trim();
                String[] splitter = line2.split(":");
                if (splitter.length > 1) {
                    if (!ht.containsKey(splitter[0].trim())) {
                        ht.put(splitter[0].trim(), ctr);
                    } else {
                        System.err.print("Repeated label. Line "
                                + Integer.toString(totalLines)
                                + " in file. Program exiting.");
                        System.exit(0);
                    }

                    al.add(splitter[1].trim());
                } else if (!line2.contains(":") 
                        && crosscheckCmds(line2.split("\\s+")[0])) {

                    al.add(splitter[0].trim());
                } else {
                    System.err.print("Empty label exception. ");
                    System.err.print("Line " + totalLines + " in file.");
                    System.err.print("System exiting.");
                    System.exit(0);
                }
                ctr++;

            }
        }
        br.close();
        return al;
    }
    
    /**
     * Check if a line is a trash value by cross checking it 
     * with the possible commands.
     * @param s
             line to check.
     * @return
             true if line contains an actual command.
             false otherwise.
     */
    public static boolean crosscheckCmds(String s) {
        String[] possibleCmds = 
            {"LDA", "LDI", "STA", "STI", "ADD", "SUB", "JMP", "JMZ", "DAT"};
        
        for (String i: possibleCmds) {
            if (i.equals(s)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Parse the list of assembly instruction and replace
     * label values with their corresponding counter values
     * from the hashtable.
     * @param al
             array list that contains assembly instruction.
     * @param ht
             hashtable that contains labels and their corresponding count value.
     * @return
             array list of assembly instructions with actual addresses (loop.z)
     */
    public static ArrayList<String> parseList(ArrayList<String> al,
            Hashtable<String, Integer> ht) {
        ArrayList<String> output = new ArrayList<String>();
        
        for (int i = 0; i < al.size(); i++) {
            String[] split = al.get(i).split("\\s+");
            int var = 0;
            if (ht.containsKey(split[1].trim())) {
                var = ht.get(split[1].trim());
            } else {
                try {
                    var = Integer.parseInt(split[1].trim());
                } catch (NumberFormatException n) {
                    System.err.print("Label \"" + split[1].trim()
                            + "\" was never defined. System exiting.");
                    System.exit(0);
                }
                
            }
            
            if (split[0].trim().equals("DAT")) {
                output.add(split[0] + "     " + split[1]);
            } else {
                output.add(split[0] + "     " + Integer.toString(var));
            }
        }

        return output;
    }

    /**
     * Convert assembly instruction to byte array for output.
     * @param code
             list that contains assembly instruction (loop.z)
     * @return
             byte array of corresponding assembly instruction ready for output.
     * @throws IOException
             io exception.
     */
    public static byte[] convert(ArrayList<String> code) throws IOException {
        Hashtable<String, Short> conv = new Hashtable<String, Short>();
        conv.put("LDA", (short) LDA);
        conv.put("LDI", (short) LDI);
        conv.put("STA", (short) STA);
        conv.put("STI", (short) STI);
        conv.put("ADD", (short) ADD);
        conv.put("SUB", (short) SUB);
        conv.put("JMP", (short) JMP);
        conv.put("JMZ", (short) JMZ);
        conv.put("DAT", (short) DAT);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String s : code) {
            String[] splitter = s.split("\\s+");
            short bytecode = conv.get(splitter[0]);
            if (Integer.parseInt(splitter[1]) >= MAX_NUM) {
                System.out.println("Address \"" + splitter[1]
                        + "\" is too large. System exiting.");
                System.exit(0);
            }
            byte b = (byte) ((byte) bytecode << SHIFT | (byte) (Integer
                    .parseInt(splitter[1])));
            baos.write(b);
        }
        byte[] c = baos.toByteArray();
        
        return c;
    }

}
