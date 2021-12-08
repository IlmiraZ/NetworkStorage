package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static ru.gb.storage.commons.message.MessageType.FILE_UPLOAD_REQUEST;

@Getter
public class FileUploadRequest extends AbstractMessage {
    private final String name;
    private final byte[] content;
    private final long position;
    private final boolean endOfFile;

    @JsonCreator
    public FileUploadRequest(@JsonProperty("name") String name,
                             @JsonProperty("content") byte[] content,
                             @JsonProperty("position") long position,
                             @JsonProperty("endOfFile") boolean endOfFile) {
        this.name = name;
        this.content = content;
        this.position = position;
        this.endOfFile = endOfFile;
    }

    @Override
    public MessageType getMessageType() {
        return FILE_UPLOAD_REQUEST;
    }
}
