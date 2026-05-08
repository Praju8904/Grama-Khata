package com.gramaKhata.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gramaKhata.R;
import com.gramaKhata.data.db.CustomerDao;
import com.gramaKhata.databinding.ItemCustomerBinding;
import com.gramaKhata.utils.AvatarUtils;
import com.gramaKhata.utils.CurrencyFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    public interface OnCustomerClickListener {
        void onClick(CustomerDao.CustomerWithBalance customer);
    }

    public interface OnCustomerLongClickListener {
        void onLongClick(CustomerDao.CustomerWithBalance customer);
    }

    private final List<CustomerDao.CustomerWithBalance> customers = new ArrayList<>();
    private final OnCustomerClickListener clickListener;
    private final OnCustomerLongClickListener longClickListener;

    public CustomerAdapter(
            List<CustomerDao.CustomerWithBalance> customers,
            OnCustomerClickListener clickListener,
            OnCustomerLongClickListener longClickListener
    ) {
        if (customers != null) {
            this.customers.addAll(customers);
        }
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomerBinding binding = ItemCustomerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CustomerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        holder.bind(customers.get(position), clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    public void updateList(List<CustomerDao.CustomerWithBalance> newList) {
        List<CustomerDao.CustomerWithBalance> safeNewList = newList == null
                ? Collections.emptyList()
                : new ArrayList<>(newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new CustomerDiffCallback(customers, safeNewList)
        );
        customers.clear();
        customers.addAll(safeNewList);
        diffResult.dispatchUpdatesTo(this);
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {

        private final ItemCustomerBinding binding;

        CustomerViewHolder(ItemCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(
                CustomerDao.CustomerWithBalance customer,
                OnCustomerClickListener clickListener,
                OnCustomerLongClickListener longClickListener
        ) {
            String name = customer.name == null ? "" : customer.name;
            String phone = customer.phone == null ? "" : customer.phone;
            double balance = customer.netBalance;

            binding.tvCustomerName.setText(name);
            binding.tvPhone.setText(phone);
            binding.tvNetBalance.setText(CurrencyFormatter.format(balance));

            int colorRes = R.color.colorTextPrimary;
            if (balance > 0) {
                colorRes = R.color.colorCreditGreen;
            } else if (balance < 0) {
                colorRes = R.color.colorDebitRed;
            }
            binding.tvNetBalance.setTextColor(
                    ContextCompat.getColor(binding.getRoot().getContext(), colorRes)
            );
            binding.tvDueBadge.setVisibility(balance > 0 ? View.VISIBLE : View.GONE);

            bindAvatar(name, customer.photoUri);

            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onClick(customer);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onLongClick(customer);
                    return true;
                }
                return false;
            });
        }

        private void bindAvatar(String name, String photoUri) {
            if (!TextUtils.isEmpty(photoUri)) {
                Glide.with(binding.ivAvatar.getContext())
                        .load(photoUri)
                        .circleCrop()
                        .into(binding.ivAvatar);
                return;
            }

            Glide.with(binding.ivAvatar.getContext()).clear(binding.ivAvatar);
            int sizePx = binding.ivAvatar.getLayoutParams() != null
                    && binding.ivAvatar.getLayoutParams().width > 0
                    ? binding.ivAvatar.getLayoutParams().width
                    : dpToPx(binding.ivAvatar.getContext(), 48);
            binding.ivAvatar.setImageBitmap(createInitialAvatar(name, sizePx));
        }

        private Bitmap createInitialAvatar(String name, int sizePx) {
            int safeSize = Math.max(sizePx, 1);
            Bitmap bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundPaint.setColor(AvatarUtils.getColorForName(name));
            canvas.drawCircle(safeSize / 2f, safeSize / 2f, safeSize / 2f, backgroundPaint);

            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(safeSize * 0.42f);
            textPaint.setFakeBoldText(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            float y = (safeSize / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
            canvas.drawText(AvatarUtils.getInitial(name), safeSize / 2f, y, textPaint);
            return bitmap;
        }

        private int dpToPx(Context context, int dp) {
            return Math.round(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            dp,
                            context.getResources().getDisplayMetrics()
                    )
            );
        }
    }

    static class CustomerDiffCallback extends DiffUtil.Callback {

        private final List<CustomerDao.CustomerWithBalance> oldList;
        private final List<CustomerDao.CustomerWithBalance> newList;

        CustomerDiffCallback(
                List<CustomerDao.CustomerWithBalance> oldList,
                List<CustomerDao.CustomerWithBalance> newList
        ) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).id == newList.get(newItemPosition).id;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            CustomerDao.CustomerWithBalance oldItem = oldList.get(oldItemPosition);
            CustomerDao.CustomerWithBalance newItem = newList.get(newItemPosition);
            return oldItem.id == newItem.id
                    && TextUtils.equals(oldItem.name, newItem.name)
                    && TextUtils.equals(oldItem.phone, newItem.phone)
                    && TextUtils.equals(oldItem.photoUri, newItem.photoUri)
                    && Double.compare(oldItem.netBalance, newItem.netBalance) == 0;
        }
    }
}
