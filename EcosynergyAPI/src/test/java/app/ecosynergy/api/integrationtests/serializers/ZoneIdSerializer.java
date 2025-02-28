package app.ecosynergy.api.integrationtests.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.ZoneId;

public class ZoneIdSerializer extends JsonSerializer<ZoneId> {
    @Override
    public void serialize(ZoneId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getId());
    }
}
