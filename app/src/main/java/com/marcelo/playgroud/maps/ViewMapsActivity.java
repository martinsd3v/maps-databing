package com.marcelo.playgroud.maps;

import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.marcelo.playgroud.maps.databinding.ViewMapBinding;

public class ViewMapsActivity extends AppCompatActivity {

    private ViewMapBinding binding;
    private boolean permissoesLocal = false;

    String[] permissoes = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setPermissoes();
        binding = DataBindingUtil.setContentView(ViewMapsActivity.this, R.layout.activity_view_maps);
        if (permissoesLocal) fragmentMaps();
    }

    public void fragmentMaps() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MapsFragment mapsFragment = new MapsFragment();
        fragmentTransaction.replace(R.id.recebeFragment, mapsFragment);
        fragmentTransaction.commit();
    }

    public void alteraEndereco(String endereco) {
        if (binding != null && endereco != null) {
            binding.enderecoId.setText(endereco);
        }
    }

    public void setPermissoes() {
        permissoesLocal = PermissionUtils.validate(this, 0, permissoes);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                // Alguma permissão foi negada
                this.setPermissoes();
                return;
            }
        }
        // Se chegou aqui está OK
        finish();
        startActivity(getIntent());
    }
}
