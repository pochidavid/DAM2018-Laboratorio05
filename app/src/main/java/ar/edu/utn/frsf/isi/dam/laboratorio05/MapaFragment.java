package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap miMapa;
    private int tipoMapa = 0;
    private OnMapaListener listener;


    public interface OnMapaListener{
        public void coordenadasSeleccionadas(LatLng c);
    }



    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;
    private List<Reclamo> listaReclamos;
    private Reclamo rSelec;

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

        Bundle argumentos = getArguments();

        if (argumentos != null) {
            this.tipoMapa = argumentos.getInt("tipo_mapa", 0);
        }
        getMapAsync(this);

        //cargar datos

            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    listaReclamos = MyDatabase.getInstance(getActivity()).getReclamoDao().getAll();
                }
            });
            t1.start();


        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        miMapa = map;
        actualizarMapa();


        miMapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    if(MapaFragment.this.tipoMapa==1)
                    listener.coordenadasSeleccionadas(latLng);
                }
            });

        if(this.tipoMapa==2){
            if(listaReclamos.size()!=0) {
                List<LatLng> c = new ArrayList<LatLng>();
                LatLngBounds.Builder limites = new LatLngBounds.Builder();
                for (Reclamo r : listaReclamos) {
                    miMapa.addMarker(new MarkerOptions()
                            .position(new LatLng(r.getLatitud(), r.getLongitud()))
                            .title(r.getReclamo())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    c.add(new LatLng(r.getLatitud(), r.getLongitud()));
                    limites.include(new LatLng(r.getLatitud(), r.getLongitud()));
                }
                miMapa.moveCamera(CameraUpdateFactory.newLatLngBounds(limites.build(), 300));
            }
        }
        if(this.tipoMapa==3){
            for(Reclamo r: listaReclamos){
                if(r.getId()==getArguments().getLong("idReclamo")){
                    rSelec=r;
                }
            }

            List<LatLng> c = new ArrayList<LatLng>();

            miMapa.addMarker(new MarkerOptions()
            .position(new LatLng(rSelec.getLatitud(), rSelec.getLongitud()))
            .title(rSelec.getReclamo())
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );

            c.add(new LatLng(rSelec.getLatitud(), rSelec.getLongitud()));

            miMapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(rSelec.getLatitud(), rSelec.getLongitud()),15));

            miMapa.addCircle(new CircleOptions()
                    .center(new LatLng(rSelec.getLatitud(),rSelec.getLongitud()))
                    .radius(500)
                    .strokeColor(Color.RED)
                    .fillColor(0x20FF0000)
                    .strokeWidth(3));
        }
        if(this.tipoMapa==4){
            if(listaReclamos.size()!=0) {
                List<LatLng> c = new ArrayList<LatLng>();
                LatLngBounds.Builder limites = new LatLngBounds.Builder();
                for (Reclamo r : listaReclamos) {
                    miMapa.addMarker(new MarkerOptions()
                            .position(new LatLng(r.getLatitud(), r.getLongitud()))
                            .title(r.getReclamo())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    c.add(new LatLng(r.getLatitud(), r.getLongitud()));
                    limites.include(new LatLng(r.getLatitud(), r.getLongitud()));
                }
                miMapa.moveCamera(CameraUpdateFactory.newLatLngBounds(limites.build(), 300));

                HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(c).build();
                TileOverlay mOverlay = miMapa.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

            }
        }


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
