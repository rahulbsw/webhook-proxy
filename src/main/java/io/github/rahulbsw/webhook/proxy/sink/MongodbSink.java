package io.github.rahulbsw.webhook.proxy.sink;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.rahulbsw.webhook.proxy.Message;
import io.github.rahulbsw.webhook.proxy.Transformer;
import io.github.rahulbsw.webhook.proxy.sink.exception.SinkFailureException;

import java.util.Map;

//todo:implemention
public class MongodbSink extends AbstractSink{
    protected MongodbSink(Map param, Transformer transformer) {
        super(param, transformer);
    }

    @Override
    public Message post(JsonNode value) throws SinkFailureException {
        return null;
    }
}
