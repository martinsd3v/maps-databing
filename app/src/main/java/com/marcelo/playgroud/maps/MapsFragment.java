package com.marcelo.playgroud.maps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class MapsFragment extends Fragment {

    private Runnable EnderecoRunnable;
    private Handler EnderecoHandler;
    private LatLng LocalAtual;
    private boolean obtendoEndereco = false;
    private ViewMapsActivity activityViewMaps;

    //Setando padrões do mapa
    private GoogleMap mMap;
    private float zoom = 16;
    private float maxZoom = 16;
    private float bearing = 0;
    private float tilt = 10;
    private int loadMapTime = 1500;

    private boolean permissoesLocal = false;
    private boolean loadLocal = false;

    String[] permissoes = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_maps, container, false);
        activityViewMaps = (ViewMapsActivity) getActivity();
        permissoesLocal = PermissionUtils.validate(getActivity(), 0, permissoes);
        initMaps();
        System.out.println("Console: Iniciou");
        return mView;
    }

    @SuppressWarnings("ResourceType")
    private void initMaps() {
        //Carregando fragmento do mapa
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.map);

        ((SupportMapFragment) fragment).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                //Setando configuraçoes do mapa
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setPadding(0, 16, 0, 0);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(true);

                if (permissoesLocal) {
                    mMap.setMyLocationEnabled(true);
                }

                //Criando tarefa para execultar o reverse endereço
                EnderecoHandler = new Handler();
                EnderecoRunnable = new Runnable() {
                    @Override
                    public void run() {
                        obtendoEndereco = true;
                        LocalAtual = mMap.getCameraPosition().target;
                        if (LocalAtual != null) getEndereco();
                    }
                };

                //Capturando o evento de alteração da camera
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        if (!obtendoEndereco) {
                            //Chamando metodo da Activity
                            activityViewMaps.alteraEndereco("Buscando Endereço...");
                        }

                        //Se já existir um processo em execução reiniar o mesmo
                        if (EnderecoRunnable != null) {
                            obtendoEndereco = false;
                            EnderecoHandler.removeCallbacks(EnderecoRunnable);
                        }
                        EnderecoHandler.postDelayed(EnderecoRunnable, loadMapTime);
                    }
                });

                //Capiturando mudança de localização atual
                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        if (!loadLocal) {
                            LocalAtual = new LatLng(location.getLatitude(), location.getLongitude());
                            if (LocalAtual != null) atualizaCamera();
                            loadLocal = true;
                        }
                    }
                });
            }
        });
    }

    //Atualiza mapa com animação
    private void atualizaCamera() {
        //Persistindo padrões do mapa
        zoom = mMap.getCameraPosition().zoom;
        bearing = mMap.getCameraPosition().bearing;
        tilt = mMap.getCameraPosition().tilt;

        //Setando zoom maximo
        if (zoom < maxZoom) zoom = maxZoom;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(LocalAtual)         // localização
                .zoom(zoom)                 // Zoom
                .bearing(bearing)           // Rotação
                .tilt(tilt)                 // Inclinação
                .build();                   // Constroi localização
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //Metodo responsavel  por obter endereço válido
    private void getEndereco() {
        Geocoder geocoder = new Geocoder(activityViewMaps);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(LocalAtual.latitude, LocalAtual.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            final Address address = addresses.get(0);

            LocalAtual = new LatLng(address.getLatitude(), address.getLongitude());
            atualizaCamera();

            //Montando String com endereço completo
            String mEndereco = "";
            String mRua = address.getThoroughfare();
            String mNumero = address.getFeatureName();
            String mComple = address.getSubLocality();

            if (mRua == null) mRua = "";
            if (mNumero == null) mNumero = "";
            if (mComple == null) mComple = "";

            if (!mRua.isEmpty()) {
                mEndereco += mRua;
            }

            if (!mNumero.isEmpty()) {
                if (!mEndereco.isEmpty()) mEndereco += ", ";
                mEndereco += mNumero;
            }

            if (!mComple.isEmpty()) {
                if (!mEndereco.isEmpty()) mEndereco += ", ";
                mEndereco += mComple;
            }

            if (!mEndereco.isEmpty()) {
                activityViewMaps.alteraEndereco(mEndereco);
            }
        }
    }
}