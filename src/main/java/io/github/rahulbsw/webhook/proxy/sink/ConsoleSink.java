package io.github.rahulbsw.webhook.proxy.sink;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.rahulbsw.webhook.proxy.Message;
import io.github.rahulbsw.webhook.proxy.Transformer;
import io.github.rahulbsw.webhook.proxy.sink.exception.SinkFailureException;

import java.util.Map;
import java.util.Objects;

public class ConsoleSink extends AbstractSink<String,String>  {


    protected ConsoleSink(Map<String, Object> param, Transformer transformer) {
        super(param,transformer);
    }

    @Override
    public Message<String,String> post(JsonNode value) throws SinkFailureException {
        System.out.println(value);
        return new Message.Builder<String,String>().put("Message","Successfully posted").build();
    }
}
