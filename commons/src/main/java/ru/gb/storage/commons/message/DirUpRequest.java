package ru.gb.storage.commons.message;

import static ru.gb.storage.commons.message.MessageType.DIR_UP_REQUEST;

public class DirUpRequest extends AbstractMessage {
    @Override
    public MessageType getMessageType() {
        return DIR_UP_REQUEST;
    }
}
