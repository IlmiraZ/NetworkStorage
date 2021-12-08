package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.MINIMAL_CLASS,
        property = "type"
)
public abstract class AbstractMessage {

    @JsonGetter
    public abstract MessageType getMessageType();
}
