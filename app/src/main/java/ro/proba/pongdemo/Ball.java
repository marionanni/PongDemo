package ro.proba.pongdemo;

import android.graphics.RectF;

import java.util.Random;

public class Ball {

    private RectF mRect;
    private float mXVelocity;
    private float mYVelocity;
    private float mBallWidth;
    private float mBallHeight;

    public Ball(int screenX, int screenY) {
        mBallWidth = screenX / 100;
        mBallHeight = mBallWidth;

        mYVelocity = screenY / 4;
        mXVelocity = mYVelocity;

        mRect = new RectF();
    }

    public RectF getRect() {
        return mRect;
    }

    public void update(long fps){
        mRect.left = mRect.left + (mXVelocity / fps);
        mRect.top = mRect.top + (mYVelocity / fps);
        mRect.right = mRect.left + mBallWidth;
        mRect.bottom = mRect.top - mBallHeight;
    }

    //reverse the vertical heading
    public void reverseYVelocity(){
        mYVelocity = -mYVelocity;
    }

    //reverse the horizontal heading
    public void reverseXVelocity(){
        mXVelocity = -mXVelocity;
    }

    public void setRandomXVelocity(){
        Random generator = new Random();
        int answer = generator.nextInt(2);

        if(answer == 0){
            reverseXVelocity();
        }
    }

    //speed up by 10%
    public void increaseVelocity(){
        mXVelocity = mXVelocity + mXVelocity / 10;
        mYVelocity = mYVelocity + mYVelocity / 10;
    }

    public void clearObstacleY(float y){
        mRect.bottom = y;
        mRect.top = y - mBallHeight;
    }

    public void clearObstacleX(float x){
        mRect.left = x;
        mRect.right = x + mBallWidth;
    }

    public void reset(int x, int y){
        mRect.left = x / 2;
        mRect.top = y - 20;
        mRect.right = mRect.left + mBallWidth;
        mRect.bottom = mRect.top - mBallHeight;
    }

}
