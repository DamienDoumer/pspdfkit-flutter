package com.pspdfkit.flutter.pspdfkit;

import android.os.Bundle;

import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pspdfkit.annotations.measurements.MeasurementPrecision;
import com.pspdfkit.annotations.measurements.Scale;
import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.ui.PdfActivity;

import java.util.concurrent.atomic.AtomicReference;

import io.flutter.plugin.common.MethodChannel.Result;

/**
 * For communication with the PSPDFKit plugin, we keep a static reference to the current
 * activity.
 */
public class FlutterPdfActivity extends PdfActivity {

    @Nullable private static FlutterPdfActivity currentActivity;
    @NonNull private static final AtomicReference<Result> loadedDocumentResult = new AtomicReference<>();

    @Nullable private static Scale scale;
    @Nullable private static MeasurementPrecision floatPrecision;

    public static void setLoadedDocumentResult(Result result) {
        loadedDocumentResult.set(result);
    }

    public static void setMeasurementScale(@Nullable  final Scale scale) {
        FlutterPdfActivity.scale = scale;
    }

    public static void setFloatPrecision(@Nullable final MeasurementPrecision floatPrecision) {
        FlutterPdfActivity.floatPrecision = floatPrecision;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        EventDispatcher.getInstance().notifyActivityOnCreate();
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bindActivity();
    }

    @Override
    protected void onPause() {
        // Notify the Flutter PSPDFKit plugin that the activity is going to enter the onPause state.
        EventDispatcher.getInstance().notifyActivityOnPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        EventDispatcher.getInstance().notifyActivityOnResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventDispatcher.getInstance().notifyActivityOnDestroy();
        releaseActivity();
    }

    @Override
    public void onDocumentLoaded(@NonNull PdfDocument pdfDocument) {
        super.onDocumentLoaded(pdfDocument);
        // Notify the Flutter PSPDFKit plugin that the document has been loaded.
        EventDispatcher.getInstance().notifyDocumentLoaded(pdfDocument);

        Result result = loadedDocumentResult.getAndSet(null);
        if (result != null) {
            result.success(true);
        }

        if (scale != null) {
            pdfDocument.setMeasurementScale(scale);
        }

        if (floatPrecision != null) {
            pdfDocument.setMeasurementPrecision(floatPrecision);
        }
    }

    @Override
    public void onDocumentLoadFailed(@NonNull Throwable throwable) {
        super.onDocumentLoadFailed(throwable);
        Result result = loadedDocumentResult.getAndSet(null);
        if (result != null) {
            result.success(false);
        }
    }

    private void bindActivity() {
        currentActivity = this;
    }

    private void releaseActivity() {
        Result result = loadedDocumentResult.getAndSet(null);
        if (result != null) {
            result.success(false);
        }
        currentActivity = null;
    }

    @Nullable
    public static FlutterPdfActivity getCurrentActivity() {
        return currentActivity;
    }
}
