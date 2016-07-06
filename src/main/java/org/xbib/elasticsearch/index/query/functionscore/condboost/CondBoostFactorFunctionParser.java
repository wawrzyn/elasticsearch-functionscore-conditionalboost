
package org.xbib.elasticsearch.index.query.functionscore.condboost;

import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParsingException;
import org.elasticsearch.index.query.functionscore.ScoreFunctionParser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;

/**
 * Parses out a function_score function that looks like:
 *
 * <pre>
 *     {
 *         "cond_boost": {
 *             "value" : 1.0,
 *             "factor" : 1.0,
 *             "modifier" : "NONE",
 *             "cond" :  {
 *                 "fieldName" : "product",
 *                 "fieldValues" : ["product_name_1"],
 *                 "value" : 5.0
 *              }
 *         }
 *     }
 * </pre>
 */
public class CondBoostFactorFunctionParser implements ScoreFunctionParser {

    static String[] NAMES = { "cond_boost", "condBoost" };

    @Override
    public String[] getNames() {
        return NAMES;
    }

    @Override
    public ScoreFunction parse(QueryParseContext parseContext, XContentParser parser) throws IOException, QueryParsingException {
        String currentFieldName = null;
        CondBoostEntry condBoost = new CondBoostEntry();
        float defaultBoost = 1.0f;
        float boostFactor = 1.0f;
        CondBoostFactorFunction.Modifier modifier = CondBoostFactorFunction.Modifier.NONE;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            }else {
                if (currentFieldName != null) {
                    switch (currentFieldName) {
                        case CondBoostEntry.BOOST:
                            defaultBoost = parser.floatValue();
                            break;
                        case "factor":
                            boostFactor = parser.floatValue();
                            break;
                        case "modifier":
                            modifier = CondBoostFactorFunction.Modifier.valueOf(parser.text().toUpperCase(Locale.ROOT));
                            break;
                        case "cond":
                            condBoost = parseCond(parseContext, parser, currentFieldName);
                            break;
                        default:
                            throw new QueryParsingException(parseContext, NAMES[0] + " query does not support [" + currentFieldName + "]");
                    }
                }
            }
        }
        return new CondBoostFactorFunction(parseContext,condBoost, defaultBoost, boostFactor, modifier);
    }

    private CondBoostEntry parseCond(QueryParseContext parseContext, XContentParser parser, String currentFieldName) throws IOException {
        XContentParser.Token token;
        CondBoostEntry entry = new CondBoostEntry();

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else {
                switch(currentFieldName){
                    case (CondBoostEntry.BOOST):
                        entry.boost = parser.floatValue();
                        break;
                    case ("fieldName"):
                        entry.fieldName = parser.text();
                        break;
                    case ("fieldValues"):
                        int i = 6;
                        HashSet<String> fieldValueList = new HashSet<String>();
                        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                            if (token.isValue()){
                                String fieldValue = parser.text();
                                fieldValueList.add(fieldValue);
                            }
                        }
                        entry.fieldValueList = fieldValueList;
                        break;
                }

            }
        }

        return entry;
    }

}
