package com.example.footballplatform.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;

import com.example.footballplatform.R;
import com.example.footballplatform.databinding.FragmentGameBinding;
import com.example.footballplatform.game.Platform;

import java.util.Random;

public class GameFragment extends Fragment {
    private FragmentGameBinding binding;
    BreakoutView breakoutView;
    Platform platform;
    public int screenX;
    public int screenY;
    public int xCoordinate;
    public int yCoordinate;
    Bitmap ball;
    Bitmap background;
    float xVelocity;
    float yVelocity;
    private int playerScore;
    private int bestScore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGameBinding.inflate(getLayoutInflater());

        breakoutView = new BreakoutView(getContext());
        // Get a Display object to access screen details
        Display display = getActivity().getWindowManager().getDefaultDisplay();
// Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);
        screenX = size.x;
        screenY = size.y;
        xVelocity = 400;
        yVelocity = -600;

        SharedPreferences sp = getActivity().getSharedPreferences("score", 0);
        bestScore = sp.getInt("score", 0);

        platform = new Platform(screenX, screenY);
        ball = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ball);
        xCoordinate = (screenX / 2) - (ball.getWidth()/2);
        yCoordinate = (int) platform.getRect().top - ball.getHeight();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        return breakoutView;
    }

    // Here is our implementation of GameView
    // It is an inner class.
    // Note how the final closing curly brace }
    // is inside SimpleGameEngine

    // Notice we implement runnable so we have
    // A thread and can override the run method.
    class BreakoutView extends SurfaceView implements Runnable {

        // This is our thread
        Thread gameThread = null;


        // This is new. We need a SurfaceHolder
        // When we use Paint and Canvas in a thread
        // We will see it in action in the draw method soon.
        SurfaceHolder ourHolder;


        // A boolean which we will set and unset
        // when the game is running- or not.
        volatile boolean playing;

        // Game is paused at the start
        boolean paused = true;

        // A Canvas and a Paint object
        Canvas canvas;
        Paint paint;
        Paint strokePaint;

        // This variable tracks the game frame rate
        long fps;

        // This is used to help calculate the fps
        private long timeThisFrame;

        // When the we initialize (call new()) on gameView
        // This special constructor method runs
        public BreakoutView(Context context) {
            // The next line of code asks the
            // SurfaceView class to set up our object.
            // How kind.
            super(context);

            // Initialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();
            paint.setTextSize(50);
            paint.setStrokeWidth(5);
            strokePaint = new Paint();
            strokePaint.setStrokeWidth(5);
            strokePaint.setStyle(Paint.Style.STROKE);
        }

        @Override
        public void run() {
            while (playing) {


                // Capture the current time in milliseconds in startFrameTime
                long startFrameTime = System.currentTimeMillis();

                // Update the frame
                // Update the frame
                if (!paused) {
                    update();
                }

                // Draw the frame
                draw();

                // Calculate the fps this frame
                // We can then use the result to
                // time animations and more.
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }

            }

        }

        // Everything that needs to be updated goes in here
        // Movement, collision detection etc.
        public void update() {
            platform.update(fps);
            xCoordinate += xVelocity / fps;
            yCoordinate += yVelocity / fps;

            // Bounce the ball back when it hits the top of screen
            if (yCoordinate < 0) {
                reverseYVelocity();
            }
            // If the ball hits left wall bounce
            if (xCoordinate < 0) {
                reverseXVelocity();
            }
            // If the ball hits right wall bounce
            if (xCoordinate > screenX - ball.getHeight()) {
                reverseXVelocity();
            }
            // Bounce the ball back when it hits the bottom of screen
            // And deduct a life
            if (yCoordinate > screenY - ball.getWidth()) {
                if (bestScore <= playerScore) {
                    bestScore = playerScore;
                }
                SharedPreferences sp = getActivity().getSharedPreferences("score", 0);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("score", bestScore);
                editor.apply();
                playerScore = 0;
                paused = true;
                restart();
            }

            if (platform.getRect().right > screenX) {
                // left x, top y, right x + length, bottom  y + height
                platform.setMovementState(platform.STOPPED);
                platform.getRect().left = screenX - platform.getRect().width();
                platform.getRect().top = screenY - platform.getRect().height();
                platform.getRect().right = screenX;
                platform.getRect().bottom = screenY;
            }

            if (platform.getRect().left < 0) {
                platform.setMovementState(platform.STOPPED);
                platform.getRect().left = 0;
                platform.getRect().top = screenY - platform.getRect().height();
                platform.getRect().right = platform.getRect().width();
                platform.getRect().bottom = screenY;
            }

            RectF invisibleRectBall = new RectF(xCoordinate, yCoordinate, xCoordinate + ball.getWidth(), yCoordinate + ball.getHeight());
            // Check for ball colliding with paddle
            if (RectF.intersects(platform.getRect(), invisibleRectBall)) {
                setRandomXVelocity();
                reverseYVelocity();
                playerScore += 1;
            }
        }

        // Draw the newly updated scene
        public void draw() {

            // Make sure our drawing surface is valid or we crash
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                canvas = ourHolder.lockCanvas();

                // Draw the background color
                canvas.drawColor(Color.argb(255, 1, 128, 1));

                canvas.drawCircle(screenX/2, screenY/2,10, paint);
                canvas.drawCircle(screenX/2, screenY/2,100, strokePaint);
                canvas.drawLine(50, 50, screenX - 50,  50, paint);
                canvas.drawLine(50, 50, 50,  screenY - 50, paint);
                canvas.drawLine(50, screenY - 50, screenX - 50,  screenY - 50, paint);
                canvas.drawLine(screenX - 50, 50, screenX - 50,  screenY - 50, paint);
                canvas.drawLine(50, screenY/2, screenX - 50,  screenY/2, paint);


                // Choose the brush color for drawing
                paint.setColor(Color.argb(255, 255, 255, 255));
                strokePaint.setColor(Color.argb(255, 255, 255, 255));

                // Draw the paddle
                canvas.drawRect(platform.getRect(), paint);



                // Draw the ball

                canvas.drawBitmap(ball, xCoordinate, yCoordinate, paint);

                // Draw the bricks

                // Draw the HUD

                canvas.drawText("Ваш счёт: " + playerScore, 100, 100, paint);
                canvas.drawText("Лучший счёт: " + bestScore, 100, 170, paint);

                // Draw everything to the screen
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        // If SimpleGameEngine Activity is paused/stopped
        // shutdown our thread.
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // If SimpleGameEngine Activity is started theb
        // start our thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        // The SurfaceView class implements onTouchListener
        // So we can override this method and detect screen touches.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                //Player has touched the screen
                case MotionEvent.ACTION_DOWN:

                    paused = false;

                    if (motionEvent.getX() > screenX / 2) {
                        platform.setMovementState(platform.RIGHT);
                    } else {
                        platform.setMovementState(platform.LEFT);
                    }

                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:

                    platform.setMovementState(platform.STOPPED);
                    break;
            }
            return true;
        }

    }

    private void restart() {
        playerScore = 0;
        platform = new Platform(screenX, screenY);
        reverseYVelocity();
        xCoordinate = (screenX / 2) - (ball.getWidth()/2);
        yCoordinate = (int) platform.getRect().top - ball.getHeight();
    }
    // This is the end of our BreakoutView inner class

    // This method executes when the player starts the game
    @Override
    public void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        breakoutView.resume();
    }

    // This method executes when the player quits the game
    @Override
    public void onPause() {
        super.onPause();

        breakoutView.pause();
    }

    public Bitmap getBitmap() {
        return ball;
    }

    public void reverseYVelocity() {
        yVelocity = -yVelocity;
    }

    public void reverseXVelocity() {
        xVelocity = -xVelocity;
    }

    public void setRandomXVelocity() {
        Random generator = new Random();
        int answer = generator.nextInt(2);

        if (answer == 0) {
            reverseXVelocity();
        }
    }
}