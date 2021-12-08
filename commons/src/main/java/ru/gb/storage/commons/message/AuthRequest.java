package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static ru.gb.storage.commons.message.MessageType.AUTH_REQUEST;

@Getter
public class AuthRequest extends AbstractMessage {
    private final String login;
    private final String password;

    @JsonCreator
    public AuthRequest(@JsonProperty("login") String login,
                       @JsonProperty("password") String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public MessageType getMessageType() {
        return AUTH_REQUEST;
    }
}
