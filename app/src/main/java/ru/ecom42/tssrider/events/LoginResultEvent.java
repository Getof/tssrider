package ru.ecom42.tssrider.events;

import ru.ecom42.tssrider.common.events.BaseResultEvent;
import ru.ecom42.tssrider.common.models.Rider;

public class LoginResultEvent extends BaseResultEvent {
    public Rider rider;
    public String riderJson;
    public String jwtToken;
    public LoginResultEvent(int response, String riderJson, String jwtToken) {
        super(response);
        this.riderJson = riderJson;
        new Rider();
        this.rider = Rider.fromJson(riderJson);
        this.jwtToken = jwtToken;
    }
    public LoginResultEvent(int response, String message) {
        super(response,message);
    }
}
