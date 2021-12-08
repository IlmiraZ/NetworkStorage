package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static ru.gb.storage.commons.message.MessageType.AUTH_RESPONSE;

@Getter
public class AuthResponse extends AbstractMessage {

    private final String userName;
    private final int resultCode;
    private final String resultMessage;

    @JsonCreator
    public AuthResponse(@JsonProperty("userName") String userName,
                        @JsonProperty("resultCode") int resultCode,
                        @JsonProperty("resultMessage") String resultMessage) {
        this.userName = userName;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    @Override
    public MessageType getMessageType() {
        return AUTH_RESPONSE;
    }
}
