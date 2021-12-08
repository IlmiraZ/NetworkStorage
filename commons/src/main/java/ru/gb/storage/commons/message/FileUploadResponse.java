package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static ru.gb.storage.commons.message.MessageType.FILE_UPLOAD_RESPONSE;

@Getter
public class FileUploadResponse extends AbstractMessage {

    private final int resultCode;
    private final String resultMessage;

    @JsonCreator
    public FileUploadResponse(@JsonProperty("resultCode") int resultCode,
                              @JsonProperty("resultMessage") String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    @Override
    public MessageType getMessageType() {
        return FILE_UPLOAD_RESPONSE;
    }
}
