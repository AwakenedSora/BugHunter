package com.example.bughunter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bughunter.databinding.ItemBugBinding;

/**
 * Representa um cartão da lista. Como o cartão é reciclado, bind() redefine
 * todos os textos, imagens, cores e estados visuais a cada chamada.
 */
public class BugViewHolder extends RecyclerView.ViewHolder {

    private final ItemBugBinding binding;

    public BugViewHolder(@NonNull ItemBugBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Bug bug,
                     BugAdapter.OnBugClickListener clickListener,
                     BugAdapter.OnBugLongClickListener longClickListener) {
        Context context = itemView.getContext();
        int severityColor = ContextCompat.getColor(context, Bug.colorFor(bug.getSeverity()));
        String severityLabel = context.getString(Bug.labelFor(bug.getSeverity()));

        // Textos
        binding.textBugCode.setText(bug.getCode());
        binding.textBugTitle.setText(bug.getTitle());
        binding.textBugModule.setText(context.getString(R.string.bug_module, bug.getModule()));
        binding.textBugDescription.setText(bug.getDescription());
        binding.textBugSeverity.setText(
                context.getString(R.string.bug_severity_badge, severityLabel, bug.getSeverity()));
        binding.textBugSeverity.setBackgroundTintList(ColorStateList.valueOf(severityColor));

        // Status: aberto ou corrigido
        boolean fixed = bug.isFixed();
        int statusColor = ContextCompat.getColor(context,
                fixed ? R.color.status_fixed : R.color.status_open);
        binding.textBugStatus.setText(fixed ? R.string.status_fixed : R.string.status_open);
        binding.textBugStatus.setBackgroundTintList(ColorStateList.valueOf(statusColor));

        binding.imageBugIcon.setImageResource(fixed ? R.drawable.ic_bug_fixed : bug.getImageResId());
        binding.imageBugIcon.setContentDescription(fixed
                ? context.getString(R.string.cd_bug_fixed)
                : context.getString(R.string.cd_bug_open, severityLabel));

        binding.viewSeverityStripe.setBackgroundColor(fixed ? statusColor : severityColor);
        binding.cardBug.setCardBackgroundColor(ContextCompat.getColor(context,
                fixed ? R.color.card_fixed : R.color.card_open));
        binding.cardBug.setStrokeColor(ContextCompat.getColor(context,
                fixed ? R.color.card_fixed_stroke : R.color.card_stroke));

        int titleColor = ContextCompat.getColor(context, fixed ? R.color.text_fixed : R.color.ink);
        int bodyColor = ContextCompat.getColor(context, fixed ? R.color.text_fixed : R.color.ink_soft);
        binding.textBugTitle.setTextColor(titleColor);
        binding.textBugModule.setTextColor(bodyColor);
        binding.textBugDescription.setTextColor(bodyColor);

        int titleFlags = binding.textBugTitle.getPaintFlags();
        binding.textBugTitle.setPaintFlags(fixed
                ? titleFlags | Paint.STRIKE_THRU_TEXT_FLAG
                : titleFlags & ~Paint.STRIKE_THRU_TEXT_FLAG);

        // Eventos: encaminham o próprio objeto Bug, nunca uma posição guardada
        binding.cardBug.setOnClickListener(v -> clickListener.onBugClick(bug));
        binding.cardBug.setOnLongClickListener(v -> {
            longClickListener.onBugLongClick(bug);
            return true;
        });
    }
}
