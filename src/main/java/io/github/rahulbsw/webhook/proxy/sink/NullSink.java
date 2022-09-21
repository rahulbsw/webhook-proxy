package io.github.rahulbsw.webhook.proxy.sink;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.rahulbsw.webhook.proxy.Message;
import io.github.rahulbsw.webhook.proxy.Transformer;
import io.github.rahulbsw.webhook.proxy.sink.exception.SinkFailureException;

import java.util.Map;
import java.util.Objects;

public class NullSink extends AbstractSink {


    protected NullSink(Map<String, Object> param, Transformer transformer) {
        super(param,transformer);
    }

    @Override
    public Message<String,String> post(JsonNode value) throws SinkFailureException {
        return null;
    }
}
