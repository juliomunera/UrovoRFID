package com.ubx.rfid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ubx.rfid.R;
import com.ubx.rfid.p006ui.scan.ScanModel;

import java.util.List;

/**
 * Adapter para la lista de TAGs RFID escaneados.
 *
 * Columna EPC/Descripción:
 *  - Si el TAG está en el inventario local → muestra la descripción (texto normal, azul oscuro)
 *  - Si el TAG NO está en el inventario    → muestra el EPC raw (fuente monospace, gris)
 */
public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ScanViewHolder> {

    private final Context mContext;
    private final List<ScanModel> mData;
    private final OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ScanAdapter(Context context, List<ScanModel> data, OnItemClickListener listener) {
        mContext  = context;
        mData     = data;
        mListener = listener;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_item_scan, parent, false);
        return new ScanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        ScanModel model = mData.get(position);

        // --- Columna EPC/Descripción ---
        if (model.hasDescription()) {
            // TAG catalogado: mostrar descripción en texto normal + EPC pequeño debajo
            holder.tvEpc.setText(model.getDisplayText());
            holder.tvEpc.setTextColor(0xFF1A237E);      // azul índigo
            holder.tvEpc.setTextSize(13f);
            holder.tvSubEpc.setText(model.getEpc());
            holder.tvSubEpc.setVisibility(View.VISIBLE);
        } else {
            // TAG no catalogado: mostrar EPC en monospace
            holder.tvEpc.setText(model.getEpc());
            holder.tvEpc.setTextColor(0xFF424242);      // gris oscuro
            holder.tvEpc.setTextSize(12f);
            holder.tvSubEpc.setVisibility(View.GONE);
        }

        holder.tvRssi.setText(model.getRssi());
        holder.tvCount.setText(String.valueOf(model.getCount()));

        // Resaltar item seleccionado
        holder.itemView.setBackgroundColor(
                model.isSelected() ? 0xFFE3F2FD : 0xFFFFFFFF);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onItemClick(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class ScanViewHolder extends RecyclerView.ViewHolder {
        TextView tvEpc;      // descripción o EPC
        TextView tvSubEpc;   // EPC pequeño debajo de la descripción (solo si catalogado)
        TextView tvRssi;
        TextView tvCount;

        ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEpc    = itemView.findViewById(R.id.tv_epc);
            tvSubEpc = itemView.findViewById(R.id.tv_sub_epc);
            tvRssi   = itemView.findViewById(R.id.tv_rssi);
            tvCount  = itemView.findViewById(R.id.tv_count);
        }
    }
}
