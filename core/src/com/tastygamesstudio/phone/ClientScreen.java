package com.tastygamesstudio.phone;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

public class ClientScreen implements Screen, GestureDetector.GestureListener {
    private final String ip;
    byte[] bytes;
    Texture texture;
    byte[] pixelData;
    SpriteBatch batch;
    Client client;
    Pixmap pixmap;
    ImageButton send;
    Stage stage;
    double time;
    private Phone app;
    byte i;
    Viewport viewport;
    OrthographicCamera camera;
    Skin skin;

    public ClientScreen(Phone app, String ip) {

        this.app = app;
        this.ip = ip;
    }

    class sendThread extends Thread {
        byte[] array;

        public sendThread(byte[] array) {
            this.array = array;
        }

        @Override
        public void run() {
            super.run();
            client.sendTCP(array);
        }

        public void setArray(byte[] array) {
            this.array = array;
        }
    }

    @Override
    public void show() {

        viewport = new StretchViewport(1280, 720);
        stage = new Stage(viewport);
        stage.setDebugAll(true);
        skin = new Skin(Gdx.files.internal("skin/skin-composer-ui.json"));
        send = new ImageButton(skin, "default");
        send.setBounds(1100, 40, 100, 100);
        send.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                // if (pixelData == null) {
                pixelData = ScreenUtils.getFrameBufferPixels(0, 0, 1280, 720, true);
               ////// System.out.println("GGG" + Arrays.toString(pixelData));
               ////// Pixmap pixmap = new Pixmap(1280, 720, Pixmap.Format.RGBA8888);
               ////// ByteBuffer pixels = pixmap.getPixels();
               ////// pixels.clear();
               ////// pixels.put(pixelData);
               ////// pixels.position(0);
                // }
               /////// //ScreenUtils.clear(1, 1, 1, 1);

                /*Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
                ByteBuffer pixels = pixmap.getPixels();
                pixels.clear();
                pixels.put(pixelData);
                pixels.position(0);

                 */

                // Pixmap pixmap = new Pixmap(pixelData, 0, pixelData.length);

               ////////// texture.draw(pixmap, 0, 0);
                bytes = new byte[184321];
                //    bytes[0] = 0;
                //    System.arraycopy(pixelData, 0, bytes, 1, bytes.length - 1);
                //    System.out.println(bytes[0]);
                //    System.out.println(Arrays.toString(bytes));
              //////////////////  sendThread t = new sendThread(bytes);
                //    t.start();
               /* Thread t = new Thread(() {
                        client.sendTCP(bytes)
                });
                t.start();

                */
                i = 0;
                new Timer().scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        bytes = null;
                        bytes = new byte[184321];
                        //Arrays.fill(bytes, (byte) 0);
                        bytes[0] = i;
                        System.arraycopy(pixelData, 184320 * bytes[0], bytes, 1, bytes.length - 1);
                        System.out.println(bytes[0]);
                       //////// System.out.println(Arrays.toString(bytes));
                        try {
                            client.sendTCP(bytes);
                        }
                        catch (KryoException e){
                            try {
                                client.reconnect();
                                client.sendTCP(bytes);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                        //t.setArray(bytes);
                        //t.start();
                        i++;
                    }
                }, 3 * 1, 1, 19);

                // byte[] bytes1 = new byte[184321];
                //    Arrays.fill(bytes, (byte) 0);
                //    bytes[0] = 1;
                //    System.arraycopy(pixelData, 184320, bytes, 1, bytes.length - 1);
                //    System.out.println(bytes[0]);
                //    System.out.println(Arrays.toString(bytes));
                //    t.setArray(bytes);
                //    t.start();
                // new Thread(() -> client.sendTCP(bytes)).start();

                //     Arrays.fill(bytes, (byte) 0);
                //     bytes[0] = 2;
                //     System.arraycopy(pixelData, 184320 * 2, bytes, 1, bytes.length - 1);
                //     System.out.println(bytes[0]);
                //     System.out.println(Arrays.toString(bytes));
                //     t.setArray(bytes);
                //     t.start();
                // new Thread(() -> client.sendTCP(bytes)).start();


                // byte[] bytes2 = new byte[184321];
                // bytes2[0] = 2;
                // System.arraycopy(pixelData, 184320 * 2, bytes2, 1, bytes2.length - 1);
                // System.out.println(bytes2[0]);
                // new Thread(() -> client.sendTCP(bytes2)).start();

               /* i = 0;
                new Timer().scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        byte[] bytes2 = new byte[184321];
                        bytes2[0] = i;
                        System.arraycopy(pixelData, i * 184320, bytes2, 1, bytes2.length - 1);
                        client.sendTCP(bytes2);
                        i++;
                    }
                }, 20 * 0.02f, 0.02f);


                */

                //bytes[0] = 0;

