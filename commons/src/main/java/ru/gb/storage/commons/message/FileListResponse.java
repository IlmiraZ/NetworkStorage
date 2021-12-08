package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.gb.storage.commons.file.FileInfo;

import java.util.List;

import static ru.gb.storage.commons.message.MessageType.FILE_LIST_RESPONSE;


public class FileListResponse extends AbstractMessage {

    private final List<FileInfo> fileInfoList;

    @JsonCreator
    public FileListResponse(@JsonProperty("fileInfoList") List<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
    }

    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    @Override
    public MessageType getMessageType() {
        return FILE_LIST_RESPONSE;
    }
}
