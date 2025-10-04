
package shared.ser;
import com.fasterxml.jackson.databind.ObjectMapper;
public final class Json {
    private static final ObjectMapper M = new ObjectMapper();
    public static String to(Object o){ try { return M.writeValueAsString(o);} catch(Exception e){throw new RuntimeException(e);} }
    public static <T> T from(String s, Class<T> c){ try { return M.readValue(s, c);} catch(Exception e){throw new RuntimeException(e);} }
}
