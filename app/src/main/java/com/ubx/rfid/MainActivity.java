package com.ubx.rfid;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.ubx.rfid.p006ui.about.AboutFragment;
import com.ubx.rfid.p006ui.inventary.InventaryFragment;
import com.ubx.rfid.p006ui.scan.ScanFragment;
import com.ubx.rfid.p006ui.settings.SettingsFragment;
import com.ubx.rfid.p006ui.sync.SyncFragment;
import com.ubx.rfid.util.sharedPreference.PreKey;
import com.ubx.rfid.util.sharedPreference.SPUtils;
import com.ubx.usdk.rfid.RfidManager;

/**
 * Activity principal de la app RFID Urovo DT50.
 *
 * Responsabilidades:
 * - Toolbar con ícono de hamburguesa que abre el DrawerLayout
 * - Menú lateral: RFID Ingreso | Acerca de
 * - Conectar al servicio RFID del sistema (com.ubx.usdk.rfid)
 * - Capturar el trigger físico (keyCode 523) y delegarlo al ScanFragment
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    /** KeyCode del trigger físico del Urovo DT50 */
    private static final int KEYCODE_TRIGGER = 523;

    private DrawerLayout drawerLayout;
    private RfidManager mRfidManager;
    private MainViewModel mainViewModel;
    private long lastBackPress = 0;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.menu_rfid_ingreso);
        }

        // DrawerLayout + toggle (ícono hamburguesa)
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Marcar "RFID Ingreso" como seleccionado por defecto
        navigationView.setCheckedItem(R.id.nav_scan);

        // Inicializar y conectar el RfidManager
        initRfidManager();

        // Cargar el ScanFragment como pantalla principal
        if (savedInstanceState == null) {
            navigateTo(new ScanFragment(), getString(R.string.menu_rfid_ingreso));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRfidManager != null) {
            mRfidManager.release();
        }
    }

    // -------------------------------------------------------------------------
    // Menú lateral
    // -------------------------------------------------------------------------

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            navigateTo(new ScanFragment(),     getString(R.string.menu_rfid_ingreso));
        } else if (id == R.id.nav_sync) {
            navigateTo(new SyncFragment(),     getString(R.string.menu_sync));
        } else if (id == R.id.nav_inventary) {
            navigateTo(new InventaryFragment(), getString(R.string.menu_inventary));
        } else if (id == R.id.nav_settings) {
            navigateTo(new SettingsFragment(), getString(R.string.menu_settings));
        } else if (id == R.id.nav_about) {
            navigateTo(new AboutFragment(),    getString(R.string.menu_about));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /** Reemplaza el Fragment actual y actualiza el título de la Toolbar. */
    private void navigateTo(Fragment fragment, String title) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    // -------------------------------------------------------------------------
    // RFID Manager
    // -------------------------------------------------------------------------

    private void initRfidManager() {
        mRfidManager = new RfidManager(this);
        mRfidManager.bindService(new RfidManager.ConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "Servicio RFID conectado");
                connectReader();
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "Servicio RFID desconectado");
                mainViewModel.setConnected(false);
            }
        });
    }

    private void connectReader() {
        if (mRfidManager.isConnected()) {
            byte id = mRfidManager.getReadId();
            mainViewModel.setReadId(id);
            mainViewModel.setConnected(true);
            Log.d(TAG, "Lector ya conectado, readId=" + (id & 0xFF));
        } else {
            String port     = SPUtils.getInstance().getString(PreKey.SERIAL.name(), "/dev/ttyHSL0");
            int    baudrate = SPUtils.getInstance().getInt(PreKey.SERIAL_PORT.name(), 115200);
            boolean ok = mRfidManager.connectCom(port, baudrate);
            Log.d(TAG, "connectCom(" + port + ", " + baudrate + ") = " + ok);
            if (ok) {
                byte id = mRfidManager.getReadId();
                mainViewModel.setReadId(id);
                mainViewModel.setConnected(true);
                Log.d(TAG, "Lector conectado, readId=" + (id & 0xFF));
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, R.string.rfid_connect_failed, Toast.LENGTH_LONG).show()
                );
            }
        }
    }

    // -------------------------------------------------------------------------
    // Trigger físico
    // -------------------------------------------------------------------------

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KEYCODE_TRIGGER
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            Log.d(TAG, "Trigger físico presionado");
            ScanFragment scanFragment = getScanFragment();
            if (scanFragment != null) {
                scanFragment.onTriggerKey();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    // -------------------------------------------------------------------------
    // Back press
    // -------------------------------------------------------------------------

    @Override
    public void onBackPressed() {
        // Si el drawer está abierto, cerrarlo primero
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        // Doble toque para salir
        long now = System.currentTimeMillis();
        if (now - lastBackPress > 2000) {
            Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show();
            lastBackPress = now;
        } else {
            finish();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Expone el RfidManager a los Fragments. */
    public RfidManager getRfidManager() {
        return mRfidManager;
    }

    private ScanFragment getScanFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        return (f instanceof ScanFragment) ? (ScanFragment) f : null;
    }
}
