package io.github.rahulbsw.webhook.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;

import java.util.Objects;

public class Transformer {

    Expression jslt;

    boolean doTransform=true;

    public Transformer(String spec){
        if(Objects.nonNull(spec))
            {
                jslt = Parser.compileString(spec);
           }else{
                doTransform=false;
            }
    }


    public JsonNode transform(JsonNode input){
        if(doTransform && Objects.nonNull(jslt)) {
            return jslt.apply(input);
        }
        return input;
    }
}
