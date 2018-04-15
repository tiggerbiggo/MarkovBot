package io.tobylarone;

/**
 * Thicc class
 */
public class Thicc {

    private String input;
    /**
     * Thicc Constructor
     * @param input the input string to translate
     */
    public Thicc(String input) {
        this.input = input;
    }

    /**
     * Replaces normal characters with extra thicc characters
     * 
     * @return the output string
     */
    public String parse() {
        String output = input.toLowerCase();
        output = output.replaceAll("a", "卂");
        output = output.replaceAll("b", "乃");
        output = output.replaceAll("c", "匚");
        output = output.replaceAll("d", "刀");
        output = output.replaceAll("e", "乇");
        output = output.replaceAll("f", "下");
        output = output.replaceAll("g", "厶");
        output = output.replaceAll("h", "卄");
        output = output.replaceAll("i", "工");
        output = output.replaceAll("j", "丁");
        output = output.replaceAll("k", "长");
        output = output.replaceAll("l", "乚");
        output = output.replaceAll("m", "从");
        output = output.replaceAll("n", "𠘨");
        output = output.replaceAll("o", "口");
        output = output.replaceAll("p", "尸");
        output = output.replaceAll("q", "㔿");
        output = output.replaceAll("r", "尺");
        output = output.replaceAll("s", "丂");
        output = output.replaceAll("t", "丅");
        output = output.replaceAll("u", "凵");
        output = output.replaceAll("v", "リ");
        output = output.replaceAll("w", "山");
        output = output.replaceAll("x", "乂");
        output = output.replaceAll("y", "丫");
        output = output.replaceAll("z", "乙");
        output = output.replaceAll(" ", "  ");

        return output;
    }
}
