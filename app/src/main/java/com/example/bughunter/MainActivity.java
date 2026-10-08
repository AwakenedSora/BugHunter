package com.example.bughunter;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.os.BundleCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bughunter.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int DEFAULT_SEVERITY = 3;
    private static final String STATE_BUGS = "state_bugs";
    private static final String STATE_NEXT_NUMBER = "state_next_number";

    private ActivityMainBinding binding;
    private ArrayList<Bug> bugs = new ArrayList<>();
    private BugAdapter adapter;
    /** Próximo número de código; nunca é reaproveitado após uma exclusão. */
    private int nextBugNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            ArrayList<Bug> saved = BundleCompat.getParcelableArrayList(
                    savedInstanceState, STATE_BUGS, Bug.class);
            if (saved != null) bugs = saved;
            nextBugNumber = savedInstanceState.getInt(STATE_NEXT_NUMBER, 1);
        }

        setupRecyclerView();
        setupSeverityControl();
        clearErrorWhenEdited(binding.editTitle, binding.inputLayoutTitle);
        clearErrorWhenEdited(binding.editModule, binding.inputLayoutModule);
        clearErrorWhenEdited(binding.editDescription, binding.inputLayoutDescription);
        binding.buttonRegister.setOnClickListener(v -> registerBug());

        updateDashboard();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_BUGS, bugs);
        outState.putInt(STATE_NEXT_NUMBER, nextBugNumber);
    }

    private void setupRecyclerView() {
        adapter = new BugAdapter(bugs, this::toggleBugStatus, this::removeBug);
        binding.recyclerBugs.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerBugs.setAdapter(adapter);
    }

    private void setupSeverityControl() {
        updateSeverityLabel(binding.seekSeverity.getProgress());
        binding.seekSeverity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSeverityLabel(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateSeverityLabel(int severity) {
        binding.textSeverityValue.setText(getString(R.string.severity_value,
                severity, getString(Bug.labelFor(severity))));
        int color = ContextCompat.getColor(this, Bug.colorFor(severity));
        binding.textSeverityValue.setBackgroundTintList(ColorStateList.valueOf(color));
        binding.seekSeverity.setProgressTintList(ColorStateList.valueOf(color));
        binding.seekSeverity.setThumbTintList(ColorStateList.valueOf(color));
    }

    // ---------- Cadastro ----------

    private void registerBug() {
        if (!validateForm()) {
            return;
        }

        Bug bug = new Bug(
                getString(R.string.bug_code_format, nextBugNumber++),
                textOf(binding.editTitle),
                textOf(binding.editModule),
                textOf(binding.editDescription),
                binding.seekSeverity.getProgress());

        bugs.add(bug);
        int position = bugs.size() - 1;
        adapter.notifyItemInserted(position);
        binding.recyclerBugs.smoothScrollToPosition(position);

        clearForm();
        updateDashboard();
        showMessage(getString(R.string.message_registered, bug.getCode()));
    }

    private boolean validateForm() {
        View firstInvalid = null;

        if (textOf(binding.editTitle).isEmpty()) {
            binding.inputLayoutTitle.setError(getString(R.string.error_title_required));
            firstInvalid = binding.editTitle;
        } else {
            binding.inputLayoutTitle.setError(null);
        }

        if (textOf(binding.editModule).isEmpty()) {
            binding.inputLayoutModule.setError(getString(R.string.error_module_required));
            if (firstInvalid == null) firstInvalid = binding.editModule;
        } else {
            binding.inputLayoutModule.setError(null);
        }

        if (textOf(binding.editDescription).length() < MIN_DESCRIPTION_LENGTH) {
            binding.inputLayoutDescription.setError(
                    getString(R.string.error_description_short, MIN_DESCRIPTION_LENGTH));
            if (firstInvalid == null) firstInvalid = binding.editDescription;
        } else {
            binding.inputLayoutDescription.setError(null);
        }

        if (firstInvalid != null) {
            firstInvalid.requestFocus();
            return false;
        }
        return true;
    }

    private void clearForm() {
        binding.editTitle.setText(null);
        binding.editModule.setText(null);
        binding.editDescription.setText(null);
        binding.seekSeverity.setProgress(DEFAULT_SEVERITY);
        binding.inputLayoutTitle.setError(null);
        binding.inputLayoutModule.setError(null);
        binding.inputLayoutDescription.setError(null);

        View focused = getCurrentFocus();
        if (focused != null) {
            focused.clearFocus();
            InputMethodManager imm = getSystemService(InputMethodManager.class);
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }

    // ---------- Eventos da lista ----------

    /** Toque: alterna entre aberto e corrigido e atualiza só aquele cartão. */
    private void toggleBugStatus(Bug bug) {
        int position = bugs.indexOf(bug);
        if (position == -1) return;

        bug.toggleFixed();
        adapter.notifyItemChanged(position);
        updateDashboard();

        showMessage(getString(bug.isFixed() ? R.string.message_fixed : R.string.message_reopened,
                bug.getCode(), bug.getPointValue()));
    }

    /** Toque longo: remove o registro da lista. */
    private void removeBug(Bug bug) {
        int position = bugs.indexOf(bug);
        if (position == -1) return;

        bugs.remove(position);
        adapter.notifyItemRemoved(position);
        updateDashboard();

        showMessage(getString(R.string.message_removed, bug.getCode(), bug.getTitle()));
    }

    // ---------- Painel ----------

    private void updateDashboard() {
        int fixedCount = 0;
        int score = 0;
        for (Bug bug : bugs) {
            if (bug.isFixed()) fixedCount++;
            score += bug.getEarnedPoints();
        }

        binding.textStatTotal.setText(String.valueOf(bugs.size()));
        binding.textStatOpen.setText(String.valueOf(bugs.size() - fixedCount));
        binding.textStatFixed.setText(String.valueOf(fixedCount));
        binding.textStatScore.setText(String.valueOf(score));

        binding.textEmptyList.setVisibility(bugs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ---------- Utilitários ----------

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    private void clearErrorWhenEdited(EditText field, TextInputLayout layout) {
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.getError() != null) {
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private static String textOf(EditText field) {
        CharSequence text = field.getText();
        return TextUtils.isEmpty(text) ? "" : text.toString().trim();
    }
}
