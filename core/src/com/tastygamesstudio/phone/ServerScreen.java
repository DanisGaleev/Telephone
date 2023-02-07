package com.tastygamesstudio.phone;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ServerScreen implements Screen, GestureDetector.GestureListener {
    private final String ip;
    Skin skin;
    Texture texture;
    OrthographicCamera camera;
    ImageButton start;
    ImageButton send;
    Viewport viewport;
    Pixmap pixmap;
    boolean[] chunk = new boolean[20];
    SpriteBatch batch;
    byte[] pixelData;
    byte[] bytes;
    byte i;
    private final Phone app;
    Server server;
    Stage stage;
    Client client;
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

        pixmapUser = new Pixmap(1280, 720, Pixmap.Format.RGB888);
        pixmapUser.setColor(1, 1, 1, 1);
        pixmapUser.fill();
        textureUser = new Texture(pixmapUser);

        skin = new Skin(Gdx.files.internal("skin/skin-composer-ui.json"));
        image = new byte[1280 * 720 * 4];
        camera = new OrthographicCamera(1280, 720);
        camera.setToOrtho(false, 1280, 720);
        viewport = new StretchViewport(1280, 720);
        stage = new Stage(viewport);
        InputMultiplexer inputMultiplexer = new InputMultiplexer(new GestureDetector(this), stage);
        start = new ImageButton(skin, "default");
        start.setBounds(10, 10, 100, 100);
        start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                server.sendToTCP(clientsArray.get(0), "start");
                start.setVisible(false);
            }
        });
        send = new ImageButton(skin, "default");
        send.setBounds(1100, 40, 100, 100);
        send.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                pixelData = ScreenUtils.getFrameBufferPixels(0, 0, 1280, 720, true);
                bytes = new byte[184321];
                i = 0;
                new Timer().scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        bytes = null;
                        bytes = new byte[184321];
                        bytes[0] = i;
                        System.arraycopy(pixelData, 184320 * bytes[0], bytes, 1, bytes.length - 1);
                        System.out.println(bytes[0]);
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
                }, 3 * 1, 1, 19);
            }
        });
        stage.addActor(start);
        stage.addActor(send);
        Gdx.input.setInputProcessor(inputMultiplexer);
        pixmap = new Pixmap(1280, 720, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
        clientsArray = new Array<>();
        batch = new SpriteBatch();
        server = new Server(184400, 184400);
        server.getKryo().register(Message.class);
        server.getKryo().register(Click.class);
        server.getKryo().register(int[].class);
        server.getKryo().register(int[][].class);
        server.getKryo().register(Vector2.class);
        server.getKryo().register(float.class);
        server.getKryo().register(float.class);
        server.getKryo().register(String.class);
        server.getKryo().register(byte[].class);
        client = new Client(184400, 184400);

        client.getKryo().register(Message.class);
        client.getKryo().register(Click.class);
        client.getKryo().register(int[].class);
        client.getKryo().register(int[][].class);
        client.getKryo().register(Vector2.class);
        client.getKryo().register(float.class);
        client.getKryo().register(float.class);
        client.getKryo().register(String.class);
        client.getKryo().register(byte[].class);
        new Thread(new Runnable() {
            @Override
            public void run() {
                server.start();
                try {
                    server.bind(5555, 6666);
                    server.addListener(new Listener() {
                        @Override
                        public void connected(Connection connection) {
                            super.connected(connection);
                            clientsArray.add(connection.getID());
                            System.out.println("Client connected : " + connection.getID());
                        }

                        @Override
                        public void disconnected(Connection connection) {
                            super.disconnected(connection);
                            clientsArray.removeValue(connection.getID(), false);
                            System.out.println("Client disconnected : " + connection.getID());
                        }

                        @Override
                        public void received(Connection connection, Object object) {
                            super.received(connection, object);
                            if (object instanceof byte[]) {
                                Gdx.app.postRunnable(() -> {
                                    System.out.println("ggggg");
                                    byte[] pixelData = (byte[]) (object);
                                    chunk[pixelData[0]] = true;
                                    System.out.println(Arrays.toString(pixelData));
                                    ///System.arraycopy(pixelData, 1, image, pixelData[0] * 184320 + 1, pixelData.length - 1);
                                    System.arraycopy(pixelData, 1, image, pixelData[0] * 184320, pixelData.length - 1);
                                    //System.arraycopy(pixelData, 1, image, pixelData[0] * 184320, pixelData.length - 1);
                                    boolean is = false;
                                    for (boolean b : chunk) {
                                        if (!b) {
                                            is = true;
                                            break;
                                        }
                                    }
                                    if (!is) {
                                        Arrays.fill(chunk, false);
                                        System.out.println(image[0] + "  " + image[1]);
                                        System.out.println(image[184320] + "  " + image[184321]);
                                        System.out.println("ttttt");
                                        for (int i = 0; i < 50; i++) {
                                            System.out.print(image[i] + "  ");
                                        }
                                        System.out.println();
                                        for (int i = 184320; i < 184320 + 50; i++) {
                                            System.out.print(image[i] + "   ");
                                        }
                                        Pixmap pixmap = new Pixmap(1280, 720, Pixmap.Format.RGBA8888);
                                        ByteBuffer pixels = pixmap.getPixels();
                                        pixels.clear();
                                        pixels.put(image);
                                        pixels.position(0);

                                        texture = new Texture(pixmap);
                                        //pixmap.dispose();
                                        ////texture.draw(pixmap, 0, 0);
                                    }
                                    /*Pixmap pixmap = new Pixmap(1280, 720 / 20, Pixmap.Format.RGBA8888);
                                    ByteBuffer pixels = pixmap.getPixels();
                                    pixels.clear();
                                    pixels.put(pixelData, 1, pixelData.length - 1);
                                    pixels.position(0);

                                    //texture = new Texture(pixmap);
                                    if (!texture.getTextureData().isPrepared()) {
                                        texture.getTextureData().prepare();
                                    }
                                    Pixmap map = texture.getTextureData().consumePixmap();
                                    System.out.println(map.isDisposed());
                                    map.drawPixmap(pixmap, 0, pixelData[0] * 36);
                                    texture.draw(map, 0, 0);
                                    */
                                    /////////////texture.draw(pixmap, 0, pixelData[0] * 36);
                                    //pixmap.dispose();
                                    /*
                                    byte[] array = (byte[]) (object);
                                    System.out.println(Arrays.toString(array));
                                    if (!texture.getTextureData().isPrepared()) {
                                        texture.getTextureData().prepare();
                                    }
                                    Pixmap pp = texture.getTextureData().consumePixmap();
                                    System.out.println(array.length);

                                    Pixmap pixmap = new Pixmap(array, 0, array.length);
                                    //pixmap.setColor(1,1,1,1);
                                    //pixmap.fill();

                                    //pixmap.getPixels().put(array, 0, array.length - 1);

                                    pp.drawPixmap(pixmap, 0, 0);
                                    texture = new Texture(pixmap);
                                    pixmap.dispose();

                                    */
                                });
                            }
                            if (object instanceof int[][]) {
                                Gdx.app.postRunnable(() -> {
                                    int[][] array = (int[][]) (object);

                                    if (!texture.getTextureData().isPrepared()) {
                                        texture.getTextureData().prepare();
                                    }
                                    Pixmap pp = texture.getTextureData().consumePixmap();

                                    Pixmap pixmap = new Pixmap(1280, 720, Pixmap.Format.RGB888);
                                    pixmap.setColor(1, 1, 1, 1);
                                    pixmap.fill();
                                    for (int i = 0; i < array.length; i++) {
                                        for (int j = 0; j < array[i].length; j++) {
                                            pixmap.drawPixel(i, j, array[i][j]);
                                            System.out.println(i + " " + j + " " + pixmap.getPixel(i, j));
                                        }
                                    }
                                    System.out.println(pp.getWidth() + "  " + pp.getHeight());
                                    pp.drawPixmap(pixmap, 0, 0);
                                    texture = new Texture(pixmap);
                                    pixmap.dispose();
                                    //pp.dispose();
                                    //ServerScreen.this.pixmap.drawPixmap(pixmap, 0,0);
                                    //texture.draw(ServerScreen.this.pixmap, 0, 0);
                                    //pixmap.dispose();
                                    //ServerScreen.this.pixmap.dispose();
                                });
                            }
                            if (object instanceof Message) {
                                Gdx.app.postRunnable(() -> {
                                    Message message = (Message) (object);
                                    System.out.println(Arrays.toString(message.getBytes()));
                                    System.out.println(message.getBytes().length);
                                    Pixmap pixmap = new Pixmap(1280, 60, Pixmap.Format.RGB888);
                                    //pixmap.getPixels().put(message.getBytes());
                                    System.out.println();
                                    pixmap.setColor(0, 1, 0, 1);
                                    pixmap.fill();

                                    System.out.println(pixmap.getPixel(0, 0));
                                    //texture.getTextureData().prepare();
                                    texture = new Texture(pixmap);
                                    pixmap.dispose();
                                    //texture = new Texture(pixmap);
                                    //texture = new Texture(new Pixmap(message.getBytes(), 0, message.getBytes().length));
                                    //pixmap.getPixels().put(message.getBytes(), 1, message.getBytes().length - 1);
                                    //pixmap.setColor(Color.GREEN);
                                    //pixmap.fillCircle(100, 100, 100);
                                    //texture.draw(pixmap, 0, 0);
                                    //System.out.println("Client : " + connection.getID() + " sended : byte array" + Arrays.toString(((Message) object).getBytes()));
                                });
                            } else if (object instanceof Click) {

                                Gdx.app.postRunnable(() -> {
                                    Click click = (Click) (object);
                                    pixmap.setColor(Color.BLACK);
                                    pixmap.fillCircle((int) (click.pos.x), (int) (click.pos.y), 10);
                                    texture.draw(pixmap, 0, 0);
                                });
                            }
                            //System.out.println("Client : " + connection.getID() + " sended : " + object.toString());
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
        /*client = new Client(231000, 231000);
        client.getKryo().register(Message.class);
        client.getKryo().register(String.class);
        client.getKryo().register(byte[].class);

        client.getKryo().register(Click.class);
        client.getKryo().register(Vector2.class);
        client.getKryo().register(float.class);
        client.getKryo().register(float.class);
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.start();
                try {
                    client.connect(5000, ip, 5555, 6666);
                } catch (IOException e) {
                    client.close();
                    e.printStackTrace();
                }
                client.addListener(new Listener() {
                    @Override
                    public void connected(Connection connection) {
                        super.connected(connection);
                        System.out.println("Client connected");
                    }

                    @Override
                    public void disconnected(Connection connection) {
                        super.disconnected(connection);
                        System.out.println("Client disconnected");
                    }

                    @Override
                    public void received(Connection connection, Object object) {
                        super.received(connection, object);
                        if (object instanceof String) {
                            String s = (String) object;

                        }
                        System.out.println("Server" + " sended : " + object.toString());
                    }

                    @Override
                    public void idle(Connection connection) {
                        super.idle(connection);
                    }
                });
            }
        }).start();
    }

    @Override
    public void render(float delta) {
        camera.update();
        ScreenUtils.clear(1, 1, 1, 1);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
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
        client.close();
        try {
            server.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            client.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        pixmapUser.setColor(Color.BLACK);
        pixmapUser.fillCircle((int) (x), (int) (y), 10);
        textureUser.draw(pixmapUser, 0, 0);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        pixmapUser.setColor(Color.BLACK);
        pixmapUser.fillCircle((int) (x), (int) (y), 10);
        textureUser.draw(pixmapUser, 0, 0);
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
        pixmapUser.setColor(Color.BLACK);
        pixmapUser.fillCircle((int) (x), (int) (y), 10);
        textureUser.draw(pixmapUser, 0, 0);
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
