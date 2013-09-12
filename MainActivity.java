package com.gk.firsttask;

import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("WrongCall")
public class MainActivity extends Activity {
	Canvas canvas;
	WhirlView whirlView;
	int[][] field2 = null;
	int[][] field = null;
	int width = 0;
	int height = 0;
	int scale = 4;
	final int MAX_COLOR = 15;
	Paint[] p = new Paint[MAX_COLOR];
	int[] palette = { 0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000,
			0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080,
			0xFFFFFFFF, 0xFF888888, 0x11, 0xFFFFFF11, 0xFF22FFFF, 0xFF123456 };
	SurfaceHolder holder;
	Thread thread = null;
	volatile boolean running = false;

	class WhirlView extends SurfaceView implements Runnable {

		public WhirlView(Context context) {
			super(context);
			holder = getHolder();
		}

		public void resume() {
			running = true;
			thread = new Thread(this);
			thread.start();
		}

		public void pause() {
			running = false;
			try {
				thread.join();
			} catch (InterruptedException ignore) {
			}
		}

		public void run() {
			while (running) {
				if (holder.getSurface().isValid()) {
					long startTime = System.nanoTime();
					canvas = holder.lockCanvas();
					updateField();
					onDraw(canvas);
					holder.unlockCanvasAndPost(canvas);
					long finishTime = System.nanoTime();
					Log.i("TIME", "Circle: " + (finishTime - startTime)
							/ 1000000);
					try {
						Thread.sleep(16);
					} catch (InterruptedException ignore) {
					}
				}
			}
		}

		@Override
		public void onSizeChanged(int w, int h, int oldW, int oldH) {
			width = w / scale;
			height = h / scale;

			initField();
		}

		void initField() {
			field = new int[width][height];
			Random rand = new Random();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					field[x][y] = rand.nextInt(MAX_COLOR);
				}
			}

			for (int i = 0; i < MAX_COLOR; i++) {
				p[i] = new Paint();
				p[i].setColor(palette[i]);
			}
		}

		void updateField() {
			field2 = new int[width][height];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {

					field2[x][y] = field[x][y];

					for (int dx = -1; dx <= 1; dx++) {
						for (int dy = -1; dy <= 1; dy++) {
							int x2 = x + dx;
							int y2 = y + dy;
							if (x2 < 0)
								x2 += width;
							if (y2 < 0)
								y2 += height;
							if (x2 >= width)
								x2 -= width;
							if (y2 >= height)
								y2 -= height;
							if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
								field2[x][y] = field[x2][y2];
							}
						}
					}
				}
			}
			field = field2;
		}

		@Override
		public void onDraw(Canvas canvas) {

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {

					canvas.drawRect(x * scale, y * scale, (x + 1) * scale,
							(y + 1) * scale, p[field[x][y]]);
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		whirlView.resume();
	}

	@Override
	public void onPause() {
		super.onPause();
		whirlView.pause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		whirlView = new WhirlView(this);
		setContentView(whirlView);
	}
}