               /* new Timer().scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        bytes[0] = i[0];
                        System.arraycopy(pixelData, i[0] * 184320, bytes, 1, bytes.length - 1);
                        System.out.println(Arrays.toString(bytes));
                        new Thread(() -> client.sendTCP(bytes));
                        i[0]++;
                    }
                }, 0.2f * 20, 0.2f);
                for (i[0] = 0; i[0] < 20; i[0]++) {
                    bytes[0] = i[0];
                    System.arraycopy(pixelData, i[0] * 184320, bytes, 1, bytes.length - 1);
                    System.out.println(Arrays.toString(bytes));
                    new Thread(() -> client.sendTCP(bytes));
                }

                */

                /////System.arraycopy(pixelData, 0, bytes, 1, bytes.length - 1);
                /////System.out.println(bytes.length);
                /////System.out.println(Arrays.toString(bytes));
                /////System.out.println(pixelData.length);

                /////new Thread(() -> client.sendTCP(bytes)).start();

                /*if (!texture.getTextureData().isPrepared()) {
                    texture.getTextureData().prepare();
                }
                Pixmap pixmap = texture.getTextureData().consumePixmap();
                ByteBuffer byteBuffer = pixmap.getPixels().asReadOnlyBuffer();
                byte[] pixelDataByteArray = new byte[byteBuffer.remaining() / 12];
                byteBuffer.get(pixelDataByteArray, 0, pixelDataByteArray.length - 1);
                System.out.println(Arrays.toString(pixelDataByteArray));
                int[][] array = new int[1280][60];
                for (int i = 0; i < array.length; i++) {
                    for (int j = 0; j < array[i].length; j++) {
                        array[i][j] = pixmap.getPixel(i, j);
                    }
                }


                ByteArrayOutputStream output;
                byte[] a;
                try {
                    PixmapIO.PNG writer = new PixmapIO.PNG(pixmap.getWidth() * pixmap.getHeight() * 4);
                    try {
                        writer.setFlipY(false);
                        writer.setCompression(Deflater.BEST_COMPRESSION);
                        output = new ByteArrayOutputStream();
                        writer.write(output, pixmap);
                    } finally {
                        writer.dispose();
                    }
                    a = output.toByteArray();

                } catch (IOException ex) {
                    throw new GdxRuntimeException("Error");
                }
                System.out.println(Arrays.toString(a));
                System.out.println(a.length + "dddd");
                //new Thread(() -> client.sendTCP(array)).start();
                new Thread(() -> client.sendTCP(pixelDataByteArray)).start();


                */
                //new Thread(() -> client.sendTCP(new Message(pixelDataByteArray, ""))).start();
               /* ByteBuffer byteBuffer = pixmap.getPixels();
                byte[] bytes = new byte[byteBuffer.remaining() / 12 + 1];
                bytes[0] = 1;
                byteBuffer.get(bytes, 1, bytes.length - 1);
                byteBuffer.clear();
                System.out.println(Arrays.toString(bytes));
                System.out.println(bytes.length);

                */
                //new Thread(() -> client.sendTCP(new Message(bytes, ""))).start();
            }
        });
        stage.addActor(send);
        camera = new OrthographicCamera(1280, 720);
        camera.setToOrtho(false, 1280, 720);
        pixmap = new Pixmap(1280, 720, Pixmap.Format.RGB888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        // pixmap.setColor(1,1,0,1);
        // pixmap.fillRectangle(0 ,0,100, 100);
        texture = new Texture(pixmap);
        InputMultiplexer inputMultiplexer = new InputMultiplexer(new GestureDetector(this), stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
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
                        System.out.println("Server" + " sended : " + object.toString());
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
        time += delta;
        if (time > 1) {
            new Thread(() -> {
                //client.sendTCP("Hello from client(client)  " + TimeUtils.millis());
                time = 0;
            }).start();
        }
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(texture, 0, 0);
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
        try {
            client.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle((int) (x), (int) (y), 10);
        //pixmap.drawLine(1280 - (int) x, (int) y, (int) (1280 - x + deltaX), (int) (y + deltaY));
        //texture.dispose();
        texture.draw(pixmap, 0, 0);
        //texture = new Texture(pixmap);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle((int) (x), (int) (y), 10);
        //pixmap.drawLine(1280 - (int) x, (int) y, (int) (1280 - x + deltaX), (int) (y + deltaY));
        //texture.dispose();
        texture.draw(pixmap, 0, 0);
        //texture = new Texture(pixmap);
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
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle((int) (x), (int) (y), 10);
        pixmap.setColor(Color.GREEN);
        pixmap.fillRectangle(0, 0, 200, 40);
        //pixmap.drawLine(1280 - (int) x, (int) y, (int) (1280 - x + deltaX), (int) (y + deltaY));
        //texture.dispose();
        texture.draw(pixmap, 0, 0);


        //texture = new Texture(pixmap);
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
