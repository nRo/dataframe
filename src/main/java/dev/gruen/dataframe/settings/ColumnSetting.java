package dev.gruen.dataframe.settings;

/**
 * Base class for settings that can be attached to a column.
 * <p>
 * Settings are stored in {@link ColumnSettings} by their exact class.
 * Setting implementations should therefore be final and must not be subclassed,
 * otherwise they can not be found by the base class.
 */
public abstract class ColumnSetting {

}
