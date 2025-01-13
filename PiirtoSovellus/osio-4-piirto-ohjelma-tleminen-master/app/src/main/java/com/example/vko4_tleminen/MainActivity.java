package com.example.vko4_tleminen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    piirtoKangas piirtoKangas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Etsitään layoutista kyseinen piirtoalue
        piirtoKangas = findViewById(R.id.canvas);
    }

    //Tämän metodin avulla voidaan antaa fragmentti luokalle piirtoalue
    public piirtoKangas getActiveCanvas() {
        return piirtoKangas;
    }


    //Uusi piirtoalue
    public void newFile(View view){
        /*
        Dialogin rakennus aloitetaan luomalla MaterialAlertDialogBuilder-luokan olio.
        Tälle oliolle asetetaan metodeita käyttäen tarvittavat tiedot.
         */
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        //.setMessage-metodi asettaa dialogin päätekstin.
        builder.setMessage(R.string.dialog_new_info);

        //LAMBDA FUNKTIOT

        // Vahvista nappia kun painetaan niin kutsutaan metodia ClearCanvas joka tyhjentää piirtoalueen
        builder.setPositiveButton(R.string.dialog_confirm,
                (dialogInterface, i) -> piirtoKangas.ClearCanvas());

        // Peruuta ei tarvitse tehdä mitään
        builder.setNegativeButton(R.string.dialog_cancel,
                (dialogInterface, i) -> {
                });

        //builder.show(), jolloin dialogi piirretään käyttäjän nähtäväksi
        builder.show();

    }

    //Tallenna piirros
    public void saveFile (View view){
        piirtoKangas.saveBitmap();
    }

    //Tulosta piirros
    public void printFile (View view){
        //PrintHelper-luokan oliolla voimme tulostaa suoraan DrawingCanvas-oliolta kysymämme bittikartan
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = piirtoKangas.getBitmapFromCanvas();
        photoPrinter.printBitmap("Piirros - print", bitmap);
    }

    //Pyyhitään viimeksi piiretty viiva
    public void peruuta (View view){
        piirtoKangas.peruutaPiirto();
    }

}