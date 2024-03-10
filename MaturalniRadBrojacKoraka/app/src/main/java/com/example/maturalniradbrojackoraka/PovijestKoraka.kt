package com.example.maturalniradbrojackoraka

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class PovijestKoraka : AppCompatActivity() {


    private lateinit var mKoraciBP: KoraciBP
    private lateinit var mSensorListView: ListView
    private lateinit var mListAdapter: ListAdapter
    private lateinit var mListaKoraka: ArrayList<DatumKoraka>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_koraka)
        mSensorListView = findViewById(R.id.ListaKoraka)

        DohvatiPodatkeZaListu()

        mListAdapter = ListAdapter(mListaKoraka, this)
        mSensorListView.adapter = mListAdapter

        val stepsIntent = Intent(applicationContext, UslugaKoraci::class.java)
        startService(stepsIntent)
    }

    private fun DohvatiPodatkeZaListu() {
        mKoraciBP = KoraciBP(this)
        mListaKoraka = mKoraciBP.ProcitajUnosKoraka()
    }
}

