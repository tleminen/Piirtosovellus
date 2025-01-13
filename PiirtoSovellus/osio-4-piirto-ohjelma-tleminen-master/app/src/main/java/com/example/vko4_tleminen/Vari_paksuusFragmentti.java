package com.example.vko4_tleminen;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

public class Vari_paksuusFragmentti extends Fragment {

    LinearLayout paintContainer;
    HorizontalScrollView paintSelector;
    SeekBar kynanPaksuus;
    CheckBox ympyraMuoto;

    Slider sadeSlider;

    TextView paksuusTeksti;
    public Vari_paksuusFragmentti() {
        super(R.layout.fragment_vari_paksuus_fragmentti);
    }



    //onCreateView-metodi, jota kutsutaan kun Fragmentin sisältöä piirretään näkyviin.
    @SuppressLint("MissingInflatedId")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vari_paksuus_fragmentti, container, false);

        paintContainer = view.findViewById(R.id.paintContainer);
        paintSelector = view.findViewById(R.id.paintSelector);
        paksuusTeksti = view.findViewById(R.id.tvPaksuusTeksti);
        kynanPaksuus = view.findViewById(R.id.seekBarPenWidth);
        ympyraMuoto = view.findViewById(R.id.checkBoxYmpyra);
        sadeSlider = view.findViewById(R.id.slider_ympyranSade);

        sadeSlider.addOnChangeListener((new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                //Jos käyttäjän tekemä niin kutsutaan metodia
                if (fromUser) {
                    sateenKoko();
                }
            }
        }));

        ympyraMuoto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            kynaYmpyraksi();
        });

        kynanPaksuus.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                paivitaSeekbar();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        //Napataan viittaus MainActivity-luokkaan, jotta pääsemme käsiksi DrawingCanvas-luokan
        //olioon, eli piirustusalueeseen
        MainActivity main = (MainActivity) getActivity();
        InitializePaintSelection(main.getActiveCanvas());

        return view;
    }

    public void InitializePaintSelection(piirtoKangas canvas){
        /*
        Aluksi haetaan piirtoKangas- luokan oliolta kaikki käytössä olevat maalit ja
        tallennetaan ne taulukkoon
         */
        Paint[] paints = canvas.getMaalit();

        //Luodaan jokaiselle värille oma nappi
        for (int i = 0; i < paints.length; i++){
            Paint p = paints[i];
            Button B = new Button(getContext());
            B.setBackgroundColor(p.getColor());
            //Huomaa, että jokainen nappi saa Tagikseen kierrosnumeron, eli indeksin,
            //jolla maali-taulukosta löydetään nappia vastaava maali
            B.setTag(i);
            B.setTextSize(28f);
            B.setOnClickListener(view -> {
                SelectPaint((int) view.getTag());
            });

            // contrast check, no dark font on dark background etc
            double contrast = ColorUtils.calculateContrast(p.getColor(), Color.BLACK);
            if (contrast < 5f) B.setTextColor(Color.WHITE);

            paintContainer.addView(B);
        }
    }

    private void sateenKoko(){
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getActiveCanvas().setSade(sadeSlider.getValue());
    }


    private void kynaYmpyraksi(){
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getActiveCanvas().setKynaYmpyraksi(ympyraMuoto.isChecked());
    }

    //Päivitetään seekbar tekstiä, joka määrittää kynän paksuuden
    private void paivitaSeekbar(){
        String Muuttuvateksti = getString(R.string.paksuus);
        Muuttuvateksti += " "+kynanPaksuus.getProgress();
        paksuusTeksti.setText(Muuttuvateksti);

        //Muutetaan kynän paksuus
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getActiveCanvas().setKynanPaksuus(kynanPaksuus.getProgress());
    }
    private void SelectPaint(int paintNumber) {

        //Kuuntelijassa napataan viittaus piirtoalueeseen, jotta voimme asettaa
        //käytössä olevan maalinumeron oikeaksi.
        MainActivity mainActivity = (MainActivity) getActivity();
        //Kutsutaan mainActivity luokan metodia setActivePaintIndex, joka vaihtaa väriä
        mainActivity.getActiveCanvas().setActivePaintIndex(paintNumber);

        // reset buttons
        for (int i = 0; i < paintContainer.getChildCount(); i++) {
            Button b = (Button) paintContainer.getChildAt(i);
            //b.setText(i == paintNumber ? "✓" : "");
            if(i == paintNumber) {
                b.setText("✓");
            }
            else {
                b.setText("");
            }
        }
    }
}