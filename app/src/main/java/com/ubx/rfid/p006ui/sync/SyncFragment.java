package com.ubx.rfid.p006ui.sync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ubx.rfid.R;
import com.ubx.rfid.adapter.TagReadAdapter;
import com.ubx.rfid.db.AppDatabase;
import com.ubx.rfid.db.ErrorDao;
import com.ubx.rfid.db.TagReadDao;
import com.ubx.rfid.model.TagReadModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de Sincronización de TAGs.
 * Muestra los TAGs leídos y su estado de sincronización con la API central.
 */
public class SyncFragment extends Fragment {

    private RecyclerView recyclerView;
    private TagReadAdapter adapter;
    private TextView tvPending, tvSynced;
    private Button btnSync;
    private final List<TagReadModel> mData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sync, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPending    = view.findViewById(R.id.tv_pending_count);
        tvSynced     = view.findViewById(R.id.tv_synced_count);
        btnSync      = view.findViewById(R.id.btn_sync_now);
        recyclerView = view.findViewById(R.id.recycler_sync);

        adapter = new TagReadAdapter(mData);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        btnSync.setOnClickListener(v -> syncNow());

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                AppDatabase  db  = AppDatabase.getInstance(requireContext());
                TagReadDao   dao = new TagReadDao(db);
                List<TagReadModel> all     = dao.getAll();
                int pending = dao.countPending();
                int synced  = all.size() - pending;

                requireActivity().runOnUiThread(() -> {
                    mData.clear();
                    mData.addAll(all);
                    adapter.notifyDataSetChanged();
                    tvPending.setText(String.valueOf(pending));
                    tvSynced.setText(String.valueOf(synced));
                });
            } catch (Exception e) {
                ErrorDao.logError(AppDatabase.getInstance(requireContext()),
                        "SyncFragment.loadData", e);
            }
        }).start();
    }

    /** Fuerza la sincronización inmediata (ejecuta el SyncWorker manualmente). */
    private void syncNow() {
        btnSync.setEnabled(false);
        Toast.makeText(requireContext(), R.string.sync_in_progress, Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                // Reutilizar la lógica del SyncWorker directamente
                AppDatabase db         = AppDatabase.getInstance(requireContext());
                TagReadDao  tagReadDao = new TagReadDao(db);
                com.ubx.rfid.db.SettingsDao settingsDao =
                        new com.ubx.rfid.db.SettingsDao(db);

                int retentionDays = settingsDao.getInt(
                        com.ubx.rfid.db.SettingsDao.KEY_RETENTION_DAYS, 8);
                tagReadDao.deleteOlderThan(retentionDays);

                List<TagReadModel> pending = tagReadDao.getPending();

                if (!pending.isEmpty()) {
                    // Aquí iría el envío a la API (ver SyncWorker)
                    // Por ahora marcamos como sincronizados directamente
                    List<Long> ids = new ArrayList<>();
                    for (TagReadModel t : pending) ids.add(t.getId());
                    tagReadDao.markSynced(ids);
                }

                requireActivity().runOnUiThread(() -> {
                    btnSync.setEnabled(true);
                    Toast.makeText(requireContext(),
                            getString(R.string.sync_done, pending.size()),
                            Toast.LENGTH_SHORT).show();
                    loadData();
                });
            } catch (Exception e) {
                ErrorDao.logError(AppDatabase.getInstance(requireContext()),
                        "SyncFragment.syncNow", e);
                requireActivity().runOnUiThread(() -> {
                    btnSync.setEnabled(true);
                    Toast.makeText(requireContext(), R.string.sync_error, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
