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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
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
    private final int WIDTH = Gdx.graphics.getWidth();
    private final int HEIGHT = Gdx.graphics.getHeight();
    private final float XK = WIDTH / 1280f, YK = HEIGHT / 720f;

    private Label receivedMsg;
    private TextField desc;
    private ImageButton send;
    private ImageButton red, orange, yellow, green, light_blue, blue, white, black;
    private ImageButton huge, large, medium, small;

    private Server server;
    private Array<Integer> clientsArray;
    private Array<Label> clientNameLabels;

    private Color CHOOSED_COLOR;
    private byte BRUSH_SIZE = 5;
    private Texture texture;
    private Texture textureUser;
    private Pixmap pixmapUser;

    private final String ip;
    private final boolean[] chunk = new boolean[Config.bytePackageCount];
    private byte[] pixelData;
    private byte[] bytes;
    private byte packegeNumber;
    private byte[] image;
    private boolean isStarted, isCanDraw;

    public ServerScreen(String ip) {
        this.ip = ip;
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
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = generator.generateFont(parameter);
        textFieldStyle.fontColor = Color.BLACK;
        textFieldStyle.background = new TextureRegionDrawable(new Texture("ui/textField_background.png"));
        textFieldStyle.selection = new TextureRegionDrawable(new Texture("ui/selection.png"));
        textFieldStyle.cursor = new TextureRegionDrawable(new Texture("ui/cursor.png"));

        image = new byte[Config.imageSize];
        clientsArray = new Array<>();
        clientNameLabels = new Array<>();

        camera = new OrthographicCamera();
        Viewport viewport = new StretchViewport(1280, 720);
        camera.setToOrtho(false, 1280, 720);
        stage = new Stage(viewport);
        batch = new SpriteBatch();
        Skin skin = new Skin(Gdx.files.internal("skin/skin-composer-ui.json"));

        InputMultiplexer inputMultiplexer = new InputMultiplexer(new GestureDetector(this), stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        CHOOSED_COLOR = new Color(Color.BLACK);
        pixmapUser = new Pixmap(1280, 720, Pixmap.Format.RGB888);
        pixmapUser.setColor(1, 1, 1, 1);
        pixmapUser.fill();
        pixmapUser.setColor(1, 1, 0, 0.1f);
        pixmapUser.fillRectangle(Config.X1, Config.Y1, Config.SIZE_X, Config.SIZE_Y);
        textureUser = new Texture(pixmapUser);

        FreeTypeFontGenerator.FreeTypeFontParameter parameterLabel = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameterLabel.size = 24;
        parameterLabel.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
                + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
                + "1234567890.,:;_¡!¿?\"'+-*/()[]={}";
        Label.LabelStyle labelStyle = new Label.LabelStyle(generator.generateFont(parameterLabel), Color.BLACK);

        receivedMsg = new Label("", labelStyle);
        receivedMsg.setVisible(false);
        receivedMsg.setSize(500, 50);
        receivedMsg.setPosition(640 - receivedMsg.getWidth() / 2, 620);
        stage.addActor(receivedMsg);

        Label ipLabel = new Label(ip, labelStyle);
        ipLabel.setPosition(0, 720 - ipLabel.getHeight());
        stage.addActor(ipLabel);

        desc = new TextField("", textFieldStyle);
        desc.setVisible(false);
        desc.setSize(500, 50);
        desc.setPosition(640 - desc.getWidth() / 2, 720 - desc.getHeight());
        desc.getStyle().font = generator.generateFont(parameter);
        stage.addActor(desc);

        ImageButton start = new ImageButton(new TextureRegionDrawable(new Texture("ui/start_up.png")),
                new TextureRegionDrawable(new Texture("ui/start_down.png")),
                new TextureRegionDrawable(new Texture("ui/start_down.png")));
        start.setPosition(640 - start.getWidth() / 2, 360 - start.getHeight() / 2);
        stage.addActor(start);
        start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (clientsArray.size % 2 == 0)
                    desc.setVisible(true);
                else {
                    isCanDraw = true;

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
                //receivedMsg.setVisible(true);
                ipLabel.setVisible(false);
                send.setVisible(true);
                start.setVisible(false);
                isStarted = true;
            }
        });

        send = new ImageButton(new TextureRegionDrawable(new Texture("ui/send_up.png")),
                new TextureRegionDrawable(new Texture("ui/send_down.png")),
                new TextureRegionDrawable(new Texture("ui/send_down.png")));
        send.setPosition(1180, 0);
        send.setVisible(false);
        stage.addActor(send);
        send.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                isCanDraw = false;
                send.setVisible(false);
                if ((clientsArray.size + 1) % 2 == 0) {
                    Gdx.app.log("MESSAGE", "send message(Pixmap) to server");
                    sendPixmap();
                } else {
                    Gdx.app.log("MESSAGE", "send message(String) to server");
                    server.sendToTCP(clientsArray.get(0), desc.getText());
                    desc.setDisabled(true);
                }
            }
        });

        Table table = new Table();
        ScrollPane clientsScroolPane = new ScrollPane(table, skin, "default");
        clientsScroolPane.setScrollBarPositions(true, true);
        clientsScroolPane.setBounds(1080, 520, 200, 200);
        stage.addActor(clientsScroolPane);

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
                            if (isStarted)
                                connection.close();
                            else
                                clientsArray.add(connection.getID());
                        }

                        @Override
                        public void disconnected(Connection connection) {
                            super.disconnected(connection);
                            clientNameLabels.get(clientsArray.indexOf(connection.getID(), true)).remove();
                            clientNameLabels.removeIndex(clientsArray.indexOf(connection.getID(), true));
                            table.clearChildren();
                            for (int i = 0; i < clientNameLabels.size; i++) {
                                table.add(clientNameLabels.get(i)).height(20).center();
                                table.row();
                            }
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
                                    Label label = new Label(msg.substring(Config.CONNECTION_NAME_CODE.length()), labelStyle);
                                    clientNameLabels.add(label);
                                    table.add(label).height(20).center();
                                    table.row();
                                } else {
                                    if (connection.getID() != clientsArray.get(clientsArray.size - 1)) {
                                        Gdx.app.log("MESSAGE", "received message(String) " + msg + ". Send message(String) to client");
                                        server.sendToTCP(connection.getID() + 1, msg);
                                    } else {
                                        Gdx.app.log("MESSAGE", "received message(String) " + msg + " from last client");
                                        receivedMsg.setVisible(true);
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
        if (texture != null)
            batch.draw(texture, 0, 0);
        else if (isStarted)
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
        if (isCanDraw && (clientsArray.size + 1) % 2 == 0 && isStarted && y / YK - BRUSH_SIZE > Config.Y1 && y / YK + BRUSH_SIZE < Config.Y2 && x / XK + BRUSH_SIZE < Config.X2 && x / XK - BRUSH_SIZE > Config.X1) {
            pixmapUser.setColor(CHOOSED_COLOR);
            pixmapUser.fillCircle((int) (x / XK), (int) (y / YK), BRUSH_SIZE);
            textureUser.draw(pixmapUser, 0, 0);
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (isCanDraw && (clientsArray.size + 1) % 2 == 0 && isStarted && y / YK - BRUSH_SIZE > Config.Y1 && y / YK + BRUSH_SIZE < Config.Y2 && x / XK + BRUSH_SIZE < Config.X2 && x / XK - BRUSH_SIZE > Config.X1) {
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
        if (isCanDraw && (clientsArray.size + 1) % 2 == 0 && isStarted && y / YK - BRUSH_SIZE > Config.Y1 && y / YK + BRUSH_SIZE < Config.Y2 && x / XK + BRUSH_SIZE < Config.X2 && x / XK - BRUSH_SIZE > Config.X1) {
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
                    server.sendToTCP(clientsArray.get(0), bytes);
                } catch (KryoException e) {
                    e.printStackTrace();
                }
                packegeNumber++;
            }
        }, Register.TIME_DELTA, Register.TIME_DELTA, Config.bytePackageCount - 1);
    }


    // private void sendPixmap() {
    //     byte[] q = ScreenUtils.getFrameBufferPixels(0, Gdx.graphics.getHeight() - (int) (Config.SIZE_Y * YK) - (int) (Config.Y1 * YK), (int) (Config.SIZE_X * XK), (int) (Config.SIZE_Y * YK), true);
    //     int count = 0;
    //     for (byte b : q) {
    //         if (b != -1)
    //             count++;
    //     }
    //     System.out.println(Arrays.toString(q));
    //     System.out.println(count + "  " + q.length);
    //     Pixmap www = new Pixmap((int) (Config.SIZE_X * XK), (int) (Config.SIZE_Y * YK), Pixmap.Format.RGBA8888);
    //     ByteBuffer tttt = www.getPixels();
    //     tttt.clear();
    //     tttt.put(q);
    //     tttt.position(0);
    //     System.out.println(www.getWidth() + "  " + www.getHeight());
