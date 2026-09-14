import jcifs.context.SingletonContext;
public class Test {
    public static void main(String[] args) throws Exception {
        var ctx = SingletonContext.getInstance();
        var client = ctx.getNameServiceClient();
        for (var m : client.getClass().getMethods()) {
            if (m.getName().contains("Name")) {
                System.out.println(m);
            }
        }
    }
}
