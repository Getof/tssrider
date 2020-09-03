package ru.ecom42.tssrider.common.events;

public class ConnectResultEvent extends BaseResultEvent {
    public ConnectResultEvent(int code) {
        super(code);
    }
    public ConnectResultEvent(int code,String message) {
        super(code,message);
    }
}
