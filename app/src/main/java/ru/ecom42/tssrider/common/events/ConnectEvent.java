package ru.ecom42.tssrider.common.events;

import ru.ecom42.tssrider.common.utils.ServerResponse;

public class ConnectEvent extends BaseRequestEvent {
    public String token;
    public ConnectEvent(String token){
        super(new ConnectResultEvent(ServerResponse.REQUEST_TIMEOUT.getValue()));
        this.token = token;
    }
}
