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
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_scan, parent, false);
        return new ScanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        ScanModel model = mData.get(position);
        holder.tvEpc.setText(model.getEpc());
        holder.tvRssi.setText(model.getRssi());
        holder.tvCount.setText(String.valueOf(model.getCount()));
        holder.tvPc.setText(model.getPc());

        // Resaltar el item seleccionado
        holder.itemView.setSelected(model.isSelected());
        holder.itemView.setBackgroundColor(
                model.isSelected()
                        ? 0xFFE3F2FD   // azul claro
                        : 0xFFFFFFFF   // blanco
        );

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onItemClick(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class ScanViewHolder extends RecyclerView.ViewHolder {
        TextView tvEpc, tvRssi, tvCount, tvPc;

        ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEpc   = itemView.findViewById(R.id.tv_epc);
            tvRssi  = itemView.findViewById(R.id.tv_rssi);
            tvCount = itemView.findViewById(R.id.tv_count);
            tvPc    = itemView.findViewById(R.id.tv_pc);
        }
    }
}
