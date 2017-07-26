package ro.proba.pongdemo;

import android.graphics.RectF;

public class Paddle {

    private RectF mRect;

    //how long and high the paddle is
    private  float mLenght;
    private  float mHeight;

    //the far left of the paddle
    private float mXCoord;

    //the far left of the tri
    private float mYCoord;

    //pixels per second
    private float mPaddleSpeed;

    //which ways the paddle can move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    //is the paddle moving and which direction
    private int mPaddleMoving = STOPPED;

    //the screen size
    private int mScreenX;
    private int mScreenY;

    /**
     * pass the screen width anf height
     */
    public Paddle(int x, int y){
        mScreenX = x;
        mScreenY = y;

        mLenght = mScreenX / 8;
        mHeight = mScreenY / 25;

        mXCoord = mScreenX / 2;
        mYCoord = mScreenY - 20;

        mRect = new RectF(mXCoord, mYCoord, mXCoord + mLenght, mYCoord + mHeight);

        // the paddle speed & cover entire screen in 1 second
        mPaddleSpeed = mScreenX;
    }

    //paddle rectangle available to GameView class
    public RectF getRect(){
        return mRect;
    }

    //change or set the mPadding direction or state
    public void setMovementState(int state){
        mPaddleMoving = state;
    }

    public void update(long fps){
        if(mPaddleMoving == LEFT){
            mXCoord = mXCoord - mPaddleSpeed / fps;
        }
        if(mPaddleMoving == RIGHT){
            mXCoord = mXCoord + mPaddleSpeed / fps;
        }
        if(mRect.left < 0){
            mXCoord = 0;
        }
        //make sure is not leaving screen
        if(mRect.right > mScreenX){
            mXCoord = mScreenX -
                    //the width of the paddle
                    (mRect.right - mRect.left);
        }

        //update de paddle graphics
        mRect.left = mXCoord;
        mRect.right = mXCoord + mLenght;
    }
}
