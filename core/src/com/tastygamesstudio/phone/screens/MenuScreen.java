package com.tastygamesstudio.phone.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tastygamesstudio.phone.ClientScreen;
import com.tastygamesstudio.phone.FreeTypeSkin;
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
    private Preferences preferences;

    public MenuScreen(final Phone app, String ip) {
        this.app = app;
        this.ip = ip;
    }

    @Override
    public void show() {
        preferences = Gdx.app.getPreferences("Settings");
        batch = new SpriteBatch();
        Viewport viewport = new StretchViewport(1280, 720);
        stage = new Stage(viewport);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
                + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
                + "1234567890.,:;_¡!¿?\"'+-*/()[]={}";

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = generator.generateFont(parameter);
        textFieldStyle.fontColor = Color.BLACK;
        textFieldStyle.background = new TextureRegionDrawable(new Texture("ui/textField_background.png"));
        textFieldStyle.selection = new TextureRegionDrawable(new Texture("ui/selection.png"));
        textFieldStyle.cursor = new TextureRegionDrawable(new Texture("ui/cursor.png"));

        name = new TextField(preferences.getString("com.tastygamesstudio.phone.name", "defaultname"), textFieldStyle);
        name.setMaxLength(12);
        name.setBounds(490, 600, 300, 50);
        stage.addActor(name);

        serverIPField = new TextField("0.0.0.0", textFieldStyle);
        serverIPField.setBounds(490, 500, 300, 50);
        stage.addActor(serverIPField);

        parameter.size = 18;
        Label.LabelStyle labelStyle = new Label.LabelStyle(generator.generateFont(parameter), Color.BLACK);
        Label serverIP = new Label(ip, labelStyle);
        serverIP.setPosition(30, 690);
        stage.addActor(serverIP);

        ImageButton client = new ImageButton(new TextureRegionDrawable(new Texture("ui/client_up.png")),
                new TextureRegionDrawable(new Texture("ui/client_down.png")),
                new TextureRegionDrawable(new Texture("ui/client_down.png")));
        client.setPosition(640 - client.getWidth() / 2, 250);
        client.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!name.getText().equals("") && !serverIPField.getText().equals("")) {
                    String[] ss = serverIPField.getText().split("\\.");
                    System.out.println(serverIPField.getText() + Arrays.toString(ss));
                    if (ss.length == 4) {
                        try {
                            if (Integer.parseInt(ss[0]) >= 0 && Integer.parseInt(ss[0]) <= 255 &&
                                    Integer.parseInt(ss[1]) >= 0 && Integer.parseInt(ss[1]) <= 255 &&
                                    Integer.parseInt(ss[2]) >= 0 && Integer.parseInt(ss[2]) <= 255 &&
                                    Integer.parseInt(ss[3]) >= 0 && Integer.parseInt(ss[3]) <= 255) {
                                preferences.putString("com.tastygamesstudio.phone.name", name.getText());
                                preferences.flush();
                                app.setScreen(new ClientScreen(serverIPField.getText(), name.getText()));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    //here will be something if ip isn't correct
                }
            }
        });
        stage.addActor(client);
        //ImageButton clien = new ImageButton()
        /*TextButton client = new TextButton("Start as client", skin, "default");
        client.setBounds(440, 250, 400, 70);
        client.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

            }
        });
        stage.addActor(client);
         */
        ImageButton server = new ImageButton(new TextureRegionDrawable(new Texture("ui/server_up.png")),
                new TextureRegionDrawable(new Texture("ui/server_down.png")),
                new TextureRegionDrawable(new Texture("ui/server_down.png")));
        server.setPosition(640 - server.getWidth() / 2, 140);
        server.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!name.getText().equals("")) {
                    preferences.putString("com.tastygamesstudio.phone.name", name.getText());
                    preferences.flush();
                    app.setScreen(new ServerScreen(ip));
                }
            }
        });
        stage.addActor(server);
        /*TextButton server = new TextButton("Start as server", skin, "default");
        server.setBounds(440, 160, 400, 70);
        server.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

            }
        });
        stage.addActor(server);
         */

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