//
    //     Pixmap newP = new Pixmap(1080, 570, Pixmap.Format.RGBA8888);
    //     newP.drawPixmap(www, 0, 0, www.getWidth(), www.getHeight(), 0, 0, newP.getWidth(), newP.getHeight());
    //     //newP.setColor(Color.BLACK);
    //     ////newP.fillRectangle(0, 0, 100, 100);
    //     rrr = new Texture(newP);
    //     System.out.println(newP.getPixels().limit() + " length");
    //     Pixmap p = Pixmap.createFromFrameBuffer(Config.X1, 720 - (Config.Y1 + Config.SIZE_Y), (Config.SIZE_X), (Config.SIZE_Y));
//
    //     byte[] buffer = new byte[newP.getPixels().limit()];
    //     newP.getPixels().get(buffer);
    //     System.out.println(Arrays.toString(buffer));
    //     ////////Pixmap p = Pixmap.createFromFrameBuffer(Config.X1, 1280 - (Config.Y1 + Config.SIZE_Y), Config.SIZE_X, Config.SIZE_Y);
    //     /////byte[]fff = ScreenUtils.getFrameBufferPixels(true);
    //     /////System.out.println(Gdx.graphics.getWidth() + "  " + Gdx.graphics.getHeight());
    //     /////System.out.println(fff.length +"tttt");
    //     /////Pixmap www = new Pixmap(Config.SIZE_X, Config.SIZE_Y, Pixmap.Format.RGBA8888);
    //     /////ByteBuffer tttt = www.getPixels();
    //     /////tttt.clear();
    //     /////tttt.put(fff);
    //     /////tttt.position(0);
    //     /////System.out.println(www.getWidth() + "  " +www.getHeight());
    //     //rrr = new Texture(www);
    //     /*Pixmap r = new Pixmap(Config.SIZE_X, Config.SIZE_Y, Pixmap.Format.RGBA8888);
    //     r.drawPixmap(p, 0, 0);
    //     System.out.println(r.getPixels().limit());
    //     byte[] x = new byte[r.getPixels().limit()];
    //     ByteBuffer.wrap(x);*/
