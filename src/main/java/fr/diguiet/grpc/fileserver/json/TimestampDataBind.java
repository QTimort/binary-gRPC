package fr.diguiet.grpc.fileserver.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.google.protobuf.Timestamp;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;

/**
 * Class that implement two sub class to serialize and deserialize Timestamp object
 */
@Immutable
public final class TimestampDataBind {
    private static final String SECONDS_FIELD_NAME = "seconds";
    private static final String NANOS_FIELD_NAME = "nanos";

    /**
     * Class is not instantiable and inheritable
     */
    private TimestampDataBind() {

    }

    /**
     * Class that serialize a Timestamp Object
     */
    @Immutable
    public static class Serializer extends JsonSerializer<Timestamp> {
        @Override
        public void serialize(final Timestamp timestamp, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField(TimestampDataBind.SECONDS_FIELD_NAME, timestamp.getSeconds());
            jsonGenerator.writeNumberField(TimestampDataBind.NANOS_FIELD_NAME, timestamp.getNanos());
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * Class that deserialize a Timestamp Object
     */
    @Immutable
    public static class Deserializer extends JsonDeserializer<Timestamp> {
        @Override
        public Timestamp deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            return (Timestamp.newBuilder()
                    .setSeconds(node.get(TimestampDataBind.SECONDS_FIELD_NAME).asLong())
                    .setNanos(node.get(TimestampDataBind.NANOS_FIELD_NAME).asInt())
                    .build());
        }
    }
}
