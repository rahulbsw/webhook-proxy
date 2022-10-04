package io.github.rahulbsw.webhook.proxy;

import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.rahulbsw.webhook.proxy.sink.KafkaSink;
import io.github.rahulbsw.webhook.proxy.sink.SinkFactory;
import io.github.rahulbsw.webhook.proxy.sink.exception.SinkFailureException;
import io.jooby.Environment;
import io.jooby.Jooby;
import io.jooby.MediaType;
import io.jooby.ModelAndView;
import io.jooby.RateLimitHandler;
import io.jooby.StatusCode;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.json.JacksonModule;
import io.jooby.kafka.KafkaProducerModule;
import io.jooby.metrics.MetricsModule;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class App extends Jooby {

  {
    Environment env = getEnvironment();
    Config conf = env.getConfig();
    Store.init(conf.getString("db.file"));
    ObjectMapper objectMapper=new ObjectMapper();
    install(new JacksonModule(objectMapper));

    //install(new JacksonModule(new XmlMapper()));
    Boolean isKafkaEnabled=conf.hasPath("kafka.producer.bootstrap.servers") && !Strings.isNullOrEmpty(conf.getString("kafka.producer.bootstrap.servers"));
    if(isKafkaEnabled)
      install(new KafkaProducerModule());
    install(new MetricsModule()
            .ping()
            .healthCheck("deadlock", new ThreadDeadlockHealthCheck())
            .metric("memory", new MemoryUsageGaugeSet())
            .metric("threads", new ThreadStatesGaugeSet())
            .metric("gc", new GarbageCollectorMetricSet())
    );

    encoder(MediaType.json, (ctx, result) -> {
      byte[] json = objectMapper.writeValueAsBytes(result);
      ctx.setDefaultResponseType(MediaType.json);
      return json;
    });

    //Store.put("test",new Store.Values(new ObjectNode() SinkFactory.SinkType.CONSOLE.name(),TEST_SPEC));
    before(new RateLimitHandler(remoteAddress -> {
      Bandwidth limit = Bandwidth.simple(1000, Duration.ofMinutes(1));
      return Bucket.builder().addLimit(limit).build();
    }));


    assets("/css/*", Paths.get("views", "css"));
    assets("/images/*", Paths.get("views", "images"));
    assets("/js/*", Paths.get("views", "js"));
    assets("/favicon.ico",Paths.get("views", "favicon.ico"));

    install(new HandlebarsModule());

    get("/", ctx -> {
      Map<String, Object> model = new HashMap<>();
      return new ModelAndView("index.html", model)
              .setLocale(Locale.US);
    });

    post("/register",ctx -> {
      try{
        Map<String, String> input=ctx.multipartMap();
        String sink=input.get("sink");
        Map<String,Object> config=new HashMap<>();
        if(!Strings.isNullOrEmpty(sink))
          input.keySet().forEach(key-> {
            if (key.startsWith(sink)) config.put(key.substring(sink.length()+1),input.get(key));
          });
        WebhookConfig webhookConfig = ctx.form(WebhookConfig.class);
        if(SinkFactory.SinkType.KAFKA.equals(sink))
        {
          if(isKafkaEnabled) {
            KafkaProducer producer = require(KafkaProducer.class);
            config.put(KafkaSink.KAFKA_PRODUCER_KEY, producer);
          }
          else {
            throw new SinkFailureException("KafkaSink not supported: Kafka Properties are not configure on server.");
          }
        }
        webhookConfig.config=config;
        Store.put(webhookConfig);
        ctx.setResponseCode(StatusCode.OK);
        return new Message.Builder().put("message","Registered Successfully.").build();
      }catch (Exception e){
        ctx.setResponseCode(StatusCode.BAD_REQUEST);
        return new Message.Builder().put("message","Registered Failed." ).put("error", e.getMessage()).build();
      }



    });

    post("/{name}",ctx -> {
      try {
        String name = ctx.path("name").value();
        List<Message> messages=Store.process(name,ctx.body(JsonNode.class));
        ctx.setResponseCode(StatusCode.OK);
        return messages;
      } catch (Exception e) {
        ctx.setResponseCode(StatusCode.BAD_REQUEST);
        return new Message.Builder().put("message", "Registered Failed.").put("error", e.getMessage()).build();
      }
    });
  }

  public static void main(final String[] args) {
    runApp(args, App::new);

  }

}
