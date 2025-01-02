package org.avni.server.domain.metabase;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.avni.server.util.ObjectMapperSingleton.getObjectMapper;

public class FilterCondition {
    private final ConditionType operator;
    private final int fieldId;
    private final String baseType;
    private final Object value;

    public FilterCondition(ConditionType operator, int fieldId, String baseType, Object value) {
        this.operator = operator;
        this.fieldId = fieldId;
        this.baseType = baseType;
        this.value = value;
    }

    public ConditionType getOperator() {
        return operator;
    }

    public int getFieldId() {
        return fieldId;
    }

    public String getBaseType() {
        return baseType;
    }

    public Object getValue() {
        return value;
    }

    public ArrayNode toJson() {
        ArrayNode filterArray = getObjectMapper().createArrayNode();
        filterArray.add(operator.getOperator());

        ArrayNode fieldArray = getObjectMapper().createArrayNode();
        fieldArray.add(QueryAttribute.FIELD.getValue());
        fieldArray.add(fieldId);

        ObjectNode fieldDetails = getObjectMapper().createObjectNode();
        fieldDetails.put(QueryAttribute.BASE_TYPE.getValue(), baseType);

        fieldArray.add(fieldDetails);
        filterArray.add(fieldArray);

        if (value instanceof String[]) {
            for (String val : (String[]) value) {
                filterArray.add(val);
            }
        } else {
            filterArray.addPOJO(value);
        }
        return filterArray;

    }
}
