package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static ru.gb.storage.commons.message.MessageType.DIR_CREATE_REQUEST;

@Getter
public class DirCreateRequest extends AbstractMessage {

    private final String name;

    @JsonCreator
    public DirCreateRequest(@JsonProperty("name") String name) {
        this.name = name;
    }

    @Override
    public MessageType getMessageType() {
        return DIR_CREATE_REQUEST;
    }
}
