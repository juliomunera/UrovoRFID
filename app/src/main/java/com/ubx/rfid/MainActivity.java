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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
 * Navegación con hide()/show() en lugar de replace():
 * Todos los Fragments se crean una sola vez y se mantienen vivos.
 * Al cambiar de pantalla solo se oculta el actual y se muestra el nuevo,
 * preservando el estado (lista de TAGs, scroll, etc.) sin destruirlos.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    // Tags para el FragmentManager (permiten recuperar instancias tras rotación)
    private static final String TAG_SCAN      = "frag_scan";
    private static final String TAG_SYNC      = "frag_sync";
    private static final String TAG_INVENTARY = "frag_inventary";
    private static final String TAG_SETTINGS  = "frag_settings";
    private static final String TAG_ABOUT     = "frag_about";

    /** KeyCode del trigger físico del Urovo DT50 */
    private static final int KEYCODE_TRIGGER = 523;

    private DrawerLayout  drawerLayout;
    private RfidManager   mRfidManager;
    private MainViewModel mainViewModel;
    private long          lastBackPress = 0;

    // Fragment activo en este momento
    private Fragment activeFragment;

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

        // DrawerLayout + toggle
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_scan);

        // Inicializar RFID
        initRfidManager();

        // Inicializar Fragments (solo la primera vez, no tras rotación)
        if (savedInstanceState == null) {
            initFragments();
        } else {
            // Tras rotación: recuperar las instancias ya existentes del back stack
            restoreFragments();
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
    // Inicialización de Fragments (hide/show)
    // -------------------------------------------------------------------------

    /**
     * Crea todos los Fragments y los agrega al contenedor de una sola vez.
     * Solo ScanFragment queda visible; los demás se ocultan inmediatamente.
     * Esto garantiza que ninguno se destruya al navegar entre ellos.
     */
    private void initFragments() {
        ScanFragment      scanFrag      = new ScanFragment();
        SyncFragment      syncFrag      = new SyncFragment();
        InventaryFragment inventaryFrag = new InventaryFragment();
        SettingsFragment  settingsFrag  = new SettingsFragment();
        AboutFragment     aboutFrag     = new AboutFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, scanFrag,      TAG_SCAN);
        ft.add(R.id.fragment_container, syncFrag,      TAG_SYNC);
        ft.add(R.id.fragment_container, inventaryFrag, TAG_INVENTARY);
        ft.add(R.id.fragment_container, settingsFrag,  TAG_SETTINGS);
        ft.add(R.id.fragment_container, aboutFrag,     TAG_ABOUT);

        // Ocultar todos excepto Scan
        ft.hide(syncFrag);
        ft.hide(inventaryFrag);
        ft.hide(settingsFrag);
        ft.hide(aboutFrag);
        ft.commit();

        activeFragment = scanFrag;
    }

    /**
     * Tras una rotación de pantalla, el FragmentManager ya tiene las instancias.
     * Las recuperamos por tag y determinamos cuál estaba visible.
     */
    private void restoreFragments() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment scan      = fm.findFragmentByTag(TAG_SCAN);
        Fragment sync      = fm.findFragmentByTag(TAG_SYNC);
        Fragment inventary = fm.findFragmentByTag(TAG_INVENTARY);
        Fragment settings  = fm.findFragmentByTag(TAG_SETTINGS);
        Fragment about     = fm.findFragmentByTag(TAG_ABOUT);

        // El Fragment activo es el que no está oculto
        for (Fragment f : new Fragment[]{scan, sync, inventary, settings, about}) {
            if (f != null && !f.isHidden()) {
                activeFragment = f;
                break;
            }
        }
        if (activeFragment == null && scan != null) {
            activeFragment = scan;
        }
    }

    // -------------------------------------------------------------------------
    // Navegación
    // -------------------------------------------------------------------------

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if      (id == R.id.nav_scan)      showFragment(TAG_SCAN,      getString(R.string.menu_rfid_ingreso));
        else if (id == R.id.nav_sync)      showFragment(TAG_SYNC,      getString(R.string.menu_sync));
        else if (id == R.id.nav_inventary) showFragment(TAG_INVENTARY, getString(R.string.menu_inventary));
        else if (id == R.id.nav_settings)  showFragment(TAG_SETTINGS,  getString(R.string.menu_settings));
        else if (id == R.id.nav_about)     showFragment(TAG_ABOUT,     getString(R.string.menu_about));

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Oculta el Fragment activo y muestra el solicitado.
     * El Fragment anterior queda vivo en memoria — su estado se preserva.
     *
     * @param fragmentTag tag del Fragment a mostrar
     * @param title       título a mostrar en la Toolbar
     */
    private void showFragment(String fragmentTag, String title) {
        Fragment target = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (target == null || target == activeFragment) return;

        getSupportFragmentManager()
                .beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();

        activeFragment = target;

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
            boolean ok      = mRfidManager.connectCom(port, baudrate);
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
            // El trigger solo actúa si ScanFragment está visible
            ScanFragment scanFragment = getActiveScanFragment();
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        // Si no estamos en Scan, volver a Scan en lugar de salir
        if (!(activeFragment instanceof ScanFragment)) {
            showFragment(TAG_SCAN, getString(R.string.menu_rfid_ingreso));
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

    /**
     * Retorna el ScanFragment solo si está actualmente visible.
     * El trigger físico no debe actuar si el usuario está en otra pantalla.
     */
    private ScanFragment getActiveScanFragment() {
        if (activeFragment instanceof ScanFragment) {
            return (ScanFragment) activeFragment;
        }
        return null;
    }
}
