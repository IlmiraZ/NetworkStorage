package ru.gb.storage.client.netty;

import ru.gb.storage.commons.message.AbstractMessage;

public interface CallBack {
    void call(AbstractMessage message);
}
