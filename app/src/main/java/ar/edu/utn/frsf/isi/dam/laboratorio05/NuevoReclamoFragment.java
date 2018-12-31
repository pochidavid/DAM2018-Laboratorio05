package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.FileProvider.getUriForFile;

public class NuevoReclamoFragment extends Fragment {

    private static final int REQUEST_IMAGE_SAVE = 2;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private static final int RECORD_AUDIO = 4;

    public interface OnNuevoLugarListener {
        public void obtenerCoordenadas();
    }

    public void setListener(OnNuevoLugarListener listener) {
        this.listener = listener;
    }

    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;

    private EditText reclamoDesc;
    private EditText mail;
    private Spinner tipoReclamo;
    private TextView tvCoord;
    private Button buscarCoord;
    private Button btnGuardar;
    private Button btnGrabar;
    private Button btnReproducir;
    private OnNuevoLugarListener listener;
    private Button btnSacarFoto;
    private ImageView foto;
    private String pathFoto;
    private String pathAudio;
    private MediaRecorder mRecorder;
    private MediaPlayer mediaPlayer;
    private Boolean grabando = false;
    private Boolean reproduciendo = false;
    private Boolean edicionActivada;

    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;
    public NuevoReclamoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View v = inflater.inflate(R.layout.fragment_nuevo_reclamo, container, false);

