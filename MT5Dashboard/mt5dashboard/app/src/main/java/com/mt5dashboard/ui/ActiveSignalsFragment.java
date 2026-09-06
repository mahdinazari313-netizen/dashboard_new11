package com.mt5dashboard.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mt5dashboard.MT5DashboardApplication;
import com.mt5dashboard.R;
import com.mt5dashboard.core.Signal;
import com.mt5dashboard.core.SignalStateManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * صفحه اول (بخش ۱۸ سند): نمایش خام و زنده سیگنال‌های فعال، کاملاً مستقل از سناریوها.
 *
 * دو منبع به‌روزرسانی دارد:
 *   1) Event-driven: هر بار SignalStateManager سیگنال جدیدی دریافت کند (onSignalStateChanged).
 *   2) Time-driven: هر ۳۰ ثانیه یک تیک برای بررسی انقضای Display Duration
 *      (چون ممکن است هیچ سیگنال جدیدی نیاید ولی زمان نمایش قبلی‌ها تمام شده باشد).
 *
 * فیلتر Display Duration اینجا و فقط اینجا اعمال می‌شود (بخش ۱۹ سند: کاملاً مستقل از Scenario).
 */
public class ActiveSignalsFragment extends Fragment implements SignalStateManager.Listener {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private SymbolCardAdapter adapter;
    private AppSettings appSettings;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshTick = new Runnable() {
        @Override
        public void run() {
            refreshList();
            handler.postDelayed(this, 30_000L);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_active_signals, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        emptyStateText = root.findViewById(R.id.emptyStateText);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SymbolCardAdapter();
        recyclerView.setAdapter(adapter);
        appSettings = new AppSettings(requireContext());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        MT5DashboardApplication.getInstance().getSignalStateManager().addListener(this);
        refreshList();
        handler.postDelayed(refreshTick, 30_000L);
    }

    @Override
    public void onPause() {
        super.onPause();
        MT5DashboardApplication.getInstance().getSignalStateManager().removeListener(this);
        handler.removeCallbacks(refreshTick);
    }

    @Override
    public void onSignalStateChanged(Signal newOrUpdatedSignal) {
        // ممکن است از Thread سیستم (NotificationListenerService) صدا زده شود
        handler.post(this::refreshList);
    }

    private void refreshList() {
        if (getContext() == null) return;

        long now = System.currentTimeMillis();
        long displayDurationMillis = appSettings.getDisplayDurationMinutes() * 60_000L;

        SignalStateManager manager = MT5DashboardApplication.getInstance().getSignalStateManager();
        List<Signal> all = manager.getAllSignals();

        // فقط سیگنال‌هایی که هنوز در بازه Display Duration هستند (بخش ۱۹)
        Map<String, List<Signal>> bySymbol = new LinkedHashMap<>();
        for (Signal s : all) {
            boolean displayable = now < s.getReceivedAt() + displayDurationMillis;
            if (!displayable) continue;
            bySymbol.computeIfAbsent(s.getSymbol(), k -> new ArrayList<>()).add(s);
        }

        List<SymbolCardAdapter.SymbolGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<Signal>> entry : bySymbol.entrySet()) {
            groups.add(new SymbolCardAdapter.SymbolGroup(entry.getKey(), entry.getValue()));
        }

        adapter.submitGroups(groups);
        boolean isEmpty = groups.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
