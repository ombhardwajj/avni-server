package org.avni.server.application;

import org.avni.server.domain.Concept;

public class TestFormElementBuilder {
    private final FormElement formElement = new FormElement();

    public TestFormElementBuilder() {
        formElement.setKeyValues(new KeyValues());
    }

    public TestFormElementBuilder withUuid(String uuid) {
        formElement.setUuid(uuid);
        return this;
    }

    public TestFormElementBuilder withId(long id) {
        formElement.setId(id);
    	return this;
    }

    public TestFormElementBuilder withConcept(Concept concept) {
        formElement.setConcept(concept);
    	return this;
    }

    public TestFormElementBuilder withQuestionGroupElement(FormElement formElement) {
        formElement.setGroup(formElement);
        return this;
    }

    public TestFormElementBuilder withRepeatable(boolean isRepeatable) {
        formElement.getKeyValues().add(new KeyValue(KeyType.repeatable, isRepeatable));
    	return this;
    }

    public FormElement build() {
        return formElement;
    }
}
