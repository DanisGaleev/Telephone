package com.tastygamesstudio.phone.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tastygamesstudio.phone.ClientScreen;
import com.tastygamesstudio.phone.Phone;
import com.tastygamesstudio.phone.ServerScreen;

import java.util.Arrays;

public class MenuScreen implements Screen {

    private final Phone app;
    private final String ip;
    private Stage stage;
    private TextField name;
    private TextField serverIPField;
    private SpriteBatch batch;

    public MenuScreen(final Phone app, String ip) {
        this.app = app;
        this.ip = ip;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        Viewport viewport = new StretchViewport(1280, 720);
        stage = new Stage(viewport);
        stage.setDebugAll(true);

        Skin skin = new Skin(Gdx.files.internal("skin/skin-composer-ui.json"));

        name = new TextField("defaultname", skin, "default");
        name.setBounds(490, 600, 300, 40);
        stage.addActor(name);

        serverIPField = new TextField("0.0.0.0", skin, "default");
        serverIPField.setBounds(490, 500, 300, 40);
        stage.addActor(serverIPField);

        Label serverIP = new Label(ip, skin, "default");
        serverIP.setPosition(30, 690);
        stage.addActor(serverIP);

        TextButton client = new TextButton("Start as client", skin, "default");
        client.setBounds(440, 250, 400, 70);
        client.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!name.getText().equals("") && !serverIPField.getText().equals("")) {
                    String[] ss = serverIPField.getText().split("\\.");
                    System.out.println(serverIPField.getText() + Arrays.toString(ss));
                    if (ss.length == 4) {
                        try {
                            if (Integer.parseInt(ss[0]) >= 0 && Integer.parseInt(ss[0]) <= 255 &&
                                    Integer.parseInt(ss[1]) >= 0 && Integer.parseInt(ss[1]) <= 255 &&
                                    Integer.parseInt(ss[2]) >= 0 && Integer.parseInt(ss[2]) <= 255 &&
                                    Integer.parseInt(ss[3]) >= 0 && Integer.parseInt(ss[3]) <= 255) {
                                app.setScreen(new ClientScreen(serverIPField.getText()));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    //here will be something if ip not correct
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        stage.addActor(client);

        TextButton server = new TextButton("Start as server", skin, "default");
        server.setBounds(440, 160, 400, 70);
        server.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!name.getText().equals(""))
                    app.setScreen(new ServerScreen());
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        stage.addActor(server);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 1, 1, 1);
        batch.begin();
        batch.end();
        stage.act();
        stage.draw();
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
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
