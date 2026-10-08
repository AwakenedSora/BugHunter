package com.example.bughunter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Falha registrada pelo caçador de bugs. Parcelable para que a lista
 * sobreviva à rotação da tela.
 */
public class Bug implements Parcelable {

    public static final int MIN_SEVERITY = 1;
    public static final int MAX_SEVERITY = 5;
    private static final int POINTS_PER_SEVERITY = 10;

    private final String code;
    private final String title;
    private final String module;
    private final String description;
    private final int severity;
    private boolean fixed;
    @DrawableRes
    private final int imageResId;

    public Bug(String code, String title, String module, String description, int severity) {
        this.code = code;
        this.title = title;
        this.module = module;
        this.description = description;
        this.severity = severity;
        this.fixed = false;
        this.imageResId = imageFor(severity);
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getModule() {
        return module;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public void toggleFixed() {
        fixed = !fixed;
    }

    @DrawableRes
    public int getImageResId() {
        return imageResId;
    }

    /** Pontos que este bug vale quando corrigido: gravidade × 10. */
    public int getPointValue() {
        return severity * POINTS_PER_SEVERITY;
    }

    /** Pontos concedidos agora (zero enquanto estiver aberto). */
    public int getEarnedPoints() {
        return fixed ? getPointValue() : 0;
    }

    // ---------- Classificação da gravidade ----------
    // 1–2: baixa · 3: média · 4: alta · 5: crítica

    @DrawableRes
    public static int imageFor(int severity) {
        if (severity <= 2) return R.drawable.ic_bug_low;
        if (severity == 3) return R.drawable.ic_bug_medium;
        return R.drawable.ic_bug_high;
    }

    @ColorRes
    public static int colorFor(int severity) {
        if (severity <= 2) return R.color.severity_low;
        if (severity == 3) return R.color.severity_medium;
        return R.color.severity_high;
    }

    @StringRes
    public static int labelFor(int severity) {
        if (severity <= 2) return R.string.severity_low;
        if (severity == 3) return R.string.severity_medium;
        if (severity == 4) return R.string.severity_high;
        return R.string.severity_critical;
    }

    // ---------- Parcelable ----------

    protected Bug(Parcel in) {
        code = in.readString();
        title = in.readString();
        module = in.readString();
        description = in.readString();
        severity = in.readInt();
        fixed = in.readInt() == 1;
        imageResId = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(title);
        dest.writeString(module);
        dest.writeString(description);
        dest.writeInt(severity);
        dest.writeInt(fixed ? 1 : 0);
        dest.writeInt(imageResId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Bug> CREATOR = new Creator<Bug>() {
        @Override
        public Bug createFromParcel(Parcel in) {
            return new Bug(in);
        }

        @Override
        public Bug[] newArray(int size) {
            return new Bug[size];
        }
    };
}
