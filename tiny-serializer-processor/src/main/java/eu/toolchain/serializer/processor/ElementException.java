package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Element;

import com.google.common.collect.ImmutableList;

public class ElementException extends Exception {
    private final List<String> messages;
    private final Optional<Element> element;

    public ElementException(String message) {
        this(ImmutableList.of(message), Optional.empty());
    }

    public ElementException(String message, Element element) {
        this(ImmutableList.of(message), Optional.of(element));
    }

    public ElementException(List<String> messages, Element element) {
        this(messages, Optional.of(element));
    }

    public ElementException(List<String> messages, Optional<Element> element) {
        super("Error in element");
        this.messages = messages;
        this.element = element;
    }

    public Optional<Element> getElement() {
        return element;
    }

    public List<String> getMessages() {
        return messages;
    }
}
