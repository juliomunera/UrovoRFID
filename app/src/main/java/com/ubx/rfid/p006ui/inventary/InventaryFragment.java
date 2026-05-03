package com.ubx.rfid.p006ui.inventary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ubx.rfid.R;
import com.ubx.rfid.adapter.InventaryAdapter;
import com.ubx.rfid.db.AppDatabase;
import com.ubx.rfid.db.ErrorDao;
import com.ubx.rfid.db.InventaryDao;
import com.ubx.rfid.model.InventaryModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de Inventario local de TAGs.
 * Permite consultar, agregar y eliminar TAGs del catálogo local.
 */
public class InventaryFragment extends Fragment {

    private RecyclerView recyclerView;
    private InventaryAdapter adapter;
    private final List<InventaryModel> mData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_inventary);
        Button btnAdd = view.findViewById(R.id.btn_add_tag);

        adapter = new InventaryAdapter(mData, this::showDeleteDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddDialog());

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
                AppDatabase    db   = AppDatabase.getInstance(requireContext());
                InventaryDao   dao  = new InventaryDao(db);
                List<InventaryModel> list = dao.getAll();
                requireActivity().runOnUiThread(() -> {
                    mData.clear();
                    mData.addAll(list);
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                ErrorDao.logError(AppDatabase.getInstance(requireContext()),
                        "InventaryFragment.loadData", e);
            }
        }).start();
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_tag, null);
        EditText etTagId = dialogView.findViewById(R.id.et_tag_id);
        EditText etDesc  = dialogView.findViewById(R.id.et_description);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.inventary_add_title)
                .setView(dialogView)
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    String tagId = etTagId.getText().toString().trim();
                    String desc  = etDesc.getText().toString().trim();
                    if (TextUtils.isEmpty(tagId)) {
                        Toast.makeText(requireContext(),
                                R.string.inventary_tag_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new Thread(() -> {
                        AppDatabase  db  = AppDatabase.getInstance(requireContext());
                        InventaryDao dao = new InventaryDao(db);
                        dao.insertOrReplace(tagId, desc);
                        requireActivity().runOnUiThread(this::loadData);
                    }).start();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showDeleteDialog(InventaryModel item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.inventary_delete_title)
                .setMessage(getString(R.string.inventary_delete_msg, item.getTagId()))
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    new Thread(() -> {
                        AppDatabase  db  = AppDatabase.getInstance(requireContext());
                        InventaryDao dao = new InventaryDao(db);
                        dao.delete(item.getId());
                        requireActivity().runOnUiThread(this::loadData);
                    }).start();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
