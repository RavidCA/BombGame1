package com.example.game1

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import android.os.Build

class MainActivity : AppCompatActivity() {


    private lateinit var actor: ImageView

    private lateinit var btnLeft: ImageView

    private lateinit var btnRight: ImageView

    private lateinit var bomb: ImageView

    private lateinit var heart0: ImageView
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView

    private lateinit var gameOverBG: View
    private lateinit var gameOverPanel: View

    private lateinit var btnRestart: View

    private lateinit var vibrator: Vibrator

    // השחקן יתחיל משמאל/0      0 שמאל 1 אמצע 2 ימין
    private var actorPosition = 0

    private var bombLane = 0

    private var lifes = 3
    private var alreadyHit: Boolean = false
    private var isGameOver = false

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
        initBomb()
        initLife()

    }

private fun initViews() {
    actor = findViewById(R.id.Chinese_actor)
    btnLeft = findViewById(R.id.Left_arrow)
    btnRight = findViewById(R.id.Right_arrow)

    btnLeft.setOnClickListener { moveLeft() }
    btnRight.setOnClickListener { moveRight() }

    gameOverBG = findViewById(R.id.game_over_bg)
    gameOverPanel = findViewById(R.id.game_over_panel)
    btnRestart = findViewById(R.id.btnRestart)

    btnRestart.setOnClickListener {
        restartGame()
    }

    updateActorPosition()
   }

private fun initBomb() {
    bomb = findViewById(R.id.Bomb)

    bomb.post {
        bombLane = (0..2).random()
        bomb.x = getBombLaneX(bombLane)
        bomb.y = 0f

        startBombFall()
    }
}


    private fun initLife(){
        heart0 = findViewById(R.id.heart0)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
    }

    private fun moveLeft() { // אם השחקן כבר בשמאל אז לא יזוז ואם בימין ישתנה לשמאל
        if (actorPosition == 1) {
            actorPosition = 0
            updateActorPosition()
        }
        else if (actorPosition == 2) {
            actorPosition = 1
            updateActorPosition()
        }
    }
    private fun moveRight() {
        if (actorPosition == 0) {
            actorPosition = 1
            updateActorPosition()
        }
        else if (actorPosition == 1) {
            actorPosition = 2
            updateActorPosition()
        }
    }

    private fun updateActorPosition() {
        actor.animate()
            .x(getLaneX(actorPosition))
            .setDuration(150)
            .start()
    }


    private fun getLaneX(position: Int): Float {
        val screenWidth = resources.displayMetrics.widthPixels

        return when (position) {
            0 -> screenWidth * 0.10f   // שמאל
            1 -> screenWidth * 0.35f   // אמצע
            2 -> screenWidth * 0.60f   // ימין
            else -> screenWidth * 0.35f
        }
    }

    private fun getBombLaneX(lane: Int): Float{
        val screenWidth = resources.displayMetrics.widthPixels

        return when (lane)  {
            0 -> screenWidth * 0.10f
            1 -> screenWidth * 0.35f
            2 -> screenWidth * 0.60f
            else -> screenWidth * 0.35f
        }
    }

    private fun loseLife() {
        vibrate(150)  // רטט של 150m
        lifes--

        when (lifes) {
            2 -> heart0.visibility = View.INVISIBLE
            1 -> heart1.visibility = View.INVISIBLE
            0 -> {
                heart2.visibility = View.INVISIBLE
                gameOver()
            }
        }
    }

    private fun vibrate(duration: Long = 150) {
        if (vibrator.hasVibrator()) {
            val effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        }
    }


    private fun startBombFall() {
        if (isGameOver) return
        val screenHeight = resources.displayMetrics.heightPixels
        val bottomMargin = 150
        val targetY = screenHeight - bottomMargin - bomb.height

        bombLane = (0..2).random()
        bomb.x = getBombLaneX(bombLane)
        bomb.y = 0f
        bomb.visibility = View.VISIBLE
        alreadyHit = false  // אתחול לדגל בתחילת הנפילה

        val animator = bomb.animate()
            .y(targetY.toFloat())
            .setDuration(3500)

        // בדיקה מתמשכת תוך כדי הנפילה
        val updateRunnable = object : Runnable {
            override fun run() {
                if (!alreadyHit && isCollision(bomb, actor)) {
                    loseLife()
                    alreadyHit = true  // מונע הורדת חיים שוב באותה נפילה
                }
                if (bomb.y < targetY) {
                    bomb.postDelayed(this, 10) // בודק כל 10ms
                }
            }
        }
        bomb.post(updateRunnable)

        animator.withEndAction {
            bomb.visibility = View.INVISIBLE
            bomb.postDelayed({
                startBombFall()
            }, 300)
        }.start()
    }


    private fun isCollision(bomb: View, actor: View): Boolean {
        val bombLeft = bomb.x // זה כבר כולל translationX
        val bombTop = bomb.y  // זה כבר כולל translationY
        val bombRight = bombLeft + bomb.width
        val bombBottom = bombTop + bomb.height

        val actorLeft = actor.x
        val actorTop = actor.y
        val actorRight = actorLeft + actor.width
        val actorBottom = actorTop + actor.height

        val padding = 110
        val horizontalOverlap = bombRight - padding >= actorLeft + padding && bombLeft + padding <= actorRight - padding
        val verticalOverlap = bombBottom - padding >= actorTop + padding && bombTop + padding <= actorBottom - padding

        return horizontalOverlap && verticalOverlap

    }



    private fun restartGame() {
        lifes = 3
        alreadyHit = false
        isGameOver = false

        heart0.visibility = View.VISIBLE
        heart1.visibility = View.VISIBLE
        heart2.visibility = View.VISIBLE

        gameOverBG.visibility = View.GONE
        gameOverPanel.visibility = View.GONE

        // הפצצה מתחילה מחדש
        bomb.clearAnimation()
        bomb.y = 0f
        startBombFall()
    }

    private fun gameOver() {
        gameOverBG.visibility = View.VISIBLE
        gameOverPanel.visibility = View.VISIBLE
        isGameOver = true
    }

}