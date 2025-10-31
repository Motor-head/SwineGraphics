package mod.swinegraphics.service.anix;

import io.airlift.compress.zstd.ZstdDecompressor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mod.swinegraphics.service.dxt.DxtWorker;
import mod.swinegraphics.util.Log;

/**
 * Modified version of ANIXDecompress.<br>
 *
 * Original
 * https://github.com/stormregion/community_tools/blob/master/swinehd_g1/src/main/java/playground/swine/ANIXDecompress.java
 *
 * @author Nani
 */
public class AnixWorker {
    
    private AnixWorker(){
        throw new UnsupportedOperationException("AnixWorker is a utility class.");
    }

    static Optional<List<BufferedImage>> parse(DxtWorker dxtWorker, File source) {
        var bufferedImages = new ArrayList<BufferedImage>();
        try (FileChannel input = new FileInputStream(source).getChannel()) {
            ByteBuffer header = ByteBuffer.allocate(28).order(ByteOrder.LITTLE_ENDIAN);
            input.read(header);
            header.flip();
            header.position(8); // Skip stormregion headers
            header.getInt(); //Skip format (important!)
            int size = header.getInt();
            int width = header.getInt();
            int height = header.getInt();
            int frames = header.getInt();

            // DXT encoding required W and H to be divisible by 4, thus the 'real'
            //  buffer is slightly bigger than the actual image size
            int frameBufferSize = width * height * 8;

            // Weird way of calculating frame buffer size, directly from disassembly
            long esi = (width + ((width + 3) & 3)) >> 2;
            long eax = (height + ((height + 3) & 3)) >> 2;
            frameBufferSize = (int) ((esi * eax) * frames) << 3;

            ByteBuffer before = ByteBuffer.allocateDirect(size - 12);
            ByteBuffer after = ByteBuffer.allocateDirect(frameBufferSize);
            input.read(before);
            before.flip();
            ZstdDecompressor decomp = new ZstdDecompressor();
            decomp.decompress(before, after);
            after.flip();
            for (int i = 0; i < frames; i++) {
                BufferedImage frame = dxtWorker.decodeBifurcatedDXT1(after, width, height);
                bufferedImages.add(frame);
            }
            return Optional.of(bufferedImages);
        } catch (IOException ex) {
            Log.log(ex, "Error exporting anif file: " + source.getName());
            return Optional.empty();
        }
    }

}
