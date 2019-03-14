package harmonia;

public class Key implements Comparable {

    public static final Key C   = new Key(Tone.C);
    public static final Key Am  = new Key(Tone.C,true);

    public static final Key Cs  = new Key(Tone.Cs);
    public static final Key Db  = new Key(Tone.Db);
    public static final Key Bbm = new Key(Tone.Db,true);
    public static final Key Asm = new Key(Tone.Cs,true);

    public static final Key D   = new Key(Tone.D);
    public static final Key Bm  = new Key(Tone.D,true);

    public static final Key Ds  = new Key(Tone.Ds);
    public static final Key Eb  = new Key(Tone.Eb);
    public static final Key Cm  = new Key(Tone.Eb,true);

    public static final Key E   = new Key(Tone.E);
    public static final Key Csm = new Key(Tone.E,true);
    public static final Key Dbm = new Key(Tone.E,true);

    public static final Key F   = new Key(Tone.F);
    public static final Key Dm  = new Key(Tone.F,true);

    public static final Key Fs  = new Key(Tone.Fs);
    public static final Key Gb  = new Key(Tone.Gb);
    public static final Key Ebm = new Key(Tone.Fs,true);
    public static final Key Dsm = new Key(Tone.Gb,true);
    
    public static final Key G   = new Key(Tone.G);
    public static final Key Em  = new Key(Tone.G,true);

    public static final Key Gs  = new Key(Tone.Gs);
    public static final Key Ab  = new Key(Tone.Ab);
    public static final Key Fm  = new Key(Tone.Ab,true);
    
    public static final Key A   = new Key(Tone.A);
    public static final Key Fsm = new Key(Tone.A,true);
    public static final Key Gbm = new Key(Tone.A,true);

    public static final Key As  = new Key(Tone.As);
    public static final Key Bb  = new Key(Tone.Bb);
    public static final Key Gm  = new Key(Tone.Bb,true);

    public static final Key B   = new Key(Tone.B);
    public static final Key Gsm = new Key(Tone.B,true);
    public static final Key Abm = new Key(Tone.B,true);

    public static final Key[] ALL_KEYS = {
        A,
        As,
        B,
        C,
        Cs,
        D,
        Ds,
        E,
        F,
        Fs,
        G,
        Gs
    };

    private Tone base;
    private boolean minor;
    private String given;

    public Key(Tone base) {
        this.base = base;
        this.minor = false;
        this.given = null;
    }

    public Key(Tone base, boolean minor) {
        this.base = base;
        this.minor = minor;
        this.given = null;
    }

    public Key(Tone base, String given) {
        given = given.trim();
        this.base = base;
        this.minor = given.endsWith("m");
        this.given = given;
    }

    public Key(String given) {
        given = given.trim();
        this.base = new Tone(given);
        if (given.matches(".*m.*") ||
            given.matches(".*min.*")) {
            this.minor = true;

        }
        if (given.matches(".*maj.*")) {
            this.minor = false;
        }
        if (this.minor) {
            switch (base.getTone()) {
                case  0: this.base = new Tone("Eb"); break;  // Cm  = Eb
                case  1: this.base = new Tone("E");  break;  // Dbm = E
                case  2: this.base = new Tone("F");  break;  // Dm  = F
                case  3: this.base = new Tone("F#"); break;  // Ebm = G#
                case  4: this.base = new Tone("G");  break;  // Em  = G
                case  5: this.base = new Tone("G#"); break;  // Fm  = G#
                case  6: this.base = new Tone("A");  break;  // F#m = A
                case  7: this.base = new Tone("Bb"); break;  // Gm  = Bb
                case  8: this.base = new Tone("B");  break;  // G#m = B
                case  9: this.base = new Tone("C");  break;  // Am  = C
                case 10: this.base = new Tone("Db"); break;  // Bbm = Db
                case 11: this.base = new Tone("D");  break;  // Bm  = D
            }
        }
        this.given = given;
    }

    public int compareTo(Object o) {
        if (o instanceof Key) {
            Key ok = (Key)o;
            int bc = base.compareTo(ok.base);
            if (bc == 0) {
                if (minor == ok.minor) {
                    return 0;
                } else if (minor) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return bc;
            }
        } else {
            return 0;
        }
    }

    public String toString() {
        String val = null;
        if (given != null) {
            val = given;
        } else {
            if (minor) {
                switch (base.getTone()) {
                    case  0: val = "Am";  break;
                    case  1: val = "Bbm"; break;
                    case  2: val = "Bm";  break;
                    case  3: val = "Cm";  break;
                    case  4: val = "Dbm"; break;
                    case  5: val = "Dm";  break;
                    case  6: val = "Ebm"; break;
                    case  7: val = "Em";  break;
                    case  8: val = "Fm";  break;
                    case  9: val = "F#m"; break;
                    case 10: val = "Gm";  break;
                    case 11: val = "G#m"; break;
                }
            } else {
                val = base.toString();
            }
        }
        return val;
    }

}

