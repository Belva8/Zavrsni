package com.example.maturalniradbrojackoraka

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun OdvediDoBrojacaKoraka(v: View) {
        val mIntent = Intent(this, BrojacKorakaAktivnost::class.java)
        startActivity(mIntent)
    }

    fun OdvediDoPovijestiKoraka(v: View) {
        val mIntent = Intent(this, PovijestKoraka::class.java)
        startActivity(mIntent)
    }

    fun OdvediDoRezultata(v: View) {
        val mIntent = Intent(this, RezultatiAktivnost::class.java)
        startActivity(mIntent)
    }
}
