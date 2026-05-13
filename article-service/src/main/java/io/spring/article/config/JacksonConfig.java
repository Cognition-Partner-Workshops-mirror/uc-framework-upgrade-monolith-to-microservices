package io.spring.article.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Jackson configuration for JodaTime DateTime serialization.
// Extracted from monolith — outputs ISO 8601 format for article timestamps.
@Configuration
public class JacksonConfig {

  @Bean
  public Module jodaTimeModule() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(DateTime.class, new DateTimeSerializer());
    return module;
  }

  // Serializes JodaTime DateTime to ISO 8601 string format
  private static class DateTimeSerializer extends StdSerializer<DateTime> {
    DateTimeSerializer() {
      super(DateTime.class);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        gen.writeString(ISODateTimeFormat.dateTime().withZoneUTC().print(value));
      }
    }
  }
}
