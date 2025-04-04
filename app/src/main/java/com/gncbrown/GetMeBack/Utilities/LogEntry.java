package com.gncbrown.GetMeBack.Utilities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class LogEntry implements Parcelable {
    private String entry;
    private String entryDate;
    private Logger.LogLevel level;


    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public Logger.LogLevel getLevel() {
        return level;
    }

    public void setLevel(Logger.LogLevel level) {
        this.level = level;
    }

    protected LogEntry(Parcel in) {
        String[] data = new String[3];

        in.readStringArray(data);
        this.entry = (data[0] != null ? data[0].trim() : null);
        this.entryDate = (data[1] != null ? data[1].trim() : null);
        this.level = (data[2] != null ? Logger.LogLevel.valueOf(data[2]) : null);
    }

    public LogEntry(String entry, String entryDate, Logger.LogLevel level) {
        this.entry = entry;
        this.entryDate = entryDate;
        this.level = level;
    }

    public LogEntry(String entry, String entryDate) {
        this.entry = entry;
        this.entryDate = entryDate;
        this.level = Logger.DEFAULT_LOG_LEVEL;
    }

    public LogEntry(String entry) {
        this.entry = entry;
        this.entryDate = Utils.getDateTime();
        this.level = Logger.DEFAULT_LOG_LEVEL;
    }

    public static final Creator<LogEntry> CREATOR = new Creator<LogEntry>() {
        @Override
        public LogEntry createFromParcel(Parcel in) {
            return new LogEntry(in);
        }

        @Override
        public LogEntry[] newArray(int size) {
            return new LogEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(entry);
        dest.writeString(entryDate);
        dest.writeString(level.name());
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("[%s]%s: %s", entryDate, level, entry);
    }

    public String fancyString(boolean colorize) {
        if (!colorize)
            return toString();
        return Utils.getStartTag(level) + toString() + Utils.getEndTag(level);
    }
}
