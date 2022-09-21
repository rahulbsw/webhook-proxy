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
import io.jooby.Environment;
import io.jooby.Jooby;
import io.jooby.MediaType;
import io.jooby.RateLimitHandler;
import io.jooby.StatusCode;
import io.jooby.json.JacksonModule;
import io.jooby.metrics.MetricsModule;
import io.jooby.rocker.RockerModule;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class App extends Jooby {

  {
    Environment env = getEnvironment();
    Config conf = env.getConfig();
    Store.init(conf.getString("db.file"));
    ObjectMapper objectMapper=new ObjectMapper();
    install(new JacksonModule(objectMapper));
    install(new RockerModule());

    //install(new JacksonModule(new XmlMapper()));
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


    assets("/css/*", Paths.get("public", "css"));
    assets("/images/*", Paths.get("public", "images"));
    assets("/js/*", Paths.get("public", "js"));
    assets("/view/*", Paths.get("public", "view"));
    assets("/index.html", Paths.get("public", "index.html"));
    assets("/favicon.ico",Paths.get("public", "images","favicon.ico"));
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
        Message message=Store.process(name,ctx.body(JsonNode.class));
        ctx.setResponseCode(StatusCode.OK);
        return message;
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
