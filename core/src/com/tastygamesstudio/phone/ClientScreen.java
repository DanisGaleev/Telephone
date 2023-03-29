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
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientScreen implements Screen, GestureDetector.GestureListener {
    private OrthographicCamera camera;
    private Stage stage;
    private SpriteBatch batch;
    private final int WIDTH = Gdx.graphics.getWidth();
    private final int HEIGHT = Gdx.graphics.getHeight();
    private final float XK = WIDTH / 1280f, YK = HEIGHT / 720f;

    private Label receivedMsg;
    private TextField desc;
    private ImageButton send;
    private ImageButton red, orange, yellow, green, light_blue, blue, white, black;
    private ImageButton huge, large, medium, small;

    private Client client;
    private final String serverIp;
    private final String clientName;

    private Color CHOOSED_COLOR;
    private byte BRUSH_SIZE = 5;
    private Texture textureUser;
    private Texture texture;
    private Pixmap pixmapUser;

    private final boolean[] chunk = new boolean[Config.bytePackageCount];
    private byte[] pixelData;
    private byte[] bytes;
    private byte packegeNumber;
    private byte[] image;

    // FIXME: 01.03.2023 убрать true если есть
    private boolean isStarted;

    private byte ID;

    public ClientScreen(String serverIp, String name) {
        this.serverIp = serverIp;
        this.clientName = name;
    }

    @Override
    public void show() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
                + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
                + "1234567890.,:;_¡!¿?\"'+-*/()[]={}";
        Label.LabelStyle labelStyle = new Label.LabelStyle(generator.generateFont(parameter), Color.BLACK);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();

        textFieldStyle.font = generator.generateFont(parameter);
        textFieldStyle.fontColor = Color.BLACK;
        textFieldStyle.background = new TextureRegionDrawable(new Texture("ui/textField_background.png"));
        textFieldStyle.selection = new TextureRegionDrawable(new Texture("ui/selection.png"));
        textFieldStyle.cursor = new TextureRegionDrawable(new Texture("ui/cursor.png"));

        image = new byte[Config.imageSize];

        camera = new OrthographicCamera();
        Viewport viewport = new StretchViewport(1280, 720);
        camera.setToOrtho(false, 1280, 720);
        stage = new Stage(viewport);
        batch = new SpriteBatch();

        InputMultiplexer inputMultiplexer = new InputMultiplexer(new GestureDetector(this), stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        receivedMsg = new Label("", labelStyle);
        receivedMsg.setVisible(false);
        receivedMsg.setSize(500, 50);
        receivedMsg.setPosition(640 - receivedMsg.getWidth() / 2, 720 - 100);
        stage.addActor(receivedMsg);

        desc = new TextField("", textFieldStyle);
        desc.setVisible(false);
        desc.setSize(500, 50);
        desc.setPosition(640 - desc.getWidth() / 2, 720 - desc.getHeight());
        desc.getStyle().font = generator.generateFont(parameter);
        stage.addActor(desc);

        send = new ImageButton(new TextureRegionDrawable(new Texture("ui/send_up.png")),
                new TextureRegionDrawable(new Texture("ui/send_down.png")),
                new TextureRegionDrawable(new Texture("ui/send_down.png")));
        send.setPosition(1280 - 100, 0);
        send.setVisible(false);
        stage.addActor(send);
        send.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                send.setVisible(false);
                if (ID % 2 == 0) {
                    Gdx.app.log("MESSAGE", "send message(Pixmap) to server");
                    sendPixmap();
                } else {
                    Gdx.app.log("MESSAGE", "send message(String) to server");
                    desc.setDisabled(true);
                    client.sendTCP(desc.getText());
                }
                ID = -1;
            }
        });
        brushColorButtons();
        brushSizeButtons();

        CHOOSED_COLOR = new Color(Color.BLACK);
        pixmapUser = new Pixmap(1280, 720, Pixmap.Format.RGB888);
        pixmapUser.setColor(1, 1, 1, 1);
        pixmapUser.fill();
        pixmapUser.setColor(1, 1, 0, 0.1f);
        System.out.println(XK + "  " + YK);
        pixmapUser.fillRectangle(Config.X1, Config.Y1, Config.SIZE_X, Config.SIZE_Y);
        textureUser = new Texture(pixmapUser);

        client = new Client(Register.BUFFER_SIZE, Register.BUFFER_SIZE);
        Register.register(client.getKryo());
        new Thread(() -> {
            client.start();
            try {
                client.connect(Register.TIMEOUT, serverIp, Register.TCP_PORT, Register.UDP_PORT);
                client.sendTCP(Config.CONNECTION_NAME_CODE + clientName);
            } catch (IOException e) {
                client.close();
                try {
                    client.reconnect(7000);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
            }
            client.addListener(new ClientListener());
        }).start();

    }
    /*
     client.start();
                try {
                    client.connect(Register.TIMEOUT, serverIp, Register.TCP_PORT, Register.UDP_PORT);
                    client.sendTCP(Config.CONNECTION_NAME_CODE + clientName);
                } catch (IOException e) {
                    client.close();
                    try {
                        client.reconnect(3000);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    e.printStackTrace();
                }
                client.addListener(new ClientListener());
     */

    /*
    new Listener() {
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
                                Gdx.app.log("MESSAGE", "received message(String) " + s + " from server");
                                ID = 0;
                                setVisible(s);
                            } else if (object instanceof byte[]) {
                                Gdx.app.log("MESSAGE", "received message(Pixmap) " + pixelData[0] + " from server");
                                ID = 1;
                                Gdx.app.postRunnable(() -> {
                                    createChunk(object);
                                    boolean isAllPackageReceived = false;
                                    for (boolean b : chunk) {
                                        if (!b) {
                                            isAllPackageReceived = true;
                                            break;
                                        }
                                    }
                                    if (!isAllPackageReceived) {
                                        createImage(send);
                                        Gdx.app.log("PIXMAP", "create Pixmap from all pixmap chunks");
                                    }
                                });
                            }
                        }
                    });
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 1, 1, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (texture != null) {
            batch.draw(texture, 0, 0);
        } else if (isStarted) {
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
        if (client.isConnected())
            client.close();
        if (texture != null)
            texture.dispose();
        textureUser.dispose();
        pixmapUser.dispose();
        try {
            client.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        batch.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (ID == 0 && isStarted && y / YK - BRUSH_SIZE > Config.Y1 && y / YK + BRUSH_SIZE < Config.Y2 && x / XK + BRUSH_SIZE < Config.X2 && x / XK - BRUSH_SIZE > Config.X1) {
            pixmapUser.setColor(CHOOSED_COLOR);
            pixmapUser.fillCircle((int) (x / XK), (int) (y / YK), BRUSH_SIZE);
            textureUser.draw(pixmapUser, 0, 0);
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (ID == 0 && isStarted && y / YK - BRUSH_SIZE > Config.Y1 && y / YK + BRUSH_SIZE < Config.Y2 && x / XK + BRUSH_SIZE < Config.X2 && x / XK - BRUSH_SIZE > Config.X1) {
            pixmapUser.setColor(CHOOSED_COLOR);
            pixmapUser.fillCircle((int) (x / XK), (int) (y / YK), BRUSH_SIZE);
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
        if (ID == 0 && isStarted && y / YK - BRUSH_SIZE > Config.Y1 && y / YK + BRUSH_SIZE < Config.Y2 && x / XK + BRUSH_SIZE < Config.X2 && x / XK - BRUSH_SIZE > Config.X1) {
            pixmapUser.setColor(CHOOSED_COLOR);
            pixmapUser.fillCircle((int) (x / XK), (int) (y / YK), BRUSH_SIZE);
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

    private void createChunk(Object object) {
        byte[] pixelData = (byte[]) (object);
        chunk[pixelData[0]] = true;
        System.arraycopy(pixelData, 1, image, pixelData[0] * Config.bytePackegeSize, pixelData.length - 1);
    }

    private void createImage(ImageButton send) {
        Arrays.fill(chunk, false);
        Pixmap pixmap = new Pixmap(Config.SIZE_X, Config.SIZE_Y, Pixmap.Format.RGBA8888);
        ByteBuffer pixels = pixmap.getPixels();
        pixels.clear();
        pixels.put(image);
        pixels.position(0);
        desc.setVisible(true);
        texture = new Texture(pixmap);

        send.setVisible(true);
    }

    private void setVisible(String s) {
        isStarted = true;
        receivedMsg.setVisible(true);
        receivedMsg.setText(s);
        send.setVisible(true);

        red.setVisible(true);
        orange.setVisible(true);
        yellow.setVisible(true);
        green.setVisible(true);
        light_blue.setVisible(true);
        blue.setVisible(true);
        white.setVisible(true);
        black.setVisible(true);

        huge.setVisible(true);
        large.setVisible(true);
        medium.setVisible(true);
        small.setVisible(true);
    }


    private void brushColorButtons() {

        Pixmap color = new Pixmap(80, 80, Pixmap.Format.RGB888);

        color.setColor(Color.RED);
        color.fill();
        red = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        red.setPosition(1280 - 160, 365);
        stage.addActor(red);
        red.setVisible(false);
        red.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.RED);
            }
        });

        color.setColor(Color.ORANGE);
        color.fill();
        orange = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        orange.setPosition(1280 - 80, 365);
        orange.setVisible(false);
        stage.addActor(orange);
        orange.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.ORANGE);
            }
        });

        color.setColor(Color.YELLOW);
        color.fill();
        yellow = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        yellow.setPosition(1280 - 160, 285);
        yellow.setVisible(false);
        stage.addActor(yellow);
        yellow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.YELLOW);
            }
        });

        color.setColor(Color.GREEN);
        color.fill();
        green = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        green.setPosition(1280 - 80, 285);
        green.setVisible(false);
        stage.addActor(green);
        green.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.GREEN);
            }
        });

        color.setColor(0, 1, 0.82f, 1);
        color.fill();
        light_blue = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        light_blue.setPosition(1280 - 160, 205);
        light_blue.setVisible(false);
        stage.addActor(light_blue);
        light_blue.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(0, 1, 0.82f, 1);
            }
        });

        color.setColor(Color.BLUE);
        color.fill();
        blue = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        blue.setPosition(1280 - 80, 205);
        blue.setVisible(false);
        stage.addActor(blue);
        blue.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.BLUE);
            }
        });

        color.setColor(Color.WHITE);
        color.fill();
        white = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        white.setPosition(1280 - 160, 125);
        white.setVisible(false);
        stage.addActor(white);
        white.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.WHITE);
            }
        });

        color.setColor(Color.BLACK);
        color.fill();
        black = new ImageButton(new TextureRegionDrawable(new Texture(color)));
        black.setPosition(1280 - 80, 125);
        black.setVisible(false);
        stage.addActor(black);
        black.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CHOOSED_COLOR.set(Color.BLACK);
            }
        });

        color.dispose();
    }

    private void brushSizeButtons() {
        Pixmap size = new Pixmap(60, 60, Pixmap.Format.RGB888);
        size.setColor(Color.WHITE);
        size.fill();
        size.setColor(Color.BLACK);

        size.fillCircle(30, 30, 5);
        small = new ImageButton(new TextureRegionDrawable(new Texture(size)));
        small.setPosition(180, 570);
        small.setVisible(false);
        stage.addActor(small);
        small.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BRUSH_SIZE = 5;
            }
        });

        size.fillCircle(30, 30, 10);
        medium = new ImageButton(new TextureRegionDrawable(new Texture(size)));
        medium.setPosition(120, 570);
        medium.setVisible(false);
        stage.addActor(medium);
        medium.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BRUSH_SIZE = 10;
            }
        });

        size.fillCircle(30, 30, 20);
        large = new ImageButton(new TextureRegionDrawable(new Texture(size)));
        large.setPosition(60, 570);
        large.setVisible(false);
        stage.addActor(large);
        large.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BRUSH_SIZE = 20;
            }
        });

        size.fillCircle(30, 30, 30);
        huge = new ImageButton(new TextureRegionDrawable(new Texture(size)));
        huge.setPosition(0, 570);
        huge.setVisible(false);
        stage.addActor(huge);
        huge.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BRUSH_SIZE = 30;
            }
        });

        size.dispose();
    }

    private void sendPixmap() {
        byte[] preBuffer = ScreenUtils.getFrameBufferPixels(0, Gdx.graphics.getHeight() - (int) (Config.SIZE_Y * YK) - (int) (Config.Y1 * YK), (int) (Config.SIZE_X * XK), (int) (Config.SIZE_Y * YK), true);
        Pixmap prePixmap = new Pixmap((int) (Config.SIZE_X * XK), (int) (Config.SIZE_Y * YK), Pixmap.Format.RGBA8888);
        ByteBuffer preByteBuffer = prePixmap.getPixels();
        preByteBuffer.clear();
        preByteBuffer.put(preBuffer);
        preByteBuffer.position(0);

        Pixmap newP = new Pixmap(1080, 570, Pixmap.Format.RGBA8888);
        newP.drawPixmap(prePixmap, 0, 0, prePixmap.getWidth(), prePixmap.getHeight(), 0, 0, newP.getWidth(), newP.getHeight());
        pixelData = new byte[newP.getPixels().limit()];
        newP.getPixels().get(pixelData);
        System.out.println(Arrays.toString(pixelData));
        bytes = new byte[Config.bytePackegeSize + 1];
        packegeNumber = 0;
        new Timer().scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                bytes[0] = packegeNumber;
                System.out.println(packegeNumber + "  " + bytes[0] * Config.bytePackegeSize);
                System.arraycopy(pixelData, Config.bytePackegeSize * bytes[0], bytes, 1, bytes.length - 1);
                try {
                    client.sendTCP(bytes);
                } catch (KryoException e) {
                    e.printStackTrace();
                }
                packegeNumber++;
            }
        }, Register.TIME_DELTA, Register.TIME_DELTA, Config.bytePackageCount - 1);
    }

    class ClientListener extends Listener {

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
                Gdx.app.log("MESSAGE", "received message(String) " + s + " from server");
                ID = 0;
                setVisible(s);
            } else if (object instanceof byte[]) {
                ID = 1;
                Gdx.app.postRunnable(() -> {
                    byte[] pixelData = (byte[]) (object);
                    Gdx.app.log("MESSAGE", "received message(Pixmap) " + pixelData[0] + " from server");
                    chunk[pixelData[0]] = true;
                    System.arraycopy(pixelData, 1, image, pixelData[0] * Config.bytePackegeSize, pixelData.length - 1);
                    boolean isAllPackageReceived = false;
                    for (boolean b : chunk) {
                        if (!b) {
                            isAllPackageReceived = true;
                            break;
                        }
                    }
                    if (!isAllPackageReceived) {
                        createImage(send);
                        Gdx.app.log("PIXMAP", "create Pixmap from all pixmap chunks");
                    }
                });
            }
        }
    }
    //private void sendPixmap() {
    //    pixelData = ScreenUtils.getFrameBufferPixels(Config.X1, Gdx.graphics.getHeight() - Config.Y1 - Config.SIZE_Y, Config.SIZE_X, Config.SIZE_Y, true);
    //    bytes = new byte[Config.bytePackegeSize + 1];
    //    packegeNumber = 0;
    //    new Timer().scheduleTask(new Timer.Task() {
    //        @Override
    //        public void run() {
    //            bytes[0] = packegeNumber;
    //            System.out.println(packegeNumber + "  " + bytes[0] * Config.bytePackegeSize);
    //            System.arraycopy(pixelData, Config.bytePackegeSize * bytes[0], bytes, 1, bytes.length - 1);
    //            try {
    //                client.sendTCP(bytes);
    //            } catch (KryoException e) {
    //                e.printStackTrace();
    //            }
    //            packegeNumber++;
    //        }
    //    }, Register.TIME_DELTA, Register.TIME_DELTA, Config.bytePackageCount - 1);
    //}
}