//
    //     p.setColor(Color.RED);
    //     p.fillRectangle(0, 0, 100, 40);
    //     p.fillRectangle(0, 570 - 40, 100, 40);
    //     //p.fill();
    //     System.out.println(p.getWidth() + "  " + p.getHeight());
    //     ///// rrr = new Texture(new Pixmap(Config.SIZE_X, Config.SIZE_Y, Pixmap.Format.RGBA8888));
    //     ///// rrr.draw(p, 0, 0);
    //     System.out.println(rrr.getWidth() + "  " + rrr.getHeight() + "    " + p.getWidth() + "  " + p.getHeight());
//
    //     /*rrr = new Texture(r);
    //     System.out.println(Arrays.toString(x));*/
    //     pixelData = ScreenUtils.getFrameBufferPixels(Config.X1, Gdx.graphics.getHeight() - Config.Y1 - Config.SIZE_Y, Config.SIZE_X, Config.SIZE_Y, true);
    //     bytes = new byte[Config.bytePackegeSize + 1];
    //     packegeNumber = 0;
    //     new Timer().scheduleTask(new Timer.Task() {
    //         @Override
    //         public void run() {
    //             bytes[0] = packegeNumber;
    //             System.out.println(packegeNumber + "  " + bytes[0] * Config.bytePackegeSize);
    //             System.arraycopy(pixelData, Config.bytePackegeSize * bytes[0], bytes, 1, bytes.length - 1);
    //             try {
    //                 server.sendToTCP(clientsArray.get(0), bytes);
    //             } catch (KryoException e) {
    //                 e.printStackTrace();
    //             }
    //             packegeNumber++;
    //         }
    //     }, Register.TIME_DELTA, Register.TIME_DELTA, Config.bytePackageCount - 1);
    // }
}
