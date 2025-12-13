package com.example.game1

import android.content.Context
import android.graphics.Color
import android.os.*
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    companion object {
        private const val NUM_ROWS = 7
        private const val NUM_COLS = 3
        private const val PLAYER_ROW = NUM_ROWS - 1
        private const val GAME_SPEED = 400L
        private const val MAX_BOMBS = 4
    }

    private lateinit var hearts: Array<ImageView>
    private lateinit var btnLeft: ImageView
    private lateinit var btnRight: ImageView
    private lateinit var gameOverBG: View
    private lateinit var gameOverPanel: View
    private lateinit var btnRestart: View
    private lateinit var vibrator: Vibrator

    private lateinit var cells: Array<Array<View>>

    private var playerCol = 1
    private var lives = 3
    private var isGameOver = false

    private val handler = Handler(Looper.getMainLooper())

    // רשימת טילים במקביל
    private val bombs = mutableListOf<Bombs>()
    data class Bombs(var row: Int, var col: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        initViews()
        initMatrix()
        resetGame()
        startGameLoop()
    }

    private fun initViews() {
        hearts = arrayOf(
            findViewById(R.id.heart0),
            findViewById(R.id.heart1),
            findViewById(R.id.heart2)
        )

        btnLeft = findViewById(R.id.Left_arrow)
        btnRight = findViewById(R.id.Right_arrow)
        gameOverBG = findViewById(R.id.game_over_bg)
        gameOverPanel = findViewById(R.id.game_over_panel)
        btnRestart = findViewById(R.id.btnRestart)

        btnLeft.setOnClickListener { movePlayer(-1) }
        btnRight.setOnClickListener { movePlayer(1) }
        btnRestart.setOnClickListener { restartGame() }
    }

    private fun initMatrix() {
        cells = Array(NUM_ROWS) { row ->
            Array(NUM_COLS) { col ->
                val idName = "mat_${row}_${col}"
                val resId = resources.getIdentifier(idName, "id", packageName)
                findViewById(resId)
            }
        }
    }

    private fun startGameLoop() {
        handler.post(gameRunnable)
    }

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (!isGameOver) {
                moveBombs()
                checkCollisions()
                draw()
                handler.postDelayed(this, GAME_SPEED)
            }
        }
    }

    private fun movePlayer(direction: Int) {
        val newCol = playerCol + direction
        if (newCol in 0 until NUM_COLS) playerCol = newCol
    }

    private fun moveBombs() {

        // הזזת כל הטילים למטה
        bombs.forEach { it.row++ }

        // הסרת טילים שיצאו מהמסך
        bombs.removeAll { it.row > PLAYER_ROW }

        // יצירת טיל חדש בכל טיק (כמעט תמיד)
        if (bombs.size < MAX_BOMBS) {
            bombs.add(
                Bombs(
                    row = 0,
                    col = Random.nextInt(NUM_COLS)
                )
            )
        }
    }


    private fun checkCollisions() {
        if (bombs.any { it.row == PLAYER_ROW && it.col == playerCol }) {
            loseLife()
        }
    }

    private fun draw() {
        // ניקוי כל התאים
        for (r in 0 until NUM_ROWS) {
            for (c in 0 until NUM_COLS) {
                cells[r][c].setBackgroundColor(Color.TRANSPARENT)
            }
        }

        // ציור טילים
        bombs.forEach {
            if (it.row in 0 until NUM_ROWS) {
                cells[it.row][it.col].setBackgroundResource(R.drawable.alien)
            }
        }

        // ציור השחקן
        cells[PLAYER_ROW][playerCol].setBackgroundResource(R.drawable.spaceman)
    }

    private fun loseLife() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        lives--
        when (lives) {
            2 -> hearts[0].visibility = View.INVISIBLE
            1 -> hearts[1].visibility = View.INVISIBLE
            0 -> {
                hearts[2].visibility = View.INVISIBLE
                gameOver()
            }
        }
    }

    private fun gameOver() {
        isGameOver = true
        gameOverBG.visibility = View.VISIBLE
        gameOverPanel.visibility = View.VISIBLE
        handler.removeCallbacks(gameRunnable)
    }

    private fun restartGame() {
        resetGame()
        startGameLoop()
    }

    private fun resetGame() {
        isGameOver = false
        lives = 3
        hearts.forEach { it.visibility = View.VISIBLE }
        gameOverBG.visibility = View.GONE
        gameOverPanel.visibility = View.GONE
        playerCol = 1
        bombs.clear()
        draw()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameRunnable)
    }
}
