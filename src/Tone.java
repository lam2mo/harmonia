package harmonia;

public class Tone implements Comparable {

    public static final Tone C   = new Tone(0);
    public static final Tone Cs  = new Tone(1);
    public static final Tone Db  = new Tone(1);
    public static final Tone D   = new Tone(2);
    public static final Tone Ds  = new Tone(3);
    public static final Tone Eb  = new Tone(3);
    public static final Tone E   = new Tone(4);
    public static final Tone F   = new Tone(5);
    public static final Tone Fs  = new Tone(6);
    public static final Tone Gb  = new Tone(6);
    public static final Tone G   = new Tone(7);
    public static final Tone Gs  = new Tone(8);
    public static final Tone Ab  = new Tone(8);
    public static final Tone A   = new Tone(9);
    public static final Tone As  = new Tone(10);
    public static final Tone Bb  = new Tone(10);
    public static final Tone B   = new Tone(11);

    private int tone;
    private String given;

    protected Tone(int tone) {
        this.tone = tone;
        this.given = null;
    }

    protected Tone(int tone, String given) {
        this.tone = tone;
        this.given = given.trim();
    }

    public Tone(String given) {

        // TODO: fix this with regex?
        
        given = given.trim();

        if      (given.startsWith("Cb")) { this.tone = 11; }
        else if (given.startsWith("C#")) { this.tone =  1; }
        else if (given.startsWith("C"))  { this.tone =  0; }

        else if (given.startsWith("Db")) { this.tone =  1; }
        else if (given.startsWith("D#")) { this.tone =  3; }
        else if (given.startsWith("D"))  { this.tone =  2; }

        else if (given.startsWith("Eb")) { this.tone =  3; }
        else if (given.startsWith("E#")) { this.tone =  5; }
        else if (given.startsWith("E"))  { this.tone =  4; }

        else if (given.startsWith("Fb")) { this.tone =  4; }
        else if (given.startsWith("F#")) { this.tone =  6; }
        else if (given.startsWith("F"))  { this.tone =  5; }

        else if (given.startsWith("Gb")) { this.tone =  6; }
        else if (given.startsWith("G#")) { this.tone =  8; }
        else if (given.startsWith("G"))  { this.tone =  7; }

        else if (given.startsWith("Ab")) { this.tone =  8; }
        else if (given.startsWith("A#")) { this.tone = 10; }
        else if (given.startsWith("A"))  { this.tone =  9; }

        else if (given.startsWith("Bb")) { this.tone = 10; }
        else if (given.startsWith("B#")) { this.tone =  0; }
        else if (given.startsWith("B"))  { this.tone = 11; }

        else { this.tone = 0; }

        this.given = given;
    }

    public int getTone() {
        return tone;
    }

    public int compareTo(Object o) {
        if (o instanceof Tone) {
            int ot = (((Tone)o).tone + 3) % 12;
            int mt = (tone + 3) % 12;
            return mt-ot;
        } else {
            return 0;
        }
    }

    public String toString() {
        String val = null;
        if (given != null) {
            val = given;
        } else {
            switch (tone) {
                case  0: val = "C";  break;
                case  1: val = "Db"; break;
                case  2: val = "D";  break;
                case  3: val = "Eb"; break;
                case  4: val = "E";  break;
                case  5: val = "F";  break;
                case  6: val = "F#"; break;
                case  7: val = "G";  break;
                case  8: val = "Ab"; break;
                case  9: val = "A";  break;
                case 10: val = "Bb"; break;
                case 11: val = "B";  break;
            }
        }
        return val;
    }

}