        reclamoDesc = (EditText) v.findViewById(R.id.reclamo_desc);
        mail= (EditText) v.findViewById(R.id.reclamo_mail);
        tipoReclamo= (Spinner) v.findViewById(R.id.reclamo_tipo);
        tvCoord= (TextView) v.findViewById(R.id.reclamo_coord);
        buscarCoord= (Button) v.findViewById(R.id.btnBuscarCoordenadas);
        btnGuardar= (Button) v.findViewById(R.id.btnGuardar);
        btnSacarFoto = v.findViewById(R.id.btn_foto_reclamo);
        foto = v.findViewById(R.id.iv_foto_reclamo);
        btnGrabar = v.findViewById(R.id.btn_grabar_audio);
        btnReproducir = v.findViewById(R.id.btn_reproducir_audio);

        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(),android.R.layout.simple_spinner_item,Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);

        btnGuardar.setEnabled(false);
        int idReclamo =0;
        if(getArguments()!=null)  {
            idReclamo = getArguments().getInt("idReclamo",0);
        }

        cargarReclamo(idReclamo);


        edicionActivada = !tvCoord.getText().toString().equals("0;0");
        reclamoDesc.setEnabled(edicionActivada );
        mail.setEnabled(edicionActivada );
        tipoReclamo.setEnabled(edicionActivada);


        buscarCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.obtenerCoordenadas();

            }
        });

        reclamoDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activarBotonGuardar();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnSacarFoto.setOnClickListener(new View.OnClickListener() { @Override
        public void onClick(View v) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            } else sacarGuardarFoto();
            }
        });

        btnGrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //termina grabacion
                if(grabando){
                    terminarGrabar();
                    grabando = false;
                    btnGrabar.setText("Grabar Audio");
                    btnReproducir.setVisibility(View.VISIBLE);

                }else{
                    //Graba
                    if(permisoAudio()){
                        grabarAudio();
                        grabando = true;
                        btnGrabar.setText("Terminar");
                    }
                }
            }
        });

        btnReproducir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Parar reproduccion
                if(reproduciendo){
                    pararReproduccion();
                    reproduciendo = false;
                    btnReproducir.setText("Reproducir");
                }else{
                    //Reproducir
                    reproducirAudio();
                    reproduciendo = true;
                    btnReproducir.setText("Parar");
                }
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrUpdateReclamo();
            }
        });
        return v;
    }

    private void cargarReclamo(final int id){
        if( id >0){
            Runnable hiloCargaDatos = new Runnable() {
                @Override
                public void run() {
                    reclamoActual = reclamoDao.getById(id);

                    // Make the thread wait half a second. If you want...
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Toast.makeText(getActivity().getApplicationContext(), "Default Signature Fail", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    // here you check the value of getActivity() and break up if needed
                    if(getActivity() == null)
                        return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mail.setText(reclamoActual.getEmail());
                            tvCoord.setText(reclamoActual.getLatitud()+";"+reclamoActual.getLongitud());
                            reclamoDesc.setText(reclamoActual.getReclamo());
                            Reclamo.TipoReclamo[] tipos= Reclamo.TipoReclamo.values();
                            for(int i=0;i<tipos.length;i++) {
                                if(tipos[i].equals(reclamoActual.getTipo())) {
                                    tipoReclamo.setSelection(i);
                                    break;
                                }
                            }
                            if(reclamoActual.getPath_foto() != null){
                                pathFoto = reclamoActual.getPath_foto();
                                cargarImagen();
                            }
                            if(reclamoActual.getPath_audio() != null){
                                pathAudio = reclamoActual.getPath_audio();
                                btnReproducir.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            };
            Thread t1 = new Thread(hiloCargaDatos);
            t1.start();
        }else{
            String coordenadas = "0;0";
            if(getArguments()!=null) coordenadas = getArguments().getString("latLng","0;0");
            tvCoord.setText(coordenadas);
            reclamoActual = new Reclamo();
        }

    }

    private void saveOrUpdateReclamo(){
        reclamoActual.setEmail(mail.getText().toString());
        reclamoActual.setReclamo(reclamoDesc.getText().toString());
        reclamoActual.setTipo(tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition()));
        if(tvCoord.getText().toString().length()>0 && tvCoord.getText().toString().contains(";")) {
            String[] coordenadas = tvCoord.getText().toString().split(";");
            reclamoActual.setLatitud(Double.valueOf(coordenadas[0]));
            reclamoActual.setLongitud(Double.valueOf(coordenadas[1]));
        }
        if(pathFoto != null) reclamoActual.setPath_foto(pathFoto);
        if(pathAudio != null) reclamoActual.setPath_audio(pathAudio);

        Runnable hiloActualizacion = new Runnable() {
            @Override
            public void run() {

                if(reclamoActual.getId()>0) reclamoDao.update(reclamoActual);
                else reclamoDao.insert(reclamoActual);

                // Make the thread wait half a second. If you want...
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "Default Signature Fail", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                // here you check the value of getActivity() and break up if needed
                if(getActivity() == null)
                    return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // limpiar vista
                        mail.setText(R.string.texto_vacio);
                        tvCoord.setText(R.string.texto_vacio);
                        reclamoDesc.setText(R.string.texto_vacio);
                        foto.setVisibility(View.GONE);
                        pathFoto = null;
                        btnReproducir.setVisibility(View.GONE);
                        pathAudio = null;
                        btnGuardar.setEnabled(false);
                        getActivity().getFragmentManager().popBackStack();
                    }
                });
            }
        };
        Thread t1 = new Thread(hiloActualizacion);
        t1.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) Objects.requireNonNull(extras).get("data");
            foto.setImageBitmap(imageBitmap);
            foto.setVisibility(View.VISIBLE);
        }
        if (requestCode == REQUEST_IMAGE_SAVE && resultCode == RESULT_OK) {
            cargarImagen();
            activarBotonGuardar();
        }
    }

    private void cargarImagen() {
        File file = new File(pathFoto);
        Bitmap imageBitmap = null;
        try {
            imageBitmap = MediaStore.Images.Media
                    .getBitmap(getContentResolver(),
                            Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(),"Imagen no encontrada",Toast.LENGTH_SHORT);
        }
        if (imageBitmap != null) {
            foto.setImageBitmap(imageBitmap);
            foto.setVisibility(View.VISIBLE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File dir = new File(getContext().getFilesDir(),"images");

        if (!dir.exists())
            dir.mkdirs();

        File image =  new File(dir,imageFileName+".jpg");

        pathFoto = image.getAbsolutePath();
        return image;
    }

    private File createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audioFileName = "AUDIO_" + timeStamp + "_";
        File dir = new File(getContext().getFilesDir(),"audios");

        if (!dir.exists())
            dir.mkdirs();

        File audio =  new File(dir,audioFileName+".3gp");

        pathAudio = audio.getAbsolutePath();
        return audio;
    }

    private void sacarGuardarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) { ex.printStackTrace();}
            if (photoFile != null) {
                    Uri photoURI = getUriForFile(getContext(), "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_SAVE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void grabarAudio(){
        mRecorder = new MediaRecorder();

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        try {
            createAudioFile();
            mRecorder.setOutputFile(pathAudio);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) { Log.e("AUDIO", "prepare() failed"); }
        mRecorder.start();
    }

    private void terminarGrabar() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        activarBotonGuardar();
    }
    private void sacarFoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager pm = NuevoReclamoFragment.this.getContext().getPackageManager();
        if (takePictureIntent.resolveActivity(pm) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private PackageManager getPackageManager() {
        return this.getContext().getPackageManager();
    }

    private ContentResolver getContentResolver(){
        return getActivity().getApplicationContext().getContentResolver();
    }

    private Boolean permisoAudio(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO);
            return false;

        } else {
            return true;
        }
    }

    private void reproducirAudio(){
        String myUri = pathAudio; // initialize Uri here
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(myUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Toast.makeText(getContext(),"Audio no encontrado",Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    private void pararReproduccion(){
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void activarBotonGuardar(){
        Reclamo.TipoReclamo tipo = (Reclamo.TipoReclamo) tipoReclamo.getSelectedItem();

        //Si el reclamo es por calle en mal estado o verdeda
        if(tipo == Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO
                || tipo == Reclamo.TipoReclamo.VEREDAS){

            //Tiene que ingresar foto
            if(pathFoto != null) btnGuardar.setEnabled(edicionActivada);

            //Sino la descripcion tiene que ser mayor a 8 o tiene que tener audio
        }else if(reclamoDesc.getText().length()>=8
                    || pathAudio != null)

                btnGuardar.setEnabled(edicionActivada);

        else btnGuardar.setEnabled(false);
        }

}
