package eu.toolchain.serializer.perftests;

import org.openjdk.jmh.util.NullOutputStream;

public class OutputStreamHelper {
    public static NullOutputStream newNullStream() {
        return new NullOutputStream();
    }
    
    public static void main(String argv[]) {
        
    }
}