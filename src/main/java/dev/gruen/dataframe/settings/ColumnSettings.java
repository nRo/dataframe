package dev.gruen.dataframe.settings;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the {@link ColumnSetting settings} of a single column.
 * At most one setting per setting class is stored, adding a setting replaces an existing one of the same class.
 * <p>
 * This class is not thread safe. Settings should be configured before a data frame is shared.
 */
public class ColumnSettings {
    private final Map<Class<? extends ColumnSetting>, ColumnSetting> settingMap = new LinkedHashMap<>();

    public ColumnSettings() {

    }

    /**
     * Removes a setting if exactly this setting instance is stored.
     *
     * @param setting setting to remove
     * @param <T>     setting type
     * @return the removed setting or <code>null</code> if the setting was not stored
     */
    public <T extends ColumnSetting> T remove(T setting) {
        if (settingMap.get(setting.getClass()) == setting) {
            settingMap.remove(setting.getClass());
            return setting;
        }
        return null;
    }

    public <T extends ColumnSetting> T remove(Class<T> cl) {
        Object o = settingMap.remove(cl);
        if (!cl.isInstance(o)) {
            return null;
        }
        return cl.cast(o);
    }

    /**
     * Adds all settings of this instance to another settings instance.
     *
     * @param settings target settings
     */
    public void applyTo(ColumnSettings settings) {
        settingMap.values().forEach(settings::add);
    }

    public void add(ColumnSetting setting) {
        settingMap.put(setting.getClass(), setting);
    }

    public <T extends ColumnSetting> T get(Class<T> cl) {
        Object o = settingMap.get(cl);
        if (cl.isInstance(o)) {
            return cl.cast(o);
        }
        return null;
    }

    public <T extends ColumnSetting> T getOrDefault(Class<T> cl, T defaultSetting) {
        T v = get(cl);
        return v == null ? defaultSetting : v;
    }

    public boolean isEmpty() {
        return settingMap.isEmpty();
    }

    public ColumnSettings copy() {
        ColumnSettings copy = new ColumnSettings();
        applyTo(copy);
        return copy;
    }

    public static ColumnSettings create(Collection<? extends ColumnSetting> settings) {
        ColumnSettings columnSettings = new ColumnSettings();
        settings.forEach(columnSettings::add);
        return columnSettings;
    }
}
