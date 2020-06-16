package com.codingwithsara.catchtheball;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Elements
    private TextView scoreLabel, startLabel;
    private ImageView box, orange, pink, black;

    // Size
    private int screenWidth;
    private int frameHeight;
    private int boxSize;

    // Position
    private float boxY;
    private float orangeX, orangeY;
    private float pinkX, pinkY;
    private float blackX, blackY;

    // Speed
    private int boxSpeed, orangeSpeed, pinkSpeed, blackSpeed;

    // Score
    private int score;

    // Timer
    private Timer timer = new Timer();
    private Handler handler = new Handler();

    // Status
    private boolean action_flg = false;
    private boolean start_flg = false;

    // SoundPlayer
    private SoundPlayer soundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundPlayer = new SoundPlayer(this);

        scoreLabel = findViewById(R.id.scoreLabel);
        startLabel = findViewById(R.id.startLabel);
        box = findViewById(R.id.box);
        orange = findViewById(R.id.orange);
        pink = findViewById(R.id.pink);
        black = findViewById(R.id.black);

        // Screen Size
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenWidth = size.x;
        int screenHeight = size.y;

        // Nexus 4 width:768 height:1184
        // Speed box:20, orange:12, pink:20, black:16
        boxSpeed = Math.round(screenHeight / 60.0f); // 1184 / 60 = 19.733... => 20
        orangeSpeed = Math.round(screenWidth / 60.0f); // 768 / 60 = 12.8 => 13
        pinkSpeed = Math.round(screenWidth / 36.0f); // 768 / 36 = 21.333 => 21
        blackSpeed = Math.round(screenWidth / 45.0f); // 768 / 45 = 17.06... => 17

//        Log.v("SPEED_BOX", boxSpeed + "");
//        Log.v("SPEED_ORANGE", orangeSpeed + "");
//        Log.v("SPEED_PINK", pinkSpeed + "");
//        Log.v("SPEED_BLACK", blackSpeed + "");

        // Initial Positions
        orange.setX(-80.0f);
        orange.setY(-80.0f);
        pink.setX(-80.0f);
        pink.setY(-80.0f);
        black.setX(-80.0f);
        black.setY(-80.0f);

        //scoreLabel.setText("Score : " + score);
        scoreLabel.setText(getString(R.string.score, score));
    }

    public void changePos() {

        hitCheck();

        // Orange
        orangeX -= orangeSpeed;
        if (orangeX < 0) {
            orangeX = screenWidth + 20;
            orangeY = (float)Math.floor(Math.random() * (frameHeight - orange.getHeight()));
        }
        orange.setX(orangeX);
        orange.setY(orangeY);

        // Black
        blackX -= blackSpeed;
        if (blackX < 0) {
            blackX = screenWidth + 10;
            blackY = (float)Math.floor(Math.random() * (frameHeight - black.getHeight()));
        }
        black.setX(blackX);
        black.setY(blackY);

        // Pink
        pinkX -= pinkSpeed;
        if (pinkX < 0) {
            pinkX = screenWidth + 5000;
            pinkY = (float)Math.floor(Math.random() * (frameHeight - pink.getHeight()));
        }
        pink.setX(pinkX);
        pink.setY(pinkY);

        // Box
        if (action_flg) {
            // Touching
            boxY -= boxSpeed;
        } else {
            // Releasing
            boxY += boxSpeed;
        }

        if (boxY < 0) boxY = 0;
        if (boxY > frameHeight - boxSize) boxY = frameHeight - boxSize;

        box.setY(boxY);

        //scoreLabel.setText("Score : " + score);
        scoreLabel.setText(getString(R.string.score, score));
    }

    public void hitCheck() {

        // Orange
        float orangeCenterX = orangeX + orange.getWidth() / 2.0f;
        float orangeCenterY = orangeY + orange.getHeight() / 2.0f;

        if (0 <= orangeCenterX && orangeCenterX <= boxSize &&
                boxY <= orangeCenterY && orangeCenterY <= boxY + boxSize) {
            orangeX = -100.0f;
            score += 10;
            soundPlayer.playHitSound();
        }

        // Pink
        float pinkCenterX = pinkX + pink.getWidth() / 2.0f;
        float pinkCenterY = pinkY + pink.getHeight() / 2.0f;

        if (0 <= pinkCenterX && pinkCenterX <= boxSize &&
                boxY <= pinkCenterY && pinkCenterY <= boxY + boxSize) {
            pinkX = -100.0f;
            score += 30;
            soundPlayer.playHitSound();
        }

        // Black
        float blackCenterX = blackX + black.getWidth() / 2.0f;
        float blackCenterY = blackY + black.getHeight() / 2.0f;

        if (0 <= blackCenterX && blackCenterX <= boxSize &&
                boxY <= blackCenterY && blackCenterY <= boxY + boxSize) {

            soundPlayer.playOverSound();

            // Game Over!!
            if (timer != null) {
                timer.cancel();
                timer = null;
            }

            // Show ResultActivity
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            intent.putExtra("SCORE", score);
            startActivity(intent);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!start_flg) {
            start_flg = true;

            // FrameHeight
            FrameLayout frameLayout = findViewById(R.id.frame);
            frameHeight = frameLayout.getHeight();

            // Box
            boxY = box.getY();
            boxSize = box.getHeight();

            startLabel.setVisibility(View.GONE);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePos();
                        }
                    });
                }
            }, 0, 20);

        } else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                action_flg = true;

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                action_flg = false;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {}
}