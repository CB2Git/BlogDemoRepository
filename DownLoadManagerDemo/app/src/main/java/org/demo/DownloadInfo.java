package org.demo;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;

public class DownloadInfo {

    /**
     * 任务id
     */
    private long id;

    /**
     * 下载地址
     */
    private String uri;

    /**
     * 本地文件路径 /mnt/sdcard/Download/weixin_1080.apk
     */
    private String local_filename;

    /**
     * 下载路径 file:///mnt/sdcard/Download/weixin_1080.apk
     * <p>
     * 下载成功以后才有值
     */
    private String hint;

    /**
     * 类似下面这样
     * content://media/external/file/11
     */
    private String mediaprovider_uri;

    /**
     * 下载状态
     * 失败 {@link android.app.DownloadManager#STATUS_FAILED}
     * 暂停 {@link android.app.DownloadManager#STATUS_PAUSED}
     * 准备 {@link android.app.DownloadManager#STATUS_PENDING}
     * 下载中 {@link android.app.DownloadManager#STATUS_RUNNING}
     * 成功 {@link android.app.DownloadManager#STATUS_SUCCESSFUL}
     */
    private int status;

    /**
     * 文件总大小
     */
    private long totalSize;

    /**
     * 已经下载的大小
     */
    private long bytes_so_far;

    /**
     * 失败原因
     */
    private String reason;


    /**
     * 任务title
     */
    private String title;

    /**
     * 任务description
     */
    private String description;

    /**
     * media_type，默认为application/vnd.android.package-archive
     */
    private String mediaType;

    /**
     * 最后一次修改的时间
     */
    private long lastModifiedTimestamp;

    private Context mContext;

    public DownloadInfo(Context context) {
        this.mContext = context;
    }

    public void query(int id) {
        this.id = id;
        DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //只能查询自己的下载记录，别人的无数据返回
        Cursor c = dm.query(new DownloadManager.Query().setFilterById(this.id));
        if (c.moveToFirst()) {
            this.uri = c.getString(c.getColumnIndex("uri"));
            this.local_filename = c.getString(c.getColumnIndex("local_filename"));
            this.hint = c.getString(c.getColumnIndex("hint"));
            this.status = c.getInt(c.getColumnIndex("status"));
            this.totalSize = c.getLong(c.getColumnIndex("total_size"));
            this.bytes_so_far = c.getLong(c.getColumnIndex("bytes_so_far"));
            this.reason = c.getString(c.getColumnIndex("reason"));
            this.title = c.getString(c.getColumnIndex("title"));
            this.description = c.getString(c.getColumnIndex("description"));
            this.mediaprovider_uri = c.getString(c.getColumnIndex("mediaprovider_uri"));
            this.mediaType = c.getString(c.getColumnIndex("media_type"));
            this.lastModifiedTimestamp = c.getLong(c.getColumnIndex("last_modified_timestamp"));
        }
        c.close();
    }

    /**
     * 获取下载百分比
     */
    public int getDownloadPercent() {
        if (this.totalSize == 0) {
            return 0;
        }
        return (int) (this.bytes_so_far * 100 / this.totalSize);
    }

    /**
     * 是否下载成功
     */
    public boolean isSuccessful() {
        return this.status == DownloadManager.STATUS_SUCCESSFUL;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLocal_filename() {
        return local_filename;
    }

    public void setLocal_filename(String local_filename) {
        this.local_filename = local_filename;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getMediaprovider_uri() {
        return mediaprovider_uri;
    }

    public void setMediaprovider_uri(String mediaprovider_uri) {
        this.mediaprovider_uri = mediaprovider_uri;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getBytes_so_far() {
        return bytes_so_far;
    }

    public void setBytes_so_far(long bytes_so_far) {
        this.bytes_so_far = bytes_so_far;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "id=" + id +
                ", uri='" + uri + '\'' +
                ", local_filename='" + local_filename + '\'' +
                ", hint='" + hint + '\'' +
                ", mediaprovider_uri='" + mediaprovider_uri + '\'' +
                ", status=" + status +
                ", totalSize=" + totalSize +
                ", bytes_so_far=" + bytes_so_far +
                ", reason='" + reason + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", lastModifiedTimestamp=" + lastModifiedTimestamp +
                '}';
    }
}
