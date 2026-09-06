package com.mt5dashboard.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mt5dashboard.MT5DashboardApplication;
import com.mt5dashboard.R;
import com.mt5dashboard.core.Timeframe;
import com.mt5dashboard.scenario.ScenarioConfig;

/**
 * فرم ساخت سناریوی جدید. طبق تصمیم صریح کاربر، تمام اینپوت‌های بخش ۱۱ سند
 * اینجا جمع شده‌اند (هیچ تنظیمی خارج از سناریو نیست):
 *   - Main Time Frame (فعال/غیرفعال + انتخاب تایم‌فریم)
 *   - Price Difference (فعال/غیرفعال)
 *   - Signal Validity Multiplier
 *   - Signal Repeat Interval
 *
 * پس از ذخیره، طبق بخش ۱۲ سند، سناریو بلافاصله و خودکار فعال می‌شود
 * (نیازی به دکمه ON/OFF جداگانه نیست).
 */
public class CreateScenarioActivity extends AppCompatActivity {

    private EditText inputName;
    private SwitchMaterial switchMainTimeFrame;
    private Spinner spinnerMainTimeframe;
    private SwitchMaterial switchPriceDifference;
    private EditText inputValidityMultiplier;
    private EditText inputRepeatInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_scenario);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        inputName = findViewById(R.id.inputName);
        switchMainTimeFrame = findViewById(R.id.switchMainTimeFrame);
        spinnerMainTimeframe = findViewById(R.id.spinnerMainTimeframe);
        switchPriceDifference = findViewById(R.id.switchPriceDifference);
        inputValidityMultiplier = findViewById(R.id.inputValidityMultiplier);
        inputRepeatInterval = findViewById(R.id.inputRepeatInterval);

        setupTimeframeSpinner();

        switchMainTimeFrame.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                spinnerMainTimeframe.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        findViewById(R.id.buttonSave).setOnClickListener(v -> saveScenario());
    }

    private void setupTimeframeSpinner() {
        ArrayAdapter<Timeframe> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, Timeframe.values());
        spinnerMainTimeframe.setAdapter(adapter);
    }

    private void saveScenario() {
        String name = inputName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.scenario_name_required_error, Toast.LENGTH_SHORT).show();
            return;
        }

        int validityMultiplier = parsePositiveIntOrDefault(inputValidityMultiplier, 10);
        int repeatInterval = parsePositiveIntOrDefault(inputRepeatInterval, 10);

        ScenarioConfig config = new ScenarioConfig(name);
        config.setValidityMultiplier(validityMultiplier);
        config.setRepeatIntervalMinutes(repeatInterval);
        config.setMainTimeFrameEnabled(switchMainTimeFrame.isChecked());
        if (switchMainTimeFrame.isChecked()) {
            Timeframe selected = (Timeframe) spinnerMainTimeframe.getSelectedItem();
            config.setMainTimeframe(selected);
        }
        config.setPriceDifferenceEnabled(switchPriceDifference.isChecked());

        MT5DashboardApplication.getInstance().getScenarioEngineManager().createScenario(config);

        setResult(RESULT_OK);
        finish();
    }

    private int parsePositiveIntOrDefault(EditText field, int defaultValue) {
        String text = field.getText().toString().trim();
        if (text.isEmpty()) return defaultValue;
        try {
            int value = Integer.parseInt(text);
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
