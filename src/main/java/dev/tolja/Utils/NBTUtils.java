package dev.tolja.Utils;

import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;

import java.io.IOException;
import java.io.InputStream;

public class NBTUtils {
    public static NamedTag readNBTData(InputStream input, boolean compressed) throws IOException {
        Throwable var3 = null;

        NamedTag var4;
        try {
            var4 = (new NBTDeserializer(compressed)).fromStream(input);
        } catch (Throwable var13) {
            var3 = var13;
            throw var13;
        } finally {
            if (var3 != null) {
                try {
                    input.close();
                } catch (Throwable var12) {
                    var3.addSuppressed(var12);
                }
            } else {
                input.close();
            }

        }

        return var4;
    }
}
