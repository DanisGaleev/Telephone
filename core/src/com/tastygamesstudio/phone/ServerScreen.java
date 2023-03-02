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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
    private OrthographicCamera camera;
    private Stage stage;
    private SpriteBatch batch;

    private Label receivedMsg;
    private TextField desc;
    private ImageButton send;
    private TextButton start;

    private Server server;
    private Array<Integer> clientsArray;
    private Array<Label> clientNameLabels;

    private Color CHOOSED_COLOR;
    private byte BRUSH_SIZE = 5;
    private Texture texture;
    private Texture textureUser;
    private Pixmap pixmapUser;

    private final boolean[] chunk = new boolean[Config.bytePackageCount];
    private byte[] pixelData;
    private byte[] bytes;
    private byte packegeNumber;
    private byte[] image;

    private boolean isStarted;
    public ServerScreen() {
    }

    @Override
    public void show() {
        image = new byte[Config.imageSize];
        clientsArray = new Array<>();
        clientNameLabels = new Array<>();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport);
        stage.setDebugAll(true);
        batch = new SpriteBatch();
        Skin skin = new Skin(Gdx.files.internal("skin/skin-composer-ui.json"));

        InputMultiplexer inputMultiplexer = new InputMultiplexer(new GestureDetector(this), stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        CHOOSED_COLOR = new Color(Color.BLACK);
        pixmapUser = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGB888);
        pixmapUser.setColor(1, 1, 1, 1);
        pixmapUser.fill();
        pixmapUser.setColor(1, 1, 0, 0.1f);
        pixmapUser.fillRectangle(Config.X1, Config.Y1, Config.SIZE_X, Config.SIZE_Y);
        textureUser = new Texture(pixmapUser);

        receivedMsg = new Label("", skin, "default");
        receivedMsg.setPosition(Gdx.graphics.getWidth() / 2 - receivedMsg.getWidth() / 2, 40);
        stage.addActor(receivedMsg);

        desc = new TextField("", skin, "default");
        desc.setVisible(false);
        desc.setSize(500, desc.getHeight());
        desc.setPosition(Gdx.graphics.getWidth() / 2 - desc.getWidth() / 2, 690);
        stage.addActor(desc);

        start = new TextButton("START GAME", skin, "default");
        start.setPosition(Gdx.graphics.getWidth() / 2 - start.getWidth() / 2, Gdx.graphics.getHeight() / 2 - start.getHeight() / 2);
        stage.addActor(start);
        start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (clientsArray.size % 2 == 0)
                    desc.setVisible(true);
                send.setVisible(true);
                start.setVisible(false);
                isStarted = true;
            }
        });

        send = new ImageButton(skin, "default");
        send.setBounds(1150, 40, 100, 100);
        send.setVisible(false);
        stage.addActor(send);
        send.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                send.setVisible(false);
                if ((clientsArray.size + 1) % 2 == 0) {
                    Gdx.app.log("MESSAGE", "send message(Pixmap) to server");
                    sendPixmap();
                } else {
                    Gdx.app.log("MESSAGE", "send message(String) to server");
                    server.sendToTCP(clientsArray.get(0), desc.getText());
                }
            }
        });

        Table table = new Table();
        table.setDebug(true);
        ScrollPane clientsScroolPane = new ScrollPane(table, skin, "default");
        clientsScroolPane.setScrollBarPositions(true, true);
        clientsScroolPane.setBounds(1100, 510, 200, 200);
        stage.addActor(clientsScroolPane);

        Pixmap color = new Pixmap(80, 80, Pixmap.Format.RGB888);

        color.setColor(Color.RED);
        color.fill();
        ImageButton red = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        red.setPosition(1100, 420);
        stage.addActor(red);
        red.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.RED);
            }
        });

        color.setColor(Color.ORANGE);
        color.fill();
        ImageButton orange = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        orange.setPosition(1180, 420);
        stage.addActor(orange);
        orange.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.ORANGE);
            }
        });
        color.setColor(Color.YELLOW);
        color.fill();
        ImageButton yellow = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        yellow.setPosition(1100, 340);
        stage.addActor(yellow);
        yellow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.YELLOW);
            }
        });
        color.setColor(Color.GREEN);
        color.fill();
        ImageButton green = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        green.setPosition(1180, 340);
        stage.addActor(green);
        green.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.GREEN);
            }
        });
        color.setColor(0, 1, 0.82f, 1);
        color.fill();
        ImageButton light_blue = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        light_blue.setPosition(1100, 260);
        stage.addActor(light_blue);
        light_blue.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(0, 1, 0.82f, 1);
            }
        });
        color.setColor(Color.BLUE);
        color.fill();
        ImageButton blue = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        blue.setPosition(1180, 260);
        stage.addActor(blue);
        blue.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.BLUE);
            }
        });
        color.setColor(Color.WHITE);
        color.fill();
        ImageButton white = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        white.setPosition(1100, 180);
        stage.addActor(white);
        white.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.WHITE);
            }
        });
        color.setColor(Color.BLACK);
        color.fill();
        ImageButton black = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        black.setPosition(1180, 180);
        stage.addActor(black);
        black.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.BLACK);
            }
        });

        color.dispose();

        Pixmap size = new Pixmap(60, 60, Pixmap.Format.RGB888);
        size.setColor(Color.WHITE);
        size.fill();
        size.setColor(Color.BLACK);

        size.fillCircle(30, 30, 5);
        ImageButton small = new ImageButton(new TextureRegionDrawable(new Texture(size)));
        small.setPosition(100, 40);
        stage.addActor(small);
        small.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BRUSH_SIZE = 5;
            }
        });

        size.fillCircle(30, 30, 10);
        ImageButton medium = new ImageButton(new TextureRegionDrawable(new Texture(size)));
        medium.setPosition(160, 40);
        stage.addActor(medium);
        medium.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BRUSH_SIZE = 10;
            }
        });

        size.fillCircle(30, 30, 20);
        ImageButton large = new ImageButton(new TextureRegionDrawable(new Texture(size)));
        large.setPosition(220, 40);
        stage.addActor(large);
        large.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BRUSH_SIZE = 20;
            }
        });

        size.fillCircle(30, 30, 30);
        ImageButton huge = new ImageButton(new TextureRegionDrawable(new Texture(size)));
        huge.setPosition(280, 40);
        stage.addActor(huge);
        huge.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BRUSH_SIZE = 30;
            }
        });

        size.dispose();

        Pixmap pixmap = new Pixmap(Config.SIZE_X, Config.SIZE_Y, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();

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
                            clientNameLabels.get(clientsArray.indexOf(connection.getID(), true)).remove();
                            clientNameLabels.removeIndex(clientsArray.indexOf(connection.getID(), true));
                            table.clearChildren();
                            for (int i = 0; i < clientNameLabels.size; i++) {
                                table.add(clientNameLabels.get(i)).height(20).width(100).expandX();
                                table.row();
                            }
                            //////table.pack();
                            //table.
                            //clientsScroolPane.removeActorAt(clientsArray.indexOf(connection.getID(), true), true);
                            clientsArray.removeValue(connection.getID(), false);
                        }

                        @Override
                        public void received(Connection connection, Object object) {
                            super.received(connection, object);
                            if (object instanceof byte[]) {
                                if (connection.getID() != clientsArray.get(clientsArray.size - 1)) {
                                    Gdx.app.log("MESSAGE", "received message(Pixmap) " + ((byte[]) (object))[0] + ". Send message(Pixmap) to client");
                                    server.sendToTCP(connection.getID() + 1, object);
                                } else {
                                    Gdx.app.postRunnable(() -> {
                                        byte[] pixelData = (byte[]) (object);
                                        chunk[pixelData[0]] = true;
                                        Gdx.app.log("MESSAGE", "received message(Pixmap) " + pixelData[0] + " from last client");
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
                                            Gdx.app.log("PIXMAP", "create Pixmap from all pixmap chunks");
                                        }
                                    });
                                }
                            } else if (object instanceof String) {
                                String msg = (String) object;
                                if (msg.startsWith(Config.CONNECTION_NAME_CODE)) {
                                    Gdx.app.log("MESSAGE", "received message(String) client name " + msg.substring(Config.CONNECTION_NAME_CODE.length()) + " from new client");
                                    Label label = new Label(msg.substring(Config.CONNECTION_NAME_CODE.length()), skin, "default");
                                    label.setSize(100, 20);
                                    clientNameLabels.add(label);
                                    table.add(label).height(20).width(100).expandX();
                                    table.row();
                                    //clientsScroolPane.setActor(new Label(msg.substring(Config.CONNECTION_NAME_CODE.length()), skin, "default"));
                                } else {
                                    if (connection.getID() != clientsArray.get(clientsArray.size - 1)) {
                                        Gdx.app.log("MESSAGE", "received message(String) " + msg + ". Send message(String) to client");
                                        server.sendToTCP(connection.getID() + 1, msg);
                                    } else {
                                        Gdx.app.log("MESSAGE", "received message(String) " + msg + " from last client");
                                        receivedMsg.setText(msg);
                                    }
                                }
                            }
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
        ScreenUtils.clear(1, 1, 1, 1);
        camera.update();
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
        if (isStarted && y - BRUSH_SIZE > Config.Y1 && y + BRUSH_SIZE < Config.Y2 && x + BRUSH_SIZE < Config.X2 && x - BRUSH_SIZE > Config.X1) {
            pixmapUser.setColor(CHOOSED_COLOR);
            pixmapUser.fillCircle((int) (x), (int) (y), BRUSH_SIZE);
            textureUser.draw(pixmapUser, 0, 0);
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (isStarted && y - BRUSH_SIZE > Config.Y1 && y + BRUSH_SIZE < Config.Y2 && x + BRUSH_SIZE < Config.X2 && x - BRUSH_SIZE > Config.X1) {
            pixmapUser.setColor(CHOOSED_COLOR);
            pixmapUser.fillCircle((int) (x), (int) (y), BRUSH_SIZE);
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
        if (isStarted && y - BRUSH_SIZE > Config.Y1 && y + BRUSH_SIZE < Config.Y2 && x + BRUSH_SIZE < Config.X2 && x - BRUSH_SIZE > Config.X1) {
            pixmapUser.setColor(CHOOSED_COLOR);
            pixmapUser.fillCircle((int) (x), (int) (y), BRUSH_SIZE);
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

    private void sendPixmap() {
        pixelData = ScreenUtils.getFrameBufferPixels(Config.X1, Gdx.graphics.getHeight() - Config.Y1 - Config.SIZE_Y, Config.SIZE_X, Config.SIZE_Y, true);
        bytes = new byte[Config.bytePackegeSize + 1];
        packegeNumber = 0;
        new Timer().scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                bytes[0] = packegeNumber;
                System.out.println(packegeNumber + "  " + bytes[0] * Config.bytePackegeSize);
                System.arraycopy(pixelData, Config.bytePackegeSize * bytes[0], bytes, 1, bytes.length - 1);
                try {
                    server.sendToTCP(clientsArray.get(0), bytes);
                } catch (KryoException e) {
                    e.printStackTrace();
                }
                packegeNumber++;
            }
        }, Register.TIME_DELTA, Register.TIME_DELTA, Config.bytePackageCount - 1);
    }
}
