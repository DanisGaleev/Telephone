package com.tastygamesstudio.phone;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ServerScreen implements Screen, GestureDetector.GestureListener {
    Label receivedMsg;
    private final String ip;
    Skin skin;
    TextField desc;
    Texture texture;
    OrthographicCamera camera;
    ImageButton send;
    Viewport viewport;
    Pixmap pixmap;
    boolean[] chunk = new boolean[Config.bytePackageCount];
    SpriteBatch batch;
    byte[] pixelData;
    byte[] bytes;
    byte i;
    private final Phone app;
    Server server;
    Stage stage;
    byte[] image;
    private Array<Integer> clientsArray;

    Texture textureUser;
    Pixmap pixmapUser;

    public ServerScreen(Phone app, String ip) {
        this.app = app;
        this.ip = ip;
    }

    @Override
    public void show() {

        pixmapUser = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGB888);
        pixmapUser.setColor(1, 1, 1, 1);
        pixmapUser.fill();
        pixmapUser.setColor(1, 1, 0, 0.1f);
        pixmapUser.fillRectangle(Config.X1, Config.Y1, Config.SIZE_X, Config.SIZE_Y);
        textureUser = new Texture(pixmapUser);

        skin = new Skin(Gdx.files.internal("skin/skin-composer-ui.json"));
        receivedMsg = new Label("", skin, "default");
        receivedMsg.setPosition(640 - receivedMsg.getWidth() / 2, 680);
        desc = new TextField("", skin, "default");
        desc.setSize(500, desc.getHeight());
        desc.setPosition(640 - desc.getWidth() / 2, 690);
        image = new byte[Config.imageSize];
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        viewport = new ScreenViewport(camera);
        stage = new Stage(viewport);
        stage.setDebugAll(true);
        stage.addActor(desc);
        stage.addActor(receivedMsg);
        InputMultiplexer inputMultiplexer = new InputMultiplexer(new GestureDetector(this), stage);
        send = new ImageButton(skin, "default");
        send.setBounds(1100, 40, 100, 100);
        send.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                send.setVisible(false);
                if ((clientsArray.size + 1) % 2 == 0) {
                    pixelData = ScreenUtils.getFrameBufferPixels(Config.X1, Gdx.graphics.getHeight() - Config.Y1 - Config.SIZE_Y, Config.SIZE_X, Config.SIZE_Y, true);
                    bytes = new byte[Config.bytePackegeSize + 1];
                    i = 0;
                    new Timer().scheduleTask(new Timer.Task() {
                        @Override
                        public void run() {
                            bytes[0] = i;
                            System.out.println(i + "  " + bytes[0] * Config.bytePackegeSize);
                            System.arraycopy(pixelData, Config.bytePackegeSize * bytes[0], bytes, 1, bytes.length - 1);
                            try {
                                server.sendToTCP(clientsArray.get(0), bytes);
                            } catch (KryoException e) {
                                e.printStackTrace();
                            }
                            i++;
                        }
                    }, Register.TIME_DELTA, Register.TIME_DELTA, Config.bytePackageCount - 1);
                    server.sendToTCP(clientsArray.get(0), "start");
                } else {
                    server.sendToTCP(clientsArray.get(0), "start" + desc.getText());
                }
            }
        });
        stage.addActor(send);
        Gdx.input.setInputProcessor(inputMultiplexer);
        pixmap = new Pixmap(Config.SIZE_X, Config.SIZE_Y, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
        clientsArray = new Array<>();
        batch = new SpriteBatch();
        server = new Server(Register.BUFFER_SIZE, Register.BUFFER_SIZE);
        Register.register(server.getKryo());
        new Thread(new Runnable() {
            @Override
            public void run() {
                server.start();
                try {
                    server.bind(Register.TCP_PORT, Register.UDP_PORT);
                    server.addListener(new Listener() {
                        @Override
                        public void connected(Connection connection) {
                            super.connected(connection);
                            clientsArray.add(connection.getID());
                        }

                        @Override
                        public void disconnected(Connection connection) {
                            super.disconnected(connection);
                            clientsArray.removeValue(connection.getID(), false);
                        }

                        @Override
                        public void received(Connection connection, Object object) {
                            super.received(connection, object);
                            if (object instanceof byte[]) {
                                if (connection.getID() != clientsArray.get(clientsArray.size - 1)) {
                                    server.sendToTCP(connection.getID() + 1, object);
                                } else {
                                    Gdx.app.postRunnable(() -> {
                                        byte[] pixelData = (byte[]) (object);
                                        chunk[pixelData[0]] = true;
                                        System.out.println(Arrays.toString(pixelData));
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

                                            texture = new Texture(pixmap);
                                        }
                                    });
                                }
                            } else if (object instanceof String) {
                                String msg = (String) object;
                                if (connection.getID() != clientsArray.get(clientsArray.size - 1)) {
                                    server.sendToTCP(connection.getID() + 1, "start" + msg);
                                } else
                                    receivedMsg.setText(msg);
                            }
                        }

                        @Override
                        public void idle(Connection connection) {
                            super.idle(connection);
                        }
                    });
                } catch (IOException e) {
                    server.close();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void render(float delta) {
        camera.update();
        ScreenUtils.clear(1, 1, 1, 1);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (!send.isVisible())
            batch.draw(texture, 40, 40);
        else
            batch.draw(textureUser, 0, 0);
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
        server.close();
        try {
            server.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (y > Config.Y1 && y < Config.Y2 && x < Config.X2 && x > Config.X1) {
            pixmapUser.setColor(Color.BLACK);
            pixmapUser.fillCircle((int) (x), (int) (y), 10);
            textureUser.draw(pixmapUser, 0, 0);
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (y > Config.Y1 && y < Config.Y2 && x < Config.X2 && x > Config.X1) {
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
        if (y > Config.Y1 && y < Config.Y2 && x < Config.X2 && x > Config.X1) {
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
