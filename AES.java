import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Scanner;

public class AES {

    private static HashMap<String, String> s_box;
    private static HashMap<String, String> reverse_s_box;
    static final String[] rcon = {
            "00000001", "00000010", "00000100", "00001000", "00010000", "00100000", "01000000", "10000000",
            "00011011", "00110110", "01101100", "11011000", "10101011", "01001101", "10011010", "00101111",
            "01011110", "10111100", "01100011", "11000110", "10010111", "00110101", "01101010", "11010100",
            "10110011", "01111101", "11111010", "11101111", "11000101", "10010001", "00111001", "01110010",
            "11100100", "11010011", "10111101", "01100001", "11000010", "10011111", "00100101", "01001010",
            "10010100", "00110011", "01100110", "11001100", "10000011", "00011101", "00111010", "01110100",
            "11101000", "11001011", "10001101"};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        initializeSBox("./s_box.txt");
//        System.out.println(reduce("1000110111110000", "100011011"));
        String input = scanner.nextLine();
        String key = scanner.nextLine();
        int mode = Integer.parseInt(scanner.nextLine());
        String enDe = scanner.nextLine();
        if (enDe.toLowerCase().equals("encrypt"))
            System.out.println(aesEncrypt(input, key, mode));
        else
            System.out.println(aesDecrypt(input, key, mode));

    }

    public static String reduce(String input, String base) {

        String temp = input;
        while (temp.indexOf("1") != -1 && temp.length() - temp.indexOf("1") >= base.length()) {

            temp = xor(temp.substring(temp.indexOf("1"), temp.indexOf("1") +base.length()), base) + temp.substring(temp.indexOf("1") +base.length(), temp.length());
        }

        if (temp.length() < 8) {
            while (temp.length() < 8) {
                temp = "0" + temp;
            }
        } else {
            temp = temp.substring(temp.length() - 8, temp.length());
        }
        return temp;
    }

    public static String multiply(String x, String y) {
        char[] chars = new char[x.length() + y.length() - 1];
        for (int i = 0; i < chars.length; i++) chars[i] = '0';
        for (int i = 0; i < y.length(); i++) {
            if (y.charAt(i) == '1') {
                for (int j = 0; j < x.length(); j++) chars[i + j] += (x.charAt(j) - '0');
            }
        }
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char)((chars[i] - '0')%2 + '0');
        }
        String returned = new String(chars);
        return removeLeadingZero(returned);
    }

    private static String hexToBin(String s) {

        String returned =  new BigInteger(s, 16).toString(2);
        StringBuilder sb = new StringBuilder();
        if (returned.length() < s.length()*4) {
            for (int i = 0; i < s.length()*4 - returned.length(); i++) {
                sb.append("0");
            }
        }
        sb.append(returned);
        return sb.toString();
    }

    private static String bintoHex(String s) {

        Long decimal = Long.parseLong(s,2);
        String hexStr = Long.toString(decimal,16);
        StringBuilder sb = new StringBuilder();
        if (hexStr.length() < 2) {
            sb.append("0");
        }
        sb.append(hexStr);
        return sb.toString();
    }

    public static String removeLeadingZero(String input) {
        String returned = input;
        int timer = returned.length() - 1;
        for (int i = 0; i < timer; i++) {
            if (returned.charAt(0) == '0') returned = returned.replaceFirst("0", "");
            else if (returned.charAt(0) == '1') break;
        }
        return returned;
    }

    public static String[] division(String divisor, String divided, String value) {

//        if (divided == "0") {
//            String[] list = {value, divided};
//            return list;
//        }
        if (divided.length() < divisor.length()) {
            if (divided.equals("")) divided = "0";
            String[] list = {value, divided};
            return list;
        }
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            while (divided.charAt(0) == '0' && divided.length() >= divisor.length() && !divided.equals("0")) {
                sb.append("0");
                divided = divided.substring(1, divided.length());
            }
            String new_value = sb.toString();
            if (divided.length() < divisor.length()) {
                String[] list = {new_value, divided};
                return list;
            }
            StringBuilder next = new StringBuilder();
            next.append(xor(divided.substring(0, divisor.length()), divisor).replaceFirst("0", ""));
            next.append(divided.substring(divisor.length(), divided.length()));
            sb.append("1");
            new_value = sb.toString();
            return division(divisor, next.toString(), new_value);
        }
    }

    private static boolean bitOf(char in) {
        return (in == '1');
    }

    private static char charOf(boolean in) {
        return (in) ? '1' : '0';
    }

    /**
     * helper function to xor
     * @param left
     * @param right
     * @return
     */
    private static String xor(String left, String right) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < left.length(); i++) {
            sb.append(charOf(bitOf(left.charAt(i)) ^ bitOf(right.charAt(i))));
        }

        return sb.toString();
    }


    /**
     * xor the key and the text
     */
    public static String addRoundKey(String state, String roundkey) {

        String new_state = hexToBin(state);
        String new_key = hexToBin(roundkey);
        String converted =  xor(new_state, new_key);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < converted.length()/8; i ++) {
            sb.append(bintoHex(converted.substring(i * 8, i * 8 + 8)));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * shift column byte by byte
     * each byte is 2 hex values
     * @param state is a 16 byte hex representation value
     */
    public static String shiftRows(String state) {

        char[] chars = state.toCharArray();
        char[] temps = new char[chars.length];
        temps[0] = chars[0];
        temps[2] = chars[10];
        temps[4] = chars[20];
        temps[6] = chars[30];
        temps[1] = chars[1];
        temps[3] = chars[11];
        temps[5] = chars[21];
        temps[7] = chars[31];


        temps[8] = chars[8];
        temps[10] = chars[18];
        temps[12] = chars[28];
        temps[14] = chars[6];
        temps[9] = chars[9];
        temps[11] = chars[19];
        temps[13] = chars[29];
        temps[15] = chars[7];

        temps[16] = chars[16];
        temps[18] = chars[26];
        temps[20] = chars[4];
        temps[22] = chars[14];
        temps[17] = chars[17];
        temps[19] = chars[27];
        temps[21] = chars[5];
        temps[23] = chars[15];

        temps[24] = chars[24];
        temps[26] = chars[2];
        temps[28] = chars[12];
        temps[30] = chars[22];
        temps[25] = chars[25];
        temps[27] = chars[3];
        temps[29] = chars[13];
        temps[31] = chars[23];

        return new String(temps);
    }

    /**
     * shift column byte by byte
     * each byte is 2 hex values
     * @param state is a 16 byte hex representation value
     */
    public static String reverseShiftRows(String state) {

        char[] chars = state.toCharArray();
        char[] temps = new char[chars.length];
        temps[0] = chars[0];
        temps[10] = chars[2];
        temps[20] = chars[4];
        temps[30] = chars[6];
        temps[1] = chars[1];
        temps[11] = chars[3];
        temps[21] = chars[5];
        temps[31] = chars[7];


        temps[8] = chars[8];
        temps[18] = chars[10];
        temps[28] = chars[12];
        temps[6] = chars[14];
        temps[9] = chars[9];
        temps[19] = chars[11];
        temps[29] = chars[13];
        temps[7] = chars[15];

        temps[16] = chars[16];
        temps[26] = chars[18];
        temps[4] = chars[20];
        temps[14] = chars[22];
        temps[17] = chars[17];
        temps[27] = chars[19];
        temps[5] = chars[21];
        temps[15] = chars[23];

        temps[24] = chars[24];
        temps[2] = chars[26];
        temps[12] = chars[28];
        temps[22] = chars[30];
        temps[25] = chars[25];
        temps[3] = chars[27];
        temps[13] = chars[29];
        temps[23] = chars[31];

        return new String(temps);
    }

    /**
     *
     * @param input is the input bytes
     * since I padded 0 at the beginning for matching length
     * so i will remove the first 0
     */
    private static String shiftByte(String input, int index) {

        if (index == 2) {

            return input.replaceFirst("0", "") + "0";
        }
        else {
            return xor(input.replaceFirst("0", "") + "0", input);
        }
    }

    /**
     *
     * @param input is the input bytes
     * since I padded 0 at the beginning for matching length
     * so i will remove the first 0
     */
    private static String shiftLargerBytes(String input, String index) {

        String temp = multiply(input, hexToBin(index));
        while (temp.length() < 12)
            temp = "0" + temp;
        return temp;
    }



    /**
     *
     * @param state is hex representation of text
     * @return mixed String
     */
    public static String mixcolumn(String state) {
        String value = hexToBin(state);
        String[] matrix = new String[16];
        String[] new_matrix = new String[16];

        //Convert binary representation of state to matrix form, then pad one 0 for easier computation later
        for (int i = 0; i < 16; i++) {
            matrix[i] = "0" + value.substring(i*8, i*8+8);
        }

        for (int i = 0; i < 4; i++) {
            //first row
            String first = shiftByte(matrix[i * 4], 2);
            String second = shiftByte(matrix[i * 4 + 1], 3);
            new_matrix[i * 4] = xor(xor(xor(first, second), matrix[i * 4 + 2]), matrix[i * 4 + 3]);
            if (new_matrix[i * 4].charAt(0) == '1')
                new_matrix[i * 4] = xor(new_matrix[i * 4], "100011011").replaceFirst("0", "");
            else
                new_matrix[i * 4] = new_matrix[i * 4].replaceFirst("0", "");

            //second row
            second = shiftByte(matrix[i * 4 + 1], 2);
            String third = shiftByte(matrix[i * 4 + 2], 3);
            new_matrix[i * 4 + 1] = xor(xor(xor(second, third), matrix[i * 4]), matrix[i * 4 + 3]);
            if (new_matrix[i * 4 + 1].charAt(0) == '1')
                new_matrix[i * 4 + 1] = xor(new_matrix[i * 4 + 1], "100011011").replaceFirst("0", "");
            else
                new_matrix[i * 4 + 1] = new_matrix[i * 4 + 1].replaceFirst("0", "");

            //third row
            third = shiftByte(matrix[i * 4 + 2], 2);
            String forth = shiftByte(matrix[i * 4 + 3], 3);
            new_matrix[i * 4 + 2] = xor(xor(xor(third, forth), matrix[i * 4]), matrix[i * 4 + 1]);
            if (new_matrix[i * 4 + 2].charAt(0) == '1')
                new_matrix[i * 4 + 2] = xor(new_matrix[i * 4 + 2], "100011011").replaceFirst("0", "");
            else
                new_matrix[i * 4 + 2] = new_matrix[i * 4 + 2].replaceFirst("0", "");

            //forth row
            first = shiftByte(matrix[i * 4], 3);
            forth = shiftByte(matrix[i * 4 + 3], 2);
            new_matrix[i * 4 + 3] = xor(xor(xor(first, forth), matrix[i * 4 + 1]), matrix[i * 4 + 2]);
            if (new_matrix[i * 4 + 3].charAt(0) == '1')
                new_matrix[i * 4 + 3] = xor(new_matrix[i * 4 + 3], "100011011").replaceFirst("0", "");
            else
                new_matrix[i * 4 + 3] = new_matrix[i * 4 + 3].replaceFirst("0", "");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++)
            sb.append(bintoHex(new_matrix[i]));
        return sb.toString().toUpperCase();
    }

    /**
     *
     * @param state is hex representation of text
     * @return mixed String
     */
    public static String reverseMixcolumn(String state) {
        String value = hexToBin(state);
        String[] matrix = new String[16];
        String[] new_matrix = new String[16];

        //Convert binary representation of state to matrix form, then pad one 0 for easier computation later
        for (int i = 0; i < 16; i++) {
            matrix[i] = "0000" + value.substring(i*8, i*8+8);
        }

        for (int i = 0; i < 4; i++) {
            //first row
            String first = shiftLargerBytes(matrix[i * 4], "0E");
            String second = shiftLargerBytes(matrix[i * 4 + 1], "0B");
            String third = shiftLargerBytes(matrix[i * 4 + 2], "0D");
            String forth = shiftLargerBytes(matrix[i * 4 + 3], "09");
            new_matrix[i * 4] = reduce(xor(xor(xor(first, second), third), forth), "100011011");

            //second row
            first = shiftLargerBytes(matrix[i * 4], "09");
            second = shiftLargerBytes(matrix[i * 4 + 1], "0E");
            third = shiftLargerBytes(matrix[i * 4 + 2], "0B");
            forth = shiftLargerBytes(matrix[i * 4 + 3], "0D");
            new_matrix[i * 4 + 1] = reduce(xor(xor(xor(first, second), third), forth), "100011011");


            //third row
            first = shiftLargerBytes(matrix[i * 4], "0D");
            second = shiftLargerBytes(matrix[i * 4 + 1], "09");
            third = shiftLargerBytes(matrix[i * 4 + 2], "0E");
            forth = shiftLargerBytes(matrix[i * 4 + 3], "0B");
            new_matrix[i * 4 + 2] = reduce(xor(xor(xor(first, second), third), forth), "100011011");


            //forth row
            first = shiftLargerBytes(matrix[i * 4], "0B");
            second = shiftLargerBytes(matrix[i * 4 + 1], "0D");
            third = shiftLargerBytes(matrix[i * 4 + 2], "09");
            forth = shiftLargerBytes(matrix[i * 4 + 3], "0E");
            new_matrix[i * 4 + 3] = reduce(xor(xor(xor(first, second), third), forth), "100011011");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++)
            sb.append(bintoHex(new_matrix[i]));
        return sb.toString().toUpperCase();
    }

    /**
     * substitute byte in the plain text with a sbox
     * @param input is the input String array for all the keys
     * @return a string array of substituted Strings
     */
    public static String subByte(String input) {

        StringBuilder newList = new StringBuilder();

        for (int i = 0; i < input.length()/2; i++) {
            newList.append(s_box.get(input.substring(i*2, i*2+2)));
        }

        return newList.toString();
    }

    /**
     * substitute byte in the plain text with a reverse_sbox
     * @param input is the input String array for all the keys
     * @return a string array of substituted Strings
     */
    public static String reverseSubByte(String input) {

        StringBuilder newList = new StringBuilder();

        for (int i = 0; i < input.length()/2; i++) {
            newList.append(reverse_s_box.get(input.substring(i*2, i*2+2)));
        }

        return newList.toString();
    }

    /**
     * key expansion core for three different key length encryption
     * @param input is the input 4 bytes string
     * @return  a new string
     */
    public static String keyExpansionCore(String input, int counter) {

        //starting to rotate left
        StringBuilder sb = new StringBuilder();
        sb.append(input.substring(2,4));
        sb.append(input.substring(4,6));
        sb.append(input.substring(6,8));
        sb.append(input.substring(0,2));

        //sbox
        String temp = subByte(sb.toString());
        sb = new StringBuilder();
        //round constant
        sb.append(bintoHex(xor(hexToBin(temp.substring(0, 2)), rcon[counter])).toUpperCase());
        sb.append(temp.substring(2, temp.length()));
        return sb.toString();
    }



    public static String keyExapnsion(String input) {

        int rcon_counter = 0;
        StringBuilder expansionKey = new StringBuilder();
        expansionKey.append(input);
        switch(input.length()) {

            case 128/4:
                while (expansionKey.length() < 176*2) {

                    for (int j= 0; j < 4; j++) {

                        String temp1 = expansionKey.substring(expansionKey.length() - 8, expansionKey.length());
                        if (j==0) {
                            temp1 = keyExpansionCore(temp1, rcon_counter);
                            rcon_counter++;
                        }
                        String temp2 = expansionKey.substring(expansionKey.length() - 32, expansionKey.length());
                        temp2 = temp2.substring(0, 8);
                        String temp1_bin = hexToBin(temp1);
                        String temp2_bin = hexToBin(temp2);
                        String new_keys = bintoHex(xor(temp1_bin, temp2_bin)).toUpperCase();
                        StringBuilder sb = new StringBuilder();
                        if (new_keys.length() < 8) {
                            for (int i = 0; i < 8-new_keys.length(); i++) {
                                sb.append("0");
                            }
                        }
                        sb.append(new_keys);
                        expansionKey.append(sb.toString());

                    }
                }
                return expansionKey.toString();
            case 192/4:
                while (expansionKey.length() < 208*2) {

                    for (int j= 0; j < 6; j++) {

                        String temp1 = expansionKey.substring(expansionKey.length() - 8, expansionKey.length());
                        if (j==0) {
                            temp1 = keyExpansionCore(temp1, rcon_counter);
                            rcon_counter++;
                        }
                        String temp2 = expansionKey.substring(expansionKey.length() - 48, expansionKey.length());
                        temp2 = temp2.substring(0, 8);
                        String temp1_bin = hexToBin(temp1);
                        String temp2_bin = hexToBin(temp2);
                        String new_keys = bintoHex(xor(temp1_bin, temp2_bin)).toUpperCase();
                        StringBuilder sb = new StringBuilder();
                        if (new_keys.length() < 8) {
                            for (int i = 0; i < 8-new_keys.length(); i++) {
                                sb.append("0");
                            }
                        }
                        sb.append(new_keys);
                        expansionKey.append(sb.toString());
                    }
                }
                return expansionKey.toString();
            case 256/4:
                while (expansionKey.length() < 240*2) {

                    for (int j= 0; j < 8; j++) {

                        String temp1 = expansionKey.substring(expansionKey.length() - 8, expansionKey.length());
                        if (j==0) {
                            temp1 = keyExpansionCore(temp1, rcon_counter);
                            rcon_counter++;
                        }
                        if (j == 4) temp1 = subByte(temp1);
                        String temp2 = expansionKey.substring(expansionKey.length() - 64, expansionKey.length());
                        temp2 = temp2.substring(0, 8);
                        String temp1_bin = hexToBin(temp1);
                        String temp2_bin = hexToBin(temp2);
                        String new_keys = bintoHex(xor(temp1_bin, temp2_bin)).toUpperCase();
                        StringBuilder sb = new StringBuilder();
                        if (new_keys.length() < 8) {
                            for (int i = 0; i < 8-new_keys.length(); i++) {
                                sb.append("0");
                            }
                        }
                        sb.append(new_keys);
                        expansionKey.append(sb.toString());
                    }
                }
                return expansionKey.toString();
            default:
                return "";
        }
    }

    /**
     * the final encrpytion step
     * @param plainText
     * @param aes_key
     * @param mode 1 is ecb, 0 is cbc
     * @return
     */
    public static String aesEncrypt(String plainText, String aes_key, int mode) {

        int rounds;
        String text;
        if (plainText.length() % 32 != 0) {
            int paddingLength = (plainText.length() % 32 + 1) * 32 - plainText.length();
            text = plainText + plainText.substring(0, paddingLength);
        }
        else
            text = plainText;

        switch (aes_key.length()) {

            case 128/4:
                rounds = 9;
                break;
            case 192/4:
                rounds = 11;
                break;
            case 256/4:
                rounds = 13;
                break;
            default:
                return "";
        }
        String newKey = keyExapnsion(aes_key.toUpperCase());
        StringBuilder sb = new StringBuilder();
        String cbc_var = "00000000000000000000000000000000";
        for (int temp = 0; temp < text.length()/32; temp++) {

            String state;
            if (mode == 0) {
                StringBuilder cipher_text = new StringBuilder();
                int length = text.substring(temp * 32, temp * 32 + 32).length() / 4;
                for (int j = 0; j < length; j++) {

                    String stored = bintoHex(xor(hexToBin(text.substring(temp * 32 + j * 4, temp * 32 + j * 4 + 4)),
                            hexToBin(cbc_var.substring(j * 4, j * 4 + 4))));
                    stored = stored.length() < 4 ? "0" + stored : stored;
                    cipher_text.append(stored.toUpperCase());
                }
                state = cipher_text.toString();
                state = addRoundKey(state, newKey.substring(0, 32));
            }
            else
                state = addRoundKey(text.substring(temp * 32, temp * 32 + 32).toUpperCase(), newKey.substring(0, 32));

            for (int i = 0; i < rounds; i++) {

                state = subByte(state);
                state = shiftRows(state);
                state = mixcolumn(state);
                state = addRoundKey(state, newKey.substring(32 + i * 32, 64 + i * 32));
            }

            state = subByte(state);
            state = shiftRows(state);
            state = addRoundKey(state, newKey.substring((rounds + 1) * 32, (rounds + 1) * 32 + 32));

            sb.append(state);
            cbc_var = state;
        }
        return sb.toString();
    }

    /**
     * the final decrpytion step
     * @param cipherText is the encrypted message
     * @param aes_key is the key to decrypt the message
     * @param mode 1 is ecb, 0 is cbc
     * @return
     */
    public static String aesDecrypt(String cipherText, String aes_key, int mode) {

        int rounds;
        String text = cipherText;

        switch (aes_key.length()) {

            case 128/4:
                rounds = 9;
                break;
            case 192/4:
                rounds = 11;
                break;
            case 256/4:
                rounds = 13;
                break;
            default:
                return "";
        }
        String newKey = keyExapnsion(aes_key.toUpperCase());
        String plaintext = "";
        String cbc_var;
        for (int temp = 0; temp < text.length()/32; temp++) {

            String state;

            state = text.substring(text.length() - temp * 32 - 32, text.length() -  temp * 32).toUpperCase();
            state = addRoundKey(state, newKey.substring((rounds + 1) * 32, (rounds + 1) * 32 + 32));
            state = reverseShiftRows(state);
            state = reverseSubByte(state);

            for (int i = rounds - 1; i > -1; i--) {

                state = addRoundKey(state, newKey.substring(32 + i * 32, 64 + i * 32));
                state = reverseMixcolumn(state);
                state = reverseShiftRows(state);
                state = reverseSubByte(state);
            }



            if (mode == 0 && temp != text.length()/32 - 1) {

                state = addRoundKey(state, newKey.substring(0, 32));
                cbc_var = text.substring(text.length() - temp * 32 - 64, text.length() - temp * 32 - 32).toUpperCase();
                StringBuilder xoredValue = new StringBuilder();
                for (int i = 0; i < state.length()/2; i++) {
                    xoredValue.append(bintoHex(xor(hexToBin(state.substring(i * 2, i * 2 + 2)), hexToBin(cbc_var.substring(i * 2, i * 2 + 2)))));
                }

                state = xoredValue.toString().toUpperCase();
            }
            else {

                state = addRoundKey(state, newKey.substring(0, 32));
            }
            plaintext = state + plaintext;
        }
        return plaintext;
    }

    /**
     * fill our sbox with a given file
     * @param file is the file name of sbox
     */
    public static void initializeSBox(String file) {

        s_box = new HashMap<>();
        reverse_s_box = new HashMap<>();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);
            String nextLine;
            String[] elements;
            while ((nextLine = bufferedReader.readLine()) != null) {

                elements = nextLine.split("[ \t]");
                s_box.put(elements[0], elements[1]);
                reverse_s_box.put(elements[1], elements[0]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("done");
    }
}