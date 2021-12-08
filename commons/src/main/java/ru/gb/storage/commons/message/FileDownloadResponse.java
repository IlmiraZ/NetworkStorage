package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.nio.file.Path;

import static ru.gb.storage.commons.message.MessageType.FILE_DOWNLOAD_RESPONSE;

@Getter
public class FileDownloadResponse extends AbstractMessage {
    private final Path savePath;
    private final byte[] content;
    private final long position;
    private final boolean endOfFile;

    @JsonCreator
    public FileDownloadResponse(@JsonProperty("savePath") Path savePath,
                                @JsonProperty("content") byte[] content,
                                @JsonProperty("position") long position,
                                @JsonProperty("endOfFile") boolean endOfFile) {
        this.savePath = savePath;
        this.content = content;
        this.position = position;
        this.endOfFile = endOfFile;
    }

    @Override
    public MessageType getMessageType() {
        return FILE_DOWNLOAD_RESPONSE;
    }
}
