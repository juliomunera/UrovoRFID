package com.ubx.rfid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ubx.rfid.R;
import com.ubx.rfid.model.TagReadModel;

import java.util.List;

/** Adapter para la lista de TAGs en la pantalla de Sincronización. */
public class TagReadAdapter extends RecyclerView.Adapter<TagReadAdapter.VH> {

    private final List<TagReadModel> data;

    public TagReadAdapter(List<TagReadModel> data) {
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag_read, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TagReadModel m = data.get(pos);
        h.tvTagId.setText(m.getTagId());
        h.tvDate.setText(m.getReadDate());
        h.tvStatus.setText(m.isSynced() ? "✓ Sincronizado" : "⏳ Pendiente");
        h.tvStatus.setTextColor(m.isSynced() ? 0xFF2E7D32 : 0xFFE65100);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTagId, tvDate, tvStatus;
        VH(@NonNull View v) {
            super(v);
            tvTagId  = v.findViewById(R.id.tv_tag_id);
            tvDate   = v.findViewById(R.id.tv_read_date);
            tvStatus = v.findViewById(R.id.tv_sync_status);
        }
    }
}
