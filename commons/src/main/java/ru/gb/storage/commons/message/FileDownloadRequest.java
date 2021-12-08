package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.nio.file.Path;

import static ru.gb.storage.commons.message.MessageType.FILE_DOWNLOAD_REQUEST;

@Getter
public class FileDownloadRequest extends AbstractMessage {
    private final String fileName;
    private final Path savePath;

    @JsonCreator
    public FileDownloadRequest(@JsonProperty("fileName") String fileName,
                               @JsonProperty("savePath") Path savePath) {
        this.fileName = fileName;
        this.savePath = savePath;
    }

    @Override
    public MessageType getMessageType() {
        return FILE_DOWNLOAD_REQUEST;
    }
}
