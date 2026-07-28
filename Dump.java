public class Dump { 
    public static void main(String[] args) throws Exception { 
        for(java.lang.reflect.Method m : Class.forName("com.thingclips.smart.sdk.api.IThingActivatorInstance").getMethods()) { 
            System.out.println(m); 
        } 
    } 
}
