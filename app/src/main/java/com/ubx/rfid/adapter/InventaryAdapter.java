package com.ubx.rfid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ubx.rfid.R;
import com.ubx.rfid.model.InventaryModel;

import java.util.List;

/** Adapter para la lista de ítems del Inventario. */
public class InventaryAdapter extends RecyclerView.Adapter<InventaryAdapter.VH> {

    public interface OnDeleteListener {
        void onDelete(InventaryModel item);
    }

    private final List<InventaryModel> data;
    private final OnDeleteListener     deleteListener;

    public InventaryAdapter(List<InventaryModel> data, OnDeleteListener listener) {
        this.data           = data;
        this.deleteListener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventary, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        InventaryModel m = data.get(pos);
        h.tvTagId.setText(m.getTagId());
        h.tvDesc.setText(m.getDescription());
        h.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(m);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView    tvTagId, tvDesc;
        ImageButton btnDelete;
        VH(@NonNull View v) {
            super(v);
            tvTagId   = v.findViewById(R.id.tv_inv_tag_id);
            tvDesc    = v.findViewById(R.id.tv_inv_description);
            btnDelete = v.findViewById(R.id.btn_inv_delete);
        }
    }
}
