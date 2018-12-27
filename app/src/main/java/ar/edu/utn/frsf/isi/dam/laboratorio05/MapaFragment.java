package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap miMapa;
    private int tipoMapa;
    private OnMapaListener listener;

    private boolean FLAG_NUEVO_LUGAR;


    public interface OnMapaListener{
        public void coordenadasSeleccionadas(LatLng c);
    }



    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;

    private EditText reclamoDesc;
    private EditText mail;
    private Spinner tipoReclamo;
    private TextView tvCoord;
    private Button buscarCoord;
    private Button btnGuardar;

    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;

    public MapaFragment() {
        // Required empty public constructor
    }

    public void setListener(OnMapaListener listener) {
        this.listener = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        FLAG_NUEVO_LUGAR = false;

        Integer tipoMapa = 0;
        Bundle argumentos = getArguments();
        if (argumentos != null) {
            tipoMapa = argumentos.getInt("tipo_mapa", 0);
        }
        getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        miMapa = map;
        actualizarMapa();

        miMapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                listener.coordenadasSeleccionadas(latLng);
            }
        });




    }

    private void actualizarMapa(){
        if ((ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED))
            if ((ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 9999);
                return;
            }
        miMapa.setMyLocationEnabled(true);
    }


}
