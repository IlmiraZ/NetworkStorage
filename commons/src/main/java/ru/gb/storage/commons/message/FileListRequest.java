package ru.gb.storage.commons.message;

import static ru.gb.storage.commons.message.MessageType.FILE_LIST_REQUEST;

public class FileListRequest extends AbstractMessage {

    @Override
    public MessageType getMessageType() {
        return FILE_LIST_REQUEST;
    }
}
