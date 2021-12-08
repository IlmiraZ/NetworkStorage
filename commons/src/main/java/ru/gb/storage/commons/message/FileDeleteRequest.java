package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static ru.gb.storage.commons.message.MessageType.FILE_DELETE_REQUEST;

@Getter
public class FileDeleteRequest extends AbstractMessage {

    private final String name;

    @JsonCreator
    public FileDeleteRequest(@JsonProperty("name") String name) {
        this.name = name;
    }

    @Override
    public MessageType getMessageType() {
        return FILE_DELETE_REQUEST;
    }
}
