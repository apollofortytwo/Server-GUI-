import java.awt.Color;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ColorSerializer extends Serializer<Color> {
    @Override
    public Color read(Kryo kryo, Input input, Class<Color> clazz) {
        return new Color(input.readInt());
    }

    @Override
    public void write(Kryo kryo, Output output, Color color) {
        output.write(color.getRGB());
    }
}