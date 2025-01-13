package com.example.vko4_tleminen;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

public class piirtoKangas extends View {

    //Käyttäjän piirtämät polut tallennetaan luokan sisäiseen listaan.
    private LinkedList<PaintedPath> piirtamatPolut = new LinkedList<>();
    private PaintedPath aktiivinenPolku;

    private int varinValinta = 0;
    private float kynanPaksuus = 0;


    private Paint[] maalit;
    private boolean ympyraisChecked = false;
    private float ympyraX;
    private float ympyraSade;
    private float ympyraY;

    /*DrawingCanvas luokan
    konstruktori luo piirtämist
    varten muutaman värin Paint- luokan olioiksi.
     */
    public piirtoKangas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        maalienAlustus();
    }

    //Vaihtaa kynän paksuutta
    public void setKynanPaksuus(float paksuus){
        System.out.println("paksuus = " + paksuus);
        kynanPaksuus = paksuus;
        System.out.println("Kynanpaksuus: " + kynanPaksuus);

    }

    public void setSade(float sade){
        System.out.println("sade = " + sade);
        ympyraSade = sade;
        System.out.println("ympyransade = " + ympyraSade);

    }

    //Vaihtaa kynän ympyrä muodoksi
    public void setKynaYmpyraksi(boolean kynaYmpyraksi){
        ympyraisChecked = kynaYmpyraksi;
    }


    private void maalienAlustus() {
        int[] colorCodes = new int[]{Color.BLACK, Color.WHITE,
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                Color.DKGRAY, Color.CYAN, Color.MAGENTA, Color.LTGRAY};

        //Käytettävät ”maalit” tallennetaan luokan sisäiseen yksityiseen taulukkoon paints.
        maalit = new Paint[colorCodes.length];

        for (int i = 0; i < colorCodes.length; i++){
            int colorCode = colorCodes[i];

            // Luodaan uusi Paint-olio, jota käytetään piirtämiseen näytölle.
            Paint p = new Paint();
            // Asetetaan värin arvo colorCode Paint-oliolle. colorCode on muuttuja,
            // joka sisältää halutun värin arvon.
            p.setColor(colorCode);
            // Asetetaan AntiAlias-ominaisuus päälle.
            // Tämä tekee piirretystä viivasta tai muodosta sulavamman ja vähemmän portaikkomaista.
            p.setAntiAlias(true);
            // Asetetaan viivan alku paksuus. Tämä määrittää, kuinka paksu viiva on piirrettäessä.
            p.setStrokeWidth(kynanPaksuus);
            // Asetetaan piirrettävien muotojen tyyli. Tässä tapauksessa se on Paint.Style.STROKE,
            // mikä tarkoittaa, että viivoja piirretään vain niiden ääriviivoina ilman täyttöä.
            p.setStyle(Paint.Style.STROKE);
            // Määrittää, kuinka viivojen liittymiskohdat näkyvät.
            // Paint.Join.MITER tarkoittaa, että liittymiskohdat ovat teräviä.
            p.setStrokeJoin(Paint.Join.MITER);

            //tallennetaan luodun Paint-olion p taulukkoon maalit
            maalit[i] = p;
        }

    }

    //getPaints, palauttaa käytössä olevat maalit taulukkona
    public Paint[] getMaalit() {
        return maalit;
    }

    //setActivePaintIndex asettaa selectedPaint muuttujan arvon (vaihtaa käytössä olevaa maalia)
    public void setActivePaintIndex(int paintNumber) {
        varinValinta = paintNumber;
    }





    /*
    Aina kun tämä näkymä (DrawingCanvas) piirretään uudelleen, Android järjestelmä kutsuu sen onDraw- metodia
    ja tarjoaa argumenttina Canvas-luokan olion targetCanvas
     */
    @Override
    protected void onDraw(Canvas targetCanvas) {
        /*
        Tämä olio on siis meidän ”kosketuspintamme” siihen käyttöliittymän piirrettyyn osaan,
        johon komponenttimme piirretään.
         */
        super.onDraw(targetCanvas);

        for (PaintedPath p : piirtamatPolut){
            /*
            Metodi drawPath. saa argumenttinaan polun muodon (PaintedPath-olion path-muuttuja) ja
            sen maalin (paint) jolla muoto halutaan piirtää.
             */
            targetCanvas.drawPath(p.path,p.paint);
        }


    }

    /*
    clearCanvas-metodi, joka uudelleenalustaa Paths-polkulistan,
    jolloin se tyhjenee ja merkistee komponentin vanhentuneeksi.
    Nyt Android järjestelmä piirtää komponenttimme uudelleen,
    ja koska tyhjensimme juuri polkulistan, ei näytölle piirretä mitään.
     */
    public void ClearCanvas(){
        piirtamatPolut = new LinkedList<PaintedPath>();
        //voimme merkitä näkymämme ”vanhentuneeksi” View luokan invalidate()- metodikutsulla.
        //Tällöin järjestelmä piirtää komponenttimme uudelleen, kutsuen sen onDraw-metodia.
        invalidate();
    }

    //Uusimman piirretyn viivan pyyhkiminen
    public void peruutaPiirto(){
        piirtamatPolut.removeLast();
        invalidate();
    }

    /*
    Varsinainen ”piirtäminen” tapahtuu onTouchEvent-metodissa. Tähän metodiin
    päädytään aina, kun View-luokasta laajennettu komponenttimme alueella tapahtuu kosketus.
     */
    public boolean onTouchEvent(MotionEvent event){
        //Metodin argumentti MotionEvent luokan
        //event-olio tarjoaa meille kaiken tarvittavan tiedon kosketuksesta.

        //napataan MotionEvent-oliolta tieto siitä, mihin näkymämme xy-koordinaatistolla
        //kosketettiin. Koordinaatti x:0 y:0 vastaa näkymämme vasenta yläkulmaa.
        float x = event.getX();
        float y = event.getY();

        //Tutkitaan tapahtumasta, minkälainen kosketus (action) on kyseessä.
        switch (event.getAction()) {

            //kun käyttäjän sormi koskettaa näyttöä ensimmäisen kerran
            case MotionEvent.ACTION_DOWN:
                // Luo uusi Paint-olio, jolla on haluttu paksuus
                Paint uusiPaint = new Paint(maalit[varinValinta]); // Kopioi nykyisen maalin ominaisuudet
                uusiPaint.setStrokeWidth(kynanPaksuus); // Asetetaan uusi paksuus
                //Luodaan uusi PaintedPath-olio tämänhetkisellä valitulla maalilla
                aktiivinenPolku = new PaintedPath(uusiPaint);

                if (ympyraisChecked) { // Jos ympyrätyökalu on valittuna
                    ympyraX = x; // Tallennetaan keskipisteen x-koordinaatti
                    ympyraY = y; // Tallennetaan keskipisteen y-koordinaatti
                    aktiivinenPolku.paint.setStyle(Paint.Style.FILL);
                    // Piirretään ympyrä
                    aktiivinenPolku.path.addCircle(ympyraX, ympyraY, ympyraSade, Path.Direction.CW);

                    
                }else {
                    //Siirretään polun juuri (lähtöpiste) saamaamme xy-koordinaattiin.
                    aktiivinenPolku.path.moveTo(x, y);
                }
                //Lisätään polku piirrettäviin polkuihin
                piirtamatPolut.add(aktiivinenPolku);

                break;

            //kun käyttäjä raahaa sormeaan näytöllä
            case MotionEvent.ACTION_MOVE:
                //Mikäli ympyrä checkbox ei ole valittuna voi piirtää polun
                if(!ympyraisChecked) {
                    //lisäämme polkuumme viivan polun nykyisestä hännästä saamaamme xy-koordinaattiin
                    aktiivinenPolku.path.lineTo(x, y);
                }
                break;
        }

        //merkitään komponenttimme vanhentuneeksi, jotta järjestelmä piirtää sen uudelleen.
        invalidate();
        return true;
    }

    // Bitmap-luokka tarjoaa varsin yksinkertaisen tavan alustaa Bittikartta-olioita
    private Bitmap createBitmap() {
        /*
        Napataan komponentiltamme sen leveys ja korkeus ja määritellään bittikartan asetukset.
        ARGB_8888 tarkoittaa siis sitä, että jokainen pikseli tallennetaan neljään tavuun (8 bittiä per tavu):
         */
        Bitmap bitmap = Bitmap.createBitmap(
                getWidth(),
                getHeight(),
                Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    //Jotta voimme tallentaa tai tulostaa piirroksemme, meidän täytyy muuntaa se bittikartaksi.

    public Bitmap getBitmapFromCanvas(){
        //Luodaan uusi canvas olio
        Canvas canvas = new Canvas();
        //Luodaan uusi bitmap olio
        Bitmap bitmap = createBitmap();

        canvas.setBitmap(bitmap);
        //piirretään valkoinen tausta
        canvas.drawColor(Color.WHITE);

        /*
        Kutsumme tälle kankaalle draw- metodia, jolloin komponenttimme näkymä piirroksineen
        piirretään tälle uudelle kankaalle, jolloin se tallentuu myös sen bittikartalle
         */
        draw(canvas);

        return bitmap;
    }

    //Piirrosten tallentamiseen käytettävä metodi
    public void saveBitmap(){
        //Annetaan metatietoja Jotta tallentamamme tiedosto näkyy käyttöjärjestelmässä oikein
        //Tiedoston luontiaijan voimme tallentaa SimpleDateFormat- luokkaa käyttäen.
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        //Luokan avulla luomme merkkijonon nykyisestä järjestelmän ajasta (System.currentTimeMillis)
        String dateTime = dateFormat.format(System.currentTimeMillis());

        //Metatiedot asetetaan käyttämällä ContentValues-luokkaa.
        ContentValues values = new ContentValues();
        //Käytämme tätä päivämäärä ja kellonaika-merkkijonoa sekä tiedoston nimeämiseen,
        //että sen lisäyspäivämääräksi.
        values.put(MediaStore.Images.Media.DISC_NUMBER, "piirros_"+dateTime+".png");
        //MediaStore mahdollistaa meille pääsyn audio, video ja kuvatiedostoihin, tarjoamalla
        //indeksoidun ”katalogin” kaikista järjestelmän tallennustilojen medioista.
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        //Lisäksi määrittelemme tiedoston tyypin, verkkokoodauksesta tutulla MIME-tyypillä ”image/png”
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");

        //haetaan metodista bitmap
        Bitmap bitmap = getBitmapFromCanvas();

        Uri url = null;

        //Jotta järjestelmä osaa näyttää käyttäjälle tallentamamme kuvan, meidän täytyy
        //napata viittaus sovelluksemme ContentResolver-olioon cr
        ContentResolver cr = getContext().getContentResolver();

        try {
            /*
            Lisäämme tätä oliota käyttäen MediaStoren EXTERNAL_CONTENT_URI- taulukkoon aikaisemmin luomamme
            metatietopaketin ja saamme palautuksena tiedostojärjestelmän osoitteen url,
            johon tämä uusi rivi viittaa.
             */

            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            //pyydetään cr-oliolta tallennusvirran saamaamme osoitteeseen
            //url, johon voimme kirjoittaa bittikarttamme.
            OutputStream imageOut = cr.openOutputStream(url);

            //Bitmap-oliomme bitmap .compress-metodi pakkaa olion antamaamme bittivirtaan
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOut);

            /*
            • Lopuksi huuhdomme ja suljemme bittivirran, jolloin siihen kirjoittamamme
            tavara tallentuu järjestelmän pysyvään muistiin
             */
            imageOut.flush();
            imageOut.close();

            //Toast-ilmoituksilla voimme kertoa käyttäjälle nopeasti, onnistuiko tiedoston
            //tallennus vai ei
            Toast.makeText(getContext(), "Tallennettu kuva nimellä "+
                    values.get(MediaStore.Images.Media.DISPLAY_NAME),Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            //jos tiedoston kirjoitus epäonnistuu (IOException),
            //meidän tulee puhdistaa ContentResolver-oliolla luomamme rivi.
            if(url != null) {
                cr.delete(url, null, null);
            }
            url = null;

            //Toast-ilmoituksilla voimme kertoa käyttäjälle nopeasti, onnistuiko tiedoston
            //tallennus vai ei
            //Argumentit: Sovelluskonteksti, Näytettävän teksti & Ilmoituksen näyttöaika
            Toast.makeText(getContext(),"Tallennus epäonnistui",Toast.LENGTH_SHORT).show();
        }


    }


    /* PaintedPath-apuluokkaa
        käytetään yhdistämässä Path- olioita Paint-olioihin.
         */
    private class PaintedPath {
        public Path path;
        public Paint paint;

        public PaintedPath(Paint paint){
            this.path = new Path();
            this.paint = paint;
        }
    }
}
