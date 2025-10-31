package mod.swinegraphics.service.dxt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import mod.swinegraphics.util.Log;

/**
 * Modified version of DXTDecompress. <br>
 * Original
 * https://github.com/stormregion/community_tools/blob/master/swinehd_g1/src/main/java/playground/swine/DXTDecompress.java
 * 
 * https://docs.microsoft.com/en-us/windows/desktop/direct3d10/d3d10-graphics-programming-guide-resources-block-compression
 *
 * @author Nani
 */
public class DxtWorker {

    public static final int DXT1 = 0x31545844;
    public static final int DXT5 = 0x35545844;
    private final int[] texel;

    public DxtWorker() {        
        texel = new int[16];
    }

    Optional<List<BufferedImage>> parse(File source) {
        var dxtList = new ArrayList<BufferedImage>();

        // Find and convert all DXT files in the root folder
        try (var files = Files.walk(source.toPath())) {
            files.forEach(path -> {
                var file = path.toFile();
                if (file.isDirectory() || !file.getName().endsWith(".dxt")) {
                    return;
                }
                try (FileChannel input = new FileInputStream(file).getChannel()) {

                    // Read file header
                    ByteBuffer header = ByteBuffer.allocate(20);
                    input.read(header);
                    header.flip();

                    // Verify stormregion signature
                    int major = header.getInt();
                    int minor = header.getInt();
                    int type = header.getInt();
                    if (major != 0x53721A1B || minor != 0x0D0A870A || type != 0x54455854) {
                        throw new IOException("Not a stormregion file.");
                    }

                    // Load texture data to memory
                    header.order(ByteOrder.LITTLE_ENDIAN);
                    int content = header.getInt();
                    ByteBuffer buffer = ByteBuffer.allocate(content).order(ByteOrder.LITTLE_ENDIAN);
                    input.read(buffer);
                    buffer.flip();
                    int sizeX = buffer.getInt();
                    int sizeY = buffer.getInt();
                    int format = buffer.getInt();
                    BufferedImage bi = decodeDXT(buffer, format, sizeX, sizeY);
                    dxtList.add(bi);
                } catch (IOException err) {
                    Log.log(err, "Unable to decode file " + source.getName());
                }
            });
            return Optional.of(dxtList);
        } catch (IOException ex) {
            Log.log(ex, "Unable to decode file " + source.getName());
            return Optional.empty();
        }
    }

    public BufferedImage decodeBifurcatedDXT1(ByteBuffer buffer, int sizeX, int sizeY) {
        validateImage(sizeX, sizeY, DXT1);
        BufferedImage image = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);

        // This DXT buffer is bifurcated, color first, indices next
        int dataSize = (sizeX / 4) * (sizeY / 4) * 8;
        ByteBuffer bcolor = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer indices = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        indices.position(dataSize / 2);
        for (int y = 0; y < sizeY; y += 4) {
            for (int x = 0; x < sizeX; x += 4) {
                Arrays.fill(texel, 0xFF000000);
                decodeDiffuse(bcolor, indices, texel, 255);
                image.setRGB(x, y, 4, 4, texel, 0, 4);
            }
        }
        buffer.position(buffer.position() + dataSize);
        return image;
    }

    private int unpack565(int color) {
        int red = (color >> 11) & 0x1F;
        int green = (color >> 5) & 0x3F;
        int blue = (color) & 0x1F;

        // Upscale to 8 bit
        red = (red << 3) | (red >> 2);
        green = (green << 2) | (green >> 4);
        blue = (blue << 3) | (blue >> 2);
        return (red << 16) | (green << 8) | blue;
    }

    private int interpolate(int color0, int m0, int color1, int m1) {
        int total = m0 + m1;
        int alphaLocal = ((color0 >> 24) & 0xFF) * m0 + ((color1 >> 24) & 0xFF) * m1;
        int red = ((color0 >> 16) & 0xFF) * m0 + ((color1 >> 16) & 0xFF) * m1;
        int green = ((color0 >> 8) & 0xFF) * m0 + ((color1 >> 8) & 0xFF) * m1;
        int blue = ((color0) & 0xFF) * m0 + ((color1) & 0xFF) * m1;
        return (alphaLocal / total) << 24 | (red / total) << 16 | (green / total) << 8 | (blue / total);
    }

    private void decodeAlpha(ByteBuffer buffer, int[] argb) {

        // Build alpha palette
        var alpha = new int[8];
        alpha[0] = Byte.toUnsignedInt(buffer.get());
        alpha[1] = Byte.toUnsignedInt(buffer.get());

        // Interpolate alpha range
        if (alpha[0] > alpha[1]) {
            for (int idx = 2; idx < 8; idx++) {
                alpha[idx] = (alpha[0] * (8 - idx) + alpha[1] * (idx - 1)) / 7;
            }
        } else {
            for (int i = 2; i < 6; i++) {
                alpha[i] = (alpha[0] * +alpha[1] * (i - 1)) / 5;
            }
            alpha[6] = 0;
            alpha[7] = 255;
        }

        // Unpack pixel alpha from indices
        for (int off = 0; off < 4 * 4; off += 8) {
            long packed = (long) buffer.get() | (buffer.get() << 8) | (buffer.get() << 16);
            for (int pixel = 0; pixel < 8; pixel++) {
                int select = (int) (packed >> 3 * pixel) & 7;
                argb[off + pixel] = alpha[select] << 24;
            }
        }
    }

    private void decodeDiffuse(ByteBuffer colors, ByteBuffer indices, int[] argb, int alpha) {
        var color = new int[4];
        color[0] = (alpha << 24) | unpack565(colors.getShort());
        color[1] = (alpha << 24) | unpack565(colors.getShort());

        if (color[0] > color[1]) {
            color[2] = interpolate(color[0], 2, color[1], 1);
            color[3] = interpolate(color[0], 1, color[1], 2);
        } else {
            color[2] = interpolate(color[0], 1, color[1], 1);
            color[3] = 0x0;
        }

        for (int off = 0; off < 4 * 4; off += 4) {
            int packed = indices.get();
            argb[off + 0] |= color[(packed) & 3];
            argb[off + 1] |= color[(packed >> 2) & 3];
            argb[off + 2] |= color[(packed >> 4) & 3];
            argb[off + 3] |= color[(packed >> 6) & 3];
        }
    }

    private void validateImage(int sizeX, int sizeY, int format) {
        if (sizeX % 4 != 0) {
            throw new IllegalArgumentException("sizeX: " + sizeX + " is not multiple of 4!");
        }
        if (sizeY % 4 != 0) {
            throw new IllegalArgumentException("sizeY: " + sizeY + " is not multiple of 4!");
        }

        if (format != DXT1 && format != DXT5) {
            throw new IllegalArgumentException("Unsupported image format " + Integer.toHexString(format));
        }
    }

    private BufferedImage decodeDXT(ByteBuffer buffer, int format, int sizeX, int sizeY) {
        validateImage(sizeX, sizeY, format);
        BufferedImage image = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);

        // All reads expect little endian order
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int y = 0; y < sizeY; y += 4) {
            for (int x = 0; x < sizeX; x += 4) {
                Arrays.fill(texel, 0);
                if (format == DXT5) {
                    decodeAlpha(buffer, texel);
                    decodeDiffuse(buffer, buffer, texel, 0);
                } else {
                    decodeDiffuse(buffer, buffer, texel, 255);
                }
                image.setRGB(x, y, 4, 4, texel, 0, 4);
            }
        }
        return image;
    }
}
