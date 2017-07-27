package ro.proba.pongdemo;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

class GameView extends SurfaceView implements Runnable{

    final private String TAG = "cip";
    private final Context mContext;
    Thread mGameThread = null;

    //we need a SurfaceHolder when we use Paint and Canvas in a thread
    SurfaceHolder mOurHolder;

    volatile boolean mPlaying;
    boolean mPaused = true;

    Canvas mCanvas;
    Paint mPaint;
    long mFPS;

    int mScreenX;
    int mScreenY;

    Paddle mPaddle;
    Ball mBall;

    SoundPool sp;
    int beep1ID = -1;
    int beep2ID = -1;
    int beep3ID = -1;
    int looseLifeID = -1;
    int explodeID = -1;

    int mScore = 0;
    int mLives = 3;

    public GameView(Context context, int x, int y){
        //it asks SurfacView to setup out object
        super(context);
        mScreenX = x;
        mScreenY = y;

        mContext = context;

        mOurHolder = getHolder();
        mPaint = new Paint();

        mPaddle = new Paddle(mScreenX, mScreenY);
        mBall = new Ball(mScreenX, mScreenY);

        loadTheSounds();

        setupAndRestart();
    }

    private void loadTheSounds() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
            sp = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        try {
            AssetManager assetManager = mContext.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("beep1.ogg");
            beep1ID = sp.load(descriptor, 0);

            descriptor = assetManager.openFd("beep2.ogg");
            beep2ID = sp.load(descriptor, 0);

            descriptor = assetManager.openFd("beep3.ogg");
            beep3ID = sp.load(descriptor, 0);

            descriptor = assetManager.openFd("loseLife.ogg");
            looseLifeID = sp.load(descriptor, 0);

            descriptor = assetManager.openFd("explode.ogg");
            explodeID = sp.load(descriptor, 0);

        } catch (IOException e){
            Log.e(TAG, "failed to load sound files");
        }

    }

    private void setupAndRestart() {
        mBall.reset(mScreenX, mScreenY);

        if(mLives == 0){
            mScore = 0;
            mLives = 3;
        }
    }

    @Override
    public void run() {
        while(mPlaying){
            long startFrameTime = System.currentTimeMillis();

            if(!mPaused){
                update();
            }

            draw();

            long timeThisFrame = System.currentTimeMillis()-startFrameTime;
            if(timeThisFrame>=1){
                mFPS = 1000 / timeThisFrame;
            }
        }
    }


    //movement, collision detection, everything that needs updated goes here
    private void update() {
        mPaddle.update(mFPS);
        mBall.update(mFPS);

        ifTheBallHistsThePaddle();
        ifTheBallHitsTheBottomOfTheScreen();
        ifTheBallHitsTheTopOfTheScreen();
        ifTheBallHitsTheLeftOfTheScreen();
        ifTheBallHitsTheRightOfTheScreen();
    }

    private void ifTheBallHitsTheBottomOfTheScreen() {
        if(mBall.getRect().bottom > mScreenY){
            mBall.reverseYVelocity();
            mBall.clearObstacleY(mScreenY - 2);

            mLives--;
            beepPlay(looseLifeID);
            if(mLives == 0){
                mPaused = true;
                setupAndRestart();
            }
        }
    }

    private void ifTheBallHitsTheTopOfTheScreen() {
        if(mBall.getRect().top < 0){
            mBall.reverseYVelocity();
            mBall.clearObstacleY(12);
            beepPlay(beep2ID);
        }
    }

    private void ifTheBallHitsTheLeftOfTheScreen() {
        if(mBall.getRect().left < 0){
            mBall.reverseXVelocity();
            mBall.clearObstacleX(2);
            beepPlay(beep3ID);
        }
    }

    private void ifTheBallHitsTheRightOfTheScreen() {
        if(mBall.getRect().right > mScreenX){
            mBall.reverseXVelocity();
            mBall.clearObstacleX(mScreenX - 22);
            beepPlay(beep3ID);
        }
    }

    private void ifTheBallHistsThePaddle() {
        if(RectF.intersects(mPaddle.getRect(), mBall.getRect())){
            mBall.setRandomXVelocity();
            mBall.reverseYVelocity();
            mBall.clearObstacleY(mPaddle.getRect().top - 2);

            mScore++;
            mBall.increaseVelocity();
            beepPlay(beep1ID);
        }
    }

    private void beepPlay(int sound) {
        sp.play(sound, 1, 1, 0, 0, 1);

    }

    private void draw() {

        //make sure the drawing surface is valid
        if(mOurHolder.getSurface().isValid()){
            //lock the canvas in order to draw
            mCanvas = mOurHolder.lockCanvas();
            //draw the bgColor
            mCanvas.drawColor(Color.argb(255, 26, 128, 128));
            //set the brush color
            mPaint.setColor(Color.argb(255, 255, 255, 255));
            //draw the paddle
            mCanvas.drawRect(mPaddle.getRect(), mPaint);
            mCanvas.drawRect(mBall.getRect(), mPaint);

            mPaint.setColor(Color.argb(255, 255, 25, 255));
            mPaint.setTextSize(40);
            mCanvas.drawText("Score: " + mScore + " Lives: " + mLives,
                    10, 50, mPaint);
            mOurHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    public void pause(){
        mPlaying = false;
        try{
            mGameThread.join();
        } catch (InterruptedException e){
            Log.e(TAG, "joining thread");
        }
    }

    public void resume(){
        mPlaying = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & event.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                //is the touch on left or right
                if(event.getX() > mScreenX / 2){
                    mPaddle.setMovementState(mPaddle.RIGHT);
                } else {
                    mPaddle.setMovementState(mPaddle.LEFT);
                }
                mPaused = false;
                break;
            //player has removed finger from screen
            case MotionEvent.ACTION_UP:
                mPaddle.setMovementState(mPaddle.STOPPED);
                break;

        }
        return true;
    }
}
