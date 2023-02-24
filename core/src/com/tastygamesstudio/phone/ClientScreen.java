package com.tastygamesstudio.phone;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ClientScreen implements Screen, GestureDetector.GestureListener {
    private final String ip;
    byte[] bytes;
    Texture textureUser, texture;
    byte[] pixelData;
    boolean isStarted;
    SpriteBatch batch;
    boolean[] chunk = new boolean[Config.bytePackageCount];
    byte[] image;
    Client client;
    Pixmap pixmapUser, pixmap;
    ImageButton send;
    Stage stage;
    Label receivedDesc;
    private Phone app;
    byte i;
    FreeTypeFontGenerator font;
    Viewport viewport;
    OrthographicCamera camera;
    Skin skin;
    TextField desc;

    public ClientScreen(Phone app, String ip) {
        this.app = app;
        this.ip = ip;
    }

    @Override
    public void show() {
        font = new FreeTypeFontGenerator(Gdx.files.internal("Red October.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 15;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        image = new byte[Config.imageSize];
        viewport = new ScreenViewport(camera);
        stage = new Stage(viewport);
        stage.setDebugAll(true);
        skin = new Skin(Gdx.files.internal("skin/skin-composer-ui.json"));
        receivedDesc = new Label("", skin, "default");
        receivedDesc.setPosition(640 - receivedDesc.getWidth() / 2, 680);
        stage.addActor(receivedDesc);
        send = new ImageButton(skin, "default");
        send.setBounds(1100, 40, 100, 100);
        send.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                send.setVisible(false);
                pixelData = ScreenUtils.getFrameBufferPixels(Config.X1, Config.Y1, Config.SIZE_X, Config.SIZE_Y, true);
                i = 0;
                isStarted = false;
                new Timer().scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        bytes[0] = i;
                        System.arraycopy(pixelData, Config.bytePackegeSize * bytes[0], bytes, 1, bytes.length - 1);
                        try {
                            client.sendTCP(bytes);
                        } catch (KryoException e) {
                            try {
                                client.reconnect();
                                client.sendTCP(bytes);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                        i++;
                    }
                }, Register.TIME_DELTA, Register.TIME_DELTA, Config.bytePackageCount - 1);
            }
        });
        stage.addActor(send);

        desc = new TextField("", skin, "default");
        desc.setSize(500, desc.getHeight());
        desc.setPosition(640 - desc.getWidth() / 2, 690);
        //camera.setToOrtho(false, Config.SCREEN_SIZE_X, Config.SCREEN_SIZE_Y);
        pixmapUser = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGB888);
        pixmapUser.setColor(1, 1, 1, 1);
        pixmapUser.fill();
        pixmapUser.setColor(1, 1, 0, 0.1f);
        pixmapUser.fillRectangle(Config.X1, Config.Y1, Config.SIZE_X, Config.SIZE_Y);
        textureUser = new Texture(pixmapUser);
        InputMultiplexer inputMultiplexer = new InputMultiplexer(new GestureDetector(this), stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        client = new Client(Register.BUFFER_SIZE, Register.BUFFER_SIZE);
        Register.register(client.getKryo());
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.start();
                try {
                    client.connect(Register.TIMEOUT, ip, Register.TCP_PORT, Register.UDP_PORT);
                } catch (IOException e) {
                    client.close();
                    e.printStackTrace();
                }
                client.addListener(new Listener() {
                    @Override
                    public void connected(Connection connection) {
                        super.connected(connection);
                    }

                    @Override
                    public void disconnected(Connection connection) {
                        super.disconnected(connection);
                    }

                    @Override
                    public void received(Connection connection, Object object) {
                        super.received(connection, object);
                        if (object instanceof String) {
                            String s = (String) object;
                            if (s.startsWith("start")) {
                                isStarted = true;
                                new Timer().scheduleTask(new Timer.Task() {
                                    @Override
                                    public void run() {
                                        receivedDesc.remove();
                                        desc.remove();
                                    }
                                }, 20);
                                new Timer().scheduleTask(new Timer.Task() {
                                    @Override
                                    public void run() {
                                        pixelData = ScreenUtils.getFrameBufferPixels(Config.X1, Gdx.graphics.getHeight() - Config.Y1 - Config.SIZE_Y, Config.SIZE_X, Config.SIZE_Y, true);
                                        bytes = new byte[Config.bytePackegeSize + 1];
                                        i = 0;
                                        new Timer().scheduleTask(new Timer.Task() {
                                            @Override
                                            public void run() {
                                                bytes = null;
                                                bytes = new byte[Config.bytePackegeSize + 1];
                                                bytes[0] = i;
                                                System.arraycopy(pixelData, Config.bytePackegeSize * bytes[0], bytes, 1, bytes.length - 1);
                                                try {
                                                    client.sendTCP(bytes);
                                                } catch (KryoException e) {
                                                    try {
                                                        client.reconnect();
                                                        client.sendTCP(bytes);
                                                    } catch (IOException ioException) {
                                                        ioException.printStackTrace();
                                                    }
                                                }
                                                i++;

                                            }
                                        }, Register.TIME_DELTA, Register.TIME_DELTA, Config.bytePackageCount - 1);
                                    }
                                }, 30);
                                stage.addActor(receivedDesc);
                                receivedDesc.setText(s.substring(5));
                            }
                        } else if (object instanceof byte[]) {
                            Gdx.app.postRunnable(() -> {
                                byte[] pixelData = (byte[]) (object);
                                chunk[pixelData[0]] = true;
                                System.out.println(pixelData[0]);
                                System.arraycopy(pixelData, 1, image, pixelData[0] * Config.bytePackegeSize, pixelData.length - 1);
                                boolean isAllPackageReceived = false;
                                for (boolean b : chunk) {
                                    if (!b) {
                                        isAllPackageReceived = true;
                                        break;
                                    }
                                }
                                if (!isAllPackageReceived) {
                                    Arrays.fill(chunk, false);
                                    Pixmap pixmap = new Pixmap(Config.SIZE_X, Config.SIZE_Y, Pixmap.Format.RGBA8888);
                                    ByteBuffer pixels = pixmap.getPixels();
                                    pixels.clear();
                                    pixels.put(image);
                                    pixels.position(0);
                                    stage.addActor(desc);
                                    new Timer().scheduleTask(new Timer.Task() {
                                        @Override
                                        public void run() {
                                            client.sendTCP(desc.getText());
                                        }
                                    }, 20);
                                    texture = new Texture(pixmap);
                                }
                            });
                        }
                    }

                    @Override
                    public void idle(Connection connection) {
                        super.idle(connection);
                    }
                });
            }
        }).start();
        batch = new SpriteBatch();
        camera.position.set(640, 360, 0);
    }

    @Override
    public void render(float delta) {
        camera.update();
        ScreenUtils.clear(1, 1, 1, 1);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (texture != null) {
            System.out.println(texture.getWidth() + "  " + texture.getHeight());
            batch.draw(texture, 40, 40);
        }
        else {
            System.out.println(textureUser.getWidth() + "  " + textureUser.getHeight());

            batch.draw(textureUser, 0, 0);
        }
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
        batch.dispose();
        client.close();
        texture.dispose();
        textureUser.dispose();
        pixmapUser.dispose();
        pixmap.dispose();
        try {
            client.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (isStarted && y > Config.Y1 && y < Config.Y2 && x < Config.X2 && x > Config.X1) {
            pixmapUser.setColor(Color.BLACK);
            pixmapUser.fillCircle((int) (x), (int) (y), 10);
            textureUser.draw(pixmapUser, 0, 0);
        }
        return false;

    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (isStarted && y > Config.Y1 && y < Config.Y2 && x < Config.X2 && x > Config.X1) {
            pixmapUser.setColor(Color.BLACK);
            pixmapUser.fillCircle((int) (x), (int) (y), 10);
            textureUser.draw(pixmapUser, 0, 0);
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (isStarted && y > Config.Y1 && y < Config.Y2 && x < Config.X2 && x > Config.X1) {
            pixmapUser.setColor(Color.BLACK);
            pixmapUser.fillCircle((int) (x), (int) (y), 10);
            textureUser.draw(pixmapUser, 0, 0);
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
}
