package com.tastygamesstudio.phone;

import com.badlogic.gdx.Game;
import com.tastygamesstudio.phone.screens.MenuScreen;

public class Phone extends Game {

    public static String ip;

    public Phone(String ip){
        Phone.ip = ip;
    }

    @Override
    public void create() {
        setScreen(new MenuScreen(this, ip));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
