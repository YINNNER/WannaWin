package citiMerchant.vo;

import java.util.*;

//The "Type" is used in "userPref" and "item"
/*
 * Type is represented by 0-1 sequence
 *      class Type and TypeWrapper is util class help process multi-type for one item or userPref.
 */
public class Type {

    static final private int radix = 16;
    static final private int amount = 512;
    static final private int long_bit = 64;
    static final private int number_of_bits_in_byte = 4;                                              // f = 1111
    static final private int number_of_bytes_in_str = 512 / number_of_bits_in_byte;                   // 128, str.length
    static final private int numbers_of_bytes_in_long_str = long_bit / number_of_bits_in_byte;        // 16
    static final private int length_of_longs = amount / long_bit;                                     // 8


    /*
     * The amount is less than #{amount}, so it can be interpreted as #{amount} bits.
     * 0 ~ (#{amount} -1) bit represents types.
     *
     * The Conversion Chain is like:
     *          enum => str => bit => enum
     *
     */
    public enum ItemType {
        normal, catering, exercise, bank, costume, education, communication;

        static Map<ItemType, String> m1 = new HashMap<>();      //  enum  =>  str
        static Map<String, Integer> m2 = new HashMap<>();       //  str   =>  bit
        static Map<Integer, ItemType> m3 = new HashMap<>();     //  bit   =>  enum
        static Integer bit = 0;

        static {

            //  enum  =>  str
            m1.put(normal, "normal");
            m1.put(catering, "catering");
            m1.put(exercise, "exercise");
            m1.put(bank, "bank");
            m1.put(costume, "costume");
            m1.put(education, "education");
            m1.put(communication, "communication");


            //  str   =>  bit
            bit = 0;
            m2.put("normal", bit++);
            m2.put("catering", bit++);
            m2.put("exercise", bit++);
            m2.put("bank", bit++);
            m2.put("costume", bit++);
            m2.put("education", bit++);
            m2.put("communication", bit++);


            //  bit   =>  enum
            bit = 0;
            m3.put(bit++, normal);
            m3.put(bit++, catering);
            m3.put(bit++, exercise);
            m3.put(bit++, bank);
            m3.put(bit++, costume);
            m3.put(bit++, education);
            m3.put(bit++, communication);


        }

        public static Integer getAmount() {
            return bit;
        }

        // str => bit => enum
        public static ItemType str2enum(String itemType) {
            return m3.get(m2.get(itemType));
        }

        // enum => str => bit
        public static Integer enum2bit(ItemType itemType) {
            return m2.get(m1.get(itemType));
        }

        // bit => enum
        public static ItemType bit2enum(Integer bit) {
            return m3.get(bit);
        }

        // bit => enum => str
        public static String bit2str(Integer bit) {
            return m1.get(m3.get(bit));
        }

        // enum => str
        public static String enum2str(ItemType itemType) {
            return m1.get(itemType);
        }

        // DBStr => List<enum>
        public static List<ItemType> DBStr2enum(String DBString) {
            List<ItemType> l = new ArrayList<>();
            TypeWrapper tw = new TypeWrapper(DBString);
            Boolean[] b = tw.getFeatureVec();
            for (int i = 0; i < b.length; ++i)
                if (b[i]) l.add(bit2enum(i));
            return l;
        }

        // List<enum> => DBStr  // null -> "000..000"
        public static String enum2DBStr(List<ItemType> itemTypes) {
            TypeWrapper tw = new TypeWrapper(String.join("", Collections.nCopies(number_of_bytes_in_str, "0")));
            if (null == itemTypes)
                return tw.toString();
            for (ItemType itemtype : itemTypes)
                tw.addType(enum2bit(itemtype));
            return tw.toString();
        }

        // List<str> => DBStr   // null -> "000..000"
        public static String str2DBStr(List<String> itemTypes) {
            List<ItemType> enumTypes = new ArrayList<>();
            for (String itemType : itemTypes) {
                enumTypes.add(str2enum(itemType));
            }
            return enum2DBStr(enumTypes);
        }
        
    }


    /*
     * TypeWrapper stores the typeStr
     *      and provide : 1. conversion operations  (DBStr <=> TypeWrapper) // NB: DBStr is not from 0 ~ (#{amount} -1)
     *                                                                      // The sequence is stored in Long[] types;
     *                    2. update operations
     *                    3. get feature Vector
     */
    public static class TypeWrapper {

        private Long[] types;

        //typeString is stored in DB
        public TypeWrapper(String typeString) {
            types = Str2Long(typeString);
        }


        /*
         * return Boolean[#{amount}],
         *              which represents the types involving itemTypes in Item or UserPref.
         */
        public Boolean[] getFeatureVec() {
            Boolean[] b = new Boolean[amount];
            for (int i = 0; i < length_of_longs; ++i)
                for (int j = 0; j < long_bit; ++j)
                    b[long_bit * i + j] = ((types[i] & (1L << j)) == (1L << j)) ? true : false;
            return b;
        }


        /*
         * Param: Boolean[#{amount}]
         *              update the type 0-1 sequence
         */
        public void setFeatureVec(Boolean[] b) {
            assert (b.length != amount) : "Vec length unsatisfied!";
            if (b.length != amount) {
                System.err.println("Vec length unsatisfied!");
                return;
            }
            for (int i = 0; i < length_of_longs; ++i) {
                Long l = 0L;
                for (int j = 0; j < long_bit; ++j) {
                    if (b[long_bit * i + j])
                        l += 1L << j;
                }
                types[i] = l;
            }
        }

        /*
         * set the dth bit to '1'
         * 0 <= dimension < #{amount}
         */
        public void addType(int dimension) {
            assert (dimension < amount && dimension >= 0) : "Invalid dimension!";
            int q = dimension / long_bit;
            int r = dimension - q * long_bit;
            types[q] |= (1L << r);
        }


        /*
         * set the dth bit to '0'
         * 0 <= dimension < #{amount}
         */
        public void eraseType(int dimension) {
            assert (dimension < amount && dimension >= 0) : "Invalid dimension!";
            int q = dimension / long_bit;
            int r = dimension - q * long_bit;
            types[q] &= ~(1L << r);
        }


        //To store in DB
        @Override
        public String toString() {
            String s = new String();
            for (Long l : types) {
                String temp = Long.toUnsignedString(l, radix);
                //fill up the leading "0" in each "Long" // ex. 123 => "0123"
                s = String.join("", Collections.nCopies(numbers_of_bytes_in_long_str - temp.length(), "0")) + temp + s;
            }
            return s;
        }


    }


    /*
     * Parameter: #{number_of_bytes_in_str} bytes String : "(ffffffffffffffffff)(....)(abcd...)(0123...)"
     * Return:
     *          Long[#{length_of_longs}] = (0123...), (abcd...), ..., (ffffffffffffffffff)
     */
    private static Long[] Str2Long(final String type) {
        Long[] l = new Long[length_of_longs];
        try {
            for (int i = 0; i < length_of_longs; ++i)
                l[length_of_longs - 1 - i] = Long.parseUnsignedLong(type.substring(0 + numbers_of_bytes_in_long_str * i, numbers_of_bytes_in_long_str + numbers_of_bytes_in_long_str * i), radix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return l;
    }


}