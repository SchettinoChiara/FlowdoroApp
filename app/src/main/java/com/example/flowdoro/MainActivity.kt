package com.example.flowdoro

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import android.media.MediaPlayer
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import pl.droidsonroids.gif.GifImageView
import android.graphics.drawable.ColorDrawable;
import android.os.Build
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    var mediaPlayer: MediaPlayer? = null
    var playCount = 0 // Conta quante volte il suono è stato riprodotto


    var isRunningCron = false
    var isRunningTimer = false
    var timerSeconds = 0 // Usato per cronometro e countdown
    var handler = Handler(Looper.getMainLooper())

    // Runnable per il cronometro
    val runnableCron = object : Runnable {
        override fun run() {
            timerSeconds++
            val hours = timerSeconds / 3600
            val minutes = (timerSeconds % 3600) / 60
            val seconds = timerSeconds % 60

            val time = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            val cronometroTextView = findViewById<TextView>(R.id.cronometro)
            cronometroTextView.text = time

            handler.postDelayed(this, 1000)
        }
    }
    // Riferimenti per le GIF
    private lateinit var gifFocus: GifImageView
    private lateinit var gifBreak: GifImageView

    // Runnable per il countdown
    val runnableCountdown = object : Runnable {
        override fun run() {
            if (timerSeconds > 0) {
                timerSeconds--
                val hours = timerSeconds / 3600
                val minutes = (timerSeconds % 3600) / 60
                val seconds = timerSeconds % 60

                val time = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                val cronometroTextView = findViewById<TextView>(R.id.cronometro)
                cronometroTextView.text = time

                handler.postDelayed(this, 1000) // Ripeti ogni secondo
            } else {
                stopCountdown() // Ferma il countdown quando il tempo è scaduto
            }
        }
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()
        setContentView(R.layout.activity_main)
        // Imposta il colore della barra di stato
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.beige))}

        val pulsanteFocus: Button = findViewById<Button>(R.id.button_main)
        val layoutMain = findViewById<ConstraintLayout>(R.id.main)

        // Inizializza le GIF
        gifFocus = findViewById(R.id.gifFocus)
        gifBreak = findViewById(R.id.gifBreak)


        // Controlla se lo sfondo è un'animazione
        val background = layoutMain.background as AnimationDrawable
        //avvia sfondo
        background.setEnterFadeDuration(4000)
        background.setExitFadeDuration(4000)

        // Imposta listener per il pulsante
        pulsanteFocus.setOnClickListener {
            if (isRunningTimer) {
                stopCountdown(manualStop = true) // Ferma manualmente il countdown
            } else if (!isRunningCron) {
                startCronometro() // Avvia il cronometro
                background.start()
            } else {
                stopCronometro() // Ferma il cronometro e inizia il countdown
                background.stop()
            }
        }

    }fun playSoundThreeTimes() {
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        playCount = 0 // Resetta il contatore delle riproduzioni

        mediaPlayer?.setOnCompletionListener {
            playCount++
            if (playCount < 3) {
                mediaPlayer?.start() // Riproduci di nuovo finché non raggiungi 3 volte
            } else {
                mediaPlayer?.release() // Libera le risorse di MediaPlayer dopo 3 volte
                mediaPlayer = null
            }
        }

        mediaPlayer?.start() // Inizia la riproduzione del suono
    }


    // Funzione per avviare il cronometro
    // Funzione per avviare il cronometro
    fun startCronometro() {
        // Se il suono è in riproduzione, fermalo
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop() // Ferma la riproduzione del suono
            mediaPlayer?.release() // Rilascia le risorse del MediaPlayer
            mediaPlayer = null // Imposta il mediaPlayer a null
        }

        if (!isRunningCron) {
            val cronometroTextView = findViewById<TextView>(R.id.cronometro)
            cronometroTextView.text = "00:00:00"
            handler.postDelayed(runnableCron, 1000)
            timerSeconds = 0
            isRunningCron = true

            val pulsanteFocus: Button = findViewById<Button>(R.id.button_main)
            pulsanteFocus.text = "Break" // Cambia il testo del pulsante

// Nascondi la GIF di break con animazione di dissolvenza
            gifBreak.animate().alpha(0f).setDuration(500).withEndAction {
                gifBreak.visibility = View.GONE
            }.start()

            // Mostra la GIF di focus con animazione di dissolvenza
            gifFocus.visibility = View.VISIBLE
            gifFocus.alpha = 0f
            gifFocus.animate().alpha(1f).setDuration(500).start()
        }
    }



    // Funzione per fermare il cronometro e avviare il countdown
    fun stopCronometro() {
        if (isRunningCron) {
            handler.removeCallbacks(runnableCron)
            isRunningCron = false

            // Inizia il countdown usando il tempo salvato dal cronometro
            startCountdown()

            val pulsanteFocus: Button = findViewById<Button>(R.id.button_main)
            pulsanteFocus.text = "End break" // Cambia il testo del pulsante
            // Nascondi la GIF di focus con animazione di dissolvenza
            gifFocus.animate().alpha(0f).setDuration(500).withEndAction {
                gifFocus.visibility = View.GONE
            }.start();

            // Mostra la GIF di break con animazione di dissolvenza
            gifBreak.visibility = View.VISIBLE
            gifBreak.alpha = 0f
            gifBreak.animate().alpha(1f).setDuration(500).start()
        }

    }

    // Modifica per fermare manualmente il countdown se viene premuto il pulsante
    fun onPulsanteFocusClicked() {
        if (isRunningTimer) {
            stopCountdown(manualStop = true) // Ferma manualmente il countdown
        }
    }


    // Funzione per avviare il countdown
    fun startCountdown() {
        if (!isRunningTimer && timerSeconds > 0) {
            timerSeconds /= 5 // Imposta il countdown a un quinto del tempo del cronometro
            timerSeconds= timerSeconds+1
            isRunningTimer = true
            handler.postDelayed(runnableCountdown, 1000) // Inizia il countdown ogni secondo
        }
    }


    // Funzione per fermare il countdown
    fun stopCountdown(manualStop: Boolean = false) {
        if (isRunningTimer) {
            handler.removeCallbacks(runnableCountdown)
            isRunningTimer = false

            val pulsanteFocus: Button = findViewById<Button>(R.id.button_main)
            pulsanteFocus.text = "Focus" // Cambia il testo del pulsante alla fine del countdown

            // Nascondi la GIF di break con animazione di dissolvenza
            gifBreak.animate().alpha(0f).setDuration(500).withEndAction {
                gifBreak.visibility = View.GONE
            }.start()


            // Riproduci il suono 3 volte solo se il countdown è arrivato a zero e non è stato fermato manualmente
            if (timerSeconds == 0 && !manualStop) {
                playSoundThreeTimes()
                pulsanteFocus.text = "Focus"
            }
            else{val cronometroTextView = findViewById<TextView>(R.id.cronometro)
                cronometroTextView.text = "00:00:00"}
        }
    }
}