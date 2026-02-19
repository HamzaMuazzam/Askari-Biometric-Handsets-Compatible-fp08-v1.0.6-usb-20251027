package com.hf.newmidx.facepass;

import android.hibory.LedApi;

public class LedManager {

    private LedApi ledApi = new LedApi();

    private static LedManager ledManager;

    public static LedManager getInstance(){

        if(ledManager==null){
            ledManager = new LedManager();
        }
        return ledManager;
    }

    public void white(){
        ledApi.ledControl(LedApi.GPIO_LED_WHITE,LedApi.LED_ON);
        ledApi.ledControl(LedApi.GPIO_LED_GREED,LedApi.LED_OFF);
        ledApi.ledControl(LedApi.GPIO_LED_RED,LedApi.LED_OFF);
    }

    public void green(){
        ledApi.ledControl(LedApi.GPIO_LED_WHITE,LedApi.LED_OFF);
        ledApi.ledControl(LedApi.GPIO_LED_GREED,LedApi.LED_ON);
        ledApi.ledControl(LedApi.GPIO_LED_RED,LedApi.LED_OFF);
    }

    public void red(){
        ledApi.ledControl(LedApi.GPIO_LED_WHITE,LedApi.LED_OFF);
        ledApi.ledControl(LedApi.GPIO_LED_GREED,LedApi.LED_OFF);
        ledApi.ledControl(LedApi.GPIO_LED_RED,LedApi.LED_ON);
    }

    public void close(){
        ledApi.ledControl(LedApi.GPIO_LED_WHITE,LedApi.LED_OFF);
        ledApi.ledControl(LedApi.GPIO_LED_GREED,LedApi.LED_OFF);
        ledApi.ledControl(LedApi.GPIO_LED_RED,LedApi.LED_OFF);
    }
}
