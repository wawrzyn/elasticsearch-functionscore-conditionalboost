package org.xbib.elasticsearch.index.query.functionscore.condboost;

import com.google.common.base.Joiner;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.HashSet;

class CondBoostEntry implements ToXContent {
    public final static String BOOST = "value";

    String fieldName;

    HashSet<String> fieldValueList;

    float boost;

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject("cond");
        builder.field("fieldName", fieldName);

        if (fieldValueList != null) {
            builder.startArray("fieldValues");
            for (String entry : fieldValueList) {
                builder.value(entry);
            }
            builder.endArray();
        }
        builder.field(BOOST, boost);
        builder.endObject();
        return builder;
    }

    public String toString() {
        String fieldValuesJoined = Joiner.on(",").join(fieldValueList);
        return "{fieldName = " + fieldName + ", fieldValues = [" + fieldValuesJoined + " ], " + BOOST + "=" + boost + "}";
    }
}