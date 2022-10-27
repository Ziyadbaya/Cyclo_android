package com.dev.cyclo.MapData;

import androidx.annotation.NonNull;

/**
 * Interface to receive by async task the data from the Parser (Overview or Points Parser)
 */
public interface TaskLoadedCallback {
    void onTaskDone(@NonNull Object... values);
}
