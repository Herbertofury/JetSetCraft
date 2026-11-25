import com.herberto.jetsetcraft.gang.HonkistasVariantPolicy;
import com.herberto.jetsetcraft.gang.HonkistasVariantPolicy.Context;

public final class HonkistasVariantPolicyTest {
    private static int count;
    private static void check(Context c, double p, double r, String expected) {
        if (!java.util.Objects.equals(HonkistasVariantPolicy.select(c, p, r), expected))
            throw new AssertionError("Case " + count);
        count++;
    }
    public static void main(String[] args) {
        String h = HonkistasVariantPolicy.HONKISTAS;
        String g = HonkistasVariantPolicy.GRINIONS;
        Context ready = new Context(h, true, true, false, false, false, true);
        check(ready, .05, 0, g);
        check(ready, .05, .049999, g);
        check(ready, .05, .05, h);
        check(ready, .05, .999, h);
        for (double p : new double[]{-1, 0, .5, 1, Double.NaN, Double.POSITIVE_INFINITY})
            check(ready, p, 0, h);
        for (double r : new double[]{-1, 1, Double.NaN, Double.POSITIVE_INFINITY})
            check(ready, .05, r, h);
        check(new Context(h,false,true,false,false,false,true), .05,0,h);
        check(new Context(h,true,false,false,false,false,true), .05,0,h);
        check(new Context(h,true,true,true,false,false,true), .05,0,h);
        check(new Context(h,true,true,false,true,false,true), .05,0,h);
        check(new Context(h,true,true,false,false,true,true), .05,0,h);
        check(new Context(h,true,true,false,false,false,false), .05,0,h);
        for (String id : new String[]{g, HonkistasVariantPolicy.AMYGDALA, "jetsetcraft:dead_beat", null})
            check(new Context(id,true,true,false,false,false,true),.05,0,id);
        check(null, .05, 0, h);
        for (int i=0; i<10000; i++) check(ready,.05,i/10000.0,i<500?g:h);
        System.out.println(count + " policy checks passed (synthetic; not native gameplay)");
    }
}
